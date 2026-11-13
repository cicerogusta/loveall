package com.loveall.app.ui.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.presentation.state.ProfileState
import com.loveall.app.ui.components.Tag

@Composable
internal fun ProfileEditorForm(
    state: ProfileState,
    onEdit: ((ProfileEdit) -> ProfileEdit) -> Unit,
    onPickPhoto: () -> Unit,
) {
    val edit = state.edit ?: return
    val enabled = !state.busy && !state.loading
    if (state.saved)
        Tag(stringResource(R.string.profile_saved), icon = Icons.Default.Check, highlighted = true)
    ProfileDetailsSection(edit, enabled, onEdit)
    ProfilePhotosSection(edit, enabled, onEdit, state.busy, onPickPhoto)
    ProfileIdentitySection(edit, enabled, onEdit)
    ProfilePreferencesSection(edit, enabled, onEdit, state.user?.id)
    ProfileInterestsSection(edit, enabled, onEdit)
}
