package com.example.data.local

data class PrayerSettings(
    val cityName: String = "مكة المكرمة",
    val countryName: String = "المملكة العربية السعودية",
    val latitude: Double = 21.4225,
    val longitude: Double = 39.8262,
    val timeZoneOffset: Double = 3.0,
    val calcMethodIndex: Int = 0, // 0: Umm Al-Qura, 1: Egyptian, 2: MWL, 3: ISNA, 4: Karachi, 5: Dubai
    val juristicMethodIndex: Int = 0, // 0: Standard (Shafi'i/Maliki/Hanbali), 1: Hanafi
    val dndMode: String = "DND", // "DND", "SILENT", "VIBRATE"
    val dndTriggerOffsetMinutes: Int = 0, // 0, 2, 5, 10, -1
    val playAdhanAudio: Boolean = true,
    val selectedReciter: String = "أذان الشيخ صديق أحمد حمدون (السودان)", // Reciter name
    val isAutoLocationEnabled: Boolean = false,
    val timeZoneName: String = "GMT+3",
    val fajrDurationMin: Int = 25,
    val dhuhrDurationMin: Int = 20,
    val asrDurationMin: Int = 20,
    val maghribDurationMin: Int = 20,
    val ishaDurationMin: Int = 25,
    val jumuahDurationMin: Int = 45,
    val fajrDndEnabled: Boolean = true,
    val dhuhrDndEnabled: Boolean = true,
    val asrDndEnabled: Boolean = true,
    val maghribDndEnabled: Boolean = true,
    val ishaDndEnabled: Boolean = true,
    val jumuahDndEnabled: Boolean = true,
    // Manual minute adjustments (+/-)
    val fajrManualOffset: Int = 0,
    val sunriseManualOffset: Int = 0,
    val dhuhrManualOffset: Int = 0,
    val asrManualOffset: Int = 0,
    val maghribManualOffset: Int = 0,
    val ishaManualOffset: Int = 0
)
