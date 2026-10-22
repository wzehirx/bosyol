import com.yokus.app.core.*
import kotlin.math.abs
import kotlin.math.PI

fun fail(msg: String): Nothing = throw AssertionError(msg)
fun req(c: Boolean, msg: () -> String) { if (!c) fail(msg()) }

val country = Maps.byId("country")

fun newCar(v: VehicleDef = Vehicles.byId("jeep"), m: MapDef = country, startX: Double = 5.0) =
    Car(v, Terrain(m), m.gravity, 1.0, startX)

/** dt = 1/60 s adımlarla n adım. */
fun run(car: Car, seconds: Double, each: (Double) -> Unit = {}) {
    val n = Math.round(seconds * 60).toInt()
    for (i in 0 until n) { car.step(1.0 / 60); each((i + 1) / 60.0) }
}

fun finite(c: Car): Boolean = listOf(c.x, c.y, c.angle, c.vx, c.vy, c.omega, c.rpm,
    c.rear.x, c.rear.y, c.rear.vx, c.rear.vy, c.rear.spin, c.front.x, c.front.y, c.front.vx, c.front.vy, c.front.spin)
    .all { it.isFinite() }

fun restsOnFlat() {
    val c = newCar()
    run(c, 4.0)
    val ys = ArrayList<Double>()
    run(c, 1.0) { ys.add(c.y) }
    req(abs(c.vx) < 0.05) { "vx=${c.vx}" }
    req(abs(c.angle) < 0.02) { "angle=${c.angle}" }
    req(c.rear.grounded && c.front.grounded) { "tekerler yerde değil" }
    req(ys.max()!! - ys.min()!! < 0.01) { "y oynuyor ${ys.max()!! - ys.min()!!}" }
}

fun climbsCountry() {
    val c = newCar()
    c.gas = 1.0
    var minA = 0.0; var maxA = 0.0; var hit = false
    run(c, 60.0) { _ ->
        minA = minOf(minA, c.angle); maxA = maxOf(maxA, c.angle)
        if (c.headHit()) hit = true
    }
    req(c.x > 300) { "x=${c.x}" }
    req(!hit) { "kafa yere değdi" }
    req(minA >= -1.4 && maxA <= 1.4) { "açı aralığı $minA..$maxA" }
}

fun airGasNoseUp() {
    val c = newCar(); c.y += 10.0; c.gas = 1.0
    val a0 = c.angle
    run(c, 0.5)
    req(c.angle - a0 > 0.3) { "açı artışı ${c.angle - a0}" }
}

fun airBrakeNoseDown() {
    val c = newCar(); c.y += 10.0; c.brake = 1.0
    val a0 = c.angle
    run(c, 0.5)
    req(a0 - c.angle > 0.3) { "açı düşüşü ${a0 - c.angle}" }
}

fun upsideDownHeadHit() {
    val c = newCar()
    c.angle = PI; c.y = c.terrain.height(c.x) + 1.0
    var t = 0.0; var hit = false
    run(c, 2.0) { tt -> t = tt; if (!hit && c.headHit()) hit = true }
    req(hit) { "kafa yere değmedi" }
}

fun airtime(m: MapDef): Double {
    val c = newCar(Vehicles.byId("jeep"), m)
    c.y += 5.0; c.vx = 10.0
    var t = 0.0
    while (t < 20.0) {
        c.step(1.0 / 60); t += 1.0 / 60
        if (c.rear.grounded || c.front.grounded) return t
    }
    return t
}

fun moonFloaty() {
    val a = airtime(country); val b = airtime(Maps.byId("moon"))
    req(b > 2 * a) { "ay $b, kır $a" }
}

