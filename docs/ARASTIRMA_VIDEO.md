# Hill Climb Racing: Video Tabanlı Araştırma (Fizik Hissi ve Menüler)

Tarih: 2026-10-22
Amaç: Görme engelli oyuncular için ses öncelikli bir klon tasarlamak. Bu belge, YouTube'daki herkese açık oynanış videolarından fizik hissi ve menü yapısını çıkarmayı hedefledi.

**Önemli sonuç:** Video dosyası indirilemedi. Fizik ölçümleri (hız, havada kalma süresi, eğim açısı, yakıt tüketimi, zamanlama) yapılamadı. Bölüm (a) ve (b)'deki çıkarımlar büyük ölçüde varsayımdır ve açıkça işaretlenmiştir. Menü yapısı (c) yalnızca birkaç düşük çözünürlüklü kareye dayanır ama en güvenilir bölümdür.

---

## 0. Ne Yapıldı, Ne Yapılamadı

### Kaynaklar

| Kaynak | Sonuç |
|---|---|
| YouTube arama sayfaları (4 sorgu) | Başarılı. `ytInitialData` içinden 48 video listesi alındı. |
| İzleme sayfaları (7 video) | 3 sayfa "Sign in to confirm you're not a bot" (LOGIN_REQUIRED) döndü. 4 sayfa Google 302 (sorry) yönlendirmesi döndü. Altyazı (timedtext) ve storyboard spec alınamadı. |
| Bölüm işaretleri (chapters) | Yalnızca EG_JWYWk_Uw sayfasının `ytInitialData` içinden okundu. |
| yt-dlp ile video indirme | Başarısız. Varsayılan, `android` ve `ios` istemcilerinin üçü de bot kontrolüne veya 403'e takıldı. Hiçbir video dosyası indirilmedi. |
| i.ytimg.com önizlemeleri | Başarılı. 4 video için `0.jpg`, `1.jpg`, `2.jpg`, `3.jpg` indirildi. `0.jpg` 480x360, diğerleri 120x90. 16 kare okundu (4 video x 0-3). |

### Yapılamayanlar
- Oynanış videosundan kare çıkarımı (2 fps, scene-change, tile): video yok, yapılamadı.
- Mesafe sayacından ortalama hız, havada kalma süresi, eğim açısı, yakıt düşüşü, coin dizilimi, flip'ten game over'a süre: hiçbiri ölçülemedi. Her video için yalnızca 4 zaman-bağımsız kare vardı.
- Storyboard sprite sayfaları: spec, izleme sayfasından alınamadığı için kullanılamadı.
- Altyazılar: alınamadı.

---

## 1. Video Listesi

Seçilen 4 video (indirilemedi, ama kare ve başlık bilgisi kullanıldı):

| # | Video ID | Kanal | Süre | Başlık | Not |
|---|---|---|---|---|---|
| V1 | `fg9smFUXJBA` | TapGameplay | 10:10 | Hill Climb Racing - Gameplay Walkthrough Part 1 - Jeep | HCR1, uzun oynanış. Bot kontrolü. |
| V2 | `CJV58TJzVPU` | TapGameplay | 14:40 | Hill Climb Racing 2 - Gameplay Walkthrough Part 1 | HCR2. Sayfa 302. Yalnızca önizleme. |
| V3 | `EG_JWYWk_Uw` | DroidCheat | 11:12 | Hill Climb Racing - High Score Run & Epic Flips! | HCR1 flip ve yüksek skor. Bölüm işaretleri var. Bot kontrolü. |
| V4 | `cjDnQZsGe4s` | Vereshchak | 9:18 | Hill Climb Racing - New AUTOMOBILE Car Unlocked | Başlık HCR1 diyor, ama kare HCR2 benzeri bir menü gösteriyor. Bot kontrolü. |

Takla başlıklarına bakan ek videolar (yalnızca başlık, izlenmedi): `j-D6hRgPVms` ("Neck Flip", HCR2), `5ItMC1nnOJQ` ("Front Flip"), `wc8ASAKaiz8` ("Backflip"), `uGojAViylqo` ("More Neck Flips"). Bunların içeriği doğrulanmadı.

### V3 bölüm işaretleri (izleme sayfasının `ytInitialData`'sından)
| Zaman | Başlık |
|---|---|
| 8 s | Reach 2000m |
| 328 s | Level 12: Reach 2400m |
| 645 s | Reach 2600m |

Bu işaretler bir kare ile tutarsız görünüyor (bkz. bölüm f). Bu nedenle zamanlama için güvenilir sayılmamalı.

---

## 2. Kullanılan Kareler

Her video için `0.jpg` (orta kare, varsayım), `1.jpg`, `2.jpg`, `3.jpg` okundu. YouTube'un bu dosyalarının zaman damgaları resmi olarak belgelenmemiştir. Yaklaşık %50, %25, %75 olduğu varsayılmıştır ve doğrulanmamıştır.

