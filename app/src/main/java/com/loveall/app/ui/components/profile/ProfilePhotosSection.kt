package com.loveall.app.ui.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.domain.validation.Rules
import com.loveall.app.ui.components.FormSection
import com.loveall.app.ui.components.Portrait
import com.loveall.app.ui.theme.AppDimensions
import com.loveall.app.ui.theme.AppSpacing

@Composable
internal fun ProfilePhotosSection(
    edit: ProfileEdit,
    enabled: Boolean,
    onEdit: ((ProfileEdit) -> ProfileEdit) -> Unit,
    busy: Boolean,
    onPickPhoto: () -> Unit,
) {
    FormSection(
        stringResource(R.string.profile_photos_section),
        stringResource(R.string.photo_hint),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium),
        ) {
            edit.photos.forEachIndexed { index, url ->
                ProfilePhotoThumbnail(url, edit.name, index, enabled) {
                    onEdit { current ->
                        current.copy(
                            photos =
                                current.photos.filterIndexed { position, _ -> position != index }
                        )
                    }
                }
            }
        }
        if (edit.photos.size < Rules.MAX_PHOTOS) {
            OutlinedButton(
                onClick = {
                    onPickPhoto()
                },
                enabled = enabled,
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(AppSpacing.small))
                Text(stringResource(R.string.add_photo))
            }
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    }
}

@Composable
private fun ProfilePhotoThumbnail(
    url: String,
    name: String,
    index: Int,
    enabled: Boolean,
    onRemove: () -> Unit,
) {
    Box(Modifier.size(AppDimensions.compactPhoto)) {
        Portrait(url, name, size = AppDimensions.compactPhoto)
        FilledTonalIconButton(
            onClick = onRemove,
            enabled = enabled,
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Icon(
                Icons.Default.Close,
                stringResource(R.string.remove_photo, index + 1),
            )
        }
    }
}
