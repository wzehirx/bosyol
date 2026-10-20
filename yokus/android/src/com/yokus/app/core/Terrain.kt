package com.yokus.app.core

import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sin

enum class FeatureKind(val say: String) {
    STEEP_UP("Önde dik yokuş"), STEEP_DOWN("Önde dik iniş"), RAMP("Önde rampa"), PIT("Önde çukur"), BUMPS("Önde tümsekler")
}

data class Feature(val kind: FeatureKind, val x0: Double, val x1: Double)
class Coin(val x: Double, val y: Double, val value: Int) { var taken = false }
class FuelCan(val x: Double, val y: Double) { var taken = false }

/** Tohumdan deterministik üretilen zemin. 1 m aralıklı örnekler, aralarında doğrusal ara değer. */
class Terrain(val map: MapDef, val length: Double = 20000.0) {
    val surface: Surface get() = map.surface
    val features: List<Feature>
    val coins: List<Coin>
    val fuels: List<FuelCan>

    private val n = length.toInt() + 1
    private val h = DoubleArray(n)
    private val featX0: DoubleArray

    fun height(x: Double): Double {
        val xc = x.coerceIn(0.0, length)
        val i = minOf(floor(xc).toInt(), n - 2)
        val t = xc - i
        return h[i] * (1 - t) + h[i + 1] * t
    }

    fun slope(x: Double): Double {
        if (x < 0.0 || x > length) return 0.0
        val i = minOf(floor(x).toInt(), n - 2)
        return h[i + 1] - h[i]
    }

    /** x0'ı (x, x+range] içinde olan ilk öznitelik. */
    fun nextFeature(x: Double, range: Double): Feature? {
        var lo = 0; var hi = featX0.size
        while (lo < hi) { val m = (lo + hi) ushr 1; if (featX0[m] > x) hi = m else lo = m + 1 }
        if (lo >= features.size) return null
        val f = features[lo]
        return if (f.x0 <= x + range) f else null
    }

    private class Ev(val kind: FeatureKind, val x0: Double, val x1: Double, val a: Double, val b: Double)

