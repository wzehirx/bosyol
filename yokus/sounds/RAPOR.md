# RAPOR: ses hazırlığı (G6)

`python3 yokus/tools/prep_sounds.py` ham CC0 kayıtları (`yokus/sounds-raw/`) oyun seslerine çevirir. Çıktı: 44,1 kHz mono mp3, 128k. Toplam yaklaşık 6,4 MB.

## Ne kullanıldı
- Döngüler (eng_*, roll_*, amb_*, wind): ham kayıttan en durağan 4-10 sn'lik pencere otomatik seçildi (50 ms RMS dB varyansı, motorlarda ayrıca ton sabitliği; ilk 0,5 sn atlandı). Kuyruk 0,15 sn eşit güçle başa çapraz solduruldu, mono, doğrusal loudnorm (-20 LUFS). Döngü dikişindeki seviye farkı (son 50 ms / ilk 50 ms) tüm döngülerde < 3 dB (en yüksek 1,66 dB).
- Efektler (coin, fuel_pick, land_thud, creak, crash, flip, buy, fuel_low): baştaki sessizlik kesildi, süre sınırına kırpıldı (coin 0,6, fuel_pick 1,2, land_thud 0,6, creak 1, crash 2,5, flip 1, buy 1, fuel_low 1,2 sn), -16 LUFS.
- bosyol'dan olduğu gibi kopyalananlar: e0_*, e1_*, e2_* (wav) ve crash.mp3 (`crash_car.mp3` adıyla). Ham crash, `crash.mp3` olarak duruyor.

## Üretilen / yedeğe düşen
- `roll_sand`: ham kayıt kum üstünde ayak sesi, lastik değil. Sentezlendi: filtrelenmiş gürültü ve ince çıtırtı (granüler kum yuvarlanması).
- `nope`: 220 Hz, 0,15 sn, yumuşak sinüs (sentez). Süre çok kısa olduğu için LUFS ölçülemez.
- `eng_tractor` ve `eng_snowmobile`: ham kayıtta en iyi pencerede bile RMS değişimi 6 dB'yi aştı (8,3 ve 9,6 dB). bosyol motor katmanı `e0_med_on.wav` kullanıldı. İkisi aynı dosyadan türediği için aynı ses.
- `eng_buggy` ve `eng_monster`: ham kayıtlar (pass-by ve rölanti) beklenenin aksine durağan bir pencere verdi (6 dB eşiğinin altında), bu yüzden ham kayıt kullanıldı. Kulakla dinlenmedi. Kötü çıkarlarsa yedek eşlemesi hazır: eng_buggy için `e2_med_on`, eng_monster için `e1_med_on`.
- `amb_cave`: kayıt aralıklı damla seslerinden oluşuyor, durağan pencere yok. Dikiş farkı 1,05 dB ama ses düz bir yatak değil.

## Kaynaklar
`CREDITS.csv`: ham dosyaların Freesound künyeleri, bosyol motor katmanları (Bruno Simon folio-2019, MIT), `crash_car` (bosyol deposu) ve sentezlenenler.

## Coin sesleri (S1)
`tools/make_coins.py` (prep_sounds.py çağırır) 6 coin sesini **sentezler**; ham CC0 `coin.mp3` kullanılmadı (spektral ağırlık merkezi ~1,5 kHz, 1,9 sn, uzun kuyruk; temiz tek vuruş değil). `coin.mp3` genel yedek olarak duruyor. Hepsi 44,1 kHz mono mp3 128k, yaklaşık -16 LUFS, tepe en fazla -1,3 dBFS. Kısa sesler için LUFS ölçümü 1,2 sn'ye sessizlikle doldurularak yapıldı; tepe sınırı için yumuşak (tanh) limiter kullanıldı.
- Vuruş modeli: 1-2 ms atak, 5 uyumsuz parçacık (~2,6 / 3,9 / 5,2 / 6,8 / 8,1 kHz), yüksek parçacık daha hızlı sönümlenir, her parçacığa 3-9 Hz kaydırılmış ikizi (parıltı), 1,5 ms gürültü tıkı, 800 Hz yüksek geçiren.
- `coin_s` (değer 5): tek vuruş, 0,32 sn. Üç varyant: `coin_s`, `coin_s2`, `coin_s3` (farklı parçacık takımı; oyun dönüşümlü çalabilir).
- `coin_m` (25): 60 ms arayla iki vuruş, ikincisi daha parlak, 0,42 sn.
- `coin_l` (100): üç vuruş (0/55/115 ms) + 9 küçük rastgele paranın yumuşak çınlama kuyruğu, 0,62 sn.
- `coin_xl` (500): 1 sn içinde 20 rastgele küçük vuruş + majör üçlü (C7-E7-G7-C8) parlak çınlama, 1,22 sn.
- Perde yükselmesi yok: aynı değerin tüm alımları aynı perde ailesinde; çeşitlilik yalnızca varyantlardan gelir. Tohumlar sabit, çıktı tekrarlanabilir.
- Lisans: sentez, CC0 / kendi işimiz (`CREDITS.csv`).
