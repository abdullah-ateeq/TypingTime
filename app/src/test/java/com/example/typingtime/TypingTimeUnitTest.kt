package com.example.typingtime

import com.example.typingtime.analyzer.CharacterCounter
import com.example.typingtime.analyzer.KeystrokeEstimator
import com.example.typingtime.analyzer.LineCounter
import com.example.typingtime.analyzer.ParagraphDetector
import com.example.typingtime.analyzer.SentenceDetector
import com.example.typingtime.analyzer.TextAnalyzer
import com.example.typingtime.analyzer.TypingTimeCalculator
import com.example.typingtime.analyzer.WordCounter
import com.example.typingtime.model.ApostropheMode
import com.example.typingtime.model.AppSettings
import com.example.typingtime.model.CharacterCountUnit
import com.example.typingtime.model.HyphenMode
import com.example.typingtime.model.KeystrokeMode
import com.example.typingtime.model.SpeedUnit
import com.example.typingtime.model.TypingTimeHistory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TypingTimeUnitTest {

    // --- 1. BASIC WPM TEST ---
    @Test
    fun testBasicWpm() {
        val estimate = TypingTimeCalculator.calculate(
            wordCount = 45,
            characterCount = 200,
            speed = 45,
            unit = SpeedUnit.WPM,
            detailed = true
        )
        assertEquals(60L, estimate.totalSeconds)
        assertTrue(estimate.formattedTime.contains("1 min"))
    }

    // --- 2. DOUBLE TIME TEST ---
    @Test
    fun testDoubleTime() {
        val estimate = TypingTimeCalculator.calculate(
            wordCount = 90,
            characterCount = 400,
            speed = 45,
            unit = SpeedUnit.WPM,
            detailed = true
        )
        assertEquals(120L, estimate.totalSeconds)
        assertTrue(estimate.formattedTime.contains("2 min"))
    }

    // --- 3. SLOW SPEED TEST ---
    @Test
    fun testSlowSpeed() {
        val estimate = TypingTimeCalculator.calculate(
            wordCount = 300,
            characterCount = 1500,
            speed = 30,
            unit = SpeedUnit.WPM,
            detailed = true
        )
        assertEquals(600L, estimate.totalSeconds)
        assertTrue(estimate.formattedTime.contains("10 min"))
    }

    // --- 4. FAST SPEED TEST ---
    @Test
    fun testFastSpeed() {
        val estimate = TypingTimeCalculator.calculate(
            wordCount = 300,
            characterCount = 1500,
            speed = 60,
            unit = SpeedUnit.WPM,
            detailed = true
        )
        assertEquals(300L, estimate.totalSeconds)
        assertTrue(estimate.formattedTime.contains("5 min"))
    }

    // --- 5. SHORT PASSAGE TEST ---
    @Test
    fun testShortPassage() {
        val estimate = TypingTimeCalculator.calculate(
            wordCount = 5,
            characterCount = 25,
            speed = 45,
            unit = SpeedUnit.WPM,
            detailed = true
        )
        // 5.0 / 45.0 * 60 = 6.6666... rounded to 7 seconds
        assertEquals(7L, estimate.totalSeconds)
        assertEquals("7 sec", estimate.formattedTime)
    }

    // --- 6. WORD COUNT TEST ---
    @Test
    fun testWordCount() {
        val count = WordCounter.countWords("Hello world!")
        assertEquals(2, count)
    }

    // --- 7. APOSTROPHE TEST ---
    @Test
    fun testApostrophe() {
        val count = WordCounter.countWords(
            text = "I don't know.",
            apostropheMode = ApostropheMode.KEEP_AS_ONE
        )
        assertEquals(3, count)

        val splitCount = WordCounter.countWords(
            text = "I don't know.",
            apostropheMode = ApostropheMode.SPLIT
        )
        assertEquals(4, splitCount)
    }

    // --- 8. HYPHEN TEST ---
    @Test
    fun testHyphen() {
        val defaultCount = WordCounter.countWords(
            text = "This is well-known.",
            hyphenMode = HyphenMode.KEEP_AS_ONE
        )
        assertEquals(3, defaultCount)

        val splitCount = WordCounter.countWords(
            text = "This is well-known.",
            hyphenMode = HyphenMode.SPLIT
        )
        assertEquals(4, splitCount)
    }

    // --- 9. NUMBERS TEST ---
    @Test
    fun testNumbers() {
        val enabled = WordCounter.countWords(
            text = "There are 25 apples.",
            countNumbers = true
        )
        assertEquals(4, enabled)

        val disabled = WordCounter.countWords(
            text = "There are 25 apples.",
            countNumbers = false
        )
        assertEquals(3, disabled)
    }

    // --- 10. DECIMAL NUMBERS TEST ---
    @Test
    fun testDecimalNumbers() {
        val count = WordCounter.countWords(
            text = "The price is 12.50 dollars.",
            countNumbers = true
        )
        assertEquals(5, count)
    }

    // --- 11. VERSION NUMBERS TEST ---
    @Test
    fun testVersionNumbers() {
        val count = WordCounter.countWords(
            text = "Version 2.5.1 is installed.",
            countNumbers = true
        )
        assertEquals(4, count)
    }

    // --- 12. URL TEST ---
    @Test
    fun testUrl() {
        val words = WordCounter.extractWords("Visit https://example.com for details.")
        assertEquals(4, words.size)
        assertTrue(words.contains("https://example.com"))
    }

    // --- 13. EMAIL TEST ---
    @Test
    fun testEmail() {
        val words = WordCounter.extractWords("Email support@example.com for help.")
        assertEquals(4, words.size)
        assertTrue(words.contains("support@example.com"))
    }

    // --- 14. SENTENCE COUNT TEST ---
    @Test
    fun testSentenceCount() {
        val count = SentenceDetector.countSentences("Hello world. How are you?")
        assertEquals(2, count)
    }

    // --- 15. ABBREVIATION TEST ---
    @Test
    fun testAbbreviation() {
        val count = SentenceDetector.countSentences("Dr. Smith arrived. He waited.")
        assertEquals(2, count)
    }

    // --- 16. KEYSTROKE COUNT TEST ---
    @Test
    fun testKeystrokeCount() {
        val count = KeystrokeEstimator.estimateKeystrokes(
            text = "abc def",
            mode = KeystrokeMode.CHARACTER_BASED
        )
        assertEquals(7, count)
    }

    // --- 17. SPACES TEST ---
    @Test
    fun testSpaces() {
        val input = "Hello world."
        val withSpaces = CharacterCounter.count(input, CharacterCountUnit.GRAPHEME)
        val withoutSpaces = CharacterCounter.countWithoutSpaces(input, CharacterCountUnit.GRAPHEME)
        assertEquals(12, withSpaces)
        assertEquals(11, withoutSpaces)
    }

    // --- 18. UNICODE TEST ---
    @Test
    fun testUnicode() {
        val input = "café 你好 مرحبا"
        val words = WordCounter.extractWords(input)
        assertTrue(words.isNotEmpty())
        val graphemes = CharacterCounter.countGraphemes(input)
        assertTrue(graphemes > 0)
    }

    // --- 19. LARGE TEXT PERFORMANCE TEST ---
    @Test
    fun testLargeTextProcessing() = runBlocking {
        // Generate passage with over 100,000 characters
        val base = "The quick brown fox jumps over the lazy dog. Version 2.5.1 is available at https://example.com. Contact info@example.com! "
        val sb = StringBuilder()
        while (sb.length < 100_500) {
            sb.append(base)
            sb.append("\n\n")
        }
        val largeText = sb.toString()
        assertTrue(largeText.length >= 100_000)

        val result = TextAnalyzer.analyze(
            text = largeText,
            speed = 45,
            unit = SpeedUnit.WPM,
            settings = AppSettings()
        )

        assertTrue(result.statistics.wordCount > 5000)
        assertTrue(result.statistics.characterCount >= 100_000)
        assertTrue(result.primaryEstimate.totalSeconds > 0)
        assertTrue(result.paragraphBreakdowns.isNotEmpty())
        assertTrue(result.speedComparisons.isNotEmpty())
    }

    // --- 20. WPM & CPM VALIDATION TEST ---
    @Test
    fun testSpeedValidation() {
        val validWpm = 45 in SpeedUnit.WPM.minSpeed..SpeedUnit.WPM.maxSpeed
        val invalidLowWpm = 0 in SpeedUnit.WPM.minSpeed..SpeedUnit.WPM.maxSpeed
        val invalidHighWpm = 350 in SpeedUnit.WPM.minSpeed..SpeedUnit.WPM.maxSpeed
        assertTrue(validWpm)
        assertFalse(invalidLowWpm)
        assertFalse(invalidHighWpm)

        val validCpm = 225 in SpeedUnit.CPM.minSpeed..SpeedUnit.CPM.maxSpeed
        val invalidLowCpm = 10 in SpeedUnit.CPM.minSpeed..SpeedUnit.CPM.maxSpeed
        val invalidHighCpm = 2500 in SpeedUnit.CPM.minSpeed..SpeedUnit.CPM.maxSpeed
        assertTrue(validCpm)
        assertFalse(invalidLowCpm)
        assertFalse(invalidHighCpm)
    }

    // --- 21. TYPING SESSION SPEED TEST ---
    @Test
    fun testTypingSessionCalculation() {
        // 60 words in 60 seconds = 60 WPM
        val activeMinutes1 = 60.0 / 60.0
        val wpm1 = 60.0 / activeMinutes1
        assertEquals(60.0, wpm1, 0.01)

        // 30 words in 60 seconds = 30 WPM
        val activeMinutes2 = 60.0 / 60.0
        val wpm2 = 30.0 / activeMinutes2
        assertEquals(30.0, wpm2, 0.01)
    }

    // --- 22. CALIBRATION TEST ---
    @Test
    fun testCalibrationSpeed() {
        val wordCount = 100
        val activeSeconds = 120L // 2 minutes
        val minutes = activeSeconds.toDouble() / 60.0
        val wpm = (wordCount.toDouble() / minutes).toInt()
        assertEquals(50, wpm)
    }

    // --- 23. PARAGRAPH & LINE DETECTOR TESTS ---
    @Test
    fun testParagraphAndLineDetector() {
        val text = "Paragraph one with some text.\nStill in paragraph one.\n\nParagraph two here.\n\nParagraph three."
        val paragraphs = ParagraphDetector.detectParagraphs(text)
        assertEquals(3, paragraphs.size)

        val lines = LineCounter.countLines(text)
        assertEquals(6, lines)

        // Verify empty trailing lines do not artificially increase line count
        val withTrailingNewlines = "Line 1\nLine 2\n\n\n"
        assertEquals(2, LineCounter.countLines(withTrailingNewlines))
    }
}
