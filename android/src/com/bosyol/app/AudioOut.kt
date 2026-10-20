package com.bosyol.app

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.bosyol.app.core.Mixer
import com.bosyol.app.core.OUT_RATE

/** Karıştırıcıyı AudioTrack'e sürekli yazan iş parçacığı. */
class AudioOut(private val mix: Mixer) {
    @Volatile private var running = false
    private var thread: Thread? = null

    fun start() {
        if (running) return
        running = true
        thread = Thread({
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
            val min = AudioTrack.getMinBufferSize(OUT_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_FLOAT)
            val track = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_FLOAT).setSampleRate(OUT_RATE).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build())
                .setBufferSizeInBytes(maxOf(min * 2, 256 * 8 * 6))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .build()
            val n = 256
            val buf = FloatArray(n * 2)
            track.play()
            try {
                while (running) {
                    mix.render(buf, n)
                    track.write(buf, 0, n * 2, AudioTrack.WRITE_BLOCKING)
                }
            } catch (e: Throwable) {
            } finally { try { track.stop() } catch (e: Throwable) {}; track.release() }
        }, "bosyol-audio")
        thread!!.start()
    }

    fun stop() { running = false; thread?.join(500); thread = null }
}
