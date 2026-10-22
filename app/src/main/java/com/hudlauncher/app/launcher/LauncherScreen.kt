package com.hudlauncher.app.launcher

import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hudlauncher.app.apps.AppPickerSheet
import com.hudlauncher.app.apps.InstalledApp
import com.hudlauncher.app.data.DefaultHudSlots
import com.hudlauncher.app.data.HudSlot
import com.hudlauncher.app.data.UiSettings
import com.hudlauncher.app.hud.HudGrid
import com.hudlauncher.app.hud.HudStatusBar
import com.hudlauncher.app.hud.HudWallpaper
import com.hudlauncher.app.settings.SettingsScreen
import com.hudlauncher.app.ui.theme.HudCyan
import com.hudlauncher.app.ui.theme.HudNeonPink
import com.hudlauncher.app.ui.theme.HudTheme

@Composable
fun LauncherScreen(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val slots by viewModel.slots.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val onboardingDone by viewModel.onboardingDone.collectAsStateWithLifecycle()
    val apps by viewModel.installedApps.collectAsStateWithLifecycle()
    val editMode by viewModel.editMode.collectAsStateWithLifecycle()
    val pickerSlotId by viewModel.pickerSlotId.collectAsStateWithLifecycle()
    val showSettings by viewModel.showSettings.collectAsStateWithLifecycle()
    val isDefaultHome by viewModel.isDefaultHome.collectAsStateWithLifecycle()

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Message -> snackbar.showSnackbar(event.text)
                is UiEvent.AppNotFound -> {
                    val result = snackbar.showSnackbar("Приложение не найдено", actionLabel = "ВЫБРАТЬ")
                    if (result == SnackbarResult.ActionPerformed) viewModel.openPicker(event.slotId)
                }
            }
        }
    }

    val homeRequest = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.onResume()
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        uri?.let(viewModel::exportConfig)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let(viewModel::importConfig)
    }
    val requestHome = { HomeRole.request(context) { homeRequest.launch(it) } }

    val reducedMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }

    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "1.0"
    }

    BackHandler {
        when {
            pickerSlotId != null -> viewModel.closePicker()
            showSettings -> viewModel.setShowSettings(false)
            editMode -> viewModel.setEditMode(false)
            else -> Unit // already home: never close the launcher
        }
    }

    val ui = settings
    Box(Modifier.fillMaxSize()) {
        if (ui != null) {
            val animation = if (reducedMotion) 0f else ui.animationIntensity
            LauncherContent(
                slots = slots,
                settings = ui,
                animationIntensity = animation,
                editMode = editMode,
                isDefaultHome = isDefaultHome,
                onSlotClick = { viewModel.onSlotClick(it.id) },
                onSlotLongClick = { viewModel.openPicker(it.id) },
                onBackgroundLongPress = { viewModel.setEditMode(true) },
                onStatusBarLongPress = { viewModel.setShowSettings(true) },
                onExitEditMode = { viewModel.setEditMode(false) },
                onOpenSettings = { viewModel.setShowSettings(true) },
                onMakeHome = requestHome,
            )

            AnimatedVisibility(showSettings, enter = fadeIn(), exit = fadeOut()) {
                SettingsScreen(
                    settings = ui,
                    isDefaultHome = isDefaultHome,
                    versionName = versionName,
                    onUpdate = viewModel::updateSettings,
                    onResetMappings = viewModel::resetMappings,
                    onExport = { exportLauncher.launch("hud-launcher-config.json") },
                    onImport = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                    onMakeHome = requestHome,
                    onClose = { viewModel.setShowSettings(false) },
                )
            }
        } else {
            HudWallpaper(intensity = 1f, dim = 0f)
        }

        val slotForPicker = slots.firstOrNull { it.id == pickerSlotId }
        if (slotForPicker != null) {
            AppPickerSheet(
                slot = slotForPicker,
                apps = apps,
                onSelect = { app -> viewModel.selectApp(slotForPicker.id, app) },
                onReset = { viewModel.selectApp(slotForPicker.id, null) },
                onDismiss = viewModel::closePicker,
            )
        }

        SnackbarHost(
            snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 8.dp),
        ) { data -> Snackbar(data) }

        if (onboardingDone == false) {
            OnboardingDialog(
                onConfigure = { viewModel.finishOnboarding(); viewModel.setEditMode(true) },
                onMakeHome = { viewModel.finishOnboarding(); requestHome() },
                onSkip = viewModel::finishOnboarding,
            )
        }
    }
}

