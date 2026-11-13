package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields
import com.loveall.app.domain.model.ProfileEdit
import retrofit2.http.*

data class ProfileRequest(
    val name: String,
    val bio: String,
    val city: String,
    val gender: String,
    @SerializedName(ApiFields.GENDER_CUSTOM) val genderCustom: String,
    val photos: List<String>,
    @SerializedName(ApiFields.LOOKING_FOR) val lookingFor: List<String>,
    @SerializedName(ApiFields.AGE_MIN) val ageMin: Int,
    @SerializedName(ApiFields.AGE_MAX) val ageMax: Int,
    @SerializedName(ApiFields.MAX_DISTANCE) val maxDistance: Int,
) {
    companion object {
        fun from(edit: ProfileEdit) =
            ProfileRequest(
                edit.name.trim(),
                edit.bio,
                edit.city,
                edit.gender,
                edit.genderCustom,
                edit.photos,
                edit.lookingFor,
                edit.ageMin,
                edit.ageMax,
                edit.maxDistance,
            )
    }
}
