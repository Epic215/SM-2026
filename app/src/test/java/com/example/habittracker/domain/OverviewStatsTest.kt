package com.example.habittracker.domain

import com.example.habittracker.data.model.FrequencyType
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class OverviewStatsTest {

    /** Wednesday. */
    private val today = LocalDate.of(2026, 11, 11)

    private fun date(month: Int, day: Int) = LocalDate.of(2026, month, day)

    private fun habit(
        id: Long,
        type: HabitType = HabitType.POSITIVE,
        frequencyType: FrequencyType = FrequencyType.DAILY,
        target: Int = 1,
    ) = Habit(
        id = id,
        name = "Habit $id",
        color = 0,
        type = type,
        frequencyType = frequencyType,
        target = target,
        startDate = date(11, 1),
    )

    @Test
    fun dayCompletion_isShareOfDueHabitsDone() {
        val habits = listOf(habit(1), habit(2))
        val done = mapOf(1L to setOf(today))
        assertEquals(0.5f, dayCompletion(habits, done, today)!!, 0.001f)
    }

    @Test
    fun dayCompletion_ignoresNegativeHabits() {
        val habits = listOf(habit(1), habit(2, type = HabitType.NEGATIVE))
        val done = mapOf(1L to setOf(today))
        assertEquals(1f, dayCompletion(habits, done, today)!!, 0.001f)
    }

    @Test
    fun dayCompletion_isNullWithoutDueHabits() {
        assertNull(dayCompletion(emptyList(), emptyMap(), today))
    }

    @Test
    fun dayCompletion_skipsWeeklyHabitWithTargetReached() {
        val habits = listOf(habit(1), habit(2, frequencyType = FrequencyType.TIMES_PER_WEEK, target = 1))
        val done = mapOf(1L to setOf(today), 2L to setOf(date(11, 9)))
        assertEquals(1f, dayCompletion(habits, done, today)!!, 0.001f)
    }

    @Test
    fun overview_separatesCompletionsAndSlips() {
        val habits = listOf(habit(1), habit(2, type = HabitType.NEGATIVE))
        val done = mapOf(
            1L to setOf(date(11, 9), date(11, 10)),
            2L to setOf(date(11, 10)),
        )
        val overview = calculateOverview(habits, done, StatsPeriod.WEEK, today)
        assertEquals(2, overview.completions)
        assertEquals(1, overview.slips)
    }

    @Test
    fun overview_sortsHabitsBySuccessRate() {
        val habits = listOf(habit(1), habit(2), habit(3, frequencyType = FrequencyType.NONE))
        val done = mapOf(
            1L to setOf(date(11, 9)),
            2L to setOf(date(11, 9), date(11, 10)),
        )
        val overview = calculateOverview(habits, done, StatsPeriod.WEEK, today)
        assertEquals(listOf(2L, 1L, 3L), overview.habits.map { it.habit.id })
    }
}
