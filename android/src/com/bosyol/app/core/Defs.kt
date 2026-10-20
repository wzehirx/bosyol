package com.bosyol.app.core

/**
 * Araç tanımı. Sayılar gerçek araçlardan: vites oranları, son oran, lastik yarıçapı, tork eğrisi, ağırlık ve sürtünmeler.
 * layers: motor kayıtlarının kaydedildiği devirler (rölanti, düşük, orta, yüksek).
 */
class VehicleDef(
    val id: String, val name: String, val desc: String,
    /** Yerleşik ses seti öneki (e0, e1, e2); kendi araçlarda null */
    val set: String?, val layers: IntArray,
    val idle: Double, val redline: Double,
    /** Tork eğrisi: rölantide, zirvede ve devir sınırında Nm */
    val torqueIdle: Double, val torquePeak: Double, val peakRpm: Double, val torqueRed: Double,
    val gears: DoubleArray, val final: Double, val r: Double, val mass: Double,
    val cd: Double, val area: Double, val crr: Double, val efficiency: Double,
    /** Gaz kapalıyken motor freni (devir sınırında Nm) */
    val engBrakeNm: Double,
    /** Otomatik vites: hafif gazda ve tam gazda vites büyütme devri, hafif gazda küçültme devri */
    val upLight: Double, val upFull: Double, val downLight: Double,
    /** Tork konvertörünün kilitlendiği devir ve kalkışta motorun çıkabileceği devir */
    val launch: Double,
    /** Lastik tutunması (kuru asfalt, g) ve en sert fren (m/s²) */
    val mu: Double, val brakeMax: Double,
    val color: Int, val kind: String,
    val synth: Boolean = false,
    val customPrefix: String? = null,
    val customRpm: Int = 3000,
    /** Elektrikli araç: en yüksek güç (W) */
    val maxPower: Double = 0.0
) {
    fun copyAsCustom(id: String, name: String, prefix: String, rpm: Int) = VehicleDef(
        id, name, "Kendi aracın", null, IntArray(0), idle, redline, torqueIdle, torquePeak, peakRpm, torqueRed,
        gears, final, r, mass, cd, area, crr, efficiency, engBrakeNm, upLight, upFull, downLight, launch, mu, brakeMax,
        0xFF6D4FA1.toInt(), kind, false, prefix, rpm
    )
}

