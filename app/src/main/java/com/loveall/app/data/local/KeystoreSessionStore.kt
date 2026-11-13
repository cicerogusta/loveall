package com.loveall.app.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class KeystoreSessionStore(context: Context, private val io: CoroutineDispatcher) : SessionStore {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    override suspend fun read(): String? =
        withContext(io) {
            val encoded = preferences.getString(TOKEN, null) ?: return@withContext null
            try {
                val parts = encoded.split(SEPARATOR)
                require(parts.size == 2)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    key(),
                    GCMParameterSpec(TAG_BITS, decode(parts[0])),
                )
                cipher.doFinal(decode(parts[1])).toString(Charsets.UTF_8)
            } catch (error: java.security.GeneralSecurityException) {
                // Restored ciphertext or an invalidated key cannot be reused: require login.
                check(preferences.edit().clear().commit())
                null
            } catch (error: IllegalArgumentException) {
                check(preferences.edit().clear().commit())
                null
            }
        }

    override suspend fun write(token: String) =
        withContext(io) {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key())
            val ciphertext = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
            val encoded = encode(cipher.iv) + SEPARATOR + encode(ciphertext)
            check(preferences.edit().putString(TOKEN, encoded).commit())
        }

    override suspend fun clear() =
        withContext(io) {
            check(preferences.edit().clear().commit())
        }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance(PROVIDER).apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let {
            return it
        }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER).run {
            init(
                KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
            generateKey()
        }
    }

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(value: String) = Base64.decode(value, Base64.NO_WRAP)

    private companion object {
        const val PREFERENCES = "secure_session"
        const val TOKEN = "encrypted_token"
        const val KEY_ALIAS = "loveall.session.v1"
        const val PROVIDER = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val SEPARATOR = ":"
        const val TAG_BITS = 128
    }
}
