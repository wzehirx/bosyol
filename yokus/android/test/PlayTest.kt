import com.yokus.app.core.*
import java.io.File
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.max
import kotlin.math.min

/**
 * Uçtan uca oyun testi: BlindDriver (gözsüz oyuncu benzetimi) GameSession + Sonify ile baştan sona oynar.
 * Çıktı: konsol özeti + yokus/docs/OYUN_TESTI.md. Hata dedektörlerinden biri tetiklenirse süreç 1 ile çıkar.
 * Kullanım: bash yokus/android/test/run.sh PlayTest [hizli]   (hizli: yalnızca Kır yolu)
 */

const val PT_DT = 1.0 / 60
const val CAP_S = 1200.0            // 20 dk oyun zamanı
const val REPORT = "/home/user/bosyol/yokus/docs/OYUN_TESTI.md"

fun f1(v: Double) = String.format(Locale.US, "%.1f", v)
fun f0(v: Double) = String.format(Locale.US, "%.0f", v)
fun wrapD(d: Double): Double { var a = d % 360.0; if (a > 180) a -= 360.0 else if (a < -180) a += 360.0; return a }

class Spec(val map: String, val veh: String, val upg: Int, val novice: Boolean, val seed: Int, val group: String) {
    val label get() = "${Maps.byId(map).name}/${Vehicles.byId(veh).name}/y$upg/${if (novice) "acemi" else "iyi"}/s$seed"
}
class Ev(val kind: String, val x: Double, val slopeDeg: Double, val tiltDeg: Double, val fuelPct: Double, val t: Double)
class Hill(val x0: Int, val x1: Int, val rise: Double, val slopeDeg: Double, val burnPct: Double)
class Res(val spec: Spec) {
    var dist = 0.0; var maxX = 0.0; var end = "?"; var endT = 0.0
    val pick = ArrayList<Pair<Double, Double>>()      // (x, yakıt % alınırken)
    val events = ArrayList<Ev>()
    var peakTilt = 0.0; var peakRaw = 0
    var coins = 0; var flips = 0; var bonus = 0
    var announce = 0; var silence = 0.0
    var vcMax = 0
    val faults = ArrayList<String>()
    var burnPerM = 0.0; var fuelMax = 0.0
    var reversals = 0
    var endSlope = 0.0; var nextCan = -1.0
    val hills = ArrayList<Hill>()
    val said = ArrayList<Pair<Double, String>>()
    val gaps: List<Double> get() { val r = ArrayList<Double>(); var p = 0.0; for (x in pick) { r.add(x.first - p); p = x.first }; return r }
    val canCount get() = pick.size
    val rangeM get() = if (burnPerM > 0) fuelMax / burnPerM else 0.0
}

fun runOne(sp: Spec, bank: Bank): Res {
    val r = Res(sp)
    val vdef = Vehicles.byId(sp.veh)
    val s = GameSession(vdef, Upgrades(sp.upg, sp.upg, sp.upg, sp.upg), Maps.byId(sp.map))
    val mix = Mixer()
    val set = AudioSettings()
    var drv: BlindDriver? = null
    var tNow = 0.0
    val said = r.said
    val son = Sonify(mix, bank, set) { txt -> said.add(Pair(tNow, txt)); drv?.hear(tNow, txt) }
    son.start(s)
    drv = BlindDriver(s, sp.novice, sp.seed.toLong())
    val spf = OUT_RATE / 60
    val buf = FloatArray(spf * 2)
    val car = s.car
    val startX = car.x
    val ter = s.terrain
    r.fuelMax = s.fuelMax
    val burnAt = DoubleArray(ter.length.toInt() + 2) { Double.NaN }
    var maxIdx = 0
    var cumBurn = 0.0
    var prevFuel = s.fuel
    var backT = 0.0; var backFlag = false
    var stuckT = 0.0; var stuckArmed = true
    var flipArmed = true
    var nanFlag = false; var tiltFlag = false; var vcFlag = false
    var lastPeakRaw = 0
    fun ev(kind: String, rel: Double) {
        r.events.add(Ev(kind, car.x - startX, Math.toDegrees(atan(ter.slope(car.x))), wrapD(Math.toDegrees(car.angle)),
            s.fuelPct().toDouble(), s.time))
    }
    var extra = 0
    while (true) {
        if (s.over) { if (extra++ >= 90) break } else if (s.time >= CAP_S) break
        drv.tick(PT_DT)
        s.update(PT_DT, drv.gas, drv.brake)
        tNow = s.time
        son.update(PT_DT)
        mix.render(buf, spf)
        if (mix.voiceCount > r.vcMax) r.vcMax = mix.voiceCount
        if (mix.voiceCount > 60 && !vcFlag) { vcFlag = true; r.faults.add("voiceCount>60 (${mix.voiceCount}) t=${f1(s.time)}") }
        if (extra > 0) continue

        // --- dedektörler ---
        val vals = doubleArrayOf(car.x, car.y, car.angle, car.vx, car.vy, car.omega, s.fuel, s.distance, car.rpm, car.rear.x, car.rear.y, car.front.x, car.front.y)
        if (!nanFlag && vals.any { it.isNaN() || it.isInfinite() }) { nanFlag = true; r.faults.add("NaN/Inf t=${f1(s.time)} x=${f1(car.x)}") }
        val raw = s.tiltDeg()
        if (abs(raw) > abs(r.peakRaw)) r.peakRaw = raw
        if (!tiltFlag && abs(raw) > 180) { tiltFlag = true; r.faults.add("|tiltDeg|>180 (${raw}) t=${f1(s.time)} x=${f1(car.x - startX)}") }
        val angDeg = wrapD(Math.toDegrees(car.angle))
        if (abs(angDeg) > r.peakTilt) r.peakTilt = abs(angDeg)
        val gDeg = Math.toDegrees(atan(ter.slope(car.x)))
        val rel = wrapD(angDeg - gDeg)
        val air = car.airborne
        // sırtüstü
        if (!air && abs(rel) > 100.0) backT += PT_DT else backT = 0.0
        if (backT >= 3.0 && !s.over && !backFlag) { backFlag = true; r.faults.add("sırtüstü >3 sn, oyun bitmedi (göreli açı ${f0(rel)}°) t=${f1(s.time)} x=${f1(car.x - startX)}") }
        if (!air && abs(rel) > 100.0 && flipArmed) { flipArmed = false; ev("TERS", rel) }
        if (abs(rel) < 60.0) flipArmed = true
        // takılma
        val sp = abs(car.speed)
        if (!air && sp < 0.5 && s.fuel > 0.0 && !s.over && abs(rel) < 100.0) stuckT += PT_DT else stuckT = 0.0
        if (stuckT >= 3.0 && stuckArmed) { stuckArmed = false; ev("TAKILMA", rel) }
        if (sp > 2.0) stuckArmed = true
        // yakıt
        if (s.fuel > prevFuel + 1e-12) r.pick.add(Pair(car.x - startX, prevFuel / s.fuelMax * 100.0))
        else if (s.fuel < prevFuel) cumBurn += prevFuel - s.fuel
        prevFuel = s.fuel
        val idx = (car.x).toInt()
        while (maxIdx <= idx && maxIdx < burnAt.size) { burnAt[maxIdx++] = cumBurn }
    }
    // --- bitiş ---
    r.dist = s.distance; r.maxX = car.x
    r.coins = s.coins; r.flips = s.flips
    val pickSum = ter.coins.filter { it.taken }.sumOf { it.value }
    r.bonus = s.coins - pickSum
    r.reversals = drv.reversals
    r.endT = s.time
    r.endSlope = Math.toDegrees(atan(ter.slope(car.x)))
    r.nextCan = (ter.fuels.firstOrNull { it.x > car.x }?.x ?: -1.0).let { if (it < 0) -1.0 else it - car.x }
    val endTilt = wrapD(Math.toDegrees(car.angle))
    if (s.over) {
        r.end = s.endReason?.name ?: "?"
        ev("SON-" + r.end, 0.0)
        val relEnd = wrapD(endTilt - Math.toDegrees(atan(ter.slope(car.x))))
        if (s.endReason != EndReason.CRASH && !car.airborne && abs(relEnd) > 100.0 && backT > 0.5)
            r.faults.add("sırtüstü iken oyun ${r.end} ile bitti (CRASH değil)")
    } else {
        r.end = "SÜRE"
        val still = abs(car.speed) < 0.3
        if (still && s.fuel <= 0.0) r.faults.add("TAKILI/HANG: süre sınırında araç duruyor ve yakıt yok, oyun bitmedi")
        else if (still) r.faults.add("TAKILI: süre sınırında araç duruyor (yakıt var)")
    }
    // sessizlik
    r.announce = said.size
    var last = 0.0; var longest = 0.0
    for (p in said) { longest = max(longest, p.first - last); last = p.first }
    longest = max(longest, r.endT - last)
    r.silence = longest
    // yakıt kullanımı (L/m)
    if (maxIdx > 1) r.burnPerM = cumBurn / max(1.0, (maxIdx - 1).toDouble())
    // yokuşlar: 5 m adımlı, 6 m histerezisli tepe/çukur bulma
    run {
        val top = min(maxIdx - 1, ter.length.toInt() - 1)
        if (top > 50) {
            var i0 = 0; var lo = ter.height(0.0); var loX = 0
            var hi = lo; var hiX = 0; var climbing = false
            var x = 5
            while (x <= top) {
                val h = ter.height(x.toDouble())
                if (!climbing) {
                    if (h < lo) { lo = h; loX = x }
                    else if (h > lo + 6.0) { climbing = true; hi = h; hiX = x }
                } else {
                    if (h > hi) { hi = h; hiX = x }
                    else if (h < hi - 6.0) {
                        if (hi - lo >= 10.0 && !burnAt[loX].isNaN() && !burnAt[hiX].isNaN()) {
                            var ms = 0.0
                            for (k in loX until hiX) ms = max(ms, (ter.height((k + 5).toDouble()) - ter.height(k.toDouble())) / 5.0)
                            r.hills.add(Hill(loX, hiX, hi - lo, Math.toDegrees(atan(ms)), (burnAt[hiX] - burnAt[loX]) / s.fuelMax * 100.0))
                        }
                        climbing = false; lo = h; loX = x
                    }
                }
                x += 5
            }
            i0 = 0
        }
    }
    son.stop()
    return r
}

