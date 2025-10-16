package com.example.labexam3


object MoodScores {


    fun fromIndex(index: Int): Int = when (index) {
        0 -> 4   // 😀 very happy
        1 -> 3   // 🙂 happy
        2 -> 2   // 😐 neutral
        3 -> 1   // 😟 sad
        else -> 0 // 😡 angry (or any out-of-range)
    }


    fun fromEmoji(emoji: String): Int = when (emoji) {
        "😀" -> 4
        "🙂" -> 3
        "😐" -> 2
        "😟" -> 1
        "😡" -> 0
        else -> 2 // unknown → neutral
    }
}
