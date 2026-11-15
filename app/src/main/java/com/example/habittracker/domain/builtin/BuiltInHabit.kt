package com.example.habittracker.domain.builtin

import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitKind
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * SUCCESS advances the minimum goal.
 * NEUTRAL ("spóźnienie") does not advance and is not shown as a hard failure.
 * FAILURE is mainly a missing entry after the deadline.
 */
enum class DayOutcome { SUCCESS, NEUTRAL, FAILURE, PENDING }

/**
 * A habit with its own rules. Register new ones in [BuiltInHabits]; the rest of the app
 * only asks [outcome] which days were successful.
 */
interface BuiltInHabit {
    val kind: HabitKind
    val name: String
    val description: String

    /** ARGB. */
    val defaultColor: Int

    fun defaultSettings(): Map<String, String>

    /** [value] is the entry logged for [date], or null when nothing was logged. */
    fun outcome(
        habit: Habit,
        settings: Map<String, String>,
        date: LocalDate,
        value: Int?,
        now: LocalDateTime,
    ): DayOutcome
}
