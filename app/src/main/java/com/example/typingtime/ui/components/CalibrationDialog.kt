package com.example.typingtime.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.typingtime.data.SamplePassages
import com.example.typingtime.model.CalibrationResult
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CalibrationDialog(
    calibratedSpeed: Int?,
    calibrationHistory: List<CalibrationResult>,
    onFinishCalibration: (sampleText: String, typedText: String, activeSeconds: Long) -> CalibrationResult,
    onApplySpeed: (Int) -> Unit,
    onDeleteCalibration: (String) -> Unit,
    onResetCalibratedSpeed: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Test, 1: History & Profile

    val calibrationSamples = remember {
        SamplePassages.SAMPLES.filter { it.category == "Calibration" || it.title == "Quick Message" }
    }
    var selectedSampleIndex by remember { mutableStateOf(0) }
    val currentSample = calibrationSamples[selectedSampleIndex]

    // Test state
    var isTesting by remember { mutableStateOf(false) }
    var typedText by remember { mutableStateOf("") }
    var testElapsedSeconds by remember { mutableStateOf(0L) }
    var latestResult by remember { mutableStateOf<CalibrationResult?>(null) }

    LaunchedEffect(isTesting) {
        if (isTesting) {
            testElapsedSeconds = 0L
            while (isTesting) {
                delay(1000)
                testElapsedSeconds++
            }
        }
    }

    Dialog(
        onDismissRequest = {
            isTesting = false
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CALIBRATE MY SPEED",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = {
                        isTesting = false
                        onDismiss()
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Typing Test", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("History & Profile", fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // TAB 0: Typing Test
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Sample Selector
                        item {
                            Text(
                                text = "CHOOSE SAMPLE PASSAGE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                calibrationSamples.forEachIndexed { index, sample ->
                                    FilterChip(
                                        selected = selectedSampleIndex == index,
                                        onClick = {
                                            if (!isTesting) {
                                                selectedSampleIndex = index
                                                typedText = ""
                                                latestResult = null
                                            }
                                        },
                                        label = { Text(sample.title) },
                                        enabled = !isTesting
                                    )
                                }
                            }
                        }

                        // Sample Reference Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "SAMPLE TEXT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = currentSample.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Typing Input
                        item {
                            OutlinedTextField(
                                value = typedText,
                                onValueChange = { newVal ->
                                    if (!isTesting && newVal.isNotEmpty()) {
                                        isTesting = true
                                    }
                                    typedText = newVal
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                placeholder = { Text("Start typing the sample text here...") },
                                label = { Text("Your Typing") }
                            )
                        }

                        // Status & Timer
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isTesting) "Time: ${testElapsedSeconds}s" else "Time: 0s",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = "${typedText.length} / ${currentSample.text.length} chars",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Finish / Start Buttons
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (isTesting) {
                                    Button(
                                        onClick = {
                                            isTesting = false
                                            val res = onFinishCalibration(currentSample.text, typedText, testElapsedSeconds)
                                            latestResult = res
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("FINISH TEST")
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            typedText = ""
                                            testElapsedSeconds = 0L
                                            latestResult = null
                                            isTesting = true
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("START TYPING TEST")
                                    }
                                }
                            }
                        }

                        // Results Display
                        latestResult?.let { res ->
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "YOUR TYPING SPEED",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${res.wpm} WPM",
                                            style = MaterialTheme.typography.displayMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Accuracy", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(String.format("%.1f%%", res.accuracy), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            }
                                            Column {
                                                Text("Errors", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("${res.errors}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            }
                                            Column {
                                                Text("Time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(res.formattedTime, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = {
                                                onApplySpeed(res.wpm)
                                                onDismiss()
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("USE THIS SPEED (${res.wpm} WPM)")
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: History & Speed Profile
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Current Profile
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "MY TYPING SPEED PROFILE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (calibratedSpeed != null) "$calibratedSpeed WPM" else "Not Calibrated (Default: 45 WPM)",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (calibratedSpeed != null) "Based on your latest calibration test." else "Perform a typing test to calibrate your personal speed.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (calibratedSpeed != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedButton(
                                            onClick = onResetCalibratedSpeed,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Reset Profile to Default")
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "CALIBRATION HISTORY (LAST 5)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (calibrationHistory.isEmpty()) {
                            item {
                                Text(
                                    text = "No calibration tests recorded yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(calibrationHistory) { item ->
                                val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(item.timestamp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${item.wpm} WPM",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Accuracy: ${String.format("%.1f%%", item.accuracy)} • Time: ${item.formattedTime}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = dateStr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { onApplySpeed(item.wpm); onDismiss() }) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = "Use speed")
                                            }
                                            IconButton(onClick = { onDeleteCalibration(item.id) }) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
