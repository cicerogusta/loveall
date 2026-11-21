package com.loveall.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.loveall.app.AppContainer
import com.loveall.app.domain.model.User
import com.loveall.app.presentation.state.SessionState

@Composable
internal fun MemberNavigation(
    container: AppContainer,
    userId: String,
    newAccount: Boolean,
    onSaved: (User) -> Unit,
    onLogout: () -> Unit,
    sessionState: SessionState,
    onDeleteAccount: (String) -> Unit,
    onClearSessionError: () -> Unit,
) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val startRoute = remember { if (newAccount) Routes.EDIT else Routes.DISCOVER }
    val back = {
        if (!nav.popBackStack()) nav.navigate(Routes.PROFILE) { launchSingleTop = true }
        Unit
    }
    Scaffold(
        bottomBar = {
            MemberBottomBar(entry?.destination?.route) { destination ->
                nav.navigate(destination) {
                    popUpTo(nav.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    ) { insets ->
        NavHost(nav, startDestination = startRoute, modifier = Modifier.padding(insets)) {
            exploreDestinations(container, nav)
            accessibilityDestinations(container, nav, back)
            matchesDestination(container, nav)
            chatDestination(container, nav, userId, back)
            profileDestinations(
                container,
                nav,
                back,
                onSaved,
                onLogout,
                sessionState,
                onDeleteAccount,
                onClearSessionError,
            )
            safetyDestination(container, back)
            plansDestination(container, back)
            composable(Routes.AD_FREE) {
                val monetization = com.loveall.app.ui.monetization.LocalMonetization.current
                com.loveall.app.ui.screens.AdFreeSubscriptionScreen(
                    monetization.state,
                    back,
                    monetization.refresh,
                    monetization.restore,
                    monetization.purchase,
                )
            }
            detailDestination(container, back)
        }
    }
}
