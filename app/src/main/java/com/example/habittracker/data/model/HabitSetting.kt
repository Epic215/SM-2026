package com.example.habittracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

/** Key-value settings of built-in habits, so a new built-in habit needs no schema change. */
@Entity(
    tableName = "habit_settings",
    primaryKeys = ["habitId", "key"],
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
data class HabitSetting(
    val habitId: Long,
    val key: String,
    val value: String,
)
