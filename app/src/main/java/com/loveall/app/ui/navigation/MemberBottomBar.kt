package com.loveall.app.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.*
import com.loveall.app.R
import com.loveall.app.ui.components.label
import com.loveall.app.ui.theme.AppDimensions
import com.loveall.app.ui.theme.AppSpacing

private val tabs =
    listOf(
        NavigationTab(Routes.DISCOVER, R.string.discover, Icons.Default.Search),
        NavigationTab(Routes.LIKES, R.string.likes, Icons.Default.Favorite),
        NavigationTab(Routes.MATCHES, R.string.matches, Icons.Default.Email),
        NavigationTab(Routes.PROFILE, R.string.profile, Icons.Default.Person),
    )

@Composable
internal fun MemberBottomBar(selectedRoute: String?, onSelect: (String) -> Unit) {
    if (tabs.any { it.route == selectedRoute })
        Box(
            Modifier.fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.small),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                Modifier.widthIn(max = AppDimensions.formWidth),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = AppDimensions.smallElevation,
                border =
                    BorderStroke(
                        AppDimensions.border,
                        MaterialTheme.colorScheme.outlineVariant,
                    ),
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = AppDimensions.smallElevation,
                    windowInsets = WindowInsets(0, 0, 0, 0),
                ) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedRoute == tab.route,
                            onClick = {
                                onSelect(tab.route)
                            },
                            icon = { Icon(tab.icon, null) },
                            label = {
                                Text(
                                    stringResource(tab.label),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors =
                                NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                        )
                    }
                }
            }
        }
}
