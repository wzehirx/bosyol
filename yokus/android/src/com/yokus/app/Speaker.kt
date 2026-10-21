package com.yokus.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.accessibility.AccessibilityManager
import java.util.Locale

/** Duyuru: dokunarak keşfet açıksa ekran okuyucuya (announceForAccessibility), değilse TTS ile tr-TR. */
class Speaker(private val ctx: Context) {
    /** announceForAccessibility için kullanılan görünüm (etkinliğin kök görünümü). */
    var root: View? = null
    private val ui = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: String? = null

    init {
        try {
            tts = TextToSpeech(ctx.applicationContext) { st ->
                if (st == TextToSpeech.SUCCESS) {
                    tts?.language = Locale("tr", "TR"); ready = true
                    pending?.let { tts?.speak(it, TextToSpeech.QUEUE_FLUSH, null, "yokus") }; pending = null
                }
            }
        } catch (e: Throwable) { tts = null }
    }

    fun say(text: String) {
        ui.post {
            val am = ctx.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            val r = root
            if (am != null && am.isEnabled && am.isTouchExplorationEnabled && r != null) r.announceForAccessibility(text)
            else if (ready) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "yokus")
            else pending = text
        }
    }

    fun shutdown() { try { tts?.shutdown() } catch (e: Throwable) {}; tts = null; ready = false }
}
