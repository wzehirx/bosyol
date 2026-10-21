package com.yokus.app

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import com.yokus.app.core.Bank
import com.yokus.app.core.Pcm
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** assets/sounds/<anahtar>.mp3 (yoksa .wav) dosyalarını çözer; çözülenler bellekte tutulur, dosya yoksa null. */
class SoundBank(private val ctx: Context) : Bank {
    private val cache = HashMap<String, Pcm>()
    private val missing = HashSet<String>()

    @Synchronized override fun get(key: String): Pcm? {
        cache[key]?.let { return it }
        if (key in missing) return null
        val p = try { load(key) } catch (e: Throwable) { null }
        if (p == null) missing.add(key) else cache[key] = p
        return p
    }

    private fun load(key: String): Pcm? {
        for (ext in arrayOf("mp3", "wav")) {
            val path = "sounds/$key.$ext"
            val afd = try { ctx.assets.openFd(path) } catch (e: FileNotFoundException) { null } ?: continue
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
                    fmtTag = bb.getShort(body).toInt() and 0xFFFF; ch = bb.getShort(body + 2).toInt()
                    rate = bb.getInt(body + 4); bits = bb.getShort(body + 14).toInt()
                } else if (id == "data") {
                    if (fmtTag != 1 || bits != 16 || ch < 1 || ch > 2) return null
                    if (sz < 0 || body + sz > b.size) sz = b.size - body
                    val frames = sz / (2 * ch)
                    val l = ShortArray(frames); val r = if (ch > 1) ShortArray(frames) else null
                    for (i in 0 until frames) {
                        l[i] = bb.getShort(body + i * 2 * ch)
                        if (r != null) r[i] = bb.getShort(body + i * 2 * ch + 2)
                    }
                    return Pcm(l, r, rate)
                }
                pos = body + sz + (sz and 1)
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
                codec.configure(fmt, null, null, 0); codec.start()
                val L = Grow(); val R = Grow()
                val info = MediaCodec.BufferInfo()
                var inDone = false; var outDone = false
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
                            val sb = ob.asShortBuffer(); val frames = sb.remaining() / ch
                            for (f in 0 until frames) { L.add(sb.get(f * ch)); if (ch > 1) R.add(sb.get(f * ch + 1)) }
                        }
                        codec.releaseOutputBuffer(oi, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outDone = true
                    }
                }
                codec.stop(); codec.release()
                return Pcm(L.get(), if (ch > 1) R.get() else null, rate)
            } finally { ex.release() }
        }
    }
}
