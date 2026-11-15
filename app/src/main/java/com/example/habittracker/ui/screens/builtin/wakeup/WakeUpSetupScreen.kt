package com.example.habittracker.ui.screens.builtin.wakeup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habittracker.domain.builtin.WakeUpHabit
import com.example.habittracker.ui.AppViewModelProvider

private enum class TimeField { START, TARGET }

@Composable
fun WakeUpSetupScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WakeUpSetupViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    var editedTime by rememberSaveable { mutableStateOf<TimeField?>(null) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDone) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Wróć")
            }
            Text(
                text = if (uiState.isEditing) "Ustawienia wstawania" else "Nowy nawyk: wstawanie",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            if (uiState.isEditing) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Usuń nawyk",
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nazwa") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            SettingRow(label = "Cel startowy (teraz wstaję)") {
                TextButton(onClick = { editedTime = TimeField.START }) {
                    Text(formatMinutes(settings.startMinutes), style = MaterialTheme.typography.titleMedium)
                }
            }
            SettingRow(label = "Cel główny") {
                TextButton(onClick = { editedTime = TimeField.TARGET }) {
                    Text(formatMinutes(settings.targetMinutes), style = MaterialTheme.typography.titleMedium)
                }
            }
            SettingStepper(
                label = "Przesunięcie celu",
                value = "${settings.stepMinutes} min",
                onMinus = { viewModel.updateSettings { it.copy(stepMinutes = it.stepMinutes - 5) } },
                onPlus = { viewModel.updateSettings { it.copy(stepMinutes = it.stepMinutes + 5) } },
                canMinus = settings.stepMinutes > 5,
                canPlus = settings.stepMinutes < 60,
            )
            SettingStepper(
                label = "Co ile udanych dni",
                value = "${settings.stepEveryDays}",
                onMinus = { viewModel.updateSettings { it.copy(stepEveryDays = it.stepEveryDays - 1) } },
                onPlus = { viewModel.updateSettings { it.copy(stepEveryDays = it.stepEveryDays + 1) } },
                canMinus = settings.stepEveryDays > 1,
                canPlus = settings.stepEveryDays < 14,
            )
            SettingStepper(
                label = "Tolerancja spóźnienia",
                value = "${settings.toleranceMinutes} min",
                onMinus = { viewModel.updateSettings { it.copy(toleranceMinutes = it.toleranceMinutes - 5) } },
                onPlus = {
                    viewModel.updateSettings {
                        val tolerance = it.toleranceMinutes + 5
                        it.copy(toleranceMinutes = tolerance, deadlineMinutes = maxOf(it.deadlineMinutes, tolerance))
                    }
                },
                canMinus = settings.toleranceMinutes > 0,
                canPlus = settings.toleranceMinutes < 60,
            )
            SettingStepper(
                label = "Czas na zaznaczenie",
                value = "${settings.deadlineMinutes} min",
                onMinus = { viewModel.updateSettings { it.copy(deadlineMinutes = it.deadlineMinutes - 10) } },
                onPlus = { viewModel.updateSettings { it.copy(deadlineMinutes = it.deadlineMinutes + 10) } },
                canMinus = settings.deadlineMinutes - 10 >= maxOf(settings.toleranceMinutes, 10),
                canPlus = settings.deadlineMinutes < 180,
            )

            Text(
                text = if (uiState.isGoalEarlier) {
                    "Pierwszy cel minimalny: ${formatMinutes(WakeUpHabit.minimumMinutes(settings, 0))}. " +
                        "Cel główny ${formatMinutes(settings.targetMinutes)} po ok. " +
                        "${WakeUpHabit.successfulDaysToGoal(settings)} udanych dniach. " +
                        "Spóźnienie nie obniża celu; brak wpisu po ${settings.deadlineMinutes} min = nieudane."
                } else {
                    "Cel główny musi być wcześniejszy niż cel startowy."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (uiState.isGoalEarlier) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.tertiary
                },
            )
        }

        Button(
            onClick = { viewModel.save(onSaved = onDone) },
            enabled = uiState.canSave,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp),
        ) {
            Text("Zapisz")
        }
    }

    editedTime?.let { field ->
        WakeUpTimeDialog(
            title = if (field == TimeField.START) "Cel startowy" else "Cel główny",
            initialMinutes = if (field == TimeField.START) settings.startMinutes else settings.targetMinutes,
            onConfirm = { minutes ->
                viewModel.updateSettings {
                    if (field == TimeField.START) it.copy(startMinutes = minutes) else it.copy(targetMinutes = minutes)
                }
                editedTime = null
            },
            onDismiss = { editedTime = null },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usunąć nawyk?") },
            text = { Text("Cała historia tego nawyku zostanie trwale usunięta.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete(onDeleted = onDone)
                }) {
                    Text("Usuń", color = MaterialTheme.colorScheme.tertiary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj") }
            },
        )
    }
}

@Composable
private fun SettingRow(label: String, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            content()
        }
    }
}

@Composable
private fun SettingStepper(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    canMinus: Boolean,
    canPlus: Boolean,
) {
    SettingRow(label = label) {
        IconButton(onClick = onMinus, enabled = canMinus) {
            Icon(Icons.Rounded.Remove, contentDescription = "Mniej")
        }
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        IconButton(onClick = onPlus, enabled = canPlus) {
            Icon(Icons.Rounded.Add, contentDescription = "Więcej")
        }
    }
}
