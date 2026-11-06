package com.example.habittracker.ui.screens.details.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.habittracker.ui.screens.habits.components.shortLabel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val MONTHS_BACK = 12L
private val polish = Locale.forLanguageTag("pl")
private val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", polish)

@Composable
fun MonthCalendar(
    doneDates: Set<LocalDate>,
    isScheduled: (LocalDate) -> Boolean,
    habitColor: Color,
    today: LocalDate,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentMonth = remember(today) { YearMonth.from(today) }
    val startMonth = remember(currentMonth) { currentMonth.minusMonths(MONTHS_BACK) }
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = currentMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY,
    )
    val scope = rememberCoroutineScope()
    val visibleMonth = state.firstVisibleMonth.yearMonth

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { scope.launch { state.animateScrollToMonth(visibleMonth.minusMonths(1)) } },
                    enabled = visibleMonth > startMonth,
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Poprzedni miesiąc")
                }
                Text(
                    text = visibleMonth.format(monthFormatter).replaceFirstChar { it.titlecase(polish) },
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { scope.launch { state.animateScrollToMonth(visibleMonth.plusMonths(1)) } },
                    enabled = visibleMonth < currentMonth,
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Następny miesiąc")
                }
            }

            Row(Modifier.padding(vertical = 8.dp)) {
                DayOfWeek.entries.forEach { day ->
                    Text(
                        text = day.shortLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    DayCell(
                        day = day,
                        done = day.date in doneDates,
                        scheduled = isScheduled(day.date),
                        isToday = day.date == today,
                        isFuture = day.date.isAfter(today),
                        habitColor = habitColor,
                        onClick = { onDayClick(day.date) },
                    )
                },
            )
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    done: Boolean,
    scheduled: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    habitColor: Color,
    onClick: () -> Unit,
) {
    if (day.position != DayPosition.MonthDate) {
        Box(Modifier.aspectRatio(1f))
        return
    }

    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(CircleShape)
            .then(
                when {
                    done -> Modifier.background(habitColor)
                    scheduled && !isFuture -> Modifier.border(1.5.dp, habitColor.copy(alpha = 0.35f), CircleShape)
                    else -> Modifier
                }
            )
            .then(if (isToday && !done) Modifier.border(2.dp, colors.primary, CircleShape) else Modifier)
            .clickable(enabled = !isFuture, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                done -> Color.White
                isFuture -> colors.onSurfaceVariant.copy(alpha = 0.4f)
                scheduled -> colors.onSurface
                else -> colors.onSurfaceVariant
            },
        )
    }
}
