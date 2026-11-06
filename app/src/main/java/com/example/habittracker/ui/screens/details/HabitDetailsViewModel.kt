package com.example.habittracker.ui.screens.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.data.model.Habit
import com.example.habittracker.domain.HabitStats
import com.example.habittracker.domain.StatsPeriod
import com.example.habittracker.domain.calculateStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitDetailsUiState(
    val habit: Habit? = null,
    val doneDates: Set<LocalDate> = emptySet(),
    val period: StatsPeriod = StatsPeriod.WEEK,
    val stats: HabitStats? = null,
    val isLoading: Boolean = true,
)

class HabitDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: HabitRepository,
) : ViewModel() {

    private val habitId: Long = checkNotNull(savedStateHandle[HABIT_ID_ARG])
    private val period = MutableStateFlow(StatsPeriod.WEEK)

    val today: LocalDate = LocalDate.now()

    val uiState: StateFlow<HabitDetailsUiState> = combine(
        repository.observeHabit(habitId),
        repository.observeEntriesForHabit(habitId),
        period,
    ) { habit, entries, period ->
        val doneDates = entries.mapTo(mutableSetOf()) { it.date }
        HabitDetailsUiState(
            habit = habit,
            doneDates = doneDates,
            period = period,
            stats = habit?.let { calculateStats(it, doneDates, period, today) },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitDetailsUiState())

    fun selectPeriod(newPeriod: StatsPeriod) {
        period.value = newPeriod
    }

    fun toggleDay(date: LocalDate) {
        if (date.isAfter(today)) return
        viewModelScope.launch { repository.toggleEntry(habitId, date) }
    }

    companion object {
        const val HABIT_ID_ARG = "habitId"
    }
}
