package com.example.kancentration.ui.viewmodel

import com.example.kancentration.data.model.BoardCard
import com.example.kancentration.data.model.BoardDimension
import com.example.kancentration.data.model.GameSettings
import com.example.kancentration.data.model.GameType
import com.example.kancentration.data.model.KanjiData

data class GameUiState(
    val cards: List<BoardCard> = emptyList(),
    val firstFlippedCardId: String? = null,
    val secondFlippedCardId: String? = null,
    val isEvaluatingMatch: Boolean = false,
    val activePlayer: Int = 1,
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val totalMoves: Int = 0,
    val totalMatches: Int = 0,
    val selectedKanjiForInspector: KanjiData? = null,
    val settings: GameSettings = GameSettings(),
    val isGameOver: Boolean = false,
    val gameOverMessage: String = "",
    val formattedTimer: String = "00:00",
    val showSettingsDialog: Boolean = false,
    val showDlcStoreDialog: Boolean = false
) {
    val totalPairs: Int get() = cards.size / 2
    val isTwoPlayer: Boolean get() = settings.gameType is GameType.TwoPlayer
    val isTimeAttack: Boolean get() = settings.gameType is GameType.TimeAttack
}
