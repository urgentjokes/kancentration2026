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

/**
 * Responsive BoardGrid calculating dynamic tile scales and typography for
 * 4x4, 6x6, and 8x8 setups while maintaining a square aspect ratio and
 * zero layout jitter across small mobile screens up to full tablet layouts.
 */
@Composable
fun BoardGrid(
    cards: List<BoardCard>,
    dimension: BoardDimension,
    tileColor: Long = 0xFF1E3A5FL,
    kanjiColor: Long = 0xFFE0B44CL,
    useSerifFont: Boolean = true,
    onCardClick: (BoardCard) -> Unit,
    modifier: Modifier = Modifier
) {
    val size = dimension.size

    // Spacing scale per dimension conforming to DESIGN_PLAN.md:
    // 4x4 -> 8dp, 6x6 -> 5dp, 8x8 -> 3dp
    val spacing = when (dimension) {
        BoardDimension.FOUR_BY_FOUR -> 8.dp
        BoardDimension.SIX_BY_SIX -> 5.dp
        BoardDimension.EIGHT_BY_EIGHT -> 3.dp
    }

    // Corner radius per dimension conforming to DESIGN_PLAN.md:
    // 4x4 -> 12dp, 6x6 -> 8dp, 8x8 -> 6dp
    val cornerRadius = when (dimension) {
        BoardDimension.FOUR_BY_FOUR -> 12.dp
        BoardDimension.SIX_BY_SIX -> 8.dp
        BoardDimension.EIGHT_BY_EIGHT -> 6.dp
    }

    // Adaptive grid density base font sizes conforming to Section 2:
    // 4x4 -> 38sp, 6x6 -> 25sp, 8x8 -> 17sp
    val baseFontSize = when (dimension) {
        BoardDimension.FOUR_BY_FOUR -> 38.sp
        BoardDimension.SIX_BY_SIX -> 25.sp
        BoardDimension.EIGHT_BY_EIGHT -> 17.sp
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        val totalSpacing = spacing * (size - 1)
        val availableWidth = maxWidth - 8.dp
        val availableHeight = maxHeight - 8.dp
        val boardSize = minOf(availableWidth, availableHeight).coerceAtLeast(0.dp)
        val cardSize = if (size > 0) ((boardSize - totalSpacing) / size).coerceAtLeast(0.dp) else 0.dp

        // Font scaling rules:
        // When adaptive grid density changes text sizes (38sp, 25sp, 17sp),
        // ensure characters fill ~75% of tile height without truncation.
        val maxSafeFontSize = (cardSize.value * 0.75f)
        val responsiveFontSize = if (cardSize.value < 36f) {
            minOf(baseFontSize.value, maxSafeFontSize).sp
        } else if (cardSize.value > 65f) {
            maxOf(baseFontSize.value, maxSafeFontSize * 0.96f).sp
        } else {
            baseFontSize
        }

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
                                fontSize = responsiveFontSize,
                                tileColor = tileColor,
                                kanjiColor = kanjiColor,
                                useSerifFont = useSerifFont,
                                cornerRadius = cornerRadius
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
