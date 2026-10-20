package com.bosyol.app

import android.content.Context
import com.bosyol.app.core.GameSettings
import com.bosyol.app.core.VehicleDef
import com.bosyol.app.core.Vehicles
import org.json.JSONArray
import org.json.JSONObject

class CustomVehicle(val id: String, var name: String, var preset: String, var rpm: Int) {
    val prefix get() = "cv_$id"
    fun def(): VehicleDef = (Vehicles.presets[preset] ?: Vehicles.sedan).copyAsCustom("custom_$id", name, prefix, rpm)
}

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("bosyol", Context.MODE_PRIVATE)
    val game = GameSettings()
    var vehicle: String get() = sp.getString("vehicle", "sedan")!!; set(v) { sp.edit().putString("vehicle", v).apply() }
    var env: String get() = sp.getString("env", "forest")!!; set(v) { sp.edit().putString("env", v).apply() }
    var volKeys: Boolean get() = sp.getBoolean("volKeys", false); set(v) { sp.edit().putBoolean("volKeys", v).apply() }
    var tts: Boolean get() = sp.getBoolean("tts", true); set(v) { sp.edit().putBoolean("tts", v).apply() }

    init {
        game.traffic = sp.getBoolean("traffic", true); game.latch = sp.getBoolean("latch", true)
        game.pace = sp.getBoolean("pace", true); game.callouts = sp.getBoolean("callouts", false); game.shiftSay = sp.getBoolean("shiftSay", false)
        game.volMaster = sp.getInt("volMaster", 85); game.volEngine = sp.getInt("volEngine", 80); game.volWorld = sp.getInt("volWorld", 80)
    }

    fun saveGame() {
        sp.edit().putBoolean("traffic", game.traffic).putBoolean("latch", game.latch).putBoolean("pace", game.pace)
            .putBoolean("callouts", game.callouts).putBoolean("shiftSay", game.shiftSay)
            .putInt("volMaster", game.volMaster).putInt("volEngine", game.volEngine).putInt("volWorld", game.volWorld).apply()
    }

    fun customVehicles(): MutableList<CustomVehicle> {
        val out = ArrayList<CustomVehicle>()
        val a = try { JSONArray(sp.getString("vehicles", "[]")) } catch (e: Throwable) { JSONArray() }
        for (i in 0 until a.length()) { val o = a.getJSONObject(i); out.add(CustomVehicle(o.getString("id"), o.getString("name"), o.optString("preset", "binek"), o.optInt("rpm", 3000))) }
        return out
    }

    fun saveCustomVehicles(list: List<CustomVehicle>) {
        val a = JSONArray()
        list.forEach { a.put(JSONObject().put("id", it.id).put("name", it.name).put("preset", it.preset).put("rpm", it.rpm)) }
        sp.edit().putString("vehicles", a.toString()).apply()
    }
}
