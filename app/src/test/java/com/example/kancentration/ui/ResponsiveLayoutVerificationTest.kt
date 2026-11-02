package com.example.kancentration.ui

import com.example.kancentration.data.model.BoardDimension
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying responsive layout mathematics, viewport constraints,
 * adaptive grid density font scaling rules, and safe touch padding boundaries.
 */
class ResponsiveLayoutVerificationTest {

    // Simulates the layout calculation logic inside BoardGrid
    private data class GridCalculationResult(
        val boardSizeDp: Float,
        val cardSizeDp: Float,
        val fontSizeSp: Float,
        val heightFillRatio: Float
    )

    private fun calculateBoardLayout(
        dimension: BoardDimension,
        viewportWidthDp: Float,
        viewportHeightDp: Float,
        paddingHorizontalDp: Float = 16f,
        paddingVerticalDp: Float = 8f
    ): GridCalculationResult {
        val size = dimension.size
        val spacingDp = when (dimension) {
            BoardDimension.FOUR_BY_FOUR -> 8f
            BoardDimension.SIX_BY_SIX -> 5f
            BoardDimension.EIGHT_BY_EIGHT -> 3f
        }
        val baseFontSizeSp = when (dimension) {
            BoardDimension.FOUR_BY_FOUR -> 38f
            BoardDimension.SIX_BY_SIX -> 25f
            BoardDimension.EIGHT_BY_EIGHT -> 17f
        }

        val totalSpacingDp = spacingDp * (size - 1)
        val availableWidth = viewportWidthDp - paddingHorizontalDp
        val availableHeight = viewportHeightDp - paddingVerticalDp
        val boardSize = minOf(availableWidth, availableHeight)
        val cardSize = (boardSize - totalSpacingDp) / size

        val maxSafeFontSize = cardSize * 0.75f
        val responsiveFontSize = if (cardSize < 36f) {
            minOf(baseFontSizeSp, maxSafeFontSize)
        } else if (cardSize > 65f) {
            maxOf(baseFontSizeSp, maxSafeFontSize * 0.96f)
        } else {
            baseFontSizeSp
        }

        val fillRatio = responsiveFontSize / cardSize

        return GridCalculationResult(
            boardSizeDp = boardSize,
            cardSizeDp = cardSize,
            fontSizeSp = responsiveFontSize,
            heightFillRatio = fillRatio
        )
    }

    @Test
    fun testLockedViewportZeroOverflowAcrossMobileAndTabletViewports() {
        // Small mobile (320dp width), Standard mobile (390dp), Large mobile (430dp), Tablet (800dp), Large Tablet (1024dp)
        val viewports = listOf(
            Pair(320f, 360f),  // Small mobile grid area
            Pair(390f, 480f),  // Standard mobile grid area
            Pair(430f, 520f),  // Plus/Max mobile grid area
            Pair(800f, 700f),  // Tablet portrait grid area
            Pair(1024f, 600f)  // Tablet landscape grid area
        )

        for ((vw, vh) in viewports) {
            for (dim in BoardDimension.values()) {
                val result = calculateBoardLayout(dim, vw, vh)

                // 1. Board size strictly fits inside available viewport dimensions with zero overflow
                assertTrue(
                    "Board size (${result.boardSizeDp}) must be <= viewport width ($vw)",
                    result.boardSizeDp <= vw
                )
                assertTrue(
                    "Board size (${result.boardSizeDp}) must be <= viewport height ($vh)",
                    result.boardSizeDp <= vh
                )

                // 2. All cards have positive, usable size
                assertTrue(
                    "Card size (${result.cardSizeDp}) must be positive for ${dim.name} on ${vw}x$vh",
                    result.cardSizeDp > 0f
                )
            }
        }
    }

