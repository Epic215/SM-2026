package com.example.habittracker.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habittracker.domain.OverviewStats
import com.example.habittracker.domain.StatsPeriod
import com.example.habittracker.domain.StreakUnit
import com.example.habittracker.ui.AppViewModelProvider
import com.example.habittracker.ui.screens.details.PeriodSelector
import com.example.habittracker.ui.screens.details.components.StatTile
import com.example.habittracker.ui.screens.details.streakText
import com.example.habittracker.ui.screens.stats.components.ActivityHeatmap
import com.example.habittracker.ui.screens.stats.components.HabitRankingRow
import kotlin.math.roundToInt

@Composable
fun StatsScreen(
    onOpenHabit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val overview = uiState.overview

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Statystyki",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        if (overview == null) return@LazyColumn
        if (overview.habits.isEmpty()) {
            item {
                Text(
                    text = "Dodaj nawyki, żeby zobaczyć statystyki",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@LazyColumn
        }

        item { PeriodSelector(selected = uiState.period, onSelect = viewModel::selectPeriod) }
        item { OverviewTiles(overview = overview, period = uiState.period) }

        item { SectionTitle("Aktywność") }
        item {
            ActivityHeatmap(
                completion = uiState.heatmap,
                start = viewModel.heatmapStart,
                weeks = StatsViewModel.HEATMAP_WEEKS,
                today = viewModel.today,
            )
        }

        item { SectionTitle("Nawyki") }
        items(overview.habits, key = { it.habit.id }) { summary ->
            HabitRankingRow(summary = summary, onClick = { onOpenHabit(summary.habit.id) })
        }
    }
}

@Composable
private fun OverviewTiles(overview: OverviewStats, period: StatsPeriod) {
    val colors = MaterialTheme.colorScheme
    val inPeriod = if (period == StatsPeriod.WEEK) "w tym tygodniu" else "w tym miesiącu"

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                label = "Średnia skuteczność",
                value = overview.averageSuccessRate?.let { "${(it * 100).roundToInt()}%" } ?: "—",
                accent = colors.primary,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = "Najlepsza aktualna seria",
                value = streakText(overview.bestDailyStreak, StreakUnit.DAYS),
                accent = colors.primary,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                label = "Wykonania $inPeriod",
                value = overview.completions.toString(),
                accent = colors.secondary,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = "Potknięcia $inPeriod",
                value = overview.slips.toString(),
                accent = colors.tertiary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp),
    )
}
