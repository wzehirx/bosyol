package com.bosyol.app.core

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

const val OUT_RATE = 48000
const val BUS_ENGINE = 0
const val BUS_WORLD = 1
const val BUS_CABIN = 2
const val BUS_FX = 3
const val BUS_COUNT = 4

/** 16 bit PCM ses. Mono ise r null. */
class Pcm(val l: ShortArray, val r: ShortArray?, val rate: Int) {
    val frames: Int get() = l.size
    val duration: Double get() = l.size.toDouble() / rate
}

/** Uç kısmı başına eşit güçte bindirilmiş, dikişsiz döngü. */
fun seamless(p: Pcm, sec: Double): Pcm {
    val len = min((p.rate * sec).toInt(), p.frames / 4)
    if (len < 32) return p
    fun one(x: ShortArray): ShortArray {
        val n = x.size - len
        val y = ShortArray(n)
        System.arraycopy(x, len, y, 0, n)
        for (i in 0 until len) {
            val t = i.toDouble() / len
            val v = x[x.size - len + i] * sqrt(1 - t) + x[i] * sqrt(t)
            y[n - len + i] = v.toInt().coerceIn(-32768, 32767).toShort()
        }
        return y
    }
    return Pcm(one(p.l), p.r?.let { one(it) }, p.rate)
}

object Noise {
    /** 0 beyaz, 1 pembe, 2 kahverengi gürültü */
    fun make(kind: Int, seconds: Double = 3.0): Pcm {
        val n = (OUT_RATE * seconds).toInt()
        val rnd = java.util.Random(7L + kind)
        val d = FloatArray(n)
        var last = 0.0; var b0 = 0.0; var b1 = 0.0; var b2 = 0.0
        for (i in 0 until n) {
            val w = rnd.nextDouble() * 2 - 1
            d[i] = when (kind) {
                2 -> { last = (last + 0.02 * w) / 1.02; (last * 3.5).toFloat() }
                1 -> { b0 = 0.997 * b0 + w * 0.029591; b1 = 0.985 * b1 + w * 0.032534; b2 = 0.95 * b2 + w * 0.048056; ((b0 + b1 + b2 + w * 0.05) * 2.2).toFloat() }
                else -> (w * 0.5).toFloat()
            }
        }
        val s = ShortArray(n) { (d[it].coerceIn(-1f, 1f) * 32000).toInt().toShort() }
        return seamless(Pcm(s, null, OUT_RATE), 0.05)
    }
}

/** Tek kutuplu alçak geçiren süzgeç */
class OnePole {
    var z = 0f
    var a = 1f
    fun setCutoff(fc: Float) { a = (1.0 - exp(-2.0 * PI * fc / OUT_RATE)).toFloat().coerceIn(0.0001f, 1f) }
    fun p(x: Float): Float { z += a * (x - z); return z }
}

/** RBJ biquad, iki kanal */
class Biquad(var type: Int, var freq: Float, var q: Float) {
    private var b0 = 1f; private var b1 = 0f; private var b2 = 0f; private var a1 = 0f; private var a2 = 0f
    private val s1 = FloatArray(2); private val s2 = FloatArray(2)
    private var curFreq = -1f; private var curQ = -1f

    fun update() {
        if (abs(freq - curFreq) < curFreq * 0.004f && q == curQ) return
        curFreq = freq; curQ = q
        val f = freq.coerceIn(20f, OUT_RATE * 0.45f)
        val w0 = 2.0 * PI * f / OUT_RATE
        val cw = cos(w0); val sw = sin(w0)
        val alpha = sw / (2.0 * q)
        val a0 = 1 + alpha
        val nb0: Double; val nb1: Double; val nb2: Double
        when (type) {
            LOWPASS -> { nb0 = (1 - cw) / 2; nb1 = 1 - cw; nb2 = (1 - cw) / 2 }
            HIGHPASS -> { nb0 = (1 + cw) / 2; nb1 = -(1 + cw); nb2 = (1 + cw) / 2 }
            else -> { nb0 = alpha; nb1 = 0.0; nb2 = -alpha }
        }
        b0 = (nb0 / a0).toFloat(); b1 = (nb1 / a0).toFloat(); b2 = (nb2 / a0).toFloat()
        a1 = (-2 * cw / a0).toFloat(); a2 = ((1 - alpha) / a0).toFloat()
    }

    fun p(x: Float, ch: Int): Float {
        val y = b0 * x + s1[ch]
        s1[ch] = b1 * x - a1 * y + s2[ch]
        s2[ch] = b2 * x - a2 * y
        return y
    }

