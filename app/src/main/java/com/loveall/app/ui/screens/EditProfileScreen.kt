package com.loveall.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.domain.model.User
import com.loveall.app.presentation.state.ProfileState
import com.loveall.app.ui.components.ErrorCard
import com.loveall.app.ui.components.Loading
import com.loveall.app.ui.components.Page
import com.loveall.app.ui.components.PrimaryButton
import com.loveall.app.ui.components.profile.ProfileEditorForm
import com.loveall.app.ui.theme.AppSpacing

@Composable
fun EditProfileScreen(
    state: ProfileState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onEdit: ((ProfileEdit) -> ProfileEdit) -> Unit,
    onSave: () -> Unit,
    onSaved: (User) -> Unit,
    onAddPhoto: (Uri) -> Unit,
) {
    val picker =
        rememberLauncherForActivityResult(PickVisualMedia()) { uri -> uri?.let(onAddPhoto) }
    LaunchedEffect(state.saved) { if (state.saved) state.user?.let(onSaved) }
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            Page(stringResource(R.string.edit_profile), onBack) {
                Text(
                    stringResource(R.string.profile_edit_support),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ErrorCard(state.error, onRefresh)
                if (state.loading) Loading()
                ProfileEditorForm(state, onEdit) {
                    picker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                }
            }
        }
        Box(
            Modifier.fillMaxWidth()
                .padding(horizontal = AppSpacing.large, vertical = AppSpacing.compact)
        ) {
            PrimaryButton(
                R.string.save,
                !state.busy && !state.loading && state.edit != null,
                onSave,
            )
        }
    }
}
