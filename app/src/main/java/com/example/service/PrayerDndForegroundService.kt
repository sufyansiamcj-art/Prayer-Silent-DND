package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.DndHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrayerDndForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "prayer_dnd_active_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_SERVICE = "com.example.prayersilent.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.prayersilent.STOP_SERVICE"
        const val ACTION_STOP_ADHAN = "com.example.prayersilent.STOP_ADHAN"

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
        const val EXTRA_DND_MODE = "extra_dnd_mode"
        const val EXTRA_PLAY_ADHAN = "extra_play_adhan"
        const val EXTRA_RECITER_NAME = "extra_reciter_name"

        @Volatile
        var isServiceRunning = false
            private set

        @Volatile
        var isAdhanPlaying = false
            private set

        @Volatile
        var activePrayerName: String = ""
            private set

        @Volatile
        var remainingSeconds: Long = 0
            private set
    }

    private var countDownTimer: CountDownTimer? = null
    private var serviceStartTimeMillis: Long = 0
    private var currentDurationMin: Int = 20
    private var currentMode: String = "DND"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_SERVICE

        if (action == ACTION_STOP_SERVICE) {
            stopPrayerDnd(cancelledEarly = true)
            return START_NOT_STICKY
        }

        if (action == ACTION_STOP_ADHAN) {
            AdhanAudioPlayer.stopPreview()
            isAdhanPlaying = false
            updateNotification("وضع الصامت مفعل - صلاة $activePrayerName", "${remainingSeconds / 60} دقيقة متبقية")
            return START_STICKY
        }

        activePrayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        currentDurationMin = intent?.getIntExtra(EXTRA_DURATION_MINUTES, 20) ?: 20
        currentMode = intent?.getStringExtra(EXTRA_DND_MODE) ?: "DND"
        val shouldPlayAdhan = intent?.getBooleanExtra(EXTRA_PLAY_ADHAN, true) ?: true
        val reciter = intent?.getStringExtra(EXTRA_RECITER_NAME) ?: "أذان الشيخ صديق أحمد حمدون (السودان)"
        serviceStartTimeMillis = System.currentTimeMillis()

        isServiceRunning = true

        // Activate phone silence for prayer
        PrayerDndManager.activateSilence(this, currentMode)

        // Play Adhan sound if enabled
        if (shouldPlayAdhan) {
            try {
                isAdhanPlaying = true
                AdhanAudioPlayer.playAdhanForService(applicationContext, reciter) {
                    isAdhanPlaying = false
                    updateNotification("وضع الصامت مفعل - صلاة $activePrayerName", "${remainingSeconds / 60} دقيقة متبقية")
                }
            } catch (e: Exception) {
                isAdhanPlaying = false
                e.printStackTrace()
            }
        }

        val totalMillis = currentDurationMin * 60 * 1000L
        remainingSeconds = totalMillis / 1000

        val initialTitle = if (isAdhanPlaying) "حان وقت صلاة $activePrayerName - يرفع الأذان الآن" else "وضع الصامت مفعل لصلاة $activePrayerName"
        val initialContent = if (isAdhanPlaying) "يرفع الأذان الآن والهاتف في وضع الصامت" else "$currentDurationMin دقيقة متبقية"
        startForeground(NOTIFICATION_ID, buildNotification(initialTitle, initialContent))

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = millisUntilFinished / 1000
                val mins = remainingSeconds / 60
                val secs = remainingSeconds % 60
                val title = if (isAdhanPlaying) "حان وقت صلاة $activePrayerName - يرفع الأذان الآن" else "وضع الصامت مفعل - صلاة $activePrayerName"
                val text = if (isAdhanPlaying) {
                    String.format("يرفع الأذان الآن (%02d:%02d متبقية للصلاة)", mins, secs)
                } else {
                    String.format("%02d:%02d متبقية لإعادة الهاتف للوضع الطبيعي", mins, secs)
                }
                updateNotification(title, text)
            }

            override fun onFinish() {
                stopPrayerDnd(cancelledEarly = false)
            }
        }.start()

        return START_STICKY
    }

    private fun stopPrayerDnd(cancelledEarly: Boolean) {
        countDownTimer?.cancel()
        countDownTimer = null
        isServiceRunning = false
        isAdhanPlaying = false

        // Restore normal mode
        PrayerDndManager.restoreNormalMode(this)
        AdhanAudioPlayer.stopPreview()

        // Log to Room database
        val endTime = System.currentTimeMillis()
        val durationActual = ((endTime - serviceStartTimeMillis) / (1000 * 60)).toInt().coerceAtLeast(1)
        val statusStr = if (cancelledEarly) "CANCELLED_EARLY" else "COMPLETED"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                db.dndHistoryDao().insertHistory(
                    DndHistoryEntity(
                        prayerName = activePrayerName,
                        startTimeMillis = serviceStartTimeMillis,
                        endTimeMillis = endTime,
                        durationMinutes = durationActual,
                        dndMode = currentMode,
                        status = statusStr
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تفعيل وضع الصامت للأذان والصلاة",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعار تفاعلي أثناء رفع الأذان ووضع الصامت للصلاة"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, content: String) =
        NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle(title)
            setContentText(content)
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setOngoing(true)
            setPriority(NotificationCompat.PRIORITY_LOW)

            val openAppIntent = Intent(this@PrayerDndForegroundService, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                this@PrayerDndForegroundService,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setContentIntent(openAppPendingIntent)

            if (isAdhanPlaying) {
                val stopAdhanIntent = Intent(this@PrayerDndForegroundService, PrayerDndForegroundService::class.java).apply {
                    action = ACTION_STOP_ADHAN
                }
                val stopAdhanPendingIntent = PendingIntent.getService(
                    this@PrayerDndForegroundService,
                    2,
                    stopAdhanIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                addAction(R.drawable.ic_launcher_foreground, "إيقاف صوت الأذان", stopAdhanPendingIntent)
            }

            val stopIntent = Intent(this@PrayerDndForegroundService, PrayerDndForegroundService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            val stopPendingIntent = PendingIntent.getService(
                this@PrayerDndForegroundService,
                1,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            addAction(R.drawable.ic_launcher_foreground, "إلغاء وضع الصامت الآن", stopPendingIntent)
        }.build()

    private fun updateNotification(title: String, content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(title, content))
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        isServiceRunning = false
        super.onDestroy()
    }
}
