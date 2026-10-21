package com.yokus.app.core

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Oyunun ses tasarımı: GameSession durumunu ve olaylarını sese çevirir.
 * Yön kuralı: ilerisi sağ kulak (pan = dx/25), uzaklık sesi kısar, yukarısı ince perde.
 * Eksik ses dosyası hiçbir zaman hata vermez; o katman sessiz kalır.
 */
class Sonify(val mix: Mixer, val bank: Bank, val set: AudioSettings, val speak: (String) -> Unit) : GameEvents {
    private var s: GameSession? = null
    private var clock = 0.0
    private var ended = false

    // --- sürekli katmanlar ---
    private var engine: SampleVoice? = null
    private var synthA: OscVoice? = null      // yedek / elektrikli: testere (ya da elektrikli sinüs)
    private var synthB: OscVoice? = null
    private var roll: SampleVoice? = null
    private var wind: SampleVoice? = null
    private var amb: SampleVoice? = null
    private var tilt: OscVoice? = null
    private val loops = ArrayList<Voice>()

    /** Testler için: motor perdesi (örnek hızı ya da sentez için frekans/100) ve bip sayacı. */
    @Volatile var engineRate = 0.0
        private set
    @Volatile var beepPulses = 0
        private set
    @Volatile var radarPulses = 0
        private set
    @Volatile var engineKind = "none"
        private set
    /** Testler için: son altın sesinin örnek hızı ve 500'lük çift ışıltı sayacı. */
    @Volatile var lastCoinRate = 0.0
        private set
    @Volatile var doubleGlints = 0
        private set

    // --- zamanlayıcılar ---
    private var nextBeep = 0.0
    private var nextRadar = 0.0
    private var nextCoinGlint = 0.0
    private var nextFuelBeacon = 0.0
    private var lastThud = -1e9
    private var lastCreak = -1e9
    private class Timed(val at: Double, val run: () -> Unit)
    private val timed = ArrayList<Timed>()

    // --- duyuru kısıtlayıcı ---
    private var pending: String? = null
    private var lastSpeakAt = -1e9
    private val lastSaid = HashMap<String, Double>()

    private companion object { const val MASTER_TRIM = 1.6f }

    private fun vg(v: Int): Float = (v.coerceIn(0, 100) / 100.0).pow(1.5).toFloat()

    fun applyVolumes() {
        mix.master = vg(set.volMaster) * MASTER_TRIM
        mix.busGain[BUS_ENGINE] = vg(set.volEngine)
        mix.busGain[BUS_WORLD] = vg(set.volWorld)
        mix.busGain[BUS_CABIN] = vg(set.volCues)
        mix.busGain[BUS_FX] = vg(set.volCues)
    }

    private fun pcm(key: String): Pcm? = try { bank.get(key) } catch (e: Throwable) { null }

    private fun loopVoice(key: String, bus: Int, seam: Double = 0.0): SampleVoice? {
        var p = pcm(key) ?: return null
        if (seam > 0) p = seamless(p, seam)
        val v = SampleVoice(p, bus, true)
        v.gain = 0f
        mix.add(v); loops.add(v)
        return v
    }

