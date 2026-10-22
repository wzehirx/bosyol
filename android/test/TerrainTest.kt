import com.yokus.app.core.*
import kotlin.math.abs

fun fail(msg: String): Nothing = throw AssertionError(msg)
fun req(c: Boolean, msg: () -> String) { if (!c) fail(msg()) }

fun deterministic() {
    for (m in Maps.all) {
        val a = Terrain(m); val b = Terrain(m)
        val rnd = java.util.Random(1)
        repeat(1000) {
            val x = rnd.nextDouble() * a.length
            req(a.height(x) == b.height(x)) { "${m.id}: x=$x farklı" }
        }
        req(a.coins.size == b.coins.size && a.fuels.size == b.fuels.size && a.features.size == b.features.size) { "${m.id}: nesne sayıları farklı" }
    }
}

fun differentSeeds() {
    val a = Terrain(Maps.byId("country")); val b = Terrain(Maps.byId("desert"))
    req((1..2000).any { a.height(it * 7.3) != b.height(it * 7.3) }) { "farklı tohumlar farklı zemin vermeli" }
    val c = a.height(1234.5)
    req(c != 0.0 || a.height(900.0) != 0.0) { "zemin düz kalmamalı" }
}

fun safePlacement() {
    for (m in Maps.all) {
        val t = Terrain(m)
        val pits = t.features.filter { it.kind == FeatureKind.PIT }
        val ramps = t.features.filter { it.kind == FeatureKind.RAMP }
        req(pits.isNotEmpty() && ramps.isNotEmpty()) { "${m.id}: çukur/rampa yok" }
        val xs = t.coins.map { it.x } + t.fuels.map { it.x }
        for (x in xs) {
            for (p in pits) req(x < p.x0 - 1.5 || x > p.x1 + 1.5) { "${m.id}: x=$x çukurun ($p) içinde/duvarında" }
            for (r in ramps) req(x < r.x1 - 0.9 || x > r.x1 + 2.9) { "${m.id}: x=$x rampa ucunda ($r)" }
        }
    }
}

fun flatStart() {
    for (m in Maps.all) {
        val t = Terrain(m)
        var x = 0.0
        while (x <= 30.0) { req(t.height(x) == 0.0) { "${m.id}: height($x)=${t.height(x)}" }; x += 0.5 }
    }
}

fun slopeBounded() {
    for (m in Maps.all) {
        val t = Terrain(m)
        var x = 0.0
        while (x <= t.length) { req(abs(t.slope(x)) <= 1.7) { "${m.id}: slope($x)=${t.slope(x)}" }; x += 0.25 }
    }
}

/** Zorluk eğrisi: ilk 300 m yumuşak (≤ 0.5), 1500 m'ye dek ≤ 1.0; 1.0'dan dik eğim yalnızca dağ/mağara/volkanda ve 1500 m'den
 *  sonra, en çok 1.2. Her yerde eğim konumun sınırını aşmaz. */
fun slopeRamp() {
    for (m in Maps.all) {
        val t = Terrain(m)
        req(m.maxSlope <= 1.2) { "${m.id}: maxSlope ${m.maxSlope}" }
        req(m.maxSlope <= 1.0 || m.id == "mountain" || m.id == "cave" || m.id == "volcano") { "${m.id}: 1.0'dan dik olmamalı" }
        for (i in 0 until t.length.toInt()) {
            val x = i + 0.5; val s = abs(t.slope(x))
            req(s <= t.maxSlopeAt(x) + 1e-9) { "${m.id}: slope($x)=$s > sınır ${t.maxSlopeAt(x)}" }
            if (x < 300) req(s <= 0.5) { "${m.id}: ilk 300 m eğim $s (x=$x)" }
            if (x < 1500) req(s <= 1.0) { "${m.id}: 1500 m öncesi eğim $s (x=$x)" }
        }
        fun meanAbs(a: Int, b: Int) = (a until b).sumOf { abs(t.slope(it + 0.5)) } / (b - a)
        req(meanAbs(2500, 3500) > meanAbs(300, 1300)) { "${m.id}: zorluk mesafeyle artmalı" }
    }
    fun mean(id: String) = Terrain(Maps.byId(id)).let { t -> (300 until 3000).sumOf { abs(t.slope(it + 0.5)) } }
    req(mean("mountain") > mean("country") && mean("country") > mean("beach")) { "harita zorluğu: dağ > kır > plaj" }
}

