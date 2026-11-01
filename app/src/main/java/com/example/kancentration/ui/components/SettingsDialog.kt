package com.example.kancentration.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
 * Redesigned Settings modal sheet featuring:
 * - Segmented controls for Kanji Typography (Mincho/Serif vs Gothic/Sans-Serif)
 * - Segmented switches for Board Dimensions (4x4, 6x6, 8x8)
 * - Learning preference switches (Confusable Twin Alerts, Show Romaji, Extra Turn)
 * - Traditional palette presets (Aizome Indigo, Washi Ivory, Bamboo Green) with live preview
 */
@Composable
fun SettingsDialog(
    currentSettings: GameSettings,
    onApplySettings: (GameSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDimension by remember { mutableStateOf(currentSettings.dimension) }
    var selectedGameType by remember { mutableStateOf(currentSettings.gameType) }
    var bonusTurn by remember { mutableStateOf(currentSettings.bonusTurnOnMatch) }
    var twinTrapHints by remember { mutableStateOf(currentSettings.showTwinTrapHints) }
    var showRomaji by remember { mutableStateOf(currentSettings.showRomaji) }
    var useSerifFont by remember { mutableStateOf(currentSettings.useSerifFont) }
    var soundEnabled by remember { mutableStateOf(currentSettings.soundEnabled) }
    var selectedTileColor by remember { mutableStateOf(currentSettings.tileColor) }
    var selectedKanjiColor by remember { mutableStateOf(currentSettings.kanjiColor) }
    var customHex by remember { mutableStateOf("") }
    var hexError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Game Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customization & Preferences (設定)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // 1. Board Dimensions Segmented Switch
                Text(
                    text = "Board Dimension",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BoardDimension.values().forEach { dim ->
                        FilterChip(
                            selected = selectedDimension == dim,
                            onClick = { selectedDimension = dim },
                            label = {
                                Text(
                                    text = "${dim.size}×${dim.size}",
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

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Kanji Font Style Segmented Switch (Serif/Mincho vs Sans/Gothic)
                Text(
                    text = "Kanji Typography",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (useSerifFont) "Mincho (明朝体) — Calligraphic stroke style" else "Gothic (ゴシック) — Modern geometric style",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Game Mode Selection
                Text(
                    text = "Game Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Solo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGameType = GameType.Solo }
                ) {
                    RadioButton(
                        selected = selectedGameType is GameType.Solo,
                        onClick = { selectedGameType = GameType.Solo }
                    )
                    Text(text = "1-Player Solo (Relaxed)", style = MaterialTheme.typography.bodyMedium)
                }

                // Two Player
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGameType = GameType.TwoPlayer }
                ) {
                    RadioButton(
                        selected = selectedGameType is GameType.TwoPlayer,
                        onClick = { selectedGameType = GameType.TwoPlayer }
                    )
                    Text(text = "2-Player Pass & Play", style = MaterialTheme.typography.bodyMedium)
                }

                // Time Attack
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (selectedGameType !is GameType.TimeAttack) {
                                selectedGameType = GameType.TimeAttack(3)
                            }
                        }
                ) {
                    RadioButton(
                        selected = selectedGameType is GameType.TimeAttack,
                        onClick = {
                            if (selectedGameType !is GameType.TimeAttack) {
                                selectedGameType = GameType.TimeAttack(3)
                            }
                        }
                    )
                    Text(text = "Time-Attack (Countdown)", style = MaterialTheme.typography.bodyMedium)
                }

                if (selectedGameType is GameType.TimeAttack) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 3, 5).forEach { min ->
                            val isSelected = (selectedGameType as? GameType.TimeAttack)?.durationMinutes == min
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedGameType = GameType.TimeAttack(min) },
                                label = { Text("${min}m") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Learning Preferences Section
                Text(
                    text = "Learning Preferences",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Confusable Twin Alerts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Almost!", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Highlight confusable near-miss pairs from the same quad in Amber",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = twinTrapHints, onCheckedChange = { twinTrapHints = it })
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Show Romaji Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Show Romaji Readings", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Display latin pronunciation alongside Kana in Inspector",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = showRomaji, onCheckedChange = { showRomaji = it })
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Extra Turn Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Extra Turn on Match", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Scoring a match allows flipping again",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = bonusTurn, onCheckedChange = { bonusTurn = it })
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sound Effects", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = soundEnabled, onCheckedChange = { soundEnabled = it })
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Tile Appearance & Color Presets
                Text(
                    text = "Tile Appearance & Presets",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

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
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = previewBg),
                            border = BorderStroke(1.5.dp, KinpakuGold.copy(alpha = 0.4f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "漢",
                                    color = previewKanji,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Live Tile Back Preview",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Instant update with zero layout shift",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tile Background Presets
                Text(text = "Tile Background Presets", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
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

                Spacer(modifier = Modifier.height(10.dp))

                // 漢 Character Color Presets
                Text(text = "漢 Character Color Presets", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
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

                Spacer(modifier = Modifier.height(10.dp))

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
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Tile", fontSize = 11.sp)
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
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("漢", fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            val needsRestart = selectedDimension != currentSettings.dimension ||
                    selectedGameType != currentSettings.gameType

            Button(
                onClick = {
                    onApplySettings(
                        GameSettings(
                            dimension = selectedDimension,
                            gameType = selectedGameType,
                            bonusTurnOnMatch = bonusTurn,
                            showTwinTrapHints = twinTrapHints,
                            showRomaji = showRomaji,
                            useSerifFont = useSerifFont,
                            soundEnabled = soundEnabled,
                            tileColor = selectedTileColor,
                            kanjiColor = selectedKanjiColor,
                            mismatchDelayMs = currentSettings.mismatchDelayMs,
                            showStrokeCount = currentSettings.showStrokeCount
                        )
                    )
                }
            ) {
                Text(if (needsRestart) "Apply & Restart" else "Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
                        .size(34.dp)
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
