package com.loveall.app.data.remote.api

import com.loveall.app.data.remote.contract.*
import com.loveall.app.data.remote.dto.CallRequest
import com.loveall.app.data.remote.dto.CallResult
import com.loveall.app.data.remote.dto.CheckoutDto
import com.loveall.app.data.remote.dto.PaymentStatusDto
import com.loveall.app.data.remote.dto.PlansResponse
import com.loveall.app.data.remote.dto.SubscribeRequest
import com.loveall.app.data.remote.dto.SubscriptionResponse
import retrofit2.http.*

interface PlansApi {
    @GET(ApiPaths.PLANS) suspend fun plans(): PlansResponse

    @GET(ApiPaths.SUBSCRIPTION) suspend fun subscription(): SubscriptionResponse

    @POST(ApiPaths.CHECKOUT) suspend fun checkout(@Body body: SubscribeRequest): CheckoutDto

    @GET(ApiPaths.PAYMENT_STATUS)
    suspend fun paymentStatus(@Path(ApiParameters.ID) id: String): PaymentStatusDto

    @POST(ApiPaths.CALL_ELIGIBILITY) suspend fun checkCall(@Body body: CallRequest): CallResult
}
