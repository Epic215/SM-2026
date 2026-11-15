package com.example.habittracker.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.data.model.Habit
import com.example.habittracker.domain.dayCompletion
import com.example.habittracker.domain.isActiveOn
import com.example.habittracker.domain.isNegative
import com.example.habittracker.domain.progressByHabit
import com.example.habittracker.domain.startOfWeek
import com.example.habittracker.domain.builtin.BuiltInHabits
import com.example.habittracker.ui.screens.details.components.CALENDAR_MONTHS_BACK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

data class DayHabitItem(
    val habit: Habit,
    val marked: Boolean,
    val scheduled: Boolean,
    val isBuiltIn: Boolean,
    val value: Int?,
    val settings: Map<String, String>,
    val priorSuccessCount: Int = 0,
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
        repository.observeAllSettings(),
    ) { habits, entries, settings ->
        val now = LocalDateTime.now()
        val progress = progressByHabit(habits, entries, settings, now)
        val doneByHabit = progress.mapValues { it.value.doneDates }
        val valuesByHabit = entries
            .groupBy { it.habitId }
            .mapValues { (_, list) -> list.associate { it.date to it.value } }
        val completion = generateSequence(from) { it.plusDays(1) }
            .takeWhile { !it.isAfter(today) }
            .mapNotNull { date -> dayCompletion(habits, doneByHabit, date)?.let { date to it } }
            .toMap()
        CalendarHistory(habits, doneByHabit, valuesByHabit, settings, completion)
    }

    val uiState: StateFlow<CalendarUiState> = combine(history, selectedDate) { hist, date ->
        CalendarUiState(
            completion = hist.completion,
            selectedDate = date,
            dayItems = hist.habits
                .filter {
                    !date.isBefore(it.startDate) || date in hist.doneByHabit[it.id].orEmpty() ||
                        date in hist.valuesByHabit[it.id].orEmpty()
                }
                .sortedBy { it.isNegative }
                .map { habit ->
                    DayHabitItem(
                        habit = habit,
                        marked = date in hist.doneByHabit[habit.id].orEmpty(),
                        scheduled = habit.isActiveOn(date),
                        isBuiltIn = BuiltInHabits.find(habit.kind) != null,
                        value = hist.valuesByHabit[habit.id]?.get(date),
                        settings = hist.settings[habit.id].orEmpty(),
                        priorSuccessCount = hist.doneByHabit[habit.id].orEmpty().count { it.isBefore(date) },
                    )
                },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun selectDate(date: LocalDate) {
        if (!date.isAfter(today)) selectedDate.value = date
    }

    fun toggle(habitId: Long, date: LocalDate) {
        viewModelScope.launch { repository.toggleEntry(habitId, date) }
    }

    fun setDayValue(habitId: Long, date: LocalDate, value: Int?) {
        viewModelScope.launch {
            if (value == null) repository.removeEntry(habitId, date)
            else repository.saveEntry(habitId, date, value)
        }
    }

    private data class CalendarHistory(
        val habits: List<Habit>,
        val doneByHabit: Map<Long, Set<LocalDate>>,
        val valuesByHabit: Map<Long, Map<LocalDate, Int>>,
        val settings: Map<Long, Map<String, String>>,
        val completion: Map<LocalDate, Float>,
    )
}
