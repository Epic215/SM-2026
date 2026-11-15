package com.example.habittracker.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.habittracker.HabitTrackerApp
import com.example.habittracker.ui.screens.builtin.wakeup.WakeUpSetupViewModel
import com.example.habittracker.ui.screens.calendar.CalendarViewModel
import com.example.habittracker.ui.screens.details.HabitDetailsViewModel
import com.example.habittracker.ui.screens.edit.HabitEditViewModel
import com.example.habittracker.ui.screens.habits.HabitsViewModel
import com.example.habittracker.ui.screens.stats.StatsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { HabitsViewModel(habitTrackerApp().repository) }
        initializer { HabitEditViewModel(createSavedStateHandle(), habitTrackerApp().repository) }
        initializer { HabitDetailsViewModel(createSavedStateHandle(), habitTrackerApp().repository) }
        initializer { CalendarViewModel(habitTrackerApp().repository) }
        initializer { StatsViewModel(habitTrackerApp().repository) }
        initializer { WakeUpSetupViewModel(createSavedStateHandle(), habitTrackerApp().repository) }
    }
}

private fun CreationExtras.habitTrackerApp(): HabitTrackerApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HabitTrackerApp
