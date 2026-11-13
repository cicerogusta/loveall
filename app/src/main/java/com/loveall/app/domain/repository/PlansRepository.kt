package com.loveall.app.domain.repository

import com.loveall.app.domain.model.CallType
import com.loveall.app.domain.model.Checkout
import com.loveall.app.domain.model.PaymentStatus
import com.loveall.app.domain.model.Plan
import com.loveall.app.domain.model.Subscription

/**
 * Contrato de catálogo de planos, saldo, checkout, pagamento e elegibilidade de chamadas. Os tipos
 * do domínio impedem que decisões da apresentação dependam dos DTOs HTTP.
 */
interface PlansRepository {
    suspend fun plans(): List<Plan>

    suspend fun subscription(): Subscription

    suspend fun checkout(planId: String): Checkout

    suspend fun paymentStatus(transactionId: String): PaymentStatus

    suspend fun checkCall(receiverId: String, type: CallType): Int
}
