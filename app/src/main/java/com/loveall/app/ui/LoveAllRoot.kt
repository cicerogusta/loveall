package com.loveall.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import com.loveall.app.AppContainer
import com.loveall.app.R
import com.loveall.app.domain.model.LegalPolicy
import com.loveall.app.presentation.SessionViewModel
import com.loveall.app.presentation.state.SessionState
import com.loveall.app.ui.components.ErrorCard
import com.loveall.app.ui.components.Loading
import com.loveall.app.ui.components.LocalPhotoBaseUrl
import com.loveall.app.ui.components.Page
import com.loveall.app.ui.monetization.ProvideMonetization
import com.loveall.app.ui.navigation.MemberNavigation
import com.loveall.app.ui.navigation.Routes
import com.loveall.app.ui.navigation.screenViewModel
import com.loveall.app.ui.screens.LoginScreen
import com.loveall.app.ui.screens.RegisterScreen
import com.loveall.app.ui.screens.TermsConsentScreen
import com.loveall.app.ui.screens.WelcomeScreen

/**
 * Configura a URL usada pelas fotos e inicia a observação da sessão. A escolha entre navegação
 * pública e autenticada depende de SessionState, sem abrir telas diretamente a partir dos
 * repositórios.
 */
@Composable
fun LoveAllRoot(container: AppContainer) {
    CompositionLocalProvider(LocalPhotoBaseUrl provides container.apiBaseUrl) {
        LoveAllContent(container)
    }
}

@Composable
private fun LoveAllContent(container: AppContainer) {
    val session = screenViewModel {
        SessionViewModel(container.authRepository, container.expiredSessions)
    }
    val state by session.state.collectAsStateWithLifecycle()
    ProvideMonetization(
        container,
        state.user?.takeIf { it.termsVersion == LegalPolicy.VERSION }?.id,
    ) {
        SessionContent(container, session, state)
    }
}

@Composable
private fun SessionContent(
    container: AppContainer,
    session: SessionViewModel,
    state: SessionState,
) {
    when {
        state.restoring ->
            Scaffold { insets ->
                Box(Modifier.padding(insets)) {
                    Page(stringResource(R.string.app_name)) {
                        Text(stringResource(R.string.loading_session))
                        Loading()
                    }
                }
            }
        state.restoreFailed ->
            Scaffold { insets ->
                Box(Modifier.padding(insets)) {
                    Page(stringResource(R.string.app_name)) {
                        ErrorCard(state.error, session::restore)
                        TextButton(onClick = { session.logout() }, enabled = !state.busy) {
                            Text(stringResource(R.string.logout))
                        }
                    }
                }
            }
        state.user == null -> PublicNavigation(state, session)
        state.user?.termsVersion != LegalPolicy.VERSION ->
            TermsConsentScreen(
                state,
                session::acceptTerms,
                { session.logout() },
                session::deleteAccount,
            )
        else ->
            key(state.user!!.id) {
                MemberNavigation(
                    container,
                    requireNotNull(state.user).id,
                    state.newAccount,
                    session::updateUser,
                    { session.logout() },
                    state,
                    session::deleteAccount,
                    session::clearError,
                )
            }
    }
}

@Composable
private fun PublicNavigation(state: SessionState, session: SessionViewModel) {
    val nav = rememberNavController()
    Scaffold { insets ->
        NavHost(nav, startDestination = Routes.WELCOME, modifier = Modifier.padding(insets)) {
            composable(Routes.WELCOME) {
                Column {
                    ErrorCard(state.error)
                    WelcomeScreen(
                        onLogin = {
                            session.clearError()
                            nav.navigate(Routes.LOGIN)
                        },
                        onRegister = {
                            session.clearError()
                            nav.navigate(Routes.REGISTER)
                        },
                    )
                }
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    state,
                    { nav.popBackStack() },
                    session::login,
                    {
                        session.clearError()
                        nav.navigate(Routes.REGISTER)
                    },
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(state, { nav.popBackStack() }, session::register)
            }
        }
    }
}
