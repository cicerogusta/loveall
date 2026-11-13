package com.loveall.app.platform.billing

import com.android.billingclient.api.ProductDetails
import com.loveall.app.domain.model.SubscriptionOffer

/**
 * Exibe planos-base recorrentes. Não escolhe silenciosamente testes grátis ou ofertas condicionais.
 */
internal object SubscriptionOfferMapper {
    const val SEPARATOR = ":"

    fun standardOffers(details: ProductDetails) =
        details.subscriptionOfferDetails.orEmpty().mapNotNull { offer ->
            val phase =
                offer.pricingPhases.pricingPhaseList.singleOrNull() ?: return@mapNotNull null
            if (
                offer.offerId != null ||
                    phase.priceAmountMicros <= 0 ||
                    phase.recurrenceMode != ProductDetails.RecurrenceMode.INFINITE_RECURRING
            )
                return@mapNotNull null
            SubscriptionOffer(
                details.productId + SEPARATOR + offer.basePlanId,
                details.productId,
                details.name,
                phase.formattedPrice,
                phase.billingPeriod,
            ) to CachedSubscriptionOffer(details, offer.offerToken)
        }
}
