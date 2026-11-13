package com.loveall.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.loveall.app.domain.model.Gender
import com.loveall.app.domain.model.ProfileEdit
import com.loveall.app.domain.model.User
import com.loveall.app.presentation.*
import com.loveall.app.presentation.state.ProfileState
import com.loveall.app.ui.screens.EditProfileScreen
import com.loveall.app.ui.theme.LoveAllTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ProfilePhotoPickerTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun launchPicker(resultCode: Int, selected: Uri?): Uri? {
        var received: Uri? = null
        var launches = 0
        val registry =
            object : ActivityResultRegistry() {
                override fun <I, O> onLaunch(
                    requestCode: Int,
                    contract: ActivityResultContract<I, O>,
                    input: I,
                    options: ActivityOptionsCompat?,
                ) {
                    launches++
                    val intent = contract.createIntent(context, input)
                    assertEquals("image/*", intent.type)
                    assertFalse(intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false))
                    dispatchResult(requestCode, resultCode, selected?.let { Intent().setData(it) })
                }
            }
        val owner =
            object : ActivityResultRegistryOwner {
                override val activityResultRegistry = registry
            }
        val user =
            User("picker-test", "Alex", "picker@example.test", gender = Gender.NON_BINARY.wire)
        compose.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) {
                LoveAllTheme {
                    EditProfileScreen(
                        ProfileState(user = user, edit = ProfileEdit.from(user)),
                        {},
                        {},
                        {},
                        {},
                        {},
                        onAddPhoto = { received = it },
                    )
                }
            }
        }
        compose
            .onNodeWithText(context.getString(R.string.add_photo))
            .performScrollTo()
            .performClick()
        compose.waitForIdle()
        assertEquals(1, launches)
        return received
    }

    @Test
    fun selectedImageIsDeliveredFromNativePicker() {
        val photo = Uri.parse("content://media/picker/test-photo")
        assertEquals(photo, launchPicker(Activity.RESULT_OK, photo))
    }

    @Test
    fun cancelingPickerDoesNotAddPhoto() {
        assertNull(launchPicker(Activity.RESULT_CANCELED, null))
    }
}
