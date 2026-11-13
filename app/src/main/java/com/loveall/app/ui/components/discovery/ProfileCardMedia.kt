package com.loveall.app.ui.components.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.loveall.app.domain.model.Profile
import com.loveall.app.domain.validation.Rules
import com.loveall.app.ui.theme.AppDimensions
import com.loveall.app.ui.theme.brandColors

@Composable
internal fun ProfileCardMedia(profile: Profile, detailed: Boolean) {
    val palette = brandColors
    val photos = remember(profile.photos) { profile.photos.filter(Rules::validProfilePhoto) }
    Box(
        Modifier.fillMaxWidth()
            .aspectRatio(
                if (detailed) AppDimensions.profileAspectRatio
                else AppDimensions.discoveryPhotoAspectRatio
            )
            .background(Brush.linearGradient(listOf(palette.heroStart, palette.heroEnd)))
    ) {
        ProfilePhotoBackdrop()
        if (photos.isEmpty()) ProfilePhotoPlaceholder(profile.name)
        else ProfilePhotoGallery(photos, profile.name)
        ProfilePhotoScrim()
        ProfileIdentityBadge(profile.verified)
        ProfileCardHeading(profile)
    }
}
