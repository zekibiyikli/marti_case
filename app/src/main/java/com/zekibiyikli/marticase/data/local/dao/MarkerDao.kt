package com.zekibiyikli.marticase.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zekibiyikli.marticase.data.local.entity.MarkerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkerDao {
    @Query("SELECT * FROM markers ORDER BY id")
    fun observeAll(): Flow<List<MarkerEntity>>

    @Insert
    suspend fun insert(marker: MarkerEntity)

    @Query("DELETE FROM markers")
    suspend fun deleteAll()
}
