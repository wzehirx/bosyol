package com.yokus.app.core

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class Wheel {
    var x = 0.0; var y = 0.0; var vx = 0.0; var vy = 0.0
    var spin = 0.0          // rad/s, + = ileri
    var grounded = false
    var compression = 0.0   // 0..1
    var slip = 0.0          // m/s
}

/**
 * Gövde + iki teker. Yarı-örtük Euler, 240 Hz alt adım.
 * Dünya: x ileri, y yukarı, açı saat yönünün tersi pozitif (burun yukarı +).
 */
class Car(val def: VehicleDef, val terrain: Terrain, val gravity: Double, val grip: Double = 1.0, startX: Double = 5.0) {
    var x = 0.0; var y = 0.0; var angle = 0.0; var vx = 0.0; var vy = 0.0; var omega = 0.0
    val rear = Wheel()
    val front = Wheel()
    var rpm = def.idleRpm
    var gas = 0.0
    var brake = 0.0
    /** Son step() çağrısı başından beri en büyük iniş hızı (m/s). */
    var landImpact = 0.0
    // ayar düğmeleri (araç başına değil, fizik genelinde)
    var angDampGround = 2.0   // 1/s: yerde gövde dönüşünü söndürür (süspansiyon/lastik)
    var angDampAir = 1.0      // 1/s: havada
    val airborne: Boolean get() = !rear.grounded && !front.grounded
    /** Gövde köşelerinden biri (alt köşeler ya da tavan) son adımda zemine değdi mi. */
    var hullTouch = false
        private set
    /** Gövdenin ileri yönündeki hız (m/s). */
    val speed: Double get() = vx * cos(angle) + vy * sin(angle)

    private val r = def.wheelR
    private val mw = def.wheelMass
    private val iw = 0.5 * def.wheelMass * def.wheelR * def.wheelR
    private val sMid = def.suspTravel / 2
    private val sFree = sMid + def.mass * gravity / 2 / def.suspK     // yayın gevşek boyu: dinlenme sıkışması orta yolculukta
    private val kp = def.suspK * 20
    private val cp = 0.7 * 2 * sqrt(kp * mw)
    private val spinMax = def.maxRpm * 2 * PI / 60 / def.ratio
    private val mu: Double = terrain.surface.mu *
        (if (terrain.surface == Surface.SNOW || terrain.surface == Surface.ICE) def.snowGrip else 1.0) * grip
    private val wheels = arrayOf(rear, front)
    // gövde temas noktaları (gövde koordinatı): iki alt köşe ve kafa hizasında iki tavan köşesi.
    // Tavan kafa merkezi hizasında olduğundan ters dönen araç tavanına oturur ve kafa yine yere değer (kaza algılanır).
    private val hullX = doubleArrayOf(def.rearX - 0.15, def.frontX + 0.15, def.rearX * 0.8, def.frontX * 0.8)
    private val hullY = doubleArrayOf(def.axleY, def.axleY, def.headY, def.headY)
    private val hullMu = 0.6 * terrain.surface.mu
    private val driveWheels = if (def.drive == Drive.ALL) arrayOf(rear, front) else arrayOf(rear)

    private var airTime = 0.0
    private var lx = 0.0; private var ly = 0.0; private var la = 0.0; private var lvx = 0.0; private var lvy = 0.0; private var lom = 0.0
    private val snap = DoubleArray(17)

    init {
        x = startX
        angle = 0.0
        val gy = terrain.height(startX)
        y = gy + r + sMid - def.axleY
        syncWheels()
        remember()
    }

    private fun remember() { lx = x; ly = y; la = angle; lvx = vx; lvy = vy; lom = omega }
    private fun externallyChanged() = x != lx || y != ly || angle != la || vx != lvx || vy != lvy || omega != lom

    /** Gövde durumu dışarıdan değiştirilirse tekerleri dinlenme konumuna getirir. */
    private fun syncWheels() {
        for (w in wheels) {
            val mx = if (w === rear) def.rearX else def.frontX
            val ca = cos(angle); val sa = sin(angle)
            val rx = ca * mx - sa * def.axleY; val ry = sa * mx + ca * def.axleY
            w.x = x + rx + sa * sMid; w.y = y + ry - ca * sMid
            val wrx = w.x - x; val wry = w.y - y
            w.vx = vx - omega * wry; w.vy = vy + omega * wrx
            w.grounded = false
        }
    }

    fun headWorld(): Pair<Double, Double> {
        val ca = cos(angle); val sa = sin(angle)
        return Pair(x + ca * def.headX - sa * def.headY, y + sa * def.headX + ca * def.headY)
    }

    fun headHit(): Boolean {
        val (hx, hy) = headWorld()
        return (hy - terrain.height(hx)) < def.headR
    }