fun highDropNoTunnel() {
    for (id in listOf("jeep", "monster", "moto", "bus", "super")) {
        val c = newCar(Vehicles.byId(id))
        c.y += 40.0
        var t = 0.0
        run(c, 6.0) { tt ->
            t = tt
            req(finite(c)) { "$id: NaN" }
            if (tt >= 2.0) for (w in listOf(c.rear, c.front))
                req(w.y >= c.terrain.height(w.x) - 0.05) { "$id: t=$tt tekerlek ${w.y} < zemin ${c.terrain.height(w.x)}" }
        }
    }
    // hızlı iniş: yokuş aşağı çok yüksek hız
    for (id in listOf("jeep", "monster")) {
        val c = newCar(Vehicles.byId(id), country, 400.0)
        c.y = c.terrain.height(400.0) + 30.0; c.vx = 40.0; c.vy = -50.0
        run(c, 8.0) { tt ->
            req(finite(c)) { "$id: NaN hızlı" }
            for (w in listOf(c.rear, c.front))
                if (tt >= 2.0) req(w.y >= c.terrain.height(w.x) - 0.05) { "$id: hızlı t=$tt tekerlek ${w.y} < zemin" }
        }
    }
}

fun randomRun(v: VehicleDef, m: MapDef, seed: Long, startX: Double = 5.0): Car {
    val c = newCar(v, m, startX)
    val rnd = java.util.Random(seed)
    var next = 0.0
    run(c, 600.0) { t ->
        if (t >= next) {
            next = t + 0.3 + rnd.nextDouble() * 1.2
            val r = rnd.nextDouble()
            c.gas = if (r < 0.65) 1.0 else if (r < 0.8) 0.5 else 0.0
            c.brake = if (rnd.nextDouble() < 0.2) 1.0 else 0.0
        }
        req(finite(c)) { "${v.id}/${m.id}: NaN t=$t" }
    }
    return c
}

/** Rastgele gaz/fren 600 s: NaN yok ve ilerleme (cip, kır yolu, x=5'ten başlar, x > 200). Diğer araç/harita çiftlerinde
 *  yalnızca sonlu durum sınanır. */
fun randomInputs10min() {
    val c = randomRun(Vehicles.byId("jeep"), country, 7)
    req(c.x > 200) { "x=${c.x}" }
    for ((id, mid) in listOf("jeep" to "country", "moto" to "moon", "super" to "mountain", "monster" to "site", "bus" to "cave"))
        randomRun(Vehicles.byId(id), Maps.byId(mid), 11)
}

fun allVehiclesClimb() {
    for (v in Vehicles.all) {
        val c = newCar(v)
        c.gas = 1.0
        var flipped = false; var xAtFlip = -1.0; var minA = 0.0; var maxA = 0.0
        run(c, 30.0) { _ ->
            minA = minOf(minA, c.angle); maxA = maxOf(maxA, c.angle)
            if (!flipped && (abs(c.angle) > 1.6 || c.headHit())) { flipped = true; xAtFlip = c.x }
        }
        println("  ${v.id}: x30=${"%.0f".format(c.x)} flip=$flipped xAtFlip=${"%.0f".format(xAtFlip)} angle=${"%.2f".format(minA)}..${"%.2f".format(maxA)}")
        if (v.id == "moto" || v.id == "super") {
            val best = if (flipped) xAtFlip else c.x
            req(best > 60 && (c.x > 100 || flipped)) { "${v.id}: x=${c.x} flipX=$xAtFlip" }
        } else req(c.x > 100) { "${v.id}: x=${c.x}" }
    }
}

/** HCR'ye yakın en yüksek hızlar: devir sınırında (maxRpm) teker çevresel hızı; devir = teker devri × ratio. */
fun topSpeeds() {
    val want = mapOf("jeep" to 15.0..18.0, "moto" to 18.5..21.5, "pickup" to 13.0..17.0, "tractor" to 8.0..10.5,
        "buggy" to 15.0..19.0, "rally" to 20.5..23.5, "monster" to 12.0..16.0, "bus" to 12.0..14.0, "snowmobile" to 15.0..19.0,
        "rover" to 12.0..16.0, "tank" to 8.0..10.5, "super" to 26.0..30.0,
        "scooter" to 13.0..16.0, "gokart" to 18.5..21.5, "quad" to 13.5..16.5, "chopper" to 15.0..18.0, "hotrod" to 18.5..22.5,
        "garbage" to 10.0..13.0, "firetruck" to 12.5..15.5, "formula" to 24.0..28.0)
    for (v in Vehicles.all) {
        val vmax = v.maxRpm * 2 * PI / 60 / v.ratio * v.wheelR
        req(vmax in want.getValue(v.id)) { "${v.id}: en yüksek hız $vmax" }
    }
    // cip, akıllı sürücüyle kır yolunda: motor sesi rölantiden en yüksek devre süpürür, hız devir sınırına yaklaşır
    val c = newCar()
    var lo = 1e9; var hi = 0.0
    val r = smartDrive(c, 30.0) { car -> SmartDriver().control(car); lo = minOf(lo, car.rpm); hi = maxOf(hi, car.rpm) }
    val j = Vehicles.byId("jeep")
    req(lo <= j.idleRpm + 1 && hi >= 0.95 * j.maxRpm) { "devir aralığı $lo..$hi" }
    req(r.topSpeed > 15.0 && r.topSpeed < 22.0) { "cip en yüksek hız ${r.topSpeed}" }
}

