package com.zekibiyikli.marticase.di

import com.zekibiyikli.marticase.service.RouteTracker
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::RouteTracker)
}

val appModules = listOf(appModule, dataModule, domainModule, presentationModule)
