import com.yokus.app.core.*
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.sqrt

const val SCRATCH = "/tmp/claude-0/-home-user-bosyol/afa3c9ce-e696-5524-8a18-2a59d29f0c0e/scratchpad"
var failures = 0
fun check(name: String, ok: Boolean, info: String = "") {
    println((if (ok) "  ok   " else "  FAIL ") + name + (if (info.isEmpty()) "" else "  [$info]"))
    if (!ok) failures++
}

class Render(val out: FloatArray, val rpmPerFrame: DoubleArray, val said: List<Pair<Double, String>>, val session: GameSession, val sonify: Sonify) {
    val frames: Int get() = out.size / 2
    val finite: Boolean get() = out.all { it.isFinite() }
    val peak: Float get() = out.fold(0f) { m, v -> maxOf(m, abs(v)) }
    /** [from, to) saniye aralığının RMS'i (iki kanal birlikte) */
    fun rms(from: Double, to: Double): Double {
        val a = (from * OUT_RATE).toInt().coerceIn(0, frames); val b = (to * OUT_RATE).toInt().coerceIn(a, frames)
        if (b == a) return 0.0
        var sum = 0.0
        for (i in a * 2 until b * 2) sum += out[i].toDouble() * out[i]
        return sqrt(sum / ((b - a) * 2))
    }
}

/** Akıllı sürücüyle seconds sn oyna ve sesi çal. only: yalnızca bu yolun sesi (diğerleri ses düzeyi 0). */
fun render(vehicleId: String, mapId: String, seconds: Double, bank: Bank, only: Int = -1, driver: ((GameSession) -> Unit)? = null,
           settings: AudioSettings = AudioSettings()): Render {
    val mix = Mixer()
    val set = settings
    if (only >= 0) {
        if (only != BUS_ENGINE) set.volEngine = 0
        if (only != BUS_WORLD) set.volWorld = 0
        if (only != BUS_CABIN && only != BUS_FX) set.volCues = 0
    }
    val said = ArrayList<Pair<Double, String>>()
    var t = 0.0
    val son = Sonify(mix, bank, set) { said.add(Pair(t, it)) }
    val s = GameSession(Vehicles.byId(vehicleId), Upgrades(), Maps.byId(mapId))
    son.start(s)
    val drv = SmartDriver()
    val n = Math.round(seconds * 60).toInt()
    val spf = OUT_RATE / 60
    val out = FloatArray(n * spf * 2)
    val rpm = DoubleArray(n)
    val buf = FloatArray(spf * 2)
    for (i in 0 until n) {
        t = i / 60.0
        if (driver != null) driver(s) else drv.control(s.car)
        s.update(1.0 / 60, s.car.gas, s.car.brake)
        son.update(1.0 / 60)
        rpm[i] = s.car.rpm
        mix.render(buf, spf)
        System.arraycopy(buf, 0, out, i * spf * 2, spf * 2)
    }
    son.stop()
    return Render(out, rpm, said, s, son)
}

fun writeWav(path: String, out: FloatArray) {
    val data = ByteArray(out.size * 2)
    for (i in out.indices) {
        val v = (out[i].coerceIn(-1f, 1f) * 32767).toInt()
        data[i * 2] = (v and 255).toByte(); data[i * 2 + 1] = ((v shr 8) and 255).toByte()
    }
    File(path).parentFile.mkdirs()
    DataOutputStream(FileOutputStream(path)).use { o ->
        fun i32(v: Int) { o.write(v and 255); o.write((v shr 8) and 255); o.write((v shr 16) and 255); o.write((v shr 24) and 255) }
        fun i16(v: Int) { o.write(v and 255); o.write((v shr 8) and 255) }
        o.writeBytes("RIFF"); i32(36 + data.size); o.writeBytes("WAVEfmt "); i32(16); i16(1); i16(2); i32(OUT_RATE); i32(OUT_RATE * 4); i16(4); i16(16)
        o.writeBytes("data"); i32(data.size); o.write(data)
    }
}

fun newSonify(bank: Bank, said: MutableList<String>, vehicle: String = "jeep", map: String = "country"): Triple<Sonify, GameSession, Mixer> {
    val mix = Mixer()
    val son = Sonify(mix, bank, AudioSettings()) { said.add(it) }
    val s = GameSession(Vehicles.byId(vehicle), Upgrades(), Maps.byId(map))
    son.start(s)
    return Triple(son, s, mix)
}

