package com.loveall.app.presentation

import com.loveall.app.core.format.BrazilianLocale
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

/** UI dates use Brazilian order; the domain and API continue to receive ISO dates. */
object BirthDateFormat {
    private const val INPUT_PATTERN = "ddMMuuuu"
    const val MAX_DIGITS = 8
    const val DAY_END = 2
    const val MONTH_END = 4
    val locale: Locale = BrazilianLocale.value
    private val inputFormatter =
        DateTimeFormatter.ofPattern(INPUT_PATTERN, locale).withResolverStyle(ResolverStyle.STRICT)

    fun normalize(value: String): String = value.filter { it in '0'..'9' }.take(MAX_DIGITS)

    fun toDate(digits: String): LocalDate? {
        if (digits.length != MAX_DIGITS || digits.any { it !in '0'..'9' }) return null
        return try {
            LocalDate.parse(digits, inputFormatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun toIso(digits: String): String? = toDate(digits)?.format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun fromDate(date: LocalDate): String = date.format(inputFormatter)

    fun display(digits: String): String = buildString {
        digits.take(MAX_DIGITS).forEachIndexed { index, digit ->
            append(digit)
            if (index + 1 == DAY_END || index + 1 == MONTH_END) append('/')
        }
    }
}
