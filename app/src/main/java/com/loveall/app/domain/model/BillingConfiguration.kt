package com.loveall.app.domain.model

data class BillingConfiguration(
    val enabled: Boolean = false,
    val productIds: List<String> = emptyList(),
    val accountId: String? = null,
)
