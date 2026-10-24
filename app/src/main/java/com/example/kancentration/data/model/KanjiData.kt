package com.example.kancentration.data.model

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class KanjiData(
    val id: String,
    val character: String,
    val meanings: List<String>,
    val onReadings: List<String>,
    val kunReadings: List<String>,
    val strokeCount: Int,
    val distinctionTip: String,
    val wiktionaryUrl: String = "https://en.wiktionary.org/wiki/${URLEncoder.encode(character, StandardCharsets.UTF_8.toString())}"
)
