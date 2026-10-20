package com.bosyol.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.view.View
import com.bosyol.app.core.EnvDef
import com.bosyol.app.core.Game
import com.bosyol.app.core.RoadPoint
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/** Gerçek perspektifle çizilen hafif yol görüntüsü. Erişilebilirlik ağacında gizlidir. */
class RoadView(ctx: Context) : View(ctx) {
    @Volatile var game: Game? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rp = RoadPoint()
    private val cam = RoadPoint()
    private var shaderEnv: EnvDef? = null
    private var skyShader: Shader? = null
    private var groundShader: Shader? = null
    private var shaderH = 0

    // Kamera
    private var cx = 0.0; private var cz = 0.0; private var cy = 2.5; private var sh = 0.0; private var ch = 1.0
    private var fpx = 1f; private var hor = 0f; private var halfW = 0f
    private var fogNear = 40.0; private var fogFar = 300.0; private var fogCol = 0

    private val MAXR = 120
    private val rowD = DoubleArray(MAXR)
    private val pX = Array(8) { FloatArray(MAXR) }
    private val pY = Array(8) { FloatArray(MAXR) }
    private val pZ = Array(8) { FloatArray(MAXR) }
    private val rowS = DoubleArray(MAXR)
    private val rowZc = FloatArray(MAXR)
    private val rowYc = FloatArray(MAXR)
    private val stars = FloatArray(240) { Math.random().toFloat() }
    private val rain = FloatArray(400) { Math.random().toFloat() }
    private val rect = RectF()
    private val lats = DoubleArray(8)
    private var lastFrame = 0L

    init { importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS }

    private fun project(x: Double, y: Double, z: Double, i: Int, slot: Int) {
        val dx = x - cx; val dz = z - cz
        val zc = dx * sh - dz * ch
        val xc = dx * ch + dz * sh
        val zz = if (zc < 0.2) 0.2 else zc
        pX[slot][i] = (halfW + fpx * xc / zz).toFloat()
        pY[slot][i] = (hor - fpx * (y - cy) / zz).toFloat()
        pZ[slot][i] = zc.toFloat()
    }

    private fun fog(col: Int, z: Float): Int {
        val t = ((z - fogNear) / (fogFar - fogNear)).toFloat().coerceIn(0f, 1f)
        return mixColor(col, fogCol, t)
    }

