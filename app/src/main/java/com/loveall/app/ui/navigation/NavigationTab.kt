package com.loveall.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

internal data class NavigationTab(
    val route: String,
    @param:StringRes val label: Int,
    val icon: ImageVector,
)
