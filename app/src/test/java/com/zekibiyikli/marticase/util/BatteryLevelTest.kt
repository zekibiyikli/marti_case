package com.zekibiyikli.marticase.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryLevelTest {

    @Test
    fun belowThresholdAndNotCharging_isLow() {
        assertTrue(isBatteryLow(level = 9, scale = 100, plugged = 0))
    }

    @Test
    fun atThreshold_isNotLow() {
        assertFalse(isBatteryLow(level = 10, scale = 100, plugged = 0))
    }

    @Test
    fun belowThresholdButCharging_isNotLow() {
        assertFalse(isBatteryLow(level = 5, scale = 100, plugged = 1))
    }

    @Test
    fun levelIsComputedFromScale() {
        assertTrue(isBatteryLow(level = 4, scale = 50, plugged = 0))
        assertFalse(isBatteryLow(level = 6, scale = 50, plugged = 0))
    }

    @Test
    fun invalidReadings_areNotLow() {
        assertFalse(isBatteryLow(level = -1, scale = 100, plugged = 0))
        assertFalse(isBatteryLow(level = 5, scale = 0, plugged = 0))
    }
}
