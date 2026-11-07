package com.example.kancentration.data.repository

import android.content.Context
import com.example.kancentration.data.model.BoardCard
import com.example.kancentration.data.model.BoardDimension
import com.example.kancentration.data.model.KanjiData
import com.example.kancentration.data.model.KanjiQuad
import java.io.File
import java.util.UUID
import kotlin.random.Random

data class KanjiPackInfo(
    val id: String,
    val title: String,
    val description: String,
    val quadCount: Int
)

interface KanjiRepository {
    fun getAllAvailableQuads(): List<KanjiQuad>
    fun generateDeck(dimension: BoardDimension, random: Random = Random.Default): List<BoardCard>
    fun loadInstalledDlcPacks()
    fun setActivePack(packId: String) {}
    fun getAvailablePacks(): List<KanjiPackInfo> = listOf(
        KanjiPackInfo("base_pack", "Base Confusables", "Foundational Jōyō pairs (20 quads, 40 kanji)", 20),
        KanjiPackInfo("grade_1", "1st-Grade Kanji", "Elementary confusables & Kana traps (18 quads, 36 characters)", 18),
        KanjiPackInfo("grade_2", "2nd-Grade Kanji", "Elementary confusables & subtle stroke shifts (18 quads, 36 characters)", 18),
        KanjiPackInfo("grade_3", "3rd-Grade Kanji", "Intermediate radical substitutions & nuance traps (18 quads, 36 characters)", 18),
        KanjiPackInfo("grade_4", "4th-Grade Kanji", "Intermediate stroke length & component lookalikes (18 quads, 36 characters)", 18),
        KanjiPackInfo("grade_5", "5th-Grade Kanji", "Advanced radical variations & prefix traps (18 quads, 36 characters)", 18),
        KanjiPackInfo("grade_6", "6th-Grade Kanji", "Advanced nuance distinctions & structure lookalikes (18 quads, 36 characters)", 18)
    )

    companion object {
        operator fun invoke(context: Context): KanjiRepository = AssetAndFileKanjiRepository(context)
    }
}

/**
 * Hardened Kanji repository that defensively reads base asset packs and installed DLC packs.
 * Intercepts malformed payloads, missing fields, or duplicate Kanji strings across bundles.
 * Falls back gracefully to DEFAULT_CORE_QUADS if asset pack structure breaks.
 */
