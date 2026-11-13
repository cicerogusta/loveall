package com.loveall.app.ui.components.plans

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.ui.components.HeroSurface
import com.loveall.app.ui.theme.AppSpacing

@Composable
internal fun PlansHero() {
    HeroSurface {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
        ) {
            Icon(Icons.Default.Star, null)
            Text(
                stringResource(R.string.premium_kicker),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Text(
            stringResource(R.string.premium_headline),
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            stringResource(R.string.premium_support),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