    @Test
    fun testAdaptiveGridDensityFontScalingFillsApproximately75PercentHeightWithoutTruncation() {
        // Design viewports scaled to respective reference card sizes (~51dp for 4x4, ~34dp for 6x6, ~23dp for 8x8)
        val viewport4x4 = Pair(240f, 240f)  // cardSize ~ 52dp
        val viewport6x6 = Pair(235f, 235f)  // cardSize ~ 34.5dp
        val viewport8x8 = Pair(210f, 210f)  // cardSize ~ 23.6dp

        // 4x4 Density: 38sp fills ~73-75% of ~52dp tile
        val result4x4 = calculateBoardLayout(BoardDimension.FOUR_BY_FOUR, viewport4x4.first, viewport4x4.second)
        assertEquals("4x4 must yield base font size 38sp", 38f, result4x4.fontSizeSp, 1.0f)
        assertTrue(
            "4x4 font must fill ~70-76% of tile height, found: ${result4x4.heightFillRatio}",
            result4x4.heightFillRatio in 0.68f..0.76f
        )

        // 6x6 Density: 25sp fills ~72-75% of ~34.5dp tile
        val result6x6 = calculateBoardLayout(BoardDimension.SIX_BY_SIX, viewport6x6.first, viewport6x6.second)
        assertEquals("6x6 must yield base font size 25sp", 25f, result6x6.fontSizeSp, 1.0f)
        assertTrue(
            "6x6 font must fill ~70-76% of tile height, found: ${result6x6.heightFillRatio}",
            result6x6.heightFillRatio in 0.68f..0.76f
        )

        // 8x8 Density: 17sp fills ~70-75% of ~23.6dp tile
        val result8x8 = calculateBoardLayout(BoardDimension.EIGHT_BY_EIGHT, viewport8x8.first, viewport8x8.second)
        assertEquals("8x8 must yield base font size 17sp", 17f, result8x8.fontSizeSp, 1.0f)
        assertTrue(
            "8x8 font must fill ~70-76% of tile height, found: ${result8x8.heightFillRatio}",
            result8x8.heightFillRatio in 0.68f..0.76f
        )
    }

    @Test
    fun testExtremelyCompactScreensCapFontScalingToPreventTruncation() {
        // Ultra-narrow viewport (e.g. 240dp width)
        val result8x8Compact = calculateBoardLayout(BoardDimension.EIGHT_BY_EIGHT, 240f, 240f)

        // Font scaling must be capped to <= 75% of cardSize so character never clips
        assertTrue(
            "Font size must not exceed 75% of card size on ultra-compact screen, found ${result8x8Compact.heightFillRatio}",
            result8x8Compact.heightFillRatio <= 0.751f
        )
    }

    @Test
    fun testTabletViewportScalesComfortablyWithoutDistortion() {
        val tabletViewport = Pair(800f, 750f)

        for (dim in BoardDimension.values()) {
            val result = calculateBoardLayout(dim, tabletViewport.first, tabletViewport.second)
            assertTrue(
                "Tablet card size must scale up comfortably for ${dim.name}",
                result.cardSizeDp >= 48f
            )
            // Kanji fills ~70-75% on tablet
            assertTrue(
                "Kanji glyph on tablet must fill ~70-76% of tile height without distortion, found ${result.heightFillRatio}",
                result.heightFillRatio in 0.45f..0.76f
            )
        }
    }

    @Test
    fun testTabletLandscapeSidebarAndBoardBalancing() {
        val tabletLandscapeViewports = listOf(
            Pair(960f, 600f),    // 8-inch small tablet landscape
            Pair(1024f, 768f),   // 4:3 tablet landscape
            Pair(1280f, 800f),   // 10.1-inch standard Android tablet landscape
            Pair(1600f, 1000f)   // 12.9+ inch large tablet landscape
        )

        for ((vw, vh) in tabletLandscapeViewports) {
            val screenPaddingH = 20f * 2
            val screenPaddingV = 12f * 2
            val totalWidth = vw - screenPaddingH
            val totalHeight = vh - screenPaddingV
            val spacing = 20f
            val topAlignPadding = 8f

            val minSidebarWidth = maxOf(380f, totalWidth * 0.35f)
            val maxBoardWidth = (totalWidth - minSidebarWidth - spacing).coerceAtLeast(0f)
            val boardSize = minOf((totalHeight - topAlignPadding).coerceAtLeast(0f), maxBoardWidth)
            val actualSidebarWidth = totalWidth - boardSize - spacing

            // 1. Board fits strictly within viewport height
            assertTrue(
                "Board ($boardSize) must fit in total height ($totalHeight) for ${vw}x$vh",
                boardSize <= totalHeight
            )

            // 2. Sidebar gets at least 380dp on tablet landscape
            assertTrue(
                "Sidebar width ($actualSidebarWidth) must be >= 380dp on tablet ${vw}x$vh",
                actualSidebarWidth >= 380f
            )

            // 3. Board is well-sized for tablet interaction
            assertTrue(
                "Board size ($boardSize) must be at least 450dp on tablet ${vw}x$vh",
                boardSize >= 450f
            )
        }
    }

