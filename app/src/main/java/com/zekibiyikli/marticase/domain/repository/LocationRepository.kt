package com.zekibiyikli.marticase.domain.repository

import com.zekibiyikli.marticase.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    /** Emits the user's location while collected. Requires the location permission. */
    fun locationUpdates(): Flow<LocationPoint>
}
