package com.loveall.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loveall.app.domain.model.Profile
import com.loveall.app.domain.repository.ConnectionsRepository
import com.loveall.app.domain.repository.ProfileRepository
import com.loveall.app.domain.repository.SafetyRepository
import com.loveall.app.presentation.state.ExploreState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Coordena a fila de perfis e as ações de descoberta. O mesmo fluxo pode consultar descoberta ou
 * curtidas recebidas, conforme incomingLikes. O perfil só é removido da fila depois que a API
 * confirma a ação.
 */
class ExploreViewModel(
    private val profiles: ProfileRepository,
    private val connections: ConnectionsRepository,
    private val safety: SafetyRepository,
    private val incomingLikes: Boolean = false,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ExploreState())
    val state = mutableState.asStateFlow()

    init {
        refresh()
    }

    /** Carrega a lista correspondente ao contexto da rota, preservando o controle de operação. */
    fun refresh() {
        if (state.value.loading || state.value.busy) return
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val result = if (incomingLikes) connections.likes() else profiles.discover()
                mutableState.update { it.copy(profiles = result) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(loading = false) }
            }
        }
    }

    /** Registra a curtida; se o backend retornar um match, publica seus dados para o diálogo. */
    fun like(profile: Profile) {
        if (state.value.loading || state.value.busy) return
        mutableState.update { it.copy(processingProfileId = profile.id, error = null) }
        viewModelScope.launch {
            try {
                val matchId = connections.like(profile.id)
                mutableState.update {
                    it.copy(
                        profiles =
                            it.profiles.filterNot { candidate -> candidate.id == profile.id },
                        matchId = matchId,
                        matchedName = if (matchId != null) profile.name else null,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(processingProfileId = null) }
            }
        }
    }

    /** Registra a decisão de passar e remove o perfil após a resposta de sucesso. */
    fun pass(profile: Profile) {
        if (state.value.loading || state.value.busy) return
        mutableState.update { it.copy(processingProfileId = profile.id, error = null) }
        viewModelScope.launch {
            try {
                connections.pass(profile.id)
                mutableState.update {
                    it.copy(
                        profiles = it.profiles.filterNot { candidate -> candidate.id == profile.id }
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(processingProfileId = null) }
            }
        }
    }

    /** Confirma o bloqueio no servidor antes de retirar o perfil da lista local. */
    fun block(profile: Profile) {
        if (state.value.loading || state.value.busy) return
        mutableState.update { it.copy(processingProfileId = profile.id, error = null) }
        viewModelScope.launch {
            try {
                safety.block(profile.id)
                mutableState.update {
                    it.copy(
                        profiles = it.profiles.filterNot { candidate -> candidate.id == profile.id }
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(processingProfileId = null) }
            }
        }
    }

    fun dismissMatch() {
        mutableState.update { it.copy(matchId = null, matchedName = null) }
    }
}
