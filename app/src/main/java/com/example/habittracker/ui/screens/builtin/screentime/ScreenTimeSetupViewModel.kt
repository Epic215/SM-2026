package com.example.habittracker.ui.screens.builtin.screentime

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitKind
import com.example.habittracker.data.usage.InstalledApp
import com.example.habittracker.data.usage.UsageStatsReader
import com.example.habittracker.domain.builtin.ScreenTimeHabit
import com.example.habittracker.domain.builtin.ScreenTimeSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ScreenTimeSetupUiState(
    val isEditing: Boolean = false,
    val name: String = ScreenTimeHabit.name,
    val settings: ScreenTimeSettings = ScreenTimeSettings.DEFAULT,
    val apps: List<InstalledApp> = emptyList(),
    val hasUsageAccess: Boolean = false,
    val appsLoading: Boolean = true,
    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = !isSaving && name.isNotBlank() && settings.packages.isNotEmpty() && settings.limitMinutes > 0
}

class ScreenTimeSetupViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: HabitRepository,
    private val usageStatsReader: UsageStatsReader,
) : ViewModel() {

    private val habitId: Long? = savedStateHandle.get<Long>(HABIT_ID_ARG)?.takeIf { it > 0 }
    private var original: Habit? = null

    private val _uiState = MutableStateFlow(ScreenTimeSetupUiState(isEditing = habitId != null))
    val uiState: StateFlow<ScreenTimeSetupUiState> = _uiState.asStateFlow()

    init {
        refreshPermission()
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) { usageStatsReader.installedLaunchableApps() }
            _uiState.update { it.copy(apps = apps, appsLoading = false) }
        }
        if (habitId != null) {
            viewModelScope.launch {
                val habit = repository.observeHabit(habitId).first() ?: return@launch
                val settings = repository.observeSettings(habitId).first()
                original = habit
                _uiState.update {
                    it.copy(name = habit.name, settings = ScreenTimeSettings.from(settings))
                }
            }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }

    fun onLimitChange(limitMinutes: Int) = _uiState.update {
        it.copy(settings = it.settings.copy(limitMinutes = limitMinutes.coerceIn(MIN_LIMIT, MAX_LIMIT)))
    }

    fun onAppToggle(packageName: String) = _uiState.update { state ->
        val packages = state.settings.packages
        state.copy(
            settings = state.settings.copy(
                packages = if (packageName in packages) packages - packageName else packages + packageName,
            )
        )
    }

    fun refreshPermission() {
        _uiState.update { it.copy(hasUsageAccess = usageStatsReader.hasUsageAccess()) }
    }

    fun openUsageAccessSettings() = usageStatsReader.openUsageAccessSettings()

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val habit = original?.copy(name = state.name.trim()) ?: Habit(
                name = state.name.trim(),
                kind = HabitKind.SCREEN_TIME,
                color = ScreenTimeHabit.defaultColor,
            )
            repository.saveBuiltInHabit(habit, state.settings.toMap())
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val current = original ?: return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.deleteHabit(current)
            onDeleted()
        }
    }

    companion object {
        const val HABIT_ID_ARG = "habitId"
        const val MIN_LIMIT = 15
        const val MAX_LIMIT = 12 * 60
    }
}
