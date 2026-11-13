package com.loveall.app.core.format

import java.util.Locale

object BrazilianLocale {
    const val LANGUAGE_TAG = "pt-BR"
    const val PREVIEW_TAG = "pt-rBR"
    const val CURRENCY_CODE = "BRL"
    val value: Locale = Locale.forLanguageTag(LANGUAGE_TAG)
}
