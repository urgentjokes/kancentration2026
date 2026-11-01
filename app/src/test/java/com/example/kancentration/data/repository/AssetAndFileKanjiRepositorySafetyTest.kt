package com.example.kancentration.data.repository

import com.example.kancentration.data.model.BoardDimension
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Isolated unit tests verifying that AssetAndFileKanjiRepository and its JSON parsing engine
 * defensively handle invalid, incomplete, or empty JSON matrices with zero runtime exceptions.
 */
class AssetAndFileKanjiRepositorySafetyTest {

    @Test
    fun testEmptyAndWhitespaceJsonFallsBackToCoreQuadsWithoutCrashing() {
        val emptyRepo = AssetAndFileKanjiRepository(basePackJsonProvider = { "" })
        val quads = emptyRepo.getAllAvailableQuads()
        assertTrue("Empty base JSON must fall back to at least 16 default core quads", quads.size >= 16)

        // Verify that 4x4, 6x6, and 8x8 decks can all be generated successfully
        for (dimension in BoardDimension.values()) {
            val deck = emptyRepo.generateDeck(dimension, Random(42))
            assertEquals(dimension.totalCards, deck.size)
            val uniqueChars = deck.map { it.kanji.character }.toSet()
            assertEquals(dimension.requiredPairs, uniqueChars.size)
        }

        val whitespaceRepo = AssetAndFileKanjiRepository(basePackJsonProvider = { "   \n\t\r   " })
        assertEquals(quads.size, whitespaceRepo.getAllAvailableQuads().size)
    }

    @Test
    fun testMalformedJsonSyntaxNeverThrowsAndFallsBack() {
        val malformedPayloads = listOf(
            "{ \"quads\": [ { \"id\": \"incomplete\"",
            "[{]]}!!<<MALFORMED_GARBAGE>>",
            "{\"quads\": null}",
            "{\"quads\": 12345}",
            "{\"quads\": \"this is a string not an array\"}",
            "{\"quads\": [ { \"pairA\": null } ]}",
            "{\"quads\": [ { \"id\": \"q1\", \"pairA\": \"string_not_obj\" } ]}",
            "{\"quads\": [ { \"id\": \"q1\", \"pairA\": { \"character\": \"\\uZZZZ\" } } ]}",
            "{{{",
            "}"
        )

        for (payload in malformedPayloads) {
            val repo = AssetAndFileKanjiRepository(basePackJsonProvider = { payload })
            val quads = repo.getAllAvailableQuads()
            assertTrue("Malformed JSON must never crash repo, falling back to core quads", quads.isNotEmpty())
            val deck4x4 = repo.generateDeck(BoardDimension.FOUR_BY_FOUR, Random(101))
            assertEquals(16, deck4x4.size)
        }
    }

    @Test
    fun testIncompleteQuadsMissingFieldsFilteredOut() {
        val incompleteJson = """
            {
              "quads": [
                {
                  "category": "Missing ID"
                },
                {
                  "id": "missing_pair_b",
                  "pairA": {
                    "id": "k1",
                    "character": "日",
                    "meanings": ["day"]
                  }
                },
                {
                  "id": "empty_char",
                  "pairA": {
                    "id": "k1",
                    "character": "",
                    "meanings": ["empty"]
                  },
                  "pairB": {
                    "id": "k2",
                    "character": "月",
                    "meanings": ["moon"]
                  }
                },
                {
                  "id": "empty_meanings",
                  "pairA": {
                    "id": "k1",
                    "character": "日",
                    "meanings": []
                  },
                  "pairB": {
                    "id": "k2",
                    "character": "月",
                    "meanings": ["moon"]
                  }
                },
                {
                  "id": "valid_quad",
                  "pairA": {
                    "id": "k1",
                    "character": "明",
                    "meanings": ["bright"]
                  },
                  "pairB": {
                    "id": "k2",
                    "character": "暗",
                    "meanings": ["dark"]
                  }
                }
              ]
            }
        """.trimIndent()

        val parsed = KanjiQuadParser.parseQuads(incompleteJson)
        assertEquals("Only the 1 completely valid quad must be parsed", 1, parsed.size)
        assertEquals("valid_quad", parsed[0].id)
        assertEquals("明", parsed[0].pairA.character)
        assertEquals("暗", parsed[0].pairB.character)
    }

    @Test
    fun testQuadWithIdenticalCharactersRejected() {
        val identicalCharJson = """
            {
              "quads": [
                {
                  "id": "quad_same_char",
                  "pairA": {
                    "id": "k1",
                    "character": "日",
                    "meanings": ["day"]
                  },
                  "pairB": {
                    "id": "k2",
                    "character": "日",
                    "meanings": ["sun"]
                  }
                }
              ]
            }
        """.trimIndent()

        val parsed = KanjiQuadParser.parseQuads(identicalCharJson)
        assertTrue("Quad where pairA and pairB have the identical character must be rejected", parsed.isEmpty())
    }

