package com.hudlauncher.app.launcher

import android.content.Context
import android.net.Uri
import com.hudlauncher.app.apps.AppRepository
import com.hudlauncher.app.apps.AutoMapper
import com.hudlauncher.app.apps.InstalledApp
import com.hudlauncher.app.apps.LaunchResult
import com.hudlauncher.app.data.AppTarget
import com.hudlauncher.app.data.ConfigRepository
import com.hudlauncher.app.data.DefaultHudSlots
import com.hudlauncher.app.data.HudSlot
import com.hudlauncher.app.data.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LauncherRepository(private val context: Context) {

    val prefs = UserPreferences(context)
    private val apps = AppRepository(context)
    private val wallpapers = WallpaperStore(context)

    val slots: Flow<List<HudSlot>> = prefs.mappings.map { DefaultHudSlots.applyMappings(it) }

    val wallpaper: Flow<ImageBitmap?> = prefs.wallpaperVersion
        .distinctUntilChanged()
        .map { version -> if (version == 0L) null else withContext(Dispatchers.IO) { wallpapers.load() } }

    suspend fun setWallpaper(uri: Uri) {
        withContext(Dispatchers.IO) { wallpapers.import(uri) }
        prefs.setWallpaperVersion(System.currentTimeMillis())
    }

    suspend fun resetWallpaper() {
        prefs.setWallpaperVersion(0L)
        withContext(Dispatchers.IO) { wallpapers.delete() }
    }

    suspend fun loadApps(): List<InstalledApp> = apps.loadInstalledApps()

    suspend fun autoMapIfNeeded(installed: List<InstalledApp>) {
        if (prefs.autoMapDone.first() || installed.isEmpty()) return
        val suggestion = AutoMapper.suggest(installed, alreadyMapped = prefs.mappings.first().keys)
        prefs.mergeMappings(suggestion)
    }

    fun launch(slot: HudSlot): LaunchResult = apps.launch(slot)

    fun isDefaultHome(): Boolean = HomeRole.isDefaultHome(context)

    suspend fun setMapping(slotId: String, target: AppTarget?) = prefs.setMapping(slotId, target)

    suspend fun exportConfig(uri: Uri) = withContext(Dispatchers.IO) {
        val text = ConfigRepository.encode(prefs.mappings.first(), prefs.settings.first())
        ConfigRepository.write(context.contentResolver, uri, text)
    }

    suspend fun importConfig(uri: Uri) = withContext(Dispatchers.IO) {
        val parsed = ConfigRepository.decode(ConfigRepository.read(context.contentResolver, uri))
        prefs.replaceConfig(parsed.mappings, parsed.ui)
    }
}
