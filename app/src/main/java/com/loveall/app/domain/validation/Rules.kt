package com.loveall.app.domain.validation

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
    const val MAX_BIO_LENGTH = 500
    const val MAX_PHOTOS = 6
    const val MAX_MESSAGE_LENGTH = 2000
    const val MESSAGE_PAGE_SIZE = 50
    const val UNLIMITED_MINUTES = 999999
    const val CHAT_REFRESH_MILLIS = 8000L

    fun validName(name: String) =
        name.trim().length in MIN_NAME_LENGTH..MAX_NAME_LENGTH && name.none(Char::isDigit)

    private val uploadedPhotoPattern = Regex(PHOTO_REFERENCE_PATTERN)

    fun uploadedPhoto(url: String) = uploadedPhotoPattern.matches(url)

    fun validProfilePhoto(url: String) = uploadedPhoto(url) || validPhoto(url)

    fun validPhoto(url: String): Boolean = runCatching {
        val uri = URI(url)
        uri.scheme == HTTPS_SCHEME && !uri.host.isNullOrBlank() && uri.userInfo == null
    }
        .getOrDefault(false)
}
