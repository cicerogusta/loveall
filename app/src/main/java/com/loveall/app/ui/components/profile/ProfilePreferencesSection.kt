package com.loveall.app.ui.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.loveall.app.R
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.ui.components.AppField
import com.loveall.app.ui.components.FormSection
import com.loveall.app.ui.theme.AppSpacing

@Composable
internal fun ProfilePreferencesSection(
    edit: ProfileEdit,
    enabled: Boolean,
    onEdit: ((ProfileEdit) -> ProfileEdit) -> Unit,
    userId: String?,
) {
    var ageMin by rememberSaveable(userId) { mutableStateOf(edit.ageMin.toString()) }
    var ageMax by rememberSaveable(userId) { mutableStateOf(edit.ageMax.toString()) }
    var distance by
        rememberSaveable(userId) {
            mutableStateOf(edit.maxDistance.toString())
        }

    FormSection(stringResource(R.string.preferences)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.compact)) {
            Box(Modifier.weight(1f)) {
                AppField(
                    ageMin,
                    { value ->
                        ageMin = value
                        onEdit { it.copy(ageMin = value.toIntOrNull() ?: 0) }
                    },
                    R.string.age_min,
                    enabled,
                    keyboardType = KeyboardType.Number,
                )
            }
            Box(Modifier.weight(1f)) {
                AppField(
                    ageMax,
                    { value ->
                        ageMax = value
                        onEdit { it.copy(ageMax = value.toIntOrNull() ?: 0) }
                    },
                    R.string.age_max,
                    enabled,
                    keyboardType = KeyboardType.Number,
                )
            }
        }
        AppField(
            distance,
            { value ->
                distance = value
                onEdit { it.copy(maxDistance = value.toIntOrNull() ?: 0) }
            },
            R.string.distance,
            enabled,
            keyboardType = KeyboardType.Number,
        )
    }
}
