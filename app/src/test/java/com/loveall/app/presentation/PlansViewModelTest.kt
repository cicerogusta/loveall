package com.loveall.app.presentation

import androidx.lifecycle.SavedStateHandle
import com.loveall.app.TestFixtures
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.PaymentState
import com.loveall.app.fake.FakePlansRepository
import com.loveall.app.fixtures.TestPlans
import com.loveall.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlansViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val dispatcher
        get() = mainDispatcherRule.dispatcher

    private val plan = TestFixtures.basicPlan

    @Test
    fun `double checkout tap creates one transaction and retains it for restoration`() =
        runTest(dispatcher) {
            val repository = FakePlansRepository()
            val saved = SavedStateHandle()
            val vm = PlansViewModel(repository, saved)
            advanceUntilIdle()
            vm.checkout(plan)
            vm.checkout(plan)
            advanceUntilIdle()
            assertEquals(1, repository.checkoutCalls)
            assertEquals(TestPlans.TRANSACTION_ID, vm.state.value.pendingTransaction)
            assertEquals(
                TestPlans.TRANSACTION_ID,
                saved.get<String>(TestPlans.SAVED_TRANSACTION_KEY),
            )
            assertEquals(TestPlans.FREE_ID, vm.state.value.subscription?.plan)
        }

    @Test
    fun `pending purchase never grants paid entitlement`() =
        runTest(dispatcher) {
            val repository = FakePlansRepository()
            val vm =
                PlansViewModel(
                    repository,
                    SavedStateHandle(
                        mapOf(TestPlans.SAVED_TRANSACTION_KEY to TestPlans.TRANSACTION_ID)
                    ),
                )
            advanceUntilIdle()
            vm.checkPayment()
            advanceUntilIdle()
            assertEquals(PaymentState.PENDING, vm.state.value.payment?.status)
            assertEquals(TestPlans.FREE_ID, vm.state.value.subscription?.plan)
            assertEquals(0, vm.state.value.subscription?.minutesRemaining)
            assertEquals(TestPlans.TRANSACTION_ID, vm.state.value.pendingTransaction)
        }

    @Test
    fun `approved status alone never overrides authoritative subscription`() =
        runTest(dispatcher) {
            val repository = FakePlansRepository().apply { status = PaymentState.APPROVED }
            val vm =
                PlansViewModel(
                    repository,
                    SavedStateHandle(
                        mapOf(TestPlans.SAVED_TRANSACTION_KEY to TestPlans.TRANSACTION_ID)
                    ),
                )
            advanceUntilIdle()
            vm.checkPayment()
            advanceUntilIdle()
            assertEquals(PaymentState.APPROVED, vm.state.value.payment?.status)
            assertEquals(TestPlans.FREE_ID, vm.state.value.subscription?.plan)
            assertNull(vm.state.value.pendingTransaction)
        }

    @Test
    fun `failed checkout preserves free plan and never saves a transaction`() =
        runTest(dispatcher) {
            val repository = FakePlansRepository().apply { error = AuthError.NETWORK }
            val vm = PlansViewModel(repository, SavedStateHandle())
            advanceUntilIdle()
            vm.checkout(plan)
            advanceUntilIdle()
            assertEquals(AuthError.NETWORK, vm.state.value.error)
            assertNull(vm.state.value.pendingTransaction)
            assertNull(vm.state.value.checkout)
            assertEquals(TestPlans.FREE_ID, vm.state.value.subscription?.plan)
        }

    @Test
    fun `failed verification keeps pending transaction for retry`() =
        runTest(dispatcher) {
            val repository = FakePlansRepository().apply { error = AuthError.NETWORK }
            val vm =
                PlansViewModel(
                    repository,
                    SavedStateHandle(
                        mapOf(TestPlans.SAVED_TRANSACTION_KEY to TestPlans.TRANSACTION_ID)
                    ),
                )
            advanceUntilIdle()
            vm.checkPayment()
            advanceUntilIdle()
            assertEquals(TestPlans.TRANSACTION_ID, vm.state.value.pendingTransaction)
            assertNull(vm.state.value.payment)
            assertEquals(TestPlans.FREE_ID, vm.state.value.subscription?.plan)
        }

    @Test
    fun `rejected refunded and revoked purchases are terminal and do not grant minutes`() =
        runTest(dispatcher) {
            listOf(
                    PaymentState.REJECTED,
                    PaymentState.CANCELLED,
                    PaymentState.REFUNDED,
                    PaymentState.CHARGED_BACK,
                )
                .forEach { status ->
                    val repository = FakePlansRepository().apply { this.status = status }
                    val vm =
                        PlansViewModel(
                            repository,
                            SavedStateHandle(
                                mapOf(TestPlans.SAVED_TRANSACTION_KEY to TestPlans.TRANSACTION_ID)
                            ),
                        )
                    advanceUntilIdle()
                    vm.checkPayment()
                    advanceUntilIdle()
                    assertNull(vm.state.value.pendingTransaction)
                    assertEquals(0, vm.state.value.subscription?.minutesRemaining)
                }
        }
}
