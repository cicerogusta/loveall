package com.loveall.app.ui.components.discovery

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.ui.theme.AppSpacing
import com.loveall.app.ui.theme.brandColors

@Composable
internal fun BoxScope.ProfilePhotoPlaceholder(name: String) {
    val palette = brandColors
    Column(
        Modifier.align(Alignment.Center).padding(AppSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
    ) {
        Text(
            name.take(1),
            style = MaterialTheme.typography.displayLarge,
            color = palette.onHero,
        )
        Text(
            stringResource(R.string.photo_placeholder),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.onHero,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}
