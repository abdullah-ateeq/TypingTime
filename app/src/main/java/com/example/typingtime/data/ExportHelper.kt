package com.example.typingtime.data

import com.example.typingtime.analyzer.TextAnalyzer
import com.example.typingtime.model.SpeedUnit

object ExportHelper {

    fun generateSummaryText(
        result: TextAnalyzer.FullAnalysisResult,
        speed: Int,
        unit: SpeedUnit
    ): String {
        val stats = result.statistics
        val estimate = result.primaryEstimate

        val sb = StringBuilder()
        sb.appendLine("TypingTime")
        sb.appendLine()
        sb.appendLine("Estimated typing time:")
        sb.appendLine(estimate.formattedTime)
        sb.appendLine()
        sb.appendLine("Typing speed:")
        sb.appendLine("$speed ${unit.name}")
        sb.appendLine()
        sb.appendLine("Text statistics:")
        sb.appendLine()
        sb.appendLine("Words: ${stats.wordCount}")
        sb.appendLine("Characters: ${stats.characterCount}")
        sb.appendLine("Characters without spaces: ${stats.characterCountWithoutSpaces}")
        sb.appendLine("Estimated keystrokes: ${stats.keystrokeCount}")
        sb.appendLine("Sentences: ${stats.sentenceCount}")
        sb.appendLine("Paragraphs: ${stats.paragraphCount}")
        sb.appendLine("Lines: ${stats.lineCount}")
        sb.appendLine()
        if (stats.averageWordLength != null) {
            sb.appendLine("Average word length:")
            sb.appendLine("${stats.averageWordLength} characters")
            sb.appendLine()
        }
        if (stats.averageWordsPerSentence != null) {
            sb.appendLine("Average words per sentence:")
            sb.appendLine("${stats.averageWordsPerSentence}")
            sb.appendLine()
        }

        if (result.speedComparisons.isNotEmpty()) {
            sb.appendLine("Typing time comparison:")
            sb.appendLine()
            for (comp in result.speedComparisons) {
                sb.appendLine("${comp.speed} ${comp.speedUnit.name}: ${comp.formattedTime}")
            }
        }

        return sb.toString().trimEnd()
    }

    fun generateCsv(
        result: TextAnalyzer.FullAnalysisResult,
        speed: Int,
        unit: SpeedUnit
    ): String {
        val stats = result.statistics
        val estimate = result.primaryEstimate

        val sb = StringBuilder()
        sb.appendLine("Metric,Value")
        sb.appendLine("Word Count,${stats.wordCount}")
        sb.appendLine("Character Count,${stats.characterCount}")
        sb.appendLine("Characters Without Spaces,${stats.characterCountWithoutSpaces}")
        sb.appendLine("Estimated Keystrokes,${stats.keystrokeCount}")
        sb.appendLine("Sentence Count,${stats.sentenceCount}")
        sb.appendLine("Paragraph Count,${stats.paragraphCount}")
        sb.appendLine("Line Count,${stats.lineCount}")
        sb.appendLine("Average Word Length,${stats.averageWordLength ?: ""}")
        sb.appendLine("Average Words Per Sentence,${stats.averageWordsPerSentence ?: ""}")
        sb.appendLine("Typing Speed,$speed")
        sb.appendLine("Speed Unit,${unit.name}")
        sb.appendLine("Estimated Seconds,${estimate.totalSeconds}")
        sb.appendLine("Estimated Typing Time,${estimate.formattedTime}")
        sb.appendLine()
        sb.appendLine("Speed,Unit,Description,Seconds,Formatted Time")
        for (comp in result.speedComparisons) {
            sb.appendLine("${comp.speed},${comp.speedUnit.name},${comp.description},${comp.totalSeconds},${comp.formattedTime}")
        }

        return sb.toString()
    }
}
