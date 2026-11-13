package com.loveall.app.data

import com.google.gson.JsonParser
import com.loveall.app.*
import com.loveall.app.data.local.SessionStore
import com.loveall.app.data.remote.api.AuthApi
import com.loveall.app.data.repository.ApiConnectionsRepository
import com.loveall.app.data.repository.ApiMessagesRepository
import com.loveall.app.data.repository.ApiPlansRepository
import com.loveall.app.data.repository.ApiProfileRepository
import com.loveall.app.data.repository.ApiSafetyRepository
import com.loveall.app.data.repository.DefaultAuthRepository
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.model.BlockedUser
import com.loveall.app.domain.model.CallType
import com.loveall.app.domain.model.PaymentState
import com.loveall.app.domain.model.ReportReason
import com.loveall.app.fake.MemorySessionStore
import com.loveall.app.fixtures.ApiExpectations
import com.loveall.app.fixtures.ContractResponses
import com.loveall.app.fixtures.TestConversations
import com.loveall.app.fixtures.TestPhotos
import com.loveall.app.fixtures.TestPlans
import com.loveall.app.fixtures.TestUsers
import java.io.IOException
import java.time.Clock
import java.time.ZoneOffset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RepositoryContractTest {
    @Test
    fun `confirmed account deletion sends password and token then clears local session`() =
        runTest {
            store.token = TestUsers.TOKEN
            server.enqueue(MockResponse().setResponseCode(204))
            auth.deleteAccount(TestUsers.PADDED_PASSWORD)
            val request = server.takeRequest()
            assertEquals(ApiExpectations.DELETE_METHOD, request.method)
            assertEquals(ApiExpectations.CURRENT_ACCOUNT_PATH, request.path)
            assertEquals(
                ApiExpectations.AUTHORIZATION_VALUE,
                request.getHeader(ApiExpectations.AUTHORIZATION_HEADER),
            )
            val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
            assertEquals(TestUsers.PADDED_PASSWORD, body[ApiExpectations.PASSWORD_FIELD].asString)
            assertNull(store.token)
        }

    @Test
    fun `rejected deletion preserves the session and blank password never reaches network`() =
        runTest {
            store.token = TestUsers.TOKEN
            json(ApiExpectations.EMPTY_JSON, 403)
            expectError(AuthError.FORBIDDEN) { auth.deleteAccount(TestUsers.PASSWORD) }
            assertEquals(TestUsers.TOKEN, store.token)
            expectError(AuthError.REQUIRED_CREDENTIALS) {
                auth.deleteAccount(com.loveall.app.fixtures.TestInputs.BLANK)
            }
            assertEquals(1, server.requestCount)
        }

    @Test
    fun `acceptance sends current policy version using the authenticated session`() = runTest {
        store.token = TestUsers.TOKEN
        json(TestFixtures.meJson)
        auth.acceptTerms()
        val request = server.takeRequest()
        assertEquals(ApiExpectations.TERMS_PATH, request.path)
        assertEquals(ApiExpectations.POST_METHOD, request.method)
        assertEquals(
            ApiExpectations.AUTHORIZATION_VALUE,
            request.getHeader(ApiExpectations.AUTHORIZATION_HEADER),
        )
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals(ApiExpectations.TERMS_VERSION, body[ApiExpectations.TERMS_FIELD].asString)
    }

    @Test
    fun `registration without consent never reaches the server`() = runTest {
        expectError(AuthError.VALIDATION) {
            auth.register(TestFixtures.registration.copy(termsAccepted = false))
        }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `given approved response when fetched then preferred field maps to approved state`() =
        runTest {
            // Arrange
            json(ContractResponses.PAYMENT_APPROVED)

            // Act
            val actual = ApiPlansRepository(api, false).paymentStatus(TestPlans.TRANSACTION_ID)

            // Assert
            assertEquals(PaymentState.APPROVED, actual.status)
            assertEquals(TestPlans.BASIC_ID, actual.plan)
            val request = server.takeRequest()
            assertEquals(ApiExpectations.PAYMENT_STATUS_PATH, request.path)
            assertEquals(ApiExpectations.GET_METHOD, request.method)
        }

    @Test
    fun `given legacy payment response when fetched then legacy status remains supported`() =
        runTest {
            json(ContractResponses.PAYMENT_LEGACY_PENDING)
            val actual = ApiPlansRepository(api, false).paymentStatus(TestPlans.TRANSACTION_ID)
            assertEquals(PaymentState.PENDING, actual.status)
            assertFalse(actual.status.isTerminal)
        }

    @Test
    fun `given unknown payment response when fetched then no terminal state is invented`() =
        runTest {
            json(ContractResponses.PAYMENT_UNKNOWN)
            val actual = ApiPlansRepository(api, false).paymentStatus(TestPlans.TRANSACTION_ID)
            assertEquals(PaymentState.UNKNOWN, actual.status)
            assertFalse(actual.status.isTerminal)
        }

    private lateinit var server: MockWebServer
    private lateinit var auth: DefaultAuthRepository
    private lateinit var api: ContractApi
    private lateinit var store: MemorySessionStore

    @Before
    fun setup() {
        server = MockWebServer().apply { start() }
        val retrofit =
            Retrofit.Builder()
                .baseUrl(server.url(ApiExpectations.API_ROOT))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        api = retrofit.create(ContractApi::class.java)
        store = MemorySessionStore()
        auth =
            DefaultAuthRepository(
                retrofit.create(AuthApi::class.java),
                store,
                Clock.fixed(
                    TestFixtures.today.atStartOfDay().toInstant(ZoneOffset.UTC),
                    ZoneOffset.UTC,
                ),
            )
    }

    @After
    fun close() {
        server.shutdown()
    }

    private fun json(body: String, status: Int = 200) {
        server.enqueue(
            MockResponse()
                .setResponseCode(status)
                .setHeader(ApiExpectations.CONTENT_TYPE_HEADER, ApiExpectations.JSON_CONTENT_TYPE)
                .setBody(body)
        )
    }

    private suspend fun expectError(expected: AuthError, block: suspend () -> Unit) {
        try {
            block()
            fail("Expected $expected")
        } catch (error: AuthException) {
            assertEquals(expected, error.error)
        }
    }

    @Test
    fun `photo upload uses multipart and only accepts server photo references`() = runTest {
        val reference = TestPhotos.UPLOADED_REFERENCE
        json(ContractResponses.PHOTO_UPLOADED, 201)
        assertEquals(reference, ApiProfileRepository(api).uploadPhoto(byteArrayOf(1, 2, 3)))
        val request = server.takeRequest()
        assertEquals(ApiExpectations.PHOTO_UPLOAD_PATH, request.path)
        assertEquals(ApiExpectations.POST_METHOD, request.method)
        assertTrue(
            request
                .getHeader(ApiExpectations.CONTENT_TYPE_HEADER)!!
                .startsWith(ApiExpectations.MULTIPART_CONTENT_TYPE)
        )
        val body = request.body.readUtf8()
        assertTrue(body.contains(ApiExpectations.PHOTO_DISPOSITION))
        assertTrue(body.contains(ApiExpectations.PHOTO_CONTENT_TYPE))
        json(ContractResponses.PHOTO_INVALID_REFERENCE)
        expectError(AuthError.SERVICE) { ApiProfileRepository(api).uploadPhoto(byteArrayOf(1)) }
    }

    @Test
    fun `empty or oversized photo never reaches network`() = runTest {
        val profiles = ApiProfileRepository(api)
        expectError(AuthError.VALIDATION) { profiles.uploadPhoto(byteArrayOf()) }
        expectError(AuthError.VALIDATION) { profiles.uploadPhoto(ByteArray(4 * 1024 * 1024 + 1)) }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `login uses JSON contract trims email preserves password and persists only valid response`() =
        runTest {
            json(TestFixtures.loginJson)
            val user = auth.login(TestUsers.PADDED_EMAIL, TestUsers.PADDED_PASSWORD)
            val request = server.takeRequest()
            assertEquals(ApiExpectations.LOGIN_PATH, request.path)
            assertEquals(ApiExpectations.POST_METHOD, request.method)
            val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
            assertEquals(TestUsers.EMAIL, body[ApiExpectations.EMAIL_FIELD].asString)
            assertEquals(TestUsers.PADDED_PASSWORD, body[ApiExpectations.PASSWORD_FIELD].asString)
            assertEquals(TestUsers.SELF_NAME, user.name)
            assertEquals(TestUsers.TOKEN, store.token)
        }

    @Test
    fun `invalid login does not save token`() = runTest {
        json(ApiExpectations.EMPTY_JSON, 401)
        expectError(AuthError.INVALID_CREDENTIALS) {
            auth.login(TestFixtures.EMAIL, TestFixtures.PASSWORD)
        }
        assertNull(store.token)
    }

    @Test
    fun `blank credentials never reach network`() = runTest {
        expectError(AuthError.REQUIRED_CREDENTIALS) { auth.login("", TestFixtures.PASSWORD) }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `malformed successful login does not grant session`() = runTest {
        json(ContractResponses.LOGIN_MISSING_TOKEN)
        expectError(AuthError.SERVICE) { auth.login(TestFixtures.EMAIL, TestFixtures.PASSWORD) }
        assertNull(store.token)
    }

    @Test
    fun `storage failure prevents successful login`() = runTest {
        json(TestFixtures.loginJson)
        store.failWrite = true
        expectError(AuthError.STORAGE) { auth.login(TestFixtures.EMAIL, TestFixtures.PASSWORD) }
        assertNull(store.token)
    }

    @Test
    fun `restoration sends bearer token and decodes integer booleans`() = runTest {
        store.token = TestFixtures.TOKEN
        json(TestFixtures.meJson)
        val user = auth.restoreSession()!!
        assertEquals(
            ApiExpectations.AUTHORIZATION_VALUE,
            server.takeRequest().getHeader(ApiExpectations.AUTHORIZATION_HEADER),
        )
        assertTrue(user.verified)
        assertEquals(listOf(TestUsers.WOMAN_WIRE), user.lookingFor)
    }

    @Test
    fun `unauthorized restoration clears stale token`() = runTest {
        store.token = TestFixtures.TOKEN
        json(ApiExpectations.EMPTY_JSON, 401)
        expectError(AuthError.SESSION_EXPIRED) { auth.restoreSession() }
        assertNull(store.token)
    }

    @Test
    fun `temporary server failure preserves session for retry`() = runTest {
        store.token = TestFixtures.TOKEN
        json(ApiExpectations.EMPTY_JSON, 503)
        expectError(AuthError.SERVICE) { auth.restoreSession() }
        assertEquals(TestUsers.TOKEN, store.token)
    }

    @Test
    fun `no saved session performs no request and logout clears it`() = runTest {
        assertNull(auth.restoreSession())
        assertEquals(0, server.requestCount)
        store.token = TestFixtures.TOKEN
        auth.logout()
        assertNull(store.token)
    }

    @Test
    fun `registration serializes deployed gender keys and persists session`() = runTest {
        json(TestFixtures.loginJson, 201)
        auth.register(
            TestFixtures.registration.copy(
                genderCustom = TestUsers.CUSTOM_IDENTITY,
                lookingFor = listOf(TestUsers.WOMAN_WIRE),
            )
        )
        val request = server.takeRequest()
        assertEquals(ApiExpectations.REGISTER_PATH, request.path)
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals(TestUsers.NON_BINARY_WIRE, body[ApiExpectations.GENDER_FIELD].asString)
        assertEquals(ApiExpectations.TERMS_VERSION, body[ApiExpectations.TERMS_FIELD].asString)
        assertEquals(TestUsers.CUSTOM_IDENTITY, body[ApiExpectations.CUSTOM_GENDER_FIELD].asString)
        assertEquals(
            TestUsers.WOMAN_WIRE,
            body[ApiExpectations.LOOKING_FOR_FIELD].asJsonArray[0].asString,
        )
        assertEquals(TestUsers.TOKEN, store.token)
    }

    @Test
    fun `minor registration never reaches API`() = runTest {
        expectError(AuthError.VALIDATION) {
            auth.register(TestFixtures.registration.copy(birthdate = TestUsers.MINOR_BIRTH_DATE))
        }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `likes send target id and map returned match`() = runTest {
        json(ContractResponses.LIKE_WITH_MATCH)
        assertEquals(
            TestConversations.MATCH_ID,
            ApiConnectionsRepository(api).like(TestUsers.PARTNER_ID),
        )
        val request = server.takeRequest()
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals(ApiExpectations.LIKE_PATH, request.path)
        assertEquals(TestUsers.PARTNER_ID, body[ApiExpectations.TARGET_USER_FIELD].asString)
        assertFalse(body[ApiExpectations.SUPER_LIKE_FIELD].asBoolean)
    }

    @Test
    fun `invalid messages are rejected before request`() = runTest {
        val messages = ApiMessagesRepository(api)
        expectError(AuthError.VALIDATION) { messages.send(TestConversations.MATCH_ID, " ") }
        expectError(AuthError.VALIDATION) {
            messages.send(TestConversations.MATCH_ID, "x".repeat(2001))
        }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `message pagination is explicit and chronological response is preserved`() = runTest {
        json(ContractResponses.MESSAGE_PAGE)
        val messages = ApiMessagesRepository(api).messages(TestConversations.MATCH_ID, 2)
        assertEquals(ApiExpectations.MESSAGE_PAGE_PATH, server.takeRequest().path)
        assertEquals(TestConversations.INCOMING_TEXT, messages.single().content)
        assertTrue(messages.single().read)
    }

    @Test
    fun `disabled external checkout makes no financial request`() = runTest {
        expectError(AuthError.FORBIDDEN) {
            ApiPlansRepository(api, false).checkout(TestPlans.PREMIUM_ID)
        }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `checkout rejects unsafe payment URLs`() = runTest {
        json(ContractResponses.CHECKOUT_UNSAFE_URL)
        expectError(AuthError.SERVICE) {
            ApiPlansRepository(api, true).checkout(TestPlans.BASIC_ID)
        }
    }

    @Test
    fun `subscription status comes from authenticated backend fields`() = runTest {
        json(ContractResponses.SUBSCRIPTION_APPROVED)
        val subscription = ApiPlansRepository(api, false).subscription()
        assertEquals(TestPlans.PLUS_ID, subscription.plan)
        assertEquals(123, subscription.minutesRemaining)
        assertEquals("approved", subscription.paymentStatus)
    }

    @Test
    fun `failed call eligibility never returns minutes`() = runTest {
        json(ContractResponses.CALL_FORBIDDEN)
        expectError(AuthError.FORBIDDEN) {
            ApiPlansRepository(api, false).checkCall(TestUsers.PARTNER_ID, CallType.AUDIO)
        }
        val request = server.takeRequest()
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals(
            ApiExpectations.AUDIO_CALL_WIRE,
            body[ApiExpectations.CALL_TYPE_FIELD].asString,
        )
    }

    @Test
    fun `report uses full server reason identifiers`() = runTest {
        json(ContractResponses.REPORT_CREATED)
        ApiSafetyRepository(api).report(TestUsers.PARTNER_ID, ReportReason.HATE, "Detalhes")
        val request = server.takeRequest()
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals(ApiExpectations.HATE_REASON_WIRE, body[ApiExpectations.REASON_FIELD].asString)
        assertEquals(TestUsers.PARTNER_ID, body[ApiExpectations.REPORTED_USER_FIELD].asString)
    }

    @Test
    fun `safety transport maps tips and blocked users into independent domain models`() = runTest {
        json(ContractResponses.SAFETY_TIPS)
        json(ContractResponses.BLOCKED_USERS)
        val safety = ApiSafetyRepository(api)
        assertEquals("Encontros seguros", safety.tips().single().title)
        assertEquals(
            BlockedUser(TestUsers.PARTNER_ID, TestUsers.PARTNER_NAME),
            safety.blocked().single(),
        )
    }

    @Test
    fun `API boundary preserves cancellation and maps network failure`() = runTest {
        try {
            apiCall<Unit> { throw CancellationException("cancelled") }
            fail("Expected cancellation")
        } catch (expected: CancellationException) {
            assertEquals("cancelled", expected.message)
        }
        expectError(AuthError.NETWORK) { apiCall<Unit> { throw IOException() } }
    }

    @Test
    fun `authenticated interceptor attaches token and reports expired session`() = runTest {
        store.token = TestFixtures.TOKEN
        var expired = 0
        val client =
            OkHttpClient.Builder()
                .addInterceptor(SessionAuthInterceptor(store) { expired++ })
                .build()
        val securedApi =
            Retrofit.Builder()
                .baseUrl(server.url(ApiExpectations.API_ROOT))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ContractApi::class.java)
        json(ApiExpectations.EMPTY_JSON, 401)
        expectError(AuthError.SESSION_EXPIRED) { apiCall { securedApi.current() } }
        assertEquals(1, expired)
        assertEquals(
            ApiExpectations.AUTHORIZATION_VALUE,
            server.takeRequest().getHeader(ApiExpectations.AUTHORIZATION_HEADER),
        )
    }

    @Test
    fun `interceptor storage failure becomes an observable error instead of worker crash`() =
        runTest {
            val brokenStore =
                object : SessionStore by store {
                    override suspend fun read(): String? = error("Storage unavailable")
                }
            val client =
                OkHttpClient.Builder()
                    .addInterceptor(SessionAuthInterceptor(brokenStore) {})
                    .build()
            val securedApi =
                Retrofit.Builder()
                    .baseUrl(server.url(ApiExpectations.API_ROOT))
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ContractApi::class.java)
            expectError(AuthError.STORAGE) { apiCall { securedApi.current() } }
            assertEquals(0, server.requestCount)
        }
}
