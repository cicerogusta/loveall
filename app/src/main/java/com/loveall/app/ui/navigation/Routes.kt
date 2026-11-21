package com.loveall.app.ui.navigation

import android.net.Uri

internal object Routes {
    const val PCD_COMMUNITY = "pcd-community"
    const val LIBRAS = "libras"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DISCOVER = "discover"
    const val LIKES = "likes"
    const val MATCHES = "matches"
    const val PROFILE = "profile"
    const val EDIT = "edit-profile"
    const val SAFETY = "safety"
    const val AD_FREE = "ad-free"
    const val PLANS = "plans"
    private const val CHAT_PREFIX = "chat/"
    const val CHAT = CHAT_PREFIX + RouteArguments.MATCH_PLACEHOLDER
    private const val DETAIL_PREFIX = "detail/"
    const val DETAIL = DETAIL_PREFIX + RouteArguments.USER_PLACEHOLDER

    fun chat(id: String) = CHAT_PREFIX + Uri.encode(id)

    fun detail(id: String) = DETAIL_PREFIX + Uri.encode(id)
}
