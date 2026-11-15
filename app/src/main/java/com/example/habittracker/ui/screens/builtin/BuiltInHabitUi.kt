package com.example.habittracker.ui.screens.builtin

import androidx.compose.runtime.Composable
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitKind
import com.example.habittracker.ui.screens.builtin.wakeup.WakeUpDayDialog
import com.example.habittracker.ui.screens.builtin.wakeup.WakeUpDetailsInfo
import com.example.habittracker.ui.screens.builtin.wakeup.WakeUpRow
import com.example.habittracker.ui.screens.builtin.wakeup.WakeUpSetupScreen
import com.example.habittracker.ui.screens.habits.BuiltInRowState
import java.time.LocalDate

// UI counterpart of BuiltInHabits: each built-in kind maps to its own composables here.

@Composable
fun BuiltInHabitRow(
    row: BuiltInRowState,
    today: LocalDate,
    onSetValue: (Int?) -> Unit,
    onClick: () -> Unit,
) {
    when (row.habit.kind) {
        HabitKind.WAKE_UP -> WakeUpRow(row = row, today = today, onSetValue = onSetValue, onClick = onClick)
        HabitKind.REGULAR, HabitKind.SCREEN_TIME -> Unit
    }
}

@Composable
fun BuiltInSetupScreen(kind: HabitKind, onDone: () -> Unit) {
    when (kind) {
        HabitKind.WAKE_UP -> WakeUpSetupScreen(onDone = onDone)
        HabitKind.REGULAR, HabitKind.SCREEN_TIME -> Unit
    }
}

@Composable
fun BuiltInDetailsInfo(
    habit: Habit,
    settings: Map<String, String>,
    todayMinimumMinutes: Int?,
) {
    when (habit.kind) {
        HabitKind.WAKE_UP -> WakeUpDetailsInfo(
            habit = habit,
            settings = settings,
            todayMinimumMinutes = todayMinimumMinutes,
        )
        HabitKind.REGULAR, HabitKind.SCREEN_TIME -> Unit
    }
}

@Composable
fun BuiltInDayDialog(
    habit: Habit,
    settings: Map<String, String>,
    date: LocalDate,
    value: Int?,
    priorSuccessCount: Int,
    onSave: (Int) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (habit.kind) {
        HabitKind.WAKE_UP -> WakeUpDayDialog(
            habit = habit,
            settings = settings,
            date = date,
            value = value,
            priorSuccessCount = priorSuccessCount,
            onSave = onSave,
            onRemove = onRemove,
            onDismiss = onDismiss,
        )
        HabitKind.REGULAR, HabitKind.SCREEN_TIME -> Unit
    }
}