    override fun onDraw(c: Canvas) {
        val g = game
        if (g == null) { c.drawColor(0xFF12161B.toInt()); return }
        val env = g.env
        val road = g.road
        val w = width.toFloat(); val h = height.toFloat()
        halfW = w / 2; hor = h * 0.42f; fpx = h * 1.05f
        fogNear = env.fogNear; fogFar = env.fogFar; fogCol = env.skyLow
        val now = System.nanoTime(); val dt = if (lastFrame == 0L) 0.016f else ((now - lastFrame) / 1e9f).coerceAtMost(0.05f); lastFrame = now

        // Kamera aracın arkasında
        val cs = g.s
        road.at(cs - 7.0, g.lane * 0.85, cam)
        road.at(cs + 4.0, g.lane * 0.6, rp)
        val hd = Math.atan2(rp.x - cam.x, -(rp.z - cam.z))
        cx = cam.x; cz = cam.z; sh = sin(hd); ch = cos(hd)

        if (shaderEnv !== env || shaderH != height) {
            shaderEnv = env; shaderH = height
            skyShader = LinearGradient(0f, 0f, 0f, hor, env.sky, env.skyLow, Shader.TileMode.CLAMP)
            groundShader = LinearGradient(0f, hor, 0f, h, env.skyLow, env.ground, Shader.TileMode.CLAMP)
        }
        paint.style = Paint.Style.FILL
        paint.shader = skyShader; c.drawRect(0f, 0f, w, hor + 2, paint)
        paint.shader = groundShader; c.drawRect(0f, hor, w, h, paint)
        paint.shader = null

        if (env.night) {
            paint.color = 0xCCE8ECFF.toInt()
            for (i in 0 until stars.size / 2) c.drawCircle(stars[2 * i] * w, stars[2 * i + 1] * hor * 0.95f, 1.6f, paint)
        } else if (!env.wet) {
            paint.color = 0x55FFF6DA; c.drawCircle(w * 0.78f, hor * 0.35f, h * 0.07f, paint)
            paint.color = 0xFFFFF4D2.toInt(); c.drawCircle(w * 0.78f, hor * 0.35f, h * 0.04f, paint)
        }

        // Satırlar: yakından uzağa
        val W = env.width
        lats[0] = -W; lats[1] = W; lats[2] = -W + 0.12; lats[3] = W - 0.12; lats[4] = -0.08; lats[5] = 0.08; lats[6] = W + 14; lats[7] = W + 600
        var n = 0
        var d = -5.0
        while (d <= 330 && n < MAXR) {
            val s = cs + d
            road.at(s, 0.0, rp)
            val rx = cos(rp.h); val rz = sin(rp.h)
            for (k in 0 until 8) project(rp.x + rx * lats[k], 0.0, rp.z + rz * lats[k], n, k)
            rowS[n] = s; rowD[n] = d
            // merkez satır yüksekliği
            val dx = rp.x - cx; val dz = rp.z - cz
            val zc = dx * sh - dz * ch
            rowZc[n] = zc.toFloat()
            rowYc[n] = (hor + fpx * cy / (if (zc < 0.2) 0.2 else zc)).toFloat()
            n++
            d += if (d < 60) 2.0 else if (d < 160) 4.0 else 8.0
        }

        val asphalt = if (env.wet) 0xFF2E3238.toInt() else 0xFF3C4047.toInt()
        val asphalt2 = if (env.wet) 0xFF33373D.toInt() else 0xFF42464D.toInt()
        for (i in n - 2 downTo 0) {
            val zf = rowZc[i + 1]; val zn = rowZc[i]
            if (zf < 0.5f) continue
            val zm = (zf + zn) / 2
            val band = (floor(rowS[i] / 8.0).toInt() and 1) == 0
            // zemin bandı
            paint.color = fog(if (band) env.ground else env.ground2, zm)
            val yTop = rowYc[i + 1].coerceAtLeast(hor); val yBot = rowYc[i]
            if (yBot > yTop) c.drawRect(0f, yTop, w, yBot + 1, paint)
            if (env.sea) quad(c, i, 6, 7, fog(0xFF2B7FA6.toInt(), zm))
            // yol
            quad(c, i, 0, 1, fog(if (band) asphalt else asphalt2, zm))
            // kenar çizgileri
            quad(c, i, 0, 2, fog(0xFFE9E9E2.toInt(), zm))
            quad(c, i, 3, 1, fog(0xFFE9E9E2.toInt(), zm))
            if ((floor(rowS[i] / 6.0).toInt() and 1) == 0) quad(c, i, 4, 5, fog(0xFFF1F1EA.toInt(), zm))
        }

        drawProps(c, g, cs, w)
        drawTraffic(c, g, cs, w)
        drawPlayer(c, g, cs)

        if (env.wet) {
            paint.color = 0x88C8D4E0.toInt(); paint.strokeWidth = 2f
            for (i in 0 until rain.size / 2) {
                rain[2 * i + 1] += dt * 1.6f
                if (rain[2 * i + 1] > 1f) { rain[2 * i + 1] -= 1f; rain[2 * i] = Math.random().toFloat() }
                val x = rain[2 * i] * w; val y = rain[2 * i + 1] * h
                c.drawLine(x, y, x - 4, y + h * 0.04f, paint)
            }
        }
        postInvalidateOnAnimation()
    }

    private fun quad(c: Canvas, i: Int, a: Int, b: Int, col: Int) {
        path.reset()
        path.moveTo(pX[a][i], pY[a][i]); path.lineTo(pX[b][i], pY[b][i])
        path.lineTo(pX[b][i + 1], pY[b][i + 1]); path.lineTo(pX[a][i + 1], pY[a][i + 1]); path.close()
        paint.color = col
        c.drawPath(path, paint)
    }

    private fun hash(i: Long, salt: Long): Double {
        var x = i * -7046029254386353131L + salt * 0x632BE59BD9B4E019L
        x = x xor (x ushr 31); x *= -4658895280553007687L; x = x xor (x ushr 29)
        return (x ushr 11).toDouble() / (1L shl 53).toDouble()
    }

    private val sx = FloatArray(1); private val sy = FloatArray(1); private val sz = FloatArray(1)
    private fun proj1(x: Double, y: Double, z: Double): Boolean {
        val dx = x - cx; val dz = z - cz
        val zc = dx * sh - dz * ch
        if (zc < 1.0) return false
        val xc = dx * ch + dz * sh
        sx[0] = (halfW + fpx * xc / zc).toFloat(); sy[0] = (hor - fpx * (y - cy) / zc).toFloat(); sz[0] = zc.toFloat()
        return true
    }