    @Test
    fun testDuplicateKanjiWithinSamePackDeduplicated() {
        val duplicateKanjiJson = """
            {
              "quads": [
                {
                  "id": "q1",
                  "pairA": {
                    "id": "k1",
                    "character": "日",
                    "meanings": ["day"]
                  },
                  "pairB": {
                    "id": "k2",
                    "character": "曰",
                    "meanings": ["say"]
                  }
                },
                {
                  "id": "q2",
                  "pairA": {
                    "id": "k3",
                    "character": "日",
                    "meanings": ["sun"]
                  },
                  "pairB": {
                    "id": "k4",
                    "character": "木",
                    "meanings": ["tree"]
                  }
                }
              ]
            }
        """.trimIndent()

        val parsed = KanjiQuadParser.parseQuads(duplicateKanjiJson)
        assertEquals("Second quad reusing '日' must be rejected to prevent duplicate characters in pack", 1, parsed.size)
        assertEquals("q1", parsed[0].id)
    }

    @Test
    fun testDuplicateKanjiAcrossDlcAndBasePackDeduplicated() {
        val baseChars = setOf("日", "曰", "木", "本")

        val dlcJson = """
            {
              "quads": [
                {
                  "id": "dlc_quad_colliding",
                  "pairA": {
                    "id": "k_colliding",
                    "character": "日",
                    "meanings": ["day"]
                  },
                  "pairB": {
                    "id": "k_new1",
                    "character": "新",
                    "meanings": ["new"]
                  }
                },
                {
                  "id": "dlc_quad_valid",
                  "pairA": {
                    "id": "k_clean1",
                    "character": "車",
                    "meanings": ["car"]
                  },
                  "pairB": {
                    "id": "k_clean2",
                    "character": "東",
                    "meanings": ["east"]
                  }
                }
              ]
            }
        """.trimIndent()

        val parsed = KanjiQuadParser.parseQuads(dlcJson, existingCharacters = baseChars)
        assertEquals("DLC quad colliding with existing base characters must be rejected", 1, parsed.size)
        assertEquals("dlc_quad_valid", parsed[0].id)
        assertEquals("車", parsed[0].pairA.character)
        assertEquals("東", parsed[0].pairB.character)
    }

    @Test
    fun testBrokenDlcPackIgnoredWhileBasePackRemainsIntact() {
        val baseJson = """
            {
              "quads": [
                {
                  "id": "base_1",
                  "pairA": { "id": "b1", "character": "日", "meanings": ["day"] },
                  "pairB": { "id": "b2", "character": "曰", "meanings": ["say"] }
                }
              ]
            }
        """.trimIndent()

        val brokenDlc = "{ corrupted json payload [["
        val validDlc = """
            {
              "quads": [
                {
                  "id": "dlc_valid",
                  "pairA": { "id": "d1", "character": "車", "meanings": ["car"] },
                  "pairB": { "id": "d2", "character": "東", "meanings": ["east"] }
                }
              ]
            }
        """.trimIndent()

        val repo = AssetAndFileKanjiRepository(
            basePackJsonProvider = { baseJson },
            dlcPacksJsonProvider = { listOf(brokenDlc, validDlc) }
        )

        val quads = repo.getAllAvailableQuads()
        assertTrue("Repository must load base quads plus valid DLC quads without crashing", quads.isNotEmpty())
        val quadIds = quads.map { it.id }
        assertTrue("Must include valid DLC quad", quadIds.contains("dlc_valid"))
        assertTrue("Must include base quad", quadIds.contains("base_1"))
    }

    @Test
    fun testBasePackProviderThrowsExceptionGracefulFallback() {
        val repo = AssetAndFileKanjiRepository(
            basePackJsonProvider = { throw IllegalStateException("Simulated disk read exception") }
        )

        // Must not throw exception
        val quads = repo.getAllAvailableQuads()
        assertEquals("Must fall back to all 20 default core quads", 20, quads.size)

        // Must generate decks across all dimensions cleanly
        val deck4x4 = repo.generateDeck(BoardDimension.FOUR_BY_FOUR, Random(1))
        val deck6x6 = repo.generateDeck(BoardDimension.SIX_BY_SIX, Random(2))
        val deck8x8 = repo.generateDeck(BoardDimension.EIGHT_BY_EIGHT, Random(3))

        assertEquals(16, deck4x4.size)
        assertEquals(36, deck6x6.size)
        assertEquals(64, deck8x8.size)
    }
}
