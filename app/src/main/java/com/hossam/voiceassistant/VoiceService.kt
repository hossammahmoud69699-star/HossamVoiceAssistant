package com.hossam.voiceassistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.media.AudioManager
import androidx.core.app.NotificationCompat
import java.util.Locale

class VoiceService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var audioManager: AudioManager
    private val channelId = "hossam_voice_channel"

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        startForeground(1, createNotification())
        startListening()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "المساعد الصوتي",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("حسام")
            .setContentText("المساعد الصوتي يعمل")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                restartListening()
            }

            override fun onError(error: Int) {
                restartListening()
            }

            override fun onResults(results: android.os.Bundle?) {
                val matches = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )
                matches?.firstOrNull()?.let { handleCommand(it) }
                restartListening()
            }

            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })

        restartListening()
    }

    private fun restartListening() {
        speechRecognizer?.let { recognizer ->
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG")
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                recognizer.startListening(intent)
            } catch (_: Exception) {
            }
        }
    }

    private fun handleCommand(command: String) {
        val text = command.lowercase(Locale("ar", "EG"))

        when {
            text.contains("ارفع الصوت") || text.contains("علي الصوت") -> {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_SHOW_UI
                )
            }

            text.contains("وطي الصوت") || text.contains("خفض الصوت") -> {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_SHOW_UI
                )
            }

            text.contains("افتح الشاشة") || text.contains("صحي الشاشة") -> {
                val powerIntent = Intent(
                    android.provider.Settings.ACTION_DISPLAY_SETTINGS
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(powerIntent)
            }

            text.contains("اقفل المساعد") || text.contains("ايقاف المساعد") -> {
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