Dosyalar (`/home/user/bosyol/sounds-raw/video-kareler/`, sonradan silindi): `fg9smFUXJBA_{0,1,2,3}.jpg`, `CJV58TJzVPU_{0,1,2,3}.jpg`, `EG_JWYWk_Uw_{0,1,2,3}.jpg`, `cjDnQZsGe4s_{0,1,2,3}.jpg`.

---

## a. Fizik Hissi Gözlemleri

**Durum: Hız, ağırlık, eğim açısı, havada kalma süresi, süspansiyon, takla, sıçrama, ölüm ve yakıt için video ölçümü yok.** Aşağıdakiler yalnızca kareden çıkan nitel gözlem ve tahmindir.

- **V3 (EG), kare 0 (`EG_JWYWk_Uw_0.jpg`, 480x360):** HUD sol üstte "1302m / 2400m" ve trofe için "2240m" okunuyor. Yakıt çubuğu yeşil. Sağ alt ve sol altta analog bir RPM göstergesi ve bir yakıt göstergesi var. Altta BRAKE ve GAS düğmeleri ekranda sabit duruyor. Araç, çok dik ve kavisli bir raylı yol üzerinde, arkasında siyah duman bulutu var. **Eğim açısı:** görsel tahmin olarak kabaca 60–75° görünüyor. Ölçü değildir, piksel ölçeği bilinmiyor.
- **V3, kare 1 (`EG_JWYWk_Uw_1.jpg`, 120x90):** "Level 10: Reach 2000m" bildirimi. Araçlar peş peşe, yüksek ve kavisli bir parkur üzerinde. Parkur bir roller-coaster gibi görünüyor. Bu bir gözlem, bir ölçüm değil.
- **V3, kare 2 (`EG_JWYWk_Uw_2.jpg`):** Kaldırım üzerinde araçlar yan yana ve yükseltide. Ayrıntı yok.
- **V3, kare 3 (`EG_JWYWk_Uw_3.jpg`):** "Level bonus +9000" yazısı. Seviye geçiş bonusu bir oyun içi bildirim olarak görünüyor.
- **V1 (fg9), kare 1 (`fg9smFUXJBA_1.jpg`):** Kırmızı jip bir toprak tepenin yamacında. Tekerlekler zemine temas ediyor görünüyor. Süspansiyon hareketi tespit edilemez.
- **V1, kare 2 (`fg9smFUXJBA_2.jpg`):** Üst kısımda "First hill" yazısı okunuyor (düşük çözünürlük, emin değilim). Araç yokuş üzerinde.
- **V2 (CJV), kare 0 (`CJV58TJzVPU_0.jpg`):** Kırmızı jip, kask takmış sürücü. Kapak/bölüm kartı izlenimi veriyor, oynanış değil.

**Varsayımlar (kaynak: yalnızca başlıklar, kareler değil):**
- Oyunda ön takla ("neck flip", "front flip") ve geri takla ("backflip") bonus verir. Başlıklar bunu öne sürüyor. Kontrol şekli (gaz/fren ile dönüş) doğrulanmadı.
- Yüksek eğimde ve zıplamada tekerlek ve burun açısı değişir. Bu bir varsayım.
- Ölüm, kafanın yere çarpmasıyla olur. Bu ölümde kare yok, doğrulanmadı.
- Yakıt sıfırlanınca araç durur. Doğrulanmadı.

---

## b. Madeni Para Davranışı

**Durum: Coin dizilimi, aralığı ve değeri ölçülemedi.** Kareler coin toplama anlarını göstermiyor.

- V3 kare 0'da sol üstte sarı bir coin ikonu ve **4 545 750** toplam para görünüyor. Bu bir toplam sayaç, tek bir coin değeri değil.
- V3 kare 3'te "Level bonus +9000" yazısı var. Bu bir seviye bonusudur, coin dizilimi değildir.
- V4 kare 0'da menüde **130 823 445** coin, **119** gem ve **23** başka bir para birimi görünüyor. Bu oyunun menü değerleridir, oyun içi coin değil.
- Coin'in küçük/büyük farkı, zincir (arka arkaya toplama) ve arc/çizgi dizilimi: **görülemedi.**

---

## c. Menü Yapısı

Bu bölüm en güvenilir kısımdır. V4 kare 0 (`cjDnQZsGe4s_0.jpg`, 480x360) en net menü görüntüsüdür.