    fun start(s: GameSession) {
        this.s = s
        s.events = this
        s.balanceAssist = set.balanceAssist
        ended = false
        applyVolumes()
        val def = s.vehicle
        // motor
        if (def.electric) {
            engineKind = "electric"
            synthA = OscVoice(BUS_ENGINE, OscVoice.SINE).also { it.gain = 0f; mix.add(it); loops.add(it) }
            synthB = OscVoice(BUS_ENGINE, OscVoice.SAW).also {
                it.gain = 0f; it.filter = Biquad(Biquad.LOWPASS, 1800f, 0.7f); mix.add(it); loops.add(it)
            }
        } else {
            val v = loopVoice(def.engineKey, BUS_ENGINE, 0.08)?.also { engineKind = def.engineKey }
                ?: loopVoice("e0_med_on", BUS_ENGINE, 0.08)?.also { engineKind = "e0_med_on" }
            if (v != null) {
                engine = v
                if (def.engineHp > 0) v.filter = Biquad(Biquad.HIGHPASS, def.engineHp.toFloat(), 0.7f)
                else if (def.engineLp < 19000) v.filter = Biquad(Biquad.LOWPASS, def.engineLp.toFloat(), 0.7f)
            } else {
                engineKind = "saw"
                synthA = OscVoice(BUS_ENGINE, OscVoice.SAW).also {
                    it.gain = 0f
                    it.filter = Biquad(Biquad.LOWPASS, (if (def.engineLp < 19000) def.engineLp else 1200.0).toFloat(), 0.7f)
                    mix.add(it); loops.add(it)
                }
            }
        }
        roll = loopVoice(s.map.surface.rollKey, BUS_WORLD)
        wind = loopVoice("wind", BUS_WORLD)
        amb = loopVoice(s.map.ambKey, BUS_WORLD)
        amb?.fadeTo(0.35f, 1.0f)
        tilt = OscVoice(BUS_CABIN, OscVoice.SINE).also { it.gain = 0f; mix.add(it); loops.add(it) }
        nextBeep = clock; nextRadar = clock; nextCoinGlint = clock + 0.3; nextFuelBeacon = clock + 0.6
    }

    fun stop() {
        mix.stopAll(0.4f)
        loops.clear(); timed.clear()
        engine = null; synthA = null; synthB = null; roll = null; wind = null; amb = null; tilt = null
    }

    fun ui(key: String) {
        val p = pcm(key) ?: return
        shot(p, BUS_FX, 0.8f, 1.0, 0f)
    }

    // ---------------------------------------------------------------- duyuru

    /** Aynı metin 3 sn içinde tekrarlanmaz, en çok 0.8 sn'de bir duyuru; beklerken yalnızca en yenisi kalır. */
    fun say(text: String) {
        val last = lastSaid[text]
        if (last != null && clock - last < 3.0) return
        if (pending == text) return
        pending = text
        flushSpeech()
    }

    private fun flushSpeech() {
        val t = pending ?: return
        if (clock - lastSpeakAt < 0.8) return
        val last = lastSaid[t]
        pending = null
        if (last != null && clock - last < 3.0) return
        lastSpeakAt = clock
        lastSaid[t] = clock
        try { speak(t) } catch (e: Throwable) { }
    }

    // ---------------------------------------------------------------- yardımcılar

    private fun shot(p: Pcm, bus: Int, gain: Float, rate: Double, pan: Float): SampleVoice {
        val v = SampleVoice(p, bus, false, fadeOutSec = 0.05)
        v.rate = rate; v.pan = pan; v.gain = gain
        mix.add(v)
        return v
    }

    private fun shot(key: String, bus: Int, gain: Float, rate: Double = 1.0, pan: Float = 0f) {
        val p = pcm(key) ?: return
        shot(p, bus, gain, rate, pan)
    }

    private fun pulse(wave: Int, freq: Double, gain: Float, pan: Float, dur: Double, bus: Int = BUS_CABIN) {
        val v = OscVoice(bus, wave)
        v.freq = freq.toFloat(); v.pan = pan; v.gain = 0f
        v.fadeTo(gain, 0.004f)
        v.stopAfter(dur.toFloat(), 0.012f)
        mix.add(v)
    }

    private fun later(delay: Double, run: () -> Unit) { timed.add(Timed(clock + delay, run)) }

    private fun panOf(dx: Double) = (dx / 25.0).coerceIn(-1.0, 1.0).toFloat() * 0.85f
    private fun distGain(dx: Double, dy: Double): Double { val d2 = (dx * dx + dy * dy) / 64.0; return 1.0 / (1.0 + d2) }

    // ---------------------------------------------------------------- her kare

