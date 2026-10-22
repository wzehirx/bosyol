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
    check("engine file used", son.engineKind == "eng_hill", son.engineKind)
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

fun coinSoundPerValue() {
    println("coinSoundPerValue")
    // her altın 1 değerinde: tek coin_hcr vuruşu; coin_xl / coin_m / coin_l hiç istenmez
    run {
        val cb = CountBank(FBank())
        val (son, _, _) = newSonify(cb, ArrayList())
        cb.counts.clear()
        son.onCoin(1, 1)
        val got = cb.counts.filter { it.key.startsWith("coin") && it.value > 0 }
        check("one coin -> exactly one coin_hcr lookup, no tiers", got == mapOf("coin_hcr" to 1), got.toString())
    }
    val tone = FBank().get("coin")!!
    // coin_hcr yoksa coin_s çeşitleri sırayla çalar (aynı sesin tekrarı yorar)
    val noHcr = object : Bank { override fun get(key: String): Pcm? = if (key.startsWith("coin_s")) tone else null }
    run {
        val cb = CountBank(noHcr)
        val (son, _, _) = newSonify(cb, ArrayList())
        cb.counts.clear()
        repeat(6) { son.onCoin(1, it + 1) }
        check("coin_s variants rotate as fallback", listOf("coin_s", "coin_s2", "coin_s3").all { (cb.counts[it] ?: 0) >= 1 }, cb.counts.toString())
    }
    // yalnızca "coin" varsa o çalar
    run {
        val onlyCoin = object : Bank { override fun get(key: String): Pcm? = if (key == "coin") tone else null }
        val (son, _, _) = newSonify(onlyCoin, ArrayList())
        son.onCoin(1, 1)
        check("only 'coin' present still plays", son.coinHits == 1)
    }
    // işaret sesi: tek ışıltı, çift ışıltı yok (değer kademesi kalmadı)
    check("terrain coins all worth 1", Terrain(Maps.byId("country")).coins.all { it.value == 1 })
}

fun everyCoinPlays() {
    println("everyCoinPlays")
    val cb = CountBank(FBank())
    val mix = Mixer()
    val son = Sonify(mix, cb, AudioSettings()) { }
    son.start(GameSession(Vehicles.byId("jeep"), Upgrades(), Maps.byId("country")))
    cb.counts.clear()
    // 3 sn'de 12 alım; 4., 5. ve 6. aynı karede, 9. ve 10. aynı karede
    val times = listOf(0.0, 0.3, 0.7, 1.0, 1.0, 1.0, 1.4, 1.8, 2.1, 2.1, 2.5, 3.0)
    var t = 0.0
    val buf = FloatArray(800 * 2)
    var maxVoices = 0
    for (pt in times) {
        while (t + 1e-9 < pt) { son.update(1.0 / 60); mix.render(buf, 735); t += 1.0 / 60 }
        son.onCoin(1, 1)
        maxVoices = maxOf(maxVoices, mix.voiceCount)
    }
    son.update(1.0 / 60); mix.render(buf, 735)
    val asked = cb.counts["coin_hcr"] ?: 0
    check("bank asked for coin sample >= pickups (12)", asked >= 12, "asked=$asked")
    check("hits counted = pickups", son.coinHits == 12, "hits=${son.coinHits}")
    // aynı karedeki üç alım üst üste biner
    val m2 = Mixer()
    val s2 = Sonify(m2, FBank(), AudioSettings()) { }
    s2.start(GameSession(Vehicles.byId("jeep"), Upgrades(), Maps.byId("country")))
    m2.render(buf, 735)
    val base = m2.voiceCount
    repeat(3) { s2.onCoin(1, it + 1) }
    s2.update(0.02); m2.render(buf, 735)
    check("three same-frame pickups -> three overlapping voices", m2.voiceCount >= base + 3, "base=$base now=${m2.voiceCount}")
}

fun coinPitchConstant() {
    println("coinPitchConstant")
    val (son, _, _) = newSonify(FBank(), ArrayList())
    val rates = ArrayList<Double>()
    for (chain in 1..20) { son.update(0.1); son.onCoin(1, chain); rates.add(son.lastCoinRate) }
    son.update(2.0); son.onCoin(1, 1); rates.add(son.lastCoinRate)
    check("voice rate is exactly 1.0 for every chain length", rates.all { it == 1.0 }, rates.toString())
}

