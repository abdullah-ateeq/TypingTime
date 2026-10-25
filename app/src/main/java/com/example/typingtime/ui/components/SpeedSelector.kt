package com.example.typingtime.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.typingtime.model.AppSettings
import com.example.typingtime.model.SpeedUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeedSelector(
    currentSpeed: Int,
    currentUnit: SpeedUnit,
    settings: AppSettings,
    onSpeedChanged: (Int) -> Unit,
    onUnitChanged: (SpeedUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isEditingExact by remember { mutableStateOf(false) }
    var exactInputText by remember(currentSpeed) { mutableStateOf(currentSpeed.toString()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Speed and Unit Tab
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TYPING SPEED",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // WPM / CPM toggle
                TabRow(
                    selectedTabIndex = if (currentUnit == SpeedUnit.WPM) 0 else 1,
                    modifier = Modifier
                        .width(160.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Tab(
                        selected = currentUnit == SpeedUnit.WPM,
                        onClick = { onUnitChanged(SpeedUnit.WPM) },
                        text = { Text("WPM", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = currentUnit == SpeedUnit.CPM,
                        onClick = { onUnitChanged(SpeedUnit.CPM) },
                        text = { Text("CPM", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current speed display & exact number input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$currentSpeed ${currentUnit.name}",
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // Speed conversion reference
                    val converted = if (currentUnit == SpeedUnit.WPM) {
                        "${currentSpeed * 5} CPM (approx)"
                    } else {
                        "${(currentSpeed / 5.0).toInt()} WPM (approx)"
                    }
                    Text(
                        text = "≈ $converted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Exact numeric input
                OutlinedTextField(
                    value = exactInputText,
                    onValueChange = { newVal ->
                        val filtered = newVal.filter { it.isDigit() }
                        exactInputText = filtered
                        val parsed = filtered.toIntOrNull()
                        if (parsed != null && parsed in currentUnit.minSpeed..currentUnit.maxSpeed) {
                            onSpeedChanged(parsed)
                        }
                    },
                    label = { Text("Exact") },
                    modifier = Modifier.width(100.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Slider
            val minSlider = currentUnit.minSpeed.toFloat()
            val maxSlider = currentUnit.maxSpeed.toFloat()
            Slider(
                value = currentSpeed.toFloat().coerceIn(minSlider, maxSlider),
                onValueChange = { onSpeedChanged(it.toInt()) },
                valueRange = minSlider..maxSlider,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Slower (${currentUnit.minSpeed})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Faster (${currentUnit.maxSpeed})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Presets row
            Text(
                text = "SPEED PRESETS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (currentUnit == SpeedUnit.WPM) {
                val presets = listOf(
                    "Beginner" to settings.presetBeginner,
                    "Casual" to settings.presetCasual,
                    "Average" to settings.presetAverage,
                    "Fast" to settings.presetFast,
                    "Expert" to settings.presetExpert
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.forEach { (label, speedVal) ->
                        val isSelected = currentSpeed == speedVal
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onSpeedChanged(speedVal)
                                exactInputText = speedVal.toString()
                            },
                            label = { Text("$label ($speedVal)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            } else {
                val cpmPresets = listOf(
                    "Slow" to 100,
                    "Casual" to 150,
                    "Average" to 225,
                    "Fast" to 300,
                    "Expert" to 400
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    cpmPresets.forEach { (label, speedVal) ->
                        val isSelected = currentSpeed == speedVal
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onSpeedChanged(speedVal)
                                exactInputText = speedVal.toString()
                            },
                            label = { Text("$label ($speedVal)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Content Type Presets (Optional starting points)
            Text(
                text = "CONTENT TYPE PRESETS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            val contentPresets = listOf(
                "General" to 45,
                "Technical" to 35,
                "Code/Symbols" to 25,
                "Casual" to 50
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                contentPresets.forEach { (label, speedVal) ->
                    val isSelected = currentUnit == SpeedUnit.WPM && currentSpeed == speedVal
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (currentUnit != SpeedUnit.WPM) onUnitChanged(SpeedUnit.WPM)
                            onSpeedChanged(speedVal)
                            exactInputText = speedVal.toString()
                        },
                        label = { Text("$label: $speedVal WPM") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Explanatory note
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "Typing speed is measured in words per minute (WPM). Higher WPM produces a shorter estimated typing time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
