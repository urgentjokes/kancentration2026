package com.example.kancentration.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kancentration.ui.viewmodel.GameViewModel

/**
 * Landscape GameScreen integrating the Tatami Grid stage on the left
 * and the Sidebar (HUD, Status Banner, and KanjiInspector) on the right.
 * Observes the immutable GameUiState contract.
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(
        factory = GameViewModel.createDefaultFactory(
            LocalContext.current.applicationContext as android.app.Application
        )
    ),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Register formal Android Lifecycle Observer to pause/resume the game clock across app lifecycles
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.onPauseApp()
                Lifecycle.Event.ON_RESUME -> viewModel.onResumeApp()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .displayCutoutPadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Center Stage Board Grid (spacious tatami card grid)
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                StatusBannerSlot(
                    uiState = uiState,
                    onRestart = { viewModel.startNewGame() }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    BoardGrid(
                        cards = uiState.cards,
                        dimension = uiState.settings.dimension,
                        tileColor = uiState.settings.tileColor,
                        kanjiColor = uiState.settings.kanjiColor,
                        useSerifFont = uiState.settings.useSerifFont,
                        onCardClick = { viewModel.onCardClicked(it) }
                    )
                }
            }

            // Right: Sidebar containing Top HUD & Kanji Inspector dock
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameHud(
                    uiState = uiState,
                    onSettingsClick = { viewModel.setShowSettingsDialog(true) },
                    onDlcStoreClick = { viewModel.setShowDlcStoreDialog(true) },
                    onRestartClick = { viewModel.startNewGame() },
                    onCycleDimensionClick = { viewModel.cycleBoardDimension() }
                )

                KanjiInspector(
                    kanji = uiState.selectedKanjiForInspector,
                    showRomaji = uiState.settings.showRomaji,
                    useSerifFont = uiState.settings.useSerifFont,
                    onWiktionaryClick = { viewModel.onOpenWiktionary() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
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
