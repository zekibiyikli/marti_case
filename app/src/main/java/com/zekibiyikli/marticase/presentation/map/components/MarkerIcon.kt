package com.zekibiyikli.marticase.presentation.map.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.zekibiyikli.marticase.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

private const val MarkerSizeDp = 40

/** Renders the ic_marker vector into a map marker bitmap. Call only once the map is initialized. */
@Composable
fun rememberMarkerIcon(): BitmapDescriptor {
    val context = LocalContext.current
    return remember(context) { context.vectorToDescriptor(R.drawable.ic_marker, MarkerSizeDp) }
}

private fun Context.vectorToDescriptor(drawableRes: Int, sizeDp: Int): BitmapDescriptor {
    val sizePx = (sizeDp * resources.displayMetrics.density).toInt()
    val drawable = checkNotNull(ContextCompat.getDrawable(this, drawableRes))
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    drawable.setBounds(0, 0, sizePx, sizePx)
    drawable.draw(Canvas(bitmap))
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