    init {
        val rnd = Random(map.seed)
        // --- temel arazi: sinüs toplamı + değer gürültüsü ---
        val p1 = rnd.nextDouble() * 2 * PI; val p2 = rnd.nextDouble() * 2 * PI; val p3 = rnd.nextDouble() * 2 * PI
        val knots = DoubleArray(n / 5 + 3) { rnd.nextDouble() * 2 - 1 }
        fun noise(x: Double): Double {
            val i = floor(x / 5).toInt(); val t = x / 5 - i
            val s = t * t * (3 - 2 * t)
            return knots[i] * (1 - s) + knots[i + 1] * s
        }
        val raw = DoubleArray(n)
        for (i in 0 until n) {
            val x = i.toDouble()
            if (x <= 30.0) continue
            val e = ((x - 30.0) / 60.0).coerceIn(0.0, 1.0).let { it * it * (3 - 2 * it) }
            val s = 0.6 * sin(2 * PI * x / 90 + p1) + 0.25 * sin(2 * PI * x / 35 + p2) + 0.15 * sin(2 * PI * x / 14 + p3) + 0.2 * noise(x)
            raw[i] = e * map.amp * (1 + map.growth * x / 1000.0) * s
        }
        // --- özel parçalar: önce rampa, sonra çukur ve tümsek; çakışanlar atılır ---
        val evs = ArrayList<Ev>()
        fun free(x0: Double, x1: Double) = evs.none { x0 < it.x1 + 30 && x1 > it.x0 - 30 }
        var k = 1
        while (true) {
            val xr = k * map.rampEvery + (rnd.nextDouble() * 2 - 1) * 0.1 * map.rampEvery
            if (xr + 12 > length - 50) break
            k++
            if (xr >= 60 && free(xr, xr + 12)) evs.add(Ev(FeatureKind.RAMP, xr, xr + 12, 3.0, 0.0))
        }
        var px = 180.0
        while (px < length - 100) {
            val w = 5 + rnd.nextDouble() * 4; val depth = 1.2 + rnd.nextDouble()
            if (free(px, px + w)) evs.add(Ev(FeatureKind.PIT, px, px + w, depth, 0.0))
            px += 150 + rnd.nextDouble() * 250
        }
        var bx = 100.0
        while (bx < length - 100) {
            val cnt = 5 + rnd.nextInt(4)
            if (free(bx, bx + cnt * 3.0)) evs.add(Ev(FeatureKind.BUMPS, bx, bx + cnt * 3.0, 0.45, 0.0))
            bx += 120 + rnd.nextDouble() * 180
        }
        evs.sortBy { it.x0 }
        for (e in evs) {
            val i0 = floor(e.x0).toInt(); val i1 = minOf(n - 1, floor(e.x1).toInt() + 1)
            for (i in i0..i1) {
                val x = i.toDouble()
                if (x < e.x0 || x > e.x1) continue
                val u = (x - e.x0) / (e.x1 - e.x0)
                when (e.kind) {
                    FeatureKind.RAMP -> raw[i] += e.a * Math.pow(u, 1.3)
                    FeatureKind.PIT -> raw[i] -= e.a
                    else -> { val s = sin(PI * (x - e.x0) / 3.0); raw[i] += e.a * s * s }
                }
            }
        }
        // --- tek örnek eğimi 1.5'i (en çok 1.6) aşamaz ---
        val maxStep = 1.5
        for (i in 1 until n) h[i] = raw[i].coerceIn(h[i - 1] - maxStep, h[i - 1] + maxStep)

        // --- öznitelikler ---
        val feats = ArrayList<Feature>()
        for (e in evs) feats.add(Feature(e.kind, e.x0, e.x1))
        val steep = ArrayList<Feature>()
        var i = 40
        while (i + 10 < n) {
            val x0 = i.toDouble(); val x1 = x0 + 10
            if (evs.none { x0 < it.x1 + 8 && x1 > it.x0 - 5 }) {
                val avg = (h[i + 10] - h[i]) / 10.0
                if (avg > 0.45) steep.add(Feature(FeatureKind.STEEP_UP, x0, x1))
                else if (avg < -0.45) steep.add(Feature(FeatureKind.STEEP_DOWN, x0, x1))
            }
            i += 10
        }
        feats.addAll(steep)
        val merged = ArrayList<Feature>()
        for (kind in FeatureKind.values()) {
            var cur: Feature? = null
            for (f in feats.filter { it.kind == kind }.sortedBy { it.x0 }) {
                if (cur != null && f.x0 <= cur.x1 + 10) cur = Feature(kind, cur.x0, maxOf(cur.x1, f.x1))
                else { if (cur != null) merged.add(cur); cur = f }
            }
            if (cur != null) merged.add(cur)
        }
        merged.sortBy { it.x0 }
        features = merged
        featX0 = DoubleArray(merged.size) { merged[it].x0 }

        // --- çukur duvarı ve rampa uçurumu yakınına altın/bidon konmaz ---
        val bad = evs.filter { it.kind == FeatureKind.PIT || it.kind == FeatureKind.RAMP }.map {
            if (it.kind == FeatureKind.PIT) doubleArrayOf(it.x0 - 2.0, it.x1 + 2.0) else doubleArrayOf(it.x1 - 1.0, it.x1 + 3.0)
        }
        fun badZone(x: Double) = bad.firstOrNull { x >= it[0] && x <= it[1] }

        // --- altın grupları ---
        val cs = ArrayList<Coin>()
        var gx = 40.0 + rnd.nextDouble() * 40
        while (gx + 20 < length) {
            val v = if (gx < 500) 5 else if (gx < 1500) 25 else if (rnd.nextDouble() < 0.08) 500 else 100
            val cnt = 5 + rnd.nextInt(6)
            for (j in 0 until cnt) { val x = gx + 2.0 * j; if (badZone(x) == null) cs.add(Coin(x, height(x) + 1.2, v)) }
            gx += 60 + rnd.nextDouble() * 60
        }
        coins = cs
        // --- yakıt bidonları ---
        val fs = ArrayList<FuelCan>()
        var fx = 250.0 + (rnd.nextDouble() * 2 - 1) * 3
        while (fx < length - 10) {
            // bölgenin içindeyse en yakın güvenli kenara kaydır (dizi kaymaz, aralık ±60 içinde kalır)
            val z = badZone(fx)
            val px2 = if (z == null) fx else if (fx - z[0] < z[1] - fx) z[0] - 0.5 else z[1] + 0.5
            fs.add(FuelCan(px2, height(px2) + 0.8))
            fx += map.fuelEvery + (rnd.nextDouble() * 2 - 1) * 40
        }
        fuels = fs
    }
}
