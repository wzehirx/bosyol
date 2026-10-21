import com.yokus.app.core.*
import kotlin.math.PI

fun fail(msg: String): Nothing = throw AssertionError(msg)
fun req(c: Boolean, msg: () -> String) { if (!c) fail(msg()) }

val jeep = Vehicles.byId("jeep")
val country = Maps.byId("country")
/** Gerçek haritalarla aynı yapıda, amp 0 (düz) test haritası. */
val flat = country.copy(id = "flat", amp = 0.0)

class Rec : GameEvents {
    val coinsGot = ArrayList<Pair<Int, Int>>()
    val low = ArrayList<Int>()
    var fuelCount = 0
    val flips = ArrayList<Pair<Int, Int>>()
    val air = ArrayList<Pair<Double, Int>>()
    val feats = ArrayList<Feature>()
    val dists = ArrayList<Int>()
    var ends = ArrayList<EndReason>()
    override fun onCoin(value: Int, streak: Int) { coinsGot.add(Pair(value, streak)) }
    override fun onFuel() { fuelCount++ }
    override fun onFuelLow(pct: Int) { low.add(pct) }
    override fun onFlip(count: Int, bonus: Int) { flips.add(Pair(count, bonus)) }
    override fun onAirBonus(sec: Double, bonus: Int) { air.add(Pair(sec, bonus)) }
    override fun onFeature(f: Feature) { feats.add(f) }
    override fun onDistance(m: Int) { dists.add(m) }
    override fun onEnd(reason: EndReason) { ends.add(reason) }
}

const val DT = 1.0 / 60

fun coinsCollected() {
    val ev = Rec()
    val s = GameSession(jeep, Upgrades(), country, ev)
    for (i in 0 until 40 * 60) { s.update(DT, 1.0, 0.0); if (s.over) break }
    req(s.coins > 0) { "coins=${s.coins}" }
    req(ev.coinsGot.isNotEmpty()) { "onCoin çağrılmadı" }
    req(ev.coinsGot.sumOf { it.first } == s.coins) { "coin toplamı tutmuyor" }
}

fun fuelRuns() {
    val ev = Rec()
    val s = GameSession(jeep.copy(fuelL = 0.5), Upgrades(), flat, ev)
    for (i in 0 until 120 * 60) { s.update(DT, 1.0, 0.0); if (s.over) break }
    req(ev.low == listOf(25, 10)) { "low=${ev.low}" }
    req(ev.ends == listOf(EndReason.FUEL)) { "ends=${ev.ends} t=${s.time}" }
    req(s.endReason == EndReason.FUEL && s.over) { "endReason=${s.endReason}" }
}

fun noFuelStopEnds() {
    val ev = Rec()
    val s = GameSession(jeep, Upgrades(), flat, ev)
    s.fuel = 0.0
    var t = 0.0
    while (t < 4.0 && !s.over) { s.update(DT, 1.0, 0.0); t += DT }
    req(ev.ends == listOf(EndReason.FUEL)) { "ends=${ev.ends} t=$t" }
}

fun stuckUpsideDownEnds() {
    val ev = Rec()
    val s = GameSession(jeep, Upgrades(), flat, ev)
    val c = s.car
    c.angle = PI
    c.y = s.terrain.height(c.x) + 2.0
    var t = 0.0
    while (t < 4.0 && !s.over) { s.update(DT, 0.0, 0.0); t += DT }
    req(ev.ends == listOf(EndReason.CRASH)) { "ends=${ev.ends} t=$t" }
}

