package com.loveall.app.ui.components.discovery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.ui.theme.AppDimensions
import com.loveall.app.ui.theme.AppSpacing

@Composable
internal fun BoxScope.ProfileIdentityBadge(verified: Boolean) {
    Surface(
        Modifier.align(Alignment.TopStart)
            .padding(AppSpacing.medium)
            .padding(top = AppSpacing.small),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            Modifier.padding(horizontal = AppSpacing.compact, vertical = AppSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.tiny),
        ) {
            Icon(
                if (verified) Icons.Default.CheckCircle else Icons.Default.FavoriteBorder,
                null,
                Modifier.size(AppDimensions.smallIcon),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                stringResource(if (verified) R.string.verified else R.string.discover_badge),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
