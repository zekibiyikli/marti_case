package com.zekibiyikli.marticase.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import com.google.android.gms.location.LocationServices
import com.zekibiyikli.marticase.domain.model.MapStyle
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.zekibiyikli.marticase.presentation.map.dialogs.DeleteRouteBottomSheet
import com.zekibiyikli.marticase.presentation.battery.rememberBatteryLow
import com.zekibiyikli.marticase.presentation.map.components.LocationWarningCard
import com.zekibiyikli.marticase.R
import com.zekibiyikli.marticase.presentation.map.components.MapControls
import com.zekibiyikli.marticase.presentation.map.dialogs.MarkerDetailsBottomSheet
import com.zekibiyikli.marticase.presentation.map.components.TrackingStatusCard
import com.zekibiyikli.marticase.presentation.permission.rememberLocationEnabledState
import com.zekibiyikli.marticase.service.LocationTrackingService
import com.zekibiyikli.marticase.presentation.permission.rememberLocationPermissionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

private val LocationIstanbul = LatLng(41.0082, 28.9784)
private const val Zoom = 12f
private const val UserZoom = 17f
private const val FollowMinZoom = 15f
private const val MarkerCenterDurationMs = 400

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val permissionState = rememberLocationPermissionState()
    val permissionGranted = permissionState.granted
    val locationEnabledState = rememberLocationEnabledState()
    val locationEnabled = locationEnabledState.enabled
    val batteryLow = rememberBatteryLow()
    var showDeleteSheet by remember { mutableStateOf(false) }
    // The service runs with or without the notification, so start it whatever the user answers.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { LocationTrackingService.start(context) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isTracking = uiState.isTracking
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LocationIstanbul, Zoom)
    }

    suspend fun zoomToUser() {
        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            // Last known fix first so the map is centered on the user right away, then refine with a fresh one.
            client.lastLocation.await()?.let {
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), UserZoom))
            }
            val location = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            if (location != null) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        UserZoom
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Could not get current location")
        }
    }

    // Following starts with tracking and stops as soon as the user moves the map or picks a marker;
    // the location button turns it back on.
    var isFollowing by remember { mutableStateOf(false) }
    var markerJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(isTracking) {
        if (isTracking) isFollowing = true
    }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.cameraMoveStartedReason }.collect { reason ->
            if (reason == CameraMoveStartedReason.GESTURE) isFollowing = false
        }
    }

    // Keep the user centered while following, without overriding a zoom level they picked.
    LaunchedEffect(uiState.currentLocation) {
        if (!isTracking || !isFollowing) return@LaunchedEffect
        val point = uiState.currentLocation ?: return@LaunchedEffect
        val target = LatLng(point.latitude, point.longitude)
        val zoom = cameraPositionState.position.zoom.takeIf { it >= FollowMinZoom } ?: UserZoom
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, zoom))
    }

    LaunchedEffect(permissionGranted, locationEnabled) {
        if (permissionGranted && locationEnabled) zoomToUser()
    }

    // Tracking that was on when the process died is resumed once, now that the app is in the foreground.
    var resumeChecked by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(permissionGranted, locationEnabled) {
        if (resumeChecked || !permissionGranted || !locationEnabled) return@LaunchedEffect
        resumeChecked = true
        if (viewModel.wasTrackingActive()) LocationTrackingService.start(context)
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = permissionGranted,
                mapType = if (uiState.mapStyle == MapStyle.HYBRID) MapType.HYBRID else MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            uiState.markers.forEachIndexed { index, point ->
                Marker(
                    state = rememberUpdatedMarkerState(LatLng(point.latitude, point.longitude)),
                    onClick = {
                        isFollowing = false
                        markerJob?.cancel()
                        // Open the sheet only after the camera arrives, so building it does not stall the animation.
                        markerJob = scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLng(LatLng(point.latitude, point.longitude)),
                                MarkerCenterDurationMs
                            )
                            viewModel.onMarkerClick(index)
                        }
                        true
                    }
                )
            }
        }
        MapControls(
            isTracking = isTracking,
            canStartTracking = permissionGranted && locationEnabled && !batteryLow,
            onToggleTracking = {
                when {
                    isTracking -> LocationTrackingService.stop(context)
                    needsNotificationPermission(context) ->
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else -> LocationTrackingService.start(context)
                }
            },
            onDeleteClick = { showDeleteSheet = true },
            onToggleMapType = viewModel::toggleMapStyle,
            onMyLocationClick = {
                if (permissionGranted && locationEnabled) {
                    isFollowing = true
                    scope.launch { zoomToUser() }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp)
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TrackingStatusCard(isTracking)
            if (!permissionGranted) {
                LocationWarningCard(
                    message = stringResource(
                        if (permissionState.permanentlyDenied) {
                            R.string.location_permission_permanently_denied
                        } else {
                            R.string.location_permission_denied
                        }
                    ),
                    onClick = permissionState.request
                )
            } else if (!locationEnabled) {
                LocationWarningCard(
                    message = stringResource(R.string.location_disabled),
                    onClick = locationEnabledState.request
                )
            }
            if (batteryLow) {
                LocationWarningCard(message = stringResource(R.string.battery_low_card))
            }
        }
    }

    uiState.selectedMarker?.let { details ->
        MarkerDetailsBottomSheet(details = details, onDismiss = viewModel::dismissMarkerDetails)
    }

    if (showDeleteSheet) {
        DeleteRouteBottomSheet(
            onDismiss = { showDeleteSheet = false },
            onConfirm = {
                showDeleteSheet = false
                viewModel.clearRoute()
            }
        )
    }
}

private fun needsNotificationPermission(context: Context) =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
