package com.example.habittracker.ui.screens.habits.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.habittracker.data.model.FrequencyType
import com.example.habittracker.domain.isNegative
import com.example.habittracker.ui.screens.habits.DayCell
import com.example.habittracker.ui.screens.habits.HabitRowState
import java.time.DayOfWeek
import java.time.LocalDate

internal val DayCellWidth = 40.dp
internal val RowStartPadding = 12.dp
internal val RowEndPadding = 8.dp

internal fun DayOfWeek.shortLabel(): String = when (this) {
    DayOfWeek.MONDAY -> "Pn"
    DayOfWeek.TUESDAY -> "Wt"
    DayOfWeek.WEDNESDAY -> "Śr"
    DayOfWeek.THURSDAY -> "Cz"
    DayOfWeek.FRIDAY -> "Pt"
    DayOfWeek.SATURDAY -> "So"
    DayOfWeek.SUNDAY -> "Nd"
}

internal fun dayCountText(count: Int): String = if (count == 1) "1 dzień" else "$count dni"

@Composable
fun HabitRow(
    row: HabitRowState,
    onToggle: (LocalDate) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habitColor = Color(row.habit.color)
    HabitCard(onClick = onClick, modifier = modifier) {
        ColorBar(habitColor)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = row.habit.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = row.frequencyLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        row.days.forEach { day ->
            Box(Modifier.width(DayCellWidth), contentAlignment = Alignment.Center) {
                DayCircle(
                    day = day,
                    habitColor = habitColor,
                    isNegative = row.habit.isNegative,
                    onClick = { onToggle(day.date) },
                )
            }
        }
    }
}

@Composable
private fun HabitCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(
                start = RowStartPadding,
                end = RowEndPadding,
                top = 12.dp,
                bottom = 12.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun ColorBar(color: Color) {
    Box(
        Modifier
            .size(width = 4.dp, height = 32.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}

@Composable
private fun DayCircle(
    day: DayCell,
    habitColor: Color,
    isNegative: Boolean,
    onClick: () -> Unit,
) {
    val markColor = if (isNegative) MaterialTheme.colorScheme.tertiary else habitColor
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .then(
                when {
                    day.done -> Modifier.background(markColor)
                    day.scheduled -> Modifier.border(2.dp, habitColor.copy(alpha = 0.35f), CircleShape)
                    else -> Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        when {
            day.done -> Icon(
                if (isNegative) Icons.Rounded.Close else Icons.Rounded.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            !day.scheduled -> Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline)
            )
        }
    }
}

private fun HabitRowState.frequencyLabel(): String {
    if (habit.isNegative) return "${dayCountText(daysSinceLastSlip ?: 0)} bez potknięcia"
    return when (habit.frequencyType) {
        FrequencyType.DAILY -> "Codziennie"
        FrequencyType.TIMES_PER_WEEK -> "$doneThisWeek/${habit.target} w tym tygodniu"
        FrequencyType.SPECIFIC_DAYS -> habit.daysOfWeek.sorted().joinToString(", ") { it.shortLabel() }
        FrequencyType.NONE -> "W tym tygodniu: $doneThisWeek×"
    }
}
