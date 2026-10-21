package com.yokus.app.core

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

enum class EndReason(val say: String) { CRASH("Kaza yaptın"), FUEL("Yakıt bitti"), QUIT("Bırakıldı") }

interface GameEvents {
    fun onCoin(value: Int, streak: Int) {}
    fun onFuel() {}
    fun onFuelLow(pct: Int) {}          // pct 25 ya da 10
    fun onLand(impact: Double) {}
    fun onFlip(count: Int, bonus: Int) {}
    fun onAirBonus(sec: Double, bonus: Int) {}
    fun onFeature(f: Feature) {}
    fun onDistance(m: Int) {}
    fun onEnd(reason: EndReason) {}
}

/** Bir oyun turu: araç + zemin + yakıt, altın, takla, duyurular. Fizik 1/60 s gibi adımlarla [update] ile ilerler. */
class GameSession(val vehicle: VehicleDef, val upgrades: Upgrades, val map: MapDef, var events: GameEvents? = null) {
    val terrain = Terrain(map)
    private val def: VehicleDef = vehicle.upgraded(upgrades)
    val car = Car(def, terrain, map.gravity, gripMul(upgrades))
    val fuelMax: Double = def.fuelL
    var fuel: Double = fuelMax
    var coins = 0
    var distance = 0.0
    var airTime = 0.0        // toplam havada geçen süre (s)
    var flips = 0            // toplam takla
    var over = false
    var endReason: EndReason? = null
    var time = 0.0

    private val startX = car.x
    private val coinList = terrain.coins.sortedBy { it.x }
    private val fuelList = terrain.fuels.sortedBy { it.x }
    private var coinLo = 0
    private var fuelLo = 0
    private var lastCoinT = -1e9
    private var streak = 0
    private var featIdx = 0
    private var nextDist = 100
    private var low25 = false
    private var low10 = false
    private var stillFuel = 0.0
    private var stillFlip = 0.0
    private var wasAir = false
    private var airStreak = 0.0
    private var angAcc = 0.0

    fun update(dt: Double, gas: Double, brake: Double) {
        if (over) return
        time += dt
        var g = gas.coerceIn(0.0, 1.0)
        if (fuel <= 0.0) g = 0.0
        car.gas = g
        car.brake = brake.coerceIn(0.0, 1.0)
        if (fuel > 0.0) fuel = maxOf(0.0, fuel - def.fuelUse * (0.2 + 0.8 * g) * dt)

        val a0 = car.angle
        car.step(dt)
        val da = car.angle - a0

        // --- havada geçen süre, takla, iniş ---
        val air = car.airborne
        if (air) {
            airTime += dt; airStreak += dt; angAcc += da
        }
        if (car.landImpact > 0.0) events?.onLand(car.landImpact)
        if (wasAir && !air) {
            val n = floor(abs(angAcc) / (2 * PI)).toInt()
            if (n >= 1) {
                val bonus = 250 * n
                flips += n; coins += bonus
                events?.onFlip(n, bonus)
            }
            if (airStreak >= 2.0) {
                val bonus = 25 * floor(airStreak).toInt()
                coins += bonus
                events?.onAirBonus(airStreak, bonus)
            }
        }
        if (!air) { airStreak = 0.0; angAcc = 0.0 }
        wasAir = air

        // --- mesafe ---
        if (car.x - startX > distance) distance = car.x - startX
        while (distance >= nextDist) { events?.onDistance(nextDist); nextDist += 100 }

        pickups()
        fuelWarnings()
        announceFeatures()

        // --- bitiş koşulları ---
        if (car.headHit()) { end(EndReason.CRASH); return }
        val spd = abs(car.speed)
        var na = car.angle % (2 * PI)
        if (na > PI) na -= 2 * PI else if (na < -PI) na += 2 * PI
        if (abs(na) > 2.2 && spd < 0.5) stillFlip += dt else stillFlip = 0.0
        if (stillFlip >= 2.5) { end(EndReason.CRASH); return }
        if (fuel <= 0.0 && spd < 0.3) stillFuel += dt else stillFuel = 0.0
        if (stillFuel >= 3.0) { end(EndReason.FUEL); return }
    }

    fun end(reason: EndReason) {
        if (over) return
        over = true; endReason = reason
        events?.onEnd(reason)
    }

    private fun near(px: Double, py: Double, x: Double, y: Double) = abs(px - x) < 1.3 && abs(py - y) < 1.8

    private fun hits(x: Double, y: Double): Boolean =
        near(car.x, car.y, x, y) || near(car.rear.x, car.rear.y, x, y) || near(car.front.x, car.front.y, x, y)

    private fun pickups() {
        val lo = car.x - 4.0; val hi = car.x + 4.0
        while (coinLo < coinList.size && coinList[coinLo].x < lo) coinLo++
        var i = coinLo
        while (i < coinList.size && coinList[i].x <= hi) {
            val c = coinList[i++]
            if (c.taken || !hits(c.x, c.y)) continue
            c.taken = true
            coins += c.value
            streak = if (time - lastCoinT <= 2.5) streak + 1 else 1
            lastCoinT = time
            events?.onCoin(c.value, streak)
        }
        while (fuelLo < fuelList.size && fuelList[fuelLo].x < lo) fuelLo++
        i = fuelLo
        while (i < fuelList.size && fuelList[i].x <= hi) {
            val f = fuelList[i++]
            if (f.taken || !hits(f.x, f.y)) continue
            f.taken = true
            fuel = fuelMax
            events?.onFuel()
        }
    }

    private fun fuelWarnings() {
        val pct = if (fuelMax > 0) fuel / fuelMax * 100 else 0.0
        if (pct > 25.0) { low25 = false; low10 = false; return }   // yenilenince yeniden kurulur
        if (pct > 10.0) low10 = false
        if (!low25) { low25 = true; events?.onFuelLow(25) }
        if (pct <= 10.0 && !low10) { low10 = true; events?.onFuelLow(10) }
    }

    private fun announceFeatures() {
        val fs = terrain.features
        while (featIdx < fs.size && fs[featIdx].x0 <= car.x + 35.0) {
            val f = fs[featIdx++]
            if (f.x0 > car.x) events?.onFeature(f)
        }
    }

    fun fuelPct(): Int = if (fuelMax > 0) (fuel / fuelMax * 100).roundToInt().coerceIn(0, 100) else 0

    fun tiltDeg(): Int = Math.round(Math.toDegrees(car.angle)).toInt()

    fun statusText(): String {
        val t = tiltDeg()
        val tilt = when { t > 0 -> "Burun $t derece yukarı"; t < 0 -> "Burun ${-t} derece aşağı"; else -> "Burun düz" }
        return "Mesafe ${distance.toInt()} metre. Yakıt yüzde ${fuelPct()}. Altın $coins. $tilt."
    }

    fun nearestAhead(): Pair<Coin?, FuelCan?> {
        val c = coinList.firstOrNull { !it.taken && it.x >= car.x && it.x <= car.x + 60.0 }
        val f = fuelList.firstOrNull { !it.taken && it.x >= car.x && it.x <= car.x + 60.0 }
        return Pair(c, f)
    }
}
