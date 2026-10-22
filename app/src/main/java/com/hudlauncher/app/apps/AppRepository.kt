package com.hudlauncher.app.apps

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.hudlauncher.app.data.AppTarget
import com.hudlauncher.app.data.HudSlot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class LaunchResult { LAUNCHED, NOT_FOUND, UNASSIGNED }

class AppRepository(private val context: Context) {

    private val pm get() = context.packageManager

    /** All launchable apps (MAIN + LAUNCHER), except this launcher itself. */
    suspend fun loadInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(query, 0)
            .asSequence()
            .filter { it.activityInfo.packageName != context.packageName }
            .map { info ->
                InstalledApp(
                    appLabel = info.loadLabel(pm).toString(),
                    packageName = info.activityInfo.packageName,
                    className = info.activityInfo.name,
                    icon = runCatching { info.loadIcon(pm).toBitmap(96, 96).asImageBitmap() }.getOrNull(),
                )
            }
            .distinctBy { it.packageName to it.className }
            .sortedBy { it.appLabel.lowercase() }
            .toList()
    }

    fun launch(slot: HudSlot): LaunchResult {
        slot.target?.let { target ->
            return if (startTarget(target)) LaunchResult.LAUNCHED else LaunchResult.NOT_FOUND
        }
        if (slot.id == "camera" && startCamera()) return LaunchResult.LAUNCHED
        for (pkg in slot.fallbackPackages) {
            if (startTarget(AppTarget(pkg))) return LaunchResult.LAUNCHED
        }
        systemCategory(slot.id)?.let { category ->
            if (start(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, category))) return LaunchResult.LAUNCHED
        }
        return LaunchResult.UNASSIGNED
    }

    private fun startTarget(target: AppTarget): Boolean {
        if (target.className != null) {
            val component = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(ComponentName(target.packageName, target.className))
            if (start(component)) return true
        }
        val launch = pm.getLaunchIntentForPackage(target.packageName) ?: return false
        return start(launch)
    }

    private fun startCamera(): Boolean =
        start(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)) ||
            start(Intent(MediaStore.ACTION_IMAGE_CAPTURE))

    private fun start(intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: SecurityException) {
        false
    }

    private fun systemCategory(slotId: String): String? = when (slotId) {
        "calendar" -> Intent.CATEGORY_APP_CALENDAR
        "gallery" -> Intent.CATEGORY_APP_GALLERY
        "emails" -> Intent.CATEGORY_APP_EMAIL
        "messages" -> Intent.CATEGORY_APP_MESSAGING
        "maps" -> Intent.CATEGORY_APP_MAPS
        else -> null
    }
}
