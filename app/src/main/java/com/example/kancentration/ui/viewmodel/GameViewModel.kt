package com.example.kancentration.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kancentration.data.firebase.FirebaseStorageDlcManager
import com.example.kancentration.data.model.BoardCard
import com.example.kancentration.data.model.BoardDimension
import com.example.kancentration.data.model.DlcPackItem
import com.example.kancentration.data.model.GameSettings
import com.example.kancentration.data.model.GameType
import com.example.kancentration.data.model.KanjiData
import com.example.kancentration.data.repository.KanjiRepository
import com.example.kancentration.data.repository.SettingsRepository
import com.example.kancentration.domain.SoundManager
import com.example.kancentration.domain.TimerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = KanjiRepository(application)
    private val settingsRepository = SettingsRepository(application)
    val dlcManager = FirebaseStorageDlcManager(application)
    val soundManager = SoundManager(application)
    private val timerManager = TimerManager(viewModelScope)

    private val _uiState = MutableStateFlow(GameUiState(settings = settingsRepository.loadSettings()))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        val initialSettings = settingsRepository.loadSettings()
        soundManager.isMuted = !initialSettings.soundEnabled
        startNewGame(initialSettings)
        observeTimer()
    }

    private fun observeTimer() {
        viewModelScope.launch {
            timerManager.elapsedSeconds.collect { sec ->
                if (!_uiState.value.isTimeAttack) {
                    _uiState.update { it.copy(formattedTimer = timerManager.formatTime(sec)) }
                }
            }
        }
        viewModelScope.launch {
            timerManager.remainingMillis.collect { millis ->
                if (_uiState.value.isTimeAttack) {
                    _uiState.update { it.copy(formattedTimer = timerManager.formatMillis(millis)) }
                }
            }
        }
    }

    var hasStartedTimer = false
        private set

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

    fun startNewGame(settings: GameSettings = _uiState.value.settings) {
        hasStartedTimer = false
        timerManager.stop()
        val deck = repository.generateDeck(settings.dimension)

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
                gameOverMessage = "",
                firstFlippedCardId = null,
                secondFlippedCardId = null,
                isEvaluatingMatch = false,
                selectedKanjiForInspector = null
            )
        }

        // Prepare Timer according to GameType (Do not start until player's first move!)
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
    }

    fun onOpenWiktionary() {
        if (hasStartedTimer && !_uiState.value.isGameOver) {
            timerManager.pause()
        }
    }

    fun onPauseApp() {
        if (hasStartedTimer && !_uiState.value.isGameOver) {
            timerManager.pause()
        }
    }

    fun onResumeApp() {
        if (hasStartedTimer && !_uiState.value.isGameOver && timerManager.isPaused.value) {
            timerManager.resume()
        }
    }

    fun onCardClicked(card: BoardCard) {
        val state = _uiState.value
        if (state.isEvaluatingMatch) return

        // When game is over: still allow player to tap squares to bring up their kanji in inspector
        if (state.isGameOver) {
            val updatedCards = state.cards.map {
                if (it.instanceId == card.instanceId) it.copy(isFaceUp = true) else it
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

        if (card.isMatched || card.isFaceUp) {
            // Already face up or matched: just display in inspector
            _uiState.update { it.copy(selectedKanjiForInspector = card.kanji) }
            return
        }

        // Don't start clock until player makes their first move
        if (!hasStartedTimer) {
            hasStartedTimer = true
            if (state.isTimeAttack) {
                timerManager.startCountdown()
            } else {
                timerManager.startStopwatch()
            }
        }

        // Play flip sound
        soundManager.playFlip()

        if (state.firstFlippedCardId == null) {
            // Flip first card
            val updatedCards = state.cards.map {
                if (it.instanceId == card.instanceId) it.copy(isFaceUp = true) else it
            }
            _uiState.update {
                it.copy(
                    cards = updatedCards,
                    firstFlippedCardId = card.instanceId,
                    selectedKanjiForInspector = card.kanji
                )
            }
        } else if (state.secondFlippedCardId == null && card.instanceId != state.firstFlippedCardId) {
            // Flip second card
            val updatedCards = state.cards.map {
                if (it.instanceId == card.instanceId) it.copy(isFaceUp = true) else it
            }
            _uiState.update {
                it.copy(
                    cards = updatedCards,
                    secondFlippedCardId = card.instanceId,
                    totalMoves = it.totalMoves + 1,
                    isEvaluatingMatch = true,
                    selectedKanjiForInspector = card.kanji
                )
            }

            evaluateMatch(state.firstFlippedCardId, card.instanceId)
        }
    }

    private fun evaluateMatch(firstId: String, secondId: String) {
        viewModelScope.launch {
            val currentCards = _uiState.value.cards
            val card1 = currentCards.find { it.instanceId == firstId }
            val card2 = currentCards.find { it.instanceId == secondId }

            if (card1 != null && card2 != null) {
                val isMatch = card1.kanji.id == card2.kanji.id

                if (isMatch) {
                    delay(500) // Brief delay for match confirmation
                    soundManager.playMatch()
                    val p = _uiState.value.activePlayer
                    val updatedCards = currentCards.map {
                        if (it.instanceId == firstId || it.instanceId == secondId) {
                            it.copy(isMatched = true, isFaceUp = true, matchedByPlayer = p)
                        } else it
                    }

                    val newP1Score = if (p == 1) _uiState.value.player1Score + 1 else _uiState.value.player1Score
                    val newP2Score = if (p == 2) _uiState.value.player2Score + 1 else _uiState.value.player2Score
                    val newTotalMatches = _uiState.value.totalMatches + 1
                    val allPairsMatched = newTotalMatches >= _uiState.value.totalPairs

                    // Next player logic
                    val nextPlayer = if (_uiState.value.isTwoPlayer && !_uiState.value.settings.bonusTurnOnMatch) {
                        if (p == 1) 2 else 1
                    } else {
                        p // Continues turn if bonus turned on
                    }

                    if (allPairsMatched) {
                        timerManager.stop()
                        soundManager.playVictory()
                        val msg = if (_uiState.value.isTwoPlayer) {
                            if (newP1Score > newP2Score) "Player 1 Wins!"
                            else if (newP2Score > newP1Score) "Player 2 Wins!"
                            else "It's a Tie!"
                        } else {
                            "Cleared in ${_uiState.value.formattedTimer} (${_uiState.value.totalMoves} moves)!"
                        }

                        _uiState.update {
                            it.copy(
                                cards = updatedCards,
                                player1Score = newP1Score,
                                player2Score = newP2Score,
                                totalMatches = newTotalMatches,
                                isGameOver = true,
                                gameOverMessage = msg,
                                firstFlippedCardId = null,
                                secondFlippedCardId = null,
                                isEvaluatingMatch = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                cards = updatedCards,
                                player1Score = newP1Score,
                                player2Score = newP2Score,
                                totalMatches = newTotalMatches,
                                activePlayer = nextPlayer,
                                firstFlippedCardId = null,
                                secondFlippedCardId = null,
                                isEvaluatingMatch = false
                            )
                        }
                    }
                } else {
                    // Mismatch: Exposed time increased by 50% (from 750ms to 1125ms) so player can memorize
                    delay(1125L)
                    soundManager.playMismatch()
                    val updatedCards = currentCards.map {
                        if (it.instanceId == firstId || it.instanceId == secondId) {
                            it.copy(isFaceUp = false)
                        } else it
                    }
                    val nextPlayer = if (_uiState.value.isTwoPlayer) {
                        if (_uiState.value.activePlayer == 1) 2 else 1
                    } else 1

                    _uiState.update {
                        it.copy(
                            cards = updatedCards,
                            activePlayer = nextPlayer,
                            firstFlippedCardId = null,
                            secondFlippedCardId = null,
                            isEvaluatingMatch = false
                        )
                    }
                }
            }
        }
    }

    private fun handleTimeExpired() {
        soundManager.playMismatch()
        val score = _uiState.value.player1Score
        val total = _uiState.value.totalPairs
        _uiState.update {
            it.copy(
                isGameOver = true,
                gameOverMessage = "Time's Up! Matches Found: $score / $total"
            )
        }
    }

    fun onKanjiInspected(kanji: KanjiData) {
        _uiState.update { it.copy(selectedKanjiForInspector = kanji) }
    }

    fun setBoardDimension(dim: BoardDimension) {
        val newSettings = _uiState.value.settings.copy(dimension = dim)
        startNewGame(newSettings)
    }

    fun setGameType(type: GameType) {
        val newSettings = _uiState.value.settings.copy(gameType = type)
        startNewGame(newSettings)
    }

    fun toggleBonusTurnRule(enabled: Boolean) {
        _uiState.update { it.copy(settings = it.settings.copy(bonusTurnOnMatch = enabled)) }
    }

    fun toggleSound() {
        val newSound = !_uiState.value.settings.soundEnabled
        soundManager.isMuted = !newSound
        _uiState.update { it.copy(settings = it.settings.copy(soundEnabled = newSound)) }
    }

    fun setShowSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun setShowDlcStoreDialog(show: Boolean) {
        _uiState.update { it.copy(showDlcStoreDialog = show) }
    }

    fun downloadDlcPack(pack: DlcPackItem) {
        viewModelScope.launch {
            dlcManager.downloadPack(pack) { success ->
                if (success) {
                    repository.loadInstalledDlcPacks()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerManager.stop()
        soundManager.release()
    }
}
