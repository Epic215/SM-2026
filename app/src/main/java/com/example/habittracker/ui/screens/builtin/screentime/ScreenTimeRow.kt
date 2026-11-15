package com.example.habittracker.ui.screens.builtin.screentime

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.habittracker.data.model.Habit
import com.example.habittracker.domain.builtin.DayOutcome
import com.example.habittracker.domain.builtin.ScreenTimeSettings
import com.example.habittracker.ui.screens.habits.BuiltInRowState

@Composable
fun ScreenTimeRow(
    row: BuiltInRowState,
    hasUsageAccess: Boolean,
    onOpenUsageAccess: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings = remember(row.settings) { ScreenTimeSettings.from(row.settings) }
    val used = row.todayValue ?: 0
    val habitColor = Color(row.habit.color)
    val colors = MaterialTheme.colorScheme

    val (status, statusColor) = when {
        !hasUsageAccess -> "Włącz dostęp do danych o użyciu" to colors.tertiary
        row.outcome == DayOutcome.FAILURE -> "Przekroczono limit" to colors.tertiary
        row.outcome == DayOutcome.SUCCESS -> "Limit dotrzymany" to colors.secondary
        else -> "W trakcie dnia" to colors.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(width = 4.dp, height = 44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(habitColor)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = row.habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Dziś: $used / ${settings.limitMinutes} min · ${settings.packages.size} apki",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = statusColor,
                )
            }
            if (!hasUsageAccess) {
                FilledTonalButton(onClick = onOpenUsageAccess) { Text("Dostęp") }
            }
        }
    }
}

@Composable
fun ScreenTimeDetailsInfo(
    habit: Habit,
    settings: Map<String, String>,
    todayMinutes: Int?,
    modifier: Modifier = Modifier,
) {
    val screenTime = remember(settings) { ScreenTimeSettings.from(settings) }
    Text(
        text = "Dziś: ${todayMinutes ?: 0} / ${screenTime.limitMinutes} min · " +
            "${screenTime.packages.size} wybranych aplikacji",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
