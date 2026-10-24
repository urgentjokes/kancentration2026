package com.example.kancentration.util

object KanaConverter {

    private val DIGRAPH_MAP = mapOf(
        // Hiragana digraphs
        "きゃ" to "kya", "きゅ" to "kyu", "きょ" to "kyo",
        "しゃ" to "sha", "しゅ" to "shu", "しょ" to "sho",
        "ちゃ" to "cha", "ちゅ" to "chu", "ちょ" to "cho",
        "にゃ" to "nya", "にゅ" to "nyu", "にょ" to "nyo",
        "ひゃ" to "hya", "ひゅ" to "hyu", "ひょ" to "hyo",
        "みゃ" to "mya", "みゅ" to "myu", "みょ" to "myo",
        "りゃ" to "rya", "りゅ" to "ryu", "りょ" to "ryo",
        "ぎゃ" to "gya", "ぎゅ" to "gyu", "ぎょ" to "gyo",
        "じゃ" to "ja", "じゅ" to "ju", "じょ" to "jo",
        "びゃ" to "bya", "びゅ" to "byu", "びょ" to "byo",
        "ぴゃ" to "pya", "ぴゅ" to "pyu", "ぴょ" to "pyo",

        // Katakana digraphs
        "キャ" to "kya", "キュ" to "kyu", "キョ" to "kyo",
        "シャ" to "sha", "シュ" to "shu", "ショ" to "sho",
        "チャ" to "cha", "チュ" to "chu", "チョ" to "cho",
        "ニャ" to "nya", "ニュ" to "nyu", "ニョ" to "nyo",
        "ヒャ" to "hya", "ヒュ" to "hyu", "ヒョ" to "hyo",
        "ミャ" to "mya", "ミュ" to "myu", "ミョ" to "myo",
        "リャ" to "rya", "リュ" to "ryu", "リョ" to "ryo",
        "ギャ" to "gya", "ギュ" to "gyu", "ギョ" to "gyo",
        "ジャ" to "ja", "ジュ" to "ju", "ジョ" to "jo",
        "ビャ" to "bya", "ビュ" to "byu", "ビョ" to "byo",
        "ピャ" to "pya", "ピュ" to "pyu", "ピョ" to "pyo",
        "ウィ" to "wi", "ウェ" to "we", "ウォ" to "wo",
        "ティ" to "ti", "ディ" to "di", "トゥ" to "tu", "ドゥ" to "du",
        "チェ" to "che", "シェ" to "she", "ジェ" to "je",
        "ファ" to "fa", "フィ" to "fi", "フェ" to "fe", "フォ" to "fo"
    )

