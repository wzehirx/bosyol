# Yokuş Ustası: gece çalışma günlüğü

Kullanıcı isteği (20 Ekim 2026 gecesi): Görme engelliler için Hill Climb Racing'e çok yakın, sesli bir Android oyunu.
Bol araç, bol harita. bosyol deposunda `yokus/` klasörü. Sabah teslim. Kullanıcı onay beklemeden ilerlememe izin verdi.
Tasarım: `docs/TASARIM.md` (kararlar orada; buna uy).

## Ajan dağılımı (kullanıcının isteği)
- Araştırma ve dosya okuma: Haiku (Agent, model "haiku")
- Kodlama: Sonnet (Agent, model "sonnet")
- Her görevden sonra denetim: ayrı bir Sonnet ajanı; çözemediği hatayı Opus'a ver (model "opus")
- Ana oturum (Opus) planlar, yönetir, birleştirir, commit + push yapar.
- Limite takılınca yavaşla; send_later ile ~75 dk'da bir kendine kontrol mesajı kur.

## Ortam bilgisi (yeni oturum için)
- Depo: /home/user/bosyol (GitHub wzehirx/bosyol, dal main). Commit kimliği: wzehirx <wzehirx@users.noreply.github.com>; commit mesajı sonunda Co-Authored-By ve Claude-Session satırları.
- Android SDK: /opt/android-sdk (build-tools 34.0.0, platform 34). Kotlin: /home/user/surus/android/tools/kotlinc (bosyol/android/tools bunun bağlantısı). Kotlin derlemesinde `-Xlambdas=class -Xsam-conversions=class` ve d8 şart (aksi halde açılışta çöker).
- Örnek derleyici: /home/user/bosyol/android/build.py. Örnek ses motoru/karıştırıcı: /home/user/bosyol/android/src/com/bosyol/app/core/Dsp.kt. Örnek arayüz: bosyol MainActivity.kt.
- Robolectric test düzeni: /tmp/claude-0/-home-user/afa3c9ce-e696-5524-8a18-2a59d29f0c0e/scratchpad/robo (lib/, atc/ androidx.test sınıfları). Oturum değişirse bu klasör kaybolabilir; o zaman yeniden kurulur (Maven Central + github.com/android/android-test kaynakları).
- Freesound arama: curl 'https://freesound.org/search/?q=...&f=license:%22Creative+Commons+0%22' → HTML'de data-mp3 önizleme bağlantıları (-hq.mp3).
- Herkese açık dosya sitesine yükleme ve yeni depo/release oluşturma bu oturumda engelli; APK depoya dosya olarak konur: indir/YokusUstasi.apk (bağlantı: https://github.com/wzehirx/bosyol/raw/main/yokus/indir/YokusUstasi.apk).

## Adımlar
- [x] 1. Ses araştırması (Haiku, arka planda başlatıldı; çıktı: sounds-raw/, CREDITS.csv, RAPOR.md)
- [x] 2. Tasarım belgesi (docs/TASARIM.md)
- [x] 3. Uygulama planı (docs/PLAN.md, superpowers:writing-plans)
- [ ] 4. Uygulama görevleri (Sonnet) + her görev sonrası denetim (Sonnet):
  - [x] G1 İskelet: bosyol'dan Dsp/SoundBank/AudioOut/CrashLog/build.py/ikon kopyala, paket com.yokus.app, boş MainActivity derlensin
  - [x] G2 Defs + Terrain
  - [x] G3 Physics + Opus ayarı (HCR hızları, yumuşak çukurlar, zorluk eğrisi, test/lib/Driver.kt) + JVM testleri
  - [x] G4 Game oturumu (yakıt, altın, kaza, takla, puan) + testler
  - [x] G5 Ses tasarımı (motor, yüzey, eğim tonu, yol radarı, işaret sesleri, iniş) + WAV ölçüm testi
  - [x] G6 Ses hazırlama (traktör ve kar motosikleti aynı yedek motor sesini aldı; G5'te perdeyle ayır) betiği (sounds-raw → sounds; döngüler dikişsiz, yükseklik eşitleme, eksikler için yedek)
  - [x] G7 Android arayüz (G7a bitti+denetlendi:  menüler/Store/SoundBank/Speaker/core/Audio.kt Sonnet'te; oyun ekranı G7b Sonnet'te çalışıyor) + GameView + Store
  - [x] G8 Robolectric testi, APK, lint, dexdump
  - [x] G9 README, indir/YokusUstasi.apk, push, kullanıcıya özet
- [x] 5. Sabah teslim: indirme bağlantısı + anahtar dosyasını (yokus.jks) kullanıcıya dosya olarak gönder

## Açık küçük konular (engelleyici değil)
- Oyun sonu sebebi iki kez okunuyor (duyuru + başlık odak).
- AudioOut ses odağı (audio focus) istemiyor; telefon çağrısında ses karışabilir.
- Gerçek cihazda TalkBack/Jieshuo hareketleri ve AudioTrack elle denenmeli.
- Uzak depoda eski wip-fizik dalı silinemedi (izin yok), zararsız.

## S-C içerik genişletme (22 Ekim 2026)
- 20 araç (yeni: Scooter, Go-kart, Dörtteker, Chopper, Hot rod, Çöp kamyonu, İtfaiye aracı, Formül araba) ve 16 harita (yeni: Tuz gölü, Donmuş göl, Gece çatıları, Mars, Volkan, Deniz tabanı). Yüzeyler SALT (0,35) ve MARS (0,65); `MapDef.drag` (deniz tabanı su direnci) ve `MapDef.pits` (çatılarda çukur sıklığı) varsayılanlı, eski haritalar aynı.
- Altı yeni ortam döngüsü `tools/prep_sounds.py` ile üretilir (`sounds-raw/yeni` varsa kullanır, yoksa sentez/miks). Donmuş gölde buz çatırtısı oyunda `crash_crack` ile rastgele çalar. Eksik ses dosyası oyunu çökertmez.
- Ekonomi: her altın 1; fiyatlar Kır yolu + jeep gelirine (432 altın / 3,6 dk) göre ~1/7 ölçeklendi, sıra aynı. Geliştirme tabanı max(60, fiyat/25). PlayTest ekonomi kapıları gerçek: Scooter 1-2 koşu, Çöl 2-3 koşu, bütün mağaza 25-40 saat (benzetim 31,8 saat). Ayrıntı: `docs/OYUN_TESTI.md`.

## S-D seviye hedefleri (22 Ekim 2026)
- Hill Climb Racing'deki "Level N: Reach X m" mantığı: her haritada 200, 450, 700, 1000, 1400, 1800, 2400 m hedefleri (sonra her biri 800 m daha uzak). Tamamlanan hedef sayısı harita bazında Store'da kalıcıdır (`goal_<harita>`, yalnızca artar), bonus bir kez ödenir: 25, 30, 35, 40, 45, 50, 55 altın (sonra +5, en çok 100). Her bonus 432 altınlık tipik koşunun %30'unun (129) altındadır ve bonus/metre oranı azalır.
- Koşu başında "Hedef: N metre.", hedefte buy_hcr sesi ve "Hedef tamamlandı, bonus N altın. Sonraki hedef M metre.", en iyi mesafe aşılınca koşu içinde bir kez "Yeni rekor". Oyun sonu özeti ve ana menü bilgisi sıradaki hedefi söyler. Yeni ses dosyası yok.
- Bonus `GameSession.goalBonus`ta tutulur (`coins` değişmez, PlayTest altın ölçüleri aynı kalır); kaydedilen altın = coins + goalBonus.
- Testler: GameTest.goalsAndRecord, SonifyTest.goalSounds, UiSmoke.goalsThroughUi, PlayTest ekonomi benzetimi (tek seferlik bonus dahil).
