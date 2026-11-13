package com.loveall.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import com.loveall.app.data.local.KeystoreSessionStore
import com.loveall.app.fixtures.*
import com.loveall.app.ui.LoveAllRoot
import com.loveall.app.ui.theme.LoveAllTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NativeFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var server: MockWebServer
    private val apiDispatcher = NativeApiDispatcher()
    private val sentBodies
        get() = apiDispatcher.sentBodies

    private val registrations
        get() = apiDispatcher.registrations

    private var failLogin: Boolean
        get() = apiDispatcher.failLogin
        set(value) {
            apiDispatcher.failLogin = value
        }

    private var previousToken: String? = null

    @Before
    fun setup() {
        runBlocking {
            val store = KeystoreSessionStore(context, Dispatchers.IO)
            previousToken = store.read()
            store.clear()
        }
        server =
            MockWebServer().apply {
                dispatcher = apiDispatcher
                start()
            }
        val container = AppContainer(context, server.url(NativeApiContract.API_ROOT).toString())
        val arguments = InstrumentationRegistry.getArguments()
        val dark =
            arguments.getString(NativeScenario.DARK_ARGUMENT)?.toBooleanStrictOrNull() == true
        val fontScale = arguments.getString(NativeScenario.FONT_SCALE_ARGUMENT)?.toFloatOrNull()
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale ?: density.fontScale)
            ) {
                LoveAllTheme(dark = dark) { LoveAllRoot(container) }
            }
        }
    }

    @After
    fun cleanup() {
        try {
            server.shutdown()
        } finally {
            runBlocking {
                val store = KeystoreSessionStore(context, Dispatchers.IO)
                store.clear()
                previousToken?.let { store.write(it) }
            }
        }
    }

    private fun label(id: Int) = context.getString(id)

    private fun awaitText(text: String) {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun field(id: Int) = compose.onNode(hasText(label(id)) and hasSetTextAction())

    private fun login() {
        awaitText(label(R.string.sign_in))
        compose.onNodeWithText(label(R.string.sign_in)).performScrollTo().performClick()
        field(R.string.email).performTextInput(NativeScenario.EMAIL)
        field(R.string.password).performTextInput(NativeScenario.PASSWORD)
        compose.onNodeWithText(label(R.string.submit_login)).performScrollTo().performClick()
        awaitText(NativeScenario.PARTNER_NAME)
    }

    @Test
    fun nativeLoginMatchesAndSendMessageThroughHTTPContract() {
        login()
        screenshot(NativeScreenshots.DISCOVER)
        compose.onNodeWithText(label(R.string.matches)).performClick()
        awaitText(label(R.string.new_match))
        screenshot(NativeScreenshots.MATCHES)
        compose.onNodeWithText(NativeScenario.PARTNER_NAME).performClick()
        awaitText(NativeScenario.INCOMING_MESSAGE)
        field(R.string.message).performTextInput(NativeScenario.OUTGOING_MESSAGE)
        compose.onNodeWithContentDescription(label(R.string.send)).performClick()
        awaitText(NativeScenario.OUTGOING_MESSAGE)
        assertEquals(1, sentBodies.size)
        assertTrue(sentBodies.single().contains(NativeScenario.OUTGOING_MESSAGE))
        screenshot(NativeScreenshots.CHAT)
    }

    @Test
    fun invalidCredentialsKeepPublicNavigationAndAllowRetry() {
        failLogin = true
        awaitText(label(R.string.sign_in))
        compose.onNodeWithText(label(R.string.sign_in)).performScrollTo().performClick()
        field(R.string.email).performTextInput(NativeScenario.EMAIL)
        field(R.string.password).performTextInput(NativeScenario.WRONG_PASSWORD)
        compose.onNodeWithText(label(R.string.submit_login)).performScrollTo().performClick()
        awaitText(label(R.string.invalid_credentials))
        compose.onNodeWithText(label(R.string.discover)).assertDoesNotExist()
        screenshot(NativeScreenshots.LOGIN_ERROR)
        failLogin = false
        compose.onNodeWithText(label(R.string.submit_login)).performScrollTo().performClick()
        awaitText(NativeScenario.PARTNER_NAME)
    }

    @Test
    fun welcomeRendersAndRegistrationStartsWithAccessibleFields() {
        awaitText(label(R.string.create_account))
        screenshot(NativeScreenshots.WELCOME)
        compose.onNodeWithText(label(R.string.create_account)).performScrollTo().performClick()
        field(R.string.name).assertExists().performTextInput(NativeScenario.SELF_NAME)
        field(R.string.email).performTextInput(NativeScenario.EMAIL)
        field(R.string.password).performTextInput(NativeScenario.PASSWORD)
        compose.onNodeWithText(label(R.string.continue_action)).performScrollTo().performClick()
        field(R.string.birthdate).assertExists()
        compose.onNodeWithText(label(R.string.identity)).assertExists()
        screenshot(NativeScreenshots.REGISTER_IDENTITY)
    }

    @Test
    fun accountDeletionRequiresPasswordAllowsRetryAndReturnsToWelcome() {
        login()
        compose.onNodeWithText(label(R.string.profile)).performClick()
        compose.onNodeWithText(label(R.string.delete_account)).performScrollTo().performClick()
        field(R.string.password).performTextInput(NativeScenario.WRONG_PASSWORD)
        val confirm =
            hasText(label(R.string.delete_account)) and
                hasClickAction() and
                hasAnyAncestor(isDialog())
        compose.onNode(confirm).performClick()
        awaitText(label(R.string.delete_account_password_error))
        field(R.string.password).performTextReplacement(NativeScenario.PASSWORD)
        compose.onNode(confirm).performClick()
        awaitText(label(R.string.create_account))
        assertEquals(2, apiDispatcher.deletions.size)
        compose.onNodeWithText(label(R.string.discover)).assertDoesNotExist()
    }

    @Test
    fun oldAccountMustReviewTermsBeforeEnteringMemberNavigation() {
        apiDispatcher.requireConsent = true
        awaitText(label(R.string.sign_in))
        compose.onNodeWithText(label(R.string.sign_in)).performScrollTo().performClick()
        field(R.string.email).performTextInput(NativeScenario.EMAIL)
        field(R.string.password).performTextInput(NativeScenario.PASSWORD)
        compose.onNodeWithText(label(R.string.submit_login)).performScrollTo().performClick()
        awaitText(label(R.string.terms_consent_body))
        compose
            .onNodeWithText(label(R.string.continue_action))
            .performScrollTo()
            .assertIsNotEnabled()
        compose.onNode(isToggleable()).performScrollTo().performClick()
        compose.onNodeWithText(label(R.string.continue_action)).performScrollTo().performClick()
        awaitText(NativeScenario.PARTNER_NAME)
    }

    @Test
    fun adultRegistrationSendsPreferencesAndOpensNativeProfileEditor() {
        awaitText(label(R.string.create_account))
        compose.onNodeWithText(label(R.string.create_account)).performScrollTo().performClick()
        field(R.string.name).performTextInput(NativeScenario.SELF_NAME)
        field(R.string.email).performTextInput(NativeScenario.EMAIL)
        field(R.string.password).performTextInput(NativeScenario.PASSWORD)
        compose.onNodeWithText(label(R.string.continue_action)).performScrollTo().performClick()
        field(R.string.birthdate).performTextInput(NativeScenario.BIRTHDATE_INPUT)
        field(R.string.birthdate).assertTextContains(NativeScenario.BIRTHDATE_INPUT)
        compose.onNodeWithText(label(R.string.gender_non_binary)).performScrollTo().performClick()
        compose.onNodeWithText(label(R.string.continue_action)).performScrollTo().performClick()
        compose.onNodeWithText(label(R.string.gender_woman)).performScrollTo().performClick()
        compose.onNode(isToggleable()).performScrollTo().performClick()
        compose.onNodeWithText(label(R.string.create_account)).performScrollTo().performClick()
        awaitText(label(R.string.edit_profile))
        assertEquals(1, registrations.size)
        val body = com.google.gson.JsonParser.parseString(registrations.single()).asJsonObject
        assertEquals(NativeScenario.NON_BINARY_WIRE, body[NativeApiContract.GENDER_FIELD].asString)
        assertEquals(NativeScenario.BIRTHDATE_ISO, body[NativeApiContract.BIRTHDATE_FIELD].asString)
        assertEquals(NativeApiContract.TERMS_VERSION, body[NativeApiContract.TERMS_FIELD].asString)
        assertEquals(
            NativeScenario.WOMAN_WIRE,
            body[NativeApiContract.PREFERENCES_FIELD].asJsonArray[0].asString,
        )
        field(R.string.name).assertTextContains(NativeScenario.SELF_NAME)
        screenshot(NativeScreenshots.EDIT_PROFILE)
    }

    @Test
    fun redesignedConversationMenuPreservesReportingAndSafetyActions() {
        login()
        compose.onNodeWithText(label(R.string.matches)).performClick()
        awaitText(label(R.string.new_match))
        compose.onNodeWithText(NativeScenario.PARTNER_NAME).performClick()
        awaitText(NativeScenario.INCOMING_MESSAGE)
        compose.onNodeWithContentDescription(label(R.string.chat_more)).performClick()
        compose.onNodeWithText(label(R.string.audio_call)).assertDoesNotExist()
        compose.onNodeWithText(label(R.string.video_call)).assertDoesNotExist()
        compose.onNodeWithText(label(R.string.block)).assertExists()
        compose.onNodeWithText(label(R.string.unmatch)).assertExists()
        compose.onNodeWithText(label(R.string.calls_menu_unavailable)).assertIsNotEnabled()
        compose.onNodeWithText(label(R.string.report)).performClick()
        compose.onNodeWithText(label(R.string.reason_scam)).performScrollTo().performClick()
        compose.onNode(hasText(label(R.string.report)) and hasClickAction()).performClick()
        awaitText(label(R.string.report_sent))
        compose.onNodeWithText(label(R.string.report_sent)).assertExists()
    }

    @Test
    fun safetyAndPlansLoadFromAuthenticatedApi() {
        login()
        compose.onNodeWithText(label(R.string.profile)).performClick()
        awaitText(context.getString(R.string.profile_greeting, NativeScenario.SELF_NAME))
        screenshot(NativeScreenshots.PROFILE)
        compose.onNodeWithText(label(R.string.safety)).performScrollTo().performClick()
        awaitText(NativeScenario.SAFETY_TITLE)
        compose.onNodeWithText(NativeScenario.SAFETY_DETAIL).assertExists()
        screenshot(NativeScreenshots.SAFETY)
        compose.onNodeWithContentDescription(label(R.string.back)).performClick()
        compose.onNodeWithText(label(R.string.plans)).performScrollTo().performClick()
        awaitText(label(R.string.calls_unavailable))
        compose.onNodeWithText(NativeScenario.BASIC_PLAN_NAME).assertDoesNotExist()
        compose
            .onNodeWithText(context.resources.getQuantityString(R.plurals.remaining_minutes, 0, 0))
            .assertExists()
        screenshot(NativeScreenshots.PLANS)
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val arguments = InstrumentationRegistry.getArguments()
        val variant =
            if (arguments.getString(NativeScenario.FONT_SCALE_ARGUMENT) != null)
                NativeScreenshots.LARGE_TEXT_VARIANT
            else if (
                arguments.getString(NativeScenario.DARK_ARGUMENT)?.toBooleanStrictOrNull() == true
            )
                NativeScreenshots.DARK_VARIANT
            else NativeScreenshots.LIGHT_VARIANT
        val directory = context.getExternalFilesDir(NativeScreenshots.DIRECTORY_ROOT + variant)!!
        directory.mkdirs()
        java.io.File(directory, name + NativeScreenshots.IMAGE_EXTENSION).outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
