package com.example.typingtime.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.typingtime.data.SamplePassages
import com.example.typingtime.model.SpeedUnit
import com.example.typingtime.ui.components.CalibrationDialog
import com.example.typingtime.ui.components.ParagraphBreakdownList
import com.example.typingtime.ui.components.SpeedComparisonTable
import com.example.typingtime.ui.components.SpeedSelector
import com.example.typingtime.ui.components.StatisticsCards
import com.example.typingtime.ui.components.TypingSessionCard
import com.example.typingtime.viewmodel.TypingTimeViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: TypingTimeViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val calibrationHistory by viewModel.calibrationHistory.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCalibrationDialog by remember { mutableStateOf(false) }
    var showSampleMenu by remember { mutableStateOf(false) }
    var showHowCalculated by remember { mutableStateOf(false) }

    // Launcher for CSV export saving
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(viewModel.getCsvContent().toByteArray(Charsets.UTF_8))
                }
                scope.launch {
                    snackbarHostState.showSnackbar("CSV exported successfully")
                }
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Failed to export CSV: ${e.message}")
                }
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.infoMessage) {
        state.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TypingTime",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Estimate how long it takes to type a passage.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCalibrationDialog = true },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Calibrate My Speed"
                        )
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "About")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Text Input Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TEXT PASSAGE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row {
                                // Paste button
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val item = clipboard.primaryClip?.getItemAt(0)
                                        val pasteText = item?.text?.toString() ?: ""
                                        if (pasteText.isNotEmpty()) {
                                            viewModel.onTextChanged(pasteText)
                                        }
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.height(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Paste", fontSize = 12.sp)
                                }

                                // Load Sample dropdown
                                Box {
                                    TextButton(onClick = { showSampleMenu = true }) {
                                        Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.height(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Samples", fontSize = 12.sp)
                                    }
                                    DropdownMenu(
                                        expanded = showSampleMenu,
                                        onDismissRequest = { showSampleMenu = false }
                                    ) {
                                        SamplePassages.SAMPLES.forEach { sample ->
                                            DropdownMenuItem(
                                                text = { Text("${sample.title} (${sample.category})") },
                                                onClick = {
                                                    viewModel.loadSampleText(sample.text)
                                                    showSampleMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                if (state.text.isNotEmpty()) {
                                    TextButton(onClick = { viewModel.clearText() }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.height(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Clear", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Large multiline text input area
                        OutlinedTextField(
                            value = state.text,
                            onValueChange = { viewModel.onTextChanged(it) },
                            placeholder = { Text("Paste or type your passage here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live lightweight statistics
                        val nf = NumberFormat.getIntegerInstance()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Words: ${nf.format(state.wordCount)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Chars: ${nf.format(state.characterCount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Lines: ${nf.format(state.lineCount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Sentences: ${nf.format(state.sentenceCount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Prominent ESTIMATE TYPING TIME Button
            item {
                Button(
                    onClick = { viewModel.estimateManual() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ESTIMATE TYPING TIME",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Typing Speed Selector
            item {
                SpeedSelector(
                    currentSpeed = state.typingSpeed,
                    currentUnit = state.speedUnit,
                    settings = state.settings,
                    onSpeedChanged = { viewModel.setTypingSpeed(it) },
                    onUnitChanged = { viewModel.setSpeedUnit(it) }
                )
            }

            // Primary Estimate Dashboard (only displayed prominently when text is present)
            if (state.wordCount > 0 || state.text.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ESTIMATED TYPING TIME",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = state.formattedEstimatedTime,
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp),
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${state.typingSpeed} ${if (state.speedUnit == SpeedUnit.WPM) "words / minute" else "characters / minute"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (state.estimatedKpm > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Estimated KPM: ${state.estimatedKpm} keystrokes/minute",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Action Bar: Copy, Save, Share, Export CSV
                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Copy Summary
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("TypingTime Summary", viewModel.getSummaryText())
                                clipboard.setPrimaryClip(clip)
                                scope.launch { snackbarHostState.showSnackbar("Summary copied to clipboard") }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy", fontSize = 13.sp)
                        }

                        // Save to History
                        OutlinedButton(
                            onClick = { viewModel.saveToHistory() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save", fontSize = 13.sp)
                        }

                        // Share
                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "TypingTime Estimate")
                                    putExtra(Intent.EXTRA_TEXT, viewModel.getSummaryText())
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share TypingTime Summary"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", fontSize = 13.sp)
                        }

                        // Export CSV
                        OutlinedButton(
                            onClick = {
                                exportCsvLauncher.launch("typing_time_analysis.csv")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CSV", fontSize = 13.sp)
                        }
                    }
                }

                // Text Statistics Cards
                item {
                    StatisticsCards(state = state)
                }

                // Typing Progress Visualizer (Informational)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "TYPING PROGRESS VISUALIZER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { 1.0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("0%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Estimated: ${state.formattedEstimatedTime}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                Text("100%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Speed Comparison Table
                if (state.speedComparisons.isNotEmpty()) {
                    item {
                        SpeedComparisonTable(
                            comparisons = state.speedComparisons,
                            userSpeed = state.typingSpeed
                        )
                    }
                }

                // Paragraph Breakdown
                if (state.paragraphBreakdowns.size > 1) {
                    item {
                        ParagraphBreakdownList(breakdowns = state.paragraphBreakdowns)
                    }
                }

                // Optional Typing Session Controls
                if (state.settings.typingSessionEnabled) {
                    item {
                        if (!state.isTypingSessionActive && state.sessionFinishedSummary == null) {
                            OutlinedButton(
                                onClick = { viewModel.startTypingSession() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Typing Session (Live Timer)")
                            }
                        }
                    }

                    item {
                        TypingSessionCard(
                            state = state,
                            onStartSession = { viewModel.startTypingSession() },
                            onPauseSession = { viewModel.pauseTypingSession() },
                            onResumeSession = { viewModel.resumeTypingSession() },
                            onStopSession = { viewModel.stopTypingSession() },
                            onDismissSummary = { viewModel.dismissSessionSummary() }
                        )
                    }
                }

                // How is this calculated? (Expandable)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showHowCalculated = !showHowCalculated },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "HOW IS THIS CALCULATED?",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(
                                    imageVector = if (showHowCalculated) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }

                            AnimatedVisibility(visible = showHowCalculated) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    if (state.speedUnit == SpeedUnit.WPM) {
                                        Text(
                                            text = "Typing time is estimated using:\n\nWord Count ÷ Typing Speed (WPM)\n\nFor example:\n${state.wordCount} words ÷ ${state.typingSpeed} WPM\n= ${String.format("%.2f", state.wordCount.toDouble() / state.typingSpeed)} minutes\n≈ ${state.formattedEstimatedTime}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Text(
                                            text = "Typing time is estimated using:\n\nCharacter Count ÷ Characters Per Minute (CPM)\n\nFor example:\n${state.characterCount} chars ÷ ${state.typingSpeed} CPM\n= ${String.format("%.2f", state.characterCount.toDouble() / state.typingSpeed)} minutes\n≈ ${state.formattedEstimatedTime}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "ACCURACY DISCLAIMER:\nTyping time is an estimate. Actual time varies with typing speed, text complexity, punctuation, symbols, keyboard layout, corrections, and pauses.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Speed Calibration Dialog
    if (showCalibrationDialog) {
        CalibrationDialog(
            calibratedSpeed = viewModel.uiState.value.settings.defaultWpm,
            calibrationHistory = calibrationHistory,
            onFinishCalibration = { sText, tText, activeSecs ->
                viewModel.finishCalibration(sText, tText, activeSecs)
            },
            onApplySpeed = { speed ->
                viewModel.applyCalibratedSpeed(speed)
            },
            onDeleteCalibration = { id ->
                viewModel.deleteCalibration(id)
            },
            onResetCalibratedSpeed = {
                viewModel.resetCalibratedSpeed()
            },
            onDismiss = { showCalibrationDialog = false }
        )
    }
}
