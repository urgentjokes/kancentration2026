package com.example.kancentration.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kancentration.data.model.BoardCard
import com.example.kancentration.theme.AizomeIndigo
import com.example.kancentration.theme.AmberTwinTrap
import com.example.kancentration.theme.AmberTwinTrapSurface
import com.example.kancentration.theme.KinpakuGold
import com.example.kancentration.theme.ShuiroSurface
import com.example.kancentration.theme.ShuiroVermilion
import com.example.kancentration.theme.SumiCharcoal
import com.example.kancentration.theme.TakeBamboo
import com.example.kancentration.theme.TakeBambooSurface
import com.example.kancentration.theme.WashiPaper

/**
 * Stateless Card component featuring a realistic 3D Y-axis flip animation,
 * perfectly centered Kanji typography filling ~75% of tile height with zero padding overhead,
 * and accessibility touch target semantics.
 */
@Composable
fun CardItem(
    card: BoardCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 38.sp,
    tileColor: Long = 0xFF1E3A5FL,
    kanjiColor: Long = 0xFFE0B44CL,
    useSerifFont: Boolean = true,
    cornerRadius: Dp = 8.dp
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFaceUp || card.isMatched) 180f else 0f,
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        ),
        label = "card_flip_${card.instanceId}"
    )

    val isFrontVisible = rotation > 90f
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }

    val accessibilityDesc = when {
        card.isMatched -> "Matched card: ${card.kanji.character}"
        card.isFaceUp -> "Face up card: ${card.kanji.character}"
        else -> "Face down card"
    }

    Box(
        modifier = modifier
            .semantics {
                contentDescription = accessibilityDesc
                role = Role.Button
            }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 16f * density
            }
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !card.isMatched && !card.isFaceUp,
                onClickLabel = "Flip card",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isFrontVisible) {
            // Card Back: Aizome Indigo tile with Kinpaku Gold '漢' seal
            val backColor = if (tileColor == 0xFFFFFFFFL || tileColor == 0xFF1E3A5FL) {
                AizomeIndigo
            } else {
                Color(tileColor)
            }
            val emblemColor = if (kanjiColor == 0xFFD4AF37L || kanjiColor == 0xFFE0B44CL) {
                KinpakuGold
            } else {
                Color(kanjiColor)
            }

            val borderColor = if (backColor == AizomeIndigo) {
                KinpakuGold.copy(alpha = 0.35f)
            } else {
                Color.White.copy(alpha = 0.25f)
            }

            Card(
                shape = shape,
                colors = CardDefaults.cardColors(containerColor = backColor),
                border = BorderStroke(1.5.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
                        color = emblemColor,
                        fontSize = fontSize * 0.88f,
                        fontWeight = FontWeight.Bold,
                        fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        style = LocalTextStyle.current.copy(
                            lineHeight = fontSize * 0.88f,
                            textAlign = TextAlign.Center,
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }
            }
        } else {
            // Card Front: Flipped 180° on Y-axis (mirrored back so glyph is upright)
            val containerColor = when {
                card.isTwinTrapHighlight -> AmberTwinTrapSurface
                card.isMatched && card.matchedByPlayer == 2 -> ShuiroSurface
                card.isMatched -> TakeBambooSurface
                else -> WashiPaper
            }

            val borderColor = when {
                card.isTwinTrapHighlight -> AmberTwinTrap
                card.isMatched && card.matchedByPlayer == 2 -> ShuiroVermilion
                card.isMatched -> TakeBamboo
                else -> AizomeIndigo
            }

            val glyphColor = when {
                card.isTwinTrapHighlight -> Color(0xFF92400E)
                card.isMatched && card.matchedByPlayer == 2 -> ShuiroVermilion
                card.isMatched -> TakeBamboo
                else -> SumiCharcoal
            }

            Card(
                shape = shape,
                colors = CardDefaults.cardColors(containerColor = containerColor),
                border = BorderStroke(
                    width = if (card.isMatched || card.isTwinTrapHighlight) 2.5.dp else 2.dp,
                    color = borderColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (card.isMatched) 1.dp else 4.dp
                ),
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
                        color = glyphColor,
                        fontSize = fontSize,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        style = LocalTextStyle.current.copy(
                            lineHeight = fontSize,
                            textAlign = TextAlign.Center,
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }
            }
        }
    }
}
