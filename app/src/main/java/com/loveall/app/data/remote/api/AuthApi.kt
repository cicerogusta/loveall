package com.loveall.app.data.remote.api

import com.loveall.app.data.remote.contract.*
import com.loveall.app.data.remote.dto.AcceptTermsRequest
import com.loveall.app.data.remote.dto.DeleteAccountRequest
import com.loveall.app.data.remote.dto.LoginRequest
import com.loveall.app.data.remote.dto.LoginResponse
import com.loveall.app.data.remote.dto.MeResponse
import com.loveall.app.data.remote.dto.RegistrationRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST(ApiPaths.AUTH_LOGIN) suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST(ApiPaths.AUTH_REGISTER)
    suspend fun register(@Body request: RegistrationRequest): LoginResponse

    @GET(ApiPaths.AUTH_ME)
    suspend fun me(@Header(ApiHeaders.AUTHORIZATION) authorization: String): MeResponse

    @POST(ApiPaths.AUTH_TERMS)
    suspend fun acceptTerms(
        @Header(ApiHeaders.AUTHORIZATION) authorization: String,
        @Body request: AcceptTermsRequest,
    ): MeResponse

    @HTTP(method = ApiMethods.DELETE, path = ApiPaths.AUTH_ME, hasBody = true)
    suspend fun deleteAccount(
        @Header(ApiHeaders.AUTHORIZATION) authorization: String,
        @Body request: DeleteAccountRequest,
    )
}