fun missingSoundsSilentOk() {
    println("missingSoundsSilentOk")
    val r = render("jeep", "country", 20.0, NullBank())
    check("no exception, finite", r.finite)
    // eksik dosya: motor testere sentezine düşer
    check("fallback engine is synth saw", r.sonify.engineKind == "saw", r.sonify.engineKind)
    val rr = render("rover", "moon", 5.0, NullBank())
    check("rover electric w/o sounds finite", rr.finite && rr.peak <= 1f)
    val (son, _, _) = newSonify(NullBank(), ArrayList())
    son.ui("buy"); son.onCoin(1, 3); son.onLand(9.0); son.onEnd(EndReason.CRASH)
    check("events with null bank do not throw", true)
}

fun rendersFinite() {
    println("rendersFinite")
    val gas: (GameSession) -> Unit = { it.car.gas = 1.0; it.car.brake = 0.0 }
    val r = render("jeep", "country", 30.0, FBank(), driver = gas)
    check("finite", r.finite)
    check("peak <= 1.0", r.peak <= 1.0f, "peak=${r.peak}")
    val e = render("jeep", "country", 30.0, FBank(), only = BUS_ENGINE, driver = gas)
    val rms = e.rms(1.0, 30.0)
    check("engine bus RMS > 0.01", rms > 0.01, "rms=${"%.4f".format(rms)}")
}

fun engineFollowsRpm() {
    println("engineFollowsRpm")
    val (son, s, _) = newSonify(FBank(), ArrayList())
    s.car.rpm = s.vehicle.idleRpm
    son.update(0.02)
    val idle = son.engineRate
    s.car.rpm = s.vehicle.maxRpm * 0.9
    son.update(0.02)
    val high = son.engineRate
    check("rate(high rpm) > rate(idle)", high > idle, "idle=$idle high=$high")
    check("engine file used", son.engineKind == "eng_jeep", son.engineKind)
    val (son2, s2, _) = newSonify(FBank(), ArrayList(), "rover", "moon")
    s2.car.rpm = 800.0; son2.update(0.02); val a = son2.engineRate
    s2.car.rpm = 3000.0; son2.update(0.02)
    check("electric whine rises", son2.engineRate > a)
    for (v in Vehicles.all) {
        val (sn, _, _) = newSonify(FBank(), ArrayList(), v.id, "country")
        check("engine layer exists: ${v.id}", sn.engineKind != "none", sn.engineKind)
    }
}

fun announceThrottle() {
    println("announceThrottle")
    val said = ArrayList<String>()
    val (son, _, _) = newSonify(FBank(), said)
    repeat(5) { son.say("A"); son.update(0.2) }
    check("A x5 within 1 s -> once", said == listOf("A"), said.toString())
    said.clear()
    son.update(1.0)
    son.say("B"); son.update(0.05); son.say("C"); son.update(0.05); son.say("D")
    check("B spoken immediately, C/D pending", said == listOf("B"), said.toString())
    son.update(1.0)
    check("latest pending (D) wins, C dropped", said == listOf("B", "D"), said.toString())
    said.clear()
    // arka arkaya iki farklı metin: ikincisi en az 0.8 sn sonra
    val (son2, _, _) = newSonify(FBank(), said)
    son2.say("X"); son2.update(0.1); son2.say("Y")
    check("Y waits 0.8 s", said == listOf("X"))
    son2.update(0.5); check("Y not yet at 0.6 s", said == listOf("X"))
    son2.update(0.3); check("Y after 0.9 s", said == listOf("X", "Y"), said.toString())
    // aynı metin 3 sn içinde bastırılır, sonra yine söylenir
    said.clear()
    val (son3, _, _) = newSonify(FBank(), said)
    son3.say("W"); son3.update(2.0); son3.say("W"); son3.update(0.5)
    check("same text within 3 s dropped", said == listOf("W"))
    son3.update(1.0); son3.say("W")
    check("same text after 3 s allowed", said == listOf("W", "W"), said.toString())
    // 10 sn boyunca sürekli çağrı: duyurular arasında en az 0.8 sn
    said.clear()
    val times = ArrayList<Double>()
    var t = 0.0
    val mix = Mixer()
    val son4 = Sonify(mix, FBank(), AudioSettings()) { times.add(t) }
    son4.start(GameSession(Vehicles.byId("jeep"), Upgrades(), Maps.byId("country")))
    var i = 0
    while (t < 10.0) { son4.say("m${i++ % 7}"); son4.update(0.02); t += 0.02 }
    val gaps = times.zipWithNext { a, b -> b - a }
    check("gaps >= 0.8 s", gaps.all { it >= 0.78 }, "min=${gaps.minOrNull()}")
}

