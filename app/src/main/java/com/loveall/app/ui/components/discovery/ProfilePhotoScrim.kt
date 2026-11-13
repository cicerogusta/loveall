package com.loveall.app.ui.components.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.loveall.app.ui.theme.brandColors

@Composable
internal fun BoxScope.ProfilePhotoScrim() {
    val palette = brandColors
    Box(
        Modifier.matchParentSize()
            .background(
                Brush.verticalGradient(
                    colorStops =
                        arrayOf(
                            0.45f to Color.Transparent,
                            1f to palette.ink.copy(alpha = 0.94f),
                        )
                )
            )
    )
}
