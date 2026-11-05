package com.example.kancentration.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kancentration.data.model.BoardCard
import com.example.kancentration.data.model.GameSettings
import com.example.kancentration.data.model.KanjiData
import com.example.kancentration.theme.AizomeIndigo
import com.example.kancentration.theme.SumiCharcoal
import com.example.kancentration.theme.TakeBamboo
import com.example.kancentration.theme.WashiPaper
import com.example.kancentration.util.KanaConverter

/**
 * Flash-card screen displaying all kanji that appeared over the course of the game.
 *
 * Each card contains the same information as KanjiInspector:
 * - Tapping the kanji seal tile takes the user to the Wiktionary page.
 * - Touching anywhere else on the card toggles definition and reading text on/off.
 * - Romaji is displayed or hidden according to the showRomaji setting in Settings.
 */
@Composable
fun FlashCardsScreen(
    cards: List<BoardCard>,
    settings: GameSettings,
    onWiktionaryClick: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    val quadGroups = remember(cards) {
        cards.groupBy { it.quadId }
            .values
            .map { group -> group.map { it.kanji }.distinctBy { it.id } }
            .filter { it.isNotEmpty() }
    }
    val kanjiList = remember(quadGroups) {
        quadGroups.flatten().distinctBy { it.id }
    }

    var globalRevealedState by rememberSaveable { mutableStateOf(false) }
    var globalVersion by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .displayCutoutPadding(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text("← Back", style = MaterialTheme.typography.labelLarge)
                        }

                        Text(
                            text = "Flash Cards",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TakeBamboo.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${kanjiList.size} Kanji",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TakeBamboo,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            globalRevealedState = !globalRevealedState
                            globalVersion++
                        },
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = if (globalRevealedState) "Hide All" else "Reveal All",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (kanjiList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No kanji cards found from the current game.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 800.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    quadGroups.forEachIndexed { index, pair ->
                        if (pair.size > 1) {
                            item(key = "quad_header_$index") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = if (index > 0) 6.dp else 0.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                    ) {
                                        Text(
                                            text = "Confusable Pair ${index + 1}: ${pair.joinToString(" & ") { it.character }}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                        items(
                            items = pair,
                            key = { it.id }
                        ) { kanji ->
                            FlashCardItem(
                                kanji = kanji,
                                settings = settings,
                                initialRevealed = globalRevealedState,
                                globalVersion = globalVersion,
                                onWiktionaryClick = onWiktionaryClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashCardItem(
    kanji: KanjiData,
    settings: GameSettings,
    initialRevealed: Boolean,
    globalVersion: Int,
    onWiktionaryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRevealed by rememberSaveable(kanji.id, globalVersion) {
        mutableStateOf(initialRevealed)
    }

    val onReadingsFormatted = if (settings.showRomaji) {
        kanji.onReadings.map { "$it (${KanaConverter.toRomaji(it)})" }
    } else {
        kanji.onReadings
    }
    val kunReadingsFormatted = if (settings.showRomaji) {
        kanji.kunReadings.map { "$it (${KanaConverter.toRomaji(it)})" }
    } else {
        kanji.kunReadings
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                role = Role.Button,
                onClickLabel = if (isRevealed) "Hide definition and readings" else "Reveal definition and readings",
                onClick = { isRevealed = !isRevealed }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left Column: Interactive Kanji Tile that opens Wiktionary
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "Open Wiktionary entry in browser for ${kanji.character}"
                    ) {
                        onWiktionaryClick()
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
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .kanjiCalligraphyGrid(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = kanji.character,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = if (settings.useSerifFont) FontFamily.Serif else FontFamily.SansSerif,
                            color = SumiCharcoal,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            style = LocalTextStyle.current.copy(
                                lineHeight = 36.sp,
                                textAlign = TextAlign.Center,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Wiktionary ↗",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Right Column: Definition & Readings, toggled on/off by touching the card
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isRevealed) {
                    // Meanings and Stroke Count Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = kanji.meanings.joinToString(", ").ifEmpty { "Kanji" },
                            fontSize = 15.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (settings.showStrokeCount) {
                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "${kanji.strokeCount}画",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // On'yomi reading
                    if (onReadingsFormatted.isNotEmpty()) {
                        Text(
                            text = "on: ${onReadingsFormatted.joinToString(", ")}",
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Kun'yomi reading
                    if (kunReadingsFormatted.isNotEmpty()) {
                        Text(
                            text = "kun: ${kunReadingsFormatted.joinToString(", ")}",
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Distinction tip
                    if (kanji.distinctionTip.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "💡 ${kanji.distinctionTip}",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // Hidden State: prompts player to touch card to reveal definition & readings
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "Definitions & Readings Hidden",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            if (settings.showStrokeCount) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "${kanji.strokeCount}画",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "👆 Tap card to reveal definition & readings",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
