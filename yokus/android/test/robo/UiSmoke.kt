import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.yokus.app.MainActivity
import com.yokus.app.Store
import com.yokus.app.core.Maps
import com.yokus.app.core.UpgradeKind
import com.yokus.app.core.Vehicles
import com.yokus.app.core.upgradeCost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun audit(screen: String) {
        val t = title()
        if (t == null) problems.add("$screen: başlık (tag=title) yok")
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
        a = Robolectric.buildActivity(MainActivity::class.java).setup().get(); idle()
        val st = Store(a)
        assertEquals("başlangıç altını", 0, st.gold)
        println("menü: ${texts()}")
        audit("Ana menü")

        for (s in listOf("Araçlar", "Haritalar", "Garaj", "Ayarlar", "Nasıl oynanır")) {
            click(s); assertEquals(s, title()?.text.toString()); audit(s); back()
            assertEquals("Yokuş Ustası", title()?.text.toString())
        }
        click("Çıkış"); assertEquals("Çıkmak istiyor musun?", title()?.text.toString()); audit("Çıkış onayı"); click("Vazgeç")
        click("Başla"); audit("Oyun"); back()

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

    private fun state(s: Store, text: String): Boolean = when (text) {
        "Eğim tonu" -> s.audio.tiltTone; "Yol radarı" -> s.audio.radar; "Mesafe duyurusu" -> s.audio.distanceCalls
        "Ses tuşları gaz/fren" -> s.volumeKeys; "Pedal kilidi" -> s.latch; else -> error("bilinmeyen anahtar $text")
    }
}
