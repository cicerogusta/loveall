package com.loveall.app.data.repository

import com.loveall.app.data.apiCall
import com.loveall.app.data.remote.api.MessagesApi
import com.loveall.app.data.remote.dto.MessageDto
import com.loveall.app.data.remote.dto.MessageRequest
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.model.Message
import com.loveall.app.domain.repository.MessagesRepository
import com.loveall.app.domain.validation.Rules

class ApiMessagesRepository(private val api: MessagesApi) : MessagesRepository {
    override suspend fun messages(matchId: String, page: Int) = apiCall {
        api.messages(matchId, page).messages.map(MessageDto::toDomain)
    }

    override suspend fun send(matchId: String, content: String): Message {
        if (content.isBlank() || content.length > Rules.MAX_MESSAGE_LENGTH)
            throw AuthException(AuthError.VALIDATION)
        return apiCall { api.send(matchId, MessageRequest(content.trim())).message.toDomain() }
    }
}
