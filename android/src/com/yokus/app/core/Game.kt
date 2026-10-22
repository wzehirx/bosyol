package com.yokus.app.core

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.floor
import kotlin.math.roundToInt

enum class EndReason(val say: String) { CRASH("Kaza yaptın"), FUEL("Yakıt bitti"), QUIT("Bırakıldı") }

/** Açıyı [-π, π) aralığına getirir. */
fun wrapAngle(a: Double): Double = a - 2 * PI * floor((a + PI) / (2 * PI))

interface GameEvents {
    fun onCoin(value: Int, streak: Int) {}
    fun onFuel() {}
    fun onFuelLow(pct: Int) {}          // pct 25 ya da 10
    fun onLand(impact: Double) {}
    fun onFlip(count: Int, bonus: Int) {}
    fun onAirBonus(sec: Double, bonus: Int) {}
    fun onFeature(f: Feature) {}
    fun onDistance(m: Int) {}
    /** Seviye hedefi tamamlandı: [level] tamamlanan hedef sayısı (1 tabanlı), [next] sıradaki hedef (m). */
    fun onGoal(level: Int, target: Int, bonus: Int, next: Int) {}
    /** Koşu içinde mevcut en iyi mesafe aşıldı (koşu başına bir kez, rekor 0 iken yok). */
    fun onRecord(m: Int) {}
    fun onTipWarning() {}               // denge yardımı: araç devrilmek üzere
    fun onStuck() {}                    // gaz veriyor ama ilerleyemiyor
    fun onEnd(reason: EndReason) {}
}

