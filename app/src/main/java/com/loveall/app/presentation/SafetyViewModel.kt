package com.loveall.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loveall.app.domain.repository.SafetyRepository
import com.loveall.app.presentation.state.SafetyState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Consulta orientações de segurança e pessoas bloqueadas. O identificador unblockingId representa a
 * ação em curso; a remoção local aguarda confirmação da API.
 */
class SafetyViewModel(private val safety: SafetyRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(SafetyState())
    val state = mutableState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (state.value.loading || state.value.busy) return
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val tips = safety.tips()
                val blocked = safety.blocked()
                mutableState.update { it.copy(tips = tips, blocked = blocked) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(loading = false) }
            }
        }
    }

    fun unblock(id: String) {
        if (state.value.loading || state.value.busy) return
        mutableState.update { it.copy(unblockingId = id, error = null) }
        viewModelScope.launch {
            try {
                safety.unblock(id)
                mutableState.update {
                    it.copy(blocked = it.blocked.filterNot { user -> user.id == id })
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(unblockingId = null) }
            }
        }
    }
}
