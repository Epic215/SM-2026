package com.example.habittracker.domain.builtin

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ScreenTimeHabitTest {

    private val settings = ScreenTimeSettings(limitMinutes = 60, packages = setOf("com.instagram.android"))
    private val today = LocalDate.of(2026, 11, 15)
    private val now = LocalDateTime.of(2026, 11, 15, 18, 0)

    @Test
    fun underLimitToday_isPending() {
        assertEquals(DayOutcome.PENDING, ScreenTimeHabit.outcome(settings, today, 40, now))
    }

    @Test
    fun overLimit_isFailure() {
        assertEquals(DayOutcome.FAILURE, ScreenTimeHabit.outcome(settings, today, 61, now))
    }

    @Test
    fun underLimitOnPastDay_isSuccess() {
        assertEquals(
            DayOutcome.SUCCESS,
            ScreenTimeHabit.outcome(settings, today.minusDays(1), 55, now),
        )
    }

    @Test
    fun overLimitOnPastDay_isFailure() {
        assertEquals(
            DayOutcome.FAILURE,
            ScreenTimeHabit.outcome(settings, today.minusDays(1), 90, now),
        )
    }

    @Test
    fun missingPastEntry_isFailure() {
        assertEquals(
            DayOutcome.FAILURE,
            ScreenTimeHabit.outcome(settings, today.minusDays(1), null, now),
        )
    }

    @Test
    fun settingsRoundTrip() {
        val original = ScreenTimeSettings(
            limitMinutes = 120,
            packages = setOf("com.discord", "com.android.chrome"),
        )
        assertEquals(original, ScreenTimeSettings.from(original.toMap()))
    }
}
