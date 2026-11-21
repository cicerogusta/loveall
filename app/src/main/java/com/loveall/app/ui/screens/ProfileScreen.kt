package com.loveall.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.loveall.app.R
import com.loveall.app.presentation.*
import com.loveall.app.presentation.state.ProfileState
import com.loveall.app.presentation.state.SessionState
import com.loveall.app.ui.components.*
import com.loveall.app.ui.theme.*

@Composable
fun ProfileScreen(
    state: ProfileState,
    onRefresh: () -> Unit,
    onEdit: () -> Unit,
    onPlans: () -> Unit,
    onSafety: () -> Unit,
    onLogout: () -> Unit,
    sessionState: SessionState,
    onDeleteAccount: (String) -> Unit,
    onClearSessionError: () -> Unit,
    onAdFree: () -> Unit,
    onPcdCommunity: () -> Unit,
    onLibras: () -> Unit,
) {
    var deleteDialog by rememberSaveable { mutableStateOf(false) }
    var logoutDialog by rememberSaveable { mutableStateOf(false) }
    Page(stringResource(R.string.profile)) {
        Text(
            stringResource(R.string.profile_support),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ErrorCard(state.error, onRefresh)
        if (state.loading) Loading()
        state.user?.let { user ->
            HeroSurface {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                ) {
                    Portrait(user.photos.firstOrNull(), user.name, size = AppDimensions.avatarLarge)
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    ) {
                        Text(
                            stringResource(R.string.profile_greeting, user.name),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(user.email, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (user.city.isNotBlank())
                    Text(user.city, style = MaterialTheme.typography.labelLarge)
                if (user.verified)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            Modifier.size(AppDimensions.smallIcon),
                        )
                        Text(
                            stringResource(R.string.verified),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
            }
            if (user.bio.isNotBlank()) InfoCard(stringResource(R.string.about_profile), user.bio)
            MenuRow(
                Icons.Default.Edit,
                stringResource(R.string.edit_profile),
                stringResource(R.string.profile_edit_hint),
                onEdit,
            )
        }
        MenuRow(
            Icons.Default.Star,
            stringResource(R.string.plans),
            stringResource(R.string.profile_plan_hint),
            onPlans,
        )
        MenuRow(
            Icons.Default.Lock,
            stringResource(R.string.safety),
            stringResource(R.string.profile_safety_hint),
            onSafety,
        )
        MenuRow(
            Icons.Default.Person,
            stringResource(R.string.pcd_title),
            stringResource(R.string.pcd_menu_hint),
            onPcdCommunity,
        )
        MenuRow(
            Icons.Default.Info,
            stringResource(R.string.libras_title),
            stringResource(R.string.libras_menu_hint),
            onLibras,
        )
        FeatureTile(
            Icons.Default.FavoriteBorder,
            stringResource(R.string.welcome_inclusive),
            stringResource(R.string.free_likes),
        )
        MenuRow(
            Icons.Default.Star,
            stringResource(R.string.ad_free_title),
            stringResource(R.string.ad_free_menu_hint),
            onAdFree,
        )
        com.loveall.app.ui.monetization.AdPrivacyButton()
        LegalDocuments()
        TextButton(
            onClick = {
                onClearSessionError()
                deleteDialog = true
            },
            enabled = !sessionState.busy,
        ) {
            Text(stringResource(R.string.delete_account), color = MaterialTheme.colorScheme.error)
        }
        TextButton(
            onClick = { logoutDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors =
                ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Text(stringResource(R.string.logout))
        }
    }
    if (deleteDialog)
        DeleteAccountDialog(
            sessionState.busy,
            sessionState.error,
            { deleteDialog = false },
            onDeleteAccount,
        )
    if (logoutDialog)
        ConfirmDialog(
            stringResource(R.string.logout_title),
            stringResource(R.string.logout_body),
            { logoutDialog = false },
            {
                logoutDialog = false
                onLogout()
            },
        )
}
