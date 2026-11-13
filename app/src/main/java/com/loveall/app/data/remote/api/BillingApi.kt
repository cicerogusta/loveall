package com.loveall.app.data.remote.api

import com.loveall.app.data.remote.contract.ApiPaths
import com.loveall.app.data.remote.dto.*
import retrofit2.http.*

interface BillingApi {
    @GET(ApiPaths.BILLING_CONFIG) suspend fun configuration(): BillingConfigurationDto

    @GET(ApiPaths.BILLING_ENTITLEMENTS) suspend fun entitlement(): EntitlementDto

    @POST(ApiPaths.BILLING_VERIFY)
    suspend fun verify(@Body request: VerifyPurchaseRequest): EntitlementDto
}
