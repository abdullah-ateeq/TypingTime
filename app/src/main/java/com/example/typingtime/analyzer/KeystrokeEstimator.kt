package com.example.typingtime.analyzer

import com.example.typingtime.model.KeystrokeMode

object KeystrokeEstimator {

    private val SHIFT_SYMBOLS = setOf(
        '~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+',
        '{', '}', '|', ':', '"', '<', '>', '?'
    )

    /**
     * Estimates keystrokes based on the chosen mode.
     */
    fun estimateKeystrokes(
        text: String,
        mode: KeystrokeMode = KeystrokeMode.CHARACTER_BASED,
        advancedShiftCounting: Boolean = false
    ): Int {
        if (text.isEmpty()) return 0

        // Handle normalized newlines: \r\n is 1 Enter keystroke
        val normalized = text.replace("\r\n", "\n").replace("\r", "\n")

        val useAdvanced = mode == KeystrokeMode.ADVANCED_KEY_MODEL || advancedShiftCounting

        if (!useAdvanced) {
            // Character-based: length of normalized characters
            return normalized.length
        }

        var keystrokes = 0
        for (c in normalized) {
            when {
                Character.isUpperCase(c) -> keystrokes += 2
                c in SHIFT_SYMBOLS -> keystrokes += 2
                else -> keystrokes += 1
            }
        }
        return keystrokes
    }

    /**
     * Calculates estimated Keystrokes Per Minute (KPM).
     */
    fun calculateKpm(keystrokes: Int, totalSeconds: Long): Int {
        if (totalSeconds <= 0 || keystrokes <= 0) return 0
        val minutes = totalSeconds.toDouble() / 60.0
        return if (minutes > 0.0) (keystrokes / minutes).toInt() else 0
    }
}
