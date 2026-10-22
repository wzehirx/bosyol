# Araştırma: Hill Climb Racing referansı ve yeni araç / harita önerileri

Bu belge, sesle oynanan (audio-first) Android oyunumuz için yapılan araştırmanın sonucudur. Oyunumuz Hill Climb Racing'den esinlenir, ama birebir kopya değildir. Amaç, mevcut 12 araç ve 10 haritanın yanına 8 yeni araç ve 6 yeni harita önermektir.

## 0. Kaynak durumu ve güven seviyeleri

Bu araştırma kısmi kapsamlıdır. Aşağıdaki etiketler her bölümde kullanılmıştır:

- **[K] Kaynak:** Arama sonuçlarında görünen, bir web sayfası veya sürüm notundan gelen bilgi. Sayfa dışarıdan açılamadığı için yalnızca arama özeti (snippet) olarak görüldü.
- **[T] Tek kaynak / çelişkili:** Yalnızca bir kaynakta geçen, ya da kaynaklar arasında çelişen bilgi. Kullanmadan önce doğrulanmalıdır.
- **[B] Genel bilgi:** Kaynaksız, bilinen oyun tasarımı pratiklerinden çıkarılmış bilgi. Doğrulanmadı.
- **[Ö] Öneri:** Bu proje için tasarlanmış, kaynak içermeyen yeni içerik.

Erişim durumu:

- Gamepedia (hillclimbracing2.gamepedia.com), Google Play, App Store, de.wikipedia.org, gameskinny.com, macworld.com ve levelwinner.com sayfaları bu ortamdan DNS hatası (ENOTFOUND) verdi. Bu sayfalar açılamadı.
- Fandom wiki (hillclimbracing2.fandom.com) yalnızca arama sonuçlarında göründü. Sayfa doğrudan açılmadı.
- en.wikipedia.org bu araştırmada denenmedi.
- Reddit sonucu hiçbir aramada gelmedi.
- Elde kalan kaynaklar ağırlıklı olarak APK değişiklik notları (apkmirror.com), içerik çiftliği (content farm) sayfaları ve eski bir Gamepedia sürümünün arama özetleridir. Bu nedenle fiyat ve araç bilgileri güncel olmayabilir.

## 1. Araç kadrosu (Hill Climb Racing 1 ve 2)

### 1.1 Hill Climb Racing 2 (HC2)

**Bilinen araç adları [K]:** Hill Climber (başlangıç aracı, ücretsiz), Scooter, Bus (Otobüs), Hill Climber Mk. 2, Motocross, Dune Buggy, Super Jeep, Monster Truck, Sports Car, Muscle Car, Tank, Formula 1 car, The Bullet, Rally Car.

**Kilidi açma [K/T]:** Araçlar, Cups (lig) sisteminde rank atlayarak ve coin harcayarak açılır. Kaynaklarda geçen örnekler:

| Araç | Kilit (kaynağa göre) | Not |
|---|---|---|
| Hill Climber | Başta açık | [K] |
| Scooter | Bronze II, 5.000 coin | [T] eski Gamepedia sürümü |
| Bus | Bronze III, 7.000 coin | [T] eski sürüm, çelişkili bir arama özetinde farklı |
| Hill Climber Mk. 2 | Silver I, 9.000 coin | [T] |
| Motocross | Silver II, 20.000 coin | [T] çelişkili (bir özette 9.000 olarak da geçiyor) |
| Dune Buggy | Silver III, 30.000 coin | [T] |
| Super Jeep | Silver I | [K] (GameSkinny, çok eski) |
| Monster Truck | Gold II | [K] (Gamezebo) |
| Tank, Formula 1 | Platinum / Diamond | [K] (GameSkinny) |

**Yükseltme (upgrade) türleri [K/T]:** Kaynaklar tüm araçların şu parçaları olduğunu söylüyor: engine (motor), tires / grip (lastik), suspension (süspansiyon), 4WD. Bazı kaynaklar ek olarak turbo, supercharger, gearbox, rollcage, air control, magnet gibi parçalardan bahsediyor. Bunların hepsinin her araçta olup olmadığı belirsiz.

**Fiyat eğrisi (fan wiki, eski sürüm) [T]:**

- Hill Climber Mk. 2: parça başına seviye 2 için 1.200 coin, seviye 20 (maks) için 41.000 coin. Eğri her parçada aynı görünüyor (engine, grip, suspension, rollcage).
- Muscle Car: parça başına seviye 2 için 2.000 coin, seviye 20 için 230.000 coin. Eğri daha dik.
- 4WD ve turbo için fiyat ve seviye bilgisi bulunamadı.

