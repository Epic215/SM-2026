package com.example.habittracker.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.data.model.FrequencyType
import com.example.habittracker.data.model.Habit
import com.example.habittracker.domain.isActiveOn
import com.example.habittracker.domain.isDueOn
import com.example.habittracker.domain.startOfWeek
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DayCell(
    val date: LocalDate,
    val done: Boolean,
    val scheduled: Boolean,
)

data class HabitRowState(
    val habit: Habit,
    val days: List<DayCell>,
    val todayValue: Int,
    val doneThisWeek: Int,
    val isDueToday: Boolean,
) {
    val isDoneToday: Boolean get() = todayValue > 0
}

data class HabitsUiState(
    val days: List<LocalDate> = emptyList(),
    val scheduled: List<HabitRowState> = emptyList(),
    val sporadic: List<HabitRowState> = emptyList(),
    val doneToday: Int = 0,
    val dueToday: Int = 0,
    val isLoading: Boolean = true,
)

class HabitsViewModel(private val repository: HabitRepository) : ViewModel() {

    private val today = LocalDate.now()
    private val days = (VISIBLE_DAYS - 1 downTo 0).map { today.minusDays(it.toLong()) }
    private val weekStart = today.startOfWeek()
    private val from = if (weekStart.isBefore(days.first())) weekStart else days.first()

    val uiState: StateFlow<HabitsUiState> = combine(
        repository.observeActiveHabits(),
        repository.observeEntries(from, today),
    ) { habits, entries ->
        val doneDatesByHabit = entries.groupBy({ it.habitId }, { it.date })
        val valueToday = entries.filter { it.date == today }.associate { it.habitId to it.value }

        val rows = habits.map { habit ->
            val doneDates = doneDatesByHabit[habit.id].orEmpty().toSet()
            HabitRowState(
                habit = habit,
                days = days.map { DayCell(it, it in doneDates, habit.isActiveOn(it)) },
                todayValue = valueToday[habit.id] ?: 0,
                doneThisWeek = doneDates.count { it.startOfWeek() == weekStart },
                isDueToday = habit.isDueOn(today, doneDates),
            )
        }
        val (sporadic, scheduled) = rows.partition { it.habit.frequencyType == FrequencyType.NONE }
        val due = scheduled.filter { it.isDueToday }

        HabitsUiState(
            days = days,
            scheduled = scheduled,
            sporadic = sporadic,
            doneToday = due.count { it.isDoneToday },
            dueToday = due.size,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitsUiState())

    fun toggle(habitId: Long, date: LocalDate) {
        viewModelScope.launch { repository.toggleEntry(habitId, date) }
    }

    fun changeTodayCount(row: HabitRowState, delta: Int) {
        viewModelScope.launch { repository.setEntryValue(row.habit.id, today, row.todayValue + delta) }
    }

    companion object {
        const val VISIBLE_DAYS = 5
    }
}
