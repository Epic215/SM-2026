package com.example.habittracker.data.local

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun localDateToEpochDay(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    @TypeConverter
    fun daysOfWeekToMask(days: Set<DayOfWeek>): Int =
        days.fold(0) { mask, day -> mask or (1 shl day.ordinal) }

    @TypeConverter
    fun maskToDaysOfWeek(mask: Int): Set<DayOfWeek> =
        DayOfWeek.entries.filterTo(mutableSetOf()) { (mask and (1 shl it.ordinal)) != 0 }
}
