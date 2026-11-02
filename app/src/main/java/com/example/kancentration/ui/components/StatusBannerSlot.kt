package com.example.kancentration.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalConfiguration
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
 * Fixed-height status banner slot displaying the "Almost"
 * alert banner when activeTwinTrapBanner is populated, and a compact celebration /
 * time-up banner when the game ends.
 *
 * Designed with a strict fixed height (46.dp on phone landscape) to guarantee zero layout shift of the board grid.
 */
@Composable
fun StatusBannerSlot(
    uiState: GameUiState,
    onRestart: () -> Unit,
    onDismissTwinTrap: () -> Unit = {},
    isPhoneLandscape: Boolean = false,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600 || configuration.smallestScreenWidthDp >= 600
    val phoneLandscape = isPhoneLandscape || (!isTablet && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
    val isPhone = !isTablet

    val bannerHeight = when {
        isPhone -> 46.dp
        isTablet -> 108.dp
        else -> 92.dp
    }
    val bannerTextSize = when {
        isPhone -> 12.sp
        isTablet -> 28.sp
        else -> 24.sp
    }
    val buttonHeight = when {
        isPhone -> 24.dp
        isTablet -> 42.dp
        else -> 36.dp
    }
    val buttonTextSize = when {
        isPhone -> 10.sp
        isTablet -> 20.sp
        else -> 18.sp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeight)
            .padding(horizontal = if (phoneLandscape) 8.dp else if (isTablet) 16.dp else 12.dp, vertical = 2.dp),
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
                    shape = RoundedCornerShape(if (phoneLandscape) 8.dp else if (isTablet) 14.dp else 12.dp),
                    color = containerColor,
                    border = BorderStroke(1.5.dp, borderColor),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = if (phoneLandscape) 10.dp else if (isTablet) 16.dp else 12.dp,
                                vertical = if (phoneLandscape) 4.dp else if (isTablet) 8.dp else 6.dp
                            ),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTimeUp) "🛑 ${uiState.gameOverMessage}" else "🎉 Victory! ${uiState.gameOverMessage.ifEmpty { "Game Complete!" }}",
                                fontSize = bannerTextSize,
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
                                shape = RoundedCornerShape(if (phoneLandscape) 10.dp else 16.dp),
                                contentPadding = PaddingValues(horizontal = if (phoneLandscape) 8.dp else if (isTablet) 16.dp else 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(buttonHeight)
                            ) {
                                Text(
                                    text = "Play Again 🔄",
                                    fontSize = buttonTextSize,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = if (uiState.isTwoPlayer) {
                                "Player 1: ${uiState.player1Score} matches • Player 2: ${uiState.player2Score} matches"
                            } else {
                                "Total Matches: ${uiState.totalMatches}/${uiState.totalPairs} • Total Moves: ${uiState.totalMoves}"
                            },
                            fontSize = if (phoneLandscape) 10.sp else if (isTablet) 22.sp else 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                        shape = RoundedCornerShape(if (isTablet) 14.dp else 8.dp),
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
                        if (isPhone) {
                            // Phone (portrait and landscape): 50% reduced height single-row banner
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Text(
                                        text = "⚠️ Almost!",
                                        fontSize = bannerTextSize,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = AmberTwinTrap.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "Kanji Trap",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                    Text(
                                        text = uiState.activeTwinTrapBanner,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF78350F),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "✕",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E).copy(alpha = 0.7f),
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        } else {
                            // Tablet: Original full-size two-row layout with spacious typography
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Top Row: Tag/Headline and Dismiss Icon
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "⚠️ Almost!",
                                            fontSize = bannerTextSize,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AmberTwinTrap.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "Kanji Trap",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF92400E),
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 2.dp
                                                )
                                            )
                                        }
                                    }

                                    Text(
                                        text = "✕",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E).copy(alpha = 0.7f)
                                    )
                                }

                                // Bottom Row: The actual twin trap clue text
                                Text(
                                    text = uiState.activeTwinTrapBanner,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF78350F),
                                    lineHeight = 28.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
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
