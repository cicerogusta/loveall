package com.loveall.app.ui.components.discovery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.loveall.app.ui.theme.brandColors

@Composable
internal fun ProfilePhotoBackdrop() {
    val palette = brandColors
    Canvas(Modifier.fillMaxSize().clearAndSetSemantics {}) {
        drawCircle(
            palette.rose.copy(alpha = 0.22f),
            radius = size.width * 0.47f,
            center = Offset(size.width * 0.91f, size.height * 0.25f),
        )
        drawCircle(
            palette.peach.copy(alpha = 0.13f),
            radius = size.width * 0.41f,
            center = Offset(size.width * 0.1f, size.height * 0.56f),
        )
        drawCircle(
            palette.onHero.copy(alpha = 0.1f),
            radius = size.width * 0.27f,
            center = center,
        )
    }
}
