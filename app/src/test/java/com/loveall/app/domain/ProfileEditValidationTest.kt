package com.loveall.app.domain

import com.loveall.app.fixtures.ProfileEditCase
import com.loveall.app.fixtures.ProfileEditCases
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class ProfileEditValidationTest(private val scenario: ProfileEditCase) {
    @Test
    fun `given validation scenario when checked then expected decision is returned`() {
        // Arrange: the runner provides one named fixture per case.
        // Act
        val actual = scenario.edit.isValid()
        // Assert
        assertEquals(scenario.description, scenario.expected, actual)
    }

    companion object {
        private const val CASE_NAME = "{0}"

        @JvmStatic
        @Parameterized.Parameters(name = CASE_NAME)
        fun cases() = ProfileEditCases.all.map { arrayOf(it) }
    }
}
