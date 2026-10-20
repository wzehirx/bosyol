package com.bosyol.app.core

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

interface Speaker { fun say(text: String, urgent: Boolean = false) }

interface Bank {
    fun get(key: String): Pcm?
    fun isCustom(key: String): Boolean
}

class GameSettings {
    @Volatile var traffic = true
    @Volatile var latch = true
    @Volatile var pace = true
    @Volatile var callouts = false
    @Volatile var shiftSay = false
    @Volatile var volMaster = 85
    @Volatile var volEngine = 80
    @Volatile var volWorld = 80
}

private fun clamp(v: Double, a: Double, b: Double) = if (v < a) a else if (v > b) b else v
private fun approach(v: Double, t: Double, d: Double) = if (v < t) min(t, v + d) else max(t, v - d)
private fun lerp(a: Double, b: Double, t: Double) = a + (b - a) * t

/* ---------------- Motor modelleri ---------------- */
interface EngineModel {
    fun start()
    fun update(rpm: Double, load: Double, v: Double, regen: Double)
    fun stop()
}

class Layer(val rpm: Double, val on: Pcm, val off: Pcm?, val idle: Boolean)

/** Devir katmanları arasında eşit güçte geçiş; yük ile gazda ve gaz kesik kayıtların karışımı. */
class SampleEngine(mix: Mixer, private val layers: List<Layer>, private val minRate: Double) : EngineModel {
    private val on = layers.map { mix.add(SampleVoice(it.on, BUS_ENGINE, true, startSec = Math.random() * it.on.duration)) }
    private val off = layers.map { l -> l.off?.let { mix.add(SampleVoice(it, BUS_ENGINE, true, startSec = Math.random() * it.duration)) } }
    private var level = 0.0
    private var levelT = 0.0
    private val w = DoubleArray(layers.size)

    override fun start() { levelT = 1.0 }
    override fun update(rpm: Double, load: Double, v: Double, regen: Double) {
        level = approach(level, levelT, 0.05)
        val n = layers.size
        java.util.Arrays.fill(w, 0.0)
        if (rpm <= layers[0].rpm) w[0] = 1.0
        else if (rpm >= layers[n - 1].rpm) w[n - 1] = 1.0
        else for (i in 0 until n - 1) if (rpm < layers[i + 1].rpm) {
            val t = (rpm - layers[i].rpm) / (layers[i + 1].rpm - layers[i].rpm)
            w[i] = cos(t * PI / 2); w[i + 1] = sin(t * PI / 2); break
        }
        // Gaz kapalıyken motor duyulmaya devam eder; gaza basıldığı an gazda kayıt ve ses düzeyi hemen yükselir
        val lo = load.coerceIn(0.0, 1.0).pow(0.6)
        val vol = 0.72 + 0.42 * load.coerceIn(0.0, 1.0).pow(0.7)
        for (i in 0 until n) {
            val rate = clamp(rpm / layers[i].rpm, minRate, 2.3)
            on[i].rate = rate
            val offV = off[i]
            if (offV != null) {
                offV.rate = rate
                on[i].fadeTo((level * w[i] * vol * (0.15 + 0.85 * lo)).toFloat(), 0.03f)
                offV.fadeTo((level * w[i] * vol * (1 - lo) * 0.95).toFloat(), 0.03f)
            } else {
                val g = if (layers[i].idle) 0.88 else vol * (0.6 + 0.4 * lo)
                on[i].fadeTo((level * w[i] * g).toFloat(), 0.03f)
            }
        }
    }
    override fun stop() { on.forEach { it.stop(0.3f) }; off.forEach { it?.stop(0.3f) } }
}

/** Tek döngülü motor (içe aktarılan elektrikli araç sesi) */
class LoopEngine(mix: Mixer, pcm: Pcm) : EngineModel {
    private val voice = mix.add(SampleVoice(pcm, BUS_ENGINE, true))
    private var on = false
    override fun start() { on = true }
    override fun update(rpm: Double, load: Double, v: Double, regen: Double) {
        voice.rate = 0.55 + clamp(rpm / 12000, 0.0, 1.0) * 1.3
        val g = if (on) 0.1 + clamp(v / 4, 0.0, 1.0) * (0.3 + 0.6 * max(load, regen * 0.6)) else 0.0
        voice.fadeTo(g.toFloat(), 0.05f)
    }
    override fun stop() = voice.stop(0.3f)
}

