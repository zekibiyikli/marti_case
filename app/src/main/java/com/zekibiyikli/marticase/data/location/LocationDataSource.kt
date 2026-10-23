package com.zekibiyikli.marticase.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.zekibiyikli.marticase.domain.model.LocationPoint
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// The distance filter lets the GPS rest while the user stands still; markers are 100 m apart anyway.
private const val UpdateIntervalMs = 2_000L
private const val MinUpdateDistanceMeters = 10f

class LocationDataSource(context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun locationUpdates(): Flow<LocationPoint> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UpdateIntervalMs)
            .setMinUpdateDistanceMeters(MinUpdateDistanceMeters)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { trySend(LocationPoint(it.latitude, it.longitude)) }
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(callback) }
    }
}
