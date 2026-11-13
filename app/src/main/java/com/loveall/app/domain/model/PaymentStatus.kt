package com.loveall.app.domain.model

/**
 * Resultado da consulta de uma transação, com status já interpretado como PaymentState. O plano
 * informado acompanha o resultado, mas a assinatura é recarregada separadamente.
 */
data class PaymentStatus(val status: PaymentState, val plan: String?)
