package com.example.typingtime.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.typingtime.analyzer.CharacterCounter
import com.example.typingtime.analyzer.SentenceDetector
import com.example.typingtime.analyzer.TextAnalyzer
import com.example.typingtime.analyzer.TypingTimeCalculator
import com.example.typingtime.analyzer.WordCounter
import com.example.typingtime.data.ExportHelper
import com.example.typingtime.data.HistoryRepository
import com.example.typingtime.data.SettingsRepository
import com.example.typingtime.model.AppSettings
import com.example.typingtime.model.CalibrationResult
import com.example.typingtime.model.SessionFinishedSummary
import com.example.typingtime.model.SpeedUnit
import com.example.typingtime.model.TypingTimeHistory
import com.example.typingtime.model.TypingTimeUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class TypingTimeViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)
    private val historyRepo = HistoryRepository(application)

    private val _uiState = MutableStateFlow(TypingTimeUiState())
    val uiState: StateFlow<TypingTimeUiState> = _uiState.asStateFlow()

    private val _historyList = MutableStateFlow<List<TypingTimeHistory>>(emptyList())
    val historyList: StateFlow<List<TypingTimeHistory>> = _historyList.asStateFlow()

    private val _calibrationHistory = MutableStateFlow<List<CalibrationResult>>(emptyList())
    val calibrationHistory: StateFlow<List<CalibrationResult>> = _calibrationHistory.asStateFlow()

    private var analysisJob: Job? = null
    private var sessionTimerJob: Job? = null

    // Session state
    private var sessionStartWords = 0
    private var sessionStartChars = 0

    init {
        val loadedSettings = settingsRepo.loadSettings()
        val initialText = if (loadedSettings.restoreLastText) settingsRepo.loadLastText() else ""
        val initialSpeed = when (loadedSettings.estimationMethod) {
            SpeedUnit.WPM -> settingsRepo.getCalibratedSpeed() ?: loadedSettings.defaultWpm
            SpeedUnit.CPM -> loadedSettings.defaultCpm
        }

        _uiState.update {
            it.copy(
                text = initialText,
                settings = loadedSettings,
                speedUnit = loadedSettings.estimationMethod,
                typingSpeed = initialSpeed
            )
        }

        _historyList.value = historyRepo.loadHistory()
        _calibrationHistory.value = settingsRepo.loadCalibrationHistory()

        if (initialText.isNotEmpty()) {
            viewModelScope.launch {
                executeAnalysis(initialText, initialSpeed, loadedSettings.estimationMethod, loadedSettings)
            }
        }
    }

    fun onTextChanged(newText: String) {
        val currentSettings = _uiState.value.settings
        _uiState.update { it.copy(text = newText, errorMessage = null, infoMessage = null) }

        if (currentSettings.restoreLastText) {
            settingsRepo.saveLastText(newText)
        }

        if (currentSettings.liveAnalysis) {
            triggerLiveAnalysisDebounced(newText)
        }
    }

    private fun triggerLiveAnalysisDebounced(text: String) {
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            delay(300) // 300 ms debounce
            executeAnalysis(text, _uiState.value.typingSpeed, _uiState.value.speedUnit, _uiState.value.settings)
        }
    }

    fun estimateManual() {
        val text = _uiState.value.text
        if (text.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Paste or type some text first.") }
            return
        }
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            executeAnalysis(text, _uiState.value.typingSpeed, _uiState.value.speedUnit, _uiState.value.settings)
        }
    }

    private suspend fun executeAnalysis(
        text: String,
        speed: Int,
        unit: SpeedUnit,
        settings: AppSettings
    ) {
        _uiState.update { it.copy(isAnalyzing = true) }
        try {
            val result = TextAnalyzer.analyze(text, speed, unit, settings)
            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    wordCount = result.statistics.wordCount,
                    characterCount = result.statistics.characterCount,
                    characterCountWithoutSpaces = result.statistics.characterCountWithoutSpaces,
                    keystrokeCount = result.statistics.keystrokeCount,
                    sentenceCount = result.statistics.sentenceCount,
                    paragraphCount = result.statistics.paragraphCount,
                    lineCount = result.statistics.lineCount,
                    averageWordLength = result.statistics.averageWordLength,
                    averageWordsPerSentence = result.statistics.averageWordsPerSentence,
                    graphemeCount = result.statistics.graphemeCount,
                    codePointCount = result.statistics.codePointCount,
                    utf16Count = result.statistics.utf16Count,
                    estimatedSeconds = result.primaryEstimate.totalSeconds,
                    formattedEstimatedTime = result.primaryEstimate.formattedTime,
                    estimatedKpm = result.estimatedKpm,
                    paragraphBreakdowns = result.paragraphBreakdowns,
                    speedComparisons = result.speedComparisons
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isAnalyzing = false, errorMessage = "Analysis failed: ${e.message}") }
        }
    }

    fun setTypingSpeed(speed: Int) {
        val unit = _uiState.value.speedUnit
        if (unit == SpeedUnit.WPM) {
            if (speed !in 5..300) {
                _uiState.update { it.copy(errorMessage = "Enter a typing speed between 5 and 300 WPM.") }
                return
            }
        } else {
            if (speed !in 20..2000) {
                _uiState.update { it.copy(errorMessage = "Enter a typing speed between 20 and 2000 CPM.") }
                return
            }
        }

        _uiState.update { it.copy(typingSpeed = speed, errorMessage = null) }
        triggerLiveAnalysisDebounced(_uiState.value.text)
    }

    fun setSpeedUnit(unit: SpeedUnit) {
        val newSpeed = when (unit) {
            SpeedUnit.WPM -> _uiState.value.settings.defaultWpm
            SpeedUnit.CPM -> _uiState.value.settings.defaultCpm
        }
        _uiState.update { it.copy(speedUnit = unit, typingSpeed = newSpeed, errorMessage = null) }
        triggerLiveAnalysisDebounced(_uiState.value.text)
    }

    fun loadSampleText(text: String) {
        onTextChanged(text)
        estimateManual()
    }

    fun clearText() {
        onTextChanged("")
    }

    // --- Typing Session Timer ---
    fun startTypingSession() {
        val currentWords = _uiState.value.wordCount
        val currentChars = _uiState.value.characterCount
        sessionStartWords = currentWords
        sessionStartChars = currentChars

        _uiState.update {
            it.copy(
                isTypingSessionActive = true,
                isTypingSessionPaused = false,
                elapsedSessionSeconds = 0,
                currentWpm = null,
                sessionWordsTyped = 0,
                sessionFinishedSummary = null
            )
        }

        startSessionTicker()
    }

    fun pauseTypingSession() {
        sessionTimerJob?.cancel()
        _uiState.update { it.copy(isTypingSessionPaused = true) }
    }

    fun resumeTypingSession() {
        _uiState.update { it.copy(isTypingSessionPaused = false) }
        startSessionTicker()
    }

    fun stopTypingSession() {
        sessionTimerJob?.cancel()
        val activeSeconds = _uiState.value.elapsedSessionSeconds
        val totalWordsNow = _uiState.value.wordCount
        val totalCharsNow = _uiState.value.characterCount
        val wordsTyped = (totalWordsNow - sessionStartWords).coerceAtLeast(0)
        val charsTyped = (totalCharsNow - sessionStartChars).coerceAtLeast(0)

        val activeMinutes = activeSeconds.toDouble() / 60.0
        val avgWpm = if (activeMinutes > 0.0) wordsTyped.toDouble() / activeMinutes else 0.0

        val estimatedSeconds = _uiState.value.estimatedSeconds
        val diffSeconds = activeSeconds - estimatedSeconds
        val formattedDiff = TypingTimeCalculator.formatDifference(diffSeconds)

        val summary = SessionFinishedSummary(
            activeSeconds = activeSeconds,
            wordCount = wordsTyped,
            characterCount = charsTyped,
            averageWpm = avgWpm,
            estimatedSecondsForText = estimatedSeconds,
            formattedActiveTime = TypingTimeCalculator.formatTime(activeSeconds, detailed = true),
            formattedEstimatedTime = _uiState.value.formattedEstimatedTime,
            differenceSeconds = diffSeconds,
            formattedDifference = formattedDiff
        )

        _uiState.update {
            it.copy(
                isTypingSessionActive = false,
                isTypingSessionPaused = false,
                sessionFinishedSummary = summary
            )
        }
    }

    fun dismissSessionSummary() {
        _uiState.update { it.copy(sessionFinishedSummary = null) }
    }

    private fun startSessionTicker() {
        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (!_uiState.value.isTypingSessionPaused) {
                    val newElapsed = _uiState.value.elapsedSessionSeconds + 1
                    val currentWords = _uiState.value.wordCount
                    val wordsTyped = (currentWords - sessionStartWords).coerceAtLeast(0)

                    // Wait until at least 5 words or 10 seconds to avoid unstable readings
                    val liveWpm = if (wordsTyped >= 5 || newElapsed >= 10) {
                        val minutes = newElapsed.toDouble() / 60.0
                        if (minutes > 0.0) (wordsTyped.toDouble() / minutes) else null
                    } else null

                    _uiState.update {
                        it.copy(
                            elapsedSessionSeconds = newElapsed,
                            sessionWordsTyped = wordsTyped,
                            currentWpm = liveWpm
                        )
                    }
                }
            }
        }
    }

    // --- History Management ---
    fun saveToHistory() {
        val state = _uiState.value
        if (state.text.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Cannot save empty text to history.") }
            return
        }

        val entry = TypingTimeHistory(
            id = UUID.randomUUID().toString(),
            text = state.text,
            analyzedAt = System.currentTimeMillis(),
            speedUnit = state.speedUnit,
            typingSpeed = state.typingSpeed,
            countNumbers = state.settings.countNumbers,
            hyphenMode = state.settings.hyphenMode.name,
            apostropheMode = state.settings.apostropheMode.name,
            keystrokeMode = state.settings.keystrokeMode.name,
            wordCount = state.wordCount,
            characterCount = state.characterCount,
            estimatedSeconds = state.estimatedSeconds,
            formattedTime = state.formattedEstimatedTime
        )

        val saved = historyRepo.saveHistory(entry)
        _historyList.value = historyRepo.loadHistory()

        if (saved) {
            _uiState.update { it.copy(infoMessage = "Saved to history") }
        } else {
            _uiState.update { it.copy(infoMessage = "Already saved in history") }
        }
    }

    fun restoreHistory(entry: TypingTimeHistory) {
        val restoredHyphen = try {
            com.example.typingtime.model.HyphenMode.valueOf(entry.hyphenMode)
        } catch (e: Exception) { com.example.typingtime.model.HyphenMode.KEEP_AS_ONE }

        val restoredApostrophe = try {
            com.example.typingtime.model.ApostropheMode.valueOf(entry.apostropheMode)
        } catch (e: Exception) { com.example.typingtime.model.ApostropheMode.KEEP_AS_ONE }

        val restoredKeystroke = try {
            com.example.typingtime.model.KeystrokeMode.valueOf(entry.keystrokeMode)
        } catch (e: Exception) { com.example.typingtime.model.KeystrokeMode.CHARACTER_BASED }

        val updatedSettings = _uiState.value.settings.copy(
            countNumbers = entry.countNumbers,
            hyphenMode = restoredHyphen,
            apostropheMode = restoredApostrophe,
            keystrokeMode = restoredKeystroke
        )

        _uiState.update {
            it.copy(
                text = entry.text,
                speedUnit = entry.speedUnit,
                typingSpeed = entry.typingSpeed,
                settings = updatedSettings,
                infoMessage = "Restored from history"
            )
        }

        executeAnalysisManual()
    }

    private fun executeAnalysisManual() {
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            val state = _uiState.value
            executeAnalysis(state.text, state.typingSpeed, state.speedUnit, state.settings)
        }
    }

    fun deleteHistoryItem(id: String) {
        historyRepo.deleteHistory(id)
        _historyList.value = historyRepo.loadHistory()
    }

    fun clearAllHistory() {
        historyRepo.clearHistory()
        _historyList.value = emptyList()
    }

    // --- Calibration ---
    fun finishCalibration(
        sampleText: String,
        typedText: String,
        activeSeconds: Long
    ): CalibrationResult {
        val totalChars = typedText.length
        var correctChars = 0
        var errors = 0

        for (i in typedText.indices) {
            if (i < sampleText.length && typedText[i] == sampleText[i]) {
                correctChars++
            } else {
                errors++
            }
        }

        val accuracy = if (totalChars > 0) {
            (correctChars.toDouble() / totalChars.toDouble()) * 100.0
        } else 0.0

        val minutes = activeSeconds.toDouble() / 60.0
        // Words based on correct words or standard 5-chars-per-word
        val sampleWords = WordCounter.extractWords(sampleText)
        val typedWords = WordCounter.extractWords(typedText)
        var correctWords = 0
        for (i in typedWords.indices) {
            if (i < sampleWords.size && typedWords[i].equals(sampleWords[i], ignoreCase = false)) {
                correctWords++
            }
        }

        val wpm = if (minutes > 0.0) {
            (correctWords.toDouble() / minutes).toInt().coerceAtLeast(5)
        } else 45

        val result = CalibrationResult(
            id = UUID.randomUUID().toString(),
            wpm = wpm,
            accuracy = accuracy,
            errors = errors,
            totalCharacters = totalChars,
            activeSeconds = activeSeconds,
            formattedTime = TypingTimeCalculator.formatTime(activeSeconds, detailed = true),
            timestamp = System.currentTimeMillis()
        )

        settingsRepo.saveCalibrationResult(result)
        _calibrationHistory.value = settingsRepo.loadCalibrationHistory()
        return result
    }

    fun applyCalibratedSpeed(speed: Int) {
        setTypingSpeed(speed)
        _uiState.update { it.copy(infoMessage = "Applied calibrated speed: $speed WPM") }
    }

    fun deleteCalibration(id: String) {
        settingsRepo.deleteCalibrationResult(id)
        _calibrationHistory.value = settingsRepo.loadCalibrationHistory()
    }

    fun resetCalibratedSpeed() {
        settingsRepo.clearCalibratedSpeed()
        setTypingSpeed(_uiState.value.settings.defaultWpm)
        _uiState.update { it.copy(infoMessage = "Reset to default speed") }
    }

    // --- Settings Updates ---
    fun updateSettings(newSettings: AppSettings) {
        settingsRepo.saveSettings(newSettings)
        _uiState.update { it.copy(settings = newSettings) }
        triggerLiveAnalysisDebounced(_uiState.value.text)
    }

    fun resetSettingsToDefaults() {
        val defaults = settingsRepo.resetToDefaults()
        _uiState.update {
            it.copy(
                settings = defaults,
                speedUnit = defaults.estimationMethod,
                typingSpeed = defaults.defaultWpm,
                infoMessage = "Settings restored to defaults"
            )
        }
        triggerLiveAnalysisDebounced(_uiState.value.text)
    }

    // --- Copy / Share / Export ---
    fun getSummaryText(): String {
        val state = _uiState.value
        val result = TextAnalyzer.FullAnalysisResult(
            statistics = com.example.typingtime.model.TextStatistics(
                wordCount = state.wordCount,
                characterCount = state.characterCount,
                characterCountWithoutSpaces = state.characterCountWithoutSpaces,
                keystrokeCount = state.keystrokeCount,
                sentenceCount = state.sentenceCount,
                paragraphCount = state.paragraphCount,
                lineCount = state.lineCount,
                averageWordLength = state.averageWordLength,
                averageWordsPerSentence = state.averageWordsPerSentence
            ),
            primaryEstimate = com.example.typingtime.model.TypingTimeEstimate(
                speed = state.typingSpeed,
                speedUnit = state.speedUnit,
                totalSeconds = state.estimatedSeconds,
                formattedTime = state.formattedEstimatedTime
            ),
            estimatedKpm = state.estimatedKpm,
            paragraphBreakdowns = state.paragraphBreakdowns,
            speedComparisons = state.speedComparisons
        )
        return ExportHelper.generateSummaryText(result, state.typingSpeed, state.speedUnit)
    }

    fun getCsvContent(): String {
        val state = _uiState.value
        val result = TextAnalyzer.FullAnalysisResult(
            statistics = com.example.typingtime.model.TextStatistics(
                wordCount = state.wordCount,
                characterCount = state.characterCount,
                characterCountWithoutSpaces = state.characterCountWithoutSpaces,
                keystrokeCount = state.keystrokeCount,
                sentenceCount = state.sentenceCount,
                paragraphCount = state.paragraphCount,
                lineCount = state.lineCount,
                averageWordLength = state.averageWordLength,
                averageWordsPerSentence = state.averageWordsPerSentence
            ),
            primaryEstimate = com.example.typingtime.model.TypingTimeEstimate(
                speed = state.typingSpeed,
                speedUnit = state.speedUnit,
                totalSeconds = state.estimatedSeconds,
                formattedTime = state.formattedEstimatedTime
            ),
            estimatedKpm = state.estimatedKpm,
            paragraphBreakdowns = state.paragraphBreakdowns,
            speedComparisons = state.speedComparisons
        )
        return ExportHelper.generateCsv(result, state.typingSpeed, state.speedUnit)
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }
}
