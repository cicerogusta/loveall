package com.loveall.app.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.loveall.app.AppContainer
import com.loveall.app.domain.model.User
import com.loveall.app.presentation.ProfileViewModel
import com.loveall.app.presentation.state.SessionState
import com.loveall.app.ui.screens.EditProfileScreen
import com.loveall.app.ui.screens.ProfileScreen

/**
 * Conecta as telas de conta e edição aos seus ViewModels. A navegação fornece o escopo de cada
 * instância; a coleta acompanha o ciclo de vida da tela. Callbacks encaminham ações e respostas sem
 * colocar navegação ou Compose dentro do ViewModel.
 */
internal fun NavGraphBuilder.profileDestinations(
    container: AppContainer,
    nav: NavHostController,
    back: () -> Unit,
    onSaved: (User) -> Unit,
    onLogout: () -> Unit,
    sessionState: SessionState,
    onDeleteAccount: (String) -> Unit,
    onClearSessionError: () -> Unit,
) {
    composable(Routes.PROFILE) {
        val vm = screenViewModel { ProfileViewModel(container.profiles, container.addProfilePhoto) }
        val state by vm.state.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) { vm.refresh() }
        ProfileScreen(
            state,
            vm::refresh,
            { nav.navigate(Routes.EDIT) },
            { nav.navigate(Routes.PLANS) },
            { nav.navigate(Routes.SAFETY) },
            onLogout,
            sessionState,
            onDeleteAccount,
            onClearSessionError,
            { nav.navigate(Routes.AD_FREE) },
            { nav.navigate(Routes.PCD_COMMUNITY) },
            { nav.navigate(Routes.LIBRAS) },
        )
    }
    composable(Routes.EDIT) {
        val vm = screenViewModel { ProfileViewModel(container.profiles, container.addProfilePhoto) }
        val state by vm.state.collectAsStateWithLifecycle()
        EditProfileScreen(
            state,
            back,
            vm::refresh,
            vm::edit,
            vm::save,
            onSaved,
            onAddPhoto = { uri -> vm.addPhoto(uri.toString()) },
        )
    }
}