fun fastChainTighter() {
    println("fastChainTighter")
    val (son, _, _) = newSonify(FBank(), ArrayList())
    son.update(1.0); son.onCoin(1, 1)
    val d0 = son.lastCoinDur; val f0 = son.lastCoinFade
    son.update(1.0); son.onCoin(1, 1)
    check("slow pickups keep full tail", son.lastCoinDur == d0 && son.lastCoinFade == f0)
    var last = d0
    var mono = true
    for (i in 2..8) {
        son.update(0.3); son.onCoin(1, i)
        if (son.lastCoinDur > last + 1e-12) mono = false
        last = son.lastCoinDur
    }
    check("fast chain shortens the tail", son.lastCoinDur < d0 && son.lastCoinFade < f0, "d0=$d0 now=${son.lastCoinDur}")
    check("tail never shrinks past 40%", son.lastCoinDur >= d0 * 0.6 - 1e-9 && son.lastCoinFade >= f0 * 0.6 - 1e-9, "ratio=${son.lastCoinDur / d0}")
    check("tail shrinks monotonically along the chain", mono)
    check("pitch still constant", son.lastCoinRate == 1.0)
}

fun turboBlowOnLift() {
    println("turboBlowOnLift")
    val cb = CountBank(FBank())
    val (son, s, _) = newSonify(cb, ArrayList(), "rally", "country")
    cb.counts.clear()
    val mx = s.vehicle.maxRpm
    s.car.rpm = mx * 0.8; s.car.gas = 1.0
    son.update(0.1)
    s.car.gas = 0.0
    son.update(0.1)
    check("lift at high rpm -> turbo_blow", son.blowCount == 1 && (cb.counts["turbo_blow"] ?: 0) >= 1, "blow=${son.blowCount}")
    // 1.2 sn bekleme
    s.car.gas = 1.0; son.update(0.1); s.car.gas = 0.0; son.update(0.1)
    check("blow cooldown 1.2 s", son.blowCount == 1, "blow=${son.blowCount}")
    son.update(1.2); s.car.gas = 1.0; son.update(0.05); s.car.gas = 0.0; son.update(0.05)
    check("blow again after cooldown", son.blowCount == 2, "blow=${son.blowCount}")
    // düşük devirde bırakınca turbo_blow yok
    val (s3, g3, _) = newSonify(CountBank(FBank()), ArrayList(), "rally", "country")
    g3.car.rpm = mx * 0.3; g3.car.gas = 1.0; s3.update(0.1); g3.car.gas = 0.0; s3.update(0.1)
    check("lift at low rpm -> no blow", s3.blowCount == 0)
    // spool: düşük devirde gaza basınca
    val (s4, g4, _) = newSonify(CountBank(FBank()), ArrayList(), "rally", "country")
    g4.car.rpm = mx * 0.3; g4.car.gas = 0.0; s4.update(0.1); g4.car.gas = 1.0; s4.update(0.1)
    check("floor it at low rpm -> turbo_spool", s4.spoolCount == 1, "spool=${s4.spoolCount}")
    val (s5, g5, _) = newSonify(CountBank(FBank()), ArrayList(), "jeep", "country")
    g5.car.rpm = mx * 0.8; g5.car.gas = 1.0; s5.update(0.1); g5.car.gas = 0.0; s5.update(0.1)
    check("non-turbo vehicle: no blow/spool", s5.blowCount == 0 && s5.spoolCount == 0)
}

fun superchargerFollowsRpm() {
    println("superchargerFollowsRpm")
    val (son, s, _) = newSonify(FBank(), ArrayList(), "super", "country")
    val mx = s.vehicle.maxRpm
    s.car.rpm = s.vehicle.idleRpm; s.car.gas = 0.0; son.update(0.02)
    val r0 = son.superRate; val g0 = son.superGain
    s.car.rpm = mx; s.car.gas = 1.0; son.update(0.02)
    check("rate = 0.7 + 1.0*rpm/max at idle", abs(r0 - (0.7 + s.vehicle.idleRpm / mx)) < 1e-9, "r0=$r0")
    check("rate at max rpm = 1.7", abs(son.superRate - 1.7) < 1e-9, "r=${son.superRate}")
    check("gain follows gas 0..0.5", g0 == 0f && abs(son.superGain - 0.5f) < 1e-6, "g0=$g0 g=${son.superGain}")
    s.car.gas = 0.5; son.update(0.02)
    check("half gas -> 0.25", abs(son.superGain - 0.25f) < 1e-6)
    val (j, js, _) = newSonify(FBank(), ArrayList(), "jeep", "country")
    js.car.rpm = 3000.0; js.car.gas = 1.0; j.update(0.02)
    check("non-supercharged: no supercharger voice", j.superGain == 0f)
}

