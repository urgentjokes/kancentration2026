package com.example.kancentration.domain

import com.example.kancentration.data.model.BoardCard
import com.example.kancentration.data.model.GameSettings
import com.example.kancentration.data.model.GameType

sealed interface TurnEvaluationResult {
    data class Match(
        val matchedPairKanjiId: String,
        val scoringPlayer: Int,
        val newPlayer1Score: Int,
        val newPlayer2Score: Int,
        val newTotalMatches: Int,
        val nextPlayer: Int,
        val isGameOver: Boolean,
        val gameOverMessage: String = ""
    ) : TurnEvaluationResult

    data class TwinTrapMismatch(
        val quadId: String,
        val firstCharacter: String,
        val secondCharacter: String,
        val nextPlayer: Int,
        val bannerMessage: String
    ) : TurnEvaluationResult

    data class StandardMismatch(
        val nextPlayer: Int
    ) : TurnEvaluationResult
}

class GameEngine {

    /**
     * Determines whether two cards represent an O(1) Confusable Twin Trap:
     * they belong to the same quad, but represent different kanji characters.
     */
    fun isTwinTrap(card1: BoardCard, card2: BoardCard): Boolean {
        return card1.quadId == card2.quadId && card1.kanji.id != card2.kanji.id
    }

    /**
     * Pure function evaluating turn progression upon flipping two cards.
     * Evaluates:
     * 1) Match confirmation and scoring
     * 2) 2-Player turn rotation with bonus turn on match (enabled vs disabled)
     * 3) O(1) Confusable Twin-Trap near-miss recognition
     * 4) Game completion state
     */
    fun evaluateFlip(
        card1: BoardCard,
        card2: BoardCard,
        activePlayer: Int,
        player1Score: Int,
        player2Score: Int,
        totalMatches: Int,
        totalPairs: Int,
        settings: GameSettings,
        formattedTimer: String = "00:00",
        totalMoves: Int = 0
    ): TurnEvaluationResult {
        val isMatch = card1.kanji.id == card2.kanji.id

        if (isMatch) {
            val newP1Score = if (activePlayer == 1) player1Score + 1 else player1Score
            val newP2Score = if (activePlayer == 2) player2Score + 1 else player2Score
            val newTotalMatches = totalMatches + 1
            val allPairsMatched = newTotalMatches >= totalPairs

            val isTwoPlayer = settings.gameType is GameType.TwoPlayer
            val nextPlayer = if (isTwoPlayer && !settings.bonusTurnOnMatch) {
                if (activePlayer == 1) 2 else 1
            } else {
                activePlayer // Keep turn when bonus turn rule is active
            }

            val gameOverMsg = if (allPairsMatched) {
                if (isTwoPlayer) {
                    when {
                        newP1Score > newP2Score -> "Player 1 Wins!"
                        newP2Score > newP1Score -> "Player 2 Wins!"
                        else -> "It's a Tie!"
                    }
                } else {
                    "Cleared in $formattedTimer ($totalMoves moves)!"
                }
            } else ""

            return TurnEvaluationResult.Match(
                matchedPairKanjiId = card1.kanji.id,
                scoringPlayer = activePlayer,
                newPlayer1Score = newP1Score,
                newPlayer2Score = newP2Score,
                newTotalMatches = newTotalMatches,
                nextPlayer = nextPlayer,
                isGameOver = allPairsMatched,
                gameOverMessage = gameOverMsg
            )
        }

        // Mismatch evaluation
        val isTwoPlayer = settings.gameType is GameType.TwoPlayer
        val nextPlayer = if (isTwoPlayer) {
            if (activePlayer == 1) 2 else 1
        } else {
            1
        }

        if (isTwinTrap(card1, card2) && settings.showTwinTrapHints) {
            return TurnEvaluationResult.TwinTrapMismatch(
                quadId = card1.quadId,
                firstCharacter = card1.kanji.character,
                secondCharacter = card2.kanji.character,
                nextPlayer = nextPlayer,
                bannerMessage = "${card1.kanji.character} vs ${card2.kanji.character}"
            )
        }

        return TurnEvaluationResult.StandardMismatch(
            nextPlayer = nextPlayer
        )
    }
}
