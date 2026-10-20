package com.bosyol.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import com.bosyol.app.core.BUS_FX
import com.bosyol.app.core.EnvDef
import com.bosyol.app.core.Envs
import com.bosyol.app.core.Game
import com.bosyol.app.core.Mixer
import com.bosyol.app.core.SampleVoice
import com.bosyol.app.core.Slots
import com.bosyol.app.core.Speaker
import com.bosyol.app.core.VehicleDef
import com.bosyol.app.core.Vehicles
import com.bosyol.app.ui.RoadView
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

class MainActivity : Activity(), Speaker {
    private lateinit var prefs: Prefs
    private lateinit var bank: SoundBank
    private val mix = Mixer()
    private lateinit var audio: AudioOut
    private lateinit var root: FrameLayout
    private val ui = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private enum class Scr { MENU, VEHICLES, ENVS, SETTINGS, SOUNDS, GARAGE, EDIT, HELP, LOADING, DRIVE }
    private val stack = ArrayList<Scr>()
    private var editId: String? = null
    private var importKey: String? = null
    private var importLabel = ""
    private var focusTag: String? = null

    // Sürüş
    @Volatile private var game: Game? = null
    @Volatile private var paused = false
    @Volatile private var loopRun = false
    private var loopThread: Thread? = null
    private var driveView: FrameLayout? = null
    private var pauseView: View? = null
    private var driveControls: View? = null
    private var hud: TextView? = null
    private var gasPedal: TextView? = null
    private var brakePedal: TextView? = null
    private var autoBtn: Button? = null
    private var winBtn: Button? = null
    private var roadView: RoadView? = null

    /* ================= Renkler ================= */
    private val cBg = 0xFF12161B.toInt()
    private val cSurface = 0xFF1E252D.toInt()
    private val cLine = 0xFF3A4550.toInt()
    private val cInk = 0xFFF1F4F2.toInt()
    private val cMuted = 0xFFB4BEB9.toInt()
    private val cSign = 0xFF0D6B4A.toInt()
    private val cAmber = 0xFFFFB627.toInt()
    private val cBrake = 0xFFFF6B5B.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashLog.install(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.setBackgroundColor(cBg)
        volumeControlStream = AudioManager.STREAM_MUSIC
        prefs = Prefs(this)
        bank = SoundBank(this)
        audio = AudioOut(mix)
        root = FrameLayout(this)
        root.setBackgroundColor(cBg)
        setContentView(root)
        tts = TextToSpeech(this) { st ->
            if (st == TextToSpeech.SUCCESS) { tts?.language = Locale("tr", "TR"); ttsReady = true }
        }
        menuVolumes()
        go(Scr.MENU)
        CrashLog.read(this)?.let { report -> showCrash(report) }
    }

