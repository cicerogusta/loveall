package com.loveall.app

import android.app.Application

/**
 * Mantém as dependências compartilhadas durante a vida do processo Android. O contêiner é criado no
 * primeiro acesso; uma recomposição de tela não recria os clientes HTTP.
 */
class LoveAllApplication : Application() {
    val container by lazy { AppContainer(this) }
}
