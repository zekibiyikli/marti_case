package com.zekibiyikli.marticase.util

/** Tracking is paused below this charge level (percent) unless the device is charging. */
const val LowBatteryPercent = 10

/**
 * Whether the battery is low enough to pause tracking. [level] and [scale] come from the battery
 * broadcast extras; [plugged] is non-zero while charging.
 */
fun isBatteryLow(level: Int, scale: Int, plugged: Int, thresholdPercent: Int = LowBatteryPercent): Boolean {
    if (level < 0 || scale <= 0) return false
    return level * 100 / scale < thresholdPercent && plugged == 0
}
