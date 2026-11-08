package com.example.habittracker.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.data.model.Habit
import com.example.habittracker.domain.dayCompletion
import com.example.habittracker.domain.isActiveOn
import com.example.habittracker.domain.isNegative
import com.example.habittracker.domain.startOfWeek
import com.example.habittracker.ui.screens.details.components.CALENDAR_MONTHS_BACK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class DayHabitItem(
    val habit: Habit,
    val marked: Boolean,
    val scheduled: Boolean,
)

data class CalendarUiState(
    /** Share of due positive habits done, per day; missing when nothing was due. */
    val completion: Map<LocalDate, Float> = emptyMap(),
    val selectedDate: LocalDate = LocalDate.now(),
    val dayItems: List<DayHabitItem> = emptyList(),
    val isLoading: Boolean = true,
)

class CalendarViewModel(private val repository: HabitRepository) : ViewModel() {

    val today: LocalDate = LocalDate.now()
    private val from = YearMonth.from(today).minusMonths(CALENDAR_MONTHS_BACK).atDay(1)
    private val selectedDate = MutableStateFlow(today)

    private val history = combine(
        repository.observeActiveHabits(),
        repository.observeEntries(from.startOfWeek(), today),
    ) { habits, entries ->
        val doneByHabit = entries.groupBy({ it.habitId }, { it.date }).mapValues { it.value.toSet() }
        val completion = generateSequence(from) { it.plusDays(1) }
            .takeWhile { !it.isAfter(today) }
            .mapNotNull { date -> dayCompletion(habits, doneByHabit, date)?.let { date to it } }
            .toMap()
        Triple(habits, doneByHabit, completion)
    }

    val uiState: StateFlow<CalendarUiState> = combine(history, selectedDate) { (habits, doneByHabit, completion), date ->
        CalendarUiState(
            completion = completion,
            selectedDate = date,
            dayItems = habits
                .filter { !date.isBefore(it.startDate) || date in doneByHabit[it.id].orEmpty() }
                .sortedBy { it.isNegative }
                .map { DayHabitItem(it, date in doneByHabit[it.id].orEmpty(), it.isActiveOn(date)) },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun selectDate(date: LocalDate) {
        if (!date.isAfter(today)) selectedDate.value = date
    }

    fun toggle(habitId: Long, date: LocalDate) {
        viewModelScope.launch { repository.toggleEntry(habitId, date) }
    }
}
