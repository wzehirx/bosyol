import com.bosyol.app.core.*
import java.io.File
import java.io.DataOutputStream
import java.io.FileOutputStream

class FBank(val dir: String) : Bank {
    val cache = HashMap<String, Pcm?>()
    override fun isCustom(key: String) = false
    override fun get(key: String): Pcm? = cache.getOrPut(key) {
        val slot = Slots.byKey(key) ?: return@getOrPut null
        val f = File(dir, slot.defaultFile ?: return@getOrPut null)
        val probe = ProcessBuilder("ffprobe", "-v", "error", "-show_entries", "stream=channels,sample_rate", "-of", "csv=p=0", f.path).start().inputStream.bufferedReader().readText().trim().split(",")
        val rate = probe[0].toInt(); val ch = probe[1].toInt().coerceAtMost(2)
        val raw = ProcessBuilder("ffmpeg", "-v", "error", "-i", f.path, "-ac", "$ch", "-f", "s16le", "-").start().inputStream.readBytes()
        val n = raw.size / 2 / ch
        val l = ShortArray(n); val r = if (ch == 2) ShortArray(n) else null
        for (i in 0 until n) {
            l[i] = ((raw[(i * ch) * 2].toInt() and 255) or (raw[(i * ch) * 2 + 1].toInt() shl 8)).toShort()
            if (r != null) r[i] = ((raw[(i * ch + 1) * 2].toInt() and 255) or (raw[(i * ch + 1) * 2 + 1].toInt() shl 8)).toShort()
        }
        val p = Pcm(l, r, rate)
        if (Slots.isEngineLoop(key)) p else p
    }
}

fun main(args: Array<String>) {
    val veh = when (args.getOrNull(0)) { "truck" -> Vehicles.truck; "sport" -> Vehicles.sport; "ev" -> Vehicles.ev; else -> Vehicles.sedan }
    val env = Envs.byId(args.getOrNull(1) ?: "forest")
    val mix = Mixer(); val set = GameSettings()
    val log = ArrayList<String>()
    val g = Game(mix, FBank("/home/user/surus/sounds"), object : Speaker { override fun say(text: String, urgent: Boolean) { log.add(text) } }, set, veh, env)
    g.start()
    val total = 90.0; val block = 256
    val out = FloatArray(block * 2)
    val pcm = java.io.ByteArrayOutputStream()
    var t = 0.0; var nextLog = 0.0
    val t0 = System.nanoTime()
    val engineProbe = ArrayList<String>()
    var blocks = 0
    while (t < total) {
        // senaryo: 0-15 gaz, 15-22 bırak, 22-30 fren, 30-60 otomatik, 60-70 gaz, 70-90 fren tut
        g.gasHold = (t < 15) || (t in 60.0..70.0)
        g.brakeHold = (t in 22.0..30.0) || t > 70
        if (t >= 30 && t < 30.05) g.setAuto(true)
        if (t >= 60 && t < 60.05) g.setAuto(false)
        if (blocks % 4 == 0) g.update(block * 4.0 / OUT_RATE)
        mix.render(out, block)
        for (i in 0 until block * 2) { val s = (out[i].coerceIn(-1f, 1f) * 32767).toInt(); pcm.write(s and 255); pcm.write((s shr 8) and 255) }
        if (t >= nextLog) { engineProbe.add(String.format("t=%4.0f v=%5.1f km/h gear=%d rpm=%5.0f brk=%.2f voices=%d", t, g.v * 3.6, g.gear + 1, g.rpm, g.brk, mix.voiceCount)); nextLog += 2.5 }
        t += block.toDouble() / OUT_RATE; blocks++
    }
    val el = (System.nanoTime() - t0) / 1e9
    val data = pcm.toByteArray()
    DataOutputStream(FileOutputStream(args.getOrNull(2) ?: "out.wav")).use { o ->
        fun i32(v: Int) { o.write(v and 255); o.write((v shr 8) and 255); o.write((v shr 16) and 255); o.write((v shr 24) and 255) }
        fun i16(v: Int) { o.write(v and 255); o.write((v shr 8) and 255) }
        o.writeBytes("RIFF"); i32(36 + data.size); o.writeBytes("WAVEfmt "); i32(16); i16(1); i16(2); i32(OUT_RATE); i32(OUT_RATE * 4); i16(4); i16(16); o.writeBytes("data"); i32(data.size); o.write(data)
    }
    engineProbe.forEach { println(it) }
    println("announcements: " + log.take(12))
    println(String.format("render+game cpu: %.2fs for %.0fs audio (%.1f%%)", el, total, el / total * 100))
}
