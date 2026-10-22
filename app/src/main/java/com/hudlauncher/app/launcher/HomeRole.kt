package com.hudlauncher.app.launcher

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

/** Helpers for making this app the default Android HOME application via official APIs. */
object HomeRole {

    fun isDefaultHome(context: Context): Boolean {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val info = context.packageManager.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
        return info?.activityInfo?.packageName == context.packageName
    }

    /** Candidate intents, most direct first (role dialog, then Home settings, then Default apps). */
    private fun candidateIntents(context: Context): List<Intent> {
        val list = mutableListOf<Intent>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = context.getSystemService(RoleManager::class.java)
            if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME) && !rm.isRoleHeld(RoleManager.ROLE_HOME)) {
                list += rm.createRequestRoleIntent(RoleManager.ROLE_HOME)
            }
        }
        list += Intent(Settings.ACTION_HOME_SETTINGS)
        list += Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        list += Intent(Settings.ACTION_SETTINGS)
        return list
    }

    /** Launches the first intent the system can handle through [launch]. */
    fun request(context: Context, launch: (Intent) -> Unit) {
        for (intent in candidateIntents(context)) {
            try {
                launch(intent)
                return
            } catch (e: ActivityNotFoundException) {
                // try the next, less direct screen
            } catch (e: SecurityException) {
                // try the next, less direct screen
            }
        }
    }
}