/** Çukur: duvar eğimi ≤ 0.8, derinlik (kenarları birleştiren kirişe göre) ≤ 1.2 m, yuvarlak kenar (eğim sıçraması küçük).
 *  Rampa: yumuşak sıçrama ağzı, ardından uçurum değil hafif iniş. */
fun pitsAndRamps() {
    for (m in Maps.all) {
        val t = Terrain(m)
        for (p in t.features.filter { it.kind == FeatureKind.PIT }) {
            val h0 = t.height(p.x0); val h1 = t.height(p.x1)
            var x = p.x0 - 2; var depth = 0.0
            while (x <= p.x1 + 2) {
                req(abs(t.slope(x)) <= 0.8) { "${m.id}: çukur $p duvar eğimi ${t.slope(x)}" }
                req(abs(t.slope(x + 1) - t.slope(x)) <= 0.35) { "${m.id}: çukur $p keskin kenar x=$x" }
                if (x >= p.x0 && x <= p.x1) depth = maxOf(depth, h0 + (h1 - h0) * (x - p.x0) / (p.x1 - p.x0) - t.height(x))
                x += 0.25
            }
            req(depth in 0.2..1.2) { "${m.id}: çukur $p derinlik $depth" }
        }
        for (r in t.features.filter { it.kind == FeatureKind.RAMP }) {
            val kick = t.slope(r.x1 - 1.0) - t.slope(r.x0 - 1.0)
            req(kick >= 0.2) { "${m.id}: rampa $r ağzı sıçratmıyor ($kick)" }
            var x = r.x1
            while (x < r.x1 + 10) { req(t.slope(x) >= -0.5) { "${m.id}: rampa $r inişi dik ${t.slope(x)} (x=$x)" }; x += 0.25 }
        }
    }
}

fun fuelSpacing() {
    for (m in Maps.all) {
        val t = Terrain(m)
        req(t.fuels.size > 5) { "${m.id}: az bidon ${t.fuels.size}" }
        req(abs(t.fuels[0].x - 250.0) <= 5.0) { "${m.id}: ilk bidon ${t.fuels[0].x}" }
        for (i in 1 until t.fuels.size) {
            val d = t.fuels[i].x - t.fuels[i - 1].x
            val want = t.fuelGapAt(t.fuels[i - 1].x)
            req(abs(d - want) <= 60.0) { "${m.id}: bidon aralığı $d, beklenen $want" }
            if (t.fuels[i - 1].x < Terrain.FUEL_GROW_FROM) req(abs(d - m.fuelEvery) <= 60.0) { "${m.id}: ilk bidon aralığı $d" }
            req(d >= t.fuels.getOrNull(i - 2)?.let { t.fuels[i - 1].x - it.x - 80.0 } ?: 0.0) { "${m.id}: bidon aralığı kısalıyor" }
            val f = t.fuels[i]
            val dy = f.y - t.height(f.x)
            req(abs(dy - 0.8) < 0.3) { "${m.id}: bidon yüksekliği $dy" }
        }
    }
}

fun coinsAboveGround() {
    for (m in Maps.all) {
        val t = Terrain(m)
        req(t.coins.size > 100) { "${m.id}: az altın ${t.coins.size}" }
        for (c in t.coins) {
            val dy = c.y - t.height(c.x)
            req(dy in 1.0..1.5) { "${m.id}: altın x=${c.x} dy=$dy" }
            req(c.value == 1) { "değer ${c.value}" }
        }
    }
}