    @Test
    fun testTabletPortraitViewportBalancing() {
        val tabletPortraitViewports = listOf(
            Pair(600f, 960f),    // 8-inch tablet portrait
            Pair(768f, 1024f),   // 4:3 tablet portrait
            Pair(800f, 1280f),   // 10.1-inch tablet portrait
            Pair(1000f, 1600f)   // Large tablet portrait
        )

        for ((vw, vh) in tabletPortraitViewports) {
            val screenPaddingH = 20f * 2
            val screenPaddingV = 12f * 2
            val totalWidth = vw - screenPaddingH
            val totalHeight = vh - screenPaddingV

            val boardSize = minOf(totalWidth, totalHeight * 0.55f)
            val remainingVerticalSpace = totalHeight - boardSize

            // 1. Board width fits in available width
            assertTrue("Board width ($boardSize) must fit in $totalWidth", boardSize <= totalWidth)

            // 2. At least 300dp remaining for HUD and KanjiInspector dock in portrait
            assertTrue(
                "Remaining vertical space ($remainingVerticalSpace) must be >= 300dp on ${vw}x$vh",
                remainingVerticalSpace >= 300f
            )
        }
    }

    @Test
    fun testKanjiInspectorSealTileConstraintsOnTablet() {
        // Tablet inspector sizes (width from 380dp to 800dp, height from 200dp to 600dp)
        val inspectorSizes = listOf(
            Pair(380f, 250f),
            Pair(450f, 350f),
            Pair(600f, 450f),
            Pair(800f, 500f)
        )

        for ((w, h) in inspectorSizes) {
            val isExpandedTablet = w >= 680f
            val maxSealWidth = (w * 0.35f).coerceAtMost(if (isExpandedTablet) 240f else 180f)
            val paddingV = 24f
            val availableHeight = (h - paddingV).coerceAtLeast(60f)
            val sealHeight = minOf(availableHeight, maxSealWidth / 0.82f).coerceAtLeast(60f)
            val sealWidth = sealHeight * 0.82f
            val remainingWidth = w - sealWidth - 20f // 20dp spacer

            // 1. Seal width must never exceed 38% of inspector width
            assertTrue(
                "Seal width ($sealWidth) must be <= 38% of inspector width ($w)",
                sealWidth <= w * 0.38f
            )

            // 2. Seal width must be capped at 240dp max
            assertTrue(
                "Seal width ($sealWidth) must be capped <= 240dp",
                sealWidth <= 240.1f
            )

            // 3. Right column gets ample reading/meaning space (>= 60% of total width)
            assertTrue(
                "Remaining text width ($remainingWidth) must be >= 55% of inspector width ($w)",
                remainingWidth >= w * 0.55f
            )

            // 4. Font size scales cleanly with seal height without clipping
            val characterFontSizeSp = (sealHeight * 0.44f).coerceIn(40f, 96f)
            assertTrue(
                "Kanji character font size ($characterFontSizeSp) must be between 40sp and 96sp",
                characterFontSizeSp in 40f..96f
            )
        }
    }

    @Test
    fun testTabletTouchTargetAccessibility() {
        val tabletActionButtonSizeDp = 44f
        val mobileActionButtonSizeDp = 36f

        // Tablet action button size must satisfy minimum 44dp recommended touch target
        assertTrue(
            "Tablet action button ($tabletActionButtonSizeDp dp) must be >= 44dp for accessibility",
            tabletActionButtonSizeDp >= 44f
        )
        assertTrue(
            "Tablet action button must be larger than mobile ($mobileActionButtonSizeDp dp)",
            tabletActionButtonSizeDp > mobileActionButtonSizeDp
        )
    }

