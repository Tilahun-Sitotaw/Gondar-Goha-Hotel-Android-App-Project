package com.gohahotel.connect.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gohahotel.connect.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class SunsetAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID   = "sunset_alerts"
        const val CHANNEL_NAME = "Sunset Alerts"
        const val NOTIF_ID     = 1001

        // Gondar approximate sunset time (varies by season ~6:00–7:00 PM)
        private fun getSunsetMinutesFromNow(): Long {
            val now = Calendar.getInstance()
            val sunset = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 18)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
            }
            val diffMs = sunset.timeInMillis - now.timeInMillis
            return (diffMs / 60_000).coerceAtLeast(0)
        }
    }

    override suspend fun doWork(): Result {
        createNotificationChannel()
        val minutesUntilSunset = getSunsetMinutesFromNow()

        if (minutesUntilSunset in 0..30) {
            showSunsetNotification(minutesUntilSunset)
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about sunset viewing times at Goha Hotel"
            }
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showSunsetNotification(minutesLeft: Long) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = if (minutesLeft <= 5)
            "🌅 Sunset is happening NOW! Head to the terrace immediately!"
        else
            "🌅 Sunset in ~$minutesLeft minutes! Head to the Goha hilltop terrace for a spectacular view over Gondar!"

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Goha Hotel · Sunset Alert")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, notification)
    }
}
