package com.example.habittracker.ui.screens.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habittracker.ui.AppViewModelProvider
import com.example.habittracker.ui.screens.builtin.BuiltInDayDialog
import com.example.habittracker.ui.screens.calendar.components.DayHabitsList
import com.example.habittracker.ui.screens.details.components.CalendarDayCell
import com.example.habittracker.ui.screens.details.components.MonthCalendar
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("pl"))

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = viewModel.today
    val colors = MaterialTheme.colorScheme
    var editingHabitId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editing = uiState.dayItems.firstOrNull { it.habit.id == editingHabitId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Kalendarz",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        MonthCalendar(today = today) { date ->
            val ratio = uiState.completion[date]
            val isFuture = date.isAfter(today)
            CalendarDayCell(
                day = date.dayOfMonth,
                fill = ratio?.takeIf { it > 0f }?.let { colors.primary.copy(alpha = 0.2f + 0.8f * it) },
                border = when (date) {
                    uiState.selectedDate -> colors.onSurface
                    today -> colors.primary
                    else -> null
                },
                textColor = when {
                    ratio != null && ratio >= 0.6f -> colors.onPrimary
                    isFuture -> colors.onSurfaceVariant.copy(alpha = 0.4f)
                    else -> colors.onSurface
                },
                bold = date == today,
                enabled = !isFuture,
                onClick = { viewModel.selectDate(date) },
            )
        }

        Text(
            text = uiState.selectedDate.format(dayFormatter).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
        )
        DayHabitsList(
            items = uiState.dayItems,
            onToggle = { habitId -> viewModel.toggle(habitId, uiState.selectedDate) },
            onEditBuiltIn = { editingHabitId = it.habit.id },
        )
    }

    editing?.let { item ->
        BuiltInDayDialog(
            habit = item.habit,
            settings = item.settings,
            date = uiState.selectedDate,
            value = item.value,
            priorSuccessCount = item.priorSuccessCount,
            onSave = { minutes ->
                viewModel.setDayValue(item.habit.id, uiState.selectedDate, minutes)
                editingHabitId = null
            },
            onRemove = {
                viewModel.setDayValue(item.habit.id, uiState.selectedDate, null)
                editingHabitId = null
            },
            onDismiss = { editingHabitId = null },
        )
    }
}
