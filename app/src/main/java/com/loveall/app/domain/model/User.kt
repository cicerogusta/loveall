package com.loveall.app.domain.model

import com.loveall.app.core.text.TextDefaults
import com.loveall.app.domain.validation.Rules

/**
 * Representa a própria conta autenticada, incluindo email e preferências. É a versão recebida do
 * backend; mudanças de formulário são representadas separadamente por ProfileEdit.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val gender: String = TextDefaults.EMPTY,
    val genderCustom: String = TextDefaults.EMPTY,
    val bio: String = TextDefaults.EMPTY,
    val city: String = TextDefaults.EMPTY,
    val photos: List<String> = emptyList(),
    val lookingFor: List<String> = emptyList(),
    val ageMin: Int = Rules.MIN_AGE,
    val ageMax: Int = Rules.DEFAULT_MAX_AGE,
    val maxDistance: Int = Rules.DEFAULT_DISTANCE,
    val verified: Boolean = false,
    val termsVersion: String? = null,
)
