package com.loveall.app.domain.usecase

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.repository.PhotoReader
import com.loveall.app.domain.repository.ProfileRepository
import com.loveall.app.domain.validation.Rules

/**
 * Coordena limite de fotos, preparação da imagem e envio, usando apenas contratos do domínio. Não
 * conhece Uri, ContentResolver ou Retrofit; as implementações são fornecidas pelo contêiner.
 */
class AddProfilePhoto(private val reader: PhotoReader, private val profiles: ProfileRepository) {
    /**
     * Lê e envia a foto somente se ainda houver espaço no perfil.
     *
     * @return referência devolvida pela API para ser acrescentada ao rascunho.
     */
    suspend operator fun invoke(reference: String, currentPhotos: List<String>): String {
        if (currentPhotos.size >= Rules.MAX_PHOTOS) throw AuthException(AuthError.VALIDATION)
        return profiles.uploadPhoto(reader.read(reference))
    }
}
