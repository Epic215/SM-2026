package com.example.habittracker.ui.screens.builtin.wakeup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.habittracker.data.model.Habit
import com.example.habittracker.domain.builtin.DayOutcome
import com.example.habittracker.domain.builtin.WakeUpHabit
import com.example.habittracker.domain.builtin.WakeUpSettings
import com.example.habittracker.ui.screens.habits.BuiltInRowState
import java.time.LocalDate
import java.time.LocalTime

fun formatMinutes(minutes: Int): String = "%d:%02d".format(minutes / 60, minutes % 60)

private fun currentMinutes(): Int = LocalTime.now().let { it.hour * 60 + it.minute }

@Composable
fun WakeUpRow(
    row: BuiltInRowState,
    today: LocalDate,
    onSetValue: (Int?) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings = remember(row.settings) { WakeUpSettings.from(row.settings) }
    val minimum = row.minimumMinutes ?: WakeUpHabit.minimumMinutes(settings, 0)
    val habitColor = Color(row.habit.color)
    val colors = MaterialTheme.colorScheme
    var showDialog by rememberSaveable { mutableStateOf(false) }

    val (status, statusColor) = when (row.outcome) {
        DayOutcome.SUCCESS -> "Wstałeś o ${formatMinutes(row.todayValue ?: 0)}" to colors.secondary
        DayOutcome.NEUTRAL -> "Spóźnienie: ${formatMinutes(row.todayValue ?: 0)}" to colors.tertiary
        DayOutcome.FAILURE -> "Nieudane: brak wpisu" to colors.tertiary
        DayOutcome.PENDING -> "Zaznacz do ${formatMinutes(minimum + settings.deadlineMinutes)}" to colors.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(width = 4.dp, height = 44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(habitColor)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = row.habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Cel minimalny: ${formatMinutes(minimum)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = statusColor,
                )
            }
            if (row.todayValue == null) {
                FilledTonalButton(onClick = { onSetValue(currentMinutes()) }) { Text("Wstałem") }
            } else {
                TextButton(onClick = { showDialog = true }) { Text("Zmień") }
                IconButton(onClick = { onSetValue(null) }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Usuń wpis")
                }
            }
        }
    }

    if (showDialog) {
        WakeUpTimeDialog(
            title = "O której wstałeś?",
            initialMinutes = row.todayValue ?: currentMinutes(),
            onConfirm = { minutes ->
                onSetValue(minutes)
                showDialog = false
            },
            onDismiss = { showDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WakeUpTimeDialog(
    title: String,
    initialMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
    onRemove: (() -> Unit)? = null,
) {
    val state = rememberTimePickerState(
        initialHour = initialMinutes / 60,
        initialMinute = initialMinutes % 60,
        is24Hour = true,
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                TimePicker(state = state)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (onRemove != null) {
                        TextButton(onClick = onRemove) {
                            Text("Usuń wpis", color = MaterialTheme.colorScheme.tertiary)
                        }
                        Spacer(Modifier.weight(1f))
                    }
                    TextButton(onClick = onDismiss) { Text("Anuluj") }
                    TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) { Text("Zapisz") }
                }
            }
        }
    }
}

@Composable
fun WakeUpDetailsInfo(
    habit: Habit,
    settings: Map<String, String>,
    todayMinimumMinutes: Int?,
    modifier: Modifier = Modifier,
) {
    val wakeUp = remember(settings) { WakeUpSettings.from(settings) }
    val minimum = todayMinimumMinutes ?: WakeUpHabit.minimumMinutes(wakeUp, 0)
    Text(
        text = "Cel minimalny: ${formatMinutes(minimum)} · cel główny: ${formatMinutes(wakeUp.targetMinutes)}" +
            " · tolerancja ${wakeUp.toleranceMinutes} min",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
fun WakeUpDayDialog(
    habit: Habit,
    settings: Map<String, String>,
    date: LocalDate,
    value: Int?,
    priorSuccessCount: Int,
    onSave: (Int) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    val wakeUp = remember(settings) { WakeUpSettings.from(settings) }
    WakeUpTimeDialog(
        title = "O której wstałeś ${date.dayOfMonth}.${"%02d".format(date.monthValue)}?",
        initialMinutes = value ?: WakeUpHabit.minimumMinutes(wakeUp, priorSuccessCount),
        onConfirm = onSave,
        onDismiss = onDismiss,
        onRemove = if (value != null) onRemove else null,
    )
}