**Araç davranışı (handling) [K/T]:** Kaynaklar kişisel görüşlerden ibarettir. Tek tutarlı bilgi: çok hafif ve yükseltilmemiş bir jeep, zıplamalardan sonra kontrol edilmesi zordur. Süspansiyon yükseltmesi zıplama kabiliyetini artırır. Motor ve 4WD yükseltmeleri hızlanmayı artırır. Hangi araçta 4WD olduğu ve ağırlık sınıfları kaynaklarda bulunamadı.

**Hava kontrolü (air control) [K]:** Gaz (accelerator) havadayken ön kısmı kaldırır ve geri takla (backflip) için kullanılır. Fren havadayken arka kısmı kaldırır ve ön takla (frontflip) için kullanılır. Air Control parçası havadayken daha fazla takla yapılmasını sağlar.

### 1.2 Hill Climb Racing 1 (HC1)

**Bilinen araç adları [K]** (APK sürüm notları ve çeşitli listelerden): jeep, motocross, monster truck, tractor, hippie van, onewheeler, quad bike, tourist bus, race car, police car, ambulance, fire truck, snow mobile, super offroad, Dune Buggy, Tank, Big Finger, Rally Car, Trophy Truck (sürüm 1.33.0 ile eklendi), The Rocket (sürüm 1.41.0 ile eklendi).

**Sayı [K]:** Geliştirici, sürüm 1.43.0 için 29'dan fazla araç olduğunu belirtiyor. Toplam sayı sürüme göre değişiyor.

**Özel durumlar [T]:** Bir forum yazısı, Dune Buggy, Tank, Big Finger ve Rally Car'ın belirli haritalarda güçlü olduğunu söylüyor. Bu tamamen oyuncu gözlemi.

**Kanıt sınırı:** HC1 için araç başına özellik (ağırlık, çekiş, hava kontrolü) bu araştırmada bulunamadı.

### 1.3 Karşılaştırmanın yorumu

- Her iki oyun da araç adlarını genel terimlerle (quad bike, fire truck, monster truck, tractor, tank, dune buggy) kullanıyor. Bu nedenle bu genel terimler genre (tür) düzeyinde serbesttir. Ancak adları, illüstrasyonları ve karakter tasarımlarını kopyalamamalıyız.
- Ürünün kendine özgü yönü, sese dayalı araç kimliği olmalıdır. Bu, HC'nin görsel farklarından değil, motor ve ses karakterinden ayrılmalıdır.

## 2. Harita (stage) kadrosu

### 2.1 Hill Climb Racing 1

**Bilinen haritalar [T]:** Macworld ipuçları listesinde ve bir forum yazısında geçenler:

- Countryside (Kır), Desert (Çöl), Arctic (Kutup), Xmas (Yılbaşı) [K, forum]
- Highway, Cave, Moon, Mars, Alien Planet, Arctic Cave, Forest, Mountain, Mudpool, Volcano, Beach, Roller Coaster, Night, Rooftops [T, Macworld listesi]
- Haunted, Northpole, Arena (sürüm 1.33.0 ile eklendi) [K, forum/sürüm notu]

**Doğrulanmayanlar:** Halloween, Jungle, Mine haritaları aramalarda hiç geçmedi. Varlıkları doğrulanamadı.

**Hız ve uzunluk:** Bir sürüm notu sonsuz (endless) haritaların sayısının 28'in üzerinde olduğunu söylüyor. Her haritanın uzunluğu bulunamadı.

**Yakıt:** Bir kaynak Xmas haritasında yakıtın sınırlayıcı faktör olduğunu söylüyor [T].

### 2.2 Hill Climb Racing 2

**Haritalar:** Kaynaklar haritaların Adventures ve Cups yapısı içinde açıldığını söylüyor, ama tam liste bulunamadı. Adventures sayfasının arama özeti var, içeriği açılamadı. Mağara (Cave), Kutup, Çöl gibi temalar bu sürümde de bulunuyor [B].

**Yeni cup adları [K]:** The Jungle Brake, A Bit Plain, Racers In The Mist (yama notu).

### 2.3 Zemin, yerçekimi ve tehlike bilgisi

Kaynaklarda harita başına zemin tipi, yerçekimi değeri ve tehlike bilgisi yoktur. Bu bilgiler [B] genel bilgi olarak aşağıda verildi:

- Yerçekimi: HC oyunlarında harita başına değişmez; Ay ve benzeri temalarda değiştirilir [B].
- Tehlikeler: Kaya, ağaç gövdesi, ateş, duvar, uçurum, sis ve kar gibi engeller temel çeşitliliği oluşturur [B].

