package com.loveall.app.fake

import com.loveall.app.domain.error.*
import com.loveall.app.domain.model.*
import com.loveall.app.domain.repository.BillingRepository
import com.loveall.app.fixtures.TestMonetization
import kotlinx.coroutines.CompletableDeferred

internal class FakeBillingRepository : BillingRepository {
    var current = AdFreeEntitlement()
    var configurationCalls = 0
    var verificationCalls = 0
    var failure: AuthError? = null
    var verifyGate: CompletableDeferred<Unit>? = null

    override suspend fun configuration(): BillingConfiguration {
        configurationCalls++
        failure?.let { throw AuthException(it) }
        return TestMonetization.config
    }

    override suspend fun entitlement(): AdFreeEntitlement {
        failure?.let { throw AuthException(it) }
        return current
    }

    override suspend fun verify(purchase: StorePurchase): AdFreeEntitlement {
        verificationCalls++
        verifyGate?.await()
        failure?.let { throw AuthException(it) }
        return current
    }
}
