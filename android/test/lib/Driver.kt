import com.yokus.app.core.*
import kotlin.math.atan

/**
 * Basit "akıllı sürücü" (testler için; G4 testleri de kullanabilir).
 * Yerde: gaz 1; burun 0.6 rad'dan fazla kalkarsa gaz 0.3 (gazla oynayarak takla önlenir), fren yok.
 * Havada: gaz/frenle gövde açısını, ileride ineceği zeminin eğimine yakın tutar (PD denetim).
 */
class SmartDriver(val featherAngle: Double = 0.6, val featherGas: Double = 0.3) {
    fun control(c: Car) {
        if (c.airborne) {
            val ahead = c.x + c.vx * 0.4
            val target = atan(c.terrain.slope(ahead))
            val u = (-(2.5 * (c.angle - target) + 0.5 * c.omega)).coerceIn(-1.0, 1.0)
            c.gas = if (u > 0) u else 0.0
            c.brake = if (u < 0) -u else 0.0
        } else {
            c.brake = 0.0
            c.gas = if (c.angle > featherAngle) featherGas else 1.0
        }
    }
}

/** Sürüş sonucu: son konum, en büyük x, takla/kafa vuruşu ve en yüksek hız. */
class DriveResult(val car: Car, val maxX: Double, val crashed: Boolean, val crashX: Double, val crashT: Double, val topSpeed: Double)

/**
 * Aracı [seconds] boyunca dt = 1/60 adımlarla sürer. [policy] null ise SmartDriver kullanılır.
 * Kafa yere değer ya da |açı| > 2.2 olursa kaza sayılır; [stopOnCrash] ise orada durur.
 */
fun smartDrive(car: Car, seconds: Double, stopOnCrash: Boolean = true, policy: ((Car) -> Unit)? = null): DriveResult {
    val drv = SmartDriver()
    val n = Math.round(seconds * 60).toInt()
    var maxX = car.x; var crashed = false; var crashX = -1.0; var crashT = -1.0; var top = 0.0
    for (i in 0 until n) {
        if (policy != null) policy(car) else drv.control(car)
        car.step(1.0 / 60)
        if (car.x > maxX) maxX = car.x
        if (car.speed > top) top = car.speed
        if (!crashed && (car.headHit() || kotlin.math.abs(car.angle) > 2.2)) {
            crashed = true; crashX = car.x; crashT = (i + 1) / 60.0
            if (stopOnCrash) break
        }
    }
    return DriveResult(car, maxX, crashed, crashX, crashT, top)
}
