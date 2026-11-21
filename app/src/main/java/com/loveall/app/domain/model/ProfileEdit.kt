package com.loveall.app.domain.model

import com.loveall.app.domain.validation.Rules

/**
 * Rascunho dos campos editáveis de uma conta. É separado de User para distinguir dados em edição da
 * versão confirmada pelo servidor.
 */
data class ProfileEdit(
    val name: String,
    val bio: String,
    val city: String,
    val gender: String,
    val genderCustom: String,
    val photos: List<String>,
    val lookingFor: List<String>,
    val ageMin: Int,
    val ageMax: Int,
    val maxDistance: Int,
) {
    /** Aplica limites e valida identidades, preferências e referências de fotos antes do envio. */
    fun isValid() =
        Rules.validName(name) &&
            bio.length <= Rules.MAX_BIO_LENGTH &&
            city.length <= Rules.MAX_CITY_LENGTH &&
            photos.size <= Rules.MAX_PHOTOS &&
            photos.distinct().size == photos.size &&
            photos.all(Rules::validProfilePhoto) &&
            ageMin in Rules.MIN_AGE..Rules.DEFAULT_MAX_AGE &&
            ageMax in ageMin..Rules.DEFAULT_MAX_AGE &&
            maxDistance in Rules.MIN_DISTANCE..Rules.MAX_DISTANCE &&
            Rules.validIdentity(gender, genderCustom) &&
            Rules.validPreferences(lookingFor)

    companion object {
        fun from(user: User) =
            ProfileEdit(
                user.name,
                user.bio,
                user.city,
                user.gender,
                user.genderCustom,
                user.photos,
                user.lookingFor,
                user.ageMin,
                user.ageMax,
                user.maxDistance,
            )
    }
}
