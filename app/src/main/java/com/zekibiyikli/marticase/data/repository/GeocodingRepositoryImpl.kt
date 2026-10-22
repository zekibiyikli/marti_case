package com.zekibiyikli.marticase.data.repository

import com.zekibiyikli.marticase.data.location.GeocoderDataSource
import com.zekibiyikli.marticase.domain.model.LocationPoint
import com.zekibiyikli.marticase.domain.repository.GeocodingRepository

class GeocodingRepositoryImpl(private val dataSource: GeocoderDataSource) : GeocodingRepository {
    override suspend fun getAddress(point: LocationPoint): String? = dataSource.getAddress(point)
}
