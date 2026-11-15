package com.example.habittracker.ui.screens.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.data.model.Habit
import com.example.habittracker.domain.HabitStats
import com.example.habittracker.domain.StatsPeriod
import com.example.habittracker.domain.calculateStats
import com.example.habittracker.domain.habitProgress
import com.example.habittracker.domain.builtin.BuiltInHabits
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class HabitDetailsUiState(
    val habit: Habit? = null,
    val settings: Map<String, String> = emptyMap(),
    val entriesByDate: Map<LocalDate, Int> = emptyMap(),
    /** Successful days for the calendar (for regular habits: every entry). */
    val doneDates: Set<LocalDate> = emptySet(),
    val period: StatsPeriod = StatsPeriod.WEEK,
    val stats: HabitStats? = null,
    val isBuiltIn: Boolean = false,
    val todayMinimumMinutes: Int? = null,
    val isLoading: Boolean = true,
)

class HabitDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: HabitRepository,
) : ViewModel() {

    private val habitId: Long = checkNotNull(savedStateHandle[HABIT_ID_ARG])
    private val period = MutableStateFlow(StatsPeriod.WEEK)

    val today: LocalDate = LocalDate.now()

    private val now = flow {
        while (true) {
            emit(LocalDateTime.now())
            delay(60_000)
        }
    }

    val uiState: StateFlow<HabitDetailsUiState> = combine(
        repository.observeHabit(habitId),
        repository.observeEntriesForHabit(habitId),
        repository.observeSettings(habitId),
        period,
        now,
    ) { habit, entries, settings, period, now ->
        val progress = habit?.let { habitProgress(it, entries, settings, now) }
        HabitDetailsUiState(
            habit = habit,
            settings = settings,
            entriesByDate = entries.associate { it.date to it.value },
            doneDates = progress?.doneDates.orEmpty(),
            period = period,
            stats = habit?.let {
                calculateStats(
                    it,
                    progress?.doneDates.orEmpty(),
                    period,
                    today,
                    progress?.isTodayDecided ?: false,
                )
            },
            isBuiltIn = habit != null && BuiltInHabits.find(habit.kind) != null,
            todayMinimumMinutes = progress?.todayMinimumMinutes,
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

    fun setDayValue(date: LocalDate, value: Int?) {
        if (date.isAfter(today)) return
        viewModelScope.launch {
            if (value == null) repository.removeEntry(habitId, date)
            else repository.saveEntry(habitId, date, value)
        }
    }

    companion object {
        const val HABIT_ID_ARG = "habitId"
    }
}
