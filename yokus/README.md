# Yokuş Ustası

Yokuş Ustası kulakla oynanan bir tepe tırmanma oyunu. Görme engelli oyuncular düşünülerek tasarlandı; TalkBack ve Jieshuo ile kullanılabilir. Hill Climb Racing'den esinlendi. Yolun eğimi, aracın açısı, altınlar ve yakıt bidonları sesle anlatılır.

## İndir

Android 8 ve üstü için:

https://github.com/wzehirx/bosyol/raw/main/yokus/indir/YokusUstasi.apk

İndirdikten sonra dosyayı aç. Android bilinmeyen kaynaklardan yükleme izni isteyebilir, izin ver.

## Nasıl oynanır

- Ekranın sağ tarafı gaz, sol tarafı fren. İkisi de basılı tutulur.
- Ayarlardan "Pedal kilidi" açılırsa tek dokunuşla sabitlenir. Ekran okuyucuyla çift dokunuş da pedalı açıp kapatır.
- Ayarlardan "Ses tuşları gaz/fren" açılırsa telefonun ses açma tuşu gaz, ses kısma tuşu fren olur.
- Geri tuşu oyunu duraklatır.
- Ortadaki "Durum" düğmesi mesafeyi, yakıtı, altını ve aracın açısını söyler.
- Araç havadayken gaz burnu kaldırır, fren burnu indirir.
- Sürücünün başı yere değerse kaza olur. Yakıt biterse oyun biter.
- Yolda yakıt bidonları var. Biri alınınca depo dolar.

## Sesler ne anlatır

- Öndeki şeyler sağ kulakta, arkadakiler sol kulakta duyulur. Yukarıdaki şeyler daha ince sesle duyulur.
- Eğim tonu: aracın burnu kalktıkça ses incelir, indikçe kalınlaşır. Açı 70 dereceyi geçince hızlanan bip sesi takla tehlikesini haber verir.
- Yol radarı: kısa tıkırtılar biraz ilerideki eğimi anlatır. Tık incelirse önde yokuş, kalınlaşırsa iniş vardır.
- Önde dik yokuş, dik iniş, rampa, çukur ya da tümsek varsa sesli uyarı gelir.
- Öndeki altınlar parıltı sesiyle duyulur. Yakıt bidonunun iki notalı kendi işaret sesi vardır.
- Yakıt yüzde 25'e ve yüzde 10'a düşünce uyarı gelir.
- Havada takla atmak ve havada uzun kalmak ek altın kazandırır.

## Araçlar

Cip, Motosiklet, Pikap, Traktör, Kum arabası, Ralli arabası, Canavar kamyon, Otobüs, Kar motosikleti, Ay aracı, Tank, Süper araba. Hepsinin kendi motor sesi ve sürüş karakteri var.

## Haritalar

Kır yolu, Çöl, Kutup, Ay (yer çekimi düşük), Orman, Dağ, Plaj, Mağara, Bataklık, Şantiye. Her haritanın kendi zemini ve ortam sesi var.

## Garaj

Her araç için dört geliştirme var: Motor, Süspansiyon, Lastik ve Yakıt deposu. Her biri 10 seviyeye kadar yükseltilir.

## Derleme

Gerekenler: JDK 17 veya üstü, Android SDK (platform 34, build-tools 34.0.0) ve Kotlin derleyicisi 1.9.24. Kotlin derleyicisi `android/tools/kotlinc/` klasöründe olmalı.

```
python3 android/build.py 1 1.0
```

İmzalama anahtarı `android/yokus.jks` depoda bulunmaz, ayrıca saklanır. Anahtar yoksa derleme yenisini oluşturur.

Testler:

```
bash android/test/run.sh TerrainTest
bash android/test/run.sh PhysicsTest
bash android/test/run.sh GameTest
bash android/test/run.sh SonifyTest
```

## Ses kaynakları

- Freesound'dan CC0 lisanslı kayıtlar. Tam liste ve bağlantılar `sounds/CREDITS.csv` dosyasında.
- Bazı motor katmanları Boş Yol'dan alındı: Bruno Simon, folio-2019 (MIT).
- Bazı sesler oyun için sentezlendi.
