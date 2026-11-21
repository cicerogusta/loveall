package com.loveall.app.fixtures

import java.util.concurrent.CopyOnWriteArrayList
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

internal class NativeApiDispatcher : Dispatcher() {
    var failLogin = false
    var requireConsent = false
    var pcdMember = false
    val membershipChanges = CopyOnWriteArrayList<String>()
    val deletions = CopyOnWriteArrayList<String>()
    val sentBodies = CopyOnWriteArrayList<String>()
    val registrations = CopyOnWriteArrayList<String>()

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.requestUrl!!.encodedPath
        if (path == NativeApiContract.AUTH_LOGIN && failLogin)
            return json(NativeApiContract.EMPTY_BODY, 401)
        if (
            path != NativeApiContract.AUTH_LOGIN &&
                path != NativeApiContract.AUTH_REGISTER &&
                request.getHeader(NativeApiContract.AUTHORIZATION_HEADER) !=
                    NativeApiContract.EXPECTED_AUTHORIZATION
        )
            return json(NativeApiContract.EMPTY_BODY, 401)
        return when {
            path == NativeApiContract.PCD_MEMBERSHIP -> {
                if (request.method == NativeApiContract.PUT_METHOD) {
                    val body = request.body.readUtf8()
                    membershipChanges.add(body)
                    pcdMember =
                        com.google.gson.JsonParser.parseString(body)
                            .asJsonObject[NativeApiContract.ENABLED_FIELD]
                            .asBoolean
                }
                val response =
                    com.google.gson.JsonObject().apply {
                        addProperty(NativeApiContract.ENABLED_FIELD, pcdMember)
                    }
                json(response.toString())
            }
            path == NativeApiContract.PCD_DISCOVER ->
                if (pcdMember) json(NativeResponses.DISCOVER)
                else json(NativeApiContract.EMPTY_BODY, 403)
            path == NativeApiContract.AUTH_LOGIN -> {
                val response =
                    com.google.gson.JsonParser.parseString(NativeResponses.LOGIN).asJsonObject
                if (requireConsent)
                    response
                        .getAsJsonObject("user")
                        .addProperty(NativeApiContract.TERMS_FIELD, null as String?)
                json(response.toString())
            }
            path == NativeApiContract.AUTH_REGISTER -> {
                registrations.add(request.body.readUtf8())
                json(NativeResponses.LOGIN, 201)
            }
            path == NativeApiContract.AUTH_TERMS -> json(NativeResponses.CURRENT_USER)
            path == NativeApiContract.AUTH_ME &&
                request.method == NativeApiContract.DELETE_METHOD -> {
                val body = request.body.readUtf8()
                deletions.add(body)
                if (
                    com.google.gson.JsonParser.parseString(body)
                        .asJsonObject[NativeApiContract.PASSWORD_FIELD]
                        .asString == NativeScenario.PASSWORD
                )
                    MockResponse().setResponseCode(204)
                else json(NativeApiContract.EMPTY_BODY, 403)
            }
            path == NativeApiContract.AUTH_ME -> json(NativeResponses.CURRENT_USER)
            path == NativeApiContract.DISCOVER -> json(NativeResponses.DISCOVER)
            path == NativeApiContract.INCOMING_LIKES -> json(NativeResponses.INCOMING_LIKES)
            path == NativeApiContract.LIKE -> json(NativeResponses.LIKE_WITH_MATCH)
            path == NativeApiContract.MATCHES -> json(NativeResponses.MATCHES)
            path == NativeApiContract.MATCH_MESSAGES &&
                request.method == NativeApiContract.POST_METHOD -> {
                sentBodies.add(request.body.readUtf8())
                json(
                    NativeResponses.MESSAGE_CREATED,
                    201,
                )
            }
            path == NativeApiContract.MATCH_MESSAGES -> json(NativeResponses.MESSAGES)
            path == NativeApiContract.SAFETY_TIPS -> json(NativeResponses.SAFETY_TIPS)
            path == NativeApiContract.BLOCKED -> json(NativeResponses.BLOCKED_EMPTY)
            path == NativeApiContract.REPORT -> json(NativeResponses.REPORT_CREATED)
            path == NativeApiContract.PLANS -> json(NativeResponses.PLANS)
            path == NativeApiContract.SUBSCRIPTION -> json(NativeResponses.SUBSCRIPTION)
            else -> json(NativeApiContract.EMPTY_BODY, 404)
        }
    }

    private fun json(body: String, status: Int = 200) =
        MockResponse()
            .setResponseCode(status)
            .setHeader(NativeApiContract.CONTENT_TYPE_HEADER, NativeApiContract.JSON_CONTENT_TYPE)
            .setBody(body)
}
