package com.hudlauncher.app.apps

import com.hudlauncher.app.data.AppTarget
import com.hudlauncher.app.data.DefaultHudSlots
import com.hudlauncher.app.data.HudSlot

/** Conservative first-run assignment: only confident matches, never random apps. */
object AutoMapper {

    fun suggest(
        apps: List<InstalledApp>,
        slots: List<HudSlot> = DefaultHudSlots.all,
        alreadyMapped: Set<String> = emptySet(),
    ): Map<String, AppTarget> {
        val used = mutableSetOf<String>()
        val result = linkedMapOf<String, AppTarget>()
        for (slot in slots) {
            if (slot.id in alreadyMapped) continue
            val match = matchByPackage(slot, apps, used) ?: matchByLabel(slot, apps, used)
            if (match != null) {
                used += match.packageName
                result[slot.id] = match.target
            }
        }
        return result
    }

    private fun matchByPackage(slot: HudSlot, apps: List<InstalledApp>, used: Set<String>): InstalledApp? {
        for (pkg in slot.fallbackPackages) {
            apps.firstOrNull { it.packageName == pkg && pkg !in used }?.let { return it }
        }
        return null
    }

    private fun matchByLabel(slot: HudSlot, apps: List<InstalledApp>, used: Set<String>): InstalledApp? {
        val keywords = DefaultHudSlots.labelKeywords[slot.id] ?: return null
        for (kw in keywords) {
            apps.firstOrNull { app ->
                val label = app.appLabel.trim().lowercase()
                app.packageName !in used && (label == kw || label.startsWith("$kw "))
            }?.let { return it }
        }
        return null
    }
}
