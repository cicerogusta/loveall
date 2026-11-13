package com.loveall.app.ui.monetization

import androidx.compose.runtime.staticCompositionLocalOf
import com.loveall.app.domain.model.SubscriptionOffer
import com.loveall.app.platform.ads.AdConsentController
import com.loveall.app.presentation.state.MonetizationState

data class MonetizationUi(
    val state: MonetizationState = MonetizationState(),
    val refresh: () -> Unit = {},
    val restore: () -> Unit = {},
    val purchase: (SubscriptionOffer) -> Unit = {},
)

val LocalMonetization = staticCompositionLocalOf { MonetizationUi() }
val LocalAdConsent = staticCompositionLocalOf<AdConsentController?> { null }
