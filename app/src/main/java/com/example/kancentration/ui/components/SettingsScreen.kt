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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

    var selectedDimension by remember { mutableStateOf(currentSettings.dimension) }
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

    val needsRestart = selectedDimension != currentSettings.dimension ||
            selectedGameType != currentSettings.gameType

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
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.labelLarge)
                        }

                        Column {
                            Text(
                                text = "Game Settings",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Customization & Preferences (設定)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onApplySettings(
                                GameSettings(
                                    dimension = selectedDimension,
                                    gameType = selectedGameType,
                                    bonusTurnOnMatch = bonusTurn,
                                    showTwinTrapHints = twinTrapHints,
                                    showRomaji = showRomaji,
                                    showStrokeCount = showStrokeCount,
                                    useSerifFont = useSerifFont,
                                    soundEnabled = soundEnabled,
                                    tileColor = selectedTileColor,
                                    kanjiColor = selectedKanjiColor,
                                    mismatchDelayMs = mismatchDelayMs
                                )
                            )
                        },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Board Dimensions Section
            SettingsSectionCard(title = "Board Dimension", subtitle = "Choose grid density and total card pairs") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BoardDimension.values().forEach { dim ->
                        FilterChip(
                            selected = selectedDimension == dim,
                            onClick = { selectedDimension = dim },
                            label = {
                                Text(
                                    text = "${dim.size}×${dim.size} (${dim.totalCards} tiles)",
                                    fontWeight = if (selectedDimension == dim) FontWeight.Bold else FontWeight.Normal
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

            // 2. Kanji Typography Section
            SettingsSectionCard(
                title = "Kanji Typography",
                subtitle = if (useSerifFont) "Mincho (明朝体) — Calligraphic brush strokes with flared serifs" else "Gothic (ゴシック) — Modern clean geometric sans-serif"
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
                                text = "Mincho (Serif)",
                                fontFamily = FontFamily.Serif,
                                fontWeight = if (useSerifFont) FontWeight.Bold else FontWeight.Normal
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
                                text = "Gothic (Sans)",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = if (!useSerifFont) FontWeight.Bold else FontWeight.Normal
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
            SettingsSectionCard(title = "Game Mode", subtitle = "Play alone, challenge a friend, or beat the clock") {
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
                            Text(text = "1-Player Solo (Relaxed)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = "Unconstrained stopwatch; study and match at your own pace", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Text(text = "2-Player Pass & Play", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = "Take turns on a single device with competitive scoring", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Text(text = "Time-Attack (Countdown)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = "Race against the clock before time runs out", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                }
            }

            // 4. Failed Match Exposure Duration
            SettingsSectionCard(
                title = "Failed Match Exposure Time",
                subtitle = "How long mismatched cards stay exposed before returning to face-down position"
            ) {
                val delayOptions = listOf(
                    800L to "0.8s (Fast)",
                    1150L to "1.2s (Standard)",
                    1500L to "1.5s",
                    2000L to "2.0s (Relaxed)",
                    3000L to "3.0s (Extended)"
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
                                        fontWeight = if (mismatchDelayMs == ms) FontWeight.Bold else FontWeight.Normal
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
            SettingsSectionCard(title = "Learning & Display Preferences", subtitle = "Fine-tune feedback and on-screen details") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Confusable Twin Alerts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Twin-Trap Alerts (惜しい！)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Highlight confusable near-miss pairs from the same quad in Amber",
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
                            Text(text = "Show Romaji Readings", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Display latin transliteration alongside Kana in the Kanji Inspector",
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
                            Text(text = "Show Stroke Count (画数)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Display stroke count badge in Inspector (turn off to give more room for definitions)",
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
                            Text(text = "Extra Turn on Match", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Scoring a match allows flipping again without passing turn",
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
                                text = "Audio cues for tile flips, matches, and alerts",
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
                            0xFFD4AF37L to "Kincha Gold",
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
                                dimension = selectedDimension,
                                gameType = selectedGameType,
                                bonusTurnOnMatch = bonusTurn,
                                showTwinTrapHints = twinTrapHints,
                                showRomaji = showRomaji,
                                showStrokeCount = showStrokeCount,
                                useSerifFont = useSerifFont,
                                soundEnabled = soundEnabled,
                                tileColor = selectedTileColor,
                                kanjiColor = selectedKanjiColor,
                                mismatchDelayMs = mismatchDelayMs
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
