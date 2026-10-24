package com.example.kancentration.data.firebase

import android.content.Context
import com.example.kancentration.data.model.DlcDownloadState
import com.example.kancentration.data.model.DlcPackItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class FirebaseStorageDlcManager(private val context: Context) {

    private val dlcDir = File(context.filesDir, "dlc_packs").apply { if (!exists()) mkdirs() }

    private val _catalogPacks = MutableStateFlow<List<DlcPackItem>>(emptyList())
    val catalogPacks: StateFlow<List<DlcPackItem>> = _catalogPacks.asStateFlow()

    private val _downloadState = MutableStateFlow<DlcDownloadState>(DlcDownloadState.Idle)
    val downloadState: StateFlow<DlcDownloadState> = _downloadState.asStateFlow()

    init {
        loadCatalog()
    }

    fun loadCatalog() {
        // Default curated DLC catalog (available on Firebase Storage / offline fallback)
        val defaultPacks = listOf(
            DlcPackItem(
                id = "dlc_radical_rivals",
                title = "Radical Rivals",
                description = "Master subtle radical distinctions (e.g. 扌 vs 牛, 彳 vs 亻, 礻 vs 衤).",
                priceFormatted = "$0.99",
                storagePath = "dlc_packs/radical_rivals/pack.json",
                quadCount = 16,
                isInstalled = isPackInstalled("dlc_radical_rivals")
            ),
            DlcPackItem(
                id = "dlc_jlpt_n1",
                title = "JLPT N1 Traps",
                description = "High-level visual confounders (e.g. 崇 vs 祟, 壺 vs 壷, 璧 vs 癖).",
                priceFormatted = "$1.49",
                storagePath = "dlc_packs/jlpt_n1/pack.json",
                quadCount = 16,
                isInstalled = isPackInstalled("dlc_jlpt_n1")
            )
        )
        _catalogPacks.value = defaultPacks
    }

    fun isPackInstalled(packId: String): Boolean {
        val file = File(dlcDir, "$packId.json")
        return file.exists() && file.length() > 0
    }

    suspend fun downloadPack(pack: DlcPackItem, onComplete: (Boolean) -> Unit) = withContext(Dispatchers.IO) {
        _downloadState.value = DlcDownloadState.Downloading(pack.id, 0.1f)

        try {
            // Simulate Firebase Storage chunked stream or direct fetch
            for (step in 1..5) {
                kotlinx.coroutines.delay(180)
                _downloadState.value = DlcDownloadState.Downloading(pack.id, step * 0.2f)
            }

            // Write sample downloadable expansion pack JSON if not present
            val destinationFile = File(dlcDir, "${pack.id}.json")
            val packPayload = generateSamplePackJson(pack.id, pack.title)
            destinationFile.writeText(packPayload)

            _downloadState.value = DlcDownloadState.Success(pack.id)

            // Update pack installed status in catalog
            _catalogPacks.value = _catalogPacks.value.map {
                if (it.id == pack.id) it.copy(isInstalled = true) else it
            }

            withContext(Dispatchers.Main) {
                onComplete(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _downloadState.value = DlcDownloadState.Error(pack.id, e.message ?: "Download failed")
            withContext(Dispatchers.Main) {
                onComplete(false)
            }
        }
    }

    private fun generateSamplePackJson(packId: String, title: String): String {
        return """
        {
          "packId": "$packId",
          "title": "$title",
          "quads": [
            {
              "id": "${packId}_q1",
              "category": "Radical Clues",
              "pairA": {
                "id": "kanji_shimesu",
                "character": "社",
                "meanings": ["company", "shrine", "society"],
                "onReadings": ["シャ"],
                "kunReadings": ["やしろ"],
                "strokeCount": 7,
                "distinctionTip": "Left radical is 礻 (altar/shinto radical, 4 strokes).",
                "wiktionaryUrl": "https://en.wiktionary.org/wiki/%E7%A4%BE"
              },
              "pairB": {
                "id": "kanji_koromo",
                "character": "初",
                "meanings": ["first", "beginning"],
                "onReadings": ["ショ"],
                "kunReadings": ["はじ.め", "はつ"],
                "strokeCount": 7,
                "distinctionTip": "Left radical is 衤 (clothing radical, 5 strokes with extra tick).",
                "wiktionaryUrl": "https://en.wiktionary.org/wiki/%E5%88%9D"
              }
            }
          ]
        }
        """.trimIndent()
    }
}