val TER_CACHE = java.util.concurrent.ConcurrentHashMap<String, Terrain>()
fun terOf(map: String): Terrain = TER_CACHE.getOrPut(map) { Terrain(Maps.byId(map)) }
/** x civarındaki (önceki 120 m ve sonraki 20 m) özellikler, duyuru metniyle. */
fun featNear(map: String, x: Double): String {
    val l = terOf(map).features.filter { it.x1 >= x - 120 && it.x0 <= x + 20 }
    return if (l.isEmpty()) "yok" else l.joinToString(", ") { "${it.kind.say.removePrefix("Önde ").lowercase()} (${f0(it.x0)}-${f0(it.x1)} m)" }
}

fun avg(l: List<Double>) = if (l.isEmpty()) 0.0 else l.sum() / l.size
fun median(l: List<Double>): Double { if (l.isEmpty()) return 0.0; val s = l.sorted(); val n = s.size; return if (n % 2 == 1) s[n / 2] else (s[n / 2 - 1] + s[n / 2]) / 2 }

/** Kutup ve Ay'ın asıl araçları (öbür haritalarda asıl araç jeep). */
val INTENDED_EXTRA = listOf("arctic" to "snowmobile", "moon" to "rover")
fun intendedFor(map: String): List<String> = listOf("jeep") + INTENDED_EXTRA.filter { it.first == map }.map { it.second }

class Gate(val name: String, val ok: Boolean, val value: String)

/** Ekonomi ölçüleri (raporda ve kapılarda aynı hesap). */
class Eco(val perRun: Double, val runMin: Double, val runsMoto: Double, val runsDesert: Double, val rate: Double, val shopHours: Double, val shop: Int)
fun economy(all: List<Res>): Eco {
    val cj = all.filter { it.spec.map == "country" && it.spec.veh == "jeep" && it.spec.upg == 0 && !it.spec.novice }
    val k = avg(cj.map { it.coins.toDouble() })
    val good = all.filter { (it.spec.group == "matris" || it.spec.group == "asil") && !it.spec.novice }
    val rate = good.sumOf { it.coins.toDouble() } / max(1.0, good.sumOf { it.endT }) * 60.0
    val shop = Vehicles.all.sumOf { it.price } + Maps.all.sumOf { it.price }
    return Eco(k, median(cj.map { it.endT / 60.0 }), if (k > 0) 8000 / k else 1e9, if (k > 0) 5000 / k else 1e9, rate,
        if (rate > 0) shop / rate / 60.0 else 1e9, shop)
}

