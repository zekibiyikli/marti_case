package com.zekibiyikli.marticase.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.zekibiyikli.marticase.MainActivity
import com.zekibiyikli.marticase.R
import com.zekibiyikli.marticase.util.isBatteryLow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

private const val ChannelId = "tracking"
private const val BatteryChannelId = "battery"
private const val NotificationId = 1
private const val BatteryNotificationId = 2

private const val ActionStart = "com.zekibiyikli.marticase.action.START_TRACKING"
private const val ActionStop = "com.zekibiyikli.marticase.action.STOP_TRACKING"

/** Foreground service that keeps [RouteTracker] running while the app is in the background. */
class LocationTrackingService : Service() {

    private val tracker: RouteTracker by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var batteryReceiverRegistered = false
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val low = isBatteryLow(
                level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
                scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1),
                plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            )
            if (low) pauseForLowBattery()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            ChannelId,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        val batteryChannel = NotificationChannel(
            BatteryChannelId,
            getString(R.string.notification_channel_battery),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        getSystemService(NotificationManager::class.java).apply {
            createNotificationChannel(channel)
            createNotificationChannel(batteryChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ActionStop) {
            tracker.stop()
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            ServiceCompat.startForeground(
                this,
                NotificationId,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } catch (e: RuntimeException) {
            // After a system restart in the background the OS may refuse a location foreground service.
            Timber.e(e, "Could not start the foreground service")
            stopSelf()
            return START_NOT_STICKY
        }
        tracker.start()
        registerBatteryReceiver()
        // The tracker also stops by itself (e.g. location failure): shut the service down with it.
        serviceScope.launch {
            tracker.isTracking.first { !it }
            stopSelf()
        }
        // Ask the system to bring the service back if it kills the process while tracking.
        return START_STICKY
    }

    override fun onDestroy() {
        if (batteryReceiverRegistered) unregisterReceiver(batteryReceiver)
        tracker.stop(forget = false)
        serviceScope.cancel()
        super.onDestroy()
    }

    // The battery broadcast is sticky: registering delivers the current level right away.
    private fun registerBatteryReceiver() {
        if (batteryReceiverRegistered) return
        ContextCompat.registerReceiver(
            this,
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        batteryReceiverRegistered = true
    }

    private fun pauseForLowBattery() {
        tracker.stop()
        val notification = NotificationCompat.Builder(this, BatteryChannelId)
            .setSmallIcon(R.drawable.ic_near_me)
            .setContentTitle(getString(R.string.battery_paused_title))
            .setContentText(getString(R.string.battery_paused_text))
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    2,
                    Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
        getSystemService(NotificationManager::class.java).notify(BatteryNotificationId, notification)
        stopSelf()
    }

    private fun buildNotification() = NotificationCompat.Builder(this, ChannelId)
        .setSmallIcon(R.drawable.ic_near_me)
        .setContentTitle(getString(R.string.notification_title))
        .setContentText(getString(R.string.notification_text))
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .addAction(
            0,
            getString(R.string.notification_stop),
            PendingIntent.getService(
                this,
                1,
                Intent(this, LocationTrackingService::class.java).setAction(ActionStop),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    companion object {
        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, LocationTrackingService::class.java).setAction(ActionStart)
            )
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, LocationTrackingService::class.java).setAction(ActionStop)
            )
        }
    }
}
