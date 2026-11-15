package com.example.habittracker.domain.builtin

import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitKind
import java.time.LocalDate
import java.time.LocalDateTime

data class ScreenTimeSettings(
    /** Daily limit in minutes for the sum of all selected apps. */
    val limitMinutes: Int,
    val packages: Set<String>,
) {
    fun toMap(): Map<String, String> = mapOf(
        KEY_LIMIT to limitMinutes.toString(),
        KEY_PACKAGES to packages.sorted().joinToString(","),
    )

    companion object {
        private const val KEY_LIMIT = "screen_time.limit_minutes"
        private const val KEY_PACKAGES = "screen_time.packages"

        val DEFAULT = ScreenTimeSettings(limitMinutes = 90, packages = emptySet())

        fun from(map: Map<String, String>): ScreenTimeSettings = ScreenTimeSettings(
            limitMinutes = map[KEY_LIMIT]?.toIntOrNull()?.coerceAtLeast(1) ?: DEFAULT.limitMinutes,
            packages = map[KEY_PACKAGES]
                ?.split(',')
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.toSet()
                .orEmpty(),
        )
    }
}

object ScreenTimeHabit : BuiltInHabit {

    override val kind = HabitKind.SCREEN_TIME
    override val name = "Limit czasu w aplikacjach"
    override val description =
        "Śledzi, ile minut dziś spędziłeś w wybranych aplikacjach (np. Instagram, Chrome, Discord). " +
            "Dzień jest udany, gdy nie przekroczysz limitu."
    override val defaultColor = 0xFF4C9AFF.toInt()

    override fun defaultSettings(): Map<String, String> = ScreenTimeSettings.DEFAULT.toMap()

    /**
     * [minutesUsed] is today's (or that day's) foreground minutes for the selected packages.
     * Today stays [DayOutcome.PENDING] while still under the limit; going over decides as failure.
     */
    fun outcome(
        settings: ScreenTimeSettings,
        date: LocalDate,
        minutesUsed: Int?,
        now: LocalDateTime,
    ): DayOutcome {
        val today = now.toLocalDate()
        if (minutesUsed == null) {
            return when {
                date.isBefore(today) -> DayOutcome.FAILURE
                else -> DayOutcome.PENDING
            }
        }
        if (minutesUsed > settings.limitMinutes) return DayOutcome.FAILURE
        return if (date.isBefore(today)) DayOutcome.SUCCESS else DayOutcome.PENDING
    }

    override fun outcome(
        habit: Habit,
        settings: Map<String, String>,
        date: LocalDate,
        value: Int?,
        now: LocalDateTime,
    ): DayOutcome = outcome(ScreenTimeSettings.from(settings), date, value, now)
}