/** PLAN2 F1-5 kabul kapıları. */
fun gateResults(all: List<Res>): List<Gate> {
    val out = ArrayList<Gate>()
    val nov = all.filter { it.spec.map == "country" && it.spec.veh == "jeep" && it.spec.upg == 0 && it.spec.novice }
    val nMean = avg(nov.map { it.dist })
    out.add(Gate("Acemi, jeep y0, Kır yolu: ortalama mesafe ≥ 1500 m", nov.isNotEmpty() && nMean >= 1500, f0(nMean) + " m (" + nov.joinToString(", ") { f0(it.dist) } + ")"))
    val n3 = nov.count { it.canCount >= 3 }
    out.add(Gate("Acemi: en az 4/5 koşu 3. bidona ulaşır", n3 >= 4, "$n3/${nov.size}"))
    val early = nov.filter { it.end == "CRASH" && it.dist < 1000 }
    out.add(Gate("Acemi: ilk 1000 m'de CRASH yok", early.isEmpty(), if (early.isEmpty()) "yok" else early.joinToString { "${it.spec.label} ${f0(it.dist)} m" }))
    val good = all.filter { it.spec.map == "country" && it.spec.veh == "jeep" && it.spec.upg == 0 && !it.spec.novice }
    val nat = good.filter { it.end != "SÜRE" && it.endT < 900.0 }
    out.add(Gate("İyi sürücü, jeep y0, Kır yolu: her koşu 15 dk'dan önce doğal biter", nat.size == good.size && good.isNotEmpty(),
        "${nat.size}/${good.size} (" + good.joinToString(", ") { "${it.end} ${f1(it.endT / 60)} dk" } + ")"))
    val gMed = median(good.map { it.dist })
    out.add(Gate("İyi sürücü: medyan mesafe 2500-8000 m", gMed in 2500.0..8000.0, f0(gMed) + " m (" + good.joinToString(", ") { f0(it.dist) } + ")"))
    for (m in Maps.all) {
        val l = all.filter { it.spec.map == m.id && it.spec.upg == 3 && !it.spec.novice && it.spec.veh in intendedFor(m.id) }
        val best = l.maxByOrNull { it.dist }
        out.add(Gate("${m.name}: asıl araç (${intendedFor(m.id).joinToString(" ya da ") { Vehicles.byId(it).name }}) y3 ≥ 1000 m",
            best != null && best.dist >= 1000, l.joinToString(", ") { "${Vehicles.byId(it.spec.veh).name} ${f0(it.dist)} m ${it.end}" }))
    }
    val moto = all.filter { it.spec.map == "country" && it.spec.veh == "moto" && it.spec.upg == 0 && !it.spec.novice }
    val mMed = median(moto.map { it.dist })
    out.add(Gate("Motosiklet y0, Kır yolu: medyan ≥ 1500 m", moto.isNotEmpty() && mMed >= 1500, f0(mMed) + " m (" + moto.joinToString(", ") { "${f0(it.dist)} ${it.end}" } + ")"))
    val e = economy(all)
    out.add(Gate("Ekonomi: motosiklet (8000) 4-6 iyi koşuda", e.runsMoto in 4.0..6.0, "${f1(e.runsMoto)} koşu (${f0(e.perRun)} altın/koşu, medyan koşu ${f1(e.runMin)} dk)"))
    out.add(Gate("Ekonomi: Çöl (5000) 2-3 koşuda", e.runsDesert in 2.0..3.0, "${f1(e.runsDesert)} koşu"))
    out.add(Gate("Ekonomi: tüm mağaza 15-25 saat", e.shopHours in 15.0..25.0, "${f1(e.shopHours)} saat (${e.shop} altın, ${f0(e.rate)} altın/dk)"))
    return out
}

fun main(args: Array<String>) {
    val quick = args.contains("hizli")
    val t0 = System.currentTimeMillis()
    val bank = FBank()
    // ses dosyalarını önceden çöz (önbellek iş parçacığı güvenli değil)
    File(FBank.DEFAULT_DIR).listFiles()?.map { it.nameWithoutExtension }?.distinct()?.forEach { bank.get(it) }

    var specs = ArrayList<Spec>()
    val maps = if (quick) listOf("country") else Maps.all.map { it.id }
    for (m in maps) for (v in listOf("jeep", "moto", "pickup", "tractor")) for (u in listOf(0, 3)) specs.add(Spec(m, v, u, false, 0, "matris"))
    // haritanın asıl aracı (kapı: her harita, asıl aracıyla 3. seviyede ≥ 1000 m)
    if (!quick) for ((m, v) in INTENDED_EXTRA) specs.add(Spec(m, v, 3, false, 0, "asil"))
    for (seed in 1..5) for (nov in listOf(false, true)) specs.add(Spec("country", "jeep", 0, nov, seed, "tohum"))
    for (seed in 1..5) specs.add(Spec("country", "moto", 0, false, seed, "tohum"))
    // ayar için: filtre=harita:araç:geliştirme (ör. filtre=country:moto:0) yalnızca eşleşen koşuları çalıştırır, rapor yazmaz
    val filt = args.firstOrNull { it.startsWith("filtre=") }?.removePrefix("filtre=")?.split(":")
    if (filt != null) {
        specs = ArrayList(specs.filter { sp -> (filt.getOrNull(0).isNullOrEmpty() || filt[0] == "*" || sp.map == filt[0]) &&
            (filt.getOrNull(1).isNullOrEmpty() || filt[1] == "*" || sp.veh == filt[1]) &&
            (filt.getOrNull(2).isNullOrEmpty() || filt[2] == "*" || sp.upg.toString() == filt[2]) })
    }
    println("PlayTest: ${specs.size} koşu, süre sınırı ${CAP_S / 60} dk oyun zamanı")

    val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    val futs = specs.map { sp -> pool.submit(Callable { runOne(sp, bank) }) }
    val results = ArrayList<Res>()
    var done = 0
    for (f in futs) {
        val r = f.get(); results.add(r); done++
        println(String.format(Locale.US, "[%2d/%d] %-34s %7.0f m  %-6s %6.1f s  bidon=%d  tepe|eğim|=%3.0f  takla=%d altın=%d%s",
            done, specs.size, r.spec.label, r.dist, r.end, r.endT, r.canCount, r.peakTilt, r.flips, r.coins,
            if (r.faults.isEmpty()) "" else "  !! ${r.faults.size} hata"))
    }
    pool.shutdown()

    val fails = results.filter { it.faults.isNotEmpty() }
    println()
    println("=== ÖZET ===")
    println("Koşu: ${results.size}, süre ${(System.currentTimeMillis() - t0) / 1000} sn gerçek zaman")
    val country0 = results.filter { it.spec.map == "country" && it.spec.veh == "jeep" && it.spec.upg == 0 && !it.spec.novice }
    for (r in country0) println("Kır yolu jeep (seed ${r.spec.seed}): ${f0(r.dist)} m, ${r.end}, ${f1(r.endT)} sn, bidon ${r.canCount}")
    val kinds = fails.flatMap { f -> f.faults.map { faultKind(it) to f } }.groupBy({ it.first }, { it.second })
    println("Hata dedektörleri:")
    if (kinds.isEmpty()) println("  hiçbiri tetiklenmedi")
    for ((k, v) in kinds) println("  $k: ${v.size} koşu, örnek: ${v.first().spec.label} -> ${v.first().faults.first()}")

    if (filt != null) {
        for (r in results) println("  ${r.spec.label}: " + r.events.takeLast(4).joinToString("; ") { "${it.kind} x=${f0(it.x)} eğim=${f0(it.slopeDeg)}° yakıt=${f0(it.fuelPct)}%" } +
            " | bidon: " + r.pick.takeLast(3).joinToString(", ") { "${f0(it.first)} (${f0(it.second)}%)" })
        for (r in results) {
            var last = 0.0; var gi = -1; var best = 0.0
            for ((i, p) in r.said.withIndex()) { if (p.first - last > best) { best = p.first - last; gi = i }; last = p.first }
            if (gi >= 0) println("  sessizlik ${f0(best)} sn: önce '${r.said.getOrNull(gi - 1)?.second}' sonra '${r.said[gi].second}' t=${f0(r.said[gi].first)}; bitiş t=${f0(r.endT)}, son duyuru t=${f0(r.said.last().first)}")
        }
        val ds = results.map { it.dist }.sorted()
        println("filtre: medyan ${f0(ds[ds.size / 2])} m, ortalama ${f0(avg(ds))} m, altın/koşu ${f0(avg(results.map { it.coins.toDouble() }))}, " +
            "altın/dk ${f0(results.sumOf { it.coins.toDouble() } / max(1.0, results.sumOf { it.endT }) * 60)}")
        return
    }
    val gates = if (quick) emptyList() else gateResults(results)
    writeReport(results, quick, (System.currentTimeMillis() - t0) / 1000, gates)
    println("Rapor: $REPORT")
    println("Kabul kapıları:")
    for (g in gates) println("  [${if (g.ok) "OK" else "FAIL"}] ${g.name}: ${g.value}")
    if (fails.isNotEmpty()) { println("FAIL: ${fails.size} koşuda hata dedektörü tetiklendi"); System.exit(1) }
    val bad = gates.filter { !it.ok }
    if (bad.isNotEmpty()) { println("FAIL: ${bad.size} kabul kapısı geçmedi"); System.exit(1) }
    println("OK")
}

