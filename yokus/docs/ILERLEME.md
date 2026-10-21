# Yokuş Ustası: gece çalışma günlüğü

Kullanıcı isteği (20 Ekim 2026 gecesi): Görme engelliler için Hill Climb Racing'e çok yakın, sesli bir Android oyunu.
Bol araç, bol harita. bosyol deposunda `yokus/` klasörü. Sabah teslim. Kullanıcı onay beklemeden ilerlememe izin verdi.
Tasarım: `yokus/docs/TASARIM.md` (kararlar orada; buna uy).

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
- Herkese açık dosya sitesine yükleme ve yeni depo/release oluşturma bu oturumda engelli; APK depoya dosya olarak konur: yokus/indir/YokusUstasi.apk (bağlantı: https://github.com/wzehirx/bosyol/raw/main/yokus/indir/YokusUstasi.apk).

## Adımlar
- [x] 1. Ses araştırması (Haiku, arka planda başlatıldı; çıktı: yokus/sounds-raw/, CREDITS.csv, RAPOR.md)
- [x] 2. Tasarım belgesi (docs/TASARIM.md)
- [x] 3. Uygulama planı (docs/PLAN.md, superpowers:writing-plans)
- [ ] 4. Uygulama görevleri (Sonnet) + her görev sonrası denetim (Sonnet):
  - [x] G1 İskelet: bosyol'dan Dsp/SoundBank/AudioOut/CrashLog/build.py/ikon kopyala, paket com.yokus.app, boş MainActivity derlensin
  - [x] G2 Defs + Terrain
  - [x] G3 Physics + Opus ayarı (HCR hızları, yumuşak çukurlar, zorluk eğrisi, test/lib/Driver.kt) + JVM testleri
  - [~] G4 Game oturumu (Sonnet çalışıyor) (yakıt, altın, kaza, takla, puan) + testler
  - G5 Ses tasarımı (motor, yüzey, eğim tonu, yol radarı, işaret sesleri, iniş) + WAV ölçüm testi
  - [x] G6 Ses hazırlama (traktör ve kar motosikleti aynı yedek motor sesini aldı; G5'te perdeyle ayır) betiği (sounds-raw → sounds; döngüler dikişsiz, yükseklik eşitleme, eksikler için yedek)
  - [~] G7 Android arayüz (G7a bitti+denetlendi:  menüler/Store/SoundBank/Speaker/core/Audio.kt Sonnet'te; oyun ekranı G7b G4+G5 sonrası) + GameView + Store
  - G8 Robolectric testi, APK, lint, dexdump
  - G9 README, indir/YokusUstasi.apk, push, kullanıcıya özet
- [ ] 5. Sabah teslim: indirme bağlantısı + anahtar dosyasını (yokus.jks) kullanıcıya dosya olarak gönder
