package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields
import com.loveall.app.domain.model.User
import com.loveall.app.domain.validation.Rules

data class UserDto(
    @SerializedName(ApiFields.ID) val id: String?,
    @SerializedName(ApiFields.NAME) val name: String?,
    @SerializedName(ApiFields.EMAIL) val email: String?,
    @SerializedName(ApiFields.GENDER) val gender: String? = null,
    @SerializedName(ApiFields.GENDER_CUSTOM) val genderCustom: String? = null,
    @SerializedName(ApiFields.BIO) val bio: String? = null,
    @SerializedName(ApiFields.CITY) val city: String? = null,
    @SerializedName(ApiFields.PHOTOS) val photos: List<String>? = null,
    @SerializedName(ApiFields.LOOKING_FOR) val lookingFor: List<String>? = null,
    @SerializedName(ApiFields.AGE_MIN) val ageMin: Int? = null,
    @SerializedName(ApiFields.AGE_MAX) val ageMax: Int? = null,
    @SerializedName(ApiFields.MAX_DISTANCE) val maxDistance: Int? = null,
    @SerializedName(ApiFields.IS_VERIFIED) val verified: Int? = null,
    @SerializedName(ApiFields.TERMS_VERSION) val termsVersion: String? = null,
) {
    fun toDomain(): User {
        require(!id.isNullOrBlank() && !name.isNullOrBlank() && !email.isNullOrBlank())
        return User(
            id,
            name,
            email,
            gender.orEmpty(),
            genderCustom.orEmpty(),
            bio.orEmpty(),
            city.orEmpty(),
            photos.orEmpty(),
            lookingFor.orEmpty(),
            ageMin ?: Rules.MIN_AGE,
            ageMax ?: Rules.DEFAULT_MAX_AGE,
            maxDistance ?: Rules.DEFAULT_DISTANCE,
            verified == 1,
            termsVersion,
        )
    }
}
