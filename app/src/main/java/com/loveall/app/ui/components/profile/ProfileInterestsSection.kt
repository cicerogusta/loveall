package com.loveall.app.ui.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.ui.components.FormSection
import com.loveall.app.ui.components.GenderPicker

@Composable
internal fun ProfileInterestsSection(
    edit: ProfileEdit,
    enabled: Boolean,
    onEdit: ((ProfileEdit) -> ProfileEdit) -> Unit,
) {
    FormSection(
        stringResource(R.string.looking_for),
        stringResource(R.string.looking_for_hint),
    ) {
        GenderPicker(
            edit.lookingFor,
            true,
            { values -> onEdit { it.copy(lookingFor = values) } },
            enabled,
        )
    }
}
