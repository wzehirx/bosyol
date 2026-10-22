# Yokuş Ustası 2.0: oyun hissi, daha çok içerik, yeni sesler

Kullanıcı geri bildirimi (22 Ekim 2026):
- Oyun, oyundan çok uygulama gibi. Menü tıklamaları bile sistem tıklaması gibi.
- Hill Climb Racing mantığından biraz uzak.
- Ekteki ses paketi kullanılacak (`kaynak/hcr/`): click, coin, purchase, refuel, beep, crack, splash, splash-add, slime, camera-shutter, turbo, turbo-flutter, supercharger, engine, engine-monster, engine-rallycar.
- Araç ve harita sayısı artırılacak (şimdi 12 araç, 10 harita).
- Altın sistemi: her altın rastgele, her alınan altında ses çalar, art arda alınınca her altında ses çalar, hızlı alımda ses hızlı olabilir.
- Mevcut araç sesleri kalabilir.
- GitHub düzeni güzelleşecek, Boş Yol kaldırılacak (yapıldı), son sürüm release anahtarıyla imzalanıp `indir/` altına doğrudan indirme olarak konacak, README'de ayrı bağlantı olacak.
- Her değişiklik commit edilecek.

Ajan dağılımı: planlama ve araştırma Haiku, kodlama Sonnet, test ve denetim Sonnet, çözülemeyen hata Opus.

Commit mesajı sonu:
`Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`
`Claude-Session: https://claude.ai/code/session_01D7Q1ZrADbH6m5pGvh6c5TH`

Depo düzeni (kök klasör): `android/` (kaynak, build.py, testler), `sounds/` (oyuna giren sesler), `kaynak/` (ham sesler), `tools/` (ses hazırlama betikleri), `docs/`, `indir/`.

## Görevler

- [x] 0. Depo temizliği: Boş Yol silindi, proje kök klasöre taşındı.
- [~] 1. Araştırma (Haiku): `docs/ARASTIRMA_ICERIK.md` (araç/harita önerileri), `docs/ARASTIRMA_SES.md` (yeni sesler, YouTube kanalı lisansı).
- [ ] 2. S-A Ses paketi ve altın sistemi (Sonnet): `tools/prep_sounds.py`, `core/Sonify.kt`.
  - Paket sesleri `sounds/` altına hazırlanır (hcr_ öneki yok, anlamlı adlar: ui_click, coin, buy, fuel_pick, fuel_low, crash_crack, splash_in, splash_out, mud_slime, shutter, turbo_spool, turbo_blow, supercharger, eng_hill, eng_monster2, eng_rally2).
  - Altın: her altın rastgele değerde, her alımda ses; art arda alımda her biri ayrı çalar ve üst üste binebilir; ses perdesi değişmez, hızlı zincirde örnek kısalır ve tempo hızlanır; 25/100/500 değerleri aynı örneğin çoklu vuruşları ile ayırt edilir.
  - Turbo: turbo_blow gaz bırakınca yüksek devirde, turbo_spool gaza basınca düşük devirde (turbo bayraklı araçlar), supercharger süper araçta devire bağlı uğultu.
  - Su ve çamur: splash/slime yüzey ve iniş sesleri.
- [ ] 3. S-B Oyun hissi / arayüz (Sonnet): `MainActivity.kt`, yeni `UiSfx.kt`.
  - Düğme sesi ui_click, odak tıkı, titreşim, satın alma sesi, ekran geçişleri.
  - Araç listesinde odaklanınca motor sesi önizlemesi, haritada ortam sesi önizlemesi.
  - Ana menüde arka plan motor/ortam sesi (açılıp kapanabilir).
  - Başlarken geri sayım ve motor çalıştırma, oyun sonu özeti.
- [ ] 4. S-C İçerik (Sonnet): araç sayısı 12 → 20, harita sayısı 10 → 16 (`Defs.kt`, `Terrain.kt`, sesler, testler, PlayTest kapıları).
- [ ] 5. Denetim (Sonnet), çözülemeyenler Opus.
- [ ] 6. Sürüm: release imzalı APK `indir/YokusUstasi.apk`, README, commit ve push.
