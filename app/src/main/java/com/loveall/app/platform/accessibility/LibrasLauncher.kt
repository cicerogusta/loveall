package com.loveall.app.platform.accessibility

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
import androidx.core.net.toUri
import com.loveall.app.R

/** O usuário escolhe o texto e o cola no VLibras; nenhuma conversa é enviada automaticamente. */
class LibrasLauncher(private val context: Context) {
    fun copy(text: String) {
        val clip = ClipData.newPlainText(context.getString(R.string.libras_text_label), text)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras =
                PersistableBundle().apply { putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true) }
        }
        context.getSystemService(ClipboardManager::class.java).setPrimaryClip(clip)
    }

    fun open(): Boolean {
        val launch = context.packageManager.getLaunchIntentForPackage(PACKAGE)
        return try {
            context.startActivity(launch ?: Intent(Intent.ACTION_VIEW, STORE_URL.toUri()))
            true
        } catch (_: android.content.ActivityNotFoundException) {
            false
        }
    }

    private companion object {
        const val PACKAGE = "com.lavid.vlibrasdroid"
        const val STORE_URL = "https://play.google.com/store/apps/details?id=com.lavid.vlibrasdroid"
    }
}