fun refuel() {
    val ev = Rec()
    val s = GameSession(jeep, Upgrades(), flat, ev)
    val can = s.terrain.fuels.first()
    val c = s.car
    val oldGround = s.terrain.height(c.x)
    c.x = can.x - 4.0
    c.y += s.terrain.height(c.x) - oldGround
    s.fuel = s.fuelMax * 0.3
    var got = false
    for (i in 0 until 20 * 60) {
        s.update(DT, 1.0, 0.0)
        if (ev.fuelCount > 0) { got = true; break }
        if (s.over) break
    }
    req(got) { "kutu alınmadı (x=${c.x}, kutu=${can.x})" }
    req(s.fuel == s.fuelMax) { "fuel=${s.fuel} max=${s.fuelMax}" }
    req(can.taken) { "kutu taken değil" }
}

fun flipCounted() {
    val moon = Maps.byId("moon")
    val ev = Rec()
    val s = GameSession(jeep, Upgrades(), moon.copy(amp = 0.0), ev)
    val c = s.car
    c.y += 10.0
    c.omega = 8.0
    for (i in 0 until 15 * 60) { s.update(DT, 0.0, 0.0); if (ev.flips.isNotEmpty() || s.over) break }
    req(ev.flips.isNotEmpty()) { "onFlip yok (over=${s.over} reason=${s.endReason})" }
    req(ev.flips[0].first >= 1) { "count=${ev.flips[0].first}" }
    req(ev.flips[0].second == 250 * ev.flips[0].first) { "bonus=${ev.flips[0].second}" }
    req(s.flips >= 1) { "flips=${s.flips}" }
}

fun statusTextFormat() {
    val s = GameSession(jeep, Upgrades(), country)
    val t = s.statusText()
    req(t.contains("Mesafe") && t.contains("Yakıt yüzde")) { t }
    req(t.matches(Regex("Mesafe \\d+ metre\\. Yakıt yüzde \\d+\\. Altın \\d+\\. Burun .*\\.")) ) { t }
    println("  statusText: $t")
}

fun featureAnnouncedOnce() {
    val ev = Rec()
    val s = GameSession(jeep, Upgrades(), country, ev)
    val drv = SmartDriver()
    for (i in 0 until 60 * 60) { drv.control(s.car); s.update(DT, s.car.gas, s.car.brake); if (s.over) break }
    val counts = HashMap<Feature, Int>()
    for (f in ev.feats) counts[f] = (counts[f] ?: 0) + 1
    req(counts.values.all { it <= 1 }) { "tekrar eden öznitelik" }
    req(ev.feats.isNotEmpty()) { "hiç öznitelik duyurulmadı (x=${s.car.x})" }
    println("  ${ev.feats.size} öznitelik duyuruldu, mesafe ${"%.0f".format(s.distance)} m")
}

fun smartDriverRun() {
    val ev = Rec()
    val s = GameSession(jeep, Upgrades(), country, ev)
    val drv = SmartDriver()
    for (i in 0 until 120 * 60) { drv.control(s.car); s.update(DT, s.car.gas, s.car.brake); if (s.over) break }
    println("  distance=${"%.0f".format(s.distance)} coins=${s.coins} over=${s.over} ${s.endReason} fuel=${"%.2f".format(s.fuel)}")
    req(s.distance > 600) { "distance=${s.distance} (${s.endReason}, t=${s.time})" }
    req(s.coins > 50) { "coins=${s.coins}" }
    req(ev.dists.isNotEmpty() && ev.dists[0] == GameSession.DIST_STEP && ev.dists.zipWithNext().all { it.second - it.first == GameSession.DIST_STEP }) { "dists=${ev.dists}" }
}

fun fuelCansReachable() {
    val ev = Rec()
    val s = GameSession(jeep, Upgrades(), country, ev)
    val drv = SmartDriver()
    // bidon aralığı mesafeyle uzar (Terrain.fuelGapAt): geliştirmesiz jeep ilk 2 km'de yakıtsız kalmamalı, ilk 3 bidonu almalı
    for (i in 0 until 180 * 60) { drv.control(s.car); s.update(DT, s.car.gas, s.car.brake); if (s.over) break }
    println("  180 s: distance=${"%.0f".format(s.distance)} t=${"%.0f".format(s.time)} cans=${ev.fuelCount} over=${s.over} ${s.endReason} fuel=${"%.2f".format(s.fuel)}")
    req(s.endReason != EndReason.FUEL) { "yakıt bitti: x=${s.distance}, t=${s.time}" }
    req(ev.fuelCount >= 3) { "ilk 3 kutu alınmadı (${ev.fuelCount})" }
}

