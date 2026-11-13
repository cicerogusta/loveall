package com.loveall.app.fake

import com.loveall.app.TestFixtures
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.domain.model.User
import com.loveall.app.domain.repository.ProfileRepository
import com.loveall.app.fixtures.TestPhotos
import kotlinx.coroutines.test.*
import org.junit.Assert.*

internal class FakeProfileRepository : ProfileRepository {
    var updateCalls = 0

    override suspend fun uploadPhoto(jpeg: ByteArray) = TestPhotos.UPLOADED_REFERENCE

    override suspend fun current() = TestFixtures.user

    override suspend fun update(edit: ProfileEdit): User {
        updateCalls++
        return TestFixtures.user.copy(name = edit.name, photos = edit.photos)
    }

    override suspend fun discover() = listOf(TestFixtures.profile)

    override suspend fun profile(id: String) = TestFixtures.profile
}
