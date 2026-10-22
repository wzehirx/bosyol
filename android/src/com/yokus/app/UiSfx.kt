package com.yokus.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import com.yokus.app.core.BUS_ENGINE
import com.yokus.app.core.BUS_FX
import com.yokus.app.core.BUS_WORLD
import com.yokus.app.core.Bank
import com.yokus.app.core.Biquad
import com.yokus.app.core.EndReason
import com.yokus.app.core.GameEvents
import com.yokus.app.core.MapDef
import com.yokus.app.core.Mixer
import com.yokus.app.core.OscVoice
import com.yokus.app.core.Pcm
import com.yokus.app.core.SampleVoice
import com.yokus.app.core.VehicleDef
import com.yokus.app.core.Voice
import com.yokus.app.core.seamless
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow

/**
 * Menü ve oyun hissi: düğme sesleri, titreşim, menü zemini, araç/harita önizlemesi, geri sayım, sayım sesi.
 * Dosya yoksa ilgili ses sessiz kalır, hiçbir zaman hata vermez. "Menü sesleri" kapalıysa ses çalmaz;
 * titreşim ayrı bir ayarla ("Titreşim") yönetilir.
 */
class UiSfx(private val ctx: Context, private val mix: Mixer, private val bank: Bank, private val store: Store) {
    private val main = Handler(Looper.getMainLooper())
    private val rnd = java.util.Random()
    private val worker = Executors.newSingleThreadExecutor { r -> Thread(r, "yokus-ui").also { it.isDaemon = true } }
    @Volatile private var closed = false

    /** Duyuru için (Hazır). */
    var speak: ((String) -> Unit)? = null

    private val vib: Vibrator? = try {
        @Suppress("DEPRECATION") ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    } catch (e: Throwable) { null }
    private val canVibrate = try { vib?.hasVibrator() == true } catch (e: Throwable) { false }

    /** Testler için sayaçlar. */
    @Volatile var clicks = 0; private set
    @Volatile var ticks = 0; private set
    @Volatile var beeps = 0; private set
    @Volatile var previews = 0; private set
    @Volatile var tallies = 0; private set
    @Volatile var buzzes = 0; private set

    private val sounds get() = store.menuSounds
    private fun now() = SystemClock.uptimeMillis()

    private fun pcm(key: String): Pcm? = try { bank.get(key) } catch (e: Throwable) { null }

    /** Çözme işi ses dışı iş parçacığında yapılır (ekran okuyucu odağı takılmasın); sonuç ana iş parçacığına döner. */
    private fun async(keys: List<String>, then: (List<Pcm?>) -> Unit) {
        if (closed) return
        try {
            worker.execute {
                val l = keys.map { pcm(it) }
                main.post { if (!closed) try { then(l) } catch (e: Throwable) { } }
            }
        } catch (e: Throwable) { }
    }

    private fun later(ms: Long, r: () -> Unit) { main.postDelayed({ if (!closed) r() }, ms) }

    /** Sık kullanılan kısa sesleri önceden çözer. */
    fun warm() = async(listOf("ui_click", "coin_s", "buy_hcr", "buy", "coin_hcr", "nope", "flip", "e0_start")) { }

    private fun shot(p: Pcm, gain: Float, rate: Double = 1.0, maxSec: Double = 2.0, bus: Int = BUS_FX): SampleVoice {
        val v = SampleVoice(p, bus, false, durSec = min(p.duration, maxSec), fadeOutSec = min(0.05, maxSec / 3))
        v.rate = rate; v.gain = gain
        return mix.add(v)
    }

    private fun jitter(amount: Double) = (rnd.nextDouble() * 2 - 1) * amount

    /* ================= Titreşim ================= */
    @Volatile private var lastBuzz = -1000L

    fun buzz(ms: Long, amp: Int) {
        if (!store.haptics) return
        buzzes++; lastBuzz = now()
        if (!canVibrate) return
        try { vib?.vibrate(VibrationEffect.createOneShot(ms, amp.coerceIn(1, 255))) } catch (e: Throwable) { }
    }

