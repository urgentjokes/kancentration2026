package com.example.kancentration.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kancentration.data.model.BoardCard
import com.example.kancentration.data.model.BoardDimension

@Composable
fun BoardGrid(
    cards: List<BoardCard>,
    dimension: BoardDimension,
    tileColor: Long = 0xFFFFFFFFL,
    kanjiColor: Long = 0xFFD4AF37L,
    onCardClick: (BoardCard) -> Unit,
    modifier: Modifier = Modifier
) {
    val size = dimension.size
    val spacing = when (dimension) {
        BoardDimension.EIGHT_BY_EIGHT -> 3.dp
        BoardDimension.SIX_BY_SIX -> 4.dp
        BoardDimension.FOUR_BY_FOUR -> 6.dp
    }
    val kanjiFontSize = when (dimension) {
        BoardDimension.FOUR_BY_FOUR -> 36.sp
        BoardDimension.SIX_BY_SIX -> 24.sp
        BoardDimension.EIGHT_BY_EIGHT -> 16.sp
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        val totalSpacing = spacing * (size - 1)
        val availableWidth = maxWidth - 16.dp
        val availableHeight = maxHeight - 16.dp
        val boardSize = minOf(availableWidth, availableHeight)
        val cardSize = (boardSize - totalSpacing) / size

        Column(
            modifier = Modifier.size(boardSize),
            verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in 0 until size) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (col in 0 until size) {
                        val index = row * size + col
                        if (index < cards.size) {
                            val card = cards[index]
                            CardItem(
                                card = card,
                                onClick = { onCardClick(card) },
                                modifier = Modifier.size(cardSize),
                                fontSize = kanjiFontSize,
                                tileColor = tileColor,
                                kanjiColor = kanjiColor
                            )
                        } else {
                            Box(modifier = Modifier.size(cardSize))
                        }
                    }
                }
            }
        }
    }
}
