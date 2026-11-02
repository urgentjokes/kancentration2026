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
            } else {
                // Unified Responsive Inspector Layout (Phone Landscape, Phone Portrait, Tablet):
                // - 50% reduced Kanji tile size (54dp phone, 72dp tablet)
                // - 50% reduced on- and kun-yomi readings (14sp phone, 16sp tablet)
                // - 50% reduced lightbulb text (12sp phone, 14sp tablet)
                // - Lightbulb (tip) element aligned with the left boundary of kanji-inspector element,
                //   with text flowing all the way to the right boundary, and wrapping.
                // - Wiktionary ↗ link positioned beneath the kanji preview tile in primary theme color.
                val isCompactHeight = inspectorHeight < 140.dp
                val paddingH = if (isTabletWidth) 16.dp else 12.dp
                val paddingV = if (isCompactHeight) 6.dp else if (isTabletWidth) 12.dp else 8.dp

                // 50% Tile Dimensions
                val sealSize = if (isTabletWidth) 72.dp else 54.dp
                val characterFontSize = if (isTabletWidth) 40.sp else 30.sp
                val wiktionaryLabelSize = if (isTabletWidth) 11.sp else 10.sp

                // 50% Typography Sizes
                val meaningFontSize = if (isTabletWidth) 16.sp else 13.sp
                val meaningLineHeight = if (isTabletWidth) 20.sp else 16.sp
                val kakiJunFontSize = if (isTabletWidth) 13.sp else 11.sp
                val readingFontSize = if (isTabletWidth) 16.sp else 14.sp
                val readingLineHeight = if (isTabletWidth) 20.sp else 18.sp
                val tipFontSize = if (isTabletWidth) 14.sp else 12.sp
                val tipLineHeight = if (isTabletWidth) 18.sp else 16.sp

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

                val readingSpacing = if (isTabletWidth) 4.dp else 2.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = paddingH, vertical = paddingV),
                    verticalArrangement = if (isPhoneLandscape) {
                        Arrangement.SpaceBetween
                    } else {
                        Arrangement.spacedBy(if (isCompactHeight) 4.dp else 8.dp)
                    }
                ) {
                    // Top Section: Kanji Tile on left (with Wiktionary link below it), Meanings & Readings on right
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
                                shape = RoundedCornerShape(if (isTabletWidth) 10.dp else 8.dp),
                                color = WashiPaper,
                                border = BorderStroke(1.5.dp, AizomeIndigo),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(sealSize)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
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
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = "Wiktionary ↗",
                                fontSize = wiktionaryLabelSize,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Spacer(modifier = Modifier.width(if (isTabletWidth) 16.dp else 12.dp))

                        // Right Column: English Meanings & Stroke Count, on-yomi, kun-yomi
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(readingSpacing)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = kanji.meanings.joinToString(", ").ifEmpty { "Kanji" },
                                    fontSize = meaningFontSize,
                                    lineHeight = meaningLineHeight,
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
                                            fontSize = kakiJunFontSize,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(
                                                horizontal = if (isTabletWidth) 10.dp else 8.dp,
                                                vertical = 2.dp
                                            )
                                        )
                                    }
                                }
                            }

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
                        }
                    }

                    if (!isPhoneLandscape && !isCompactHeight) {
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Bottom Section: Lightbulb (tip) element aligned with the left boundary of the kanji-inspector element,
                    // with text flowing all the way to the right boundary, and wrapping.
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
