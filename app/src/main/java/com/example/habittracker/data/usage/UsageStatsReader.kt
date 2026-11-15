package com.example.habittracker.data.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

data class InstalledApp(
    val packageName: String,
    val label: String,
)

/**
 * Reads foreground time for selected packages via [UsageStatsManager].
 * Requires the special "usage access" permission granted by the user in system settings.
 */
class UsageStatsReader(private val context: Context) {

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageAccessSettings() {
        context.startActivity(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    /** Sum of foreground time today for [packages], in whole minutes. */
    fun foregroundMinutesToday(packages: Set<String>): Int {
        if (packages.isEmpty() || !hasUsageAccess()) return 0
        val usm = context.getSystemService(UsageStatsManager::class.java) ?: return 0
        val start = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val end = System.currentTimeMillis()
        val events = usm.queryEvents(start, end)

        var totalMs = 0L
        val resumedAt = mutableMapOf<String, Long>()
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            if (pkg !in packages) continue
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED,
                UsageEvents.Event.MOVE_TO_FOREGROUND,
                -> resumedAt[pkg] = event.timeStamp
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.MOVE_TO_BACKGROUND,
                -> {
                    val from = resumedAt.remove(pkg) ?: continue
                    totalMs += (event.timeStamp - from).coerceAtLeast(0L)
                }
            }
        }
        for (from in resumedAt.values) {
            totalMs += (end - from).coerceAtLeast(0L)
        }
        return TimeUnit.MILLISECONDS.toMinutes(totalMs).toInt().coerceAtLeast(0)
    }

    fun installedLaunchableApps(): List<InstalledApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { info ->
                val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
                if (packageName == context.packageName) return@mapNotNull null
                val label = try {
                    val appInfo: ApplicationInfo = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (_: PackageManager.NameNotFoundException) {
                    packageName
                }
                InstalledApp(packageName, label)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }
}