/** Aracı düz zemine, sırtüstü ya da verilen açıda, en alçak noktası zeminin hemen üstünde bırakır. */
fun placeAtAngle(s: GameSession, angleRad: Double) {
    val c = s.car; val d = s.vehicle
    c.angle = angleRad
    val ca = Math.cos(angleRad); val sa = Math.sin(angleRad)
    val sMid = d.suspTravel / 2
    val pts = listOf(
        doubleArrayOf(d.rearX, d.axleY - sMid, d.wheelR), doubleArrayOf(d.frontX, d.axleY - sMid, d.wheelR),
        doubleArrayOf(d.rearX - 0.15, d.axleY, 0.0), doubleArrayOf(d.frontX + 0.15, d.axleY, 0.0),
        doubleArrayOf(d.rearX * 0.8, d.headY, 0.0), doubleArrayOf(d.frontX * 0.8, d.headY, 0.0))
    val low = pts.minOf { sa * it[0] + ca * it[1] - it[2] }
    c.y = s.terrain.height(c.x) - low + 0.05
}

fun lyingOnBackEndsCrash() {
    for (v in Vehicles.all) for (deg in listOf(110, 124, 140, 160, 180)) {
        val ev = Rec()
        val s = GameSession(v, Upgrades(), flat, ev)
        placeAtAngle(s, Math.toRadians(deg.toDouble()))
        var t = 0.0
        while (t < 2.0 && !s.over) { s.update(DT, 0.0, 0.0); t += DT }
        req(s.over && s.endReason == EndReason.CRASH) { "${v.id} $deg°: bitmedi (over=${s.over} ${s.endReason}) açı=${s.tiltDeg()}" }
    }
    // eğimde 124° (burun yukarı, eğim 0.4 rad): zemine göre açı > 100°
    for (v in Vehicles.all) {
        val steep = flat.copy(id = "slope")
        val ev = Rec()
        val s = GameSession(v, Upgrades(), steep, ev)
        val ang = Math.toRadians(124.0)
        placeAtAngle(s, ang)
        s.car.vx = 0.0
        var t = 0.0
        while (t < 2.5 && !s.over) { s.update(DT, 0.0, 0.0); t += DT }
        req(s.over && s.endReason == EndReason.CRASH) { "${v.id} 124° (yamaç yok): bitmedi" }
    }
    // gerçek yamaçta: eğimli yerde ters dönmüş (zemine göre 110°+) araç
    run {
        val t0 = Terrain(country)
        var bx = 0.0; var bs = 0.0
        var x = 300.0
        while (x < 1500) { if (t0.slope(x) > bs) { bs = t0.slope(x); bx = x }; x += 1.0 }
        val s = GameSession(jeep, Upgrades(), country)
        val c = s.car
        c.x = bx; c.vx = 0.0
        val sl = Math.atan(t0.slope(bx))
        c.angle = sl + Math.toRadians(124.0)
        c.y = t0.height(bx) + 1.0
        var t = 0.0
        while (t < 4.0 && !s.over) { s.update(DT, 0.0, 0.0); t += DT }
        req(s.over && s.endReason == EndReason.CRASH) { "yamaçta 124°: bitmedi (eğim ${Math.toDegrees(sl).toInt()}° açı=${s.tiltDeg()} x=${c.x})" }
    }
}

fun headTouchesOnRoof() {
    // tam sırtüstü (tavan köşelerinde) duran her aracın kafası zemine değmeli
    for (v in Vehicles.all) {
        val s = GameSession(v, Upgrades(), flat)
        placeAtAngle(s, PI)
        var hit = false
        for (i in 0 until 90) { s.car.step(DT); if (s.car.headHit()) hit = true }
        req(hit) { "${v.id}: sırtüstü durunca kafa zemine değmedi" }
    }
}

