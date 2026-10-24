package com.example.kancentration.data.model

data class BoardCard(
    val instanceId: String,
    val kanji: KanjiData,
    val quadId: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false,
    val matchedByPlayer: Int? = null // 1 or 2
)
