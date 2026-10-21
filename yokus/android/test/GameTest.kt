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
    req(ev.dists.isNotEmpty() && ev.dists[0] == 100 && ev.dists.zipWithNext().all { it.second - it.first == 100 }) { "dists=${ev.dists}" }
}

fun fuelCansReachable() {
    val ev = Rec()
    val s = GameSession(jeep, Upgrades(), country, ev)
    val drv = SmartDriver()
    for (i in 0 until 300 * 60) { drv.control(s.car); s.update(DT, s.car.gas, s.car.brake); if (s.over) break }
    println("  300 s: distance=${"%.0f".format(s.distance)} t=${"%.0f".format(s.time)} cans=${ev.fuelCount} over=${s.over} ${s.endReason} fuel=${"%.2f".format(s.fuel)}")
    req(s.endReason != EndReason.FUEL) { "yakıt bitti: x=${s.distance}, t=${s.time}" }
    req(ev.fuelCount >= 1) { "hiç kutu alınmadı" }
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
    println("OK")
}
