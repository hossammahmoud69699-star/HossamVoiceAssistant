package com.hossam.voiceassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val requestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }

        val title = TextView(this).apply {
            text = "حسام - المساعد الصوتي"
            textSize = 25f
        }

        val status = TextView(this).apply {
            text = "اضغط تشغيل لبدء المساعد"
            textSize = 17f
            setPadding(0, 24, 0, 24)
        }

        val startButton = Button(this).apply {
            text = "تشغيل المساعد"
            setOnClickListener {
                if (checkPermission()) {
                    startAssistant()
                    status.text = "المساعد يعمل في الخلفية"
                } else {
                    requestPermissions()
                }
            }
        }

        val stopButton = Button(this).apply {
            text = "إيقاف المساعد"
            setOnClickListener {
                stopService(Intent(this@MainActivity, VoiceService::class.java))
                status.text = "تم إيقاف المساعد"
            }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(startButton)
        layout.addView(stopButton)

        setContentView(layout)
    }

    private fun checkPermission(): Boolean {
        val audioGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val notificationGranted =
            Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        return audioGranted && notificationGranted
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)

        if (Build.VERSION.SDK_INT >= 33) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        ActivityCompat.requestPermissions(
            this, permissions.toTypedArray(), requestCode
        )
    }

    private fun startAssistant() {
        val intent = Intent(this, VoiceService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }
}
