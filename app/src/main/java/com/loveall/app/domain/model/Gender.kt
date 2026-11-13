package com.loveall.app.domain.model

/**
 * Identidades conhecidas e seus identificadores no protocolo existente. Os rótulos visíveis são
 * definidos pela UI nos recursos Android, sem traduzir os valores wire.
 */
enum class Gender(val wire: String) {
    WOMAN("woman"),
    MAN("man"),
    NON_BINARY("non_binary"),
    TRANS_WOMAN("transgender_woman"),
    TRANS_MAN("transgender_man"),
    GENDERQUEER("genderqueer"),
    GENDERFLUID("genderfluid"),
    AGENDER("agender"),
    BIGENDER("bigender"),
    TWO_SPIRIT("two_spirit"),
    OTHER("other"),
    PREFER_NOT_SAY("prefer_not_say"),
}
