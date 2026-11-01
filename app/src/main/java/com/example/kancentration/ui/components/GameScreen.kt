package com.example.kancentration.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
 * Landscape GameScreen with top-aligned maximized Tatami Grid stage on the left,
 * precisely aligning card tops with the "Kancentration" header text, and the Sidebar
 * (HUD with timer & board-size pill underneath, Status Banner, and KanjiInspector) on the right.
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
        ) {
            val totalHeight = maxHeight
            val topAlignPadding = 6.dp
            // Board is square. Fill available height minus top alignment padding,
            // while reserving at least 320dp width for the sidebar.
            val boardSize = minOf(
                (totalHeight - topAlignPadding).coerceAtLeast(0.dp),
                (maxWidth - 320.dp).coerceAtLeast(0.dp)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left: Left-aligned Center Stage Board Grid (top aligned with Kancentration word)
                Box(
                    modifier = Modifier
                        .padding(top = topAlignPadding)
                        .size(boardSize),
                    contentAlignment = Alignment.TopStart
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

                // Right: Sidebar containing Top HUD, Status Banner (when active), & Kanji Inspector dock
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameHud(
                        uiState = uiState,
                        onSettingsClick = { viewModel.setShowSettingsDialog(true) },
                        onDlcStoreClick = { viewModel.setShowDlcStoreDialog(true) },
                        onRestartClick = { viewModel.startNewGame() },
                        onCycleDimensionClick = { viewModel.cycleBoardDimension() }
                    )

                    if (uiState.activeTwinTrapBanner != null || uiState.isGameOver) {
                        StatusBannerSlot(
                            uiState = uiState,
                            onRestart = { viewModel.startNewGame() }
                        )
                    }

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
