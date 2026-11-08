package com.example.habittracker.domain

import com.example.habittracker.data.model.FrequencyType
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class StatsPeriod { WEEK, MONTH }

enum class StreakUnit { DAYS, WEEKS }

data class DateRange(val start: LocalDate, val end: LocalDate) {
    operator fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)
}

/**
 * For NEGATIVE habits an entry is a slip: [periodCompletions] counts slips and a streak
 * counts clean days (or weeks within the limit).
 */
data class HabitStats(
    val periodCompletions: Int,
    val totalCompletions: Int,
    /** Null when the habit has no schedule or nothing in the period can be judged yet. */
    val successRate: Float?,
    val currentStreak: Int,
    val longestStreak: Int,
    val streakUnit: StreakUnit,
    /** NEGATIVE habits only. */
    val daysSinceLastSlip: Int? = null,
)

val Habit.isNegative: Boolean get() = type == HabitType.NEGATIVE

fun StatsPeriod.rangeContaining(date: LocalDate): DateRange = when (this) {
    StatsPeriod.WEEK -> date.startOfWeek().let { DateRange(it, it.plusDays(6)) }
    StatsPeriod.MONTH -> date.withDayOfMonth(1).let { DateRange(it, it.plusMonths(1).minusDays(1)) }
}

fun calculateStats(
    habit: Habit,
    doneDates: Set<LocalDate>,
    period: StatsPeriod,
    today: LocalDate,
): HabitStats {
    val range = period.rangeContaining(today)
    val periodCompletions = doneDates.count { it in range }
    val streakUnit =
        if (habit.frequencyType == FrequencyType.TIMES_PER_WEEK) StreakUnit.WEEKS else StreakUnit.DAYS

    // Days marked before the start date (backfilled history) still count.
    val firstDone = doneDates.minByOrNull { it.toEpochDay() }
    val start = if (firstDone != null && firstDone.isBefore(habit.startDate)) firstDone else habit.startDate

    val daysSinceLastSlip = if (habit.isNegative) {
        val lastSlip = doneDates.maxByOrNull { it.toEpochDay() }
        ChronoUnit.DAYS.between(lastSlip ?: start, today).toInt()
    } else {
        null
    }

    if (habit.frequencyType == FrequencyType.NONE) {
        return HabitStats(periodCompletions, doneDates.size, null, 0, 0, streakUnit, daysSinceLastSlip)
    }

    val effectiveHabit = habit.copy(startDate = start)
    val outcomes: List<Outcome>
    val periodFilter: DateRange
    when (streakUnit) {
        StreakUnit.DAYS -> {
            outcomes = dailyOutcomes(effectiveHabit, doneDates, today)
            periodFilter = range
        }
        StreakUnit.WEEKS -> {
            outcomes = weeklyOutcomes(effectiveHabit, doneDates, today)
            periodFilter = DateRange(range.start.startOfWeek(), range.end)
        }
    }

    val inPeriod = outcomes.filter { it.date in periodFilter }
    val successRate = if (inPeriod.isEmpty()) null else inPeriod.count { it.success }.toFloat() / inPeriod.size

    return HabitStats(
        periodCompletions = periodCompletions,
        totalCompletions = doneDates.size,
        successRate = successRate,
        currentStreak = outcomes.asReversed().takeWhile { it.success }.size,
        longestStreak = longestStreak(outcomes),
        streakUnit = streakUnit,
        daysSinceLastSlip = daysSinceLastSlip,
    )
}

private data class Outcome(val date: LocalDate, val success: Boolean)

/** Today is only judged once marked, so an unfinished day never affects a streak. */
private fun dailyOutcomes(habit: Habit, doneDates: Set<LocalDate>, today: LocalDate): List<Outcome> =
    generateSequence(habit.startDate) { it.plusDays(1) }
        .takeWhile { !it.isAfter(today) }
        .filter { habit.isActiveOn(it) }
        .map { Outcome(it, success = (it in doneDates) != habit.isNegative) }
        .filter { it.date != today || it.date in doneDates }
        .toList()

/** The current week is only judged once its outcome can no longer change. */
private fun weeklyOutcomes(habit: Habit, doneDates: Set<LocalDate>, today: LocalDate): List<Outcome> {
    val countsByWeek = doneDates.groupingBy { it.startOfWeek() }.eachCount()
    val currentWeek = today.startOfWeek()
    return generateSequence(habit.startDate.startOfWeek()) { it.plusWeeks(1) }
        .takeWhile { !it.isAfter(currentWeek) }
        .map { week ->
            val count = countsByWeek[week] ?: 0
            Outcome(week, if (habit.isNegative) count <= habit.target else count >= habit.target)
        }
        .filter { it.date != currentWeek || it.success != habit.isNegative }
        .toList()
}

private fun longestStreak(outcomes: List<Outcome>): Int {
    var longest = 0
    var run = 0
    for (outcome in outcomes) {
        run = if (outcome.success) run + 1 else 0
        longest = maxOf(longest, run)
    }
    return longest
}