// ------------------------------------------------------------------------------------------ rapor

fun mdTable(head: List<String>, rows: List<List<String>>): String {
    val sb = StringBuilder()
    sb.append("| ").append(head.joinToString(" | ")).append(" |\n")
    sb.append("|").append(head.joinToString("|") { "---" }).append("|\n")
    for (r in rows) sb.append("| ").append(r.joinToString(" | ")).append(" |\n")
    return sb.toString()
}

fun writeReport(all: List<Res>, quick: Boolean, secs: Long, gates: List<Gate> = emptyList()) {
    val sb = StringBuilder()
    val mat = all.filter { it.spec.group == "matris" }
    val seeds = all.filter { it.spec.group == "tohum" }
    val fails = all.filter { it.faults.isNotEmpty() }
    val vehNames = listOf("jeep", "moto", "pickup", "tractor")
    sb.append("# Yokuş Ustası: oyun testi raporu\n\n")
    sb.append("Bu rapor `bash yokus/android/test/run.sh PlayTest` tarafından otomatik üretilir (elle düzenlemeyin, yeniden çalıştırınca yazılır). ")
    sb.append("Sürücü `BlindDriver`: yalnızca sesle algılanabilen bilgilere bakar (eğim açısı, 10-15 m ilerideki radar eğimi, duyurular, havada olma, yakıt uyarıları, hız), ")
    sb.append("0.25 sn tepki gecikmesi ve küçük gürültüyle oynar. Strateji: tam gaz, burun zemin eğiminden 35° fazla kalkarsa gazı kes, havada burnu iniş eğimine yatır, takılırsa kısa geri git. ")
    sb.append("\"Acemi\" sürücü yalnızca takla bipi (>70°) çalınca gazı keser.\n\n")
    sb.append("- Koşu sayısı: ${all.size} (matris ${mat.size}, tohum ${seeds.size}); oyun zamanı sınırı 20 dk; gerçek süre ${secs} sn.\n")
    sb.append("- Hata dedektörü tetiklenen koşu: ${fails.size}\n\n")

    // --- kapılar
    if (gates.isNotEmpty()) {
        sb.append("## 0. Kabul kapıları (PLAN2 F1-5)\n\n")
        sb.append(mdTable(listOf("Kapı", "Sonuç", "Değer"), gates.map { listOf(it.name, if (it.ok) "GEÇTİ" else "**KALDI**", it.value) }))
        sb.append("\n")
    }
    // --- dedektörler
    sb.append("## 1. Hata dedektörleri\n\n")
    val groups = HashMap<String, MutableList<Res>>()
    for (f in fails) for (m in f.faults) groups.getOrPut(faultKind(m)) { ArrayList() }.add(f)
    val kindsAll = listOf("NaN", "tiltDeg>180", "sırtüstü", "TAKILI/HANG", "TAKILI", "voiceCount>60")
    sb.append(mdTable(listOf("Dedektör", "Tetiklenen koşu", "İlk örnek"), kindsAll.map { k ->
        val l = groups[k]
        listOf(k, (l?.size ?: 0).toString(), if (l == null) "-" else "${l.first().spec.label}: ${l.first().faults.first { faultKind(it) == k }}")
    }))
    sb.append("\n")

    // --- matris
    for (u in listOf(0, 3)) {
        sb.append("## ${if (u == 0) "2" else "3"}. Harita × araç, geliştirme ${if (u == 0) "yok" else "hepsi 3. seviye"} (iyi sürücü)\n\n")
        val rows = ArrayList<List<String>>()
        for (m in Maps.all) for (v in vehNames) {
            val r = mat.firstOrNull { it.spec.map == m.id && it.spec.veh == v && it.spec.upg == u } ?: continue
            val minFuel = r.pick.minOfOrNull { it.second }
            rows.add(listOf(m.name, Vehicles.byId(v).name, f0(r.dist), r.end, f0(r.endT), r.canCount.toString(),
                if (minFuel == null) "-" else f0(minFuel) + "%", f0(r.peakTilt) + "°", r.flips.toString(), r.coins.toString(), r.bonus.toString(),
                r.reversals.toString()))
        }
        sb.append(mdTable(listOf("Harita", "Araç", "Mesafe (m)", "Bitiş", "Süre (sn)", "Bidon", "En az yakıt", "Tepe açı", "Takla", "Altın", "Bonus", "Geri vites"), rows))
        sb.append("\n")
    }

    // --- Kır yolu jeep tohumlar
    sb.append("## 4. Kır yolu, jeep, geliştirme yok: tohumlar\n\n")
    val cj = all.filter { it.spec.map == "country" && it.spec.veh == "jeep" && it.spec.upg == 0 }.sortedWith(compareBy({ it.spec.novice }, { it.spec.seed }))
    sb.append(mdTable(listOf("Sürücü", "Tohum", "Mesafe (m)", "Bitiş", "Süre (sn)", "Bidon", "Bidon konumları (m, yakıt%)", "Tepe açı", "Altın", "Duyuru", "En uzun sessizlik (sn)"),
        cj.map { r -> listOf(if (r.spec.novice) "acemi" else "iyi", r.spec.seed.toString(), f0(r.dist), r.end, f0(r.endT), r.canCount.toString(),
            r.pick.joinToString(", ") { "${f0(it.first)} (${f0(it.second)}%)" }.ifEmpty { "-" }, f0(r.peakTilt) + "°", r.coins.toString(), r.announce.toString(), f0(r.silence)) }))
    val goodC = cj.filter { !it.spec.novice && it.spec.seed >= 1 }
    val reach3 = goodC.count { it.canCount >= 3 }
    sb.append("\nİyi sürücü, tohum 1-5: 3. yakıt bidonuna ulaşan = **$reach3/${goodC.size}** (PLAN2 F1 kuralı: en az 4/5).\n\n")
    val motoS = all.filter { it.spec.map == "country" && it.spec.veh == "moto" && it.spec.upg == 0 }.sortedBy { it.spec.seed }
    if (motoS.isNotEmpty()) sb.append("Motosiklet, Kır yolu, geliştirme yok (iyi sürücü, tohum 0-5): " + motoS.joinToString(", ") { "${f0(it.dist)} m ${it.end} (tepe ${f0(it.peakTilt)}°)" } + ".\n\n")
    val ter = Terrain(Maps.byId("country"))
    sb.append("Haritadaki bidonlar (Kır yolu): " + ter.fuels.take(8).joinToString(", ") { f0(it.x) } + " m; ardışık aralıklar: " +
        ter.fuels.take(8).let { l -> l.indices.map { i -> f0(l[i].x - (if (i == 0) 0.0 else l[i - 1].x)) } }.joinToString(", ") + " m.\n\n")

    // --- olaylar
    sb.append("## 5. Takılma, takla ve bitiş olayları\n\n")
    sb.append("TAKILMA = yerde, yakıt var, hız < 0.5 m/s, 3 sn. TERS = yerde göreli açı > 100°. SON-* = oyunu bitiren olay. Eğim = olay noktasındaki zemin eğimi, Açı = burun açısı (dünyaya göre).\n\n")
    val evRows = ArrayList<List<String>>()
    for (r in all.sortedBy { it.spec.label }) for (e in r.events) {
        if (e.kind.startsWith("SON-") && e.kind != "SON-CRASH" && e.kind != "SON-FUEL") continue
        evRows.add(listOf(r.spec.label, e.kind, f0(e.x), f0(e.slopeDeg) + "°", f0(e.tiltDeg) + "°", f0(e.fuelPct) + "%", f0(e.t)))
    }
    val shown = if (evRows.size > 160) evRows.take(160) else evRows
    sb.append(mdTable(listOf("Koşu", "Olay", "x (m)", "Eğim", "Açı", "Yakıt", "t (sn)"), shown))
    if (evRows.size > shown.size) sb.append("\n(${evRows.size} olaydan ilk ${shown.size} gösterildi.)\n")
    sb.append("\n")

    // --- araç özeti
    sb.append("## 5b. Araç özeti (matris, her iki geliştirme düzeyi, iyi sürücü)\n\n")
    sb.append(mdTable(listOf("Araç", "Ortalama mesafe (m)", "Ortalama süre (sn)", "CRASH", "FUEL", "SÜRE sınırı", "Koşu"), vehNames.map { v ->
        val l = mat.filter { it.spec.veh == v }
        listOf(Vehicles.byId(v).name, f0(avg(l.map { it.dist })), f0(avg(l.map { it.endT })), l.count { it.end == "CRASH" }.toString(),
            l.count { it.end == "FUEL" }.toString(), l.count { it.end == "SÜRE" }.toString(), l.size.toString())
    }))
    sb.append("\n")
    sb.append("### Yakıt bitişleri\n\n")
    sb.append("\"Bidona kalan\" = oyun bittiği yerden sonraki ilk yakıt bidonuna uzaklık. Eğim küçük ve bidon yakınsa neden büyük olasılıkla bidonun kaçırılması ya da erken yakıt tükenmesidir; eğim büyükse yokuşta takılmadır.\n\n")
    val fe = all.filter { it.end == "FUEL" && it.spec.group == "matris" }.sortedBy { it.spec.label }
    sb.append(mdTable(listOf("Koşu", "Mesafe (m)", "Eğim", "Bidona kalan (m)", "Son bidondan beri (m)", "Yakında olan özellikler"), fe.map { r ->
        listOf(r.spec.label, f0(r.dist), f0(r.endSlope) + "°", if (r.nextCan < 0) "-" else f0(r.nextCan), f0(r.dist - (r.pick.lastOrNull()?.first ?: 0.0)), featNear(r.spec.map, r.maxX))
    }))
    sb.append("\n")

    // --- yakıt
    sb.append("## 6. Yakıt: menzil ve bidon aralığı\n\n")
    sb.append("Menzil = depo / ölçülen yakıt harcaması (m başına). Bu değer tam gazla ve yokuşlarla bu sürücünün gerçek ortalamasıdır; bidon aralığı menzilin çok altında olmalı.\n\n")
    val rangeRows = ArrayList<List<String>>()
    for (m in Maps.all) {
        val j = mat.firstOrNull { it.spec.map == m.id && it.spec.veh == "jeep" && it.spec.upg == 0 } ?: continue
        val gaps = j.gaps
        rangeRows.add(listOf(m.name, f0(m.fuelEvery), f0(j.rangeM), if (j.rangeM > 0) f0(m.fuelEvery / j.rangeM * 100) + "%" else "-",
            if (gaps.isEmpty()) "-" else f0(gaps.maxOrNull()!!), if (j.pick.isEmpty()) "-" else f0(j.pick.minOf { it.second }) + "%"))
    }
    sb.append(mdTable(listOf("Harita (jeep, y0)", "Haritadaki bidon aralığı (m)", "Menzil (m)", "Aralık/menzil", "En uzun gerçek aralık (m)", "En düşük yakıt (alırken)"), rangeRows))
    sb.append("\n")

    // --- yokuşlar
    sb.append("## 7. En çok yakıt harcatan yokuşlar (jeep, geliştirme yok)\n\n")
    val hillRows = ArrayList<List<String>>()
    for (m in Maps.all) {
        val j = mat.firstOrNull { it.spec.map == m.id && it.spec.veh == "jeep" && it.spec.upg == 0 } ?: continue
        for (h in j.hills.sortedByDescending { it.burnPct }.take(3))
            hillRows.add(listOf(m.name, "${h.x0}-${h.x1}", f0(h.x1 - h.x0 + 0.0), f1(h.rise), f0(h.slopeDeg) + "°", f0(h.burnPct) + "%"))
    }
    sb.append(mdTable(listOf("Harita", "x aralığı (m)", "Uzunluk (m)", "Yükseklik (m)", "En dik eğim", "Depo payı"), hillRows))
    sb.append("\n")

    // --- ses
    sb.append("## 8. Duyurular ve karıştırıcı\n\n")
    val vcMax = all.maxOf { it.vcMax }
    sb.append("Mixer.voiceCount en yüksek: **$vcMax** (sınır 60). Koşu başına duyuru: ortalama ${f1(avg(all.map { it.announce.toDouble() }))}; ")
    val sil = all.maxByOrNull { it.silence }!!
    sb.append("en uzun sessizlik: ${f0(sil.silence)} sn (${sil.spec.label}).\n\n")
    val silRows = all.filter { it.endT > 60 }.sortedByDescending { it.silence }.take(6).map { listOf(it.spec.label, f0(it.silence), it.announce.toString(), f0(it.endT)) }
    sb.append(mdTable(listOf("Koşu", "En uzun sessizlik (sn)", "Duyuru sayısı", "Süre (sn)"), silRows))
    sb.append("\n")

    // --- ekonomi
    sb.append("## 9. Ekonomi\n\n")
    val eco = ArrayList<List<String>>()
    for (m in Maps.all) for (v in vehNames) for (u in listOf(0, 3)) {
        val l = all.filter { it.spec.map == m.id && it.spec.veh == v && it.spec.upg == u && !it.spec.novice }
        if (l.isEmpty()) continue
        val c = avg(l.map { it.coins.toDouble() })
        val vi = Vehicles.all.indexOfFirst { it.id == v }
        val nextV = Vehicles.all.getOrNull(vi + 1)
        val mi = Maps.all.indexOfFirst { it.id == m.id }
        val nextM = Maps.all.getOrNull(mi + 1)
        fun runs(p: Int) = if (c <= 0.0) "∞" else Math.ceil(p / c).toInt().toString()
        val perMin = c / max(1.0, avg(l.map { it.endT })) * 60.0
        fun mins(p: Int) = if (perMin <= 0.0) "∞" else f0(Math.ceil(p / perMin))
        eco.add(listOf(m.name, Vehicles.byId(v).name, "y$u", f0(c), f0(avg(l.map { it.endT })), f0(perMin),
            if (nextV == null) "-" else "${nextV.name} (${nextV.price}): ${runs(nextV.price)} koşu, ${mins(nextV.price)} dk",
            if (nextM == null) "-" else "${nextM.name} (${nextM.price}): ${runs(nextM.price)} koşu, ${mins(nextM.price)} dk"))
    }
    sb.append("Sürücü yakıt bitene ya da 20 dk dolana kadar oynadığı için koşu başı altın gerçek bir oyuncunun kısa koşularından çok daha yüksek çıkar; bu yüzden altın/dakika da verilir. ")
    sb.append("\"Koşu\" = tam bir koşu (uzun), \"dk\" = dakika cinsinden oyun süresi.\n\n")
    sb.append(mdTable(listOf("Harita", "Araç", "Geliştirme", "Koşu başı altın", "Süre (sn)", "Altın/dk", "Sıradaki araç", "Sıradaki harita"), eco))
    val jeepC = all.filter { it.spec.map == "country" && it.spec.veh == "jeep" && it.spec.upg == 0 && !it.spec.novice }
    val jc = avg(jeepC.map { it.coins.toDouble() })
    val jeepDef = Vehicles.byId("jeep")
    val upgAll = UpgradeKind.values().sumOf { k -> (0 until 3).sumOf { upgradeCost(jeepDef, k, it) } }
    sb.append("\nKır yolu + jeep ortalaması (${jeepC.size} koşu): ${f0(jc)} altın/koşu. Jeep için 4 geliştirmenin 3. seviyeye maliyeti: $upgAll altın = ${if (jc > 0) Math.ceil(upgAll / jc).toInt() else -1} koşu; ")
    sb.append("motosiklet (8000): ${if (jc > 0) Math.ceil(8000 / jc).toInt() else -1} koşu; Çöl haritası (5000): ${if (jc > 0) Math.ceil(5000 / jc).toInt() else -1} koşu.\n\n")

    run {
        val e = economy(all)
        sb.append("**İlerleme özeti (kapılarla aynı hesap):** Kır yolu + jeep (geliştirme yok) iyi koşu ortalaması ${f0(e.perRun)} altın, medyan koşu süresi ${f1(e.runMin)} dk. ")
        sb.append("Motosiklet (8000) = ${f1(e.runsMoto)} koşu, Çöl (5000) = ${f1(e.runsDesert)} koşu. Matristeki tüm iyi sürücü koşularının ortalama hızı ${f0(e.rate)} altın/dk; ")
        sb.append("tüm araçlar ve haritalar (${e.shop} altın) ≈ ${f1(e.shopHours)} saat. Hedef: motosiklet 4-6 koşu, Çöl 2-3 koşu, tüm mağaza 15-25 saat.\n\n")
    }
    val shopTotal = Vehicles.all.sumOf { it.price } + Maps.all.sumOf { it.price }
    val jMin = if (jeepC.isNotEmpty()) jc / max(1.0, avg(jeepC.map { it.endT })) * 60.0 else 0.0
    sb.append("Tüm araçlar ve haritalar toplam $shopTotal altın. Kır yolu + jeep hızıyla (${f0(jMin)} altın/dk) hepsini almak ≈ ${if (jMin > 0) f0(shopTotal / jMin / 60.0) else "?"} saat oyun sürer. ")
    sb.append("Motosiklet gibi devrilgen araçlarla altın/dk 3-5 kat düşer (bkz. tablo).\n\n")

    // --- sorunlar
    sb.append("## 10. Sorunlar (ciddiyet sırasıyla)\n\n")
    val issues = buildIssues(all, mat, seeds, goodC, reach3, jc, upgAll)
    for ((i, it) in issues.withIndex()) sb.append("${i + 1}. **[${it.first}]** ${it.second}\n")
    if (issues.isEmpty()) sb.append("Sorun bulunmadı.\n")
    sb.append("\n## 11. Ayar önerileri\n\n")
    val tips = buildTips(all, mat, goodC, jc)
    for (t in tips) sb.append("- $t\n")
    if (tips.isEmpty()) sb.append("- Öneri yok.\n")
    sb.append("\n## 12. Karşılaştırma: düzeltmeden önceki çekirdek (commit d417faf)\n\n")
    sb.append("Bu bölüm sabit metindir (aynı test, eski çekirdekle bir kez çalıştırıldı). Güncel sonuçlar yukarıdadır.\n\n")
    sb.append("- 90 koşunun 25'inde hata dedektörü tetiklendi; test FAIL ile çıktı.\n")
    sb.append("- `sırtüstü` (araç yerde göreli açı > 100° iken 3 sn'den uzun süre oyun bitmedi): 43 olay, 25 koşuda. Örnek: Kır yolu/Motosiklet/y0, x=1944 m, göreli açı 109°. Bu, kullanıcının bildirdiği \"burun 124°'ye kalktı, yakıt bitti\" durumunun birebir aynısıdır: Kır yolu motosiklet 1889 m'de, tepe açı 125°, oyun CRASH ile değil \"yakıt bitti\" ile bitti.\n")
    sb.append("- `tiltDeg>180`: 1 koşu (Ay/Motosiklet/y3, -181°; ham tepe değeri 494°). `tiltDeg()` sarılmıyordu.\n")
    sb.append("- `TAKILI`: Ay/Traktör/y3, 20 dk sonunda araç duruyor, yakıt var, oyun bitmiyor (kaçırılan bir bitiş koşulu).\n")
    sb.append("- NaN ve voiceCount dedektörleri hiç tetiklenmedi.\n")
    sb.append("- Kır yolu + jeep (iyi sürücü) eski çekirdekte de 5/5 tohumda ~16.2 km gidip 20 dk sınırına ulaştı: ilk haritadaki yokuş, dikkatli sürücü için sorun değil. Sorun, gazı kesmeyen/yalnızca bipe tepki veren oyuncuda (acemi sürücü: 1546 m'de ters dönüp CRASH).\n")
    File(REPORT).writeText(sb.toString())
}

