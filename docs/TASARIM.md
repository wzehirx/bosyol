# Yokuş Ustası: tasarım belgesi

Tarih: 20 Ekim 2026. Kullanıcı uyurken kararları Claude verdi; kullanıcı otomatik ilerlemeye önceden izin verdi.

## Amaç
Görme engelliler için Hill Climb Racing'e çok yakın, sesle oynanan bir Android oyunu. Bol araç, bol harita.
Hill Climb Racing'in adı, görselleri ve sesleri kullanılmaz (telif). Sesler yalnızca CC0 (Freesound) ve bosyol deposundaki mevcut sesler.
Başarı ölçütü: TalkBack/Jieshuo ile menüler tam okunur; ekrana bakmadan tepe tırmanılabilir, yakıt ve altın toplanabilir, takla/kaza sesle anlaşılır.

## Oyun mekaniği (HCR'ye yakın)
- Yandan görünüm 2D dünya. Araç sağa (+x) gider. Yer çekimi haritaya göre (Ay 1.62 m/s², diğerleri 9.81).
- Gaz: çekiş tekerine tork. Havadayken gaz aracı arkaya (burun yukarı), fren öne (burun aşağı) döndürür.
- Fren: yerde fren/geri; havada öne döndürme.
- Yakıt azalır (temel + gazla orantılı). Yakıt bidonu yakıtı doldurur. Yakıt bitip araç ~3 sn durursa oyun biter.
- Sürücünün kafası (gövde üst noktası) yere değerse kaza: oyun biter.
- Altınlar yol boyunca gruplar halinde (5, 25, 100, 500 değerli). Altınla araç, harita ve geliştirme alınır.
- Havada tam dönüş (takla) bonusu, uzun havada kalma bonusu.
- Her haritada en iyi mesafe kaydedilir.

## Fizik modeli (core/Physics.kt)
- Gövde: katı cisim (x, y, açı, vx, vy, açısal hız), kütle m, eylemsizlik I.
- İki teker: ayrı daire cisimler (x, y, vx, vy, açısal hız ω), yarıçap r, kütle mw.
- Süspansiyon: her teker gövdeye bağlantı noktasından yay-sönüm (k, c) ile bağlı; eksen boyunca sınırlı hareket, eksene dik yönde çok sert yay.
- Zemin teması: teker dairesi zemin yüksekliği fonksiyonuna gömülürse normal boyunca dışarı itilir (sekme yok); sürtünme kuvveti = kayma (ω·r − teğetsel hız) ile orantılı, μ·N ile sınırlı. μ haritanın yüzeyine göre (asfalt 1.0, toprak 0.9, kum 0.6, kar 0.45, buz 0.25, çamur 0.5, ay tozu 0.7).
- Motor: tork eğrisi (devre göre), dişli yok (HCR gibi tek oran). Devir = teker hızından; kalkışta debriyaj kayması.
- Motor tepkisi: tekere verilen torkun tersi gövdeye; ayrıca havada doğrudan döndürme torku (hava kontrolü).
- Sabit adım: 240 Hz alt adım, NaN koruması, hız sınırları.
- Kafa noktası gövdenin üst ortası; zemine değerse kaza.

