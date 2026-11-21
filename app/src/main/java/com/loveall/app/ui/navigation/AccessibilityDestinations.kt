package com.loveall.app.ui.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.loveall.app.AppContainer
import com.loveall.app.presentation.PcdCommunityViewModel
import com.loveall.app.ui.screens.LibrasScreen
import com.loveall.app.ui.screens.PcdCommunityScreen

internal fun NavGraphBuilder.accessibilityDestinations(
    container: AppContainer,
    nav: NavHostController,
    back: () -> Unit,
) {
    composable(Routes.LIBRAS) { LibrasScreen(back) }
    composable(Routes.PCD_COMMUNITY) {
        val vm = screenViewModel {
            PcdCommunityViewModel(container.pcdCommunity, container.connections)
        }
        val state by vm.state.collectAsStateWithLifecycle()
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }
        PcdCommunityScreen(
            state,
            back,
            vm::refresh,
            vm::setMembership,
            vm::like,
            vm::pass,
            { nav.navigate(Routes.detail(it.id)) },
            { nav.navigate(Routes.chat(it)) },
            vm::dismissMatch,
            { nav.navigate(Routes.LIBRAS) },
        )
    }
}
