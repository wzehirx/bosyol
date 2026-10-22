package com.yokus.app.core

import kotlin.math.pow

/** rollRate: yuvarlanma sesinin örnek hızı çarpanı (tuz: kum sesi daha tiz). */
enum class Surface(val mu: Double, val rollKey: String, val label: String, val rollRate: Double = 1.0) {
    DIRT(0.9, "roll_dirt", "toprak"), SAND(0.6, "roll_sand", "kum"), SNOW(0.45, "roll_snow", "kar"), ICE(0.25, "roll_snow", "buz"),
    MUD(0.5, "roll_mud", "çamur"), MOON(0.7, "roll_dirt", "ay tozu"), ROCK(0.95, "roll_rock", "kaya"), ASPHALT(1.0, "roll_asphalt", "asfalt"),
    SALT(0.35, "roll_sand", "tuz", 1.35), MARS(0.65, "roll_dirt", "kırmızı toz", 0.85)
}

enum class Drive { REAR, ALL }

data class VehicleDef(
    val id: String, val name: String, val price: Int, val desc: String,
    val mass: Double, val inertia: Double, val wheelR: Double, val wheelMass: Double,
    val rearX: Double, val frontX: Double, val axleY: Double,          // tekerlek bağlantı noktaları, gövde koordinatı (m)
    val suspK: Double, val suspC: Double, val suspTravel: Double,      // N/m, N·s/m, m
    val torque: Double, val idleRpm: Double, val maxRpm: Double, val ratio: Double, // motor devri = teker devri × ratio
    val drive: Drive, val airTorque: Double, val fuelL: Double, val fuelUse: Double, // L, tam gazda L/s
    val headX: Double, val headY: Double, val headR: Double,           // kafa dairesi, gövde koordinatı
    val engineKey: String, val engineRefRpm: Double, val enginePitch: Double, val electric: Boolean = false,
    val snowGrip: Double = 1.0,                                         // KAR/BUZ üzerinde μ çarpanı
    val grip: Double = 1.0,                                             // her yüzeyde μ çarpanı (lastik/dişli teker)
    val engineLp: Double = 20000.0,                                     // motor sesi alçak geçiren süzgeç (Hz); 20000 = kapalı
    val engineHp: Double = 0.0,                                         // motor sesi yüksek geçiren süzgeç (Hz); 0 = kapalı
    val turbo: Boolean = false,                                         // turbo ıslığı/boşaltma sesleri
    val supercharged: Boolean = false)                                  // devire bağlı kompresör uğultusu

data class MapDef(
    val id: String, val name: String, val price: Int, val desc: String, val surface: Surface, val gravity: Double,
    val ambKey: String, val seed: Long, val amp: Double, val growth: Double, val rampEvery: Double, val fuelEvery: Double,
    val maxSlope: Double = 1.0,   // izin verilen en büyük eğim (ilk 300 m 0.5; 1500 m'ye dek bu değere yumuşakça çıkar)
    val drag: Double = 0.0,       // ek hava/su direnci, N/(m/s)² (deniz tabanı); 0 = yok
    val pits: Double = 1.0)       // çukur sıklığı çarpanı (gece çatıları: binalar arası boşluklar); 1 = eski değer

