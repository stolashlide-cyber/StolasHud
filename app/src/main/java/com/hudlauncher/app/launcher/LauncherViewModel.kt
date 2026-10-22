package com.hudlauncher.app.launcher

import android.app.Application
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hudlauncher.app.apps.InstalledApp
import com.hudlauncher.app.apps.LaunchResult
import com.hudlauncher.app.data.DefaultHudSlots
import com.hudlauncher.app.data.DmbTimer
import com.hudlauncher.app.data.HudSlot
import com.hudlauncher.app.data.UiSettings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiEvent {
    data class Message(val text: String) : UiEvent
    data class AppNotFound(val slotId: String) : UiEvent
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = LauncherRepository(application)

    val slots: StateFlow<List<HudSlot>> =
        repo.slots.stateIn(viewModelScope, SharingStarted.Eagerly, DefaultHudSlots.all)

    /** null until DataStore has been read for the first time. */
    val settings: StateFlow<UiSettings?> =
        repo.prefs.settings.map<UiSettings, UiSettings?> { it }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val onboardingDone: StateFlow<Boolean?> =
        repo.prefs.onboardingDone.map<Boolean, Boolean?> { it }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val wallpaper: StateFlow<ImageBitmap?> =
        repo.wallpaper.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val _editMode = MutableStateFlow(false)
    val editMode: StateFlow<Boolean> = _editMode.asStateFlow()

    private val _pickerSlotId = MutableStateFlow<String?>(null)
    val pickerSlotId: StateFlow<String?> = _pickerSlotId.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _isDefaultHome = MutableStateFlow(repo.isDefaultHome())
    val isDefaultHome: StateFlow<Boolean> = _isDefaultHome.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    init {
        refreshApps()
        viewModelScope.launch {
            val s = repo.prefs.settings.first()
            if (s.dmbStartDay == 0L) repo.prefs.updateSettings { it.copy(dmbStartDay = DmbTimer.today()) }
        }
    }

    fun onResume() {
        _isDefaultHome.value = repo.isDefaultHome()
    }

    /** HOME pressed while the launcher is already running: return to the plain home screen. */
    fun onHomePressed() {
        _pickerSlotId.value = null
        _showSettings.value = false
        _editMode.value = false
    }

    fun refreshApps() {
        viewModelScope.launch {
            val apps = repo.loadApps()
            _installedApps.value = apps
            repo.autoMapIfNeeded(apps)
        }
    }

    fun onSlotClick(slotId: String) {
        if (_editMode.value) {
            openPicker(slotId)
            return
        }
        val slot = slots.value.firstOrNull { it.id == slotId } ?: return
        when (repo.launch(slot)) {
            LaunchResult.LAUNCHED -> Unit
            LaunchResult.NOT_FOUND -> _events.tryEmit(UiEvent.AppNotFound(slotId))
            LaunchResult.UNASSIGNED -> {
                _events.tryEmit(UiEvent.Message("Приложение не назначено"))
                openPicker(slotId)
            }
        }
    }

    fun openPicker(slotId: String) {
        refreshApps()
        _pickerSlotId.value = slotId
    }

    fun closePicker() {
        _pickerSlotId.value = null
    }

    fun selectApp(slotId: String, app: InstalledApp?) {
        viewModelScope.launch { repo.setMapping(slotId, app?.target) }
    }

    fun setEditMode(enabled: Boolean) {
        _editMode.value = enabled
    }

    fun setShowSettings(show: Boolean) {
        _showSettings.value = show
    }

    fun updateSettings(transform: (UiSettings) -> UiSettings) {
        viewModelScope.launch { repo.prefs.updateSettings(transform) }
    }

    fun resetMappings() {
        viewModelScope.launch {
            repo.prefs.clearMappings()
            _events.tryEmit(UiEvent.Message("Назначения сброшены"))
        }
    }

    fun setWallpaper(uri: Uri) {
        viewModelScope.launch {
            val ok = runCatching { repo.setWallpaper(uri) }.isSuccess
            if (!ok) _events.tryEmit(UiEvent.Message("Не удалось установить обои"))
        }
    }

    fun resetWallpaper() {
        viewModelScope.launch { repo.resetWallpaper() }
    }

    fun finishOnboarding() {
        viewModelScope.launch { repo.prefs.setOnboardingDone() }
    }

    fun exportConfig(uri: Uri) {
        viewModelScope.launch {
            val ok = runCatching { repo.exportConfig(uri) }.isSuccess
            _events.tryEmit(UiEvent.Message(if (ok) "Конфигурация экспортирована" else "Не удалось экспортировать"))
        }
    }

    fun importConfig(uri: Uri) {
        viewModelScope.launch {
            val ok = runCatching { repo.importConfig(uri) }.isSuccess
            _events.tryEmit(UiEvent.Message(if (ok) "Конфигурация импортирована" else "Неверный файл конфигурации"))
        }
    }
}