object Vehicles {
    /** 2.5 litre benzinli aile sedanı, 8 ileri tork konvertörlü otomatik (Camry sınıfı) */
    val sedan = VehicleDef("sedan", "Aile sedanı", "2.5 benzinli, 8 ileri otomatik", "e0", intArrayOf(840, 2340, 3550, 4300),
        idle = 750.0, redline = 6000.0, torqueIdle = 150.0, torquePeak = 250.0, peakRpm = 4000.0, torqueRed = 205.0,
        gears = doubleArrayOf(5.519, 3.184, 2.050, 1.492, 1.235, 1.000, 0.801, 0.673), final = 2.796, r = 0.334, mass = 1550.0,
        cd = 0.27, area = 2.24, crr = 0.012, efficiency = 0.88, engBrakeNm = 55.0,
        upLight = 1900.0, upFull = 5400.0, downLight = 1100.0, launch = 2300.0, mu = 0.95, brakeMax = 9.0,
        color = 0xFF3D6FA8.toInt(), kind = "sedan")
    /** 2.1 litre dizel panelvan, 7 ileri otomatik (Sprinter sınıfı) */
    val truck = VehicleDef("truck", "Dizel kamyonet", "2.1 dizel, 7 ileri otomatik, ağır", "e1", intArrayOf(750, 1150, 1800, 2400),
        idle = 750.0, redline = 3800.0, torqueIdle = 220.0, torquePeak = 360.0, peakRpm = 1700.0, torqueRed = 250.0,
        gears = doubleArrayOf(4.377, 2.859, 1.921, 1.368, 1.000, 0.820, 0.728), final = 3.923, r = 0.355, mass = 2700.0,
        cd = 0.36, area = 3.5, crr = 0.014, efficiency = 0.85, engBrakeNm = 75.0,
        upLight = 1500.0, upFull = 3300.0, downLight = 950.0, launch = 1700.0, mu = 0.85, brakeMax = 8.0,
        color = 0xFF8C5A2B.toInt(), kind = "truck")
    /** 3.0 litre turbo spor coupe, 7 ileri çift kavrama (911 sınıfı) */
    val sport = VehicleDef("sport", "Spor coupe", "3.0 turbo benzinli, 7 ileri çift kavrama", "e2", intArrayOf(1000, 2100, 3300, 4600),
        idle = 950.0, redline = 7200.0, torqueIdle = 220.0, torquePeak = 450.0, peakRpm = 3500.0, torqueRed = 340.0,
        gears = doubleArrayOf(3.91, 2.29, 1.65, 1.30, 1.08, 0.88, 0.62), final = 3.44, r = 0.346, mass = 1550.0,
        cd = 0.29, area = 2.0, crr = 0.011, efficiency = 0.90, engBrakeNm = 65.0,
        upLight = 2300.0, upFull = 6600.0, downLight = 1300.0, launch = 3200.0, mu = 1.05, brakeMax = 10.5,
        color = 0xFFC0302A.toInt(), kind = "sport")
    /** Elektrikli sedan, tek oran */
    val ev = VehicleDef("ev", "Elektrikli araç", "Sessiz, ıslık gibi motor, tek vites", null, IntArray(0),
        idle = 0.0, redline = 15000.0, torqueIdle = 400.0, torquePeak = 400.0, peakRpm = 5000.0, torqueRed = 130.0,
        gears = doubleArrayOf(1.0), final = 9.0, r = 0.33, mass = 1850.0,
        cd = 0.23, area = 2.3, crr = 0.010, efficiency = 0.92, engBrakeNm = 70.0,
        upLight = 1e9, upFull = 1e9, downLight = -1.0, launch = 0.0, mu = 0.95, brakeMax = 9.0,
        color = 0xFFE8ECEF.toInt(), kind = "ev", synth = true, maxPower = 210000.0)
    val builtIn = listOf(sedan, truck, sport, ev)
    /** Kendi araçlar için sürüş karakterleri */
    val presets = linkedMapOf("binek" to sedan, "spor" to sport, "agir" to truck)
    val presetNames = linkedMapOf("binek" to "Binek otomobil", "spor" to "Spor araba", "agir" to "Ağır dizel")
}

class Emitter(val key: String, val n: Int, val latMin: Double, val latMax: Double, val side: Int, val gain: Float, val high: Boolean = false)
class OneShot(val key: String, val everyMin: Double, val everyMax: Double, val gain: Float, val latMin: Double = 20.0, val latMax: Double = 50.0, val stereo: Boolean = false)
class Cabin(val key: String, val gain: Float)

class EnvDef(
    val id: String, val name: String, val desc: String,
    val bed: String, val bedGain: Float,
    val cabin: List<Cabin>, val emitters: List<Emitter>, val oneshots: List<OneShot>,
    val trafficMin: Double, val trafficMax: Double, val siren: Boolean,
    val curveMin: Double, val curveMax: Double, val width: Double,
    val night: Boolean = false, val sea: Boolean = false, val wet: Boolean = false, val highway: Boolean = false,
    val sky: Int, val skyLow: Int, val ground: Int, val ground2: Int, val fogNear: Double, val fogFar: Double, val props: String,
    /** Yol yüzeyinin tutunma çarpanı: kuru asfalt 1, ıslak yol daha az */
    val grip: Double = 1.0
)

