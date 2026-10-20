# Boş Yol

Kulakla sürülen, ekran okuyucu uyumlu bir sürüş oyunu. Motor, lastikler, rüzgâr ve yanından geçen araçlar etrafında üç boyutlu duyulur. Görme engelli oyuncular düşünülerek tasarlandı ve TalkBack ile Jieshuo'da kullanılabilir.

## İndir

[Boş Yol APK'sını indir](https://github.com/wzehirx/bosyol/raw/main/indir/BosYol.apk) (sürüm 2.2, Android 8 ve üstü).

İndirdikten sonra dosyayı aç. Android bilinmeyen kaynaklardan yükleme izni isteyebilir, izin ver.

## Özellikler

- Gerçek motor kayıtları. Devire göre katmanlar arasında geçiş yapılır, otomatik vites değişimleri duyulur.
- 3D ses: karşıdan gelen ve sollayan araçlar soldan geçer, Doppler etkisiyle perdeleri değişir.
- Yedi ortam: orman yolu, gece kır yolu, sahil yolu, şehir içi, otoyol, yağmurlu yol, dağ geçidi.
- Dört araç, ayrıca Garaj'da kendi motor sesinle sınırsız araç eklenebilir.
- Oyundaki her ses kendi kaydınla değiştirilebilir.
- Basılı tutulan gaz ve fren. İstenirse tek dokunuşla sabitlenir. Telefonun ses tuşlarıyla da sürülebilir.
- Otomatik sürüş, yardımcı pilotun viraj uyarıları, trafiği açma ve kapama.

## Klasörler

- `android/`: Kotlin ile yazılmış Android uygulaması
- `sounds/`: oyun sesleri
- `web/`: ilk web sürümü (tek HTML dosyası)

## Derleme

Gerekenler: JDK 17 veya üstü, Android SDK (platform 34, build-tools 34.0.0) ve Kotlin derleyicisi 1.9.24. Kotlin derleyicisi `android/tools/kotlinc/` klasöründe olmalı. İmzalama anahtarı `android/bosyol.jks` depoda bulunmaz, ayrıca saklanır.

```
cd android
ANDROID_HOME=/opt/android-sdk python3 build.py 3 2.1
```

## Ses kaynakları ve lisanslar

- Motor kayıtları: Bruno Simon, [folio-2019](https://github.com/brunosimon/folio-2019) (MIT)
- Korna ve lastik sesleri: [pmndrs/racing-game](https://github.com/pmndrs/racing-game) (CC0) ve folio-2019
- Doğa, yağmur, şehir ve otoyol kayıtları: [Moodist](https://github.com/remvze/moodist) (CC0 ve Pixabay İçerik Lisansı)
- Elektrikli araç sesi, rüzgâr ve lastik uğultusu kodla üretilir.
