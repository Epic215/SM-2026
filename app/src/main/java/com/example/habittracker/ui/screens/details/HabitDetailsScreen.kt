package com.example.habittracker.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habittracker.data.model.FrequencyType
import com.example.habittracker.domain.HabitStats
import com.example.habittracker.domain.StatsPeriod
import com.example.habittracker.domain.StreakUnit
import com.example.habittracker.domain.isActiveOn
import com.example.habittracker.ui.AppViewModelProvider
import com.example.habittracker.ui.screens.details.components.MonthCalendar
import com.example.habittracker.ui.screens.details.components.StatTile
import kotlin.math.roundToInt

@Composable
fun HabitDetailsScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val habit = uiState.habit

    LaunchedEffect(uiState.isLoading, habit) {
        if (!uiState.isLoading && habit == null) onBack()
    }
    if (habit == null) return

    val habitColor = Color(habit.color)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Wróć")
            }
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(habitColor)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onEdit(habit.id) }) {
                Icon(Icons.Outlined.Edit, contentDescription = "Edytuj nawyk")
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (habit.description.isNotBlank()) {
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            PeriodSelector(selected = uiState.period, onSelect = viewModel::selectPeriod)

            uiState.stats?.let { stats ->
                StatsGrid(
                    stats = stats,
                    hasSchedule = habit.frequencyType != FrequencyType.NONE,
                    period = uiState.period,
                    accent = habitColor,
                )
            }

            Text(
                text = "Historia",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            MonthCalendar(
                doneDates = uiState.doneDates,
                isScheduled = { habit.isActiveOn(it) },
                habitColor = habitColor,
                today = viewModel.today,
                onDayClick = viewModel::toggleDay,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PeriodSelector(selected: StatsPeriod, onSelect: (StatsPeriod) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatsPeriod.entries.forEach { period ->
            FilterChip(
                selected = period == selected,
                onClick = { onSelect(period) },
                label = { Text(if (period == StatsPeriod.WEEK) "Tydzień" else "Miesiąc") },
            )
        }
    }
}

@Composable
private fun StatsGrid(
    stats: HabitStats,
    hasSchedule: Boolean,
    period: StatsPeriod,
    accent: Color,
) {
    val periodLabel = if (period == StatsPeriod.WEEK) "W tym tygodniu" else "W tym miesiącu"

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (hasSchedule) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    label = "Aktualna seria",
                    value = streakText(stats.currentStreak, stats.streakUnit),
                    accent = accent,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "Najdłuższa seria",
                    value = streakText(stats.longestStreak, stats.streakUnit),
                    accent = accent,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (hasSchedule) {
                StatTile(
                    label = "Skuteczność",
                    value = stats.successRate?.let { "${(it * 100).roundToInt()}%" } ?: "—",
                    accent = accent,
                    modifier = Modifier.weight(1f),
                )
            } else {
                StatTile(
                    label = "Łącznie",
                    value = stats.totalCompletions.toString(),
                    accent = accent,
                    modifier = Modifier.weight(1f),
                )
            }
            StatTile(
                label = periodLabel,
                value = stats.periodCompletions.toString(),
                accent = accent,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun streakText(count: Int, unit: StreakUnit): String {
    val word = when (unit) {
        StreakUnit.DAYS -> if (count == 1) "dzień" else "dni"
        StreakUnit.WEEKS -> when {
            count == 1 -> "tydzień"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "tygodnie"
            else -> "tygodni"
        }
    }
    return "$count $word"
}