### V4 kare 0'daki ana menü (okunabilen öğeler)
- **Üst sol:** coin (130 823 445), gem (119), 23 (başka para birimi).
- **Üst sağ:** dört ikon: "FREE" etiketli bir sandık (ücretsiz ödül), günlük görev listesi, kupa (başarılar), dişli (ayarlar).
- **Orta kart:** "AUTOMOBILE" başlığı ve araç önizlemesi. Bu, şu anda seçili aracın kartıdır.
- **Sol kart:** "GARAGE" etiketi ve **02:33:29** sayaç. Zamanlayıcı bir ödül/yenileme göstergesi olabilir (tahmin).
- **Sağ kart:** "HILL CLIMBER" etiketi ve kırmızı bir jip önizlemesi. Komşu araç ya da mod olabilir, kesin değil.
- **Alt çubuk (soldan sağa):** SHOP, STAGE, VEHICLE (seçili, vurgulu), TUNE, START (yeşil, ana eylem).

### V1 kare 3'teki menü (düşük çözünürlük)
- Ortada "JEEP" yazısı ve jip önizlemesi. Alt kısımda yeşil bir buton (START benzeri).
- Sol alt ve sağ alt etiketler okunamadı. Bir "GARAGE" benzeri etiket olabilir.

### Oyun akışı (metin diyagramı)
Bu diyagram menü etiketlerinden çıkarıldı. Yükseltme ekranı (TUNE) ve oyun sonu ekranı kareden doğrulanmadı.

```
[Açılış / Ana menü]
      |
      +--> VEHICLE  ---> Araç seçimi (orta kart; sol GARAGE, sağ HILL CLIMBER)
      |                    |
      |                    +--> TUNE  ---> Yükseltme (doğrulanmadı)
      |
      +--> STAGE    ---> Bölge / seviye seçimi (doğrulanmadı)
      |
      +--> SHOP     ---> Satın alma (doğrulanmadı)
      |
      +--> START    ---> Oyun içi
                           |
                           +--> HUD: mesafe (m/hedef), yakıt, RPM, para, seviye
                           +--> Seviye bildirimi: "Reach 2000m", "Level bonus +9000"
                           |
                           +--> Oyun sonu / sonuç (doğrulanmadı) ---> Ana menü

Üst bar (her ekranda): para (coin, gem, ...), sandık (FREE), görevler, başarılar, ayarlar
```

### Seçim mekanizması
- Araç, orta kartta seçilir ve "VEHICLE" sekmesi vurgulu kalır.
- Bölge/seviye, STAGE sekmesi üzerinden seçilir (etiket gözlendi, ekranı gözlemlenmedi).
- Yükseltme: TUNE etiketi gözlendi, içeriği görülmedi.
- Oyun sonu ekranı: görülmedi. Oyun içi "Level bonus" ve "Reach Xm" bildirimleri görüldü.

---

## d. Ses Olarak Aktarılması Gereken Bilgiler

Görme engelli bir oyuncu, ekrandan şunları sesle almalı. Bu liste, kareler ve oyun türünün genel yapısına dayanır.

