package com.loveall.app.domain.model

/**
 * Representa os dados públicos de outra pessoa usados na descoberta, nas curtidas e nos matches. A
 * idade pode estar ausente, e fotos são referências remotas ou relativas à API, não arquivos
 * locais.
 */
data class Profile(
    val id: String,
    val name: String,
    val age: Int?,
    val gender: String,
    val genderCustom: String,
    val bio: String,
    val city: String,
    val photos: List<String>,
    val verified: Boolean,
)
