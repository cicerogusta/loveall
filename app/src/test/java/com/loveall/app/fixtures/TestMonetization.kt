package com.loveall.app.fixtures

import com.loveall.app.domain.model.*

internal object TestMonetization {
    const val PRODUCT = "ad_free_monthly"
    const val OFFER_ID = "ad_free_monthly:monthly"
    const val PURCHASE_TOKEN = "google-purchase-token"
    const val ACCOUNT_ID = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    const val VALID_EXPIRY = "2027-01-01T00:00:00Z"
    const val CONFIG_JSON =
        "{\"enabled\":true,\"product_ids\":[\"ad_free_monthly\"],\"obfuscated_account_id\":\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"}"
    const val ACTIVE_JSON = "{\"ad_free\":true,\"expires_at\":\"2027-01-01T00:00:00Z\"}"
    const val INVALID_ACTIVE_JSON = "{\"ad_free\":true,\"expires_at\":null}"
    const val DISABLED_JSON =
        "{\"enabled\":false,\"product_ids\":[],\"obfuscated_account_id\":null}"
    const val VERIFY_PATH = "/api/billing/verify"
    const val CONFIG_PATH = "/api/billing/config"
    const val ENTITLEMENT_PATH = "/api/billing/entitlements"
    const val PRODUCT_FIELD = "product_id"
    const val TOKEN_FIELD = "purchase_token"
    val offer = SubscriptionOffer(OFFER_ID, PRODUCT, "Sem anúncios", "R$ 12,90", "P1M")
    val owned = StorePurchase(PRODUCT, PURCHASE_TOKEN, false)
    val active = AdFreeEntitlement(true, VALID_EXPIRY)
    val config = BillingConfiguration(true, listOf(PRODUCT), ACCOUNT_ID)
}
