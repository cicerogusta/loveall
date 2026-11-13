package com.loveall.app.ui.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.domain.validation.Rules
import com.loveall.app.ui.components.AppField
import com.loveall.app.ui.components.FormSection

@Composable
internal fun ProfileDetailsSection(
    edit: ProfileEdit,
    enabled: Boolean,
    onEdit: ((ProfileEdit) -> ProfileEdit) -> Unit,
) {
    FormSection(stringResource(R.string.profile_details_section)) {
        AppField(
            edit.name,
            { value -> onEdit { it.copy(name = value) } },
            R.string.name,
            enabled,
        )
        AppField(
            edit.bio,
            { value ->
                if (value.length <= Rules.MAX_BIO_LENGTH) onEdit { it.copy(bio = value) }
            },
            R.string.bio,
            enabled,
            singleLine = false,
        )
        AppField(
            edit.city,
            { value -> onEdit { it.copy(city = value) } },
            R.string.city,
            enabled,
        )
    }
}
