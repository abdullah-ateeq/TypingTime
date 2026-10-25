package com.example.typingtime.analyzer

import com.example.typingtime.model.SpeedUnit
import com.example.typingtime.model.TypingTimeEstimate
import kotlin.math.roundToLong

object TypingTimeCalculator {

    /**
     * Calculates typing duration using floating-point arithmetic.
     */
    fun calculate(
        wordCount: Int,
        characterCount: Int,
        speed: Int,
        unit: SpeedUnit = SpeedUnit.WPM,
        detailed: Boolean = true
    ): TypingTimeEstimate {
        val safeSpeed = speed.coerceAtLeast(1)

        val totalSeconds = when (unit) {
            SpeedUnit.WPM -> {
                if (wordCount <= 0) 0L
                else {
                    val minutes = wordCount.toDouble() / safeSpeed.toDouble()
                    (minutes * 60.0).roundToLong().coerceAtLeast(if (wordCount > 0) 1L else 0L)
                }
            }
            SpeedUnit.CPM -> {
                if (characterCount <= 0) 0L
                else {
                    val minutes = characterCount.toDouble() / safeSpeed.toDouble()
                    (minutes * 60.0).roundToLong().coerceAtLeast(if (characterCount > 0) 1L else 0L)
                }
            }
        }

        val formatted = formatTime(totalSeconds, detailed)

        return TypingTimeEstimate(
            speed = speed,
            speedUnit = unit,
            totalSeconds = totalSeconds,
            formattedTime = formatted
        )
    }

    /**
     * Formats seconds into human-readable representation:
     * - Under 1 min: "24 sec"
     * - 1 min to 1 hr: "8 min 24 sec"
     * - Over 1 hr: "1 hr 08 min" (or with seconds if detailed, e.g. "1 hr 08 min 12 sec")
     */
    fun formatTime(totalSeconds: Long, detailed: Boolean = true): String {
        if (totalSeconds <= 0L) return if (detailed) "0 sec" else "0s"

        val hours = totalSeconds / 3600
        val remainder = totalSeconds % 3600
        val minutes = remainder / 60
        val seconds = remainder % 60

        return if (detailed) {
            when {
                hours > 0 -> {
                    val minStr = if (minutes < 10) "0$minutes" else "$minutes"
                    if (seconds > 0) {
                        val secStr = if (seconds < 10) "0$seconds" else "$seconds"
                        "$hours hr $minStr min $secStr sec"
                    } else {
                        "$hours hr $minStr min"
                    }
                }
                minutes > 0 -> {
                    if (seconds > 0) {
                        "$minutes min $seconds sec"
                    } else {
                        "$minutes min"
                    }
                }
                else -> "$seconds sec"
            }
        } else {
            when {
                hours > 0 -> {
                    val minStr = if (minutes < 10) "0$minutes" else "$minutes"
                    "${hours}h ${minStr}m"
                }
                minutes > 0 -> {
                    if (seconds > 0) "${minutes}m ${seconds}s" else "${minutes}m"
                }
                else -> "${seconds}s"
            }
        }
    }

    /**
     * Formats difference in seconds between actual and estimated (e.g. "+27 sec", "-42 sec", "0 sec").
     */
    fun formatDifference(differenceSeconds: Long): String {
        val sign = when {
            differenceSeconds > 0 -> "+"
            differenceSeconds < 0 -> "-"
            else -> ""
        }
        val absSeconds = kotlin.math.abs(differenceSeconds)
        return "$sign${formatTime(absSeconds, detailed = true)}"
    }
}
