package com.yokus.app

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

/** Yer tutucu ekran: tek bir başlık. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashLog.install(this)
        val t = TextView(this)
        t.text = "Yokuş Ustası"
        t.textSize = 32f
        t.setPadding(48, 48, 48, 48)
        t.isAccessibilityHeading = true
        setContentView(t)
        t.requestFocus()
        t.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }
}
