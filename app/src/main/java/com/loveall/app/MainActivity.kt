package com.loveall.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.loveall.app.ui.LoveAllRoot
import com.loveall.app.ui.theme.LoveAllTheme

/**
 * Ponto de entrada da interface Android. Obtém as dependências da Application e instala a árvore
 * Compose. As decisões de sessão e navegação ficam em LoveAllRoot.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as LoveAllApplication).container
        container.attachActivity(this)
        setContent { LoveAllTheme { Surface { LoveAllRoot(container) } } }
    }

    override fun onDestroy() {
        (application as LoveAllApplication).container.detachActivity(this)
        super.onDestroy()
    }
}
