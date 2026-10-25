package com.example.typingtime.analyzer

import com.example.typingtime.model.AppSettings
import com.example.typingtime.model.ParagraphBreakdown
import com.example.typingtime.model.SpeedComparison
import com.example.typingtime.model.SpeedUnit
import com.example.typingtime.model.TextStatistics
import com.example.typingtime.model.TypingTimeEstimate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

object TextAnalyzer {

    data class FullAnalysisResult(
        val statistics: TextStatistics,
        val primaryEstimate: TypingTimeEstimate,
        val estimatedKpm: Int,
        val paragraphBreakdowns: List<ParagraphBreakdown>,
        val speedComparisons: List<SpeedComparison>
    )

    suspend fun analyze(
        text: String,
        speed: Int,
        unit: SpeedUnit,
        settings: AppSettings
    ): FullAnalysisResult = withContext(Dispatchers.Default) {
        val trimmed = text.trim()

        if (trimmed.isEmpty()) {
            val emptyStats = TextStatistics(
                wordCount = 0,
                characterCount = 0,
                characterCountWithoutSpaces = 0,
                keystrokeCount = 0,
                sentenceCount = 0,
                paragraphCount = 0,
                lineCount = 0,
                averageWordLength = null,
                averageWordsPerSentence = null,
                graphemeCount = 0,
                codePointCount = 0,
                utf16Count = 0
            )
            val emptyEstimate = TypingTimeCalculator.calculate(0, 0, speed, unit, settings.timeDisplayDetailed)
            return@withContext FullAnalysisResult(
                statistics = emptyStats,
                primaryEstimate = emptyEstimate,
                estimatedKpm = 0,
                paragraphBreakdowns = emptyList(),
                speedComparisons = emptyList()
            )
        }

        // 1. Words
        val words = WordCounter.extractWords(
            text = text,
            countNumbers = settings.countNumbers,
            hyphenMode = settings.hyphenMode,
            apostropheMode = settings.apostropheMode,
            urlAsOneWord = settings.urlHandling,
            emailAsOneWord = settings.emailHandling
        )
        val wordCount = words.size

        // 2. Characters
        val graphemes = CharacterCounter.countGraphemes(text)
        val codePoints = CharacterCounter.countCodePoints(text)
        val utf16 = CharacterCounter.countUtf16Units(text)
        val selectedCharCount = when (settings.characterCountUnit) {
            com.example.typingtime.model.CharacterCountUnit.GRAPHEME -> graphemes
            com.example.typingtime.model.CharacterCountUnit.CODE_POINTS -> codePoints
            com.example.typingtime.model.CharacterCountUnit.UTF16 -> utf16
        }
        val charCountWithoutSpaces = CharacterCounter.countWithoutSpaces(text, settings.characterCountUnit)

        // 3. Keystrokes
        val keystrokes = KeystrokeEstimator.estimateKeystrokes(
            text = text,
            mode = settings.keystrokeMode,
            advancedShiftCounting = settings.advancedShiftCounting
        )

        // 4. Sentences
        val sentenceCount = SentenceDetector.countSentences(text)

        // 5. Paragraphs
        val paragraphs = ParagraphDetector.detectParagraphs(text)
        val paragraphCount = paragraphs.size

        // 6. Lines
        val lineCount = LineCounter.countLines(text)

        // 7. Averages
        val avgWordLength = if (wordCount > 0) {
            val totalWordChars = words.sumOf { it.length }
            roundToTwoDecimals(totalWordChars.toDouble() / wordCount.toDouble())
        } else null

        val avgWordsPerSentence = if (sentenceCount > 0 && wordCount > 0) {
            roundToTwoDecimals(wordCount.toDouble() / sentenceCount.toDouble())
        } else null

        val statistics = TextStatistics(
            wordCount = wordCount,
            characterCount = selectedCharCount,
            characterCountWithoutSpaces = charCountWithoutSpaces,
            keystrokeCount = keystrokes,
            sentenceCount = sentenceCount,
            paragraphCount = paragraphCount,
            lineCount = lineCount,
            averageWordLength = avgWordLength,
            averageWordsPerSentence = avgWordsPerSentence,
            graphemeCount = graphemes,
            codePointCount = codePoints,
            utf16Count = utf16
        )

        // 8. Primary typing time estimate
        val primaryEstimate = TypingTimeCalculator.calculate(
            wordCount = wordCount,
            characterCount = selectedCharCount,
            speed = speed,
            unit = unit,
            detailed = settings.timeDisplayDetailed
        )

        // 9. Estimated KPM
        val estimatedKpm = KeystrokeEstimator.calculateKpm(keystrokes, primaryEstimate.totalSeconds)

        // 10. Paragraph Breakdowns
        val breakdowns = paragraphs.mapIndexed { idx, paraText ->
            val pWords = WordCounter.countWords(
                text = paraText,
                countNumbers = settings.countNumbers,
                hyphenMode = settings.hyphenMode,
                apostropheMode = settings.apostropheMode,
                urlAsOneWord = settings.urlHandling,
                emailAsOneWord = settings.emailHandling
            )
            val pChars = CharacterCounter.count(paraText, settings.characterCountUnit)
            val pEst = TypingTimeCalculator.calculate(
                wordCount = pWords,
                characterCount = pChars,
                speed = speed,
                unit = unit,
                detailed = settings.timeDisplayDetailed
            )
            ParagraphBreakdown(
                index = idx + 1,
                text = paraText,
                wordCount = pWords,
                characterCount = pChars,
                estimatedSeconds = pEst.totalSeconds,
                formattedTime = pEst.formattedTime
            )
        }

        // 11. Speed Comparisons
        val presetList = if (unit == SpeedUnit.WPM) {
            listOf(
                Triple(settings.presetBeginner, "Beginner", false),
                Triple(settings.presetCasual, "Casual", false),
                Triple(settings.presetAverage, "Average", false),
                Triple(settings.presetFast, "Fast", false),
                Triple(settings.presetExpert, "Expert", false)
            )
        } else {
            listOf(
                Triple(100, "Slow", false),
                Triple(150, "Casual", false),
                Triple(225, "Average", false),
                Triple(300, "Fast", false),
                Triple(400, "Expert", false)
            )
        }

        val comparisons = mutableListOf<SpeedComparison>()
        var customAdded = false

        for ((pSpeed, label, _) in presetList) {
            val isCurrent = pSpeed == speed
            val est = TypingTimeCalculator.calculate(wordCount, selectedCharCount, pSpeed, unit, detailed = false)
            comparisons.add(
                SpeedComparison(
                    speed = pSpeed,
                    speedUnit = unit,
                    description = label,
                    totalSeconds = est.totalSeconds,
                    formattedTime = est.formattedTime,
                    isUserSpeed = isCurrent
                )
            )
            if (isCurrent) {
                customAdded = true
            }
        }

        // If the user's custom speed is not in the preset list, add it as "Your Speed"
        if (!customAdded && speed > 0) {
            val userEst = TypingTimeCalculator.calculate(wordCount, selectedCharCount, speed, unit, detailed = false)
            val userComp = SpeedComparison(
                speed = speed,
                speedUnit = unit,
                description = "Your Speed",
                totalSeconds = userEst.totalSeconds,
                formattedTime = userEst.formattedTime,
                isUserSpeed = true
            )
            // Insert in sorted order
            val insertIdx = comparisons.indexOfFirst { it.speed > speed }
            if (insertIdx >= 0) {
                comparisons.add(insertIdx, userComp)
            } else {
                comparisons.add(userComp)
            }
        }

        FullAnalysisResult(
            statistics = statistics,
            primaryEstimate = primaryEstimate,
            estimatedKpm = estimatedKpm,
            paragraphBreakdowns = breakdowns,
            speedComparisons = comparisons
        )
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toDouble()
    }
}
