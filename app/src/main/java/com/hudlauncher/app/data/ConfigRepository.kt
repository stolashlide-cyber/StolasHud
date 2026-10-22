package com.hudlauncher.app.data

import android.content.ContentResolver
import android.net.Uri
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class ParsedConfig(val mappings: Map<String, AppTarget>, val ui: UiSettings)

/** JSON export / import of slot mappings and UI settings. */
object ConfigRepository {

    const val VERSION = 1

    @Serializable
    private data class SlotDto(
        val id: String,
        val displayName: String? = null,
        val packageName: String? = null,
        val componentName: String? = null,
    )

    @Serializable
    private data class UiDto(
        val columns: Int = 4,
        val showLabels: Boolean = true,
        val iconSize: Float = 72f,
        val labelSize: Float = 8f,
        val labelSpacing: Float = 2f,
        val showBatteryPercent: Boolean = true,
        val showSignal: Boolean = true,
        val wallpaperIntensity: Float = 1f,
        val backgroundDim: Float = 0f,
        val animationIntensity: Float = 1f,
        val hideSystemStatusBar: Boolean = true,
        val dmbEnabled: Boolean = true,
        val dmbStartDay: Long = 0L,
    )

    @Serializable
    private data class ConfigDto(
        val version: Int = VERSION,
        val slots: List<SlotDto> = emptyList(),
        val ui: UiDto = UiDto(),
    )

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }

    fun encode(mappings: Map<String, AppTarget>, ui: UiSettings): String {
        val slots = DefaultHudSlots.all.map { slot ->
            val t = mappings[slot.id]
            SlotDto(slot.id, slot.displayName, t?.packageName, t?.className)
        }
        val dto = ConfigDto(
            version = VERSION,
            slots = slots,
            ui = UiDto(
                ui.columns, ui.showLabels, ui.iconSize, ui.labelSize, ui.labelSpacing,
                ui.showBatteryPercent, ui.showSignal, ui.wallpaperIntensity,
                ui.backgroundDim, ui.animationIntensity, ui.hideSystemStatusBar,
                ui.dmbEnabled, ui.dmbStartDay,
            ),
        )
        return json.encodeToString(ConfigDto.serializer(), dto)
    }

    /** Throws on malformed input or an unsupported version. */
    fun decode(text: String): ParsedConfig {
        val dto = json.decodeFromString(ConfigDto.serializer(), text)
        require(dto.version in 1..VERSION) { "Unsupported config version ${dto.version}" }
        val known = DefaultHudSlots.all.map { it.id }.toSet()
        val mappings = dto.slots.filter { it.id in known }.mapNotNull { s ->
            val pkg = s.packageName?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            s.id to AppTarget(pkg, s.componentName?.takeIf { it.isNotBlank() })
        }.toMap()
        val u = dto.ui
        val ui = UiSettings(
            u.columns, u.iconSize, u.labelSize, u.labelSpacing, u.showLabels,
            u.showBatteryPercent, u.showSignal, u.wallpaperIntensity,
            u.backgroundDim, u.animationIntensity, u.hideSystemStatusBar,
            u.dmbEnabled, u.dmbStartDay,
        ).sanitized()
        return ParsedConfig(mappings, ui)
    }

    fun write(resolver: ContentResolver, uri: Uri, text: String) {
        resolver.openOutputStream(uri, "wt")?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
            ?: error("Cannot open output stream")
    }

    fun read(resolver: ContentResolver, uri: Uri): String =
        resolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            ?: error("Cannot open input stream")
}