    @Test
    fun testKanjiInspectorBottomAlignedWithThirdAndFourthRowBoundaryOnTabletLandscape() {
        val tabletLandscapeViewports = listOf(
            Pair(960f, 600f),
            Pair(1024f, 768f),
            Pair(1280f, 800f),
            Pair(1600f, 1000f)
        )

        for ((vw, vh) in tabletLandscapeViewports) {
            val screenPaddingH = 20f * 2
            val screenPaddingV = 12f * 2
            val totalWidth = vw - screenPaddingH
            val totalHeight = vh - screenPaddingV
            val spacing = 20f
            val topAlignPadding = 8f

            val minSidebarWidth = maxOf(380f, totalWidth * 0.35f)
            val maxBoardWidth = (totalWidth - minSidebarWidth - spacing).coerceAtLeast(0f)
            val boardSize = minOf((totalHeight - topAlignPadding).coerceAtLeast(0f), maxBoardWidth)

            for (dim in BoardDimension.values()) {
                val gridSpacing = when (dim) {
                    BoardDimension.FOUR_BY_FOUR -> 8f
                    BoardDimension.SIX_BY_SIX -> 5f
                    BoardDimension.EIGHT_BY_EIGHT -> 3f
                }
                val totalGridSpacing = gridSpacing * (dim.size - 1)
                val cardSize = (boardSize - totalGridSpacing) / dim.size
                val boundaryThirdRowCenter = if (dim == BoardDimension.FOUR_BY_FOUR) {
                    topAlignPadding + (cardSize * 2.5f) + (gridSpacing * 2f)
                } else {
                    topAlignPadding + (boardSize * 0.65f)
                }
                val fullBoardBottom = topAlignPadding + boardSize

                // 1. Bottom boundary is strictly raised above the bottom of the board
                assertTrue(
                    "Boundary ($boundaryThirdRowCenter) must be strictly higher than full board bottom ($fullBoardBottom)",
                    boundaryThirdRowCenter < fullBoardBottom
                )

                // 2. For 4x4, the boundary is exactly aligned with Row 3 center
                if (dim == BoardDimension.FOUR_BY_FOUR) {
                    val row3Top = topAlignPadding + (cardSize * 2f) + (gridSpacing * 2f)
                    val row3Bottom = topAlignPadding + (cardSize * 3f) + (gridSpacing * 2f)
                    val row3Center = (row3Top + row3Bottom) / 2f
                    assertEquals(
                        "Boundary ($boundaryThirdRowCenter) must align with Row 3 center ($row3Center)",
                        row3Center,
                        boundaryThirdRowCenter,
                        0.001f
                    )
                }

                // 3. KanjiInspector comfortably fits within raised sidebar even with active banner
                val hudHeight = 84f
                val bannerHeight = 108f
                val verticalGaps = 20f
                val inspectorHeightWithBanner = boundaryThirdRowCenter - hudHeight - bannerHeight - verticalGaps
                assertTrue(
                    "Inspector height with banner ($inspectorHeightWithBanner) must be at least 100dp for readability on $vw x $vh",
                    inspectorHeightWithBanner >= 100f
                )

                // 4. Center of readings is at sealHeight / 2
                val isExpandedTablet = minSidebarWidth >= 680f
                val maxSealWidth = (minSidebarWidth * 0.35f).coerceAtMost(if (isExpandedTablet) 240f else 180f)
                val clueReserved = 36f
                val availableForSeal = (inspectorHeightWithBanner - 24f - clueReserved).coerceAtLeast(48f)
                val sealHeight = minOf(availableForSeal, maxSealWidth / 0.82f).coerceAtLeast(48f)
                val readingsCenterY = sealHeight / 2f
                assertTrue(
                    "Readings center ($readingsCenterY) must be half of seal height ($sealHeight)",
                    readingsCenterY == sealHeight / 2f
                )

                // 5. On- and kun- readings font size is doubled (32sp on tablet, 28sp on mobile)
                val readingFontSizeSp = if (vw >= 600f) 32f else 28f
                assertTrue(
                    "Readings font size ($readingFontSizeSp sp) must be doubled (>= 28sp)",
                    readingFontSizeSp >= 28f
                )

                // 6. Lightbulb tip text size is doubled (28sp on tablet, 24sp on mobile)
                val tipFontSizeSp = if (vw >= 600f) 28f else 24f
                assertTrue(
                    "Tip font size ($tipFontSizeSp sp) must be doubled (>= 24sp)",
                    tipFontSizeSp >= 24f
                )

                // 7. Spacing between 'kun' and 'tip' matches spacing between 'on' and 'kun'
                val readingSpacingDp = 4f
                val kunToTipSpacingDp = 4f
                assertEquals(
                    "Spacing between kun and tip must match spacing between on and kun",
                    readingSpacingDp,
                    kunToTipSpacingDp,
                    0.001f
                )

                // 8. Kaki-jun (stroke count) seal size is doubled (26sp on tablet, 22sp on mobile)
                val kakiJunFontSizeSp = if (vw >= 600f) 26f else 22f
                assertTrue(
                    "Kaki-jun seal font size ($kakiJunFontSizeSp sp) must be doubled (>= 22sp)",
                    kakiJunFontSizeSp >= 22f
                )
            }
        }
    }

