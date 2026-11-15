package com.example.habittracker.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.domain.OverviewStats
import com.example.habittracker.domain.StatsPeriod
import com.example.habittracker.domain.calculateOverview
import com.example.habittracker.domain.dayCompletion
import com.example.habittracker.domain.progressByHabit
import com.example.habittracker.domain.startOfWeek
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalDateTime

data class StatsUiState(
    val period: StatsPeriod = StatsPeriod.WEEK,
    val overview: OverviewStats? = null,
    val heatmap: Map<LocalDate, Float> = emptyMap(),
    val isLoading: Boolean = true,
)

class StatsViewModel(repository: HabitRepository) : ViewModel() {

    val today: LocalDate = LocalDate.now()
    val heatmapStart: LocalDate = today.startOfWeek().minusWeeks(HEATMAP_WEEKS - 1L)
    private val period = MutableStateFlow(StatsPeriod.WEEK)

    val uiState: StateFlow<StatsUiState> = combine(
        repository.observeActiveHabits(),
        repository.observeAllEntries(),
        repository.observeAllSettings(),
        period,
    ) { habits, entries, settings, period ->
        val progress = progressByHabit(habits, entries, settings, LocalDateTime.now())
        val doneByHabit = progress.mapValues { it.value.doneDates }
        val todayDecided = progress.mapValues { it.value.isTodayDecided }
        val heatmap = generateSequence(heatmapStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(today) }
            .mapNotNull { date -> dayCompletion(habits, doneByHabit, date)?.let { date to it } }
            .toMap()

        StatsUiState(
            period = period,
            overview = calculateOverview(habits, doneByHabit, period, today, todayDecided),
            heatmap = heatmap,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    fun selectPeriod(newPeriod: StatsPeriod) {
        period.value = newPeriod
    }

    companion object {
        const val HEATMAP_WEEKS = 17
    }
}
