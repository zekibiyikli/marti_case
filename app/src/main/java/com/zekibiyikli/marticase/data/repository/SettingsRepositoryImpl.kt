package com.zekibiyikli.marticase.data.repository

import com.zekibiyikli.marticase.data.local.datastore.SettingsDataStore
import com.zekibiyikli.marticase.domain.model.MapStyle
import com.zekibiyikli.marticase.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(private val dataStore: SettingsDataStore) : SettingsRepository {
    override fun observeMapStyle(): Flow<MapStyle> = dataStore.mapStyle

    override suspend fun setMapStyle(style: MapStyle) = dataStore.setMapStyle(style)

    override suspend fun isTrackingActive(): Boolean = dataStore.isTrackingActive()

    override suspend fun setTrackingActive(active: Boolean) = dataStore.setTrackingActive(active)
}
