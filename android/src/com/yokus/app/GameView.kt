package com.yokus.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import com.yokus.app.core.GameSession
import com.yokus.app.core.Surface
import kotlin.math.floor

/**
 * Oyunun Canvas çizimi. Görme engelsiz yardımcılar içindir; ekran okuyucu için anlamsızdır,
 * bu yüzden erişilebilirlik ağacından çıkarılmıştır.
 */
class GameView(ctx: Context, val session: GameSession) : View(ctx) {
    private val sky = Paint()
    private val ground = Paint(Paint.ANTI_ALIAS_FLAG)
    private val edge = Paint(Paint.ANTI_ALIAS_FLAG)
    private val body = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trim = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tire = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hub = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coinP = Paint(Paint.ANTI_ALIAS_FLAG)
    private val canP = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hudBg = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hudInk = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bar = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fill = Path()
    private val line = Path()
    private val rect = RectF()
    private var camY = Double.NaN

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        val surf = session.map.surface
        sky.color = when (surf) { Surface.MOON -> 0xFF05060A.toInt(); Surface.MARS -> 0xFF3B1E1A.toInt(); Surface.SNOW, Surface.ICE -> 0xFFB8CFE0.toInt(); else -> 0xFF86C5EA.toInt() }
        ground.color = when (surf) {
            Surface.DIRT -> 0xFF7A5230; Surface.SAND -> 0xFFD9B26A; Surface.SNOW -> 0xFFEAF2F7; Surface.ICE -> 0xFFBFE3F0
            Surface.MUD -> 0xFF4B3A2A; Surface.MOON -> 0xFF8C8C92; Surface.ROCK -> 0xFF6B6B70; Surface.ASPHALT -> 0xFF3A3D42
            Surface.SALT -> 0xFFE9E6DC; Surface.MARS -> 0xFFB5532F
        }.toInt()
        edge.color = 0xFF2E7D32.toInt().takeIf { surf == Surface.DIRT } ?: 0xFFFFFFFF.toInt()
        edge.style = Paint.Style.STROKE; edge.strokeWidth = 0.22f; edge.strokeJoin = Paint.Join.ROUND
        body.color = 0xFFD7263D.toInt(); trim.color = 0xFF111111.toInt()
        tire.color = 0xFF151515.toInt(); hub.color = 0xFFCCCCCC.toInt()
        coinP.color = 0xFFFFD21F.toInt(); canP.color = 0xFFE53935.toInt()
        hudBg.color = 0x99000000.toInt()
        hudInk.color = 0xFFFFFFFF.toInt(); hudInk.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        val s = session; val car = s.car; val d = car.def
        c.drawRect(0f, 0f, w, h, sky)
        if (camY.isNaN()) camY = car.y
        camY += (car.y - camY) * 0.12
        val sc = h / 22f                          // piksel / metre
        val halfW = w / sc / 2.0
        val camX = car.x + w / sc * 0.18          // araç ekranın solundan yaklaşık üçte bir
        val x0 = floor(camX - halfW) - 2.0; val x1 = camX + halfW + 2.0
        val bottom = camY - h / sc / 2.0 - 6.0

        c.save()
        c.translate(w / 2f, h / 2f); c.scale(sc, -sc); c.translate(-camX.toFloat(), -camY.toFloat())
        // zemin
        fill.rewind(); line.rewind()
        fill.moveTo(x0.toFloat(), bottom.toFloat())
        var x = x0; var first = true
        while (x <= x1) {
            val y = s.terrain.height(x).toFloat()
            fill.lineTo(x.toFloat(), y)
            if (first) { line.moveTo(x.toFloat(), y); first = false } else line.lineTo(x.toFloat(), y)
            x += 0.5
        }
        fill.lineTo(x1.toFloat(), bottom.toFloat()); fill.close()
        c.drawPath(fill, ground); c.drawPath(line, edge)
        // altınlar ve yakıt bidonları
        for (k in s.terrain.coins) {
            if (k.taken || k.x < x0 || k.x > x1) continue
            c.drawCircle(k.x.toFloat(), k.y.toFloat(), 0.42f, coinP)
        }
        for (f in s.terrain.fuels) {
            if (f.taken || f.x < x0 || f.x > x1) continue
            rect.set((f.x - 0.4).toFloat(), (f.y - 0.5).toFloat(), (f.x + 0.4).toFloat(), (f.y + 0.5).toFloat())
            c.drawRoundRect(rect, 0.12f, 0.12f, canP)
        }
        // araç gövdesi: dönmüş dikdörtgen + kafa
        c.save()
        c.translate(car.x.toFloat(), car.y.toFloat()); c.rotate(Math.toDegrees(car.angle).toFloat())
        rect.set((d.rearX - 0.3).toFloat(), (d.axleY - 0.05).toFloat(), (d.frontX + 0.3).toFloat(), (d.axleY + 0.75).toFloat())
        c.drawRoundRect(rect, 0.15f, 0.15f, body)
        c.drawCircle(d.headX.toFloat(), d.headY.toFloat(), d.headR.toFloat(), trim)
        c.restore()
        // tekerlekler
        for (wh in arrayOf(car.rear, car.front)) {
            c.drawCircle(wh.x.toFloat(), wh.y.toFloat(), d.wheelR.toFloat(), tire)
            c.drawCircle(wh.x.toFloat(), wh.y.toFloat(), (d.wheelR * 0.45).toFloat(), hub)
        }
        c.restore()

