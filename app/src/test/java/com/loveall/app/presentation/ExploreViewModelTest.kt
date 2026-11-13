package com.loveall.app.presentation

import com.loveall.app.TestFixtures
import com.loveall.app.domain.error.AuthError
import com.loveall.app.fake.*
import com.loveall.app.fixtures.TestConversations
import com.loveall.app.fixtures.TestUsers
import com.loveall.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val dispatcher
        get() = mainDispatcherRule.dispatcher

    @Test
    fun `failed like keeps the visible profile and permits retry`() =
        runTest(dispatcher) {
            val connections = FakeConnectionsRepository().apply { failLike = true }
            val vm = ExploreViewModel(FakeProfileRepository(), connections, FakeSafetyRepository())
            advanceUntilIdle()
            vm.like(TestFixtures.profile)
            advanceUntilIdle()
            assertEquals(TestUsers.PARTNER_ID, vm.state.value.profiles.single().id)
            assertEquals(AuthError.NETWORK, vm.state.value.error)
            connections.failLike = false
            vm.like(TestFixtures.profile)
            advanceUntilIdle()
            assertTrue(vm.state.value.profiles.isEmpty())
            assertEquals(TestConversations.MATCH_ID, vm.state.value.matchId)
        }

    @Test
    fun `repeated taps while like is pending produce one operation`() =
        runTest(dispatcher) {
            val connections = FakeConnectionsRepository()
            val vm = ExploreViewModel(FakeProfileRepository(), connections, FakeSafetyRepository())
            advanceUntilIdle()
            vm.like(TestFixtures.profile)
            vm.like(TestFixtures.profile)
            advanceUntilIdle()
            assertEquals(1, connections.likeCalls)
        }

    @Test
    fun `unexpected like failure preserves candidates and permits retry`() =
        runTest(dispatcher) {
            val connections = FakeConnectionsRepository().apply { unexpectedFailure = true }
            val explore =
                ExploreViewModel(FakeProfileRepository(), connections, FakeSafetyRepository())
            advanceUntilIdle()
            explore.like(TestFixtures.profile)
            advanceUntilIdle()
            assertFalse(explore.state.value.busy)
            assertEquals(AuthError.SERVICE, explore.state.value.error)
            assertEquals(1, explore.state.value.profiles.size)
            connections.unexpectedFailure = false
            explore.like(TestFixtures.profile)
            advanceUntilIdle()
            assertTrue(explore.state.value.profiles.isEmpty())
        }
}
