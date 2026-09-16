package com.example.data.prayer

import com.example.data.local.PrayerSettings
import java.util.Calendar
import java.util.Date
import kotlin.math.*

data class PrayerTimeItem(
    val nameAr: String,
    val nameEn: String,
    val key: String, // "FAJR", "SUNRISE", "DHUHR", "ASR", "MAGHRIB", "ISHA"
    val timeMillis: Long,
    val formattedTime: String,
    val isDndEnabled: Boolean,
    val durationMinutes: Int
)

data class PrayerSchedule(
    val dateMillis: Long,
    val hijriDateString: String,
    val gregorianDateString: String,
    val fajr: PrayerTimeItem,
    val sunrise: PrayerTimeItem,
    val dhuhr: PrayerTimeItem,
    val asr: PrayerTimeItem,
    val maghrib: PrayerTimeItem,
    val isha: PrayerTimeItem,
    val allPrayers: List<PrayerTimeItem>
)

object PrayerCalculator {

    val CALCULATION_METHODS = listOf(
        "أم القرى (مكة المكرمة)",
        "الهيئة المصرية العامة للمساحة",
        "رابطة العالم الإسلامي",
        "الجمعية الإسلامية لشمال أمريكا (ISNA)",
        "جامعة العلوم الإسلامية بكراتشي",
        "دائرة الشؤون الإسلامية بدبي"
    )

    val JURISTIC_METHODS = listOf(
        "الجمهور (شافعي، مالكي، حنبلي)",
        "الحنفي (ظل الشيء مثليه)"
    )

    fun calculateSchedule(calendar: Calendar, settings: PrayerSettings): PrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val lat = settings.latitude
        val lng = settings.longitude
        val tz = settings.timeZoneOffset

        // Angles per calculation method
        val (fajrAngle, ishaAngle, ishaIntervalMin) = when (settings.calcMethodIndex) {
            0 -> Triple(18.5, 0.0, 90) // Umm Al-Qura: Fajr 18.5°, Isha 90 min after Maghrib
            1 -> Triple(19.5, 17.5, 0) // Egyptian: Fajr 19.5°, Isha 17.5°
            2 -> Triple(18.0, 17.0, 0) // MWL: Fajr 18°, Isha 17°
            3 -> Triple(15.0, 15.0, 0) // ISNA: Fajr 15°, Isha 15°
            4 -> Triple(18.0, 18.0, 0) // Karachi: Fajr 18°, Isha 18°
            5 -> Triple(18.2, 18.2, 0) // Dubai: Fajr 18.2°, Isha 18.2°
            else -> Triple(18.5, 0.0, 90)
        }

        val asrFactor = if (settings.juristicMethodIndex == 1) 2 else 1

        // Julian Date
        val julianDate = julianDay(year, month, day)

        // Times in hours UTC
        val dhuhrUtc = dhuhrUtc(julianDate, lng)
        val sunriseUtc = sunAngleTimeUtc(julianDate, lat, lng, 0.833, true)
        val sunsetUtc = sunAngleTimeUtc(julianDate, lat, lng, 0.833, false)
        val fajrUtc = sunAngleTimeUtc(julianDate, lat, lng, fajrAngle, true)
        val asrUtc = asrTimeUtc(julianDate, lat, lng, asrFactor)
        val maghribUtc = sunsetUtc

        val ishaUtc = if (ishaIntervalMin > 0) {
            sunsetUtc + (ishaIntervalMin / 60.0)
        } else {
            sunAngleTimeUtc(julianDate, lat, lng, ishaAngle, false)
        }

        fun toMillis(utcHours: Double, manualOffsetMin: Int): Long {
            val localHours = utcHours + tz
            var hours = localHours.toInt()
            val fractionalHour = localHours - hours
            var minutes = (fractionalHour * 60).roundToInt() + manualOffsetMin

            if (minutes >= 60) {
                hours += minutes / 60
                minutes %= 60
            } else if (minutes < 0) {
                hours -= 1 + (abs(minutes) / 60)
                minutes = (60 - (abs(minutes) % 60)) % 60
            }

            val c = calendar.clone() as Calendar
            c.set(Calendar.HOUR_OF_DAY, (hours + 24) % 24)
            c.set(Calendar.MINUTE, minutes)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            return c.timeInMillis
        }

        fun formatTime(timeMillis: Long): String {
            val c = Calendar.getInstance()
            c.timeInMillis = timeMillis
            val hour = c.get(Calendar.HOUR)
            val formattedHour = if (hour == 0) 12 else hour
            val minute = c.get(Calendar.MINUTE)
            val amPm = if (c.get(Calendar.AM_PM) == Calendar.AM) "ص" else "م"
            return String.format("%02d:%02d %s", formattedHour, minute, amPm)
        }

        val isFriday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY

        val fajrTime = toMillis(fajrUtc, settings.fajrManualOffset)
        val sunriseTime = toMillis(sunriseUtc, settings.sunriseManualOffset)
        val dhuhrTime = toMillis(dhuhrUtc, settings.dhuhrManualOffset)
        val asrTime = toMillis(asrUtc, settings.asrManualOffset)
        val maghribTime = toMillis(maghribUtc, settings.maghribManualOffset)
        val ishaTime = toMillis(ishaUtc, settings.ishaManualOffset)