object Vehicles {
    val all: List<VehicleDef> = listOf(
        VehicleDef("jeep", "Cip", 0, "Dengeli başlangıç aracı.",
            mass = 300.0, inertia = 565.0, wheelR = 0.4, wheelMass = 20.0, rearX = -1.1, frontX = 1.1, axleY = -0.3,
            suspK = 20760.0, suspC = 2490.0, suspTravel = 0.35,
            torque = 650.0, idleRpm = 1000.0, maxRpm = 6000.0, ratio = 15.20,
            drive = Drive.REAR, airTorque = 5690.0, fuelL = 10.0, fuelUse = 0.16,
            headX = -0.1, headY = 0.85, headR = 0.25,
            engineKey = "eng_hill", engineRefRpm = 2730.0, enginePitch = 1.0),
        VehicleDef("scooter", "Scooter", 450, "Minik tekerlekli, çok hafif ve havada çevik; yokuşta zorlanır.",
            mass = 110.0, inertia = 230.0, wheelR = 0.22, wheelMass = 6.0, rearX = -0.7, frontX = 0.7, axleY = -0.1,
            suspK = 4800.0, suspC = 420.0, suspTravel = 0.18,
            torque = 150.0, idleRpm = 1300.0, maxRpm = 7800.0, ratio = 12.5,
            drive = Drive.REAR, airTorque = 4500.0, fuelL = 4.5, fuelUse = 0.055,
            headX = -0.05, headY = 0.72, headR = 0.2,
            engineKey = "eng_moto", engineRefRpm = 4400.0, enginePitch = 1.45, engineHp = 500.0),
        VehicleDef("moto", "Motosiklet", 1100, "Hafif ve güçlü, havada kolay takla atar.",
            mass = 150.0, inertia = 380.0, wheelR = 0.35, wheelMass = 10.0, rearX = -0.7, frontX = 0.7, axleY = -0.15,
            suspK = 8000.0, suspC = 700.0, suspTravel = 0.25,
            torque = 420.0, idleRpm = 1200.0, maxRpm = 7500.0, ratio = 13.70,
            drive = Drive.REAR, airTorque = 6200.0, fuelL = 7.0, fuelUse = 0.09,
            headX = -0.05, headY = 0.95, headR = 0.22,
            engineKey = "eng_moto", engineRefRpm = 3500.0, enginePitch = 1.1),
        VehicleDef("gokart", "Go-kart", 1700, "Alçak ve hafif, düz yolda çok hızlı; sıçramada savrulur.",
            mass = 140.0, inertia = 260.0, wheelR = 0.2, wheelMass = 7.0, rearX = -0.7, frontX = 0.7, axleY = -0.05,
            suspK = 14000.0, suspC = 1200.0, suspTravel = 0.1,
            torque = 260.0, idleRpm = 1800.0, maxRpm = 8500.0, ratio = 8.9,
            drive = Drive.REAR, airTorque = 2200.0, fuelL = 6.0, fuelUse = 0.09,
            headX = -0.1, headY = 0.5, headR = 0.2,
            engineKey = "eng_buggy", engineRefRpm = 4600.0, enginePitch = 1.35),
        VehicleDef("pickup", "Pikap", 2200, "Sağlam ve dayanıklı, orta hızlı.",
            mass = 450.0, inertia = 1000.0, wheelR = 0.45, wheelMass = 28.0, rearX = -1.4, frontX = 1.3, axleY = -0.3,
            suspK = 18000.0, suspC = 1500.0, suspTravel = 0.3,
            torque = 950.0, idleRpm = 900.0, maxRpm = 5500.0, ratio = 17.30,
            drive = Drive.REAR, airTorque = 6400.0, fuelL = 14.0, fuelUse = 0.13,
            headX = -0.2, headY = 0.95, headR = 0.25,
            engineKey = "e1_med_on", engineRefRpm = 3000.0, enginePitch = 1.0, grip = 1.1),
        VehicleDef("quad", "Dörtteker", 2900, "Hafif ve dört çeker, düzensiz zeminde güvenli, havada çevik.",
            mass = 220.0, inertia = 400.0, wheelR = 0.33, wheelMass = 14.0, rearX = -0.65, frontX = 0.65, axleY = -0.15,
            suspK = 12000.0, suspC = 1100.0, suspTravel = 0.25,
            torque = 400.0, idleRpm = 1400.0, maxRpm = 6500.0, ratio = 15.0,
            drive = Drive.ALL, airTorque = 5200.0, fuelL = 8.0, fuelUse = 0.1,
            headX = -0.05, headY = 0.9, headR = 0.22,
            engineKey = "eng_moto", engineRefRpm = 3500.0, enginePitch = 0.85, grip = 1.15),
        VehicleDef("tractor", "Traktör", 3600, "Yavaş ama çok tutunur ve dengelidir.",
            mass = 600.0, inertia = 1600.0, wheelR = 0.55, wheelMass = 40.0, rearX = -1.0, frontX = 1.2, axleY = -0.2,
            suspK = 26000.0, suspC = 2400.0, suspTravel = 0.2,
            torque = 1700.0, idleRpm = 800.0, maxRpm = 3500.0, ratio = 20.20,
            drive = Drive.REAR, airTorque = 7680.0, fuelL = 16.0, fuelUse = 0.12,
            headX = -0.4, headY = 1.2, headR = 0.25,
            engineKey = "eng_tractor", engineRefRpm = 2200.0, enginePitch = 0.6, engineLp = 1500.0, grip = 1.3, snowGrip = 1.25),
        VehicleDef("buggy", "Kum arabası", 5000, "Hafif, yumuşak süspansiyonlu, zıplamayı sever.",
            mass = 250.0, inertia = 900.0, wheelR = 0.42, wheelMass = 18.0, rearX = -1.0, frontX = 1.0, axleY = -0.25,
            suspK = 8500.0, suspC = 700.0, suspTravel = 0.4,
            torque = 650.0, idleRpm = 1100.0, maxRpm = 6500.0, ratio = 16.80,
            drive = Drive.REAR, airTorque = 4320.0, fuelL = 9.0, fuelUse = 0.11,
            headX = -0.1, headY = 0.9, headR = 0.24,
            engineKey = "eng_buggy", engineRefRpm = 3500.0, enginePitch = 1.05),
        VehicleDef("chopper", "Chopper", 6000, "Uzun dingilli ağır motosiklet, düz yolda çok dengeli ama burnu kolay kalkar.",
            mass = 250.0, inertia = 700.0, wheelR = 0.38, wheelMass = 14.0, rearX = -0.9, frontX = 1.5, axleY = -0.15,
            suspK = 11000.0, suspC = 1000.0, suspTravel = 0.25,
            torque = 650.0, idleRpm = 1500.0, maxRpm = 6200.0, ratio = 15.0,
            drive = Drive.REAR, airTorque = 6000.0, fuelL = 9.0, fuelUse = 0.1,
            headX = -0.3, headY = 0.85, headR = 0.22,
            engineKey = "eng_moto", engineRefRpm = 3000.0, enginePitch = 0.7),
        VehicleDef("rally", "Ralli arabası", 7200, "Hızlı ve alçak.",
            mass = 330.0, inertia = 1200.0, wheelR = 0.36, wheelMass = 18.0, rearX = -1.15, frontX = 1.15, axleY = -0.2,
            suspK = 16000.0, suspC = 1300.0, suspTravel = 0.2,
            torque = 800.0, idleRpm = 1000.0, maxRpm = 7000.0, ratio = 12.00,
            drive = Drive.ALL, airTorque = 4320.0, fuelL = 11.0, fuelUse = 0.14,
            headX = -0.1, headY = 0.75, headR = 0.22,
            engineKey = "eng_rally2", engineRefRpm = 3180.0, enginePitch = 1.0, turbo = true),
        VehicleDef("hotrod", "Hot rod", 8600, "Güçlü kalkışlı, burnu ağır, gürleyen V8 sesli bir yarış arabası.",
            mass = 480.0, inertia = 1400.0, wheelR = 0.5, wheelMass = 30.0, rearX = -1.1, frontX = 1.7, axleY = -0.25,
            suspK = 22000.0, suspC = 1900.0, suspTravel = 0.3,
            torque = 1200.0, idleRpm = 1300.0, maxRpm = 6200.0, ratio = 16.2,
            drive = Drive.REAR, airTorque = 7000.0, fuelL = 14.0, fuelUse = 0.17,
            headX = -0.4, headY = 0.8, headR = 0.25,
            engineKey = "e2_med_on", engineRefRpm = 2800.0, enginePitch = 0.85),
        VehicleDef("monster", "Canavar kamyon", 10000, "Dev tekerler, yumuşak süspansiyon.",
            mass = 700.0, inertia = 1800.0, wheelR = 0.75, wheelMass = 60.0, rearX = -1.4, frontX = 1.4, axleY = -0.1,
            suspK = 24000.0, suspC = 2000.0, suspTravel = 0.5,
            torque = 2400.0, idleRpm = 900.0, maxRpm = 5000.0, ratio = 28.00,
            drive = Drive.ALL, airTorque = 10080.0, fuelL = 18.0, fuelUse = 0.18,
            headX = -0.2, headY = 1.6, headR = 0.28,
            engineKey = "eng_monster2", engineRefRpm = 2270.0, enginePitch = 1.0),
        VehicleDef("garbage", "Çöp kamyonu", 11500, "Ağır ve yavaş, çok iyi tutunur; havada ağır davranır.",
            mass = 1300.0, inertia = 3800.0, wheelR = 0.55, wheelMass = 55.0, rearX = -1.9, frontX = 1.9, axleY = -0.35,
            suspK = 38000.0, suspC = 3600.0, suspTravel = 0.3,
            torque = 2200.0, idleRpm = 800.0, maxRpm = 3500.0, ratio = 17.5,
            drive = Drive.REAR, airTorque = 12000.0, fuelL = 22.0, fuelUse = 0.18,
            headX = 1.2, headY = 1.4, headR = 0.28,
            engineKey = "eng_bus", engineRefRpm = 1700.0, enginePitch = 0.75, engineLp = 2000.0, grip = 1.25),
        VehicleDef("bus", "Otobüs", 13000, "Uzun dingilli ve ağır.",
            mass = 1200.0, inertia = 4000.0, wheelR = 0.5, wheelMass = 45.0, rearX = -2.4, frontX = 2.2, axleY = -0.4,
            suspK = 40000.0, suspC = 3600.0, suspTravel = 0.3,
            torque = 2000.0, idleRpm = 800.0, maxRpm = 4000.0, ratio = 16.10,
            drive = Drive.REAR, airTorque = 16000.0, fuelL = 24.0, fuelUse = 0.2,
            headX = 1.6, headY = 1.3, headR = 0.28,
            engineKey = "eng_bus", engineRefRpm = 2200.0, enginePitch = 0.9),
        VehicleDef("snowmobile", "Kar motosikleti", 16000, "Kar ve buzda çok iyi tutunur.",
            mass = 220.0, inertia = 700.0, wheelR = 0.33, wheelMass = 12.0, rearX = -0.9, frontX = 0.9, axleY = -0.2,
            suspK = 9000.0, suspC = 800.0, suspTravel = 0.3,
            torque = 420.0, idleRpm = 1200.0, maxRpm = 7000.0, ratio = 14.20,
            drive = Drive.REAR, airTorque = 3360.0, fuelL = 9.0, fuelUse = 0.1,
            headX = -0.1, headY = 0.9, headR = 0.22,
            engineKey = "eng_snowmobile", engineRefRpm = 3500.0, enginePitch = 1.4, snowGrip = 2.2, engineHp = 900.0),
        VehicleDef("firetruck", "İtfaiye aracı", 18000, "Ağır, güçlü ve dört çeker; tırmanışta ısrarcıdır.",
            mass = 1400.0, inertia = 4800.0, wheelR = 0.6, wheelMass = 60.0, rearX = -2.1, frontX = 2.0, axleY = -0.35,
            suspK = 45000.0, suspC = 4200.0, suspTravel = 0.3,
            torque = 3200.0, idleRpm = 900.0, maxRpm = 4200.0, ratio = 18.8,
            drive = Drive.ALL, airTorque = 15000.0, fuelL = 26.0, fuelUse = 0.22,
            headX = 1.4, headY = 1.4, headR = 0.28,
            engineKey = "eng_monster2", engineRefRpm = 2000.0, enginePitch = 0.8, grip = 1.1),
        VehicleDef("rover", "Ay aracı", 20000, "Elektrikli ve hafif, Ay'da at gibi zıplar.",
            mass = 200.0, inertia = 800.0, wheelR = 0.38, wheelMass = 15.0, rearX = -1.0, frontX = 1.0, axleY = -0.25,
            suspK = 7000.0, suspC = 600.0, suspTravel = 0.35,
            torque = 380.0, idleRpm = 600.0, maxRpm = 6000.0, ratio = 17.00,
            drive = Drive.ALL, airTorque = 3200.0, fuelL = 10.0, fuelUse = 0.08,
            headX = -0.1, headY = 0.85, headR = 0.24,
            engineKey = "synth", engineRefRpm = 3000.0, enginePitch = 1.0, electric = true),
        VehicleDef("formula", "Formül araba", 23000, "Çok hafif ve alçak, yüksek devirli turbolu; kontrolü zordur.",
            mass = 220.0, inertia = 380.0, wheelR = 0.3, wheelMass = 12.0, rearX = -1.1, frontX = 1.3, axleY = -0.1,
            suspK = 26000.0, suspC = 2000.0, suspTravel = 0.1,
            torque = 1500.0, idleRpm = 2500.0, maxRpm = 11000.0, ratio = 13.3,
            drive = Drive.REAR, airTorque = 2000.0, fuelL = 9.0, fuelUse = 0.2,
            headX = -0.1, headY = 0.55, headR = 0.2,
            engineKey = "e2_high_on", engineRefRpm = 5800.0, enginePitch = 1.1, turbo = true),
        VehicleDef("tank", "Tank", 26000, "Çok yavaş, çok tutunur, durdurulamaz.",
            mass = 1100.0, inertia = 2600.0, wheelR = 0.5, wheelMass = 80.0, rearX = -1.5, frontX = 1.5, axleY = -0.3,
            suspK = 60000.0, suspC = 6000.0, suspTravel = 0.2,
            torque = 3200.0, idleRpm = 700.0, maxRpm = 2500.0, ratio = 13.10,
            drive = Drive.ALL, airTorque = 11200.0, fuelL = 30.0, fuelUse = 0.2,
            headX = 0.0, headY = 1.3, headR = 0.28,
            engineKey = "eng_tractor", engineRefRpm = 1600.0, enginePitch = 0.45, snowGrip = 1.5, engineLp = 700.0),
        VehicleDef("super", "Süper araba", 36000, "Çok hızlı, kontrolü zor.",
            mass = 350.0, inertia = 600.0, wheelR = 0.32, wheelMass = 16.0, rearX = -1.2, frontX = 1.2, axleY = -0.2,
            suspK = 30000.0, suspC = 2400.0, suspTravel = 0.15,
            torque = 3600.0, idleRpm = 1200.0, maxRpm = 8500.0, ratio = 10.20,
            drive = Drive.ALL, airTorque = 2400.0, fuelL = 12.0, fuelUse = 0.2,
            headX = -0.1, headY = 0.7, headR = 0.2,
            engineKey = "e2_high_on", engineRefRpm = 4500.0, enginePitch = 1.0, turbo = true, supercharged = true)
    )
    fun byId(id: String): VehicleDef = all.first { it.id == id }
}

