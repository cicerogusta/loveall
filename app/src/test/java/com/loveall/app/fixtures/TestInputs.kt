package com.loveall.app.fixtures
internal object TestInputs {
    val MULTIBYTE_PASSWORD = "Abc1" + "é".repeat(35)
    const val EMPTY = ""
    const val BLANK = " "
    const val ONE_LETTER = "A"
    const val NUMBERED_NAME = "Alex2"
    const val ROMAN_NUMBERED_NAME = "AlexⅧ"
    const val SUPPLEMENTARY_NUMBERED_NAME = "Alex𝟠"
    const val INVALID_EMAIL = "alex@"
    const val LETTERS_PASSWORD = "OnlyLetters"
    const val DIGITS_PASSWORD = "12345678"
    const val SHORT_PASSWORD = "Short1"
    const val UNKNOWN_IDENTITY = "unknown"
    const val INVALID_PREFERENCE = "invalid"
    const val INSECURE_PHOTO_URL = "http://images.example.test/photo.jpg"
    const val FILLER = "x"
}
