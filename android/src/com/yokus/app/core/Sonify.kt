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
    /** Testler için: son altın vuruşunun örnek hızı (hep 1.0), kuyruk süresi (sn), sönme süresi (sn) ve toplam vuruş sayısı. */
    @Volatile var lastCoinRate = 0.0
        private set
    @Volatile var lastCoinDur = 0.0
        private set
    @Volatile var lastCoinFade = 0.0
        private set
    @Volatile var coinHits = 0
        private set
    @Volatile var lastCoinChain = 0
        private set
    /** Testler için: kompresör sesinin örnek hızı ve kazancı, turbo sesi sayaçları, çamur sesi sayacı. */
    @Volatile var superRate = 0.0
        private set
    @Volatile var superGain = 0f
        private set
    @Volatile var spoolCount = 0
        private set
    @Volatile var blowCount = 0
        private set

    // --- zamanlayıcılar ---
    private var coinSeq = 0
    private var lastCoinAt = -1e9
    private var coinChain = 0
    private val rng = java.util.Random(0x5EEDL)
    private var supercharger: SampleVoice? = null
    private var gasLowSeen = true
    private var gasHighSeen = false
    private var nextSpool = 0.0
    private var nextBlow = 0.0
    private var nextSlime = 0.0
    private var nextKick = 0.0
    private var nextBeep = 0.0
    private var nextRadar = 0.0
    private var nextCoinGlint = 0.0
    private var nextFuelBeacon = 0.0
    private var lastThud = -1e9
    private var lastCreak = -1e9
    private var lastMudLand = -1e9
    private class Timed(val at: Double, val run: () -> Unit)
    private val timed = ArrayList<Timed>()

    // --- duyuru kısıtlayıcı ---
    private var pending: String? = null
    private var lastSpeakAt = -1e9
    private val lastSaid = HashMap<String, Double>()

    private companion object {
        const val MASTER_TRIM = 1.6f
        /** Mesafe duyurusu aralığı (m): uzun sessizlikleri böler, ilerlemeyi bildirir. */
        const val DIST_CALL_EVERY = 250
        val COIN_S = arrayOf("coin_s", "coin_s2", "coin_s3")   // coin_hcr yoksa yedekler
        /** Yeni motor döngüleri: döngü noktası dosyaya işlenmiştir, ek bindirme gerekmez. */
        val BAKED_LOOPS = setOf("eng_hill", "eng_monster2", "eng_rally2", "supercharger")
        /** Yeni motor dosyası yoksa eski motor sesi. */
        val ENGINE_FALLBACK = mapOf("eng_hill" to "eng_jeep", "eng_monster2" to "eng_monster", "eng_rally2" to "e2_med_on")
    }

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
            val k0 = def.engineKey
            val v = loopVoice(k0, BUS_ENGINE, if (k0 in BAKED_LOOPS) 0.0 else 0.08)?.also { engineKind = k0 }
                ?: ENGINE_FALLBACK[k0]?.let { fb -> loopVoice(fb, BUS_ENGINE, 0.08)?.also { engineKind = fb } }
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
        if (def.supercharged) supercharger = loopVoice("supercharger", BUS_ENGINE, 0.0)
        gasLowSeen = true; gasHighSeen = false
        nextSpool = clock; nextBlow = clock; nextSlime = clock + 0.5; nextKick = clock + 1.0
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
        engine = null; supercharger = null; synthA = null; synthB = null; roll = null; wind = null; amb = null; tilt = null
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

        // turbo ve kompresör
        val rpmFrac = ((car.rpm) / max(1.0, def.maxRpm))
        if (gas < 0.2) gasLowSeen = true
        if (gas > 0.7) gasHighSeen = true
        if (def.turbo) {
            if (gas > 0.8 && gasLowSeen) {
                gasLowSeen = false
                if (rpmFrac < 0.6 && clock >= nextSpool) { nextSpool = clock + 1.2; spoolCount++; shot("turbo_spool", BUS_FX, 0.6f) }
            }
            if (gas < 0.2 && gasHighSeen) {
                gasHighSeen = false
                if (rpmFrac > 0.55 && clock >= nextBlow) { nextBlow = clock + 1.2; blowCount++; shot("turbo_blow", BUS_FX, 0.7f) }
            }
        }
        supercharger?.let { v ->
            v.rate = 0.7 + 1.0 * rpmFrac.coerceIn(0.0, 1.2)
            superRate = v.rate
            superGain = (0.5 * gas).toFloat()
            v.fadeTo(superGain, 0.04f)
        }
        // çamurda yuvarlanma: yuvarlanırken ~1.5 sn'de bir sümük, ara sıra çamur fırlatma
        if (s.map.surface == Surface.MUD && grounded && speed > 5.0) {
            if (clock >= nextSlime) { nextSlime = clock + 1.5; shot("mud_slime", BUS_FX, (0.25 + 0.25 * min(1.0, speed / 20.0)).toFloat(), 1.0, 0f) }
            if (speed > 8.0 && clock >= nextKick) {
                nextKick = clock + 2.0 + 3.0 * rng.nextDouble()
                shot("splash_out", BUS_FX, 0.3f, 0.95 + 0.1 * rng.nextDouble(), (rng.nextDouble() * 0.6 - 0.3).toFloat())
            }
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

    /**
     * Her altın tam bir coin_hcr vuruşudur ve hemen çalar (zaman aşımı yok; art arda alımlar üst üste biner).
     * Perde hiç değişmez. Hızlı zincirde (önceki alımdan < 0.6 sn) vuruşun kuyruğu ve sönmesi zincir uzunluğuna
     * göre en çok %40 kısalır: hızlı bir tril gibi duyulur. Her vuruşta ±1.5 dB kazanç ve minik zaman oynaması vardır.
     */
    override fun onCoin(value: Int, streak: Int) {
        val p = pcm("coin_hcr") ?: pcm(COIN_S[coinSeq++ % COIN_S.size]) ?: pcm("coin_s") ?: pcm("coin") ?: return
        val dt = clock - lastCoinAt
        coinChain = if (dt < 0.6) coinChain + 1 else 1
        lastCoinAt = clock
        val tight = if (coinChain >= 2) min(0.4, 0.1 * (coinChain - 1)) else 0.0
        val dur = p.frames.toDouble() / p.rate * (1.0 - tight)
        val fade = 0.05 * (1.0 - tight)
        val gain = (0.7 * 10.0.pow((rng.nextDouble() * 3.0 - 1.5) / 20.0)).toFloat()
        lastCoinRate = 1.0; lastCoinDur = dur; lastCoinFade = fade; lastCoinChain = coinChain
        coinHits++
        val jitter = if (coinChain >= 2) rng.nextDouble() * 0.006 else 0.0
        val play = {
            val v = SampleVoice(p, BUS_FX, false, durSec = dur, fadeOutSec = fade)
            v.rate = 1.0; v.gain = gain
            mix.add(v)
        }
        if (jitter < 0.001) play() else later(jitter) { play() }
    }

    override fun onTipWarning() { say("Dikkat, devrilmek üzeresin") }

    override fun onStuck() { say("Yokuşu çıkamıyorsun, biraz geri gidip hızlan") }

    override fun onFuel() {
        shot("fuel_pick", BUS_FX, 0.8f)
        shot("fuel_pick_hcr", BUS_FX, 0.8f)
        say("Yakıt doldu")
    }

    override fun onFuelLow(pct: Int) {
        shot("fuel_low", BUS_FX, 0.8f)
        shot("fuel_low_hcr", BUS_FX, 0.7f)
        say("Yakıt yüzde $pct")
    }

    override fun onLand(impact: Double) {
        if (impact <= 0.0 || ended) return
        val c = s?.car
        val ss = s
        if (ss != null && (ss.map.surface == Surface.MUD || ss.map.id == "swamp") && impact >= 0.6 && clock - lastMudLand >= 0.15) {
            lastMudLand = clock
            shot("mud_slime", BUS_FX, (0.3 + 0.5 * min(1.0, impact / 8.0)).toFloat(), 1.0, 0f)
            if (impact >= 1.5) shot("splash_in", BUS_FX, (0.3 + 0.5 * min(1.0, impact / 8.0)).toFloat(), 1.0, 0f)
        }
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

    override fun onDistance(m: Int) { if (set.distanceCalls && m % DIST_CALL_EVERY == 0) say("$m metre") }

    override fun onEnd(reason: EndReason) {
        // kaza sesi ve son duyuru dışında her şey kısılır
        ended = true
        timed.clear()
        for (v in loops) v.fadeTo(0f, 0.5f)
        if (reason == EndReason.CRASH) {
            if (pcm("crash") != null) shot("crash", BUS_FX, 1.0f) else shot("crash_car", BUS_FX, 1.0f)
            shot("crash_crack", BUS_FX, 0.9f)      // kafa çarpması ve ters dönme: ek çatırtı
        }
        pending = null
        // son duyuru aynı metin kısıtına takılmaz; yalnızca 0.8 sn aralığı korunur
        val msg = s?.endMessage?.takeIf { it.isNotEmpty() } ?: reason.say
        lastSaid.remove(msg)
        say(msg)
    }
}
