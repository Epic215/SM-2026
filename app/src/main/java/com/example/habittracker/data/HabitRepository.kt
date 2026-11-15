package com.example.habittracker.data

import com.example.habittracker.data.local.HabitDao
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitEntry
import com.example.habittracker.data.model.HabitSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    fun observeAllEntries(): Flow<List<HabitEntry>> = dao.observeAllEntries()

    fun observeLastEntryDates(): Flow<Map<Long, LocalDate>> =
        dao.observeLastEntryDates().map { list -> list.associate { it.habitId to it.date } }

    fun observeSettings(habitId: Long): Flow<Map<String, String>> =
        dao.observeSettings(habitId).map { list -> list.associate { it.key to it.value } }

    fun observeAllSettings(): Flow<Map<Long, Map<String, String>>> =
        dao.observeAllSettings().map { list ->
            list.groupBy { it.habitId }.mapValues { (_, settings) -> settings.associate { it.key to it.value } }
        }

    suspend fun saveBuiltInHabit(habit: Habit, settings: Map<String, String>): Long {
        val id = if (habit.id == 0L) {
            dao.insertHabit(habit)
        } else {
            dao.updateHabit(habit)
            habit.id
        }
        dao.upsertSettings(settings.map { (key, value) -> HabitSetting(id, key, value) })
        return id
    }

    suspend fun saveEntry(habitId: Long, date: LocalDate, value: Int) =
        dao.upsertEntry(HabitEntry(habitId, date, value))

    suspend fun removeEntry(habitId: Long, date: LocalDate) = dao.deleteEntry(habitId, date)

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
