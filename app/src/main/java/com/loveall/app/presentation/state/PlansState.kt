package com.loveall.app.presentation.state

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.Checkout
import com.loveall.app.domain.model.PaymentStatus
import com.loveall.app.domain.model.Plan
import com.loveall.app.domain.model.Subscription

/**
 * Dados das ofertas, da assinatura e do acompanhamento de pagamento.
 *
 * @property subscription saldo recebido da API; não é concedido a partir do checkout local.
 * @property checkout solicitação de abrir o navegador, consumida depois da tentativa de abertura.
 * @property pendingTransaction identificador que continua disponível após abrir o navegador.
 * @property payment último resultado consultado para a transação pendente.
 * @property startingCheckout criação de uma transação em andamento.
 * @property checkingPayment consulta da transação e da assinatura em andamento.
 */
data class PlansState(
    val plans: List<Plan> = emptyList(),
    val subscription: Subscription? = null,
    val checkout: Checkout? = null,
    val payment: PaymentStatus? = null,
    val pendingTransaction: String? = null,
    val loading: Boolean = false,
    val startingCheckout: Boolean = false,
    val checkingPayment: Boolean = false,
    val error: AuthError? = null,
) {
    val busy: Boolean
        get() = startingCheckout || checkingPayment
}