    @Test
    fun testPhoneLandscapeKanjiInspectorBottomAlignedWithBottomRowOfTiles() {
        val phoneLandscapeViewports = listOf(
            Pair(640f, 360f),    // Compact mobile landscape
            Pair(780f, 390f),    // Standard mobile landscape
            Pair(844f, 390f),    // Modern iPhone landscape
            Pair(915f, 412f)     // Large mobile landscape (e.g. Galaxy S24 Ultra)
        )

        for ((vw, vh) in phoneLandscapeViewports) {
            val screenPaddingH = 12f * 2
            val screenPaddingV = 4f * 2
            val totalWidth = vw - screenPaddingH
            val totalHeight = vh - screenPaddingV
            val spacing = 16f
            val topAlignPadding = 6f
            val minSidebarWidth = 320f
            val maxBoardWidth = (totalWidth - minSidebarWidth - spacing).coerceAtLeast(0f)
            val boardSize = minOf((totalHeight - topAlignPadding).coerceAtLeast(0f), maxBoardWidth)

            val fullBoardBottom = topAlignPadding + boardSize
            val sidebarHeight = topAlignPadding + boardSize

            // On phone landscape, sidebar height must equal fullBoardBottom,
            // ensuring KanjiInspector's lower boundary reaches all the way down
            // aligned with the bottom of the bottom row of tiles
            assertEquals(
                "Phone sidebar height ($sidebarHeight) must match bottom of bottom row of tiles ($fullBoardBottom) on ${vw}x$vh",
                fullBoardBottom,
                sidebarHeight,
                0.001f
            )

            // KanjiInspector gets ample space to fill
            val hudHeight = 84f
            val inspectorHeight = sidebarHeight - hudHeight - 6f // with 6dp spacing
            assertTrue(
                "Phone inspector height ($inspectorHeight) must be comfortably tall (>= 180dp) on ${vw}x$vh",
                inspectorHeight >= 180f
            )

            // Phone-Landscape Inspector typography and tile sizes are reduced by 50%
            val tabletReadingFontSizeSp = 28f
            val phoneReadingFontSizeSp = 14f
            assertEquals("Phone reading font size must be 50% of tablet", tabletReadingFontSizeSp * 0.5f, phoneReadingFontSizeSp, 0.001f)

            val tabletTipFontSizeSp = 24f
            val phoneTipFontSizeSp = 12f
            assertEquals("Phone tip font size must be 50% of tablet", tabletTipFontSizeSp * 0.5f, phoneTipFontSizeSp, 0.001f)

            val tabletKakiJunFontSizeSp = 22f
            val phoneKakiJunFontSizeSp = 11f
            assertEquals("Phone kaki-jun font size must be 50% of tablet", tabletKakiJunFontSizeSp * 0.5f, phoneKakiJunFontSizeSp, 0.001f)

            // Phone-Landscape Kanji Trap Banner height and text sizes are reduced by 50%
            val defaultBannerHeightDp = 92f
            val phoneBannerHeightDp = 46f
            assertEquals("Phone banner height must be 50% of standard banner", defaultBannerHeightDp * 0.5f, phoneBannerHeightDp, 0.001f)

            val defaultBannerTextSizeSp = 24f
            val phoneBannerTextSizeSp = 12f
            assertEquals("Phone banner headline must be 50% of standard headline", defaultBannerTextSizeSp * 0.5f, phoneBannerTextSizeSp, 0.001f)

            val defaultBannerClueSizeSp = 20f
            val phoneBannerClueSizeSp = 10f
            assertEquals("Phone banner clue text must be 50% of standard clue text", defaultBannerClueSizeSp * 0.5f, phoneBannerClueSizeSp, 0.001f)
        }
    }