/** Her altın 1; gruplar 4-12'lik, düzensiz aralıklı ve rastgele yükseklikte; yoğunluk mesafe ve fiyatla artar; tohumdan deterministik. */
fun coinsRandomGroups() {
    val m = Maps.byId("country")
    val t = Terrain(m)
    req(t.coins.all { it.value == 1 }) { "değer 1 olmalı" }
    fun per100(map: MapDef, from: Double, to: Double): Double =
        Terrain(map).coins.count { it.x in from..to } / ((to - from) / 100.0)
    val early = per100(m, 0.0, 1500.0)
    req(early in 10.0..20.0) { "ilk 1500 m yoğunluk $early / 100 m" }
    val far = per100(m, 6000.0, 8000.0)
    req(far > early) { "uzakta daha yoğun olmalı: $early -> $far" }
    req(per100(Maps.byId("site"), 0.0, 1500.0) > early) { "pahalı harita daha yoğun olmalı" }
    // gruplar: art arda altınlar arası 1.2-2.6 m (grup içi), boşluklar büyük
    val xs = t.coins.map { it.x }.sorted()
    val gaps = xs.zipWithNext().map { it.second - it.first }
    val inGroup = gaps.filter { it < 3.0 }
    req(inGroup.isNotEmpty() && inGroup.toSet().size > 20) { "grup içi aralıklar düzensiz olmalı" }
    req(gaps.any { it > 15.0 }) { "gruplar arası boşluk yok" }
    // yükseklik rastgele
    val dys = t.coins.filter { it.x < 2000 }.map { it.y - t.height(it.x) }
    val mean = dys.average()
    val sd = Math.sqrt(dys.sumOf { (it - mean) * (it - mean) } / dys.size)
    req(sd > 0.08) { "yükseklik rastgele değil sd=$sd" }
    // grup boyutları 4..12 (kötü bölge nedeniyle eksilen olabilir, 12'yi aşmaz)
    var run = 1; var maxRun = 1
    for (g in gaps) { if (g < 3.0) { run++; maxRun = maxOf(maxRun, run) } else run = 1 }
    req(maxRun <= 12) { "grup en fazla 12: $maxRun" }
    // deterministik
    val t2 = Terrain(m)
    req(t.coins.map { it.x } == t2.coins.map { it.x } && t.coins.map { it.y } == t2.coins.map { it.y }) { "yerleşim deterministik değil" }
    req(Terrain(Maps.byId("desert")).coins.map { it.x } != t.coins.map { it.x }) { "haritalar aynı yerleşim" }
}