/** Akıllı sürücü (test/lib/Driver.kt): her araç kır yolunda 30 s'de x > 250, cip 120 s'de x > 600; kaza yok. */
fun smartDriverCountry() {
    for (v in Vehicles.all) {
        val r = smartDrive(newCar(v), 30.0)
        println("  ${v.id}: x30=${"%.0f".format(r.car.x)} top=${"%.1f".format(r.topSpeed)}")
        req(!r.crashed && r.car.x > 250) { "${v.id}: x=${r.car.x} kaza=${r.crashed}@${r.crashX}" }
    }
    val j = smartDrive(newCar(), 120.0)
    req(!j.crashed && j.car.x > 600) { "cip 120 s: x=${j.car.x} kaza=${j.crashed}@${j.crashX}" }
}

/** Dikte tam gaz hafif aracı arkaya devirir; gazla oynamak (akıllı sürücü) bunu önler. */
fun featherPreventsFlip() {
    val m = Vehicles.byId("moto")
    val floor = smartDrive(newCar(m), 120.0) { it.gas = 1.0; it.brake = 0.0 }
    val smart = smartDrive(newCar(m), 120.0)
    req(floor.crashed) { "moto tam gazla devrilmeli (x=${floor.car.x})" }
    req(!smart.crashed && smart.maxX > floor.crashX + 200) { "akıllı sürücü devrilmemeli: ${smart.crashed}@${smart.crashX}, x=${smart.maxX}" }
}

/** Sağlam ayar: tork ±%15 ve süspansiyon (k, c) ±%15 ile her araç akıllı sürücüyle 30 s'de olağan mesafesinin %80'inden fazlasını gider. */
fun robustTuning() {
    for (v in Vehicles.all) {
        val nominal = smartDrive(newCar(v), 30.0).car.x - 5.0
        var worst = 1e9; var worstName = ""
        for (tq in listOf(0.85, 1.0, 1.15)) for (sp in listOf(0.85, 1.0, 1.15)) {
            if (tq == 1.0 && sp == 1.0) continue
            val d = v.copy(torque = v.torque * tq, suspK = v.suspK * sp, suspC = v.suspC * sp)
            val r = smartDrive(newCar(d), 30.0)
            val dist = r.car.x - 5.0
            if (dist < worst) { worst = dist; worstName = "tork×$tq süsp×$sp" }
            req(!r.crashed && dist > 0.8 * nominal) { "${v.id} tork×$tq süsp×$sp: ${"%.0f".format(dist)} m < %80 × ${"%.0f".format(nominal)} (kaza=${r.crashed})" }
        }
        println("  ${v.id}: olağan ${"%.0f".format(nominal)} m, en kötü ${"%.0f".format(worst)} m ($worstName)")
    }
}

fun main() {
    restsOnFlat(); println("restsOnFlat ok")
    climbsCountry(); println("climbsCountry ok")
    airGasNoseUp(); println("airGasNoseUp ok")
    airBrakeNoseDown(); println("airBrakeNoseDown ok")
    upsideDownHeadHit(); println("upsideDownHeadHit ok")
    moonFloaty(); println("moonFloaty ok")
    highDropNoTunnel(); println("highDropNoTunnel ok")
    randomInputs10min(); println("randomInputs10min ok")
    allVehiclesClimb(); println("allVehiclesClimb ok")
    topSpeeds(); println("topSpeeds ok")
    smartDriverCountry(); println("smartDriverCountry ok")
    featherPreventsFlip(); println("featherPreventsFlip ok")
    robustTuning(); println("robustTuning ok")
    println("OK")
}