    companion object { const val LOWPASS = 0; const val HIGHPASS = 1; const val BANDPASS = 2 }
}

abstract class Voice(val bus: Int) {
    @Volatile var dead = false
    @Volatile var gain = 0f
    private var gTarget = 0f
    private var gStep = 0f
    private var dieAtZero = false
    private var lifeLeft = -1
    private var lifeFade = 0.1f

    fun fadeTo(v: Float, sec: Float) {
        gTarget = v
        val n = max(1f, sec * OUT_RATE)
        gStep = (v - gain) / n
        if (gStep == 0f) gain = v
    }
    fun stop(sec: Float = 0.1f) { dieAtZero = true; fadeTo(0f, sec); if (gain <= 0f) dead = true }
    fun stopAfter(sec: Float, fade: Float) { lifeLeft = (sec * OUT_RATE).toInt(); lifeFade = fade }

    protected fun tickLife(n: Int) {
        if (lifeLeft >= 0) { lifeLeft -= n; if (lifeLeft < 0) stop(lifeFade) }
    }
    protected fun nextGain(): Float {
        if (gStep != 0f) {
            gain += gStep
            if ((gStep > 0 && gain >= gTarget) || (gStep < 0 && gain <= gTarget)) {
                gain = gTarget; gStep = 0f
                if (dieAtZero && gain <= 0f) dead = true
            }
        }
        return gain
    }
    abstract fun render(l: FloatArray, r: FloatArray, n: Int, mix: Mixer)
}

/** Dünyadaki bir noktadan gelen sesi iki kulağa böler: seviye farkı, varış süresi farkı, kafa gölgesi ve arka süzgeci. */
class Spatial(var ref: Float = 6f, var roll: Float = 1.2f) {
    @Volatile var x = 0.0
    @Volatile var y = 1.0
    @Volatile var z = 0.0
    @Volatile var air = true
    @Volatile var extraLp = 20000f
    private val ring = FloatArray(128)
    private var idx = 0
    private var gl = 0f; private var gr = 0f; private var dly = 0f
    private var tgl = 0f; private var tgr = 0f; private var tdly = 0f
    private var rightSide = true
    private val lpL = OnePole(); private val lpR = OnePole()
    private var dgl = 0f; private var dgr = 0f; private var ddly = 0f
    private var first = true

    fun prepare(mix: Mixer, n: Int) {
        val dx = x - mix.lx; val dz = z - mix.lz; val dy = y - 1.1
        val sh = sin(mix.lh); val ch = cos(mix.lh)
        val fwd = dx * sh - dz * ch
        val right = dx * ch + dz * sh
        val dist = sqrt(dx * dx + dz * dz + dy * dy)
        val az = atan2(right, fwd)
        val pan = sin(az).toFloat()
        val ap = abs(pan)
        val dd = max(dist, ref.toDouble())
        val distGain = (ref / (ref + roll * (dd - ref))).toFloat()
        val rear = if (fwd < 0) (-fwd / max(dist, 0.001)).toFloat() else 0f
        val rearGain = 1f - 0.15f * rear
        tgl = distGain * rearGain * sqrt(0.5f * (1f - 0.75f * pan))
        tgr = distGain * rearGain * sqrt(0.5f * (1f + 0.75f * pan))
        // Woodworth varış süresi farkı
        tdly = ((0.0875 / 343.0) * (asin(ap.toDouble().coerceAtMost(1.0)) + ap) * OUT_RATE).toFloat()
        rightSide = pan >= 0
        val airFc = if (air) (17000.0 * exp(-dist / 110.0)).toFloat().coerceIn(600f, 17000f) else 18000f
        val rearFc = 18000f - 11000f * rear
        val nearFc = min(min(airFc, rearFc), extraLp)
        val farFc = min(nearFc, 18000f - 14500f * ap)
        if (rightSide) { lpR.setCutoff(nearFc); lpL.setCutoff(farFc) } else { lpL.setCutoff(nearFc); lpR.setCutoff(farFc) }
        if (first) { gl = tgl; gr = tgr; dly = tdly; first = false }
        dgl = (tgl - gl) / n; dgr = (tgr - gr) / n; ddly = (tdly - dly) / n
    }

