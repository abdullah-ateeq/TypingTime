package com.example.typingtime.model

enum class SpeedUnit(val displayName: String, val shortName: String, val minSpeed: Int, val maxSpeed: Int, val defaultSpeed: Int) {
    WPM("Words Per Minute", "WPM", 5, 300, 45),
    CPM("Characters Per Minute", "CPM", 20, 2000, 225)
}

enum class HyphenMode(val label: String) {
    KEEP_AS_ONE("Keep as one word"),
    SPLIT("Split into words")
}

enum class ApostropheMode(val label: String) {
    KEEP_AS_ONE("Keep as one word"),
    SPLIT("Split into words")
}

enum class CharacterCountUnit(val label: String) {
    GRAPHEME("Grapheme Characters"),
    CODE_POINTS("Unicode Code Points"),
    UTF16("UTF-16 Code Units")
}

enum class KeystrokeMode(val label: String) {
    CHARACTER_BASED("Character-based"),
    ADVANCED_KEY_MODEL("Advanced keypress model")
}
