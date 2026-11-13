package com.loveall.app.fixtures

import com.loveall.app.domain.model.ProfileEdit

internal data class ProfileEditCase(
    val description: String,
    val edit: ProfileEdit,
    val expected: Boolean,
) {
    override fun toString() = description
}
