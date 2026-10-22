import com.yokus.app.core.*
import java.io.File

/** sounds/<anahtar>.mp3 (yoksa .wav) dosyasını ffmpeg ile 16 bit PCM'e çözen test bankası. Sonuçlar süreç boyunca önbelleklenir. */
class FBank(val dir: String = DEFAULT_DIR) : Bank {
    override fun get(key: String): Pcm? = CACHE.getOrPut(dir + "/" + key) { decode(key) }

    private fun decode(key: String): Pcm? {
        val f = listOf("mp3", "wav").map { File(dir, "$key.$it") }.firstOrNull { it.exists() } ?: return null
        val probe = ProcessBuilder("ffprobe", "-v", "error", "-select_streams", "a:0", "-show_entries", "stream=channels,sample_rate", "-of", "csv=p=0", f.path)
            .start().inputStream.bufferedReader().readText().trim().split(",")
        val rate = probe[0].toInt(); val ch = probe[1].toInt().coerceIn(1, 2)
        val raw = ProcessBuilder("ffmpeg", "-v", "error", "-i", f.path, "-ac", "$ch", "-f", "s16le", "-").start().inputStream.readBytes()
        val n = raw.size / 2 / ch
        if (n <= 0) return null
        val l = ShortArray(n); val r = if (ch == 2) ShortArray(n) else null
        fun s16(i: Int) = ((raw[i * 2].toInt() and 255) or (raw[i * 2 + 1].toInt() shl 8)).toShort()
        for (i in 0 until n) { l[i] = s16(i * ch); if (r != null) r[i] = s16(i * ch + 1) }
        return Pcm(l, r, rate)
    }

    companion object {
        const val DEFAULT_DIR = "/home/user/bosyol/sounds"
        private val CACHE = HashMap<String, Pcm?>()
    }
}

/** Hiçbir şey döndürmeyen banka (eksik ses testi). */
class NullBank : Bank { override fun get(key: String): Pcm? = null }
