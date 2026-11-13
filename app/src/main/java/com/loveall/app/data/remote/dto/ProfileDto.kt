package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields
import com.loveall.app.domain.model.Profile
import retrofit2.http.*

data class ProfileDto(
    val id: String,
    val name: String,
    val age: Int?,
    val gender: String?,
    @SerializedName(ApiFields.GENDER_CUSTOM) val genderCustom: String?,
    val bio: String?,
    val city: String?,
    val photos: List<String>?,
    @SerializedName(ApiFields.IS_VERIFIED) val verified: Int?,
) {
    fun toDomain() =
        Profile(
            id,
            name,
            age,
            gender.orEmpty(),
            genderCustom.orEmpty(),
            bio.orEmpty(),
            city.orEmpty(),
            photos.orEmpty(),
            verified == 1,
        )
}
