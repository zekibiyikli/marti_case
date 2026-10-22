package com.zekibiyikli.marticase.util

import com.zekibiyikli.marticase.domain.model.LocationPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val EarthRadiusMeters = 6_371_000.0

/** Great-circle (haversine) distance between two points in meters. */
fun distanceMeters(from: LocationPoint, to: LocationPoint): Double {
    val lat1 = Math.toRadians(from.latitude)
    val lat2 = Math.toRadians(to.latitude)
    val dLat = lat2 - lat1
    val dLon = Math.toRadians(to.longitude - from.longitude)
    val a = sin(dLat / 2) * sin(dLat / 2) + cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
    return EarthRadiusMeters * 2 * atan2(sqrt(a), sqrt(1 - a))
}
