package com.loveall.app.presentation

import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.repository.PhotoReader
import com.loveall.app.domain.usecase.AddProfilePhoto
import com.loveall.app.domain.validation.Rules
import com.loveall.app.fake.*
import com.loveall.app.fixtures.TestPhotos
import com.loveall.app.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val dispatcher
        get() = mainDispatcherRule.dispatcher

    @Test
    fun `profile distinguishes upload from save and prevents overlapping writes`() =
        runTest(dispatcher) {
            val profiles = FakeProfileRepository()
            val gate = CompletableDeferred<Unit>()
            var reads = 0
            val reader = PhotoReader {
                reads++
                gate.await()
                byteArrayOf(1)
            }
            val vm = ProfileViewModel(profiles, AddProfilePhoto(reader, profiles))
            advanceUntilIdle()
            vm.addPhoto("photo")
            runCurrent()
            assertTrue(vm.state.value.uploadingPhoto)
            assertFalse(vm.state.value.saving)
            vm.addPhoto("duplicate")
            vm.save()
            runCurrent()
            assertEquals(1, reads)
            assertEquals(0, profiles.updateCalls)
            gate.complete(Unit)
            advanceUntilIdle()
            assertFalse(vm.state.value.uploadingPhoto)
            assertEquals(1, vm.state.value.edit!!.photos.size)
            vm.save()
            assertTrue(vm.state.value.saving)
            assertFalse(vm.state.value.uploadingPhoto)
            advanceUntilIdle()
            assertEquals(1, profiles.updateCalls)
            assertTrue(vm.state.value.saved)
            assertFalse(vm.state.value.busy)
        }

    @Test
    fun `photo upload preserves other profile edits and failure leaves photos unchanged`() =
        runTest(dispatcher) {
            val profiles = FakeProfileRepository()
            var readError: AuthError? = null
            var readCount = 0
            val reader = PhotoReader {
                readCount++
                readError?.let { throw AuthException(it) }
                byteArrayOf(1)
            }
            val vm = ProfileViewModel(profiles, AddProfilePhoto(reader, profiles))
            advanceUntilIdle()
            vm.edit { it.copy(bio = "Minha história", photos = emptyList()) }
            vm.addPhoto(TestPhotos.SELECTED_REFERENCE)
            advanceUntilIdle()
            assertEquals("Minha história", vm.state.value.edit!!.bio)
            val photos = vm.state.value.edit!!.photos
            assertEquals(1, photos.size)
            readError = AuthError.NETWORK
            vm.addPhoto(TestPhotos.SELECTED_REFERENCE)
            advanceUntilIdle()
            assertEquals(photos, vm.state.value.edit!!.photos)
            assertEquals(AuthError.NETWORK, vm.state.value.error)
            vm.edit { it.copy(photos = List(Rules.MAX_PHOTOS) { photos.single() }) }
            vm.addPhoto(TestPhotos.SEVENTH_REFERENCE)
            advanceUntilIdle()
            assertEquals(Rules.MAX_PHOTOS, vm.state.value.edit!!.photos.size)
            assertEquals(AuthError.VALIDATION, vm.state.value.error)
            assertEquals(2, readCount)
        }
}
