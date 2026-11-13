package com.loveall.app.data.repository

import com.loveall.app.data.apiCall
import com.loveall.app.data.remote.api.PlansApi
import com.loveall.app.data.remote.dto.CallRequest
import com.loveall.app.data.remote.dto.SubscribeRequest
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.model.CallType
import com.loveall.app.domain.model.Checkout
import com.loveall.app.domain.model.PaymentState
import com.loveall.app.domain.model.PaymentStatus
import com.loveall.app.domain.model.Plan
import com.loveall.app.domain.model.Subscription
import com.loveall.app.domain.repository.PlansRepository
import com.loveall.app.domain.validation.Rules

class ApiPlansRepository(private val api: PlansApi, private val externalCheckoutEnabled: Boolean) :
    PlansRepository {
    override suspend fun plans() = apiCall {
        api.plans().plans.map { (id, plan) -> Plan(id, plan.name, plan.price, plan.minutes) }
    }

    override suspend fun subscription() = apiCall {
        api.subscription().subscription.let { Subscription(it.plan, it.minutes, it.paymentStatus) }
    }

    override suspend fun checkout(planId: String): Checkout {
        if (!externalCheckoutEnabled) throw AuthException(AuthError.FORBIDDEN)
        return apiCall {
            api.checkout(SubscribeRequest(planId)).let {
                require(Rules.validPhoto(it.paymentUrl))
                Checkout(it.transactionId, it.paymentUrl)
            }
        }
    }

    override suspend fun paymentStatus(transactionId: String) = apiCall {
        api.paymentStatus(transactionId).let {
            PaymentStatus(PaymentState.fromWire(it.paymentStatus ?: it.status), it.plan)
        }
    }

    override suspend fun checkCall(receiverId: String, type: CallType) = apiCall {
        api.checkCall(CallRequest(receiverId, type.wire))
            .also { if (!it.allowed) throw AuthException(AuthError.FORBIDDEN) }
            .minutes
    }
}