fun twentyVehiclesSixteenMaps() {
    // docs/ICERIK_2.md tabloları (fiyatlar altın gelirine göre yeniden ölçeklendi; sıra aynı)
    val vp = listOf(0, 450, 1100, 1700, 2200, 2900, 3600, 5000, 6000, 7200, 8600, 10000, 11500, 13000, 16000, 18000, 20000, 23000, 26000, 36000)
    val vi = listOf("jeep", "scooter", "moto", "gokart", "pickup", "quad", "tractor", "buggy", "chopper", "rally", "hotrod", "monster",
        "garbage", "bus", "snowmobile", "firetruck", "rover", "formula", "tank", "super")
    req(Vehicles.all.size == 20) { "araç sayısı ${Vehicles.all.size}" }
    req(Vehicles.all.map { it.id } == vi) { "araç kimlikleri ${Vehicles.all.map { it.id }}" }
    req(Vehicles.all.map { it.price } == vp) { "araç fiyatları ${Vehicles.all.map { it.price }}" }
    req(Vehicles.all.map { it.id }.toSet().size == 20) { "araç kimlikleri benzersiz değil" }
    req((1 until vp.size).all { vp[it] > vp[it - 1] }) { "araç fiyatları artan sırada değil" }
    val mp = listOf(0, 1000, 1300, 1800, 2800, 3500, 4200, 5000, 5800, 7200, 8600, 9300, 10000, 11500, 13000, 14500)
    val mi = listOf("country", "desert", "saltflat", "arctic", "moon", "frozenlake", "forest", "rooftops", "mountain", "beach", "cave", "mars",
        "swamp", "volcano", "site", "deepsea")
    req(Maps.all.size == 16) { "harita sayısı ${Maps.all.size}" }
    req(Maps.all.map { it.id } == mi) { "harita kimlikleri ${Maps.all.map { it.id }}" }
    req(Maps.all.map { it.price } == mp) { "harita fiyatları ${Maps.all.map { it.price }}" }
    req(Maps.all.map { it.id }.toSet().size == 16) { "harita kimlikleri benzersiz değil" }
    req((1 until mp.size).all { mp[it] > mp[it - 1] }) { "harita fiyatları artan sırada değil" }
    req(Vehicles.all.map { it.name }.toSet().size == 20) { "araç adları tekrar" }
    req(Maps.all.map { it.name }.toSet().size == 16) { "harita adları tekrar" }
    for (v in Vehicles.all) {
        req(Vehicles.byId(v.id) === v) { "byId ${v.id}" }
        req(v.headY > v.axleY + v.wheelR) { "${v.id}: headY ${v.headY} axleY ${v.axleY} r ${v.wheelR}" }
        req(v.headR in 0.2..0.3) { "${v.id}: headR ${v.headR}" }
        req(v.rearX < v.frontX && v.mass > 0 && v.inertia > 0 && v.fuelL > 0 && v.fuelUse > 0) { "${v.id}: temel değerler" }
        req(v.desc.isNotBlank() && v.name.isNotBlank()) { "${v.id}: metin" }
    }
    for (m in Maps.all) { req(Maps.byId(m.id) === m) { "byId ${m.id}" }; req(m.desc.isNotBlank()) { "${m.id}: desc" } }
    val j = Vehicles.byId("jeep")
    req(j.mass == 300.0 && j.rearX == -1.1 && j.frontX == 1.1 && j.wheelR == 0.4 && j.drive == Drive.REAR) { "jeep değerleri" }
    val moto = Vehicles.byId("moto")
    req(moto.mass == 150.0 && abs((moto.frontX - moto.rearX) - 1.4) < 1e-9) { "moto" }
    req(moto.airTorque > Vehicles.byId("jeep").airTorque) { "moto airTorque" }
    val tank = Vehicles.byId("tank")
    req(tank.torque > j.torque && tank.maxRpm < j.maxRpm && tank.snowGrip == 1.5) { "tank" }
    val sup = Vehicles.byId("super")
    req(sup.torque > tank.torque && sup.wheelR < j.wheelR && sup.suspK > j.suspK) { "super" }
    req(sup.torque > tank.torque && tank.torque > j.torque) { "tork sırası: süper > tank > cip" }
    val snow = Vehicles.byId("snowmobile")
    req(snow.snowGrip == 2.2 && Vehicles.all.all { it === snow || it.snowGrip < snow.snowGrip }) { "en yüksek snowGrip kar motosikletinde olmalı" }
    val rover = Vehicles.byId("rover")
    req(rover.electric && rover.engineKey == "synth") { "rover" }
    req(Vehicles.all.count { it.electric } == 1) { "tek elektrikli" }
    val moon = Maps.byId("moon")
    req(moon.gravity == 1.62 && Maps.all.filter { it.id !in listOf("moon", "mars", "deepsea") }.all { it.gravity == 9.81 }) { "yer çekimi" }
    req(Maps.byId("mars").gravity == 3.71 && Maps.byId("deepsea").gravity == 4.9) { "mars/deniz tabanı yer çekimi" }
    req(Maps.byId("deepsea").drag > 0.0 && Maps.all.filter { it.id != "deepsea" }.all { it.drag == 0.0 && it.pits >= 1.0 }) { "yalnız deniz tabanında su direnci" }
    req(Maps.byId("saltflat").surface == Surface.SALT && Surface.SALT.mu == 0.35 && Surface.SALT.rollKey == "roll_sand" && Surface.SALT.rollRate > 1.0) { "tuz yüzeyi" }
    req(Maps.byId("mars").surface == Surface.MARS && Surface.MARS.mu == 0.65 && Surface.MARS.rollKey == "roll_dirt") { "mars yüzeyi" }
    req(Maps.byId("frozenlake").surface == Surface.ICE && Maps.byId("rooftops").surface == Surface.ASPHALT && Maps.byId("volcano").surface == Surface.ROCK) { "yeni harita yüzeyleri" }
    for (m in Maps.all) req(m.ambKey.startsWith("amb_")) { "${m.id}: ambKey" }
    req(Maps.byId("arctic").surface == Surface.ICE || Maps.byId("arctic").surface == Surface.SNOW) { "kutup yüzeyi" }
    req(Maps.byId("country").surface == Surface.DIRT && Maps.byId("site").surface == Surface.ASPHALT) { "yüzeyler" }
    req(Maps.all.map { it.seed }.toSet().size == 16) { "tohumlar farklı olmalı" }
    req(Surface.ICE.label == "buz" && Surface.MOON.label == "ay tozu" && Surface.SAND.mu == 0.6) { "yüzey tablosu" }
}

