package com.yokus.app.core

import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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

    private class Ev(val kind: FeatureKind, val x0: Double, val x1: Double, val a: Double, val b: Double, val c: Double = 0.0, val d: Double = 0.0)

    /** x konumunda izin verilen en büyük eğim: ilk 300 m 0.5; 1500 m'ye dek yumuşakça min(1, maxSlope)'ye çıkar;
     *  1.0'dan dik haritalarda (dağ, mağara) ancak 1500 m'den sonra, 2500 m'ye dek [MapDef.maxSlope]'a varır. */
    fun maxSlopeAt(x: Double): Double {
        if (x <= GENTLE_END) return GENTLE_SLOPE
        val m1 = minOf(map.maxSlope, 1.0)
        val a = GENTLE_SLOPE + (m1 - GENTLE_SLOPE) * ss((x - GENTLE_END) / (HARD_FROM - GENTLE_END))
        return if (x <= HARD_FROM || map.maxSlope <= m1) a else a + (map.maxSlope - m1) * ss((x - HARD_FROM) / 1000.0)
    }

    /** 0..1 zorluk: 300 m'ye dek 0, sonra mesafe, genlik ve büyümeyle artar. */
    fun difficulty(x: Double): Double =
        ((x - GENTLE_END) / 2500.0 * sqrt(map.amp / 3.0) * sqrt(map.growth / 0.15)).coerceIn(0.0, 1.0)

    init {
        val rnd = Random(map.seed)
        // --- temel arazi: tepe dizisi. Her bileşenin genliği, eğim payı × dalga boyu / 2π × S(x) olarak seçilir;
        //     böylece S(x) bileşenlerin hepsi aynı anda dik olduğunda bile ulaşılan en büyük eğimdir. ---
        val l1 = 150 + rnd.nextDouble() * 60; val l2 = 48 + rnd.nextDouble() * 20; val l3 = 17 + rnd.nextDouble() * 7
        val p1 = rnd.nextDouble() * 2 * PI; val p2 = rnd.nextDouble() * 2 * PI; val p3 = rnd.nextDouble() * 2 * PI
        val kn = 12.0
        val knots = DoubleArray((n / kn).toInt() + 3) { rnd.nextDouble() * 2 - 1 }
        fun noise(x: Double): Double {
            val i = floor(x / kn).toInt(); val t = x / kn - i
            return knots[i] + (knots[i + 1] - knots[i]) * ss(t)
        }
        fun bigS(x: Double): Double {
            val sx = 0.16 * map.amp * (1 + map.growth * x / 1000.0) * (0.7 + 0.3 * ss((x - GENTLE_END) / 700.0))
            val cap = 0.95 * maxSlopeAt(x)
            return minOf(sx, cap, if (x < GENTLE_END) 0.4 else 9.0)
        }
        val base = DoubleArray(n)
        for (i in 0 until n) {
            val x = i.toDouble()
            if (x <= FLAT) continue
            val e = ss((x - FLAT) / 60.0)
            val s = 0.47 * l1 / (2 * PI) * sin(2 * PI * x / l1 + p1) + 0.33 * l2 / (2 * PI) * sin(2 * PI * x / l2 + p2) +
                0.10 * l3 / (2 * PI) * sin(2 * PI * x / l3 + p3) + 0.10 * kn / 3.0 * noise(x)
            base[i] = e * bigS(x) * s
        }
        // tepeleri yuvarla: 7'li kayan ortalama, iki geçiş (eğimleri ortaladığından eğim sınırını bozmaz)
        for (pass in 0 until 2) {
            val src = base.copyOf()
            for (i in 0 until n) {
                var acc = 0.0
                for (k in i - 3..i + 3) acc += src[k.coerceIn(0, n - 1)]
                base[i] = acc / 7.0
            }
        }
        // yoğunluk: özellikler mesafe ve harita zorluğuyla sıklaşır
        fun density(x: Double) = 1 + 2.0 * (map.amp / 3.0) * map.growth * maxOf(0.0, x - GENTLE_END) / 1000.0

        // --- özel parçalar: önce rampa, sonra çukur ve tümsek; çakışanlar atılır (aralarında en az 40 m).
        //     Rampa ve çukur, temel eğimin küçük olduğu yere (en çok 120 m ileri kaydırılarak) konur; altlarındaki
        //     temel arazi sonra doğrusal kirişe düzlenir, böylece biçimleri (derinlik, duvar ve iniş eğimi) tam tutar. ---
        val evs = ArrayList<Ev>()
        fun free(x0: Double, x1: Double) = evs.none { x0 < it.x1 + 40 && x1 > it.x0 - 40 }
        fun chord(x0: Double, x1: Double): Double {
            val i0 = floor(x0).toInt().coerceIn(0, n - 1); val i1 = floor(x1).toInt().coerceIn(0, n - 1)
            return if (i1 > i0) (base[i1] - base[i0]) / (i1 - i0) else 0.0
        }
        // ilk rampa: kolay haritalarda geç (kır yolu ~1.2 km), zor ve rampalı haritalarda erken
        val rampFrom = minOf(HARD_FROM, GENTLE_END + 1.75 * map.rampEvery / (sqrt(map.amp / 3.0) * sqrt(map.growth / 0.15)))
        var xr = 0.0
        while (true) {
            xr += map.rampEvery / sqrt(density(xr)) * (0.9 + rnd.nextDouble() * 0.2)
            val d = difficulty(xr)
            val hgt = 1.0 + (0.6 + 0.4 * rnd.nextDouble()) * 1.8 * d       // rampa yüksekliği (m)
            val lip = 0.3 + 0.4 * d                                         // ağız eğimi (sıçrama)
            if (xr + 80 > length - 50) break
            if (xr < rampFrom) continue
            var x0 = xr
            while (x0 < xr + 120) {
                // iniş: en çok LAND_MAX eğimli, uçuşu karşılayacak kadar uzun yumuşak iniş (uçurum değil)
                val ld = 10.0 + 6.0 * hgt
                val lk0 = hgt / (0.75 * lip)
                val cs = chord(x0, x0 + lk0 + ld)
                val lipS = minOf(lip, maxSlopeAt(x0) - 0.05 - cs)
                val lk = hgt / (0.75 * lipS)
                val landSlope = 2 * hgt / ld - cs
                if (abs(cs) <= 0.15 && lipS >= 0.25 && landSlope <= LAND_MAX) {
                    if (free(x0, x0 + lk + ld)) evs.add(Ev(FeatureKind.RAMP, x0, x0 + lk, hgt, ld))
                    break
                }
                x0 += 2.0
            }
        }
        var px = GENTLE_END + rnd.nextDouble() * 60
        while (px < length - 100) {
            val d = difficulty(px)
            val depth = minOf(MAX_PIT_DEPTH, 0.45 + 0.75 * d * (0.6 + 0.4 * rnd.nextDouble()))
            val wallTarget = 0.3 + 0.45 * d
            val bottom = 1.5 + rnd.nextDouble() * 3.0
            var x0 = px
            while (x0 < px + 120) {
                val w0 = PI * depth / wallTarget + bottom
                val csS = chord(x0, x0 + w0); val cs = abs(csS)
                // çıkıştan sonra zemin hemen düşmesin (tepe üstündeki çukur, çıkış duvarını sıçrama rampasına çevirir)
                val convex = chord(x0 + w0, x0 + w0 + 16) < csS - 0.03
                val sw = minOf(wallTarget, MAX_PIT_WALL - 0.04 - cs, maxSlopeAt(x0) - 0.04 - cs)
                if (cs <= 0.25 && sw >= 0.25 && !convex) {
                    val lw = PI * depth / (2 * sw)
                    val w = 2 * lw + bottom
                    if (free(x0, x0 + w)) evs.add(Ev(FeatureKind.PIT, x0, x0 + w, depth, lw))
                    break
                }
                x0 += 2.0
            }
            px += (150 + rnd.nextDouble() * 250) / density(px)
        }
        // rampa ve çukur altındaki temel araziyi kirişe düzle (kenarlarda FLAT_BLEND m yumuşak geçiş)
        for (e in evs) {
            val end = if (e.kind == FeatureKind.RAMP) e.x1 + e.b else e.x1
            val i0 = floor(e.x0).toInt(); val i1 = minOf(n - 1, floor(end).toInt() + 1)
            val b0 = base[i0]; val sl = (base[i1] - b0) / (i1 - i0)
            val j0 = maxOf(0, i0 - FLAT_BLEND); val j1 = minOf(n - 1, i1 + FLAT_BLEND)
            for (j in j0..j1) {
                val w = if (j < i0) ss((j - j0).toDouble() / (i0 - j0)) else if (j > i1) ss((j1 - j).toDouble() / (j1 - i1)) else 1.0
                base[j] += w * (b0 + sl * (j - i0) - base[j])
            }
        }
        var bx = 100.0
        while (bx < length - 100) {
            val d = difficulty(bx)
            val cnt = 4 + rnd.nextInt(5)
            val per = 3.0 + rnd.nextDouble()
            val a = 0.2 + 0.3 * d
            if (free(bx, bx + cnt * per)) evs.add(Ev(FeatureKind.BUMPS, bx, bx + cnt * per, a, per))
            bx += (120 + rnd.nextDouble() * 180) / density(bx)
        }
        evs.sortBy { it.x0 }
        val raw = base.copyOf()
        for (e in evs) {
            val end = if (e.kind == FeatureKind.RAMP) e.x1 + e.b else e.x1
            val i0 = floor(e.x0).toInt(); val i1 = minOf(n - 1, floor(end).toInt() + 1)
            for (i in i0..i1) {
                val x = i.toDouble()
                if (x < e.x0 || x > end) continue
                when (e.kind) {
                    FeatureKind.RAMP -> {
                        // sıçrama rampası: ilk yarıda eğim 0'dan ağız eğimine yumuşakça çıkar, ikinci yarı ağız eğiminde düz;
                        // ardından ağızdan başlayan uzun, hafif iniş (h = H·(1−v)², en dik yeri ağızda 2H/ld)
                        raw[i] += if (x <= e.x1) {
                            val u = (x - e.x0) / (e.x1 - e.x0)
                            e.a * (if (u <= 0.5) u * u else u - 0.25) / 0.75
                        } else { val v = (x - e.x1) / e.b; e.a * (1 - v) * (1 - v) }
                    }
                    FeatureKind.PIT -> {
                        // kosinüs duvarlı, düz tabanlı çukur
                        val lw = e.b
                        val q = when {
                            x < e.x0 + lw -> (1 - cos(PI * (x - e.x0) / lw)) / 2
                            x > e.x1 - lw -> (1 - cos(PI * (e.x1 - x) / lw)) / 2
                            else -> 1.0
                        }
                        raw[i] -= e.a * q
                    }
                    else -> { val s = sin(PI * (x - e.x0) / e.b); raw[i] += e.a * s * s }
                }
            }
        }
        // --- eğim sınırı (izleyerek kırpma) ve köşeleri yuvarlayan iki geçişli 3'lü ortalama (rampa yükselişi hariç).
        //     Ortalama, eğimleri ortaladığı için sınırı bozmaz. ---
        val clip = DoubleArray(n)
        for (i in 1 until n) { val c = maxSlopeAt(i - 0.5); clip[i] = raw[i].coerceIn(clip[i - 1] - c, clip[i - 1] + c) }
        var sm = clip
        // rampa yükselişi zaten yumuşak; ortalama ağzı köreltmesin
        val keep = BooleanArray(n)
        for (e in evs) if (e.kind == FeatureKind.RAMP) for (i in floor(e.x0).toInt() + 1..floor(e.x1).toInt()) keep[i] = true
        for (pass in 0 until 2) {
            val nx = DoubleArray(n)
            nx[0] = sm[0]; nx[n - 1] = sm[n - 1]
            for (i in 1 until n - 1) nx[i] = if (keep[i]) sm[i] else (sm[i - 1] + sm[i] + sm[i + 1]) / 3.0
            sm = nx
        }
        for (i in 0 until n) h[i] = if (i <= FLAT) 0.0 else sm[i]
        // düzlüğün bitişindeki küçük sıçramayı da sınır altında tut
        for (i in FLAT.toInt() + 1 until n) { val c = maxSlopeAt(i - 0.5); h[i] = h[i].coerceIn(h[i - 1] - c, h[i - 1] + c) }

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

    companion object {
        private fun ss(u: Double): Double { val c = u.coerceIn(0.0, 1.0); return c * c * (3 - 2 * c) }
        const val FLAT = 30.0
        const val GENTLE_END = 300.0
        const val GENTLE_SLOPE = 0.5
        const val HARD_FROM = 1500.0
        const val MAX_PIT_WALL = 0.8
        const val MAX_PIT_DEPTH = 1.2
        const val LAND_MAX = 0.45
        const val FLAT_BLEND = 18
    }
}