/** Bir oyun turu: araç + zemin + yakıt, altın, takla, duyurular. Fizik 1/60 s gibi adımlarla [update] ile ilerler. */
class GameSession(val vehicle: VehicleDef, val upgrades: Upgrades, val map: MapDef, var events: GameEvents? = null,
                  goalLevel: Int = 0, val bestStart: Int = 0) {
    val terrain = Terrain(map)
    private val def: VehicleDef = vehicle.upgraded(upgrades)
    val car = Car(def, terrain, map.gravity, gripMul(upgrades), extraDrag = map.drag)
    val fuelMax: Double = def.fuelL
    var fuel: Double = fuelMax
    var coins = 0
    var distance = 0.0
    var airTime = 0.0        // toplam havada geçen süre (s)
    var flips = 0            // toplam takla
    var over = false
    var endReason: EndReason? = null
    /** Bitişin duyurulan/yazılan nedeni: "Ters döndün", "Kafa üstü düştün", yoksa [EndReason.say]. */
    var endMessage: String = ""
    /** Denge yardımı: yerde burun zemine göre 40°'yi aşınca gazı yumuşakça kısar (60°'de 0.3). */
    var balanceAssist = true
    var time = 0.0
    /** Bu haritada tamamlanmış hedef sayısı (koşu başında Store'dan gelir, hedef geçildikçe artar). */
    var goalLevel = goalLevel
        private set
    /** Bu koşuda tamamlanan hedef sayısı ve bonus altını (coins'e dahil değildir; altın = coins + goalBonus). */
    var goalsDone = 0
        private set
    var goalBonus = 0
        private set
    /** Sıradaki hedefin mesafesi (m). */
    val nextGoal: Int get() = Goals.target(goalLevel)
    /** Koşu başı hedef duyurusu yapıldı mı (duraklat-devam ile tekrarlanmasın). */
    var goalIntroDone = false
    var recordHit = false
        private set

    private val startX = car.x
    private val coinList = terrain.coins.sortedBy { it.x }
    private val fuelList = terrain.fuels.sortedBy { it.x }
    private var coinLo = 0
    private var fuelLo = 0
    private var lastCoinT = -1e9
    private var streak = 0
    private var featIdx = 0
    private var nextDist = DIST_STEP
    private var low25 = false
    private var low10 = false
    private var stillFuel = 0.0
    private var noFuelT = 0.0
    private var noFuelBest = 0.0
    private var flipT = 0.0
    private var lastTipT = -1e9
    private var stuckT = 0.0
    private var stuckX = 0.0
    private var lastStuckT = -1e9
    private var tryT = 0.0
    private var tryBest = 0.0
    private var wasAir = false
    private var airStreak = 0.0
    private var angAcc = 0.0

    fun update(dt: Double, gas: Double, brake: Double) {
        if (over) return
        time += dt
        val input = gas.coerceIn(0.0, 1.0)
        var g = input
        if (fuel <= 0.0) g = 0.0
        val rel = relAngle()
        if (balanceAssist) {
            if (!car.airborne) {
                if (rel > ASSIST_FROM) {
                    val u = ((rel - ASSIST_FROM) / (ASSIST_FULL - ASSIST_FROM)).coerceIn(0.0, 1.0)
                    g *= 1.0 - 0.7 * u * u * (3 - 2 * u)
                }
            } else if (rel > AIR_FROM) {
                // havada gaz yalnızca burnu kaldırır; burun zemine göre fazla kalkmışsa gazı iyice keser
                val u = ((rel - AIR_FROM) / (AIR_FULL - AIR_FROM)).coerceIn(0.0, 1.0)
                g *= 1.0 - u * u * (3 - 2 * u)
            }
            if (rel > TIP_WARN && time - lastTipT >= 6.0 && input > 0.0) { lastTipT = time; events?.onTipWarning() }
        }
        var b = brake.coerceIn(0.0, 1.0)
        if (balanceAssist && b > 0.0) {
            // fren burnu indirir (havada doğrudan, yerde hızlıyken frenleme): burun zemine göre fazla inmişse freni keser
            val from = if (car.airborne) AIR_FROM else ASSIST_FROM
            val full = if (car.airborne) AIR_FULL else ASSIST_FULL
            if (-rel > from && (car.airborne || car.speed > 0.5)) {
                val u = ((-rel - from) / (full - from)).coerceIn(0.0, 1.0)
                b *= 1.0 - u * u * (3 - 2 * u)
            }
        }
        car.gas = g
        car.brake = b
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
        while (distance >= nextDist) { events?.onDistance(nextDist); nextDist += DIST_STEP }
        while (distance >= Goals.target(goalLevel)) {
            val t = Goals.target(goalLevel); val b = Goals.bonus(goalLevel)
            goalLevel++; goalsDone++; goalBonus += b
            events?.onGoal(goalLevel, t, b, Goals.target(goalLevel))
        }
        if (!recordHit && bestStart > 0 && distance > bestStart) { recordHit = true; events?.onRecord(distance.toInt()) }

        pickups()
        fuelWarnings()
        announceFeatures()

        // --- ilerleyemiyor ipucu ---
        if (input > 0.3 && fuel > 0.0) {
            if (abs(car.x - stuckX) >= 1.0) { stuckX = car.x; stuckT = 0.0 } else stuckT += dt
            if (stuckT >= 6.0) {
                stuckT = 0.0; stuckX = car.x
                if (time - lastStuckT >= 20.0) { lastStuckT = time; events?.onStuck() }
            }
        } else { stuckT = 0.0; stuckX = car.x }
        // ileri geri deneyip (gaz ya da geri) yine de yeni mesafe kazanamıyorsa: 12 sn denemede 3 m'den az ilerleme
        if (fuel > 0.0 && (input > 0.3 || brake > 0.3)) {
            if (distance > tryBest + 3.0) { tryBest = distance; tryT = 0.0 } else tryT += dt
            if (tryT >= 12.0) {
                tryT = 0.0; tryBest = distance
                if (time - lastStuckT >= 20.0) { lastStuckT = time; events?.onStuck() }
            }
        } else if (distance > tryBest + 3.0) { tryBest = distance; tryT = 0.0 }

        // --- bitiş koşulları ---
        if (car.headHit()) { end(EndReason.CRASH, "Kafa üstü düştün"); return }
        val spd = abs(car.speed)
        // zemine göre 100°'den fazla ters duran araç, yerdeyken 1 sn sonra kaza sayılır (havada sayaç durur)
        val onGround = !car.airborne || car.hullTouch
        if (abs(relAngle()) > FLIP_DEG) { if (onGround) flipT += dt } else if (onGround) flipT = 0.0
        if (flipT >= FLIP_SECS) { end(EndReason.CRASH, "Ters döndün"); return }
        if (fuel <= 0.0 && spd < 0.3) stillFuel += dt else stillFuel = 0.0
        if (stillFuel >= 3.0) { end(EndReason.FUEL); return }
        // yakıtsız araç iki tepe arasında ileri geri sallanıp durmayabilir: 10 sn yeni mesafe yoksa da biter
        if (fuel <= 0.0) {
            if (distance > noFuelBest + 0.5) { noFuelBest = distance; noFuelT = 0.0 } else noFuelT += dt
            if (noFuelT >= NO_FUEL_SECS) { end(EndReason.FUEL); return }
        } else { noFuelBest = distance; noFuelT = 0.0 }
    }

    /** Aracın zemin eğimine göre açısı (derece, burun yukarı +, [-180, 180)). */
    fun relAngle(): Double = Math.toDegrees(wrapAngle(car.angle - atan(terrain.slope(car.x))))

    fun end(reason: EndReason, message: String = reason.say) {
        if (over) return
        over = true; endReason = reason; endMessage = message
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

    fun tiltDeg(): Int = Math.round(Math.toDegrees(wrapAngle(car.angle))).toInt()

    companion object {
        const val ASSIST_FROM = 40.0
        const val ASSIST_FULL = 60.0
        const val AIR_FROM = 15.0
        const val AIR_FULL = 35.0
        const val TIP_WARN = 50.0
        const val FLIP_DEG = 100.0
        const val FLIP_SECS = 1.0
        /** onDistance olayı bu adımla gelir (m); Sonify 250 m'de bir söyler. */
        const val DIST_STEP = 50
        const val NO_FUEL_SECS = 10.0
    }

    fun statusText(): String {
        val t = tiltDeg()
        val tilt = when { abs(t) < 3 -> "Burun düz"; t > 0 -> "Burun $t derece yukarı"; else -> "Burun ${-t} derece aşağı" }
        return "Mesafe ${distance.toInt()} metre. Yakıt yüzde ${fuelPct()}. Altın $coins. $tilt."
    }

    fun nearestAhead(): Pair<Coin?, FuelCan?> {
        val c = coinList.firstOrNull { !it.taken && it.x >= car.x && it.x <= car.x + 60.0 }
        val f = fuelList.firstOrNull { !it.taken && it.x >= car.x && it.x <= car.x + 60.0 }
        return Pair(c, f)
    }
}
