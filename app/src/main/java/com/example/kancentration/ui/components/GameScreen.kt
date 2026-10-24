package com.example.kancentration.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
                onRestartClick = { viewModel.startNewGame() }
            )

            // Non-obtrusive Victory Banner when game finishes (board remains 100% visible)
            if (uiState.isGameOver) {
                GameOverBanner(
                    uiState = uiState,
                    onRestart = { viewModel.startNewGame() }
                )
            }

            // Board Grid (Flexible center area - ALWAYS FULLY VISIBLE!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                BoardGrid(
                    cards = uiState.cards,
                    dimension = uiState.settings.dimension,
                    tileColor = uiState.settings.tileColor,
                    kanjiColor = uiState.settings.kanjiColor,
                    onCardClick = { viewModel.onCardClicked(it) }
                )
            }

            // Bottom Kanji Inspector
            KanjiInspector(
                kanji = uiState.selectedKanjiForInspector,
                onWiktionaryClick = { viewModel.onOpenWiktionary() }
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
