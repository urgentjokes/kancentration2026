package com.example.kancentration.ui.viewmodel

import com.example.kancentration.data.firebase.FakeDlcManager
import com.example.kancentration.data.model.BoardDimension
import com.example.kancentration.data.model.GameSettings
import com.example.kancentration.data.model.GameType
import com.example.kancentration.data.model.KanjiData
import com.example.kancentration.data.model.KanjiQuad
import com.example.kancentration.data.repository.FakeKanjiRepository
import com.example.kancentration.data.repository.InMemorySettingsRepository
import com.example.kancentration.domain.NoOpSoundPlayer
import com.example.kancentration.domain.TimerManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private fun createTestQuads(): List<KanjiQuad> {
        val pairs = listOf(
            Triple("q1", "持", "特"),
            Triple("q2", "木", "本"),
            Triple("q3", "日", "曰"),
            Triple("q4", "偉", "違")
        )
        return pairs.map { (id, a, b) ->
            KanjiQuad(
                id = id,
                category = "Test Category",
                pairA = KanjiData(id = "k_$a", character = a, meanings = listOf("m_$a"), onReadings = emptyList(), kunReadings = emptyList(), strokeCount = 1, distinctionTip = "tip_$a"),
                pairB = KanjiData(id = "k_$b", character = b, meanings = listOf("m_$b"), onReadings = emptyList(), kunReadings = emptyList(), strokeCount = 1, distinctionTip = "tip_$b")
            )
        }
    }

    @Test
    fun testViewModelInitializationWithPureInterfaces() = runTest {
        val quads = createTestQuads()
        val kanjiRepo = FakeKanjiRepository(quads)
        val settingsRepo = InMemorySettingsRepository()
        val dlcManager = FakeDlcManager()
        val soundPlayer = NoOpSoundPlayer()

        val viewModel = GameViewModel(
            repository = kanjiRepo,
            settingsRepository = settingsRepo,
            dlcManager = dlcManager,
            soundManager = soundPlayer,
            coroutineScope = backgroundScope
        )

        val state = viewModel.uiState.value
        assertEquals("Initial deck for 4x4 must have 16 cards", 16, state.cards.size)
        assertTrue("showTwinTrapHints must be true", state.settings.showTwinTrapHints)
        assertFalse("showRomaji must be false", state.settings.showRomaji)
        assertTrue("useSerifFont must be true", state.settings.useSerifFont)
        assertEquals(0, state.totalMatches)
        assertEquals(0, state.totalMoves)
        assertFalse(state.isGameOver)
        assertNull(state.activeTwinTrapBanner)
        assertEquals(TurnPhase.IDLE_AWAITING_FIRST_MOVE, state.turnPhase)
    }

    @Test
    fun testCardFlipAndMatchingCycleWithTurnPhases() = runTest {
        val quads = createTestQuads()
        val kanjiRepo = FakeKanjiRepository(quads)
        val settingsRepo = InMemorySettingsRepository()

        val viewModel = GameViewModel(
            repository = kanjiRepo,
            settingsRepository = settingsRepo,
            dlcManager = FakeDlcManager(),
            soundManager = NoOpSoundPlayer(),
            coroutineScope = backgroundScope
        )

        assertEquals(TurnPhase.IDLE_AWAITING_FIRST_MOVE, viewModel.uiState.value.turnPhase)

        val initialCards = viewModel.uiState.value.cards
        val motsuCards = initialCards.filter { it.kanji.character == "持" }
        assertEquals(2, motsuCards.size)

        // 1. Flip first card -> TurnPhase.FIRST_CARD_REVEALED
        viewModel.onCardClicked(motsuCards[0].instanceId)
        val stateAfterFirst = viewModel.uiState.value
        assertEquals(motsuCards[0].instanceId, stateAfterFirst.firstFlippedCardId)
        assertNull(stateAfterFirst.secondFlippedCardId)
        assertEquals("持", stateAfterFirst.selectedKanjiForInspector?.character)
        assertEquals(TurnPhase.FIRST_CARD_REVEALED, stateAfterFirst.turnPhase)

        // 2. Flip second matching card -> TurnPhase.EVALUATING_MATCH
        viewModel.onCardClicked(motsuCards[1].instanceId)
        val stateDuringEval = viewModel.uiState.value
        assertEquals(TurnPhase.EVALUATING_MATCH, stateDuringEval.turnPhase)
        assertTrue(stateDuringEval.isEvaluatingMatch)

        testScheduler.runCurrent()
        advanceTimeBy(600) // Advance past match confirmation delay
        testScheduler.runCurrent()

        // 3. Match complete -> TurnPhase.AWAITING_FIRST_CARD
        val stateAfterMatch = viewModel.uiState.value
        assertEquals(1, stateAfterMatch.totalMatches)
        assertEquals(1, stateAfterMatch.totalMoves)
        assertNull(stateAfterMatch.firstFlippedCardId)
        assertNull(stateAfterMatch.secondFlippedCardId)
        assertFalse(stateAfterMatch.isEvaluatingMatch)
        assertEquals(TurnPhase.AWAITING_FIRST_CARD, stateAfterMatch.turnPhase)

        val matchedCard1 = stateAfterMatch.cards.find { it.instanceId == motsuCards[0].instanceId }
        val matchedCard2 = stateAfterMatch.cards.find { it.instanceId == motsuCards[1].instanceId }
        assertTrue(matchedCard1?.isMatched == true)
        assertTrue(matchedCard2?.isMatched == true)
    }

    @Test
    fun testRaceConditionPreventionDuringObservationDelay() = runTest {
        val quads = createTestQuads()
        val kanjiRepo = FakeKanjiRepository(quads)
        val settingsRepo = InMemorySettingsRepository()

        val viewModel = GameViewModel(
            repository = kanjiRepo,
            settingsRepository = settingsRepo,
            dlcManager = FakeDlcManager(),
            soundManager = NoOpSoundPlayer(),
            coroutineScope = backgroundScope
        )

        val initialCards = viewModel.uiState.value.cards
        val cardMotsu = initialCards.first { it.kanji.character == "持" }
        val cardToku = initialCards.first { it.kanji.character == "特" }
        val thirdCard = initialCards.first { it.kanji.character == "木" }

        // Flip 持 then 特 (Twin-Trap mismatch)
        viewModel.onCardClicked(cardMotsu.instanceId)
        assertEquals(TurnPhase.FIRST_CARD_REVEALED, viewModel.uiState.value.turnPhase)

        viewModel.onCardClicked(cardToku.instanceId)
        testScheduler.runCurrent()

        // Immediately during evaluation: isEvaluatingMatch = true, TurnPhase.EVALUATING_MATCH
        val evaluatingState = viewModel.uiState.value
        assertTrue("Evaluating state must be true", evaluatingState.isEvaluatingMatch)
        assertEquals(TurnPhase.EVALUATING_MATCH, evaluatingState.turnPhase)
        assertNotNull(evaluatingState.activeTwinTrapBanner)

        // Attempt 1: Click third face-down card during observation window -> must be ignored!
        viewModel.onCardClicked(thirdCard.instanceId)
        testScheduler.runCurrent()

        val stateAfterBlockedClick = viewModel.uiState.value
        val thirdCardState = stateAfterBlockedClick.cards.find { it.instanceId == thirdCard.instanceId }
        assertFalse("Third card must NOT be flipped during observation delay", thirdCardState?.isFaceUp == true)
        assertEquals("Total moves must not increment for blocked click", 1, stateAfterBlockedClick.totalMoves)

        // Attempt 2: Click already face-up card (cardMotsu) during observation window -> inspection permitted!
        viewModel.onCardClicked(cardMotsu.instanceId)
        testScheduler.runCurrent()
        assertEquals("持", viewModel.uiState.value.selectedKanjiForInspector?.character)
        assertTrue("Still evaluating match", viewModel.uiState.value.isEvaluatingMatch)

        // Advance time past the 1,150ms observation delay
        advanceTimeBy(1200L)
        testScheduler.runCurrent()

        val settledState = viewModel.uiState.value
        assertFalse("Evaluating state must be false after observation window", settledState.isEvaluatingMatch)
        assertEquals(TurnPhase.AWAITING_FIRST_CARD, settledState.turnPhase)
        // Near-Miss pop-up must persist until the user taps the screen again
        assertNotNull("Near-Miss pop-up must persist until next tap", settledState.activeTwinTrapBanner)

        val settledCard1 = settledState.cards.find { it.instanceId == cardMotsu.instanceId }
        val settledCard2 = settledState.cards.find { it.instanceId == cardToku.instanceId }
        assertFalse(settledCard1?.isFaceUp == true)
        assertFalse(settledCard2?.isFaceUp == true)

        // When the user taps the screen again (e.g. clicks a card), the pop-up is dismissed
        viewModel.onCardClicked(thirdCard.instanceId)
        testScheduler.runCurrent()
        assertNull("Near-Miss banner must be dismissed upon next card tap", viewModel.uiState.value.activeTwinTrapBanner)
        val thirdCardFlipped = viewModel.uiState.value.cards.find { it.instanceId == thirdCard.instanceId }
        assertTrue("Third card must be flipped", thirdCardFlipped?.isFaceUp == true)
    }

    @Test
    fun testConfigurableFailedMatchDelayRespected() = runTest {
        val quads = createTestQuads()
        val kanjiRepo = FakeKanjiRepository(quads)
        val customDelay = 2500L
        val settingsRepo = InMemorySettingsRepository(
            initialSettings = GameSettings(mismatchDelayMs = customDelay)
        )

        val viewModel = GameViewModel(
            repository = kanjiRepo,
            settingsRepository = settingsRepo,
            dlcManager = FakeDlcManager(),
            soundManager = NoOpSoundPlayer(),
            coroutineScope = backgroundScope
        )

        val state = viewModel.uiState.value
        val card1 = state.cards.first { it.kanji.character == "持" }
        val card2 = state.cards.first { it.kanji.character == "特" } // Twin-trap mismatch

        viewModel.onCardClicked(card1.instanceId)
        viewModel.onCardClicked(card2.instanceId)
        testScheduler.runCurrent()

        assertTrue("Evaluating state must be true", viewModel.uiState.value.isEvaluatingMatch)

        // At 1500ms, the cards must STILL be exposed because mismatchDelayMs is 2500ms
        advanceTimeBy(1500L)
        testScheduler.runCurrent()
        assertTrue("Cards must still be exposed at 1500ms when delay is 2500ms", viewModel.uiState.value.isEvaluatingMatch)

        // Advance past 2500ms (e.g. additional 1200ms -> total 2700ms)
        advanceTimeBy(1200L)
        testScheduler.runCurrent()
        assertFalse("Evaluating state must be false after custom mismatch delay elapses", viewModel.uiState.value.isEvaluatingMatch)
        val settledCard1 = viewModel.uiState.value.cards.find { it.instanceId == card1.instanceId }
        val settledCard2 = viewModel.uiState.value.cards.find { it.instanceId == card2.instanceId }
        assertFalse(settledCard1?.isFaceUp == true)
        assertFalse(settledCard2?.isFaceUp == true)
    }

    @Test
    fun testArmedLazyClockInitialization() = runTest {
        val quads = createTestQuads()
        val kanjiRepo = FakeKanjiRepository(quads)
        val settingsRepo = InMemorySettingsRepository()

        val viewModel = GameViewModel(
            repository = kanjiRepo,
            settingsRepository = settingsRepo,
            dlcManager = FakeDlcManager(),
            soundManager = NoOpSoundPlayer(),
            coroutineScope = backgroundScope
        )

        // 1. Clock is armed but NOT running before first card flip
        assertFalse("Timer must not be running before first card click", viewModel.timerManager.isRunning)
        assertEquals("00:00", viewModel.uiState.value.formattedTimer)

        // Advancing time before first click must NOT advance the clock
        advanceTimeBy(5000L)
        testScheduler.runCurrent()
        assertEquals("00:00", viewModel.uiState.value.formattedTimer)

        // 2. Click first card -> Clock starts ticking!
        val card = viewModel.uiState.value.cards.first()
        viewModel.onCardClicked(card.instanceId)
        testScheduler.runCurrent()

        assertTrue("Timer must be running after first card click", viewModel.timerManager.isRunning)

        // Advancing time should now increment the formatted timer
        advanceTimeBy(2000L)
        testScheduler.runCurrent()
        assertEquals("00:02", viewModel.uiState.value.formattedTimer)
    }

    @Test
    fun testTimeAttackLazyClockCountdownAndLowWarning() = runTest {
        val quads = createTestQuads()
        val kanjiRepo = FakeKanjiRepository(quads)
        val settingsRepo = InMemorySettingsRepository(
            initialSettings = GameSettings(gameType = GameType.TimeAttack(1)) // 1 minute
        )

        val viewModel = GameViewModel(
            repository = kanjiRepo,
            settingsRepository = settingsRepo,
            dlcManager = FakeDlcManager(),
            soundManager = NoOpSoundPlayer(),
            coroutineScope = backgroundScope
        )

        // Initial state before click
        assertEquals("01:00", viewModel.uiState.value.formattedTimer)
        assertFalse(viewModel.timerManager.isRunning)
        assertFalse(viewModel.uiState.value.isTimerLowWarning)

        // Click first card -> Countdown begins!
        val card = viewModel.uiState.value.cards.first()
        viewModel.onCardClicked(card.instanceId)
        testScheduler.runCurrent()

        assertTrue(viewModel.timerManager.isRunning)

        // Advance 46 seconds (14 seconds remain -> <= 15s warning triggers)
        advanceTimeBy(46000L)
        testScheduler.runCurrent()
        assertEquals("00:14", viewModel.uiState.value.formattedTimer)
        assertTrue("isTimerLowWarning must be true when <= 15s remain", viewModel.uiState.value.isTimerLowWarning)

        // Advance remaining 14 seconds -> Time's Up!
        advanceTimeBy(14000L)
        testScheduler.runCurrent()

        val finalState = viewModel.uiState.value
        assertTrue("Game over must be true when countdown expires", finalState.isGameOver)
        assertTrue("isTimeExpired must be true", finalState.isTimeExpired)
        assertEquals(TurnPhase.GAME_OVER, finalState.turnPhase)
        assertTrue(finalState.gameOverMessage.contains("Time's Up"))
    }

    @Test
    fun testCustomViewModelProviderFactoryWiring() {
        val quads = createTestQuads()
        val kanjiRepo = FakeKanjiRepository(quads)
        val settingsRepo = InMemorySettingsRepository()
        val timerManager = TimerManager(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined))
        val dlcManager = FakeDlcManager()
        val soundPlayer = NoOpSoundPlayer()

        val factory = GameViewModel.createFactory(
            repository = kanjiRepo,
            settingsRepository = settingsRepo,
            timerManager = timerManager,
            dlcManager = dlcManager,
            soundManager = soundPlayer
        )

        val viewModel = factory.create(GameViewModel::class.java)
        assertNotNull("Factory must construct GameViewModel instance", viewModel)
        assertEquals(16, viewModel.uiState.value.cards.size)
    }

    @Test
    fun testCycleBoardDimensionsResponsiveCardsAndPairs() = runTest {
        val file = File("src/main/assets/kanji_base_pack.json")
        val json = file.readText()
        val kanjiRepo = com.example.kancentration.data.repository.DefaultKanjiRepository(basePackJsonProvider = { json })
        val settingsRepo = InMemorySettingsRepository()

        val viewModel = GameViewModel(
            repository = kanjiRepo,
            settingsRepository = settingsRepo,
            dlcManager = FakeDlcManager(),
            soundManager = NoOpSoundPlayer(),
            coroutineScope = backgroundScope
        )

        // 1. Initial 4x4
        var state = viewModel.uiState.value
        assertEquals(BoardDimension.FOUR_BY_FOUR, state.settings.dimension)
        assertEquals(16, state.cards.size)
        assertEquals(8, state.totalPairs)

        // 2. Cycle to 6x6
        viewModel.cycleBoardDimension()
        state = viewModel.uiState.value
        assertEquals(BoardDimension.SIX_BY_SIX, state.settings.dimension)
        assertEquals(36, state.cards.size)
        assertEquals(18, state.totalPairs)

        // 3. Cycle to 8x8
        viewModel.cycleBoardDimension()
        state = viewModel.uiState.value
        assertEquals(BoardDimension.EIGHT_BY_EIGHT, state.settings.dimension)
        assertEquals(64, state.cards.size)
        assertEquals(32, state.totalPairs)

        // 4. Cycle back to 4x4
        viewModel.cycleBoardDimension()
        state = viewModel.uiState.value
        assertEquals(BoardDimension.FOUR_BY_FOUR, state.settings.dimension)
        assertEquals(16, state.cards.size)
        assertEquals(8, state.totalPairs)
    }

    @Test
    fun testKanjiInspectorUpdatesOnCardSelection() = runTest {
        val quads = createTestQuads()
        val kanjiRepo = FakeKanjiRepository(quads)
        val settingsRepo = InMemorySettingsRepository()

        val viewModel = GameViewModel(
            repository = kanjiRepo,
            settingsRepository = settingsRepo,
            dlcManager = FakeDlcManager(),
            soundManager = NoOpSoundPlayer(),
            coroutineScope = backgroundScope
        )

        // Initially no kanji inspected
        assertNull(viewModel.uiState.value.selectedKanjiForInspector)

        // Click a card
        val card = viewModel.uiState.value.cards.first()
        viewModel.onCardClicked(card)

        val inspected = viewModel.uiState.value.selectedKanjiForInspector
        assertNotNull(inspected)
        assertEquals(card.kanji.character, inspected!!.character)
        assertEquals(card.kanji.id, inspected.id)
    }

    @Test
    fun testLifecycleSafetyCancellationOnResetAndSizeSwitch() = runTest {
        val file = File("src/main/assets/kanji_base_pack.json")
        val json = file.readText()
        val kanjiRepo = com.example.kancentration.data.repository.DefaultKanjiRepository(basePackJsonProvider = { json })
        val settingsRepo = InMemorySettingsRepository()

        val viewModel = GameViewModel(
            repository = kanjiRepo,
            settingsRepository = settingsRepo,
            dlcManager = FakeDlcManager(),
            soundManager = NoOpSoundPlayer(),
            coroutineScope = backgroundScope
        )

        // 1. Flip two cards to trigger a mismatch observation delay (1,150ms)
        val initialCards = viewModel.uiState.value.cards
        val card1 = initialCards[0]
        val card2 = initialCards.first { it.kanji.character != card1.kanji.character }

        viewModel.onCardClicked(card1.instanceId)
        viewModel.onCardClicked(card2.instanceId)
        testScheduler.runCurrent()

        assertTrue("Evaluation must be in progress", viewModel.uiState.value.isEvaluatingMatch)
        assertEquals(TurnPhase.EVALUATING_MATCH, viewModel.uiState.value.turnPhase)

        // 2. While observation delay is in-flight at 300ms, user switches size / restarts
        advanceTimeBy(300L)
        testScheduler.runCurrent()
        viewModel.cycleBoardDimension() // 4x4 -> 6x6
        testScheduler.runCurrent()

        // 3. New game state must immediately be 6x6, clean, and not evaluating
        val stateAfterSwitch = viewModel.uiState.value
        assertEquals("Dimension must be 6x6", BoardDimension.SIX_BY_SIX, stateAfterSwitch.settings.dimension)
        assertEquals("Must have 36 cards", 36, stateAfterSwitch.cards.size)
        assertFalse("Evaluation must be cleanly cancelled", stateAfterSwitch.isEvaluatingMatch)
        assertEquals(TurnPhase.IDLE_AWAITING_FIRST_MOVE, stateAfterSwitch.turnPhase)

        // 4. Advance time past the remaining 850ms of the old 1,150ms delay
        advanceTimeBy(1000L)
        testScheduler.runCurrent()

        // 5. Verify the new board was NOT touched by any stale coroutine execution
        val stateAfterOldDelay = viewModel.uiState.value
        assertEquals(36, stateAfterOldDelay.cards.size)
        assertFalse(stateAfterOldDelay.isEvaluatingMatch)
        assertEquals(TurnPhase.IDLE_AWAITING_FIRST_MOVE, stateAfterOldDelay.turnPhase)
        assertTrue("All cards on new board must remain face-down", stateAfterOldDelay.cards.all { !it.isFaceUp })
    }

    @Test
    fun testLazyClockGuardsExclusivelyOnFirstFaceDownCardFlip() = runTest {
        val quads = createTestQuads()
        val kanjiRepo = FakeKanjiRepository(quads)
        val settingsRepo = InMemorySettingsRepository()

        val viewModel = GameViewModel(
            repository = kanjiRepo,
            settingsRepository = settingsRepo,
            dlcManager = FakeDlcManager(),
            soundManager = NoOpSoundPlayer(),
            coroutineScope = backgroundScope
        )

        // 1. Initial State: clock armed but not running
        assertFalse("Timer must not run on initialization", viewModel.timerManager.isRunning)
        assertEquals("00:00", viewModel.uiState.value.formattedTimer)

        // 2. Pausing, resuming, or opening/closing dialogs must NOT trigger the timer before first move
        viewModel.onPauseApp()
        viewModel.onResumeApp()
        viewModel.setShowSettingsDialog(true)
        viewModel.setShowSettingsDialog(false)
        viewModel.setShowDlcStoreDialog(true)
        viewModel.setShowDlcStoreDialog(false)
        viewModel.onOpenWiktionary()
        viewModel.onResumeApp()
        advanceTimeBy(10000L)
        testScheduler.runCurrent()

        assertFalse("Timer must STILL not run after backgrounding/dialog events before first move", viewModel.timerManager.isRunning)
        assertEquals("00:00", viewModel.uiState.value.formattedTimer)

        // 3. Execute first face-down card flip -> Clock starts ticking!
        val card1 = viewModel.uiState.value.cards[0]
        viewModel.onCardClicked(card1.instanceId)
        testScheduler.runCurrent()

        assertTrue("Timer must start running upon first face-down card flip", viewModel.timerManager.isRunning)

        advanceTimeBy(3000L)
        testScheduler.runCurrent()
        assertEquals("00:03", viewModel.uiState.value.formattedTimer)

        // 4. Clicking the second card (matching or mismatch) must NOT restart or disrupt the running clock
        val matchingCard = viewModel.uiState.value.cards.first { it.instanceId != card1.instanceId && it.kanji.character == card1.kanji.character }
        viewModel.onCardClicked(matchingCard.instanceId)
        testScheduler.runCurrent()

        advanceTimeBy(2000L)
        testScheduler.runCurrent()
        assertEquals("00:05", viewModel.uiState.value.formattedTimer)
        assertTrue(viewModel.timerManager.isRunning)
    }

    @Test
    fun testApplySettingsWithNewPackTriggersRestart() = runTest {
        val quads = (1..16).map { i ->
            KanjiQuad(
                id = "quad_$i",
                category = "Category $i",
                pairA = KanjiData("k_${i}_a", "A$i", listOf("Meaning"), listOf("ON"), listOf("kun"), 1, "Tip"),
                pairB = KanjiData("k_${i}_b", "B$i", listOf("Meaning"), listOf("ON"), listOf("kun"), 1, "Tip")
            )
        }
        val kanjiRepo = FakeKanjiRepository(quads)
        val settingsRepo = InMemorySettingsRepository()
        val viewModel = GameViewModel(
            repository = kanjiRepo,
            settingsRepository = settingsRepo,
            dlcManager = FakeDlcManager(),
            soundManager = NoOpSoundPlayer(),
            coroutineScope = backgroundScope
        )

        assertEquals("base_pack", viewModel.uiState.value.settings.selectedPackId)

        val updatedSettings = viewModel.uiState.value.settings.copy(selectedPackId = "grade_1")
        viewModel.applySettings(updatedSettings)

        assertEquals("grade_1", viewModel.uiState.value.settings.selectedPackId)
        assertEquals("grade_1", settingsRepo.loadSettings().selectedPackId)
    }

    @Test
    fun testShowFlashCardsScreenStateAndKanjiExtraction() = runTest {
        val quads = createTestQuads()
        val kanjiRepo = FakeKanjiRepository(quads)
        val settingsRepo = InMemorySettingsRepository()
        val viewModel = GameViewModel(
            repository = kanjiRepo,
            settingsRepository = settingsRepo,
            dlcManager = FakeDlcManager(),
            soundManager = NoOpSoundPlayer(),
            coroutineScope = backgroundScope
        )

        assertFalse(viewModel.uiState.value.showFlashCardsScreen)

        viewModel.setShowFlashCardsScreen(true)
        assertTrue(viewModel.uiState.value.showFlashCardsScreen)

        // Verify distinct kanji from the board cards
        val distinctKanji = viewModel.uiState.value.cards.map { it.kanji }.distinctBy { it.id }
        assertEquals(8, distinctKanji.size)
        distinctKanji.forEach { kanji ->
            assertTrue(kanji.character.isNotEmpty())
            assertTrue(kanji.wiktionaryUrl.contains("en.wiktionary.org/wiki/"))
        }

        viewModel.setShowFlashCardsScreen(false)
        assertFalse(viewModel.uiState.value.showFlashCardsScreen)
    }

    @Test
    fun testGameTabsAndExposedKanjiTracking() = runTest {
        val quads = createTestQuads()
        val kanjiRepo = FakeKanjiRepository(quads)
        val settingsRepo = InMemorySettingsRepository()
        val viewModel = GameViewModel(
            repository = kanjiRepo,
            settingsRepository = settingsRepo,
            dlcManager = FakeDlcManager(),
            soundManager = NoOpSoundPlayer(),
            coroutineScope = backgroundScope
        )

        // Default tab is CURRENT_GAME
        assertEquals(GameTab.CURRENT_GAME, viewModel.uiState.value.selectedTab)
        assertFalse(viewModel.uiState.value.showFlashCardsScreen)

        // Initially no cards are exposed
        val initialExposed = viewModel.uiState.value.cards.filter { it.hasBeenExposed || it.isFaceUp || it.isMatched }
        assertEquals(0, initialExposed.size)

        // Switch tab to FLASH_CARDS
        viewModel.selectTab(GameTab.FLASH_CARDS)
        assertEquals(GameTab.FLASH_CARDS, viewModel.uiState.value.selectedTab)
        assertTrue(viewModel.uiState.value.showFlashCardsScreen)

        // Switch back to CURRENT_GAME
        viewModel.selectTab(GameTab.CURRENT_GAME)
        assertEquals(GameTab.CURRENT_GAME, viewModel.uiState.value.selectedTab)

        // Flip card 1
        val firstCard = viewModel.uiState.value.cards[0]
        viewModel.onCardClicked(firstCard.instanceId)

        val exposedAfterFirstFlip = viewModel.uiState.value.cards.filter { it.hasBeenExposed || it.isFaceUp || it.isMatched }
        assertEquals(1, exposedAfterFirstFlip.size)
        assertEquals(firstCard.kanji.id, exposedAfterFirstFlip[0].kanji.id)

        // Flip card 2 (which is from a different quad, standard mismatch)
        val mismatchCard = viewModel.uiState.value.cards.first { it.quadId != firstCard.quadId }
        viewModel.onCardClicked(mismatchCard.instanceId)

        // Advance past mismatch delay
        advanceTimeBy(viewModel.uiState.value.settings.mismatchDelayMs + 100)

        // Both cards should still be marked as hasBeenExposed even though they flipped face-down
        val exposedAfterMismatch = viewModel.uiState.value.cards.filter { it.hasBeenExposed || it.isFaceUp || it.isMatched }
        assertEquals(2, exposedAfterMismatch.size)
        assertTrue(exposedAfterMismatch.any { it.kanji.id == firstCard.kanji.id })
        assertTrue(exposedAfterMismatch.any { it.kanji.id == mismatchCard.kanji.id })

        // Check grouping confusable-pair by confusable-pair
        val quadGroups = exposedAfterMismatch.groupBy { it.quadId }
            .values
            .map { group -> group.map { it.kanji }.distinctBy { it.id } }
        assertEquals(2, quadGroups.size)
    }
}
