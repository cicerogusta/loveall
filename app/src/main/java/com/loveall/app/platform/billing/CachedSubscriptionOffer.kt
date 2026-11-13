package com.loveall.app.platform.billing

import com.android.billingclient.api.ProductDetails

internal data class CachedSubscriptionOffer(val details: ProductDetails, val token: String)