fun faultKind(m: String): String = when {
    m.startsWith("NaN") -> "NaN"
    m.startsWith("|tiltDeg|") -> "tiltDeg>180"
    m.startsWith("sırtüstü") -> "sırtüstü"
    m.startsWith("TAKILI/HANG") -> "TAKILI/HANG"
    m.startsWith("TAKILI") -> "TAKILI"
    m.startsWith("voiceCount") -> "voiceCount>60"
    else -> m
}

fun buildIssues(all: List<Res>, mat: List<Res>, seeds: List<Res>, goodC: List<Res>, reach3: Int, jc: Double, upgAll: Int): List<Pair<String, String>> {
    val out = ArrayList<Pair<String, String>>()
    // 1. dedektörler
    val byKind = HashMap<String, MutableList<Res>>()
    for (f in all) for (m in f.faults) byKind.getOrPut(faultKind(m)) { ArrayList() }.add(f)
    for ((k, l) in byKind) {
        val sev = if (k == "TAKILI") "Yüksek" else "Kritik"
        val ex = l.first()
        out.add(Pair(sev, "Dedektör `$k` ${l.size} koşuda tetiklendi. Örnek: ${ex.spec.label} -> ${ex.faults.first { faultKind(it) == k }}."))
    }
    // 2. Kır yolu 3. bidon kuralı
    if (goodC.isNotEmpty() && reach3 < 4) {
        out.add(Pair("Kritik", "Kır yolu + jeep (y0) iyi sürücüyle 5 tohumun yalnızca $reach3 tanesinde 3. bidona ulaşıyor (gereken 4/5). Mesafeler: " +
            goodC.joinToString(", ") { "${f0(it.dist)} m (${it.end}, ${it.canCount} bidon)" } + "."))
    }
    // 3. kazaların ve yakıt bitmesinin erken olduğu koşular
    val early = all.filter { it.end != "SÜRE" && it.dist < 1500 && it.spec.group == "matris" }
    if (early.isNotEmpty()) out.add(Pair("Yüksek", "İlk 1500 m içinde biten ${early.size} koşu: " + early.sortedBy { it.dist }.take(14).joinToString("; ") { "${it.spec.label} ${f0(it.dist)} m ${it.end} (eğim ${f0(it.endSlope)}°)" } + "."))
    for (v in listOf("jeep", "moto", "pickup", "tractor")) {
        val l = mat.filter { it.spec.veh == v }
        val cr = l.count { it.end == "CRASH" }
        if (l.isNotEmpty() && cr * 2 > l.size) out.add(Pair("Yüksek", "${Vehicles.byId(v).name}: ${l.size} koşunun $cr tanesi CRASH ile bitiyor, ortalama mesafe ${f0(avg(l.map { it.dist }))} m. Araç aşırı devrilgen (denge/atalet/havada tork ayarı gerekir)."))
        val fu = l.count { it.end == "FUEL" }
        if (l.isNotEmpty() && fu * 2 > l.size) out.add(Pair("Yüksek", "${Vehicles.byId(v).name}: ${l.size} koşunun $fu tanesi yakıt bitişiyle sonlanıyor, ortalama mesafe ${f0(avg(l.map { it.dist }))} m; çoğunda araç yokuşta ya da kumda çok yavaşlıyor, yakıt bidona yetmiyor."))
    }
    val fuelEnd = all.filter { it.end == "FUEL" }
    if (fuelEnd.isNotEmpty()) {
        val byMap = fuelEnd.groupBy { it.spec.map }
        out.add(Pair("Yüksek", "Yakıt bitişiyle sonlanan koşu: ${fuelEnd.size}/${all.size}. Haritalara göre: " + byMap.entries.joinToString(", ") { "${Maps.byId(it.key).name} ${it.value.size}" } + ". Bunların ${fuelEnd.count { it.nextCan in 0.0..150.0 }} tanesi bir sonraki bidona 150 m'den az kala bitiyor (menzil marjı çok dar), ${fuelEnd.count { r -> r.events.any { it.kind == "TAKILMA" } }} tanesinde bitişten önce yamaçta takılma olayı var (yani yakıt, yamaçta çıkamayıp beklerken yanmış). \"Yakıt bitti\" duyurusu oyuncuya nedeni söylemez."))
    }
    // 4. takılma kümeleri
    val stuck = all.flatMap { r -> r.events.filter { it.kind == "TAKILMA" }.map { r to it } }
    if (stuck.isNotEmpty()) {
        val cl = stuck.groupBy { Pair(it.first.spec.map, (it.second.x / 50).toInt()) }.entries.sortedByDescending { it.value.size }.take(6)
        out.add(Pair("Yüksek", "Takılma ${stuck.size} kez görüldü. En sık noktalar: " + cl.joinToString("; ") { e ->
            val ev = e.value.first().second
            "${Maps.byId(e.key.first).name} x≈${e.key.second * 50} m (eğim ${f0(ev.slopeDeg)}°, ${e.value.size} kez)" } + "."))
    }
    val flipEv = all.flatMap { r -> r.events.filter { it.kind == "TERS" }.map { r to it } }
    if (flipEv.isNotEmpty()) {
        val cl = flipEv.groupBy { Pair(it.first.spec.map, (it.second.x / 100).toInt()) }.entries.sortedByDescending { it.value.size }.take(5)
        out.add(Pair("Yüksek", "Yerde göreli açı 100°'yi geçen (ters dönme) olay ${flipEv.size}. En sık: " + cl.joinToString("; ") { e ->
            val ev = e.value.first().second
            "${Maps.byId(e.key.first).name} x≈${e.key.second * 100} m (eğim ${f0(ev.slopeDeg)}°, açı ${f0(ev.tiltDeg)}°, ${e.value.size} kez)" } + "."))
    }
    // 5. bidon yakıt marjı
    val lowPick = all.filter { it.pick.any { p -> p.second < 5.0 } }
    if (lowPick.isNotEmpty()) out.add(Pair("Orta", "Bidona %5'ten az yakıtla ulaşılan ${lowPick.size} koşu var (marj çok dar): " + lowPick.take(5).joinToString("; ") { it.spec.label } + "."))
    // 6. dik yokuş yakıt payı (Kır yolu)
    val cj0 = mat.firstOrNull { it.spec.map == "country" && it.spec.veh == "jeep" && it.spec.upg == 0 }
    val big = cj0?.hills?.filter { it.burnPct > 60.0 }
    if (!big.isNullOrEmpty()) out.add(Pair("Yüksek", "Kır yolunda tek yokuş deponun %60'ından fazlasını harcıyor: " + big.joinToString("; ") { "x ${it.x0}-${it.x1} m (${f1(it.rise)} m yükseklik, %${f0(it.burnPct)})" } + "."))
    // 7. süre sınırı
    val capped = all.filter { it.end == "SÜRE" }
    if (capped.isNotEmpty()) out.add(Pair("Düşük", "20 dk sınırına dayanan ${capped.size} koşu (oyun doğal bitmedi; sürücü sorunsuz ilerliyor, harita uzunluğu 20 km): " + capped.take(6).joinToString("; ") { "${it.spec.label} ${f0(it.dist)} m" } + "."))
    // 8. acemi - iyi farkı
    val gn = avg(seeds.filter { it.spec.novice }.map { it.dist }); val gi = avg(seeds.filter { !it.spec.novice && it.spec.veh == "jeep" }.map { it.dist })
    if (seeds.isNotEmpty() && gn < gi * 0.5) out.add(Pair("Orta", "Acemi sürücü (yalnızca takla bipiyle) Kır yolunda ortalama ${f0(gn)} m gidiyor, iyi sürücü ${f0(gi)} m. Denge yardımı ve eğim duyuruları olmadan ilk oyun çok zor."))
    // 9. ekonomi
    if (jc > 0 && 8000 / jc > 40) out.add(Pair("Orta", "Ekonomi yavaş: Kır yolunda jeepin ortalama ${f0(jc)} altını var; motosiklet (8000) ${Math.ceil(8000 / jc).toInt()} koşu, Çöl (5000) ${Math.ceil(5000 / jc).toInt()} koşu sürüyor."))
    run {
        val jl = mat.filter { it.spec.map == "country" && it.spec.veh == "jeep" && it.spec.upg == 0 }
        if (jl.isNotEmpty()) {
            val pm = jl.sumOf { it.coins.toDouble() } / jl.sumOf { it.endT } * 60.0
            val tot = Vehicles.all.sumOf { it.price } + Maps.all.sumOf { it.price }
            val hrs = tot / pm / 60.0
            if (hrs < 6.0) out.add(Pair("Orta", "Ekonomi çok hızlı: Kır yolunda jeepin altını ${f0(pm)} altın/dk. İlk araç (motosiklet 8000) ${f0(Math.ceil(8000 / pm))} dk, Çöl haritası ${f0(Math.ceil(5000 / pm))} dk, tüm mağaza ($tot altın) ≈ ${f1(hrs)} saat. Altın değerleri düşürülür ya da fiyatlar artırılırsa ilerleme uzar."))
        }
    }
    if (jc > 0 && upgAll / jc > 25) out.add(Pair("Düşük", "Jeep için 4 geliştirmeyi 3. seviyeye çıkarmak ($upgAll altın) ${Math.ceil(upgAll / jc).toInt()} koşu."))
    // 10. sessizlik
    val longSil = all.filter { it.silence > 30.0 && it.endT > 60 }
    if (longSil.isNotEmpty()) out.add(Pair("Düşük", "30 sn'den uzun duyurusuz süre olan ${longSil.size} koşu (en uzunu ${f0(longSil.maxOf { it.silence })} sn). Mesafe duyurusu (250 m) varsayılan açık; bu süreler o duyurular dahil ölçülür."))
    val vc = all.maxOf { it.vcMax }
    if (vc > 40) out.add(Pair("Düşük", "Mixer ses sayısı en çok $vc (60 sınırının altında ama yüksek)."))
    val order = listOf("Kritik", "Yüksek", "Orta", "Düşük")
    return out.sortedBy { order.indexOf(it.first) }
}

