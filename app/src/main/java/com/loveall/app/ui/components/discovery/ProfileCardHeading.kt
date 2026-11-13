package com.loveall.app.ui.components.discovery

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.loveall.app.R
import com.loveall.app.domain.model.Profile
import com.loveall.app.ui.theme.AppDimensions
import com.loveall.app.ui.theme.AppSpacing
import com.loveall.app.ui.theme.brandColors

@Composable
internal fun BoxScope.ProfileCardHeading(profile: Profile) {
    val palette = brandColors
    Column(
        Modifier.align(Alignment.BottomStart).padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
        ) {
            Text(
                profile.name,
                style = MaterialTheme.typography.headlineLarge,
                color = palette.onHero,
                modifier = Modifier.weight(1f, fill = false),
            )
            profile.age?.let { age ->
                val description = pluralStringResource(R.plurals.age_years, age, age)
                Text(
                    stringResource(R.string.age_value, age),
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.onHero,
                    modifier = Modifier.semantics { contentDescription = description },
                )
            }
        }
        if (profile.city.isNotBlank())
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.tiny),
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    null,
                    Modifier.size(AppDimensions.smallIcon),
                    tint = palette.onHero,
                )
                Text(
                    profile.city,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.onHero,
                )
            }
    }
}