object Envs {
    val list = listOf(
        EnvDef("forest", "Orman yolu", "Gündüz, kuşlar ve rüzgâr", "bed_forest", 0.9f, emptyList(),
            listOf(Emitter("em_birds", 5, 9.0, 40.0, 0, 1.1f, high = true)),
            listOf(OneShot("os_woodpecker", 30.0, 70.0, 0.8f, 15.0, 40.0)),
            9.0, 22.0, false, 90.0, 500.0, 3.5,
            sky = 0xFF7FB0DA.toInt(), skyLow = 0xFFCFE3EF.toInt(), ground = 0xFF3F6A33.toInt(), ground2 = 0xFF477539.toInt(), fogNear = 40.0, fogFar = 330.0, props = "trees"),
        EnvDef("night", "Gece kır yolu", "Cırcır böcekleri, baykuş", "bed_night", 0.9f, emptyList(),
            listOf(Emitter("em_frog", 3, 12.0, 45.0, 0, 0.9f)),
            listOf(OneShot("os_owl", 22.0, 50.0, 0.9f, 20.0, 50.0), OneShot("os_wolf", 70.0, 140.0, 0.5f, 120.0, 250.0), OneShot("os_dog", 45.0, 100.0, 0.6f, 40.0, 90.0)),
            18.0, 40.0, false, 80.0, 450.0, 3.3, night = true,
            sky = 0xFF05080F.toInt(), skyLow = 0xFF101A2E.toInt(), ground = 0xFF0B140F.toInt(), ground2 = 0xFF0E1913.toInt(), fogNear = 10.0, fogFar = 140.0, props = "trees"),
        EnvDef("coast", "Sahil yolu", "Sağda deniz, dalgalar, martılar", "bed_coast", 0.6f, emptyList(),
            listOf(Emitter("em_waves", 4, 26.0, 40.0, 1, 1.4f), Emitter("em_gulls", 2, 20.0, 70.0, 1, 0.7f, high = true)),
            emptyList(), 10.0, 24.0, false, 150.0, 700.0, 3.5, sea = true,
            sky = 0xFF8CC8EA.toInt(), skyLow = 0xFFE2F1F8.toInt(), ground = 0xFFC9B98A.toInt(), ground2 = 0xFFD2C395.toInt(), fogNear = 60.0, fogFar = 420.0, props = "coast"),
        EnvDef("city", "Şehir içi", "Yoğun cadde, kalabalık, siren", "bed_city", 0.8f, emptyList(),
            listOf(Emitter("em_crowd", 3, 9.0, 15.0, 0, 0.8f)),
            listOf(OneShot("os_dog", 60.0, 130.0, 0.5f, 12.0, 30.0)),
            3.0, 9.0, true, 200.0, 900.0, 3.4,
            sky = 0xFFC9B08E.toInt(), skyLow = 0xFFE8D9C2.toInt(), ground = 0xFF4A4B4F.toInt(), ground2 = 0xFF515257.toInt(), fogNear = 30.0, fogFar = 300.0, props = "city"),
        EnvDef("highway", "Otoyol", "Hızlı ve yoğun trafik", "bed_highway", 0.7f, emptyList(), emptyList(), emptyList(),
            2.5, 7.0, false, 600.0, 1800.0, 3.7, highway = true,
            sky = 0xFF93A7BB.toInt(), skyLow = 0xFFD5DDE5.toInt(), ground = 0xFF6D7558.toInt(), ground2 = 0xFF747C5E.toInt(), fogNear = 60.0, fogFar = 480.0, props = "highway"),
        EnvDef("rain", "Yağmurlu yol", "Sağanak, silecek, gök gürültüsü", "bed_rain", 0.8f,
            listOf(Cabin("cab_rainroof", 1.0f), Cabin("cab_wipers", 0.55f)), emptyList(),
            listOf(OneShot("os_thunder", 25.0, 60.0, 1.0f, stereo = true)),
            10.0, 25.0, false, 120.0, 600.0, 3.5, wet = true,
            sky = 0xFF3E4752.toInt(), skyLow = 0xFF6A7480.toInt(), ground = 0xFF263426.toInt(), ground2 = 0xFF2B3A2B.toInt(), fogNear = 12.0, fogFar = 170.0, props = "trees", grip = 0.62),
        EnvDef("mountain", "Dağ geçidi", "Uğuldayan rüzgâr, dere, dar virajlar", "bed_mountain", 0.8f, emptyList(),
            listOf(Emitter("em_river", 2, 14.0, 26.0, -1, 1.2f)),
            listOf(OneShot("os_crows", 40.0, 90.0, 0.6f, 30.0, 80.0)),
            20.0, 45.0, false, 45.0, 220.0, 3.2,
            sky = 0xFFA9BCCD.toInt(), skyLow = 0xFFE4EAEF.toInt(), ground = 0xFFDCE3E7.toInt(), ground2 = 0xFFE6ECEF.toInt(), fogNear = 30.0, fogFar = 300.0, props = "pines", grip = 0.85)
    )
    fun byId(id: String) = list.firstOrNull { it.id == id } ?: list[0]
}