/** Elektrikli motor sentezi */
class EvSynth(private val mix: Mixer) : EngineModel {
    private val m1 = mix.add(OscVoice(BUS_ENGINE, OscVoice.SINE))
    private val m2 = mix.add(OscVoice(BUS_ENGINE, OscVoice.TRI))
    private val m3 = mix.add(OscVoice(BUS_ENGINE, OscVoice.SINE))
    private val m4 = mix.add(OscVoice(BUS_ENGINE, OscVoice.SAW).also { it.filter = Biquad(Biquad.BANDPASS, 1200f, 6f) })
    private val hum = mix.add(SampleVoice(Noises.pink, BUS_ENGINE, true).also { it.filter = Biquad(Biquad.BANDPASS, 240f, 2f) })
    private var on = 0.0
    override fun start() {
        on = 1.0
        listOf(523f, 659f, 784f).forEachIndexed { i, f ->
            val o = mix.add(OscVoice(BUS_FX, OscVoice.SINE)); o.freq = f
            Thread { Thread.sleep(120L * i); o.fadeTo(0.12f, 0.02f); o.stopAfter(0.05f, 0.25f) }.start()
        }
    }
    override fun update(rpm: Double, load: Double, v: Double, regen: Double) {
        val f = (18 + rpm / 60 * 0.9).toFloat()
        m1.freq = f * 2; m2.freq = f * 6.02f; m3.freq = (900 + rpm * 0.32).toFloat(); m4.freq = f * 3
        m4.filter?.freq = (1200 + rpm * 0.25).toFloat()
        val mv = clamp(v / 6, 0.0, 1.0) * on; val act = max(load, regen * 0.7)
        m1.fadeTo((mv * (0.05 + 0.12 * act)).toFloat(), 0.05f)
        m2.fadeTo((mv * (0.012 + 0.05 * act)).toFloat(), 0.05f)
        m3.fadeTo((mv * (0.004 + 0.016 * act)).toFloat(), 0.05f)
        m4.fadeTo((mv * 0.03 * act).toFloat(), 0.05f)
        hum.fadeTo(((0.02 + 0.03 * act) * on).toFloat(), 0.1f)
    }
    override fun stop() { listOf(m1, m2, m3, m4, hum).forEach { it.stop(0.3f) } }
}

object Noises {
    val white by lazy { Noise.make(0) }
    val pink by lazy { Noise.make(1) }
    val brown by lazy { Noise.make(2) }
}

/* ---------------- Trafik ve çevre ---------------- */
class TrafficCar(@Volatile var s: Double, val lat: Double, val v: Double, val siren: Boolean, val color: Int, val kind: Int) {
    var voices = ArrayList<SampleVoice>()
    var rates = ArrayList<Double>()
    var noise: SampleVoice? = null
    var prevD = -1.0
    var dop = 1.0
    var honk = false
}

class EmitterInst(val def: Emitter, val voice: SampleVoice, val sp: Spatial) { var s = 0.0; var lat = 0.0; var y = 1.0 }

/**
 * Bir sürüş oturumu. Fizik gerçek araç verileriyle çalışır:
 * - Gaz pedalı basılı tutuldukça derinleşir; otomatik şanzıman pedal konumuna göre vites seçer ve gaza basınca küçültür.
 * - Tork konvertörü kalkışta motoru serbest bırakır, hız arttıkça kilitlenir.
 * - Lastik tutunması bir sürtünme dairesidir: virajda harcanan tutunma frene ve gaza kalmaz.
 * - Viraja hızlı girilirse araç dışa kayar; kenar şeridine, sonra çakıla çıkar; fazlasında kaza olur.
 */
