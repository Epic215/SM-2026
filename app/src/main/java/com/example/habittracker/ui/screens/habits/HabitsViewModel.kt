package com.example.habittracker.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.data.model.FrequencyType
import com.example.habittracker.data.model.Habit
import com.example.habittracker.domain.builtin.BuiltInHabits
import com.example.habittracker.domain.builtin.DayOutcome
import com.example.habittracker.domain.habitProgress
import com.example.habittracker.domain.isActiveOn
import com.example.habittracker.domain.isDueOn
import com.example.habittracker.domain.isNegative
import com.example.habittracker.domain.startOfWeek
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

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
    /** NEGATIVE habits only. */
    val daysSinceLastSlip: Int? = null,
) {
    val isDoneToday: Boolean get() = todayValue > 0
}

data class BuiltInRowState(
    val habit: Habit,
    val settings: Map<String, String>,
    val todayValue: Int?,
    val outcome: DayOutcome,
    val minimumMinutes: Int?,
)

data class HabitsUiState(
    val days: List<LocalDate> = emptyList(),
    val scheduled: List<HabitRowState> = emptyList(),
    val builtIn: List<BuiltInRowState> = emptyList(),
    val sporadic: List<HabitRowState> = emptyList(),
    val avoid: List<HabitRowState> = emptyList(),
    val doneToday: Int = 0,
    val dueToday: Int = 0,
    val slipsToday: Int = 0,
    val isLoading: Boolean = true,
) {
    val isEmpty: Boolean
        get() = scheduled.isEmpty() && builtIn.isEmpty() && sporadic.isEmpty() && avoid.isEmpty()
}

class HabitsViewModel(private val repository: HabitRepository) : ViewModel() {

    private val today = LocalDate.now()
    private val days = (VISIBLE_DAYS - 1 downTo 0).map { today.minusDays(it.toLong()) }
    private val weekStart = today.startOfWeek()
    private val from = if (weekStart.isBefore(days.first())) weekStart else days.first()

    /** Re-evaluates time-based outcomes (e.g. a missed wake-up deadline) while the screen is open. */
    private val now = flow {
        while (true) {
            emit(LocalDateTime.now())
            delay(60_000)
        }
    }

    val uiState: StateFlow<HabitsUiState> = combine(
        repository.observeActiveHabits(),
        repository.observeAllEntries(),
        repository.observeLastEntryDates(),
        repository.observeAllSettings(),
        now,
    ) { habits, allEntries, lastEntryDates, settings, now ->
        val weekEntries = allEntries.filter { !it.date.isBefore(from) && !it.date.isAfter(today) }
        val doneDatesByHabit = weekEntries.groupBy({ it.habitId }, { it.date })
        val valueToday = allEntries.filter { it.date == today }.associate { it.habitId to it.value }
        val entriesByHabit = allEntries.groupBy { it.habitId }

        val builtInRows = habits.mapNotNull { habit ->
            if (BuiltInHabits.find(habit.kind) == null) return@mapNotNull null
            val habitSettings = settings[habit.id].orEmpty()
            val progress = habitProgress(habit, entriesByHabit[habit.id].orEmpty(), habitSettings, now)
            BuiltInRowState(
                habit = habit,
                settings = habitSettings,
                todayValue = valueToday[habit.id],
                outcome = progress.todayOutcome ?: DayOutcome.PENDING,
                minimumMinutes = progress.todayMinimumMinutes,
            )
        }
        val builtInIds = builtInRows.mapTo(mutableSetOf()) { it.habit.id }

        val rows = habits.filter { it.id !in builtInIds }.map { habit ->
            val doneDates = doneDatesByHabit[habit.id].orEmpty().toSet()
            HabitRowState(
                habit = habit,
                days = days.map { DayCell(it, it in doneDates, habit.isActiveOn(it)) },
                todayValue = valueToday[habit.id] ?: 0,
                doneThisWeek = doneDates.count { it.startOfWeek() == weekStart },
                isDueToday = !habit.isNegative && habit.isDueOn(today, doneDates),
                daysSinceLastSlip = if (habit.isNegative) {
                    ChronoUnit.DAYS.between(lastEntryDates[habit.id] ?: habit.startDate, today).toInt()
                } else {
                    null
                },
            )
        }
        val (avoid, positive) = rows.partition { it.habit.isNegative }
        val (sporadic, scheduled) = positive.partition { it.habit.frequencyType == FrequencyType.NONE }
        val due = scheduled.filter { it.isDueToday }

        HabitsUiState(
            days = days,
            scheduled = scheduled,
            builtIn = builtInRows,
            sporadic = sporadic,
            avoid = avoid,
            doneToday = due.count { it.isDoneToday } + builtInRows.count { it.outcome == DayOutcome.SUCCESS },
            dueToday = due.size + builtInRows.size,
            slipsToday = avoid.count { it.isDoneToday },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitsUiState())

    fun toggle(habitId: Long, date: LocalDate) {
        viewModelScope.launch { repository.toggleEntry(habitId, date) }
    }

    fun setBuiltInValue(habitId: Long, value: Int?) {
        viewModelScope.launch {
            if (value == null) repository.removeEntry(habitId, today) else repository.saveEntry(habitId, today, value)
        }
    }

    companion object {
        const val VISIBLE_DAYS = 5
    }
}