    fun process(x: Float, l: FloatArray, r: FloatArray, i: Int) {
        ring[idx] = x
        gl += dgl; gr += dgr; dly += ddly
        val rp = idx - dly
        val i0 = kotlin.math.floor(rp).toInt()
        val fr = rp - i0
        val a = ring[(i0 + 256) and 127]
        val b = ring[(i0 + 257) and 127]
        val far = a + (b - a) * fr
        idx = (idx + 1) and 127
        if (rightSide) { r[i] += lpR.p(x * gr); l[i] += lpL.p(far * gl) }
        else { l[i] += lpL.p(x * gl); r[i] += lpR.p(far * gr) }
    }
}

/**
 * Kayıt çalan ses. Döngü bölgesi [loopStart, loopEnd); xfade > 0 ise bölge sonu başına eşit güçte bindirilir.
 * Döngüsüz seste endFrame'e yaklaşınca fadeOut süresinde kısılır.
 */
class SampleVoice(
    val pcm: Pcm, bus: Int, val loop: Boolean,
    xfadeSec: Double = 0.0, startSec: Double = 0.0, durSec: Double = -1.0, fadeOutSec: Double = 0.4,
    loopStartSec: Double = 0.0, loopEndSec: Double = -1.0
) : Voice(bus) {
    @Volatile var rate = 1.0
    @Volatile var pan = 0f
    var spatial: Spatial? = null
    var filter: Biquad? = null
    private var rateCur = 1.0
    private val frames = pcm.frames
    private val ls = (loopStartSec * pcm.rate).coerceIn(0.0, frames - 2.0)
    private val le = if (loopEndSec > 0) (loopEndSec * pcm.rate).coerceIn(ls + 2, frames.toDouble()) else frames.toDouble()
    private val xf = min(xfadeSec * pcm.rate, (le - ls) / 4)
    private var pos = (startSec * pcm.rate).coerceIn(0.0, frames - 1.0)
    private val endF = if (durSec > 0) min(frames.toDouble(), pos + durSec * pcm.rate) else frames.toDouble()
    private val fadeF = fadeOutSec * pcm.rate
    private val baseStep = pcm.rate.toDouble() / OUT_RATE
    private var firstBlock = true

    private fun rd(a: ShortArray, p: Double): Float {
        val i = p.toInt()
        val f = (p - i).toFloat()
        val s0 = a[i.coerceIn(0, frames - 1)].toFloat()
        val j = i + 1
        val s1 = a[if (j < frames) j else if (loop) ls.toInt() else frames - 1].toFloat()
        return (s0 + (s1 - s0) * f) * (1f / 32768f)
    }

    override fun render(l: FloatArray, r: FloatArray, n: Int, mix: Mixer) {
        tickLife(n)
        if (firstBlock) { rateCur = rate; firstBlock = false } else rateCur += (rate - rateCur) * 0.3
        val step = rateCur * baseStep
        val f = filter
        f?.update()
        val sp = spatial
        sp?.prepare(mix, n)
        val stereo = pcm.r != null
        val rr = pcm.r ?: pcm.l
        val pl = cos((pan + 1) * PI / 4).toFloat(); val pr = sin((pan + 1) * PI / 4).toFloat()
        for (i in 0 until n) {
            if (!loop && pos >= endF - 1) { dead = true; return }
            var a = rd(pcm.l, pos)
            var b = if (stereo) rd(rr, pos) else a
            if (loop && xf > 0 && pos >= le - xf) {
                val w = ((pos - (le - xf)) / xf).toFloat().coerceIn(0f, 1f)
                val q = ls + (pos - (le - xf))
                val wa = sqrt(1f - w); val wb = sqrt(w)
                a = a * wa + rd(pcm.l, q) * wb
                b = if (stereo) b * wa + rd(rr, q) * wb else a
            }
            var g = nextGain()
            if (!loop && fadeF > 0 && pos > endF - fadeF) g *= ((endF - pos) / fadeF).toFloat().coerceIn(0f, 1f)
            if (f != null) { a = f.p(a, 0); b = if (stereo) f.p(b, 1) else a }
            if (sp != null) sp.process((if (stereo) (a + b) * 0.5f else a) * g, l, r, i)
            else if (stereo) { l[i] += a * g; r[i] += b * g }
            else { l[i] += a * g * pl; r[i] += a * g * pr }
            pos += step
            if (loop && pos >= le) pos -= if (xf > 0) (le - ls - xf) else (le - ls)
        }
    }
}

