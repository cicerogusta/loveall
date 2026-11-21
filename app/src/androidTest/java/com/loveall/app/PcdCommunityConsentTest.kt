package com.loveall.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.loveall.app.presentation.state.PcdCommunityState
import com.loveall.app.ui.screens.PcdCommunityScreen
import com.loveall.app.ui.theme.LoveAllTheme
import org.junit.Rule
import org.junit.Test

class PcdCommunityConsentTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rejoiningAfterRemoteExitRequiresFreshConsent() {
        val state = mutableStateOf(PcdCommunityState(member = false))
        compose.setContent {
            LoveAllTheme {
                PcdCommunityScreen(state.value, {}, {}, { _, _ -> }, {}, {}, {}, {}, {}, {})
            }
        }
        val join =
            InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.pcd_join)
        compose.onNode(isToggleable()).performScrollTo().performClick()
        compose.onNodeWithText(join).performScrollTo().assertIsEnabled()
        compose.runOnIdle { state.value = PcdCommunityState(member = true) }
        compose.waitForIdle()
        compose.runOnIdle { state.value = PcdCommunityState(member = false) }
        compose.onNodeWithText(join).performScrollTo().assertIsNotEnabled()
        compose.onNode(isToggleable()).assertIsOff()
    }
}
