package com.example.kancentration.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kancentration.ui.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle lifecycle for app pause / resume
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.onPauseApp()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onResumeApp()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top HUD
            GameHud(
                uiState = uiState,
                onSettingsClick = { viewModel.setShowSettingsDialog(true) },
                onDlcStoreClick = { viewModel.setShowDlcStoreDialog(true) },
                onRestartClick = { viewModel.startNewGame() },
                onCycleDimensionClick = { viewModel.cycleBoardDimension() }
            )

            // Proportionate Victory Banner slot (never causes board to move)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.10f),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                if (uiState.isGameOver) {
                    GameOverBanner(
                        uiState = uiState,
                        onRestart = { viewModel.startNewGame() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Board Grid (63% of flexible height - scales dynamically to screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.63f)
            ) {
                BoardGrid(
                    cards = uiState.cards,
                    dimension = uiState.settings.dimension,
                    tileColor = uiState.settings.tileColor,
                    kanjiColor = uiState.settings.kanjiColor,
                    onCardClick = { viewModel.onCardClicked(it) }
                )
            }

            // Bottom Kanji Inspector (27% of flexible height - proportionate & static)
            KanjiInspector(
                kanji = uiState.selectedKanjiForInspector,
                onWiktionaryClick = { viewModel.onOpenWiktionary() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.27f)
            )
        }

        // Dialogs
        if (uiState.showSettingsDialog) {
            SettingsDialog(
                currentSettings = uiState.settings,
                onApplySettings = { newSettings ->
                    viewModel.setShowSettingsDialog(false)
                    viewModel.applySettings(newSettings)
                },
                onDismiss = { viewModel.setShowSettingsDialog(false) }
            )
        }

        if (uiState.showDlcStoreDialog) {
            DlcStoreDialog(
                dlcManager = viewModel.dlcManager,
                onDownloadPack = { viewModel.downloadDlcPack(it) },
                onDismiss = { viewModel.setShowDlcStoreDialog(false) }
            )
        }
    }
}
