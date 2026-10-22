package com.hudlauncher.app.apps

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import com.hudlauncher.app.data.AppTarget

@Immutable
data class InstalledApp(
    val appLabel: String,
    val packageName: String,
    val className: String,
    val icon: ImageBitmap? = null,
) {
    val target: AppTarget get() = AppTarget(packageName, className)
}
