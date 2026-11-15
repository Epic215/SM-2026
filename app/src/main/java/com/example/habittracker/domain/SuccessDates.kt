package com.example.habittracker.domain

import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitEntry
import com.example.habittracker.data.model.HabitKind
import com.example.habittracker.domain.builtin.BuiltInHabits
import com.example.habittracker.domain.builtin.DayOutcome
import com.example.habittracker.domain.builtin.WakeUpHabit
import com.example.habittracker.domain.builtin.WakeUpSettings
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * [doneDates] are the days that count as "marked" for statistics: every entry for regular
 * habits, only successful days for built-in ones.
 */
data class HabitProgress(
    val doneDates: Set<LocalDate>,
    val isTodayDecided: Boolean,
    val todayOutcome: DayOutcome? = null,
    val todayMinimumMinutes: Int? = null,
)

fun habitProgress(
    habit: Habit,
    entries: List<HabitEntry>,
    settings: Map<String, String>,
    now: LocalDateTime,
): HabitProgress {
    val today = now.toLocalDate()

    if (habit.kind == HabitKind.WAKE_UP) {
        val evaluation = WakeUpHabit.evaluate(
            WakeUpSettings.from(settings),
            habit.startDate,
            entries.associate { it.date to it.value },
            now,
        )
        return HabitProgress(
            doneDates = evaluation.successDates,
            isTodayDecided = evaluation.todayOutcome != DayOutcome.PENDING,
            todayOutcome = evaluation.todayOutcome,
            todayMinimumMinutes = evaluation.todayMinimumMinutes,
        )
    }

    val builtIn = BuiltInHabits.find(habit.kind)
    if (builtIn == null) {
        val dates = entries.mapTo(mutableSetOf()) { it.date }
        return HabitProgress(dates, today in dates)
    }

    val successDates = entries
        .filter { builtIn.outcome(habit, settings, it.date, it.value, now) == DayOutcome.SUCCESS }
        .mapTo(mutableSetOf()) { it.date }
    val todayValue = entries.firstOrNull { it.date == today }?.value
    val todayOutcome = builtIn.outcome(habit, settings, today, todayValue, now)
    return HabitProgress(successDates, todayOutcome != DayOutcome.PENDING, todayOutcome)
}

fun progressByHabit(
    habits: List<Habit>,
    entries: List<HabitEntry>,
    settingsByHabit: Map<Long, Map<String, String>>,
    now: LocalDateTime,
): Map<Long, HabitProgress> {
    val entriesByHabit = entries.groupBy { it.habitId }
    return habits.associate { habit ->
        habit.id to habitProgress(
            habit,
            entriesByHabit[habit.id].orEmpty(),
            settingsByHabit[habit.id].orEmpty(),
            now,
        )
    }
}
