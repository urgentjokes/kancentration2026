package com.example.kancentration.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.example.kancentration.data.model.BoardCard

@Composable
fun CardItem(
    card: BoardCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 36.sp,
    tileColor: Long = 0xFFFFFFFFL,
    kanjiColor: Long = 0xFFD4AF37L
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFaceUp || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "card_flip_${card.instanceId}"
    )

    val isFrontVisible = rotation > 90f

    val shape = RoundedCornerShape(8.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 16f * density
            }
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isFrontVisible) {
            // Card Back (Tile with customizable tile color and 漢 character color)
            val backColor = Color(tileColor)
            val textColor = Color(kanjiColor)
            val isLight = (backColor.red * 0.299f + backColor.green * 0.587f + backColor.blue * 0.114f) > 0.6f
            val borderColor = if (isLight) Color(0xFFCFD8DC) else Color.White.copy(alpha = 0.25f)

            Card(
                shape = shape,
                colors = CardDefaults.cardColors(containerColor = backColor),
                border = BorderStroke(1.5.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "漢",
                        color = textColor,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        } else {
            // Card Front (Flipped 180deg; mirror rotationY so glyph is upright)
            val borderColor = when {
                card.isMatched && card.matchedByPlayer == 1 -> Color(0xFF2E7D32) // P1 Green/Jade
                card.isMatched && card.matchedByPlayer == 2 -> Color(0xFFC2185B) // P2 Crimson
                card.isMatched -> Color(0xFF2E7D32)
                else -> Color(0xFF1976D2) // Active flip: blue border
            }

            Card(
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = if (card.isMatched) Color(0xFFF1F8E9) else Color(0xFFFAF9F6)
                ),
                border = BorderStroke(if (card.isMatched) 2.dp else 2.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = if (card.isMatched) 1.dp else 4.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = card.kanji.character,
                        color = if (card.isMatched) Color(0xFF1B5E20) else Color(0xFF212121),
                        fontSize = fontSize,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
    }
}
