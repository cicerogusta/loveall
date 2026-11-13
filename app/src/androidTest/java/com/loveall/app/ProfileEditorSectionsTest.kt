package com.loveall.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import com.loveall.app.domain.model.Gender
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.domain.model.User
import com.loveall.app.presentation.state.ProfileState
import com.loveall.app.ui.screens.EditProfileScreen
import com.loveall.app.ui.theme.LoveAllTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ProfileEditorSectionsTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun editsAcrossSectionsAndPhotoRemovalReachSave() {
        val user =
            User(
                "editor-test",
                "Alex",
                "editor@example.test",
                gender = Gender.NON_BINARY.wire,
                photos = listOf("https://example.test/photo.jpg"),
            )
        var saved: ProfileEdit? = null
        compose.setContent {
            var edit by remember { mutableStateOf(ProfileEdit.from(user)) }
            LoveAllTheme {
                EditProfileScreen(
                    ProfileState(user = user, edit = edit),
                    {},
                    {},
                    onEdit = { transform -> edit = transform(edit) },
                    onSave = { saved = edit },
                    onSaved = {},
                    onAddPhoto = {},
                )
            }
        }
        field(R.string.bio).performScrollTo().performTextInput("Gosto de música.")
        field(R.string.city).performScrollTo().performTextInput("São Paulo")
        compose
            .onNodeWithContentDescription(context.getString(R.string.remove_photo, 1))
            .performScrollTo()
            .performClick()
        field(R.string.age_min).performScrollTo().performTextClearance()
        field(R.string.age_min).performTextInput("21")
        field(R.string.distance).performScrollTo().performTextClearance()
        field(R.string.distance).performTextInput("75")
        closeSoftKeyboard()
        compose.onNodeWithText(context.getString(R.string.save)).performClick()
        assertEquals("Gosto de música.", saved?.bio)
        assertEquals("São Paulo", saved?.city)
        assertEquals(21, saved?.ageMin)
        assertEquals(75, saved?.maxDistance)
        assertTrue(saved!!.photos.isEmpty())
    }

    private fun field(id: Int) =
        compose.onNode(hasText(context.getString(id)) and hasSetTextAction())
}
