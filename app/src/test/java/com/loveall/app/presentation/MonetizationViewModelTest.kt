package com.loveall.app.presentation

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.StoreUpdate
import com.loveall.app.fake.*
import com.loveall.app.fixtures.*
import com.loveall.app.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MonetizationViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val api = FakeBillingRepository()
    private val store = FakeSubscriptionStore()

    @Test
    fun `disabled monetization performs no network or store work`() =
        runTest(main.dispatcher) {
            val vm = MonetizationViewModel(api, store, false, false)
            vm.bindAccount(TestUsers.SELF_ID)
            advanceUntilIdle()
            assertEquals(0, api.configurationCalls)
            assertFalse(vm.state.value.configured)
            assertFalse(vm.state.value.entitlementKnown)
        }

    @Test
    fun `offers use store prices and obfuscated account id is passed to purchase`() =
        runTest(main.dispatcher) {
            val vm = ready()
            vm.purchase(TestMonetization.offer)
            advanceUntilIdle()
            assertEquals(TestMonetization.OFFER_ID, store.selected)
            assertEquals(TestMonetization.ACCOUNT_ID, store.account)
            assertEquals(TestMonetization.offer.price, vm.state.value.offers.single().price)
        }

    @Test
    fun `store callback never grants benefits without server confirmation`() =
        runTest(main.dispatcher) {
            val vm = ready()
            store.updates.emit(StoreUpdate(listOf(TestMonetization.owned)))
            advanceUntilIdle()
            assertEquals(1, api.verificationCalls)
            assertFalse(vm.state.value.entitlement.active)
        }

    @Test
    fun `confirmed restoration activates ad free access and blocks another purchase`() =
        runTest(main.dispatcher) {
            val vm = ready()
            store.owned = listOf(TestMonetization.owned)
            api.current = TestMonetization.active
            vm.restore()
            advanceUntilIdle()
            assertTrue(vm.state.value.entitlement.active)
            vm.purchase(TestMonetization.offer)
            advanceUntilIdle()
            assertNull(store.selected)
        }

    @Test
    fun `pending purchase stays pending and does not activate locally`() =
        runTest(main.dispatcher) {
            val vm = ready()
            store.updates.emit(StoreUpdate(listOf(TestMonetization.owned.copy(pending = true))))
            advanceUntilIdle()
            assertTrue(vm.state.value.pending)
            assertFalse(vm.state.value.entitlement.active)
            vm.purchase(TestMonetization.offer)
            advanceUntilIdle()
            assertNull(store.selected)
        }

    @Test
    fun `verification failure prevents ad requests until recovery`() =
        runTest(main.dispatcher) {
            val vm = ready()
            api.failure = AuthError.NETWORK
            store.updates.emit(StoreUpdate(listOf(TestMonetization.owned)))
            advanceUntilIdle()
            assertFalse(vm.state.value.entitlementKnown)
            assertEquals(AuthError.NETWORK, vm.state.value.error)
            api.failure = null
            vm.refresh()
            advanceUntilIdle()
            assertTrue(vm.state.value.entitlementKnown)
        }

    @Test
    fun `logging out cancels a pending verification and clears paid account state`() =
        runTest(main.dispatcher) {
            val vm = ready()
            api.verifyGate = CompletableDeferred()
            store.updates.emit(StoreUpdate(listOf(TestMonetization.owned)))
            runCurrent()
            vm.bindAccount(null)
            api.current = TestMonetization.active
            api.verifyGate!!.complete(Unit)
            advanceUntilIdle()
            assertFalse(vm.state.value.entitlement.active)
            assertFalse(vm.state.value.entitlementKnown)
        }

    @Test
    fun `already owned event restores instead of initiating another sale`() =
        runTest(main.dispatcher) {
            val vm = ready()
            val calls = store.restoreCalls
            store.updates.emit(StoreUpdate(restoreRequired = true))
            advanceUntilIdle()
            assertEquals(calls + 1, store.restoreCalls)
            assertFalse(vm.state.value.busy)
        }

    @Test
    fun `user cancellation is not shown as a purchase failure`() =
        runTest(main.dispatcher) {
            val vm = ready()
            store.updates.emit(StoreUpdate(cancelled = true))
            advanceUntilIdle()
            assertNull(vm.state.value.error)
            assertFalse(vm.state.value.busy)
        }

    private suspend fun TestScope.ready(): MonetizationViewModel {
        val vm = MonetizationViewModel(api, store, true, true)
        vm.bindAccount(TestUsers.SELF_ID)
        advanceUntilIdle()
        return vm
    }
}
