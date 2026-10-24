package com.example.kancentration.data.repository

import android.content.Context
import com.example.kancentration.data.model.BoardCard
import com.example.kancentration.data.model.BoardDimension
import com.example.kancentration.data.model.KanjiData
import com.example.kancentration.data.model.KanjiQuad
import org.json.JSONObject
import java.io.File
import java.util.UUID

class KanjiRepository(private val context: Context) {

    private val baseQuads: MutableList<KanjiQuad> = mutableListOf()
    private val dlcQuads: MutableList<KanjiQuad> = mutableListOf()

    init {
        loadBasePack()
        loadInstalledDlcPacks()
    }

    private fun loadBasePack() {
        try {
            val jsonString = context.assets.open("kanji_base_pack.json").bufferedReader().use { it.readText() }
            val quads = parseQuadsFromJson(jsonString)
            baseQuads.clear()
            baseQuads.addAll(quads)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadInstalledDlcPacks() {
        try {
            dlcQuads.clear()
            val dlcDir = File(context.filesDir, "dlc_packs")
            if (dlcDir.exists() && dlcDir.isDirectory) {
                dlcDir.listFiles { file -> file.extension == "json" && file.name != "catalog.json" }?.forEach { packFile ->
                    val json = packFile.readText()
                    dlcQuads.addAll(parseQuadsFromJson(json))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllAvailableQuads(): List<KanjiQuad> {
        return baseQuads + dlcQuads
    }

    /**
     * Generates a randomized deck of cards for the given board dimension.
     * Each quad provides 2 pairs (4 cards total): 2 of PairA and 2 of PairB.
     * Guarantees that no kanji character or pair appears more than once on a single game board.
     */
    fun generateDeck(dimension: BoardDimension): List<BoardCard> {
        val requiredQuads = dimension.requiredQuads
        val allQuads = getAllAvailableQuads().shuffled()

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

        // Fallback in case not enough strictly disjoint quads exist
        if (selectedQuads.size < requiredQuads) {
            for (quad in allQuads) {
                if (!selectedQuads.contains(quad)) {
                    selectedQuads.add(quad)
                    if (selectedQuads.size == requiredQuads) break
                }
            }
        }

        val cards = mutableListOf<BoardCard>()
        for (quad in selectedQuads) {
            // 2 cards of Pair A
            cards.add(BoardCard(instanceId = UUID.randomUUID().toString(), kanji = quad.pairA, quadId = quad.id))
            cards.add(BoardCard(instanceId = UUID.randomUUID().toString(), kanji = quad.pairA, quadId = quad.id))
            // 2 cards of Pair B
            cards.add(BoardCard(instanceId = UUID.randomUUID().toString(), kanji = quad.pairB, quadId = quad.id))
            cards.add(BoardCard(instanceId = UUID.randomUUID().toString(), kanji = quad.pairB, quadId = quad.id))
        }

        return cards.shuffled()
    }

    private fun parseQuadsFromJson(jsonString: String): List<KanjiQuad> {
        val list = mutableListOf<KanjiQuad>()
        val root = JSONObject(jsonString)
        val quadsArray = root.optJSONArray("quads") ?: return list

        for (i in 0 until quadsArray.length()) {
            val qObj = quadsArray.getJSONObject(i)
            val qId = qObj.getString("id")
            val category = qObj.optString("category", "General")
            val pairAObj = qObj.getJSONObject("pairA")
            val pairBObj = qObj.getJSONObject("pairB")

            val kanjiA = parseKanji(pairAObj)
            val kanjiB = parseKanji(pairBObj)

            list.add(KanjiQuad(id = qId, category = category, pairA = kanjiA, pairB = kanjiB))
        }
        return list
    }

    private fun parseKanji(obj: JSONObject): KanjiData {
        val id = obj.getString("id")
        val char = obj.getString("character")

        val meaningsList = mutableListOf<String>()
        val meaningsArr = obj.optJSONArray("meanings")
        if (meaningsArr != null) {
            for (m in 0 until meaningsArr.length()) {
                meaningsList.add(meaningsArr.getString(m))
            }
        }

        val onList = mutableListOf<String>()
        val onArr = obj.optJSONArray("onReadings")
        if (onArr != null) {
            for (o in 0 until onArr.length()) {
                onList.add(onArr.getString(o))
            }
        }

        val kunList = mutableListOf<String>()
        val kunArr = obj.optJSONArray("kunReadings")
        if (kunArr != null) {
            for (k in 0 until kunArr.length()) {
                kunList.add(kunArr.getString(k))
            }
        }

        val strokeCount = obj.optInt("strokeCount", 1)
        val tip = obj.optString("distinctionTip", "")
        val wiktionary = obj.optString("wiktionaryUrl", "https://en.wiktionary.org/wiki/$char")

        return KanjiData(
            id = id,
            character = char,
            meanings = meaningsList,
            onReadings = onList,
            kunReadings = kunList,
            strokeCount = strokeCount,
            distinctionTip = tip,
            wiktionaryUrl = wiktionary
        )
    }
}
