package com.loveall.app

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.loveall.app.data.local.KeystoreSessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class KeystoreSessionStoreTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val store = KeystoreSessionStore(context, Dispatchers.IO)
    private val preferences = context.getSharedPreferences("secure_session", Context.MODE_PRIVATE)
    private var previousToken: String? = null

    @Before
    fun setup() = runBlocking {
        previousToken = store.read()
        store.clear()
    }

    @After
    fun cleanup() = runBlocking {
        store.clear()
        previousToken?.let { store.write(it) }
        Unit
    }

    @Test
    fun tokenRoundTripsWithoutPlaintextOnDisk() = runBlocking {
        store.write(TOKEN)
        assertEquals(TOKEN, store.read())
        val saved = preferences.all.values.filterIsInstance<String>().single()
        assertFalse(saved.contains(TOKEN))
        assertTrue(saved.contains(":"))
        store.clear()
        assertNull(store.read())
        assertTrue(preferences.all.isEmpty())
    }

    @Test
    fun sameTokenUsesDifferentRandomIv() = runBlocking {
        store.write(TOKEN)
        val first = preferences.all.values.single()
        store.write(TOKEN)
        assertNotEquals(first, preferences.all.values.single())
        assertEquals(TOKEN, store.read())
    }

    @Test
    fun corruptedCiphertextRequiresNewLogin() = runBlocking {
        store.write(TOKEN)
        preferences.edit().putString("encrypted_token", "invalid:ciphertext").commit()
        assertNull(store.read())
        assertTrue(preferences.all.isEmpty())
    }

    private companion object {
        const val TOKEN = "keystore-test-session-token"
    }
}
