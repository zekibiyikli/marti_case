package com.zekibiyikli.marticase.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zekibiyikli.marticase.data.local.dao.MarkerDao
import com.zekibiyikli.marticase.data.local.entity.MarkerEntity

@Database(entities = [MarkerEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun markerDao(): MarkerDao
}
