package com.loveall.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.loveall.app.data.remote.contract.ApiFields
import com.loveall.app.domain.model.LegalPolicy

data class AcceptTermsRequest(
    @SerializedName(ApiFields.TERMS_VERSION) val termsVersion: String = LegalPolicy.VERSION
)
