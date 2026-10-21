package com.yokus.app

import android.content.Context
import com.yokus.app.core.AudioSettings
import com.yokus.app.core.Maps
import com.yokus.app.core.Vehicles
import com.yokus.app.core.MapDef
import com.yokus.app.core.UpgradeKind
import com.yokus.app.core.Upgrades
import com.yokus.app.core.VehicleDef
import com.yokus.app.core.upgradeCost

/** Kalıcı veriler: altın, sahiplik, geliştirmeler, en iyi mesafeler, ayarlar. */
class Store(ctx: Context) {
    private val sp = ctx.getSharedPreferences("yokus", Context.MODE_PRIVATE)
    val audio = AudioSettings()

    private fun vol(k: String, d: Int) = sp.getInt(k, d).coerceIn(0, 100)

    init {
        audio.tiltTone = sp.getBoolean("tiltTone", true); audio.radar = sp.getBoolean("radar", true)
        audio.distanceCalls = sp.getBoolean("distanceCalls", false)
        audio.balanceAssist = sp.getBoolean("balanceAssist", true)
        audio.volMaster = vol("volMaster", 80); audio.volEngine = vol("volEngine", 80)
        audio.volWorld = vol("volWorld", 70); audio.volCues = vol("volCues", 80)
    }

    /** Ses ayarlarını kaydeder. */
    fun saveAudio() {
        sp.edit().putBoolean("tiltTone", audio.tiltTone).putBoolean("radar", audio.radar)
            .putBoolean("distanceCalls", audio.distanceCalls).putBoolean("balanceAssist", audio.balanceAssist).putInt("volMaster", audio.volMaster)
            .putInt("volEngine", audio.volEngine).putInt("volWorld", audio.volWorld).putInt("volCues", audio.volCues).apply()
    }

    var gold: Int
        get() = maxOf(0, sp.getInt("gold", 0))
        set(v) { sp.edit().putInt("gold", maxOf(0, v)).apply() }

    fun owns(vehicleId: String) = vehicleId == "jeep" || sp.getBoolean("own_v_$vehicleId", false)
    fun ownsMap(id: String) = id == "country" || sp.getBoolean("own_m_$id", false)

    fun buyVehicle(v: VehicleDef): Boolean {
        if (owns(v.id)) return true
        if (gold < v.price) return false
        sp.edit().putInt("gold", gold - v.price).putBoolean("own_v_${v.id}", true).apply()  // tek yazma: yarım kalmaz
        return true
    }

    fun buyMap(m: MapDef): Boolean {
        if (ownsMap(m.id)) return true
        if (gold < m.price) return false
        sp.edit().putInt("gold", gold - m.price).putBoolean("own_m_${m.id}", true).apply()
        return true
    }

    var vehicleId: String
        get() = sp.getString("vehicle", "jeep")!!.let { id -> if (Vehicles.all.any { it.id == id } && owns(id)) id else "jeep" }
        set(v) { sp.edit().putString("vehicle", v).apply() }
    var mapId: String
        get() = sp.getString("map", "country")!!.let { id -> if (Maps.all.any { it.id == id } && ownsMap(id)) id else "country" }
        set(v) { sp.edit().putString("map", v).apply() }

    private fun lv(vehicleId: String, name: String) = sp.getInt("up_${vehicleId}_$name", 0).coerceIn(0, MAX_LEVEL)
    fun upgrades(vehicleId: String) = Upgrades(
        lv(vehicleId, "engine"), lv(vehicleId, "susp"), lv(vehicleId, "tire"), lv(vehicleId, "fuel"))

    fun buyUpgrade(v: VehicleDef, k: UpgradeKind): Boolean {
        val u = upgrades(v.id)
        val lvl = level(u, k)
        if (lvl >= MAX_LEVEL) return false
        val cost = upgradeCost(v, k, lvl)
        if (gold < cost) return false
        sp.edit().putInt("gold", gold - cost).putInt("up_${v.id}_${k.name.lowercase()}", lvl + 1).apply()
        return true
    }

    fun best(mapId: String) = sp.getInt("best_$mapId", 0)
    fun setBest(mapId: String, m: Int) { if (m > best(mapId)) sp.edit().putInt("best_$mapId", m).apply() }

    var volumeKeys: Boolean
        get() = sp.getBoolean("volumeKeys", false)
        set(v) { sp.edit().putBoolean("volumeKeys", v).apply() }
    var latch: Boolean
        get() = sp.getBoolean("latch", false)
        set(v) { sp.edit().putBoolean("latch", v).apply() }

    companion object {
        const val MAX_LEVEL = 10
        fun level(u: Upgrades, k: UpgradeKind) = when (k) {
            UpgradeKind.ENGINE -> u.engine; UpgradeKind.SUSP -> u.susp; UpgradeKind.TIRE -> u.tire; UpgradeKind.FUEL -> u.fuel
        }
    }
}
