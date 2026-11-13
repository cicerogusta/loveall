package com.loveall.app.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loveall.app.domain.model.Plan
import com.loveall.app.domain.repository.PlansRepository
import com.loveall.app.presentation.state.PlansState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Acompanha ofertas, assinatura e uma transação de checkout pendente. O identificador pendente é
 * salvo no SavedStateHandle para restauração pelo Android; a confirmação de benefícios continua
 * vindo da API.
 */
class PlansViewModel(private val plans: PlansRepository, private val savedState: SavedStateHandle) :
    ViewModel() {
    private val mutableState =
        MutableStateFlow(PlansState(pendingTransaction = savedState[TRANSACTION]))
    val state = mutableState.asStateFlow()

    init {
        refresh()
    }

    /** Consulta as ofertas e o saldo atual da assinatura antes de atualizar a tela. */
    fun refresh() {
        if (state.value.loading || state.value.busy) return
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val available = plans.plans()
                val subscription = plans.subscription()
                mutableState.update { it.copy(plans = available, subscription = subscription) }
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
     * Solicita uma nova transação quando não há outra pendente. O endereço recebido é publicado
     * para o efeito de UI abrir o navegador, sem conceder assinatura localmente.
     */
    fun checkout(plan: Plan) {
        if (state.value.loading || state.value.busy || state.value.pendingTransaction != null)
            return
        mutableState.update { it.copy(startingCheckout = true, error = null) }
        viewModelScope.launch {
            try {
                val checkout = plans.checkout(plan.id)
                savedState[TRANSACTION] = checkout.transactionId
                mutableState.update {
                    it.copy(
                        checkout = checkout,
                        pendingTransaction = checkout.transactionId,
                        payment = null,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(startingCheckout = false) }
            }
        }
    }

    /**
     * Consulta o pagamento e recarrega a assinatura do backend. Somente um estado terminal remove o
     * identificador pendente; UNKNOWN permite nova consulta.
     */
    fun checkPayment() {
        if (state.value.loading || state.value.busy || state.value.pendingTransaction == null)
            return
        mutableState.update { it.copy(checkingPayment = true, error = null) }
        viewModelScope.launch {
            try {
                val transaction = state.value.pendingTransaction ?: return@launch
                val result = plans.paymentStatus(transaction)
                val subscription = plans.subscription()
                val terminal = result.status.isTerminal
                if (terminal) savedState.remove<String>(TRANSACTION)
                mutableState.update {
                    it.copy(
                        payment = result,
                        subscription = subscription,
                        pendingTransaction = if (terminal) null else transaction,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update { it.copy(error = error.toUiError()) }
            } finally {
                mutableState.update { it.copy(checkingPayment = false) }
            }
        }
    }

    /**
     * Consome a solicitação de abrir o navegador para não repeti-la em recomposições. O
     * identificador pendente permanece até a consulta de pagamento indicar um estado terminal.
     */
    fun checkoutOpened() {
        mutableState.update { it.copy(checkout = null) }
    }

    private companion object {
        const val TRANSACTION = "pending_transaction"
    }
}
