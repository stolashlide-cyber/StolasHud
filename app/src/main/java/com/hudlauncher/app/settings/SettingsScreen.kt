package com.hudlauncher.app.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.hudlauncher.app.data.DmbTimer
import com.hudlauncher.app.data.UiSettings
import com.hudlauncher.app.hud.HudPanelShape
import com.hudlauncher.app.hud.HudWallpaper
import com.hudlauncher.app.ui.theme.HudStyle
import com.hudlauncher.app.ui.theme.HudCyan
import com.hudlauncher.app.ui.theme.HudDeepPurple
import com.hudlauncher.app.ui.theme.HudNeonPink
import com.hudlauncher.app.ui.theme.HudTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: UiSettings,
    isDefaultHome: Boolean,
    versionName: String,
    wallpaper: ImageBitmap?,
    onPickWallpaper: () -> Unit,
    onResetWallpaper: () -> Unit,
    onUpdate: ((UiSettings) -> UiSettings) -> Unit,
    onResetMappings: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onMakeHome: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmReset by remember { mutableStateOf(false) }
    var showDmbDatePicker by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize()) {
    HudWallpaper(intensity = 1f, dim = 0.55f, image = wallpaper)
    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("НАСТРОЙКИ", color = HudNeonPink, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onClose) { Text("ГОТОВО") }
        }

        if (!isDefaultHome) {
            Button(onClick = onMakeHome, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = HudStyle.buttonColors()) { Text("СДЕЛАТЬ ДОМАШНИМ ЭКРАНОМ") }
        }

        Section("GRID")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (c in 4..5) {
                FilterChip(
                    selected = settings.columns == c,
                    onClick = { onUpdate { it.copy(columns = c) } },
                    label = { Text("$c columns") },
                )
            }
        }

        SettingSlider("ICON SIZE", settings.iconSize, 48f..88f, "${settings.iconSize.roundToInt()} dp") { v ->
            onUpdate { it.copy(iconSize = v) }
        }
        SettingSlider("LABEL SIZE", settings.labelSize, 6f..12f, "%.1f sp".format(settings.labelSize)) { v ->
            onUpdate { it.copy(labelSize = v) }
        }
        SettingSlider("LABEL SPACING", settings.labelSpacing, 0f..12f, "%.1f dp".format(settings.labelSpacing)) { v ->
            onUpdate { it.copy(labelSpacing = v) }
        }

        SettingSwitch("SHOW LABELS", settings.showLabels) { v -> onUpdate { it.copy(showLabels = v) } }
        SettingSwitch("SHOW BATTERY %", settings.showBatteryPercent) { v -> onUpdate { it.copy(showBatteryPercent = v) } }
        SettingSwitch("SHOW SIGNAL", settings.showSignal) { v -> onUpdate { it.copy(showSignal = v) } }
        SettingSwitch("СКРЫВАТЬ СИСТЕМНЫЙ СТАТУС-БАР", settings.hideSystemStatusBar) { v -> onUpdate { it.copy(hideSystemStatusBar = v) } }

        Section("ДМБ")
        SettingSwitch("ТАЙМЕР ДМБ", settings.dmbEnabled) { v -> onUpdate { it.copy(dmbEnabled = v) } }
        Text(
            "Срок службы ${DmbTimer.SERVICE_DAYS} дней, начало: ${DmbTimer.formatDay(settings.dmbStartDay)}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { showDmbDatePicker = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = HudStyle.buttonColors(),
            ) { Text("ДАТА ПРИЗЫВА") }
            OutlinedButton(onClick = { onUpdate { it.copy(dmbStartDay = DmbTimer.today()) } }, modifier = Modifier.weight(1f)) {
                Text("СЕГОДНЯ")
            }
        }

        Section("WALLPAPER")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onPickWallpaper,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = HudStyle.buttonColors(),
            ) { Text("СВОИ ОБОИ") }
            OutlinedButton(onClick = onResetWallpaper, enabled = wallpaper != null, modifier = Modifier.weight(1f)) {
                Text("ВСТРОЕННЫЕ")
            }
        }
        SettingSlider("WALLPAPER INTENSITY", settings.wallpaperIntensity, 0f..1f, "${(settings.wallpaperIntensity * 100).roundToInt()}%") { v ->
            onUpdate { it.copy(wallpaperIntensity = v) }
        }
        SettingSlider("BACKGROUND DIM", settings.backgroundDim, 0f..0.8f, "${(settings.backgroundDim * 100).roundToInt()}%") { v ->
            onUpdate { it.copy(backgroundDim = v) }
        }
        SettingSlider("ANIMATION INTENSITY", settings.animationIntensity, 0f..1f, "${(settings.animationIntensity * 100).roundToInt()}%") { v ->
            onUpdate { it.copy(animationIntensity = v) }
        }

        Section("CONFIG")
        OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) { Text("RESET APP MAPPINGS") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onExport, modifier = Modifier.weight(1f)) { Text("EXPORT CONFIG") }
            OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) { Text("IMPORT CONFIG") }
        }

        Section("ABOUT / VERSION")
        Text("HUD Launcher $versionName", color = MaterialTheme.colorScheme.onSurface)
        Text(
            "Подписи иконок фиксированы. Долгое нажатие на иконку — выбрать приложение, " +
                "на фон — режим редактирования, на верхнюю панель — настройки.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        )
        Spacer(Modifier.height(24.dp))
    }
    }

    if (showDmbDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (if (settings.dmbStartDay > 0L) settings.dmbStartDay else DmbTimer.today()) * 86_400_000L,
        )
        DatePickerDialog(
            onDismissRequest = { showDmbDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms -> onUpdate { it.copy(dmbStartDay = Math.floorDiv(ms, 86_400_000L)) } }
                    showDmbDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDmbDatePicker = false }) { Text("ОТМЕНА") } },
        ) { DatePicker(state = pickerState) }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = HudPanelColor,
            shape = HudPanelShape,
            title = { Text("Сбросить назначения?", color = HudNeonPink) },
            text = { Text("Все выбранные приложения будут отвязаны от HUD-иконок.") },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; onResetMappings() }) { Text("СБРОСИТЬ") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("ОТМЕНА") } },
        )
    }
}

@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(10.dp))
    Text(title, color = HudCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    HorizontalDivider(color = HudCyan.copy(alpha = 0.3f))
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onChange, colors = HudStyle.switchColors())
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueText: String,
    onCommit: (Float) -> Unit,
) {
    var local by remember(value) { mutableFloatStateOf(value) }
    Column {
        Row(Modifier.fillMaxWidth()) {
            Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp)
            Text(valueText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        }
        Slider(
            value = local,
            onValueChange = { local = it },
            onValueChangeFinished = { onCommit(local) },
            valueRange = range,
            colors = HudStyle.sliderColors(),
        )
    }
}

@Preview(heightDp = 800)
@Composable
private fun SettingsPreview() {
    HudTheme {
        SettingsScreen(UiSettings(), false, "1.0.0", null, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

private val HudPanelColor = Color(0xFF241046)
