# Yokuş Ustası 2.0: içerik listesi (S-C görevi için)

Şimdi 12 araç ve 10 harita var. Hedef: 20 araç ve 16 harita. Aşağıdaki listeler kesindir. Değerler (kütle, tork, süspansiyon vb.) mevcut araçlarla karşılaştırılarak ayarlanır ve `PlayTest` ile doğrulanır. Ayrıntılı araştırma: `docs/ARASTIRMA_ICERIK.md`.

## Araçlar (fiyat sırasıyla, 20 adet; yenileri kalın)

| # | Araç | id | Fiyat | Karakter | Motor sesi |
|---|---|---|---|---|---|
| 1 | Cip | jeep | 0 | dengeli | eng_hill (S-A) |
| 2 | **Scooter** | scooter | 3.000 | minik tekerlek, çok hafif, zayıf, havada çok çevik, yokuşta zorlanır | eng_moto, perde yüksek |
| 3 | Motosiklet | moto | 8.000 | hafif, güçlü | eng_moto |
| 4 | **Go-kart** | gokart | 12.000 | alçak, hafif, yer çekimi yüksek hissi, düz yolda hızlı, sıçramada savrulur | eng_buggy, perde yüksek |
| 5 | Pikap | pickup | 15.000 | sağlam | e1 seti |
| 6 | **Dörtteker (ATV)** | quad | 20.000 | hafif, 4WD, düzensiz zeminde güvenli, havada çevik | eng_moto, perde alçak |
| 7 | Traktör | tractor | 25.000 | yavaş, çok tutunur | eng_tractor |
| 8 | Kum arabası | buggy | 35.000 | hafif, yumuşak süspansiyon | eng_buggy |
| 9 | **Chopper** | chopper | 42.000 | uzun dingil, ağır motosiklet, burnu kolay kalkar, düz yolda çok dengeli | eng_moto, perde çok alçak |
| 10 | Ralli arabası | rally | 50.000 | hızlı, alçak, turbo | eng_rally2 (S-A) |
| 11 | **Hot rod** | hotrod | 60.000 | büyük arka teker, güçlü kalkış, burnu ağır, turbo yok, V8 patlama sesi | e2 seti, perde alçak |
| 12 | Canavar kamyon | monster | 70.000 | dev teker, yumuşak | eng_monster2 (S-A) |
| 13 | **Çöp kamyonu** | garbage | 80.000 | ağır, yavaş, çok tutunur, havada ağır | eng_bus, perde alçak |
| 14 | Otobüs | bus | 90.000 | uzun dingil, ağır | eng_bus |
| 15 | Kar motosikleti | snowmobile | 110.000 | kar ve buzda çok iyi | eng_snowmobile |
| 16 | **İtfaiye aracı** | firetruck | 125.000 | ağır, güçlü, 4WD, tırmanışta ısrarcı | eng_monster2, perde alçak |
| 17 | Ay aracı | rover | 140.000 | elektrikli, hafif | sentez |
| 18 | **Formül araba** | formula | 160.000 | çok hafif, alçak, yüksek devir, turbo, zor kontrol | e2_high, turbo |
| 19 | Tank | tank | 180.000 | çok yavaş, çok tutunur | eng_tractor |
| 20 | Süper araba | super | 250.000 | çok hızlı, süper şarj | e2 seti, süper şarj |

Kurallar:
- Her yeni araç için `VehicleDef` alanlarının tamamı doldurulur. Kafa noktası, teker konumu ve süspansiyon, aracın gövdesine uyar.
- Her yeni araç Kır yolunda (yükseltmesiz) en az 800 metre gider (BlindDriver iyi sürücü).
- Hiçbir aracın zayıf noktası "hemen devrilir" olmamalı. Denge yardımı açıkken acemi sürücü ilk 500 metrede kaza yapmamalı.
- Turbo bayrağı: ralli, hot rod değil, formül. Süper şarj: süper araba.
- Araçlar arası hız sırası mantıklı kalmalı (fiyat arttıkça genelde daha hızlı ya da daha özel yetenekli).
- `description` alanı Türkçe, tek cümle, aracı ekran okuyucuyla dinleyene karakterini anlatır.

## Haritalar (fiyat sırasıyla, 16 adet; yenileri kalın)

