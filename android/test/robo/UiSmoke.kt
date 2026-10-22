import com.yokus.app.core.Bank
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
import org.junit.Assert.assertNotNull
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
    /** Geri sayımı tick ile bitirir. */
    fun skip() { var n = 0; while (a.countingDown && n++ < 600) a.tick(1.0 / 60) }
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
        // S-C: araç ve harita listeleri 20 ve 16 satırdır, her satırda ad ve fiyat ya da "sahipsin" okunur
        click("Araçlar")
        val vb = views().filter { it.tag?.toString()?.startsWith("v_") == true }.filterIsInstance<TextView>()
        assertEquals("araç listesi", 20, vb.size); assertEquals("araç listesi sırası", Vehicles.all.map { "v_${it.id}" }, vb.map { it.tag.toString() })
        assertTrue("araç satırı fiyatsız", vb.drop(1).all { it.text.contains("altın") })
        back()
        click("Haritalar")
        val mb = views().filter { it.tag?.toString()?.startsWith("m_") == true }.filterIsInstance<TextView>()
        assertEquals("harita listesi", 16, mb.size); assertEquals("harita listesi sırası", Maps.all.map { "m_${it.id}" }, mb.map { it.tag.toString() })
        back()
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
        val sw = views().filterIsInstance<CompoundButton>(); assertTrue("anahtar yok", sw.size >= 7)
        assertTrue("Menü sesleri anahtarı yok", sw.any { it.text.toString() == "Menü sesleri" })
        assertTrue("Titreşim anahtarı yok", sw.any { it.text.toString() == "Titreşim" })
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
        click("Başla"); skip()
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
        click("Yeniden başla"); skip(); val s2 = a.gameSession!!
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
        click("Başla"); skip(); val s3 = a.gameSession!!
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


    /** BlindDriver kararlarını gerçek pedallara (dokunma olayı) aktarır: gaz/fren 0..1 değerini 60 Hz'de titreşimle ikili basışa çevirir. */
    private var accG = 0.0; private var accB = 0.0
    private fun drive(d: BlindDriver, gas: PedalView, brake: PedalView) {
        accG += d.gas; accB += d.brake
        val wantG = accG >= 1.0; val wantB = accB >= 1.0 && !wantG
        if (wantG) accG -= 1.0 else accG = minOf(accG, 0.999)
        if (wantB) accB -= 1.0 else accB = minOf(accB, 0.999)
        if (wantG != gas.held) touch(gas, if (wantG) MotionEvent.ACTION_DOWN else MotionEvent.ACTION_UP)
        if (wantB != brake.held) touch(brake, if (wantB) MotionEvent.ACTION_DOWN else MotionEvent.ACTION_UP)
    }

    /** Oyunu BlindDriver ile gerçek UI üzerinden oynar; bitiş ekranı ya da [maxSec] simüle saniyeye dek. */
    private fun playUi(seed: Long, maxSec: Double, stopOnEnd: Boolean): Pair<Double, Boolean> {
        val s = a.gameSession!!
        val d = BlindDriver(s, seed = seed)
        val gas = pedal("Gaz"); val brake = pedal("Fren")
        val dt = 1.0 / 60; var t = 0.0; var n = 0
        while (t < maxSec) {
            if (!s.over) { d.tick(dt); drive(d, gas, brake) } else { if (gas.held) touch(gas, MotionEvent.ACTION_UP); if (brake.held) touch(brake, MotionEvent.ACTION_UP) }
            a.tick(dt); t += dt
            if (++n % 30 == 0 && stopOnEnd) { idle(); if (title()?.text.toString() == "Oyun bitti") break }
        }
        if (gas.held) touch(gas, MotionEvent.ACTION_UP); if (brake.held) touch(brake, MotionEvent.ACTION_UP)
        idle()
        return Pair(t, title()?.text.toString() == "Oyun bitti")
    }

    /** Seviye hedefleri: ışınlanarak hedefe ulaş, bonus bir kez ödenir, kalıcıdır, oyun sonu özetinde görünür. */
    @Test fun goalsThroughUi() {
        a = Robolectric.buildActivity(MainActivity::class.java).setup().get(); a.autoLoop = false; idle()
        assertTrue("menüde hedef yok: ${texts()}", texts().any { it.contains("Sıradaki hedef: 200 metre") })
        click("Nasıl oynanır"); assertTrue("yardımda hedef yok", texts().any { it.contains("Hedefler:") }); back()
        assertEquals(0, Store(a).goalLevel("country"))
        fun jump(s: com.yokus.app.core.GameSession, m: Double) { s.car.x = s.car.x + m; s.car.rear.x += m; s.car.front.x += m }
        click("Başla"); skip(); val s = a.gameSession!!
        assertEquals(0, s.goalLevel)
        jump(s, 210.0); a.tick(1.0 / 60)
        assertEquals("ilk hedef", 1, s.goalsDone); assertEquals(25, s.goalBonus)
        jump(s, 10.0); a.tick(1.0 / 60); a.tick(1.0 / 60)
        assertEquals("bonus yinelenmedi", 25, s.goalBonus)
        val g0 = Store(a).gold
        s.end(EndReason.CRASH); run(3.0); idle()
        assertEquals("Oyun bitti", title()?.text.toString())
        println("hedefli oyun sonu: ${texts()}")
        assertTrue("özette hedef/bonus yok: ${texts()}", texts().any { it.contains("1 hedef tamamlandı, bonus 25 altın") && it.contains("Sıradaki hedef: 450 metre") })
        assertEquals("bonus altına eklendi", g0 + 25 + s.coins, Store(a).gold)
        assertEquals(1, Store(a).goalLevel("country"))
        // yeni koşu: hedef 1 tamamlanmış, tekrar ödenmez; rekor 210 m aşılınca koşu içi rekor bir kez
        click("Tekrar"); skip(); val s2 = a.gameSession!!
        assertEquals(1, s2.goalLevel); assertEquals(450, s2.nextGoal)
        val g1 = Store(a).gold
        jump(s2, 235.0); a.tick(1.0 / 60); a.tick(1.0 / 60)
        assertEquals("tekrar ödenmedi", 0, s2.goalBonus); assertTrue("rekor", s2.recordHit)
        jump(s2, 230.0); a.tick(1.0 / 60)
        assertEquals(30, s2.goalBonus); assertEquals(2, s2.goalLevel)
        back(); click("Ana menü")
        assertEquals(2, Store(a).goalLevel("country"))
        assertTrue("2. hedef bonusu altına eklenmedi", Store(a).gold >= g1 + 30)
        println("OK")
    }

    /** P3: tam oyun UI üzerinden: BlindDriver ile bitişe dek oyna, Garaj'da geliştirme al, yeniden oyna, duraklat, Ana menü. */
    @Test fun fullGameThroughUi() {
        a = Robolectric.buildActivity(MainActivity::class.java).setup().get(); a.autoLoop = false; idle()
        Store(a).latch = false
        val gold0 = Store(a).gold
        click("Başla"); skip()
        val s = a.gameSession!!
        val (t, ended) = playUi(seed = 3, maxSec = 360.0, stopOnEnd = true)
        println("UI oyunu: ${"%.0f".format(t)} sn, mesafe=${s.distance.toInt()} m, bitti=$ended, neden=${s.endReason} '${s.endMessage}', oturum altını=${s.coins}, uçuş=${"%.1f".format(s.airTime)} sn, takla=${s.flips}")
        assertTrue("6 simüle dakikada bitiş ekranı gelmedi (mesafe ${s.distance.toInt()} m, yakıt %${s.fuelPct()})", ended)
        println("bitiş ekranı: ${texts()}")
        audit("Oyun sonu (UI oyunu)")
        val reasons = listOf("Kaza yaptın", "Ters döndün", "Kafa üstü düştün", "Yakıt bitti")
        assertTrue("bitiş ekranında neden yok: ${texts()}", texts().any { x -> reasons.any { x.startsWith(it) } })
        assertNotNull(s.endReason)
        val gold1 = Store(a).gold
        println("altın: $gold0 -> $gold1")
        assertTrue("altın artmadı ($gold0 -> $gold1)", gold1 > gold0)
        assertEquals("altın = oturum altını + hedef bonusu (çift sayma yok)", gold0 + s.coins + s.goalBonus, gold1)

        // Garaj: geliştirme al (gerekirse altın ver)
        click("Garaj"); assertEquals("Garaj", title()?.text.toString())
        val v = Vehicles.byId(Store(a).vehicleId)
        val cost = upgradeCost(v, UpgradeKind.ENGINE, Store(a).upgrades(v.id).engine)
        if (Store(a).gold < cost) Store(a).gold = cost + 10
        val gb = Store(a).gold; val lv0 = Store(a).upgrades(v.id).engine
        click("Motor seviye $lv0")
        assertEquals("motor seviyesi artmadı", lv0 + 1, Store(a).upgrades(v.id).engine)
        assertEquals("geliştirme bedeli", gb - cost, Store(a).gold)
        println("Garaj: motor $lv0 -> ${lv0 + 1}, bedel $cost")
        back(); assertEquals("Yokuş Ustası", title()?.text.toString())

        // yeniden başla, 30 sn oyna, duraklat, Ana menü
        click("Başla"); skip()
        val s2 = a.gameSession!!
        assertNotSame(s, s2)
        playUi(seed = 4, maxSec = 30.0, stopOnEnd = false)
        println("2. koşu 30 sn: mesafe=${s2.distance.toInt()} m, bitti=${s2.over}, altın=${s2.coins}")
        assertTrue("2. koşuda ilerleme yok", s2.distance > 20.0)
        if (!s2.over) { back(); assertEquals("Duraklatıldı", title()?.text.toString()); click("Ana menü") }
        else { click("Ana menü") }
        assertEquals("Yokuş Ustası", title()?.text.toString())
        assertTrue("altın azaldı", Store(a).gold >= gb - cost)
        println("OK")
    }


    /** Ana menü sırası, ipucu satırı, yeni düğmeler ve odak/önizleme/ses çağrıları (boş banka: sessiz, çökmez). */
    private fun menuFeelBody() {
        a = Robolectric.buildActivity(MainActivity::class.java).setup().get(); a.autoLoop = false; idle()
        val btns = views().filterIsInstance<Button>().map { it.text.toString() }
        assertEquals(listOf("Başla", "Garaj", "Araçlar", "Haritalar", "Ayarlar", "Nasıl oynanır", "Çıkış"), btns)
        val tip = texts().first { it.startsWith("İpucu: ") }
        assertTrue("ipucu boş", tip.length > 12)
        assertFalse("ipucunda eski değerler: $tip", Regex("\\b(25|100|500)\\b").containsMatchIn(tip))
        audit("Ana menü (yeni)")
        // sistem sesleri kapalı
        for (b in views().filterIsInstance<Button>()) {
            assertFalse("${b.text} sistem tıklaması açık", b.isSoundEffectsEnabled); assertFalse(b.isHapticFeedbackEnabled)
        }
        val sf = a.sounds
        val c0 = sf.clicks + sf.buzzes
        click("Garaj"); assertTrue("tıklama titreşimi/sesi yok", sf.buzzes > 0)
        // odak tıkı: yeni düğmeye erişilebilirlik odağı
        val t0 = sf.buzzes
        back(); click("Araçlar")
        val vb = views().filterIsInstance<Button>().first { (it.tag as? String)?.startsWith("v_") == true }
        vb.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
        Thread.sleep(300); idle()
        val vbs = views().filterIsInstance<Button>().filter { (it.tag as? String)?.startsWith("v_") == true }
        for (b in vbs) { b.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) }  // hızlı kaydırma
        Thread.sleep(300); idle()
        assertTrue("araç önizlemesi başlamadı", sf.previews >= 1)
        back(); click("Haritalar")
        for (b in views().filterIsInstance<Button>().filter { (it.tag as? String)?.startsWith("m_") == true }.take(3)) b.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
        Thread.sleep(300); idle()
        back()
        // garaj geliştirmesi (motor gazı önizlemesi) ve satın alma sesleri çökmemeli
        Store(a).gold = 100000
        click("Garaj"); click("Motor seviye 0"); Thread.sleep(200); idle(); back()
        // Menü sesleri ve Titreşim kapatılınca ses/titreşim sayaçları artmaz
        click("Ayarlar")
        for (n in listOf("Menü sesleri", "Titreşim")) views().filterIsInstance<CompoundButton>().first { it.text.toString() == n }.performClick()
        assertFalse(Store(a).menuSounds); assertFalse(Store(a).haptics)
        val b0 = sf.buzzes; val k0 = sf.clicks
        back(); click("Garaj"); back()
        assertEquals("titreşim kapalıyken sayaç arttı", b0, sf.buzzes); assertEquals("menü sesleri kapalıyken tık çaldı", k0, sf.clicks)
        assertFalse("menü sesleri kapalıyken zemin açık", sf.bedActive)
        // kapalıyken geri sayım yok
        click("Başla"); assertFalse("menü sesleri kapalıyken geri sayım", a.countingDown); back(); click("Ana menü")
        for (n in listOf("Menü sesleri", "Titreşim")) { /* tekrar aç */ }
        Store(a).menuSounds = true; Store(a).haptics = true
        println("OK menuFeel bed=${sf.bedActive} clicks=${sf.clicks} ticks=${sf.ticks} previews=${sf.previews}")
    }

    @Test fun menuFeelSilent() { MainActivity.bankFactory = { NullBank() }; try { menuFeelBody() } finally { MainActivity.bankFactory = null } }
    @Test fun menuFeelRealBank() { MainActivity.bankFactory = { SyncBank(FBank()) }; try { menuFeelBody() } finally { MainActivity.bankFactory = null } }

    class SyncBank(val b: com.yokus.app.core.Bank) : com.yokus.app.core.Bank { override fun get(key: String) = synchronized(this) { b.get(key) } }

    /** Başla: geri sayım. Gaz yok sayılır, oyun saati sayımdan sonra başlar, toplam 1.9 sn altında. */
    @Test fun countdown() {
        MainActivity.bankFactory = { SyncBank(FBank()) }
        try {
            a = Robolectric.buildActivity(MainActivity::class.java).setup().get(); a.autoLoop = false; idle()
            click("Başla")
            val s = a.gameSession!!
            assertTrue("geri sayım başlamadı", a.countingDown)
            val gas = pedal("Gaz"); touch(gas, MotionEvent.ACTION_DOWN)
            var t = 0.0
            while (a.countingDown && t < 3.0) { a.tick(1.0 / 60); t += 1.0 / 60; if (a.countingDown) assertEquals("sayım sırasında saat ilerledi", 0.0, s.time, 0.0) }
            println("geri sayım ${"%.2f".format(t)} sn, bip=${a.sounds.beeps}")
            assertTrue("geri sayım 1.9 sn'den uzun: $t", t < 1.9)
            assertTrue("geri sayım çok kısa: $t", t > 1.4)
            assertEquals("bip sayısı", 4, a.sounds.beeps)
            assertEquals(0.0, s.distance, 0.0)
            run(1.0)
            assertTrue("sayımdan sonra oyun ilerlemedi", s.time > 0.9 && s.distance > 0.0)
            touch(gas, MotionEvent.ACTION_UP)
            // duraklat/devam: sayım sırasında duraklatılırsa devamda yeniden başlar
            back(); click("Ana menü"); click("Başla"); assertTrue(a.countingDown); back(); assertEquals("Duraklatıldı", title()?.text.toString())
            assertFalse(a.countingDown); click("Devam"); assertTrue("devamda geri sayım yeniden", a.countingDown)
            back(); click("Ana menü")
        } finally { MainActivity.bankFactory = null }
    }

    private fun state(s: Store, text: String): Boolean = when (text) {
        "Eğim tonu" -> s.audio.tiltTone; "Yol radarı" -> s.audio.radar; "Mesafe duyurusu" -> s.audio.distanceCalls; "Denge yardımı" -> s.audio.balanceAssist
        "Ses tuşları gaz/fren" -> s.volumeKeys; "Pedal kilidi" -> s.latch; "Menü sesleri" -> s.menuSounds; "Titreşim" -> s.haptics; else -> error("bilinmeyen anahtar $text")
    }
}