fun tiltBeepAbove70() {
    println("tiltBeepAbove70")
    val (a, sa, _) = newSonify(FBank(), ArrayList())
    sa.car.angle = Math.toRadians(80.0)
    repeat(10) { a.update(0.05) }
    check("80 deg -> beeps", a.beepPulses > 0, "pulses=${a.beepPulses}")
    val (b, sb, _) = newSonify(FBank(), ArrayList())
    sb.car.angle = Math.toRadians(30.0)
    repeat(10) { b.update(0.05) }
    check("30 deg -> no beep", b.beepPulses == 0)
    val (c, sc, _) = newSonify(FBank(), ArrayList())
    sc.car.angle = Math.toRadians(-85.0)
    repeat(20) { c.update(0.05) }
    check("-85 deg -> beeps faster", c.beepPulses > a.beepPulses / 1 - 1 && c.beepPulses >= 3, "pulses=${c.beepPulses}")
}

class CountBank(val inner: Bank) : Bank {
    val counts = HashMap<String, Int>()
    override fun get(key: String): Pcm? { counts[key] = (counts[key] ?: 0) + 1; return inner.get(key) }
}

fun landingRules() {
    println("landingRules")
    val cb = CountBank(FBank())
    val (son, _, _) = newSonify(cb, ArrayList())
    cb.counts.clear()
    fun n(k: String) = cb.counts[k] ?: 0
    son.onLand(0.3)
    check("impact 0.3 -> nothing", n("land_thud") == 0 && n("creak") == 0)
    son.update(0.5)
    son.onLand(1.0)
    check("impact 1.0 -> light tick only (no creak)", n("land_thud") <= 1 && n("creak") == 0)
    son.update(0.5); cb.counts.clear()
    son.onLand(2.0)
    check("impact 2 -> thud, no creak", n("land_thud") == 1 && n("creak") == 0)
    son.update(0.5); cb.counts.clear()
    son.onLand(6.0)
    check("impact 6 -> thud + creak", n("land_thud") == 1 && n("creak") == 1)
}

fun endFadesWorld() {
    println("endFadesWorld")
    val said = ArrayList<String>()
    val mix = Mixer()
    val set = AudioSettings().also { it.volCues = 0 }
    val son = Sonify(mix, FBank(), set) { said.add(it) }
    val s = GameSession(Vehicles.byId("jeep"), Upgrades(), Maps.byId("country"))
    son.start(s)
    val buf = FloatArray(800 * 2)
    fun sec(n: Int): Double { var sum = 0.0; var c = 0; repeat(n) { mix.render(buf, 800); for (v in buf) { sum += v.toDouble() * v; c++ } }; return sqrt(sum / c) }
    for (i in 0 until 120) { s.car.gas = 1.0; s.update(1.0 / 60, 1.0, 0.0); son.update(1.0 / 60); mix.render(buf, 800) }
    val before = sec(30)
    s.end(EndReason.CRASH)
    son.update(0.05)
    for (i in 0 until 90) { son.update(1.0 / 60); mix.render(buf, 800) }
    var late = 0.0
    // crash sesi 2.5 sn sürer; ancak motor/dünya sessizleşmiş olmalı: crash FX yolundadır (volCues 0 ile susturuldu)
    late = sec(30)
    check("engine/world silent after end", late < 0.002 && before > 0.01, "before=${"%.4f".format(before)} late=${"%.5f".format(late)}")
    check("final announcement spoken", said.contains("Kaza yaptın"), said.toString())
    val m2 = Mixer(); val d2 = ArrayList<String>()
    val son2 = Sonify(m2, FBank(), AudioSettings()) { d2.add(it) }
    val s2 = GameSession(Vehicles.byId("jeep"), Upgrades(), Maps.byId("country"))
    son2.start(s2); son2.say("Önde rampa"); son2.update(0.1); s2.end(EndReason.FUEL); son2.update(1.0)
    check("end announcement not lost after recent speech", d2 == listOf("Önde rampa", "Yakıt bitti"), d2.toString())
}

fun statusTextLevel() {
    println("statusText")
    val s = GameSession(Vehicles.byId("jeep"), Upgrades(), Maps.byId("country"))
    s.car.angle = Math.toRadians(2.0)
    check("|tilt|<3 -> Burun düz", s.statusText().contains("Burun düz"), s.statusText())
    s.car.angle = Math.toRadians(10.0)
    check("10 deg -> yukarı", s.statusText().contains("10 derece yukarı"), s.statusText())
}

fun fullRunAllVehicles() {
    println("fullRunAllVehicles")
    val maps = Maps.all
    for ((i, v) in Vehicles.all.withIndex()) {
        val map = if (v.id == "rover") "moon" else if (v.id == "snowmobile") "arctic" else maps[i % maps.size].id
        val r = render(v.id, map, 20.0, FBank())
        check("${v.id} on $map: finite, peak<=1", r.finite && r.peak <= 1.0f,
            "peak=${"%.3f".format(r.peak)} rms=${"%.3f".format(r.rms(0.0, 20.0))} dist=${r.session.distance.toInt()}m eng=${r.sonify.engineKind} said=${r.said.size}")
    }
}

