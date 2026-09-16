package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import com.example.R

object AdhanAudioPlayer {

    private const val TAG = "AdhanAudioPlayer"

    val RECITERS = listOf(
        "أذان الشيخ صديق أحمد حمدون (السودان)",
        "أذان الحرم المكي الشريف",
        "أذان المسجد النبوي الشريف",
        "أذان المسجد الأقصى المبارك",
        "الشيخ عبد الباسط عبد الصمد",
        "الشيخ مشاري العفاسي"
    )

    private val RAW_RECITERS = mapOf(
        "أذان الشيخ صديق أحمد حمدون (السودان)" to R.raw.adhan_siddiq,
        "الشيخ صديق حمدون" to R.raw.adhan_siddiq,
        "أذان الحرم المكي الشريف" to R.raw.adhan_makkah,
        "أذان المسجد النبوي الشريف" to R.raw.adhan_madinah,
        "أذان المسجد الأقصى المبارك" to R.raw.adhan_aqsa
    )

    private val RECITER_URLS = mapOf(
        "أذان الشيخ صديق أحمد حمدون (السودان)" to "https://www.islamcan.com/audio/adhan/azan1.mp3",
        "الشيخ صديق حمدون" to "https://www.islamcan.com/audio/adhan/azan1.mp3",
        "أذان الحرم المكي الشريف" to "https://www.islamcan.com/audio/adhan/azan2.mp3",
        "أذان المسجد النبوي الشريف" to "https://www.islamcan.com/audio/adhan/azan3.mp3",
        "أذان المسجد الأقصى المبارك" to "https://www.islamcan.com/audio/adhan/azan4.mp3",
        "الشيخ عبد الباسط عبد الصمد" to "https://www.islamcan.com/audio/adhan/azan5.mp3",
        "الشيخ مشاري العفاسي" to "https://www.islamcan.com/audio/adhan/azan6.mp3"
    )

    private var mediaPlayer: MediaPlayer? = null
    @Volatile
    var isPlaying: Boolean = false
        private set

    fun isPlayingAudio(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }

    fun playPreviewAdhanSound(
        reciterName: String,
        context: Context? = null,
        onFinished: () -> Unit = {}
    ) {
        stopPreview()

        val rawResId = RAW_RECITERS[reciterName] ?: RAW_RECITERS["أذان الشيخ صديق أحمد حمدون (السودان)"]

        if (context != null && rawResId != null) {
            try {
                mediaPlayer = MediaPlayer.create(context, rawResId)?.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setVolume(1.0f, 1.0f)
                    setOnCompletionListener {
                        stopPreview()
                        onFinished()
                    }
                    setOnErrorListener { _, _, _ ->
                        stopPreview()
                        playFromUrl(reciterName, context, onFinished)
                        true
                    }
                    start()
                }
                if (mediaPlayer != null) {
                    isPlaying = true
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing raw resource audio", e)
            }
        }

        playFromUrl(reciterName, context, onFinished)
    }

    private fun playFromUrl(
        reciterName: String,
        context: Context?,
        onFinished: () -> Unit
    ) {
        val audioUrl = RECITER_URLS[reciterName]
            ?: RECITER_URLS["أذان الشيخ صديق أحمد حمدون (السودان)"]
            ?: RECITER_URLS.values.first()

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    try {
                        mp.setVolume(1.0f, 1.0f)
                        mp.start()
                        this@AdhanAudioPlayer.isPlaying = true
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting preview", e)
                        fallbackSystemRingtone(context, onFinished)
                    }
                }
                setOnCompletionListener {
                    stopPreview()
                    onFinished()
                }
                setOnErrorListener { _, _, _ ->
                    stopPreview()
                    fallbackSystemRingtone(context, onFinished)
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up preview MediaPlayer", e)
            fallbackSystemRingtone(context, onFinished)
        }
    }

    fun playAdhanForService(
        context: Context,
        reciterName: String,
        onFinished: () -> Unit = {}
    ) {
        stopPreview()

        // Ensure alarm stream volume is audible
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            audioManager?.let { am ->
                val maxVol = am.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM)
                val currentVol = am.getStreamVolume(android.media.AudioManager.STREAM_ALARM)
                if (currentVol < (maxVol * 0.6f).toInt()) {
                    am.setStreamVolume(android.media.AudioManager.STREAM_ALARM, (maxVol * 0.85f).toInt().coerceAtLeast(1), 0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adjusting alarm volume", e)
        }

        val rawResId = RAW_RECITERS[reciterName] ?: RAW_RECITERS["أذان الشيخ صديق أحمد حمدون (السودان)"]

        if (rawResId != null) {
            try {
                mediaPlayer = MediaPlayer.create(context, rawResId)?.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    )
                    setVolume(1.0f, 1.0f)
                    setOnCompletionListener {
                        stopPreview()
                        onFinished()
                    }
                    setOnErrorListener { _, _, _ ->
                        stopPreview()
                        fallbackSystemRingtone(context, onFinished)
                        true
                    }
                    start()
                }
                if (mediaPlayer != null) {
                    isPlaying = true
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing raw adhan for service", e)
            }
        }

        val audioUrl = RECITER_URLS[reciterName]
            ?: RECITER_URLS["أذان الشيخ صديق أحمد حمدون (السودان)"]
            ?: RECITER_URLS.values.first()

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    try {
                        mp.setVolume(1.0f, 1.0f)
                        mp.start()
                        this@AdhanAudioPlayer.isPlaying = true
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting service adhan playback", e)
                        fallbackSystemRingtone(context, onFinished)
                    }
                }
                setOnCompletionListener {
                    stopPreview()
                    onFinished()
                }
                setOnErrorListener { _, _, _ ->
                    stopPreview()
                    fallbackSystemRingtone(context, onFinished)
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing adhan for service", e)
            fallbackSystemRingtone(context, onFinished)
        }
    }

    private fun fallbackSystemRingtone(context: Context?, onFinished: () -> Unit = {}) {
        if (context == null) {
            onFinished()
            return
        }
        try {
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, alarmUri)
                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnCompletionListener {
                    stopPreview()
                    onFinished()
                }
                setOnErrorListener { _, _, _ ->
                    stopPreview()
                    onFinished()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback system ringtone failed", e)
            onFinished()
        }
    }

    fun stopPreview() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping preview", e)
        } finally {
            mediaPlayer = null
            isPlaying = false
        }
    }
}

