package com.example.kancentration.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kancentration.theme.AmberTwinTrap
import com.example.kancentration.theme.AmberTwinTrapSurface
import com.example.kancentration.theme.TakeBamboo
import com.example.kancentration.theme.TakeBambooSurface
import com.example.kancentration.ui.viewmodel.GameUiState

/**
 * Fixed-height status banner slot displaying the "Near-Miss / Kanji Trap! (惜しい！)"
 * alert banner when activeTwinTrapBanner is populated, and a compact celebration /
 * time-up banner when the game ends.
 *
 * Designed with a strict fixed height (46.dp) to guarantee zero layout shift of the board grid.
 */
@Composable
fun StatusBannerSlot(
    uiState: GameUiState,
    onRestart: () -> Unit,
    onDismissTwinTrap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .padding(horizontal = 12.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isGameOver -> {
                val isTimeUp = uiState.isTimeExpired || uiState.gameOverMessage.contains("Time's Up", ignoreCase = true)
                val containerColor = if (isTimeUp) Color(0xFFFFF0F0) else TakeBambooSurface
                val borderColor = if (isTimeUp) Color(0xFFE53935) else TakeBamboo
                val textColor = if (isTimeUp) Color(0xFFC62828) else Color(0xFF1B5E20)
                val buttonColor = if (isTimeUp) Color(0xFFC62828) else TakeBamboo

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = containerColor,
                    border = BorderStroke(1.5.dp, borderColor),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTimeUp) "🛑 ${uiState.gameOverMessage}" else "🎉 Victory! ${uiState.gameOverMessage.ifEmpty { "Matches: ${uiState.totalMatches}/${uiState.totalPairs}" }}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onRestart,
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = "Play Again 🔄",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            uiState.activeTwinTrapBanner != null -> {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AmberTwinTrapSurface,
                        border = BorderStroke(1.5.dp, AmberTwinTrap),
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                role = androidx.compose.ui.semantics.Role.Button,
                                onClickLabel = "Dismiss Near-Miss notification",
                                onClick = onDismissTwinTrap
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "⚠️ Near-Miss! (惜しい！) ${uiState.activeTwinTrapBanner}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✕",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E).copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            else -> {
                // Fixed-height spacer guaranteeing zero board jitter
                Spacer(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
