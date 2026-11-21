package com.loveall.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.loveall.app.R
import com.loveall.app.domain.model.Profile
import com.loveall.app.presentation.state.PcdCommunityState
import com.loveall.app.ui.components.*
import com.loveall.app.ui.components.discovery.*
import com.loveall.app.ui.theme.AppSpacing

@Composable
fun PcdCommunityScreen(
    state: PcdCommunityState,
    back: () -> Unit,
    onRefresh: () -> Unit,
    onMembership: (Boolean, Boolean) -> Unit,
    onLike: (Profile) -> Unit,
    onPass: (Profile) -> Unit,
    onOpen: (Profile) -> Unit,
    onChat: (String) -> Unit,
    onDismissMatch: () -> Unit,
    onLibras: () -> Unit,
) {
    var consent by rememberSaveable(state.member) { mutableStateOf(false) }
    var confirmLeave by rememberSaveable { mutableStateOf(false) }
    Page(stringResource(R.string.pcd_title), back) {
        InfoCard(stringResource(R.string.pcd_welcome), stringResource(R.string.pcd_description))
        TextButton(onClick = onLibras) { Text(stringResource(R.string.libras_title)) }
        ErrorCard(state.error, onRefresh)
        if (state.busy) Loading()
        when (state.member) {
            null -> if (!state.busy) PrimaryButton(R.string.refresh, true, onRefresh)
            false -> {
                PcdConsent(consent, !state.busy) { consent = it }
                PrimaryButton(R.string.pcd_join, consent && !state.busy) {
                    onMembership(true, consent)
                }
            }
            true -> {
                Text(stringResource(R.string.pcd_member_notice))
                OutlinedButton(onClick = { confirmLeave = true }, enabled = !state.busy) {
                    Text(stringResource(R.string.pcd_leave))
                }
                PrimaryButton(R.string.refresh, !state.busy, onRefresh)
                ExploreProfiles(state.explore, false, onOpen)
                state.explore.profiles.firstOrNull()?.let { profile ->
                    ExploreActions(!state.busy, { onPass(profile) }, { onLike(profile) })
                }
            }
        }
    }
    state.explore.matchId?.let { id ->
        ExploreMatchDialog(state.explore.matchedName.orEmpty(), onDismissMatch) { onChat(id) }
    }
    if (confirmLeave)
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text(stringResource(R.string.pcd_leave)) },
            text = { Text(stringResource(R.string.pcd_leave_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmLeave = false
                        consent = false
                        onMembership(false, false)
                    }
                ) {
                    Text(stringResource(R.string.pcd_leave))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLeave = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
}

@Composable
private fun PcdConsent(checked: Boolean, enabled: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .toggleable(checked, enabled = enabled, role = Role.Checkbox, onValueChange = onChecked)
            .padding(vertical = AppSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked, onCheckedChange = null, enabled = enabled)
        Text(stringResource(R.string.pcd_consent), Modifier.weight(1f))
    }
}
