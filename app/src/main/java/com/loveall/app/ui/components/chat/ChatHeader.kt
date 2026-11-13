package com.loveall.app.ui.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.presentation.state.ChatState
import com.loveall.app.ui.components.Portrait
import com.loveall.app.ui.theme.AppDimensions
import com.loveall.app.ui.theme.AppSpacing

@Composable
internal fun ChatHeader(
    state: ChatState,
    onBack: () -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit,
    onUnmatch: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier.padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.compact),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
            ) {
                FilledTonalIconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                }
                state.partner?.let {
                    Portrait(
                        it.photos.firstOrNull(),
                        it.name,
                        size = AppDimensions.touchTarget,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        state.partner?.name ?: stringResource(R.string.chat),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        stringResource(R.string.brand_wordmark),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                ChatActionMenu(
                    state = state,
                    onReport = onReport,
                    onBlock = onBlock,
                    onUnmatch = onUnmatch,
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
            ) {
                Icon(
                    Icons.Default.Lock,
                    null,
                    Modifier.size(AppDimensions.smallIcon),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    stringResource(R.string.chat_respect),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