/** Stateless home screen (wallpaper, HUD bar, grid, edit bar) so it can be previewed. */
@Composable
fun LauncherContent(
    slots: List<HudSlot>,
    settings: UiSettings,
    animationIntensity: Float,
    editMode: Boolean,
    isDefaultHome: Boolean,
    onSlotClick: (HudSlot) -> Unit,
    onSlotLongClick: (HudSlot) -> Unit,
    onBackgroundLongPress: () -> Unit,
    onStatusBarLongPress: () -> Unit,
    onExitEditMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onMakeHome: () -> Unit,
) {
    val bgAlpha = remember { Animatable(if (animationIntensity > 0.05f) 0f else 1f) }
    LaunchedEffect(Unit) { if (bgAlpha.value < 1f) bgAlpha.animateTo(1f, tween(450)) }
    val currentLongPress by rememberUpdatedState(onBackgroundLongPress)
    val haptics = LocalHapticFeedback.current

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    currentLongPress()
                })
            },
    ) {
        HudWallpaper(
            intensity = settings.wallpaperIntensity,
            dim = settings.backgroundDim,
            modifier = Modifier.alpha(bgAlpha.value),
        )
        Column(Modifier.fillMaxSize()) {
            HudStatusBar(
                showSignal = settings.showSignal,
                showBatteryPercent = settings.showBatteryPercent,
                onLongPress = onStatusBarLongPress,
            )
            val cutout = WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal).asPaddingValues()
            val nav = WindowInsets.navigationBars.asPaddingValues()
            HudGrid(
                slots = slots,
                settings = settings,
                editMode = editMode,
                animationIntensity = animationIntensity,
                onSlotClick = onSlotClick,
                onSlotLongClick = onSlotLongClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 8.dp + cutout.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    end = 8.dp + cutout.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    top = 18.dp,
                    bottom = 16.dp + if (editMode) 64.dp else nav.calculateBottomPadding(),
                ),
            )
        }
        if (editMode) {
            EditBar(
                isDefaultHome = isDefaultHome,
                onSettings = onOpenSettings,
                onMakeHome = onMakeHome,
                onDone = onExitEditMode,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 10.dp),
            )
        }
    }
}

@Composable
private fun EditBar(
    isDefaultHome: Boolean,
    onSettings: () -> Unit,
    onMakeHome: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = Color(0xE61B0B33),
        tonalElevation = 4.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text("РЕДАКТИРОВАНИЕ", color = HudNeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp))
            TextButton(onClick = onSettings) { Text("НАСТРОЙКИ", fontSize = 12.sp) }
            if (!isDefaultHome) TextButton(onClick = onMakeHome) { Text("HOME", fontSize = 12.sp) }
            TextButton(onClick = onDone) { Text("ГОТОВО", fontSize = 12.sp, color = HudCyan) }
        }
    }
}

@Composable
private fun OnboardingDialog(onConfigure: () -> Unit, onMakeHome: () -> Unit, onSkip: () -> Unit) {
    Dialog(onDismissRequest = onSkip) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF241046)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("HUD LAUNCHER", color = HudNeonPink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Назначьте приложения долгим нажатием на иконки.")
                Text("После установки вы можете выбрать этот HUD как домашний экран Android.")
                Button(onClick = onConfigure, modifier = Modifier.fillMaxWidth()) { Text("НАСТРОИТЬ") }
                OutlinedButton(onClick = onMakeHome, modifier = Modifier.fillMaxWidth()) {
                    Text("СДЕЛАТЬ ДОМАШНИМ ЭКРАНОМ", fontSize = 13.sp)
                }
                TextButton(onClick = onSkip, modifier = Modifier.align(Alignment.End)) { Text("ПРОПУСТИТЬ") }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 720)
@Composable
private fun LauncherScreenPreview() {
    HudTheme {
        LauncherContent(
            slots = DefaultHudSlots.all,
            settings = UiSettings(),
            animationIntensity = 0f,
            editMode = false,
            isDefaultHome = true,
            onSlotClick = {}, onSlotLongClick = {}, onBackgroundLongPress = {},
            onStatusBarLongPress = {}, onExitEditMode = {}, onOpenSettings = {}, onMakeHome = {},
        )
    }
}