/** Değiştirilebilen her ses bir yuvadır. */
class Slot(val key: String, val label: String, val group: String, val defaultFile: String?)

object Slots {
    val trafficKeys = listOf("tr_1", "tr_2", "tr_3")
    val all: List<Slot> by lazy {
        val out = ArrayList<Slot>()
        val parts = listOf("idle" to "rölanti", "low_on" to "düşük devir, gazda", "low_off" to "düşük devir, gaz kesik",
            "med_on" to "orta devir, gazda", "med_off" to "orta devir, gaz kesik", "high_on" to "yüksek devir, gazda",
            "high_off" to "yüksek devir, gaz kesik", "start" to "marş")
        for (v in listOf(Vehicles.sedan, Vehicles.truck, Vehicles.sport)) for ((p, l) in parts) out.add(Slot("${v.set}_$p", "${v.name}: $l", v.name, "${v.set}_$p.wav"))
        out.add(Slot("ev_loop", "Elektrikli araç: motor döngüsü (isteğe bağlı, sentez yerine)", "Elektrikli araç", null))
        val names = mapOf("em_birds" to "Kuşlar", "em_frog" to "Kurbağalar", "em_waves" to "Dalgalar", "em_gulls" to "Martılar", "em_river" to "Dere",
            "em_crowd" to "Kalabalık", "os_woodpecker" to "Ağaçkakan", "os_owl" to "Baykuş", "os_wolf" to "Kurt", "os_dog" to "Köpek havlaması",
            "os_thunder" to "Gök gürültüsü", "os_crows" to "Kargalar", "cab_rainroof" to "Tavana yağmur", "cab_wipers" to "Silecek")
        val seen = LinkedHashMap<String, MutableList<String>>()
        for (e in Envs.list) {
            out.add(Slot(e.bed, "${e.name}: ortam sesi", "Ortamlar", e.bed + ".mp3"))
            (e.cabin.map { it.key } + e.emitters.map { it.key } + e.oneshots.map { it.key }).forEach { seen.getOrPut(it) { ArrayList() }.add(e.name) }
        }
        for ((k, envs) in seen) out.add(Slot(k, "${names[k] ?: k} (${envs.joinToString(", ")})", "Ortamlar", "$k.mp3"))
        out.add(Slot("tr_1", "Trafik aracı 1, binek", "Trafik ve efektler", "e0_med_on.wav"))
        out.add(Slot("tr_2", "Trafik aracı 2, dizel", "Trafik ve efektler", "e1_med_on.wav"))
        out.add(Slot("tr_3", "Trafik aracı 3, spor", "Trafik ve efektler", "e2_low_on.wav"))
        out.add(Slot("em_siren", "Ambulans sireni (Şehir içi)", "Trafik ve efektler", "em_siren.mp3"))
        out.add(Slot("horn", "Korna", "Trafik ve efektler", "horn.mp3"))
        out.add(Slot("screech", "Lastik ötmesi", "Trafik ve efektler", "screech.mp3"))
        out.add(Slot("crash", "Kaza", "Trafik ve efektler", "crash.mp3"))
        out
    }
    fun byKey(k: String) = all.firstOrNull { it.key == k }
    /** Kendi araç sesleri: önek_loop, önek_idle, önek_off, önek_high, önek_start */
    val customParts = listOf("loop" to "Motor sesi (gerekli, orta devirde bir döngü)", "idle" to "Rölanti (isteğe bağlı)",
        "off" to "Gaz kesik döngü (isteğe bağlı)", "high" to "Yüksek devir (isteğe bağlı)", "start" to "Marş (isteğe bağlı)")
    fun isEngineLoop(k: String) = (Regex("^(e[0-2]|cv_[a-z0-9]+)_").containsMatchIn(k) && !k.endsWith("_start")) || k.startsWith("tr_") || k == "ev_loop"
}
