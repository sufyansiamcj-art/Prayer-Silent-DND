package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.PrayerSettings
import com.example.data.prayer.PrayerCalculator
import java.util.Calendar

object AlarmScheduler {

    fun scheduleNextAlarms(context: Context, settings: PrayerSettings) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val now = System.currentTimeMillis()

        // Schedule for today and tomorrow
        for (dayOffset in 0..1) {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }

            val schedule = PrayerCalculator.calculateSchedule(calendar, settings)

            schedule.allPrayers.forEachIndexed { index, prayer ->
                // Skip Sunrise
                if (prayer.key == "SUNRISE") return@forEachIndexed

                if (!prayer.isDndEnabled && !settings.playAdhanAudio) return@forEachIndexed

                // If Adhan audio is enabled, trigger exactly at prayer time so the adhan calls on time
                val triggerMillis = if (settings.playAdhanAudio) {
                    prayer.timeMillis
                } else {
                    prayer.timeMillis + (settings.dndTriggerOffsetMinutes * 60 * 1000L)
                }

                if (triggerMillis > now) {
                    val requestCode = (dayOffset * 10) + index
                    scheduleAlarm(
                        context = context,
                        requestCode = requestCode,
                        triggerMillis = triggerMillis,
                        prayerName = prayer.nameAr,
                        durationMinutes = prayer.durationMinutes,
                        dndMode = settings.dndMode,
                        playAdhan = settings.playAdhanAudio,
                        reciterName = settings.selectedReciter
                    )
                }
            }
        }
    }

    private fun scheduleAlarm(
        context: Context,
        requestCode: Int,
        triggerMillis: Long,
        prayerName: String,
        durationMinutes: Int,
        dndMode: String,
        playAdhan: Boolean,
        reciterName: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, PrayerDndReceiver::class.java).apply {
            action = PrayerDndReceiver.ACTION_START_PRAYER_DND
            putExtra(PrayerDndReceiver.EXTRA_PRAYER_NAME, prayerName)
            putExtra(PrayerDndReceiver.EXTRA_DURATION_MINUTES, durationMinutes)
            putExtra(PrayerDndReceiver.EXTRA_DND_MODE, dndMode)
            putExtra(PrayerDndReceiver.EXTRA_PLAY_ADHAN, playAdhan)
            putExtra(PrayerDndReceiver.EXTRA_RECITER_NAME, reciterName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerTestDnd(context: Context, durationSeconds: Int = 30, mode: String = "DND") {
        val intent = Intent(context, PrayerDndReceiver::class.java).apply {
            action = PrayerDndReceiver.ACTION_TEST_DND
            putExtra(PrayerDndReceiver.EXTRA_PRAYER_NAME, "اختبار عدم الإزعاج (30 ثانية)")
            putExtra(PrayerDndReceiver.EXTRA_DURATION_MINUTES, (durationSeconds / 60.0).coerceAtLeast(1.0).toInt())
            putExtra(PrayerDndReceiver.EXTRA_DND_MODE, mode)
            putExtra(PrayerDndReceiver.EXTRA_PLAY_ADHAN, false)
        }
        context.sendBroadcast(intent)
    }

    fun triggerTestAdhan(context: Context, reciterName: String) {
        val intent = Intent(context, PrayerDndReceiver::class.java).apply {
            action = PrayerDndReceiver.ACTION_TEST_ADHAN
            putExtra(PrayerDndReceiver.EXTRA_PRAYER_NAME, "أذان تجريبي")
            putExtra(PrayerDndReceiver.EXTRA_DURATION_MINUTES, 2)
            putExtra(PrayerDndReceiver.EXTRA_DND_MODE, "DND")
            putExtra(PrayerDndReceiver.EXTRA_PLAY_ADHAN, true)
            putExtra(PrayerDndReceiver.EXTRA_RECITER_NAME, reciterName)
        }
        context.sendBroadcast(intent)
    }

    fun cancelActiveDnd(context: Context) {
        val intent = Intent(context, PrayerDndReceiver::class.java).apply {
            action = PrayerDndReceiver.ACTION_STOP_PRAYER_DND
        }
        context.sendBroadcast(intent)
    }
}
