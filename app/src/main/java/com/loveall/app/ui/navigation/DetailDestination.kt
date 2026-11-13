package com.loveall.app.ui.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.loveall.app.AppContainer
import com.loveall.app.presentation.DetailViewModel
import com.loveall.app.ui.screens.DetailScreen

internal fun NavGraphBuilder.detailDestination(container: AppContainer, back: () -> Unit) {
    composable(
        Routes.DETAIL,
        arguments = listOf(navArgument(RouteArguments.USER_ID) { type = NavType.StringType }),
    ) { destination ->
        val vm = screenViewModel {
            DetailViewModel(
                requireNotNull(destination.arguments?.getString(RouteArguments.USER_ID)),
                container.profiles,
                container.safety,
            )
        }
        val state by vm.state.collectAsStateWithLifecycle()
        DetailScreen(state, back, vm::refresh, vm::block, vm::report)
    }
}