/** Basit osilatör (sinüs, üçgen, testere) */
class OscVoice(bus: Int, val wave: Int) : Voice(bus) {
    @Volatile var freq = 440f
    @Volatile var pan = 0f
    var filter: Biquad? = null
    private var fCur = -1f
    private var ph = 0.0
    override fun render(l: FloatArray, r: FloatArray, n: Int, mix: Mixer) {
        tickLife(n)
        if (fCur < 0) fCur = freq
        val f0 = fCur; val f1 = freq; fCur = f1
        filter?.update()
        val pl = cos((pan + 1) * PI / 4).toFloat(); val pr = sin((pan + 1) * PI / 4).toFloat()
        for (i in 0 until n) {
            val fr = f0 + (f1 - f0) * i / n
            ph += fr / OUT_RATE
            if (ph >= 1) ph -= kotlin.math.floor(ph)
            var s = when (wave) {
                1 -> (if (ph < 0.5) 4 * ph - 1 else 3 - 4 * ph).toFloat()
                2 -> (2 * ph - 1).toFloat()
                else -> sin(2 * PI * ph).toFloat()
            }
            filter?.let { s = it.p(s, 0) }
            val g = nextGain() * s
            l[i] += g * pl; r[i] += g * pr
        }
    }
    companion object { const val SINE = 0; const val TRI = 1; const val SAW = 2 }
}

/** Tüm sesleri karıştırır: ses yolları (motor, dış dünya, kabin, efekt), yol süzgeçleri, ana ses ve yumuşak sınırlayıcı. */
class Mixer {
    @Volatile var lx = 0.0
    @Volatile var lz = 0.0
    @Volatile var lh = 0.0
    @Volatile var master = 1f
    val busGain = FloatArray(BUS_COUNT) { 1f }
    val busLp = FloatArray(BUS_COUNT) { 20000f }
    private val busGainCur = FloatArray(BUS_COUNT) { 1f }
    private val busFilt = Array(BUS_COUNT) { Biquad(Biquad.LOWPASS, 20000f, 0.6f) }
    private var masterCur = 0f
    private val bl = Array(BUS_COUNT) { FloatArray(1024) }
    private val br = Array(BUS_COUNT) { FloatArray(1024) }
    private val voices = ArrayList<Voice>()
    private val pending = ArrayList<Voice>()

    fun <T : Voice> add(v: T): T { synchronized(pending) { pending.add(v) }; return v }

    fun stopAll(fade: Float) {
        synchronized(pending) { pending.forEach { it.stop(fade) }; synchronized(voices) { voices.forEach { it.stop(fade) } } }
    }

    val voiceCount: Int get() = voices.size

    /** n kare, çıkış iç içe stereo */
    fun render(out: FloatArray, n: Int) {
        synchronized(pending) { synchronized(voices) { voices.addAll(pending) }; pending.clear() }
        for (b in 0 until BUS_COUNT) { java.util.Arrays.fill(bl[b], 0, n, 0f); java.util.Arrays.fill(br[b], 0, n, 0f) }
        synchronized(voices) {
            var w = 0
            for (k in voices.indices) {
                val v = voices[k]
                if (!v.dead) v.render(bl[v.bus], br[v.bus], n, this)
                if (!v.dead) voices[w++] = v
            }
            while (voices.size > w) voices.removeAt(voices.size - 1)
        }
        java.util.Arrays.fill(out, 0, n * 2, 0f)
        for (b in 0 until BUS_COUNT) {
            val g0 = busGainCur[b]; val g1 = busGain[b]; busGainCur[b] = g1
            val filt = busFilt[b]
            val useF = busLp[b] < 19000f
            if (useF) { filt.freq = filt.freq + (busLp[b] - filt.freq) * 0.08f; filt.update() } else filt.freq = 19000f
            val L = bl[b]; val R = br[b]
            for (i in 0 until n) {
                val g = g0 + (g1 - g0) * i / n
                var a = L[i]; var c = R[i]
                if (useF) { a = filt.p(a, 0); c = filt.p(c, 1) }
                out[2 * i] += a * g; out[2 * i + 1] += c * g
            }
        }
        val m0 = masterCur; val m1 = master; masterCur = m0 + (m1 - m0) * 0.2f
        for (i in 0 until n) {
            val g = m0 + (masterCur - m0) * i / n
            out[2 * i] = soft(out[2 * i] * g); out[2 * i + 1] = soft(out[2 * i + 1] * g)
        }
    }

    private fun soft(x: Float): Float {
        val y = x.coerceIn(-3f, 3f)
        return y * (27f + y * y) / (27f + 9f * y * y)
    }
}
