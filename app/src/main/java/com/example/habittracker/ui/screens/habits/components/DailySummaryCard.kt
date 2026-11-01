package com.example.habittracker.ui.screens.habits.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DailySummaryCard(done: Int, total: Int, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(
        targetValue = if (total == 0) 0f else done.toFloat() / total,
        label = "dailyProgress",
    )
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.primaryContainer,
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$done",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                )
                Text(
                    text = " / $total",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (done == total) "Wszystko zrobione!" else "zrobione dziś",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = colors.primary,
                trackColor = colors.surface,
            )
        }
    }
}
