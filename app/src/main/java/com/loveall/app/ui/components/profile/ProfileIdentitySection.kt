package com.loveall.app.ui.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.model.Gender
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.ui.components.AppField
import com.loveall.app.ui.components.FormSection
import com.loveall.app.ui.components.GenderPicker

@Composable
internal fun ProfileIdentitySection(
    edit: ProfileEdit,
    enabled: Boolean,
    onEdit: ((ProfileEdit) -> ProfileEdit) -> Unit,
) {
    FormSection(
        stringResource(R.string.profile_identity_section),
        stringResource(R.string.identity),
    ) {
        GenderPicker(
            listOf(edit.gender),
            false,
            { values -> onEdit { it.copy(gender = values.first()) } },
            enabled,
        )
        if (edit.gender == Gender.OTHER.wire)
            AppField(
                edit.genderCustom,
                { value -> onEdit { it.copy(genderCustom = value) } },
                R.string.custom_identity,
                enabled,
            )
    }
}
