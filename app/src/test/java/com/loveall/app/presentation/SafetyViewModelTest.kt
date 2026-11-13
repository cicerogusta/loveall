package com.loveall.app.presentation

import com.loveall.app.fake.*
import com.loveall.app.fixtures.TestUsers
import com.loveall.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SafetyViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val dispatcher
        get() = mainDispatcherRule.dispatcher

    @Test
    fun `unblocking removes only confirmed user from list`() =
        runTest(dispatcher) {
            val vm = SafetyViewModel(FakeSafetyRepository())
            advanceUntilIdle()
            assertEquals(1, vm.state.value.blocked.size)
            vm.unblock(TestUsers.PARTNER_ID)
            advanceUntilIdle()
            assertTrue(vm.state.value.blocked.isEmpty())
        }
}
