package com.yokus.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import com.yokus.app.core.BUS_FX
import com.yokus.app.core.Maps
import com.yokus.app.core.Mixer
import com.yokus.app.core.SampleVoice
import com.yokus.app.core.UpgradeKind
import com.yokus.app.core.Vehicles
import com.yokus.app.core.upgradeCost
import kotlin.math.pow

class MainActivity : Activity() {
    private lateinit var store: Store
    private lateinit var bank: SoundBank
    private lateinit var speaker: Speaker
    private val mix = Mixer()
    private lateinit var audio: AudioOut
    private lateinit var root: FrameLayout
    private val ui = Handler(Looper.getMainLooper())

    private enum class Scr { MENU, VEHICLES, MAPS, GARAGE, SETTINGS, HELP, EXIT, GAME, CRASH }
    private var screen = Scr.MENU
    private var focusTag: String? = null
    private var crashReport: String? = null
    private var focusJob: Runnable? = null
    private var pendingSay: String? = null

    /* ================= Renkler (koyu, yüksek karşıtlık) ================= */
    private val cBg = 0xFF0B0E12.toInt()
    private val cSurface = 0xFF1E252D.toInt()
    private val cLine = 0xFF5A6772.toInt()
    private val cInk = 0xFFFFFFFF.toInt()
    private val cMuted = 0xFFD2DAD6.toInt()
    private val cSign = 0xFF0D8A5F.toInt()
    private val cAmber = 0xFFFFC233.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashLog.install(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.setBackgroundColor(cBg)
        volumeControlStream = AudioManager.STREAM_MUSIC
        store = Store(this)
        bank = SoundBank(this)
        audio = AudioOut(mix)
        root = FrameLayout(this)
        root.setBackgroundColor(cBg)
        setContentView(root)
        speaker = Speaker(this)
        speaker.root = root
        applyVolumes()
        crashReport = CrashLog.read(this)
        screen = if (crashReport != null) Scr.CRASH else Scr.MENU
        render()
    }

    override fun onResume() { super.onResume(); audio.start() }
    override fun onPause() { super.onPause(); audio.stop() }
    override fun onDestroy() {
        ui.removeCallbacksAndMessages(null); audio.stop(); speaker.shutdown(); super.onDestroy()
    }

    private fun applyVolumes() {
        mix.master = ((store.audio.volMaster / 100.0).pow(1.6) * 1.1).toFloat()
        mix.busGain[BUS_FX] = 1f
    }

    /** Kısa arayüz sesi: "buy", "nope". Dosya yoksa sessiz kalır. */
    private fun uiSound(key: String) {
        val p = bank.get(key) ?: return
        mix.add(SampleVoice(p, BUS_FX, false, durSec = minOf(p.duration, 2.0), fadeOutSec = 0.2)).fadeTo(0.9f, 0.01f)
    }

    /** Hemen söyler (ekran değişmiyorsa). */
    private fun say(t: String) = speaker.say(t)
    /** Ekran yeniden çizilecekse: odak yerleştikten sonra söylenir. */
    private fun sayAfterRender(t: String) { pendingSay = t }

