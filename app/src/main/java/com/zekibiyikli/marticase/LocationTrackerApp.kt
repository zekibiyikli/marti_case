package com.zekibiyikli.marticase

import android.app.Application
import com.zekibiyikli.marticase.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LocationTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LocationTrackerApp)
            modules(appModules)
        }
    }
}