    fun buzzTwice(ms: Long, gap: Long, amp: Int) {
        if (!store.haptics) return
        buzzes++; lastBuzz = now()
        if (!canVibrate) return
        try {
            val a = amp.coerceIn(1, 255)
            vib?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, ms, gap, ms), intArrayOf(0, a, 0, a), -1))
        } catch (e: Throwable) { }
    }

    /** Altın: kısa darbe; son 80 ms içinde başka titreşim olduysa atlanır. */
    fun coinBuzz() { if (now() - lastBuzz >= 80) buzz(14, 110) }
    fun landBuzz(impact: Double) {
        if (impact <= 3.0) return
        buzz((12 + impact * 4).toLong().coerceAtMost(70), (50 + impact * 20).toInt())
    }
    fun fuelLowBuzz() = buzzTwice(60, 90, 200)
    fun crashBuzz() = buzz(450, 255)

    /* ================= Düğmeler ================= */
    fun click() {
        if (sounds) {
            val p = pcm("ui_click") ?: pcm("coin_s")
            if (p != null) { clicks++; shot(p, (0.5 + jitter(0.06)).toFloat(), 1.0 + jitter(0.03), 0.35) }
        }
        buzz(12, 90)
    }

    private var lastTick = -1000L
    /** Erişilebilirlik odağı yeni düğmeye geçince: daha kısık ve kısa tık, en çok 90 ms'de bir. */
    fun focusTick() {
        val t = now()
        if (t - lastTick < 90) return
        lastTick = t
        if (!sounds) return
        val p = pcm("ui_click") ?: pcm("coin_s") ?: return
        ticks++
        shot(p, (0.2 + jitter(0.02)).toFloat(), 1.35 + jitter(0.04), 0.014)
    }

    fun back() {
        if (sounds) { val p = pcm("ui_click") ?: pcm("coin_s"); if (p != null) { clicks++; shot(p, 0.5f, 0.78 + jitter(0.02), 0.45) } }
        buzz(10, 70)
    }

    fun buy() {
        if (sounds) {
            val p = pcm("buy_hcr") ?: pcm("buy")
            if (p != null) shot(p, 0.8f, 1.0, 1.5)
            val c = pcm("coin_hcr")
            if (c != null) for (i in 0..2) later(120L + 90L * i) { shot(c, 0.4f, 1.0 + 0.1 * i, 0.3) }
        }
        buzzTwice(30, 50, 160)
    }

    fun nope() {
        if (sounds) pcm("nope")?.let { shot(it, 0.8f, 1.0, 1.0) }
        buzzTwice(30, 40, 120)
    }

    /** Oyuna girerken alçak bir uğultu. */
    fun whoosh() { if (sounds) pcm("flip")?.let { shot(it, 0.35f, 1.0, 0.9) } }

    /* ================= Menü zemini ================= */
    private var bedKey: String? = null
    private var bedGen = 0
    private val bedVoices = ArrayList<Voice>()
    val bedActive: Boolean get() = bedKey != null

    fun startMenuBed(v: VehicleDef, m: MapDef) {
        if (!sounds) { stopMenuBed(); return }
        val key = v.id + "/" + m.id
        if (key == bedKey) return
        stopMenuBed()
        bedKey = key
        val gen = bedGen
        async(listOf(m.ambKey, v.engineKey, "e0_med_on")) { l ->
            if (gen != bedGen) return@async
            l[0]?.let { p -> bedVoices.add(mix.add(SampleVoice(p, BUS_WORLD, true).also { it.gain = 0f; it.fadeTo(0.12f, 1.5f) })) }
            if (v.electric) {
                val o = OscVoice(BUS_ENGINE, OscVoice.SINE); o.freq = (120.0 + v.idleRpm * 0.12).toFloat(); o.gain = 0f; o.fadeTo(0.02f, 1.5f)
                bedVoices.add(mix.add(o))
            } else {
                val p = l[1] ?: l[2]
                if (p != null) bedVoices.add(mix.add(engineVoice(v, seamless(p, 0.08), idleRate(v), 0.07f, 1.5f)))
            }
        }
    }

    fun stopMenuBed() {
        bedGen++; bedKey = null
        for (v in bedVoices) v.stop(0.4f)
        bedVoices.clear()
    }

    private fun rateOf(v: VehicleDef, rpm: Double) = (rpm / v.engineRefRpm * v.enginePitch).coerceIn(0.35, 2.4)
    private fun idleRate(v: VehicleDef) = rateOf(v, v.idleRpm)

    /** Sonify ile aynı motor eşlemesi: perde, alçak/yüksek geçiren süzgeç. */
    private fun engineVoice(v: VehicleDef, p: Pcm, rate: Double, gain: Float, fade: Float): SampleVoice {
        val sv = SampleVoice(p, BUS_ENGINE, true)
        sv.rate = rate; sv.gain = 0f
        if (v.engineHp > 0) sv.filter = Biquad(Biquad.HIGHPASS, v.engineHp.toFloat(), 0.7f)
        else if (v.engineLp < 19000) sv.filter = Biquad(Biquad.LOWPASS, v.engineLp.toFloat(), 0.7f)
        sv.fadeTo(gain, fade)
        return sv
    }

    /* ================= Önizleme ================= */
    private var pvGen = 0
    private var pvLast = -1000L
    private var pvTask: Runnable? = null
    private var pvSweep: Runnable? = null
    private val pvVoices = ArrayList<Voice>()

    fun stopPreview() {
        pvGen++
        pvTask?.let { main.removeCallbacks(it) }; pvTask = null
        pvSweep?.let { main.removeCallbacks(it) }; pvSweep = null
        for (v in pvVoices) v.stop(0.12f)
        pvVoices.clear()
    }

    /** Hızlı kaydırmada (400 ms içinde art arda odak) ses beklenir ve yalnızca son öğe çalınır; öncekiler iptal edilir. */
    private fun schedule(immediate: Boolean, start: () -> Unit) {
        if (!sounds) return
        val t = now()
        val burst = t - pvLast < 400
        pvLast = t
        stopPreview()
        if (immediate || !burst) start()
        else {
            val r = Runnable { pvTask = null; start() }
            pvTask = r; main.postDelayed(r, 400)
        }
    }

    /** Araç düğmesine odak gelince 1.2 sn motor: rölanti, azami devrin yaklaşık %60'ı, rölanti. */
    fun previewVehicle(v: VehicleDef, immediate: Boolean = false, seconds: Double = 1.2) = schedule(immediate) { startEngine(v, seconds) }

    /** Garajda geliştirme sonrası kısa motor gazı. */
    fun rev(v: VehicleDef) = previewVehicle(v, immediate = true, seconds = 0.9)

    private fun startEngine(v: VehicleDef, seconds: Double) {
        stopPreview()
        val gen = pvGen
        previews++
        val peak = v.idleRpm + (v.maxRpm * 0.6 - v.idleRpm).coerceAtLeast(0.0)
        fun rpmAt(x: Double) = v.idleRpm + (peak - v.idleRpm) * (0.5 - 0.5 * cos(2 * PI * x))
        val update: (Double) -> Unit
        if (v.electric) {
            val a = OscVoice(BUS_ENGINE, OscVoice.SINE); a.gain = 0f; a.freq = 120f; a.fadeTo(0.12f, 0.08f)
            val b = OscVoice(BUS_ENGINE, OscVoice.SAW); b.gain = 0f; b.freq = 120.6f; b.filter = Biquad(Biquad.LOWPASS, 1800f, 0.7f); b.fadeTo(0.035f, 0.08f)
            pvVoices.add(mix.add(a)); pvVoices.add(mix.add(b))
            update = { rpm -> val f = (120.0 + rpm * 0.12).toFloat(); a.freq = f; b.freq = f * 1.005f }
            sweep(gen, seconds, ::rpmAt, update)
        } else {
            async(listOf(v.engineKey, "e0_med_on")) { l ->
                if (gen != pvGen) return@async
                val p = l[0] ?: l[1] ?: return@async
                val sv = mix.add(engineVoice(v, seamless(p, 0.08), idleRate(v), 0.32f, 0.08f))
                pvVoices.add(sv)
                sweep(gen, seconds, ::rpmAt) { rpm -> sv.rate = rateOf(v, rpm) }
            }
        }
    }

    private fun sweep(gen: Int, seconds: Double, rpmAt: (Double) -> Double, update: (Double) -> Unit) {
        val steps = (seconds * 1000 / 30).toInt().coerceAtLeast(1)
        var i = 0
        val r = object : Runnable {
            override fun run() {
                if (gen != pvGen || closed) return
                if (i >= steps) { stopPreview(); return }
                update(rpmAt(i.toDouble() / steps)); i++
                main.postDelayed(this, 30)
            }
        }
        pvSweep = r
        r.run()
    }

    /** Harita düğmesine odak gelince yaklaşık 2 sn ortam sesi, girişte ve çıkışta yumuşak. */
    fun previewMap(m: MapDef) = schedule(false) {
        stopPreview()
        val gen = pvGen
        previews++
        async(listOf(m.ambKey)) { l ->
            if (gen != pvGen) return@async
            val p = l[0] ?: return@async
            val v = SampleVoice(p, BUS_WORLD, false, durSec = min(p.duration, 2.0), fadeOutSec = 0.6)
            v.gain = 0f; v.fadeTo(0.45f, 0.5f)
            pvVoices.add(mix.add(v))
        }
    }

    /* ================= Oyun sonu sayımı ================= */
    private var tallyGen = 0

    /** Altın sayma: her altına bir vuruş (40'a dek), fazlası onda biri; en çok 1.6 sn, hızlanan tempo; rekorda satın alma sesi. */
    fun tally(coins: Int, record: Boolean) {
        tallyGen++
        if (!sounds) return
        val gen = tallyGen
        val n = when { coins <= 0 -> 0; coins <= 40 -> coins; else -> (40 + (coins - 40) / 10).coerceAtMost(60) }
        val total = if (n == 0) 0.0 else min(1.6, 0.3 + 0.04 * n)
        tallies++
        val c = pcm("coin_hcr") ?: pcm("coin_s")
        for (i in 0 until n) {
            val ms = (total * 1000 * (1.0 - (1.0 - i.toDouble() / n).pow(1.7))).toLong()
            later(ms) {
                if (gen == tallyGen) {
                    if (c != null) shot(c, (0.32 + jitter(0.03)).toFloat(), 1.0, 0.25)
                    if (i % 3 == 0) buzz(8, 70)
                }
            }
        }
        if (record) later((total * 1000).toLong() + 180) {
            if (gen == tallyGen) { (pcm("buy_hcr") ?: pcm("buy"))?.let { shot(it, 0.85f, 1.0, 1.5) }; buzzTwice(40, 60, 200) }
        }
    }

    fun stopTally() { tallyGen++ }

    /* ================= Geri sayım ================= */
    private var cdActive = false
    private var cdFired = 0
    private var cdGen = 0
    private val cdVoices = ArrayList<Voice>()
    private var cdElec: OscVoice? = null
    private var cdElecB: OscVoice? = null
    private var cdDef: VehicleDef? = null

    /**
     * "Hazır, başla": alçak üç bip 0.45 sn arayla, yüksek bir bip; motor çalışır.
     * Zamanlama [countdownTick] ile sürülür (oyun adımıyla), toplam [COUNT_END] sn. Menü sesleri kapalıysa false döner.
     */
    fun countdown(v: VehicleDef): Boolean {
        cancelCountdown()
        if (!sounds) return false
        cdActive = true; cdFired = 0; cdDef = v
        val gen = ++cdGen
        if (v.electric) {
            val a = OscVoice(BUS_ENGINE, OscVoice.SINE); a.gain = 0f; a.freq = 60f; a.fadeTo(0.16f, 0.3f)
            val b = OscVoice(BUS_ENGINE, OscVoice.SAW); b.gain = 0f; b.freq = 60f; b.filter = Biquad(Biquad.LOWPASS, 1800f, 0.7f); b.fadeTo(0.04f, 0.3f)
            cdVoices.add(mix.add(a)); cdVoices.add(mix.add(b)); cdElec = a; cdElecB = b
        } else {
            async(listOf("e0_start", v.engineKey, "e0_med_on")) { l ->
                if (gen != cdGen || !cdActive) return@async
                val st = l[0]
                if (st != null) {
                    val sv = SampleVoice(st, BUS_ENGINE, false, durSec = min(st.duration, 1.9), fadeOutSec = 0.35)
                    sv.rate = v.enginePitch.coerceIn(0.7, 1.3); sv.gain = 0f; sv.fadeTo(0.5f, 0.05f)
                    cdVoices.add(mix.add(sv))
                } else {
                    val p = l[1] ?: l[2]
                    if (p != null) cdVoices.add(mix.add(engineVoice(v, seamless(p, 0.08), idleRate(v), 0.4f, 0.6f)))
                }
            }
        }
        return true
    }

    /** Geri sayımın başlangıçtan geçen süresi (sn); biter bitmez true. */
    fun countdownTick(t: Double): Boolean {
        if (!cdActive) return true
        while (cdFired < 4 && t >= BEEP_AT[cdFired]) { beep(cdFired); cdFired++ }
        val x = (t / COUNT_END).coerceIn(0.0, 1.0)
        val d = cdDef
        if (d != null && cdElec != null) {
            val f = (60.0 + ((120.0 + d.idleRpm * 0.12) - 60.0) * x.pow(0.7)).toFloat()
            cdElec?.freq = f; cdElecB?.freq = f * 1.005f
        }
        if (t >= COUNT_END) { finishCountdown(); return true }
        return false
    }

    private fun beep(i: Int) {
        val high = i == 3
        beeps++
        val o = OscVoice(BUS_FX, OscVoice.SINE)
        o.freq = if (high) 880f else 440f
        o.gain = 0f; o.fadeTo(0.3f, 0.008f)
        o.stopAfter(if (high) 0.32f else 0.14f, 0.05f)
        mix.add(o)
        if (i == 0) try { speak?.invoke("Hazır") } catch (e: Throwable) { }
        buzz(if (high) 60 else 20, if (high) 220 else 120)
    }

    private fun finishCountdown() {
        cdActive = false; cdElec = null; cdElecB = null
        for (v in cdVoices) v.stop(0.3f)   // örnekli çalıştırma sesi kendi sönüşüyle biter; Sonify motoru devralır
        cdVoices.clear()
    }

    fun cancelCountdown() {
        cdGen++
        cdActive = false; cdElec = null; cdElecB = null
        for (v in cdVoices) v.stop(0.2f)
        cdVoices.clear()
    }

    /** Duraklama ya da arka plan: bekleyen geçici sesleri keser. */
    fun cancelTransient() { stopPreview(); stopTally() }

    fun release() {
        closed = true
        main.removeCallbacksAndMessages(null)
        stopPreview(); stopMenuBed(); cancelCountdown()
        worker.shutdownNow()
    }

    companion object {
        val BEEP_AT = doubleArrayOf(0.0, 0.45, 0.9, 1.35)
        /** Yüksek bip bittikten sonra oyun saati başlar. */
        const val COUNT_END = 1.6
    }
}

/** Oturum olaylarını Sonify'a iletir, üstüne titreşim ekler. Fizik ve zamanlamaya dokunmaz. */
class HapticEvents(private val inner: GameEvents, private val fx: UiSfx) : GameEvents by inner {
    override fun onCoin(value: Int, streak: Int) { inner.onCoin(value, streak); fx.coinBuzz() }
    override fun onLand(impact: Double) { inner.onLand(impact); fx.landBuzz(impact) }
    override fun onFuelLow(pct: Int) { inner.onFuelLow(pct); fx.fuelLowBuzz() }
    override fun onEnd(reason: EndReason) { inner.onEnd(reason); if (reason == EndReason.CRASH) fx.crashBuzz() }
}