## 3. Temel mekanikler

### 3.1 Coin (madeni para)

- **Değer [T]:** Macworld kaynağına göre havada uzun kalmak her zıplamada yaklaşık 5.000–10.000 coin verir. Arka takla (360 derece) başına 1.000 coin ek ödül verilir. Bu tek kaynak. Sürüme ve mesafeye göre değişebilir, çelişkili bir kanıt yok ama doğrulanmadı.
- **Yerleşim (spawn) deseni [B]:** Kaynaklar belirli bir yerleşim desenini doğrulamadı. Genel uygulama: coinler yol üzerinde yay, dizi veya küme (cluster) halinde yerleştirilir; havada ulaşılabilecek yüksekliklere konur, zıplama noktalarına yakın yoğunlaştırılır.
- **Zincir (chain) [B]:** Art arda toplanan coinler ödülü veya çarpanı artırabilir. Kaynaklarda doğrulanmadı.
- **Toplama hissi (pickup feel) [B]:** Kaynak bulunamadı. Genel tasarım pratiği olarak şunlar öneriliyor: her coin için kısa, tiz ve tatmin edici bir "tınlama"; zincirde pitch (perde) yükselmesi; toplamada hafif bir titreşim (haptic); ve toplanan coinin HUD sayacına doğru görsel uçuşu. Bu işitsel ipucu sesli oyun için kritik, çünkü görme olmadan konum bildiren tek kanal ses olmalıdır. [B/Ö]

### 3.2 Yakıt (fuel)

- **Kaynak [K]:** Yakıt, yol üzerindeki gaz kutusu (gas canister) veya pil (battery) toplanarak doldurulur. Yakıt biterse koşu biter.
- **Yakıt sınırı [K]:** Bir kaynak, gaz kutusu ve pilin yakıtın yenilenme kaynağı olduğunu söylüyor. Yakıt sınırlayıcı olabilir.
- **HC2 magnet parçası [K]:** Magnet yükseltmesi, yakıt tanklarını yoldan geçmeden toplamayı sağlar.

### 3.3 Takla (flip) ve hava süresi (air time) bonusu

- **Ödüller [K]:** Kaynaklar, havada uzun kalmanın ve takla atmanın coin kazandırdığını söylüyor. Takla için sabit bir değer bulunamadı.
- **Takla değerleri araca bağlı olabilir [T]:** Bir kullanıcı yorumu, bazı yeni araçlarda ön/arka takla coin değerinin düşürüldüğünü söylüyor. Tek kaynak.
- **Air control [K]:** Takla yapmayı ve dengeyi sağlayan parçadır.

### 3.4 Ölüm kuralları ve bitiş

- **Koşu sonu [B]:** Oyun genellikle şu durumlarda biter: araç takla atıp üstüne düştüğünde (sürücü başı yere değdiğinde), yakıt bittiğinde veya haritanın dışına çıkıldığında.
- **Level bonusu [K]:** Her haritada işaret noktaları (marker points) vardır. İlk kez ulaşıldığında "level bonus" verilir.
- **Sürüş modeli [B]:** Mevcut koşu sonu kuralları oyuncu eğitiminde kritik olabilir; sesle oyunda "düştün" bilgisi net verilmelidir.

### 3.5 Nitro / trick mekanikleri

Kaynaklarda nitro (hız artırıcı) mekaniği bulunamadı. Trick (hile/numara) sistemi, havadaki takla ve dönüşlerle ilişkilidir [K]. Nitro, bu araştırmada doğrulanmadı.

## 4. Menü ve akış tasarımı

Kaynaklar bu konuda sınırlıdır. Aşağıdaki akış, kaynaklardan (lig, Cups, Adventures, garaj) ve genel HC pratiklerinden [B] çıkarıldı:

1. **Açılış ekranı [B]:** Oyun başlar, oyuncu oyun modunu seçer.
2. **Ana menü [B]:** Oyna (Play), Garaj (Garage), Mağaza (Shop), Ayarlar (Settings).
3. **Mod seçimi [K]:** Cups (lig sistemi, Bronze, Silver, Gold, Platinum, Diamond) ve Adventures (macera) modları.
4. **Araç seçimi [B]:** Açık araçlar listelenir, kilitli olanlar kilidi açma koşuluyla gösterilir.
5. **Harita seçimi [B/K]:** Açık haritalar, Adventures veya Cups sırasına göre açılır.
6. **Koşu [B]:** Oyuncu koşar, coin ve yakıt toplar, takla atar.
7. **Sonuç ekranı [B]:** Mesafe, coin ve bonuslar gösterilir. Oyuncu garaja dönebilir.
8. **Garaj ve yükseltme [K/B]:** Coin ile parçalar (engine, tires, suspension, 4WD) yükseltilir. Parça seviyeleri ve maksimum değerleri vardır.

