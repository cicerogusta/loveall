package com.loveall.app.ui.components.discovery

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.model.Profile
import com.loveall.app.presentation.state.ExploreState
import com.loveall.app.ui.components.EmptyIllustration
import com.loveall.app.ui.components.label

@Composable
internal fun ExploreProfiles(
    state: ExploreState,
    incomingLikes: Boolean,
    onOpen: (Profile) -> Unit,
) {
    val profile = state.profiles.firstOrNull()
    AnimatedContent(
        targetState = profile,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = PROFILE_TRANSITION_LABEL,
    ) { visible ->
        if (visible != null) ProfileCard(visible) { onOpen(visible) }
        else if (!state.loading && state.error == null)
            EmptyIllustration(
                stringResource(
                    if (incomingLikes) R.string.empty_likes_title else R.string.empty_discover_title
                ),
                stringResource(
                    if (incomingLikes) R.string.empty_likes else R.string.empty_discover
                ),
            )
    }
}

private const val PROFILE_TRANSITION_LABEL = "profileCard"
