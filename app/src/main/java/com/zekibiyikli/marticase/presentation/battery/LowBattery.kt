package com.zekibiyikli.marticase.presentation.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.zekibiyikli.marticase.util.isBatteryLow

private fun Intent?.isLow(): Boolean {
    if (this == null) return false
    return isBatteryLow(
        level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
        scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1),
        plugged = getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
    )
}

/** Whether the battery is low and the device is not charging; updates live while the screen is shown. */
@Composable
fun rememberBatteryLow(): Boolean {
    val context = LocalContext.current
    val filter = remember { IntentFilter(Intent.ACTION_BATTERY_CHANGED) }
    // The battery broadcast is sticky: a null receiver just returns the current state.
    var low by remember { mutableStateOf(context.registerReceiver(null, filter).isLow()) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                low = intent.isLow()
            }
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }
    return low
}
