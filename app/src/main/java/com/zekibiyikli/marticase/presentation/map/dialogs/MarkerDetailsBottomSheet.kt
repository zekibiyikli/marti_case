package com.zekibiyikli.marticase.presentation.map.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zekibiyikli.marticase.R
import com.zekibiyikli.marticase.presentation.map.MarkerDetails
import com.zekibiyikli.marticase.presentation.map.components.HideNavigationBarEffect
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkerDetailsBottomSheet(
    details: MarkerDetails,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        HideNavigationBarEffect()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(R.string.marker_title, details.number),
                style = MaterialTheme.typography.headlineSmall
            )
            DetailRow(
                label = stringResource(R.string.marker_address),
                value = when {
                    details.isLoadingAddress -> stringResource(R.string.marker_address_loading)
                    details.address != null -> details.address
                    else -> stringResource(R.string.marker_address_unavailable)
                }
            )
            DetailRow(
                label = stringResource(R.string.marker_location),
                value = String.format(Locale.US, "%.6f, %.6f", details.point.latitude, details.point.longitude)
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
