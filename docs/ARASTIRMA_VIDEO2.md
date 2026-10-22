# Hill Climb Racing: Video 2 Araştırması (menü, oynanış, fizik, ses)

Tarih: 2026-10-22
Kaynak: 609,8 sn oynanış videosu. Kareler 3 sn aralıkla alındı (203 kare, 0:00–10:06). Ses: 22050 Hz mono, 1 sn çözünürlüklü özet dosyası ve yüksek bant vuruş listesi.
Kapsam: menü akışı, HUD, altın/yakıt/hava bonusu, arazi, fizik tahmini, ses verisi ve bizim oyunla karşılaştırma.

Etiketler: **[Ö]** = ölçüm (sayı doğrudan ekrandan okundu ya da zaman damgasından hesaplandı). **[T]** = tahmin (kareden göz kararı, yaklaşık ±%10–20). **[?]** = ölçülemedi.

---

## 0. Yöntem ve güvenilirlik

### 0.1 Ne okundu
- **Izgaralar:** s01–s17 (17 adet, her biri 4x3 = 12 hücre). s01–s16 tam dolu (192 kare). s17'de 11 dolu hücre var, son hücre boş. Toplam **203 kare** görsel olarak okundu. Kareler 0:00'dan 10:06'ya kadar 3 sn aralıkla dizilidir.
- **Tam çözünürlük doğrulaması:** 6 kare ayrıca açıldı (f/0049 = 2:24, f/0055 = 2:42, f/0064 = 3:09, f/0094 = 4:39, f/0104 = 5:09, f/0201 = 10:00). Bu karelerde seviye bonusu, yükseltme fiyatı ve 5/100/500 jetonları doğrulandı.
- **Ses:** audio_timeline.txt (610 satır, 0–609 sn) ve onsets_hi.txt (977 vuruş) Python ile özetlendi. Ham a.wav (609,83 sn) analizde kullanılmadı.

### 0.2 Ölçülemeyenler ve sınırlar
- **OCR yok.** Sistemde tesseract/pytesseract bulunmuyor. Sol üst sayaç ve fiyat rakamları 320x180 karede kısmen bulanık. Okunan rakamlar ±1 hane hatası içerebilir.
- **Metre ölçeği yok.** Ekran pikseli ile metre arasındaki bağ bilinmiyor. Tepe aralığı, boşluk genişliği ve yakıt kutusu aralığı metre cinsinden **ölçülemedi**. Yalnızca zaman damgası ve HUD mesafesinden hız tahmin edildi.
- **Yakıt göstergesi yok.** Hiçbir karede yakıt çubuğu görünmüyor. Yakıt tüketim hızı **[?]**.
- **Fizik parametreleri çıkarılamaz.** Kütle, ivme, sürtünme ve dönme ataleti kareden belirlenemez.
- **Motor perdesi ölçülemedi.** Ses kaydı müzik ve efektin karışımı. Baskın frekans ile gaz arasında tutarlı bir ilişki bulunamadı **[?]**.
- **onsets_hi.txt güvenilir değil.** Yüksek bant oranı (high%) ile saniyedeki vuruş sayısı arasında r = -0,65 (ters yön) çıktı. Bu dosya altın sesini doğru yakalamıyor gibi. Altın tespiti için kullanılmadı.
- **Örnekleme alt sınırı verir.** 3 sn aralık nedeniyle 1–2 sn süren "AIR TIME" ve altın pop-up'ları kaçmış olabilir. Bu belgedeki sayılar alt sınırdır.

---

## 1. Menü yapısı

### 1.1 Ana ekran ve sekmeler [Ö]
- **Alt sekme çubuğu:** SHOP (mağaza) · STAGE (harita) · VEHICLE (araç) · TUNE (yükseltme) · START (yeşil). Seçili sekme vurgulu ve mavi.
- **Üst çubuk:** Sol tarafta altın sayacı, hemen altında mavi elmas (gem) sayacı. Sağda kupa ve dişli ikonu.
- **Arka plan:** Seçili haritaya ve araca göre değişiyor (gece, mevsim, arena).

### 1.2 Ekran akışı (zaman damgaları)

