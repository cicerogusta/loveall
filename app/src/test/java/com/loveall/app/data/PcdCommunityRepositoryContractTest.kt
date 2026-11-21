package com.loveall.app.data

import com.google.gson.JsonParser
import com.loveall.app.TestFixtures
import com.loveall.app.data.remote.api.PcdCommunityApi
import com.loveall.app.data.repository.ApiPcdCommunityRepository
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PcdCommunityRepositoryContractTest {
    @Test
    fun `joining sends explicit consent and exit never reuses consent`() = runTest {
        MockWebServer().use { server ->
            val repository = repository(server)
            server.enqueue(json("""{"enabled":true}"""))
            assertTrue(repository.setMembership(true, true))
            val join = server.takeRequest()
            assertEquals("PUT", join.method)
            assertEquals("/profiles/me/pcd-community", join.path)
            val body = JsonParser.parseString(join.body.readUtf8()).asJsonObject
            assertTrue(body["enabled"].asBoolean)
            assertTrue(body["consent"].asBoolean)
            server.enqueue(json("""{"enabled":false}"""))
            assertFalse(repository.setMembership(false, false))
            val exit = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject
            assertFalse(exit["enabled"].asBoolean)
            assertFalse(exit["consent"].asBoolean)
        }
    }

    @Test
    fun `community likes use gated route instead of ordinary likes`() = runTest {
        MockWebServer().use { server ->
            val repository = repository(server)
            server.enqueue(json("""{"liked":true,"match":{"id":"match"}}"""))
            assertEquals("match", repository.like(TestFixtures.profile.id))
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/likes/pcd-like", request.path)
            assertEquals(
                TestFixtures.profile.id,
                JsonParser.parseString(request.body.readUtf8()).asJsonObject["to_user"].asString,
            )
        }
    }

    @Test
    fun `revoked membership surfaces forbidden instead of an empty success`() = runTest {
        MockWebServer().use { server ->
            val repository = repository(server)
            server.enqueue(MockResponse().setResponseCode(403))
            try {
                repository.discover()
                fail("Expected forbidden")
            } catch (error: AuthException) {
                assertEquals(AuthError.FORBIDDEN, error.error)
            }
        }
    }

    private fun repository(server: MockWebServer) =
        ApiPcdCommunityRepository(
            Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PcdCommunityApi::class.java)
        )

    private fun json(body: String) =
        MockResponse().setHeader("Content-Type", "application/json").setBody(body)
}
