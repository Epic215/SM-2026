package com.example.habittracker.domain

import com.example.habittracker.data.model.FrequencyType
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class NegativeHabitStatsTest {

    /** Wednesday. */
    private val today = LocalDate.of(2026, 11, 11)

    private fun date(month: Int, day: Int) = LocalDate.of(2026, month, day)

    private fun habit(
        frequencyType: FrequencyType = FrequencyType.DAILY,
        target: Int = 1,
        startDate: LocalDate = date(11, 1),
    ) = Habit(
        name = "Test",
        color = 0,
        type = HabitType.NEGATIVE,
        frequencyType = frequencyType,
        target = target,
        startDate = startDate,
    )

    private fun stats(habit: Habit, vararg slips: LocalDate) =
        calculateStats(habit, slips.toSet(), StatsPeriod.WEEK, today)

    @Test
    fun daily_streakCountsCleanDaysSinceSlip() {
        val result = stats(habit(), date(11, 5))
        assertEquals(5, result.currentStreak)
        assertEquals(5, result.longestStreak)
        assertEquals(6, result.daysSinceLastSlip)
    }

    @Test
    fun daily_slipTodayResetsStreak() {
        val result = stats(habit(), today)
        assertEquals(0, result.currentStreak)
        assertEquals(0, result.daysSinceLastSlip)
    }

    @Test
    fun daily_noSlipsCountsFromStartDate() {
        val result = stats(habit(startDate = date(11, 8)))
        assertEquals(3, result.currentStreak)
        assertEquals(3, result.daysSinceLastSlip)
        assertEquals(1f, result.successRate!!, 0.001f)
    }

    @Test
    fun weekly_overLimitWeekBreaksStreak() {
        val result = stats(habit(FrequencyType.TIMES_PER_WEEK, target = 1), date(11, 3), date(11, 4), date(11, 10))
        assertEquals(StreakUnit.WEEKS, result.streakUnit)
        assertEquals(0, result.currentStreak)
        assertEquals(1, result.longestStreak)
    }

    @Test
    fun weekly_currentWeekOverLimitCountsImmediately() {
        val result = stats(habit(FrequencyType.TIMES_PER_WEEK, target = 1), date(11, 9), date(11, 10))
        assertEquals(0, result.currentStreak)
        assertEquals(2, result.longestStreak)
    }

    @Test
    fun periodCompletionsCountSlips() {
        assertEquals(2, stats(habit(), date(11, 9), date(11, 10)).periodCompletions)
    }
}