    fun step(dt: Double) {
        landImpact = 0.0
        if (externallyChanged()) syncWheels()
        val n = max(1, ceil(dt * 240 - 1e-9).toInt())
        val h = dt / n
        for (i in 0 until n) {
            save()
            sub(h)
            if (!ok()) { restore(); zeroVel() }
        }
        for (w in wheels) {
            w.compression = (1 - suspExt(w) / def.suspTravel).coerceIn(0.0, 1.0)
        }
        remember()
    }

    // ---- NaN koruması ----
    private fun save() {
        snap[0] = x; snap[1] = y; snap[2] = angle; snap[3] = vx; snap[4] = vy; snap[5] = omega
        snap[6] = rear.x; snap[7] = rear.y; snap[8] = rear.vx; snap[9] = rear.vy; snap[10] = rear.spin
        snap[11] = front.x; snap[12] = front.y; snap[13] = front.vx; snap[14] = front.vy; snap[15] = front.spin
        snap[16] = rpm
    }
    private fun restore() {
        x = snap[0]; y = snap[1]; angle = snap[2]; vx = snap[3]; vy = snap[4]; omega = snap[5]
        rear.x = snap[6]; rear.y = snap[7]; rear.vx = snap[8]; rear.vy = snap[9]; rear.spin = snap[10]
        front.x = snap[11]; front.y = snap[12]; front.vx = snap[13]; front.vy = snap[14]; front.spin = snap[15]
        rpm = snap[16]
    }
    private fun zeroVel() {
        vx = 0.0; vy = 0.0; omega = 0.0
        for (w in wheels) { w.vx = 0.0; w.vy = 0.0; w.spin = 0.0 }
    }
    private fun ok(): Boolean {
        val sum = x + y + angle + vx + vy + omega + rpm + rear.x + rear.y + rear.vx + rear.vy + rear.spin +
            front.x + front.y + front.vx + front.vy + front.spin
        return !(sum.isNaN() || sum.isInfinite())
    }

    private fun mountX(w: Wheel) = if (w === rear) def.rearX else def.frontX

    private fun suspExt(w: Wheel): Double {
        val ca = cos(angle); val sa = sin(angle)
        val mx = mountX(w)
        val px = x + ca * mx - sa * def.axleY; val py = y + sa * mx + ca * def.axleY
        return (w.x - px) * sa + (w.y - py) * (-ca)
    }

