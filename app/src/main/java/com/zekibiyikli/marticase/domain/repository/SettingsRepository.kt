package com.zekibiyikli.marticase.domain.repository

import com.zekibiyikli.marticase.domain.model.MapStyle
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeMapStyle(): Flow<MapStyle>
    suspend fun setMapStyle(style: MapStyle)

    /** Whether tracking was on when the app last ran, so it can be resumed after the process was killed. */
    suspend fun isTrackingActive(): Boolean
    suspend fun setTrackingActive(active: Boolean)
}