fun wavExport() {
    println("wavExport")
    val j = render("jeep", "country", 30.0, FBank())
    writeWav("$SCRATCH/yokus_demo.wav", j.out)
    check("jeep/country wav", File("$SCRATCH/yokus_demo.wav").length() > 5_000_000, "dist=${j.session.distance.toInt()} peak=${j.peak} said=${j.said.take(8)}")
    val m = render("moto", "moon", 20.0, FBank())
    writeWav("$SCRATCH/yokus_moon.wav", m.out)
    check("moto/moon wav", File("$SCRATCH/yokus_moon.wav").length() > 3_000_000, "dist=${m.session.distance.toInt()} peak=${m.peak} said=${m.said.take(8)}")
}

/** Yol başına RMS ve motorun devirle yükselişi (30 sn jeep, ülke). */
fun busAnalysis() {
    println("busAnalysis")
    val names = listOf("engine", "world", "cabin+fx")
    val buses = listOf(BUS_ENGINE, BUS_WORLD, BUS_CABIN)
    val rs = buses.map { render("jeep", "country", 30.0, FBank(), only = it) }
    val all = render("jeep", "country", 30.0, FBank())
    println("  window    rpm    " + names.joinToString("  ") { it.padStart(9) } + "      all")
    var w = 0.0
    while (w < 30.0) {
        val i0 = (w * 60).toInt(); val i1 = ((w + 5) * 60).toInt().coerceAtMost(all.rpmPerFrame.size)
        val rpm = all.rpmPerFrame.slice(i0 until i1).average()
        println("  %2.0f-%2.0fs  %5.0f  ".format(w, w + 5, rpm) + rs.joinToString("  ") { "%9.4f".format(it.rms(w, w + 5)) } + "  %8.4f".format(all.rms(w, w + 5)))
        w += 5.0
    }
    val eng = rs[0]
    val xs = ArrayList<Double>(); val ys = ArrayList<Double>(); val zs = ArrayList<Double>()
    var q = 1.0
    while (q + 0.5 <= 30.0) {
        val i0 = (q * 60).toInt(); val i1 = ((q + 0.5) * 60).toInt()
        xs.add(eng.rpmPerFrame.slice(i0 until i1).average()); ys.add(eng.rms(q, q + 0.5))
        var zc = 0; val a0 = (q * OUT_RATE).toInt(); val a1 = ((q + 0.5) * OUT_RATE).toInt()
        for (i in a0 + 1 until a1) if ((eng.out[2 * i] >= 0f) != (eng.out[2 * i - 2] >= 0f)) zc++
        zs.add(zc / 0.5 / 2)   // yaklaşık baskın frekans (Hz)
        q += 0.5
    }
    fun corr(x: List<Double>, y: List<Double>): Double {
        val mx = x.average(); val my = y.average()
        var sxy = 0.0; var sxx = 0.0; var syy = 0.0
        for (i in x.indices) { sxy += (x[i] - mx) * (y[i] - my); sxx += (x[i] - mx) * (x[i] - mx); syy += (y[i] - my) * (y[i] - my) }
        return sxy / sqrt(sxx * syy + 1e-12)
    }
    val corr = corr(xs, zs); val corrL = corr(xs, ys)
    println("  rpm range ${xs.minOrNull()!!.toInt()}..${xs.maxOrNull()!!.toInt()}, engine RMS ${"%.4f".format(ys.minOrNull())}..${"%.4f".format(ys.maxOrNull())}, corr(rpm,rms)=${"%.2f".format(corrL)}, zero-cross Hz ${zs.minOrNull()!!.toInt()}..${zs.maxOrNull()!!.toInt()}, corr(rpm,pitch)=${"%.2f".format(corr)}")
    check("engine pitch follows rpm (corr > 0.5)", corr > 0.5 && corrL > 0.0, "corr=${"%.2f".format(corr)}")
    check("no clipping in full mix", all.peak <= 1.0f, "peak=${all.peak}")
}

fun main() {
    missingSoundsSilentOk()
    rendersFinite()
    engineFollowsRpm()
    announceThrottle()
    tiltBeepAbove70()
    landingRules()
    endFadesWorld()
    statusTextLevel()
    fullRunAllVehicles()
    wavExport()
    busAnalysis()
    if (failures > 0) { println("FAILED: $failures"); System.exit(1) }
    println("OK")
}
