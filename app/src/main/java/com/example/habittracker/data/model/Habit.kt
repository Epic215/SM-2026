package com.example.habittracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate

enum class HabitType { POSITIVE, NEGATIVE }

enum class FrequencyType { DAILY, TIMES_PER_WEEK, SPECIFIC_DAYS, NONE }

enum class HabitKind { REGULAR, WAKE_UP, SCREEN_TIME }

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val type: HabitType = HabitType.POSITIVE,
    val kind: HabitKind = HabitKind.REGULAR,
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    /** TIMES_PER_WEEK only: minimum per week for POSITIVE, maximum per week for NEGATIVE. */
    val target: Int = 1,
    /** SPECIFIC_DAYS only. */
    val daysOfWeek: Set<DayOfWeek> = emptySet(),
    /** ARGB. */
    val color: Int,
    val startDate: LocalDate = LocalDate.now(),
    val archived: Boolean = false,
)
