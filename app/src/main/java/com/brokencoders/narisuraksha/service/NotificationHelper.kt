package com.brokencoders.narisuraksha.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.brokencoders.narisuraksha.MainActivity
import com.brokencoders.narisuraksha.R
import com.brokencoders.narisuraksha.ble.ReceivedSos
import com.brokencoders.narisuraksha.core.Constants

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // High priority emergency alert channel
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val alertChannel = NotificationChannel(
                Constants.CHANNEL_ALERT_ID,
                Constants.CHANNEL_ALERT_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency SOS alerts from nearby people"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 1000)
                setSound(
                    alarmSound,
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            // Low priority discreet foreground service channel
            val serviceChannel = NotificationChannel(
                Constants.CHANNEL_SERVICE_ID,
                Constants.CHANNEL_SERVICE_NAME,
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Background process"
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(alertChannel)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    fun buildScanServiceNotification(): Notification {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Discreet neutral notification title & icon to protect user in Decoy Mode
        return NotificationCompat.Builder(context, Constants.CHANNEL_SERVICE_ID)
            .setContentTitle("Calculator Service")
            .setContentText("Background memory helper active")
            .setSmallIcon(R.drawable.ic_calculator_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    fun buildSosBroadcastingNotification(): Notification {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, Constants.CHANNEL_ALERT_ID)
            .setContentTitle("EMERGENCY BEACON TRANSMITTING")
            .setContentText("Broadcasting distress packet peer-to-peer over BLE")
            .setSmallIcon(R.drawable.ic_calculator_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(0xFFD32F2F.toInt())
            .build()
    }

    fun showReceivedSosAlert(receivedSos: ReceivedSos) {
        val distanceBucket = Constants.getDistanceBucket(receivedSos.rssi)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_ALERT_SCREEN", true)
            putExtra("SENDER_ID", receivedSos.packet.senderId)
            putExtra("LAT", receivedSos.packet.lat)
            putExtra("LON", receivedSos.packet.lon)
            putExtra("RSSI", receivedSos.rssi)
            putExtra("TIMESTAMP", receivedSos.packet.timestamp)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (receivedSos.packet.senderId.toInt() and 0xFFFF),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val locationText = if (!receivedSos.packet.isLocationUnavailable && (receivedSos.packet.lat != 0f || receivedSos.packet.lon != 0f)) {
            "GPS: ${String.format("%.4f", receivedSos.packet.lat)}, ${String.format("%.4f", receivedSos.packet.lon)}"
        } else {
            "Location: GPS unavailable (track via BLE RSSI)"
        }

        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_ALERT_ID)
            .setContentTitle("🚨 EMERGENCY SOS NEARBY (${distanceBucket.approxDistanceText})")
            .setContentText("Distress beacon detected! $locationText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🚨 DISTRESS BEACON DETECTED NEARBY!\n\nDistance: ${distanceBucket.label} (${distanceBucket.approxDistanceText}, RSSI: ${receivedSos.rssi} dBm)\n$locationText\n\nTap immediately to view coordinates or send acknowledgment.")
            )
            .setSmallIcon(R.drawable.ic_calculator_launcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setColor(0xFFD32F2F.toInt())
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setVibrate(longArrayOf(0, 600, 200, 600, 200, 1000))
            .build()

        val notificationId = Constants.NOTIFICATION_ID_ALERT_BASE + (receivedSos.packet.senderId.toInt() and 0x7FF)
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // notification permission fallback
        }
    }
}
