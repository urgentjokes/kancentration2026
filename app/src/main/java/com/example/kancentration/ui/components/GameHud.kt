package com.example.kancentration.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kancentration.ui.viewmodel.GameUiState

/**
 * Top HUD Bar maintaining a clean two-row layout in the sidebar:
 * - Row 1: "Kancentration" title, board-size pill, and control icons (DLC, Settings, Restart)
 * - Row 2: Timer badge with "Moves:" field to its right on the left, score/matches telemetry on the right.
 */
@Composable
fun GameHud(
    uiState: GameUiState,
    onSettingsClick: () -> Unit,
    onDlcStoreClick: () -> Unit,
    onRestartClick: () -> Unit,
    onCycleDimensionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600 || configuration.smallestScreenWidthDp >= 600

    val actionButtonSize = if (isTablet) 44.dp else 36.dp
    val actionIconSize = if (isTablet) 20.sp else 16.sp
    val row1Height = if (isTablet) 44.dp else 36.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (isTablet) 6.dp else 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(if (isTablet) 10.dp else 8.dp)
    ) {
        // Row 1: Header (Title, Board-Size Pill, & Action Icons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(row1Height),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 10.dp else 8.dp)
            ) {
                Text(
                    text = "Kancentration",
                    style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )

                // Board-Size Pill (positioned next to the word "Kancentration")
                Surface(
                    shape = RoundedCornerShape(if (isTablet) 10.dp else 8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(if (isTablet) 10.dp else 8.dp))
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Cycle board dimension",
                            onClick = { onCycleDimensionClick() }
                        )
                ) {
                    Text(
                        text = "${uiState.settings.dimension.size}×${uiState.settings.dimension.size}",
                        style = if (isTablet) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(
                            horizontal = if (isTablet) 12.dp else 8.dp,
                            vertical = if (isTablet) 5.dp else 3.dp
                        )
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 6.dp else 2.dp)
            ) {
                // DLC Store Button
                IconButton(
                    onClick = onDlcStoreClick,
                    modifier = Modifier.size(actionButtonSize)
                ) {
                    Text(text = "🛒", fontSize = actionIconSize)
                }

                // Settings Button
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(actionButtonSize)
                ) {
                    Text(text = "⚙️", fontSize = actionIconSize)
                }

                // Restart Button
                IconButton(
                    onClick = onRestartClick,
                    modifier = Modifier.size(actionButtonSize)
                ) {
                    Text(text = "🔄", fontSize = actionIconSize)
                }
            }
        }

        // Row 2: Telemetry (Timer & Moves on Left, Scores/Matches on Right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Timer Badge with Moves field directly to its right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 14.dp else 10.dp)
            ) {
                // Timer Badge
                val timerBg = when {
                    uiState.isTimerLowWarning -> Color(0xFFFFEBEE)
                    uiState.isTimeAttack -> Color(0xFFFFF3E0)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val timerBorder = when {
                    uiState.isTimerLowWarning -> Color(0xFFE53935)
                    uiState.isTimeAttack -> Color(0xFFFFB74D)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                }
                val timerText = when {
                    uiState.isTimerLowWarning -> Color(0xFFC62828)
                    uiState.isTimeAttack -> Color(0xFFE65100)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = timerBg,
                    border = BorderStroke(1.dp, timerBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = if (isTablet) 14.dp else 10.dp,
                            vertical = if (isTablet) 5.dp else 3.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                uiState.isTimerLowWarning -> "🚨"
                                uiState.isTimeAttack -> "⏳"
                                else -> "⏱"
                            },
                            fontSize = if (isTablet) 14.sp else 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = uiState.formattedTimer,
                            style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = timerText
                        )
                    }
                }

                // Moves field to the right of the timer
                Text(
                    text = "Moves: ${uiState.totalMoves}",
                    style = if (isTablet) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right: Score & Turn Info
            if (uiState.isTwoPlayer) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(if (isTablet) 10.dp else 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Player 1 Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (uiState.activePlayer == 1) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            if (uiState.activePlayer == 1) 2.dp else 1.dp,
                            if (uiState.activePlayer == 1) Color(0xFF2E7D32) else Color.LightGray
                        )
                    ) {
                        Text(
                            text = "P1: ${uiState.player1Score}",
                            style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                            fontWeight = if (uiState.activePlayer == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.activePlayer == 1) Color(0xFF1B5E20) else Color.Gray,
                            modifier = Modifier.padding(
                                horizontal = if (isTablet) 12.dp else 8.dp,
                                vertical = if (isTablet) 6.dp else 4.dp
                            )
                        )
                    }

                    // Player 2 Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (uiState.activePlayer == 2) Color(0xFFFCE4EC) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            if (uiState.activePlayer == 2) 2.dp else 1.dp,
                            if (uiState.activePlayer == 2) Color(0xFFC2185B) else Color.LightGray
                        )
                    ) {
                        Text(
                            text = "P2: ${uiState.player2Score}",
                            style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                            fontWeight = if (uiState.activePlayer == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.activePlayer == 2) Color(0xFF880E4F) else Color.Gray,
                            modifier = Modifier.padding(
                                horizontal = if (isTablet) 12.dp else 8.dp,
                                vertical = if (isTablet) 6.dp else 4.dp
                            )
                        )
                    }
                }
            } else {
                Text(
                    text = "Matches: ${uiState.totalMatches}/${uiState.totalPairs}",
                    style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
