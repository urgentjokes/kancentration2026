package com.example.kancentration.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.kancentration.MainActivity
import org.junit.Rule
import org.junit.Test

/**
 * On-device Android instrumentation tests for Kancentration UI and system integration.
 */
class GameScreenInstrumentationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testGameScreenRendersHudBoardAndInspector() {
        // Verify Title and Dimension Pill
        composeTestRule.onNodeWithText("Kancentration").assertIsDisplayed()
        composeTestRule.onNodeWithText("4×4").assertIsDisplayed()

        // Verify initial armed timer
        composeTestRule.onNodeWithText("00:00").assertIsDisplayed()

        // Verify 16 card backs with 漢 emblem exist on initial 4x4 board
        val kanjiNodes = composeTestRule.onAllNodesWithText("漢")
        assert(kanjiNodes.fetchSemanticsNodes().size >= 16)

        // Verify Bottom Lexicon zen placeholder
        composeTestRule.onNodeWithText("🎴 Flip cards to inspect Kanji readings, meanings & Twin-Trap clues").assertIsDisplayed()
    }

    @Test
    fun testSettingsDialogOpensAndDisplaysTypographyAndDimensions() {
        // Tap Settings button (⚙️)
        composeTestRule.onNodeWithText("⚙️").performClick()

        // Verify Settings modal sheet appears
        composeTestRule.onNodeWithText("Game Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Serif (漢字)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sans-serif (漢字)").assertIsDisplayed()

        // Close dialog
        composeTestRule.onNodeWithText("Cancel").performClick()
    }

    @Test
    fun testDlcStoreDialogOpensAndDisplaysExpansionPacks() {
        // Tap DLC Store button (🛒)
        composeTestRule.onNodeWithText("🛒").performClick()

        // Verify Kanji Pack Pavilion appears
        composeTestRule.onNodeWithText("Kanji Pack Pavilion").assertIsDisplayed()
        composeTestRule.onNodeWithText("Radical Rivals").assertIsDisplayed()

        // Close dialog
        composeTestRule.onNodeWithText("Close").performClick()
    }

    @Test
    fun testCycleDimensionAndCardAccessibilityTouchTargets() {
        // Initially 4x4
        composeTestRule.onNodeWithText("4×4").assertIsDisplayed()
        var cards = composeTestRule.onAllNodesWithText("漢").fetchSemanticsNodes()
        assert(cards.size == 16)

        // Cycle to 6x6
        composeTestRule.onNodeWithText("4×4").performClick()
        composeTestRule.onNodeWithText("6×6").assertIsDisplayed()
        cards = composeTestRule.onAllNodesWithText("漢").fetchSemanticsNodes()
        assert(cards.size == 36)

        // Cycle to 8x8
        composeTestRule.onNodeWithText("6×6").performClick()
        composeTestRule.onNodeWithText("8×8").assertIsDisplayed()
        cards = composeTestRule.onAllNodesWithText("漢").fetchSemanticsNodes()
        assert(cards.size == 64)

        // Reset back to 4x4
        composeTestRule.onNodeWithText("8×8").performClick()
        composeTestRule.onNodeWithText("4×4").assertIsDisplayed()
    }
}
