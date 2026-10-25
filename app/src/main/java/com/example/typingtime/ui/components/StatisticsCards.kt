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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.typingtime.model.TypingTimeUiState
import java.text.NumberFormat

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatisticsCards(
    state: TypingTimeUiState,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getIntegerInstance()

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TEXT STATISTICS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Stat Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    title = "WORDS",
                    value = numberFormat.format(state.wordCount),
                    icon = Icons.Default.TextFields,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    title = "CHARACTERS",
                    value = numberFormat.format(state.characterCount),
                    icon = Icons.Default.ShortText,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    title = "KEYSTROKES",
                    value = numberFormat.format(state.keystrokeCount),
                    icon = Icons.Default.Keyboard,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    title = "SENTENCES",
                    value = numberFormat.format(state.sentenceCount),
                    icon = Icons.Default.FormatListNumbered,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    title = "PARAGRAPHS",
                    value = numberFormat.format(state.paragraphCount),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    title = "LINES",
                    value = numberFormat.format(state.lineCount),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(14.dp))

            // Detailed Character metrics
            Text(
                text = "CHARACTER METRICS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            DetailRow(label = "Characters including spaces", value = numberFormat.format(state.characterCount))
            DetailRow(label = "Characters excluding spaces", value = numberFormat.format(state.characterCountWithoutSpaces))
            DetailRow(label = "Grapheme Clusters", value = numberFormat.format(state.graphemeCount))
            DetailRow(label = "Unicode Code Points", value = numberFormat.format(state.codePointCount))
            DetailRow(label = "UTF-16 Code Units", value = numberFormat.format(state.utf16Count))

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Averages & Cadence
            Text(
                text = "AVERAGES & ESTIMATED CADENCE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            DetailRow(
                label = "Average word length",
                value = if (state.averageWordLength != null) "${state.averageWordLength} characters" else "—"
            )
            DetailRow(
                label = "Average characters per word",
                value = if (state.wordCount > 0) {
                    val avgChars = state.characterCountWithoutSpaces.toDouble() / state.wordCount.toDouble()
                    String.format("%.2f", avgChars)
                } else "—"
            )
            DetailRow(
                label = "Average words per sentence",
                value = if (state.averageWordsPerSentence != null) "${state.averageWordsPerSentence}" else "—"
            )
            DetailRow(
                label = "Estimated KPM (keystrokes/min)",
                value = if (state.estimatedKpm > 0) numberFormat.format(state.estimatedKpm) else "—"
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Estimated keystrokes include characters, spaces, and common punctuation/symbols.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun StatTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.height(16.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
