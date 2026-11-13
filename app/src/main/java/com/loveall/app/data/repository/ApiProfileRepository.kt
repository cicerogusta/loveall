package com.loveall.app.data.repository

import com.loveall.app.data.apiCall
import com.loveall.app.data.remote.api.ProfileApi
import com.loveall.app.data.remote.contract.PhotoUploadContract
import com.loveall.app.data.remote.dto.ProfileDto
import com.loveall.app.data.remote.dto.ProfileRequest
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.domain.model.User
import com.loveall.app.domain.repository.ProfileRepository
import com.loveall.app.domain.validation.Rules
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Adapta o contrato de perfil para o JSON e os endpoints da API. Valida o rascunho antes de
 * enviá-lo e devolve modelos de domínio, sem expor DTOs à apresentação.
 */
class ApiProfileRepository(private val api: ProfileApi) : ProfileRepository {
    /**
     * Envia bytes JPEG por multipart e aceita apenas a referência de foto esperada na resposta. O
     * servidor recebe o arquivo aqui; a associação ao perfil ocorre na atualização do rascunho
     * salvo.
     */
    override suspend fun uploadPhoto(jpeg: ByteArray): String {
        if (jpeg.isEmpty() || jpeg.size > 4 * 1024 * 1024) throw AuthException(AuthError.VALIDATION)
        return apiCall {
            val part =
                MultipartBody.Part.createFormData(
                    PhotoUploadContract.FIELD_NAME,
                    PhotoUploadContract.FILE_NAME,
                    jpeg.toRequestBody(PhotoUploadContract.MIME_TYPE.toMediaType()),
                )
            api.uploadPhoto(part).photo.also { require(Rules.uploadedPhoto(it)) }
        }
    }

    override suspend fun current() = apiCall { requireNotNull(api.current().user).toDomain() }

    override suspend fun update(edit: ProfileEdit): User {
        if (!edit.isValid()) throw AuthException(AuthError.VALIDATION)
        return apiCall { requireNotNull(api.update(ProfileRequest.from(edit)).user).toDomain() }
    }

    override suspend fun discover() = apiCall { api.discover().profiles.map(ProfileDto::toDomain) }

    override suspend fun profile(id: String) = apiCall { api.profile(id).profile.toDomain() }
}