**Oyun içi:**
1. **Mesafe ve hedef:** "1302 metre, 2400 metre hedef" gibi periyodik konuşma (örneğin her 100 m'de bir), ve seviye hedefine yaklaşma.
2. **Yakıt:** Yüzde ve düşüş hızı, eşikte uyarı. Ekran üstünde yeşil çubuk, düşük olunca kırmızıya dönüyor (V3 kare 0'da yeşil gözlendi).
3. **Motor / hız:** RPM göstergesi kareden okunuyor (analog). Ses ile RPM'yi temsil etmek gerekir: motor tonu yükseldikçe devir artar.
4. **Gaz ve fren:** Düğme durumu ekranda sabit, kontrol sesle geri bildirim gerektirir (gaz basılı / bırakıldı).
5. **Araç durumu:** Eğim açısı (ön yükseliş, arka yükseliş), havada olma, tekerlek temas durumu. Bu, takla ve dengeyi anlamak için kritik.
6. **Para:** Coin toplama sesi, toplam sayaç (4 545 750 gibi), değer farkı.
7. **Seviye bildirimleri:** "Seviye bonusu +9000", "Reach 2000m" gibi.
8. **Oyun sonu:** Ölüm nedeni (kafa çarpması, yakıt bitti), sonuç mesafesi, kazanılan toplam.

**Menüde:**
1. Seçili öğe ve sekme ("Vehicle", "Stage", "Tune", "Start"). Gezinme sırası ve bulunduğu yer.
2. Kart bilgisi: araç adı, fiyat, zamanlayıcı, kilit durumu.
3. Üst bardaki para birimleri (coin, gem, diğer).
4. Ödül sandığı ve günlük görev: "Ücretsiz ödül hazır" gibi.

---

## e. Mevcut Oyunla Farklar ve Somut Öneriler

Mevcut oyun: 2D yan görünüm, rijit gövde + spring süspansiyonlu 2 tekerlek, gaz ile burun yukarı (havada), fren ile burun aşağı, yakıt, coin, kafa çarpmasıyla ölüm.

Aşağıdaki öneriler, video verisi olmadan çıkarıldı. Ölçü ile doğrulanmalıdır.

1. **Eğim sesi:** Burun açısı arttıkça motor tonunu ve yük sesini değiştir. Ön tekerlek kalkınca ve arka tekerlek kalkınca ayrı kısa sesler kullan (kareler dik eğimde araç arkası yukarı görünüyor, bu kaldırma olayının sesle gösterilmesi gerektiğini düşündürüyor).
2. **Takla bildirimi:** Her 90° ve 360° dönüşte kısa ses ve sol/sağ kanalda döndürme yönünü konuş. Başlıklar takla bonusunu doğruluyor, ama kontrol şeklini doğrulamadık.
3. **Yakıt uyarısı:** Yüzde eşiklerinde (örneğin %25, %10) farklı ton, sonra hızlanan bip. Bu, yeşil-kırmızı çubuk yerine geçer.
4. **Coin:** Değere göre iki farklı ton (küçük / büyük). Zincir toplamada ton yükselsin. Coin yönü, sol/sağ panning ile belirtilsin.
5. **Mesafe:** Her 100 m'de bir konuşma; hedefe 200 m kala uyarı.
6. **Seviye bonusu:** "Seviye bonusu" bildirimi, kazanılan puan konuşulsun.
7. **Ölüm:** Çarpma sesi, ardından "kafa çarptı" konuşması, sonra toplam mesafe. Ölümün nedeni (yakıt / çarpma) ayrı seslendirilsin.
8. **Menü:** Sekmeler arasında gezinme için sol/sağ ok, seçimde konuşma: "Araç seçimi, Jeep, Yakıt 40, kilitli değil". Her kart için bilgi tek cümlede.
9. **Zamanlayıcı:** "Ücretsiz sandık 2 saat 33 dakika sonra" gibi konuşma.
10. **Kayıt:** Yakıt, mesafe, coin, araç durumu tek bir ses sürekliliği olarak kanallara ayrılsın (motor sol, uyarılar sağ, coin ortada).

---

## f. Dürüstlük Notu

**Doğrulananlar:**
- YouTube arama sonuçları (4 sorgu) ve video başlıkları, kanal, süre ve görüntülenme sayıları.
- V3 bölüm işaretleri (izleme sayfasından).
- Kare dosyaları: 16 adet okundu. Yalnızca 4 adet `_0.jpg` (480x360) güvenilir okunabilir. `_1/2/3.jpg` (120x90) çok düşük çözünürlük.
- Menü etiketleri (SHOP, STAGE, VEHICLE, TUNE, START, GARAGE, HILL CLIMBER) V4 kare 0'da net okunuyor.

**Doğrulanamayanlar:**
- **Fizik (a):** Hız, havada süre, eğim açısı, süspansiyon, takla, ölüm, yakıt: video ölçümü yok.
- **Zamanlama:** `0.jpg`, `1.jpg`, `2.jpg`, `3.jpg` zaman damgaları varsayım.
- **Bölüm işaretleri vs. kare tutarsızlığı:** V3 bölüm işaretleri 8 s'de 2000 m, 328 s'de 2400 m diyor. Kare 0'da (orta kare, yaklaşık 336 s varsayılıyor) HUD "1302m / 2400m" gösteriyor. Bu, işaretler ile HUD'un çeliştiğini gösteriyor. İşaretler güvenilir değil ya da kareler yanlış zamanda. Bu yüzden zamanlamayla hız hesabı yapılmadı.
- **Düşük çözünürlük:** 120x90 karelerdeki yazılar ("First hill", "FUEL LOW" benzeri, "JEEP", "GARAGE") emin değil. Sadece 480x360 karede okunanlar güvenilir.
- **Sürüm:** V4 başlığı HCR1 diyor, ama menü HCR2 benzeri görünüyor. Oyun sürümü karışık olabilir.
- **Altyazı ve storyboard:** alınamadı.
- **Oyun sonu ekranı, yükseltme ekranı, coin dizilimi, ölüm anı:** görülmedi.

**Yapılan teknik denemeler:** YouTube bot kontrolünü aşmak için ek çaba gösterilmedi. yt-dlp'nin varsayılan, `android` ve `ios` istemcileri denendi ve üçü de başarısız oldu. Çerez veya proxy kullanılmadı. Bundan sonra, bot kontrolü devam ederse, kare tabanlı çalışma yerine metin kaynaklarına (oyun wiki'leri, geliştirici forumları) başvurulmalı. Ancak bu kaynaklar bu araştırma kapsamında erişilebilir değildi (fandom erişimi engelli).

**İndirilen dosyalar:** Hiçbir video dosyası indirilmedi. Kare görüntüleri (16 okunan + 4 yedek hqdefault kopyası) analiz sonrası silindi.
