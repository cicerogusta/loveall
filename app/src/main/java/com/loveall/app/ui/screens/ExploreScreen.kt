package com.loveall.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.model.Profile
import com.loveall.app.presentation.state.ExploreState
import com.loveall.app.ui.components.ErrorCard
import com.loveall.app.ui.components.Loading
import com.loveall.app.ui.components.MenuRow
import com.loveall.app.ui.components.PrimaryButton
import com.loveall.app.ui.components.discovery.ExploreActions
import com.loveall.app.ui.components.discovery.ExploreHeader
import com.loveall.app.ui.components.discovery.ExploreMatchDialog
import com.loveall.app.ui.components.discovery.ExploreProfiles
import com.loveall.app.ui.theme.AppDimensions
import com.loveall.app.ui.theme.AppSpacing

@Composable
fun ExploreScreen(
    state: ExploreState,
    onRefresh: () -> Unit,
    onLike: (Profile) -> Unit,
    onPass: (Profile) -> Unit,
    onOpen: (Profile) -> Unit,
    onPlans: () -> Unit,
    onChat: (String) -> Unit,
    onDismissMatch: () -> Unit,
    incomingLikes: Boolean = false,
    adContent: @Composable () -> Unit = {},
) {
    val profile = state.profiles.firstOrNull()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = AppDimensions.contentWidth).fillMaxSize()) {
            Column(
                Modifier.weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.large),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.medium),
            ) {
                ExploreHeader(incomingLikes, !state.loading && !state.busy, onRefresh)
                ErrorCard(state.error, onRefresh)
                if (state.loading && profile == null) Loading()
                ExploreProfiles(state, incomingLikes, onOpen)
                if (!state.loading && profile == null && state.error == null)
                    PrimaryButton(R.string.refresh, !state.busy, onRefresh)
                Text(
                    stringResource(R.string.free_likes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                adContent()
                MenuRow(
                    Icons.Default.Star,
                    stringResource(R.string.promotion_title),
                    stringResource(R.string.promotion_body),
                    onPlans,
                )
                Spacer(Modifier.height(AppSpacing.small))
            }
            if (profile != null)
                ExploreActions(
                    !state.busy && !state.loading,
                    { onPass(profile) },
                    { onLike(profile) },
                )
        }
    }
    state.matchId?.let { matchId ->
        ExploreMatchDialog(state.matchedName.orEmpty(), onDismissMatch) { onChat(matchId) }
    }
}
