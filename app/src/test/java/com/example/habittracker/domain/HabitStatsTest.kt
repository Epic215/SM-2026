package com.example.habittracker.domain

import com.example.habittracker.data.model.FrequencyType
import com.example.habittracker.data.model.Habit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class HabitStatsTest {

    /** Wednesday. */
    private val today = LocalDate.of(2026, 11, 11)

    private fun date(month: Int, day: Int) = LocalDate.of(2026, month, day)

    private fun habit(
        frequencyType: FrequencyType = FrequencyType.DAILY,
        target: Int = 1,
        daysOfWeek: Set<DayOfWeek> = emptySet(),
        startDate: LocalDate = date(11, 1),
    ) = Habit(
        name = "Test",
        color = 0,
        frequencyType = frequencyType,
        target = target,
        daysOfWeek = daysOfWeek,
        startDate = startDate,
    )

    private fun stats(
        habit: Habit,
        vararg done: LocalDate,
        period: StatsPeriod = StatsPeriod.WEEK,
    ) = calculateStats(habit, done.toSet(), period, today)

    @Test
    fun daily_unfinishedTodayDoesNotBreakStreak() {
        val result = stats(habit(), date(11, 8), date(11, 9), date(11, 10))
        assertEquals(3, result.currentStreak)
    }

    @Test
    fun daily_doneTodayExtendsStreak() {
        val result = stats(habit(), date(11, 9), date(11, 10), today)
        assertEquals(3, result.currentStreak)
    }

    @Test
    fun daily_gapBreaksStreak() {
        val result = stats(
            habit(),
            date(11, 3), date(11, 4), date(11, 5), date(11, 6),
            date(11, 9), date(11, 10),
        )
        assertEquals(2, result.currentStreak)
        assertEquals(4, result.longestStreak)
        assertEquals(StreakUnit.DAYS, result.streakUnit)
    }

    @Test
    fun specificDays_unscheduledDaysDoNotBreakStreak() {
        val habit = habit(
            FrequencyType.SPECIFIC_DAYS,
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        )
        val result = stats(habit, date(11, 2), date(11, 4), date(11, 6), date(11, 9))
        assertEquals(4, result.currentStreak)
    }

    @Test
    fun daily_successRateSkipsUnfinishedToday() {
        assertEquals(1f, stats(habit(), date(11, 9), date(11, 10)).successRate!!, 0.001f)
        assertEquals(0.5f, stats(habit(), date(11, 9)).successRate!!, 0.001f)
    }

    @Test
    fun monthPeriod_countsOnlyCurrentMonth() {
        val result = stats(habit(), date(10, 31), date(11, 1), date(11, 5), period = StatsPeriod.MONTH)
        assertEquals(2, result.periodCompletions)
        assertEquals(3, result.totalCompletions)
    }

    @Test
    fun timesPerWeek_countsStreakInWeeks() {
        val habit = habit(FrequencyType.TIMES_PER_WEEK, target = 2)
        val result = stats(
            habit,
            date(10, 27), date(10, 28),
            date(11, 3), date(11, 5),
            date(11, 10),
        )
        assertEquals(StreakUnit.WEEKS, result.streakUnit)
        assertEquals(2, result.currentStreak)
        assertEquals(2, result.longestStreak)
    }

    @Test
    fun timesPerWeek_missedWeekBreaksStreak() {
        val habit = habit(FrequencyType.TIMES_PER_WEEK, target = 2)
        val result = stats(habit, date(10, 27), date(11, 3), date(11, 5))
        assertEquals(1, result.currentStreak)
        assertEquals(1, result.longestStreak)
    }

    @Test
    fun none_hasOnlyCompletions() {
        val result = stats(habit(FrequencyType.NONE), date(11, 1), date(11, 9), date(11, 10))
        assertEquals(2, result.periodCompletions)
        assertEquals(3, result.totalCompletions)
        assertNull(result.successRate)
        assertEquals(0, result.currentStreak)
        assertEquals(0, result.longestStreak)
    }

    @Test
    fun decidedUnmarkedTodayBreaksStreak() {
        val result = calculateStats(
            habit(),
            setOf(date(11, 9), date(11, 10)),
            StatsPeriod.WEEK,
            today,
            isTodayDecided = true,
        )
        assertEquals(0, result.currentStreak)
        assertEquals(2, result.longestStreak)
    }

    @Test
    fun backfilledDaysBeforeStartDateCount() {
        val result = stats(habit(startDate = today), date(11, 9), date(11, 10))
        assertEquals(2, result.currentStreak)
    }
}