fun flipMessages() {
    // kafa vurması
    val s = GameSession(jeep, Upgrades(), flat)
    placeAtAngle(s, PI)
    for (i in 0 until 4 * 60) { s.update(DT, 0.0, 0.0); if (s.over) break }
    req(s.over && s.endReason == EndReason.CRASH) { "bitmedi" }
    req(s.endMessage == "Kafa üstü düştün" || s.endMessage == "Ters döndün") { "mesaj=${s.endMessage}" }
    // tek başına devrilme: kafa değmeden zemine göre >100° durur
    val s2 = GameSession(jeep, Upgrades(), flat)
    s2.end(EndReason.CRASH)
    req(s2.endMessage == "Kaza yaptın") { "varsayılan mesaj=${s2.endMessage}" }
    val s3 = GameSession(jeep, Upgrades(), flat)
    s3.end(EndReason.CRASH, "Ters döndün")
    req(s3.endMessage == "Ters döndün") { "mesaj=${s3.endMessage}" }
    req(EndReason.FUEL.say == "Yakıt bitti") { "FUEL.say" }
}

fun tiltWrapped() {
    val moon = Maps.byId("moon")
    val s = GameSession(jeep, Upgrades(), moon.copy(amp = 0.0))
    val c = s.car
    c.y += 100.0
    var minT = 0; var maxT = 0; var revs = 0.0
    var prev = c.angle
    for (i in 0 until 6 * 60) {
        c.omega = 8.0
        s.update(DT, 0.0, 0.0)
        revs += Math.abs(c.angle - prev) / (2 * PI); prev = c.angle
        val t = s.tiltDeg()
        if (t < minT) minT = t; if (t > maxT) maxT = t
        if (s.over) break
    }
    req(revs >= 3.0) { "yeterince dönmedi: $revs tur" }
    req(minT >= -180 && maxT <= 180) { "tiltDeg aralık dışı: $minT..$maxT (açı=${c.angle})" }
    req(Math.abs(wrapAngle(7 * PI) - (-PI)) < 1e-9 || Math.abs(wrapAngle(7 * PI) - PI) < 1e-9) { "wrapAngle(7π)=${wrapAngle(7 * PI)}" }
    req(Math.abs(wrapAngle(-3.0 * PI + 0.1) - (-PI + 0.1)) < 1e-9) { "wrapAngle" }
}

/** Hep tam gaz (yakıt sınırsız) sürer; 2000 m'ye ya da kazaya dek. Döndürür: (kaza mı, mesafe). */
fun fullGasRun(assist: Boolean): Pair<Boolean, Int> {
    val s = GameSession(jeep, Upgrades(), country)
    s.balanceAssist = assist
    for (i in 0 until 200 * 60) {
        s.fuel = s.fuelMax
        s.update(DT, 1.0, 0.0)
        if (s.over || s.distance >= 2000) break
    }
    return Pair(s.over && s.endReason == EndReason.CRASH, s.distance.toInt())
}

fun assistPreventsFlip() {
    val on = fullGasRun(true)
    val off = fullGasRun(false)
    println("  tam gaz, cip: destek açık crash=${on.first} ${on.second} m | kapalı crash=${off.first} ${off.second} m")
    // 1543. m civarındaki tümsekten çıkışta havada gaz burnu 120°'ye kaldırıp ters devirir; destek bunu önler
    req(!on.first) { "destek açıkken cip ${on.second} m'de devrildi" }
    req(on.second >= 2000) { "destek açıkken 2000 m'ye varılamadı: ${on.second}" }
}

