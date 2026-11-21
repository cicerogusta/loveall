package com.loveall.app.presentation.state

import com.loveall.app.domain.error.AuthError

data class PcdCommunityState(
    val member: Boolean? = null,
    val busy: Boolean = false,
    val error: AuthError? = null,
    val explore: ExploreState = ExploreState(),
)
