package com.loveall.app.presentation

import androidx.compose.ui.text.AnnotatedString
import com.loveall.app.fixtures.TestUsers
import com.loveall.app.ui.components.BrazilianDateVisualTransformation
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class BirthDateFormatTest {
    @Test
    fun `Brazilian dates keep day and month order when sent to API`() {
        assertEquals(TestUsers.ADULT_BIRTH_DATE, BirthDateFormat.toIso("12112000"))
        assertEquals("2000-06-05", BirthDateFormat.toIso("05062000"))
        assertEquals("12112000", BirthDateFormat.fromDate(LocalDate.of(2000, 11, 12)))
    }

    @Test
    fun `invalid dates are rejected rather than normalized to another birthday`() {
        listOf(
                "31022000",
                "29021900",
                "31042000",
                "00122000",
                "12132000",
                "1211200",
                "",
                "12aa2000",
            )
            .forEach { assertNull("Invalid input: $it", BirthDateFormat.toIso(it)) }
        assertEquals("2000-02-29", BirthDateFormat.toIso("29022000"))
    }

    @Test
    fun `typing and pasting accept Brazilian separators and cap date length`() {
        assertEquals("12112000", BirthDateFormat.normalize("12/11/2000"))
        assertEquals("12112000", BirthDateFormat.normalize("1211200099"))
        assertEquals("", BirthDateFormat.normalize("abc"))
    }

    @Test
    fun `mask handles partial dates and clearing the field`() {
        val expected =
            mapOf(
                "" to "",
                "1" to "1",
                "12" to "12/",
                "121" to "12/1",
                "1211" to "12/11/",
                "12112" to "12/11/2",
                "12112000" to "12/11/2000",
            )
        expected.forEach { (input, output) ->
            assertEquals(
                output,
                BrazilianDateVisualTransformation.filter(AnnotatedString(input)).text.text,
            )
        }
    }

    @Test
    fun `cursor positions account for separators at day and month boundaries`() {
        val transformed = BrazilianDateVisualTransformation.filter(AnnotatedString("12112000"))
        val expected = listOf(0, 1, 3, 4, 6, 7, 8, 9, 10)
        expected.forEachIndexed { original, visual ->
            assertEquals(visual, transformed.offsetMapping.originalToTransformed(original))
            assertEquals(original, transformed.offsetMapping.transformedToOriginal(visual))
        }
    }

    @Test
    fun `partial date cursor offsets stay in bounds while editing`() {
        (0..8).forEach { length ->
            val transformed =
                BrazilianDateVisualTransformation.filter(AnnotatedString("12112000".take(length)))
            val offsets = (0..length).map(transformed.offsetMapping::originalToTransformed)
            assertEquals(offsets.sorted(), offsets)
            assertTrue(offsets.all { it in 0..transformed.text.length })
            assertTrue(
                (0..transformed.text.length).all {
                    transformed.offsetMapping.transformedToOriginal(it) in 0..length
                }
            )
        }
    }
}