    // ---- bir alt adım ----
    private fun sub(h: Double) {
        val m = def.mass
        val ca = cos(angle); val sa = sin(angle)
        var fbx = 0.0; var fby = -m * gravity; var tb = 0.0

        for (w in wheels) {
            val mx = mountX(w)
            val rx = ca * mx - sa * def.axleY; val ry = sa * mx + ca * def.axleY
            val px = x + rx; val py = y + ry
            val vpx = vx - omega * ry; val vpy = vy + omega * rx
            val dx = w.x - px; val dy = w.y - py
            val ax = sa; val ay = -ca                  // eksen: gövde -y
            val s = dx * ax + dy * ay
            val p = dx * ca + dy * sa
            val rvx = w.vx - vpx; val rvy = w.vy - vpy
            val vs = rvx * ax + rvy * ay
            val vp = rvx * ca + rvy * sa
            val fa = def.suspK * (sFree - s) - def.suspC * vs
            val fp = -kp * p - cp * vp
            val fx = ax * fa + ca * fp; val fy = ay * fa + sa * fp
            w.vx += fx / mw * h
            w.vy += (fy / mw - gravity) * h
            fbx -= fx; fby -= fy
            tb += rx * (-fy) - ry * (-fx)
        }

        // --- motor, fren, hava kontrolü ---
        val dw = driveWheels
        var spinDrive = 0.0
        for (w in dw) spinDrive += w.spin
        spinDrive /= dw.size
        val rawRpm = abs(spinDrive) * def.ratio * 60 / (2 * PI)
        rpm = rawRpm.coerceIn(def.idleRpm, def.maxRpm * 1.05)
        val shape = if (def.electric) 1.0 else 0.6 + 0.4 * sin(PI * min(rpm, def.maxRpm) / def.maxRpm)
        val cut = ((def.maxRpm - rawRpm) / (0.04 * def.maxRpm)).coerceIn(0.0, 1.0)
        val curve = def.torque * shape * cut
        val sp = speed
        var drive = gas * curve
        if (brake > 0 && sp < 0.5) drive -= 0.4 * brake * curve
        // motor freni (gaz bırakılınca)
        val drag = -ENGINE_DRAG * def.torque * (spinDrive / spinMax) * (1 - gas) -
            // devir sınırının üstünde motor freni (yokuş aşağı hız, en yüksek hızın biraz üstünde dengelenir)
            sign(spinDrive) * OVER_DRAG * def.torque * ((rawRpm / def.maxRpm - 1.05) / 0.2).coerceIn(0.0, 1.0)
        for (w in dw) {
            val tau = (drive + drag) / dw.size
            w.spin += tau / iw * h
            tb += tau
        }
        if (brake > 0 && sp >= 0.5) {
            val tbMax = BRAKE_MUL * def.torque * brake / 2
            for (w in wheels) {
                val tau = -sign(w.spin) * min(tbMax, abs(w.spin) * iw / h)
                w.spin += tau / iw * h
                tb += tau
            }
        }
        if (airborne) {
            // hava kontrolü: ivme (gaz-fren)·airTorque/I; dönüş hızı airTorque/I·AIR_RATE_T ile doyar (takla kontrolden çıkmasın).
            // Yetki havada geçen ilk AIR_RAMP saniyede sıfırdan tama çıkar: tümsekten küçük sekmeler gazı basılı tutanı döndürmez.
            airTime += h
            val u = (gas - brake) * min(1.0, airTime / AIR_RAMP)
            val lim = min(AIR_RATE_MAX, def.airTorque / def.inertia * AIR_RATE_T)
            if (u * omega < lim * abs(u)) tb += u * def.airTorque
        } else airTime = 0.0

        // hava direnci + yuvarlanma direnci (yokuş aşağı aşırı hızı sınırlar)
        val vv = sqrt(vx * vx + vy * vy)
        if (vv > 1e-6) {
            val fd = AIR_DRAG * vv * vv + (if (airborne) 0.0 else ROLL_RES * m * gravity)
            val fdc = min(fd, vv * m / h)
            fbx -= fdc * vx / vv; fby -= fdc * vy / vv
        }
        vx += fbx / m * h; vy += fby / m * h; omega += tb / def.inertia * h

        // --- açısal sönüm: yerde güçlü (süspansiyon/lastik), havada hafif ---
        omega *= 1.0 - (if (airborne) angDampAir else angDampGround) * h

        // --- konum ---
        x += vx * h; y += vy * h; angle += omega * h
        for (w in wheels) { w.x += w.vx * h; w.y += w.vy * h }

        // --- süspansiyon sert duruşu, zemin, sürtünme ---
        for (w in wheels) hardStop(w)
        for (w in wheels) contact(w)
        hullTouch = false
        for (k in 0 until 4) hull(hullX[k], hullY[k])

        // --- sınırlar ---
        omega = omega.coerceIn(-12.0, 12.0)
        clampV()
        val sm = spinMax * SPIN_OVER
        for (w in wheels) w.spin = w.spin.coerceIn(-sm, sm)
    }

    private fun sign(v: Double) = if (v > 0) 1.0 else if (v < 0) -1.0 else 0.0

    private fun clampV() {
        val v = sqrt(vx * vx + vy * vy)
        if (v > 60.0) { vx *= 60.0 / v; vy *= 60.0 / v }
        for (w in wheels) {
            val wv = sqrt(w.vx * w.vx + w.vy * w.vy)
            if (wv > 60.0) { w.vx *= 60.0 / wv; w.vy *= 60.0 / wv }
        }
    }

    private fun hardStop(w: Wheel) {
        val ca = cos(angle); val sa = sin(angle)
        val mx = mountX(w)
        val rx = ca * mx - sa * def.axleY; val ry = sa * mx + ca * def.axleY
        val ax = sa; val ay = -ca
        val s = (w.x - (x + rx)) * ax + (w.y - (y + ry)) * ay
        var dirx = 0.0; var diry = 0.0; var d = 0.0
        if (s < 0) { dirx = ax; diry = ay; d = -s }
        else if (s > def.suspTravel) { dirx = -ax; diry = -ay; d = s - def.suspTravel }
        else return
        val wi = 1.0 / mw
        val bi = 1.0 / def.mass
        val tot = wi + bi
        w.x += dirx * d * wi / tot; w.y += diry * d * wi / tot
        x -= dirx * d * bi / tot; y -= diry * d * bi / tot
        val vpx = vx - omega * ry; val vpy = vy + omega * rx
        val vr = (w.vx - vpx) * dirx + (w.vy - vpy) * diry
        if (vr < 0) {
            val rc = rx * diry - ry * dirx
            val j = -vr / (tot + rc * rc / def.inertia)
            w.vx += dirx * j * wi; w.vy += diry * j * wi
            vx -= dirx * j * bi; vy -= diry * j * bi
            omega -= rc * j / def.inertia
        }
    }

