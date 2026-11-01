package com.example.kancentration.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kancentration.data.firebase.DlcManager
import com.example.kancentration.data.firebase.FirebaseStorageDlcManager
import com.example.kancentration.data.model.BoardCard
import com.example.kancentration.data.model.BoardDimension
import com.example.kancentration.data.model.DlcPackItem
import com.example.kancentration.data.model.GameSettings
import com.example.kancentration.data.model.GameType
import com.example.kancentration.data.model.KanjiData
import com.example.kancentration.data.repository.KanjiRepository
import com.example.kancentration.data.repository.SettingsRepository
import com.example.kancentration.domain.GameEngine
import com.example.kancentration.domain.SoundManager
import com.example.kancentration.domain.SoundPlayer
import com.example.kancentration.domain.TimerManager
import com.example.kancentration.domain.TurnEvaluationResult
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Coordinates game rules, turn state transitions, and decoupled domain services.
 * Conforms to Sections 5.2, 5.4, and 5.5 of DESIGN_PLAN.md with complete input
 * locking, lifecycle-safe coroutine scope management, and armed lazy-clock initialization.
 */
class GameViewModel(
    private val repository: KanjiRepository,
    private val settingsRepository: SettingsRepository,
    val dlcManager: DlcManager,
    val soundManager: SoundPlayer,
    val timerManager: TimerManager,
    private val gameEngine: GameEngine = GameEngine(),
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    // Overload for testing and instantiation where TimerManager shares coroutineScope
    constructor(
        repository: KanjiRepository,
        settingsRepository: SettingsRepository,
        dlcManager: DlcManager,
        soundManager: SoundPlayer,
        coroutineScope: CoroutineScope,
        gameEngine: GameEngine = GameEngine()
    ) : this(
        repository = repository,
        settingsRepository = settingsRepository,
        dlcManager = dlcManager,
        soundManager = soundManager,
        timerManager = TimerManager(coroutineScope),
        gameEngine = gameEngine,
        coroutineScope = coroutineScope
    )

    // Platform application constructor
    constructor(application: Application) : this(
        repository = KanjiRepository(application),
        settingsRepository = SettingsRepository(application),
        dlcManager = FirebaseStorageDlcManager(application),
        soundManager = SoundManager(application),
        timerManager = TimerManager(CoroutineScope(Dispatchers.Default + SupervisorJob())),
        gameEngine = GameEngine()
    )

    private val rootScope: CoroutineScope = coroutineScope ?: viewModelScope

    // Game session scope bound to a child SupervisorJob that cleanly cancels on reset or dimension switch
    private var gameSessionJob: CompletableJob = SupervisorJob(rootScope.coroutineContext[Job])
    private var gameSessionScope: CoroutineScope = CoroutineScope(rootScope.coroutineContext + gameSessionJob)
    private var matchEvaluationJob: Job? = null

    private val _uiState = MutableStateFlow(
        GameUiState(settings = settingsRepository.loadSettings())
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        val initialSettings = settingsRepository.loadSettings()
        soundManager.isMuted = !initialSettings.soundEnabled
        startNewGame(initialSettings)
    }

    /**
     * Binds timer observation flows to the active game session scope.
     * These collectors are cleanly cancelled and recreated when a game resets or switches dimensions.
     */
    private fun observeTimer(sessionScope: CoroutineScope) {
        sessionScope.launch {
            timerManager.elapsedSeconds.collect { sec ->
                if (!_uiState.value.isTimeAttack) {
                    _uiState.update { it.copy(formattedTimer = timerManager.formatTime(sec)) }
                }
            }
        }
        sessionScope.launch {
            timerManager.remainingMillis.collect { millis ->
                if (_uiState.value.isTimeAttack) {
                    _uiState.update {
                        it.copy(
                            formattedTimer = timerManager.formatMillis(millis),
                            isTimerLowWarning = millis in 1L..15000L
                        )
                    }
                }
            }
        }
    }

    fun cycleBoardDimension() {
        val nextDimension = _uiState.value.settings.dimension.next()
        val newSettings = _uiState.value.settings.copy(dimension = nextDimension)
        settingsRepository.saveSettings(newSettings)
        startNewGame(newSettings)
    }

    fun applySettings(newSettings: GameSettings) {
        val currentSettings = _uiState.value.settings
        val requiresRestart = newSettings.dimension != currentSettings.dimension ||
                newSettings.gameType != currentSettings.gameType

        settingsRepository.saveSettings(newSettings)
        soundManager.isMuted = !newSettings.soundEnabled

        if (requiresRestart) {
            startNewGame(newSettings)
        } else {
            _uiState.update { it.copy(settings = newSettings) }
        }
    }

    /**
     * Resets the game state, cleanly cancels any in-flight coroutines from the previous session,
     * arms the timer (without ticking), and establishes a fresh session coroutine scope.
     */
    fun startNewGame(settings: GameSettings = _uiState.value.settings) {
        // Lifecycle Safety: Cancel all in-flight evaluation jobs and timer collectors from previous session
        matchEvaluationJob?.cancel()
        matchEvaluationJob = null
        gameSessionJob.cancel()

        // Create a new bound session job and scope
        val newSessionJob = SupervisorJob(rootScope.coroutineContext[Job])
        gameSessionJob = newSessionJob
        gameSessionScope = CoroutineScope(rootScope.coroutineContext + newSessionJob)

        timerManager.stop()
        val deck = repository.generateDeck(settings.dimension)

        val initialTimerStr = when (val mode = settings.gameType) {
            is GameType.TimeAttack -> timerManager.formatMillis(mode.durationMillis)
            else -> "00:00"
        }

        _uiState.update {
            GameUiState(
                cards = deck,
                settings = settings,
                activePlayer = 1,
                player1Score = 0,
                player2Score = 0,
                totalMoves = 0,
                totalMatches = 0,
                isGameOver = false,
                isTimeExpired = false,
                gameOverMessage = "",
                formattedTimer = initialTimerStr,
                isTimerLowWarning = false,
                firstFlippedCardId = null,
                secondFlippedCardId = null,
                isEvaluatingMatch = false,
                activeTwinTrapBanner = null,
                selectedKanjiForInspector = null,
                turnPhase = TurnPhase.IDLE_AWAITING_FIRST_MOVE
            )
        }

        // Arm Timer: Prepared but NOT started until the very first valid face-down card flip!
        when (val mode = settings.gameType) {
            is GameType.TimeAttack -> {
                timerManager.prepareCountdown(
                    totalMillis = mode.durationMillis,
                    onTickWarning = { soundManager.playTick() },
                    onTimeExpired = { handleTimeExpired() }
                )
            }
            else -> {
                timerManager.prepareStopwatch()
            }
        }

        // Rebind timer observation collectors to the fresh session scope
        observeTimer(gameSessionScope)
    }

    fun onOpenWiktionary() {
        if (timerManager.isRunning && !_uiState.value.isGameOver) {
            timerManager.pause()
        }
    }

    fun onPauseApp() {
        if (timerManager.isRunning && !_uiState.value.isGameOver) {
            timerManager.pause()
        }
    }

    fun onResumeApp() {
        val state = _uiState.value
        val hasStarted = state.turnPhase != TurnPhase.IDLE_AWAITING_FIRST_MOVE
        if (hasStarted && !state.isGameOver && timerManager.isPaused.value && !state.showSettingsDialog && !state.showDlcStoreDialog) {
            timerManager.resume()
        }
    }

    /**
     * Primary event handler tracking TurnPhase transitions and enforcing complete input locking.
     *
     * Rules:
     * 1. If card is already face-up or matched, immediately update inspector and return (inspection only).
     * 2. If isEvaluatingMatch is true (during the 1,150ms observation delay), immediately ignore the tap.
     * 3. If game is over, permit post-game inspection and reveal.
     * 4. Lazy-clock initialization triggers exclusively on the first valid face-down flip of a new game.
     */
    fun onCardClicked(cardId: String) {
        val state = _uiState.value
        val card = state.cards.find { it.instanceId == cardId } ?: return

        // Rule 1: Always allow inspecting cards that are already face-up or matched
        if (card.isFaceUp || card.isMatched) {
            _uiState.update { it.copy(selectedKanjiForInspector = card.kanji) }
            return
        }

        // Rule 2: Complete Input Locking during match evaluation delay
        if (state.isEvaluatingMatch || state.turnPhase == TurnPhase.EVALUATING_MATCH) {
            return
        }

        // Rule 3: Post-game tile reveal & inspection
        if (state.isGameOver) {
            val updatedCards = state.cards.map {
                if (it.instanceId == cardId) it.copy(isFaceUp = true) else it
            }
            _uiState.update {
                it.copy(
                    cards = updatedCards,
                    selectedKanjiForInspector = card.kanji
                )
            }
            soundManager.playFlip()
            return
        }

        // Rule 4: Lazy-clock initialization: Start timer flow exclusively on the very first valid face-down flip
        if (state.turnPhase == TurnPhase.IDLE_AWAITING_FIRST_MOVE) {
            if (state.isTimeAttack) {
                timerManager.startCountdown()
            } else {
                timerManager.startStopwatch()
            }
        }

        soundManager.playFlip()

        when (state.turnPhase) {
            TurnPhase.IDLE_AWAITING_FIRST_MOVE, TurnPhase.AWAITING_FIRST_CARD -> {
                // Reveal First Card
                val updatedCards = state.cards.map {
                    if (it.instanceId == cardId) it.copy(isFaceUp = true) else it
                }
                _uiState.update {
                    it.copy(
                        cards = updatedCards,
                        firstFlippedCardId = cardId,
                        secondFlippedCardId = null,
                        selectedKanjiForInspector = card.kanji,
                        turnPhase = TurnPhase.FIRST_CARD_REVEALED
                    )
                }
            }

            TurnPhase.FIRST_CARD_REVEALED -> {
                val firstId = state.firstFlippedCardId
                if (firstId == null || firstId == cardId) {
                    _uiState.update { it.copy(selectedKanjiForInspector = card.kanji) }
                    return
                }

                // Reveal Second Card & Transition to EVALUATING_MATCH with input lock
                val updatedCards = state.cards.map {
                    if (it.instanceId == cardId) it.copy(isFaceUp = true) else it
                }
                _uiState.update {
                    it.copy(
                        cards = updatedCards,
                        secondFlippedCardId = cardId,
                        totalMoves = it.totalMoves + 1,
                        isEvaluatingMatch = true,
                        selectedKanjiForInspector = card.kanji,
                        turnPhase = TurnPhase.EVALUATING_MATCH
                    )
                }

                evaluateMatch(firstId, cardId)
            }

            TurnPhase.EVALUATING_MATCH, TurnPhase.GAME_OVER -> {
                // Completely locked
            }
        }
    }

    /**
     * Overload accepting BoardCard directly from Compose UI components.
     */
    fun onCardClicked(card: BoardCard) {
        onCardClicked(card.instanceId)
    }

    private fun evaluateMatch(firstId: String, secondId: String) {
        matchEvaluationJob?.cancel()
        matchEvaluationJob = gameSessionScope.launch {
            val currentCards = _uiState.value.cards
            val card1 = currentCards.find { it.instanceId == firstId }
            val card2 = currentCards.find { it.instanceId == secondId }

            if (card1 != null && card2 != null) {
                val state = _uiState.value
                val result = gameEngine.evaluateFlip(
                    card1 = card1,
                    card2 = card2,
                    activePlayer = state.activePlayer,
                    player1Score = state.player1Score,
                    player2Score = state.player2Score,
                    totalMatches = state.totalMatches,
                    totalPairs = state.totalPairs,
                    settings = state.settings,
                    formattedTimer = state.formattedTimer,
                    totalMoves = state.totalMoves
                )

                when (result) {
                    is TurnEvaluationResult.Match -> {
                        delay(500L) // Brief delay for match confirmation
                        soundManager.playMatch()
                        val p = result.scoringPlayer
                        val updatedCards = _uiState.value.cards.map {
                            if (it.instanceId == firstId || it.instanceId == secondId) {
                                it.copy(
                                    isMatched = true,
                                    isFaceUp = true,
                                    matchedByPlayer = p,
                                    isTwinTrapHighlight = false
                                )
                            } else it
                        }

                        if (result.isGameOver) {
                            timerManager.stop()
                            soundManager.playVictory()
                        }

                        _uiState.update {
                            it.copy(
                                cards = updatedCards,
                                player1Score = result.newPlayer1Score,
                                player2Score = result.newPlayer2Score,
                                totalMatches = result.newTotalMatches,
                                activePlayer = result.nextPlayer,
                                isGameOver = result.isGameOver,
                                gameOverMessage = result.gameOverMessage,
                                firstFlippedCardId = null,
                                secondFlippedCardId = null,
                                isEvaluatingMatch = false,
                                activeTwinTrapBanner = null,
                                turnPhase = if (result.isGameOver) TurnPhase.GAME_OVER else TurnPhase.AWAITING_FIRST_CARD
                            )
                        }
                    }

                    is TurnEvaluationResult.TwinTrapMismatch -> {
                        val markedCards = _uiState.value.cards.map {
                            if (it.instanceId == firstId || it.instanceId == secondId) {
                                it.copy(isTwinTrapHighlight = true)
                            } else it
                        }
                        _uiState.update {
                            it.copy(
                                cards = markedCards,
                                activeTwinTrapBanner = result.bannerMessage
                            )
                        }

                        // Play distinct Twin-Trap anomaly alert immediately upon detection
                        soundManager.playTwinTrap()

                        // Observation delay of 1,150ms as specified in Section 5.2 & 5.5
                        delay(1150L)

                        val updatedCards = _uiState.value.cards.map {
                            if (it.instanceId == firstId || it.instanceId == secondId) {
                                it.copy(isFaceUp = false, isTwinTrapHighlight = false)
                            } else it
                        }

                        _uiState.update {
                            it.copy(
                                cards = updatedCards,
                                activePlayer = result.nextPlayer,
                                firstFlippedCardId = null,
                                secondFlippedCardId = null,
                                isEvaluatingMatch = false,
                                activeTwinTrapBanner = null,
                                turnPhase = TurnPhase.AWAITING_FIRST_CARD
                            )
                        }
                    }

                    is TurnEvaluationResult.StandardMismatch -> {
                        // Observation delay of 1,150ms as specified in Section 5.2 & 5.5
                        delay(1150L)
                        soundManager.playMismatch()

                        val updatedCards = _uiState.value.cards.map {
                            if (it.instanceId == firstId || it.instanceId == secondId) {
                                it.copy(isFaceUp = false, isTwinTrapHighlight = false)
                            } else it
                        }

                        _uiState.update {
                            it.copy(
                                cards = updatedCards,
                                activePlayer = result.nextPlayer,
                                firstFlippedCardId = null,
                                secondFlippedCardId = null,
                                isEvaluatingMatch = false,
                                activeTwinTrapBanner = null,
                                turnPhase = TurnPhase.AWAITING_FIRST_CARD
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleTimeExpired() {
        matchEvaluationJob?.cancel()
        soundManager.playMismatch()
        val score = _uiState.value.player1Score
        val total = _uiState.value.totalPairs
        _uiState.update {
            it.copy(
                isGameOver = true,
                isTimeExpired = true,
                gameOverMessage = "Time's Up! Matches Found: $score / $total",
                turnPhase = TurnPhase.GAME_OVER,
                isEvaluatingMatch = false
            )
        }
    }

    fun onKanjiInspected(kanji: KanjiData) {
        _uiState.update { it.copy(selectedKanjiForInspector = kanji) }
    }

    fun setShowSettingsDialog(show: Boolean) {
        if (show && timerManager.isRunning) {
            timerManager.pause()
        } else if (!show && timerManager.isPaused.value && !_uiState.value.isGameOver && _uiState.value.turnPhase != TurnPhase.IDLE_AWAITING_FIRST_MOVE) {
            timerManager.resume()
        }
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun setShowDlcStoreDialog(show: Boolean) {
        if (show && timerManager.isRunning) {
            timerManager.pause()
        } else if (!show && timerManager.isPaused.value && !_uiState.value.isGameOver && _uiState.value.turnPhase != TurnPhase.IDLE_AWAITING_FIRST_MOVE) {
            timerManager.resume()
        }
        _uiState.update { it.copy(showDlcStoreDialog = show) }
    }

    fun downloadDlcPack(pack: DlcPackItem) {
        rootScope.launch {
            dlcManager.downloadPack(pack) { success ->
                if (success) {
                    repository.loadInstalledDlcPacks()
                }
            }
        }
    }

    public override fun onCleared() {
        super.onCleared()
        matchEvaluationJob?.cancel()
        matchEvaluationJob = null
        gameSessionJob.cancel()
        timerManager.dispose()
        soundManager.release()
    }

    companion object {
        fun createFactory(
            repository: KanjiRepository,
            settingsRepository: SettingsRepository,
            timerManager: TimerManager,
            dlcManager: DlcManager,
            soundManager: SoundPlayer,
            gameEngine: GameEngine = GameEngine()
        ): ViewModelProvider.Factory = GameViewModelFactory(
            repository = repository,
            settingsRepository = settingsRepository,
            timerManager = timerManager,
            dlcManager = dlcManager,
            soundManager = soundManager,
            gameEngine = gameEngine
        )

        fun createDefaultFactory(application: Application): ViewModelProvider.Factory {
            val timerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
            return GameViewModelFactory(
                repository = KanjiRepository(application),
                settingsRepository = SettingsRepository(application),
                timerManager = TimerManager(timerScope),
                dlcManager = FirebaseStorageDlcManager(application),
                soundManager = SoundManager(application),
                gameEngine = GameEngine()
            )
        }
    }
}
