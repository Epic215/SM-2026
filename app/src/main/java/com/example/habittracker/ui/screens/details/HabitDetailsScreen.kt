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
import com.example.habittracker.data.model.Habit
import com.example.habittracker.domain.HabitStats
import com.example.habittracker.domain.StatsPeriod
import com.example.habittracker.domain.StreakUnit
import com.example.habittracker.domain.isActiveOn
import com.example.habittracker.domain.isNegative
import com.example.habittracker.ui.AppViewModelProvider
import com.example.habittracker.ui.screens.details.components.HabitMonthCalendar
import com.example.habittracker.ui.screens.details.components.StatTile
import com.example.habittracker.ui.screens.habits.components.dayCountText
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
            if (habit.isNegative) {
                Text(
                    text = "Nawyk do ograniczenia: zaznaczone dni to potknięcia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            if (habit.description.isNotBlank()) {
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            PeriodSelector(selected = uiState.period, onSelect = viewModel::selectPeriod)

            uiState.stats?.let { stats ->
                StatsGrid(habit = habit, stats = stats, period = uiState.period, accent = habitColor)
            }

            Text(
                text = "Historia",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            HabitMonthCalendar(
                doneDates = uiState.doneDates,
                isScheduled = { habit.isActiveOn(it) },
                habitColor = habitColor,
                isNegative = habit.isNegative,
                today = viewModel.today,
                onDayClick = viewModel::toggleDay,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
internal fun PeriodSelector(selected: StatsPeriod, onSelect: (StatsPeriod) -> Unit) {
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
    habit: Habit,
    stats: HabitStats,
    period: StatsPeriod,
    accent: Color,
) {
    val hasSchedule = habit.frequencyType != FrequencyType.NONE
    val inPeriod = if (period == StatsPeriod.WEEK) "w tym tygodniu" else "w tym miesiącu"
    val rate = stats.successRate?.let { "${(it * 100).roundToInt()}%" } ?: "—"

    val tiles: List<Pair<String, String>> = buildList {
        if (habit.isNegative) {
            add("Bez potknięcia" to dayCountText(stats.daysSinceLastSlip ?: 0))
            if (hasSchedule) {
                add("Najdłuższa seria" to streakText(stats.longestStreak, stats.streakUnit))
                add("Skuteczność" to rate)
            }
            add("Potknięcia $inPeriod" to stats.periodCompletions.toString())
            if (!hasSchedule) add("Potknięcia łącznie" to stats.totalCompletions.toString())
        } else if (hasSchedule) {
            add("Aktualna seria" to streakText(stats.currentStreak, stats.streakUnit))
            add("Najdłuższa seria" to streakText(stats.longestStreak, stats.streakUnit))
            add("Skuteczność" to rate)
            add("Wykonania $inPeriod" to stats.periodCompletions.toString())
        } else {
            add("Wykonania $inPeriod" to stats.periodCompletions.toString())
            add("Łącznie" to stats.totalCompletions.toString())
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (label, value) ->
                    StatTile(label = label, value = value, accent = accent, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

internal fun streakText(count: Int, unit: StreakUnit): String = when (unit) {
    StreakUnit.DAYS -> dayCountText(count)
    StreakUnit.WEEKS -> {
        val word = when {
            count == 1 -> "tydzień"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "tygodnie"
            else -> "tygodni"
        }
        "$count $word"
    }
}
