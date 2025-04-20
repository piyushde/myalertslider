package com.example.myalertslider

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var currentModeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val silentButton = findViewById<Button>(R.id.btnSilent)
        val vibrateButton = findViewById<Button>(R.id.btnVibrate)
        val ringButton = findViewById<Button>(R.id.btnRing)
        currentModeText = findViewById(R.id.textCurrentMode)

        silentButton.setOnClickListener {
            if (hasDndPermission()) {
                // Try to set Silent + DND
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT

                val finalMode = audioManager.ringerMode
                val isDnd = notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE

                when {
                    finalMode == AudioManager.RINGER_MODE_SILENT && isDnd -> {
                        showToast("Silent mode activated (DND enabled)")
                    }
                    finalMode == AudioManager.RINGER_MODE_SILENT && !isDnd -> {
                        showToast("Silent mode active, but DND not granted")
                    }
                    finalMode == AudioManager.RINGER_MODE_VIBRATE -> {
                        showToast("Could not switch to Silent — Alert Slider is likely on Vibrate")
                    }
                    else -> {
                        showToast("Could not switch to Silent — check Alert Slider")
                    }
                }

                updateCurrentModeDisplay()
            }
        }

        vibrateButton.setOnClickListener {
            if (hasDndPermission()) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE

                if (audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                    showToast("Vibrate mode activated")
                } else {
                    showToast("Could not switch to Vibrate — Alert Slider may block it")
                }

                updateCurrentModeDisplay()
            }
        }

        ringButton.setOnClickListener {
            if (hasDndPermission()) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL

                if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                    showToast("Ring mode activated")
                } else if (audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                    showToast("Could not switch to Ring — Alert Slider is set to Vibrate")
                } else {
                    showToast("Could not switch to Ring — check Alert Slider")
                }

                updateCurrentModeDisplay()
            }
        }

        if (!hasDndPermission()) {
            Toast.makeText(
                this,
                "Please grant 'Do Not Disturb' access to enable full control.",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }

        updateCurrentModeDisplay()
    }

    override fun onResume() {
        super.onResume()
        updateCurrentModeDisplay()
        registerReceiver(ringerModeReceiver, IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(ringerModeReceiver)
    }

    private fun hasDndPermission(): Boolean {
        return if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
            false
        } else true
    }

    private fun updateCurrentModeDisplay() {
        val modeText = when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> "Silent"
            AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
            AudioManager.RINGER_MODE_NORMAL -> "Ring"
            else -> "Unknown"
        }
        currentModeText.text = "Current Mode: $modeText"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private val ringerModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
                updateCurrentModeDisplay()
            }
        }
    }
}