| Zaman | Ekran | Gözlem |
|---|---|---|
| 0:00 | VEHICLE | Jeep seçili. Garage ve Motocross Bike kilitli (Motocross 75 000). |
| 0:03 | STAGE | Night kilitli (1 200 000, okunuş). Roller Coaster ve Rooftops kilitli. |
| 0:06 | Daily Challenge | Arena, 4400 m. "Free tries: 5". |
| 0:09 | STAGE | Countryside seçili. Kart üzerinde 806 m rekor ve 1000 m hedef. Seasons kilitli. |
| 0:12 | Koşu başı | "Level 4: Reach 800m" bannerı. |
| 2:39 | Koşu içi | "LEVEL 4 COMPLETE" ve "+50 AIR TIME". |
| 2:42 | Koşu içi | "Level bonus +14000" (f/0055 ile doğrulandı). |
| 2:48 | Oyun sonu | OUT OF FUEL: 806 m, +9 elmas, +28 975 altın, 22 hava bonusu, 0 back flip, 0 neck flip. |
| 2:51–2:57 | Menüler | Sırayla TUNE, STAGE, VEHICLE (Jeep). |
| 3:00 | VEHICLE | Jeep, Motocross Bike (75 000) ve Monster Truck görünüyor. |
| 3:03 | Popup | "UPGRADE ENGINE?" 25 000. |
| 3:06 | Popup | "UPGRADE 4WD?" 16 000. |
| 3:09 | Popup | "UPGRADE ENGINE?" 36 000 (f/0064 ile doğrulandı). |
| 3:12 | Popup | "BUY BOOSTER?" 30 elmas. Açıklama: booster "birkaç metre fazla sürmeye" yarar. |
| 3:15 | TUNE | Dört yuva. Fiyatlar 49 000 (1. yuva) ve 25 000 (diğer üç yuva). |
| 3:18 | Koşu başı | "Level 5: Reach 1000m". |
| 3:33 | Koşu içi | "First Hills" bölge başlığı (Countryside). |
| 3:42 | Oyun sonu | DRIVER DOWN: 243 m, +3 elmas. |
| 3:51 | Koşu başı | "Level 5: Reach 1000m" (yeni koşu). |
| 4:06 | Oyun sonu | DRIVER DOWN: 142 m (okunuş belirsiz). |
| 4:12–4:15 | STAGE | Seasons 50 000 kilitli. Seçilince "0 m / 200 m" hedef. |
| 4:18 | Koşu başı | "Level 1: Reach 200m", Seasons, YEAR 1. |
| 4:39 | Koşu içi | "Level bonus +6000" (f/0094 ile doğrulandı). |
| 4:42 | Koşu içi | "Level 2: Reach 450m". |
| 4:45 | Koşu içi | "BIG AIR TIME +200". |
| 5:06 | Koşu içi | "LEVEL 2 COMPLETE". |
| 5:09 | Koşu içi | "Level bonus +11000" (f/0104 ile doğrulandı). |
| 5:12 | Koşu içi | "Level 3: Reach 700m". |
| 5:54 | Oyun sonu | DRIVER DOWN and OUT OF FUEL: 528 m, +8 elmas, +22 140 altın, 17 hava bonusu, 1 back flip. "RECORD: 528m". |
| 6:00–6:03 | Daily Challenge | Arena, "Free tries: 5". Vehicle ekranında Jeep. |
| 6:06 | Challenge koşusu | "CHALLENGE: Drive 4400m". |
| 6:21 | Challenge koşusu | "FUEL LOW". |
| 6:27 | Oyun sonu | TAP TO SAVE: 61 m (Arena). |
| 6:30 / 6:48 | Daily Challenge | "Free tries: 4", sonra "Free tries: 3". |
| 6:51–7:09 | STAGE / VEHICLE | Kilitli harita ve araç listesi. Bkz. 1.4. |
| 7:12–7:27 | VEHICLE / STAGE | Sleigh, Garage (300 elmas), Forest, Mountain. Bkz. 1.4. |
| 7:30–7:33 | Koşu başı | Countryside, "Level 5: Reach 1000m". |
| 8:24 | Oyun sonu | DRIVER DOWN: 519 m (araç ters dönmüş, ardından ölüm). |
| 8:27–8:30 | TUNE | Yuva 2 üzerinde "FREE" etiketi. |
| 8:33 | Koşu başı | Countryside, "Level 5: Reach 1000m". |
| 10:00 | Koşu içi | Değerli jetonlar: 5, 100 ve 500 aynı karede. |
| 10:06 | Oyun sonu | DRIVER DOWN: 746 m, +8 elmas, +12 095 altın, 16 hava bonusu, 0 back flip. Video burada bitiyor. |