    /** Önceki çökmenin raporu: okunabilir ve paylaşılabilir */
    private fun showCrash(report: String) {
        val c = page("Önceki açılışta uygulama çöktü")
        c.addView(para("Aşağıdaki raporu paylaşırsan sorunu düzeltebilirim. Rapor kişisel bilgi içermez."))
        val t = TextView(this); t.text = report; t.setTextColor(cInk); t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        t.setTextIsSelectable(true); t.isFocusable = true
        c.addView(t, lp(top = 12))
        c.addView(btn("Raporu paylaş", primary = true) {
            val i = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, report).putExtra(Intent.EXTRA_SUBJECT, "Boş Yol çökme raporu")
            try { startActivity(Intent.createChooser(i, "Raporu paylaş")) } catch (e: Exception) {}
        }, lp(top = 16))
        c.addView(btn("Raporu sil ve devam et") { CrashLog.clear(this); render() }, lp())
    }

    override fun onResume() { super.onResume(); audio.start() }
    override fun onPause() {
        super.onPause()
        if (game != null && !paused) pauseDrive()
        audio.stop()
    }
    override fun onDestroy() { endDrive(); tts?.shutdown(); super.onDestroy() }

    private fun menuVolumes() {
        mix.master = ((prefs.game.volMaster / 100.0).pow(1.6) * 1.1).toFloat()
        mix.busGain[BUS_FX] = 1f
    }

    /* ================= Duyurular ================= */
    override fun say(text: String, urgent: Boolean) {
        ui.post {
            val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
            if (am.isEnabled && am.isTouchExplorationEnabled) root.announceForAccessibility(text)
            else if (prefs.tts && ttsReady) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "bosyol")
        }
    }

    /* ================= Arayüz yardımcıları ================= */
    private fun dp(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics).toInt()
    private fun bgShape(fill: Int, stroke: Int, radius: Float = 14f): RippleDrawable {
        val g = GradientDrawable(); g.cornerRadius = dp(radius).toFloat(); g.setColor(fill); g.setStroke(dp(1.5f), stroke)
        return RippleDrawable(ColorStateList.valueOf(0x40FFFFFF), g, null)
    }
    private fun lp(w: Int = ViewGroup.LayoutParams.MATCH_PARENT, h: Int = ViewGroup.LayoutParams.WRAP_CONTENT, top: Int = 10) =
        LinearLayout.LayoutParams(w, h).also { it.topMargin = dp(top.toFloat()) }

    private fun page(title: String): LinearLayout {
        val sv = ScrollView(this); sv.isFillViewport = true
        val outer = LinearLayout(this); outer.gravity = Gravity.CENTER_HORIZONTAL
        val col = LinearLayout(this); col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(24f), dp(20f), dp(24f), dp(28f))
        val w = minOf(resources.displayMetrics.widthPixels, dp(720f))
        outer.addView(col, LinearLayout.LayoutParams(w, ViewGroup.LayoutParams.WRAP_CONTENT))
        sv.addView(outer)
        setPage(sv)
        val h = heading(title, 30f); h.tag = "title"
        col.addView(h)
        return col
    }

    private fun setPage(v: View) {
        root.removeAllViews()
        root.addView(v, FrameLayout.LayoutParams(-1, -1))
        val tag = focusTag; focusTag = null
        ui.postDelayed({
            val target: View? = (if (tag != null) root.findViewWithTag<View?>(tag) else null) ?: root.findViewWithTag<View?>("title")
            target?.let {
                it.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
                it.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_FOCUSED)
            }
        }, 250)
    }

    private fun heading(text: String, size: Float = 22f): TextView {
        val t = TextView(this); t.text = text; t.setTextColor(cInk); t.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
        t.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        t.setPadding(0, dp(if (size > 24) 4f else 18f), 0, dp(4f))
        if (Build.VERSION.SDK_INT >= 28) t.isAccessibilityHeading = true
        t.isFocusable = true
        return t
    }
    private fun para(text: String): TextView {
        val t = TextView(this); t.text = text; t.setTextColor(cMuted); t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
        t.setLineSpacing(0f, 1.2f); t.setPadding(0, dp(4f), 0, dp(4f)); return t
    }
    private fun btn(text: String, primary: Boolean = false, tag: String? = null, onClick: () -> Unit): Button {
        val b = Button(this); b.text = text; b.isAllCaps = false
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (primary) 21f else 18f)
        b.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        b.setTextColor(if (primary) 0xFF1D1405.toInt() else cInk)
        b.background = bgShape(if (primary) cAmber else cSurface, if (primary) cAmber else cLine)
        b.minHeight = dp(if (primary) 64f else 56f)
        b.gravity = Gravity.CENTER_VERTICAL or Gravity.START
        b.setPadding(dp(18f), dp(10f), dp(18f), dp(10f))
        b.stateListAnimator = null
        if (tag != null) b.tag = tag
        b.setOnClickListener { onClick() }
        return b
    }
    private fun sw(text: String, checked: Boolean, onChange: (Boolean) -> Unit): Switch {
        val s = Switch(this); s.text = text; s.isChecked = checked
        s.setTextColor(cInk); s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        s.minHeight = dp(56f); s.setPadding(dp(4f), dp(6f), dp(4f), dp(6f))
        s.thumbTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(cAmber, 0xFFBFC6C2.toInt()))
        s.trackTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(cSign, 0xFF4A535B.toInt()))
        s.setOnCheckedChangeListener { _, v -> onChange(v) }
        return s
    }
    private fun seek(col: LinearLayout, label: String, value: Int, onChange: (Int) -> Unit) {
        val t = TextView(this); t.text = label; t.setTextColor(cInk); t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        t.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        val sb = SeekBar(this); sb.max = 20; sb.progress = value / 5
        sb.contentDescription = label
        sb.progressTintList = ColorStateList.valueOf(cAmber); sb.thumbTintList = ColorStateList.valueOf(cAmber)
        sb.minimumHeight = dp(48f)
        fun state(v: Int) { if (Build.VERSION.SDK_INT >= 30) sb.stateDescription = "$v yüzde" }
        state(value)
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) { val v = p * 5; state(v); onChange(v) }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {}
        })
        col.addView(t, lp(top = 14)); col.addView(sb, lp(top = 2))
    }
    private fun radios(col: LinearLayout, items: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
        val rg = RadioGroup(this); rg.orientation = RadioGroup.VERTICAL
        for ((id, text) in items) {
            val rb = RadioButton(this); rb.text = text; rb.id = View.generateViewId(); rb.tag = "r_$id"
            rb.setTextColor(cInk); rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f); rb.minHeight = dp(56f)
            rb.buttonTintList = ColorStateList.valueOf(cAmber)
            rb.setPadding(dp(8f), dp(8f), dp(8f), dp(8f))
            rg.addView(rb)
            if (id == selected) rb.isChecked = true
            rb.setOnCheckedChangeListener { _, c -> if (c) onSelect(id) }
        }
        col.addView(rg, lp(top = 6))
    }

    /* ================= Gezinme ================= */
    private fun go(s: Scr) { stack.add(s); render() }
    private fun back() { if (stack.size > 1) { stack.removeAt(stack.size - 1); render() } else finish() }
    private fun replaceTop(s: Scr) { if (stack.isNotEmpty()) stack.removeAt(stack.size - 1); go(s) }
    private fun render() {
        when (stack.last()) {
            Scr.MENU -> menu(); Scr.VEHICLES -> vehicles(); Scr.ENVS -> envs(); Scr.SETTINGS -> settings()
            Scr.SOUNDS -> sounds(); Scr.GARAGE -> garage(); Scr.EDIT -> editVehicle(); Scr.HELP -> help()
            Scr.LOADING -> loading(); Scr.DRIVE -> showDrive()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            stack.last() == Scr.DRIVE -> if (paused) resumeDrive() else pauseDrive()
            stack.last() == Scr.LOADING -> {}
            else -> back()
        }
    }

    /* ================= Ekranlar ================= */
    private fun vehicleDef(id: String): VehicleDef? {
        Vehicles.builtIn.firstOrNull { it.id == id }?.let { return it }
        val cv = prefs.customVehicles().firstOrNull { "custom_${it.id}" == id } ?: return null
        return if (bank.isCustom(cv.prefix + "_loop")) cv.def() else null
    }
    private fun currentVehicle() = vehicleDef(prefs.vehicle) ?: Vehicles.sedan
    private fun currentEnv(): EnvDef = Envs.byId(prefs.env)

    private fun menu() {
        val c = page("Boş Yol")
        c.addView(para("Kulakla sürülen sakin bir yol oyunu. Motor, lastikler, rüzgâr ve geçen araçlar etrafında üç boyutlu duyulur. Kulaklık takman önerilir."))
        c.addView(btn("Sürüşe başla", primary = true) { startDrive() }, lp(top = 16))
        c.addView(btn("Araç: ${currentVehicle().name}") { go(Scr.VEHICLES) }, lp())
        c.addView(btn("Ortam: ${currentEnv().name}") { go(Scr.ENVS) }, lp())
        c.addView(btn("Ayarlar") { go(Scr.SETTINGS) }, lp())
        c.addView(btn("Garaj: kendi araçların") { go(Scr.GARAGE) }, lp())
        c.addView(btn("Sesleri değiştir") { go(Scr.SOUNDS) }, lp())
        c.addView(btn("Nasıl oynanır") { go(Scr.HELP) }, lp())
        c.addView(btn("Çıkış") { finish() }, lp())
    }

    private fun vehicles() {
        val c = page("Araç seç")
        val items = ArrayList<Pair<String, String>>()
        Vehicles.builtIn.forEach { items.add(it.id to "${it.name}. ${it.desc}") }
        prefs.customVehicles().filter { bank.isCustom(it.prefix + "_loop") }.forEach {
            items.add("custom_${it.id}" to "${it.name}. Kendi aracın, ${Vehicles.presetNames[it.preset]}")
        }
        radios(c, items, if (vehicleDef(prefs.vehicle) != null) prefs.vehicle else "sedan") { prefs.vehicle = it }
        if (items.size == Vehicles.builtIn.size) c.addView(para("Kendi motor sesinle araç eklemek için ana menüdeki Garaj bölümünü kullan."))
        c.addView(btn("Tamam", primary = true) { back() }, lp(top = 18))
    }

    private fun envs() {
        val c = page("Ortam seç")
        radios(c, Envs.list.map { it.id to "${it.name}. ${it.desc}" }, prefs.env) { prefs.env = it }
        c.addView(btn("Tamam", primary = true) { back() }, lp(top = 18))
    }

    private fun settings() {
        val g = prefs.game
        val c = page("Ayarlar")
        val save = { prefs.saveGame(); game?.applyVolumes() ?: menuVolumes() }
        c.addView(heading("Sürüş"))
        c.addView(sw("Trafik: yanından geçen araçlar", g.traffic) { g.traffic = it; save() }, lp(top = 4))
        c.addView(sw("Pedala tek dokunuş pedalı sabitlesin, ikinci dokunuş bıraksın", g.latch) { g.latch = it; save() }, lp(top = 4))
        c.addView(sw("Telefonun ses tuşlarıyla sür: ses açma gaz, ses kısma fren", prefs.volKeys) { prefs.volKeys = it }, lp(top = 4))
        c.addView(heading("Duyurular"))
        c.addView(sw("Virajları önceden söyle", g.pace) { g.pace = it; save() }, lp(top = 4))
        c.addView(sw("Hızı her 20 kilometrede söyle", g.callouts) { g.callouts = it; save() }, lp(top = 4))
        c.addView(sw("Vites değişimini söyle", g.shiftSay) { g.shiftSay = it; save() }, lp(top = 4))
        c.addView(sw("Ekran okuyucu kapalıyken duyuruları sesli oku", prefs.tts) { prefs.tts = it }, lp(top = 4))
        c.addView(heading("Ses düzeyi"))
        seek(c, "Ana ses", g.volMaster) { g.volMaster = it; save() }
        seek(c, "Motor sesi", g.volEngine) { g.volEngine = it; save() }
        seek(c, "Ortam ve trafik sesi", g.volWorld) { g.volWorld = it; save() }
        c.addView(btn("Tamam", primary = true) { back() }, lp(top = 22))
    }

    private fun help() {
        val c = page("Nasıl oynanır")
        listOf(
            "Fren ekranın sol kenarında, gaz sağ kenarında. Basılı tuttukça çalışırlar.",
            "Ekran okuyucu açıkken pedala çift dokunman, Ayarlar'daki sabitleme açıksa pedalı sabitler. Tekrar çift dokunmak bırakır. Sabitleme kapalıysa çift dokunuş kısa bir basış olur.",
            "Ayarlar'dan ses tuşlarıyla sürüşü açarsan ses açma tuşu gaz, ses kısma tuşu fren olur ve basılı tuttuğun sürece çalışır.",
            "Fren basılı tutuldukça basınç artar: önce yumuşak yavaşlar, uzun basarsan sert durur.",
            "Direksiyon otomatik. Yardımcı pilot virajları önceden söyler ve virajın yönünden bir zil çalar. Viraja hızlı girersen lastikler öter.",
            "Otomatik sürüş açıkken araç hedef hızı kendisi tutar ve virajlarda yavaşlar. Pedallardan birine basınca kapanır.",
            "Geri tuşu sürüşü duraklatır. Duraklatma ekranından trafiği ve pedal sabitlemeyi değiştirebilir, ana menüye dönebilirsin.",
            "Garaj'da kendi motor sesinle istediğin kadar araç ekleyebilirsin. Sesleri değiştir bölümünde oyundaki her sesi kendi kaydınla değiştirebilirsin."
        ).forEach { c.addView(para(it)) }
        c.addView(btn("Tamam", primary = true) { back() }, lp(top = 18))
    }

    /* ---------- Garaj ---------- */
    private fun garage() {
        val c = page("Garaj")
        c.addView(para("Kendi motor sesinle istediğin kadar araç ekleyebilirsin. Bir araç, motor sesi yüklendiğinde araç listesinde görünür."))
        val list = prefs.customVehicles()
        if (list.isEmpty()) c.addView(para("Henüz kendi aracın yok."))
        list.forEach { cv ->
            val ready = bank.isCustom(cv.prefix + "_loop")
            c.addView(btn("${cv.name}, ${if (ready) "hazır" else "motor sesi yok"}", tag = "v_${cv.id}") { editId = cv.id; go(Scr.EDIT) }, lp())
        }
        c.addView(btn("Yeni araç ekle", primary = true) {
            val all = prefs.customVehicles()
            val id = java.lang.Long.toString(System.currentTimeMillis() % 2176782336L, 36)
            all.add(CustomVehicle(id, "Aracım ${all.size + 1}", "binek", 3000)); prefs.saveCustomVehicles(all)
            editId = id; go(Scr.EDIT)
        }, lp(top = 18))
        c.addView(btn("Tamam") { back() }, lp())
    }

    private fun editVehicle() {
        val all = prefs.customVehicles()
        val cv = all.firstOrNull { it.id == editId } ?: run { back(); return }
        val persist = { val l = prefs.customVehicles(); val i = l.indexOfFirst { it.id == cv.id }; if (i >= 0) l[i] = cv else l.add(cv); prefs.saveCustomVehicles(l) }
        val c = page("Araç: ${cv.name}")
        val nl = TextView(this); nl.text = "Araç adı"; nl.setTextColor(cInk); nl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        val et = EditText(this); et.setText(cv.name); et.setTextColor(cInk); et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
        et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES; et.id = View.generateViewId()
        et.backgroundTintList = ColorStateList.valueOf(cAmber); et.minHeight = dp(56f)
        nl.labelFor = et.id
        et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { val n = s?.toString()?.trim().orEmpty(); if (n.isNotEmpty()) { cv.name = n; persist() } }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, d: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, d: Int) {}
        })
        c.addView(nl, lp(top = 8)); c.addView(et, lp(top = 2))
        c.addView(heading("Sürüş karakteri"))
        radios(c, Vehicles.presetNames.map { it.key to it.value }, cv.preset) { cv.preset = it; persist() }
        val rl = TextView(this); rl.setTextColor(cInk); rl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        rl.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        val rs = SeekBar(this); rs.max = 45; rs.progress = (cv.rpm - 1500) / 100
        rs.progressTintList = ColorStateList.valueOf(cAmber); rs.thumbTintList = ColorStateList.valueOf(cAmber); rs.minimumHeight = dp(48f)
        fun rpmText() { rl.text = "Motor sesinin kaydedildiği devir: ${cv.rpm}"; rs.contentDescription = "Motor sesinin kaydedildiği devir"; if (Build.VERSION.SDK_INT >= 30) rs.stateDescription = "${cv.rpm} devir" }
        rpmText()
        rs.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) { cv.rpm = 1500 + p * 100; rpmText(); persist() }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {}
        })
        c.addView(rl, lp(top = 16)); c.addView(rs, lp(top = 2))
        c.addView(para("Emin değilsen 3000 bırak. Kayıt rölantiye yakınsa düşür, yüksek devirse artır."))
        c.addView(heading("Sesler"))
        for ((part, label) in Slots.customParts) {
            val key = "${cv.prefix}_$part"
            val has = bank.isCustom(key)
            c.addView(btn("$label: ${if (has) "yüklendi" else "boş"}", tag = "s_$key") { pick(key, label) }, lp())
            if (has) {
                val row = LinearLayout(this); row.orientation = LinearLayout.HORIZONTAL
                row.addView(btn("Dinle") { preview(key) }, LinearLayout.LayoutParams(0, -2, 1f).also { it.rightMargin = dp(8f) })
                row.addView(btn("Kaldır") { bank.reset(key); say("Kaldırıldı"); focusTag = "s_$key"; render() }, LinearLayout.LayoutParams(0, -2, 1f))
                // Düğme adlarının hangi sese ait olduğu duyulsun
                (row.getChildAt(0) as Button).contentDescription = "Dinle: $label"
                (row.getChildAt(1) as Button).contentDescription = "Kaldır: $label"
                c.addView(row, lp(top = 6))
            }
        }
        c.addView(btn("Tamam", primary = true) { back() }, lp(top = 22))
        c.addView(btn("Aracı sil") {
            AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("${cv.name} silinsin mi?").setMessage("Aracın kendi sesleri de silinir.")
                .setPositiveButton("Sil") { _, _ ->
                    Slots.customParts.forEach { bank.reset("${cv.prefix}_${it.first}") }
                    prefs.saveCustomVehicles(prefs.customVehicles().filter { it.id != cv.id })
                    if (prefs.vehicle == "custom_${cv.id}") prefs.vehicle = "sedan"
                    say("Araç silindi"); back()
                }.setNegativeButton("Vazgeç", null).show()
        }, lp())
    }

    /* ---------- Sesleri değiştir ---------- */
    private fun sounds() {
        val c = page("Sesleri değiştir")
        c.addView(para("Bir sese dokun, sonra kendi dosyanı seç. MP3, WAV, OGG, M4A ve FLAC çalışır, boyut sınırı yoktur. Döngü sesleri kesintisiz dönecek şekilde birleştirilir."))
        var group = ""
        for (s in Slots.all) {
            if (s.group != group) { group = s.group; c.addView(heading(group)) }
            val custom = bank.isCustom(s.key)
            c.addView(btn(s.label + if (custom) ", senin sesin" else "", tag = "s_${s.key}") { soundMenu(s.key, s.label) }, lp(top = 6))
        }
        c.addView(btn("Tamam", primary = true) { back() }, lp(top = 22))
    }

    private fun soundMenu(key: String, label: String) {
        val custom = bank.isCustom(key)
        val items = ArrayList<String>(); items.add("Dosyadan içe aktar"); items.add("Dinle"); if (custom) items.add("Varsayılana döndür")
        AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert).setTitle(label)
            .setItems(items.toTypedArray()) { _, i ->
                when (items[i]) {
                    "Dosyadan içe aktar" -> pick(key, label)
                    "Dinle" -> preview(key)
                    else -> { bank.reset(key); say("Varsayılana döndürüldü: $label"); focusTag = "s_$key"; render() }
                }
            }.setNegativeButton("Vazgeç", null).show()
    }

    private fun pick(key: String, label: String) {
        importKey = key; importLabel = label
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("audio/*")
        i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*", "application/ogg", "application/octet-stream"))
        try { startActivityForResult(i, 7) } catch (e: Exception) { say("Dosya seçici açılamadı.", true) }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 7) return
        val key = importKey ?: return
        val uri = data?.data
        if (resultCode != RESULT_OK || uri == null) return
        say("İçe aktarılıyor")
        Thread {
            val ok = bank.import(key, uri)
            ui.post {
                say(if (ok) "İçe aktarıldı: $importLabel" else "Bu dosya açılamadı. Başka bir ses dosyası seç.", !ok)
                focusTag = "s_$key"; render()
            }
        }.start()
    }

    private var previewVoice: SampleVoice? = null
    private fun preview(key: String) {
        Thread {
            val p = bank.get(key)
            if (p == null) { say("Bu yuvada çalınacak ses yok."); return@Thread }
            previewVoice?.stop(0.05f)
            menuVolumes()
            previewVoice = mix.add(SampleVoice(p, BUS_FX, false, durSec = minOf(p.duration, 6.0), fadeOutSec = 0.4)).also { it.fadeTo(0.9f, 0.02f) }
        }.start()
    }

    /* ================= Sürüş ================= */
    private fun loading() {
        val c = page("Sesler hazırlanıyor")
        val pb = ProgressBar(this); pb.isIndeterminate = true; pb.indeterminateTintList = ColorStateList.valueOf(cAmber)
        c.addView(pb, lp(top = 24))
        c.addView(para("${currentVehicle().name}, ${currentEnv().name}. Birkaç saniye sürebilir."))
    }

    private fun neededKeys(v: VehicleDef, e: EnvDef): List<String> {
        val k = ArrayList<String>()
        when {
            v.synth -> if (bank.isCustom("ev_loop")) k.add("ev_loop")
            v.customPrefix != null -> listOf("loop", "idle", "off", "high", "start").map { "${v.customPrefix}_$it" }.filter { bank.isCustom(it) }.forEach { k.add(it) }
            else -> listOf("idle", "low_on", "low_off", "med_on", "med_off", "high_on", "high_off", "start").forEach { k.add("${v.set}_$it") }
        }
        k.add(e.bed); e.cabin.forEach { k.add(it.key) }; e.emitters.forEach { k.add(it.key) }; e.oneshots.forEach { k.add(it.key) }
        if (e.siren) k.add("em_siren")
        k.addAll(listOf("horn", "screech")); k.addAll(Slots.trafficKeys)
        return k.distinct()
    }

    private fun startDrive() {
        val v = currentVehicle(); val e = currentEnv()
        go(Scr.LOADING)
        say("Sesler hazırlanıyor")
        Thread {
            val keys = neededKeys(v, e)
            bank.keepOnly(keys)
            keys.forEach { bank.get(it) }
            ui.post {
                val g = Game(mix, bank, this, prefs.game, v, e)
                game = g; paused = false
                g.start()
                buildDrive(g)
                replaceTop(Scr.DRIVE)
                startLoop()
                say("${v.name}, ${e.name}. Motor çalışıyor. Fren sol kenarda, gaz sağ kenarda. Duraklatmak için geri tuşu.")
            }
        }.start()
    }

    private fun startLoop() {
        loopRun = true
        loopThread = Thread({
            var last = System.nanoTime()
            while (loopRun) {
                val now = System.nanoTime()
                val dt = ((now - last) / 1e9).coerceAtMost(0.05); last = now
                val g = game
                if (g != null && !paused) try { g.update(dt) } catch (e: Throwable) {}
                try { Thread.sleep(8) } catch (e: InterruptedException) { break }
            }
        }, "bosyol-game")
        loopThread!!.start()
    }

    private fun endDrive() {
        loopRun = false; loopThread?.join(300); loopThread = null
        game?.stop(); game = null
        roadView?.game = null
        driveView = null; pauseView = null; roadView = null
        ui.removeCallbacks(hudTick)
        menuVolumes()
    }

    private fun showDrive() {
        val dv = driveView ?: return
        (dv.parent as? ViewGroup)?.removeView(dv)
        root.removeAllViews(); root.addView(dv, FrameLayout.LayoutParams(-1, -1))
        if (paused) showPause() else gasPedal?.let { ui.postDelayed({ it.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null) }, 300) }
        ui.removeCallbacks(hudTick); ui.post(hudTick)
    }

    private fun buildDrive(g: Game) {
        val fl = FrameLayout(this)
        val rv = RoadView(this); rv.game = g; roadView = rv
        fl.addView(rv, FrameLayout.LayoutParams(-1, -1))
        val row = LinearLayout(this); row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(dp(10f), dp(10f), dp(10f), dp(10f))
        val brake = pedal("Fren", false); brakePedal = brake
        val gas = pedal("Gaz", true); gasPedal = gas
        val center = LinearLayout(this); center.orientation = LinearLayout.VERTICAL
        center.setPadding(dp(10f), 0, dp(10f), 0)
        val top = LinearLayout(this); top.orientation = LinearLayout.HORIZONTAL
        val h = TextView(this); h.setTextColor(cInk); h.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f); h.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        h.background = bgShape(0xD9161B21.toInt(), cLine); h.setPadding(dp(14f), dp(8f), dp(14f), dp(8f))
        h.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO; hud = h
        top.addView(h, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(smallBtn("Duraklat") { pauseDrive() }, LinearLayout.LayoutParams(-2, -2).also { it.leftMargin = dp(8f) })
        center.addView(top)
        center.addView(View(this), LinearLayout.LayoutParams(-1, 0, 1f))
        val r1 = LinearLayout(this); val r2 = LinearLayout(this)
        val ab = smallBtn("Otomatik sürüş: kapalı") { g.setAuto(!g.auto); syncDrive() }; autoBtn = ab
        r1.addView(ab, LinearLayout.LayoutParams(0, -2, 1.3f))
        r1.addView(smallBtn("Hedef hızı azalt") { g.changeCruise(-10) }, LinearLayout.LayoutParams(0, -2, 1f).also { it.leftMargin = dp(8f) })
        r1.addView(smallBtn("Hedef hızı artır") { g.changeCruise(10) }, LinearLayout.LayoutParams(0, -2, 1f).also { it.leftMargin = dp(8f) })
        r2.addView(smallBtn("Hız ve vites") { say(g.statusText()) }, LinearLayout.LayoutParams(0, -2, 1.3f))
        r2.addView(hornBtn(g), LinearLayout.LayoutParams(0, -2, 1f).also { it.leftMargin = dp(8f) })
        val wb = smallBtn("Camları kapat") { g.setWindows(!g.windows, false); syncDrive() }; winBtn = wb
        r2.addView(wb, LinearLayout.LayoutParams(0, -2, 1f).also { it.leftMargin = dp(8f) })
        center.addView(r1); center.addView(r2, LinearLayout.LayoutParams(-1, -2).also { it.topMargin = dp(8f) })
        row.addView(brake, LinearLayout.LayoutParams(0, -1, 1f))
        row.addView(center, LinearLayout.LayoutParams(0, -1, 2.7f))
        row.addView(gas, LinearLayout.LayoutParams(0, -1, 1f))
        fl.addView(row, FrameLayout.LayoutParams(-1, -1))
        driveControls = row
        driveView = fl
        syncDrive()
    }

    private fun smallBtn(text: String, onClick: () -> Unit): Button {
        val b = btn(text, onClick = onClick)
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f); b.minHeight = dp(52f); b.gravity = Gravity.CENTER
        b.background = bgShape(0xD91E252D.toInt(), cLine); b.setPadding(dp(8f), dp(6f), dp(8f), dp(6f))
        return b
    }

    private fun hornBtn(g: Game): Button {
        val b = smallBtn("Korna") { g.hornOn(); ui.postDelayed({ g.hornOff() }, 450) }
        b.setOnTouchListener { v, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { v.isPressed = true; g.hornOn() }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { v.isPressed = false; ui.postDelayed({ g.hornOff() }, 120) }
            }
            true
        }
        return b
    }

    /** Pedal: dokunmayla basılı tutulur; ekran okuyucunun tıklaması ayara göre sabitler ya da kısa basar. */
    private fun pedal(label: String, gas: Boolean): TextView {
        val t = object : TextView(this) {
            override fun performClick(): Boolean { super.performClick(); return true }
        }
        t.text = label; t.gravity = Gravity.CENTER; t.setTextColor(cInk)
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f); t.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        t.isClickable = true; t.isFocusable = true
        t.contentDescription = label
        var downAt = 0L; var wasLatched = false
        t.setOnTouchListener { _, e ->
            val g = game ?: return@setOnTouchListener true
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downAt = System.currentTimeMillis(); wasLatched = if (gas) g.gasLatch else g.brakeLatch
                    g.pedalTouched(gas); if (gas) g.gasHold = true else g.brakeHold = true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (gas) g.gasHold = false else g.brakeHold = false
                    val quick = System.currentTimeMillis() - downAt < 320
                    if (prefs.game.latch && quick && e.actionMasked == MotionEvent.ACTION_UP) g.setLatch(gas, !wasLatched)
                    else if (gas) g.gasLatch = false else g.brakeLatch = false
                }
            }
            syncPedals(); true
        }
        t.setOnClickListener {
            val g = game ?: return@setOnClickListener
            if (prefs.game.latch) g.setLatch(gas, !(if (gas) g.gasLatch else g.brakeLatch)) else g.pulse(gas)
            syncPedals()
        }
        return t
    }

    private fun syncPedals() {
        val g = game ?: return
        listOf(brakePedal to (g.brakeOn), gasPedal to g.gasOn).forEachIndexed { i, (p, on) ->
            p ?: return@forEachIndexed
            val col = if (i == 0) cBrake else cAmber
            p.background = bgShape(if (on) (col and 0x00FFFFFF) or 0x55000000 else 0xB31B2128.toInt(), if (on) col else cLine, 22f)
            if (Build.VERSION.SDK_INT >= 30) p.stateDescription = if (on) "basılı" else "bırakılmış"
        }
    }

    private fun syncDrive() {
        val g = game ?: return
        autoBtn?.text = if (g.auto) "Otomatik sürüş: açık, hedef ${g.cruise}" else "Otomatik sürüş: kapalı"
        winBtn?.text = if (g.windows) "Camları kapat" else "Camları aç"
        syncPedals()
    }

    private val hudTick = object : Runnable {
        override fun run() {
            val g = game ?: return
            val kmh = (g.v * 3.6).roundToInt()
            val gear = if (g.vehicle.synth) "D" else "${g.gear + 1}. vites"
            val rpm = if (g.vehicle.synth) "" else "  ·  ${(g.rpm / 50).roundToInt() * 50} devir"
            hud?.text = "$kmh km/sa  ·  $gear$rpm\n${if (g.auto) "Otomatik, hedef ${g.cruise}" else "Elle sürüş"}  ·  ${g.env.name}"
            syncDrive()
            ui.postDelayed(this, 150)
        }
    }

    private fun pauseDrive() {
        val g = game ?: return
        paused = true
        g.gasHold = false; g.brakeHold = false; g.keyGas = false; g.keyBrake = false; g.hornOff()
        mix.master = 0f
        if (stack.last() == Scr.DRIVE) showPause()
    }

    private fun showPause() {
        val dv = driveView ?: return
        pauseView?.let { dv.removeView(it) }
        driveControls?.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        val g = game ?: return
        val sv = ScrollView(this); sv.setBackgroundColor(0xE6101418.toInt()); sv.isFillViewport = true
        val outer = LinearLayout(this); outer.gravity = Gravity.CENTER_HORIZONTAL
        val c = LinearLayout(this); c.orientation = LinearLayout.VERTICAL; c.setPadding(dp(24f), dp(18f), dp(24f), dp(24f))
        outer.addView(c, LinearLayout.LayoutParams(minOf(resources.displayMetrics.widthPixels, dp(640f)), -2))
        sv.addView(outer)
        val h = heading("Duraklatıldı", 28f); h.tag = "ptitle"; c.addView(h)
        c.addView(btn("Sürüşe devam et", primary = true) { resumeDrive() }, lp(top = 12))
        c.addView(sw("Trafik: yanından geçen araçlar", prefs.game.traffic) { prefs.game.traffic = it; prefs.saveGame() }, lp(top = 6))
        c.addView(sw("Pedala tek dokunuş pedalı sabitlesin", prefs.game.latch) { prefs.game.latch = it; prefs.saveGame() }, lp(top = 4))
        c.addView(sw("Ses tuşlarıyla sür", prefs.volKeys) { prefs.volKeys = it }, lp(top = 4))
        c.addView(btn("Tüm ayarlar") { go(Scr.SETTINGS) }, lp())
        c.addView(btn("Ana menüye dön") { endDrive(); stack.clear(); go(Scr.MENU) }, lp())
        dv.addView(sv, FrameLayout.LayoutParams(-1, -1))
        pauseView = sv
        ui.postDelayed({ h.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null) }, 250)
        say("Duraklatıldı. ${g.statusText()}")
    }

    private fun resumeDrive() {
        val g = game ?: return
        pauseView?.let { driveView?.removeView(it) }; pauseView = null
        driveControls?.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
        paused = false
        g.applyVolumes()
        say("Devam")
        gasPedal?.let { ui.postDelayed({ it.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null) }, 250) }
    }

    /* ================= Donanım tuşları ================= */
    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        val g = game
        if (g != null && !paused && stack.lastOrNull() == Scr.DRIVE) {
            val down = e.action == KeyEvent.ACTION_DOWN
            val first = down && e.repeatCount == 0
            when (e.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> if (prefs.volKeys) { if (first) g.pedalTouched(true); if (first || !down) g.keyGas = down; syncPedals(); return true }
                KeyEvent.KEYCODE_VOLUME_DOWN -> if (prefs.volKeys) { if (first) g.pedalTouched(false); if (first || !down) g.keyBrake = down; syncPedals(); return true }
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> { if (first) g.pedalTouched(true); if (first || !down) g.keyGas = down; syncPedals(); return true }
                KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> { if (first) g.pedalTouched(false); if (first || !down) g.keyBrake = down; syncPedals(); return true }
                KeyEvent.KEYCODE_H -> { if (first) g.hornOn() else if (!down) g.hornOff(); return true }
                KeyEvent.KEYCODE_O -> { if (first) { g.setAuto(!g.auto); syncDrive() }; return true }
                KeyEvent.KEYCODE_T -> { if (first) { prefs.game.traffic = !prefs.game.traffic; prefs.saveGame(); say(if (prefs.game.traffic) "Trafik açık" else "Trafik kapalı") }; return true }
                KeyEvent.KEYCODE_SPACE -> { if (first) say(g.statusText()); return true }
            }
        }
        return super.dispatchKeyEvent(e)
    }
}
