package com.hudlauncher.app.data

/** The fixed set of 17 HUD slots, in display order. */
object DefaultHudSlots {

    private fun slot(
        id: String,
        name: String,
        type: HudIconType,
        vararg fallback: String,
    ) = HudSlot(
        id = id,
        displayName = name,
        selectedPackageName = null,
        fallbackPackages = fallback.toList(),
        iconType = type,
    )

    val all: List<HudSlot> = listOf(
        slot("calendar", "CALENDAR", HudIconType.CALENDAR,
            "com.google.android.calendar", "com.samsung.android.calendar", "com.android.calendar", "com.miui.calendar"),
        slot("camera", "CAMERA", HudIconType.CAMERA),
        slot("gallery", "GALLERY", HudIconType.GALLERY,
            "com.google.android.apps.photos", "com.sec.android.gallery3d", "com.miui.gallery", "com.android.gallery3d"),
        slot("emails", "EMAILS", HudIconType.EMAILS,
            "com.google.android.gm", "ru.yandex.mail", "ru.mail.mailapp"),
        slot("messages", "MESSAGES", HudIconType.MESSAGES,
            "ru.oneme.app", "org.telegram.messenger", "com.google.android.apps.messaging", "com.samsung.android.messaging"),
        slot("find_me", "FIND ME", HudIconType.FIND_ME,
            "com.google.android.apps.adm", "com.life360.android.safetymapd"),
        slot("maps", "MAPS", HudIconType.MAPS,
            "ru.yandex.yandexmaps", "ru.dublgis.dgismobile", "ru.yandex.yandexnavi", "com.google.android.apps.maps"),
        slot("sleepy_pillz", "SLEEPY PILLZ", HudIconType.SLEEPY_PILLZ,
            "com.urbandroid.sleep"),
        slot("voxflix", "VOXFLIX", HudIconType.VOXFLIX,
            "ru.kinopoisk", "ru.ivi.client", "ru.more.play", "ru.rt.video.app.mobile"),
        slot("plant_lover", "PLANT LOVER", HudIconType.PLANT_LOVER,
            "org.plantnet"),
        slot("audio_tomes", "AUDIO TOMES", HudIconType.AUDIO_TOMES,
            "ru.litres.android"),
        slot("moneymakerz_bank", "MONEYMAKERZ BANK", HudIconType.MONEYMAKERZ_BANK,
            "com.idamob.tinkoff.android", "ru.sberbankmobile", "ru.alfabank.mobile.android"),
        slot("sinstagram", "SINSTAGRAM", HudIconType.SINSTAGRAM,
            "com.vkontakte.android"),
        slot("envee", "ENVEE", HudIconType.ENVEE,
            "com.vk.vkvideo"),
        slot("lucimaster", "LUCIMASTER", HudIconType.LUCIMASTER),
        slot("diary", "DIARY", HudIconType.DIARY,
            "com.google.android.keep", "com.samsung.android.app.notes", "com.miui.notes"),
        slot("bee_eats", "BEE EATS", HudIconType.BEE_EATS,
            "ru.yandex.eda", "ru.foodfox.client", "ru.instamart"),
    )

    /** Lower-case app labels that are a confident match for a slot (exact or "<keyword> ..." prefix). */
    val labelKeywords: Map<String, List<String>> = mapOf(
        "maps" to listOf("яндекс карты", "2гис", "яндекс навигатор", "карты", "maps"),
        "voxflix" to listOf("кинопоиск", "ivi", "иви", "okko", "wink"),
        "audio_tomes" to listOf("литрес", "яндекс книги", "audible"),
        "moneymakerz_bank" to listOf("т-банк", "тинькофф", "сбербанк", "сбер", "альфа-банк"),
        "sinstagram" to listOf("вконтакте", "vk"),
        "envee" to listOf("vk видео", "vk клипы"),
        "lucimaster" to listOf("яндекс афиша", "афиша"),
        "diary" to listOf("дневник", "diary", "заметки", "notes"),
        "bee_eats" to listOf("яндекс еда", "купер", "delivery club"),
    )

    fun byId(id: String): HudSlot? = all.firstOrNull { it.id == id }

    /** Applies stored mappings; display names always come from the defaults. */
    fun applyMappings(mappings: Map<String, AppTarget>): List<HudSlot> =
        all.map { it.withTarget(mappings[it.id]) }
}
