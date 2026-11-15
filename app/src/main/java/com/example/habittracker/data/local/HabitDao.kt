package com.example.habittracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitEntry
import com.example.habittracker.data.model.HabitSetting
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class LastEntryDate(val habitId: Long, val date: LocalDate)

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY id")
    fun observeActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE archived = 1 ORDER BY id")
    fun observeArchivedHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun observeHabit(id: Long): Flow<Habit?>

    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habit_entries WHERE date BETWEEN :from AND :to")
    fun observeEntries(from: LocalDate, to: LocalDate): Flow<List<HabitEntry>>

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId ORDER BY date")
    fun observeEntriesForHabit(habitId: Long): Flow<List<HabitEntry>>

    @Query("SELECT * FROM habit_entries")
    fun observeAllEntries(): Flow<List<HabitEntry>>

    @Query("SELECT habitId, MAX(date) AS date FROM habit_entries GROUP BY habitId")
    fun observeLastEntryDates(): Flow<List<LastEntryDate>>

    @Query("SELECT * FROM habit_settings WHERE habitId = :habitId")
    fun observeSettings(habitId: Long): Flow<List<HabitSetting>>

    @Query("SELECT * FROM habit_settings")
    fun observeAllSettings(): Flow<List<HabitSetting>>

    @Upsert
    suspend fun upsertSettings(settings: List<HabitSetting>)

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId AND date = :date")
    suspend fun getEntry(habitId: Long, date: LocalDate): HabitEntry?

    @Upsert
    suspend fun upsertEntry(entry: HabitEntry)

    @Query("DELETE FROM habit_entries WHERE habitId = :habitId AND date = :date")
    suspend fun deleteEntry(habitId: Long, date: LocalDate)
}
