package com.yokus.app.core

import kotlin.math.pow

enum class Surface(val mu: Double, val rollKey: String, val label: String) {
    DIRT(0.9, "roll_dirt", "toprak"), SAND(0.6, "roll_sand", "kum"), SNOW(0.45, "roll_snow", "kar"), ICE(0.25, "roll_snow", "buz"),
    MUD(0.5, "roll_mud", "çamur"), MOON(0.7, "roll_dirt", "ay tozu"), ROCK(0.95, "roll_rock", "kaya"), ASPHALT(1.0, "roll_asphalt", "asfalt")
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
    val snowGrip: Double = 1.0)                                         // KAR/BUZ üzerinde μ çarpanı

data class MapDef(
    val id: String, val name: String, val price: Int, val desc: String, val surface: Surface, val gravity: Double,
    val ambKey: String, val seed: Long, val amp: Double, val growth: Double, val rampEvery: Double, val fuelEvery: Double,
    val maxSlope: Double = 1.0)                                         // 1500 m sonrası izin verilen en büyük eğim

object Vehicles {
    val all: List<VehicleDef> = listOf(
        VehicleDef("jeep", "Cip", 0, "Dengeli başlangıç aracı.",
            mass = 300.0, inertia = 565.0, wheelR = 0.4, wheelMass = 20.0, rearX = -1.1, frontX = 1.1, axleY = -0.3,
            suspK = 20760.0, suspC = 2490.0, suspTravel = 0.35,
            torque = 650.0, idleRpm = 1000.0, maxRpm = 6000.0, ratio = 15.20,
            drive = Drive.REAR, airTorque = 5690.0, fuelL = 10.0, fuelUse = 0.12,
            headX = -0.1, headY = 0.85, headR = 0.25,
            engineKey = "eng_jeep", engineRefRpm = 3000.0, enginePitch = 1.0),
        VehicleDef("moto", "Motosiklet", 8000, "Hafif ve güçlü, havada kolay takla atar.",
            mass = 150.0, inertia = 250.0, wheelR = 0.35, wheelMass = 10.0, rearX = -0.7, frontX = 0.7, axleY = -0.25,
            suspK = 8000.0, suspC = 700.0, suspTravel = 0.25,
            torque = 420.0, idleRpm = 1200.0, maxRpm = 7500.0, ratio = 13.70,
            drive = Drive.REAR, airTorque = 6200.0, fuelL = 7.0, fuelUse = 0.09,
            headX = -0.05, headY = 0.95, headR = 0.22,
            engineKey = "eng_moto", engineRefRpm = 3500.0, enginePitch = 1.1),
        VehicleDef("pickup", "Pikap", 15000, "Sağlam ve dayanıklı, orta hızlı.",
            mass = 450.0, inertia = 1000.0, wheelR = 0.45, wheelMass = 28.0, rearX = -1.4, frontX = 1.3, axleY = -0.3,
            suspK = 18000.0, suspC = 1500.0, suspTravel = 0.3,
            torque = 950.0, idleRpm = 900.0, maxRpm = 5500.0, ratio = 17.30,
            drive = Drive.REAR, airTorque = 6400.0, fuelL = 14.0, fuelUse = 0.13,
            headX = -0.2, headY = 0.95, headR = 0.25,
            engineKey = "e1_med_on", engineRefRpm = 3000.0, enginePitch = 1.0),
        VehicleDef("tractor", "Traktör", 25000, "Yavaş ama çok tutunur ve dengelidir.",
            mass = 600.0, inertia = 1600.0, wheelR = 0.55, wheelMass = 40.0, rearX = -1.2, frontX = 1.0, axleY = -0.2,
            suspK = 26000.0, suspC = 2400.0, suspTravel = 0.2,
            torque = 1700.0, idleRpm = 800.0, maxRpm = 3500.0, ratio = 22.40,
            drive = Drive.REAR, airTorque = 7680.0, fuelL = 16.0, fuelUse = 0.12,
            headX = -0.4, headY = 1.2, headR = 0.25,
            engineKey = "eng_tractor", engineRefRpm = 2200.0, enginePitch = 1.0),
        VehicleDef("buggy", "Kum arabası", 35000, "Hafif, yumuşak süspansiyonlu, zıplamayı sever.",
            mass = 250.0, inertia = 900.0, wheelR = 0.42, wheelMass = 18.0, rearX = -1.0, frontX = 1.0, axleY = -0.25,
            suspK = 8500.0, suspC = 700.0, suspTravel = 0.4,
            torque = 650.0, idleRpm = 1100.0, maxRpm = 6500.0, ratio = 16.80,
            drive = Drive.REAR, airTorque = 4320.0, fuelL = 9.0, fuelUse = 0.11,
            headX = -0.1, headY = 0.9, headR = 0.24,
            engineKey = "eng_buggy", engineRefRpm = 3500.0, enginePitch = 1.05),
        VehicleDef("rally", "Ralli arabası", 50000, "Hızlı ve alçak.",
            mass = 330.0, inertia = 1200.0, wheelR = 0.36, wheelMass = 18.0, rearX = -1.15, frontX = 1.15, axleY = -0.2,
            suspK = 16000.0, suspC = 1300.0, suspTravel = 0.2,
            torque = 800.0, idleRpm = 1000.0, maxRpm = 7000.0, ratio = 12.00,
            drive = Drive.ALL, airTorque = 4320.0, fuelL = 11.0, fuelUse = 0.14,
            headX = -0.1, headY = 0.75, headR = 0.22,
            engineKey = "e2_med_on", engineRefRpm = 3500.0, enginePitch = 1.0),
        VehicleDef("monster", "Canavar kamyon", 70000, "Dev tekerler, yumuşak süspansiyon.",
            mass = 700.0, inertia = 1800.0, wheelR = 0.75, wheelMass = 60.0, rearX = -1.4, frontX = 1.4, axleY = -0.1,
            suspK = 24000.0, suspC = 2000.0, suspTravel = 0.5,
            torque = 2400.0, idleRpm = 900.0, maxRpm = 5000.0, ratio = 28.00,
            drive = Drive.ALL, airTorque = 10080.0, fuelL = 18.0, fuelUse = 0.18,
            headX = -0.2, headY = 1.6, headR = 0.28,
            engineKey = "eng_monster", engineRefRpm = 2800.0, enginePitch = 0.9),
        VehicleDef("bus", "Otobüs", 90000, "Uzun dingilli ve ağır.",
            mass = 1200.0, inertia = 4000.0, wheelR = 0.5, wheelMass = 45.0, rearX = -2.4, frontX = 2.2, axleY = -0.4,
            suspK = 40000.0, suspC = 3600.0, suspTravel = 0.3,
            torque = 2000.0, idleRpm = 800.0, maxRpm = 4000.0, ratio = 16.10,
            drive = Drive.REAR, airTorque = 16000.0, fuelL = 24.0, fuelUse = 0.2,
            headX = 1.6, headY = 1.3, headR = 0.28,
            engineKey = "eng_bus", engineRefRpm = 2200.0, enginePitch = 0.9),
        VehicleDef("snowmobile", "Kar motosikleti", 110000, "Kar ve buzda çok iyi tutunur.",
            mass = 220.0, inertia = 700.0, wheelR = 0.33, wheelMass = 12.0, rearX = -0.9, frontX = 0.9, axleY = -0.2,
            suspK = 9000.0, suspC = 800.0, suspTravel = 0.3,
            torque = 420.0, idleRpm = 1200.0, maxRpm = 7000.0, ratio = 14.20,
            drive = Drive.REAR, airTorque = 3360.0, fuelL = 9.0, fuelUse = 0.1,
            headX = -0.1, headY = 0.9, headR = 0.22,
            engineKey = "eng_snowmobile", engineRefRpm = 3500.0, enginePitch = 1.0, snowGrip = 2.2),
        VehicleDef("rover", "Ay aracı", 140000, "Elektrikli ve hafif, Ay'da at gibi zıplar.",
            mass = 200.0, inertia = 800.0, wheelR = 0.38, wheelMass = 15.0, rearX = -1.0, frontX = 1.0, axleY = -0.25,
            suspK = 7000.0, suspC = 600.0, suspTravel = 0.35,
            torque = 380.0, idleRpm = 600.0, maxRpm = 6000.0, ratio = 17.00,
            drive = Drive.ALL, airTorque = 3200.0, fuelL = 10.0, fuelUse = 0.08,
            headX = -0.1, headY = 0.85, headR = 0.24,
            engineKey = "synth", engineRefRpm = 3000.0, enginePitch = 1.0, electric = true),
        VehicleDef("tank", "Tank", 180000, "Çok yavaş, çok tutunur, durdurulamaz.",
            mass = 1100.0, inertia = 2600.0, wheelR = 0.5, wheelMass = 80.0, rearX = -1.5, frontX = 1.5, axleY = -0.3,
            suspK = 60000.0, suspC = 6000.0, suspTravel = 0.2,
            torque = 3200.0, idleRpm = 700.0, maxRpm = 2500.0, ratio = 14.50,
            drive = Drive.ALL, airTorque = 11200.0, fuelL = 30.0, fuelUse = 0.2,
            headX = 0.0, headY = 1.3, headR = 0.28,
            engineKey = "eng_tractor", engineRefRpm = 1600.0, enginePitch = 0.6, snowGrip = 1.5),
        VehicleDef("super", "Süper araba", 250000, "Çok hızlı, kontrolü zor.",
            mass = 350.0, inertia = 600.0, wheelR = 0.32, wheelMass = 16.0, rearX = -1.2, frontX = 1.2, axleY = -0.2,
            suspK = 30000.0, suspC = 2400.0, suspTravel = 0.15,
            torque = 3600.0, idleRpm = 1200.0, maxRpm = 8500.0, ratio = 10.20,
            drive = Drive.ALL, airTorque = 2400.0, fuelL = 12.0, fuelUse = 0.2,
            headX = -0.1, headY = 0.7, headR = 0.2,
            engineKey = "e2_high_on", engineRefRpm = 4500.0, enginePitch = 1.0)
    )
    fun byId(id: String): VehicleDef = all.first { it.id == id }
}

