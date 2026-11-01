package com.example.kancentration

import com.example.kancentration.data.model.BoardDimension
import com.example.kancentration.data.repository.DefaultKanjiRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

class KanjiDeckTest {

    private fun createRepository(): DefaultKanjiRepository {
        val file = File("src/main/assets/kanji_base_pack.json")
        val json = file.readText()
        return DefaultKanjiRepository(basePackJsonProvider = { json })
    }

    @Test
    fun testBasePackContainsSignatureQuadsAndNoDuplicates() {
        val repo = createRepository()
        val quads = repo.getAllAvailableQuads()

        assertEquals("Pack should contain 20 quads", 20, quads.size)

        val characters = mutableListOf<String>()
        for (q in quads) {
            characters.add(q.pairA.character)
            characters.add(q.pairB.character)
        }

        // Verify all 40 characters are unique
        val counts = characters.groupingBy { it }.eachCount()
        val duplicates = counts.filter { it.value > 1 }
        assertTrue("No kanji should be duplicated in pack, but found: $duplicates", duplicates.isEmpty())
        assertEquals("Pack should contain exactly 40 unique characters", 40, characters.size)

        // Verify DESIGN.md signature pairs exist in the pack
        val charSet = characters.toSet()
        assertTrue("Must contain 木", charSet.contains("木"))
        assertTrue("Must contain 本", charSet.contains("本"))
        assertTrue("Must contain 持", charSet.contains("持"))
        assertTrue("Must contain 特", charSet.contains("特"))
        assertTrue("Must contain 偉", charSet.contains("偉"))
        assertTrue("Must contain 違", charSet.contains("違"))
        assertTrue("Must contain 日", charSet.contains("日"))
        assertTrue("Must contain 曰", charSet.contains("曰"))

        // Must not contain archaic kyujitai 賣
        assertFalse("Pack must not contain kyujitai 賣", charSet.contains("賣"))
    }

    @Test
    fun testDisjointDeckGeneration4x4() {
        val repo = createRepository()
        val dimension = BoardDimension.FOUR_BY_FOUR

        repeat(20) { seed ->
            val deck = repo.generateDeck(dimension, Random(seed))
            assertEquals(16, deck.size)

            // Group by quadId: exactly 4 quads, each supplying 4 cards
            val cardsByQuad = deck.groupBy { it.quadId }
            assertEquals("4x4 must have exactly 4 quads", 4, cardsByQuad.size)

            for ((quadId, cards) in cardsByQuad) {
                assertEquals("Each quad in 4x4 must contribute 4 cards (2 pairs)", 4, cards.size)
                val byChar = cards.groupBy { it.kanji.character }
                assertEquals("Each quad must have exactly 2 distinct characters", 2, byChar.size)
                for ((char, charCards) in byChar) {
                    assertEquals("Character '$char' in quad '$quadId' must appear exactly twice", 2, charCards.size)
                }
            }

            // Zero cross-quad leakage: total characters across the board must be 8 unique characters, each appearing 2 times
            val charCounts = deck.map { it.kanji.character }.groupingBy { it }.eachCount()
            assertEquals("4x4 deck must have 8 unique characters", 8, charCounts.size)
            for ((char, count) in charCounts) {
                assertEquals("Kanji '$char' on 4x4 must appear exactly twice (1 pair)", 2, count)
            }

            // Verify confusableTwinCharacter linking
            for (card in deck) {
                assertTrue("Card must have a valid non-empty instanceId", card.instanceId.isNotEmpty())
                assertTrue("Card must have non-empty confusable twin character", card.confusableTwinCharacter.isNotEmpty())
                val twinCards = deck.filter { it.kanji.character == card.confusableTwinCharacter }
                assertEquals("Twin '${card.confusableTwinCharacter}' must be present on board exactly twice", 2, twinCards.size)
                assertEquals("Twin cards must share the same quadId", card.quadId, twinCards[0].quadId)
            }
        }
    }

    @Test
    fun testDisjointDeckGeneration6x6() {
        val repo = createRepository()
        val dimension = BoardDimension.SIX_BY_SIX

        repeat(20) { seed ->
            val deck = repo.generateDeck(dimension, Random(seed))
            assertEquals(36, deck.size)

            val cardsByQuad = deck.groupBy { it.quadId }
            assertEquals("6x6 must have exactly 9 quads", 9, cardsByQuad.size)

            for ((quadId, cards) in cardsByQuad) {
                assertEquals("Each quad in 6x6 must contribute 4 cards", 4, cards.size)
            }

            val charCounts = deck.map { it.kanji.character }.groupingBy { it }.eachCount()
            assertEquals("6x6 deck must have 18 unique characters", 18, charCounts.size)
            for ((char, count) in charCounts) {
                assertEquals("Kanji '$char' on 6x6 must appear exactly twice", 2, count)
            }
        }
    }

    @Test
    fun testDisjointDeckGeneration8x8() {
        val repo = createRepository()
        val dimension = BoardDimension.EIGHT_BY_EIGHT

        repeat(20) { seed ->
            val deck = repo.generateDeck(dimension, Random(seed))
            assertEquals(64, deck.size)

            val cardsByQuad = deck.groupBy { it.quadId }
            assertEquals("8x8 must have exactly 16 quads", 16, cardsByQuad.size)

            for ((quadId, cards) in cardsByQuad) {
                assertEquals("Each quad in 8x8 must contribute 4 cards", 4, cards.size)
            }

            val charCounts = deck.map { it.kanji.character }.groupingBy { it }.eachCount()
            assertEquals("8x8 deck must have 32 unique characters", 32, charCounts.size)
            for ((char, count) in charCounts) {
                assertEquals("Kanji '$char' on 8x8 must appear exactly twice", 2, count)
            }
        }
    }
}
