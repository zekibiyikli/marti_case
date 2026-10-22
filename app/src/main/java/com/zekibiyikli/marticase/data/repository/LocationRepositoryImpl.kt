package com.zekibiyikli.marticase.data.repository

import com.zekibiyikli.marticase.data.location.LocationDataSource
import com.zekibiyikli.marticase.domain.model.LocationPoint
import com.zekibiyikli.marticase.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

class LocationRepositoryImpl(private val dataSource: LocationDataSource) : LocationRepository {
    override fun locationUpdates(): Flow<LocationPoint> = dataSource.locationUpdates()
}
