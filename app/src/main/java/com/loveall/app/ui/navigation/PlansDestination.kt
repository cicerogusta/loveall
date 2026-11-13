package com.loveall.app.ui.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.*
import com.loveall.app.AppContainer
import com.loveall.app.BuildConfig
import com.loveall.app.presentation.PlansViewModel
import com.loveall.app.ui.screens.PlansScreen

internal fun NavGraphBuilder.plansDestination(container: AppContainer, back: () -> Unit) {
    composable(Routes.PLANS) {
        val vm: PlansViewModel =
            viewModel(
                factory =
                    viewModelFactory {
                        initializer {
                            PlansViewModel(container.plans, createSavedStateHandle())
                        }
                    }
            )
        val state by vm.state.collectAsStateWithLifecycle()
        PlansScreen(
            state,
            BuildConfig.EXTERNAL_CHECKOUT_ENABLED,
            back,
            vm::refresh,
            vm::checkout,
            vm::checkoutOpened,
            vm::checkPayment,
        )
    }
}
