package com.hudlauncher.app

import com.hudlauncher.app.apps.AutoMapper
import com.hudlauncher.app.apps.InstalledApp
import com.hudlauncher.app.data.AppTarget
import com.hudlauncher.app.data.ConfigRepository
import com.hudlauncher.app.data.DefaultHudSlots
import com.hudlauncher.app.data.DmbTimer
import com.hudlauncher.app.data.UiSettings
import com.hudlauncher.app.data.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class HudDataTest {

    private val expectedNames = listOf(
        "CALENDAR", "CAMERA", "GALLERY", "EMAILS",
        "MESSAGES", "FIND ME", "MAPS", "SLEEPY PILLZ",
        "VOXFLIX", "PLANT LOVER", "AUDIO TOMES", "MONEYMAKERZ BANK",
        "SINSTAGRAM", "ENVEE", "LUCIMASTER", "DIARY",
        "BEE EATS", "VOXTUBE",
    )

    @Test
    fun defaultSlotsHaveAllEntriesInOrder() {
        assertEquals(18, DefaultHudSlots.all.size)
        assertEquals(expectedNames, DefaultHudSlots.all.map { it.displayName })
        assertEquals(18, DefaultHudSlots.all.map { it.id }.toSet().size)
    }

    @Test
    fun displayNameNeverChangesWhenMapped() {
        val mapped = DefaultHudSlots.applyMappings(
            mapOf("voxflix" to AppTarget("ru.kinopoisk", "ru.kinopoisk.Main")),
        )
        val vox = mapped.first { it.id == "voxflix" }
        assertEquals("VOXFLIX", vox.displayName)
        assertEquals("ru.kinopoisk", vox.selectedPackageName)
        assertEquals(expectedNames, mapped.map { it.displayName })
    }

    @Test
    fun configRoundTrip() {
        val mappings = mapOf(
            "maps" to AppTarget("ru.yandex.yandexmaps", "ru.yandex.maps.Main"),
            "diary" to AppTarget("com.google.android.keep"),
        )
        val ui = UiSettings(columns = 5, iconSize = 64f, showLabels = false, backgroundDim = 0.3f, hideSystemStatusBar = false)
        val parsed = ConfigRepository.decode(ConfigRepository.encode(mappings, ui))
        assertEquals(mappings, parsed.mappings)
        assertEquals(ui, parsed.ui)
    }

    @Test
    fun importIgnoresUnknownSlotsAndClampsValues() {
        val json = """{"version":1,"slots":[{"id":"nope","packageName":"a.b"},
            {"id":"envee","displayName":"HACKED","packageName":"c.d"}],
            "ui":{"columns":9,"iconSize":500}}"""
        val parsed = ConfigRepository.decode(json)
        assertEquals(setOf("envee"), parsed.mappings.keys)
        assertEquals(5, parsed.ui.columns)
        assertEquals(88f, parsed.ui.iconSize)
    }

    @Test
    fun importRejectsBadInput() {
        try {
            ConfigRepository.decode("not json")
            fail("expected exception")
        } catch (e: Exception) {
            // expected
        }
        try {
            ConfigRepository.decode("""{"version":99}""")
            fail("expected exception")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun appTargetParsing() {
        assertEquals(AppTarget("a.b", "a.b.C"), AppTarget.parse("a.b/a.b.C"))
        assertEquals(AppTarget("a.b"), AppTarget.parse("a.b"))
        assertNull(AppTarget.parse(""))
        assertNull(AppTarget.parse(null))
        assertNull(AppTarget.parse("/x"))
    }

    @Test
    fun corruptStoredMappingsFallBackToEmpty() {
        assertTrue(UserPreferences.decodeMappings("{broken").isEmpty())
        assertTrue(UserPreferences.decodeMappings(null).isEmpty())
    }

    @Test
    fun autoMapperOnlyAssignsConfidentMatches() {
        fun app(label: String, pkg: String) = InstalledApp(label, pkg, "$pkg.Main")
        val apps = listOf(
            app("Кинопоиск", "ru.kinopoisk"),
            app("Калькулятор", "com.calc"),
            app("VK", "com.vkontakte.android"),
            app("2ГИС", "ru.dublgis.dgismobile"),
        )
        val result = AutoMapper.suggest(apps)
        assertEquals("ru.kinopoisk", result["voxflix"]?.packageName)
        assertEquals("com.vkontakte.android", result["sinstagram"]?.packageName)
        assertEquals("ru.dublgis.dgismobile", result["maps"]?.packageName)
        assertNull(result["lucimaster"])
        assertTrue(result.values.none { it.packageName == "com.calc" })
    }

    @Test
    fun oldConfigWithoutNewFieldKeepsDefault() {
        val parsed = ConfigRepository.decode("""{"version":1,"slots":[],"ui":{"columns":4}}""")
        assertTrue(parsed.ui.hideSystemStatusBar)
    }

    @Test
    fun dmbTimerCountsDown() {
        assertEquals(365, DmbTimer.daysLeft(1000, 1000))
        assertEquals(265, DmbTimer.daysLeft(1000, 1100))
        assertEquals(0, DmbTimer.daysLeft(1000, 1365))
        assertEquals(0, DmbTimer.daysLeft(1000, 2000))
        assertEquals(365, DmbTimer.daysLeft(1000, 900))
        assertEquals(0f, DmbTimer.progress(10, 10), 0.001f)
        assertEquals(1f, DmbTimer.progress(10, 400), 0.001f)
    }

    @Test
    fun dmbSettingsRoundTrip() {
        val ui = UiSettings(dmbEnabled = false, dmbStartDay = 20000L)
        val parsed = ConfigRepository.decode(ConfigRepository.encode(emptyMap(), ui))
        assertEquals(ui, parsed.ui)
    }
}
