package com.example.typingtime.data

import android.content.Context
import android.content.SharedPreferences
import com.example.typingtime.model.SpeedUnit
import com.example.typingtime.model.TypingTimeHistory
import org.json.JSONArray
import org.json.JSONObject

class HistoryRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "typing_time_history_prefs"
        private const val KEY_HISTORY = "history_entries"
        const val MAX_HISTORY_ITEMS = 10
    }

    fun loadHistory(): List<TypingTimeHistory> {
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<TypingTimeHistory>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TypingTimeHistory(
                        id = obj.getString("id"),
                        text = obj.getString("text"),
                        analyzedAt = obj.getLong("analyzedAt"),
                        speedUnit = SpeedUnit.valueOf(obj.getString("speedUnit")),
                        typingSpeed = obj.getInt("typingSpeed"),
                        countNumbers = obj.optBoolean("countNumbers", true),
                        hyphenMode = obj.optString("hyphenMode", "KEEP_AS_ONE"),
                        apostropheMode = obj.optString("apostropheMode", "KEEP_AS_ONE"),
                        keystrokeMode = obj.optString("keystrokeMode", "CHARACTER_BASED"),
                        wordCount = obj.optInt("wordCount", 0),
                        characterCount = obj.optInt("characterCount", 0),
                        estimatedSeconds = obj.optLong("estimatedSeconds", 0L),
                        formattedTime = obj.optString("formattedTime", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    /**
     * Saves a history entry, preventing immediate duplicates and maintaining at most 10 items.
     * Returns true if saved, false if skipped as duplicate.
     */
    fun saveHistory(entry: TypingTimeHistory): Boolean {
        val current = loadHistory().toMutableList()

        // Check for immediate duplicate
        if (current.isNotEmpty()) {
            val latest = current.first()
            if (latest.text == entry.text &&
                latest.typingSpeed == entry.typingSpeed &&
                latest.speedUnit == entry.speedUnit &&
                latest.countNumbers == entry.countNumbers &&
                latest.hyphenMode == entry.hyphenMode &&
                latest.apostropheMode == entry.apostropheMode
            ) {
                return false
            }
        }

        // Add to front
        current.add(0, entry)

        // Ensure max 10 entries (drops oldest)
        val trimmed = if (current.size > MAX_HISTORY_ITEMS) {
            current.take(MAX_HISTORY_ITEMS)
        } else {
            current
        }

        saveList(trimmed)
        return true
    }

    fun deleteHistory(id: String) {
        val current = loadHistory().filterNot { it.id == id }
        saveList(current)
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveList(list: List<TypingTimeHistory>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("text", item.text)
                put("analyzedAt", item.analyzedAt)
                put("speedUnit", item.speedUnit.name)
                put("typingSpeed", item.typingSpeed)
                put("countNumbers", item.countNumbers)
                put("hyphenMode", item.hyphenMode)
                put("apostropheMode", item.apostropheMode)
                put("keystrokeMode", item.keystrokeMode)
                put("wordCount", item.wordCount)
                put("characterCount", item.characterCount)
                put("estimatedSeconds", item.estimatedSeconds)
                put("formattedTime", item.formattedTime)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }
}
