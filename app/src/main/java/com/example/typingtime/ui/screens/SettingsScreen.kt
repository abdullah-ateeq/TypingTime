package com.example.typingtime.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.typingtime.model.ApostropheMode
import com.example.typingtime.model.CharacterCountUnit
import com.example.typingtime.model.HyphenMode
import com.example.typingtime.model.KeystrokeMode
import com.example.typingtime.model.SpeedUnit
import com.example.typingtime.viewmodel.TypingTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TypingTimeViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val settings = state.settings

    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // APPEARANCE
            item {
                SettingsSectionCard(title = "APPEARANCE") {
                    Text("Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("System", "Light", "Dark").forEach { themeName ->
                            FilterChip(
                                selected = settings.theme == themeName,
                                onClick = { viewModel.updateSettings(settings.copy(theme = themeName)) },
                                label = { Text(themeName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingSwitchRow(
                        title = "Dynamic Color (Material You)",
                        subtitle = "Uses system wallpaper colors on Android 12+",
                        checked = settings.dynamicColor,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(dynamicColor = it)) }
                    )
                }
            }

            // ESTIMATION METHOD & TYPING SETTINGS
            item {
                SettingsSectionCard(title = "ESTIMATION & SPEED") {
                    Text("Primary Estimation Method", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.estimationMethod == SpeedUnit.WPM,
                            onClick = { viewModel.updateSettings(settings.copy(estimationMethod = SpeedUnit.WPM)) },
                            label = { Text("Words Per Minute (WPM)") }
                        )
                        FilterChip(
                            selected = settings.estimationMethod == SpeedUnit.CPM,
                            onClick = { viewModel.updateSettings(settings.copy(estimationMethod = SpeedUnit.CPM)) },
                            label = { Text("Characters (CPM)") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingSwitchRow(
                        title = "Detailed Time Format",
                        subtitle = "Show '8 min 24 sec' instead of '8m 24s'",
                        checked = settings.timeDisplayDetailed,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(timeDisplayDetailed = it)) }
                    )
                }
            }

            // TEXT PARSING & TOKENIZATION RULES
            item {
                SettingsSectionCard(title = "TEXT PARSING RULES") {
                    SettingSwitchRow(
                        title = "Count Numbers",
                        subtitle = "Include standalone digits, decimals, and versions in word count",
                        checked = settings.countNumbers,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(countNumbers = it)) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Hyphen Handling (e.g. well-known)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.hyphenMode == HyphenMode.KEEP_AS_ONE,
                            onClick = { viewModel.updateSettings(settings.copy(hyphenMode = HyphenMode.KEEP_AS_ONE)) },
                            label = { Text("Keep as one word") }
                        )
                        FilterChip(
                            selected = settings.hyphenMode == HyphenMode.SPLIT,
                            onClick = { viewModel.updateSettings(settings.copy(hyphenMode = HyphenMode.SPLIT)) },
                            label = { Text("Split into words") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Apostrophe Handling (e.g. don't)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.apostropheMode == ApostropheMode.KEEP_AS_ONE,
                            onClick = { viewModel.updateSettings(settings.copy(apostropheMode = ApostropheMode.KEEP_AS_ONE)) },
                            label = { Text("Keep as one word") }
                        )
                        FilterChip(
                            selected = settings.apostropheMode == ApostropheMode.SPLIT,
                            onClick = { viewModel.updateSettings(settings.copy(apostropheMode = ApostropheMode.SPLIT)) },
                            label = { Text("Split into words") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingSwitchRow(
                        title = "URL Handling",
                        subtitle = "Treat full URLs (https://...) as single word tokens",
                        checked = settings.urlHandling,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(urlHandling = it)) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingSwitchRow(
                        title = "Email Handling",
                        subtitle = "Treat email addresses as single word tokens",
                        checked = settings.emailHandling,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(emailHandling = it)) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Character Count Metric", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.characterCountUnit == CharacterCountUnit.GRAPHEME,
                            onClick = { viewModel.updateSettings(settings.copy(characterCountUnit = CharacterCountUnit.GRAPHEME)) },
                            label = { Text("Graphemes") }
                        )
                        FilterChip(
                            selected = settings.characterCountUnit == CharacterCountUnit.CODE_POINTS,
                            onClick = { viewModel.updateSettings(settings.copy(characterCountUnit = CharacterCountUnit.CODE_POINTS)) },
                            label = { Text("Code Points") }
                        )
                        FilterChip(
                            selected = settings.characterCountUnit == CharacterCountUnit.UTF16,
                            onClick = { viewModel.updateSettings(settings.copy(characterCountUnit = CharacterCountUnit.UTF16)) },
                            label = { Text("UTF-16 Units") }
                        )
                    }
                }
            }

            // KEYSTROKE ESTIMATION
            item {
                SettingsSectionCard(title = "KEYSTROKE ESTIMATION") {
                    Text("Keystroke Model", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.keystrokeMode == KeystrokeMode.CHARACTER_BASED,
                            onClick = { viewModel.updateSettings(settings.copy(keystrokeMode = KeystrokeMode.CHARACTER_BASED)) },
                            label = { Text("Character-based") }
                        )
                        FilterChip(
                            selected = settings.keystrokeMode == KeystrokeMode.ADVANCED_KEY_MODEL,
                            onClick = { viewModel.updateSettings(settings.copy(keystrokeMode = KeystrokeMode.ADVANCED_KEY_MODEL)) },
                            label = { Text("Advanced Key Model") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingSwitchRow(
                        title = "Shift-Key Accounting",
                        subtitle = "Count Shift + Letter / Symbol as 2 keypresses",
                        checked = settings.advancedShiftCounting,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(advancedShiftCounting = it)) }
                    )
                }
            }

            // SESSION & BEHAVIOR
            item {
                SettingsSectionCard(title = "TYPING SESSION & BEHAVIOR") {
                    SettingSwitchRow(
                        title = "Live Typing Timer Feature",
                        subtitle = "Enable local typing session timer and speed metrics",
                        checked = settings.typingSessionEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(typingSessionEnabled = it)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingSwitchRow(
                        title = "Live WPM Calculation",
                        subtitle = "Show current WPM during active typing",
                        checked = settings.liveWpm,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(liveWpm = it)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingSwitchRow(
                        title = "Actual vs Estimated Comparison",
                        subtitle = "Compare completed typing duration with initial estimate",
                        checked = settings.actualVsEstimated,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(actualVsEstimated = it)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingSwitchRow(
                        title = "Live Analysis",
                        subtitle = "Automatically analyze passages as you type (300ms debounce)",
                        checked = settings.liveAnalysis,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(liveAnalysis = it)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingSwitchRow(
                        title = "Confirm Delete",
                        subtitle = "Show confirmation dialog before clearing all history",
                        checked = settings.confirmDelete,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(confirmDelete = it)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingSwitchRow(
                        title = "Restore Last Text",
                        subtitle = "Automatically restore passage on application launch",
                        checked = settings.restoreLastText,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(restoreLastText = it)) }
                    )
                }
            }

            // RESET SETTINGS
            item {
                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore Default Settings", fontWeight = FontWeight.Bold)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Settings?") },
            text = { Text("This will restore all configuration defaults. Your saved history will NOT be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSettingsToDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset to Defaults", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
