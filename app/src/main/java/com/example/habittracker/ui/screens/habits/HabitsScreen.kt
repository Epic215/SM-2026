package com.example.habittracker.ui.screens.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habittracker.ui.AppViewModelProvider
import com.example.habittracker.ui.screens.habits.components.DailySummaryCard
import com.example.habittracker.ui.screens.habits.components.DayCellWidth
import com.example.habittracker.ui.screens.habits.components.HabitRow
import com.example.habittracker.ui.screens.habits.components.RowEndPadding
import com.example.habittracker.ui.screens.habits.components.RowStartPadding
import com.example.habittracker.ui.screens.habits.components.SporadicHabitRow
import com.example.habittracker.ui.screens.habits.components.shortLabel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("pl"))

@Composable
fun HabitsScreen(
    onAddHabit: () -> Unit,
    onEditHabit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> Unit
            uiState.scheduled.isEmpty() && uiState.sporadic.isEmpty() ->
                EmptyState(Modifier.align(Alignment.Center))
            else -> HabitsList(
                uiState = uiState,
                onToggle = viewModel::toggle,
                onChangeCount = viewModel::changeTodayCount,
                onEditHabit = onEditHabit,
            )
        }

        FloatingActionButton(
            onClick = onAddHabit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Dodaj nawyk")
        }
    }
}

@Composable
private fun HabitsList(
    uiState: HabitsUiState,
    onToggle: (Long, LocalDate) -> Unit,
    onChangeCount: (HabitRowState, Int) -> Unit,
    onEditHabit: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { Header(today = uiState.days.last()) }

        if (uiState.dueToday > 0) {
            item { DailySummaryCard(done = uiState.doneToday, total = uiState.dueToday) }
        }

        if (uiState.scheduled.isNotEmpty()) {
            item { DaysHeader(days = uiState.days) }
            items(uiState.scheduled, key = { it.habit.id }) { row ->
                HabitRow(
                    row = row,
                    onToggle = { date -> onToggle(row.habit.id, date) },
                    onClick = { onEditHabit(row.habit.id) },
                )
            }
        }

        if (uiState.sporadic.isNotEmpty()) {
            item { SectionTitle("Sporadyczne") }
            items(uiState.sporadic, key = { it.habit.id }) { row ->
                SporadicHabitRow(
                    row = row,
                    onIncrement = { onChangeCount(row, 1) },
                    onDecrement = { onChangeCount(row, -1) },
                    onClick = { onEditHabit(row.habit.id) },
                )
            }
        }
    }
}

@Composable
private fun Header(today: LocalDate) {
    Column(Modifier.padding(bottom = 4.dp)) {
        Text(
            text = "Dzisiaj",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = today.format(dateFormatter),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DaysHeader(days: List<LocalDate>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = RowStartPadding, end = RowEndPadding, top = 8.dp),
    ) {
        Spacer(Modifier.weight(1f))
        days.forEachIndexed { index, date ->
            val isToday = index == days.lastIndex
            val color = if (isToday) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
            Column(
                modifier = Modifier.width(DayCellWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(date.dayOfWeek.shortLabel(), style = MaterialTheme.typography.labelSmall, color = color)
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = color,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp, start = 4.dp),
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Nie masz jeszcze nawyków",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Dodaj pierwszy przyciskiem +",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
