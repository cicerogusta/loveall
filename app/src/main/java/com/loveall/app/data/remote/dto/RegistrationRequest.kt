package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields
import com.loveall.app.domain.model.LegalPolicy
import com.loveall.app.domain.model.Registration
import retrofit2.http.*

data class RegistrationRequest(
    val name: String,
    val email: String,
    val password: String,
    val birthdate: String,
    val gender: String,
    @SerializedName(ApiFields.GENDER_CUSTOM) val genderCustom: String,
    @SerializedName(ApiFields.LOOKING_FOR) val lookingFor: List<String>,
    @SerializedName(ApiFields.TERMS_VERSION) val termsVersion: String,
) {
    companion object {
        fun from(form: Registration) =
            RegistrationRequest(
                form.name.trim(),
                form.email.trim(),
                form.password,
                form.birthdate,
                form.gender,
                form.genderCustom.trim(),
                form.lookingFor,
                LegalPolicy.VERSION,
            )
    }
}
