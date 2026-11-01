package com.example.kancentration.data.repository

import com.example.kancentration.data.model.BoardDimension
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.random.Random

class KanjiRepositoryTest {

    private lateinit var repository: DefaultKanjiRepository

    @Before
    fun setup() {
        val file = File("src/main/assets/kanji_base_pack.json")
        val json = file.readText()
        repository = DefaultKanjiRepository(basePackJsonProvider = { json })
    }

    @Test
    fun testBasePackLoadedWith20DisjointQuads() {
        val quads = repository.getAllAvailableQuads()
        assertEquals("Repository must load 20 quads from base pack", 20, quads.size)

        val allCharacters = quads.flatMap { listOf(it.pairA.character, it.pairB.character) }
        assertEquals("Total characters must be 40", 40, allCharacters.size)

        val uniqueCharacters = allCharacters.toSet()
        assertEquals("All 40 characters in the base pack must be 100% unique", 40, uniqueCharacters.size)
    }

    // ==============================================================
    // 4. PERFECT ARRAY POPULATION SIZES WITH ZERO DUPLICATION
    // ==============================================================

    @Test
    fun testPerfectArrayPopulationSize16CardsFourByFour() {
        val dimension = BoardDimension.FOUR_BY_FOUR
        assertEquals("4x4 must require 16 total cards", 16, dimension.totalCards)
        assertEquals("4x4 must require 8 pairs", 8, dimension.requiredPairs)
        assertEquals("4x4 must require 4 quads", 4, dimension.requiredQuads)

        repeat(50) { seed ->
            val deck = repository.generateDeck(dimension, Random(seed))

            // 1. Exact Array Size
            assertEquals("Deck array size must be exactly 16", 16, deck.size)

            // 2. Exactly 4 quads selected
            val quadsMap = deck.groupBy { it.quadId }
            assertEquals("Deck must be populated by exactly 4 quads", 4, quadsMap.size)

            // 3. Each quad supplies exactly 4 cards (2 of Pair A and 2 of Pair B)
            for ((quadId, cardsInQuad) in quadsMap) {
                assertEquals("Quad '$quadId' must supply exactly 4 cards", 4, cardsInQuad.size)

                val charsInQuad = cardsInQuad.groupBy { it.kanji.character }
                assertEquals("Quad '$quadId' must have exactly 2 distinct characters", 2, charsInQuad.size)

                for ((char, charCards) in charsInQuad) {
                    assertEquals("Character '$char' in quad '$quadId' must appear exactly twice", 2, charCards.size)
                }
            }

            // 4. Zero character duplication across the entire board (exactly 8 unique characters, each appearing 2 times)
            val characterCounts = deck.map { it.kanji.character }.groupingBy { it }.eachCount()
            assertEquals("Board must contain exactly 8 unique characters", 8, characterCounts.size)

            for ((char, count) in characterCounts) {
                assertEquals(
                    "Character '$char' must appear exactly twice (zero cross-quad leakage or duplicate pairs), found: $count",
                    2,
                    count
                )
            }

            // 5. Unique instance IDs
            val instanceIds = deck.map { it.instanceId }.toSet()
            assertEquals("All 16 cards must possess unique instanceIds", 16, instanceIds.size)
        }
    }

    @Test
    fun testPerfectArrayPopulationSize36CardsSixBySix() {
        val dimension = BoardDimension.SIX_BY_SIX
        assertEquals("6x6 must require 36 total cards", 36, dimension.totalCards)
        assertEquals("6x6 must require 18 pairs", 18, dimension.requiredPairs)
        assertEquals("6x6 must require 9 quads", 9, dimension.requiredQuads)

        repeat(50) { seed ->
            val deck = repository.generateDeck(dimension, Random(seed))

            // 1. Exact Array Size
            assertEquals("Deck array size must be exactly 36", 36, deck.size)

            // 2. Exactly 9 quads selected
            val quadsMap = deck.groupBy { it.quadId }
            assertEquals("Deck must be populated by exactly 9 quads", 9, quadsMap.size)

            // 3. Each quad supplies exactly 4 cards
            for ((quadId, cardsInQuad) in quadsMap) {
                assertEquals("Quad '$quadId' must supply exactly 4 cards", 4, cardsInQuad.size)
            }

            // 4. Zero character duplication across the entire board (exactly 18 unique characters, each appearing 2 times)
            val characterCounts = deck.map { it.kanji.character }.groupingBy { it }.eachCount()
            assertEquals("Board must contain exactly 18 unique characters", 18, characterCounts.size)

            for ((char, count) in characterCounts) {
                assertEquals(
                    "Character '$char' must appear exactly twice on 6x6, found: $count",
                    2,
                    count
                )
            }

            // 5. Unique instance IDs
            val instanceIds = deck.map { it.instanceId }.toSet()
            assertEquals("All 36 cards must possess unique instanceIds", 36, instanceIds.size)
        }
    }

