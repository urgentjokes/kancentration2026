package com.example.kancentration.domain

import com.example.kancentration.data.model.BoardCard
import com.example.kancentration.data.model.GameSettings
import com.example.kancentration.data.model.GameType
import com.example.kancentration.data.model.KanjiData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class GameEngineTest {

    private lateinit var gameEngine: GameEngine

    // Sample kanji entries from signature DESIGN.md quads
    private val kanjiMotsu = KanjiData(id = "k_持", character = "持", meanings = listOf("hold"), onReadings = listOf("ジ"), kunReadings = listOf("も.つ"), strokeCount = 9, distinctionTip = "扌 radical")
    private val kanjiToku = KanjiData(id = "k_特", character = "特", meanings = listOf("special"), onReadings = listOf("トク"), kunReadings = emptyList(), strokeCount = 10, distinctionTip = "牛 radical")
    private val kanjiKi = KanjiData(id = "k_木", character = "木", meanings = listOf("tree"), onReadings = listOf("モク"), kunReadings = listOf("き"), strokeCount = 4, distinctionTip = "no root tick")
    private val kanjiHon = KanjiData(id = "k_本", character = "本", meanings = listOf("book"), onReadings = listOf("ホン"), kunReadings = listOf("もと"), strokeCount = 5, distinctionTip = "root tick")

    @Before
    fun setup() {
        gameEngine = GameEngine()
    }

    private fun createCard(kanji: KanjiData, quadId: String, twinChar: String = ""): BoardCard {
        return BoardCard(
            instanceId = UUID.randomUUID().toString(),
            kanji = kanji,
            quadId = quadId,
            confusableTwinCharacter = twinChar
        )
    }

    // ==========================================
    // 1. MATCH SCORING TESTS
    // ==========================================

    @Test
    fun testMatchScoringInSoloMode() {
        val card1 = createCard(kanjiMotsu, "quad_hold_special", "特")
        val card2 = createCard(kanjiMotsu, "quad_hold_special", "特")
        val settings = GameSettings(gameType = GameType.Solo)

        val result = gameEngine.evaluateFlip(
            card1 = card1,
            card2 = card2,
            activePlayer = 1,
            player1Score = 2,
            player2Score = 0,
            totalMatches = 2,
            totalPairs = 8,
            settings = settings
        )

        assertTrue("Result must be Match", result is TurnEvaluationResult.Match)
        val match = result as TurnEvaluationResult.Match
        assertEquals("k_持", match.matchedPairKanjiId)
        assertEquals("Scoring player must be 1", 1, match.scoringPlayer)
        assertEquals("Player 1 score increments to 3", 3, match.newPlayer1Score)
        assertEquals("Player 2 score unchanged", 0, match.newPlayer2Score)
        assertEquals("Total matches increments to 3", 3, match.newTotalMatches)
        assertEquals("Solo mode active player remains 1", 1, match.nextPlayer)
        assertFalse("Game is not over yet (3/8 pairs)", match.isGameOver)
    }

    @Test
    fun testMatchScoringPlayer1InTwoPlayerMode() {
        val card1 = createCard(kanjiKi, "quad_tree_book", "本")
        val card2 = createCard(kanjiKi, "quad_tree_book", "本")
        val settings = GameSettings(gameType = GameType.TwoPlayer, bonusTurnOnMatch = true)

        val result = gameEngine.evaluateFlip(
            card1 = card1,
            card2 = card2,
            activePlayer = 1,
            player1Score = 4,
            player2Score = 3,
            totalMatches = 7,
            totalPairs = 18,
            settings = settings
        )

        assertTrue(result is TurnEvaluationResult.Match)
        val match = result as TurnEvaluationResult.Match
        assertEquals(5, match.newPlayer1Score)
        assertEquals(3, match.newPlayer2Score)
        assertEquals(8, match.newTotalMatches)
        assertFalse(match.isGameOver)
    }

    @Test
    fun testMatchScoringPlayer2InTwoPlayerMode() {
        val card1 = createCard(kanjiHon, "quad_tree_book", "木")
        val card2 = createCard(kanjiHon, "quad_tree_book", "木")
        val settings = GameSettings(gameType = GameType.TwoPlayer, bonusTurnOnMatch = true)

        val result = gameEngine.evaluateFlip(
            card1 = card1,
            card2 = card2,
            activePlayer = 2,
            player1Score = 4,
            player2Score = 3,
            totalMatches = 7,
            totalPairs = 18,
            settings = settings
        )

        assertTrue(result is TurnEvaluationResult.Match)
        val match = result as TurnEvaluationResult.Match
        assertEquals(4, match.newPlayer1Score)
        assertEquals(4, match.newPlayer2Score)
        assertEquals(8, match.newTotalMatches)
        assertFalse(match.isGameOver)
    }

    @Test
    fun testGameCompletionScoringPlayer1Victory() {
        val card1 = createCard(kanjiMotsu, "quad_hold_special", "特")
        val card2 = createCard(kanjiMotsu, "quad_hold_special", "特")
        val settings = GameSettings(gameType = GameType.TwoPlayer)

        val result = gameEngine.evaluateFlip(
            card1 = card1,
            card2 = card2,
            activePlayer = 1,
            player1Score = 4,
            player2Score = 3,
            totalMatches = 7,
            totalPairs = 8, // Last pair!
            settings = settings
        )

        val match = result as TurnEvaluationResult.Match
        assertEquals(5, match.newPlayer1Score)
        assertEquals(3, match.newPlayer2Score)
        assertEquals(8, match.newTotalMatches)
        assertTrue("Game must be marked over", match.isGameOver)
        assertEquals("Player 1 Wins!", match.gameOverMessage)
    }

    @Test
    fun testGameCompletionScoringPlayer2Victory() {
        val card1 = createCard(kanjiMotsu, "quad_hold_special", "特")
        val card2 = createCard(kanjiMotsu, "quad_hold_special", "特")
        val settings = GameSettings(gameType = GameType.TwoPlayer)

        val result = gameEngine.evaluateFlip(
            card1 = card1,
            card2 = card2,
            activePlayer = 2,
            player1Score = 3,
            player2Score = 4,
            totalMatches = 7,
            totalPairs = 8,
            settings = settings
        )

        val match = result as TurnEvaluationResult.Match
        assertEquals(3, match.newPlayer1Score)
        assertEquals(5, match.newPlayer2Score)
        assertTrue(match.isGameOver)
        assertEquals("Player 2 Wins!", match.gameOverMessage)
    }

    @Test
    fun testGameCompletionScoringTie() {
        val card1 = createCard(kanjiMotsu, "quad_hold_special", "特")
        val card2 = createCard(kanjiMotsu, "quad_hold_special", "特")
        val settings = GameSettings(gameType = GameType.TwoPlayer)

        val result = gameEngine.evaluateFlip(
            card1 = card1,
            card2 = card2,
            activePlayer = 2,
            player1Score = 4,
            player2Score = 3,
            totalMatches = 7,
            totalPairs = 8,
            settings = settings
        )

        val match = result as TurnEvaluationResult.Match
        assertEquals(4, match.newPlayer1Score)
        assertEquals(4, match.newPlayer2Score)
        assertTrue(match.isGameOver)
        assertEquals("It's a Tie!", match.gameOverMessage)
    }

    // ==========================================
    // 2. 2-PLAYER TURN ROTATION TESTS
    // ==========================================

    @Test
    fun testTwoPlayerBonusTurnEnabledPlayerKeepsTurnOnMatch() {
        val card1 = createCard(kanjiKi, "quad_tree_book", "本")
        val card2 = createCard(kanjiKi, "quad_tree_book", "本")
        val settings = GameSettings(gameType = GameType.TwoPlayer, bonusTurnOnMatch = true)

        // P1 matches -> P1 keeps turn
        val p1Match = gameEngine.evaluateFlip(
            card1 = card1, card2 = card2,
            activePlayer = 1, player1Score = 0, player2Score = 0,
            totalMatches = 0, totalPairs = 8, settings = settings
        ) as TurnEvaluationResult.Match
        assertEquals("Player 1 must keep turn on match when bonusTurnOnMatch is true", 1, p1Match.nextPlayer)

        // P2 matches -> P2 keeps turn
        val p2Match = gameEngine.evaluateFlip(
            card1 = card1, card2 = card2,
            activePlayer = 2, player1Score = 1, player2Score = 0,
            totalMatches = 1, totalPairs = 8, settings = settings
        ) as TurnEvaluationResult.Match
        assertEquals("Player 2 must keep turn on match when bonusTurnOnMatch is true", 2, p2Match.nextPlayer)
    }

    @Test
    fun testTwoPlayerBonusTurnDisabledPlayerPassesTurnOnMatch() {
        val card1 = createCard(kanjiKi, "quad_tree_book", "本")
        val card2 = createCard(kanjiKi, "quad_tree_book", "本")
        val settings = GameSettings(gameType = GameType.TwoPlayer, bonusTurnOnMatch = false)

        // P1 matches -> passes turn to P2
        val p1Match = gameEngine.evaluateFlip(
            card1 = card1, card2 = card2,
            activePlayer = 1, player1Score = 0, player2Score = 0,
            totalMatches = 0, totalPairs = 8, settings = settings
        ) as TurnEvaluationResult.Match
        assertEquals("Player 1 must pass turn to Player 2 when bonusTurnOnMatch is false", 2, p1Match.nextPlayer)

        // P2 matches -> passes turn to P1
        val p2Match = gameEngine.evaluateFlip(
            card1 = card1, card2 = card2,
            activePlayer = 2, player1Score = 1, player2Score = 0,
            totalMatches = 1, totalPairs = 8, settings = settings
        ) as TurnEvaluationResult.Match
        assertEquals("Player 2 must pass turn to Player 1 when bonusTurnOnMatch is false", 1, p2Match.nextPlayer)
    }

    @Test
    fun testTwoPlayerTurnRotationOnMismatchAlwaysPassesTurn() {
        val cardMotsu = createCard(kanjiMotsu, "quad_hold_special", "特")
        val cardKi = createCard(kanjiKi, "quad_tree_book", "本")
        val settings = GameSettings(gameType = GameType.TwoPlayer, bonusTurnOnMatch = true)

        // P1 mismatches -> passes to P2
        val p1Mismatch = gameEngine.evaluateFlip(
            card1 = cardMotsu, card2 = cardKi,
            activePlayer = 1, player1Score = 2, player2Score = 2,
            totalMatches = 4, totalPairs = 8, settings = settings
        ) as TurnEvaluationResult.StandardMismatch
        assertEquals("Player 1 mismatch must always pass turn to Player 2", 2, p1Mismatch.nextPlayer)

        // P2 mismatches -> passes to P1
        val p2Mismatch = gameEngine.evaluateFlip(
            card1 = cardMotsu, card2 = cardKi,
            activePlayer = 2, player1Score = 2, player2Score = 2,
            totalMatches = 4, totalPairs = 8, settings = settings
        ) as TurnEvaluationResult.StandardMismatch
        assertEquals("Player 2 mismatch must always pass turn to Player 1", 1, p2Mismatch.nextPlayer)
    }

    // ==========================================
    // 3. O(1) TWIN-TRAP RECOGNITION TESTS
    // ==========================================

    @Test
    fun testO1TwinTrapRecognitionSameQuadDifferentKanji() {
        val cardMotsu = createCard(kanjiMotsu, "quad_02_hold_special", "特")
        val cardToku = createCard(kanjiToku, "quad_02_hold_special", "持")

        // Direct O(1) predicate verification
        assertTrue("isTwinTrap must be true for 持 and 特 from quad_02_hold_special", gameEngine.isTwinTrap(cardMotsu, cardToku))
        assertTrue("isTwinTrap must be symmetric", gameEngine.isTwinTrap(cardToku, cardMotsu))

        // When hints are ON
        val settingsWithHints = GameSettings(gameType = GameType.TwoPlayer, showTwinTrapHints = true)
        val resultWithHints = gameEngine.evaluateFlip(
            card1 = cardMotsu, card2 = cardToku,
            activePlayer = 1, player1Score = 0, player2Score = 0,
            totalMatches = 0, totalPairs = 8, settings = settingsWithHints
        )

        assertTrue("Result must be TwinTrapMismatch", resultWithHints is TurnEvaluationResult.TwinTrapMismatch)
        val twinTrap = resultWithHints as TurnEvaluationResult.TwinTrapMismatch
        assertEquals("quad_02_hold_special", twinTrap.quadId)
        assertEquals("持", twinTrap.firstCharacter)
        assertEquals("特", twinTrap.secondCharacter)
        assertEquals(2, twinTrap.nextPlayer)
        assertTrue(twinTrap.bannerMessage.contains("惜しい！"))
        assertTrue(twinTrap.bannerMessage.contains("持 vs 特"))
    }

    @Test
    fun testTwinTrapDisabledInSettingsBecomesStandardMismatch() {
        val cardMotsu = createCard(kanjiMotsu, "quad_02_hold_special", "特")
        val cardToku = createCard(kanjiToku, "quad_02_hold_special", "持")

        val settingsWithoutHints = GameSettings(gameType = GameType.TwoPlayer, showTwinTrapHints = false)
        val result = gameEngine.evaluateFlip(
            card1 = cardMotsu, card2 = cardToku,
            activePlayer = 1, player1Score = 0, player2Score = 0,
            totalMatches = 0, totalPairs = 8, settings = settingsWithoutHints
        )

        assertTrue("When hints are disabled, result must be StandardMismatch", result is TurnEvaluationResult.StandardMismatch)
        val standard = result as TurnEvaluationResult.StandardMismatch
        assertEquals(2, standard.nextPlayer)
    }

    @Test
    fun testDifferentQuadsAreNeverTwinTraps() {
        val cardMotsu = createCard(kanjiMotsu, "quad_02_hold_special", "特")
        val cardKi = createCard(kanjiKi, "quad_19_tree_book", "本")

        assertFalse("Different quads must never be twin traps", gameEngine.isTwinTrap(cardMotsu, cardKi))

        val settings = GameSettings(showTwinTrapHints = true)
        val result = gameEngine.evaluateFlip(
            card1 = cardMotsu, card2 = cardKi,
            activePlayer = 1, player1Score = 0, player2Score = 0,
            totalMatches = 0, totalPairs = 8, settings = settings
        )

        assertTrue("Different quads must produce StandardMismatch", result is TurnEvaluationResult.StandardMismatch)
    }

    @Test
    fun testMatchingPairIsNeverTwinTrap() {
        val card1 = createCard(kanjiMotsu, "quad_02_hold_special", "特")
        val card2 = createCard(kanjiMotsu, "quad_02_hold_special", "特")

        assertFalse("Identical kanji match is never a twin trap", gameEngine.isTwinTrap(card1, card2))
    }
}
