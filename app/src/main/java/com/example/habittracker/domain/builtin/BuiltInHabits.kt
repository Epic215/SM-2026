package com.example.habittracker.domain.builtin

import com.example.habittracker.data.model.HabitKind

object BuiltInHabits {

    val all: List<BuiltInHabit> = listOf(
        WakeUpHabit,
    )

    fun find(kind: HabitKind): BuiltInHabit? = all.firstOrNull { it.kind == kind }
}
