package com.loveall.app.domain.model

/**
 * Motivos conhecidos de denúncia e os identificadores exigidos pela API. A apresentação escolhe os
 * textos localizados; o servidor recebe o valor wire.
 */
enum class ReportReason(val wire: String) {
    FAKE("fake_profile"),
    CONTENT("inappropriate_content"),
    HARASSMENT("harassment"),
    SPAM("spam"),
    UNDERAGE("underage"),
    SCAM("scam"),
    HATE("hate_speech"),
    VIOLENCE("violence"),
    OTHER("other"),
}
