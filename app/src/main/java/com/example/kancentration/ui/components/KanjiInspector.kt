package com.example.kancentration.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kancentration.data.model.KanjiData
import com.example.kancentration.theme.AizomeIndigo
import com.example.kancentration.theme.SumiCharcoal
import com.example.kancentration.theme.WashiPaper
import com.example.kancentration.util.KanaConverter

/**
 * Extended lexicon inspector card showcasing Kanji details, distinct readings
 * with multi-line natural text wrapping, distinction clues, and a clickable Wiktionary intent link.
 */
@Composable
fun KanjiInspector(
    kanji: KanjiData?,
    modifier: Modifier = Modifier,
    showRomaji: Boolean = false,
    showStrokeCount: Boolean = true,
    useSerifFont: Boolean = true,
    isPhoneLandscape: Boolean = false,
    onWiktionaryClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTabletWidth = maxWidth >= 480.dp
            val isExpandedTablet = maxWidth >= 680.dp
            val inspectorHeight = maxHeight
            val inspectorWidth = maxWidth

            if (kanji == null) {
                // Calm zen placeholder state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (isTabletWidth) 24.dp else 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎴 Flip cards to inspect Kanji readings, meanings & Twin-Trap clues",
                        style = if (isTabletWidth) MaterialTheme.typography.titleMedium else if (isPhoneLandscape) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center
                    )
                }
            } else if (isPhoneLandscape) {
                // Phone-Landscape Layout:
                // - Kanji tile size reduced by 50%
                // - All text elements reduced by 50%
                // - Lightbulb (tip) element aligned with the left boundary of kanji-inspector element,
                //   with text flowing all the way to the right boundary, and wrapping.
                val paddingH = 12.dp
                val paddingV = 8.dp

                val availableHeight = (inspectorHeight - paddingV * 2).coerceAtLeast(48.dp)
                val maxSealWidth = (inspectorWidth * 0.35f).coerceAtMost(180.dp)
                val clueReservedHeight = 28.dp
                val availableForSeal = (availableHeight - clueReservedHeight).coerceAtLeast(48.dp)
                val baseSealHeight = minOf(availableForSeal, maxSealWidth / 0.82f).coerceAtLeast(48.dp)

                // 50% Tile Dimensions
                val phoneSealSize = 54.dp
                val phoneCharacterFontSize = 30.sp
                val phoneWiktionaryLabelSize = 10.sp

                // 50% Typography Sizes
                val phoneMeaningFontSize = 13.sp
                val phoneMeaningLineHeight = 16.sp
                val phoneKakiJunFontSize = 11.sp
                val phoneReadingFontSize = 14.sp
                val phoneReadingLineHeight = 18.sp
                val phoneTipFontSize = 12.sp
                val phoneTipLineHeight = 16.sp

                val onReadingsFormatted = if (showRomaji) {
                    kanji.onReadings.map { "$it (${KanaConverter.toRomaji(it)})" }
                } else {
                    kanji.onReadings
                }
                val kunReadingsFormatted = if (showRomaji) {
                    kanji.kunReadings.map { "$it (${KanaConverter.toRomaji(it)})" }
                } else {
                    kanji.kunReadings
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = paddingH, vertical = paddingV),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Section: Kanji Tile on left (with Wiktionary link below it), Header & Readings on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left Column: Kanji Box Tile with Wiktionary link directly beneath it
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    role = androidx.compose.ui.semantics.Role.Button,
                                    onClickLabel = "Open Wiktionary entry in browser"
                                ) {
                                    onWiktionaryClick?.invoke()
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(kanji.wiktionaryUrl)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = WashiPaper,
                                border = BorderStroke(1.5.dp, AizomeIndigo),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(phoneSealSize)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = kanji.character,
                                        fontSize = phoneCharacterFontSize,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif,
                                        color = SumiCharcoal,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        softWrap = false,
                                        style = LocalTextStyle.current.copy(
                                            lineHeight = phoneCharacterFontSize,
                                            textAlign = TextAlign.Center,
                                            platformStyle = PlatformTextStyle(
                                                includeFontPadding = false
                                            )
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = "Wiktionary ↗",
                                fontSize = phoneWiktionaryLabelSize,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Right Column: English Meanings & Stroke Count on row 1, Readings on row 2
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = kanji.meanings.joinToString(", ").ifEmpty { "Kanji" },
                                    fontSize = phoneMeaningFontSize,
                                    lineHeight = phoneMeaningLineHeight,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                if (showStrokeCount) {
                                    Spacer(modifier = Modifier.width(6.dp))

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = "${kanji.strokeCount}画",
                                            fontSize = phoneKakiJunFontSize,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            if (onReadingsFormatted.isNotEmpty()) {
                                Text(
                                    text = "on: ${onReadingsFormatted.joinToString(", ")}",
                                    fontSize = phoneReadingFontSize,
                                    lineHeight = phoneReadingLineHeight,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (kunReadingsFormatted.isNotEmpty()) {
                                Text(
                                    text = "kun: ${kunReadingsFormatted.joinToString(", ")}",
                                    fontSize = phoneReadingFontSize,
                                    lineHeight = phoneReadingLineHeight,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bottom Row: Lightbulb (tip) element aligned with the left boundary of the kanji-inspector element,
                    // with text flowing all the way to the right boundary, and wrapping.
                    if (kanji.distinctionTip.isNotEmpty()) {
                        Text(
                            text = "💡 ${kanji.distinctionTip}",
                            fontSize = phoneTipFontSize,
                            lineHeight = phoneTipLineHeight,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "Kanji: ${kanji.character} (ID: ${kanji.id})",
                            fontSize = phoneTipFontSize,
                            lineHeight = phoneTipLineHeight,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                val isCompactHeight = inspectorHeight < 140.dp
                val paddingH = if (isTabletWidth) 18.dp else 12.dp
                val paddingV = if (isCompactHeight) 6.dp else if (isTabletWidth) 12.dp else 8.dp
                val availableHeight = (inspectorHeight - paddingV * 2).coerceAtLeast(48.dp)
                val maxSealWidth = (inspectorWidth * 0.35f).coerceAtMost(if (isExpandedTablet) 240.dp else 180.dp)
                val clueReservedHeight = if (isCompactHeight) 24.dp else 36.dp
                val availableForSeal = (availableHeight - clueReservedHeight).coerceAtLeast(48.dp)
                val sealHeight = minOf(availableForSeal, maxSealWidth / 0.82f).coerceAtLeast(48.dp)
                val sealWidth = sealHeight * 0.82f
                val characterFontSize = (sealHeight.value * 0.44f).coerceIn(34f, 96f).sp
                val wiktionaryLabelSize = (sealHeight.value * 0.08f).coerceIn(9f, 14f).sp

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = paddingH, vertical = paddingV),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Tile (Kanji Box): Matches actual game tile, Tappable Wiktionary Link
                    Surface(
                        shape = RoundedCornerShape(if (isTabletWidth) 14.dp else 10.dp),
                        color = WashiPaper,
                        border = BorderStroke(2.dp, AizomeIndigo),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(width = sealWidth, height = sealHeight)
                            .clip(RoundedCornerShape(if (isTabletWidth) 14.dp else 10.dp))
                            .clickable(
                                role = androidx.compose.ui.semantics.Role.Button,
                                onClickLabel = "Open Wiktionary entry in browser"
                            ) {
                                onWiktionaryClick?.invoke()
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(kanji.wiktionaryUrl)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = kanji.character,
                                fontSize = characterFontSize,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif,
                                color = SumiCharcoal,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false,
                                style = LocalTextStyle.current.copy(
                                    lineHeight = characterFontSize,
                                    textAlign = TextAlign.Center,
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )

                            Text(
                                text = "Wiktionary ↗",
                                fontSize = wiktionaryLabelSize,
                                fontWeight = FontWeight.SemiBold,
                                color = AizomeIndigo,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = if (isCompactHeight) 2.dp else 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(if (isCompactHeight) 10.dp else if (isTabletWidth) 18.dp else 14.dp))

                    // Right Information Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        // English Definition and Stroke Count Header (Top Aligned with Kanji Box)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = kanji.meanings.joinToString(", ").ifEmpty { "Kanji" },
                                style = if (isTabletWidth && !isCompactHeight) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (showStrokeCount) {
                                Spacer(modifier = Modifier.width(if (isTabletWidth) 12.dp else 8.dp))

                                Surface(
                                    shape = RoundedCornerShape(if (isTabletWidth) 12.dp else 8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "${kanji.strokeCount}画",
                                        fontSize = if (isTabletWidth) 26.sp else 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = if (isTabletWidth) 16.dp else 12.dp,
                                            vertical = if (isCompactHeight) 4.dp else 6.dp
                                        )
                                    )
                                }
                            }
                        }

                        // On-yomi, Kun-yomi Readings, and Lightbulb Tip
                        val onReadingsFormatted = if (showRomaji) {
                            kanji.onReadings.map { "$it (${KanaConverter.toRomaji(it)})" }
                        } else {
                            kanji.onReadings
                        }
                        val kunReadingsFormatted = if (showRomaji) {
                            kanji.kunReadings.map { "$it (${KanaConverter.toRomaji(it)})" }
                        } else {
                            kanji.kunReadings
                        }

                        val readingSpacing = if (isCompactHeight) 2.dp else 4.dp
                        val readingFontSize = if (isTabletWidth && !isCompactHeight) 32.sp else if (isCompactHeight) (sealHeight.value * 0.22f).coerceIn(18f, 24f).sp else 28.sp
                        val readingLineHeight = if (isTabletWidth && !isCompactHeight) 38.sp else if (isCompactHeight) (readingFontSize.value * 1.2f).sp else 34.sp

                        // Doubled tip text size (28sp on tablet, 24sp on mobile)
                        val tipFontSize = if (isTabletWidth && !isCompactHeight) 28.sp else if (isCompactHeight) (sealHeight.value * 0.18f).coerceIn(16f, 22f).sp else 24.sp
                        val tipLineHeight = if (isTabletWidth && !isCompactHeight) 34.sp else if (isCompactHeight) (tipFontSize.value * 1.2f).sp else 30.sp

                        val density = LocalDensity.current
                        val readingLineHeightDp = with(density) { readingLineHeight.toDp() }
                        val headerHeight = if (isTabletWidth && !isCompactHeight) 40.dp else 28.dp
                        val topSpacerHeight = ((sealHeight / 2) - readingLineHeightDp - (readingSpacing / 2) - headerHeight).coerceAtLeast(if (isCompactHeight) 2.dp else 6.dp)

                        Spacer(modifier = Modifier.height(topSpacerHeight))

                        // Column with identical spacing between on, kun, and lightbulb tip lines
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(readingSpacing)
                        ) {
                            if (onReadingsFormatted.isNotEmpty()) {
                                Text(
                                    text = "on: ${onReadingsFormatted.joinToString(", ")}",
                                    fontSize = readingFontSize,
                                    lineHeight = readingLineHeight,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (kunReadingsFormatted.isNotEmpty()) {
                                Text(
                                    text = "kun: ${kunReadingsFormatted.joinToString(", ")}",
                                    fontSize = readingFontSize,
                                    lineHeight = readingLineHeight,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (kanji.distinctionTip.isNotEmpty()) {
                                Text(
                                    text = "💡 ${kanji.distinctionTip}",
                                    fontSize = tipFontSize,
                                    lineHeight = tipLineHeight,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = "Kanji: ${kanji.character} (ID: ${kanji.id})",
                                    fontSize = tipFontSize,
                                    lineHeight = tipLineHeight,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
