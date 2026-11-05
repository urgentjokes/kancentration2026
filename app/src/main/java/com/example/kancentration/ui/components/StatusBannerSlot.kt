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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kancentration.data.model.KanjiData
import com.example.kancentration.theme.AizomeIndigo
import com.example.kancentration.theme.AmberTwinTrap
import com.example.kancentration.theme.AmberTwinTrapSurface
import com.example.kancentration.theme.SumiCharcoal
import com.example.kancentration.theme.TakeBamboo
import com.example.kancentration.theme.TakeBambooSurface
import com.example.kancentration.theme.WashiPaper
import com.example.kancentration.ui.viewmodel.GameUiState

/**
 * Fixed-height status banner slot displaying the "Almost"
 * alert banner when activeTwinTrapBanner is populated, and a compact celebration /
 * time-up banner when the game ends.
 *
 * Designed with a strict fixed height (46.dp on phones) to guarantee zero layout shift of the board grid.
 */
@Composable
fun StatusBannerSlot(
    uiState: GameUiState,
    onRestart: () -> Unit,
    onDismissTwinTrap: () -> Unit = {},
    onFlashCards: () -> Unit = {},
    onInspectKanji: (KanjiData) -> Unit = {},
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
    val chipTextSize = when {
        isPhone -> 10.sp
        isTablet -> 18.sp
        else -> 16.sp
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
                    shape = RoundedCornerShape(if (isPhone) 8.dp else if (isTablet) 14.dp else 12.dp),
                    color = containerColor,
                    border = BorderStroke(1.5.dp, borderColor),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isPhone) {
                        // Phone (portrait and landscape): Single-row 46dp Victory / Time's Up banner with zero clipping
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isTimeUp) {
                                Text(
                                    text = "🛑 ${uiState.gameOverMessage}",
                                    fontSize = bannerTextSize,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Text(
                                        text = if (uiState.isTwoPlayer) "🎉 ${uiState.gameOverMessage.ifEmpty { "Victory!" }}" else "🎉 Victory!",
                                        fontSize = bannerTextSize,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(percent = 50),
                                        color = Color.White.copy(alpha = 0.9f),
                                        border = BorderStroke(1.dp, TakeBamboo.copy(alpha = 0.6f)),
                                        modifier = Modifier
                                            .height(buttonHeight)
                                            .clickable(
                                                role = androidx.compose.ui.semantics.Role.Button,
                                                onClickLabel = "Flash cards",
                                                onClick = onFlashCards
                                            )
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .padding(horizontal = 10.dp)
                                        ) {
                                            Text(
                                                text = "Flash cards",
                                                fontSize = chipTextSize,
                                                fontWeight = FontWeight.SemiBold,
                                                color = textColor,
                                                textAlign = TextAlign.Center,
                                                style = LocalTextStyle.current.copy(
                                                    lineHeight = chipTextSize,
                                                    textAlign = TextAlign.Center,
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                                    lineHeightStyle = LineHeightStyle(
                                                        alignment = LineHeightStyle.Alignment.Center,
                                                        trim = LineHeightStyle.Trim.Both
                                                    )
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = onRestart,
                                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier.height(buttonHeight)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Play Again",
                                            fontSize = buttonTextSize,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            style = LocalTextStyle.current.copy(
                                                lineHeight = buttonTextSize,
                                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                                lineHeightStyle = LineHeightStyle(
                                                    alignment = LineHeightStyle.Alignment.Center,
                                                    trim = LineHeightStyle.Trim.Both
                                                )
                                            )
                                        )
                                        Text(
                                            text = "🔄",
                                            fontSize = (buttonTextSize.value * 0.95f).sp,
                                            style = LocalTextStyle.current.copy(
                                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Tablet: Spacious two-row Victory / Time's Up layout
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    horizontal = if (isTablet) 16.dp else 12.dp,
                                    vertical = if (isTablet) 8.dp else 6.dp
                                ),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isTimeUp) {
                                    Text(
                                        text = "🛑 ${uiState.gameOverMessage}",
                                        fontSize = bannerTextSize,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp),
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Text(
                                            text = if (uiState.isTwoPlayer) "🎉 ${uiState.gameOverMessage.ifEmpty { "Victory!" }}" else "🎉 Victory!",
                                            fontSize = bannerTextSize,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(percent = 50),
                                            color = Color.White.copy(alpha = 0.9f),
                                            border = BorderStroke(1.dp, TakeBamboo.copy(alpha = 0.6f)),
                                            modifier = Modifier
                                                .height(buttonHeight)
                                                .clickable(
                                                    role = androidx.compose.ui.semantics.Role.Button,
                                                    onClickLabel = "Flash cards",
                                                    onClick = onFlashCards
                                                )
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .padding(horizontal = if (isTablet) 16.dp else 12.dp)
                                            ) {
                                                Text(
                                                    text = "Flash cards",
                                                    fontSize = chipTextSize,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = textColor,
                                                    textAlign = TextAlign.Center,
                                                    style = LocalTextStyle.current.copy(
                                                        lineHeight = chipTextSize,
                                                        textAlign = TextAlign.Center,
                                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                                        lineHeightStyle = LineHeightStyle(
                                                            alignment = LineHeightStyle.Alignment.Center,
                                                            trim = LineHeightStyle.Trim.Both
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = onRestart,
                                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = if (isTablet) 16.dp else 12.dp, vertical = 0.dp),
                                    modifier = Modifier.height(buttonHeight)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxHeight()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Play Again",
                                                fontSize = buttonTextSize,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                style = LocalTextStyle.current.copy(
                                                    lineHeight = buttonTextSize,
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                                    lineHeightStyle = LineHeightStyle(
                                                        alignment = LineHeightStyle.Alignment.Center,
                                                        trim = LineHeightStyle.Trim.Both
                                                    )
                                                )
                                            )
                                            Text(
                                                text = "🔄",
                                                fontSize = (buttonTextSize.value * 0.95f).sp,
                                                style = LocalTextStyle.current.copy(
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = if (uiState.isTwoPlayer) {
                                    "Player 1: ${uiState.player1Score} matches • Player 2: ${uiState.player2Score} matches"
                                } else {
                                    "Total Matches: ${uiState.totalMatches}/${uiState.totalPairs} • Total Moves: ${uiState.totalMoves}"
                                },
                                fontSize = if (isTablet) 22.sp else 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            uiState.activeTwinTrapBanner != null -> {
                val parts = uiState.activeTwinTrapBanner.split(" vs ")
                val firstChar = parts.getOrNull(0)?.trim()
                val secondChar = parts.getOrNull(1)?.trim()
                val firstKanji = firstChar?.let { ch -> uiState.cards.firstOrNull { it.kanji.character == ch }?.kanji }
                val secondKanji = secondChar?.let { ch -> uiState.cards.firstOrNull { it.kanji.character == ch }?.kanji }
                val tileFont = if (uiState.settings.useSerifFont) FontFamily.Serif else FontFamily.SansSerif

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
                            // Phone (portrait and landscape): 50% reduced height single-row banner with tappable mini-tiles
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

                                    if (firstKanji != null && secondKanji != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            val isFirstSelected = uiState.selectedKanjiForInspector?.id == firstKanji.id
                                            val isSecondSelected = uiState.selectedKanjiForInspector?.id == secondKanji.id

                                            Surface(
                                                shape = RoundedCornerShape(5.dp),
                                                color = WashiPaper,
                                                border = BorderStroke(
                                                    width = if (isFirstSelected) 1.5.dp else 1.dp,
                                                    color = if (isFirstSelected) AizomeIndigo else AmberTwinTrap
                                                ),
                                                shadowElevation = if (isFirstSelected) 2.dp else 0.dp,
                                                modifier = Modifier
                                                    .height(26.dp)
                                                    .clickable(
                                                        role = androidx.compose.ui.semantics.Role.Button,
                                                        onClickLabel = "Inspect ${firstKanji.character}"
                                                    ) {
                                                        onInspectKanji(firstKanji)
                                                    }
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .padding(horizontal = 6.dp)
                                                ) {
                                                    Text(
                                                        text = firstKanji.character,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = tileFont,
                                                        color = if (isFirstSelected) SumiCharcoal else Color(0xFF92400E),
                                                        style = LocalTextStyle.current.copy(
                                                            lineHeight = 15.sp,
                                                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                        )
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "vs",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF92400E)
                                            )

                                            Surface(
                                                shape = RoundedCornerShape(5.dp),
                                                color = WashiPaper,
                                                border = BorderStroke(
                                                    width = if (isSecondSelected) 1.5.dp else 1.dp,
                                                    color = if (isSecondSelected) AizomeIndigo else AmberTwinTrap
                                                ),
                                                shadowElevation = if (isSecondSelected) 2.dp else 0.dp,
                                                modifier = Modifier
                                                    .height(26.dp)
                                                    .clickable(
                                                        role = androidx.compose.ui.semantics.Role.Button,
                                                        onClickLabel = "Inspect ${secondKanji.character}"
                                                    ) {
                                                        onInspectKanji(secondKanji)
                                                    }
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .padding(horizontal = 6.dp)
                                                ) {
                                                    Text(
                                                        text = secondKanji.character,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = tileFont,
                                                        color = if (isSecondSelected) SumiCharcoal else Color(0xFF92400E),
                                                        style = LocalTextStyle.current.copy(
                                                            lineHeight = 15.sp,
                                                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "Tap tile to compare",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF78350F),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    } else {
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
                            // Tablet: Full-size two-row layout with tappable mini-tiles
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

                                // Bottom Row: Interactive mini-tiles or clue text
                                if (firstKanji != null && secondKanji != null) {
                                    val isFirstSelected = uiState.selectedKanjiForInspector?.id == firstKanji.id
                                    val isSecondSelected = uiState.selectedKanjiForInspector?.id == secondKanji.id

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = WashiPaper,
                                            border = BorderStroke(
                                                width = if (isFirstSelected) 2.dp else 1.dp,
                                                color = if (isFirstSelected) AizomeIndigo else AmberTwinTrap
                                            ),
                                            modifier = Modifier.clickable { onInspectKanji(firstKanji) }
                                        ) {
                                            Text(
                                                text = firstKanji.character,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = tileFont,
                                                color = if (isFirstSelected) SumiCharcoal else Color(0xFF92400E),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = "vs",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = WashiPaper,
                                            border = BorderStroke(
                                                width = if (isSecondSelected) 2.dp else 1.dp,
                                                color = if (isSecondSelected) AizomeIndigo else AmberTwinTrap
                                            ),
                                            modifier = Modifier.clickable { onInspectKanji(secondKanji) }
                                        ) {
                                            Text(
                                                text = secondKanji.character,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = tileFont,
                                                color = if (isSecondSelected) SumiCharcoal else Color(0xFF92400E),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = "Tap either tile to compare in Inspector",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF78350F),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
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
            }

            else -> {
                // Fixed-height spacer guaranteeing zero board jitter
                Spacer(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
