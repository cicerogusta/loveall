package com.loveall.app.presentation.state

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.*

data class MonetizationState(
    val configured: Boolean = false,
    val entitlementKnown: Boolean = false,
    val entitlement: AdFreeEntitlement = AdFreeEntitlement(),
    val offers: List<SubscriptionOffer> = emptyList(),
    val busy: Boolean = false,
    val pending: Boolean = false,
    val error: AuthError? = null,
)
