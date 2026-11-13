package com.loveall.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loveall.app.domain.model.ReportReason
import com.loveall.app.domain.repository.ProfileRepository
import com.loveall.app.domain.repository.SafetyRepository
import com.loveall.app.presentation.state.DetailState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Carrega o perfil público de outra pessoa e coordena bloqueio e denúncia. Os sinais de conclusão
 * são publicados no estado; a decisão de navegar pertence à UI.
 */
class DetailViewModel(
    private val id: String,
    private val profiles: ProfileRepository,
    private val safety: SafetyRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(DetailState())
    val state = mutableState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (state.value.loading || state.value.busy) return
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val profile = profiles.profile(id)
                mutableState.update { it.copy(profile = profile) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(loading = false) }
            }
        }
    }

    fun block() {
        if (state.value.loading || state.value.busy) return
        mutableState.update { it.copy(blocking = true, error = null) }
        viewModelScope.launch {
            try {
                safety.block(id)
                mutableState.update { it.copy(blocked = true) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(blocking = false) }
            }
        }
    }

    fun report(reason: ReportReason, description: String) {
        if (state.value.loading || state.value.busy) return
        mutableState.update { it.copy(reporting = true, error = null) }
        viewModelScope.launch {
            try {
                safety.report(id, reason, description)
                mutableState.update { it.copy(reported = true) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(reporting = false) }
            }
        }
    }
}
