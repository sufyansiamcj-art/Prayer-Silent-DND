package com.example.service

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build

object PrayerDndManager {

    fun hasDndPermission(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    fun activateSilence(context: Context, mode: String): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        return try {
            when (mode.uppercase()) {
                "DND" -> {
                    if (notificationManager.isNotificationPolicyAccessGranted) {
                        // Allows alarms (Adhan) while silencing all calls, notifications, and alerts
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
                        true
                    } else {
                        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                        false
                    }
                }
                "SILENT" -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                    true
                }
                "VIBRATE" -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    true
                }
                else -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun restoreNormalMode(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        try {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
