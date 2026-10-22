package com.zekibiyikli.marticase.di

import com.zekibiyikli.marticase.data.location.GeocoderDataSource
import com.zekibiyikli.marticase.data.location.LocationDataSource
import com.zekibiyikli.marticase.data.repository.GeocodingRepositoryImpl
import com.zekibiyikli.marticase.data.repository.LocationRepositoryImpl
import com.zekibiyikli.marticase.domain.repository.GeocodingRepository
import com.zekibiyikli.marticase.domain.repository.LocationRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single { LocationDataSource(androidContext()) }
    singleOf(::LocationRepositoryImpl) bind LocationRepository::class
    single { GeocoderDataSource(androidContext()) }
    singleOf(::GeocodingRepositoryImpl) bind GeocodingRepository::class
}