## Zemin (core/Terrain.kt)
- Harita tohumuyla üretilen yükseklik: katmanlı gürültü (birkaç sinüs/değer gürültüsü), mesafe arttıkça genlik ve diklik artar.
- Özel parçalar: rampa (sıçrama), tümsek dizisi, dik duvar, çukur, düzlük.
- 1 m aralıkla önbelleğe alınan diziler; eğim ve normal hesaplanabilir.
- Altın grupları (110-170 m'de bir, 5-9 altın) ve yakıt bidonları zemine göre yerleştirilir. Bidon aralığı ilk 800 m'de haritanın `fuelEvery` değeri (400-600 m), sonra kare kökle uzar (fuelEvery × (1 + 1.7·√km)); HCR gibi ileride bidonlar seyrekleşir, iyi koşular yakıtla doğal biter, yakıt deposu geliştirmesi daha uzağa götürür.
- Mesafe arttıkça büyük tepeler uzar (1500 m'den sonra dalga boyu km başına %35 artar, en çok 3 kat): yokuşlar hem uzar hem yükselir.
- Altın değeri mesafeyle artar: ilk 1 km'de çoğu 5, sonra 25 ve 100, 4.5 km'den sonra 500 de çıkar. Pahalı haritalar daha çok öder (değer tablosu harita fiyatının 0.1 katı metre ileriden okunur).

## Araçlar (core/Defs.kt) — fiyatlar altın
| Araç | Fiyat | Karakter | Motor sesi |
|---|---|---|---|
| Cip | 0 | dengeli başlangıç | eng_jeep, yoksa e0 seti |
| Motosiklet | 8.000 | hafif, güçlü, kolay takla | eng_moto |
| Pikap | 15.000 | sağlam, orta | e1 seti |
| Traktör | 25.000 | yavaş, çok tutunur, dengeli | eng_tractor |
| Kum arabası | 35.000 | hafif, yumuşak süspansiyon | eng_buggy |
| Ralli arabası | 50.000 | hızlı, alçak | eng_rally / e2 |
| Canavar kamyon | 70.000 | dev teker, yumuşak | eng_monster |
| Otobüs | 90.000 | uzun dingil, ağır | eng_bus |
| Kar motosikleti | 110.000 | kar ve buzda çok iyi | eng_snowmobile |
| Ay aracı | 140.000 | elektrikli, hafif | sentez |
| Tank | 180.000 | çok yavaş, çok tutunur | eng_tractor alçak perde |
| Süper araba | 250.000 | çok hızlı, zor kontrol | e2 seti |

## Haritalar
| Harita | Fiyat | Yüzey | Yer çekimi | Ortam sesi |
|---|---|---|---|---|
| Kır yolu | 0 | toprak | 9.81 | amb_country / bed_forest |
| Çöl | 5.000 | kum | 9.81 | amb_desert |
| Kutup | 12.000 | buz/kar | 9.81 | amb_arctic |
| Ay | 20.000 | ay tozu | 1.62 | amb_moon |
| Orman | 30.000 | toprak | 9.81 | amb_forest |
| Dağ | 40.000 | kaya, dik | 9.81 | bed_mountain |
| Plaj | 50.000 | kum | 9.81 | amb_beach |
| Mağara | 60.000 | kaya | 9.81 | amb_cave |
| Bataklık | 70.000 | çamur | 9.81 | amb_swamp |
| Şantiye | 90.000 | asfalt, rampalar | 9.81 | bed_city |

## Geliştirmeler (araç başına, 1-10. seviye)
Motor (+8% tork), Süspansiyon (yay/sönüm ayarı), Lastik (+6% tutunma), Yakıt deposu (+10% kapasite). Fiyat = taban × 1.6^seviye; taban araç fiyatına göre.

## Ses tasarımı (asıl erişilebilirlik)
- Yön kuralı: önündeki şeyler SAĞ kulağa, arkandakiler sol kulağa daha yakın duyulur; yukarıdaki şey daha ince perdede. Mesafe ses düzeyini düşürür.
- Motor: devir ve gaza göre (bosyol'daki SampleEngine mantığı; tek döngülü yeni motorlar perdeyle).
- Eğim tonu (ayarla kapatılır, varsayılan açık): sürekli yumuşak ton; burun yukarı kalktıkça ince, aşağı indikçe kalın. Açı 70°'yi geçerse hızlanan bip (takla tehlikesi).
- Yol radarı (ayarla kapatılır, varsayılan açık): her 0.4 sn kısa tık; perdesi 10-15 m ilerideki eğimi anlatır. Büyük parça yaklaşınca sesli duyuru: "Önde dik yokuş", "Önde rampa", "Önde çukur".
- Tekerler: arka teker sola, ön teker sağa yakın; yere iniş tok vuruşu, süspansiyon gıcırtısı.
- Yüzey sesi: haritaya göre toprak/kum/kar/çamur yuvarlanma döngüsü, teker hızına göre.
- Havada: rüzgâr uğultusu yükselir; inişte vuruş.
- Altın grupları önünde hafif parıltı sesiyle duyulur; toplarken "ding", ardışık altınlarda perde yükselir.
- Yakıt bidonu önünde ayrı bir işaret sesiyle duyulur; yakıt %25 ve %10'da uyarı sesi ve duyuru.
- Mesafe duyurusu (varsayılan açık, Ayarlar'dan kapatılır): her 250 m'de kısa "N metre".
- "Durum" düğmesi: mesafe, yakıt, altın, aracın açısı.
- Kaza ve oyun sonu sesle ve duyuruyla bildirilir; sonuç ekranı okunur.

## Arayüz (bosyol'daki Kotlin yerel arayüz kalıbı)
Ana menü (Başla, Araç, Harita, Garaj, Ayarlar, Nasıl oynanır, Çıkış), Araçlar ve Haritalar (fiyat/sahiplik, satın al veya seç), Garaj (4 geliştirme), Ayarlar (anahtarlar ve ses düzeyleri), Oyun (fren sol kenar, gaz sağ kenar, ortada Durum ve Duraklat), Duraklat (Devam, Yeniden başla, Ana menü), Oyun sonu (sonuç, Tekrar, Garaj, Ana menü).
Yatay ekran, etiketli Android anahtarları, başlıklar, ekran değişince odak başlığa. Ses tuşlarıyla gaz/fren seçeneği. Ekran okuyucu yoksa TTS ile duyuru.

## Mimari ve dosyalar (paket com.yokus.app)
- `android/src/com/yokus/app/core/`: Dsp.kt (bosyol'dan kopya karıştırıcı), Defs.kt, Terrain.kt, Physics.kt, Game.kt (oturum: fizik, yakıt, altın, kaza, puan, ses ipuçları), JVM'de test edilebilir; Android içermez.
- `android/src/com/yokus/app/`: MainActivity.kt (ekranlar), GameView.kt (Canvas 2D çizim), SoundBank.kt, AudioOut.kt, CrashLog.kt, Store.kt (SharedPreferences: altın, sahiplik, seviyeler, en iyi mesafeler).
- `android/build.py`: bosyol'daki resmî SDK derleyicisinin uyarlaması (Kotlin + d8 + apksigner). Anahtar `android/yokus.jks` (git dışında).
- `sounds/`: hazırlanmış sesler. `sounds-raw/`: indirilen ham CC0 sesler ve CREDITS.csv.
- `android/test/`: JVM fizik ve oyun testleri.

## Test
- JVM: düz zeminde araç sabit durur (titreme, batma yok); tam gazla kır yolunda yokuş çıkılır; havada gaz burnu kaldırır; ters dönünce kaza algılanır; yakıt bitince oyun biter; altınlar sayılır; Ay'da havada kalma süresi uzun; 10 dk rastgele girişte NaN yok.
- Robolectric: tüm ekranlar dolaşılır, satın alma, oyun başlatma, duraklatma, oyun sonu.
- d8 sonrası dexdump: invoke-custom yok. lint NewApi temiz.