object Maps {
    val all: List<MapDef> = listOf(
        MapDef("country", "Kır yolu", 0, "Toprak yolda hafif tepeler.", Surface.DIRT, 9.81, "amb_country", 1101L, 3.0, 0.15, 500.0, 450.0, maxSlope = 0.9),
        MapDef("desert", "Çöl", 1000, "Kum tepeleri, tutunma düşük.", Surface.SAND, 9.81, "amb_desert", 2202L, 3.5, 0.15, 600.0, 500.0),
        MapDef("saltflat", "Tuz gölü", 1300, "Çok düz ve kaygan tuz düzlüğü, nadir rampalar.", Surface.SALT, 9.81, "amb_saltflat", 11211L, 1.2, 0.1, 1400.0, 600.0),
        MapDef("arctic", "Kutup", 1800, "Karlı, kaygan yamaçlar.", Surface.SNOW, 9.81, "amb_arctic", 3303L, 3.5, 0.2, 700.0, 450.0),
        MapDef("moon", "Ay", 2800, "Düşük yer çekimi, uzun sıçramalar.", Surface.MOON, 1.62, "amb_moon", 4404L, 3.5, 0.15, 400.0, 500.0),
        MapDef("frozenlake", "Donmuş göl", 3500, "Düz buz, kenarlarda kar tepeleri ve çatırdayan küçük rampalar.", Surface.ICE, 9.81, "amb_frozenlake", 12312L, 1.6, 0.12, 550.0, 550.0),
        MapDef("forest", "Orman", 4200, "Toprak yol, sık tümsekler.", Surface.DIRT, 9.81, "amb_forest", 5505L, 4.0, 0.2, 600.0, 500.0),
        MapDef("rooftops", "Gece çatıları", 5000, "Binalar arası boşluklar ve dik rampalar.", Surface.ASPHALT, 9.81, "amb_rooftops", 13413L, 2.5, 0.15, 250.0, 550.0, pits = 2.2),
        MapDef("mountain", "Dağ", 5800, "Kayalık, çok dik yamaçlar.", Surface.ROCK, 9.81, "amb_mountain", 6606L, 6.0, 0.25, 800.0, 400.0, maxSlope = 1.2),
        MapDef("beach", "Plaj", 7200, "Yumuşak kum, uzun düzlükler.", Surface.SAND, 9.81, "amb_beach", 7707L, 2.5, 0.1, 500.0, 600.0, maxSlope = 0.9),
        MapDef("cave", "Mağara", 8600, "Kaya zemin, sert inişler.", Surface.ROCK, 9.81, "amb_cave", 8808L, 4.5, 0.2, 600.0, 450.0, maxSlope = 1.2),
        MapDef("mars", "Mars", 9300, "Kırmızı toz, düşük yer çekimi, uzun sıçramalar.", Surface.MARS, 3.71, "amb_mars", 14514L, 3.5, 0.15, 450.0, 500.0, maxSlope = 0.8),
        MapDef("swamp", "Bataklık", 10000, "Çamurda tutunmak zor.", Surface.MUD, 9.81, "amb_swamp", 9909L, 3.0, 0.15, 600.0, 550.0),
        MapDef("volcano", "Volkan", 11500, "Çok dik bazalt yamaçlar, büyük boşluklar, gürleyen yer.", Surface.ROCK, 9.81, "amb_volcano", 15615L, 6.5, 0.35, 500.0, 420.0, maxSlope = 1.2, pits = 1.5),
        MapDef("site", "Şantiye", 13000, "Asfalt zemin ve çok rampa.", Surface.ASPHALT, 9.81, "amb_city", 10110L, 2.5, 0.15, 300.0, 600.0),
        MapDef("deepsea", "Deniz tabanı", 14500, "Su altında yumuşak ve geniş zemin; ağır, yavaş ve uzun süzülen hareket.", Surface.SAND, 4.9, "amb_deepsea", 16716L, 2.5, 0.1, 500.0, 500.0, drag = 6.0)
    )
    fun byId(id: String): MapDef = all.first { it.id == id }
}

data class Upgrades(val engine: Int = 0, val susp: Int = 0, val tire: Int = 0, val fuel: Int = 0)  // her biri 0..10

enum class UpgradeKind(val label: String) { ENGINE("Motor"), SUSP("Süspansiyon"), TIRE("Lastik"), FUEL("Yakıt deposu") }

/** En ucuz geliştirme tabanı (altın). */
const val UPGRADE_BASE_MIN = 60

/** taban = max(60, fiyat/25); bedel = round(taban × 1.6^seviye). Tür bedeli değiştirmez. */
fun upgradeCost(v: VehicleDef, kind: UpgradeKind, level: Int): Int {
    val base = maxOf(UPGRADE_BASE_MIN, v.price / 25)
    return Math.round(base * 1.6.pow(level)).toInt()
}

fun VehicleDef.upgraded(u: Upgrades): VehicleDef = copy(
    torque = torque * (1 + 0.08 * u.engine),
    fuelL = fuelL * (1 + 0.10 * u.fuel),
    suspK = suspK * (1 + 0.04 * u.susp),
    suspC = suspC * (1 + 0.07 * u.susp))

fun gripMul(u: Upgrades): Double = 1 + 0.06 * u.tire
