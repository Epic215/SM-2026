package com.example.habittracker.ui.screens.builtin

import androidx.compose.runtime.Composable
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitKind
import com.example.habittracker.ui.screens.builtin.screentime.ScreenTimeDetailsInfo
import com.example.habittracker.ui.screens.builtin.screentime.ScreenTimeRow
import com.example.habittracker.ui.screens.builtin.screentime.ScreenTimeSetupScreen
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
    hasUsageAccess: Boolean,
    onOpenUsageAccess: () -> Unit,
    onSetValue: (Int?) -> Unit,
    onClick: () -> Unit,
) {
    when (row.habit.kind) {
        HabitKind.WAKE_UP -> WakeUpRow(row = row, today = today, onSetValue = onSetValue, onClick = onClick)
        HabitKind.SCREEN_TIME -> ScreenTimeRow(
            row = row,
            hasUsageAccess = hasUsageAccess,
            onOpenUsageAccess = onOpenUsageAccess,
            onClick = onClick,
        )
        HabitKind.REGULAR -> Unit
    }
}

@Composable
fun BuiltInSetupScreen(kind: HabitKind, onDone: () -> Unit) {
    when (kind) {
        HabitKind.WAKE_UP -> WakeUpSetupScreen(onDone = onDone)
        HabitKind.SCREEN_TIME -> ScreenTimeSetupScreen(onDone = onDone)
        HabitKind.REGULAR -> Unit
    }
}

@Composable
fun BuiltInDetailsInfo(
    habit: Habit,
    settings: Map<String, String>,
    todayMinimumMinutes: Int?,
    todayMinutes: Int? = null,
) {
    when (habit.kind) {
        HabitKind.WAKE_UP -> WakeUpDetailsInfo(
            habit = habit,
            settings = settings,
            todayMinimumMinutes = todayMinimumMinutes,
        )
        HabitKind.SCREEN_TIME -> ScreenTimeDetailsInfo(
            habit = habit,
            settings = settings,
            todayMinutes = todayMinutes,
        )
        HabitKind.REGULAR -> Unit
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
        HabitKind.SCREEN_TIME, HabitKind.REGULAR -> Unit
    }
}
