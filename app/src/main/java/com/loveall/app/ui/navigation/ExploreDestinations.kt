package com.loveall.app.ui.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.loveall.app.AppContainer
import com.loveall.app.presentation.ExploreViewModel
import com.loveall.app.ui.screens.ExploreScreen

internal fun NavGraphBuilder.exploreDestinations(container: AppContainer, nav: NavHostController) {
    listOf(Routes.DISCOVER, Routes.LIKES).forEach { destination ->
        composable(destination) {
            val vm = screenViewModel {
                ExploreViewModel(
                    container.profiles,
                    container.connections,
                    container.safety,
                    destination == Routes.LIKES,
                )
            }
            val state by vm.state.collectAsStateWithLifecycle()
            ExploreScreen(
                state,
                vm::refresh,
                vm::like,
                vm::pass,
                { nav.navigate(Routes.detail(it.id)) },
                { nav.navigate(Routes.PLANS) },
                { nav.navigate(Routes.chat(it)) },
                vm::dismissMatch,
                destination == Routes.LIKES,
                adContent = {
                    if (destination == Routes.DISCOVER) com.loveall.app.ui.monetization.AdBanner()
                },
            )
        }
    }
}
