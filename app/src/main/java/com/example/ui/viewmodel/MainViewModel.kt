package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DndHistoryEntity
import com.example.data.local.PrayerSettings
import com.example.data.local.SettingsRepository
import com.example.data.prayer.CityInfo
import com.example.data.prayer.PrayerCalculator
import com.example.data.prayer.PrayerSchedule
import com.example.data.prayer.PrayerTimeItem
import com.example.service.AlarmScheduler
import com.example.service.PrayerDndForegroundService
import com.example.data.location.DeviceLocationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class MainUiState(
    val settings: PrayerSettings = PrayerSettings(),
    val schedule: PrayerSchedule? = null,
    val nextPrayer: PrayerTimeItem? = null,
    val timeToNextPrayerString: String = "00:00:00",
    val isDndActive: Boolean = false,
    val isAdhanPlaying: Boolean = false,
    val activeDndPrayerName: String = "",
    val activeDndRemainingSeconds: Long = 0,
    val historyList: List<DndHistoryEntity> = emptyList(),
    val completedCount: Int = 0,
    val totalSilencedMinutes: Int = 0,
    // GPS & Location Status
    val isLocating: Boolean = false,
    val locationMessage: String? = null,
    // Iqamah Countdown Status
    val isPrayerTimeEntered: Boolean = false,
    val currentEnteredPrayer: PrayerTimeItem? = null,
    val remainingSecondsToIqama: Long = 0,
    val formattedTimeToIqama: String = "00:00",
    val iqamaProgress: Float = 0f,
    val isTestIqamaActive: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val settingsRepo = SettingsRepository(application)

    val settingsState: StateFlow<PrayerSettings> = settingsRepo.settings

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Observe settings changes
        viewModelScope.launch {
            settingsRepo.settings.collect { settings ->
                recalculateSchedule(settings)
                AlarmScheduler.scheduleNextAlarms(getApplication(), settings)
            }
        }

        // Observe Room DB History
        viewModelScope.launch {
            db.dndHistoryDao().getAllHistory().collect { history ->
                _uiState.update { it.copy(historyList = history) }
            }
        }

        viewModelScope.launch {
            db.dndHistoryDao().getCompletedCount().collect { count ->
                _uiState.update { it.copy(completedCount = count) }
            }
        }

        viewModelScope.launch {
            db.dndHistoryDao().getTotalSilencedMinutes().collect { minutes ->
                _uiState.update { it.copy(totalSilencedMinutes = minutes ?: 0) }
            }
        }

        // Timer ticker loop for countdowns & foreground service state polling
        viewModelScope.launch {
            while (true) {
                tickTimer()
                delay(1000)
            }
        }
    }

    private fun recalculateSchedule(settings: PrayerSettings) {
        val nowCal = Calendar.getInstance()
        val schedule = PrayerCalculator.calculateSchedule(nowCal, settings)

        // Find next prayer
        val nowMillis = System.currentTimeMillis()
        var next: PrayerTimeItem? = schedule.allPrayers.firstOrNull { it.timeMillis > nowMillis }

        if (next == null) {
            // Tomorrow's Fajr
            val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val tomorrowSchedule = PrayerCalculator.calculateSchedule(tomorrowCal, settings)
            next = tomorrowSchedule.fajr
        }

        _uiState.update {
            it.copy(
                settings = settings,
                schedule = schedule,
                nextPrayer = next
            )
        }
    }

    private var simulatedIqamaSeconds: Long? = null

    private fun tickTimer() {
        val currentNext = _uiState.value.nextPrayer
        val nowMillis = System.currentTimeMillis()

        var formattedCountdown = "00:00:00"
        if (currentNext != null && currentNext.timeMillis > nowMillis) {
            val diffSecs = (currentNext.timeMillis - nowMillis) / 1000
            val hours = diffSecs / 3600
            val mins = (diffSecs % 3600) / 60
            val secs = diffSecs % 60
            formattedCountdown = String.format("%02d:%02d:%02d", hours, mins, secs)
        } else {
            // Recalculate schedule if next prayer time passed
            _uiState.value.settings.let { recalculateSchedule(it) }
        }

        // Check if any prayer time has entered and calculate remaining time to Iqamah
        val schedule = _uiState.value.schedule
        var isEntered = false
        var enteredPrayer: PrayerTimeItem? = null
        var remainingSecsToIqama = 0L
        var formattedIqama = "00:00"
        var iqamaProg = 0f

        if (simulatedIqamaSeconds != null) {
            val secs = simulatedIqamaSeconds!!
            if (secs > 0) {
                isEntered = true
                simulatedIqamaSeconds = secs - 1
                remainingSecsToIqama = secs
                val m = secs / 60
                val s = secs % 60
                formattedIqama = String.format("%02d:%02d", m, s)
                val totalSecs = 15 * 60f
                iqamaProg = ((totalSecs - secs) / totalSecs).coerceIn(0f, 1f)
                enteredPrayer = schedule?.dhuhr ?: PrayerTimeItem("الظهر", "Dhuhr", "DHUHR", nowMillis, "12:15 م", true, 15)
            } else {
                simulatedIqamaSeconds = null
                _uiState.update { it.copy(isTestIqamaActive = false) }
            }
        } else if (schedule != null) {
            // Check each of the 5 prayers with Iqamah (Fajr, Dhuhr, Asr, Maghrib, Isha)
            val prayersToCheck = listOf(schedule.fajr, schedule.dhuhr, schedule.asr, schedule.maghrib, schedule.isha)
            for (prayer in prayersToCheck) {
                val iqamaMillis = prayer.timeMillis + (prayer.durationMinutes * 60 * 1000L)
                if (nowMillis >= prayer.timeMillis && nowMillis < iqamaMillis) {
                    isEntered = true
                    enteredPrayer = prayer
                    val diffMs = iqamaMillis - nowMillis
                    remainingSecsToIqama = (diffMs / 1000L).coerceAtLeast(0L)
                    val m = remainingSecsToIqama / 60
                    val s = remainingSecsToIqama % 60
                    formattedIqama = String.format("%02d:%02d", m, s)
                    val totalSecs = (prayer.durationMinutes * 60).toFloat()
                    iqamaProg = if (totalSecs > 0f) ((totalSecs - remainingSecsToIqama) / totalSecs).coerceIn(0f, 1f) else 0f
                    break
                }
            }
        }

        val dndRunning = PrayerDndForegroundService.isServiceRunning
        val adhanRunning = PrayerDndForegroundService.isAdhanPlaying || com.example.service.AdhanAudioPlayer.isPlaying
        val activeName = PrayerDndForegroundService.activePrayerName
        val remainingSecs = PrayerDndForegroundService.remainingSeconds

        _uiState.update {
            it.copy(
                timeToNextPrayerString = formattedCountdown,
                isDndActive = dndRunning,
                isAdhanPlaying = adhanRunning,
                activeDndPrayerName = activeName,
                activeDndRemainingSeconds = remainingSecs,
                isPrayerTimeEntered = isEntered,
                currentEnteredPrayer = enteredPrayer,
                remainingSecondsToIqama = remainingSecsToIqama,
                formattedTimeToIqama = formattedIqama,
                iqamaProgress = iqamaProg
            )
        }
    }

    fun togglePrayerDnd(prayerKey: String, enabled: Boolean) {
        val current = settingsState.value
        val updated = when (prayerKey) {
            "FAJR" -> current.copy(fajrDndEnabled = enabled)
            "DHUHR" -> current.copy(dhuhrDndEnabled = enabled)
            "ASR" -> current.copy(asrDndEnabled = enabled)
            "MAGHRIB" -> current.copy(maghribDndEnabled = enabled)
            "ISHA" -> current.copy(ishaDndEnabled = enabled)
            else -> current
        }
        settingsRepo.updateSettings(updated)
    }

    fun updateDndMode(mode: String) {
        val updated = settingsState.value.copy(dndMode = mode)
        settingsRepo.updateSettings(updated)
    }

    fun updateDndOffset(offsetMinutes: Int) {
        val updated = settingsState.value.copy(dndTriggerOffsetMinutes = offsetMinutes)
        settingsRepo.updateSettings(updated)
    }

    fun updatePrayerDuration(prayerKey: String, durationMinutes: Int) {
        val current = settingsState.value
        val updated = when (prayerKey) {
            "FAJR" -> current.copy(fajrDurationMin = durationMinutes)
            "DHUHR" -> current.copy(dhuhrDurationMin = durationMinutes)
            "ASR" -> current.copy(asrDurationMin = durationMinutes)
            "MAGHRIB" -> current.copy(maghribDurationMin = durationMinutes)
            "ISHA" -> current.copy(ishaDurationMin = durationMinutes)
            "JUMUAH" -> current.copy(jumuahDurationMin = durationMinutes)
            else -> current
        }
        settingsRepo.updateSettings(updated)
    }

    fun selectCity(city: CityInfo) {
        val updated = settingsState.value.copy(
            cityName = city.nameAr,
            countryName = city.countryAr,
            latitude = city.lat,
            longitude = city.lng,
            timeZoneOffset = city.timeZone
        )
        settingsRepo.updateSettings(updated)
    }

    fun setCalculationMethod(methodIndex: Int) {
        val updated = settingsState.value.copy(calcMethodIndex = methodIndex)
        settingsRepo.updateSettings(updated)
    }

    fun setJuristicMethod(methodIndex: Int) {
        val updated = settingsState.value.copy(juristicMethodIndex = methodIndex)
        settingsRepo.updateSettings(updated)
    }

    fun adjustPrayerOffset(prayerKey: String, deltaMinutes: Int) {
        val current = settingsState.value
        val updated = when (prayerKey) {
            "FAJR" -> current.copy(fajrManualOffset = current.fajrManualOffset + deltaMinutes)
            "SUNRISE" -> current.copy(sunriseManualOffset = current.sunriseManualOffset + deltaMinutes)
            "DHUHR" -> current.copy(dhuhrManualOffset = current.dhuhrManualOffset + deltaMinutes)
            "ASR" -> current.copy(asrManualOffset = current.asrManualOffset + deltaMinutes)
            "MAGHRIB" -> current.copy(maghribManualOffset = current.maghribManualOffset + deltaMinutes)
            "ISHA" -> current.copy(ishaManualOffset = current.ishaManualOffset + deltaMinutes)
            else -> current
        }
        settingsRepo.updateSettings(updated)
    }

    fun setReciter(reciterName: String) {
        val updated = settingsState.value.copy(selectedReciter = reciterName)
        settingsRepo.updateSettings(updated)
    }

    fun toggleAdhanAudio(enabled: Boolean) {
        val updated = settingsState.value.copy(playAdhanAudio = enabled)
        settingsRepo.updateSettings(updated)
    }

    fun testDnd30Seconds() {
        AlarmScheduler.triggerTestDnd(getApplication(), durationSeconds = 30, mode = settingsState.value.dndMode)
    }

    fun testAdhanAlarm() {
        AlarmScheduler.triggerTestAdhan(getApplication(), settingsState.value.selectedReciter)
    }

    fun stopAdhanAudio() {
        val intent = android.content.Intent(getApplication(), PrayerDndForegroundService::class.java).apply {
            action = PrayerDndForegroundService.ACTION_STOP_ADHAN
        }
        getApplication<Application>().startService(intent)
        com.example.service.AdhanAudioPlayer.stopPreview()
    }

    fun cancelActiveDnd() {
        AlarmScheduler.cancelActiveDnd(getApplication())
    }

    fun detectDeviceLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocating = true, locationMessage = "جاري الاتصال بموقع الجهاز (GPS)...") }
            val res = DeviceLocationHelper.getDeviceLocation(getApplication())
            res.onSuccess { loc ->
                val tzString = "GMT" + (if (loc.timeZoneOffset >= 0) "+${loc.timeZoneOffset}" else "${loc.timeZoneOffset}")
                val updated = settingsState.value.copy(
                    cityName = loc.cityName,
                    countryName = loc.countryName,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    timeZoneOffset = loc.timeZoneOffset,
                    timeZoneName = tzString,
                    isAutoLocationEnabled = true
                )
                settingsRepo.updateSettings(updated)
                _uiState.update {
                    it.copy(
                        isLocating = false,
                        locationMessage = "تم ضبط الموقع بنجاح: ${loc.cityName} ($tzString)"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLocating = false,
                        locationMessage = err.message ?: "تعذر تحديد الموقع الحالي"
                    )
                }
            }
        }
    }

    fun testIqamaCountdown(minutes: Int = 15) {
        simulatedIqamaSeconds = minutes * 60L
        _uiState.update { it.copy(isTestIqamaActive = true) }
    }

    fun stopTestIqamaCountdown() {
        simulatedIqamaSeconds = null
        _uiState.update { it.copy(isTestIqamaActive = false) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            db.dndHistoryDao().clearHistory()
        }
    }
}
