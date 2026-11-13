package com.loveall.app.fixtures

internal object ContractResponses {
    val PAYMENT_APPROVED
        get() = JsonFixture.read(PAYMENT_APPROVED_RESOURCE)

    val PAYMENT_LEGACY_PENDING
        get() = JsonFixture.read(PAYMENT_LEGACY_PENDING_RESOURCE)

    val PAYMENT_UNKNOWN
        get() = JsonFixture.read(PAYMENT_UNKNOWN_RESOURCE)

    private const val PAYMENT_APPROVED_RESOURCE = "api/payment-approved.json"
    private const val PAYMENT_LEGACY_PENDING_RESOURCE = "api/payment-legacy-pending.json"
    private const val PAYMENT_UNKNOWN_RESOURCE = "api/payment-unknown.json"
    val PHOTO_UPLOADED
        get() = JsonFixture.read(PHOTO_UPLOADED_RESOURCE)

    private const val PHOTO_UPLOADED_RESOURCE = "api/photo-uploaded.json"
    val PHOTO_INVALID_REFERENCE
        get() = JsonFixture.read(PHOTO_INVALID_REFERENCE_RESOURCE)

    private const val PHOTO_INVALID_REFERENCE_RESOURCE = "api/photo-invalid-reference.json"
    val LOGIN_MISSING_TOKEN
        get() = JsonFixture.read(LOGIN_MISSING_TOKEN_RESOURCE)

    private const val LOGIN_MISSING_TOKEN_RESOURCE = "api/login-missing-token.json"
    val LIKE_WITH_MATCH
        get() = JsonFixture.read(LIKE_WITH_MATCH_RESOURCE)

    private const val LIKE_WITH_MATCH_RESOURCE = "api/like-with-match.json"
    val MESSAGE_PAGE
        get() = JsonFixture.read(MESSAGE_PAGE_RESOURCE)

    private const val MESSAGE_PAGE_RESOURCE = "api/message-page.json"
    val CHECKOUT_UNSAFE_URL
        get() = JsonFixture.read(CHECKOUT_UNSAFE_URL_RESOURCE)

    private const val CHECKOUT_UNSAFE_URL_RESOURCE = "api/checkout-unsafe-url.json"
    val SUBSCRIPTION_APPROVED
        get() = JsonFixture.read(SUBSCRIPTION_APPROVED_RESOURCE)

    private const val SUBSCRIPTION_APPROVED_RESOURCE = "api/subscription-approved.json"
    val CALL_FORBIDDEN
        get() = JsonFixture.read(CALL_FORBIDDEN_RESOURCE)

    private const val CALL_FORBIDDEN_RESOURCE = "api/call-forbidden.json"
    val REPORT_CREATED
        get() = JsonFixture.read(REPORT_CREATED_RESOURCE)

    private const val REPORT_CREATED_RESOURCE = "api/report-created.json"
    val SAFETY_TIPS
        get() = JsonFixture.read(SAFETY_TIPS_RESOURCE)

    private const val SAFETY_TIPS_RESOURCE = "api/safety-tips.json"
    val BLOCKED_USERS
        get() = JsonFixture.read(BLOCKED_USERS_RESOURCE)

    private const val BLOCKED_USERS_RESOURCE = "api/blocked-users.json"
}
