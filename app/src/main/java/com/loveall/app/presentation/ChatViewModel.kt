package com.loveall.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.model.CallType
import com.loveall.app.domain.model.ReportReason
import com.loveall.app.domain.repository.ConnectionsRepository
import com.loveall.app.domain.repository.MessagesRepository
import com.loveall.app.domain.repository.PlansRepository
import com.loveall.app.domain.repository.SafetyRepository
import com.loveall.app.domain.usecase.MessageTimeline
import com.loveall.app.domain.validation.Rules
import com.loveall.app.presentation.state.ChatState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Coordena a conversa identificada por matchId, usando contratos de mensagens e conexões. Envio,
 * atualização periódica e histórico têm controles separados: uma consulta em andamento não deve
 * impedir o envio nem substituir mensagens recebidas por outra operação.
 */
class ChatViewModel(
    val matchId: String,
    private val messages: MessagesRepository,
    private val connections: ConnectionsRepository,
    private val safety: SafetyRepository,
    private val plans: PlansRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ChatState())
    val state = mutableState.asStateFlow()

    init {
        load()
    }

    /**
     * Localiza a pessoa participante e carrega a primeira página. A quantidade retornada indica se
     * pode existir uma página anterior.
     */
    fun load() {
        if (
            state.value.loading ||
                state.value.busy ||
                state.value.refreshing ||
                state.value.loadingOlder
        )
            return
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val partner =
                    connections.matches().firstOrNull { it.id == matchId }?.partner
                        ?: throw AuthException(AuthError.NOT_FOUND)
                val result = messages.messages(matchId, 1)
                mutableState.update {
                    it.copy(
                        partner = partner,
                        messages = MessageTimeline.merge(emptyList(), result),
                        page = 1,
                        hasOlder = result.size == Rules.MESSAGE_PAGE_SIZE,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(loading = false) }
            }
        }
    }

    /**
     * Consulta mensagens recentes e mescla o resultado com o estado mais novo disponível. Se a
     * carga inicial falhou, repete essa carga para recuperar também a pessoa participante.
     */
    fun refresh() {
        if (state.value.partner == null) {
            load()
            return
        }
        if (state.value.loading || state.value.refreshing || state.value.closed) return
        mutableState.update { it.copy(refreshing = true, error = null) }
        viewModelScope.launch {
            try {
                val result = messages.messages(matchId, 1)
                mutableState.update {
                    it.copy(messages = MessageTimeline.merge(it.messages, result))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(refreshing = false) }
            }
        }
    }

    /**
     * Busca a próxima página de histórico sem apagar as mensagens já exibidas. O índice de página
     * só avança quando a consulta termina com sucesso.
     */
    fun older() {
        if (
            state.value.loading ||
                state.value.busy ||
                state.value.loadingOlder ||
                !state.value.hasOlder ||
                state.value.closed
        )
            return
        mutableState.update { it.copy(loadingOlder = true, error = null) }
        viewModelScope.launch {
            try {
                if (!state.value.hasOlder || state.value.closed) return@launch
                val next = state.value.page + 1
                val result = messages.messages(matchId, next)
                mutableState.update {
                    it.copy(
                        messages = MessageTimeline.merge(it.messages, result),
                        page = next,
                        hasOlder = result.size == Rules.MESSAGE_PAGE_SIZE,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(loadingOlder = false) }
            }
        }
    }

    /**
     * Acrescenta apenas a mensagem confirmada pelo servidor e publica a confirmação do rascunho. Em
     * caso de erro, não cria uma mensagem fictícia nem solicita que a UI limpe o texto.
     */
    fun send(content: String) {
        if (
            state.value.loading ||
                state.value.busy ||
                state.value.closed ||
                state.value.partner == null
        )
            return
        mutableState.update { it.copy(sending = true, error = null) }
        viewModelScope.launch {
            try {
                val message = messages.send(matchId, content)
                mutableState.update {
                    it.copy(
                        messages = MessageTimeline.merge(it.messages, listOf(message)),
                        sentDraft = content,
                        sendVersion = it.sendVersion + 1,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(sending = false) }
            }
        }
    }

    /** Bloqueia a pessoa participante e sinaliza fechamento após a confirmação da API. */
    fun block() {
        if (
            state.value.loading ||
                state.value.busy ||
                state.value.closed ||
                state.value.partner == null
        )
            return
        mutableState.update { it.copy(moderating = true, error = null) }
        viewModelScope.launch {
            try {
                safety.block(requirePartner().id)
                mutableState.update { it.copy(closed = true) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(moderating = false) }
            }
        }
    }

    /** Desfaz a conexão no servidor e só então sinaliza que a conversa deve ser fechada. */
    fun unmatch() {
        if (
            state.value.loading ||
                state.value.busy ||
                state.value.closed ||
                state.value.partner == null
        )
            return
        mutableState.update { it.copy(moderating = true, error = null) }
        viewModelScope.launch {
            try {
                connections.unmatch(matchId)
                mutableState.update { it.copy(closed = true) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(moderating = false) }
            }
        }
    }

    /** Envia a denúncia e publica o sinal usado pelo diálogo de confirmação da interface. */
    fun report(reason: ReportReason, description: String) {
        if (
            state.value.loading ||
                state.value.busy ||
                state.value.closed ||
                state.value.partner == null
        )
            return
        mutableState.update { it.copy(moderating = true, error = null) }
        viewModelScope.launch {
            try {
                safety.report(requirePartner().id, reason, description)
                mutableState.update { it.copy(reported = true) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(moderating = false) }
            }
        }
    }

    /**
     * Consulta elegibilidade e saldo no servidor. Esta operação não estabelece uma chamada de áudio
     * ou vídeo; a UI apresenta o resultado da consulta.
     */
    fun checkCall(type: CallType) {
        if (
            state.value.loading ||
                state.value.busy ||
                state.value.closed ||
                state.value.partner == null
        )
            return
        mutableState.update { it.copy(checkingCall = true, error = null) }
        viewModelScope.launch {
            try {
                val minutes = plans.checkCall(requirePartner().id, type)
                mutableState.update { it.copy(eligibleMinutes = minutes) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(checkingCall = false) }
            }
        }
    }

    fun dismissFeedback() {
        mutableState.update { it.copy(reported = false, eligibleMinutes = null) }
    }

    private fun requirePartner() = state.value.partner ?: throw AuthException(AuthError.NOT_FOUND)
}
