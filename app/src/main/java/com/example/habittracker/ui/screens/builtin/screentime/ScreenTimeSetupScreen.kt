package com.example.habittracker.ui.screens.builtin.screentime

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import com.example.habittracker.ui.AppViewModelProvider

@Composable
fun ScreenTimeSetupScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScreenTimeSetupViewModel = viewModel(factory = AppViewModelProvider.Factory),
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
            IconButton(onClick = onDone) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Wróć")
            }
            Text(
                text = if (uiState.isEditing) "Ustawienia limitu czasu" else "Nowy nawyk: limit czasu",
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

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Nazwa") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                if (!uiState.hasUsageAccess) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Włącz dostęp do danych o użyciu, żeby aplikacja mogła liczyć czas w wybranych apkach.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Button(onClick = viewModel::openUsageAccessSettings) {
                                Text("Otwórz ustawienia")
                            }
                            TextButton(onClick = viewModel::refreshPermission) {
                                Text("Sprawdź ponownie")
                            }
                        }
                    }
                }
            }

            item {
                LimitStepper(
                    limitMinutes = uiState.settings.limitMinutes,
                    onChange = viewModel::onLimitChange,
                )
            }

            item {
                Text(
                    text = "Aplikacje (${uiState.settings.packages.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (uiState.appsLoading) {
                item {
                    Text("Ładowanie listy aplikacji…", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(uiState.apps, key = { it.packageName }) { app ->
                    val selected = app.packageName in uiState.settings.packages
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onAppToggle(app.packageName) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = selected,
                            onCheckedChange = { viewModel.onAppToggle(app.packageName) },
                        )
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
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
private fun LimitStepper(limitMinutes: Int, onChange: (Int) -> Unit) {
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
            Text(
                text = "Dzienny limit",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { onChange(limitMinutes - 15) },
                enabled = limitMinutes > ScreenTimeSetupViewModel.MIN_LIMIT,
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "Mniej")
            }
            Text(
                text = "$limitMinutes min",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            IconButton(
                onClick = { onChange(limitMinutes + 15) },
                enabled = limitMinutes < ScreenTimeSetupViewModel.MAX_LIMIT,
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Więcej")
            }
        }
    }
}
