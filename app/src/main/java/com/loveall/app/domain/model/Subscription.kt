package com.loveall.app.domain.model

/**
 * Fotografia da assinatura informada pelo backend, incluindo plano e saldo de minutos.
 * paymentStatus é metadado opcional da assinatura; o acompanhamento de uma transação usa
 * PaymentStatus.
 */
data class Subscription(val plan: String, val minutesRemaining: Int, val paymentStatus: String?)
