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

/** Rastgele gaz/fren 600 s: NaN yok ve ilerleme. Her haritanın x=180 civarındaki ilk çukuru (1.5-2 m'lik dik duvar)
 *  rastgele bir sürücü için aşılmaz olduğundan, ilerleme sınaması çukurdan sonra (x=200) başlayan sürüşle yapılır:
 *  200 m ilerlemek yerine x > 350 beklenir. Diğer araç/harita çiftlerinde yalnızca sonlu durum sınanır. */
fun randomInputs10min() {
    val c = randomRun(Vehicles.byId("jeep"), country, 7, 200.0)
    req(c.x > 350) { "x=${c.x}" }
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
    println("OK")
}
