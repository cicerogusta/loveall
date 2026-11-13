package com.loveall.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loveall.app.domain.model.*
import com.loveall.app.domain.repository.*
import com.loveall.app.presentation.state.MonetizationState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/** A Play coleta o pagamento; apenas a API confirma o direito de remover anúncios. */
class MonetizationViewModel(
    private val billing: BillingRepository,
    private val store: SubscriptionStore,
    private val billingEnabled: Boolean,
    private val adsEnabled: Boolean,
) : ViewModel() {
    private val mutableState = MutableStateFlow(MonetizationState())
    val state = mutableState.asStateFlow()
    private var account: String? = null
    private var configuration = BillingConfiguration()
    private var operation: Job? = null

    init {
        viewModelScope.launch {
            store.updates.collect { update ->
                val owner = account ?: return@collect
                operation?.join()
                if (account != owner || !state.value.configured) return@collect
                when {
                    update.cancelled -> mutableState.update { it.copy(error = null) }
                    update.restoreRequired -> restore()
                    update.error != null -> mutableState.update { it.copy(error = update.error) }
                    else -> verify(update.purchases)
                }
            }
        }
    }

    fun bindAccount(userId: String?) {
        if (account == userId) return
        operation?.cancel()
        account = userId
        configuration = BillingConfiguration()
        mutableState.value = MonetizationState()
        refresh()
    }

    fun refresh() {
        if (account == null || (!billingEnabled && !adsEnabled) || state.value.busy) return
        runOperation {
            val loadedConfiguration = billing.configuration()
            currentCoroutineContext().ensureActive()
            configuration = loadedConfiguration
            val current = billing.entitlement()
            currentCoroutineContext().ensureActive()
            mutableState.update {
                it.copy(
                    entitlement = current,
                    entitlementKnown = true,
                    configured = billingEnabled && configuration.enabled,
                )
            }
            if (billingEnabled && configuration.enabled) {
                // Restaurar também ao voltar ao app recupera compras concluídas fora do processo.
                verifyPurchases(store.restore())
                val offers = store.offers(configuration.productIds)
                currentCoroutineContext().ensureActive()
                mutableState.update { it.copy(offers = offers) }
            }
        }
    }

    fun restore() {
        if (!state.value.configured || state.value.busy) return
        runOperation { verifyPurchases(store.restore()) }
    }

    fun purchase(offer: SubscriptionOffer) {
        if (
            !state.value.configured ||
                !state.value.entitlementKnown ||
                state.value.pending ||
                state.value.busy ||
                state.value.entitlement.active ||
                offer !in state.value.offers
        )
            return
        val accountId = configuration.accountId ?: return
        runOperation { store.purchase(offer.id, accountId) }
    }

    private fun verify(purchases: List<StorePurchase>) {
        runOperation { verifyPurchases(purchases) }
    }

    private suspend fun verifyPurchases(purchases: List<StorePurchase>) {
        var pending = false
        if (purchases.any { it.pending }) mutableState.update { it.copy(pending = true) }
        for (purchase in purchases.filter { it.productId in configuration.productIds }) {
            pending = pending || purchase.pending
            billing.verify(purchase)
            currentCoroutineContext().ensureActive()
        }
        val confirmed = billing.entitlement()
        currentCoroutineContext().ensureActive()
        mutableState.update {
            it.copy(entitlement = confirmed, entitlementKnown = true, pending = pending)
        }
    }

    private fun runOperation(work: suspend () -> Unit) {
        val owner = account ?: return
        mutableState.update { it.copy(busy = true, error = null) }
        operation = viewModelScope.launch {
            try {
                work()
                currentCoroutineContext().ensureActive()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (account == owner)
                    mutableState.update {
                        it.copy(error = error.toUiError(), entitlementKnown = false)
                    }
            } finally {
                if (account == owner) mutableState.update { it.copy(busy = false) }
            }
        }
    }
}
