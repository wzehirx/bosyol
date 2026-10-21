package com.yokus.app.core

/** Ses bankası: anahtar = uzantısız dosya adı; dosya yoksa null. */
interface Bank { fun get(key: String): Pcm? }

/** Ses ayarları (Ayarlar ekranından gelir, Sonify okur). */
class AudioSettings {
    var tiltTone = true
    var radar = true
    var distanceCalls = false
    var balanceAssist = true      // Denge yardımı: burun çok kalkınca gazı kısar, uyarır
    var volMaster = 80
    var volEngine = 80
    var volWorld = 70
    var volCues = 80
}
