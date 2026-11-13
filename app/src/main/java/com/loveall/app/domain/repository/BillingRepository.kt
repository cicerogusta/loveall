package com.loveall.app.domain.repository

import com.loveall.app.domain.model.*

interface BillingRepository {
    suspend fun configuration(): BillingConfiguration

    suspend fun entitlement(): AdFreeEntitlement

    suspend fun verify(purchase: StorePurchase): AdFreeEntitlement
}
