package com.loveall.app.domain.validation

/**
 * Um callback atrasado de inicialização não pode desfazer uma falha ou revogação de consentimento.
 */
class AdConsentGate {
    private var permission = false
    private var sdkReady = false
    val canInitialize
        get() = permission

    val canLoadAds
        get() = permission && sdkReady

    fun beginUpdate() {
        permission = false
    }

    fun confirm(allowed: Boolean) {
        permission = allowed
    }

    fun fail() {
        permission = false
    }

    fun sdkInitialized() {
        sdkReady = true
    }
}
