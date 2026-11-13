package com.loveall.app.ui.components.discovery

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.ui.components.BrandWordmark
import com.loveall.app.ui.theme.AppSpacing

@Composable
internal fun ExploreHeader(incomingLikes: Boolean, enabled: Boolean, onRefresh: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = AppSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BrandWordmark()
        FilledTonalIconButton(
            onClick = onRefresh,
            enabled = enabled,
            colors =
                IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
        ) {
            Icon(Icons.Default.Refresh, stringResource(R.string.refresh))
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.tiny)) {
        Text(
            stringResource(
                if (incomingLikes) R.string.likes_headline else R.string.discover_headline
            ),
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            stringResource(
                if (incomingLikes) R.string.likes_support else R.string.discover_support
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
