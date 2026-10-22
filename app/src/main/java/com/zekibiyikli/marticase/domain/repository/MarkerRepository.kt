package com.zekibiyikli.marticase.domain.repository

import com.zekibiyikli.marticase.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface MarkerRepository {
    /** Saved markers in the order they were added. */
    fun observeMarkers(): Flow<List<LocationPoint>>
    suspend fun addMarker(point: LocationPoint)
    suspend fun clearMarkers()
}
