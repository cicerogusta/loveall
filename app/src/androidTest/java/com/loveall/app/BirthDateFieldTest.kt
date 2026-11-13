package com.loveall.app

import android.view.View
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.loveall.app.ui.components.BirthDateField
import com.loveall.app.ui.theme.LoveAllTheme
import org.hamcrest.Matcher
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class BirthDateFieldTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun label(id: Int) = context.getString(id)

    private fun field() = compose.onNode(hasText(label(R.string.birthdate)) and hasSetTextAction())

    private fun content() {
        compose.setContent {
            var value by remember { mutableStateOf("") }
            LoveAllTheme { Surface { BirthDateField(value, { value = it }) } }
        }
    }

    @Test
    fun numericInputShowsBrazilianMaskAndRejectsImpossibleDate() {
        content()
        field().performTextInput("31022000")
        field().assertTextContains("31/02/2000")
        compose.onNodeWithText(label(R.string.birthdate_invalid)).assertExists()
        field().performTextClearance()
        field().performTextInput("29022000")
        field().assertTextContains("29/02/2000")
        compose.onNodeWithText(label(R.string.birthdate_invalid)).assertDoesNotExist()
    }

    @Test
    fun calendarUsesPortugueseBrazilAndReturnsDayMonthYear() {
        content()
        compose.onNodeWithContentDescription(label(R.string.birthdate_choose)).performClick()
        onView(isAssignableFrom(DatePicker::class.java)).check(matches(isDisplayed()))
        onView(isAssignableFrom(DatePicker::class.java))
            .perform(
                object : ViewAction {
                    override fun getDescription() = "Select a birth date in the Brazilian calendar"

                    override fun getConstraints(): Matcher<View> =
                        isAssignableFrom(DatePicker::class.java)

                    override fun perform(uiController: UiController, view: View) {
                        val picker = view as DatePicker
                        val locale = picker.resources.configuration.locales[0]
                        assertEquals("pt", locale.language)
                        assertEquals("BR", locale.country)
                        picker.updateDate(2000, 10, 12)
                        uiController.loopMainThreadUntilIdle()
                    }
                }
            )
        onView(withText(label(R.string.confirm))).perform(click())
        field().assertTextContains("12/11/2000")
    }
}