    @Test
    fun testPerfectArrayPopulationSize64CardsEightByEight() {
        val dimension = BoardDimension.EIGHT_BY_EIGHT
        assertEquals("8x8 must require 64 total cards", 64, dimension.totalCards)
        assertEquals("8x8 must require 32 pairs", 32, dimension.requiredPairs)
        assertEquals("8x8 must require 16 quads", 16, dimension.requiredQuads)

        repeat(50) { seed ->
            val deck = repository.generateDeck(dimension, Random(seed))

            // 1. Exact Array Size
            assertEquals("Deck array size must be exactly 64", 64, deck.size)

            // 2. Exactly 16 quads selected
            val quadsMap = deck.groupBy { it.quadId }
            assertEquals("Deck must be populated by exactly 16 quads", 16, quadsMap.size)

            // 3. Each quad supplies exactly 4 cards
            for ((quadId, cardsInQuad) in quadsMap) {
                assertEquals("Quad '$quadId' must supply exactly 4 cards", 4, cardsInQuad.size)
            }

            // 4. Zero character duplication across the entire board (exactly 32 unique characters, each appearing 2 times)
            val characterCounts = deck.map { it.kanji.character }.groupingBy { it }.eachCount()
            assertEquals("Board must contain exactly 32 unique characters", 32, characterCounts.size)

            for ((char, count) in characterCounts) {
                assertEquals(
                    "Character '$char' must appear exactly twice on 8x8, found: $count",
                    2,
                    count
                )
            }

            // 5. Unique instance IDs
            val instanceIds = deck.map { it.instanceId }.toSet()
            assertEquals("All 64 cards must possess unique instanceIds", 64, instanceIds.size)
        }
    }

    @Test
    fun testDeckGenerationSeedDeterminism() {
        val dimension = BoardDimension.FOUR_BY_FOUR
        val seed = 424242L

        val deck1 = repository.generateDeck(dimension, Random(seed))
        val deck2 = repository.generateDeck(dimension, Random(seed))

        // Same seed yields identical character sequence
        val chars1 = deck1.map { it.kanji.character }
        val chars2 = deck2.map { it.kanji.character }
        assertEquals("Identical seeds must produce identical board character layout", chars1, chars2)

        val quadIds1 = deck1.map { it.quadId }
        val quadIds2 = deck2.map { it.quadId }
        assertEquals("Identical seeds must produce identical quad layout", quadIds1, quadIds2)

        // Different seeds yield distinct layouts
        val deckDifferent = repository.generateDeck(dimension, Random(999999L))
        val charsDifferent = deckDifferent.map { it.kanji.character }
        assertNotEquals("Different seeds should produce different board layouts", chars1, charsDifferent)
    }

    @Test
    fun testConfusableTwinCardLinkingInGeneratedDeck() {
        val dimension = BoardDimension.FOUR_BY_FOUR
        val deck = repository.generateDeck(dimension, Random(12345))

        for (card in deck) {
            val twinChar = card.confusableTwinCharacter
            assertTrue("Twin character must not be blank", twinChar.isNotBlank())
            assertNotEquals("Twin character must be distinct from card character", card.kanji.character, twinChar)

            // The twin character must exist on the board
            val matchingTwinCards = deck.filter { it.kanji.character == twinChar }
            assertEquals("Twin character '$twinChar' must appear exactly twice on the board", 2, matchingTwinCards.size)
            assertEquals("Twin cards must share the same quadId as card", card.quadId, matchingTwinCards[0].quadId)
        }
    }
}
