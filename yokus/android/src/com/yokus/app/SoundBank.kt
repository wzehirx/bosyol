package com.yokus.app

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import com.yokus.app.core.Bank
import com.yokus.app.core.Pcm
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** assets/sounds/<anahtar>.mp3 (yoksa .wav) dosyalarını çözer; çözülenler bellekte tutulur, dosya yoksa null. */
class SoundBank(private val ctx: Context) : Bank {
    // Ses iş parçacığı da get çağırır: çözülmüş ve eksik anahtarlar kilitsiz okunur,
    // yalnızca ilk çözüm kilit altında yapılır (aynı dosya iki kez çözülmez).
    private val cache = java.util.concurrent.ConcurrentHashMap<String, Pcm>()
    private val missing = java.util.concurrent.ConcurrentHashMap<String, Boolean>()
    private val loadLock = Any()

    override fun get(key: String): Pcm? {
        cache[key]?.let { return it }
        if (missing.containsKey(key)) return null
        synchronized(loadLock) {
            cache[key]?.let { return it }
            if (missing.containsKey(key)) return null
            val p = try { load(key) } catch (e: Throwable) { null }
            if (p == null || p.l.isEmpty()) missing[key] = true else cache[key] = p
            return if (p == null || p.l.isEmpty()) null else p
        }
    }

