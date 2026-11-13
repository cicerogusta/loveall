package com.loveall.app.fake

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.model.CallType
import com.loveall.app.domain.model.Checkout
import com.loveall.app.domain.model.PaymentState
import com.loveall.app.domain.model.PaymentStatus
import com.loveall.app.domain.model.Plan
import com.loveall.app.domain.model.Subscription
import com.loveall.app.domain.repository.PlansRepository
import com.loveall.app.fixtures.TestPlans
import kotlinx.coroutines.test.*
import org.junit.Assert.*

internal class FakePlansRepository : PlansRepository {
    var checkoutCalls = 0
    var error: AuthError? = null
    var status = PaymentState.PENDING
    var current = Subscription(TestPlans.FREE_ID, 0, null)

    override suspend fun plans() = listOf(Plan(TestPlans.BASIC_ID, TestPlans.BASIC_NAME, 9.9, 60))

    override suspend fun subscription() = current

    override suspend fun checkout(planId: String): Checkout {
        checkoutCalls++
        error?.let { throw AuthException(it) }
        return Checkout(TestPlans.TRANSACTION_ID, TestPlans.CHECKOUT_URL)
    }

    override suspend fun paymentStatus(transactionId: String): PaymentStatus {
        error?.let { throw AuthException(it) }
        return PaymentStatus(status, TestPlans.BASIC_ID)
    }

    override suspend fun checkCall(receiverId: String, type: CallType): Int {
        throw AuthException(AuthError.FORBIDDEN)
    }
}
