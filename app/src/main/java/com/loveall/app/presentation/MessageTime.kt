package com.loveall.app.presentation

import com.loveall.app.domain.model.MessageTimestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun formatMessageTime(value: String, locale: Locale, zone: ZoneId): String? {
    val instant = MessageTimestamp.parse(value) ?: return null
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        .withLocale(locale)
        .withZone(zone)
        .format(instant)
}
