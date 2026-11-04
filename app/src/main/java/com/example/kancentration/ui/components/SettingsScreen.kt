package com.example.kancentration.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kancentration.data.model.BoardDimension
import com.example.kancentration.data.model.GameSettings
import com.example.kancentration.data.model.GameType
import com.example.kancentration.theme.AizomeIndigo
import com.example.kancentration.theme.KinpakuGold

/**
 * Dedicated full-screen Settings experience:
 * - Segmented controls for Kanji Typography (Mincho/Serif vs Gothic/Sans-Serif)
 * - Segmented switches for Board Dimensions (4x4, 6x6, 8x8)
 * - Configurable Failed Match Exposure Time (0.8s, 1.2s, 1.5s, 2.0s, 3.0s)
 * - Learning and Display preference switches (Confusable Twin Alerts, Show Romaji, Show Stroke Count, Extra Turn)
 * - Traditional palette presets (Aizome Indigo, Washi Ivory, Bamboo Green) with live preview
 */
@Composable
fun SettingsScreen(
    currentSettings: GameSettings,
    onApplySettings: (GameSettings) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    var selectedPackId by remember { mutableStateOf(currentSettings.selectedPackId) }
    var selectedGameType by remember { mutableStateOf(currentSettings.gameType) }
    var bonusTurn by remember { mutableStateOf(currentSettings.bonusTurnOnMatch) }
    var twinTrapHints by remember { mutableStateOf(currentSettings.showTwinTrapHints) }
    var showRomaji by remember { mutableStateOf(currentSettings.showRomaji) }
    var showStrokeCount by remember { mutableStateOf(currentSettings.showStrokeCount) }
    var useSerifFont by remember { mutableStateOf(currentSettings.useSerifFont) }
    var soundEnabled by remember { mutableStateOf(currentSettings.soundEnabled) }
    var mismatchDelayMs by remember { mutableStateOf(currentSettings.mismatchDelayMs) }
    var selectedTileColor by remember { mutableStateOf(currentSettings.tileColor) }
    var selectedKanjiColor by remember { mutableStateOf(currentSettings.kanjiColor) }
    var customHex by remember { mutableStateOf("") }
    var hexError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val needsRestart = selectedGameType != currentSettings.gameType ||
            selectedPackId != currentSettings.selectedPackId

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
                        .height(44.dp)
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
                            Text("Cancel", style = MaterialTheme.typography.labelLarge)
                        }

                        Text(
                            text = "Game Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = {
                            onApplySettings(
                                GameSettings(
                                    dimension = currentSettings.dimension,
                                    gameType = selectedGameType,
                                    bonusTurnOnMatch = bonusTurn,
                                    showTwinTrapHints = twinTrapHints,
                                    showRomaji = showRomaji,
                                    showStrokeCount = showStrokeCount,
                                    useSerifFont = useSerifFont,
                                    soundEnabled = soundEnabled,
                                    tileColor = selectedTileColor,
                                    kanjiColor = selectedKanjiColor,
                                    mismatchDelayMs = mismatchDelayMs,
                                    selectedPackId = selectedPackId
                                )
                            )
                        },
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (needsRestart) "Apply & Restart" else "Apply")
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val configuration = LocalConfiguration.current
        val isTablet = configuration.screenWidthDp >= 600

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = if (isTablet) 32.dp else 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
            // 1. Kanji Card Pack Section
            SettingsSectionCard(
                title = "Kanji Card Pack"
            ) {
                val leftPacks = listOf(
                    "base_pack" to "Starter pack",
                    "grade_1" to "1st grade",
                    "grade_2" to "2nd grade",
                    "grade_3" to "3rd grade"
                )
                val rightPacks = listOf(
                    "grade_4" to "4th grade",
                    "grade_5" to "5th grade",
                    "grade_6" to "6th grade"
                )

                Column {
                    for (i in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val left = leftPacks[i]
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedPackId = left.first },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPackId == left.first,
                                    onClick = { selectedPackId = left.first }
                                )
                                Text(
                                    text = left.second,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (i < rightPacks.size) {
                                val right = rightPacks[i]
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedPackId = right.first },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedPackId == right.first,
                                        onClick = { selectedPackId = right.first }
                                    )
                                    Text(
                                        text = right.second,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 2. Kanji Typography Section
            SettingsSectionCard(
                title = "Tile font"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = useSerifFont,
                        onClick = { useSerifFont = true },
                        label = {
                            Text(
                                text = "Serif (漢字)",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    FilterChip(
                        selected = !useSerifFont,
                        onClick = { useSerifFont = false },
                        label = {
                            Text(
                                text = "Sans-serif (漢字)",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // 3. Game Mode Selection
            SettingsSectionCard(title = "Game Mode", subtitle = "Play alone, beat the clock, or challenge a friend") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Solo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedGameType = GameType.Solo }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedGameType is GameType.Solo,
                            onClick = { selectedGameType = GameType.Solo }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "1-Player (Relaxed)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Time Attack 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (selectedGameType !is GameType.TimeAttack) {
                                    selectedGameType = GameType.TimeAttack(3)
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedGameType is GameType.TimeAttack,
                            onClick = {
                                if (selectedGameType !is GameType.TimeAttack) {
                                    selectedGameType = GameType.TimeAttack(3)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "1-Player (Time Attack)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (selectedGameType is GameType.TimeAttack) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 40.dp, top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1, 3, 5).forEach { min ->
                                val isSelected = (selectedGameType as? GameType.TimeAttack)?.durationMinutes == min
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedGameType = GameType.TimeAttack(min) },
                                    label = { Text("${min} Minute${if (min > 1) "s" else ""}") }
                                )
                            }
                        }
                    }

                    // Two Player 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedGameType = GameType.TwoPlayer }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedGameType is GameType.TwoPlayer,
                            onClick = { selectedGameType = GameType.TwoPlayer }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "2-Player (Pass & play)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 4. Failed-Match Exposure Duration
            SettingsSectionCard(
                title = "Tile Face-up Time",
                subtitle = "On a failed match attempt, how long until tiles go back to being face-down"
            ) {
                val delayOptions = listOf(
                    800L to "0.8s",
                    1150L to "1.2s (Standard)",
                    1500L to "1.5s",
                    2000L to "2s",
                    3000L to "3s"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val scrollChips = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollChips),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        delayOptions.forEach { (ms, label) ->
                            FilterChip(
                                selected = mismatchDelayMs == ms,
                                onClick = { mismatchDelayMs = ms },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // 5. Learning & Display Preferences Section
            SettingsSectionCard(title = "Display Preferences", subtitle = "Make it your own!") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Confusable Twin Alerts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Almost!", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Highlight near-misses",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(checked = twinTrapHints, onCheckedChange = { twinTrapHints = it })
                    }

                    // Show Romaji Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Romaji", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Show Latin-alphabet transliteration next to the kana in the Kanji Inspector",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(checked = showRomaji, onCheckedChange = { showRomaji = it })
                    }

                    // Show Stroke Count Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Stroke Count (画数)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Display stroke-count badge. Disabling it leaves more room for English definitions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(checked = showStrokeCount, onCheckedChange = { showStrokeCount = it })
                    }

                    // Extra Turn Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Keep control of board (2-player mode)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Go again after getting a match",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(checked = bonusTurn, onCheckedChange = { bonusTurn = it })
                    }

                    // Sound Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Sound Effects", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "For tile flips, matches, and alerts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(checked = soundEnabled, onCheckedChange = { soundEnabled = it })
                    }
                }
            }

            // 6. Tile Appearance & Customization
            SettingsSectionCard(title = "Tile Appearance & Color Presets", subtitle = "Customize card back background and emblem colors") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Live Preview Tile
                    val previewBg = if (selectedTileColor == 0xFFFFFFFFL || selectedTileColor == 0xFF1E3A5FL) {
                        AizomeIndigo
                    } else {
                        Color(selectedTileColor)
                    }
                    val previewKanji = if (selectedKanjiColor == 0xFFD4AF37L || selectedKanjiColor == 0xFFE0B44CL) {
                        KinpakuGold
                    } else {
                        Color(selectedKanjiColor)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = previewBg),
                                border = BorderStroke(1.5.dp, KinpakuGold.copy(alpha = 0.4f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "漢",
                                        color = previewKanji,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Live Tile Back Preview",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Instant update with zero layout shift during gameplay",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Tile Background Presets
                    Text(text = "Tile Background Presets", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    ColorPaletteRow(
                        colors = listOf(
                            0xFF1E3A5FL to "Aizome Indigo",
                            0xFFFFF8E7L to "Washi Ivory",
                            0xFFFFFFFFL to "Pure White",
                            0xFF263238L to "Slate Lacquer",
                            0xFF1B5E20L to "Bamboo Green",
                            0xFF880E4FL to "Crimson",
                            0xFF121212L to "Midnight Ink"
                        ),
                        selectedColor = selectedTileColor,
                        onSelect = { selectedTileColor = it }
                    )

                    // 漢 Character Color Presets
                    Text(text = "漢 Character Color Presets", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    ColorPaletteRow(
                        colors = listOf(
                            0xFFE0B44CL to "Kinpaku Gold",
                            0xFFFFFFFFL to "Pure White",
                            0xFF18181BL to "Sumi Ink",
                            0xFFC83E2BL to "Vermilion",
                            0xFF2E7D32L to "Green",
                            0xFF1976D2L to "Blue"
                        ),
                        selectedColor = selectedKanjiColor,
                        onSelect = { selectedKanjiColor = it }
                    )

                    // Custom Hex Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customHex,
                            onValueChange = {
                                customHex = it.take(8)
                                hexError = false
                            },
                            label = { Text("Custom Hex (e.g. #FF9800)") },
                            isError = hexError,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = {
                                    val parsed = parseHexColor(customHex)
                                    if (parsed != null) {
                                        selectedTileColor = parsed
                                        hexError = false
                                    } else {
                                        hexError = true
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Tile", fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    val parsed = parseHexColor(customHex)
                                    if (parsed != null) {
                                        selectedKanjiColor = parsed
                                        hexError = false
                                    } else {
                                        hexError = true
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("漢", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Bottom Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        onApplySettings(
                            GameSettings(
                                dimension = currentSettings.dimension,
                                gameType = selectedGameType,
                                bonusTurnOnMatch = bonusTurn,
                                showTwinTrapHints = twinTrapHints,
                                showRomaji = showRomaji,
                                showStrokeCount = showStrokeCount,
                                useSerifFont = useSerifFont,
                                soundEnabled = soundEnabled,
                                tileColor = selectedTileColor,
                                kanjiColor = selectedKanjiColor,
                                mismatchDelayMs = mismatchDelayMs,
                                selectedPackId = selectedPackId
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (needsRestart) "Apply & Restart" else "Apply")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun ColorPaletteRow(
    colors: List<Pair<Long, String>>,
    selectedColor: Long,
    onSelect: (Long) -> Unit
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEach { (colorVal, name) ->
            val isSelected = selectedColor == colorVal
            val color = Color(colorVal)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelect(colorVal) }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Text(
                            text = "✓",
                            color = if (colorVal == 0xFFFFFFFFL || colorVal == 0xFFFFF8E7L) Color.Black else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = name,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun parseHexColor(input: String): Long? {
    val clean = input.trim().removePrefix("#")
    return try {
        when (clean.length) {
            6 -> 0xFF000000L or clean.toLong(16)
            8 -> clean.toLong(16)
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}