    private fun drawProps(c: Canvas, g: Game, cs: Double, w: Float) {
        val env = g.env; val W = env.width
        val sp = 9.0
        val i0 = floor((cs - 3) / sp).toLong(); val i1 = floor((cs + 300) / sp).toLong()
        val q = RoadPoint()
        for (i in i1 downTo i0) {
            for (side in 0..1) {
                val r1 = hash(i, side * 7L + 1); val r2 = hash(i, side * 7L + 2); val r3 = hash(i, side * 7L + 3)
                var sgn = if (side == 0) -1 else 1
                var type: Int; var lat: Double
                when (env.props) {
                    "city" -> { type = if (i % 3 == 0L) 4 else 3; lat = if (type == 4) W + 1.5 else W + 6 + r2 * 14 }
                    "highway" -> { if (side == 0) { if (i % 3 != 0L) continue; type = 4; lat = W + 1.5; sgn = 1 } else { if (r1 > 0.5) continue; type = 0; lat = 18 + r2 * 50; sgn = if (r3 > 0.5) 1 else -1 } }
                    "coast" -> { if (side == 1 || r1 > 0.6) continue; type = if (r2 > 0.6) 0 else 2; lat = W + 4 + r3 * 50 }
                    "pines" -> { if (r1 > 0.8) continue; type = if (r2 > 0.85) 2 else 1; lat = W + 4 + r3 * 60 }
                    else -> { if (r1 > 0.75) continue; type = if (r2 > 0.88) 2 else 0; lat = W + 4 + r3 * 60 }
                }
                val s = i * sp + r1 * 4
                g.road.at(s, sgn * lat, q)
                if (!proj1(q.x, 0.0, q.z)) continue
                val px = sx[0]; val py = sy[0]; val z = sz[0]
                val m = fpx / z
                if (px < -w * 0.5f || px > w * 1.5f) continue
                val sc = 0.75 + r2 * 0.8
                when (type) {
                    0 -> { // ağaç
                        paint.color = fog(0xFF4A3526.toInt(), z); rect.set(px - 0.25f * m, py - 2f * m, px + 0.25f * m, py); c.drawRect(rect, paint)
                        tri(c, px, py - 1.6f * m, (2.2f * sc * m).toFloat(), (5.5f * sc * m).toFloat(), fog(if (r3 > 0.5) 0xFF2F5A2A.toInt() else 0xFF3B6B30.toInt(), z))
                    }
                    1 -> { // çam, karlı uç
                        tri(c, px, py, (1.6f * sc * m).toFloat(), (7.5f * sc * m).toFloat(), fog(0xFF24402F.toInt(), z))
                        tri(c, px, py - (5.2f * sc * m).toFloat(), (0.5f * sc * m).toFloat(), (2.3f * sc * m).toFloat(), fog(0xFFF4F7FA.toInt(), z))
                    }
                    2 -> { paint.color = fog(0xFF7C7A75.toInt(), z); rect.set(px - 1.3f * m, py - 1.2f * m, px + 1.3f * m, py + 0.1f * m); c.drawOval(rect, paint) }
                    3 -> { // bina
                        val bw = ((8 + r3 * 8) * m).toFloat(); val bh = ((8 + r2 * 30) * m).toFloat()
                        paint.color = fog(BUILD[(r3 * BUILD.size).toInt().coerceAtMost(BUILD.size - 1)], z)
                        rect.set(px - bw / 2, py - bh, px + bw / 2, py); c.drawRect(rect, paint)
                    }
                    4 -> { // aydınlatma direği
                        paint.color = fog(0xFF9AA0A6.toInt(), z); paint.strokeWidth = (0.15f * m).coerceAtLeast(1f)
                        c.drawLine(px, py, px, py - 8f * m, paint)
                        paint.color = fog(0xFFFFE2A0.toInt(), z); rect.set(px - 0.7f * m, py - 8.2f * m, px + 0.2f * m, py - 7.9f * m); c.drawRect(rect, paint)
                    }
                }
            }
        }
    }

    private fun tri(c: Canvas, x: Float, base: Float, halfWidth: Float, height: Float, col: Int) {
        path.reset(); path.moveTo(x - halfWidth, base); path.lineTo(x + halfWidth, base); path.lineTo(x, base - height); path.close()
        paint.color = col; c.drawPath(path, paint)
    }