class Game(
    private val mix: Mixer, private val bank: Bank, private val speak: Speaker,
    private val set: GameSettings, val vehicle: VehicleDef, val env: EnvDef
) {
    val road = Road(env)
    @Volatile var s = 30.0
    @Volatile var v = 0.0
    @Volatile var rpm = vehicle.idle
    @Volatile var gear = 0
    @Volatile var brk = 0.0
    @Volatile var pedal = 0.0
    @Volatile var auto = false
    @Volatile var cruise = 90
    @Volatile var windows = true
    @Volatile var squeal = 0.0
    /** Şerit ortasına göre yanal konum (m), artı sağ */
    @Volatile var offset = 0.0
    /** 0 asfalt, 1 kenar şeridi, 2 çakıl */
    @Volatile var surface = 0
    @Volatile var crashT = 0.0
    /** Motor sesinin yük düzeyi (0..1) */
    val engineLoad get() = sndLoad
    private var latVel = 0.0
    private var shiftT = 0.0
    private var shiftDur = 0.3
    private var shiftFromRpm = 0.0
    private var shiftCool = 0.0
    private var cut = 0.0
    private var ai = 0.0
    private var autoPedal = 0.0
    private var autoBrk = 0.0
    private var sndLoad = 0.0
    private var lastBand = 0
    private var calloutT = 0.0
    private var lastSurface = 0
    private var offroadSaid = 0.0
    var time = 0.0
        private set

    // Kontroller
    @Volatile var gasHold = false
    @Volatile var gasLatch = false
    @Volatile var brakeHold = false
    @Volatile var brakeLatch = false
    @Volatile var keyGas = false
    @Volatile var keyBrake = false
    @Volatile var gasPulse = 0.0
    @Volatile var brakePulse = 0.0
    private val gasPressed get() = gasHold || keyGas || gasPulse > 0
    val gasOn get() = gasPressed || gasLatch
    private val brakePressed get() = brakeHold || keyBrake || brakePulse > 0
    val brakeOn get() = brakePressed || brakeLatch

    private var engine: EngineModel? = null
    private val beds = ArrayList<SampleVoice>()
    private val emitters = ArrayList<EmitterInst>()
    private val traffic = ArrayList<TrafficCar>()
    @Volatile var trafficView: Array<TrafficCar> = emptyArray()
        private set
    private val nextShot = DoubleArray(env.oneshots.size)
    private var nextTraffic = 0.0
    private var nextSiren = 0.0
    private lateinit var tire: SampleVoice
    private lateinit var tire2: SampleVoice
    private lateinit var wind: SampleVoice
    private lateinit var wet: SampleVoice
    private lateinit var gravel: SampleVoice
    private lateinit var gravelLow: SampleVoice
    private lateinit var rumble: OscVoice
    private var screech: SampleVoice? = null
    private var horn: SampleVoice? = null
    private val rnd = java.util.Random()
    private val p = RoadPoint()
    private val q = RoadPoint()
    val lane get() = env.width / 2

    private fun rand(a: Double, b: Double) = a + rnd.nextDouble() * (b - a)

    /** Bu yolda ve bu araçla kullanılabilecek en büyük yanal ivme (m/s²) */
    private fun gripAccel(): Double = vehicle.mu * 9.81 * env.grip * when (surface) { 2 -> 0.55; 1 -> 0.9; else -> 1.0 }

    /** Bir viraj için önerilen en yüksek hız (km/sa), 10'a aşağı yuvarlanmış */
    fun advisedKmh(k: Double): Int {
        val vmax = sqrt(0.82 * vehicle.mu * 9.81 * env.grip / abs(k)) * 3.6
        return max(20, (vmax / 10).toInt() * 10)
    }

    /* ---------- başlat / durdur ---------- */
    fun start() {
        applyVolumes()
        bank.get(env.bed)?.let { beds.add(mix.add(SampleVoice(it, BUS_WORLD, true, 3.0, rand(0.0, it.duration * 0.5))).also { b -> b.fadeTo(env.bedGain, 2f) }) }
        for (c in env.cabin) bank.get(c.key)?.let { beds.add(mix.add(SampleVoice(it, BUS_CABIN, true, 2.0, rand(0.0, it.duration * 0.5))).also { b -> b.fadeTo(c.gain, 1.5f) }) }
        for (d in env.emitters) {
            val pcm = bank.get(d.key) ?: continue
            repeat(d.n) {
                val sp = Spatial(10f, 1f)
                val vc = SampleVoice(pcm, BUS_WORLD, true, 2.5, rand(0.0, pcm.duration * 0.6))
                vc.rate = rand(0.94, 1.06); vc.spatial = sp
                val e = EmitterInst(d, vc, sp)
                place(e, s + rand(-60.0, 420.0))
                mix.add(vc).fadeTo(d.gain, 2f)
                emitters.add(e)
            }
        }
        env.oneshots.forEachIndexed { i, o -> nextShot[i] = rand(o.everyMin, o.everyMax) }
        nextTraffic = rand(4.0, 8.0); nextSiren = rand(25.0, 60.0)
        tire = mix.add(SampleVoice(Noises.brown, BUS_CABIN, true, startSec = rand(0.0, 2.0)).also { it.filter = Biquad(Biquad.LOWPASS, 200f, 0.7f) })
        tire2 = mix.add(SampleVoice(Noises.pink, BUS_CABIN, true, startSec = rand(0.0, 2.0)).also { it.filter = Biquad(Biquad.BANDPASS, 600f, 0.9f) })
        wind = mix.add(SampleVoice(Noises.pink, BUS_CABIN, true, startSec = rand(0.0, 2.0)).also { it.filter = Biquad(Biquad.BANDPASS, 500f, 0.6f) })
        wet = mix.add(SampleVoice(Noises.white, BUS_CABIN, true, startSec = rand(0.0, 2.0)).also { it.filter = Biquad(Biquad.HIGHPASS, 2500f, 0.5f) })
        gravel = mix.add(SampleVoice(Noises.white, BUS_CABIN, true, startSec = rand(0.0, 2.0)).also { it.filter = Biquad(Biquad.BANDPASS, 2200f, 0.8f) })
        gravelLow = mix.add(SampleVoice(Noises.brown, BUS_CABIN, true, startSec = rand(0.0, 2.0)).also { it.filter = Biquad(Biquad.LOWPASS, 140f, 0.8f) })
        rumble = mix.add(OscVoice(BUS_CABIN, OscVoice.SAW).also { it.filter = Biquad(Biquad.LOWPASS, 260f, 0.9f) })
        startEngine()
        setWindows(windows, true)
    }

    private fun startEngine() {
        val V = vehicle
        if (V.synth) {
            val loop = if (bank.isCustom("ev_loop")) bank.get("ev_loop") else null
            engine = if (loop != null) LoopEngine(mix, loop) else EvSynth(mix)
            engine!!.start(); rpm = 0.0; return
        }
        val pre = V.customPrefix ?: V.set!!
        val startPcm = bank.get(pre + "_start")
        val layers = ArrayList<Layer>()
        if (V.customPrefix != null) {
            bank.get("${pre}_idle")?.let { layers.add(Layer(850.0, it, null, true)) }
            val main = bank.get("${pre}_loop") ?: return
            layers.add(Layer(V.customRpm.toDouble(), main, bank.get("${pre}_off"), false))
            bank.get("${pre}_high")?.let { layers.add(Layer(max(V.customRpm * 1.7, 4500.0), it, null, false)) }
        } else {
            val names = listOf("idle", "low", "med", "high")
            for (i in V.layers.indices) {
                val on = bank.get(if (i == 0) "${pre}_idle" else "${pre}_${names[i]}_on") ?: continue
                layers.add(Layer(V.layers[i].toDouble(), on, if (i == 0) null else bank.get("${pre}_${names[i]}_off"), i == 0))
            }
        }
        if (layers.isEmpty()) return
        val eng = SampleEngine(mix, layers, if (V.customPrefix != null) 0.3 else 0.5)
        engine = eng
        rpm = V.idle
        if (startPcm != null) {
            mix.add(SampleVoice(startPcm, BUS_ENGINE, false, fadeOutSec = 0.2)).fadeTo(0.9f, 0.01f)
            startDelay = min(startPcm.duration, 3.0) * 0.65
        } else eng.start()
    }
    private var startDelay = 0.0

    fun stop() { mix.stopAll(0.6f) }

    fun applyVolumes() {
        mix.master = ((set.volMaster / 100.0).pow(1.6) * 1.1).toFloat()
        mix.busGain[BUS_ENGINE] = ((set.volEngine / 100.0).pow(1.5) * 1.1).toFloat()
        mix.busGain[BUS_WORLD] = ((set.volWorld / 100.0).pow(1.5) * 1.2 * (if (windows) 1.0 else 0.55)).toFloat()
        mix.busGain[BUS_CABIN] = (set.volWorld / 100.0).pow(1.2).toFloat()
        mix.busGain[BUS_FX] = 1f
    }

    fun setWindows(open: Boolean, silent: Boolean) {
        windows = open
        mix.busLp[BUS_WORLD] = if (open) 20000f else 1200f
        mix.busLp[BUS_ENGINE] = if (open) 9000f else 5500f
        applyVolumes()
        if (!silent) {
            val o = mix.add(OscVoice(BUS_FX, OscVoice.SAW).also { it.filter = Biquad(Biquad.LOWPASS, 700f, 0.7f) })
            o.freq = 105f; o.fadeTo(0.05f, 0.08f); o.stopAfter(1.2f, 0.15f)
            speak.say(if (open) "Camlar açık" else "Camlar kapalı")
        }
    }

    /* ---------- kontroller ---------- */
    fun pedalTouched(gas: Boolean) {
        if (auto) setAuto(false)
        if (gas) brakeLatch = false else gasLatch = false
    }
    fun setLatch(gas: Boolean, on: Boolean) {
        pedalTouched(gas)
        if (gas) gasLatch = on else brakeLatch = on
        speak.say(if (gas) (if (on) "Gaz sabit. Bırakmak için tekrar dokun." else "Gaz bırakıldı.") else (if (on) "Fren sabit." else "Fren bırakıldı."))
    }
    fun pulse(gas: Boolean) { pedalTouched(gas); if (gas) gasPulse = 0.7 else brakePulse = 0.7 }

    fun setAuto(on: Boolean, silent: Boolean = false) {
        auto = on; ai = pedal * 0.8
        if (on) { gasLatch = false; brakeLatch = false; if (v * 3.6 > 25) cruise = ((v * 3.6 / 10).roundToInt() * 10).coerceIn(30, 160) }
        if (!silent) speak.say(if (on) "Otomatik sürüş açık. Hedef $cruise kilometre." else "Otomatik sürüş kapandı.")
    }
    fun changeCruise(d: Int) {
        cruise = (cruise + d).coerceIn(30, 160)
        speak.say("Hedef $cruise kilometre" + if (auto) "" else ". Otomatik sürüş kapalı.")
    }

    fun hornOn() {
        if (horn != null) return
        val pcm = bank.get("horn") ?: return
        val custom = bank.isCustom("horn")
        val h = SampleVoice(pcm, BUS_FX, true, 0.0, if (custom) 0.0 else 0.35, loopStartSec = if (custom) 0.0 else 0.35, loopEndSec = if (custom) -1.0 else 2.4)
        h.fadeTo(0.7f, 0.01f); horn = mix.add(h)
    }
    fun hornOff() { horn?.stop(0.04f); horn = null }

    fun statusText(): String {
        val kmh = (v * 3.6).roundToInt()
        var t = "$kmh kilometre, " + if (vehicle.synth) "tek vites" else "${gear + 1}. vites"
        if (!vehicle.synth) t += ", ${(rpm / 100).roundToInt() * 100} devir"
        t += if (auto) ". Otomatik sürüş, hedef $cruise" else ". Elle sürüş"
        when (surface) { 1 -> t += ". Yol kenarındasın"; 2 -> t += ". Yoldan çıktın, çakıldasın" }
        return "$t. ${env.name}."
    }

    /* ---------- ana döngü ---------- */
    fun update(dt: Double) {
        time += dt
        if (gasPulse > 0) gasPulse = max(0.0, gasPulse - dt)
        if (brakePulse > 0) brakePulse = max(0.0, brakePulse - dt)
        if (startDelay > 0) { startDelay -= dt; if (startDelay <= 0) engine?.start() }
        // Fizik küçük adımlarla: yüksek hızda ve sert virajda kararlı kalsın
        val steps = max(1, kotlin.math.ceil(dt / 0.01).toInt())
        val h = dt / steps
        repeat(steps) { physics(h) }
        road.at(s, lane + offset, p)
        mix.lx = p.x; mix.lz = p.z; mix.lh = p.h
        engine?.update(rpm, sndLoad, v, brk)
        cabin()
        updateTraffic(dt)
        for (e in emitters) if (e.s < s - 140) place(e, s + rand(300.0, 500.0))
        oneShots()
        paceNotes()
        surfaceNotes(dt)
        if (set.callouts) {
            val band = (v * 3.6 / 20).roundToInt()
            if (band != lastBand) { calloutT += dt; if (calloutT > 1.2) { lastBand = band; calloutT = 0.0; if (band > 0) speak.say("${band * 20}") } } else calloutT = 0.0
        }
    }

    /** Motor torku (Nm): rölanti, zirve ve devir sınırı arasında yumuşak eğri */
    private fun torqueAt(r: Double): Double {
        val V = vehicle
        if (V.synth) return min(V.torquePeak, V.maxPower / max(1.0, r * 2 * PI / 60))
        return if (r <= V.peakRpm) {
            val t = ((r - V.idle) / (V.peakRpm - V.idle)).coerceIn(0.0, 1.0)
            V.torqueIdle + (V.torquePeak - V.torqueIdle) * sin(t * PI / 2)
        } else {
            val t = ((r - V.peakRpm) / (V.redline - V.peakRpm)).coerceIn(0.0, 1.0)
            V.torquePeak + (V.torqueRed - V.torquePeak) * t * t
        }
    }

    private fun ratio(g: Int) = vehicle.gears[g] * vehicle.final

    private fun physics(dt: Double) {
        val V = vehicle
        val crashed = crashT > 0
        if (crashed) crashT -= dt

        // Gaz pedalı: basılı tutuldukça derinleşir, bırakınca hızla kalkar
        when {
            crashed -> pedal = 0.0
            auto -> pedal = approach(pedal, autoPedal, dt * 1.6)
            gasPressed -> pedal = min(1.0, pedal + dt * if (pedal < 0.35) 2.6 else 0.22)
            gasLatch -> pedal = approach(pedal, 0.55, dt * 0.9)
            else -> pedal = max(0.0, pedal - dt * 5.0)
        }
        // Fren basıncı: önce yumuşak, basılı tuttukça sertleşir
        brk = when {
            crashed -> 0.0
            auto -> approach(brk, autoBrk, dt * 1.2)
            brakePressed -> min(1.0, brk + dt * if (brk < 0.18) 0.9 else 0.24)
            brakeLatch -> approach(brk, 0.42, dt * 0.6)
            else -> approach(brk, 0.0, dt * 2.5)
        }

        val wheelRpm = v / (2 * PI * V.r) * 60
        val coupled = wheelRpm * ratio(gear)
        var torque: Double
        var conv = 1.0
        var lock = 1.0
        if (V.synth) {
            rpm = coupled
            torque = if (pedal > 0.02) torqueAt(rpm) * pedal else -V.engBrakeNm * clamp(v / 4, 0.0, 1.0)
        } else {
            // Tork konvertörü: kalkışta motor serbest döner ve torku katlar; hız arttıkça kilitlenir
            lock = ((coupled - V.idle * 0.9) / (V.launch - V.idle * 0.9)).coerceIn(0.0, 1.0)
            val freeRpm = V.idle + pedal * (V.launch * 1.05 - V.idle)
            var target = max(coupled, freeRpm * (1 - lock) + coupled * lock)
            target = max(target, V.idle)
            if (shiftT > 0) {
                shiftT -= dt
                val t = (1 - shiftT / shiftDur).coerceIn(0.0, 1.0)
                target = shiftFromRpm + (target - shiftFromRpm) * t
            }
            rpm += (target - rpm) * min(1.0, dt * 14)
            conv = 1 + 0.9 * (1 - lock)
            if (cut > 0) cut -= dt
            if (rpm >= V.redline && cut <= 0) cut = 0.07
            torque = if (cut > 0) 0.0 else torqueAt(rpm) * pedal
            if (shiftT > 0) torque *= 0.35
            if (pedal < 0.03 && coupled > V.idle * 1.05) torque = -V.engBrakeNm * (0.3 + 0.7 * rpm / V.redline) * lock
        }
        val tmax = max(1.0, torqueAt(max(rpm, 1.0)))
        val loadT = if (torque > 0) (torque / tmax).coerceIn(0.0, 1.0) else 0.0
        sndLoad = approach(sndLoad, loadT, dt * if (loadT > sndLoad) 14 else 5)

        // Tutunma: virajın istediği yanal ivme ve kalan boyuna tutunma (sürtünme dairesi)
        val k = road.curvature(s)
        val aReq = v * v * k
        val aMax = gripAccel()
        val aLong = sqrt(max(0.0, aMax * aMax - aReq * aReq))
        var fDrive = if (torque > 0) torque * ratio(gear) * V.efficiency / V.r * conv else torque * ratio(gear) / V.r
        val tractionLimit = aLong * V.mass * 0.55
        var spin = 0.0
        if (fDrive > tractionLimit) { spin = ((fDrive - tractionLimit) / tractionLimit).coerceIn(0.0, 1.0); fDrive = tractionLimit }
        val brakeWant = brk * V.brakeMax * env.grip.coerceAtLeast(0.7)
        val brakeA = min(brakeWant, aLong)
        val lockup = if (brakeWant > aLong + 0.3 && v > 3) ((brakeWant - aLong) / 3).coerceIn(0.0, 1.0) else 0.0
        val drag = 0.5 * 1.2 * V.cd * V.area * v * v
        val roll = (if (v > 0.1) V.crr * V.mass * 9.81 else 0.0) * (if (surface == 2) 4.0 else 1.0)
        var acc = (fDrive - drag - roll) / V.mass - (if (v > 0.01) brakeA * clamp(v / 1.2, 0.35, 1.0) else 0.0)
        if (surface == 2) acc -= 1.5 * clamp(v / 5, 0.0, 1.0)

        // Viraj: tutunma yetmezse araç dışa kayar ve hız kaybeder
        val excess = abs(aReq) - aMax
        if (excess > 0 && !crashed) {
            latVel += -kotlin.math.sign(k) * excess * dt
            acc -= excess * 0.35
        } else {
            val back = (-offset * 0.9).coerceIn(-1.6, 1.6)
            latVel = approach(latVel, back, dt * if (surface == 2) 1.5 else 3.5)
        }
        offset += latVel * dt
        v = max(0.0, v + acc * dt)
        if (v < 0.12 && pedal < 0.03) v = max(0.0, v - dt * 0.6)

        // Yol kenarı: kenar şeridi, çakıl, kaza
        val right = env.width / 2; val left = -1.5 * env.width
        val dR = right - (offset + 0.9); val dL = (offset - 0.9) - left
        surface = if (dR < -0.4 || dL < -0.4) 2 else if (dR < 0.35 || dL < 0.35) 1 else 0
        if (!crashed && (offset > right + 5.0 || offset < left - 5.0)) {
            if (v > 9) crash() else { offset = offset.coerceIn(left - 5.0, right + 5.0); latVel = 0.0 }
        }

        val lat = if (aMax > 0) abs(aReq) / aMax else 0.0
        squeal = max(max(((lat - 0.85) / 0.15).coerceIn(0.0, 1.0), lockup), spin * 0.8) * (if (surface == 2) 0.3 else 1.0)

        // Otomatik sürüş: hedef hız ve önündeki virajlar
        if (auto && !crashed) {
            val vT = min(cruise / 3.6, road.safeSpeed(s, v, 0.72 * aMax, 3.0))
            val err = vT - v
            ai = clamp(ai + err * dt * 0.04, 0.0, 0.8)
            autoPedal = clamp(err * 0.22 + ai, 0.0, 1.0)
            autoBrk = if (err < -1.2) clamp(-err * 0.1, 0.0, 0.6) else 0.0
            if (autoBrk > 0) autoPedal = 0.0
        }

        if (!V.synth) shiftLogic(dt, wheelRpm, lock)
        s += v * dt
    }

    /** Otomatik şanzıman: pedal konumuna göre vites büyütür; gaza basınca ya da hız düşünce küçültür */
    private fun shiftLogic(dt: Double, wheelRpm: Double, lock: Double) {
        val V = vehicle
        if (shiftCool > 0) { shiftCool -= dt; return }
        if (shiftT > 0 || crashT > 0) return
        if (v < 1.0 && gear > 0) { gear = 0; return }
        val pe = pedal.coerceIn(0.0, 1.0)
        val up = V.upLight + (V.upFull - V.upLight) * pe.pow(1.4)
        val down = V.downLight + (V.upFull * 0.5 - V.downLight) * pe.pow(1.2)
        val now = wheelRpm * ratio(gear)
        if (gear < V.gears.size - 1 && lock >= 1.0 && now > up && wheelRpm * ratio(gear + 1) > down + 150) {
            shift(gear + 1, if (V.kind == "sport") 0.15 else 0.32)
        } else if (gear > 0 && now < down && wheelRpm * ratio(gear - 1) < V.upFull * 0.97) {
            shift(gear - 1, 0.22)
        }
    }

    private fun shift(to: Int, dur: Double) {
        shiftFromRpm = rpm; shiftDur = dur; shiftT = dur; shiftCool = dur + 0.25
        gear = to
        if (set.shiftSay) speak.say("${gear + 1}. vites")
    }

    private fun crash() {
        bank.get("crash")?.let { mix.add(SampleVoice(it, BUS_FX, false, fadeOutSec = 0.3)).fadeTo(1f, 0.005f) }
        v = 0.0; latVel = 0.0; offset = 0.0; gear = 0; pedal = 0.0; brk = 0.0; sndLoad = 0.0
        gasLatch = false; brakeLatch = false
        if (auto) auto = false
        crashT = 3.0
        speak.say("Kaza yaptın, yoldan çıktın. Araç yola geri alındı.", true)
    }

    private fun surfaceNotes(dt: Double) {
        if (offroadSaid > 0) offroadSaid -= dt
        if (surface == 2 && lastSurface != 2 && offroadSaid <= 0) { speak.say("Yoldan çıkıyorsun, yavaşla!", true); offroadSaid = 4.0 }
        lastSurface = surface
    }

    private fun cabin() {
        val open = windows
        val onGravel = surface == 2
        tire.fadeTo((clamp(v / 30, 0.0, 1.2) * 0.32 * if (onGravel) 0.4 else 1.0).toFloat(), 0.08f); tire.filter?.freq = (120 + v * 7).toFloat()
        tire2.fadeTo((clamp(v / 35, 0.0, 1.2) * 0.09).toFloat(), 0.08f); tire2.filter?.freq = (400 + v * 14).toFloat()
        val amp = (v / 35).pow(2) * if (open) 0.42 else 0.07
        val lfo = if (open) 1 + 0.5 * sin(time * 2 * PI * 0.35) else 1.0
        wind.fadeTo((amp * lfo).toFloat(), 0.1f); wind.filter?.freq = ((if (open) 380 else 650) + v * 9).toFloat()
        wet.fadeTo((if (env.wet) clamp(v / 25, 0.0, 1.3) * 0.11 else 0.0).toFloat(), 0.2f)
        // Çakıl: çıtırtılı yüksek gürültü ve alçak uğultu
        val gv = if (onGravel) clamp(v / 15, 0.15, 1.2) else 0.0
        val crackle = 0.6 + 0.4 * rnd.nextDouble()
        gravel.fadeTo((gv * 0.32 * crackle).toFloat(), 0.03f); gravel.filter?.freq = (1600 + v * 40).toFloat()
        gravelLow.fadeTo((gv * 0.5).toFloat(), 0.1f)
        // Kenar şeridi titreşimi: kayılan tarafta, hızla sıklaşan tok vuruşlar
        if (surface == 1 && v > 3) {
            rumble.freq = (v / 0.35).toFloat().coerceIn(15f, 140f)
            rumble.pan = if (offset > 0) 0.8f else -0.8f
            rumble.fadeTo(0.22f, 0.05f)
        } else rumble.fadeTo(0f, 0.08f)
        val sc = bank.get("screech")
        if (sc != null) {
            if (squeal > 0.02 && screech == null) {
                val def = !bank.isCustom("screech")
                screech = mix.add(SampleVoice(sc, BUS_FX, true, 0.0, if (def) 0.3 else 0.0, loopStartSec = if (def) 0.3 else 0.0, loopEndSec = if (def) 2.6 else -1.0))
            }
            screech?.let {
                it.fadeTo((squeal * 0.55).toFloat(), 0.05f); it.rate = 0.9 + squeal * 0.2
                if (squeal <= 0.01) { it.stop(0.1f); screech = null }
            }
        }
    }

    private fun place(e: EmitterInst, at: Double) {
        val side = if (e.def.side != 0) e.def.side else if (rnd.nextBoolean()) 1 else -1
        e.s = at; e.lat = side * rand(e.def.latMin, e.def.latMax); e.y = if (e.def.high) rand(4.0, 12.0) else 1.0
        road.at(e.s, e.lat, q); e.sp.x = q.x; e.sp.z = q.z; e.sp.y = e.y
    }

    private fun oneShots() {
        env.oneshots.forEachIndexed { i, o ->
            if (time < nextShot[i]) return@forEachIndexed
            nextShot[i] = time + rand(o.everyMin, o.everyMax)
            val pcm = bank.get(o.key) ?: return@forEachIndexed
            if (o.stereo) {
                val dur = min(13.0, pcm.duration)
                mix.add(SampleVoice(pcm, BUS_WORLD, false, 0.0, rand(0.0, max(0.0, pcm.duration - dur)), dur, 0.6)).fadeTo(o.gain, 0.05f)
            } else {
                val dur = min(pcm.duration, rand(5.0, 9.0))
                val vc = SampleVoice(pcm, BUS_WORLD, false, 0.0, rand(0.0, max(0.0, pcm.duration - dur)), dur, 0.6)
                val sp = Spatial(12f, 1f)
                road.at(s + rand(20.0, 160.0), (if (rnd.nextBoolean()) 1 else -1) * rand(o.latMin, o.latMax), q)
                sp.x = q.x; sp.z = q.z; sp.y = rand(2.0, 10.0)
                vc.spatial = sp
                mix.add(vc).fadeTo(o.gain * 1.6f, 0.05f)
            }
        }
    }

    private fun spawnTraffic(siren: Boolean) {
        val W = env.width
        val oncomingLat = if (env.highway) -(W * 2 + 2) else -W / 2
        val fast = if (env.highway) 1.35 else 1.0
        val car = when {
            siren -> TrafficCar(s + 520, oncomingLat, -rand(16.0, 22.0), true, 0xFFFFFFFF.toInt(), 0)
            v > 12 && rnd.nextDouble() < (if (env.highway) 0.55 else 0.4) -> TrafficCar(s - 300, -W / 2, v + rand(7.0, 16.0), false, COLORS[rnd.nextInt(COLORS.size)], rnd.nextInt(3))
            else -> TrafficCar(s + 520, oncomingLat, -rand(14.0, 26.0) * fast, false, COLORS[rnd.nextInt(COLORS.size)], rnd.nextInt(3))
        }
        val mk = { pcm: Pcm, g: Float, rate: Double ->
            val vc = SampleVoice(pcm, BUS_WORLD, true, 0.0, rand(0.0, pcm.duration))
            vc.spatial = Spatial(5f, 1.25f); vc.rate = rate; car.voices.add(vc); car.rates.add(rate)
            mix.add(vc).fadeTo(g, 0.05f); vc
        }
        if (siren) bank.get("em_siren")?.let { mk(it, 1.4f, 1.0) }
        bank.get(Slots.trafficKeys[rnd.nextInt(3)])?.let { mk(it, 0.55f, rand(0.8, 1.15) * if (car.v > 0) 1.12 else 1.0) }
        val n = mk(Noises.pink, (0.35 + if (env.wet) 0.35 else 0.0).toFloat(), 1.0)
        n.filter = Biquad(Biquad.BANDPASS, 800f, 0.6f)
        car.noise = n
        car.honk = rnd.nextDouble() < 0.06
        traffic.add(car)
    }

    private fun updateTraffic(dt: Double) {
        if (!set.traffic) {
            if (traffic.isNotEmpty()) { traffic.forEach { c -> c.voices.forEach { it.stop(0.4f) } }; traffic.clear(); trafficView = emptyArray() }
            nextTraffic = time + rand(3.0, 6.0); nextSiren = time + rand(20.0, 50.0)
            return
        }
        if (time > nextTraffic) { if (traffic.size < 5) spawnTraffic(false); nextTraffic = time + rand(env.trafficMin, env.trafficMax) }
        if (env.siren && time > nextSiren) { spawnTraffic(true); nextSiren = time + rand(50.0, 120.0) }
        val lx = mix.lx; val lz = mix.lz
        var changed = false
        val it = traffic.iterator()
        while (it.hasNext()) {
            val c = it.next()
            c.s += c.v * dt
            road.at(c.s, c.lat, q)
            val d = hypot(q.x - lx, q.z - lz)
            if (c.prevD >= 0 && dt > 0) {
                val vr = (d - c.prevD) / dt
                val dop = clamp(343 / (343 + vr), 0.75, 1.3)
                c.dop += (dop - c.dop) * min(1.0, dt * 8)
            }
            c.prevD = d
            val air = clamp(17000 * exp(-d / 110) * (if (env.wet) 0.7 else 1.0), 500.0, 17000.0).toFloat()
            for (k in c.voices.indices) {
                val vc = c.voices[k]
                vc.spatial?.let { sp -> sp.x = q.x; sp.z = q.z; sp.y = 0.6; sp.air = false; sp.extraLp = air }
                vc.rate = c.rates[k] * c.dop
            }
            c.noise?.filter?.freq = ((500 + abs(c.v) * 18) * c.dop).toFloat()
            if (c.honk && d < 40) {
                c.honk = false
                bank.get("horn")?.let { h ->
                    val hv = SampleVoice(h, BUS_WORLD, false, 0.0, 0.3, 0.45, 0.1)
                    hv.spatial = Spatial(5f, 1.25f).also { sp -> sp.x = q.x; sp.z = q.z }
                    hv.rate = rand(0.9, 1.15); mix.add(hv).fadeTo(0.6f, 0.01f)
                }
            }
            if (c.s - s > 560 || c.s - s < -360) { c.voices.forEach { v -> v.stop(0.3f) }; it.remove(); changed = true }
        }
        if (changed || trafficView.size != traffic.size) trafficView = traffic.toTypedArray()
    }

    private fun paceNotes() {
        if (!set.pace) return
        val ahead = max(100.0, v * 6.5)
        val list = road.curves
        synchronized(list) {
            while (road.curveIdx < list.size && list[road.curveIdx].s1 < s) road.curveIdx++
            for (i in road.curveIdx until list.size) {
                val c = list[i]
                if (c.noted) continue
                if (c.s0 - s > ahead) break
                c.noted = true
                if (c.s0 < s) continue
                val r = 1 / abs(c.k)
                val dir = if (c.k > 0) "Sağa" else "Sola"
                val sev = if (r < 90) " keskin" else if (r > 300) " hafif" else ""
                val dist = max(10, ((c.s0 - s) / 10).roundToInt() * 10)
                val adv = advisedKmh(c.k)
                var msg = "$dir$sev viraj, $dist metre"
                if (adv < 150) msg += ", en fazla $adv"
                if (v * 3.6 > adv + 15) msg += ", yavaşla"
                chime(if (c.k > 0) 1f else -1f)
                speak.say(msg)
                break
            }
        }
    }

    private fun chime(dir: Float) {
        listOf(880f, 1175f).forEachIndexed { i, f ->
            val o = OscVoice(BUS_FX, OscVoice.SINE); o.freq = f; o.pan = dir * 0.85f
            mix.add(o)
            Thread { Thread.sleep(130L * i); o.fadeTo(0.13f, 0.015f); o.stopAfter(0.03f, 0.3f) }.start()
        }
    }

    companion object {
        val COLORS = intArrayOf(0xFFD8D8D8.toInt(), 0xFF2D3A4A.toInt(), 0xFF8A1C1C.toInt(), 0xFF1F5F3A.toInt(), 0xFFE0B040.toInt(), 0xFF222222.toInt())
    }
}