    private fun load(key: String): Pcm? {
        for (ext in arrayOf("mp3", "wav")) {
            val path = "sounds/$key.$ext"
            val afd = try { ctx.assets.openFd(path) } catch (e: java.io.IOException) { null }
            if (afd == null) {
                // Sıkıştırılmış varlık (openFd açamaz): baytları okuyup bellekten çöz. Dosya hiç yoksa sıradaki uzantıya geç.
                val bytes = try { ctx.assets.open(path).use { it.readBytes() } } catch (e: java.io.IOException) { continue }
                if (ext == "wav") parseWav(bytes)?.let { return it }
                val ex = MediaExtractor(); ex.setDataSource(BytesSource(bytes))
                return decode(ex)
            }
            if (ext == "wav") {
                val bytes = try { afd.createInputStream().use { it.readBytes() } } finally { afd.close() }
                parseWav(bytes)?.let { return it }
                val afd2 = ctx.assets.openFd(path)
                val ex = MediaExtractor(); ex.setDataSource(afd2.fileDescriptor, afd2.startOffset, afd2.length); afd2.close()
                return decode(ex)
            }
            val ex = MediaExtractor(); ex.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length); afd.close()
            return decode(ex)
        }
        return null
    }

    private class BytesSource(private val d: ByteArray) : android.media.MediaDataSource() {
        override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
            if (position >= d.size) return -1
            val n = minOf(size.toLong(), d.size - position).toInt()
            System.arraycopy(d, position.toInt(), buffer, offset, n); return n
        }
        override fun getSize() = d.size.toLong()
        override fun close() {}
    }

    private class Grow { var a = ShortArray(1 shl 16); var n = 0
        fun add(v: Short) { if (n == a.size) a = a.copyOf(a.size * 2); a[n++] = v }
        fun get() = a.copyOf(n) }

    companion object {
        /** 16 bit PCM WAV'ı doğrudan okur; başka biçimse null (kodek yoluna düşülür). */
        fun parseWav(b: ByteArray): Pcm? {
            if (b.size < 44 || String(b, 0, 4) != "RIFF" || String(b, 8, 4) != "WAVE") return null
            val bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN)
            var pos = 12; var ch = 0; var rate = 0; var bits = 0; var fmtTag = 0
            while (pos + 8 <= b.size) {
                val id = String(b, pos, 4); var sz = bb.getInt(pos + 4)
                val body = pos + 8
                if (id == "fmt ") {
                    if (sz < 16 || body + 16 > b.size) return null
                    fmtTag = bb.getShort(body).toInt() and 0xFFFF; ch = bb.getShort(body + 2).toInt()
                    rate = bb.getInt(body + 4); bits = bb.getShort(body + 14).toInt()
                    // WAVE_FORMAT_EXTENSIBLE: gerçek biçim alt türün ilk iki baytında
                    if (fmtTag == 0xFFFE && sz >= 26 && body + 26 <= b.size) fmtTag = bb.getShort(body + 24).toInt() and 0xFFFF
                } else if (id == "data") {
                    if (fmtTag != 1 || bits != 16 || ch < 1 || ch > 2 || rate < 4000 || rate > 192000) return null
                    if (sz < 0 || body + sz > b.size) sz = b.size - body  // akış veya bozuk boyut: kalan her şey
                    val frames = sz / (2 * ch)
                    val l = ShortArray(frames); val r = if (ch > 1) ShortArray(frames) else null
                    for (i in 0 until frames) {
                        l[i] = bb.getShort(body + i * 2 * ch)
                        if (r != null) r[i] = bb.getShort(body + i * 2 * ch + 2)
                    }
                    return Pcm(l, r, rate)
                }
                if (sz < 0) return null  // bozuk öbek boyutu: geriye sarıp sonsuz döngüye girme
                val next = body.toLong() + sz + (sz and 1)
                if (next > b.size) return null
                pos = next.toInt()
            }
            return null
        }

        fun decode(ex: MediaExtractor): Pcm {
            try {
                var track = -1
                for (i in 0 until ex.trackCount) if (ex.getTrackFormat(i).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) { track = i; break }
                if (track < 0) throw IllegalStateException("no audio")
                ex.selectTrack(track)
                val fmt = ex.getTrackFormat(track)
                val mime = fmt.getString(MediaFormat.KEY_MIME)!!
                var rate = fmt.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                var ch = fmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                var enc = AudioFormat.ENCODING_PCM_16BIT
                val codec = MediaCodec.createDecoderByType(mime)
                try {
                codec.configure(fmt, null, null, 0); codec.start()
                val L = Grow(); val R = Grow()
                val info = MediaCodec.BufferInfo()
                var inDone = false; var outDone = false; var idle = 0
                while (!outDone) {
                    if (!inDone) {
                        val ii = codec.dequeueInputBuffer(5000)
                        if (ii >= 0) {
                            val buf = codec.getInputBuffer(ii)!!
                            val sz = ex.readSampleData(buf, 0)
                            if (sz < 0) { codec.queueInputBuffer(ii, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM); inDone = true }
                            else { codec.queueInputBuffer(ii, 0, sz, ex.sampleTime, 0); ex.advance() }
                        }
                    }
                    val oi = codec.dequeueOutputBuffer(info, 5000)
                    if (oi == MediaCodec.INFO_TRY_AGAIN_LATER) { if (inDone && ++idle > 400) break }  // EOS hiç gelmezse ~2 sn sonra bırak
                    else idle = 0
                    if (oi == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        val of = codec.outputFormat
                        rate = of.getInteger(MediaFormat.KEY_SAMPLE_RATE); ch = of.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        if (of.containsKey(MediaFormat.KEY_PCM_ENCODING)) enc = of.getInteger(MediaFormat.KEY_PCM_ENCODING)
                    } else if (oi >= 0) {
                        val ob = codec.getOutputBuffer(oi)!!
                        ob.position(info.offset); ob.limit(info.offset + info.size); ob.order(ByteOrder.nativeOrder())
                        if (enc == AudioFormat.ENCODING_PCM_FLOAT) {
                            val fb = ob.asFloatBuffer(); val frames = fb.remaining() / ch
                            for (f in 0 until frames) {
                                L.add((fb.get(f * ch).coerceIn(-1f, 1f) * 32767).toInt().toShort())
                                if (ch > 1) R.add((fb.get(f * ch + 1).coerceIn(-1f, 1f) * 32767).toInt().toShort())
                            }
                        } else {
                            val sb = ob.asShortBuffer(); val frames = sb.remaining() / maxOf(1, ch)
                            for (f in 0 until frames) { L.add(sb.get(f * ch)); if (ch > 1) R.add(sb.get(f * ch + 1)) }
                        }
                        codec.releaseOutputBuffer(oi, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outDone = true
                    }
                }
                return Pcm(L.get(), if (ch > 1) R.get() else null, rate)
                } finally { try { codec.stop() } catch (e: Throwable) {}; codec.release() }
            } finally { ex.release() }
        }
    }
}
