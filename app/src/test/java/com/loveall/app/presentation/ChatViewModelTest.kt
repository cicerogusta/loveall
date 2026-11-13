package com.loveall.app.presentation

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.model.CallType
import com.loveall.app.domain.model.Message
import com.loveall.app.fake.*
import com.loveall.app.fixtures.TestConversations
import com.loveall.app.fixtures.TestUsers
import com.loveall.app.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val dispatcher
        get() = mainDispatcherRule.dispatcher

    @Test
    fun `background chat refresh permits sending and preserves new messages`() =
        runTest(dispatcher) {
            val messages = FakeMessagesRepository()
            val vm =
                ChatViewModel(
                    TestConversations.MATCH_ID,
                    messages,
                    FakeConnectionsRepository(),
                    FakeSafetyRepository(),
                    FakePlansRepository(),
                )
            advanceUntilIdle()
            val gate = CompletableDeferred<Unit>()
            messages.refreshGate = gate
            vm.refresh()
            runCurrent()
            vm.refresh() // Coalesce another polling tick while this one is pending.
            assertFalse(vm.state.value.busy)
            assertTrue(vm.state.value.refreshing)
            vm.send(TestConversations.OUTGOING_TEXT)
            runCurrent()
            assertFalse(vm.state.value.sending)
            assertTrue(vm.state.value.refreshing)
            assertEquals(
                listOf(TestConversations.FIRST_MESSAGE_ID, TestConversations.SECOND_MESSAGE_ID),
                vm.state.value.messages.map(Message::id),
            )
            gate.complete(Unit)
            advanceUntilIdle()
            assertFalse(vm.state.value.refreshing)
            assertEquals(2, messages.messageCalls)
            assertEquals(
                listOf(TestConversations.FIRST_MESSAGE_ID, TestConversations.SECOND_MESSAGE_ID),
                vm.state.value.messages.map(Message::id),
            )
            vm.older() // Last page was short: no more history requests.
            advanceUntilIdle()
            assertEquals(2, messages.messageCalls)
        }

    @Test
    fun `retry after initial chat failure loads partner and messages`() =
        runTest(dispatcher) {
            val messages = FakeMessagesRepository().apply { failLoad = true }
            val vm =
                ChatViewModel(
                    TestConversations.MATCH_ID,
                    messages,
                    FakeConnectionsRepository(),
                    FakeSafetyRepository(),
                    FakePlansRepository(),
                )
            advanceUntilIdle()
            assertNull(vm.state.value.partner)
            assertFalse(vm.state.value.loading)
            assertEquals(AuthError.NETWORK, vm.state.value.error)
            messages.failLoad = false
            vm.refresh()
            advanceUntilIdle()
            assertEquals(TestUsers.PARTNER_ID, vm.state.value.partner?.id)
            assertEquals(1, vm.state.value.messages.size)
            assertFalse(vm.state.value.loading)
            assertNull(vm.state.value.error)
        }

    @Test
    fun `failed send never adds a phantom message or clears draft`() =
        runTest(dispatcher) {
            val messages = FakeMessagesRepository().apply { failSend = true }
            val vm =
                ChatViewModel(
                    TestConversations.MATCH_ID,
                    messages,
                    FakeConnectionsRepository(),
                    FakeSafetyRepository(),
                    FakePlansRepository(),
                )
            advanceUntilIdle()
            vm.send(TestConversations.OUTGOING_TEXT)
            advanceUntilIdle()
            assertNull(vm.state.value.sentDraft)
            assertEquals(0L, vm.state.value.sendVersion)
            assertEquals(
                listOf(TestConversations.FIRST_MESSAGE_ID),
                vm.state.value.messages.map(Message::id),
            )
            assertEquals(AuthError.NETWORK, vm.state.value.error)
        }

    @Test
    fun `successful send appends server message and acknowledges draft`() =
        runTest(dispatcher) {
            val vm =
                ChatViewModel(
                    TestConversations.MATCH_ID,
                    FakeMessagesRepository(),
                    FakeConnectionsRepository(),
                    FakeSafetyRepository(),
                    FakePlansRepository(),
                )
            advanceUntilIdle()
            vm.send(TestConversations.OUTGOING_TEXT)
            advanceUntilIdle()
            assertEquals(TestConversations.OUTGOING_TEXT, vm.state.value.sentDraft)
            assertEquals(1L, vm.state.value.sendVersion)
            assertEquals(
                listOf(TestConversations.FIRST_MESSAGE_ID, TestConversations.SECOND_MESSAGE_ID),
                vm.state.value.messages.map(Message::id),
            )
        }

    @Test
    fun `blocked chat closes only after server operation succeeds`() =
        runTest(dispatcher) {
            val vm =
                ChatViewModel(
                    TestConversations.MATCH_ID,
                    FakeMessagesRepository(),
                    FakeConnectionsRepository(),
                    FakeSafetyRepository(),
                    FakePlansRepository(),
                )
            advanceUntilIdle()
            assertFalse(vm.state.value.closed)
            vm.block()
            advanceUntilIdle()
            assertTrue(vm.state.value.closed)
        }

    @Test
    fun `call without entitlement exposes refusal and never starts a simulated call`() =
        runTest(dispatcher) {
            val vm =
                ChatViewModel(
                    TestConversations.MATCH_ID,
                    FakeMessagesRepository(),
                    FakeConnectionsRepository(),
                    FakeSafetyRepository(),
                    FakePlansRepository(),
                )
            advanceUntilIdle()
            vm.checkCall(CallType.VIDEO)
            advanceUntilIdle()
            assertEquals(AuthError.FORBIDDEN, vm.state.value.error)
            assertNull(vm.state.value.eligibleMinutes)
        }
}
