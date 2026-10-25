package com.example.typingtime.analyzer

import com.example.typingtime.model.CharacterCountUnit
import java.text.BreakIterator

object CharacterCounter {

    /**
     * Counts grapheme clusters using BreakIterator.
     */
    fun countGraphemes(text: String): Int {
        if (text.isEmpty()) return 0
        val boundary = BreakIterator.getCharacterInstance()
        boundary.setText(text)
        var count = 0
        while (boundary.next() != BreakIterator.DONE) {
            count++
        }
        return count
    }

    /**
     * Counts Unicode code points.
     */
    fun countCodePoints(text: String): Int {
        if (text.isEmpty()) return 0
        return text.codePointCount(0, text.length)
    }

    /**
     * Counts UTF-16 code units.
     */
    fun countUtf16Units(text: String): Int {
        return text.length
    }

    /**
     * Counts characters according to the specified unit.
     */
    fun count(text: String, unit: CharacterCountUnit = CharacterCountUnit.GRAPHEME): Int {
        return when (unit) {
            CharacterCountUnit.GRAPHEME -> countGraphemes(text)
            CharacterCountUnit.CODE_POINTS -> countCodePoints(text)
            CharacterCountUnit.UTF16 -> countUtf16Units(text)
        }
    }

    /**
     * Counts characters excluding whitespace.
     */
    fun countWithoutSpaces(text: String, unit: CharacterCountUnit = CharacterCountUnit.GRAPHEME): Int {
        if (text.isEmpty()) return 0

        return when (unit) {
            CharacterCountUnit.GRAPHEME -> {
                val boundary = BreakIterator.getCharacterInstance()
                boundary.setText(text)
                var count = 0
                var start = boundary.first()
                var end = boundary.next()
                while (end != BreakIterator.DONE) {
                    val cluster = text.substring(start, end)
                    // If the cluster has any non-whitespace character, count it
                    if (cluster.any { !Character.isWhitespace(it) }) {
                        count++
                    }
                    start = end
                    end = boundary.next()
                }
                count
            }
            CharacterCountUnit.CODE_POINTS -> {
                var count = 0
                var i = 0
                val len = text.length
                while (i < len) {
                    val cp = text.codePointAt(i)
                    if (!Character.isWhitespace(cp)) {
                        count++
                    }
                    i += Character.charCount(cp)
                }
                count
            }
            CharacterCountUnit.UTF16 -> {
                text.count { !it.isWhitespace() }
            }
        }
    }
}
