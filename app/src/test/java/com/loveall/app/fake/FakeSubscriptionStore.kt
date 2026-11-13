package com.loveall.app.fake

import com.loveall.app.domain.model.*
import com.loveall.app.domain.repository.SubscriptionStore
import com.loveall.app.fixtures.TestMonetization
import kotlinx.coroutines.flow.MutableSharedFlow

internal class FakeSubscriptionStore : SubscriptionStore {
    override val updates = MutableSharedFlow<StoreUpdate>(extraBufferCapacity = 10)
    var owned = emptyList<StorePurchase>()
    var restoreCalls = 0
    var selected: String? = null
    var account: String? = null

    override suspend fun offers(productIds: List<String>) = listOf(TestMonetization.offer)

    override suspend fun restore(): List<StorePurchase> {
        restoreCalls++
        return owned
    }

    override suspend fun purchase(offerId: String, accountId: String) {
        selected = offerId
        account = accountId
    }
}
