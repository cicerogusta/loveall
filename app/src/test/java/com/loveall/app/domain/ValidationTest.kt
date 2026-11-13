package com.loveall.app.domain

import com.loveall.app.TestFixtures
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.domain.validation.Rules
import com.loveall.app.fixtures.TestPhotos
import com.loveall.app.fixtures.TestUsers
import org.junit.Assert.*
import org.junit.Test

class ValidationTest {

    @Test
    fun `names allow unicode letters spaces and boundary lengths`() {
        assertTrue(Rules.validName(" Bia "))
        assertTrue(Rules.validName("João"))
        assertTrue(Rules.validName("Al"))
        assertTrue(Rules.validName("A".repeat(50)))
        assertFalse(Rules.validName(" "))
    }

    @Test
    fun `photo URLs require HTTPS host and no user information`() {
        assertTrue(Rules.validPhoto(TestPhotos.VALID_URL))
        listOf(
                "http://images.example.test/photo.jpg",
                "https:///photo.jpg",
                "file:///photo.jpg",
                "javascript:alert(1)",
                "https://secret@images.example.test/photo.jpg",
                "not a url",
            )
            .forEach { assertFalse(Rules.validPhoto(it)) }
    }

    @Test
    fun `editing maps existing account preferences without resetting values`() {
        val user =
            TestFixtures.user.copy(
                gender = TestUsers.NON_BINARY_WIRE,
                ageMin = 25,
                ageMax = 45,
                maxDistance = 70,
                photos = listOf("https://images.example.test/alex.jpg"),
                lookingFor = listOf(TestUsers.MAN_WIRE),
            )
        val edit = ProfileEdit.from(user)
        assertEquals(25, edit.ageMin)
        assertEquals(45, edit.ageMax)
        assertEquals(70, edit.maxDistance)
        assertEquals(listOf(TestUsers.MAN_WIRE), edit.lookingFor)
        assertEquals(user.photos, edit.photos)
    }
}
