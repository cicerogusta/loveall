package com.loveall.app.fixtures

import androidx.test.platform.app.InstrumentationRegistry

internal object NativeResponses {
    private fun read(path: String): String =
        InstrumentationRegistry.getInstrumentation()
            .context
            .assets
            .open(path)
            .bufferedReader()
            .use { it.readText() }

    val LOGIN
        get() = read(LOGIN_ASSET)

    private const val LOGIN_ASSET = "api/login.json"
    val CURRENT_USER
        get() = read(CURRENT_USER_ASSET)

    private const val CURRENT_USER_ASSET = "api/current-user.json"
    val DISCOVER
        get() = read(DISCOVER_ASSET)

    private const val DISCOVER_ASSET = "api/discover.json"
    val INCOMING_LIKES
        get() = read(INCOMING_LIKES_ASSET)

    private const val INCOMING_LIKES_ASSET = "api/incoming-likes.json"
    val LIKE_WITH_MATCH
        get() = read(LIKE_WITH_MATCH_ASSET)

    private const val LIKE_WITH_MATCH_ASSET = "api/like-with-match.json"
    val MATCHES
        get() = read(MATCHES_ASSET)

    private const val MATCHES_ASSET = "api/matches.json"
    val MESSAGE_CREATED
        get() = read(MESSAGE_CREATED_ASSET)

    private const val MESSAGE_CREATED_ASSET = "api/message-created.json"
    val MESSAGES
        get() = read(MESSAGES_ASSET)

    private const val MESSAGES_ASSET = "api/messages.json"
    val SAFETY_TIPS
        get() = read(SAFETY_TIPS_ASSET)

    private const val SAFETY_TIPS_ASSET = "api/safety-tips.json"
    val BLOCKED_EMPTY
        get() = read(BLOCKED_EMPTY_ASSET)

    private const val BLOCKED_EMPTY_ASSET = "api/blocked-empty.json"
    val REPORT_CREATED
        get() = read(REPORT_CREATED_ASSET)

    private const val REPORT_CREATED_ASSET = "api/report-created.json"
    val PLANS
        get() = read(PLANS_ASSET)

    private const val PLANS_ASSET = "api/plans.json"
    val SUBSCRIPTION
        get() = read(SUBSCRIPTION_ASSET)

    private const val SUBSCRIPTION_ASSET = "api/subscription.json"
}
