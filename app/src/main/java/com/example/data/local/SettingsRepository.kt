package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("prayer_silent_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<PrayerSettings> = _settings.asStateFlow()

    private fun loadSettings(): PrayerSettings {
        return PrayerSettings(
            cityName = prefs.getString("cityName", "مكة المكرمة") ?: "مكة المكرمة",
            countryName = prefs.getString("countryName", "المملكة العربية السعودية") ?: "المملكة العربية السعودية",
            latitude = prefs.getFloat("latitude", 21.4225f).toDouble(),
            longitude = prefs.getFloat("longitude", 39.8262f).toDouble(),
            timeZoneOffset = prefs.getFloat("timeZoneOffset", 3.0f).toDouble(),
            calcMethodIndex = prefs.getInt("calcMethodIndex", 0),
            juristicMethodIndex = prefs.getInt("juristicMethodIndex", 0),
            dndMode = prefs.getString("dndMode", "DND") ?: "DND",
            dndTriggerOffsetMinutes = prefs.getInt("dndTriggerOffsetMinutes", 0),
            playAdhanAudio = prefs.getBoolean("playAdhanAudio", true),
            selectedReciter = prefs.getString("selectedReciter", "أذان الشيخ صديق أحمد حمدون (السودان)") ?: "أذان الشيخ صديق أحمد حمدون (السودان)",
            isAutoLocationEnabled = prefs.getBoolean("isAutoLocationEnabled", false),
            timeZoneName = prefs.getString("timeZoneName", "GMT+3") ?: "GMT+3",
            fajrDurationMin = prefs.getInt("fajrDurationMin", 25),
            dhuhrDurationMin = prefs.getInt("dhuhrDurationMin", 20),
            asrDurationMin = prefs.getInt("asrDurationMin", 20),
            maghribDurationMin = prefs.getInt("maghribDurationMin", 20),
            ishaDurationMin = prefs.getInt("ishaDurationMin", 25),
            jumuahDurationMin = prefs.getInt("jumuahDurationMin", 45),
            fajrDndEnabled = prefs.getBoolean("fajrDndEnabled", true),
            dhuhrDndEnabled = prefs.getBoolean("dhuhrDndEnabled", true),
            asrDndEnabled = prefs.getBoolean("asrDndEnabled", true),
            maghribDndEnabled = prefs.getBoolean("maghribDndEnabled", true),
            ishaDndEnabled = prefs.getBoolean("ishaDndEnabled", true),
            jumuahDndEnabled = prefs.getBoolean("jumuahDndEnabled", true),
            fajrManualOffset = prefs.getInt("fajrManualOffset", 0),
            sunriseManualOffset = prefs.getInt("sunriseManualOffset", 0),
            dhuhrManualOffset = prefs.getInt("dhuhrManualOffset", 0),
            asrManualOffset = prefs.getInt("asrManualOffset", 0),
            maghribManualOffset = prefs.getInt("maghribManualOffset", 0),
            ishaManualOffset = prefs.getInt("ishaManualOffset", 0)
        )
    }

    fun updateSettings(newSettings: PrayerSettings) {
        prefs.edit().apply {
            putString("cityName", newSettings.cityName)
            putString("countryName", newSettings.countryName)
            putFloat("latitude", newSettings.latitude.toFloat())
            putFloat("longitude", newSettings.longitude.toFloat())
            putFloat("timeZoneOffset", newSettings.timeZoneOffset.toFloat())
            putInt("calcMethodIndex", newSettings.calcMethodIndex)
            putInt("juristicMethodIndex", newSettings.juristicMethodIndex)
            putString("dndMode", newSettings.dndMode)
            putInt("dndTriggerOffsetMinutes", newSettings.dndTriggerOffsetMinutes)
            putBoolean("playAdhanAudio", newSettings.playAdhanAudio)
            putString("selectedReciter", newSettings.selectedReciter)
            putBoolean("isAutoLocationEnabled", newSettings.isAutoLocationEnabled)
            putString("timeZoneName", newSettings.timeZoneName)
            putInt("fajrDurationMin", newSettings.fajrDurationMin)
            putInt("dhuhrDurationMin", newSettings.dhuhrDurationMin)
            putInt("asrDurationMin", newSettings.asrDurationMin)
            putInt("maghribDurationMin", newSettings.maghribDurationMin)
            putInt("ishaDurationMin", newSettings.ishaDurationMin)
            putInt("jumuahDurationMin", newSettings.jumuahDurationMin)
            putBoolean("fajrDndEnabled", newSettings.fajrDndEnabled)
            putBoolean("dhuhrDndEnabled", newSettings.dhuhrDndEnabled)
            putBoolean("asrDndEnabled", newSettings.asrDndEnabled)
            putBoolean("maghribDndEnabled", newSettings.maghribDndEnabled)
            putBoolean("ishaDndEnabled", newSettings.ishaDndEnabled)
            putBoolean("jumuahDndEnabled", newSettings.jumuahDndEnabled)
            putInt("fajrManualOffset", newSettings.fajrManualOffset)
            putInt("sunriseManualOffset", newSettings.sunriseManualOffset)
            putInt("dhuhrManualOffset", newSettings.dhuhrManualOffset)
            putInt("asrManualOffset", newSettings.asrManualOffset)
            putInt("maghribManualOffset", newSettings.maghribManualOffset)
            putInt("ishaManualOffset", newSettings.ishaManualOffset)
            apply()
        }
        _settings.value = newSettings
    }
}
