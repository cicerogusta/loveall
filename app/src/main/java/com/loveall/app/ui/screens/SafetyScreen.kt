package com.loveall.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.model.BlockedUser
import com.loveall.app.presentation.*
import com.loveall.app.presentation.state.SafetyState
import com.loveall.app.ui.components.*
import com.loveall.app.ui.theme.*

@Composable
fun SafetyScreen(
    state: SafetyState,
    onRefresh: () -> Unit,
    onUnblock: (String) -> Unit,
    onBack: (() -> Unit)? = null,
) {
    var selected by remember { mutableStateOf<BlockedUser?>(null) }
    ListPage(stringResource(R.string.safety), onBack) {
        item {
            ErrorCard(state.error, onRefresh)
            if (state.loading) Loading()
            HeroSurface {
                Icon(Icons.Default.Lock, null, Modifier.size(AppDimensions.touchTarget))
                Text(
                    stringResource(R.string.safety_headline),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    stringResource(R.string.safety_support),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            TextButton(onClick = onRefresh, enabled = !state.loading) {
                Text(stringResource(R.string.refresh))
            }
            SectionTitle(stringResource(R.string.safety_tips))
        }
        items(state.tips) { tip ->
            FeatureTile(Icons.Default.CheckCircle, tip.title, tip.text)
        }
        item {
            Text(
                stringResource(R.string.blocked_users),
                style = MaterialTheme.typography.titleLarge,
            )
            if (!state.loading && state.blocked.isEmpty())
                InfoCard(
                    stringResource(R.string.no_blocked_title),
                    stringResource(R.string.no_blocked),
                )
        }
        items(state.blocked, key = BlockedUser::id) { blocked ->
            Card(
                Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border =
                    BorderStroke(AppDimensions.border, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    Modifier.padding(AppSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                ) {
                    Portrait(null, blocked.name)
                    Column(Modifier.weight(1f)) {
                        Text(blocked.name, style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { selected = blocked }, enabled = !state.busy) {
                            Text(stringResource(R.string.unblock))
                        }
                    }
                }
            }
        }
    }
    selected?.let { user ->
        ConfirmDialog(
            stringResource(R.string.unblock),
            stringResource(R.string.unblock_body, user.name),
            { selected = null },
            {
                selected = null
                onUnblock(user.id)
            },
        )
    }
}
