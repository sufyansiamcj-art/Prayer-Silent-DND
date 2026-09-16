package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.SettingsRepository

class PrayerDndReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_START_PRAYER_DND = "com.example.prayersilent.ACTION_START_PRAYER_DND"
        const val ACTION_STOP_PRAYER_DND = "com.example.prayersilent.ACTION_STOP_PRAYER_DND"
        const val ACTION_TEST_DND = "com.example.prayersilent.ACTION_TEST_DND"
        const val ACTION_TEST_ADHAN = "com.example.prayersilent.ACTION_TEST_ADHAN"

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
        const val EXTRA_DND_MODE = "extra_dnd_mode"
        const val EXTRA_PLAY_ADHAN = "extra_play_adhan"
        const val EXTRA_RECITER_NAME = "extra_reciter_name"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            ACTION_START_PRAYER_DND, ACTION_TEST_DND, ACTION_TEST_ADHAN -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
                val duration = intent.getIntExtra(EXTRA_DURATION_MINUTES, 20)
                val mode = intent.getStringExtra(EXTRA_DND_MODE) ?: "DND"
                val playAdhan = intent.getBooleanExtra(EXTRA_PLAY_ADHAN, true)
                val reciterName = intent.getStringExtra(EXTRA_RECITER_NAME) ?: "أذان الشيخ صديق أحمد حمدون (السودان)"

                val serviceIntent = Intent(context, PrayerDndForegroundService::class.java).apply {
                    this.action = PrayerDndForegroundService.ACTION_START_SERVICE
                    putExtra(PrayerDndForegroundService.EXTRA_PRAYER_NAME, prayerName)
                    putExtra(PrayerDndForegroundService.EXTRA_DURATION_MINUTES, duration)
                    putExtra(PrayerDndForegroundService.EXTRA_DND_MODE, mode)
                    putExtra(PrayerDndForegroundService.EXTRA_PLAY_ADHAN, playAdhan)
                    putExtra(PrayerDndForegroundService.EXTRA_RECITER_NAME, reciterName)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }

            ACTION_STOP_PRAYER_DND -> {
                val serviceIntent = Intent(context, PrayerDndForegroundService::class.java).apply {
                    this.action = PrayerDndForegroundService.ACTION_STOP_SERVICE
                }
                context.startService(serviceIntent)
            }

            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // Reschedule all prayer alarms
                try {
                    val settingsRepo = SettingsRepository(context)
                    val settings = settingsRepo.settings.value
                    AlarmScheduler.scheduleNextAlarms(context, settings)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