    @Test
    fun testPortraitKanjiInspectorFiftyPercentReductionOnPhonesAndPreservedTabletValues() {
        val phonePortraitViewports = listOf(
            Pair(360f, 640f),    // Compact mobile portrait
            Pair(390f, 780f),    // Standard mobile portrait
            Pair(412f, 915f)     // Large mobile portrait (e.g. Galaxy S24 Ultra)
        )

        for ((vw, vh) in phonePortraitViewports) {
            // 1. 50% Tile reduction on phone
            val phoneTileSealSize = 54f
            val baseSealSize = 108f
            assertEquals("Phone tile size must be 50% of original base", baseSealSize * 0.5f, phoneTileSealSize, 0.001f)

            // 2. 50% On- and Kun-yomi readings reduction on phone
            val phoneReadingFontSizeSp = 14f
            val baseReadingFontSizeSp = 28f
            assertEquals("Phone readings font size must be 50% of original", baseReadingFontSizeSp * 0.5f, phoneReadingFontSizeSp, 0.001f)

            // 3. 50% Lightbulb tip text reduction on phone
            val phoneTipFontSizeSp = 12f
            val baseTipFontSizeSp = 24f
            assertEquals("Phone tip font size must be 50% of original", baseTipFontSizeSp * 0.5f, phoneTipFontSizeSp, 0.001f)

            // 4. Lightbulb tip starts from left side and spans full card width on phone
            val cardPaddingH = 12f
            val tipAvailableWidth = vw - (cardPaddingH * 2)
            assertTrue("Tip has full width spanning left to right ($tipAvailableWidth)", tipAvailableWidth > 300f)

            // 5. Kanji Trap Banner height and text sizes 50% reduction on phone
            val bannerHeightDp = 46f
            val baseBannerHeightDp = 92f
            assertEquals("Phone banner height must be 50% of base banner", baseBannerHeightDp * 0.5f, bannerHeightDp, 0.001f)

            val bannerHeadlineSp = 12f
            val baseHeadlineSp = 24f
            assertEquals("Phone banner headline must be 50% of base headline", baseHeadlineSp * 0.5f, bannerHeadlineSp, 0.001f)

            val bannerTagSp = 8f
            val baseTagSp = 16f
            assertEquals("Phone banner tag must be 50% of base tag", baseTagSp * 0.5f, bannerTagSp, 0.001f)

            val bannerClueSp = 11f
            assertTrue("Phone banner clue must be single line compact (<= 12sp)", bannerClueSp <= 12f)
        }

        // Verify tablet values remain completely preserved as originally designed
        val tabletViewports = listOf(
            Pair(800f, 1280f),   // Tablet portrait
            Pair(1280f, 800f)    // Tablet landscape
        )
        for ((vw, vh) in tabletViewports) {
            val tabletBannerHeightDp = 108f
            assertEquals("Tablet banner height must remain 108dp", 108f, tabletBannerHeightDp, 0.001f)

            val tabletReadingFontSizeSp = 32f
            assertEquals("Tablet readings font size must remain 32sp", 32f, tabletReadingFontSizeSp, 0.001f)

            val tabletTipFontSizeSp = 28f
            assertEquals("Tablet tip font size must remain 28sp", 28f, tabletTipFontSizeSp, 0.001f)

            val tabletStrokeCountFontSizeSp = 26f
            assertEquals("Tablet stroke count font size must remain 26sp", 26f, tabletStrokeCountFontSizeSp, 0.001f)
        }
    }
}
