package com.example.kancentration.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kancentration.data.model.KanjiData
import com.example.kancentration.theme.AizomeIndigo
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
    useSerifFont: Boolean = true,
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
        if (kanji == null) {
            // Calm zen placeholder state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎴 Flip cards to inspect Kanji readings, meanings & Twin-Trap clues",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Hanko Seal Tile: Tappable Wiktionary Link
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AizomeIndigo.copy(alpha = 0.08f),
                    border = BorderStroke(1.5.dp, AizomeIndigo),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(0.82f)
                        .clip(RoundedCornerShape(14.dp))
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
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = kanji.character,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif,
                            color = AizomeIndigo
                        )

                        Text(
                            text = "Wiktionary ↗",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AizomeIndigo.copy(alpha = 0.85f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right Information Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Meanings and Stroke Count Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = kanji.meanings.joinToString(", ").ifEmpty { "Kanji" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "${kanji.strokeCount}画",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // On-yomi & Kun-yomi Readings (on separate lines, wrapping naturally without ellipsis)
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
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (onReadingsFormatted.isNotEmpty()) {
                            Text(
                                text = "on: ${onReadingsFormatted.joinToString(", ")}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (kunReadingsFormatted.isNotEmpty()) {
                            Text(
                                text = "kun: ${kunReadingsFormatted.joinToString(", ")}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Distinction Tip / Clue
                    if (kanji.distinctionTip.isNotEmpty()) {
                        Text(
                            text = "💡 ${kanji.distinctionTip}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    } else {
                        Text(
                            text = "Kanji: ${kanji.character} (ID: ${kanji.id})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
