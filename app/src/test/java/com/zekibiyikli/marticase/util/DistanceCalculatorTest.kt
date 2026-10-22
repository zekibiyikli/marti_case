package com.zekibiyikli.marticase.util

import com.zekibiyikli.marticase.domain.model.LocationPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceCalculatorTest {

    @Test
    fun samePoint_isZero() {
        val point = LocationPoint(41.0082, 28.9784)

        assertEquals(0.0, distanceMeters(point, point), 0.001)
    }

    @Test
    fun oneDegreeOfLatitude_isAbout111Km() {
        val distance = distanceMeters(LocationPoint(0.0, 0.0), LocationPoint(1.0, 0.0))

        assertEquals(111_195.0, distance, 10.0)
    }

    @Test
    fun istanbulToAnkara_isAbout349Km() {
        val istanbul = LocationPoint(41.0082, 28.9784)
        val ankara = LocationPoint(39.9334, 32.8597)

        assertEquals(349_356.0, distanceMeters(istanbul, ankara), 100.0)
    }

    @Test
    fun distanceIsSymmetric() {
        val a = LocationPoint(41.0082, 28.9784)
        val b = LocationPoint(39.9334, 32.8597)

        assertEquals(distanceMeters(a, b), distanceMeters(b, a), 0.001)
    }
}
