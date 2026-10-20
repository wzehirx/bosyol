package com.bosyol.app

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import com.bosyol.app.core.Bank
import com.bosyol.app.core.Pcm
import com.bosyol.app.core.Slots
import com.bosyol.app.core.seamless
import java.io.File
import java.nio.ByteOrder

/** Varsayılan sesler uygulama içinde, içe aktarılanlar uygulama klasöründe durur. Çözülen sesler bellekte tutulur. */
class SoundBank(private val ctx: Context) : Bank {
    private val cache = HashMap<String, Pcm>()
    private val dir = File(ctx.filesDir, "custom").also { it.mkdirs() }

    fun customFile(key: String) = File(dir, key)
    override fun isCustom(key: String) = customFile(key).exists()

    @Synchronized override fun get(key: String): Pcm? {
        cache[key]?.let { return it }
        val p = try { load(key) } catch (e: Throwable) { null } ?: return null
        cache[key] = p
        return p
    }

    private fun load(key: String): Pcm? {
        val f = customFile(key)
        if (f.exists()) {
            val ex = MediaExtractor(); ex.setDataSource(f.absolutePath)
            val p = decode(ex)
            return if (Slots.isEngineLoop(key)) seamless(p, 0.06) else p
        }
        val def = Slots.byKey(key)?.defaultFile ?: return null
        val afd = ctx.assets.openFd("sounds/$def")
        val ex = MediaExtractor(); ex.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        return decode(ex)
    }

    @Synchronized fun invalidate(key: String) { cache.remove(key) }
    @Synchronized fun keepOnly(keys: Collection<String>) { cache.keys.retainAll(keys.toSet()) }

    /** Seçilen dosyayı yuvaya kopyalar ve çözülebildiğini doğrular. */
    fun import(key: String, uri: Uri): Boolean {
        val tmp = File(dir, "$key.tmp")
        try {
            ctx.contentResolver.openInputStream(uri)?.use { inp -> tmp.outputStream().use { inp.copyTo(it, 1 shl 16) } } ?: return false
            val ex = MediaExtractor(); ex.setDataSource(tmp.absolutePath)
            val p = decode(ex)
            if (p.frames < 100) throw IllegalStateException("empty")
            val f = customFile(key); f.delete()
            if (!tmp.renameTo(f)) return false
            invalidate(key)
            return true
        } catch (e: Throwable) {
            tmp.delete(); return false
        }
    }

    fun reset(key: String) { customFile(key).delete(); invalidate(key) }

    private class Grow { var a = ShortArray(1 shl 16); var n = 0
        fun add(v: Short) { if (n == a.size) a = a.copyOf(a.size * 2); a[n++] = v }
        fun get() = a.copyOf(n) }

    companion object {
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
