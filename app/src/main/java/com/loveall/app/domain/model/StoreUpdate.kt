package com.loveall.app.domain.model

import com.loveall.app.domain.error.AuthError

data class StoreUpdate(
    val purchases: List<StorePurchase> = emptyList(),
    val cancelled: Boolean = false,
    val restoreRequired: Boolean = false,
    val error: AuthError? = null,
)
