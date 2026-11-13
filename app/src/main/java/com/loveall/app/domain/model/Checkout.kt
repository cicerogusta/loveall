package com.loveall.app.domain.model

/**
 * Resultado da criação de uma transação: identificador para consulta e endereço para abertura
 * externa. Esse resultado não comprova pagamento ou concessão de minutos.
 */
data class Checkout(val transactionId: String, val url: String)
