package com.example.kancentration.ui.viewmodel

import com.example.kancentration.data.model.BoardCard
import com.example.kancentration.data.model.BoardDimension
import com.example.kancentration.data.model.GameSettings
import com.example.kancentration.data.model.GameType
import com.example.kancentration.data.model.KanjiData

/**
 * Explicit phases of the turn state machine conforming to Section 5.2.
 */
enum class TurnPhase {
    IDLE_AWAITING_FIRST_MOVE,
    FIRST_CARD_REVEALED,
    EVALUATING_MATCH,
    AWAITING_FIRST_CARD,
    GAME_OVER
}

/**
 * Immutable single source of truth for the presentation layer conforming to Section 5.4.
 */
data class GameUiState(
    val cards: List<BoardCard> = emptyList(),
    val firstFlippedCardId: String? = null,
    val secondFlippedCardId: String? = null,
    val isEvaluatingMatch: Boolean = false,
    val activeTwinTrapBanner: String? = null,
    val activePlayer: Int = 1,
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val totalMoves: Int = 0,
    val totalMatches: Int = 0,
    val selectedKanjiForInspector: KanjiData? = null,
    val settings: GameSettings = GameSettings(),
    val isGameOver: Boolean = false,
    val isTimeExpired: Boolean = false,
    val gameOverMessage: String = "",
    val formattedTimer: String = "00:00",
    val isTimerLowWarning: Boolean = false,
    val turnPhase: TurnPhase = TurnPhase.IDLE_AWAITING_FIRST_MOVE,
    val showSettingsDialog: Boolean = false,
    val showDlcStoreDialog: Boolean = false
) {
    val totalPairs: Int get() = cards.size / 2
    val isTwoPlayer: Boolean get() = settings.gameType is GameType.TwoPlayer
    val isTimeAttack: Boolean get() = settings.gameType is GameType.TimeAttack
}
