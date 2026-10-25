package com.example.typingtime.analyzer

import java.util.Locale

object SentenceDetector {

    private val COMMON_ABBREVIATIONS = setOf(
        "dr", "mr", "mrs", "ms", "prof", "sr", "jr", "vs", "etc", "eg", "ie",
        "no", "vol", "approx", "est", "dept", "inc", "ltd", "co", "corp",
        "st", "ave", "jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep",
        "oct", "nov", "dec", "univ", "assn", "bros", "gen", "gov", "rep", "sen"
    )

    private val TERMINAL_CHARS = setOf(
        '.', '!', '?', '。', '！', '？', '…'
    )

    fun countSentences(text: String): Int {
        return detectSentences(text).size
    }

    fun detectSentences(text: String): List<String> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return emptyList()

        val sentences = mutableListOf<String>()
        val len = trimmed.length
        var sentenceStart = 0
        var i = 0

        while (i < len) {
            val c = trimmed[i]

            if (c in TERMINAL_CHARS) {
                // Consume consecutive terminal punctuation (e.g. "...", "!!!", "?!")
                var termEnd = i
                while (termEnd < len && trimmed[termEnd] in TERMINAL_CHARS) {
                    termEnd++
                }

                // Consume trailing quotation marks or closing brackets if any
                var boundaryEnd = termEnd
                while (boundaryEnd < len && (trimmed[boundaryEnd] in "\"'”’)]}")) {
                    boundaryEnd++
                }

                // Check if this terminal punctuation is a false positive:
                val isFalsePositive = isFalseTerminal(trimmed, i, termEnd)

                if (!isFalsePositive) {
                    // Check if it is followed by whitespace, newline, or end of string
                    val isEndOfString = boundaryEnd >= len
                    val isFollowedByWhitespace = boundaryEnd < len && Character.isWhitespace(trimmed[boundaryEnd])

                    if (isEndOfString || isFollowedByWhitespace) {
                        val candidate = trimmed.substring(sentenceStart, boundaryEnd).trim()
                        // Ensure the candidate has at least one letter, digit, or CJK char
                        if (candidate.any { Character.isLetterOrDigit(it) || it in '一'..'龥' }) {
                            sentences.add(candidate)
                        }

                        // Skip subsequent whitespaces
                        var nextStart = boundaryEnd
                        while (nextStart < len && Character.isWhitespace(trimmed[nextStart])) {
                            nextStart++
                        }
                        sentenceStart = nextStart
                        i = sentenceStart
                        continue
                    }
                }
                i = termEnd
                continue
            }
            i++
        }

        // Remaining text after last terminal punctuation
        if (sentenceStart < len) {
            val remaining = trimmed.substring(sentenceStart).trim()
            if (remaining.any { Character.isLetterOrDigit(it) || it in '一'..'龥' }) {
                sentences.add(remaining)
            }
        }

        return sentences
    }

    private fun isFalseTerminal(text: String, termStart: Int, termEnd: Int): Boolean {
        val char = text[termStart]

        // Only period '.' is typically ambiguous with decimals, versions, abbreviations, emails, URLs
        if (char != '.') {
            return false
        }

        // 1. Decimal or version check: digit before and digit after (e.g. 12.50, 2.5.1)
        if (termStart > 0 && Character.isDigit(text[termStart - 1]) &&
            termEnd < text.length && Character.isDigit(text[termEnd])
        ) {
            return true
        }

        // 2. Email or URL dot check: letter/digit before and letter/digit after without whitespace
        // (e.g. example.com, support@example.com)
        if (termStart > 0 && Character.isLetterOrDigit(text[termStart - 1]) &&
            termEnd < text.length && Character.isLetterOrDigit(text[termEnd])
        ) {
            return true
        }

        // 3. Abbreviation check: look at the token right before this period
        var wordStart = termStart - 1
        while (wordStart >= 0 && Character.isLetter(text[wordStart])) {
            wordStart--
        }
        val wordBefore = text.substring(wordStart + 1, termStart).lowercase(Locale.ROOT)

        if (wordBefore in COMMON_ABBREVIATIONS) {
            return true
        }

        // Single letter abbreviation / initial (e.g., "John F. Kennedy" or "Dr. A. Smith")
        if (wordBefore.length == 1 && Character.isUpperCase(text[termStart - 1])) {
            // If next word starts with uppercase, or not end of string, likely an initial
            return true
        }

        // Multi-dot abbreviation like "e.g." or "i.e."
        if (wordBefore == "g" && termStart >= 3 && text.substring(termStart - 3, termStart).equals("e.g", ignoreCase = true)) {
            return true
        }
        if (wordBefore == "e" && termStart >= 3 && text.substring(termStart - 3, termStart).equals("i.e", ignoreCase = true)) {
            return true
        }

        return false
    }
}
