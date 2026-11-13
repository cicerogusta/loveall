package com.loveall.app.ui.components.discovery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.loveall.app.domain.model.Profile
import com.loveall.app.ui.theme.AppDimensions

@Composable
fun ProfileCard(profile: Profile, onOpen: (() -> Unit)? = null) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(AppDimensions.border, MaterialTheme.colorScheme.outlineVariant),
    ) {
        ProfileCardMedia(profile, detailed = onOpen == null)
        ProfileCardDetails(profile, onOpen)
    }
}
