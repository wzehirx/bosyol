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
import android.view.Choreographer
import android.view.Gravity
import android.view.KeyEvent
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
import com.yokus.app.core.EndReason
import com.yokus.app.core.GameSession
import com.yokus.app.core.Sonify
import com.yokus.app.core.Maps
import com.yokus.app.core.Mixer
import com.yokus.app.core.SampleVoice
import com.yokus.app.core.UpgradeKind
import com.yokus.app.core.Vehicles
import com.yokus.app.core.upgradeCost
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class MainActivity : Activity() {
    private lateinit var store: Store
    private lateinit var bank: SoundBank
    private lateinit var speaker: Speaker
    private val mix = Mixer()
    private lateinit var audio: AudioOut
    private lateinit var root: FrameLayout
    private val ui = Handler(Looper.getMainLooper())

    private enum class Scr { MENU, VEHICLES, MAPS, GARAGE, SETTINGS, HELP, EXIT, GAME, PAUSE, OVER, CRASH }
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

    override fun onResume() { super.onResume(); audio.start() }   // oyundaysak Duraklat ekranında kalırız
    override fun onPause() {
        if (screen == Scr.GAME) { if (session?.over == true) showEnd() else pauseGame() }
        bankProgress()   // uygulama arka planda sistem tarafından öldürülürse kazanılan altın kaybolmasın
        audio.stop(); super.onPause()
    }
    override fun onDestroy() {
        stopLoop(); sonify?.stop(); sonify = null
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
            Scr.GAME -> pauseGame()
            Scr.PAUSE -> resumeGame()
            Scr.OVER -> toMenu()
            else -> go(Scr.MENU)
        }
    }

    private fun render() {
        when (screen) {
            Scr.MENU -> menu(); Scr.VEHICLES -> vehicles(); Scr.MAPS -> maps(); Scr.GARAGE -> garage()
            Scr.SETTINGS -> settings(); Scr.HELP -> help(); Scr.EXIT -> confirmExit(); Scr.GAME -> game(); Scr.PAUSE -> pauseScreen(); Scr.OVER -> overScreen()
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
        c.addView(sw("Denge yardımı", a.balanceAssist) { a.balanceAssist = it; save() }, lp(top = 4))
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


    /* ================= Oyun ================= */
    private var session: GameSession? = null
    /** Testler için. */
    val gameSession: GameSession? get() = session
    private var sonify: Sonify? = null
    private var gameView: GameView? = null
    private var brakePedal: PedalView? = null
    private var gasPedal: PedalView? = null
    private var keyGas = false
    private var keyBrake = false
    private var gasCur = 0.0
    private var looping = false
    private var lastFrame = 0L
    private var overT = 0.0
    private var settled = false
    private var resMeters = 0
    private var resCoins = 0
    private var resBest = 0
    private var resRecord = false

    /** Testler false yaparak döngüyü kendisi [tick] ile sürer. */
    var autoLoop = true

    /** Oyunu (yeniden) başlatır. */
    fun startGame() {
        leaveGame()
        val v = Vehicles.byId(store.vehicleId)
        val s = GameSession(v, store.upgrades(v.id), Maps.byId(store.mapId))
        val so = Sonify(mix, bank, store.audio) { speaker.say(it) }
        so.start(s)
        session = s; sonify = so
        settled = false; overT = 0.0; gasCur = 0.0; credited = 0; bestAtStart = store.best(s.map.id)
        warmSounds()
        screen = Scr.GAME
        introSay = true
        render()
    }
    private var introSay = false

    /** Oyun sırasında ilk kez kullanılacak kısa sesleri önceden çözer (UI iş parçacığında takılma olmasın). */
    private fun warmSounds() {
        val b = bank
        val t = Thread { for (k in arrayOf("coin", "coin_s", "coin_s2", "coin_s3", "coin_m", "coin_l", "coin_xl", "fuel_pick", "fuel_low", "land_thud", "creak", "flip", "crash", "crash_car")) b.get(k) }
        t.isDaemon = true; t.start()
    }

    /** Açık oyunu ve ses/döngü kaynaklarını bırakır. */
    private fun leaveGame() {
        stopLoop()
        sonify?.stop(); sonify = null
        session = null; gameView = null; brakePedal = null; gasPedal = null
        keyGas = false; keyBrake = false; gasCur = 0.0
        applyVolumes()
    }

    private fun startLoop() {
        if (!autoLoop || looping) return
        looping = true; lastFrame = 0L
        Choreographer.getInstance().postFrameCallback(frameCb)
    }
    private fun stopLoop() {
        looping = false; lastFrame = 0L
        Choreographer.getInstance().removeFrameCallback(frameCb)
    }
    private val frameCb = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!looping) return
            val dt = if (lastFrame == 0L) 1.0 / 60 else (frameTimeNanos - lastFrame) / 1e9
            lastFrame = frameTimeNanos
            tick(dt)
            if (looping) Choreographer.getInstance().postFrameCallback(this)
        }
    }

    /** Bir oyun adımı: fizik, ses, çizim. Choreographer çağırır; testler de doğrudan çağırır. */
    fun tick(dt0: Double) {
        val s = session ?: return
        if (screen != Scr.GAME) return
        val dt = min(max(dt0, 0.0), 0.05)
        val gasTarget = if (gasPedal?.active == true || keyGas) 1.0 else 0.0
        val brake = if (brakePedal?.active == true || keyBrake) 1.0 else 0.0
        val step = dt / 0.12                        // gaz yaklaşık 0.12 sn'de tam açılır
        gasCur = if (gasTarget > gasCur) min(gasTarget, gasCur + step) else max(gasTarget, gasCur - step)
        s.update(dt, gasCur, brake)
        sonify?.update(dt)
        gameView?.invalidate()
        if (s.over) {
            settle()
            overT += dt
            if (overT >= 2.5) showEnd()             // çarpma sesi ve son duyuru çalsın
        }
    }

    private var credited = 0
    private var bestAtStart = 0

    /** Şimdiye dek kazanılan altını ve en iyi mesafeyi kaydeder. Tekrar çağrılabilir: her altın yalnızca bir kez eklenir. */
    private fun bankProgress() {
        val s = session ?: return
        if (s.coins > credited) { store.gold = store.gold + (s.coins - credited); credited = s.coins }
        store.setBest(s.map.id, s.distance.toInt())
    }

    /** Tur bitince sonucu bir kez kesinleştirir ve kaydeder. */
    private fun settle() {
        val s = session ?: return
        if (settled) return
        settled = true
        val m = s.distance.toInt()
        resRecord = m > bestAtStart && m > 0
        resMeters = m; resCoins = s.coins; resBest = max(bestAtStart, m)
        bankProgress()
    }

    /** Oyunu bırakır (olaylar susturulur, ek duyuru yok) ve altını kaydeder. */
    private fun quitRun() {
        val s = session ?: return
        s.events = null
        s.end(EndReason.QUIT)
        settle()
    }

    private fun showEnd() {
        stopLoop(); sonify?.stop()
        screen = Scr.OVER; render()
    }

    private fun pauseGame() {
        if (screen != Scr.GAME) return
        stopLoop()
        gasPedal?.release(); brakePedal?.release(); keyGas = false; keyBrake = false; gasCur = 0.0
        sonify?.stop()
        screen = Scr.PAUSE; render()
    }

    private fun resumeGame() {
        val s = session ?: return toMenu()
        sonify?.start(s)
        screen = Scr.GAME; render()
    }

    private fun toMenu() {
        quitRun(); leaveGame(); go(Scr.MENU)
    }

    private fun game() {
        val s = session ?: return go(Scr.MENU)
        val intro = introSay; introSay = false
        val fl = FrameLayout(this)
        val gv = GameView(this, s)
        fl.addView(gv, FrameLayout.LayoutParams(-1, -1))
        gameView = gv

        val brake = pedal("Fren", 0xFFE53935.toInt())
        val gas = pedal("Gaz", 0xFFFFC233.toInt())
        brake.onActivate = { gas.unlatch() }
        gas.onActivate = { brake.unlatch() }
        brakePedal = brake; gasPedal = gas
        syncPedal(brake, 0xFFE53935.toInt()); syncPedal(gas, 0xFFFFC233.toInt())

        val durum = btn("Durum") {
            val t = session?.statusText() ?: return@btn
            say(t)
        }
        durum.gravity = Gravity.CENTER
        val dur = btn("Duraklat") { pauseGame() }
        dur.gravity = Gravity.CENTER
        val center = LinearLayout(this); center.orientation = LinearLayout.VERTICAL; center.gravity = Gravity.CENTER
        center.setPadding(dp(12f), dp(12f), dp(12f), dp(12f))
        center.addView(durum, lp(top = 0)); center.addView(dur, lp(top = 14))

        val row = LinearLayout(this); row.orientation = LinearLayout.HORIZONTAL
        row.addView(brake, LinearLayout.LayoutParams(0, -1, 1f))
        row.addView(center, LinearLayout.LayoutParams(0, -1, 1f))
        row.addView(gas, LinearLayout.LayoutParams(0, -1, 1f))
        fl.addView(row, FrameLayout.LayoutParams(-1, -1))

        focusJob?.let { ui.removeCallbacks(it) }
        root.removeAllViews()
        root.addView(fl, FrameLayout.LayoutParams(-1, -1))
        val job = Runnable {
            gas.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
            gas.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            if (intro) ui.postDelayed({ if (screen == Scr.GAME) speaker.say("Başla! Gaz sağda, fren solda.") }, 600)
        }
        focusJob = job
        ui.postDelayed(job, 250)
        startLoop()
    }

    private fun pedal(label: String, color: Int): PedalView {
        val p = PedalView(this, label)
        p.gravity = Gravity.CENTER; p.setTextColor(cInk)
        p.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34f); p.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        p.latchOn = { store.latch }
        p.onChange = { syncPedal(p, color) }
        return p
    }

    private fun syncPedal(p: PedalView, color: Int) {
        val on = p.active
        val g = GradientDrawable(); g.cornerRadius = dp(22f).toFloat()
        g.setColor(if (on) (color and 0x00FFFFFF) or 0x80000000.toInt() else 0x331E252D)
        g.setStroke(dp(3f), if (on) color else cLine)
        p.background = g
    }

    /** Ses tuşları: açıksa yukarı = gaz, aşağı = fren (basılı tutuldukça); sistem sesi değişmez. */
    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        if (screen == Scr.GAME && store.volumeKeys) {
            val down = e.action == KeyEvent.ACTION_DOWN
            when (e.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> { keyGas = down; if (down) brakePedal?.unlatch(); return true }
                KeyEvent.KEYCODE_VOLUME_DOWN -> { keyBrake = down; if (down) gasPedal?.unlatch(); return true }
            }
        }
        return super.dispatchKeyEvent(e)
    }

    private fun pauseScreen() {
        val c = page("Duraklatıldı")
        c.addView(btn("Devam", primary = true) { resumeGame() }, lp(top = 14))
        c.addView(btn("Yeniden başla") { quitRun(); startGame() }, lp())
        c.addView(btn("Ana menü") { toMenu() }, lp())
    }

    private fun overScreen() {
        val reason = session?.endReason ?: EndReason.QUIT
        val c = page("Oyun bitti")   // başlık odaklanınca okunur; neden Sonify'dan zaten söylendi, metinde yazılı kalır
        val why = session?.endMessage?.takeIf { it.isNotEmpty() } ?: reason.say
        val rec = if (resRecord) " Yeni rekor!" else ""
        c.addView(para("$why. $resMeters metre, $resCoins altın kazandın. En iyi: $resBest metre.$rec", cInk))
        c.addView(btn("Tekrar", primary = true) { startGame() }, lp(top = 14))
        c.addView(btn("Garaj") { leaveGame(); go(Scr.GARAGE) }, lp())
        c.addView(btn("Ana menü") { toMenu() }, lp())
    }
}
