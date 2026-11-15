package com.example.habittracker.ui.screens.builtin.wakeup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitKind
import com.example.habittracker.domain.builtin.WakeUpHabit
import com.example.habittracker.domain.builtin.WakeUpSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WakeUpSetupUiState(
    val isEditing: Boolean = false,
    val name: String = WakeUpHabit.name,
    val settings: WakeUpSettings = WakeUpSettings.DEFAULT,
    val isSaving: Boolean = false,
) {
    val isGoalEarlier: Boolean get() = settings.targetMinutes < settings.startMinutes
    val canSave: Boolean get() = !isSaving && name.isNotBlank() && isGoalEarlier
}

class WakeUpSetupViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: HabitRepository,
) : ViewModel() {

    private val habitId: Long? = savedStateHandle.get<Long>(HABIT_ID_ARG)?.takeIf { it > 0 }
    private var original: Habit? = null

    private val _uiState = MutableStateFlow(WakeUpSetupUiState(isEditing = habitId != null))
    val uiState: StateFlow<WakeUpSetupUiState> = _uiState.asStateFlow()

    init {
        if (habitId != null) {
            viewModelScope.launch {
                val habit = repository.observeHabit(habitId).first() ?: return@launch
                val settings = repository.observeSettings(habitId).first()
                original = habit
                _uiState.update { it.copy(name = habit.name, settings = WakeUpSettings.from(settings)) }
            }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }

    fun updateSettings(transform: (WakeUpSettings) -> WakeUpSettings) =
        _uiState.update { it.copy(settings = transform(it.settings)) }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val habit = original?.copy(name = state.name.trim()) ?: Habit(
                name = state.name.trim(),
                kind = HabitKind.WAKE_UP,
                color = WakeUpHabit.defaultColor,
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
    }
}
