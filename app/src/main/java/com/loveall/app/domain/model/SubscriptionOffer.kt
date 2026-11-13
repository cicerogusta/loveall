package com.loveall.app.domain.model

/** Preço e período vêm da Play; o identificador seleciona uma oferta recém-consultada. */
data class SubscriptionOffer(
    val id: String,
    val productId: String,
    val name: String,
    val price: String,
    val billingPeriod: String,
)
