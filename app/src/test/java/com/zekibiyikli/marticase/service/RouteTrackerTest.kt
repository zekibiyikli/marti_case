package com.zekibiyikli.marticase.service

import com.zekibiyikli.marticase.domain.model.LocationPoint
import com.zekibiyikli.marticase.domain.repository.LocationRepository
import com.zekibiyikli.marticase.domain.model.MapStyle
import com.zekibiyikli.marticase.domain.repository.MarkerRepository
import com.zekibiyikli.marticase.domain.repository.SettingsRepository
import com.zekibiyikli.marticase.domain.usecase.AddMarkerUseCase
import com.zekibiyikli.marticase.domain.usecase.ClearMarkersUseCase
import com.zekibiyikli.marticase.domain.usecase.ObserveLocationUpdatesUseCase
import com.zekibiyikli.marticase.domain.usecase.ObserveMarkersUseCase
import com.zekibiyikli.marticase.domain.usecase.SetTrackingActiveUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeLocationRepository : LocationRepository {
    val locations = MutableSharedFlow<LocationPoint>()
    override fun locationUpdates(): Flow<LocationPoint> = locations
}

private class FakeMarkerRepository : MarkerRepository {
    val markers = MutableStateFlow<List<LocationPoint>>(emptyList())
    override fun observeMarkers(): Flow<List<LocationPoint>> = markers
    override suspend fun addMarker(point: LocationPoint) {
        markers.value = markers.value + point
    }

    override suspend fun clearMarkers() {
        markers.value = emptyList()
    }
}

private class FakeSettingsRepository : SettingsRepository {
    var trackingActive = false
    override fun observeMapStyle(): Flow<MapStyle> = MutableStateFlow(MapStyle.NORMAL)
    override suspend fun setMapStyle(style: MapStyle) = Unit
    override suspend fun isTrackingActive(): Boolean = trackingActive
    override suspend fun setTrackingActive(active: Boolean) {
        trackingActive = active
    }
}

// Latitude offsets from the start point, in meters: 0.0005 deg is ~56 m, 0.00091 deg is ~101 m.
private const val START_LAT = 41.0
private const val LON = 29.0
private const val NEAR = 0.0005
private const val FAR = 0.00091

private fun point(latOffset: Double) = LocationPoint(START_LAT + latOffset, LON)

@OptIn(ExperimentalCoroutinesApi::class)
class RouteTrackerTest {

    private val locationRepository = FakeLocationRepository()
    private val markerRepository = FakeMarkerRepository()
    private val settingsRepository = FakeSettingsRepository()

    private fun TestScope.createTracker() = RouteTracker(
        ObserveLocationUpdatesUseCase(locationRepository),
        ObserveMarkersUseCase(markerRepository),
        AddMarkerUseCase(markerRepository),
        ClearMarkersUseCase(markerRepository),
        SetTrackingActiveUseCase(settingsRepository),
        scope = backgroundScope
    )

    @Test
    fun firstLocation_isOnlyAReference_noMarker() = runTest(UnconfinedTestDispatcher()) {
        val tracker = createTracker()
        tracker.start()

        locationRepository.locations.emit(point(0.0))

        assertTrue(markerRepository.markers.value.isEmpty())
        assertEquals(point(0.0), tracker.currentLocation.value)
    }

    @Test
    fun locationCloserThan100m_addsNoMarker() = runTest(UnconfinedTestDispatcher()) {
        val tracker = createTracker()
        tracker.start()

        locationRepository.locations.emit(point(0.0))
        locationRepository.locations.emit(point(NEAR))

        assertTrue(markerRepository.markers.value.isEmpty())
    }

    @Test
    fun locationFartherThan100m_addsMarker() = runTest(UnconfinedTestDispatcher()) {
        val tracker = createTracker()
        tracker.start()

        locationRepository.locations.emit(point(0.0))
        locationRepository.locations.emit(point(FAR))

        assertEquals(listOf(point(FAR)), markerRepository.markers.value)
    }

    @Test
    fun nextMarker_isMeasuredFromTheLastMarker() = runTest(UnconfinedTestDispatcher()) {
        val tracker = createTracker()
        tracker.start()
        locationRepository.locations.emit(point(0.0))
        locationRepository.locations.emit(point(FAR))

        // ~56 m past the first marker: too close, even though it is ~157 m from the start.
        locationRepository.locations.emit(point(FAR + NEAR))
        assertEquals(1, markerRepository.markers.value.size)

        locationRepository.locations.emit(point(FAR * 2))
        assertEquals(listOf(point(FAR), point(FAR * 2)), markerRepository.markers.value)
    }

    @Test
    fun clearRoute_removesMarkers_andDoesNotDropANewOneRightAway() = runTest(UnconfinedTestDispatcher()) {
        val tracker = createTracker()
        tracker.start()
        locationRepository.locations.emit(point(0.0))
        locationRepository.locations.emit(point(FAR))

        tracker.clearRoute()
        assertTrue(markerRepository.markers.value.isEmpty())

        // Still next to the place where the route was cleared.
        locationRepository.locations.emit(point(FAR + NEAR))
        assertTrue(markerRepository.markers.value.isEmpty())

        locationRepository.locations.emit(point(FAR * 2))
        assertEquals(1, markerRepository.markers.value.size)
    }

    @Test
    fun startAndStop_updateTrackingState() = runTest(UnconfinedTestDispatcher()) {
        val tracker = createTracker()
        assertFalse(tracker.isTracking.value)

        tracker.start()
        assertTrue(tracker.isTracking.value)

        tracker.stop()
        assertFalse(tracker.isTracking.value)
    }

    @Test
    fun start_remembersThatTrackingIsActive_andStopForgetsIt() = runTest(UnconfinedTestDispatcher()) {
        val tracker = createTracker()

        tracker.start()
        assertTrue(settingsRepository.trackingActive)

        tracker.stop()
        assertFalse(settingsRepository.trackingActive)
    }

    @Test
    fun stopWithoutForget_keepsTrackingMarkedActiveForResume() = runTest(UnconfinedTestDispatcher()) {
        val tracker = createTracker()
        tracker.start()

        tracker.stop(forget = false)

        assertFalse(tracker.isTracking.value)
        assertTrue(settingsRepository.trackingActive)
    }
}
