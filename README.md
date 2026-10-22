# Yokuş Ustası

Kulakla oynanan, Hill Climb Racing tarzı bir tepe tırmanma oyunu. Görme engelli oyuncular için yapıldı; TalkBack ve Jieshuo ile kullanılır. Yolun eğimi, aracın açısı, altınlar ve yakıt bidonları sesle anlatılır. Ekrana bakmadan gazı ve freni yönetir, tepeleri sesinden okursun.

**Sürüm 2.0:** 20 araç, 16 harita, her altında ses, menü sesleri ve titreşim, seviye hedefleri.

## İndir

Android 8 ve üstü için, release anahtarıyla imzalı APK (doğrudan indirme, GitHub sürümü değil):

https://github.com/wzehirx/bosyol/raw/main/indir/YokusUstasi.apk

İndirdikten sonra dosyayı aç. Android bilinmeyen kaynaklardan yükleme izni isteyebilir, izin ver. Eski sürümün üstüne kurulur, kayıtların korunur.

## Öne çıkanlar

- **Sesle oynanış:** Eğim tonu, yol radarı, altın ve yakıt işaret sesleri, uyarılar. Öndeki şeyler sağ, arkadakiler sol kulakta.
- **Altın:** Her altın 1 altın eder ve her alınışında ses çalar. Art arda alınca sesler üst üste biner, tempo hızlanır.
- **Seviye hedefleri:** Her haritada 200, 450, 700 m gibi hedefler. Ulaşınca ses, duyuru ve bonus altın. Koşu içinde rekor duyurusu.
- **Menü hissi:** Düğme ve odak sesleri, titreşim, araç ve harita önizlemesi, geri sayım, altın sayımı. Ayarlardan kapatılır.
- **20 araç, 16 harita, 4 geliştirme:** Her aracın kendi motor sesi ve karakteri var.

## Nasıl oynanır

- Ekranın sağ tarafı gaz, sol tarafı fren. İkisi de basılı tutulur.
- Ayarlardan "Pedal kilidi" açılırsa tek dokunuşla sabitlenir. Ekran okuyucuyla çift dokunuş da pedalı açıp kapatır.
- Ayarlardan "Ses tuşları gaz/fren" açılırsa telefonun ses açma tuşu gaz, ses kısma tuşu fren olur.
- Geri tuşu oyunu duraklatır.
- Ortadaki "Durum" düğmesi mesafeyi, yakıtı, altını ve aracın açısını söyler.
- Araç havadayken gaz burnu kaldırır, fren burnu indirir.
- Sürücünün başı yere değerse "Kafa üstü düştün" diye kaza olur. Araç zemine göre baş aşağı dönüp bir saniye yerde kalırsa "Ters döndün" diye kaza olur. Yakıt biterse oyun biter.
- Denge yardımı (Ayarlar, varsayılan açık): burun zemine göre çok kalkarsa gazı yumuşakça kısar, havada gaz ve freni sınırlar. "Dikkat, devrilmek üzeresin" diye uyarır. İstersen kapatabilirsin.
- Yokuşu çıkamayıp yerinde kalırsan "Yokuşu çıkamıyorsun, biraz geri gidip hızlan" diye ipucu verir.
- Mesafe duyurusu varsayılan olarak açıktır: her 250 metrede bir "250 metre" gibi kısa bir duyuru gelir. Ayarlardan kapatılır.
- Yolda yakıt bidonları var. Biri alınınca depo dolar.

## Sesler ne anlatır

