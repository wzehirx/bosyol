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
    @Volatile private var ready = false
    @Volatile private var pending: String? = null
    @Volatile private var closed = false

    init {
        try {
            tts = TextToSpeech(ctx.applicationContext) { st ->
                if (st == TextToSpeech.SUCCESS && !closed) {
                    val t = tts
                    // tr-TR yoksa "tr", o da yoksa varsayılan dille konuşmaya devam et (sessiz kalmaktan iyi)
                    if (t != null) {
                        var r = t.setLanguage(Locale("tr", "TR"))
                        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) r = t.setLanguage(Locale("tr"))
                    }
                    ready = true
                    pending?.let { tts?.speak(it, TextToSpeech.QUEUE_FLUSH, null, "yokus") }; pending = null
                }
            }
        } catch (e: Throwable) { tts = null }
    }

    fun say(text: String) {
        ui.post {
            if (closed) return@post
            val r = root
            if (r != null && screenReaderOn()) r.announceForAccessibility(text)
            else if (ready) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "yokus")
            else pending = text
        }
    }

    /** Dokunarak keşfet açık ya da sesli geri bildirim veren bir erişilebilirlik hizmeti (TalkBack, Jieshuo) çalışıyor mu. */
    private fun screenReaderOn(): Boolean = try {
        val am = ctx.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        am != null && am.isEnabled && (am.isTouchExplorationEnabled ||
            am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_SPOKEN).isNotEmpty())
    } catch (e: Throwable) { false }

    fun shutdown() {
        closed = true; ui.removeCallbacksAndMessages(null); pending = null
        try { tts?.stop(); tts?.shutdown() } catch (e: Throwable) {}
        tts = null; ready = false; root = null
    }
}