    private fun drawTraffic(c: Canvas, g: Game, cs: Double, w: Float) {
        val cars = g.trafficView.sortedByDescending { it.s - cs }
        val q = RoadPoint()
        for (car in cars) {
            val d = car.s - cs
            if (d < -6 || d > 320) continue
            g.road.at(car.s, car.lat, q)
            if (!proj1(q.x, 0.0, q.z)) continue
            drawCarSprite(c, sx[0], sy[0], fpx / sz[0], car.color, oncoming = car.v < 0, braking = false, z = sz[0], siren = car.siren)
        }
    }

    private fun drawPlayer(c: Canvas, g: Game, cs: Double) {
        val q = RoadPoint()
        g.road.at(cs, g.lane, q)
        if (!proj1(q.x, 0.0, q.z)) return
        drawCarSprite(c, sx[0], sy[0], fpx / sz[0], g.vehicle.color, oncoming = false, braking = g.brakeOn || g.brk > 0.1, z = 0f, siren = false)
    }

    private fun drawCarSprite(c: Canvas, x: Float, y: Float, m: Float, color: Int, oncoming: Boolean, braking: Boolean, z: Float, siren: Boolean) {
        val bw = 1.85f * m; val bh = 0.75f * m
        paint.color = fog(0xFF15171A.toInt(), z)
        rect.set(x - bw / 2 + 0.1f * m, y - 0.38f * m, x - bw / 2 + 0.5f * m, y); c.drawRect(rect, paint)
        rect.set(x + bw / 2 - 0.5f * m, y - 0.38f * m, x + bw / 2 - 0.1f * m, y); c.drawRect(rect, paint)
        paint.color = fog(color, z)
        rect.set(x - bw / 2, y - 0.3f * m - bh, x + bw / 2, y - 0.28f * m); c.drawRoundRect(rect, 0.15f * m, 0.15f * m, paint)
        paint.color = fog(0xFF223344.toInt(), z)
        rect.set(x - bw * 0.38f, y - 0.3f * m - bh - 0.5f * m, x + bw * 0.38f, y - 0.3f * m - bh + 0.05f * m); c.drawRoundRect(rect, 0.12f * m, 0.12f * m, paint)
        val ly = y - 0.3f * m - bh * 0.62f
        if (oncoming) {
            paint.color = 0xFFFFF6D8.toInt()
            c.drawCircle(x - bw * 0.36f, ly, 0.13f * m, paint); c.drawCircle(x + bw * 0.36f, ly, 0.13f * m, paint)
        } else {
            paint.color = if (braking) 0xFFFF2A1A.toInt() else 0xFF8A1A14.toInt()
            rect.set(x - bw * 0.48f, ly - 0.07f * m, x - bw * 0.26f, ly + 0.07f * m); c.drawRect(rect, paint)
            rect.set(x + bw * 0.26f, ly - 0.07f * m, x + bw * 0.48f, ly + 0.07f * m); c.drawRect(rect, paint)
            if (braking) { paint.color = 0x55FF2A1A; c.drawCircle(x - bw * 0.37f, ly, 0.35f * m, paint); c.drawCircle(x + bw * 0.37f, ly, 0.35f * m, paint) }
        }
        if (siren) {
            paint.color = if ((System.currentTimeMillis() / 250) % 2 == 0L) 0xFFFF3030.toInt() else 0xFF3060FF.toInt()
            rect.set(x - 0.3f * m, y - 0.3f * m - bh - 0.7f * m, x + 0.3f * m, y - 0.3f * m - bh - 0.5f * m); c.drawRect(rect, paint)
        }
    }

    companion object {
        val BUILD = intArrayOf(0xFF8D8A86.toInt(), 0xFFB39B7F.toInt(), 0xFF6F7A86.toInt(), 0xFFA5AAB0.toInt(), 0xFF7D6B5D.toInt())
        fun mixColor(a: Int, b: Int, t: Float): Int {
            val ar = (a shr 16) and 255; val ag = (a shr 8) and 255; val ab = a and 255
            val br = (b shr 16) and 255; val bg = (b shr 8) and 255; val bb = b and 255
            return (0xFF shl 24) or ((ar + (br - ar) * t).toInt() shl 16) or ((ag + (bg - ag) * t).toInt() shl 8) or (ab + (bb - ab) * t).toInt()
        }
    }
}
