package com.example.kancentration.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kancentration.ui.viewmodel.GameTab
import com.example.kancentration.ui.viewmodel.GameViewModel

/**
 * Main game screen for Kancentration:
 * - Row 1: Top row ("Kancentration", board-size pill, pack badge, 🛒, ⚙️, 🔄)
 * - Row 2: Tabs ("Flash cards" and "Current game")
 * - Below tabs:
 *    - "Current game": The game board stage & sidebar (telemetry, status banner, Kanji inspector)
 *    - "Flash cards": Flash cards for every kanji exposed so far, grouped confusable-pair by confusable-pair
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

    // Android hardware/gesture Back navigation: return from Flash cards tab to Current game
    BackHandler(enabled = uiState.selectedTab == GameTab.FLASH_CARDS) {
        viewModel.selectTab(GameTab.CURRENT_GAME)
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
                val containerMaxWidth = maxWidth
                val containerMaxHeight = maxHeight
                val isLandscape = containerMaxWidth >= containerMaxHeight
                val minDimension = minOf(containerMaxWidth, containerMaxHeight)
                val isTablet = minDimension >= 600.dp || configuration.smallestScreenWidthDp >= 600

                val screenPaddingH = if (isTablet) 20.dp else 12.dp
                val screenPaddingV = if (isTablet) 12.dp else 4.dp

                val exposedCards = remember(uiState.cards) {
                    uiState.cards.filter { it.hasBeenExposed || it.isFaceUp || it.isMatched }
                }
                val exposedKanjiCount = remember(exposedCards) {
                    exposedCards.groupBy { it.quadId }
                        .values
                        .map { group -> group.map { it.kanji }.distinctBy { it.id } }
                        .flatten()
                        .distinctBy { it.id }
                        .size
                }

                val row1Height = if (isTablet) 44.dp else 34.dp
                val tabHeight = if (isTablet) 42.dp else 36.dp
                val spacingBetweenHeader = if (isTablet) 6.dp else 2.dp
                val headerTotalHeight = row1Height + tabHeight + spacingBetweenHeader

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = screenPaddingH, vertical = screenPaddingV),
                    verticalArrangement = Arrangement.spacedBy(spacingBetweenHeader)
                ) {
                    // Top row ("Kancentration...")
                    GameTopHeaderRow(
                        uiState = uiState,
                        onSettingsClick = { viewModel.setShowSettingsDialog(true) },
                        onDlcStoreClick = { viewModel.setShowDlcStoreDialog(true) },
                        onRestartClick = { viewModel.startNewGame() },
                        onCycleDimensionClick = { viewModel.cycleBoardDimension() }
                    )

                    // New row: two tabs ("Flash cards" and "Current game")
                    GameTabsRow(
                        selectedTab = uiState.selectedTab,
                        exposedKanjiCount = exposedKanjiCount,
                        onTabSelected = { viewModel.selectTab(it) }
                    )

                    Spacer(modifier = Modifier.height(if (isTablet) 4.dp else 2.dp))

                    // Tab Body
                    when (uiState.selectedTab) {
                        GameTab.FLASH_CARDS -> {
                            FlashCardsTabContent(
                                cards = uiState.cards,
                                settings = uiState.settings,
                                onWiktionaryClick = { viewModel.onOpenWiktionary() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }

                        GameTab.CURRENT_GAME -> {
                            if (isLandscape) {
                                // Landscape layout: Center Stage Board Grid on Left, Sidebar on Right
                                val availableHeight = (containerMaxHeight - (screenPaddingV * 2) - headerTotalHeight - 8.dp).coerceAtLeast(0.dp)
                                val totalWidth = containerMaxWidth - (screenPaddingH * 2)
                                val minSidebarWidth = if (isTablet) maxOf(380.dp, totalWidth * 0.35f) else 320.dp
                                val maxBoardWidth = (totalWidth - minSidebarWidth - (if (isTablet) 20.dp else 16.dp)).coerceAtLeast(0.dp)
                                val boardSize = minOf(availableHeight, maxBoardWidth)

                                val dimension = uiState.settings.dimension
                                val gridSpacing = when (dimension) {
                                    BoardDimension.FOUR_BY_FOUR -> 8.dp
                                    BoardDimension.SIX_BY_SIX -> 5.dp
                                    BoardDimension.EIGHT_BY_EIGHT -> 3.dp
                                }
                                val totalGridSpacing = gridSpacing * (dimension.size - 1)
                                val cardSize = if (dimension.size > 0) ((boardSize - totalGridSpacing) / dimension.size).coerceAtLeast(0.dp) else 0.dp
                                val boundaryThirdRowCenter = if (dimension == BoardDimension.FOUR_BY_FOUR) {
                                    (cardSize * 2.5f) + (gridSpacing * 2f)
                                } else {
                                    boardSize * 0.65f
                                }
                                val sidebarHeight = if (isTablet) boundaryThirdRowCenter else boardSize

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(if (isTablet) 20.dp else 16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Left: Center Stage Board Grid
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

                                    // Right: Sidebar containing Telemetry, Status Banner, & Kanji Inspector
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(sidebarHeight),
                                        verticalArrangement = Arrangement.spacedBy(if (isTablet) 10.dp else 6.dp)
                                    ) {
                                        GameTelemetryRow(uiState = uiState)

                                        if (uiState.activeTwinTrapBanner != null || uiState.isGameOver) {
                                            StatusBannerSlot(
                                                uiState = uiState,
                                                onRestart = { viewModel.startNewGame() },
                                                onDismissTwinTrap = { viewModel.dismissTwinTrapBanner() },
                                                onFlashCards = { viewModel.selectTab(GameTab.FLASH_CARDS) },
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
                                // Portrait layout (Tablet or Phone)
                                val availableHeight = (containerMaxHeight - (screenPaddingV * 2) - headerTotalHeight - 8.dp).coerceAtLeast(0.dp)
                                val totalWidth = containerMaxWidth - (screenPaddingH * 2)
                                val boardSize = minOf(
                                    totalWidth,
                                    availableHeight * (if (isTablet) 0.55f else 0.52f)
                                ).coerceAtLeast(0.dp)

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    GameTelemetryRow(
                                        uiState = uiState,
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
                                            onFlashCards = { viewModel.selectTab(GameTab.FLASH_CARDS) },
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
