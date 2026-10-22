package com.zekibiyikli.marticase.data.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.zekibiyikli.marticase.domain.model.LocationPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import kotlin.coroutines.resume

class GeocoderDataSource(context: Context) {

    private val geocoder = Geocoder(context, Locale.getDefault())

    suspend fun getAddress(point: LocationPoint): String? {
        if (!Geocoder.isPresent()) return null
        return try {
            fetch(point).firstOrNull()?.getAddressLine(0)
        } catch (e: Exception) {
            Timber.e(e, "Reverse geocoding failed")
            null
        }
    }

    private suspend fun fetch(point: LocationPoint): List<Address> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(
                    point.latitude,
                    point.longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            continuation.resume(addresses)
                        }

                        override fun onError(errorMessage: String?) {
                            Timber.e("Reverse geocoding error: %s", errorMessage)
                            continuation.resume(emptyList())
                        }
                    }
                )
            }
        } else {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(point.latitude, point.longitude, 1).orEmpty()
            }
        }
}
