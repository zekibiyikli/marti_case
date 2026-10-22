package com.zekibiyikli.marticase.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zekibiyikli.marticase.domain.model.MapStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

private val MapStyleKey = stringPreferencesKey("map_style")

class SettingsDataStore(private val context: Context) {

    val mapStyle: Flow<MapStyle> = context.dataStore.data.map { it.toMapStyle() }

    suspend fun setMapStyle(style: MapStyle) {
        context.dataStore.edit { it[MapStyleKey] = style.name }
    }

    private fun Preferences.toMapStyle(): MapStyle =
        this[MapStyleKey]?.let { name -> MapStyle.entries.find { it.name == name } } ?: MapStyle.NORMAL
}
