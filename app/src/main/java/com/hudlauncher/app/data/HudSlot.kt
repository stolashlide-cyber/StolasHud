package com.hudlauncher.app.data

import androidx.compose.runtime.Immutable

/** Real Android application a HUD slot launches. [className] may be null (package launch intent is used). */
@Immutable
data class AppTarget(val packageName: String, val className: String? = null) {
    fun encode(): String = if (className == null) packageName else "$packageName/$className"

    companion object {
        fun parse(raw: String?): AppTarget? {
            val value = raw?.trim().orEmpty()
            if (value.isEmpty()) return null
            val pkg = value.substringBefore('/')
            if (pkg.isEmpty()) return null
            val cls = if ('/' in value) value.substringAfter('/').takeIf { it.isNotEmpty() } else null
            return AppTarget(pkg, cls)
        }
    }
}

/**
 * A HUD icon. [displayName] is the fictional HUD caption and is never derived from
 * the real application behind the slot.
 */
@Immutable
data class HudSlot(
    val id: String,
    val displayName: String,
    val selectedPackageName: String?,
    val fallbackPackages: List<String>,
    val iconType: HudIconType,
    val selectedClassName: String? = null,
) {
    val target: AppTarget?
        get() = selectedPackageName?.let { AppTarget(it, selectedClassName) }

    fun withTarget(target: AppTarget?): HudSlot =
        copy(selectedPackageName = target?.packageName, selectedClassName = target?.className)
}
