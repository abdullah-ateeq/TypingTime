package com.example.typingtime.analyzer

object LineCounter {

    /**
     * Counts actual newline-separated lines. Empty trailing lines do not artificially increase the count.
     */
    fun countLines(text: String): Int {
        val trimmed = text.trimEnd('\r', '\n')
        if (trimmed.isEmpty()) return 0
        return trimmed.lines().size
    }
}
