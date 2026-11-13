package com.loveall.app.ui.components.discovery

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.loveall.app.R
import com.loveall.app.domain.model.Gender
import com.loveall.app.domain.model.Profile
import com.loveall.app.ui.components.Tag
import com.loveall.app.ui.components.label
import com.loveall.app.ui.theme.AppSpacing

@Composable
internal fun ProfileCardDetails(profile: Profile, onOpen: (() -> Unit)?) {
    Column(
        Modifier.padding(AppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
    ) {
        val gender = Gender.entries.firstOrNull { it.wire == profile.gender }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.compact),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
        ) {
            if (gender != null)
                Tag(
                    if (gender == Gender.OTHER && profile.genderCustom.isNotBlank())
                        profile.genderCustom
                    else stringResource(gender.label())
                )
            if (onOpen != null)
                TextButton(
                    onClick = onOpen,
                    contentPadding = PaddingValues(horizontal = AppSpacing.tiny),
                ) {
                    Text(stringResource(R.string.view_profile))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                }
        }
        if (profile.bio.isNotBlank()) {
            Text(
                stringResource(R.string.about_profile),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                profile.bio,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = if (onOpen != null) 2 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
