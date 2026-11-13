package com.loveall.app.domain.repository

import com.loveall.app.domain.model.*
import kotlinx.coroutines.flow.Flow

/** A interface não expõe Activity, BillingClient ou ProductDetails ao ViewModel. */
interface SubscriptionStore {
    val updates: Flow<StoreUpdate>

    suspend fun offers(productIds: List<String>): List<SubscriptionOffer>

    suspend fun restore(): List<StorePurchase>

    suspend fun purchase(offerId: String, accountId: String)
}
