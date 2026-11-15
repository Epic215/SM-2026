package com.example.habittracker.domain.builtin

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class WakeUpHabitTest {

    /** Start 8:30, goal 6:30, 15 min every 3 successes, tolerance 10 min, deadline 30 min. */
    private val settings = WakeUpSettings.DEFAULT
    private val startDate = LocalDate.of(2026, 11, 1)

    private fun date(day: Int) = LocalDate.of(2026, 11, day)
    private fun time(hour: Int, minute: Int) = hour * 60 + minute
    private fun at(day: Int, hour: Int, minute: Int) = LocalDateTime.of(2026, 11, day, hour, minute)

    @Test
    fun firstMinimum_isOneStepEarlierThanStart() {
        assertEquals(time(8, 15), WakeUpHabit.minimumMinutes(settings, priorSuccessCount = 0))
    }

    @Test
    fun minimum_movesOnlyAfterEnoughSuccesses() {
        assertEquals(time(8, 15), WakeUpHabit.minimumMinutes(settings, 2))
        assertEquals(time(8, 0), WakeUpHabit.minimumMinutes(settings, 3))
        assertEquals(time(8, 0), WakeUpHabit.minimumMinutes(settings, 5))
        assertEquals(time(7, 45), WakeUpHabit.minimumMinutes(settings, 6))
    }

    @Test
    fun minimum_stopsAtMainGoal() {
        assertEquals(time(6, 30), WakeUpHabit.minimumMinutes(settings, 100))
    }

    @Test
    fun lateWakeIsNeutralNotFailure() {
        val minimum = time(8, 15)
        assertEquals(
            DayOutcome.NEUTRAL,
            WakeUpHabit.outcomeForMinimum(settings, minimum, date(1), time(8, 35), at(1, 10, 0)),
        )
    }

    @Test
    fun withinToleranceIsSuccess() {
        val minimum = time(8, 15)
        assertEquals(
            DayOutcome.SUCCESS,
            WakeUpHabit.outcomeForMinimum(settings, minimum, date(1), time(8, 25), at(1, 10, 0)),
        )
        assertEquals(
            DayOutcome.NEUTRAL,
            WakeUpHabit.outcomeForMinimum(settings, minimum, date(1), time(8, 26), at(1, 10, 0)),
        )
    }

    @Test
    fun wakingEarlierIsSuccess() {
        assertEquals(
            DayOutcome.SUCCESS,
            WakeUpHabit.outcomeForMinimum(settings, time(8, 15), date(1), time(6, 0), at(1, 10, 0)),
        )
    }

    @Test
    fun missingEntryTodayFailsAfterDeadline() {
        val minimum = time(8, 15)
        assertEquals(
            DayOutcome.PENDING,
            WakeUpHabit.outcomeForMinimum(settings, minimum, date(1), null, at(1, 8, 44)),
        )
        assertEquals(
            DayOutcome.FAILURE,
            WakeUpHabit.outcomeForMinimum(settings, minimum, date(1), null, at(1, 8, 45)),
        )
    }

    @Test
    fun evaluate_lateDaysDoNotAdvanceMinimum() {
        val values = mapOf(
            date(1) to time(8, 40),
            date(2) to time(8, 40),
            date(3) to time(8, 40),
        )
        val evaluation = WakeUpHabit.evaluate(settings, startDate, values, at(3, 12, 0))
        assertEquals(emptySet<LocalDate>(), evaluation.successDates)
        assertEquals(time(8, 15), evaluation.todayMinimumMinutes)
        assertEquals(DayOutcome.NEUTRAL, evaluation.todayOutcome)
    }

    @Test
    fun evaluate_threeSuccessesMoveMinimum() {
        val values = mapOf(
            date(1) to time(8, 10),
            date(2) to time(8, 10),
            date(3) to time(8, 10),
        )
        val evaluation = WakeUpHabit.evaluate(settings, startDate, values, at(4, 7, 0))
        assertEquals(setOf(date(1), date(2), date(3)), evaluation.successDates)
        assertEquals(time(8, 0), evaluation.todayMinimumMinutes)
        assertEquals(DayOutcome.PENDING, evaluation.todayOutcome)
    }

    @Test
    fun successfulDaysToGoal_skipsFreeFirstStep() {
        assertEquals(21, WakeUpHabit.successfulDaysToGoal(settings))
    }
}
