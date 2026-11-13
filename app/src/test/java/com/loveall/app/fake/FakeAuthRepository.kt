package com.loveall.app.fake

import com.loveall.app.TestFixtures
import com.loveall.app.domain.error.AuthError
import com.loveall.app.domain.error.AuthException
import com.loveall.app.domain.model.Registration
import com.loveall.app.domain.model.User
import com.loveall.app.domain.repository.AuthRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.*
import org.junit.Assert.*

internal class FakeAuthRepository : AuthRepository {
    var loginGate: CompletableDeferred<Unit>? = null
    var unexpectedRestoreFailure = false
    var saved: User? = null
    var error: AuthError? = null

    override suspend fun restoreSession(): User? {
        if (unexpectedRestoreFailure) throw IllegalStateException(MALFORMED_SESSION_MESSAGE)
        error?.let { throw AuthException(it) }
        return saved
    }

    override suspend fun login(email: String, password: String): User {
        loginGate?.await()
        error?.let { throw AuthException(it) }
        return TestFixtures.user.also { saved = it }
    }

    override suspend fun register(form: Registration) = login(form.email, form.password)

    override suspend fun acceptTerms(): User =
        requireNotNull(saved)
            .copy(termsVersion = com.loveall.app.domain.model.LegalPolicy.VERSION)
            .also { saved = it }

    var deleted = false

    override suspend fun deleteAccount(password: String) {
        error?.let { throw AuthException(it) }
        deleted = true
        saved = null
    }

    override suspend fun logout() {
        saved = null
    }
}

private const val MALFORMED_SESSION_MESSAGE = "Malformed session"
