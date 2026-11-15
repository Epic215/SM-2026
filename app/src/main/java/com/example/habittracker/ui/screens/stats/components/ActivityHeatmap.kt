package com.example.habittracker.ui.screens.stats.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/** GitHub-style grid: one column per week (Monday on top), newest week on the right. */
@Composable
fun ActivityHeatmap(
    completion: Map<LocalDate, Float>,
    start: LocalDate,
    weeks: Int,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                repeat(weeks) { week ->
                    Column(Modifier.weight(1f)) {
                        repeat(7) { dayIndex ->
                            val date = start.plusWeeks(week.toLong()).plusDays(dayIndex.toLong())
                            HeatCell(color = cellColor(completion[date], date.isAfter(today)))
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Mniej", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                listOf(0f, 0.33f, 0.66f, 1f).forEach { ratio ->
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(cellColor(ratio, isFuture = false))
                    )
                }
                Text("Więcej", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HeatCell(color: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(1.5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color)
    )
}

@Composable
private fun cellColor(ratio: Float?, isFuture: Boolean): Color {
    val colors = MaterialTheme.colorScheme
    return when {
        isFuture -> Color.Transparent
        ratio == null -> colors.outline.copy(alpha = 0.5f)
        ratio == 0f -> colors.outline
        else -> colors.primary.copy(alpha = 0.25f + 0.75f * ratio)
    }
}
