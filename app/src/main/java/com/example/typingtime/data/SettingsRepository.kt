package com.example.typingtime.data

import android.content.Context
import android.content.SharedPreferences
import com.example.typingtime.model.ApostropheMode
import com.example.typingtime.model.AppSettings
import com.example.typingtime.model.CalibrationResult
import com.example.typingtime.model.CharacterCountUnit
import com.example.typingtime.model.HyphenMode
import com.example.typingtime.model.KeystrokeMode
import com.example.typingtime.model.SpeedUnit
import org.json.JSONArray
import org.json.JSONObject

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "typing_time_preferences"
        private const val KEY_THEME = "theme"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_DEFAULT_WPM = "default_wpm"
        private const val KEY_PRESET_BEGINNER = "preset_beginner"
        private const val KEY_PRESET_CASUAL = "preset_casual"
        private const val KEY_PRESET_AVERAGE = "preset_average"
        private const val KEY_PRESET_FAST = "preset_fast"
        private const val KEY_PRESET_EXPERT = "preset_expert"
        private const val KEY_ESTIMATION_METHOD = "estimation_method"
        private const val KEY_DEFAULT_CPM = "default_cpm"
        private const val KEY_TIME_DISPLAY_DETAILED = "time_display_detailed"
        private const val KEY_COUNT_NUMBERS = "count_numbers"
        private const val KEY_HYPHEN_MODE = "hyphen_mode"
        private const val KEY_APOSTROPHE_MODE = "apostrophe_mode"
        private const val KEY_CHAR_UNIT = "character_count_unit"
        private const val KEY_URL_HANDLING = "url_handling"
        private const val KEY_EMAIL_HANDLING = "email_handling"
        private const val KEY_KEYSTROKE_MODE = "keystroke_mode"
        private const val KEY_ADVANCED_SHIFT = "advanced_shift_counting"
        private const val KEY_TYPING_SESSION = "typing_session_enabled"
        private const val KEY_LIVE_WPM = "live_wpm"
        private const val KEY_ACTUAL_VS_EST = "actual_vs_estimated"
        private const val KEY_LIVE_ANALYSIS = "live_analysis"
        private const val KEY_HAPTICS = "haptics"
        private const val KEY_ANIMATIONS = "animations"
        private const val KEY_CONFIRM_DELETE = "confirm_delete"
        private const val KEY_RESTORE_LAST_TEXT = "restore_last_text"
        private const val KEY_LAST_TEXT = "last_typed_text"
        private const val KEY_CALIBRATION_HISTORY = "calibration_history"
        private const val KEY_CALIBRATED_SPEED = "calibrated_speed"
    }

    fun loadSettings(): AppSettings {
        return AppSettings(
            theme = prefs.getString(KEY_THEME, "System") ?: "System",
            dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true),
            defaultWpm = prefs.getInt(KEY_DEFAULT_WPM, 45),
            presetBeginner = prefs.getInt(KEY_PRESET_BEGINNER, 20),
            presetCasual = prefs.getInt(KEY_PRESET_CASUAL, 30),
            presetAverage = prefs.getInt(KEY_PRESET_AVERAGE, 45),
            presetFast = prefs.getInt(KEY_PRESET_FAST, 60),
            presetExpert = prefs.getInt(KEY_PRESET_EXPERT, 80),
            estimationMethod = try {
                SpeedUnit.valueOf(prefs.getString(KEY_ESTIMATION_METHOD, SpeedUnit.WPM.name) ?: SpeedUnit.WPM.name)
            } catch (e: Exception) { SpeedUnit.WPM },
            defaultCpm = prefs.getInt(KEY_DEFAULT_CPM, 225),
            timeDisplayDetailed = prefs.getBoolean(KEY_TIME_DISPLAY_DETAILED, true),
            countNumbers = prefs.getBoolean(KEY_COUNT_NUMBERS, true),
            hyphenMode = try {
                HyphenMode.valueOf(prefs.getString(KEY_HYPHEN_MODE, HyphenMode.KEEP_AS_ONE.name) ?: HyphenMode.KEEP_AS_ONE.name)
            } catch (e: Exception) { HyphenMode.KEEP_AS_ONE },
            apostropheMode = try {
                ApostropheMode.valueOf(prefs.getString(KEY_APOSTROPHE_MODE, ApostropheMode.KEEP_AS_ONE.name) ?: ApostropheMode.KEEP_AS_ONE.name)
            } catch (e: Exception) { ApostropheMode.KEEP_AS_ONE },
            characterCountUnit = try {
                CharacterCountUnit.valueOf(prefs.getString(KEY_CHAR_UNIT, CharacterCountUnit.GRAPHEME.name) ?: CharacterCountUnit.GRAPHEME.name)
            } catch (e: Exception) { CharacterCountUnit.GRAPHEME },
            urlHandling = prefs.getBoolean(KEY_URL_HANDLING, true),
            emailHandling = prefs.getBoolean(KEY_EMAIL_HANDLING, true),
            keystrokeMode = try {
                KeystrokeMode.valueOf(prefs.getString(KEY_KEYSTROKE_MODE, KeystrokeMode.CHARACTER_BASED.name) ?: KeystrokeMode.CHARACTER_BASED.name)
            } catch (e: Exception) { KeystrokeMode.CHARACTER_BASED },
            advancedShiftCounting = prefs.getBoolean(KEY_ADVANCED_SHIFT, false),
            typingSessionEnabled = prefs.getBoolean(KEY_TYPING_SESSION, true),
            liveWpm = prefs.getBoolean(KEY_LIVE_WPM, true),
            actualVsEstimated = prefs.getBoolean(KEY_ACTUAL_VS_EST, true),
            liveAnalysis = prefs.getBoolean(KEY_LIVE_ANALYSIS, true),
            haptics = prefs.getBoolean(KEY_HAPTICS, true),
            animations = prefs.getBoolean(KEY_ANIMATIONS, true),
            confirmDelete = prefs.getBoolean(KEY_CONFIRM_DELETE, true),
            restoreLastText = prefs.getBoolean(KEY_RESTORE_LAST_TEXT, true)
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.theme)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            .putInt(KEY_DEFAULT_WPM, settings.defaultWpm)
            .putInt(KEY_PRESET_BEGINNER, settings.presetBeginner)
            .putInt(KEY_PRESET_CASUAL, settings.presetCasual)
            .putInt(KEY_PRESET_AVERAGE, settings.presetAverage)
            .putInt(KEY_PRESET_FAST, settings.presetFast)
            .putInt(KEY_PRESET_EXPERT, settings.presetExpert)
            .putString(KEY_ESTIMATION_METHOD, settings.estimationMethod.name)
            .putInt(KEY_DEFAULT_CPM, settings.defaultCpm)
            .putBoolean(KEY_TIME_DISPLAY_DETAILED, settings.timeDisplayDetailed)
            .putBoolean(KEY_COUNT_NUMBERS, settings.countNumbers)
            .putString(KEY_HYPHEN_MODE, settings.hyphenMode.name)
            .putString(KEY_APOSTROPHE_MODE, settings.apostropheMode.name)
            .putString(KEY_CHAR_UNIT, settings.characterCountUnit.name)
            .putBoolean(KEY_URL_HANDLING, settings.urlHandling)
            .putBoolean(KEY_EMAIL_HANDLING, settings.emailHandling)
            .putString(KEY_KEYSTROKE_MODE, settings.keystrokeMode.name)
            .putBoolean(KEY_ADVANCED_SHIFT, settings.advancedShiftCounting)
            .putBoolean(KEY_TYPING_SESSION, settings.typingSessionEnabled)
            .putBoolean(KEY_LIVE_WPM, settings.liveWpm)
            .putBoolean(KEY_ACTUAL_VS_EST, settings.actualVsEstimated)
            .putBoolean(KEY_LIVE_ANALYSIS, settings.liveAnalysis)
            .putBoolean(KEY_HAPTICS, settings.haptics)
            .putBoolean(KEY_ANIMATIONS, settings.animations)
            .putBoolean(KEY_CONFIRM_DELETE, settings.confirmDelete)
            .putBoolean(KEY_RESTORE_LAST_TEXT, settings.restoreLastText)
            .apply()
    }

    fun resetToDefaults(): AppSettings {
        val defaults = AppSettings()
        saveSettings(defaults)
        return defaults
    }

    fun saveLastText(text: String) {
        prefs.edit().putString(KEY_LAST_TEXT, text).apply()
    }

    fun loadLastText(): String {
        return prefs.getString(KEY_LAST_TEXT, "") ?: ""
    }

    fun getCalibratedSpeed(): Int? {
        val speed = prefs.getInt(KEY_CALIBRATED_SPEED, -1)
        return if (speed > 0) speed else null
    }

    fun setCalibratedSpeed(speed: Int) {
        prefs.edit().putInt(KEY_CALIBRATED_SPEED, speed).apply()
    }

    fun clearCalibratedSpeed() {
        prefs.edit().remove(KEY_CALIBRATED_SPEED).apply()
    }

    fun loadCalibrationHistory(): List<CalibrationResult> {
        val jsonStr = prefs.getString(KEY_CALIBRATION_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<CalibrationResult>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CalibrationResult(
                        id = obj.getString("id"),
                        wpm = obj.getInt("wpm"),
                        accuracy = obj.getDouble("accuracy"),
                        errors = obj.getInt("errors"),
                        totalCharacters = obj.getInt("totalCharacters"),
                        activeSeconds = obj.getLong("activeSeconds"),
                        formattedTime = obj.getString("formattedTime"),
                        timestamp = obj.getLong("timestamp")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveCalibrationResult(result: CalibrationResult) {
        val history = loadCalibrationHistory().toMutableList()
        history.add(0, result)
        // Keep only last 5 calibrations
        val trimmed = if (history.size > 5) history.take(5) else history

        val array = JSONArray()
        for (item in trimmed) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("wpm", item.wpm)
                put("accuracy", item.accuracy)
                put("errors", item.errors)
                put("totalCharacters", item.totalCharacters)
                put("activeSeconds", item.activeSeconds)
                put("formattedTime", item.formattedTime)
                put("timestamp", item.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_CALIBRATION_HISTORY, array.toString()).apply()
        setCalibratedSpeed(result.wpm)
    }

    fun deleteCalibrationResult(id: String) {
        val history = loadCalibrationHistory().filterNot { it.id == id }
        val array = JSONArray()
        for (item in history) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("wpm", item.wpm)
                put("accuracy", item.accuracy)
                put("errors", item.errors)
                put("totalCharacters", item.totalCharacters)
                put("activeSeconds", item.activeSeconds)
                put("formattedTime", item.formattedTime)
                put("timestamp", item.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_CALIBRATION_HISTORY, array.toString()).apply()
    }
}