### 1.3 Yükseltme (TUNE) ve fiyatlar [Ö]
- **Yuva sayısı ve seviyeler:** Dört yuva. Seviye sayaçları x/13, x/14, x/16 ve x/10 (okunan). Yuvaların adları okunamadı **[?]**.
- **Onay popup'ı:** Her yükseltme önce onay ister. Popup'ta yükseltmenin açıklaması ve fiyat yer alır.
  - Motor: "güç artışı, tepe ve atlama mesafesi" (25 000 ve 36 000 okundu, ikisi de farklı seviyeler).
  - 4WD: "çekiş ve tutuş" (16 000).
- **Booster:** 30 elmas, onay popup'ı ile satın alınıyor. 8:27–8:30 karelerinde bir yuvada "FREE" etiketi var. Bu, ücretsiz kullanım hakkı olarak yorumlanabilir **[T]**.

### 1.4 Harita ve araç fiyatları (okunan) [Ö/?]

| Harita | Fiyat / durum | Not |
|---|---|---|
| Countryside | Ücretsiz (açık) | Rekor 806 m, hedef 1000 m. |
| Seasons | 50 000 | Kilit. |
| Arctic | 75 000 | Kilit. |
| Night | 1 200 000 | Okunuş. |
| Ragnarok, Mudpool | 1 000 000 | Okunuş. |
| Forest, Mountain | 850 000 | Okunuş. |
| Desert, Highway, Roller Coaster, Rooftops, Nuclear Plant, Jungle, Volcano | **[?]** | Rakamlar bulanık. |

| Araç | Fiyat / durum | Not |
|---|---|---|
| Jeep | Başlangıç aracı | Tüm koşularda kullanıldı. |
| Motocross Bike | 75 000 | Kilit. |
| Tourist Bus | 200 000 | Okunuş. |
| Race Car | 250 000 | Okunuş. |
| Rally Car | 750 000 | Okunuş. |
| Dragster | 800 000 | Okunuş. |
| Truck, Sleigh, Dune Buggy | 1 000 000 | Okunuş. |
| Garage | 300 elmas | Okunuş. |
| Monster Truck, Tractor | **[?]** | Rakamlar bulanık. |

### 1.5 Günlük görev (Daily Challenge) [Ö]
- **Hedef:** Arena, "Drive 4400m". Hedefe ulaşmak yerine bu haritada koşu sonunda 61 m görüldü.
- **Hak:** Ücretsiz deneme 5 → 4 → 3 (6:00 → 6:30 → 6:48). Her deneme bir hak düşürüyor.
- **Ödül kartı:** Kartta 150 ile yazılı bir simge var (simgenin türü belirsiz). Geri sayım sayacı görünüyor, okunuşlar tutarsız. Sayacın davranışı **[?]**.

