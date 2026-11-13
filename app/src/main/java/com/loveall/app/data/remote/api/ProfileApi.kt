package com.loveall.app.data.remote.api

import com.loveall.app.data.remote.contract.*
import com.loveall.app.data.remote.dto.MeResponse
import com.loveall.app.data.remote.dto.PhotoResponse
import com.loveall.app.data.remote.dto.ProfileRequest
import com.loveall.app.data.remote.dto.ProfileResponse
import com.loveall.app.data.remote.dto.ProfilesResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface ProfileApi {
    @Multipart
    @POST(ApiPaths.PROFILE_PHOTO_UPLOAD)
    suspend fun uploadPhoto(@Part photo: MultipartBody.Part): PhotoResponse

    @GET(ApiPaths.AUTH_ME) suspend fun current(): MeResponse

    @PUT(ApiPaths.CURRENT_PROFILE) suspend fun update(@Body body: ProfileRequest): MeResponse

    @GET(ApiPaths.DISCOVER) suspend fun discover(): ProfilesResponse

    @GET(ApiPaths.PROFILE_BY_ID)
    suspend fun profile(@Path(ApiParameters.ID) id: String): ProfileResponse
}