    /** Gövde noktasının zemine teması: dışarı it, içe doğru hızı sil (sekme yok), Coulomb sürtünmesi. */
    private fun hull(bx: Double, by: Double) {
        val ca = cos(angle); val sa = sin(angle)
        val rx = ca * bx - sa * by; val ry = sa * bx + ca * by
        val px = x + rx; val py = y + ry
        val g = terrain.height(px)
        if (py >= g) return
        val sl = terrain.slope(px)
        val len = sqrt(1 + sl * sl)
        val nx = -sl / len; val ny = 1 / len
        val pen = (g - py) * ny
        hullTouch = true
        x += nx * pen; y += ny * pen
        for (w in wheels) { w.x += nx * pen; w.y += ny * pen }
        val vpx = vx - omega * ry; val vpy = vy + omega * rx
        val vn = vpx * nx + vpy * ny
        if (vn >= 0) return
        val m = def.mass; val ii = def.inertia
        val rn = rx * ny - ry * nx
        val jn = -vn / (1 / m + rn * rn / ii)
        vx += nx * jn / m; vy += ny * jn / m; omega += rn * jn / ii
        val tx = ny; val ty = -nx
        val vt = (vx - omega * ry) * tx + (vy + omega * rx) * ty
        val rt = rx * ty - ry * tx
        val jt = (-vt / (1 / m + rt * rt / ii)).coerceIn(-hullMu * jn, hullMu * jn)
        vx += tx * jt / m; vy += ty * jt / m; omega += rt * jt / ii
    }

    // zemin teması için aday: nx, ny, penetrasyon
    private var cNx = 0.0; private var cNy = 1.0; private var cPen = 0.0

    private fun findContact(cx: Double, cy: Double) {
        cPen = 0.0
        val i0 = floor(cx - r).toInt() - 1
        val i1 = floor(cx + r).toInt() + 1
        for (i in i0..i1) {
            val h0 = terrain.height(i.toDouble()); val h1 = terrain.height(i + 1.0)
            val dy = h1 - h0
            val len = sqrt(1 + dy * dy)
            val nx = -dy / len; val ny = 1 / len
            val rx = cx - i; val ry = cy - h0
            val sd = rx * nx + ry * ny
            if (sd < r) {
                val t = (rx + dy * ry) / (len * len)
                val containing = cx >= i && cx <= i + 1
                val valid = if (containing) (sd < 0 && sd > -TUNNEL_MAX) || (t >= 0 && t <= 1 && sd > -TUNNEL_MAX)
                            else (t >= 0 && t <= 1 && sd >= 0)
                if (valid && r - sd > cPen) { cPen = r - sd; cNx = nx; cNy = ny }
            }
            // tepe noktası
            val vdx = cx - i; val vdy = cy - h0
            val dist = sqrt(vdx * vdx + vdy * vdy)
            if (dist < r && dist > 1e-9 && vdy >= 0 && r - dist > cPen) { cPen = r - dist; cNx = vdx / dist; cNy = vdy / dist }
        }
    }

    private fun contact(w: Wheel) {
        val was = w.grounded
        var jn = 0.0
        var got = false
        var fnx = 0.0; var fny = 1.0
        var worstVn = 0.0
        for (it in 0 until 3) {
            findContact(w.x, w.y)
            if (cPen <= 1e-9) break
            w.x += cNx * cPen; w.y += cNy * cPen
            val vn = w.vx * cNx + w.vy * cNy
            if (vn < 0) {
                jn += -vn * mw
                w.vx -= vn * cNx; w.vy -= vn * cNy
                if (-vn > worstVn) worstVn = -vn
            }
            if (!got) { got = true; fnx = cNx; fny = cNy }
        }
        w.grounded = got
        if (!got) { w.slip = 0.0; return }
        if (!was && worstVn > landImpact) landImpact = worstVn
        val tx = fny; val ty = -fnx
        val vt = w.vx * tx + w.vy * ty
        val slip = w.spin * r - vt
        val meff = 1.0 / (1.0 / mw + r * r / iw)
        val jmax = mu * jn
        val jt = (slip * meff).coerceIn(-jmax, jmax)
        w.vx += tx * jt / mw; w.vy += ty * jt / mw
        w.spin -= jt * r / iw
        w.slip = abs(slip)
    }

    companion object {
        const val AIR_RAMP = 0.5
        const val AIR_RATE_T = 0.15
        const val AIR_RATE_MAX = 4.0
        const val AIR_DRAG = 0.6       // N/(m/s)²
        const val ROLL_RES = 0.012
        const val ENGINE_DRAG = 0.15
        const val OVER_DRAG = 0.6
        const val BRAKE_MUL = 1.5
        const val SPIN_OVER = 1.6
        const val TUNNEL_MAX = 8.0
    }
}
