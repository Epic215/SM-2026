package com.example.habittracker.domain

import com.example.habittracker.data.model.FrequencyType
import com.example.habittracker.data.model.Habit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

fun LocalDate.startOfWeek(): LocalDate = with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

fun Habit.isActiveOn(date: LocalDate): Boolean {
    if (date.isBefore(startDate)) return false
    return when (frequencyType) {
        FrequencyType.DAILY, FrequencyType.TIMES_PER_WEEK -> true
        FrequencyType.SPECIFIC_DAYS -> date.dayOfWeek in daysOfWeek
        FrequencyType.NONE -> false
    }
}

/** A TIMES_PER_WEEK habit stops being due once the weekly target is reached. */
fun Habit.isDueOn(date: LocalDate, doneDates: Set<LocalDate>): Boolean {
    if (!isActiveOn(date)) return false
    if (frequencyType != FrequencyType.TIMES_PER_WEEK || date in doneDates) return true
    val weekStart = date.startOfWeek()
    return doneDates.count { it.startOfWeek() == weekStart } < target
}
