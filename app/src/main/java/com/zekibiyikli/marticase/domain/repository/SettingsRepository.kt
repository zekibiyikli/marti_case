package com.zekibiyikli.marticase.domain.repository

import com.zekibiyikli.marticase.domain.model.MapStyle
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeMapStyle(): Flow<MapStyle>
    suspend fun setMapStyle(style: MapStyle)
}
