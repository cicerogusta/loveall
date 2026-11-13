package com.loveall.app.domain.validation

object AdEligibility {
    /** Na dúvida sobre assinatura ou compra pendente, não exibe anúncios. */
    fun canDisplay(
        entitlementKnown: Boolean,
        adFree: Boolean,
        pending: Boolean,
        checking: Boolean = false,
    ) = entitlementKnown && !adFree && !pending && !checking
}
