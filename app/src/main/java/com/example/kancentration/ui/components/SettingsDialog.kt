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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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

@Composable
fun SettingsDialog(
    currentSettings: GameSettings,
    onApplySettings: (GameSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDimension by remember { mutableStateOf(currentSettings.dimension) }
    var selectedGameType by remember { mutableStateOf(currentSettings.gameType) }
    var bonusTurn by remember { mutableStateOf(currentSettings.bonusTurnOnMatch) }
    var soundEnabled by remember { mutableStateOf(currentSettings.soundEnabled) }
    var selectedTileColor by remember { mutableStateOf(currentSettings.tileColor) }
    var selectedKanjiColor by remember { mutableStateOf(currentSettings.kanjiColor) }
    var customHex by remember { mutableStateOf("") }
    var hexError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Game Settings", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Board Dimension
                Text(text = "Board Size", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BoardDimension.values().forEach { dim ->
                        FilterChip(
                            selected = selectedDimension == dim,
                            onClick = { selectedDimension = dim },
                            label = { Text("${dim.size}×${dim.size}") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Game Mode
                Text(text = "Game Mode", style = MaterialTheme.typography.titleSmall)
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
                    Text(text = "1-Player Solo (Casual)", style = MaterialTheme.typography.bodyMedium)
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
                                selectedGameType = GameType.TimeAttack(1)
                            }
                        }
                ) {
                    RadioButton(
                        selected = selectedGameType is GameType.TimeAttack,
                        onClick = {
                            if (selectedGameType !is GameType.TimeAttack) {
                                selectedGameType = GameType.TimeAttack(1)
                            }
                        }
                    )
                    Text(text = "Time-Attack Mode", style = MaterialTheme.typography.bodyMedium)
                }

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

                Spacer(modifier = Modifier.height(14.dp))

                // Appearance: Tile & Character Colors
                Text(text = "Tile Appearance", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(6.dp))

                // Live Preview Card
                val previewBg = Color(selectedTileColor)
                val previewKanji = Color(selectedKanjiColor)
                val isBgLight = (previewBg.red * 0.299f + previewBg.green * 0.587f + previewBg.blue * 0.114f) > 0.6f
                val previewBorder = if (isBgLight) Color(0xFFCFD8DC) else Color.White.copy(alpha = 0.3f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF7F7F7), RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = previewBg),
                        border = BorderStroke(1.5.dp, previewBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "漢",
                                color = previewKanji,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Tile Preview",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Card back in-game appearance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tile Background Color Palette
                Text(text = "Tile Background Color", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                ColorPaletteRow(
                    colors = listOf(
                        0xFFFFFFFFL to "White",
                        0xFFFFF8E7L to "Cream",
                        0xFF263238L to "Slate",
                        0xFF1A237EL to "Navy",
                        0xFF1B5E20L to "Forest",
                        0xFF880E4FL to "Crimson",
                        0xFF121212L to "Black"
                    ),
                    selectedColor = selectedTileColor,
                    onSelect = { selectedTileColor = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 漢 Character Color Palette
                Text(text = "漢 Character Color", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                ColorPaletteRow(
                    colors = listOf(
                        0xFFD4AF37L to "Gold",
                        0xFFFFA000L to "Amber",
                        0xFF263238L to "Charcoal",
                        0xFFFFFFFFL to "White",
                        0xFFD32F2FL to "Red",
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

                Spacer(modifier = Modifier.height(14.dp))

                // Turn Continuation Rule
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Extra Turn on Match", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Scoring grants another turn",
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
                            soundEnabled = soundEnabled,
                            tileColor = selectedTileColor,
                            kanjiColor = selectedKanjiColor
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
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEach { (colorValue, name) ->
            val color = Color(colorValue)
            val isSelected = selectedColor == colorValue
            val isLight = (color.red * 0.299f + color.green * 0.587f + color.blue * 0.114f) > 0.6f

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else (if (isLight) Color(0xFFCFD8DC) else Color.Transparent),
                        shape = CircleShape
                    )
                    .clickable { onSelect(colorValue) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Text(
                        text = "✓",
                        color = if (isLight) Color(0xFF212121) else Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun parseHexColor(input: String): Long? {
    val clean = input.trim().removePrefix("#")
    return when (clean.length) {
        6 -> try { ("FF$clean").toLong(16) } catch (_: Exception) { null }
        8 -> try { clean.toLong(16) } catch (_: Exception) { null }
        else -> null
    }
}

