package com.loveall.app.domain

import com.loveall.app.TestFixtures
import com.loveall.app.domain.validation.Rules
import com.loveall.app.fixtures.TestPhotos
import org.junit.Assert.*
import org.junit.Test

class ProfilePhotoRulesTest {
    @Test
    fun `uploaded photos are portable and constrained to the photo endpoint`() {
        val photo = TestPhotos.UPLOADED_REFERENCE
        assertTrue(Rules.validProfilePhoto(photo))
        assertTrue(TestFixtures.edit.copy(photos = listOf(photo)).isValid())
        assertFalse(Rules.validPhoto(photo)) // Checkout links still require HTTPS.
        listOf(
                "file:///private.jpg",
                "content://media/1",
                "http://host/photo.jpg",
                "//host/photo.jpg",
                "/api/photos/../private.jpg",
                "$photo?redirect=elsewhere",
                "/api/photos/a.jpg",
            )
            .forEach {
                assertFalse(it, Rules.validProfilePhoto(it))
            }
        assertTrue(Rules.validProfilePhoto("https://example.test/photo.jpg"))
    }
}
