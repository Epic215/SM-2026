package com.example.habittracker.domain

import com.example.habittracker.data.model.Habit
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
): OverviewStats {
    val summaries = habits.map { habit ->
        HabitSummary(habit, calculateStats(habit, doneByHabit[habit.id].orEmpty(), period, today))
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

/** Share of positive habits due on [date] that were done; null when none were due. */
fun dayCompletion(
    habits: List<Habit>,
    doneByHabit: Map<Long, Set<LocalDate>>,
    date: LocalDate,
): Float? {
    val due = habits.filter { !it.isNegative && it.isDueOn(date, doneByHabit[it.id].orEmpty()) }
    if (due.isEmpty()) return null
    return due.count { date in doneByHabit[it.id].orEmpty() }.toFloat() / due.size
}
