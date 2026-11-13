package com.loveall.app.presentation.state

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.Message
import com.loveall.app.domain.model.Profile

/**
 * Fotografia corrente de uma conversa, compartilhada somente para leitura com a UI. As flags
 * representam operações independentes; atualizar mensagens não ocupa o envio.
 *
 * @property messages histórico acumulado, mesclado e ordenado por MessageTimeline.
 * @property partner pessoa associada ao match; permanece nula se a carga inicial não concluiu.
 * @property page última página de histórico carregada com sucesso.
 * @property hasOlder indica se o tamanho da resposta permite tentar mais uma página.
 * @property closed sinaliza conclusão de bloqueio ou desfazer conexão, usada pela UI para voltar.
 * @property sentDraft texto associado ao último envio confirmado, para limpar o rascunho correto.
 * @property sendVersion contador de confirmações, observado pelo efeito do compositor.
 * @property refreshing consulta de mensagens recentes em andamento.
 * @property loadingOlder consulta de uma página anterior em andamento.
 * @property eligibleMinutes resultado da consulta de elegibilidade, sem iniciar uma chamada.
 */
data class ChatState(
    val messages: List<Message> = emptyList(),
    val partner: Profile? = null,
    val page: Int = 1,
    val hasOlder: Boolean = true,
    val closed: Boolean = false,
    val reported: Boolean = false,
    val eligibleMinutes: Int? = null,
    val sentDraft: String? = null,
    val sendVersion: Long = 0,
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val sending: Boolean = false,
    val loadingOlder: Boolean = false,
    val moderating: Boolean = false,
    val checkingCall: Boolean = false,
    val error: AuthError? = null,
) {
    val busy: Boolean
        get() = sending || moderating || checkingCall
}
