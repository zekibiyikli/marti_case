package com.zekibiyikli.marticase.domain.repository

import com.zekibiyikli.marticase.domain.model.LocationPoint

interface GeocodingRepository {
    /** Street address of [point], or null when it cannot be resolved. */
    suspend fun getAddress(point: LocationPoint): String?
}
