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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import com.example.kancentration.data.model.BoardDimension
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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

    if (uiState.showSettingsDialog) {
        SettingsScreen(
            currentSettings = uiState.settings,
            onApplySettings = { newSettings ->
                viewModel.setShowSettingsDialog(false)
                viewModel.applySettings(newSettings)
            },
            onDismiss = { viewModel.setShowSettingsDialog(false) },
            modifier = modifier
        )
    } else if (uiState.showFlashCardsScreen) {
        FlashCardsScreen(
            cards = uiState.cards,
            settings = uiState.settings,
            onWiktionaryClick = { viewModel.onOpenWiktionary() },
            onDismiss = { viewModel.setShowFlashCardsScreen(false) },
            modifier = modifier
        )
    } else {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .systemBarsPadding()
                .displayCutoutPadding(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            val configuration = LocalConfiguration.current

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val isLandscape = maxWidth >= maxHeight
                val minDimension = minOf(maxWidth, maxHeight)
                val isTablet = minDimension >= 600.dp || configuration.smallestScreenWidthDp >= 600

                val screenPaddingH = if (isTablet) 20.dp else 12.dp
                val screenPaddingV = if (isTablet) 12.dp else 4.dp

                if (isLandscape) {
                    val totalHeight = maxHeight - (screenPaddingV * 2)
                    val totalWidth = maxWidth - (screenPaddingH * 2)
                    val topAlignPadding = if (isTablet) 8.dp else 6.dp
                    val minSidebarWidth = if (isTablet) maxOf(380.dp, totalWidth * 0.35f) else 320.dp
                    val maxBoardWidth = (totalWidth - minSidebarWidth - (if (isTablet) 20.dp else 16.dp)).coerceAtLeast(0.dp)
                    val boardSize = minOf(
                        (totalHeight - topAlignPadding).coerceAtLeast(0.dp),
                        maxBoardWidth
                    )

                    val dimension = uiState.settings.dimension
                    val gridSpacing = when (dimension) {
                        BoardDimension.FOUR_BY_FOUR -> 8.dp
                        BoardDimension.SIX_BY_SIX -> 5.dp
                        BoardDimension.EIGHT_BY_EIGHT -> 3.dp
                    }
                    val totalGridSpacing = gridSpacing * (dimension.size - 1)
                    val cardSize = if (dimension.size > 0) ((boardSize - totalGridSpacing) / dimension.size).coerceAtLeast(0.dp) else 0.dp
                    // Lower boundary aligned with the center of the third row of tiles on 4x4 (or equivalent baseline on dense grids) for tablets;
                    // For phones, the lower boundary reaches all the way down to the bottom (aligned with the bottom of the bottom row of tiles)
                    val boundaryThirdRowCenter = if (dimension == BoardDimension.FOUR_BY_FOUR) {
                        topAlignPadding + (cardSize * 2.5f) + (gridSpacing * 2f)
                    } else {
                        topAlignPadding + (boardSize * 0.65f)
                    }
                    val sidebarHeight = if (isTablet) boundaryThirdRowCenter else (topAlignPadding + boardSize)

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = screenPaddingH, vertical = screenPaddingV),
                        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 20.dp else 16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left: Left-aligned Center Stage Board Grid (top aligned with Kancentration word)
                        Box(
                            modifier = Modifier
                                .padding(top = topAlignPadding)
                                .size(boardSize),
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

                        // Right: Sidebar containing Top HUD, Status Banner (when active), & Kanji Inspector dock
                        // On tablets, the bottom boundary is aligned with the center of the third row of tiles;
                        // on phones, the lower boundary reaches all the way down to the bottom of the bottom row of tiles
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(sidebarHeight),
                            verticalArrangement = Arrangement.spacedBy(if (isTablet) 10.dp else 6.dp)
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
                                    onRestart = { viewModel.startNewGame() },
                                    onDismissTwinTrap = { viewModel.dismissTwinTrapBanner() },
                                    onFlashCards = { viewModel.setShowFlashCardsScreen(true) },
                                    onInspectKanji = { viewModel.onKanjiInspected(it) },
                                    isPhoneLandscape = !isTablet
                                )
                            }

                            KanjiInspector(
                                kanji = uiState.selectedKanjiForInspector,
                                showRomaji = uiState.settings.showRomaji,
                                showStrokeCount = uiState.settings.showStrokeCount,
                                useSerifFont = uiState.settings.useSerifFont,
                                isPhoneLandscape = !isTablet,
                                onWiktionaryClick = { viewModel.onOpenWiktionary() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    }
                } else {
                    // Portrait mode (Tablet or Phone)
                    val totalHeight = maxHeight - (screenPaddingV * 2)
                    val totalWidth = maxWidth - (screenPaddingH * 2)
                    val boardSize = minOf(
                        totalWidth,
                        totalHeight * (if (isTablet) 0.55f else 0.52f)
                    ).coerceAtLeast(0.dp)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = screenPaddingH, vertical = screenPaddingV),
                        verticalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GameHud(
                            uiState = uiState,
                            onSettingsClick = { viewModel.setShowSettingsDialog(true) },
                            onDlcStoreClick = { viewModel.setShowDlcStoreDialog(true) },
                            onRestartClick = { viewModel.startNewGame() },
                            onCycleDimensionClick = { viewModel.cycleBoardDimension() },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Box(
                            modifier = Modifier.size(boardSize),
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

                        if (uiState.activeTwinTrapBanner != null || uiState.isGameOver) {
                            StatusBannerSlot(
                                uiState = uiState,
                                onRestart = { viewModel.startNewGame() },
                                onDismissTwinTrap = { viewModel.dismissTwinTrapBanner() },
                                onFlashCards = { viewModel.setShowFlashCardsScreen(true) },
                                onInspectKanji = { viewModel.onKanjiInspected(it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        KanjiInspector(
                            kanji = uiState.selectedKanjiForInspector,
                            showRomaji = uiState.settings.showRomaji,
                            showStrokeCount = uiState.settings.showStrokeCount,
                            useSerifFont = uiState.settings.useSerifFont,
                            onWiktionaryClick = { viewModel.onOpenWiktionary() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (isTablet) Modifier.weight(1f) else Modifier)
                        )
                    }
                }
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
}