    fun update(dt0: Double) {
        val dt = max(0.0, dt0)
        clock += dt
        applyVolumes()
        val due = timed.filter { it.at <= clock }
        if (due.isNotEmpty()) { timed.removeAll(due.toSet()); due.forEach { it.run() } }
        flushSpeech()
        val s = s ?: return
        if (ended) return
        val car = s.car
        val def = s.vehicle
        val grounded = car.rear.grounded || car.front.grounded
        val speed = abs(car.speed)
        val gas = car.gas.coerceIn(0.0, 1.0)

        // motor
        val ratio = car.rpm / def.engineRefRpm * def.enginePitch
        val rate = ratio.coerceIn(0.35, 2.4)
        val eg = (0.5 + 0.5 * gas).toFloat()
        // yüksek devirde biraz daha gür (gerçek motor gibi): rölanti 0.8, azami devir 1.0
        val load = (0.8 + 0.2 * ((car.rpm - def.idleRpm) / max(1.0, def.maxRpm - def.idleRpm)).coerceIn(0.0, 1.0)).toFloat()
        val ev = engine
        if (ev != null) {
            ev.rate = rate; engineRate = rate
            ev.fadeTo(1.1f * eg * load, 0.03f)
        }
        if (def.electric) {
            val f = (120.0 + car.rpm * 0.12).toFloat()
            engineRate = f / 100.0
            synthA?.let { it.freq = f; it.fadeTo(0.22f * eg * load, 0.03f) }
            synthB?.let { it.freq = f * 1.005f; it.fadeTo(0.07f * eg * load, 0.03f) }
        } else if (ev == null) {
            val f = (40.0 + car.rpm * 0.05).toFloat()
            engineRate = rate
            synthA?.let { it.freq = f; it.fadeTo(0.28f * eg * load, 0.03f) }
        }

        // yüzey yuvarlanması
        roll?.let { v ->
            val ws = max(max(abs(car.rear.spin), abs(car.front.spin)) * def.wheelR, speed)
            v.rate = 0.8 + 0.4 * min(1.0, speed / 20.0)
            v.fadeTo(if (grounded) (min(1.0, ws / 12.0) * 0.55).toFloat() else 0f, 0.08f)
        }
        // rüzgâr
        wind?.let { v ->
            val g = if (!grounded) 0.6 else min(1.0, speed / 25.0) * 0.3
            v.fadeTo((g * 0.7).toFloat(), 0.25f)
        }

        // eğim tonu ve takla bipi
        val a = wrapAngle(car.angle)
        tilt?.let { v ->
            v.freq = (330.0 * 2.0.pow(a / (PI / 2))).toFloat()
            v.fadeTo(if (set.tiltTone) 0.05f else 0f, 0.1f)
        }
        val deg = Math.toDegrees(abs(a))
        if (set.tiltTone && deg > 70.0 && clock >= nextBeep) {
            nextBeep = clock + max(0.08, 0.4 - (deg - 70.0) / 90.0)
            beepPulses++
            pulse(OscVoice.SINE, 1200.0, 0.09f, 0f, 0.04)
        }

        // yol radarı
        if (set.radar && clock >= nextRadar) {
            nextRadar = clock + 0.4
            val sl = ((s.terrain.height(car.x + 15.0) - s.terrain.height(car.x + 10.0)) / 5.0).coerceIn(-1.5, 1.5)
            radarPulses++
            pulse(OscVoice.TRI, 600.0 * 2.0.pow(sl), 0.045f, 0.5f, 0.035)
        }

        // işaret sesleri: en yakın altın ve yakıt bidonu
        if (clock >= nextCoinGlint || clock >= nextFuelBeacon) {
            val (coin, can) = s.nearestAhead()
            if (clock >= nextCoinGlint) {
                nextCoinGlint = clock + 0.7
                if (coin != null) {
                    val dx = coin.x - car.x; val dy = coin.y - car.y
                    val g = max(0.15, distGain(dx, dy))
                    val k = 2.0.pow(dy / 12.0)
                    pulse(OscVoice.SINE, 1800.0 * k, (0.1 * g).toFloat(), panOf(dx), 0.06)
                    if (coin.value >= 500) {      // 500'lük: biraz daha parlak çift ışıltı
                        doubleGlints++
                        later(0.09) { pulse(OscVoice.SINE, 2400.0 * k, (0.1 * g).toFloat(), panOf(dx), 0.06) }
                    }
                }
            }
            if (clock >= nextFuelBeacon) {
                nextFuelBeacon = clock + 1.0
                if (can != null) {
                    val dx = can.x - car.x; val dy = can.y - car.y
                    val g = max(0.15, distGain(dx, dy)); val pn = panOf(dx); val k = 2.0.pow(dy / 12.0)
                    val gg = (0.14 * g).toFloat()
                    pulse(OscVoice.SINE, 500.0 * k, gg, pn, 0.08)
                    later(0.11) { pulse(OscVoice.SINE, 750.0 * k, gg, pn, 0.1) }
                }
            }
        }
    }

