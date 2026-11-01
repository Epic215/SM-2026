package com.example.habittracker.ui.screens.edit

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.data.model.FrequencyType
import com.example.habittracker.data.model.Habit
import com.example.habittracker.ui.components.HabitColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek

data class HabitEditUiState(
    val isEditing: Boolean = false,
    val name: String = "",
    val description: String = "",
    val color: Int = HabitColors.first().toArgb(),
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val target: Int = 3,
    val daysOfWeek: Set<DayOfWeek> = emptySet(),
    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = !isSaving && name.isNotBlank() &&
            (frequencyType != FrequencyType.SPECIFIC_DAYS || daysOfWeek.isNotEmpty())
}

class HabitEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: HabitRepository,
) : ViewModel() {

    private val habitId: Long? = savedStateHandle.get<Long>(HABIT_ID_ARG)?.takeIf { it > 0 }
    private var original: Habit? = null

    private val _uiState = MutableStateFlow(HabitEditUiState(isEditing = habitId != null))
    val uiState: StateFlow<HabitEditUiState> = _uiState.asStateFlow()

    init {
        if (habitId != null) {
            viewModelScope.launch {
                val habit = repository.observeHabit(habitId).first() ?: return@launch
                original = habit
                _uiState.update {
                    it.copy(
                        name = habit.name,
                        description = habit.description,
                        color = habit.color,
                        frequencyType = habit.frequencyType,
                        target = if (habit.frequencyType == FrequencyType.TIMES_PER_WEEK) habit.target else it.target,
                        daysOfWeek = habit.daysOfWeek,
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }

    fun onDescriptionChange(description: String) = _uiState.update { it.copy(description = description) }

    fun onColorChange(color: Int) = _uiState.update { it.copy(color = color) }

    fun onFrequencyChange(type: FrequencyType) = _uiState.update { it.copy(frequencyType = type) }

    fun onTargetChange(target: Int) =
        _uiState.update { it.copy(target = target.coerceIn(MIN_TARGET, MAX_TARGET)) }

    fun onDayToggle(day: DayOfWeek) = _uiState.update {
        it.copy(daysOfWeek = if (day in it.daysOfWeek) it.daysOfWeek - day else it.daysOfWeek + day)
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true) }

        val target = if (state.frequencyType == FrequencyType.TIMES_PER_WEEK) state.target else 1
        val days = if (state.frequencyType == FrequencyType.SPECIFIC_DAYS) state.daysOfWeek else emptySet()

        viewModelScope.launch {
            val current = original
            if (current == null) {
                repository.addHabit(
                    Habit(
                        name = state.name.trim(),
                        description = state.description.trim(),
                        frequencyType = state.frequencyType,
                        target = target,
                        daysOfWeek = days,
                        color = state.color,
                    )
                )
            } else {
                repository.updateHabit(
                    current.copy(
                        name = state.name.trim(),
                        description = state.description.trim(),
                        frequencyType = state.frequencyType,
                        target = target,
                        daysOfWeek = days,
                        color = state.color,
                    )
                )
            }
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
        const val MIN_TARGET = 1
        const val MAX_TARGET = 6
    }
}
