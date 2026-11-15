package com.example.habittracker.domain

import com.example.habittracker.data.model.Habit
import com.example.habittracker.domain.builtin.BuiltInHabits
import java.time.LocalDate

data class HabitSummary(val habit: Habit, val stats: HabitStats)

data class OverviewStats(
    val averageSuccessRate: Float?,
    val completions: Int,
    val slips: Int,
    val bestDailyStreak: Int,
    /** Sorted by success rate, habits without one last. */
    val habits: List<HabitSummary>,
)

fun calculateOverview(
    habits: List<Habit>,
    doneByHabit: Map<Long, Set<LocalDate>>,
    period: StatsPeriod,
    today: LocalDate,
    todayDecided: Map<Long, Boolean> = emptyMap(),
): OverviewStats {
    val summaries = habits.map { habit ->
        val doneDates = doneByHabit[habit.id].orEmpty()
        val decided = todayDecided[habit.id] ?: (today in doneDates)
        HabitSummary(habit, calculateStats(habit, doneDates, period, today, decided))
    }
    val rates = summaries.mapNotNull { it.stats.successRate }
    val (negative, positive) = summaries.partition { it.habit.isNegative }

    return OverviewStats(
        averageSuccessRate = if (rates.isEmpty()) null else rates.average().toFloat(),
        completions = positive.sumOf { it.stats.periodCompletions },
        slips = negative.sumOf { it.stats.periodCompletions },
        bestDailyStreak = summaries
            .filter { it.stats.streakUnit == StreakUnit.DAYS }
            .maxOfOrNull { it.stats.currentStreak } ?: 0,
        habits = summaries.sortedByDescending { it.stats.successRate ?: -1f },
    )
}

/**
 * Share of positive habits due on [date] that were done; null when none were due.
 * [doneByHabit] must already contain success dates for built-in habits.
 */
fun dayCompletion(
    habits: List<Habit>,
    doneByHabit: Map<Long, Set<LocalDate>>,
    date: LocalDate,
): Float? {
    val due = habits.filter { habit ->
        !habit.isNegative && (
            BuiltInHabits.find(habit.kind) != null ||
                habit.isDueOn(date, doneByHabit[habit.id].orEmpty())
        ) && !date.isBefore(habit.startDate)
    }
    if (due.isEmpty()) return null
    return due.count { date in doneByHabit[it.id].orEmpty() }.toFloat() / due.size
}
