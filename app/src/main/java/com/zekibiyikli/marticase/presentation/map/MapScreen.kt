package com.zekibiyikli.marticase.presentation.map

import android.annotation.SuppressLint
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.zekibiyikli.marticase.presentation.map.components.DeleteRouteBottomSheet
import com.zekibiyikli.marticase.presentation.map.components.LocationWarningCard
import com.zekibiyikli.marticase.R
import com.zekibiyikli.marticase.presentation.map.components.MapControls
import com.zekibiyikli.marticase.presentation.map.components.MarkerDetailsBottomSheet
import com.zekibiyikli.marticase.presentation.map.components.TrackingStatusCard
import com.zekibiyikli.marticase.presentation.map.components.rememberMarkerIcon
import com.zekibiyikli.marticase.presentation.permission.rememberLocationEnabledState
import com.zekibiyikli.marticase.presentation.permission.rememberLocationPermissionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

private val Istanbul = LatLng(41.0082, 28.9784)
private const val UserZoom = 17f
private const val FollowMinZoom = 15f

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
    var showDeleteSheet by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isTracking = uiState.isTracking
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(Istanbul, 12f)
    }

    suspend fun zoomToUser() {
        try {
            val location = LocationServices.getFusedLocationProviderClient(context)
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .await()
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

    // Follow the user while tracking, without overriding a zoom level they picked.
    LaunchedEffect(uiState.currentLocation) {
        val point = uiState.currentLocation ?: return@LaunchedEffect
        val target = LatLng(point.latitude, point.longitude)
        val zoom = cameraPositionState.position.zoom.takeIf { it >= FollowMinZoom } ?: UserZoom
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, zoom))
    }

    LaunchedEffect(permissionGranted, locationEnabled) {
        if (permissionGranted && locationEnabled) zoomToUser()
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = permissionGranted,
                mapType = mapType
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            val markerIcon = rememberMarkerIcon()
            uiState.markers.forEachIndexed { index, point ->
                Marker(
                    state = rememberUpdatedMarkerState(LatLng(point.latitude, point.longitude)),
                    icon = markerIcon,
                    anchor = Offset(0.5f, 1f),
                    onClick = {
                        viewModel.onMarkerClick(index)
                        true
                    }
                )
            }
        }
        MapControls(
            isTracking = isTracking,
            onToggleTracking = {
                when {
                    !permissionGranted -> permissionState.request()
                    !locationEnabled -> locationEnabledState.request()
                    else -> viewModel.toggleTracking()
                }
            },
            onDeleteClick = { showDeleteSheet = true },
            onToggleMapType = {
                mapType = if (mapType == MapType.NORMAL) MapType.HYBRID else MapType.NORMAL
            },
            onMyLocationClick = { if (permissionGranted && locationEnabled) scope.launch { zoomToUser() } },
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
