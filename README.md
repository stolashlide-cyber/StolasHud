# HUD Launcher

Кастомный Android HOME launcher (Kotlin + Jetpack Compose) в стиле вымышленной мультяшной мобильной ОС:
сетка 4×N, неоновые обои, собственная HUD-панель (сигнал, замок, реальный заряд батареи) и фиксированные
подписи иконок (`CALENDAR`, `VOXFLIX`, `SINSTAGRAM`, `BEE EATS` …). Подпись никогда не меняется на название
реального приложения — слот запускает любое выбранное вами приложение.

- applicationId: `com.hudlauncher.app`
- minSdk 26, compile/target SDK 35

## Build locally

```bash
./gradlew assembleDebug
```

Нужны JDK 17+ и Android SDK (`ANDROID_HOME` или `local.properties` с `sdk.dir=...`).

APK path:

```
app/build/outputs/apk/debug/app-debug.apk
```

Тесты и lint: `./gradlew test lint`.

## Build on GitHub (без компьютера)

Workflow `.github/workflows/build-apk.yml` (Build Android APK) запускается на push в `main`, pull request и
вручную (Actions → Build Android APK → Run workflow). Готовый APK — в artifact **hud-launcher-debug-apk**.

## Install

Скачайте APK (artifact или файл из сборки) и установите на Android (разрешите установку из неизвестных источников).

## Select as Home app

Обычно: Settings → Apps → Default apps → Home app → HUD Launcher.
У разных производителей путь может немного отличаться. Также можно нажать кнопку
**СДЕЛАТЬ ДОМАШНИМ ЭКРАНОМ** в онбординге, настройках или панели редактирования.

## Configure

- Long press на иконку — выбрать связанное приложение (поиск, Reset).
- Long press на фон — Edit Mode (иконки покачиваются; тап по иконке — выбор приложения).
- Long press на верхнюю HUD-панель — Settings (колонки, размеры, подписи, обои, анимации,
  сброс назначений, экспорт/импорт JSON-конфига через системный файловый диалог).

Назначения и настройки хранятся в DataStore и переживают перезагрузку.