fun upgradeCostGrows() {
    for (v in Vehicles.all) for (k in UpgradeKind.values()) {
        for (l in 0 until 9) req(upgradeCost(v, k, l + 1) > upgradeCost(v, k, l)) { "${v.id} $k $l" }
        req(upgradeCost(v, k, 0) == maxOf(UPGRADE_BASE_MIN, v.price / 25)) { "${v.id} taban" }
    }
    req(upgradeCost(Vehicles.byId("jeep"), UpgradeKind.ENGINE, 0) == 60) { "jeep 0 != 60" }
    req(upgradeCost(Vehicles.byId("jeep"), UpgradeKind.FUEL, 1) == 96) { "jeep 1 != 96" }
    req(upgradeCost(Vehicles.byId("super"), UpgradeKind.TIRE, 2) == Math.round(1440 * 1.6 * 1.6).toInt()) { "super 2" }
    val j = Vehicles.byId("jeep")
    val u = j.upgraded(Upgrades(engine = 5, susp = 3, tire = 10, fuel = 10))
    req(abs(u.torque - j.torque * 1.4) < 1e-9 && abs(u.fuelL - j.fuelL * 2.0) < 1e-9) { "upgraded motor/yakıt" }
    req(abs(u.suspK - j.suspK * 1.12) < 1e-9 && abs(u.suspC - j.suspC * 1.21) < 1e-9) { "upgraded süspansiyon" }
    req(j.upgraded(Upgrades()) == j) { "seviye 0 aynı kalmalı" }
    req(abs(gripMul(Upgrades(tire = 10)) - 1.6) < 1e-9 && gripMul(Upgrades()) == 1.0) { "gripMul" }
    req(UpgradeKind.SUSP.label == "Süspansiyon" && UpgradeKind.FUEL.label == "Yakıt deposu") { "etiketler" }
}

fun featuresFound() {
    val t = Terrain(Maps.byId("country"))
    val first = t.features.filter { it.x0 < 5000 }
    req(first.size >= 10) { "country ilk 5 km öznitelik: ${first.size}" }
    for (f in t.features) req(f.x1 >= f.x0) { "ters aralık $f" }
    val kinds = t.features.map { it.kind }.toSet()
    req(FeatureKind.RAMP in kinds && FeatureKind.PIT in kinds && FeatureKind.BUMPS in kinds) { "türler $kinds" }
    req(FeatureKind.STEEP_UP in Terrain(Maps.byId("mountain")).features.map { it.kind }) { "dağda dik yokuş yok" }
    // nextFeature
    val f = t.features[3]
    val n = t.nextFeature(f.x0 - 10.0, 20.0)
    req(n != null && n.x0 > f.x0 - 10.0 && n.x0 <= f.x0 + 10.0) { "nextFeature $n" }
    req(t.nextFeature(f.x0, 0.5)?.x0 != f.x0) { "nextFeature x'in kendisini dönmemeli" }
    req(t.nextFeature(-100.0, 10.0) == null || t.nextFeature(-100.0, 10.0)!!.x0 <= -90.0) { "nextFeature aralık" }
    // nextFeature kaba kuvvetle aynı sonucu vermeli
    val rr = java.util.Random(7)
    repeat(2000) {
        val q = rr.nextDouble() * 6000 - 50; val range = rr.nextDouble() * 60
        val exp = t.features.filter { it.x0 > q }.minByOrNull { it.x0 }?.takeIf { it.x0 <= q + range }
        req(t.nextFeature(q, range) === exp) { "nextFeature($q,$range) = ${t.nextFeature(q, range)}, beklenen $exp" }
    }
    // clamp
    req(t.height(-5.0) == t.height(0.0) && t.height(t.length + 50) == t.height(t.length)) { "clamp" }
    // sample interpolation
    val x = 1234.0; val mid = t.height(x + 0.5)
    req(abs(mid - (t.height(x) + t.height(x + 1.0)) / 2) < 1e-9) { "doğrusal ara değer" }
}

fun main() {
    deterministic(); println("deterministic ok")
    differentSeeds(); println("differentSeeds ok")
    safePlacement(); println("safePlacement ok")
    flatStart(); println("flatStart ok")
    slopeBounded(); println("slopeBounded ok")
    slopeRamp(); println("slopeRamp ok")
    pitsAndRamps(); println("pitsAndRamps ok")
    fuelSpacing(); println("fuelSpacing ok")
    coinsAboveGround(); println("coinsAboveGround ok")
    coinsRandomGroups(); println("coinsRandomGroups ok")
    twentyVehiclesSixteenMaps(); println("twentyVehiclesSixteenMaps ok")
    upgradeCostGrows(); println("upgradeCostGrows ok")
    featuresFound(); println("featuresFound ok")
    println("OK")
}
