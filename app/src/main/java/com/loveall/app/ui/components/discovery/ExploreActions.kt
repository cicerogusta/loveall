package com.loveall.app.ui.components.discovery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.ui.theme.AppDimensions
import com.loveall.app.ui.theme.AppSpacing

@Composable
internal fun ExploreActions(enabled: Boolean, onPass: () -> Unit, onLike: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Row(
        Modifier.fillMaxWidth()
            .padding(horizontal = AppSpacing.extraLarge, vertical = AppSpacing.compact),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onPass()
            },
            enabled = enabled,
            modifier = Modifier.weight(1f).heightIn(min = AppDimensions.buttonHeight),
            shape = CircleShape,
            colors =
                ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
            border =
                BorderStroke(
                    AppDimensions.border,
                    MaterialTheme.colorScheme.outlineVariant,
                ),
        ) {
            Icon(Icons.Default.Close, null, Modifier.size(AppDimensions.smallIcon))
            Spacer(Modifier.width(AppSpacing.small))
            Text(stringResource(R.string.pass_profile))
        }
        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onLike()
            },
            enabled = enabled,
            modifier = Modifier.weight(1f).heightIn(min = AppDimensions.buttonHeight),
            shape = CircleShape,
        ) {
            Icon(Icons.Default.Favorite, null, Modifier.size(AppDimensions.smallIcon))
            Spacer(Modifier.width(AppSpacing.small))
            Text(stringResource(R.string.like_profile))
        }
    }
}