    /* ================= Arayüz yardımcıları ================= */
    private fun dp(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics).toInt()
    private fun bgShape(fill: Int, stroke: Int): RippleDrawable {
        val g = GradientDrawable(); g.cornerRadius = dp(14f).toFloat(); g.setColor(fill); g.setStroke(dp(2f), stroke)
        return RippleDrawable(ColorStateList.valueOf(0x40FFFFFF), g, null)
    }
    private fun lp(top: Int = 10) =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it.topMargin = dp(top.toFloat()) }

    private fun page(title: String): LinearLayout {
        val sv = ScrollView(this); sv.isFillViewport = true
        val outer = LinearLayout(this); outer.gravity = Gravity.CENTER_HORIZONTAL
        val col = LinearLayout(this); col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(24f), dp(16f), dp(24f), dp(28f))
        val w = minOf(resources.displayMetrics.widthPixels, dp(820f))
        outer.addView(col, LinearLayout.LayoutParams(w, ViewGroup.LayoutParams.WRAP_CONTENT))
        sv.addView(outer)
        root.removeAllViews()
        root.addView(sv, FrameLayout.LayoutParams(-1, -1))
        val h = heading(title, 32f); h.tag = "title"
        col.addView(h)
        val tag = focusTag; focusTag = null
        focusJob?.let { ui.removeCallbacks(it) }  // hızlı art arda ekran değişiminde eski odak işi yeni ekrana kaymasın
        val job = Runnable {
            val target: View? = (if (tag != null) root.findViewWithTag<View?>(tag) else null) ?: root.findViewWithTag<View?>("title")
            target?.let {
                it.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
                it.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            }
            // Duyuru odağın okunmasının ardından gelsin ki ikisi birbirini kesmesin
            pendingSay?.let { s -> pendingSay = null; ui.postDelayed({ speaker.say(s) }, 600) }
        }
        focusJob = job
        ui.postDelayed(job, 250)
        return col
    }

    private fun heading(text: String, size: Float = 24f): TextView {
        val t = TextView(this); t.text = text; t.setTextColor(cInk); t.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
        t.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        t.setPadding(0, dp(if (size > 26) 4f else 16f), 0, dp(4f))
        if (Build.VERSION.SDK_INT >= 28) t.isAccessibilityHeading = true
        else t.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                @Suppress("DEPRECATION") info.collectionItemInfo = AccessibilityNodeInfo.CollectionItemInfo.obtain(0, 1, 0, 1, true)
            }
        }
        t.isFocusable = true
        return t
    }
    private fun para(text: String, color: Int = cMuted): TextView {
        val t = TextView(this); t.text = text; t.setTextColor(color); t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
        t.setLineSpacing(0f, 1.2f); t.setPadding(0, dp(4f), 0, dp(4f)); t.isFocusable = true; return t
    }
    private fun btn(text: String, primary: Boolean = false, tag: String? = null, onClick: () -> Unit): Button {
        val b = Button(this); b.text = text; b.isAllCaps = false
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (primary) 24f else 20f)
        b.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        b.setTextColor(if (primary) 0xFF1D1405.toInt() else cInk)
        b.background = bgShape(if (primary) cAmber else cSurface, if (primary) cAmber else cLine)
        b.minHeight = dp(if (primary) 68f else 60f)
        b.gravity = Gravity.CENTER_VERTICAL or Gravity.START
        b.setPadding(dp(18f), dp(10f), dp(18f), dp(10f))
        b.stateListAnimator = null
        if (tag != null) b.tag = tag
        b.setOnClickListener { onClick() }
        return b
    }
    private fun sw(text: String, checked: Boolean, onChange: (Boolean) -> Unit): Switch {
        val s = Switch(this); s.text = text; s.isChecked = checked
        s.setTextColor(cInk); s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        s.minHeight = dp(60f); s.setPadding(dp(4f), dp(6f), dp(4f), dp(6f))
        s.thumbTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(cAmber, 0xFFE0E6E3.toInt()))
        s.trackTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(cSign, 0xFF5A6772.toInt()))
        s.setOnCheckedChangeListener { _, v -> onChange(v) }
        return s
    }
    private fun seek(col: LinearLayout, label: String, value: Int, onChange: (Int) -> Unit) {
        val t = TextView(this); t.text = label; t.setTextColor(cInk); t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        t.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        val sb = SeekBar(this); sb.max = 20; sb.progress = value / 5
        sb.contentDescription = label
        sb.progressTintList = ColorStateList.valueOf(cAmber); sb.thumbTintList = ColorStateList.valueOf(cAmber)
        sb.minimumHeight = dp(52f)
        fun state(v: Int) {
            if (Build.VERSION.SDK_INT >= 30) sb.stateDescription = "yüzde $v" else sb.contentDescription = "$label, yüzde $v"
        }
        state(value)
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) { val v = p * 5; state(v); onChange(v) }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {}
        })
        col.addView(t, lp(top = 14)); col.addView(sb, lp(top = 2))
    }

    /* ================= Gezinme ================= */
    private fun go(s: Scr) { screen = s; render() }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when (screen) {
            Scr.MENU -> go(Scr.EXIT)
            Scr.CRASH -> closeCrash()
            else -> go(Scr.MENU)
        }
    }

    private fun render() {
        when (screen) {
            Scr.MENU -> menu(); Scr.VEHICLES -> vehicles(); Scr.MAPS -> maps(); Scr.GARAGE -> garage()
            Scr.SETTINGS -> settings(); Scr.HELP -> help(); Scr.EXIT -> confirmExit(); Scr.GAME -> game()
            Scr.CRASH -> crash()
        }
    }

    /* ================= Ekranlar ================= */
    private fun crash() {
        val report = crashReport ?: return go(Scr.MENU)
        val c = page("Son çökme kaydı")
        c.addView(para("Uygulama geçen açılışta çöktü. Kaydı kopyalayıp gönderirsen sorun düzeltilebilir. Kayıt kişisel bilgi içermez."))
        val t = TextView(this); t.text = report; t.setTextColor(cInk); t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        t.setTextIsSelectable(true); t.isFocusable = true
        c.addView(t, lp(top = 12))
        c.addView(btn("Kopyala", primary = true) {
            try {
                (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Yokuş Ustası çökme kaydı", report))
                say("Kopyalandı")
            } catch (e: Throwable) { say("Kopyalanamadı") }
        }, lp(top = 16))
        c.addView(btn("Kaydı sil ve devam et") { closeCrash() }, lp())
    }

    private fun closeCrash() { CrashLog.clear(this); crashReport = null; go(Scr.MENU) }

    private fun menu() {
        val c = page("Yokuş Ustası")
        val v = Vehicles.byId(store.vehicleId); val m = Maps.byId(store.mapId)
        val info = para("Altın: ${store.gold}. Araç: ${v.name}. Harita: ${m.name}. En iyi: ${store.best(m.id)} metre.", cInk)
        info.tag = "gold"
        c.addView(info)
        c.addView(btn("Başla", primary = true) { startGame() }, lp(top = 14))
        c.addView(btn("Araçlar") { go(Scr.VEHICLES) }, lp())
        c.addView(btn("Haritalar") { go(Scr.MAPS) }, lp())
        c.addView(btn("Garaj") { go(Scr.GARAGE) }, lp())
        c.addView(btn("Ayarlar") { go(Scr.SETTINGS) }, lp())
        c.addView(btn("Nasıl oynanır") { go(Scr.HELP) }, lp())
        c.addView(btn("Çıkış") { go(Scr.EXIT) }, lp())
    }

    private fun confirmExit() {
        val c = page("Çıkmak istiyor musun?")
        c.addView(para("Oyundan çıkarsan ilerlemen kayıtlı kalır."))
        c.addView(btn("Evet, çık", primary = true) { finish() }, lp(top = 16))
        c.addView(btn("Vazgeç") { go(Scr.MENU) }, lp())
    }

    private fun vehicles() {
        val c = page("Araçlar")
        c.addView(para("Altın: ${store.gold}", cInk).also { it.tag = "gold" })
        for (v in Vehicles.all) {
            val label = when {
                store.owns(v.id) && store.vehicleId == v.id -> "${v.name}, sahipsin, seçili"
                store.owns(v.id) -> "${v.name}, sahipsin"
                else -> "${v.name}, ${v.price} altın. ${v.desc}"
            }
            c.addView(btn(label, tag = "v_${v.id}") {
                if (store.owns(v.id)) {
                    store.vehicleId = v.id; sayAfterRender("${v.name} seçildi")
                } else if (store.buyVehicle(v)) {
                    store.vehicleId = v.id; uiSound("buy"); sayAfterRender("Satın alındı")
                } else {
                    uiSound("nope"); sayAfterRender("Yeterli altın yok, ${v.price - store.gold} altın gerek")
                }
                focusTag = "v_${v.id}"; render()
            }, lp())
        }
        c.addView(btn("Geri", primary = true) { go(Scr.MENU) }, lp(top = 18))
    }

    private fun maps() {
        val c = page("Haritalar")
        c.addView(para("Altın: ${store.gold}", cInk).also { it.tag = "gold" })
        for (m in Maps.all) {
            val label = when {
                store.ownsMap(m.id) && store.mapId == m.id -> "${m.name}, sahipsin, seçili"
                store.ownsMap(m.id) -> "${m.name}, sahipsin"
                else -> "${m.name}, ${m.price} altın. ${m.desc}"
            }
            c.addView(btn(label, tag = "m_${m.id}") {
                if (store.ownsMap(m.id)) {
                    store.mapId = m.id; sayAfterRender("${m.name} seçildi")
                } else if (store.buyMap(m)) {
                    store.mapId = m.id; uiSound("buy"); sayAfterRender("Satın alındı")
                } else {
                    uiSound("nope"); sayAfterRender("Yeterli altın yok, ${m.price - store.gold} altın gerek")
                }
                focusTag = "m_${m.id}"; render()
            }, lp())
        }
        c.addView(btn("Geri", primary = true) { go(Scr.MENU) }, lp(top = 18))
    }

    private fun garage() {
        val v = Vehicles.byId(store.vehicleId)
        val c = page("Garaj")
        c.addView(para("Araç: ${v.name}. Altın: ${store.gold}", cInk).also { it.tag = "gold" })
        val u = store.upgrades(v.id)
        for (k in UpgradeKind.values()) {
            val lvl = Store.level(u, k)
            val label = if (lvl >= Store.MAX_LEVEL) "${k.label} seviye $lvl / ${Store.MAX_LEVEL}, en yüksek seviye"
            else "${k.label} seviye $lvl / ${Store.MAX_LEVEL}, sonraki ${upgradeCost(v, k, lvl)} altın"
            c.addView(btn(label, tag = "u_${k.name}") {
                val cur = Store.level(store.upgrades(v.id), k)
                if (cur >= Store.MAX_LEVEL) {
                    sayAfterRender("${k.label} en yüksek seviyede")
                } else if (store.buyUpgrade(v, k)) {
                    uiSound("buy"); sayAfterRender("${k.label} geliştirildi, seviye ${cur + 1}")
                } else {
                    uiSound("nope"); sayAfterRender("Yeterli altın yok, ${upgradeCost(v, k, cur) - store.gold} altın gerek")
                }
                focusTag = "u_${k.name}"; render()
            }, lp())
        }
        c.addView(btn("Geri", primary = true) { go(Scr.MENU) }, lp(top = 18))
    }

    private fun settings() {
        val a = store.audio
        val c = page("Ayarlar")
        val save = { store.saveAudio(); applyVolumes() }
        c.addView(heading("Sesler"))
        c.addView(sw("Eğim tonu", a.tiltTone) { a.tiltTone = it; save() }, lp(top = 4))
        c.addView(sw("Yol radarı", a.radar) { a.radar = it; save() }, lp(top = 4))
        c.addView(sw("Mesafe duyurusu", a.distanceCalls) { a.distanceCalls = it; save() }, lp(top = 4))
        c.addView(heading("Kontroller"))
        c.addView(sw("Ses tuşları gaz/fren", store.volumeKeys) { store.volumeKeys = it }, lp(top = 4))
        c.addView(sw("Pedal kilidi", store.latch) { store.latch = it }, lp(top = 4))
        c.addView(heading("Ses düzeyi"))
        seek(c, "Ana ses", a.volMaster) { a.volMaster = it; save() }
        seek(c, "Motor", a.volEngine) { a.volEngine = it; save() }
        seek(c, "Ortam", a.volWorld) { a.volWorld = it; save() }
        seek(c, "İşaret sesleri", a.volCues) { a.volCues = it; save() }
        c.addView(btn("Geri", primary = true) { go(Scr.MENU) }, lp(top = 22))
    }

    private fun help() {
        val c = page("Nasıl oynanır")
        listOf(
            "Amaç: arabanı tepelerden aşırıp mümkün olduğunca uzağa sürmek, yolda altın ve yakıt toplamak.",
            "Kontroller: fren ekranın sol tarafında, gaz sağ tarafında. Basılı tuttukça çalışırlar. Ayarlar'dan ses tuşları gaz/fren seçeneğini açarsan ses açma tuşu gaz, ses kısma tuşu fren olur. Geri tuşu oyunu duraklatır.",
            "Önündeki her şey sağ kulağında duyulur. Yakınsa yüksek, uzaksa kısık gelir.",
            "Eğim tonu: arabanın burnu yukarı kalktıkça ses incelir, aşağı inince kalınlaşır. Araba neredeyse takla atacak kadar yatarsa uyarı bipi duyulur ve açı arttıkça sıklaşır.",
            "Yol radarı: sürekli tık sesi duyulur. Tıklar inceliyorsa önünde yokuş yukarı, kalınlaşıyorsa yokuş aşağı var. Düz yolda orta perdede kalır.",
            "Duyurular: önünde rampa, çukur gibi bir şey varsa önceden sesli söylenir. Ekran okuyucu kapalıysa konuşma sesi kullanılır.",
            "Altınlar önünden parıltılı bir tın sesiyle haber verir. Sesin geldiği yöne gidince altını toplarsın. Yakıt bidonu ise iki notalı, yükselen bir işaret çalar.",
            "Yakıt: gaza bastıkça yakıt azalır. Yüzde yirminin altında uyarı gelir. Yakıt biterse yavaşlar ve oyun biter. Yakıt bidonlarını toplayarak depoyu doldurabilirsin.",
            "Takla: havada tam bir tur dönüp güvenle inersen bonus altın kazanırsın. Ters dönüp kafan yere değerse kaza olur ve oyun biter.",
            "Kazandığın altınla Araçlar ve Haritalar ekranlarından yenilerini alabilir, Garaj'da aracını geliştirebilirsin."
        ).forEach { c.addView(para(it)) }
        c.addView(btn("Geri", primary = true) { go(Scr.MENU) }, lp(top = 18))
    }

    /** Oyun ekranı: sonraki görev burayı gerçek oyunla değiştirir. */
    fun startGame() { go(Scr.GAME) }

    private fun game() {
        val c = page("Oyun")
        c.addView(para("Oyun ekranı henüz hazır değil."))
        c.addView(btn("Geri", primary = true) { go(Scr.MENU) }, lp(top = 18))
    }
}
