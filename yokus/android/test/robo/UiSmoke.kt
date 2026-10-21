import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.yokus.app.GameView
import com.yokus.app.MainActivity
import com.yokus.app.PedalView
import com.yokus.app.Store
import com.yokus.app.core.EndReason
import com.yokus.app.core.Maps
import com.yokus.app.core.UpgradeKind
import com.yokus.app.core.Vehicles
import com.yokus.app.core.upgradeCost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.io.File

/** Yokuş Ustası menü ekranları için Robolectric duman testi: erişilebilirlik etiketleri, satın alma, kalıcılık. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE, qualifiers = "w800dp-h400dp-land")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UiSmoke {
    lateinit var a: MainActivity
    val dens get() = a.resources.displayMetrics.density
    val problems = ArrayList<String>()

    fun all(v: View, out: MutableList<View> = ArrayList()): MutableList<View> {
        out.add(v); if (v is ViewGroup) for (i in 0 until v.childCount) all(v.getChildAt(i), out); return out
    }
    fun views() = all(a.window.decorView).filter { it.isShown }
    fun idle() { ShadowLooper.idleMainLooper(); ShadowLooper.runUiThreadTasksIncludingDelayedTasks() }
    fun texts() = views().filterIsInstance<TextView>().map { it.text.toString() }
    fun title() = views().firstOrNull { it.tag == "title" } as? TextView
    fun click(prefix: String) {
        val b = views().filterIsInstance<TextView>().firstOrNull { it.text.toString().startsWith(prefix) }
            ?: error("ekranda yok: $prefix  ekran: ${texts()}")
        assertTrue("tıklanamaz: $prefix", b.isClickable)
        b.performClick(); idle()
    }
    fun back() { @Suppress("DEPRECATION") a.onBackPressed(); idle() }

    /** Her tıklanabilir ya da odaklanabilir görünümün sesli etiketi olmalı. */
    fun audit(screen: String, needTitle: Boolean = true) {
        val t = title()
        if (!needTitle) {}
        else if (t == null) problems.add("$screen: başlık (tag=title) yok")
        else {
            if (!t.isAccessibilityHeading) problems.add("$screen: başlık isAccessibilityHeading değil")
            if (t.text.isNullOrBlank()) problems.add("$screen: başlık boş")
        }
        for (v in views()) {
            val label = (v as? TextView)?.text?.toString()?.trim().orEmpty().ifEmpty { v.contentDescription?.toString()?.trim().orEmpty() }
            val interactive = v.isClickable || (v.isFocusable && v !is ViewGroup)
            val name = "$screen: ${v.javaClass.simpleName}"
            if (v is ImageView && v.importantForAccessibility != View.IMPORTANT_FOR_ACCESSIBILITY_NO && label.isEmpty()) problems.add("$name etiketsiz ImageView")
            if (!interactive) continue
            if (v.importantForAccessibility == View.IMPORTANT_FOR_ACCESSIBILITY_NO) continue
            if (label.isEmpty()) problems.add("$name tıklanabilir ama etiketi yok")
            if (v is Button || v is CompoundButton || v is SeekBar) {
                val minH = if (v is TextView) v.minHeight else v.minimumHeight  // TextView.setMinHeight ayrı bir alan
                if (minH < 48 * dens) problems.add("$name '$label' minimum yükseklik ${minH / dens}dp < 48dp")
            }
            if (v is SeekBar && v.stateDescription.isNullOrEmpty()) problems.add("$name '$label' değer duyurmuyor (stateDescription yok)")
            if (v is CompoundButton && v.text.isNullOrBlank()) problems.add("$name anahtar etiketsiz")
        }
        println("  [$screen] başlık='${t?.text}' tıklanabilir=${views().count { it.isClickable }}")
    }

    @Test fun flow() {
        a = Robolectric.buildActivity(MainActivity::class.java).setup().get(); a.autoLoop = false; idle()
        val st = Store(a)
        assertEquals("başlangıç altını", 0, st.gold)
        println("menü: ${texts()}")
        audit("Ana menü")

        for (s in listOf("Araçlar", "Haritalar", "Garaj", "Ayarlar", "Nasıl oynanır")) {
            click(s); assertEquals(s, title()?.text.toString()); audit(s); back()
            assertEquals("Yokuş Ustası", title()?.text.toString())
        }
        click("Çıkış"); assertEquals("Çıkmak istiyor musun?", title()?.text.toString()); audit("Çıkış onayı"); click("Vazgeç")
        click("Başla"); audit("Oyun", needTitle = false); back(); assertEquals("Duraklatıldı", title()?.text.toString()); click("Ana menü")

        // --- 0 altınla satın alma başarısız olmalı
        val veh = Vehicles.all.filter { !st.owns(it.id) && it.price in 1..10000 }.minByOrNull { it.price }!!
        click("Araçlar"); click("${veh.name}, ")
        assertFalse("0 altınla araç alındı", Store(a).owns(veh.id)); assertEquals(0, Store(a).gold)
        back()
        val map = Maps.all.filter { !st.ownsMap(it.id) && it.price in 1..10000 }.minByOrNull { it.price }!!
        click("Haritalar"); click("${map.name}, ")
        assertFalse("0 altınla harita alındı", Store(a).ownsMap(map.id)); assertEquals(0, Store(a).gold); back()
        click("Garaj"); click("Motor seviye 0")
        assertEquals("0 altınla geliştirme alındı", 0, Store(a).upgrades("jeep").engine); assertEquals(0, Store(a).gold); back()
        println("0 altın: satın alma reddedildi (araç ${veh.name}, harita ${map.name})")

        // --- 10000 altınla başarılı, ikinci tıklama tekrar ücret almaz
        Store(a).gold = 10000
        click("Araçlar"); click("${veh.name}, ")
        assertTrue("araç alınamadı", Store(a).owns(veh.id)); assertEquals(10000 - veh.price, Store(a).gold); assertEquals(veh.id, Store(a).vehicleId)
        click("${veh.name}, sahipsin")
        assertEquals("çift ücret", 10000 - veh.price, Store(a).gold); back()
        val g1 = Store(a).gold
        click("Haritalar")
        if (g1 >= map.price) { click("${map.name}, "); assertTrue(Store(a).ownsMap(map.id)); assertEquals(g1 - map.price, Store(a).gold) }
        back()
        println("10000 altın: araç ${veh.name} (${veh.price}) alındı, kalan ${Store(a).gold}")

        // --- Garaj: bedel upgradeCost, en yüksek seviye 10, altın asla negatif
        val cur = Vehicles.byId(Store(a).vehicleId)
        Store(a).gold = 50_000_000
        click("Garaj")
        var spent = 0L
        for (lvl in 0 until 12) {
            val label = "Motor seviye "
            click(label)
            val lv = Store(a).upgrades(cur.id).engine
            if (lvl < Store.MAX_LEVEL) spent += upgradeCost(cur, UpgradeKind.ENGINE, lvl)
            assertEquals("seviye ${lvl + 1}", minOf(lvl + 1, Store.MAX_LEVEL), lv)
        }
        assertEquals("harcanan = upgradeCost toplamı", 50_000_000L - spent, Store(a).gold.toLong())
        assertTrue(texts().any { it.startsWith("Motor seviye 10 / 10, en yüksek") })
        audit("Garaj (en yüksek)")
        Store(a).gold = 5
        click("Lastik seviye 0"); assertEquals(0, Store(a).upgrades(cur.id).tire); assertEquals(5, Store(a).gold)
        back()

        // --- Ayarlar: anahtarlar, çubuklar, kalıcılık
        click("Ayarlar")
        val sw = views().filterIsInstance<CompoundButton>(); assertTrue("anahtar yok", sw.size >= 5)
        sw.forEach { val o = it.isChecked; it.performClick(); assertEquals(!o, Store(a).let { s -> state(s, it.text.toString()) }) }
        val sbs = views().filterIsInstance<SeekBar>(); assertEquals(4, sbs.size)
        sbs[0].progress = 6; idle()
        assertEquals(30, Store(a).audio.volMaster); assertEquals("yüzde 30", sbs[0].stateDescription.toString())
        audit("Ayarlar (değişmiş)"); back()

        // manifest: yatay/yeniden oluşturma yok
        val mf = File(System.getProperty("yokus.manifest") ?: "../../AndroidManifest.xml").readText()
        assertTrue("configChanges orientation yok", Regex("configChanges=\"[^\"]*orientation[^\"]*screenSize").containsMatchIn(mf))

        if (problems.isNotEmpty()) { problems.forEach { println("SORUN: $it") }; error("${problems.size} erişilebilirlik sorunu") }
        println("OK")
    }

    fun touch(v: View, action: Int) {
        val t = SystemClock.uptimeMillis()
        val e = MotionEvent.obtain(t, t, action, 5f, 5f, 0); v.dispatchTouchEvent(e); e.recycle()
    }
    fun pedal(label: String) = views().filterIsInstance<PedalView>().first { it.contentDescription == label }
    fun run(sec: Double) { var t = 0.0; while (t < sec) { a.tick(1.0 / 60); t += 1.0 / 60 } }

    @Test fun game() {
        val ctl = Robolectric.buildActivity(MainActivity::class.java).setup()
        a = ctl.get(); a.autoLoop = false; idle()
        val st = Store(a)
        val gold0 = st.gold
        click("Başla")
        val s = a.gameSession!!
        assertTrue("GameView yok", views().any { it is GameView })
        assertEquals("GameView erişilebilirlik dışı olmalı", View.IMPORTANT_FOR_ACCESSIBILITY_NO,
            views().first { it is GameView }.importantForAccessibility)
        val gas = pedal("Gaz"); val brake = pedal("Fren")
        assertTrue(gas.isFocusable && brake.isFocusable && gas.isClickable)
        audit("Oyun", needTitle = false)

        // gaz: basılı tut ~3 sn
        touch(gas, MotionEvent.ACTION_DOWN); assertTrue(gas.active)
        run(3.0)
        touch(gas, MotionEvent.ACTION_UP); assertFalse("bırakınca pedal basılı kalmamalı", gas.active)
        println("3 sn gaz sonrası mesafe=${s.distance} m, yakıt=%${s.fuelPct()}")
        assertTrue("mesafe artmadı: ${s.distance}", s.distance > 0.0)

        // pedal kilidi + erişilebilirlik tıklaması
        gas.performAccessibilityAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK, null); assertTrue("ACTION_CLICK gazı sabitlemedi", gas.active)
        brake.performAccessibilityAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK, null)
        assertTrue(brake.active); assertFalse("fren gazı bırakmalı", gas.active)
        brake.performAccessibilityAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK, null); assertFalse(brake.active)

        // ses tuşları kapalıyken sistem tuşu tüketilmez, açıkken tüketilir
        val up = android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_VOLUME_UP)
        Store(a).volumeKeys = true
        assertTrue("ses tuşu tüketilmedi", a.dispatchKeyEvent(up))
        a.dispatchKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_VOLUME_UP))
        Store(a).volumeKeys = false

        // Durum
        click("Durum")
        assertTrue(s.statusText().startsWith("Mesafe"))

        // geri -> Duraklat -> Devam -> onPause -> Duraklat -> onResume sonrası Duraklat'ta kal
        val d1 = s.distance
        back(); assertEquals("Duraklatıldı", title()?.text.toString()); audit("Duraklat")
        a.tick(0.05); assertEquals("duraklatılmışken fizik ilerledi", d1, s.distance, 0.0)
        click("Devam"); assertTrue(views().any { it is GameView })
        ctl.pause(); idle(); assertEquals("onPause Duraklat'a gitmeli", "Duraklatıldı", title()?.text.toString())
        ctl.resume(); idle(); assertEquals("onResume Duraklat'ta kalmalı", "Duraklatıldı", title()?.text.toString())
        assertSame("oturum korunmalı", s, a.gameSession)

        // Yeniden başla, sonra Ana menü: altın eklenir
        click("Yeniden başla"); val s2 = a.gameSession!!
        assertNotSame(s, s2)
        val g2 = pedal("Gaz"); touch(g2, MotionEvent.ACTION_DOWN); run(3.0); touch(g2, MotionEvent.ACTION_UP)
        s2.coins += 7
        val before = Store(a).gold
        back(); click("Ana menü")
        assertEquals("Yokuş Ustası", title()?.text.toString())
        val after = Store(a).gold
        println("Ana menü: altın $before -> $after")
        assertTrue("altın azaldı", after >= before && after >= gold0)
        assertTrue("kazanılan altın eklenmedi", after >= before + 7)

        // oyun sonu: kaza -> 2.5 sn sonra Oyun sonu ekranı
        click("Başla"); val s3 = a.gameSession!!
        run(1.0); s3.end(EndReason.CRASH); s3.coins += 5
        run(2.0); assertTrue("2.5 sn dolmadan bitiş ekranı", views().any { it is GameView })
        run(1.0); idle()
        assertEquals("Oyun bitti", title()?.text.toString()); audit("Oyun sonu")
        println("oyun sonu: ${texts()}")
        assertTrue(texts().any { it.contains("metre") && it.contains("altın kazandın. En iyi:") })
        assertTrue(Store(a).gold >= after + 5)
        click("Tekrar"); assertTrue(views().any { it is GameView }); back(); click("Ana menü")
        println("OK")
    }

    private fun state(s: Store, text: String): Boolean = when (text) {
        "Eğim tonu" -> s.audio.tiltTone; "Yol radarı" -> s.audio.radar; "Mesafe duyurusu" -> s.audio.distanceCalls; "Denge yardımı" -> s.audio.balanceAssist
        "Ses tuşları gaz/fren" -> s.volumeKeys; "Pedal kilidi" -> s.latch; else -> error("bilinmeyen anahtar $text")
    }
}
