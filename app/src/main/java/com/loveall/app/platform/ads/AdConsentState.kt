package com.loveall.app.platform.ads

data class AdConsentState(
    val ready: Boolean = false,
    val privacyOptionsRequired: Boolean = false,
    val failed: Boolean = false,
)
