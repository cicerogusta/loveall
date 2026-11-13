package com.loveall.app.fixtures

import com.loveall.app.domain.model.Registration

internal data class RegistrationCase(
    val description: String,
    val form: Registration,
    val expected: Boolean,
) {
    override fun toString() = description
}