open class AssetAndFileKanjiRepository(
    private val basePackJsonProvider: () -> String = { "" },
    private val dlcPacksJsonProvider: () -> List<String> = { emptyList() },
    private val packJsonProvider: (String) -> String = { packId ->
        if (packId == "base_pack") basePackJsonProvider() else ""
    }
) : KanjiRepository {

    constructor(context: Context) : this(
        basePackJsonProvider = {
            try {
                context.assets.open("kanji_base_pack.json").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                ""
            }
        },
        dlcPacksJsonProvider = {
            try {
                val dlcDir = File(context.filesDir, "dlc_packs")
                if (dlcDir.exists() && dlcDir.isDirectory) {
                    dlcDir.listFiles { file -> file.extension == "json" && file.name != "catalog.json" }
                        ?.mapNotNull { file ->
                            try {
                                file.readText()
                            } catch (e: Exception) {
                                null
                            }
                        } ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        },
        packJsonProvider = { packId ->
            try {
                val fileName = when (packId) {
                    "grade_1" -> "grade_1.json"
                    "grade_2" -> "grade_2.json"
                    "grade_3" -> "grade_3.json"
                    "grade_4" -> "grade_4.json"
                    "grade_5" -> "grade_5.json"
                    "grade_6" -> "grade_6.json"
                    else -> "kanji_base_pack.json"
                }
                context.assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                try {
                    val file = File(File(context.filesDir, "dlc_packs"), "$packId.json")
                    if (file.exists()) file.readText() else ""
                } catch (e2: Exception) {
                    ""
                }
            }
        }
    )

    private val baseQuads = mutableListOf<KanjiQuad>()
    private val dlcQuads = mutableListOf<KanjiQuad>()
    private var activePackId: String = "base_pack"

    init {
        loadActivePack()
        loadInstalledDlcPacks()
    }

    override fun setActivePack(packId: String) {
        if (activePackId != packId) {
            activePackId = packId
            loadActivePack()
        }
    }

    private fun loadActivePack() {
        try {
            val json = if (activePackId == "base_pack") {
                val direct = basePackJsonProvider()
                if (direct.isNotBlank()) direct else packJsonProvider("base_pack")
            } else {
                val direct = packJsonProvider(activePackId)
                if (direct.isNotBlank()) direct else basePackJsonProvider()
            }
            val parsed = KanjiQuadParser.parseQuads(json)
            baseQuads.clear()
            if (parsed.size >= BoardDimension.EIGHT_BY_EIGHT.requiredQuads) {
                baseQuads.addAll(parsed)
            } else if (parsed.isNotEmpty()) {
                // Complement with default core quads if parsed quads are fewer than 16
                baseQuads.addAll(parsed)
                val existingChars = baseQuads.flatMap { listOf(it.pairA.character, it.pairB.character) }.toSet()
                for (fallback in DEFAULT_CORE_QUADS) {
                    if (fallback.pairA.character !in existingChars && fallback.pairB.character !in existingChars) {
                        baseQuads.add(fallback)
                        if (baseQuads.size >= DEFAULT_CORE_QUADS.size) break
                    }
                }
            } else {
                // If base asset payload is malformed, missing, or empty, fallback gracefully to default core quads
                baseQuads.addAll(DEFAULT_CORE_QUADS)
            }
        } catch (e: Exception) {
            baseQuads.clear()
            baseQuads.addAll(DEFAULT_CORE_QUADS)
        }
    }

    override fun loadInstalledDlcPacks() {
        try {
            dlcQuads.clear()
            val packJsons = dlcPacksJsonProvider()
            val baseChars = baseQuads.flatMap { listOf(it.pairA.character, it.pairB.character) }.toSet()
            val seenChars = baseChars.toMutableSet()

            for (json in packJsons) {
                try {
                    val parsed = KanjiQuadParser.parseQuads(json, existingCharacters = seenChars)
                    for (quad in parsed) {
                        dlcQuads.add(quad)
                        seenChars.add(quad.pairA.character)
                        seenChars.add(quad.pairB.character)
                    }
                } catch (e: Exception) {
                    // Skip malformed DLC payload gracefully
                }
            }
        } catch (e: Exception) {
            // Defensive ignore
        }
    }

    override fun getAllAvailableQuads(): List<KanjiQuad> {
        val total = baseQuads + dlcQuads
        return if (total.isNotEmpty()) total else DEFAULT_CORE_QUADS
    }

    /**
     * Generates a randomized deck of cards for the given board dimension.
     * Each quad supplies exactly 2 distinct pairs (4 cards total: 2 of Pair A and 2 of Pair B).
     * Guarantees that all selected quads are pairwise disjoint with zero cross-quad leakage or duplicate characters.
     */
    override fun generateDeck(dimension: BoardDimension, random: Random): List<BoardCard> {
        val requiredQuads = dimension.requiredQuads
        val allQuads = getAllAvailableQuads().shuffled(random)

        val selectedQuads = mutableListOf<KanjiQuad>()
        val seenKanjiCharacters = mutableSetOf<String>()

        for (quad in allQuads) {
            val charA = quad.pairA.character
            val charB = quad.pairB.character
            if (charA !in seenKanjiCharacters && charB !in seenKanjiCharacters && charA != charB) {
                selectedQuads.add(quad)
                seenKanjiCharacters.add(charA)
                seenKanjiCharacters.add(charB)
                if (selectedQuads.size == requiredQuads) {
                    break
                }
            }
        }

        // Defensive fallback: If for any unexpected reason not enough disjoint quads exist in current pool,
        // complement with DEFAULT_CORE_QUADS to strictly avoid runtime crashes.
        if (selectedQuads.size < requiredQuads) {
            for (fallback in DEFAULT_CORE_QUADS) {
                val charA = fallback.pairA.character
                val charB = fallback.pairB.character
                if (charA !in seenKanjiCharacters && charB !in seenKanjiCharacters && charA != charB) {
                    selectedQuads.add(fallback)
                    seenKanjiCharacters.add(charA)
                    seenKanjiCharacters.add(charB)
                    if (selectedQuads.size == requiredQuads) break
                }
            }
        }

        require(selectedQuads.size == requiredQuads) {
            "Insufficient disjoint quads available. Required: $requiredQuads, found disjoint: ${selectedQuads.size}"
        }

        val cards = mutableListOf<BoardCard>()
        for (quad in selectedQuads) {
            // 2 cards of Pair A
            cards.add(
                BoardCard(
                    instanceId = UUID.randomUUID().toString(),
                    kanji = quad.pairA,
                    quadId = quad.id,
                    confusableTwinCharacter = quad.pairB.character
                )
            )
            cards.add(
                BoardCard(
                    instanceId = UUID.randomUUID().toString(),
                    kanji = quad.pairA,
                    quadId = quad.id,
                    confusableTwinCharacter = quad.pairB.character
                )
            )

            // 2 cards of Pair B
            cards.add(
                BoardCard(
                    instanceId = UUID.randomUUID().toString(),
                    kanji = quad.pairB,
                    quadId = quad.id,
                    confusableTwinCharacter = quad.pairA.character
                )
            )
            cards.add(
                BoardCard(
                    instanceId = UUID.randomUUID().toString(),
                    kanji = quad.pairB,
                    quadId = quad.id,
                    confusableTwinCharacter = quad.pairA.character
                )
            )
        }

        return cards.shuffled(random)
    }

    companion object {
        val DEFAULT_CORE_QUADS: List<KanjiQuad> = listOf(
            KanjiQuad(
                id = "quad_01_sun_say",
                category = "Proportions & Framing",
                pairA = KanjiData(
                    id = "kanji_hi_sun",
                    character = "日",
                    meanings = listOf("day", "sun", "Japan"),
                    onReadings = listOf("ニチ", "ジツ"),
                    kunReadings = listOf("ひ", "-び", "-か"),
                    strokeCount = 4,
                    distinctionTip = "Tall, narrow vertical rectangle frame (aspect ratio > 1).",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E6%97%A5"
                ),
                pairB = KanjiData(
                    id = "kanji_iwaku_say",
                    character = "曰",
                    meanings = listOf("say", "speak", "declare"),
                    onReadings = listOf("エツ"),
                    kunReadings = listOf("いわ.く"),
                    strokeCount = 4,
                    distinctionTip = "Wide, flat horizontal rectangle frame (aspect ratio < 1); flatter frame.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E6%9B%B0"
                )
            ),
            KanjiQuad(
                id = "quad_02_hold_special",
                category = "Radical Nuance",
                pairA = KanjiData(
                    id = "kanji_motsu_hold",
                    character = "持",
                    meanings = listOf("hold", "have", "possess"),
                    onReadings = listOf("ジ"),
                    kunReadings = listOf("も.つ", "も.てる"),
                    strokeCount = 9,
                    distinctionTip = "Left radical is 扌 (hand / te-hen).",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E6%8C%81"
                ),
                pairB = KanjiData(
                    id = "kanji_toku_special",
                    character = "特",
                    meanings = listOf("special", "unique"),
                    onReadings = listOf("トク"),
                    kunReadings = emptyList(),
                    strokeCount = 10,
                    distinctionTip = "Left radical is 牛 (cow / ushi-hen).",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E7%89%B9"
                )
            ),
            KanjiQuad(
                id = "quad_03_person_enter",
                category = "Stroke Angle & Overlap",
                pairA = KanjiData(
                    id = "kanji_hito_person",
                    character = "人",
                    meanings = listOf("person", "human", "people"),
                    onReadings = listOf("ジン", "ニン"),
                    kunReadings = listOf("ひと", "-り", "-と"),
                    strokeCount = 2,
                    distinctionTip = "Left stroke is drawn first and stands taller; right stroke leans on it.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E4%BA%BA"
                ),
                pairB = KanjiData(
                    id = "kanji_iru_enter",
                    character = "入",
                    meanings = listOf("enter", "insert"),
                    onReadings = listOf("ニュウ"),
                    kunReadings = listOf("い.る", "はい.る"),
                    strokeCount = 2,
                    distinctionTip = "Right stroke starts higher and sweeps down; left stroke tucks underneath.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%85%A5"
                )
            ),
            KanjiQuad(
                id = "quad_04_earth_scholar",
                category = "Stroke Length Balance",
                pairA = KanjiData(
                    id = "kanji_tsuchi_earth",
                    character = "土",
                    meanings = listOf("earth", "soil", "ground", "Saturday"),
                    onReadings = listOf("ド", "ト"),
                    kunReadings = listOf("つち"),
                    strokeCount = 3,
                    distinctionTip = "Bottom horizontal stroke is noticeably LONGER than the top horizontal stroke.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%9C%9F"
                ),
                pairB = KanjiData(
                    id = "kanji_shi_scholar",
                    character = "士",
                    meanings = listOf("scholar", "samurai", "gentleman"),
                    onReadings = listOf("シ"),
                    kunReadings = listOf("さむらい"),
                    strokeCount = 3,
                    distinctionTip = "Top horizontal stroke is noticeably LONGER than the bottom horizontal stroke.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%A3%AB"
                )
            ),
            KanjiQuad(
                id = "quad_05_end_notyet",
                category = "Horizontal Stroke Dominance",
                pairA = KanjiData(
                    id = "kanji_sue_end",
                    character = "末",
                    meanings = listOf("end", "tip", "extremity", "future"),
                    onReadings = listOf("マツ", "バツ"),
                    kunReadings = listOf("すえ"),
                    strokeCount = 5,
                    distinctionTip = "TOP horizontal line is noticeably longer than the lower horizontal line.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E6%9C%AB"
                ),
                pairB = KanjiData(
                    id = "kanji_hitsuji_notyet",
                    character = "未",
                    meanings = listOf("not yet", "un-", "even now"),
                    onReadings = listOf("ミ", "ビ"),
                    kunReadings = listOf("いま.だ", "ま.だ"),
                    strokeCount = 5,
                    distinctionTip = "BOTTOM horizontal line is noticeably longer than the top horizontal line.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E6%9C%AA"
                )
            ),
            KanjiQuad(
                id = "quad_06_right_stone",
                category = "Top Stroke Crossing",
                pairA = KanjiData(
                    id = "kanji_migi_right",
                    character = "右",
                    meanings = listOf("right"),
                    onReadings = listOf("ウ", "ユウ"),
                    kunReadings = listOf("みぎ"),
                    strokeCount = 5,
                    distinctionTip = "Slanted stroke (ノ) is drawn first; the horizontal stroke crosses it, extending noticeably to the left.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%8F%B3"
                ),
                pairB = KanjiData(
                    id = "kanji_ishi_stone",
                    character = "石",
                    meanings = listOf("stone"),
                    onReadings = listOf("セキ", "コク"),
                    kunReadings = listOf("いし"),
                    strokeCount = 5,
                    distinctionTip = "Horizontal stroke is drawn first, forming a cliff (厂) over 口 with no horizontal protrusion to the left.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E7%9F%B3"
                )
            ),
            KanjiQuad(
                id = "quad_07_thousand_dry",
                category = "Top Stroke Angle",
                pairA = KanjiData(
                    id = "kanji_sen_thousand",
                    character = "千",
                    meanings = listOf("thousand"),
                    onReadings = listOf("セン"),
                    kunReadings = listOf("ち"),
                    strokeCount = 3,
                    distinctionTip = "Top stroke is a slanted left-falling sweep (harai / ノ).",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%8D%83"
                ),
                pairB = KanjiData(
                    id = "kanji_kan_dry",
                    character = "干",
                    meanings = listOf("dry", "parch", "ebb"),
                    onReadings = listOf("カン"),
                    kunReadings = listOf("ほ.す", "ひ.る"),
                    strokeCount = 3,
                    distinctionTip = "Top stroke is a straight horizontal line (ichi / 一).",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%B9%B2"
                )
            ),
            KanjiQuad(
                id = "quad_08_heaven_arrow",
                category = "Vertical Pierce Component",
                pairA = KanjiData(
                    id = "kanji_ten_heaven",
                    character = "天",
                    meanings = listOf("heavens", "sky", "imperial"),
                    onReadings = listOf("テン"),
                    kunReadings = listOf("あまつ", "あめ", "あま"),
                    strokeCount = 4,
                    distinctionTip = "Vertical stroke does not extend above the upper horizontal bar.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%A4%A9"
                ),
                pairB = KanjiData(
                    id = "kanji_ya_arrow",
                    character = "矢",
                    meanings = listOf("arrow", "dart"),
                    onReadings = listOf("シ"),
                    kunReadings = listOf("や"),
                    strokeCount = 5,
                    distinctionTip = "Top slanted stroke pierces through the upper horizontal bar.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E7%9F%A2"
                )
            ),
            KanjiQuad(
                id = "quad_09_buy_member",
                category = "Radical Complexity",
                pairA = KanjiData(
                    id = "kanji_kau_buy",
                    character = "買",
                    meanings = listOf("buy"),
                    onReadings = listOf("バイ"),
                    kunReadings = listOf("か.う"),
                    strokeCount = 12,
                    distinctionTip = "Top component is 罒 (net / amigashira) over 貝.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E8%B2%B7"
                ),
                pairB = KanjiData(
                    id = "kanji_in_member",
                    character = "員",
                    meanings = listOf("employee", "member", "number"),
                    onReadings = listOf("イン"),
                    kunReadings = emptyList(),
                    strokeCount = 10,
                    distinctionTip = "Top component is a single 口 (mouth) over 貝.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%93%A1"
                )
            ),
            KanjiQuad(
                id = "quad_10_sword_blade",
                category = "Single Accent Dot",
                pairA = KanjiData(
                    id = "kanji_katana_sword",
                    character = "刀",
                    meanings = listOf("sword", "saber", "knife"),
                    onReadings = listOf("トウ"),
                    kunReadings = listOf("かたな", "そり"),
                    strokeCount = 2,
                    distinctionTip = "Has no internal tick or dot.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%88%80"
                ),
                pairB = KanjiData(
                    id = "kanji_ha_blade",
                    character = "刃",
                    meanings = listOf("blade", "sword edge"),
                    onReadings = listOf("ジン", "ニン"),
                    kunReadings = listOf("は", "やいば"),
                    strokeCount = 3,
                    distinctionTip = "Contains a diagonal tick dot on the left side of the blade.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%88%83"
                )
            ),
            KanjiQuad(
                id = "quad_11_wait_samurai",
                category = "Left Radical Match",
                pairA = KanjiData(
                    id = "kanji_matsu_wait",
                    character = "待",
                    meanings = listOf("wait", "depend on"),
                    onReadings = listOf("タイ"),
                    kunReadings = listOf("ま.つ"),
                    strokeCount = 9,
                    distinctionTip = "Left radical is 彳 (step / gyou-nin-ben, 2 strokes).",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%BE%85"
                ),
                pairB = KanjiData(
                    id = "kanji_samurai_retainer",
                    character = "侍",
                    meanings = listOf("waiter", "samurai", "attend upon"),
                    onReadings = listOf("ジ", "シ"),
                    kunReadings = listOf("さむらい"),
                    strokeCount = 8,
                    distinctionTip = "Left radical is 亻 (person / nin-ben, 1 stroke).",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E4%BE%8D"
                )
            ),
            KanjiQuad(
                id = "quad_12_dog_thick",
                category = "Dot Placement",
                pairA = KanjiData(
                    id = "kanji_inu_dog",
                    character = "犬",
                    meanings = listOf("dog"),
                    onReadings = listOf("ケン"),
                    kunReadings = listOf("いぬ"),
                    strokeCount = 4,
                    distinctionTip = "The accent dot is at the upper-right corner above 大.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E7%8A%AC"
                ),
                pairB = KanjiData(
                    id = "kanji_futo_thick",
                    character = "太",
                    meanings = listOf("plump", "thick", "big around"),
                    onReadings = listOf("タイ", "タ"),
                    kunReadings = listOf("ふと.い", "ふと.る"),
                    strokeCount = 4,
                    distinctionTip = "The accent dot is at the center bottom between the legs of 大.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%A4%AA"
                )
            ),
            KanjiQuad(
                id = "quad_13_shell_see",
                category = "Lower Leg Shape",
                pairA = KanjiData(
                    id = "kanji_kai_shell",
                    character = "貝",
                    meanings = listOf("shellfish"),
                    onReadings = listOf("バイ"),
                    kunReadings = listOf("かい"),
                    strokeCount = 7,
                    distinctionTip = "Bottom legs are two separated small diagonal tick strokes (八).",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E8%B2%9D"
                ),
                pairB = KanjiData(
                    id = "kanji_miru_see",
                    character = "見",
                    meanings = listOf("see", "hopes", "chances", "idea"),
                    onReadings = listOf("ケン"),
                    kunReadings = listOf("み.る", "み.える", "み.せる"),
                    strokeCount = 7,
                    distinctionTip = "Bottom legs form human legs (ひとあし / 儿), curving to the right.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E8%A6%8B"
                )
            ),
            KanjiQuad(
                id = "quad_14_white_hundred",
                category = "Upper Horizontal Bar",
                pairA = KanjiData(
                    id = "kanji_shiro_white",
                    character = "白",
                    meanings = listOf("white"),
                    onReadings = listOf("ハク", "ビャク"),
                    kunReadings = listOf("しろ", "しら-", "しろ.い"),
                    strokeCount = 5,
                    distinctionTip = "Top stroke is a single diagonal downward-left tick (ノ).",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E7%99%BD"
                ),
                pairB = KanjiData(
                    id = "kanji_hyaku_hundred",
                    character = "百",
                    meanings = listOf("hundred"),
                    onReadings = listOf("ヒャク", "ビャク"),
                    kunReadings = listOf("もも"),
                    strokeCount = 6,
                    distinctionTip = "Features an extra horizontal roof stroke (一) across the top.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E7%99%BE"
                )
            ),
            KanjiQuad(
                id = "quad_15_cow_noon",
                category = "Vertical Stroke Pierce",
                pairA = KanjiData(
                    id = "kanji_ushi_cow",
                    character = "牛",
                    meanings = listOf("cow"),
                    onReadings = listOf("ギュウ"),
                    kunReadings = listOf("うし"),
                    strokeCount = 4,
                    distinctionTip = "Central vertical stroke pierces through the upper horizontal stroke.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E7%89%9B"
                ),
                pairB = KanjiData(
                    id = "kanji_hiru_noon",
                    character = "午",
                    meanings = listOf("noon", "sign of the horse"),
                    onReadings = listOf("ゴ"),
                    kunReadings = listOf("うま"),
                    strokeCount = 4,
                    distinctionTip = "Central vertical stroke does NOT pierce above the top horizontal stroke.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%8D%88"
                )
            ),
            KanjiQuad(
                id = "quad_16_bird_island",
                category = "Bottom Radical Accent",
                pairA = KanjiData(
                    id = "kanji_tori_bird",
                    character = "鳥",
                    meanings = listOf("bird", "chicken"),
                    onReadings = listOf("チョウ"),
                    kunReadings = listOf("とり"),
                    strokeCount = 11,
                    distinctionTip = "Bottom component has four distinct tail-feather dots (灬 / renga).",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E9%B3%A5"
                ),
                pairB = KanjiData(
                    id = "kanji_shima_island",
                    character = "島",
                    meanings = listOf("island"),
                    onReadings = listOf("トウ"),
                    kunReadings = listOf("しま"),
                    strokeCount = 10,
                    distinctionTip = "Bottom component is 山 (mountain) instead of the four dots.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%B3%B6"
                )
            ),
            KanjiQuad(
                id = "quad_17_interval_question",
                category = "Gate Interior Component",
                pairA = KanjiData(
                    id = "kanji_aida_interval",
                    character = "間",
                    meanings = listOf("interval", "space", "between", "room"),
                    onReadings = listOf("カン", "ケン"),
                    kunReadings = listOf("あいだ", "ま", "あい"),
                    strokeCount = 12,
                    distinctionTip = "Inside the 門 (gate) radical is 日 (sun / day).",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E9%96%93"
                ),
                pairB = KanjiData(
                    id = "kanji_tou_question",
                    character = "問",
                    meanings = listOf("question", "ask", "problem"),
                    onReadings = listOf("モン"),
                    kunReadings = listOf("と.う", "と.い", "とん"),
                    strokeCount = 11,
                    distinctionTip = "Inside the 門 (gate) radical is 口 (mouth).",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%95%8F"
                )
            ),
            KanjiQuad(
                id = "quad_18_say_reason",
                category = "Vertical Pierce Direction",
                pairA = KanjiData(
                    id = "kanji_mousu_say",
                    character = "申",
                    meanings = listOf("say", "report", "monkey"),
                    onReadings = listOf("シン"),
                    kunReadings = listOf("もう.す", "さる"),
                    strokeCount = 5,
                    distinctionTip = "Central vertical stroke extends out of BOTH the top and bottom of the box.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E7%94%B3"
                ),
                pairB = KanjiData(
                    id = "kanji_yoshi_reason",
                    character = "由",
                    meanings = listOf("wherefore", "reason", "cause"),
                    onReadings = listOf("ユ", "ユウ"),
                    kunReadings = listOf("よし", "よ.る"),
                    strokeCount = 5,
                    distinctionTip = "Central vertical stroke extends ONLY out of the TOP of the box.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E7%94%B1"
                )
            ),
            KanjiQuad(
                id = "quad_19_tree_book",
                category = "Root Stroke Distinction",
                pairA = KanjiData(
                    id = "kanji_ki_tree",
                    character = "木",
                    meanings = listOf("tree", "wood"),
                    onReadings = listOf("モク", "ボク"),
                    kunReadings = listOf("き", "こ-"),
                    strokeCount = 4,
                    distinctionTip = "Main vertical trunk has no lower horizontal mark (4 strokes).",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E6%9C%A8"
                ),
                pairB = KanjiData(
                    id = "kanji_hon_book",
                    character = "本",
                    meanings = listOf("book", "origin", "main"),
                    onReadings = listOf("ホン"),
                    kunReadings = listOf("もと"),
                    strokeCount = 5,
                    distinctionTip = "Features an extra short horizontal stroke crossing the lower trunk marking the root.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E6%9C%AC"
                )
            ),
            KanjiQuad(
                id = "quad_20_great_differ",
                category = "Person vs Movement Radical",
                pairA = KanjiData(
                    id = "kanji_erai_great",
                    character = "偉",
                    meanings = listOf("admirable", "great", "distinguished"),
                    onReadings = listOf("イ"),
                    kunReadings = listOf("えら.い"),
                    strokeCount = 12,
                    distinctionTip = "Left radical is 亻 (person / nin-ben) alongside 韋.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E5%81%89"
                ),
                pairB = KanjiData(
                    id = "kanji_chigau_differ",
                    character = "違",
                    meanings = listOf("differ", "mistake", "vary"),
                    onReadings = listOf("イ"),
                    kunReadings = listOf("ちが.う", "ちが.い"),
                    strokeCount = 13,
                    distinctionTip = "Left and bottom radical is 辶 (road/movement / shin-nyou) wrapping around 韋.",
                    wiktionaryUrl = "https://en.wiktionary.org/wiki/%E9%81%95"
                )
            )
        )
    }
}

typealias DefaultKanjiRepository = AssetAndFileKanjiRepository

class FakeKanjiRepository(
    private val quads: List<KanjiQuad> = emptyList()
) : KanjiRepository {
    override fun getAllAvailableQuads(): List<KanjiQuad> = quads
    override fun loadInstalledDlcPacks() {}
    override fun generateDeck(dimension: BoardDimension, random: Random): List<BoardCard> {
        val requiredQuads = dimension.requiredQuads
        val selectedQuads = quads.take(requiredQuads)
        val cards = mutableListOf<BoardCard>()
        for (quad in selectedQuads) {
            cards.add(BoardCard(instanceId = UUID.randomUUID().toString(), kanji = quad.pairA, quadId = quad.id, confusableTwinCharacter = quad.pairB.character))
            cards.add(BoardCard(instanceId = UUID.randomUUID().toString(), kanji = quad.pairA, quadId = quad.id, confusableTwinCharacter = quad.pairB.character))
            cards.add(BoardCard(instanceId = UUID.randomUUID().toString(), kanji = quad.pairB, quadId = quad.id, confusableTwinCharacter = quad.pairA.character))
            cards.add(BoardCard(instanceId = UUID.randomUUID().toString(), kanji = quad.pairB, quadId = quad.id, confusableTwinCharacter = quad.pairA.character))
        }
        return cards.shuffled(random)
    }
}

/**
 * Defensive parser for KanjiQuad JSON datasets.
 * Intercepts malformed payloads, missing required fields, and rejects duplicate characters.
 */
internal object KanjiQuadParser {

    fun parseQuads(
        jsonString: String,
        existingCharacters: Set<String> = emptySet()
    ): List<KanjiQuad> {
        if (jsonString.isBlank()) return emptyList()

        return try {
            val root = SimpleJsonParser.parse(jsonString) as? SimpleJsonParser.Element.Obj ?: return emptyList()
            val quadsArr = root.getArray("quads")
            val list = mutableListOf<KanjiQuad>()
            val seenCharactersInBatch = mutableSetOf<String>()
            val seenQuadIds = mutableSetOf<String>()

            for (item in quadsArr) {
                val qObj = item as? SimpleJsonParser.Element.Obj ?: continue
                val qId = qObj.getString("id").trim()
                if (qId.isEmpty() || qId in seenQuadIds) continue

                val category = qObj.getString("category", "General").trim().ifBlank { "General" }
                val pairAObj = qObj.getObject("pairA") ?: continue
                val pairBObj = qObj.getObject("pairB") ?: continue

                val kanjiA = parseKanji(pairAObj) ?: continue
                val kanjiB = parseKanji(pairBObj) ?: continue

                // Validation 1: Distinct characters within the quad
                if (kanjiA.character == kanjiB.character) continue
                if (kanjiA.id == kanjiB.id) continue

                // Validation 2: Intercept cross-quad leakage or cross-bundle duplicates
                val charA = kanjiA.character
                val charB = kanjiB.character
                if (charA in existingCharacters || charB in existingCharacters) continue
                if (charA in seenCharactersInBatch || charB in seenCharactersInBatch) continue

                seenQuadIds.add(qId)
                seenCharactersInBatch.add(charA)
                seenCharactersInBatch.add(charB)

                list.add(KanjiQuad(id = qId, category = category, pairA = kanjiA, pairB = kanjiB))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseKanji(obj: SimpleJsonParser.Element.Obj): KanjiData? {
        val id = obj.getString("id").trim()
        val char = obj.getString("character").trim()
        val meanings = obj.getStringList("meanings").map { it.trim() }.filter { it.isNotEmpty() }

        // Require non-empty ID, character, and meanings
        if (id.isEmpty() || char.isEmpty() || meanings.isEmpty()) {
            return null
        }

        val onReadings = obj.getStringList("onReadings").map { it.trim() }.filter { it.isNotEmpty() }
        val kunReadings = obj.getStringList("kunReadings").map { it.trim() }.filter { it.isNotEmpty() }
        val rawStrokeCount = obj.getInt("strokeCount", 1)
        val strokeCount = if (rawStrokeCount > 0) rawStrokeCount else 1
        val tip = obj.getString("distinctionTip", "").trim()
        val rawWiktionary = obj.getString("wiktionaryUrl").trim()
        val wiktionary = if (rawWiktionary.isNotEmpty()) rawWiktionary else "https://en.wiktionary.org/wiki/$char"

        return KanjiData(
            id = id,
            character = char,
            meanings = meanings,
            onReadings = onReadings,
            kunReadings = kunReadings,
            strokeCount = strokeCount,
            distinctionTip = tip,
            wiktionaryUrl = wiktionary
        )
    }
}

internal object SimpleJsonParser {
    sealed class Element {
        data class Obj(val map: Map<String, Element>) : Element() {
            fun getString(key: String, default: String = ""): String = (map[key] as? Str)?.value ?: default
            fun getInt(key: String, default: Int = 0): Int = (map[key] as? Num)?.value?.toInt() ?: default
            fun getArray(key: String): List<Element> = (map[key] as? Arr)?.list ?: emptyList()
            fun getObject(key: String): Obj? = map[key] as? Obj
            fun getStringList(key: String): List<String> = getArray(key).mapNotNull { (it as? Str)?.value }
        }
        data class Arr(val list: List<Element>) : Element()
        data class Str(val value: String) : Element()
        data class Num(val value: Double) : Element()
        data class Bool(val value: Boolean) : Element()
        object NullVal : Element()
    }

    fun parse(json: String): Element {
        var pos = 0
        val len = json.length

        fun skipWhitespace() {
            while (pos < len && json[pos].isWhitespace()) pos++
        }

        fun parseString(): String {
            pos++ // skip leading '"'
            val sb = java.lang.StringBuilder()
            while (pos < len) {
                val c = json[pos++]
                if (c == '"') return sb.toString()
                if (c == '\\' && pos < len) {
                    when (val esc = json[pos++]) {
                        '"', '\\', '/' -> sb.append(esc)
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            try {
                                if (pos + 4 <= len) {
                                    val hex = json.substring(pos, pos + 4)
                                    pos += 4
                                    sb.append(hex.toInt(16).toChar())
                                }
                            } catch (e: Exception) {
                                // Ignore malformed unicode escape
                            }
                        }
                        else -> sb.append(esc)
                    }
                } else {
                    sb.append(c)
                }
            }
            return sb.toString()
        }

        fun parseElement(): Element {
            skipWhitespace()
            if (pos >= len) return Element.NullVal
            return when (val c = json[pos]) {
                '{' -> {
                    pos++ // skip '{'
                    val map = mutableMapOf<String, Element>()
                    skipWhitespace()
                    if (pos < len && json[pos] == '}') {
                        pos++
                        return Element.Obj(map)
                    }
                    while (pos < len) {
                        skipWhitespace()
                        if (pos >= len || json[pos] != '"') {
                            // If unexpected token inside object, skip character to prevent infinite loop
                            if (pos < len && json[pos] != '}') pos++
                            else if (pos < len && json[pos] == '}') { pos++; break }
                            continue
                        }
                        val key = parseString()
                        skipWhitespace()
                        if (pos < len && json[pos] == ':') pos++
                        val value = parseElement()
                        map[key] = value
                        skipWhitespace()
                        if (pos < len && json[pos] == ',') {
                            pos++
                        } else if (pos < len && json[pos] == '}') {
                            pos++
                            break
                        }
                    }
                    Element.Obj(map)
                }
                '[' -> {
                    pos++ // skip '['
                    val list = mutableListOf<Element>()
                    skipWhitespace()
                    if (pos < len && json[pos] == ']') {
                        pos++
                        return Element.Arr(list)
                    }
                    while (pos < len) {
                        skipWhitespace()
                        if (pos < len && json[pos] == ']') {
                            pos++
                            break
                        }
                        val item = parseElement()
                        list.add(item)
                        skipWhitespace()
                        if (pos < len && json[pos] == ',') {
                            pos++
                        } else if (pos < len && json[pos] == ']') {
                            pos++
                            break
                        }
                    }
                    Element.Arr(list)
                }
                '"' -> Element.Str(parseString())
                't', 'f' -> {
                    if (json.startsWith("true", pos)) { pos += 4; Element.Bool(true) }
                    else if (json.startsWith("false", pos)) { pos += 5; Element.Bool(false) }
                    else { pos++; Element.NullVal }
                }
                'n' -> {
                    if (json.startsWith("null", pos)) pos += 4
                    Element.NullVal
                }
                else -> {
                    val start = pos
                    if (c == '-') pos++
                    while (pos < len && (json[pos].isDigit() || json[pos] == '.' || json[pos] == 'e' || json[pos] == 'E' || json[pos] == '+' || json[pos] == '-')) {
                        pos++
                    }
                    if (pos == start) {
                        // Unexpected character: advance to avoid infinite recursion
                        pos++
                        Element.NullVal
                    } else {
                        val numStr = json.substring(start, pos)
                        Element.Num(numStr.toDoubleOrNull() ?: 0.0)
                    }
                }
            }
        }

        return try {
            parseElement()
        } catch (e: Exception) {
            Element.NullVal
        }
    }
}
