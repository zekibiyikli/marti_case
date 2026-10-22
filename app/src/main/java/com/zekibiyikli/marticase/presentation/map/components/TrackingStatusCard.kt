package com.zekibiyikli.marticase.presentation.map.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zekibiyikli.marticase.R

private val TrackingColor = Color(0xFF2E9E4F)
private val NotTrackingColor = Color(0xFFD64545)

@Composable
fun TrackingStatusCard(isTracking: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = animateColorAsState(if (isTracking) TrackingColor else NotTrackingColor).value,
        contentColor = Color.White,
        shadowElevation = 6.dp
    ) {
        Text(
            text = stringResource(if (isTracking) R.string.tracking else R.string.not_tracking),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}
