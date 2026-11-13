package com.loveall.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loveall.app.domain.repository.ConnectionsRepository
import com.loveall.app.presentation.state.MatchesState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Carrega a lista de conexões confirmadas pelo backend. loading evita consultas sobrepostas; o
 * estado mantém os dados anteriores caso uma recarga falhe.
 */
class MatchesViewModel(private val connections: ConnectionsRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(MatchesState())
    val state = mutableState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (state.value.loading) return
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val matches = connections.matches()
                mutableState.update { it.copy(matches = matches) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(loading = false) }
            }
        }
    }
}
