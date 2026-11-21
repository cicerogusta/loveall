package com.loveall.app.presentation

import com.loveall.app.TestFixtures
import com.loveall.app.fake.FakeConnectionsRepository
import com.loveall.app.fake.FakePcdCommunityRepository
import com.loveall.app.fixtures.TestConversations
import com.loveall.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PcdCommunityViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `non member cannot discover or like and joining requires consent`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = FakePcdCommunityRepository()
            val vm = PcdCommunityViewModel(repository, FakeConnectionsRepository())
            advanceUntilIdle()
            vm.setMembership(true, false)
            vm.like(TestFixtures.profile)
            advanceUntilIdle()
            assertEquals(false, vm.state.value.member)
            assertEquals(0, repository.discoverCalls)
            assertEquals(0, repository.mutationCalls)
            assertEquals(0, repository.likeCalls)
            vm.setMembership(true, true)
            advanceUntilIdle()
            assertEquals(true, vm.state.value.member)
            assertEquals(listOf(TestFixtures.profile), vm.state.value.explore.profiles)
        }

    @Test
    fun `revocation clears candidates and match dialog`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = FakePcdCommunityRepository().apply { member = true }
            val vm = PcdCommunityViewModel(repository, FakeConnectionsRepository())
            advanceUntilIdle()
            vm.like(TestFixtures.profile)
            advanceUntilIdle()
            assertEquals(TestConversations.MATCH_ID, vm.state.value.explore.matchId)
            vm.setMembership(false)
            advanceUntilIdle()
            assertEquals(false, vm.state.value.member)
            assertTrue(vm.state.value.explore.profiles.isEmpty())
            assertNull(vm.state.value.explore.matchId)
        }

    @Test
    fun `failed like retains profile and repeated taps submit once`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository =
                FakePcdCommunityRepository().apply {
                    member = true
                    failLike = true
                }
            val vm = PcdCommunityViewModel(repository, FakeConnectionsRepository())
            advanceUntilIdle()
            vm.like(TestFixtures.profile)
            vm.like(TestFixtures.profile)
            advanceUntilIdle()
            assertEquals(1, repository.likeCalls)
            assertEquals(1, vm.state.value.explore.profiles.size)
            assertNotNull(vm.state.value.error)
            assertFalse(vm.state.value.busy)
            repository.failLike = false
            vm.like(TestFixtures.profile)
            advanceUntilIdle()
            assertTrue(vm.state.value.explore.profiles.isEmpty())
        }

    @Test
    fun `refresh detects exit on another device and failed exit is retryable`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository =
                FakePcdCommunityRepository().apply {
                    member = true
                    failMutation = true
                }
            val vm = PcdCommunityViewModel(repository, FakeConnectionsRepository())
            advanceUntilIdle()
            vm.setMembership(false)
            advanceUntilIdle()
            assertEquals(true, vm.state.value.member)
            assertNotNull(vm.state.value.error)
            repository.member = false
            vm.refresh()
            advanceUntilIdle()
            assertEquals(false, vm.state.value.member)
            assertTrue(vm.state.value.explore.profiles.isEmpty())
        }
}
