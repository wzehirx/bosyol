import com.bosyol.app.core.*

class NoSpeak : Speaker { val said = ArrayList<String>(); var t = 0.0; override fun say(text: String, urgent: Boolean) { said.add(String.format("%.1fs %s", t, text)) } }

val BANK = FBank("/home/user/bosyol/sounds")
fun mk(veh: VehicleDef, env: String = "forest"): Pair<Game, NoSpeak> {
    val sp = NoSpeak()
    val set = GameSettings(); set.traffic = false
    val g = Game(Mixer(), BANK, sp, set, veh, Envs.byId(env))
    g.start()
    return g to sp
}
fun run(g: Game, sp: NoSpeak, sec: Double, each: (Double) -> Unit = {}) { var t = 0.0; while (t < sec) { g.update(0.01); t += 0.01; sp.t += 0.01; each(t) } }

fun main() {
    for (veh in listOf(Vehicles.sedan, Vehicles.truck, Vehicles.sport)) {
        println("===== ${veh.name}")
        // 1) Tam gaz kalkış: vites geçiş hızları ve 0-100
        run {
            val (g, sp) = mk(veh); g.gasHold = true
            var lastGear = 0; var t100 = -1.0; var tt = 0.0
            val shifts = ArrayList<String>()
            run(g, sp, 25.0) { t -> tt = t; if (g.gear != lastGear) { shifts.add("${lastGear + 1}->${g.gear + 1} @${(g.v * 3.6).toInt()} km/s ${g.rpm.toInt()}rpm"); lastGear = g.gear }; if (t100 < 0 && g.v * 3.6 >= 100) t100 = t }
            println("  tam gaz: 0-100 = ${String.format("%.1f", t100)} s, 25 sn sonra ${(g.v * 3.6).toInt()} km/s; vitesler: $shifts")
        }
        // 2) Hafif gaz (sabitlenmiş pedal): vites geçiş hızları
        run {
            val (g, sp) = mk(veh); g.gasLatch = true
            var lastGear = 0; val shifts = ArrayList<String>()
            run(g, sp, 30.0) { if (g.gear != lastGear) { shifts.add("${lastGear + 1}->${g.gear + 1} @${(g.v * 3.6).toInt()}"); lastGear = g.gear } }
            println("  hafif gaz: 30 sn sonra ${(g.v * 3.6).toInt()} km/s; vitesler: $shifts")
        }
        // 3) Gazı bırak, tekrar bas: vites küçültme ve ses yükü
        run {
            val (g, sp) = mk(veh); g.gasLatch = true
            run(g, sp, 25.0)
            g.gasLatch = false; run(g, sp, 3.0)
            val gearOff = g.gear; val rpmOff = g.rpm.toInt(); val loadOff = g.engineLoad
            g.gasHold = true
            var t30 = -1.0; var maxGearDrop = gearOff
            run(g, sp, 2.0) { t -> if (t30 < 0 && g.engineLoad > 0.3) t30 = t; if (g.gear < maxGearDrop) maxGearDrop = g.gear }
            println("  bırak 3 sn: ${gearOff + 1}. vites ${rpmOff} rpm yük ${String.format("%.2f", loadOff)} -> tekrar gaz: ses yükü %30'a ${String.format("%.2f", t30)} sn'de, vites ${maxGearDrop + 1}'e indi, 2 sn sonra ${g.rpm.toInt()} rpm")
        }
        // 4) 100'den fren: ilk 1 sn yavaşlama, durma süresi ve mesafesi
        run {
            val (g, sp) = mk(veh); g.gasHold = true
            run(g, sp, 40.0) { if (g.v * 3.6 > 100) g.gasHold = false }
            g.gasHold = false; while (g.v * 3.6 > 100) g.update(0.01)
            val v0 = g.v; val s0 = g.s; g.brakeHold = true
            var v1 = 0.0; var tStop = -1.0
            run(g, sp, 15.0) { t -> if (t <= 1.0) v1 = g.v; if (tStop < 0 && g.v < 0.1) tStop = t }
            println("  frenleme ${(v0 * 3.6).toInt()} km/s: ilk 1 sn ${String.format("%.1f", (v0 - v1))} m/s² , durma ${String.format("%.1f", tStop)} sn, ${(g.s - s0).toInt()} m")
        }
    }
    // 5) Virajlar: önerilen hızla ve 140 ile girmek
    for (kmh in listOf(0, 140)) {
        val (g, sp) = mk(Vehicles.sedan, "mountain")
        g.road.ensure(4000.0)
        val c = synchronized(g.road.curves) { g.road.curves.first { 1 / Math.abs(it.k) < 120 && it.s0 > 300 } }
        val adv = g.advisedKmh(c.k)
        val speed = if (kmh == 0) adv else kmh
        g.s = c.s0 - 60; g.v = speed / 3.6; g.gear = 4
        g.setAuto(true, silent = true); g.cruise = speed
        var maxOff = 0.0; var crashed = false; var maxSurf = 0
        run(g, sp, 12.0) { if (Math.abs(g.offset) > maxOff) maxOff = Math.abs(g.offset); if (g.crashT > 0) crashed = true; if (g.surface > maxSurf) maxSurf = g.surface }
        println("viraj R=${(1 / Math.abs(c.k)).toInt()} m, önerilen $adv, girilen $speed (otomatik hız tutucu): en fazla kayma ${String.format("%.1f", maxOff)} m, yüzey $maxSurf, kaza $crashed")
    }
    run {
        val (g, sp) = mk(Vehicles.sedan, "mountain")
        g.road.ensure(4000.0)
        val c = synchronized(g.road.curves) { g.road.curves.first { 1 / Math.abs(it.k) < 120 && it.s0 > 300 } }
        g.s = c.s0 - 60; g.v = 140 / 3.6; g.gear = 6; g.gasLatch = true
        var maxOff = 0.0; var crashed = false
        run(g, sp, 8.0) { if (Math.abs(g.offset) > maxOff) maxOff = Math.abs(g.offset); if (g.crashT > 0) crashed = true }
        println("elle 140 ile, R=${(1 / Math.abs(c.k)).toInt()}: kayma ${String.format("%.1f", maxOff)} m, kaza $crashed; duyurular: ${sp.said.take(4)}")
    }
}
