# Ses Kaynak Araştırması (yeni grup)

Tarih: 2026-10-22. İndirilen dosyalar `sounds-raw/yeni/` klasöründe (git'te yok sayılıyor). Hiçbir commit atılmadı, başka dosya düzenlenmedi.

## Özet

- freesound.org ve pixabay.com bu oturumda kullanılamadı (403). Ses kaynağı olarak archive.org (CC0 ses kütüphaneleri), OpenGameArt (CC0 işaretli sayfalar) ve Kenney (CC0) kullanıldı.
- Dosyalar indirildi ve `ffprobe` ile süreleri doğrulandı. **Hiçbiri kulakla dinlenmedi.** "Döngü" sütunundaki notlar dosya adına ve süreye dayanır; gerçek döngü temizliği test edilmedi.
- Kaynak sayfalarda lisans görüldü: archive.org öğeleri `licenseurl = creativecommons.org/publicdomain/zero/1.0/` (CC0), Kenney paketi "CC0 licensed" ve `License.txt` ile CC0, OpenGameArt ilgili sayfalarda "License(s): CC0".
- Birçok slot için ideal ses bulunamadı (bkz. "Eksikler"). Özellikle Mars, uzun döngülü rüzgar, horoz, formula arabası, çöp kamyonu ve ATV için tam eşleşme yok.
- Task B: "idle car sounds" adlı kanal bulunamadı. Lisansı kontrol edilebilen iki video standart YouTube lisansı gibi görünüyor, üç video kontrol edilemedi.

## Task A: İndirilen dosyalar

Tüm dosyalar `/home/user/bosyol/sounds-raw/yeni/` altında. Süreler `ffprobe` ile ölçüldü. "Kaynak" sütunundaki archive.org bağlantıları öğe sayfasıdır; lisans o sayfadaki `licenseurl` alanından okundu.

| Slot | Dosya | Kaynak sayfa | Yazar | Lisans | Süre | Döngü notu |
|---|---|---|---|---|---|---|
| eng_atv | eng_atv.mp3 | https://archive.org/details/SSE_Library_VEHICLES | USC Cinema / Sunset Editorial Collection (metadata) | CC0 | 109.3 s | Dune buggy kalkış ve sürüş, ATV değil. Döngü yok (olay kaydı). |
| eng_gokart | eng_gokart.mp3 | https://archive.org/details/SSE_Library_VEHICLES | aynı | CC0 | 25.6 s | "go cart or lawnmower idling" (rölanti). Kısmen döngüye uygun olabilir, test edilmedi. |
| eng_chopper | eng_chopper.mp3 | https://archive.org/details/SSE_Library_VEHICLES | aynı | CC0 | 16.2 s | "Chopper motorcycle jumping hill", tepe atlayışı. Döngü yok. |
| eng_chopper_b | eng_chopper_b.ogg | https://archive.org/details/bike-engine-sounds | metadata'da yok | CC0 (`licenseurl` http://creativecommons.org/publicdomain/zero/1.0/) | 13.5 s | 2010 Harley Iron 883. Derin ses; döngü yok (kısa kayıt). |
| eng_scooter | eng_scooter.mp3 | https://archive.org/details/SSE_Library_VEHICLES | aynı | CC0 | 44.0 s | "Vespa scooter". İki zamanlı değil, dört zamanlı Vespa olabilir. Döngü yok. |
| eng_scooter_b | eng_scooter_b.mp3 | https://archive.org/details/SSE_Library_VEHICLES | aynı | CC0 | 25.7 s | "2 Stroke dirt bike", iki zamanlı motor. Döngü yok. |
| eng_hotrod | eng_hotrod.mp3 | https://archive.org/details/GOLD_TAPE_13_13.1_Cars | "GOLD TAPE" serisi (USC/Sunset) | CC0 | 25.2 s | "V8 Engine Idle Out", V8 rölantisi, sonunda gaz kesme. Döngü kısmi. |
| eng_formula | eng_formula.mp3 | https://archive.org/details/SSE_Library_VEHICLES | aynı | CC0 | 14.5 s | "1972 Porsche in fast; idle; stop". Formula değil, yüksek devirli araba. Döngü yok. |
| eng_diesel_truck | eng_diesel_truck.mp3 | https://archive.org/details/GOLD_TAPE_14_Trucks | aynı | CC0 | 18.5 s | "Truck Background", kamyon arka planı. Diesel olduğu doğrulanmadı. |
| eng_firetruck | eng_firetruck.mp3 | https://archive.org/details/GOLD_TAPE_14_Trucks | aynı | CC0 | 28.0 s | "Fire Truck Idle", itfaiye rölantisi. Döngüye en uygun aday gibi görünüyor, test edilmedi. |
| eng_turbine | eng_turbine.mp3 | https://archive.org/details/SSE_Library_MOTORS | aynı | CC0 | 24.8 s | "Engine room; high whine from turbines". Yüksek vınlama. Döngü test edilmedi. |
| amb_mars | amb_mars.mp3 | https://archive.org/details/GOLD_TAPE_55_56_Weather-Wind | aynı | CC0 | 122.1 s | "Chill Wind". Mars değil, düşük rüzgar. Uzun, döngü test edilmedi. |
| amb_volcano | amb_volcano.mp3 | https://archive.org/details/SSE_Library_GEOTHERMAL | aynı | CC0 | 56.9 s | "Scifi lava bubbling". Kabarcık sesi, en iyi aday. Döngü test edilmedi. |
| amb_volcano_b | amb_volcano_b.mp3 | https://archive.org/details/SSE_Library_GEOTHERMAL | aynı | CC0 | 25.7 s | "Volcano and lava flow". Lav akışı. Döngü yok. |
| amb_alpine | amb_alpine.mp3 | https://archive.org/details/GOLD_TAPE_55_56_Weather-Wind | aynı | CC0 | 41.0 s | "Whistling Wind". Dağ rüzgarı olarak uygun, test edilmedi. |
| amb_highway | amb_highway.mp3 | https://archive.org/details/GOLD_TAPE_13_13.1_Cars | aynı | CC0 | 72.7 s | "Highway with Light Traffic". Uzak trafik olabilir. Döngü test edilmedi. |
| amb_farm | amb_farm.mp3 | https://archive.org/details/Red_Library_Animals_Farm | "Red Library" (USC/Sunset) | CC0 | 20.9 s | "Herd of Cattle and Cowboys". Büyükbaş hayvan var. |
| amb_farm_b | amb_farm_b.mp3 | https://archive.org/details/GOLD_TAPE_16_17_18_Ambience | aynı | CC0 | 149.3 s | "Barnyard with Chickens". Horoz yok, tavuk var. Uzun, döngü test edilmedi. |
| amb_canyon | amb_canyon.mp3 | https://archive.org/details/GOLD_TAPE_55_56_Weather-Wind | aynı | CC0 | 54.0 s | "Cannon Wind". Yankı içerip içermediği doğrulanmadı. |
| amb_canyon_b | amb_canyon_b.mp3 | https://archive.org/details/Red_Library_Animals_Birds | aynı | CC0 | 16.1 s | "Canyon Wren Sings". Kuş sesi, rüzgar yok. |
| oneshot_tire_squeal | oneshot_tire_squeal.mp3 | https://archive.org/details/Designers-Choice-Collection-Vehicles | Nicholas A. Judy (metadata) | CC0 | 3.0 s | "Car approach, tires screech to stop". Kısa lastik cızırtısı, one-shot için uygun. |
| oneshot_tire_squeal_b | oneshot_tire_squeal_b.mp3 | https://archive.org/details/GOLD_TAPE_13_13.1_Cars | USC/Sunset (GOLD TAPE) | CC0 | 34.9 s | "Ford in Fast and Screech Stop". Uzun, one-shot için kırpılmalı. |
| oneshot_steel_crash | oneshot_steel_crash.ogg | https://kenney.nl/assets/impact-sounds (dosya: impactMetal_heavy_000) | Kenney | CC0 (Kenney paketi ve `License.txt`) | 0.17 s | Çok kısa metal vuruşu. "Crash" için kısa; daha uzun bir çarpışma gerekirse archive.org'da "METLCrsh" kayıtları bakılmalı. |
| oneshot_steel_crash_b | oneshot_steel_crash_b.ogg | https://kenney.nl/assets/impact-sounds (impactMetal_heavy_003) | Kenney | CC0 | 0.21 s | Aynı. |
| oneshot_crate_smash | oneshot_crate_smash.ogg | https://kenney.nl/assets/impact-sounds (impactWood_heavy_000) | Kenney | CC0 | 0.31 s | Ahşap vuruşu, kırılma değil. Kısa. |
| oneshot_crate_smash_b | oneshot_crate_smash_b.ogg | https://kenney.nl/assets/impact-sounds (impactWood_heavy_003) | Kenney | CC0 | 0.31 s | Aynı. |

Notlar:
- "USC/Sunset" kayıtları archive.org'da "Sound effects from the USC Cinema and/or the Sunset Editorial Collection" açıklamasıyla geliyor. Bireysel yazar alanı boş; kaynak kurum olarak yazıldı.
- Kenney dosyaları `.ogg` formatında (44.1 kHz). Diğerleri `.mp3`, 44.1/48 kHz stereo.

## Task B: "idle car sounds" YouTube kanalı

Sonuç: Bu adla bir kanal bulunamadı.

- YouTube aramasında (`results?search_query=idle+car+sounds`) "idle car sounds" adlı kanal çıkmadı. Kanal filtreli aramada ("Ambient Hour", "ASMR Car Sounds" gibi) benzer adlar çıktı, tam eşleşme yok.
- `@idlecarsounds`, `@IdleCarSounds`, `@idle-car-sounds` handle'ları 404 döndü.
- Bu nedenle kanal açıklamasındaki yeniden kullanım metni alınamadı.

En ilgili 5 video (arama sonucu sırasına göre) ve lisans durumu:

| Video | Kanal | Lisans durumu |
|---|---|---|
| FIFBXz3MJmA "Idling Car Sound Effect" | Free Sound Stock | Kontrol edilemedi (YouTube 302/rate-limit, WebFetch çözümlemedi) |
| 2CJ1jBMwR38 "9 Hours of Relaxing V8 Engine Idle" | HonJazzz | CC metni yok (standart YouTube lisansı gibi görünüyor) |
| KrHTlUCuvoM "ASMR White Noise - 1 HOUR V8 CAR ENGINE IDLING" | RELAXING ASMR 1122 | Kontrol edilemedi (rate-limit) |
| TO0zGdpjXg8 "CAR ENGINE DIESEL IDLE Sound ... 10H" | YourPeacefulSound | CC metni yok (standart YouTube lisansı gibi görünüyor) |
| o-UFWFpoSHY "6 Reasons Your Engine Revs Up and Down at Idle" | AutocornerdPros | Kontrol edilemedi (rate-limit) |

Lisans kontrolü nasıl yapıldı: Watch sayfalarının HTML'inde "Creative Commons" ve "licenseInfos" aranıldı. 2CJ1jBMwR38 ve TO0zGdpjXg8 sayfaları yüklendi ancak CC metni bulunmadı. Bu, sunucu HTML'inde bulunmama anlamına gelir; JS ile yüklenen lisans alanı kaçmış olabilir. FIFBXz3MJmA, KrHTlUCuvoM ve o-UFWFpoSHY için Google'ın rate-limit sayfasına yönlendirildi, WebFetch da youtube.com'u çözemedi.

Sonuç: Kanal adı doğrulanamadı ve lisansları Creative Commons olduğu gösterilebilen video yok. Bu seslerin kullanılması için video/kanal sahibinden izin gerekir. Doğrulamak için bu üç videonun sayfasını tarayıcıda açıp "Ses hakkında / Lisans" bölümüne bakmak gerekir.

## Eksikler

| Slot | Durum |
|---|---|
| eng_atv | Gerçek ATV/quad yok, dune buggy var |
| eng_chopper | Tepe atlayışı kaydı; sürekli rölanti yok |
| eng_scooter | Vespa dört zamanlı olabilir; küçük iki zamanlı scooter tam eşleşmedi |
| eng_hotrod | V8 rölantisi var, döngü test edilmedi |
| eng_formula | Formula değil; yüksek devirli bir araba |
| eng_diesel_truck | Çöp kamyonu sesi yok; "Truck Background" genel bir kamyon |
| eng_firetruck | İtfaiye rölantisi var, döngü test edilmedi |
| eng_gokart | Go-kart ve çim biçme makinesi karışık |
| amb_mars | Mars sesi yok; düşük rüzgar var ama 122 s, döngü test edilmedi |
| amb_alpine | Dağ rüzgarı için "Whistling Wind"; test edilmedi |
| amb_highway | Uzak trafik olabilir; test edilmedi |
| amb_farm | Horoz bulunamadı; tavuk ve sığır var |
| amb_canyon | Yankı doğrulanamadı; "Cannon Wind" test edilmedi |
| oneshot_steel_crash | Kenney metal vuruşları 0.2 s; daha uzun çelik çarpışma yok |
| oneshot_crate_smash | Kırılma yok, sadece ahşap vuruşu (0.3 s) |
| Task B kanal | "idle car sounds" kanalı bulunamadı |
| Task B lisans | 3 / 5 videonun lisansı kontrol edilemedi |

Lisansı kontrol edilmemiş dosya yok; tüm dosyalar CC0 olarak işaretli kaynaklardan. Yine de her dosyanın içeriği kulakla doğrulanmalı.
