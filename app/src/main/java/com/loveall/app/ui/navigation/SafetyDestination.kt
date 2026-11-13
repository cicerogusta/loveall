package com.loveall.app.ui.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.*
import com.loveall.app.AppContainer
import com.loveall.app.presentation.SafetyViewModel
import com.loveall.app.ui.screens.SafetyScreen

internal fun NavGraphBuilder.safetyDestination(container: AppContainer, back: () -> Unit) {
    composable(Routes.SAFETY) {
        val vm = screenViewModel { SafetyViewModel(container.safety) }
        val state by vm.state.collectAsStateWithLifecycle()
        SafetyScreen(state, vm::refresh, vm::unblock, back)
    }
}