object Maps {
    val all: List<MapDef> = listOf(
        MapDef("country", "Kır yolu", 0, "Toprak yolda hafif tepeler.", Surface.DIRT, 9.81, "amb_country", 1101L, 3.0, 0.15, 500.0, 450.0),
        MapDef("desert", "Çöl", 5000, "Kum tepeleri, tutunma düşük.", Surface.SAND, 9.81, "amb_desert", 2202L, 3.5, 0.15, 600.0, 500.0),
        MapDef("arctic", "Kutup", 12000, "Karlı, kaygan yamaçlar.", Surface.SNOW, 9.81, "amb_arctic", 3303L, 3.5, 0.2, 700.0, 450.0),
        MapDef("moon", "Ay", 20000, "Düşük yer çekimi, uzun sıçramalar.", Surface.MOON, 1.62, "amb_moon", 4404L, 3.5, 0.15, 400.0, 500.0),
        MapDef("forest", "Orman", 30000, "Toprak yol, sık tümsekler.", Surface.DIRT, 9.81, "amb_forest", 5505L, 4.0, 0.2, 600.0, 500.0),
        MapDef("mountain", "Dağ", 40000, "Kayalık, çok dik yamaçlar.", Surface.ROCK, 9.81, "bed_mountain", 6606L, 6.0, 0.25, 800.0, 400.0, maxSlope = 1.2),
        MapDef("beach", "Plaj", 50000, "Yumuşak kum, uzun düzlükler.", Surface.SAND, 9.81, "amb_beach", 7707L, 2.5, 0.1, 500.0, 600.0),
        MapDef("cave", "Mağara", 60000, "Kaya zemin, sert inişler.", Surface.ROCK, 9.81, "amb_cave", 8808L, 4.5, 0.2, 600.0, 450.0, maxSlope = 1.2),
        MapDef("swamp", "Bataklık", 70000, "Çamurda tutunmak zor.", Surface.MUD, 9.81, "amb_swamp", 9909L, 3.0, 0.15, 600.0, 550.0),
        MapDef("site", "Şantiye", 90000, "Asfalt zemin ve çok rampa.", Surface.ASPHALT, 9.81, "bed_city", 10110L, 2.5, 0.15, 300.0, 600.0)
    )
    fun byId(id: String): MapDef = all.first { it.id == id }
}

data class Upgrades(val engine: Int = 0, val susp: Int = 0, val tire: Int = 0, val fuel: Int = 0)  // her biri 0..10

enum class UpgradeKind(val label: String) { ENGINE("Motor"), SUSP("Süspansiyon"), TIRE("Lastik"), FUEL("Yakıt deposu") }

/** taban = max(400, fiyat/25); bedel = round(taban × 1.6^seviye). Tür bedeli değiştirmez. */
fun upgradeCost(v: VehicleDef, kind: UpgradeKind, level: Int): Int {
    val base = maxOf(400, v.price / 25)
    return Math.round(base * 1.6.pow(level)).toInt()
}

fun VehicleDef.upgraded(u: Upgrades): VehicleDef = copy(
    torque = torque * (1 + 0.08 * u.engine),
    fuelL = fuelL * (1 + 0.10 * u.fuel),
    suspK = suspK * (1 + 0.04 * u.susp),
    suspC = suspC * (1 + 0.07 * u.susp))

fun gripMul(u: Upgrades): Double = 1 + 0.06 * u.tire
