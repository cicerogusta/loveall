package com.loveall.app.presentation.state

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.Match

/**
 * Lista de conexões confirmadas e situação da consulta. Uma recarga pode manter a lista anterior
 * enquanto loading ou error representam o resultado da tentativa.
 */
data class MatchesState(
    val matches: List<Match> = emptyList(),
    val loading: Boolean = false,
    val error: AuthError? = null,
)
