package com.zekibiyikli.marticase

import android.app.Application
import android.content.pm.ApplicationInfo
import com.zekibiyikli.marticase.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class LocationTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) Timber.plant(Timber.DebugTree())
        startKoin {
            androidContext(this@LocationTrackerApp)
            modules(appModules)
        }
    }
}
