package com.example.typingtime.model

data class TypingTimeEstimate(
    val speed: Int,
    val speedUnit: SpeedUnit,
    val totalSeconds: Long,
    val formattedTime: String
)

data class TextStatistics(
    val wordCount: Int,
    val characterCount: Int,
    val characterCountWithoutSpaces: Int,
    val keystrokeCount: Int,
    val sentenceCount: Int,
    val paragraphCount: Int,
    val lineCount: Int,
    val averageWordLength: Double?,
    val averageWordsPerSentence: Double?,
    val graphemeCount: Int = characterCount,
    val codePointCount: Int = characterCount,
    val utf16Count: Int = characterCount
)

data class ParagraphBreakdown(
    val index: Int,
    val text: String,
    val wordCount: Int,
    val characterCount: Int,
    val estimatedSeconds: Long,
    val formattedTime: String
)

data class SpeedComparison(
    val speed: Int,
    val speedUnit: SpeedUnit,
    val description: String,
    val totalSeconds: Long,
    val formattedTime: String,
    val isUserSpeed: Boolean = false
)

data class TypingSession(
    val startedAt: Long,
    val activeSeconds: Long,
    val wordCount: Int,
    val characterCount: Int,
    val averageWpm: Double
)

data class SessionFinishedSummary(
    val activeSeconds: Long,
    val wordCount: Int,
    val characterCount: Int,
    val averageWpm: Double,
    val estimatedSecondsForText: Long,
    val formattedActiveTime: String,
    val formattedEstimatedTime: String,
    val differenceSeconds: Long,
    val formattedDifference: String
)

data class CalibrationResult(
    val id: String,
    val wpm: Int,
    val accuracy: Double,
    val errors: Int,
    val totalCharacters: Int,
    val activeSeconds: Long,
    val formattedTime: String,
    val timestamp: Long
)

data class TypingTimeHistory(
    val id: String,
    val text: String,
    val analyzedAt: Long,
    val speedUnit: SpeedUnit,
    val typingSpeed: Int,
    val countNumbers: Boolean,
    val hyphenMode: String,
    val apostropheMode: String,
    val keystrokeMode: String,
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val estimatedSeconds: Long = 0,
    val formattedTime: String = ""
)

data class AppSettings(
    val theme: String = "System", // System, Light, Dark
    val dynamicColor: Boolean = true,
    val defaultWpm: Int = 45,
    val presetBeginner: Int = 20,
    val presetCasual: Int = 30,
    val presetAverage: Int = 45,
    val presetFast: Int = 60,
    val presetExpert: Int = 80,
    val estimationMethod: SpeedUnit = SpeedUnit.WPM,
    val defaultCpm: Int = 225,
    val timeDisplayDetailed: Boolean = true,
    val countNumbers: Boolean = true,
    val hyphenMode: HyphenMode = HyphenMode.KEEP_AS_ONE,
    val apostropheMode: ApostropheMode = ApostropheMode.KEEP_AS_ONE,
    val characterCountUnit: CharacterCountUnit = CharacterCountUnit.GRAPHEME,
    val urlHandling: Boolean = true, // count as one word
    val emailHandling: Boolean = true, // count as one word
    val keystrokeMode: KeystrokeMode = KeystrokeMode.CHARACTER_BASED,
    val advancedShiftCounting: Boolean = false,
    val typingSessionEnabled: Boolean = true,
    val liveWpm: Boolean = true,
    val actualVsEstimated: Boolean = true,
    val liveAnalysis: Boolean = true,
    val haptics: Boolean = true,
    val animations: Boolean = true,
    val confirmDelete: Boolean = true,
    val restoreLastText: Boolean = true
)

data class TypingTimeUiState(
    val text: String = "",
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val characterCountWithoutSpaces: Int = 0,
    val keystrokeCount: Int = 0,
    val sentenceCount: Int = 0,
    val paragraphCount: Int = 0,
    val lineCount: Int = 0,
    val averageWordLength: Double? = null,
    val averageWordsPerSentence: Double? = null,
    val graphemeCount: Int = 0,
    val codePointCount: Int = 0,
    val utf16Count: Int = 0,
    val speedUnit: SpeedUnit = SpeedUnit.WPM,
    val typingSpeed: Int = 45,
    val estimatedSeconds: Long = 0,
    val formattedEstimatedTime: String = "0 sec",
    val estimatedKpm: Int = 0,
    val isAnalyzing: Boolean = false,
    val isTypingSessionActive: Boolean = false,
    val isTypingSessionPaused: Boolean = false,
    val elapsedSessionSeconds: Long = 0,
    val currentWpm: Double? = null,
    val sessionWordsTyped: Int = 0,
    val sessionFinishedSummary: SessionFinishedSummary? = null,
    val paragraphBreakdowns: List<ParagraphBreakdown> = emptyList(),
    val speedComparisons: List<SpeedComparison> = emptyList(),
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val settings: AppSettings = AppSettings()
)