        // gösterge: büyük yazı
        val ts = h * 0.085f
        hudInk.textSize = ts
        val pad = h * 0.03f
        hudInk.textAlign = Paint.Align.LEFT
        val dist = "${s.distance.toInt()} m"
        rect.set(pad, pad, pad + hudInk.measureText(dist) + pad * 2, pad + ts * 1.4f)
        c.drawRoundRect(rect, pad, pad, hudBg)
        c.drawText(dist, pad * 2, pad + ts * 1.05f, hudInk)
        val coins = "Altın ${s.coins}"
        val cw = hudInk.measureText(coins)
        rect.set(w - pad * 3 - cw, pad, w - pad, pad + ts * 1.4f)
        c.drawRoundRect(rect, pad, pad, hudBg)
        c.drawText(coins, w - pad * 2 - cw, pad + ts * 1.05f, hudInk)
        // yakıt çubuğu (üst orta)
        val bw = w * 0.26f; val bx = (w - bw) / 2f; val by = pad
        rect.set(bx, by, bx + bw, by + ts * 1.4f)
        c.drawRoundRect(rect, pad, pad, hudBg)
        val pct = s.fuelPct()
        bar.color = when { pct <= 10 -> 0xFFE53935.toInt(); pct <= 25 -> 0xFFFFB300.toInt(); else -> 0xFF43A047.toInt() }
        rect.set(bx + pad * 0.5f, by + pad * 0.5f, bx + pad * 0.5f + (bw - pad) * pct / 100f, by + ts * 1.4f - pad * 0.5f)
        c.drawRoundRect(rect, pad * 0.5f, pad * 0.5f, bar)
        hudInk.textAlign = Paint.Align.CENTER
        hudInk.textSize = ts * 0.8f
        c.drawText("Yakıt %$pct", bx + bw / 2f, by + ts * 1.05f, hudInk)
    }
}

/**
 * Pedal. Parmak basılıyken basılı kalır. Pedal kilidi açıksa kısa dokunuş pedalı sabitler ya da bırakır.
 * Dokunarak keşfet açıkken çift dokun-ve-basılı-tut gerçek bir ACTION_DOWN gönderir (basılı tutma);
 * çift dokunma ise ACTION_CLICK erişilebilirlik eylemidir ve pedalı sabitler / bırakır.
 */
class PedalView(ctx: Context, val label: String) : TextView(ctx) {
    var held = false; private set
    var latched = false
    var latchOn: () -> Boolean = { false }
    /** Bu pedal etkinleşince (öbür pedalın kilidini bırakmak için). */
    var onActivate: () -> Unit = {}
    var onChange: () -> Unit = {}
    private var downAt = 0L
    private var wasLatched = false

    val active: Boolean get() = held || latched

    init {
        text = label; contentDescription = label
        isClickable = true; isFocusable = true
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    fun unlatch() { if (latched) { latched = false; changed() } }

    fun release() { held = false; latched = false; changed() }

    private fun changed() {
        if (Build.VERSION.SDK_INT >= 30) stateDescription = if (active) "basılı" else "bırakılmış"
        else contentDescription = if (active) "$label, basılı" else label   // eski sürümlerde durum açıklaması yok
        onChange()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downAt = e.eventTime; wasLatched = latched; held = true; isPressed = true
                onActivate(); changed()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                held = false; isPressed = false
                val quick = e.eventTime - downAt < 320
                latched = if (latchOn() && quick && e.actionMasked == MotionEvent.ACTION_UP) !wasLatched else false
                if (latched) onActivate()
                changed()
            }
        }
        return true
    }

    override fun performClick(): Boolean { toggle(); return true }

    private fun toggle() {
        latched = !latched
        if (latched) onActivate()
        changed()
    }

    override fun performAccessibilityAction(action: Int, arguments: android.os.Bundle?): Boolean {
        if (action == AccessibilityNodeInfo.ACTION_CLICK) { toggle(); return true }
        return super.performAccessibilityAction(action, arguments)
    }
}
