package com.yokus.app.core

/**
 * Seviye hedefleri (gerçek Hill Climb Racing'deki "Level N: Reach X m"). Her harita için ayrı ilerler:
 * tamamlanan hedef sayısı Store'da kalıcıdır, her hedefin bonusu yalnızca bir kez ödenir.
 * Bonus küçük tutulur (her altın 1 değerinde) ve bonus/metre oranı azalır (videoda 30, 24, 17,5 altın/m).
 */
object Goals {
    private val TARGETS = intArrayOf(200, 450, 700, 1000, 1400, 1800, 2400)
    private val BONUS = intArrayOf(25, 30, 35, 40, 45, 50, 55)
    /** Tek bir hedef bonusunun üst sınırı: 432 altınlık tipik koşunun %30'u. */
    const val BONUS_CAP = 129

    /** [level] tamamlanmış hedef sayısıyken sıradaki hedefin mesafesi (m). Tablodan sonra her hedef 800 m daha uzak. */
    fun target(level: Int): Int {
        val l = level.coerceAtLeast(0)
        return if (l < TARGETS.size) TARGETS[l] else TARGETS.last() + 800 * (l - TARGETS.size + 1)
    }

    /** [level]. hedefi (0 tabanlı) tamamlamanın bonusu: 25 ile başlar, yavaş artar, 100'de durur. */
    fun bonus(level: Int): Int {
        val l = level.coerceAtLeast(0)
        val b = if (l < BONUS.size) BONUS[l] else BONUS.last() + 5 * (l - BONUS.size + 1)
        return minOf(b, 100, BONUS_CAP)
    }
}
