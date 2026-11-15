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
import androidx.compose.material3.TextButton
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
import com.example.habittracker.ui.screens.builtin.BuiltInHabitRow
import com.example.habittracker.ui.screens.habits.components.DailySummaryCard
import com.example.habittracker.ui.screens.habits.components.DayCellWidth
import com.example.habittracker.ui.screens.habits.components.HabitRow
import com.example.habittracker.ui.screens.habits.components.RowEndPadding
import com.example.habittracker.ui.screens.habits.components.RowStartPadding
import com.example.habittracker.ui.screens.habits.components.shortLabel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("pl"))

@Composable
fun HabitsScreen(
    onAddHabit: () -> Unit,
    onAddSporadic: () -> Unit,
    onAddBuiltIn: () -> Unit,
    onOpenHabit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> Unit
            uiState.isEmpty ->
                EmptyState(
                    modifier = Modifier.align(Alignment.Center),
                    onAddBuiltIn = onAddBuiltIn,
                )
            else -> HabitsList(
                uiState = uiState,
                onToggle = viewModel::toggle,
                onSetBuiltInValue = viewModel::setBuiltInValue,
                onAddSporadic = onAddSporadic,
                onAddBuiltIn = onAddBuiltIn,
                onOpenHabit = onOpenHabit,
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
    onSetBuiltInValue: (Long, Int?) -> Unit,
    onAddSporadic: () -> Unit,
    onAddBuiltIn: () -> Unit,
    onOpenHabit: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { Header(today = uiState.days.last()) }

        if (uiState.dueToday > 0 || uiState.avoid.isNotEmpty()) {
            item {
                DailySummaryCard(
                    done = uiState.doneToday,
                    total = uiState.dueToday,
                    slipsToday = uiState.slipsToday,
                    showSlips = uiState.avoid.isNotEmpty(),
                )
            }
        }

        if (uiState.scheduled.isNotEmpty()) {
            item { DaysHeader(days = uiState.days) }
            items(uiState.scheduled, key = { it.habit.id }) { row ->
                HabitRow(
                    row = row,
                    onToggle = { date -> onToggle(row.habit.id, date) },
                    onClick = { onOpenHabit(row.habit.id) },
                )
            }
        }

        item { SectionTitle("Wbudowane", actionLabel = "Dodaj", onAction = onAddBuiltIn) }
        if (uiState.builtIn.isEmpty()) {
            item {
                Text(
                    text = "Gotowe nawyki z własnymi zasadami, np. wstawanie.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        } else {
            items(uiState.builtIn, key = { it.habit.id }) { row ->
                BuiltInHabitRow(
                    row = row,
                    today = uiState.days.last(),
                    onSetValue = { value -> onSetBuiltInValue(row.habit.id, value) },
                    onClick = { onOpenHabit(row.habit.id) },
                )
            }
        }

        if (uiState.avoid.isNotEmpty()) {
            item { SectionTitle("Do unikania") }
            item { DaysHeader(days = uiState.days) }
            items(uiState.avoid, key = { it.habit.id }) { row ->
                HabitRow(
                    row = row,
                    onToggle = { date -> onToggle(row.habit.id, date) },
                    onClick = { onOpenHabit(row.habit.id) },
                )
            }
        }

        if (uiState.sporadic.isNotEmpty()) {
            item { SectionTitle("Sporadyczne", actionLabel = "Dodaj", onAction = onAddSporadic) }
            item { DaysHeader(days = uiState.days) }
            items(uiState.sporadic, key = { it.habit.id }) { row ->
                HabitRow(
                    row = row,
                    onToggle = { date -> onToggle(row.habit.id, date) },
                    onClick = { onOpenHabit(row.habit.id) },
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
private fun SectionTitle(
    title: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
private fun EmptyState(onAddBuiltIn: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Nie masz jeszcze nawyków",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Dodaj pierwszy przyciskiem + albo wybierz wbudowany.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onAddBuiltIn) { Text("Wbudowane nawyki") }
    }
}
