package com.zekibiyikli.marticase.service

import com.zekibiyikli.marticase.domain.model.LocationPoint
import com.zekibiyikli.marticase.domain.usecase.AddMarkerUseCase
import com.zekibiyikli.marticase.domain.usecase.ClearMarkersUseCase
import com.zekibiyikli.marticase.domain.usecase.ObserveLocationUpdatesUseCase
import com.zekibiyikli.marticase.domain.usecase.ObserveMarkersUseCase
import com.zekibiyikli.marticase.util.distanceMeters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber

private const val MarkerDistanceMeters = 100.0

/**
 * App-wide owner of route tracking, so it outlives the UI: [LocationTrackingService] keeps it running
 * in the background while the screen only observes its state.
 */
class RouteTracker(
    private val observeLocationUpdates: ObserveLocationUpdatesUseCase,
    observeMarkers: ObserveMarkersUseCase,
    private val addMarker: AddMarkerUseCase,
    private val clearMarkers: ClearMarkersUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationPoint?>(null)
    val currentLocation: StateFlow<LocationPoint?> = _currentLocation.asStateFlow()

    private var trackingJob: Job? = null

    // Reference for the next marker while the route has none: the first location after tracking starts
    // or the location where the route was cleared. Markers are measured from it, never dropped on it.
    @Volatile
    private var anchor: LocationPoint? = null

    // The latest marker, updated synchronously so two quick locations cannot both pass the distance check
    // before the database emits.
    @Volatile
    private var lastMarker: LocationPoint? = null

    init {
        scope.launch {
            observeMarkers().collect { markers -> lastMarker = markers.lastOrNull() }
        }
    }

    fun start() {
        if (trackingJob != null) return
        anchor = null
        _isTracking.value = true
        trackingJob = scope.launch {
            observeLocationUpdates()
                .catch { e -> Timber.e(e, "Location tracking failed") }
                .collect(::onLocation)
        }.also { job ->
            job.invokeOnCompletion {
                trackingJob = null
                _isTracking.value = false
            }
        }
    }

    fun stop() {
        trackingJob?.cancel()
    }

    fun clearRoute() {
        anchor = _currentLocation.value
        lastMarker = null
        scope.launch { clearMarkers() }
    }

    private fun onLocation(point: LocationPoint) {
        val reference = lastMarker ?: anchor
        if (reference == null) anchor = point
        val shouldAddMarker = reference != null && distanceMeters(reference, point) >= MarkerDistanceMeters
        Timber.d("Location %s, distance from last marker: %s m, marker added: %s",
            point, reference?.let { distanceMeters(it, point).toInt() }, shouldAddMarker)
        if (shouldAddMarker) {
            lastMarker = point
            scope.launch { addMarker(point) }
        }
        _currentLocation.value = point
    }
}
