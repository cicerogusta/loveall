package com.loveall.app.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.loveall.app.AppContainer
import com.loveall.app.presentation.MatchesViewModel
import com.loveall.app.ui.screens.MatchesScreen

internal fun NavGraphBuilder.matchesDestination(container: AppContainer, nav: NavHostController) {
    composable(Routes.MATCHES) {
        val vm = screenViewModel { MatchesViewModel(container.connections) }
        val state by vm.state.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) { vm.refresh() }
        MatchesScreen(state, vm::refresh) { nav.navigate(Routes.chat(it)) }
    }
}
