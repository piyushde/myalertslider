package com.example.myalertslider

import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast

class SoundModeWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_SILENT = "com.example.myalertslider.ACTION_SILENT"
        const val ACTION_VIBRATE = "com.example.myalertslider.ACTION_VIBRATE"
        const val ACTION_RING = "com.example.myalertslider.ACTION_RING"
        const val TAG = "SoundModeWidget"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (intent?.action) {
            ACTION_SILENT -> {
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                    showToast(context, "Silent mode (DND) activated")
                    Log.d(TAG, "Switched to Silent + DND")
                } else {
                    showToast(context, "Missing Do Not Disturb access")
                    Log.d(TAG, "No DND access for Silent")
                }
            }

            ACTION_VIBRATE -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                showToast(context, "Vibrate mode activated")
                Log.d(TAG, "Switched to Vibrate")
            }

            ACTION_RING -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                showToast(context, "Ring mode activated")
                Log.d(TAG, "Switched to Ring")
            }
        }

        updateWidgetUI(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            views.setOnClickPendingIntent(
                R.id.btn_widget_silent,
                createPendingIntent(context, ACTION_SILENT)
            )

            views.setOnClickPendingIntent(
                R.id.btn_widget_vibrate,
                createPendingIntent(context, ACTION_VIBRATE)
            )

            views.setOnClickPendingIntent(
                R.id.btn_widget_ring,
                createPendingIntent(context, ACTION_RING)
            )

            appWidgetManager.updateAppWidget(widgetId, views)
        }

        Log.d(TAG, "Widget onUpdate: PendingIntents set")
    }

    private fun createPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, SoundModeWidget::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateWidgetUI(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, SoundModeWidget::class.java)
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        appWidgetManager.updateAppWidget(widgetComponent, views)
    }
}
