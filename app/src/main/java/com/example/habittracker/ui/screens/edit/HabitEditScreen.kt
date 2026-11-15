package com.example.habittracker.ui.screens.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habittracker.data.model.FrequencyType
import com.example.habittracker.data.model.HabitType
import com.example.habittracker.ui.AppViewModelProvider
import com.example.habittracker.ui.components.HabitColors
import com.example.habittracker.ui.screens.habits.components.shortLabel
import java.time.DayOfWeek

private val FieldShape = RoundedCornerShape(14.dp)

@Composable
fun HabitEditScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Wróć")
            }
            Text(
                text = if (uiState.isEditing) "Edytuj nawyk" else "Nowy nawyk",
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Nazwa") },
                    singleLine = true,
                    shape = FieldShape,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Opis (opcjonalnie)") },
                    shape = FieldShape,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Section("Typ") {
                TypePicker(
                    selected = uiState.type,
                    enabled = !uiState.isEditing,
                    onSelect = viewModel::onTypeChange,
                )
            }

            Section("Kolor") {
                ColorPicker(selected = uiState.color, onSelect = viewModel::onColorChange)
            }

            if (uiState.type == HabitType.NEGATIVE) {
                Section("Jak to działa") {
                    Text(
                        text = "Zaznaczasz dni, w których to wystąpiło. Każdy dzień bez wpisu liczy się jako sukces.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Section("Częstotliwość") {
                    FrequencyPicker(
                        selected = uiState.frequencyType,
                        onSelect = viewModel::onFrequencyChange,
                    )
                    when (uiState.frequencyType) {
                        FrequencyType.TIMES_PER_WEEK -> TargetStepper(
                            target = uiState.target,
                            onChange = viewModel::onTargetChange,
                        )
                        FrequencyType.SPECIFIC_DAYS -> DaysPicker(
                            selected = uiState.daysOfWeek,
                            onToggle = viewModel::onDayToggle,
                        )
                        else -> Unit
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.save(onSaved = onBack) },
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usunąć nawyk?") },
            text = { Text("Cała historia tego nawyku zostanie trwale usunięta.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete(onDeleted = onBack)
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
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPicker(selected: Int, onSelect: (Int) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HabitColors.forEach { color ->
            val argb = color.toArgb()
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onSelect(argb) },
                contentAlignment = Alignment.Center,
            ) {
                if (argb == selected) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TypePicker(selected: HabitType, enabled: Boolean, onSelect: (HabitType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HabitType.entries.forEach { type ->
            FilterChip(
                selected = type == selected,
                onClick = { onSelect(type) },
                enabled = enabled,
                label = {
                    Text(if (type == HabitType.POSITIVE) "Pozytywny (buduję)" else "Negatywny (ograniczam)")
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FrequencyPicker(selected: FrequencyType, onSelect: (FrequencyType) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FrequencyType.entries.forEach { type ->
            FilterChip(
                selected = type == selected,
                onClick = { onSelect(type) },
                label = { Text(type.label()) },
            )
        }
    }
}

@Composable
private fun TargetStepper(target: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Ile razy w tygodniu",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = { onChange(target - 1) },
            enabled = target > HabitEditViewModel.MIN_TARGET,
        ) {
            Icon(Icons.Rounded.Remove, contentDescription = "Mniej")
        }
        Text(
            text = target.toString(),
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(
            onClick = { onChange(target + 1) },
            enabled = target < HabitEditViewModel.MAX_TARGET,
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Więcej")
        }
    }
}

@Composable
private fun DaysPicker(selected: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        DayOfWeek.entries.forEach { day ->
            val isSelected = day in selected
            val colors = MaterialTheme.colorScheme
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) Modifier.background(colors.primary)
                        else Modifier.border(1.dp, colors.outline, CircleShape)
                    )
                    .clickable { onToggle(day) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.shortLabel(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) colors.onPrimary else colors.onSurface,
                )
            }
        }
    }
    Spacer(Modifier.height(4.dp))
}

private fun FrequencyType.label(): String = when (this) {
    FrequencyType.DAILY -> "Codziennie"
    FrequencyType.TIMES_PER_WEEK -> "X razy w tygodniu"
    FrequencyType.SPECIFIC_DAYS -> "Wybrane dni"
    FrequencyType.NONE -> "Bez planu (sporadyczny)"
}