        val fajrItem = PrayerTimeItem("الفجر", "Fajr", "FAJR", fajrTime, formatTime(fajrTime), settings.fajrDndEnabled, settings.fajrDurationMin)
        val sunriseItem = PrayerTimeItem("الشروق", "Sunrise", "SUNRISE", sunriseTime, formatTime(sunriseTime), false, 0)
        val dhuhrName = if (isFriday) "الجمعة" else "الظهر"
        val dhuhrDuration = if (isFriday) settings.jumuahDurationMin else settings.dhuhrDurationMin
        val dhuhrEnabled = if (isFriday) settings.jumuahDndEnabled else settings.dhuhrDndEnabled
        val dhuhrItem = PrayerTimeItem(dhuhrName, "Dhuhr", "DHUHR", dhuhrTime, formatTime(dhuhrTime), dhuhrEnabled, dhuhrDuration)
        val asrItem = PrayerTimeItem("العصر", "Asr", "ASR", asrTime, formatTime(asrTime), settings.asrDndEnabled, settings.asrDurationMin)
        val maghribItem = PrayerTimeItem("المغرب", "Maghrib", "MAGHRIB", maghribTime, formatTime(maghribTime), settings.maghribDndEnabled, settings.maghribDurationMin)
        val ishaItem = PrayerTimeItem("العشاء", "Isha", "ISHA", ishaTime, formatTime(ishaTime), settings.ishaDndEnabled, settings.ishaDurationMin)

        val gregorianDate = String.format("%d/%02d/%d", day, month, year)
        val hijriDate = calculateHijriDate(calendar)

        val list = listOf(fajrItem, sunriseItem, dhuhrItem, asrItem, maghribItem, ishaItem)

        return PrayerSchedule(
            dateMillis = calendar.timeInMillis,
            hijriDateString = hijriDate,
            gregorianDateString = gregorianDate,
            fajr = fajrItem,
            sunrise = sunriseItem,
            dhuhr = dhuhrItem,
            asr = asrItem,
            maghrib = maghribItem,
            isha = ishaItem,
            allPrayers = list
        )
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun dhuhrUtc(jd: Double, lng: Double): Double {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        return fixHour(12.0 - eqt - lng / 15.0)
    }

    private fun sunAngleTimeUtc(jd: Double, lat: Double, lng: Double, angle: Double, isMorning: Boolean): Double {
        val dhuhr = dhuhrUtc(jd, lng)
        val decl = sunDeclination(jd)
        val num = -sin(Math.toRadians(angle)) - sin(Math.toRadians(lat)) * sin(Math.toRadians(decl))
        val den = cos(Math.toRadians(lat)) * cos(Math.toRadians(decl))
        val cosH = num / den
        if (cosH < -1.0 || cosH > 1.0) return dhuhr // Latitude boundary default fallback

        val h = Math.toDegrees(acos(cosH)) / 15.0
        return if (isMorning) dhuhr - h else dhuhr + h
    }

    private fun asrTimeUtc(jd: Double, lat: Double, lng: Double, factor: Int): Double {
        val dhuhr = dhuhrUtc(jd, lng)
        val decl = sunDeclination(jd)
        val phi = abs(lat - decl)
        val acotVal = factor + tan(Math.toRadians(phi))
        val angle = Math.toDegrees(atan(1.0 / acotVal))
        val num = sin(Math.toRadians(angle)) - sin(Math.toRadians(lat)) * sin(Math.toRadians(decl))
        val den = cos(Math.toRadians(lat)) * cos(Math.toRadians(decl))
        val cosH = num / den
        if (cosH < -1.0 || cosH > 1.0) return dhuhr + 3.0

        val h = Math.toDegrees(acos(cosH)) / 15.0
        return dhuhr + h
    }

    private fun sunDeclination(jd: Double): Double {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        return Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
    }

    private fun fixAngle(a: Double): Double {
        var angle = a - 360.0 * floor(a / 360.0)
        if (angle < 0) angle += 360.0
        return angle
    }

    private fun fixHour(a: Double): Double {
        var hour = a - 24.0 * floor(a / 24.0)
        if (hour < 0) hour += 24.0
        return hour
    }

    fun calculateHijriDate(calendar: Calendar): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        var jd = julianDay(year, month, day).toInt()
        var l = jd - 1948440 + 10632
        val n = floor((l - 1) / 10631.0).toInt()
        l = l - 10631 * n + 354
        val j = (floor((10985 - l) / 5316.0) * floor((50 * l) / 17719.0) + floor(l / 5670.0) * floor((43 * l) / 15238.0)).toInt()
        l = l - floor((30 - j) / 15.0).toInt() * floor((17719 * j) / 50.0).toInt() - floor(j / 30.0).toInt() * floor((15238 * j) / 43.0).toInt() + 29
        val m = floor((24 * l) / 709.0).toInt()
        val d = l - floor((709 * m) / 24.0).toInt()
        val y = 30 * n + j - 30

        val hijriMonths = listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )

        val monthName = if (m in 1..12) hijriMonths[m - 1] else "محرم"
        return "$d $monthName $y هـ"
    }

    // Great circle Qibla angle calculation from Kaaba (21.4225 N, 39.8262 E)
    fun calculateQiblaDirection(lat: Double, lng: Double): Double {
        val kaabaLat = Math.toRadians(21.4225)
        val kaabaLng = Math.toRadians(39.8262)
        val userLat = Math.toRadians(lat)
        val userLng = Math.toRadians(lng)

        val dLng = kaabaLng - userLng
        val y = sin(dLng)
        val x = cos(userLat) * tan(kaabaLat) - sin(userLat) * cos(dLng)

        var qiblaRad = atan2(y, x)
        var qiblaDeg = Math.toDegrees(qiblaRad)
        if (qiblaDeg < 0) qiblaDeg += 360.0
        return qiblaDeg
    }
}
