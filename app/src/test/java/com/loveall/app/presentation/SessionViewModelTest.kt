package com.loveall.app.presentation

import com.loveall.app.TestFixtures
import com.loveall.app.domain.error.AuthError
import com.loveall.app.fake.*
import com.loveall.app.fixtures.TestUsers
import com.loveall.app.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {
    @Test
    fun `successful deletion removes authenticated navigation`() =
        runTest(dispatcher) {
            val auth = FakeAuthRepository().apply { saved = TestFixtures.user }
            val vm = SessionViewModel(auth, emptyFlow())
            advanceUntilIdle()
            vm.deleteAccount(TestFixtures.PASSWORD)
            advanceUntilIdle()
            assertTrue(auth.deleted)
            assertNull(vm.state.value.user)
            assertFalse(vm.state.value.busy)
        }

    @Test
    fun `failed deletion keeps account available for correction and retry`() =
        runTest(dispatcher) {
            val auth = FakeAuthRepository().apply { saved = TestFixtures.user }
            val vm = SessionViewModel(auth, emptyFlow())
            advanceUntilIdle()
            auth.error = AuthError.FORBIDDEN
            vm.deleteAccount(TestFixtures.PASSWORD)
            advanceUntilIdle()
            assertFalse(auth.deleted)
            assertEquals(TestUsers.SELF_ID, vm.state.value.user?.id)
            assertEquals(AuthError.FORBIDDEN, vm.state.value.error)
            assertFalse(vm.state.value.busy)
        }

    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val dispatcher
        get() = mainDispatcherRule.dispatcher

    @Test
    fun `session restores user before exposing private navigation`() =
        runTest(dispatcher) {
            // Arrange
            val auth = FakeAuthRepository().apply { saved = TestFixtures.user }
            // Act
            val vm = SessionViewModel(auth, emptyFlow())
            // Assert
            assertTrue(vm.state.value.restoring)
            advanceUntilIdle()
            assertEquals(TestUsers.SELF_NAME, vm.state.value.user?.name)
            assertFalse(vm.state.value.restoring)
        }

    @Test
    fun `session expiration cancels pending login and prevents late authentication`() =
        runTest(dispatcher) {
            val expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
            val auth = FakeAuthRepository()
            val vm = SessionViewModel(auth, expired)
            advanceUntilIdle()
            val gate = CompletableDeferred<Unit>()
            auth.loginGate = gate
            vm.login(TestFixtures.EMAIL, TestFixtures.PASSWORD)
            runCurrent()
            assertTrue(vm.state.value.busy)
            expired.emit(Unit)
            runCurrent()
            assertFalse(vm.state.value.busy)
            assertNull(vm.state.value.user)
            assertEquals(AuthError.SESSION_EXPIRED, vm.state.value.error)
            gate.complete(Unit)
            advanceUntilIdle()
            assertNull(vm.state.value.user)
            assertNull(auth.saved)
        }

    @Test
    fun `unexpected restore failure is recoverable through retry`() =
        runTest(dispatcher) {
            val auth = FakeAuthRepository().apply { unexpectedRestoreFailure = true }
            val session = SessionViewModel(auth, emptyFlow())
            advanceUntilIdle()
            assertTrue(session.state.value.restoreFailed)
            assertFalse(session.state.value.busy)
            assertEquals(AuthError.SERVICE, session.state.value.error)
            auth.unexpectedRestoreFailure = false
            session.restore()
            advanceUntilIdle()
            assertFalse(session.state.value.restoreFailed)
        }

    @Test
    fun `offline restore allows retry and retains saved account`() =
        runTest(dispatcher) {
            val auth =
                FakeAuthRepository().apply {
                    saved = TestFixtures.user
                    error = AuthError.NETWORK
                }
            val vm = SessionViewModel(auth, emptyFlow())
            advanceUntilIdle()
            assertTrue(vm.state.value.restoreFailed)
            assertEquals(AuthError.NETWORK, vm.state.value.error)
            auth.error = null
            vm.restore()
            advanceUntilIdle()
            assertEquals(TestUsers.SELF_ID, vm.state.value.user?.id)
            assertFalse(vm.state.value.restoreFailed)
        }

    @Test
    fun `login failure exposes error without granting account access`() =
        runTest(dispatcher) {
            val auth = FakeAuthRepository()
            val vm = SessionViewModel(auth, emptyFlow())
            advanceUntilIdle()
            auth.error = AuthError.INVALID_CREDENTIALS
            vm.login(TestFixtures.EMAIL, TestFixtures.PASSWORD)
            advanceUntilIdle()
            assertNull(vm.state.value.user)
            assertFalse(vm.state.value.busy)
            assertEquals(AuthError.INVALID_CREDENTIALS, vm.state.value.error)
        }

    @Test
    fun `logout clears authenticated state`() =
        runTest(dispatcher) {
            val auth = FakeAuthRepository().apply { saved = TestFixtures.user }
            val vm = SessionViewModel(auth, emptyFlow())
            advanceUntilIdle()
            vm.logout()
            advanceUntilIdle()
            assertNull(vm.state.value.user)
            assertNull(auth.saved)
        }
}
