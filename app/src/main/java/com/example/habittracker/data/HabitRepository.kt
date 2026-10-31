package com.example.habittracker.data

import com.example.habittracker.data.local.HabitDao
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class HabitRepository(private val dao: HabitDao) {

    fun observeActiveHabits(): Flow<List<Habit>> = dao.observeActiveHabits()

    fun observeArchivedHabits(): Flow<List<Habit>> = dao.observeArchivedHabits()

    fun observeHabit(id: Long): Flow<Habit?> = dao.observeHabit(id)

    suspend fun addHabit(habit: Habit): Long = dao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = dao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = dao.deleteHabit(habit)

    suspend fun setArchived(habit: Habit, archived: Boolean) =
        dao.updateHabit(habit.copy(archived = archived))

    fun observeEntries(from: LocalDate, to: LocalDate): Flow<List<HabitEntry>> =
        dao.observeEntries(from, to)

    fun observeEntriesForHabit(habitId: Long): Flow<List<HabitEntry>> =
        dao.observeEntriesForHabit(habitId)

    suspend fun toggleEntry(habitId: Long, date: LocalDate) {
        if (dao.getEntry(habitId, date) != null) {
            dao.deleteEntry(habitId, date)
        } else {
            dao.upsertEntry(HabitEntry(habitId, date))
        }
    }

    suspend fun setEntryValue(habitId: Long, date: LocalDate, value: Int) {
        if (value <= 0) {
            dao.deleteEntry(habitId, date)
        } else {
            dao.upsertEntry(HabitEntry(habitId, date, value))
        }
    }
}