| # | Harita | id | Fiyat | Yüzey | Yer çekimi | Karakter | Ortam sesi |
|---|---|---|---|---|---|---|---|
| 1 | Kır yolu | country | 0 | toprak | 9,81 | yumuşak tepeler | amb_country |
| 2 | Çöl | desert | 5.000 | kum | 9,81 | kum tepeleri | amb_desert |
| 3 | **Tuz gölü** | saltflat | 8.000 | tuz (SALT, tutunma 0,35) | 9,81 | çok düz, uzun düzlükler, hız ve kayma, nadir rampa | rüzgâr (amb_desert, farklı perde) |
| 4 | Kutup | arctic | 12.000 | kar | 9,81 | kar tepeleri | amb_arctic |
| 5 | Ay | moon | 20.000 | ay tozu | 1,62 | uzun havada kalma | amb_moon |
| 6 | **Donmuş göl** | frozenlake | 25.000 | buz (0,25) | 9,81 | düz, kenarlarda kar tepeleri, küçük rampalar; arada buz çatırtısı (crack sesi) | amb_arctic + crack |
| 7 | Orman | forest | 30.000 | toprak | 9,81 | kıvrımlı | amb_forest |
| 8 | **Gece çatıları** | rooftops | 35.000 | asfalt | 9,81 | binalar arası boşluklar (çukur sayısı çok), dik rampalar | bed_night + bed_city (kaynak/bosyol) |
| 9 | Dağ | mountain | 40.000 | kaya | 9,81 | dik | amb_mountain |
| 10 | Plaj | beach | 50.000 | kum | 9,81 | dalgalı | amb_beach |
| 11 | Mağara | cave | 60.000 | kaya | 9,81 | dik, çukurlu | amb_cave |
| 12 | **Mars** | mars | 65.000 | kırmızı toz (MARS, 0,65) | 3,71 | uzun sıçramalar, orta eğim | alçak rüzgâr + amb_moon |
| 13 | Bataklık | swamp | 70.000 | çamur | 9,81 | çamurlu | amb_swamp |
| 14 | **Volkan** | volcano | 80.000 | bazalt (ROCK) | 9,81 | çok dik, büyük boşluklar, sert zorluk eğrisi; lav çıtırtısı ve gürleme | sentez: gürleme + çıtırtı |
| 15 | Şantiye | site | 90.000 | asfalt | 9,81 | rampalar | bed_city |
| 16 | **Deniz tabanı** | deepsea | 100.000 | kum | 4,9 + su direnci (`drag`) | yumuşak, geniş; ağır ve yavaş hareket, uzun süzülme | kısık dalga + kabarcık (sentez) |

Kurallar:
- Her haritanın `MapDef` değerleri (`amp`, `growth`, `rampEvery`, `fuelEvery`, `maxSlope`, tohum) mevcut ayarlarla uyumludur. Yeni alan gerekiyorsa (`drag`) varsayılan değerli eklenir ve eski haritalar etkilenmez.
- Her harita, önerilen aracıyla ve yükseltme 3 ile en az 1000 metre gidilebilir. Mars ve Deniz tabanında uzun havada kalmaya göre `maxSlope` ve rampa sıklığı ayarlanır.
- Yeni yüzeyler: `SALT` (tutunma 0,35, yuvarlanma sesi roll_sand yüksek perde), `MARS` (0,65, roll_dirt), `BASALT` yerine `ROCK`. `Surface` enumuna eklenir, `roll` anahtarı mevcut seslerden seçilir.
- Ses: yeni ortam sesleri `tools/prep_sounds.py` ile `kaynak/` ve `sounds-raw/yeni/` altından üretilir. Eksik olanlar sentezlenir (gürleme, çıtırtı, kabarcık, alçak rüzgâr). Hiçbir ses dosyası eksik olduğunda oyun çökmemeli (zaten sessiz kalır).
- Haritaların bir `description` alanı (Türkçe, tek cümle) bulunur.

## Ekonomi

**Güncelleme (kullanıcı isteği): her altın tam 1 altın değerindedir.** Altın değer katmanları (5, 25, 100, 500) yoktur. Bu yüzden bir oyunda kazanılan altın yüzler mertebesindedir ve yukarıdaki fiyat sütunları (3.000 ... 250.000 vb.) göreli sıralamayı gösterir. S-C görevinde bütün fiyatlar (araç, harita, geliştirme) altın gelirine göre yeniden ölçeklenir, hedefler aşağıdaki gibi kalır. Haritanın fiyatı arttıkça altın yoğunluğu artar.

Toplam 20 araç ve 16 harita satın alınınca ekonomi uzar. PlayTest ekonomi kapısı: bütün mağaza yaklaşık 25–40 saatlik iyi oyunda tamamlanır. İlk araç (scooter) 1–2 oyunda, ilk harita (Çöl) 2–3 oyunda alınır. Altın değerleri ve yoğunluğu gerekirse haritanın fiyatına göre oranlanır (`Terrain` içinde mevcut mekanizma).

## Testler

`TerrainTest`: 20 araç, 16 harita, kimlikler benzersiz, fiyatlar artan sırada ve bu tabloyla aynı. `PhysicsTest`: tüm araçlar için tırmanma testi, devrilme ve titreme yok. `PlayTest`: yeni araç ve haritalar matrise katılır (süre 2,5 dakikayı çok aşmasın, gerekirse örnekleme yapılır), kabul kapıları güncellenir.