Sesli oyun için not [Ö]: Her menü adımında ses rehberliği (ne seçildiğinin sesli okunması), kilitli öğeler için açıklayıcı sesli ipucu ve odak (focus) sırasının tutarlı olması gerekir.

## 5. Önerilen yeni araçlar (8 adet) [Ö]

Kurallar:

- Adlar genel terimlerdir (Go-kart, Quad bike, Chopper, Hot rod, Garbage truck, Formula car, Forklift, Fire truck). Oyunumuzdaki mevcut adlar (Cip, Motosiklet, Pikap, Traktör, Kum arabası, Ralli arabası, Canavar kamyon, Otobüs, Kar motosikleti, Ay aracı, Tank, Süper araba) tekrar edilmedi.
- Fiyatlar 8.000 ile 250.000 aralığında. Not: Girişte "250.000 üstüne çıkan" ifadesi ile sonradan verilen "8.000–250.000 aralığı" kısıtı çelişiyor. Bu öneride ikinci kısıt uygulandı. Üst sınır üstünde bir fiyat istenirse ayrıca karar verilmeli.
- Ses kolonunda "yeniden kullanım" önerisi, mevcut ses dosyalarının isimlerine göre yapıldı. Ses dosyalarının içeriği bu araştırmada dinlenmedi. Bu öneriler doğrulanmalıdır.

| Türkçe ad | id | Fiyat (coin) | Karakter (tek cümle) | Kütle sınıfı | Tekerlek boyutu | Çekiş | Hava kontrolü | Motor ve ses karakteri | Mevcut sesten yeniden kullanım |
|---|---|---|---|---|---|---|---|---|---|
| Go-kart | go_kart | 8.000 | Çok hafif ve alçak, kısa mesafede hızlı ama zıplamada savrulur. | Hafif | Küçük | Arka | Orta | Yüksek perdeli iki zamanlı (two-stroke) vınlama | Motosiklet (perde yukarı kaydırılarak) |
| Dörtteker (Quad bike) | quad_bike | 15.000 | Kolay sürülür, düzensiz zeminde zıplar, bu yüzden dengeli bir başlangıç aracı. | Hafif | Küçük-orta | 4WD (düşük güçlü) | Yüksek | Tek silindir dört zamanlı, tık-tık boşta; düşük perdeli | Motosiklet (perde aşağı kaydırılarak) |
| Çopper (Chopper) | chopper | 35.000 | Uzun çatallı, ağır bir motosiklet; düz yolda dengeli, zıplamada burnu yukarı kalkar. | Orta | Büyük arka, orta ön | Arka | Düşük | Derin V-iki, yavaş ve gürleyen (lope) boşta | Motosiklet (daha düşük perdeli kopya) |
| Hot rod | hot_rod | 65.000 | Büyük arka tekerlekli, burnu ağır klasik araba; kalkışta güçlü, inişte burnu eğilir. | Orta | Büyük arka | Arka | Orta | V8, patlamalı egzoz (burble) | Süper araba (farklı perde ile); varsayım: motor sesi V8 ise |
| Çöp kamyonu (Garbage truck) | garbage_truck | 95.000 | Ağır, düşük hızlı, yükü sürüklediği için inişte sert ve yavaş tepki verir. | Ağır | Büyük | Arka | Çok düşük | Dizel, üstünde hidrolik vınlama (sıkıştırıcı) | Canavar kamyon (dizel tabanı), hidrolik vınlaması yeni |
| Formül araba (Formula car) | formula_car | 135.000 | Alçak profilli, hafif, yüksek devir; havada kanat dengesi sayesinde yönü korur. | Hafif | Küçük, alçak profil | Arka | Orta-yüksek | Yüksek devirli turbo V6, sert ıslık | Ralli arabası (turbo ıslığı), motor yeni |
| Forklift | forklift | 170.000 | Arka ağırlık dengeli ama ön kısmı ağır; çok dar bir koşu aracı, zıplamada tehlikeli. | Ağır | Küçük | Arka | Çok düşük | Elektrikli vınlama (electric whine), sessiz motor, bip uyarısı | Yeni; elektrikli vınlama ve geri gidiş bip sesi |
| İtfaiye aracı (Fire truck) | fire_truck | 230.000 | Dört tekerlekli, ağır ve güçlü; tırmanışta yavaş ama düşmeden ilerler. | Ağır | Büyük | 4WD | Düşük | Dizel V8, üstüne sirenli katman (ayrı kayıt) | Otobüs (dizel tabanı), sirenin yeni eklenmesi |

