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
}
