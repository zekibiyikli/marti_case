package com.zekibiyikli.marticase.data.repository

import com.zekibiyikli.marticase.data.local.dao.MarkerDao
import com.zekibiyikli.marticase.data.mapper.toDomain
import com.zekibiyikli.marticase.data.mapper.toEntity
import com.zekibiyikli.marticase.domain.model.LocationPoint
import com.zekibiyikli.marticase.domain.repository.MarkerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MarkerRepositoryImpl(private val dao: MarkerDao) : MarkerRepository {
    override fun observeMarkers(): Flow<List<LocationPoint>> =
        dao.observeAll().map { markers -> markers.map { it.toDomain() } }

    override suspend fun addMarker(point: LocationPoint) = dao.insert(point.toEntity())

    override suspend fun clearMarkers() = dao.deleteAll()
}
