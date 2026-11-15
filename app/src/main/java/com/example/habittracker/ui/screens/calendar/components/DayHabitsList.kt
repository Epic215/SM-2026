package com.example.habittracker.ui.screens.calendar.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.habittracker.domain.isNegative
import com.example.habittracker.ui.screens.builtin.wakeup.formatMinutes
import com.example.habittracker.ui.screens.calendar.DayHabitItem

@Composable
fun DayHabitsList(
    items: List<DayHabitItem>,
    onToggle: (Long) -> Unit,
    onEditBuiltIn: (DayHabitItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) {
        Text(
            text = "Brak nawyków tego dnia",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            DayHabitRow(
                item = item,
                onClick = {
                    if (item.isBuiltIn) onEditBuiltIn(item) else onToggle(item.habit.id)
                },
            )
        }
    }
}

@Composable
private fun DayHabitRow(item: DayHabitItem, onClick: () -> Unit) {
    val habitColor = Color(item.habit.color)
    val isNegative = item.habit.isNegative
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(width = 4.dp, height = 28.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(habitColor)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.habit.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = when {
                    item.isBuiltIn && item.marked && item.value != null ->
                        "Wstałeś o ${formatMinutes(item.value)}"
                    item.isBuiltIn && item.value != null ->
                        "Spóźnienie: ${formatMinutes(item.value)}"
                    item.isBuiltIn -> "Brak wpisu"
                    isNegative && item.marked -> "Potknięcie"
                    isNegative -> "Do unikania"
                    !item.scheduled -> "Niezaplanowany"
                    else -> null
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isNegative && item.marked -> colors.tertiary
                            item.isBuiltIn && !item.marked -> colors.tertiary
                            else -> colors.onSurfaceVariant
                        },
                    )
                }
            }
            val markColor = when {
                isNegative -> colors.tertiary
                item.isBuiltIn && !item.marked && item.value != null -> colors.tertiary
                else -> habitColor
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .then(
                        if (item.marked || (item.isBuiltIn && item.value != null)) {
                            Modifier.background(if (item.marked) markColor else colors.tertiary)
                        } else {
                            Modifier.border(2.dp, habitColor.copy(alpha = 0.35f), CircleShape)
                        }
                    )
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                if (item.marked || (item.isBuiltIn && item.value != null)) {
                    Icon(
                        if (isNegative || (item.isBuiltIn && !item.marked)) Icons.Rounded.Close else Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