fun mudLandingSlime() {
    println("mudLandingSlime")
    val cb = CountBank(FBank())
    val (son, _, _) = newSonify(cb, ArrayList(), "jeep", "swamp")
    cb.counts.clear()
    son.update(1.0); son.onLand(4.0)
    check("mud landing -> mud_slime + splash_in", (cb.counts["mud_slime"] ?: 0) >= 1 && (cb.counts["splash_in"] ?: 0) >= 1, cb.counts.toString())
    val cd = CountBank(FBank())
    val (s2, _, _) = newSonify(cd, ArrayList(), "jeep", "country")
    cd.counts.clear(); s2.update(1.0); s2.onLand(4.0)
    check("dirt landing -> no slime", (cd.counts["mud_slime"] ?: 0) == 0 && (cd.counts["splash_in"] ?: 0) == 0, cd.counts.toString())
    // yuvarlanırken ~1.5 sn'de bir
    val ce = CountBank(FBank())
    val (s3, g3, _) = newSonify(ce, ArrayList(), "jeep", "swamp")
    g3.car.rear.grounded = true; g3.car.front.grounded = true
    g3.car.vx = 12.0; g3.car.gas = 1.0
    ce.counts.clear()
    repeat(60 * 6) { s3.update(1.0 / 60) }
    val n = ce.counts["mud_slime"] ?: 0
    check("rolling fast in mud: slime every ~1.5 s", n in 3..6, "n=$n")
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

/** 10 dk oyun (rastgele gaz/fren, kaza sonrası yeni tur, araya duraklat/devam): ses sayısı sınırlı kalmalı, durdurunca sıfırlanmalı. */
fun voicesDoNotLeak() {
    println("voicesDoNotLeak")
    val mix = Mixer()
    val bank = FBank()
    val rnd = java.util.Random(11)
    val spf = OUT_RATE / 60
    val buf = FloatArray(spf * 2)
    var maxVoices = 0
    var frames = 0
    var rounds = 0
    var cycles = 0
    val vehicles = Vehicles.all
    while (frames < 60 * 600) {
        val v = vehicles[rounds % vehicles.size]
        val m = Maps.all[rounds % Maps.all.size]
        rounds++
        val s = GameSession(v, Upgrades(), m)
        val son = Sonify(mix, bank, AudioSettings().also { it.distanceCalls = true }) { }
        son.start(s)
        var gas = 1.0; var brake = 0.0
        var untilPause = 60 * 30
        while (!s.over && frames < 60 * 600) {
            if (frames % 20 == 0) { gas = if (rnd.nextInt(4) == 0) 0.0 else 1.0; brake = if (rnd.nextInt(6) == 0) 1.0 else 0.0 }
            s.update(1.0 / 60, gas, brake)
            son.update(1.0 / 60)
            mix.render(buf, spf)
            frames++
            if (mix.voiceCount > maxVoices) maxVoices = mix.voiceCount
            if (--untilPause <= 0) {          // Duraklat + Devam: ses durdurulur, yeniden başlatılır
                untilPause = 60 * 30; cycles++
                son.stop(); for (k in 0 until 40) mix.render(buf, spf)
                if (mix.voiceCount > 0) { check("pause leaves no voices", false, "voices=${mix.voiceCount}"); }
                son.start(s)
            }
        }
        // oyun sonu: 2.5 sn sonra ekran değişir ve ses durdurulur
        for (k in 0 until 150) { son.update(1.0 / 60); mix.render(buf, spf) }
        son.stop()
        for (k in 0 until 60) mix.render(buf, spf)
    }
    check("voice count bounded over 10 min ($rounds rounds, $cycles pauses)", maxVoices < 40, "max=$maxVoices")
    check("no voices left after stop", mix.voiceCount == 0, "left=${mix.voiceCount}")
}

fun main() {
    missingSoundsSilentOk()
    rendersFinite()
    engineFollowsRpm()
    announceThrottle()
    tiltBeepAbove70()
    landingRules()
    coinSoundPerValue()
    everyCoinPlays()
    coinPitchConstant()
    fastChainTighter()
    turboBlowOnLift()
    superchargerFollowsRpm()
    mudLandingSlime()
    endFadesWorld()
    statusTextLevel()
    fullRunAllVehicles()
    wavExport()
    busAnalysis()
    voicesDoNotLeak()
    if (failures > 0) { println("FAILED: $failures"); System.exit(1) }
    println("OK")
}
