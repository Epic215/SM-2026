package com.example.habittracker.domain.builtin

import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitKind
import java.time.LocalDate
import java.time.LocalDateTime

/** All times are minutes since midnight. */
data class WakeUpSettings(
    /** Baseline: when the user currently wakes up. Not the first daily minimum. */
    val startMinutes: Int,
    /** Final goal. */
    val targetMinutes: Int,
    val stepMinutes: Int,
    /** How many successful days before the minimum moves another step earlier. */
    val stepEveryDays: Int,
    /** How late after the day's minimum still counts as success. */
    val toleranceMinutes: Int,
    /** Without an entry by minimum + this, the day fails automatically. */
    val deadlineMinutes: Int,
) {
    fun toMap(): Map<String, String> = mapOf(
        KEY_START to startMinutes.toString(),
        KEY_TARGET to targetMinutes.toString(),
        KEY_STEP to stepMinutes.toString(),
        KEY_STEP_EVERY to stepEveryDays.toString(),
        KEY_TOLERANCE to toleranceMinutes.toString(),
        KEY_DEADLINE to deadlineMinutes.toString(),
    )

    companion object {
        private const val KEY_START = "wake_up.start_minutes"
        private const val KEY_TARGET = "wake_up.target_minutes"
        private const val KEY_STEP = "wake_up.step_minutes"
        private const val KEY_STEP_EVERY = "wake_up.step_every_days"
        private const val KEY_TOLERANCE = "wake_up.tolerance_minutes"
        private const val KEY_DEADLINE = "wake_up.deadline_minutes"

        val DEFAULT = WakeUpSettings(
            startMinutes = 8 * 60 + 30,
            targetMinutes = 6 * 60 + 30,
            stepMinutes = 15,
            stepEveryDays = 3,
            toleranceMinutes = 10,
            deadlineMinutes = 30,
        )

        fun from(map: Map<String, String>): WakeUpSettings = WakeUpSettings(
            startMinutes = map[KEY_START]?.toIntOrNull() ?: DEFAULT.startMinutes,
            targetMinutes = map[KEY_TARGET]?.toIntOrNull() ?: DEFAULT.targetMinutes,
            stepMinutes = map[KEY_STEP]?.toIntOrNull() ?: DEFAULT.stepMinutes,
            stepEveryDays = map[KEY_STEP_EVERY]?.toIntOrNull() ?: DEFAULT.stepEveryDays,
            toleranceMinutes = map[KEY_TOLERANCE]?.toIntOrNull() ?: DEFAULT.toleranceMinutes,
            deadlineMinutes = map[KEY_DEADLINE]?.toIntOrNull() ?: DEFAULT.deadlineMinutes,
        )
    }
}

data class WakeUpEvaluation(
    val successDates: Set<LocalDate>,
    val todayOutcome: DayOutcome,
    val todayMinimumMinutes: Int,
)

object WakeUpHabit : BuiltInHabit {

    override val kind = HabitKind.WAKE_UP
    override val name = "Wcześniejsze wstawanie"
    override val description =
        "Cel minimalny schodzi wcześniej tylko po udanych dniach, aż dojdziesz do celu głównego. " +
            "Spóźnienie nie przyspiesza postępu."
    override val defaultColor = 0xFFFFB547.toInt()

    override fun defaultSettings(): Map<String, String> = WakeUpSettings.DEFAULT.toMap()

    /**
     * First day already aims one step earlier than [WakeUpSettings.startMinutes].
     * Further steps unlock every [WakeUpSettings.stepEveryDays] successes.
     */
    fun minimumMinutes(settings: WakeUpSettings, priorSuccessCount: Int): Int {
        val every = settings.stepEveryDays.coerceAtLeast(1)
        val steps = 1 + priorSuccessCount / every
        return (settings.startMinutes - steps * settings.stepMinutes)
            .coerceAtLeast(settings.targetMinutes)
    }

    fun successfulDaysToGoal(settings: WakeUpSettings): Int {
        val difference = settings.startMinutes - settings.targetMinutes
        if (difference <= 0) return 0
        val totalSteps = (difference + settings.stepMinutes - 1) / settings.stepMinutes
        val remainingSteps = (totalSteps - 1).coerceAtLeast(0)
        return remainingSteps * settings.stepEveryDays.coerceAtLeast(1)
    }

    fun outcomeForMinimum(
        settings: WakeUpSettings,
        minimumMinutes: Int,
        date: LocalDate,
        wokeAtMinutes: Int?,
        now: LocalDateTime,
    ): DayOutcome {
        if (wokeAtMinutes != null) {
            return if (wokeAtMinutes <= minimumMinutes + settings.toleranceMinutes) {
                DayOutcome.SUCCESS
            } else {
                DayOutcome.NEUTRAL
            }
        }
        val today = now.toLocalDate()
        return when {
            date.isBefore(today) -> DayOutcome.FAILURE
            date.isAfter(today) -> DayOutcome.PENDING
            now.hour * 60 + now.minute >= minimumMinutes + settings.deadlineMinutes -> DayOutcome.FAILURE
            else -> DayOutcome.PENDING
        }
    }

    fun evaluate(
        settings: WakeUpSettings,
        startDate: LocalDate,
        valuesByDate: Map<LocalDate, Int>,
        now: LocalDateTime,
    ): WakeUpEvaluation {
        val today = now.toLocalDate()
        val successDates = mutableSetOf<LocalDate>()
        var successCount = 0
        var todayOutcome = DayOutcome.PENDING
        var todayMinimum = minimumMinutes(settings, 0)

        var date = startDate
        while (!date.isAfter(today)) {
            val minimum = minimumMinutes(settings, successCount)
            val outcome = outcomeForMinimum(settings, minimum, date, valuesByDate[date], now)
            if (date == today) {
                todayOutcome = outcome
                todayMinimum = minimum
            }
            if (outcome == DayOutcome.SUCCESS) {
                successDates += date
                successCount++
            }
            date = date.plusDays(1)
        }

        return WakeUpEvaluation(successDates, todayOutcome, todayMinimum)
    }

    /** Single-day helper assuming no prior successes (first minimum). Prefer [evaluate] when history exists. */
    fun outcome(
        settings: WakeUpSettings,
        startDate: LocalDate,
        date: LocalDate,
        wokeAtMinutes: Int?,
        now: LocalDateTime,
    ): DayOutcome = outcomeForMinimum(settings, minimumMinutes(settings, 0), date, wokeAtMinutes, now)

    override fun outcome(
        habit: Habit,
        settings: Map<String, String>,
        date: LocalDate,
        value: Int?,
        now: LocalDateTime,
    ): DayOutcome = outcome(WakeUpSettings.from(settings), habit.startDate, date, value, now)
}
