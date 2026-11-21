package com.loveall.app.domain.model

import com.loveall.app.core.text.TextDefaults
import com.loveall.app.domain.validation.Rules
import java.time.LocalDate
import java.time.Period

/**
 * Entrada do cadastro antes da criação da conta. A data já deve estar em formato ISO quando chega à
 * validação do domínio; o campo da UI faz a conversão.
 */
data class Registration(
    val name: String = TextDefaults.EMPTY,
    val email: String = TextDefaults.EMPTY,
    val password: String = TextDefaults.EMPTY,
    val birthdate: String = TextDefaults.EMPTY,
    val gender: String = TextDefaults.EMPTY,
    val genderCustom: String = TextDefaults.EMPTY,
    val lookingFor: List<String> = emptyList(),
    val termsAccepted: Boolean = false,
) {
    fun isValid(today: LocalDate): Boolean {
        val birth = runCatching { LocalDate.parse(birthdate) }.getOrNull() ?: return false
        val age = Period.between(birth, today).years
        return termsAccepted &&
            Rules.validName(name) &&
            EMAIL.matches(email.trim()) &&
            Rules.validPassword(password) &&
            age in Rules.MIN_AGE..Rules.MAX_AGE &&
            !birth.isAfter(today) &&
            Rules.validIdentity(gender, genderCustom) &&
            Rules.validPreferences(lookingFor)
    }

    private companion object {
        private const val EMAIL_PATTERN = "[^\\s@]+@[^\\s@]+\\.[^\\s@]+"
        val EMAIL = Regex(EMAIL_PATTERN)
    }
}
