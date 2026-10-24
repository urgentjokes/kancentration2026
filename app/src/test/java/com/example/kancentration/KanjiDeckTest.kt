package com.example.kancentration

import com.example.kancentration.data.model.BoardCard
import com.example.kancentration.data.model.BoardDimension
import com.example.kancentration.data.model.KanjiData
import com.example.kancentration.data.model.KanjiQuad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.UUID

class KanjiDeckTest {

    private fun loadBaseQuads(): List<KanjiQuad> {
        val file = File("src/main/assets/kanji_base_pack.json")
        val json = file.readText()

        // Extract quad ids
        val quadRegex = Regex(""""id":\s*"(quad_[^"]+)"""")
        val charRegex = Regex(""""character":\s*"([^"]+)"""")

        val quadMatches = quadRegex.findAll(json).map { it.groupValues[1] }.toList()
        val charMatches = charRegex.findAll(json).map { it.groupValues[1] }.toList()

        val list = mutableListOf<KanjiQuad>()
        for (i in quadMatches.indices) {
            val charA = charMatches[i * 2]
            val charB = charMatches[i * 2 + 1]

            val kanjiA = KanjiData(id = "k_${charA}", character = charA, meanings = listOf("meaning"), onReadings = emptyList(), kunReadings = emptyList(), strokeCount = 1, distinctionTip = "tip", wiktionaryUrl = "")
            val kanjiB = KanjiData(id = "k_${charB}", character = charB, meanings = listOf("meaning"), onReadings = emptyList(), kunReadings = emptyList(), strokeCount = 1, distinctionTip = "tip", wiktionaryUrl = "")
            list.add(KanjiQuad(id = quadMatches[i], category = "test", pairA = kanjiA, pairB = kanjiB))
        }
        return list
    }

    @Test
    fun testNoDuplicateKanjiInBasePack() {
        val quads = loadBaseQuads()
        val characters = mutableListOf<String>()
        for (q in quads) {
            characters.add(q.pairA.character)
            characters.add(q.pairB.character)
        }

        // Must not contain archaic kyujitai 賣
        assertFalse("Pack must not contain kyujitai 賣", characters.contains("賣"))

        // Check for duplicates
        val counts = characters.groupingBy { it }.eachCount()
        val duplicates = counts.filter { it.value > 1 }
        assertTrue("No kanji should be duplicated in pack, but found: $duplicates", duplicates.isEmpty())
        assertEquals("Pack should contain 40 unique characters", 40, characters.size)
    }

    @Test
    fun testGenerateDeckNeverDuplicatesKanjiPairOnBoard() {
        val quads = loadBaseQuads()

        fun generateDeck(dimension: BoardDimension): List<BoardCard> {
            val requiredQuads = dimension.requiredQuads
            val allQuads = quads.shuffled()

            val selectedQuads = mutableListOf<KanjiQuad>()
            val seenKanjiCharacters = mutableSetOf<String>()

            for (quad in allQuads) {
                val charA = quad.pairA.character
                val charB = quad.pairB.character
                if (charA !in seenKanjiCharacters && charB !in seenKanjiCharacters) {
                    selectedQuads.add(quad)
                    seenKanjiCharacters.add(charA)
                    seenKanjiCharacters.add(charB)
                    if (selectedQuads.size == requiredQuads) {
                        break
                    }
                }
            }

            val cards = mutableListOf<BoardCard>()
            for (quad in selectedQuads) {
                cards.add(BoardCard(instanceId = UUID.randomUUID().toString(), kanji = quad.pairA, quadId = quad.id))
                cards.add(BoardCard(instanceId = UUID.randomUUID().toString(), kanji = quad.pairA, quadId = quad.id))
                cards.add(BoardCard(instanceId = UUID.randomUUID().toString(), kanji = quad.pairB, quadId = quad.id))
                cards.add(BoardCard(instanceId = UUID.randomUUID().toString(), kanji = quad.pairB, quadId = quad.id))
            }
            return cards
        }

        for (dim in listOf(BoardDimension.FOUR_BY_FOUR, BoardDimension.SIX_BY_SIX, BoardDimension.EIGHT_BY_EIGHT)) {
            repeat(20) {
                val deck = generateDeck(dim)
                assertEquals(dim.totalCards, deck.size)

                val charCounts = deck.map { it.kanji.character }.groupingBy { it }.eachCount()
                for ((char, count) in charCounts) {
                    assertEquals(
                        "Kanji '$char' on board dimension $dim should appear exactly twice (1 pair), but appeared $count times",
                        2,
                        count
                    )
                }
            }
        }
    }
}