    // ---------------------------------------------------------------- olaylar

    override fun onCoin(value: Int, streak: Int) {
        // her değerin kendi sesi vardır, perde değişmez; dosya yoksa genel "coin"
        val key = when { value >= 500 -> "coin_xl"; value >= 100 -> "coin_l"; value >= 25 -> "coin_m"; else -> "coin_s" }
        val p = pcm(key) ?: pcm("coin") ?: return
        lastCoinRate = 1.0
        shot(p, BUS_FX, 0.7f, 1.0, 0f)
    }

    override fun onTipWarning() { say("Dikkat, devrilmek üzeresin") }

    override fun onStuck() { say("Yokuşu çıkamıyorsun, biraz geri gidip hızlan") }

    override fun onFuel() {
        shot("fuel_pick", BUS_FX, 0.8f)
        say("Yakıt doldu")
    }

    override fun onFuelLow(pct: Int) {
        shot("fuel_low", BUS_FX, 0.8f)
        say("Yakıt yüzde $pct")
    }

    override fun onLand(impact: Double) {
        if (impact <= 0.0 || ended) return
        val c = s?.car
        val pan = if (c == null) 0f else when {
            c.rear.grounded && !c.front.grounded -> -0.6f
            c.front.grounded && !c.rear.grounded -> 0.6f
            else -> 0f
        }
        if (impact < 1.5) {
            // hafif iniş: çok hafif bir tık (art arda gelen küçük çarpmalar için sınırlı)
            if (impact >= 0.6 && clock - lastThud >= 0.15) {
                lastThud = clock
                shot("land_thud", BUS_FX, (0.12 * impact / 1.5).toFloat(), 1.5, pan)
            }
            return
        }
        if (clock - lastThud >= 0.1) {
            lastThud = clock
            shot("land_thud", BUS_FX, max(0.2, min(1.0, impact / 8.0)).toFloat(), 1.0, pan)
        }
        if (impact > 3.0 && clock - lastCreak >= 0.4) {
            lastCreak = clock
            shot("creak", BUS_FX, (0.35 + 0.4 * min(1.0, impact / 10.0)).toFloat(), 1.0, pan)
        }
    }

    override fun onFlip(count: Int, bonus: Int) {
        shot("flip", BUS_FX, 0.8f)
        say("Takla! $bonus altın")
    }

    override fun onAirBonus(sec: Double, bonus: Int) { say("Havada ${sec.toInt()} saniye, $bonus altın") }

    override fun onFeature(f: Feature) { say(f.kind.say) }

    override fun onDistance(m: Int) { if (set.distanceCalls) say("$m metre") }

    override fun onEnd(reason: EndReason) {
        // kaza sesi ve son duyuru dışında her şey kısılır
        ended = true
        timed.clear()
        for (v in loops) v.fadeTo(0f, 0.5f)
        if (reason == EndReason.CRASH) {
            if (pcm("crash") != null) shot("crash", BUS_FX, 1.0f) else shot("crash_car", BUS_FX, 1.0f)
        }
        pending = null
        // son duyuru aynı metin kısıtına takılmaz; yalnızca 0.8 sn aralığı korunur
        val msg = s?.endMessage?.takeIf { it.isNotEmpty() } ?: reason.say
        lastSaid.remove(msg)
        say(msg)
    }
}
