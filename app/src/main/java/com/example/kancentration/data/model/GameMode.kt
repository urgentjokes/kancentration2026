package com.example.kancentration.data.model

enum class BoardDimension(val size: Int) {
    FOUR_BY_FOUR(4),
    SIX_BY_SIX(6),
    EIGHT_BY_EIGHT(8);

    val totalCards: Int get() = size * size
    val requiredPairs: Int get() = totalCards / 2
    val requiredQuads: Int get() = totalCards / 4
}

sealed interface GameType {
    object Solo : GameType
    object TwoPlayer : GameType
    data class TimeAttack(val durationMinutes: Int) : GameType {
        val durationMillis: Long get() = durationMinutes * 60 * 1000L
    }
}

data class GameSettings(
    val dimension: BoardDimension = BoardDimension.FOUR_BY_FOUR,
    val gameType: GameType = GameType.Solo,
    val bonusTurnOnMatch: Boolean = true,
    val soundEnabled: Boolean = true,
    val tileColor: Long = 0xFFFFFFFFL,      // Default White
    val kanjiColor: Long = 0xFFD4AF37L       // Default Gold (#D4AF37)
)