## 6. Önerilen yeni haritalar (6 adet) [Ö]

Kurallar:

- Mevcut haritalar (Kır yolu, Çöl, Kutup, Ay, Orman, Dağ, Plaj, Mağara, Bataklık, Şantiye) tekrar edilmedi. Yeni haritalar farklı zemin ve yerçekimi kombinasyonu sunar.
- Fiyatlar 5.000 ile 90.000 aralığında.

| Türkçe ad | id | Fiyat (coin) | Zemin | Yerçekimi | Tehlikeler | Arazi karakteri (eğim, boşluk, rampa) | Ortam sesi fikri |
|---|---|---|---|---|---|---|---|
| Tuz gölü | salt_flat | 5.000 | Tuz kabuğu (salt crust), çok düşük sürtünme | Normal | Ani tuz-salamura birikintileri (ani kayma) | Çok düz, hafif dalgalı, neredeyse hiç rampa yok, boşluk yok | Rüzgar sesi ve uzak kuş; düzlük hissi |
| Donmuş göl | frozen_lake | 25.000 | Buz, düşük sürtünme | Normal | Çatlayan buz (üstünden geçince düşer), delik | Düz ama kenarlarda kar tepeleri, küçük rampalar | Buz çatırtısı, rüzgar uğultusu |
| Gece çatıları | rooftops_night | 35.000 | Asfalt ve beton çatı | Normal | Düşen tuğla / saçak, kırılan çatı | Binalar arası boşluklar, dik çatı rampaları, dar çıkışlar | Şehir uğultusu, uzak trafik, çatı rüzgarı |
| Volkan | volcano | 55.000 | Bazalt kaya, sıcak yüzey | Normal | Lav sıçraması, sıcak nokta (zamanla yanma) | Dik yamaçlar, keskin kayalar, büyük boşluklar | Gürleme, kabarcık sesi, lav akışı |
| Lunapark | lunapark | 70.000 | Ahşap ray ve metal platform | Normal | Döngü (loop) ve ray kenarı boşluğu | Dönen rampalar, döngüler, sıçrama platformları | Lunapark müziği, döner atlıkarınca sesi |
| Deniz tabanı | deep_sea | 90.000 | Kum ve mercan kayası, su direnci | Düşük (yaklaşık yarı yerçekimi) ve su sürükleme | Mercan kayası, kabarcık akıntısı | Yumuşak eğimler, geniş açık alan, düşük zıplama | Boğuk ses, kabarcık sesi, su hareketi |

Not: "Deniz tabanı" yerçekimi açısından Ay haritasına benzer. Ayırt edici unsur su direncidir. Bu yüzden zıplama ve takla fiziği ayrıca ayarlanmalıdır.

## 7. Açık sorular

1. Fiyat aralığı çelişkisi: "250.000 üstüne çıkan" mi, yoksa "8.000–250.000 aralığında" mı? Bu öneride ikinci kısıt uygulandı.
2. Mevcut ses dosyalarının listesi ve içerikleri bilinmiyor. "Yeniden kullanım" önerileri isimlere dayanıyor. Ses listesi verilirse eşleşme yeniden kontrol edilebilir.
3. HC1 ve HC2'de haritalara özgü yerçekimi ve zemin bilgisi bulunamadı. Doğrulanmak isterse oyun içinde (ya da Gamepedia/Fandom açılabilen bir ortamda) bakılmalı.
4. Coin değerleri ve takla ödülleri tek kaynağa dayanıyor. Oyun içi test ile doğrulanmalı.
5. Ses odaklı oyunda coin toplama hissi için işitsel tasarım (pitch zinciri, titreşim) kaynaksızdır [B/Ö]. Prototip testi gerekir.

## 8. Özet

- HC2 araç ve yükseltme yapısı (engine, tires, suspension, 4WD, ek parçalar) fan wiki ve sürüm notlarıyla kısmen doğrulandı. Fiyat eğrisi eski bir sürüme ait.
- HC1 ve HC2 harita listeleri kısmen bulundu. Zemin, yerçekimi ve tehlike bilgileri kaynaklarda yoktur.
- Coin, yakıt ve takla mekanikleri temel düzeyde doğrulandı. Değerler tek kaynaklıdır.
- Menü akışı büyük ölçüde genel bilgiye dayanır.
- Önerilen 8 araç ve 6 harita, mevcut içerikle çakışmayan ve sesli oyuna uygun olarak tasarlandı [Ö].
