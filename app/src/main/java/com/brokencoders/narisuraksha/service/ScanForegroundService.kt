package com.brokencoders.narisuraksha.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.brokencoders.narisuraksha.NariSurakshaApp
import com.brokencoders.narisuraksha.core.Constants
import com.brokencoders.narisuraksha.data.SosEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ScanForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ScanForegroundService onCreate")

        val notificationHelper = (application as NariSurakshaApp).notificationHelper
        startForeground(Constants.NOTIFICATION_ID_SERVICE, notificationHelper.buildScanServiceNotification())

        acquireWakeLock()
        startScanning()
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NariSuraksha::ScanWakeLock")?.apply {
                acquire(10 * 60 * 1000L) // 10 minutes timeout per cycle
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not acquire WakeLock: ${e.message}")
        }
    }

    private fun startScanning() {
        val app = application as NariSurakshaApp
        val bleTransport = app.bleTransport
        val sosDao = app.database.sosDao()
        val notificationHelper = app.notificationHelper

        bleTransport.startScanning()

        serviceScope.launch {
            bleTransport.received.collectLatest { receivedSos ->
                Log.i(TAG, "Handling received SOS event from sender: ${receivedSos.packet.senderId}")

                // Save to Room Database
                val entity = SosEventEntity(
                    eventType = SosEventEntity.TYPE_RECEIVED,
                    senderId = receivedSos.packet.senderId,
                    timestamp = (receivedSos.packet.timestamp.toLong() * 1000L),
                    lat = if (receivedSos.packet.isLocationUnavailable) null else receivedSos.packet.lat.toDouble(),
                    lon = if (receivedSos.packet.isLocationUnavailable) null else receivedSos.packet.lon.toDouble(),
                    audioPath = null,
                    rssi = receivedSos.rssi,
                    isLocationUnavailable = receivedSos.packet.isLocationUnavailable,
                    createdAt = receivedSos.receivedAt
                )
                sosDao.insertEvent(entity)

                // Show High Priority Heads-up notification
                notificationHelper.showReceivedSosAlert(receivedSos)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SCAN) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ScanForegroundService onDestroy")
        try {
            val app = application as NariSurakshaApp
            app.bleTransport.stopScanning()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping scan in service onDestroy", e)
        }
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "ScanForegroundService"
        const val ACTION_START_SCAN = "com.brokencoders.narisuraksha.START_SCAN"
        const val ACTION_STOP_SCAN = "com.brokencoders.narisuraksha.STOP_SCAN"

        fun start(context: Context) {
            val intent = Intent(context, ScanForegroundService::class.java).apply {
                action = ACTION_START_SCAN
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ScanForegroundService::class.java).apply {
                action = ACTION_STOP_SCAN
            }
            context.startService(intent)
        }
    }
}
