package com.loveall.app.domain.validation

import com.loveall.app.domain.model.Gender
import java.net.URI

object Rules {
    private const val PHOTO_REFERENCE_PATTERN =
        "/api/photos/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.jpg"
    private const val HTTPS_SCHEME = "https"
    const val MIN_AGE = 18
    const val MAX_AGE = 120
    const val DEFAULT_MAX_AGE = 99
    const val DEFAULT_DISTANCE = 50
    const val MIN_DISTANCE = 5
    const val MAX_DISTANCE = 200
    const val MIN_NAME_LENGTH = 2
    const val MAX_NAME_LENGTH = 50
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 72
    const val MAX_CITY_LENGTH = 100
    const val MAX_CUSTOM_GENDER_LENGTH = 100
    const val MAX_BIO_LENGTH = 500
    const val MAX_PHOTOS = 6
    const val MAX_MESSAGE_LENGTH = 2000
    const val MESSAGE_PAGE_SIZE = 50
    const val UNLIMITED_MINUTES = 999999
    const val CHAT_REFRESH_MILLIS = 8000L

    fun validName(name: String) =
        name.trim().length in MIN_NAME_LENGTH..MAX_NAME_LENGTH &&
            name.codePoints().noneMatch { Character.getType(it) in numericCategories }

    // Equivale a \p{N} da API, inclusive números fora do BMP e numerais romanos.
    private val numericCategories =
        setOf(
            Character.DECIMAL_DIGIT_NUMBER.toInt(),
            Character.LETTER_NUMBER.toInt(),
            Character.OTHER_NUMBER.toInt(),
        )

    fun validPassword(password: String) =
        password.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH &&
            password.toByteArray(Charsets.UTF_8).size <= MAX_PASSWORD_LENGTH &&
            password.any { it in 'a'..'z' || it in 'A'..'Z' } &&
            password.any { it in '0'..'9' }

    fun validIdentity(gender: String, description: String) =
        Gender.entries.any { it.wire == gender } &&
            description.length <= MAX_CUSTOM_GENDER_LENGTH &&
            (gender != Gender.OTHER.wire || description.isNotBlank())

    fun validPreferences(values: List<String>) =
        values.size <= Gender.entries.size &&
            values.distinct().size == values.size &&
            values.all { value -> Gender.entries.any { it.wire == value } }

    private val uploadedPhotoPattern = Regex(PHOTO_REFERENCE_PATTERN)

    fun uploadedPhoto(url: String) = uploadedPhotoPattern.matches(url)

    fun validProfilePhoto(url: String) = uploadedPhoto(url) || validPhoto(url)

    fun validPhoto(url: String): Boolean = runCatching {
        val uri = URI(url)
        uri.scheme == HTTPS_SCHEME && !uri.host.isNullOrBlank() && uri.userInfo == null
    }
        .getOrDefault(false)
}
