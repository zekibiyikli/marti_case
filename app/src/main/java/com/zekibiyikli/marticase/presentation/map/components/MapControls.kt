package com.zekibiyikli.marticase.presentation.map.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zekibiyikli.marticase.R

@Composable
fun MapControls(
    isTracking: Boolean,
    onToggleTracking: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleMapType: () -> Unit,
    onMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ControlPill {
            ControlButton(R.drawable.ic_map, R.string.cd_change_map_type, onToggleMapType)
            ControlButton(R.drawable.ic_near_me, R.string.cd_my_location, onMyLocationClick)
        }
        ControlPill {
            ControlButton(R.drawable.ic_delete, R.string.cd_delete, onDeleteClick)
            ControlButton(
                icon = if (isTracking) R.drawable.ic_pause else R.drawable.ic_play,
                contentDescription = if (isTracking) R.string.cd_pause_tracking else R.string.cd_start_tracking,
                onClick = onToggleTracking
            )
        }
    }
}

@Composable
private fun ControlPill(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .background(Color(0xB3465066), RoundedCornerShape(28.dp))
            .padding(vertical = 8.dp)
    ) {
        content()
    }
}

@Composable
private fun ControlButton(
    @DrawableRes icon: Int,
    @StringRes contentDescription: Int,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(icon),
            contentDescription = stringResource(contentDescription),
            tint = Color.White
        )
    }
}
