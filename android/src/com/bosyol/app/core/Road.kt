package com.bosyol.app.core

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

class Curve(val s0: Double, val s1: Double, val k: Double) { @Volatile var noted = false }

class RoadPoint { var x = 0.0; var z = 0.0; var h = 0.0; var k = 0.0 }

/** Sonsuz yol: eğrilik bölümlerinden adım adım üretilir. */
class Road(var env: EnvDef) {
    val step = 2.0
    private class Arr(val x: DoubleArray, val z: DoubleArray, val h: DoubleArray, val k: DoubleArray)
    @Volatile private var a = Arr(DoubleArray(8192), DoubleArray(8192), DoubleArray(8192), DoubleArray(8192))
    @Volatile var count = 1
        private set
    val curves = ArrayList<Curve>()
    var curveIdx = 0
    private var segK = 0.0
    private var segLeft = 0.0
    private var hasSeg = false
    private var kCur = 0.0
    private val rnd = java.util.Random()

    private fun rand(lo: Double, hi: Double) = lo + rnd.nextDouble() * (hi - lo)

    private fun nextSegment() {
        val curve = { dir: Double ->
            val r = rand(env.curveMin, env.curveMax)
            segK = dir / r; segLeft = (rand(0.4, 1.4) * r).coerceIn(50.0, 420.0)
        }
        if (!hasSeg) { segK = 0.0; segLeft = 200.0; hasSeg = true }
        else if (segK == 0.0) { if (rnd.nextDouble() < 0.8) curve(if (rnd.nextBoolean()) -1.0 else 1.0) else { segK = 0.0; segLeft = rand(150.0, 500.0) } }
        else if (rnd.nextDouble() < 0.25) curve(-sign(segK))
        else { segK = 0.0; segLeft = rand(120.0, 600.0) }
        if (segK != 0.0) synchronized(curves) { curves.add(Curve((count - 1) * step, (count - 1) * step + segLeft, segK)) }
    }

    private fun extend() {
        if (segLeft <= 0) nextSegment()
        segLeft -= step
        kCur += (segK - kCur) * 0.09
        var arr = a
        if (count >= arr.x.size) {
            val n = arr.x.size * 2
            arr = Arr(arr.x.copyOf(n), arr.z.copyOf(n), arr.h.copyOf(n), arr.k.copyOf(n)); a = arr
        }
        val i = count - 1
        val h = arr.h[i] + kCur * step
        arr.h[i + 1] = h; arr.k[i + 1] = kCur
        arr.x[i + 1] = arr.x[i] + sin(h) * step
        arr.z[i + 1] = arr.z[i] - cos(h) * step
        count++
    }

    @Synchronized fun ensure(s: Double) { while ((count - 1) * step < s) extend() }

    fun at(sIn: Double, lat: Double, out: RoadPoint): RoadPoint {
        val s = if (sIn < 0) 0.0 else sIn
        if ((count - 1) * step < s + step * 2) ensure(s + step * 2)
        val arr = a
        val f = s / step
        val i = f.toInt().coerceAtMost(count - 2)
        val t = f - i
        val h = arr.h[i] + (arr.h[i + 1] - arr.h[i]) * t
        out.h = h; out.k = arr.k[i] + (arr.k[i + 1] - arr.k[i]) * t
        out.x = arr.x[i] + (arr.x[i + 1] - arr.x[i]) * t + cos(h) * lat
        out.z = arr.z[i] + (arr.z[i + 1] - arr.z[i]) * t + sin(h) * lat
        return out
    }

    fun curvature(s: Double): Double {
        val ss = if (s < 0) 0.0 else s
        if ((count - 1) * step < ss + 4) ensure(ss + 4)
        return a.k[(ss / step).toInt().coerceAtMost(count - 1)]
    }

    /** Önümüzdeki virajlara göre güvenli hız (m/s): yanal ivme sınırı aLat ve frenleme ivmesi aBrake ile */
    fun safeSpeed(s: Double, v: Double, aLat: Double, aBrake: Double): Double {
        val look = maxOf(60.0, v * v / (2 * aBrake) + 40)
        var lim = 1e9
        var d = 0.0
        while (d <= look) {
            val k = abs(curvature(s + d))
            if (k > 1e-4) {
                val vl = kotlin.math.sqrt(aLat / k)
                lim = minOf(lim, kotlin.math.sqrt(vl * vl + 2 * aBrake * d))
            }
            d += 5.0
        }
        return lim
    }
}