- Öndeki şeyler sağ kulakta, arkadakiler sol kulakta duyulur. Yukarıdaki şeyler daha ince sesle duyulur.
- Eğim tonu: aracın burnu kalktıkça ses incelir, indikçe kalınlaşır. Açı 70 dereceyi geçince hızlanan bip sesi takla tehlikesini haber verir.
- Yol radarı: kısa tıkırtılar biraz ilerideki eğimi anlatır. Tık incelirse önde yokuş, kalınlaşırsa iniş vardır.
- Önde dik yokuş, dik iniş, rampa, çukur ya da tümsek varsa sesli uyarı gelir.
- Öndeki altınlar parıltı sesiyle duyulur. Her altın 1 altın değerindedir ve her alınışında ses çalar; harita pahalandıkça altın yoğunluğu artar. Yakıt bidonunun iki notalı kendi işaret sesi vardır.
- Yakıt yüzde 25'e ve yüzde 10'a düşünce uyarı gelir.
- Havada takla atmak ve havada uzun kalmak ek altın kazandırır.

## Araçlar

20 araç var (fiyat sırasıyla): Cip, Scooter, Motosiklet, Go-kart, Pikap, Dörtteker, Traktör, Kum arabası, Chopper, Ralli arabası, Hot rod, Canavar kamyon, Çöp kamyonu, Otobüs, Kar motosikleti, İtfaiye aracı, Ay aracı, Formül araba, Tank, Süper araba. Hepsinin kendi motor sesi ve sürüş karakteri var.

## Haritalar

16 harita var (fiyat sırasıyla): Kır yolu, Çöl, Tuz gölü, Kutup, Ay (yer çekimi düşük), Donmuş göl, Orman, Gece çatıları, Dağ, Plaj, Mağara, Mars (yer çekimi düşük), Bataklık, Volkan, Şantiye, Deniz tabanı (su direnci, yavaş ve uzun süzülme). Her haritanın kendi zemini ve ortam sesi var.

## Garaj

Her araç için dört geliştirme var: Motor, Süspansiyon, Lastik ve Yakıt deposu. Her biri 10 seviyeye kadar yükseltilir.

## Derleme

Gerekenler: JDK 17 veya üstü, Android SDK (platform 34, build-tools 34.0.0) ve Kotlin derleyicisi 1.9.24. Kotlin derleyicisi `android/tools/kotlinc/` klasöründe olmalı.

```
python3 android/build.py 3 2.0
```

İmzalama anahtarı `android/yokus.jks` depoda bulunmaz, ayrıca saklanır. Anahtar yoksa derleme yenisini oluşturur.

Testler:

```
bash android/test/run.sh TerrainTest
bash android/test/run.sh PhysicsTest
bash android/test/run.sh GameTest
bash android/test/run.sh SonifyTest
bash android/test/run.sh PlayTest
bash android/test/robo/run.sh
```

## Sürüm imzası

`indir/YokusUstasi.apk` release anahtarıyla imzalıdır (v2/v3 imza, debug değil). Güncellemelerin eski sürümün üstüne kurulması için aynı anahtar kullanılır.

## Ses kaynakları ve lisans notu

- Freesound'dan CC0 lisanslı kayıtlar. Tam liste ve bağlantılar `sounds/CREDITS.csv` dosyasında.
- Bazı motor katmanları Boş Yol'dan alındı: Bruno Simon, folio-2019 (MIT).
- Bazı sesler oyun için sentezlendi.
- `kaynak/hcr/`: Hill Climb Racing'den alınmış kısa ses efektleri (tık, altın, satın alma, yakıt, turbo, motor vb.). Bu sesler oyunun sahibine aittir; depo sahibi tarafından kişisel, ticari olmayan bir hayran projesi için kullanılır. Ticari kullanım ya da dağıtım için uygun değildir, hak sahibi istemesi hâlinde kaldırılır.
- Eski sürümlerin sesleri ve tüm kod: bu depodaki kaynaklar.

## Depo düzeni

- `android/`: kaynak kod, derleme betiği, testler
- `sounds/`: oyuna giren sesler
- `kaynak/`: ham ses kaynakları
- `tools/`: ses hazırlama betikleri
- `docs/`: tasarım, plan, araştırma ve oyun testi raporları
- `indir/`: imzalı APK
