import com.yokus.app.core.*
import java.util.ArrayDeque
import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.roundToInt

/**
 * "Gözsüz insan oyuncu" sürücüsü. Yalnızca bir kör oyuncunun sesle duyabileceği şeylere bakar:
 *  - eğim tonu / takla bipi: burnun açısı (tiltDeg, tam derece, gürültülü)
 *  - yol radarı: 10-15 m ilerideki zemin eğimi (0.4 sn'de bir tık, gürültülü)
 *  - duyurular: Sonify'ın speak geri çağrısından gelen metinler ([hear])
 *  - havada mı (rüzgâr sesi), yakıt uyarıları (duyurulardan), hız (motor perdesi)
 * Tepki gecikmesi 0.25 sn: kararlar 0.25 sn öncesinin gözlemine göre verilir. Zemin konumu doğrudan okunmaz.
 *
 * Strateji (HCR tarzı): tam gaz; burun zemin eğiminden 35° fazla kalkarsa gazı kes; havada burnu radardan
 * öğrenilen iniş eğimine yatır; takılırsa kısa süre geri git ve yeniden dene.
 * [novice] = true: yalnızca takla bipi (>70°) çalınca gazı keser, havada hiçbir şey yapmaz.
 */
class BlindDriver(val s: GameSession, val novice: Boolean = false, seed: Long = 0L,
                  val easeDeg: Double = 35.0, val reaction: Double = 0.25) {
    var gas = 0.0
    var brake = 0.0

    private val rnd = Random(seed * 7919 + 17)
    private class Obs(val t: Double, val tilt: Double, val air: Boolean, val speed: Double, val odo: Double)
    private class Radar(val odo: Double, val slope: Double)
    private val hist = ArrayDeque<Obs>()
    private val radar = ArrayList<Radar>()
    private var nextRadar = 0.0
    private var odo = 0.0
    private var clock = 0.0
    private var lastTilt = 0.0
    private var tiltRate = 0.0

    // duyurular ve yakıt uyarıları
    val heard = ArrayList<Pair<Double, String>>()
    var fuelWarn = 100                      // son duyulan yakıt uyarısı (yüzde 25 / 10), yenilenince 100
    private var hintAt = -1e9

    // takılma / geri gitme durumu
    private var slowFor = 0.0
    private var reverseUntil = -1.0
    private var attempts = 0
    private var fastFor = 0.0
    private var easing = false
    private var airFor = 0.0
    var reversals = 0
        private set
    // insan değişkenliği: başlangıç gecikmesi, rastgele gaz kesme, eşik oynaması
    private val startDelay = rnd.nextDouble() * 1.5
    private var lift = 0.0
    private var easeJit = 0.0

    /** Sonify'ın speak geri çağrısı buraya bağlanır. */
    fun hear(t: Double, text: String) {
        heard.add(Pair(t, text))
        if (text.startsWith("Yakıt doldu")) fuelWarn = 100
        else if (text.startsWith("Yakıt yüzde")) fuelWarn = text.filter { it.isDigit() }.toIntOrNull() ?: fuelWarn
        else if (text.contains("çıkamıyorsun")) hintAt = t
    }

    private fun wrapDeg(d: Double): Double { var a = d % 360.0; if (a > 180) a -= 360.0 else if (a < -180) a += 360.0; return a }
    private fun g(sigma: Double) = rnd.nextGaussian() * sigma

    /** dt saniyelik bir adım için gaz ve fren kararı verir ([gas], [brake] alanlarına yazar). */
    fun tick(dt: Double) {
        clock += dt
        val car = s.car
        // --- algı (gürültülü) ---
        val speedTrue = car.speed
        odo += speedTrue * dt
        val tilt = wrapDeg(s.tiltDeg().toDouble()) + g(1.5)
        val obsNow = Obs(clock, tilt, car.airborne, speedTrue + g(0.3), odo)
        hist.addLast(obsNow)
        while (hist.size > 1 && hist.peekFirst().t < clock - reaction - 0.5) hist.removeFirst()
        // radar: 0.4 sn'de bir, 10-15 m ilerideki eğim
        if (clock >= nextRadar) {
            nextRadar = clock + 0.4
            val x = car.x
            val sl = ((s.terrain.height(x + 15.0) - s.terrain.height(x + 10.0)) / 5.0).coerceIn(-1.5, 1.5) + g(0.04)
            radar.add(Radar(odo, sl))
            if (radar.size > 60) radar.removeAt(0)
        }
        // 0.25 sn öncesinin gözlemi
        var o = hist.peekFirst()
        for (h in hist) if (h.t <= clock - reaction) o = h
        val tiltD = o.tilt
        tiltRate = tiltRate * 0.8 + 0.2 * (tiltD - lastTilt) / dt
        lastTilt = tiltD
        if (o.air) airFor += dt else airFor = 0.0
        val inAir = airFor > 0.15

        // yerdeki eğim: radarın şimdi geçtiğimiz noktaya ait eski okuması (yaklaşık 12.5 m önce okunmuş)
        var groundSlope = radar.lastOrNull()?.slope ?: 0.0
        run {
            var best = Double.MAX_VALUE
            for (r in radar) { val e = abs(r.odo + 12.5 - o.odo); if (e < best) { best = e; groundSlope = r.slope } }
            if (best > 8.0) groundSlope = radar.lastOrNull()?.slope ?: 0.0
        }
        val groundDeg = Math.toDegrees(atan(groundSlope))
        val ahead = radar.lastOrNull()?.slope ?: 0.0
        val aheadDeg = Math.toDegrees(atan(ahead))

        // --- karar ---
        var gs = 1.0; var br = 0.0
        if (lift > 0) lift -= dt else if (rnd.nextDouble() < dt / 30.0) lift = 0.3 + rnd.nextDouble() * 0.7
        val fuelEmpty = s.fuelPct() <= 0
        if (inAir) {
            slowFor = 0.0
            if (novice) { gs = 0.0 }
            else {
                // burnu iniş eğimine yatır (PD). Gaz burnu kaldırır, fren indirir.
                val err = Math.toRadians(tiltD - aheadDeg)
                if (abs(err) < Math.toRadians(8.0) && abs(tiltRate) < 30) { gs = 0.0 }
                else {
                    val u = (-(2.5 * err + 0.5 * Math.toRadians(tiltRate))).coerceIn(-1.0, 1.0)
                    gs = if (u > 0) u else 0.0; br = if (u < 0) -u else 0.0
                }
            }
        } else if (clock < reverseUntil) {
            gs = 0.0; br = 1.0                                      // geri git
        } else {
            val rel = if (novice) abs(tiltD) else tiltD - groundDeg
            val hi = (if (novice) 70.0 else easeDeg) + easeJit
            if (rel > hi) { if (!easing) easeJit = g(4.0).coerceIn(-8.0, 8.0); easing = true } else if (rel < hi - 6.0) easing = false
            gs = if (easing || lift > 0 || clock < startDelay) 0.0 else 1.0
            // takılma: gaz verilirken ve yakıt varken ~1.5 sn neredeyse duruyorsa kısa geri git
            val hint = clock - hintAt < 1.0
            if (abs(o.speed) < 0.6 && !fuelEmpty) slowFor += dt else slowFor = 0.0
            if (o.speed > 3.0) fastFor += dt else fastFor = 0.0
            if (fastFor > 3.0) attempts = 0
            if (slowFor > 1.5 || (hint && slowFor > 0.5)) {
                attempts++; reversals++
                reverseUntil = clock + minOf(4.0, 0.8 + 0.7 * attempts)
                slowFor = 0.0; easing = false
            }
        }
        gas = gs; brake = br
    }
}