### 1.6 Oyun sonu ekranı [Ö]
Akış: koşu biter → başlık ("DRIVER DOWN" veya "OUT OF FUEL") → kart: harita görüntüsü, mesafe (m), "+N GEMS", "+N COINS", "xN AIR TIME", "xN BACK FLIP", "xN NECK FLIP" → "TAP TO SAVE" (kaydetme) → "TOUCH TO CONTINUE".
- Üst bildirim: "NEW RECORD!" (1:51), koşu içinde.
- Kart başlıkları: DRIVER DOWN (5 kez: 3:42, 4:06, 8:24, 10:06 ve 5:54'te DRIVER DOWN and OUT OF FUEL) ve OUT OF FUEL (2:48).

---

## 2. Oynanış: HUD, altın, yakıt, bonuslar, arazi

### 2.1 HUD [Ö]
- **Sol üst:** Altın sayacı (örn. 605 000 → 619 000). Koşu içinde artıyor.
- **Sol üst alt satır:** Mavi elmas (gem) sayacı. Koşu içinde 0'dan başlıyor, koşu sonunda kart değeriyle tutarlı.
- **Üst sağ:** Hedef ve rekor metreleri (Countryside'da 806 m rekor, 1000 m hedef). İnce bir ilerleme çubuğu ve bayrak simgesi konumu gösteriyor.
- **Alt sol:** BRAKE (fren) pedalı.
- **Alt orta:** RPM kadranı (sol kadran, kırmızı bölge sağda). Ortada mavi-kırmızı bir top göstergesi var. İşlevi (eğim veya dönüş) **[T]**.
- **Alt sağ:** BOOST kadranı (turuncu-kırmızı bölge) ve GAS (gaz) pedalı.
- **Pause:** Sağ üst köşede.
- **Yakıt çubuğu yok.** Yakıt yalnızca kırmızı bidon toplama ve "FUEL LOW" uyarısı ile takip edilebiliyor.

**Elmas tutarlılığı [Ö]:**
- Koşu 1: 0 → 9 (kart: +9).
- Koşu 2: 13 (3:57) → 21 (5:57'deki menü) (kart: +8).
- Koşu 3: 27 (8:24) → 35 (10:06) (kart: +8).
- Elmas kazancı koşu başına 8–9 ile tutarlı.

### 2.2 Altın dizilimi [T/Ö]
- **Yay (arc) tipi:** Altınlar tepe üstünde yay biçiminde dizilmiş. Gözlemlenen gruplar 5–8 altın arasında (ör. 0:27–0:33 yay 7, 0:45 yay 6–7, 1:03 yay 5–6, 4:54 yay 7). Tipik grup uzunluğu **5–8 altın**.
- **Yayın konumu:** Tepe üstü, çukurdan çıkış ve sıçrama sonrası havada yer alıyor. Havadaki yaylar tek sıra halinde ilerliyor.
- **Tek altın:** Boşlukta ve iniş yönünde serpiştirilmiş.
- **Değerli jetonlar:** 10:00 ve 10:03 karelerinde 5, 100 ve 500 etiketli jetonlar yan yana görüldü. Bu tür jeton 10 dakikada yalnızca bu iki karede görüldü. Nadir **[Ö]**.
- **Altın sayacı ile kart farkı:** Koşu 1'de sayaç 605 000 (0:12) → ~619 000 (2:36–2:42) arasında ~14 000 artıyor. Oysa kart "+28 975" gösteriyor. Fark, kart toplamının sayaca yansımadığını veya sayacın başka bir şey (banka, harcama) gösterdiğini düşündürüyor **[?]**. 3:24'te sayaç 516 880 gösteriyor (yaklaşık 100 000 düşüş). Bunun nedeni (yükseltme harcaması olabilir) doğrulanamadı **[?]**.

### 2.3 Yakıt [Ö/T]
- **Kırmızı bidon (jerrycan):** Koşu 1 içinde (0:12–2:48) en az 10 karede görüldü (örn. 0:18, 0:27, 0:45, 0:51, 1:00, 1:12, 1:24). Aralık, karede göz kararı ~8–15 sn **[T]**. Metre karşılığı **[?]**.
- **FUEL LOW uyarısı:** 2:36 (Koşu 1) ve 6:21 (Arena).
- **Bitiş süresi:** FUEL LOW → "OUT OF FUEL" 12 sn (2:36 → 2:48). Arena'da 6 sn (6:21 → 6:27). Yakıt bitişi, uyarıdan 6–12 sn sonra gerçekleşiyor **[Ö, iki örnek]**.
- **Tüketim hızı:** Gösterge olmadığı için ölçülemedi **[?]**.

### 2.4 Hava bonusları (AIR TIME) [Ö]
Koşu başına hava bonusu sayısı (oyun sonu kartından):

| Koşu | Mesafe | Hava bonusu | Mesafe / bonus |
|---|---|---|---|
| Koşu 1 (Countryside, 0:12–2:48) | 806 m | 22 | ~37 m |
| Koşu 2 (Seasons, 4:18–5:54) | 528 m | 17 | ~31 m |
| Koşu 3 (Countryside, 8:33–10:06) | 746 m | 16 | ~47 m |

Örneklemde görülen pop-up değerleri (3 sn aralıkta, 29 adet): +25 (en sık), +50, +75, +100 ve tek örnek +200 ("BIG AIR TIME", 4:45).

Örnek zaman damgaları: 0:24 (+50), 0:33 (+75), 1:24 (+50), 1:30 (+25), 2:39 (+50), 3:27 (+75), 3:33 (+100), 4:36 (+75), 4:39 (+25), 4:45 (+200), 4:48 (+100), 4:54 (+25), 5:39 (+25), 6:12 (+75), 7:42 (+75), 7:51 (+75), 7:54 (+50), 8:09 (+100), 8:42 (+75), 8:57 (+100), 9:42 (+100), 9:51 (+50).

Yakalanma oranı ölçülemedi **[?]**, ama örnekleme bonusların bir kısmını kaçırmış.

**Flip bonusu [Ö]:** Kart değerlerine göre back flip 0, 1, 0 (Koşu 1, 2, 3). Neck flip yalnızca Koşu 1'de 0 olarak yazılı. Örneklemede flip pop-up'ı görülmedi. Flip bonusu nadir.

### 2.5 Seviye (level) sistemi [Ö]
- Her harita, "Level N: Reach X m" hedefi ile ilerliyor. Hedef tamamlandığında "Level N COMPLETE" ve "Level bonus +Y" çıkıyor.
- Ölçülen seviyeler:

| Seviye | Harita | Hedef | Bonus | Zaman |
|---|---|---|---|---|
| Level 4 | Countryside | 800 m | +14 000 | 2:42 (Koşu 1) |
| Level 1 | Seasons | 200 m | +6 000 | 4:39 (Koşu 2) |
| Level 2 | Seasons | 450 m | +11 000 | 5:09 (Koşu 2) |
| Level 3 | Seasons | 700 m | ulaşılmadı | Koşu 2 sonu 528 m |
| Level 5 | Countryside | 1000 m | ulaşılmadı | Koşu 3 sonu 746 m |

- **Bonus / hedef oranı (türetilmiş):** L1 = 30 altın/m, L2 = 24,4 altın/m, L4 = 17,5 altın/m. İlerledikçe birim bonus düşüyor.
- **Bonus payı (türetilmiş):** Koşu 1'de bonus 14 000 / 28 975 = %48. Koşu 2'de bonus 17 000 / 22 140 = %77. Bu, kartın altın toplamına seviye bonusunun dahil olduğu varsayımına dayanıyor **[T]**.

### 2.6 Arazi [T/Ö]
- **Tepe dalgası:** Tepe aralığı kabaca ekran genişliğinin 1/3 ila 1/2'si (320 px karede ~110–160 px). Tepe yüksekliği kabaca ekran yüksekliğinin 1/5 ila 1/3'ü. Metre karşılığı **[?]**.
- **Dik iniş ve yokuş:** 2:57 (uçurum benzeri inişe araç atlıyor), 5:24–5:33 (dik iniş ve kayma), 8:21 (dik yokuşta araç ters dönüyor). Eğim, göz kararı 40–60° **[T]**.
- **İp köprü (rope bridge):** Örneklerde 0:57, 1:30, 2:03, 2:12, 3:39 ve 8:54 karelerinde. Boşluk genişliği ekranın ~1/4 ila 1/3'ü **[T]**.
- **Su ve çukur:** 1:27–1:33 ve 2:03 karelerinde mavi su boşlukları. Araç çukurdan çıkıyor veya yüzeyde ilerliyor.
- **Arena:** Ateş ve lav boşlukları, karanlık, spot ışıkları. Engel yoğunluğu daha fazla.

---

## 3. Fizik ve tempo

### 3.1 Koşu tablosu (hız tahmini) [Ö/T]

| Koşu | Harita | Başlangıç (banner) | Bitiş | Mesafe | Süre | Ort. hız | Son |
|---|---|---|---|---|---|---|---|
| K1 | Countryside | 0:12 | 2:48 | 806 m | 156 sn | ~5,2 m/s | Out of fuel |
| K2 | Countryside | 3:18 | 3:42 | 243 m | 24 sn | ~10 m/s | Driver down |
| K3 | Countryside | 3:51 | 4:06 | ~142 m | 15 sn | ~9,5 m/s | Driver down (okunuş belirsiz) |
| K4 | Seasons | 4:18 | 5:54 | 528 m | 96 sn | ~5,5 m/s | Driver down & out of fuel |
| K5 | Arena (challenge) | 6:06 | 6:27 | 61 m | 21 sn | ~2,9 m/s | Driver down (sebep **[?]**) |
| K6 | Countryside | 7:33 | 8:24 | 519 m | ~51 sn | ~10 m/s | Driver down |
| K7 | Countryside | 8:33 | 10:06 | 746 m | 93 sn | ~8,0 m/s | Driver down |

Zaman damgaları ±3 sn. Başlangıç için banner zamanı kullanıldı. Hız aralığı yaklaşık ±10%.

### 3.2 Yorum [T]
- **Hız aralığı:** Countryside koşuları 8–10 m/s, Seasons 5,5 m/s, Arena ~3 m/s.
- **Yükseltme hipotezi:** K1, 3:03–3:09 yükseltmelerinden önce yapıldı ve ~5,2 m/s. Yükseltmelerden sonra Countryside koşuları (K2, K3, K6, K7) 8–10 m/s. Bu, motor yükseltmesinin hızı artırdığı ihtimalini düşündürüyor. Ancak harita (Level 4 ve Level 5) ve rota farkı ayrıştırılamadı. **Hipotez, doğrulanmadı.**
- **Havada dönme:** Hava bonusları sırasında araç ön/arka eksende dönüyor. Flip bonusu nadir (Koşu 2'de 1 back flip).
- **İniş:** Tekerlek-zemin temasında toz/duman bulutu çıkıyor (örn. 1:21, 5:21, 5:24). Bu, iniş darbesi veya gaz ile tekerlek kayması olabilir **[T]**.
- **Devrilme ve ölüm:** Araç ters dönünce 3–5 sn içinde DRIVER DOWN (8:21 → 8:24, 3:36 → 3:42). Ölüm, sürücünün zemine çarpmasıyla tetikleniyor görünüyor **[T]**.
- **Ölüm koşulları:** İki ayrı sonuç: (a) DRIVER DOWN (araç ters, sürücü düşüyor), (b) OUT OF FUEL (yakıt bitiyor).

---

## 4. Ses (audio_timeline.txt ve onsets_hi.txt)

### 4.1 Genel özet [Ö]
- **Seviye (rms dB):** Medyan -21 dB, aralık -25 ile -16 dB. Hiçbir saniye -40 dB altında değil, yani sessizlik yok. Ses sürekli (müzik ve efekt).
- **Bant ortalamaları:** Düşük (<300 Hz) %57, orta %31, yüksek (>2 kHz) %12.
- **Baskın frekans:** Saniyelerin %82,8'inde baskın bir tepe var. En sık değerler 86, 88, 104, 116, 130 ve 168 Hz. Medyan ~100 Hz. 500–734 Hz arası nadir değerler (menü müziği veya efekt olabilir).
- **Motor perdesi:** Gaz ile baskın frekans arasında tutarlı bir ilişki bulunamadı. **[?]**

### 4.2 Zirveler ve ölüm anları [Ö/T]
Ses zirveleri (en yüksek dB) ile DRIVER DOWN anları örtüşüyor:

| Ses zirvesi (sn) | Zaman | dB | Eşleşme |
|---|---|---|---|
| 223–224 | 3:43–3:44 | -17 | DRIVER DOWN (3:42) |
| 248–249 | 4:08–4:09 | -16 / -17 | DRIVER DOWN kartı (4:06) |
| 506 | 8:26 | -16 | DRIVER DOWN (8:24) |
| 606–607 | 10:06–10:07 | -17 / -16 | DRIVER DOWN (10:06) |

Eşleşmeyen zirveler: 13 sn (0:13, -18 dB, koşu başı), 263 sn (4:23, -17 dB), 408 sn (6:48, Arena, -16 dB). Bu üç zirvenin nedeni doğrulanamadı **[?]**.

Sonuç: Ses zirvesi, sürücünün ölümü için bir ipucu olabilir **[T, 4 örnek]**.

### 4.3 Vuruşlar (onsets_hi.txt) [Ö/?]
- **Sayı:** 977 vuruş, 0–609 sn.
- **Dakika başına:** 0. dk 89, 1. dk 70, 2. dk 103, 3. dk 118, 4. dk 76, 5. dk 59, 6. dk 144, 7. dk 148, 8. dk 73, 9. dk 71, 10. dk 26.
- **Aralık:** Medyan 0,30 sn. En sık aralıklar 0,2 sn (246), 0,3 sn (215), 0,1 sn (182).
- **Hipotez:** 0,2–0,3 sn aralıklı vuruşlar, altın yayının tempo'su olabilir (yaklaşık 3 altın/sn). **Doğrulanmadı.**
- **Güvenilirlik:** r = -0,65 (high% ile vuruş sayısı). Bu, vuruş dosyasının altın tespiti için uygun olmadığını gösteriyor **[?]**.

---

## 5. Bizim oyunla karşılaştırma ve öneriler

Bizim oyun: Kır yolu haritası, altın = 1 değer, yakıt kutusu, havada kalma bonusu. Aşağıdaki öneriler videodaki **oranlara ve zamanlamalara** dayanıyor. Metre ölçeği bilinmediği için mutlak metre değerleri **[T]**.

| # | Konu | Video değeri | Öneri | Güven |
|---|---|---|---|---|
| 1 | Altın grubu | Tepe üstünde yay, 5–8 altın/grup | Grup uzunluğu 5–8, yay tepe üstünde. Grup genişliği ekranın ~1/4'ü. | [T] |
| 2 | Değerli jeton | 5/100/500 jetonları 10 dk'da 2 karede | Büyük değerli altın nadir tutulsun (%2–3 spawn). Altın = 1 ayarını bozmadan opsiyonel. | [Ö/T] |
| 3 | Elmas | Koşu başına 8–9 elmas, 12–19 sn'de bir | Elmas aralığını 12–19 sn olarak ayarla. Koşu başına 8–9 hedef. | [Ö] |
| 4 | Yakıt kutusu aralığı | Karede 8–15 sn'de bir bidon | Hızla eşle: 5–10 m/s ile 60–110 m aralık (metre ölçeği bilinmediğinden kaba). | [T] |
| 5 | Yakıt uyarısı | FUEL LOW → bitiş 6–12 sn | Uyarı, bitişten en az 6 sn önce. Son bidon sonrası 6–12 sn pencere. | [Ö] |
| 6 | Hava bonusu kademeleri | +25, +50, +75, +100, nadir +200 | Kademeler 25 birim. +200 "BIG AIR" eşiği için uzun uçuş şartı. Koşu başına 16–22 bonus. | [Ö] |
| 7 | Hava bonusu / mesafe | 31–47 m/bonus | Bonus frekansını mesafeyle ilişkilendir. Hedef: ~35–45 m'de bir bonus. | [Ö/T] |
| 8 | Seviye bonusu | 30, 24, 17,5 altın/m (L1, L2, L4) | Bonus / hedef oranını azalan yap. Yüksek seviyelerde daha az altın/m. | [Ö] |
| 9 | Ölüm koşulları | DRIVER DOWN (ters + yere çarpma) ve OUT OF FUEL | İki ayrı sonuç metni. DRIVER DOWN devrilme sonrası 3–5 sn. | [Ö/T] |
| 10 | Hız ve harita | Countryside 8–10 m/s, Seasons 5,5 m/s | Tepe yoğunluğu ve eğim harita bazında ayarlanabilir. Seasons daha yavaş, daha dengeli tepeler. | [T] |
| 11 | Menü akışı | STAGE, VEHICLE, TUNE, START. Yükseltmede onay popup'ı. Booster'da 30 elmas onayı. Daily Challenge hakkı (5 → 4 → 3). | Bu sıra ve onaylar bizim akışa uygulanabilir. Günlük görev hakkı için sayaç ekle. | [Ö] |
| 12 | Oyun sonu kartı | Mesafe, elmas, altın, hava bonusu, back flip, neck flip, TAP TO SAVE | Kartta mesafe, altın, elmas, hava sayısı ve flip sayısını göster. | [Ö] |
| 13 | Rekor bildirimi | "NEW RECORD!" koşu içinde | Rekor aşıldığında koşu içinde bildirim ekle. | [Ö] |
| 14 | Yükseltme etkisi | Upgrade sonrası hız 5,2 → 8–10 m/s (hipotez) | Yükseltme etkisi ölçülebilir olsun diye bizim oyunda hız çıktısı kaydet. | [T] |

---

## 6. Açık sorular ve sonraki adım
- **Metre ölçeği:** Ham video (tam çözünürlük) ile bilinen bir nesnenin genişliği (ör. araç) kullanılarak piksel-metre oranı kurulmalı. Böylece tepe aralığı, boşluk ve yakıt kutusu aralığı metre cinsinden ölçülebilir.
- **Yakıt tüketimi:** Tek bir koşu, yakıt göstergesi açık bir video ile ölçülebilir.
- **Ses etiketi:** Gaz/bırakma ve motor perdesi için etiketli bir ses kaydı gerekli. Mevcut ses verisi motor perdesini ayırt etmeye yetmiyor.
- **Altın sesi:** onsets_hi.txt yeniden üretilmeli (farklı bant ve eşik ile). Altın toplama anları kareyle doğrulanmalı.
- **Sayaç tutarsızlığı:** Sol üst altın sayacı ile kart toplamı arasındaki fark (2.2) açıklanmalı.
