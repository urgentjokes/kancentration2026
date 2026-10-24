package com.example.kancentration.data.model

data class KanjiQuad(
    val id: String,
    val category: String,
    val pairA: KanjiData,
    val pairB: KanjiData
)
