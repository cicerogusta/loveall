package com.loveall.app

import com.loveall.app.domain.model.Gender
import com.loveall.app.domain.model.Plan
import com.loveall.app.domain.model.Profile
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.domain.model.Registration
import com.loveall.app.domain.model.User
import com.loveall.app.fixtures.JsonFixture
import com.loveall.app.fixtures.TestPlans
import com.loveall.app.fixtures.TestUsers
import java.time.LocalDate

object TestFixtures {
    val basicPlan = Plan(TestPlans.BASIC_ID, TestPlans.BASIC_NAME, 9.9, 60)
    const val TOKEN = TestUsers.TOKEN
    const val EMAIL = TestUsers.EMAIL
    const val PASSWORD = TestUsers.PASSWORD
    val today: LocalDate = LocalDate.of(2026, 11, 12)
    val user = User(TestUsers.SELF_ID, TestUsers.SELF_NAME, EMAIL)
    val profile =
        Profile(
            TestUsers.PARTNER_ID,
            TestUsers.PARTNER_NAME,
            28,
            Gender.NON_BINARY.wire,
            "",
            TestUsers.BIO,
            TestUsers.CITY,
            emptyList(),
            true,
        )
    val registration =
        Registration(
            TestUsers.SELF_NAME,
            EMAIL,
            PASSWORD,
            TestUsers.ADULT_BIRTH_DATE,
            Gender.NON_BINARY.wire,
            termsAccepted = true,
        )
    val edit =
        ProfileEdit(
            TestUsers.SELF_NAME,
            TestUsers.BIO,
            TestUsers.CITY,
            Gender.NON_BINARY.wire,
            "",
            emptyList(),
            emptyList(),
            18,
            99,
            50,
        )
    val loginJson
        get() = JsonFixture.read(AUTH_LOGIN_SUCCESS_RESOURCE)

    val meJson
        get() = JsonFixture.read(AUTH_CURRENT_USER_RESOURCE)

    private const val AUTH_LOGIN_SUCCESS_RESOURCE = "api/auth-login-success.json"
    private const val AUTH_CURRENT_USER_RESOURCE = "api/auth-current-user.json"
}