fun assistWarns() {
    val ev = object : GameEvents { var n = 0; override fun onTipWarning() { n++ } }
    val s = GameSession(jeep, Upgrades(), flat, ev)
    s.car.angle = Math.toRadians(55.0); s.car.y += 1.0
    s.update(DT, 1.0, 0.0)
    // havada uyarı yok; yerde kurulumu denemek için tekerlekler değene dek bekle
    for (i in 0 until 120) { s.car.angle = Math.toRadians(55.0); s.car.omega = 0.0; s.update(DT, 1.0, 0.0); if (s.over) break }
    req(ev.n in 1..2) { "uyarı sayısı ${ev.n}" }
    val ev2 = object : GameEvents { var n = 0; override fun onTipWarning() { n++ } }
    val s2 = GameSession(jeep, Upgrades(), flat, ev2)
    s2.balanceAssist = false
    for (i in 0 until 120) { s2.car.angle = Math.toRadians(55.0); s2.car.omega = 0.0; s2.update(DT, 1.0, 0.0); if (s2.over) break }
    req(ev2.n == 0) { "destek kapalıyken uyarı: ${ev2.n}" }
}

fun stuckHint() {
    class E : GameEvents { var n = 0; val at = ArrayList<Double>(); var t = 0.0; override fun onStuck() { n++; at.add(t) } }
    // güçsüz araç: gaz verir ama kıpırdamaz
    val e = E()
    val weak = jeep.copy(torque = 5.0)
    val s = GameSession(weak, Upgrades(), flat, e)
    for (i in 0 until 30 * 60) { e.t = s.time; s.update(DT, 1.0, 0.0); if (s.over) break }
    req(e.n >= 1) { "ipucu verilmedi" }
    req(e.at[0] in 5.5..7.0) { "ilk ipucu zamanı ${e.at[0]}" }
    req(e.at.zipWithNext().all { it.second - it.first >= 19.9 }) { "20 sn kısıtı çiğnendi: ${e.at}" }
    req(e.n <= 2) { "30 sn içinde ${e.n} ipucu" }
    // gaz yok: ipucu yok
    val e2 = E()
    val s2 = GameSession(weak, Upgrades(), flat, e2)
    for (i in 0 until 15 * 60) { s2.update(DT, 0.0, 0.0) }
    req(e2.n == 0) { "gazsız ipucu" }
    // yakıt yok: ipucu yok
    val e3 = E()
    val s3 = GameSession(weak, Upgrades(), flat, e3)
    s3.fuel = 0.0
    for (i in 0 until 2 * 60) s3.update(DT, 1.0, 0.0)
    req(e3.n == 0) { "yakıtsız ipucu" }
    // normal cip düzde ilerler: ipucu yok
    val e4 = E()
    val s4 = GameSession(jeep, Upgrades(), flat, e4)
    for (i in 0 until 20 * 60) { s4.update(DT, 1.0, 0.0); if (s4.over) break }
    req(e4.n == 0) { "ilerlerken ipucu" }
}

fun main() {
    coinsCollected(); println("coinsCollected ok")
    fuelRuns(); println("fuelRuns ok")
    noFuelStopEnds(); println("noFuelStopEnds ok")
    stuckUpsideDownEnds(); println("stuckUpsideDownEnds ok")
    refuel(); println("refuel ok")
    flipCounted(); println("flipCounted ok")
    statusTextFormat(); println("statusTextFormat ok")
    featureAnnouncedOnce(); println("featureAnnouncedOnce ok")
    smartDriverRun(); println("smartDriverRun ok")
    fuelCansReachable(); println("fuelCansReachable ok")
    lyingOnBackEndsCrash(); println("lyingOnBackEndsCrash ok")
    headTouchesOnRoof(); println("headTouchesOnRoof ok")
    flipMessages(); println("flipMessages ok")
    tiltWrapped(); println("tiltWrapped ok")
    assistPreventsFlip(); println("assistPreventsFlip ok")
    assistWarns(); println("assistWarns ok")
    stuckHint(); println("stuckHint ok")
    println("OK")
}
