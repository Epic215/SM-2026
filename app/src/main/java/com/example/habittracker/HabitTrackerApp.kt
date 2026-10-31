package com.example.habittracker

import android.app.Application
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.data.local.AppDatabase

class HabitTrackerApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
    val repository: HabitRepository by lazy { HabitRepository(database.habitDao()) }
}
