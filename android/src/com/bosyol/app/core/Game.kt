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
        // Gaz kesilince motor sesi kaybolmasın: genel düzey yüke az bağlı
        val vol = 0.84 + 0.16 * load
        for (i in 0 until n) {
            val rate = clamp(rpm / layers[i].rpm, minRate, 2.3)
            on[i].rate = rate
            val offV = off[i]
            if (offV != null) {
                offV.rate = rate
                on[i].fadeTo((level * w[i] * vol * (0.2 + 0.8 * load)).toFloat(), 0.05f)
                offV.fadeTo((level * w[i] * vol * (1 - load) * 0.95).toFloat(), 0.05f)
            } else {
                val g = if (layers[i].idle) 0.88 else vol * (0.62 + 0.38 * load)
                on[i].fadeTo((level * w[i] * g).toFloat(), 0.05f)
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

/** Bir sürüş oturumu: fizik, ses ve yardımcı pilot. Tüm zaman oyun saatine göre işler. */
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
    @Volatile var auto = false
    @Volatile var cruise = 90
    @Volatile var windows = true
    @Volatile var squeal = 0.0
    private var thr = 0.0
    private var shiftT = 0.0
    private var cut = 0.0
    private var ai = 0.2
    private var lastBand = 0
    private var calloutT = 0.0
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
    val gasOn get() = gasHold || gasLatch || keyGas || gasPulse > 0
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
    private var screech: SampleVoice? = null
    private var horn: SampleVoice? = null
    private val rnd = java.util.Random()
    private val p = RoadPoint()
    private val q = RoadPoint()
    val lane get() = env.width / 2

    private fun rand(a: Double, b: Double) = a + rnd.nextDouble() * (b - a)

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
        mix.master = (pow(set.volMaster / 100.0, 1.6) * 1.1).toFloat()
        mix.busGain[BUS_ENGINE] = (pow(set.volEngine / 100.0, 1.5) * 1.1).toFloat()
        mix.busGain[BUS_WORLD] = (pow(set.volWorld / 100.0, 1.5) * 1.2 * (if (windows) 1.0 else 0.55)).toFloat()
        mix.busGain[BUS_CABIN] = pow(set.volWorld / 100.0, 1.2).toFloat()
        mix.busGain[BUS_FX] = 1f
    }
    private fun pow(a: Double, b: Double) = a.pow(b)

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
        auto = on; ai = clamp(thr * 0.6, 0.1, 0.6)
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
        return "$t. ${env.name}."
    }

    /* ---------- ana döngü ---------- */
    fun update(dt: Double) {
        time += dt
        if (gasPulse > 0) gasPulse = max(0.0, gasPulse - dt)
        if (brakePulse > 0) brakePulse = max(0.0, brakePulse - dt)
        if (startDelay > 0) { startDelay -= dt; if (startDelay <= 0) engine?.start() }
        val load = physics(dt)
        road.at(s, lane, p)
        mix.lx = p.x; mix.lz = p.z; mix.lh = p.h
        engine?.update(rpm, load, v, brk)
        cabin()
        updateTraffic(dt)
        for (e in emitters) if (e.s < s - 140) place(e, s + rand(300.0, 500.0))
        oneShots()
        paceNotes()
        if (set.callouts) {
            val band = (v * 3.6 / 20).roundToInt()
            if (band != lastBand) { calloutT += dt; if (calloutT > 1.2) { lastBand = band; calloutT = 0.0; if (band > 0) speak.say("${band * 20}") } } else calloutT = 0.0
        }
    }

    private fun torqueAt(rpm: Double): Double {
        val V = vehicle
        if (V.synth) return if (rpm < V.peak) V.torque else V.torque * V.peak / rpm
        val x = rpm / V.peak
        return V.torque * if (x < 1) 0.62 + 0.38 * sin(x * PI / 2) else 1 - 0.38 * ((x - 1) / (V.redline / V.peak - 1)).pow(2)
    }

    private fun physics(dt: Double): Double {
        val V = vehicle
        var thrT: Double
        var autoBrk = 0.0
        if (auto) {
            val vT = min(cruise / 3.6, road.safeSpeed(s, v))
            val err = vT - v
            ai = clamp(ai + err * dt * 0.05, 0.0, 0.7)
            thrT = clamp(err * 0.3 + ai, 0.0, 1.0)
            if (err < -1.5) { autoBrk = clamp(-err * 0.08, 0.0, 0.5); thrT = 0.0 }
        } else thrT = if (gasOn) 1.0 else 0.0
        thr = approach(thr, thrT, dt * if (thrT > thr) 2.6 else 2.2)

        // Fren: pedal basılı tutuldukça basınç kademeli artar, sabitlenince orta düzeyde kalır
        brk = when {
            auto -> approach(brk, autoBrk, dt * 1.2)
            brakePressed -> min(1.0, brk + dt * if (brk < 0.18) 0.9 else 0.24)
            brakeLatch -> approach(brk, 0.42, dt * 0.6)
            else -> approach(brk, 0.0, dt * 2.5)
        }

        val ratio = V.gears[gear] * V.final
        val wheelRpm = v / V.r * 60 / (2 * PI)
        val rpmRaw = wheelRpm * ratio
        var rpmT = rpmRaw
        var load = thr
        if (shiftT > 0) { shiftT -= dt; load *= 0.05 }
        if (cut > 0) { cut -= dt; load = 0.0 }
        val slipping = !V.synth && gear == 0 && rpmRaw < V.launch
        if (slipping) rpmT = max(rpmRaw, V.idle + thr * (V.launch - V.idle) * 1.15)
        if (!V.synth) rpmT = max(rpmT, V.idle)
        rpm += (rpmT - rpm) * min(1.0, dt * if (shiftT > 0) 7 else 12)
        if (rpm > V.redline) cut = 0.08

        var f = torqueAt(rpm) * load * ratio * 0.9 / V.r
        if (load < 0.05 && rpmRaw > V.idle * 1.1) f -= V.mass * V.engBrake * (rpmRaw / V.redline) * sqrt(V.gears[gear] / V.gears[0])
        f -= 0.5 * 1.2 * V.cd * V.area * v * v + (if (v > 0.2) 0.013 * V.mass * 9.81 else 0.0)
        val grip = if (env.wet) 0.68 else 1.0
        // Düşük hızda fren kuvveti yumuşar, araç sarsılmadan durur
        val stopSoft = clamp(v / 1.5, 0.35, 1.0)
        if (v > 0.01) f -= brk * V.brake * grip * stopSoft * V.mass
        val k = abs(road.curvature(s))
        val gripV = if (k > 1e-4) sqrt(7 * grip / k) else 1e9
        squeal = clamp((v - gripV * 0.82) / (gripV * 0.25), 0.0, 1.0)
        if (squeal > 0) f -= V.mass * 3 * squeal
        v = max(0.0, v + f / V.mass * dt)
        if (v < 0.12 && load < 0.05) v = max(0.0, v - dt * 0.6)

        if (!V.synth && shiftT <= 0) {
            val up = lerp(V.shiftLight, V.shiftFull, clamp(thr * 1.15, 0.0, 1.0))
            if (gear < V.gears.size - 1 && rpm > up && !slipping) { gear++; shiftT = 0.32; if (set.shiftSay) speak.say("${gear + 1}. vites") }
            else if (gear > 0 && rpmRaw < V.shiftDown * (if (brk > 0.1) 1.25 else 1.0)) { gear--; shiftT = 0.22; if (set.shiftSay) speak.say("${gear + 1}. vites") }
        }
        s += v * dt
        return load
    }

    private fun cabin() {
        val open = windows
        tire.fadeTo((clamp(v / 30, 0.0, 1.2) * 0.32).toFloat(), 0.08f); tire.filter?.freq = (120 + v * 7).toFloat()
        tire2.fadeTo((clamp(v / 35, 0.0, 1.2) * 0.09).toFloat(), 0.08f); tire2.filter?.freq = (400 + v * 14).toFloat()
        val amp = (v / 35).pow(2) * if (open) 0.42 else 0.07
        val lfo = if (open) 1 + 0.5 * sin(time * 2 * PI * 0.35) else 1.0
        wind.fadeTo((amp * lfo).toFloat(), 0.1f); wind.filter?.freq = ((if (open) 380 else 650) + v * 9).toFloat()
        wet.fadeTo((if (env.wet) clamp(v / 25, 0.0, 1.3) * 0.11 else 0.0).toFloat(), 0.2f)
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
        val ahead = max(90.0, v * 6)
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
                val safe = (sqrt(3.2 * r) * 3.6 / 10).roundToInt() * 10
                var msg = "$dir$sev viraj, $dist metre"
                if (v * 3.6 > safe + 10) msg += ", $safe ile gir"
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