    private val MONOGRAPH_MAP = mapOf(
        // Hiragana
        "あ" to "a", "い" to "i", "う" to "u", "え" to "e", "お" to "o",
        "か" to "ka", "き" to "ki", "く" to "ku", "け" to "ke", "こ" to "ko",
        "さ" to "sa", "し" to "shi", "す" to "su", "せ" to "se", "そ" to "so",
        "た" to "ta", "ち" to "chi", "つ" to "tsu", "て" to "te", "と" to "to",
        "な" to "na", "に" to "ni", "ぬ" to "nu", "ね" to "ne", "の" to "no",
        "は" to "ha", "ひ" to "hi", "ふ" to "fu", "へ" to "he", "ほ" to "ho",
        "ま" to "ma", "み" to "mi", "む" to "mu", "め" to "me", "も" to "mo",
        "や" to "ya", "ゆ" to "yu", "よ" to "yo",
        "ら" to "ra", "り" to "ri", "る" to "ru", "れ" to "re", "ろ" to "ro",
        "わ" to "wa", "ゐ" to "wi", "ゑ" to "we", "を" to "o",
        "ん" to "n",
        "が" to "ga", "ぎ" to "gi", "ぐ" to "gu", "げ" to "ge", "ご" to "go",
        "ざ" to "za", "じ" to "ji", "ず" to "zu", "ぜ" to "ze", "ぞ" to "zo",
        "だ" to "da", "ぢ" to "ji", "づ" to "zu", "で" to "de", "ど" to "do",
        "ば" to "ba", "び" to "bi", "ぶ" to "bu", "べ" to "be", "ぼ" to "bo",
        "ぱ" to "pa", "ぴ" to "pi", "ぷ" to "pu", "ぺ" to "pe", "ぽ" to "po",

        // Katakana
        "ア" to "a", "イ" to "i", "ウ" to "u", "エ" to "e", "オ" to "o",
        "カ" to "ka", "キ" to "ki", "ク" to "ku", "ケ" to "ke", "コ" to "ko",
        "サ" to "sa", "シ" to "shi", "ス" to "su", "セ" to "se", "ソ" to "so",
        "タ" to "ta", "チ" to "chi", "ツ" to "tsu", "テ" to "te", "ト" to "to",
        "ナ" to "na", "ニ" to "ni", "ヌ" to "nu", "ネ" to "ne", "ノ" to "no",
        "ハ" to "ha", "ヒ" to "hi", "フ" to "fu", "ヘ" to "he", "ホ" to "ho",
        "マ" to "ma", "ミ" to "mi", "ム" to "mu", "メ" to "me", "モ" to "mo",
        "ヤ" to "ya", "ユ" to "yu", "ヨ" to "yo",
        "ラ" to "ra", "リ" to "ri", "ル" to "ru", "レ" to "re", "ロ" to "ro",
        "ワ" to "wa", "ヰ" to "wi", "ヱ" to "we", "ヲ" to "o",
        "ン" to "n",
        "ガ" to "ga", "ギ" to "gi", "グ" to "gu", "ゲ" to "ge", "ゴ" to "go",
        "ザ" to "za", "ジ" to "ji", "ズ" to "zu", "ゼ" to "ze", "ゾ" to "zo",
        "ダ" to "da", "ヂ" to "ji", "ヅ" to "zu", "デ" to "de", "ド" to "do",
        "バ" to "ba", "ビ" to "bi", "ブ" to "bu", "ベ" to "be", "ボ" to "bo",
        "パ" to "pa", "ピ" to "pi", "プ" to "pu", "ペ" to "pe", "ポ" to "po",
        "ヴ" to "vu"
    )

    fun toRomaji(input: String): String {
        if (input.isEmpty()) return ""
        val sb = StringBuilder()
        var i = 0
        var sokuon = false

        while (i < input.length) {
            // Check for sokuon (small tsu)
            val char = input[i]
            if (char == 'っ' || char == 'ッ') {
                sokuon = true
                i++
                continue
            }

            // Check for katakana long vowel mark ー
            if (char == 'ー') {
                // Lengthen the previous vowel
                val lastChar = sb.lastOrNull()
                if (lastChar in listOf('a', 'i', 'u', 'e', 'o')) {
                    sb.append(lastChar)
                }
                i++
                continue
            }

            // Check digraphs (2 chars)
            if (i + 1 < input.length) {
                val pair = input.substring(i, i + 2)
                val digraphRomaji = DIGRAPH_MAP[pair]
                if (digraphRomaji != null) {
                    if (sokuon) {
                        val firstConsonant = if (digraphRomaji.startsWith("ch")) 't' else digraphRomaji[0]
                        sb.append(firstConsonant)
                        sokuon = false
                    }
                    sb.append(digraphRomaji)
                    i += 2
                    continue
                }
            }

            // Check single kana
            val single = input.substring(i, i + 1)
            val monoRomaji = MONOGRAPH_MAP[single]
            if (monoRomaji != null) {
                if (sokuon) {
                    val firstConsonant = if (monoRomaji.startsWith("ch")) 't' else monoRomaji[0]
                    sb.append(firstConsonant)
                    sokuon = false
                }
                sb.append(monoRomaji)
                i++
                continue
            }

            // Non-kana characters (dots, hyphens, alphanumeric, spaces, etc.)
            if (sokuon) {
                sb.append("t")
                sokuon = false
            }
            sb.append(char)
            i++
        }

        if (sokuon) {
            sb.append("t")
        }

        // Apply macrons / circumflexes for long "u" and "o" sounds (e.g. yuu -> yû, ou -> ô)
        return sb.toString()
            .replace("uu", "û")
            .replace("ou", "ô")
            .replace("oo", "ô")
    }
}