fun buildTips(all: List<Res>, mat: List<Res>, goodC: List<Res>, jc: Double): List<String> {
    val out = ArrayList<String>()
    // menzil / bidon aralığı
    for (m in Maps.all) {
        val j = mat.firstOrNull { it.spec.map == m.id && it.spec.veh == "jeep" && it.spec.upg == 0 } ?: continue
        if (j.rangeM <= 0) continue
        val ratio = m.fuelEvery / j.rangeM
        if (ratio > 0.7) out.add("${m.name}: jeep deposu ortalama ${f0(j.rangeM)} m menzil veriyor, bidon aralığı ${f0(m.fuelEvery)} m (menzilin %${f0(ratio * 100)}'i). `fuelEvery` değerini en çok ${f0(j.rangeM * 0.6)} m'ye indirin ya da jeep `fuelUse` değerini %${f0(min(40.0, (ratio / 0.6 - 1) * 100))} azaltın.")
    }
    // yokuşlar
    for (m in Maps.all) {
        val j = mat.firstOrNull { it.spec.map == m.id && it.spec.veh == "jeep" && it.spec.upg == 0 } ?: continue
        val worst = j.hills.maxByOrNull { it.burnPct } ?: continue
        if (worst.burnPct > 45) out.add("${m.name}: x ${worst.x0}-${worst.x1} m yokuşu (${f1(worst.rise)} m yükseklik, en dik ${f0(worst.slopeDeg)}°) deponun %${f0(worst.burnPct)}'ini yakıyor. Bu aralıkta `Terrain` genliğini/dikliğini yumuşatın ya da yokuşun hemen öncesine bidon koyun.")
    }
    // takılma ve ters dönme noktaları
    val stuck = all.flatMap { r -> r.events.filter { it.kind == "TAKILMA" || it.kind == "TERS" }.map { r to it } }
    val cl = stuck.groupBy { Pair(it.first.spec.map, (it.second.x / 50).toInt()) }.entries.sortedByDescending { it.value.size }.take(6)
    for (e in cl) {
        val ev = e.value.first().second
        val mapName = Maps.byId(e.key.first).name
        val vehs = e.value.map { Vehicles.byId(it.first.spec.veh).name }.distinct().joinToString("/")
        if (abs(ev.slopeDeg) >= 12.0)
            out.add("$mapName x≈${e.key.second * 50} m: ${e.value.size} takılma/ters olayı ($vehs), zemin eğimi ${f0(ev.slopeDeg)}°. Bu yamacın en dik kısmını ${f0(max(10.0, abs(ev.slopeDeg) - 8))}° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.")
        else
        {
            val tt = terOf(e.key.first); val xx = e.key.second * 50
            val prev = (xx - 60..xx).maxOf { Math.toDegrees(atan(tt.slope(it.toDouble()))) }
            out.add("$mapName x≈$xx m: ${e.value.size} takılma/ters olayı ($vehs); olay noktasında zemin eğimi düşük (${f0(ev.slopeDeg)}°) ama önceki 60 m'de en dik yamaç ${f0(prev)}°: araç yamaçta burnunu çok kaldırıp düzlükte arkaya devriliyor (gaz-burun yukarı). Yakındaki özellikler: ${featNear(e.key.first, xx.toDouble())}. Denge yardımının eşiğini (40°) ve gaz kısma eğrisini bu parçada sınayın; yamaç sonu eğimini yumuşatın.")
        }
    }
    // yakıt bitişi kümeleri: önce takılma olduysa neden yokuştur, olmadıysa bidon aralığı
    val fe = all.filter { it.end == "FUEL" && it.spec.group == "matris" }.groupBy { it.spec.map }
    for ((m, l) in fe) {
        if (l.size < 3) continue
        val withStuck = l.filter { r -> r.events.any { it.kind == "TAKILMA" } }
        val noStuck = l.filter { r -> r.events.none { it.kind == "TAKILMA" } }
        val md = Maps.byId(m)
        if (withStuck.size >= 2) {
            val sl = withStuck.flatMap { r -> r.events.filter { it.kind == "TAKILMA" }.map { it.slopeDeg } }
            out.add("${md.name}: ${l.size} koşu yakıtla bitiyor, ${withStuck.size} tanesinde önce yamaçta takılma var (takılma eğimleri ${f0(sl.minOrNull()!!)}-${f0(sl.maxOrNull()!!)}°, mesafeler ${withStuck.map { f0(it.dist) }.sorted().joinToString(", ")} m). Asıl sorun yakıt değil tırmanış: yüzeyin tutunması (${md.surface.label}, μ=${md.surface.mu}) bu eğim için yetersiz; ${f0(sl.minOrNull()!!)}°'nin üstündeki yamaçları azaltın, ya da bu yüzeyin μ değerini/araç torkunu artırın. Takılınca yakıt boşa yanıyor: \"yokuşu çıkamıyorsun\" ipucu ve geri vites daha erken devreye girmeli.")
        }
        if (noStuck.size >= 2) {
            out.add("${md.name}: ${noStuck.size} koşu takılmadan yakıtla bitiyor (mesafeler ${noStuck.map { f0(it.dist) }.sorted().joinToString(", ")} m); bidon aralığı `fuelEvery`=${f0(md.fuelEvery)} m menzile göre geniş. `fuelEvery` değerini ${f0(md.fuelEvery * 0.7)} m'ye indirmeyi deneyin.")
        }
    }
    if (goodC.isNotEmpty() && goodC.count { it.canCount >= 3 } < 4)
        out.add("Kır yolu üçüncü bidon kuralı için: ilk 3 bidonun x konumları ${goodC.first().pick.take(3).joinToString(", ") { f0(it.first) }} m civarında; ilk bidona kadar yakıt ve takılma kayıplarını azaltmak için ilk 600 m'yi düzlemek ya da jeep `fuelUse` değerini düşürmek yeterli olur.")
    val fl = all.filter { it.peakRaw != 0 && abs(it.peakRaw) > 180 }
    if (fl.isNotEmpty()) out.add("`tiltDeg()` değerini [-180, 180] aralığına sarın (en kötü ham değer ${fl.maxOf { abs(it.peakRaw) }}°).")
    if (jc > 0 && 8000 / jc > 40) out.add("Kır yolunda altın verimi düşük: koşu başına ${f0(jc)}. Altın değerlerini artırın ya da araç/harita fiyatlarını düşürün (hedef: motosiklet 15-25 koşuda).")
    return out
}
