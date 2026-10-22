package com.hudlauncher.app.data

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

@Immutable
data class UiSettings(
    val columns: Int = 4,
    val iconSize: Float = 72f,
    val labelSize: Float = 8f,
    val labelSpacing: Float = 2f,
    val showLabels: Boolean = true,
    val showBatteryPercent: Boolean = true,
    val showSignal: Boolean = true,
    val wallpaperIntensity: Float = 1f,
    val backgroundDim: Float = 0f,
    val animationIntensity: Float = 1f,
    val hideSystemStatusBar: Boolean = true,
    val dmbEnabled: Boolean = true,
    /** Service start as epoch day; 0 = not set yet (initialised to today on first launch). */
    val dmbStartDay: Long = 0L,
) {
    fun sanitized() = copy(
        columns = columns.coerceIn(4, 5),
        iconSize = iconSize.coerceIn(48f, 88f),
        labelSize = labelSize.coerceIn(6f, 12f),
        labelSpacing = labelSpacing.coerceIn(0f, 12f),
        wallpaperIntensity = wallpaperIntensity.coerceIn(0f, 1f),
        backgroundDim = backgroundDim.coerceIn(0f, 0.8f),
        animationIntensity = animationIntensity.coerceIn(0f, 1f),
    )
}

private val Context.hudDataStore: DataStore<Preferences> by preferencesDataStore(name = "hud_prefs")

class UserPreferences(private val context: Context) {

    private object Keys {
        val mappings = stringPreferencesKey("mappings")
        val columns = intPreferencesKey("columns")
        val iconSize = floatPreferencesKey("icon_size")
        val labelSize = floatPreferencesKey("label_size")
        val labelSpacing = floatPreferencesKey("label_spacing")
        val showLabels = booleanPreferencesKey("show_labels")
        val showBattery = booleanPreferencesKey("show_battery_percent")
        val showSignal = booleanPreferencesKey("show_signal")
        val wallpaperIntensity = floatPreferencesKey("wallpaper_intensity")
        val backgroundDim = floatPreferencesKey("background_dim")
        val animationIntensity = floatPreferencesKey("animation_intensity")
        val hideSystemBar = booleanPreferencesKey("hide_system_status_bar")
        val dmbEnabled = booleanPreferencesKey("dmb_enabled")
        val dmbStartDay = longPreferencesKey("dmb_start_day")
        val onboardingDone = booleanPreferencesKey("onboarding_done")
        val autoMapDone = booleanPreferencesKey("auto_map_done")
        val wallpaperVersion = longPreferencesKey("wallpaper_version")
    }

    private val store get() = context.hudDataStore

    val settings: Flow<UiSettings> = store.data.map { p -> p.toSettings() }

    val mappings: Flow<Map<String, AppTarget>> = store.data.map { p -> decodeMappings(p[Keys.mappings]) }

    val onboardingDone: Flow<Boolean> = store.data.map { it[Keys.onboardingDone] ?: false }

    val autoMapDone: Flow<Boolean> = store.data.map { it[Keys.autoMapDone] ?: false }

    /** 0 = built-in wallpaper; otherwise a timestamp that changes with every new custom image. */
    val wallpaperVersion: Flow<Long> = store.data.map { it[Keys.wallpaperVersion] ?: 0L }

    suspend fun setWallpaperVersion(version: Long) {
        store.edit { it[Keys.wallpaperVersion] = version }
    }

    suspend fun updateSettings(transform: (UiSettings) -> UiSettings) {
        store.edit { p -> p.writeSettings(transform(p.toSettings()).sanitized()) }
    }

    suspend fun setMapping(slotId: String, target: AppTarget?) {
        store.edit { p ->
            val map = decodeMappings(p[Keys.mappings]).toMutableMap()
            if (target == null) map.remove(slotId) else map[slotId] = target
            p[Keys.mappings] = encodeMappings(map)
        }
    }

    suspend fun mergeMappings(extra: Map<String, AppTarget>) {
        store.edit { p ->
            val map = decodeMappings(p[Keys.mappings]).toMutableMap()
            extra.forEach { (k, v) -> map.putIfAbsent(k, v) }
            p[Keys.mappings] = encodeMappings(map)
            p[Keys.autoMapDone] = true
        }
    }

    suspend fun replaceConfig(mappings: Map<String, AppTarget>, settings: UiSettings) {
        store.edit { p ->
            p[Keys.mappings] = encodeMappings(mappings)
            p.writeSettings(settings.sanitized())
            p[Keys.autoMapDone] = true
        }
    }

    suspend fun clearMappings() {
        store.edit { p ->
            p[Keys.mappings] = encodeMappings(emptyMap())
            p[Keys.autoMapDone] = true
        }
    }

    suspend fun setOnboardingDone() {
        store.edit { it[Keys.onboardingDone] = true }
    }

    private fun Preferences.toSettings(): UiSettings {
        val d = UiSettings()
        return UiSettings(
            columns = this[Keys.columns] ?: d.columns,
            iconSize = this[Keys.iconSize] ?: d.iconSize,
            labelSize = this[Keys.labelSize] ?: d.labelSize,
            labelSpacing = this[Keys.labelSpacing] ?: d.labelSpacing,
            showLabels = this[Keys.showLabels] ?: d.showLabels,
            showBatteryPercent = this[Keys.showBattery] ?: d.showBatteryPercent,
            showSignal = this[Keys.showSignal] ?: d.showSignal,
            wallpaperIntensity = this[Keys.wallpaperIntensity] ?: d.wallpaperIntensity,
            backgroundDim = this[Keys.backgroundDim] ?: d.backgroundDim,
            animationIntensity = this[Keys.animationIntensity] ?: d.animationIntensity,
            hideSystemStatusBar = this[Keys.hideSystemBar] ?: d.hideSystemStatusBar,
            dmbEnabled = this[Keys.dmbEnabled] ?: d.dmbEnabled,
            dmbStartDay = this[Keys.dmbStartDay] ?: d.dmbStartDay,
        ).sanitized()
    }

    private fun androidx.datastore.preferences.core.MutablePreferences.writeSettings(s: UiSettings) {
        this[Keys.columns] = s.columns
        this[Keys.iconSize] = s.iconSize
        this[Keys.labelSize] = s.labelSize
        this[Keys.labelSpacing] = s.labelSpacing
        this[Keys.showLabels] = s.showLabels
        this[Keys.showBattery] = s.showBatteryPercent
        this[Keys.showSignal] = s.showSignal
        this[Keys.wallpaperIntensity] = s.wallpaperIntensity
        this[Keys.backgroundDim] = s.backgroundDim
        this[Keys.animationIntensity] = s.animationIntensity
        this[Keys.hideSystemBar] = s.hideSystemStatusBar
        this[Keys.dmbEnabled] = s.dmbEnabled
        this[Keys.dmbStartDay] = s.dmbStartDay
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        private val serializer = MapSerializer(String.serializer(), String.serializer())

        fun encodeMappings(map: Map<String, AppTarget>): String =
            json.encodeToString(serializer, map.mapValues { it.value.encode() })

        fun decodeMappings(raw: String?): Map<String, AppTarget> {
            if (raw.isNullOrBlank()) return emptyMap()
            return try {
                json.decodeFromString(serializer, raw).mapNotNull { (k, v) ->
                    AppTarget.parse(v)?.let { k to it }
                }.toMap()
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }
}
