package com.example.habittracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDate

@Entity(
    tableName = "habit_entries",
    primaryKeys = ["habitId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("date")],
)
data class HabitEntry(
    val habitId: Long,
    val date: LocalDate,
    /** 1 for yes/no habits; amount (e.g. glasses, minutes) for measurable habits. */
    val value: Int = 1,
)
