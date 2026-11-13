package com.loveall.app.ui.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.loveall.app.AppContainer
import com.loveall.app.presentation.ChatViewModel
import com.loveall.app.ui.screens.ChatScreen

internal fun NavGraphBuilder.chatDestination(
    container: AppContainer,
    nav: NavHostController,
    userId: String,
    back: () -> Unit,
) {
    composable(
        Routes.CHAT,
        arguments = listOf(navArgument(RouteArguments.MATCH_ID) { type = NavType.StringType }),
    ) { destination ->
        val vm = screenViewModel {
            ChatViewModel(
                requireNotNull(destination.arguments?.getString(RouteArguments.MATCH_ID)),
                container.messages,
                container.connections,
                container.safety,
                container.plans,
            )
        }
        val state by vm.state.collectAsStateWithLifecycle()
        ChatScreen(
            state = state,
            userId = userId,
            onBack = back,
            onRefresh = vm::refresh,
            onSend = vm::send,
            onOlder = vm::older,
            onBlock = vm::block,
            onUnmatch = vm::unmatch,
            onReport = vm::report,
            onDismissFeedback = vm::dismissFeedback,
            onPlans = { nav.navigate(Routes.PLANS) },
        )
    }
}
