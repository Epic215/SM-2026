package com.example.habittracker.domain

import com.example.habittracker.data.model.FrequencyType
import com.example.habittracker.data.model.Habit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class HabitScheduleTest {

    private fun date(month: Int, day: Int) = LocalDate.of(2026, month, day)

    private fun habit(
        frequencyType: FrequencyType,
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

    @Test
    fun startOfWeek_returnsMonday() {
        assertEquals(date(11, 9), date(11, 15).startOfWeek())
        assertEquals(date(11, 9), date(11, 9).startOfWeek())
    }

    @Test
    fun daily_isActiveFromStartDate() {
        val habit = habit(FrequencyType.DAILY, startDate = date(11, 5))
        assertFalse(habit.isActiveOn(date(11, 4)))
        assertTrue(habit.isActiveOn(date(11, 5)))
        assertTrue(habit.isActiveOn(date(11, 6)))
    }

    @Test
    fun specificDays_isActiveOnlyOnChosenDays() {
        val habit = habit(FrequencyType.SPECIFIC_DAYS, daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY))
        assertTrue(habit.isActiveOn(date(11, 9)))
        assertFalse(habit.isActiveOn(date(11, 10)))
        assertTrue(habit.isActiveOn(date(11, 13)))
    }

    @Test
    fun none_isNeverActive() {
        assertFalse(habit(FrequencyType.NONE).isActiveOn(date(11, 9)))
    }

    @Test
    fun timesPerWeek_isDueUntilTargetReached() {
        val habit = habit(FrequencyType.TIMES_PER_WEEK, target = 2)
        val today = date(11, 11)

        assertTrue(habit.isDueOn(today, setOf(date(11, 9))))
        assertFalse(habit.isDueOn(today, setOf(date(11, 9), date(11, 10))))
    }

    @Test
    fun timesPerWeek_isDueWhenDoneToday() {
        val habit = habit(FrequencyType.TIMES_PER_WEEK, target = 2)
        val today = date(11, 11)
        assertTrue(habit.isDueOn(today, setOf(date(11, 9), date(11, 10), today)))
    }

    @Test
    fun timesPerWeek_ignoresPreviousWeek() {
        val habit = habit(FrequencyType.TIMES_PER_WEEK, target = 2)
        assertTrue(habit.isDueOn(date(11, 11), setOf(date(11, 5), date(11, 6))))
    }
}
