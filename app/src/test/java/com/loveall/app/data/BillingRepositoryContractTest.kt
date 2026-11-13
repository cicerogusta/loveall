package com.loveall.app.data

import com.google.gson.JsonParser
import com.loveall.app.data.remote.api.BillingApi
import com.loveall.app.data.repository.ApiBillingRepository
import com.loveall.app.domain.error.*
import com.loveall.app.fixtures.*
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BillingRepositoryContractTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: ApiBillingRepository

    @Before
    fun setup() {
        server = MockWebServer().apply { start() }
        repository =
            ApiBillingRepository(
                Retrofit.Builder()
                    .baseUrl(server.url(ApiExpectations.API_ROOT))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(BillingApi::class.java)
            )
    }

    @After
    fun cleanup() {
        server.shutdown()
    }

    @Test
    fun `configuration loads product allowlist and obfuscated account id`() = runTest {
        json(TestMonetization.CONFIG_JSON)
        assertEquals(TestMonetization.config, repository.configuration())
        assertEquals(TestMonetization.CONFIG_PATH, server.takeRequest().path)
    }

    @Test
    fun `verification sends Google token and product only and reads server entitlement`() =
        runTest {
            json(TestMonetization.ACTIVE_JSON)
            assertEquals(TestMonetization.active, repository.verify(TestMonetization.owned))
            val request = server.takeRequest()
            assertEquals(TestMonetization.VERIFY_PATH, request.path)
            assertEquals(ApiExpectations.POST_METHOD, request.method)
            val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
            assertEquals(TestMonetization.PRODUCT, body[TestMonetization.PRODUCT_FIELD].asString)
            assertEquals(
                TestMonetization.PURCHASE_TOKEN,
                body[TestMonetization.TOKEN_FIELD].asString,
            )
            assertEquals(2, body.size())
        }

    @Test
    fun `entitlement without valid expiry cannot activate the native client`() = runTest {
        json(TestMonetization.INVALID_ACTIVE_JSON)
        try {
            repository.entitlement()
            fail()
        } catch (error: AuthException) {
            assertEquals(AuthError.SERVICE, error.error)
        }
    }

    @Test
    fun `missing entitlement field never implies a free account eligible for ads`() = runTest {
        json(ApiExpectations.EMPTY_JSON)
        try {
            repository.entitlement()
            fail()
        } catch (error: AuthException) {
            assertEquals(AuthError.SERVICE, error.error)
        }
    }

    @Test
    fun `disabled configuration exposes no products or account id`() = runTest {
        json(TestMonetization.DISABLED_JSON)
        val configuration = repository.configuration()
        assertFalse(configuration.enabled)
        assertTrue(configuration.productIds.isEmpty())
        assertNull(configuration.accountId)
    }

    private fun json(value: String) {
        server.enqueue(
            MockResponse()
                .setHeader(ApiExpectations.CONTENT_TYPE_HEADER, ApiExpectations.JSON_CONTENT_TYPE)
                .setBody(value)
        )
    }
}
