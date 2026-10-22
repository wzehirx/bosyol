# Yokuş Ustası: oyun testi raporu

Bu rapor `bash android/test/run.sh PlayTest` tarafından otomatik üretilir (elle düzenlemeyin, yeniden çalıştırınca yazılır). Sürücü `BlindDriver`: yalnızca sesle algılanabilen bilgilere bakar (eğim açısı, 10-15 m ilerideki radar eğimi, duyurular, havada olma, yakıt uyarıları, hız), 0.25 sn tepki gecikmesi ve küçük gürültüyle oynar. Strateji: tam gaz, burun zemin eğiminden 35° fazla kalkarsa gazı kes, havada burnu iniş eğimine yatır, takılırsa kısa geri git. "Acemi" sürücü yalnızca takla bipi (>70°) çalınca gazı keser.

- Koşu sayısı: 97 (matris 80, tohum 15); oyun zamanı sınırı 20 dk; gerçek süre 64 sn.
- Hata dedektörü tetiklenen koşu: 0

## 0. Kabul kapıları (PLAN2 F1-5)

| Kapı | Sonuç | Değer |
|---|---|---|
| Acemi, jeep y0, Kır yolu: ortalama mesafe ≥ 1500 m | GEÇTİ | 3197 m (3196, 3197, 3197, 3197, 3197) |
| Acemi: en az 4/5 koşu 3. bidona ulaşır | GEÇTİ | 5/5 |
| Acemi: ilk 1000 m'de CRASH yok | GEÇTİ | yok |
| İyi sürücü, jeep y0, Kır yolu: her koşu 15 dk'dan önce doğal biter | GEÇTİ | 6/6 (FUEL 3.6 dk, FUEL 3.6 dk, FUEL 3.6 dk, FUEL 3.7 dk, FUEL 3.6 dk, FUEL 3.6 dk) |
| İyi sürücü: medyan mesafe 2500-8000 m | GEÇTİ | 3196 m (3196, 3196, 3196, 3196, 3196, 3196) |
| Kır yolu: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 4620 m FUEL |
| Çöl: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3463 m FUEL |
| Kutup: asıl araç (Cip ya da Kar motosikleti) y3 ≥ 1000 m | GEÇTİ | Cip 2614 m FUEL, Kar motosikleti 8872 m FUEL |
| Ay: asıl araç (Cip ya da Ay aracı) y3 ≥ 1000 m | GEÇTİ | Cip 1266 m CRASH, Ay aracı 5793 m FUEL |
| Orman: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3580 m FUEL |
| Dağ: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 4184 m FUEL |
| Plaj: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3031 m FUEL |
| Mağara: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3366 m FUEL |
| Bataklık: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3990 m FUEL |
| Şantiye: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3239 m FUEL |
| Motosiklet y0, Kır yolu: medyan ≥ 1500 m | GEÇTİ | 4931 m (4933 FUEL, 4932 FUEL, 4931 FUEL, 4931 FUEL, 4931 FUEL, 4931 FUEL) |
| Ekonomi: motosiklet (8000) 4-6 iyi koşuda | GEÇTİ | 4.5 koşu (1765 altın/koşu, medyan koşu 3.6 dk) |
| Ekonomi: Çöl (5000) 2-3 koşuda | GEÇTİ | 2.8 koşu |
| Ekonomi: tüm mağaza 15-25 saat | GEÇTİ | 16.4 saat (1350000 altın, 1370 altın/dk) |

## 1. Hata dedektörleri

| Dedektör | Tetiklenen koşu | İlk örnek |
|---|---|---|
| NaN | 0 | - |
| tiltDeg>180 | 0 | - |
| sırtüstü | 0 | - |
| TAKILI/HANG | 0 | - |
| TAKILI | 0 | - |
| voiceCount>60 | 0 | - |

## 2. Harita × araç, geliştirme yok (iyi sürücü)

| Harita | Araç | Mesafe (m) | Bitiş | Süre (sn) | Bidon | En az yakıt | Tepe açı | Takla | Altın | Bonus | Geri vites |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Kır yolu | Cip | 3196 | FUEL | 218 | 4 | 11% | 51° | 0 | 1765 | 0 | 0 |
| Kır yolu | Motosiklet | 4933 | FUEL | 307 | 5 | 16% | 81° | 0 | 3110 | 0 | 0 |
| Kır yolu | Pikap | 4933 | FUEL | 374 | 5 | 18% | 47° | 0 | 3110 | 0 | 0 |
| Kır yolu | Traktör | 3867 | FUEL | 486 | 5 | 4% | 51° | 0 | 2125 | 0 | 0 |
| Çöl | Cip | 3083 | FUEL | 227 | 4 | 0% | 50° | 0 | 2135 | 0 | 0 |
| Çöl | Motosiklet | 3391 | FUEL | 241 | 4 | 33% | 73° | 0 | 2510 | 0 | 0 |
| Çöl | Pikap | 3486 | FUEL | 279 | 4 | 36% | 47° | 0 | 2575 | 0 | 0 |
| Çöl | Traktör | 3448 | FUEL | 372 | 4 | 25% | 49° | 0 | 2575 | 0 | 0 |
| Kutup | Cip | 2606 | FUEL | 217 | 4 | 1% | 47° | 0 | 1815 | 0 | 0 |
| Kutup | Motosiklet | 2360 | FUEL | 221 | 4 | 29% | 64° | 0 | 1750 | 0 | 0 |
| Kutup | Pikap | 2612 | FUEL | 269 | 4 | 41% | 46° | 0 | 1815 | 0 | 0 |
| Kutup | Traktör | 2881 | FUEL | 351 | 4 | 34% | 48° | 0 | 2055 | 0 | 0 |
| Ay | Cip | 2340 | FUEL | 248 | 2 | 12% | 180° | 0 | 3380 | 2300 | 0 |
| Ay | Motosiklet | 1942 | FUEL | 319 | 3 | 40% | 94° | 0 | 2225 | 1100 | 0 |
| Ay | Pikap | 5380 | FUEL | 573 | 3 | 12% | 77° | 0 | 10155 | 5775 | 0 |
| Ay | Traktör | 5425 | FUEL | 668 | 5 | 1% | 55° | 0 | 9470 | 4525 | 0 |
| Orman | Cip | 2234 | FUEL | 162 | 3 | 54% | 44° | 0 | 2640 | 0 | 0 |
| Orman | Motosiklet | 5062 | FUEL | 348 | 5 | 0% | 76° | 0 | 10615 | 200 | 0 |
| Orman | Pikap | 3665 | FUEL | 294 | 4 | 29% | 47° | 0 | 6625 | 0 | 0 |
| Orman | Traktör | 2449 | FUEL | 388 | 4 | 13% | 46° | 0 | 2825 | 0 | 1 |
| Dağ | Cip | 2788 | FUEL | 214 | 4 | 27% | 86° | 0 | 3810 | 0 | 0 |
| Dağ | Motosiklet | 5654 | FUEL | 420 | 6 | 0% | 73° | 0 | 13690 | 250 | 0 |
| Dağ | Pikap | 2347 | FUEL | 254 | 4 | 51% | 50° | 0 | 3370 | 0 | 0 |
| Dağ | Traktör | 1932 | FUEL | 340 | 4 | 34% | 50° | 0 | 3095 | 0 | 0 |
| Plaj | Cip | 2785 | FUEL | 192 | 3 | 25% | 40° | 0 | 5275 | 0 | 0 |
| Plaj | Motosiklet | 3114 | FUEL | 193 | 3 | 51% | 58° | 0 | 7530 | 0 | 0 |
| Plaj | Pikap | 4768 | FUEL | 349 | 4 | 5% | 48° | 0 | 12000 | 0 | 0 |
| Plaj | Traktör | 3012 | FUEL | 314 | 3 | 44% | 45° | 0 | 6755 | 0 | 0 |
| Mağara | Cip | 2428 | FUEL | 214 | 4 | 11% | 60° | 0 | 5820 | 0 | 0 |
| Mağara | Motosiklet | 4881 | FUEL | 336 | 5 | 0% | 75° | 0 | 14105 | 100 | 0 |
| Mağara | Pikap | 2426 | FUEL | 272 | 4 | 42% | 51° | 0 | 5820 | 0 | 0 |
| Mağara | Traktör | 1475 | FUEL | 265 | 3 | 65% | 41° | 0 | 3200 | 0 | 0 |
| Bataklık | Cip | 2602 | FUEL | 193 | 3 | 34% | 35° | 0 | 6560 | 0 | 0 |
| Bataklık | Motosiklet | 3250 | FUEL | 272 | 4 | 0% | 73° | 0 | 8815 | 0 | 0 |
| Bataklık | Pikap | 3942 | FUEL | 324 | 4 | 12% | 48° | 0 | 10390 | 0 | 0 |
| Bataklık | Traktör | 3923 | FUEL | 431 | 4 | 0% | 50° | 0 | 10340 | 0 | 0 |
| Şantiye | Cip | 2942 | FUEL | 207 | 3 | 20% | 53° | 0 | 10180 | 0 | 0 |
| Şantiye | Motosiklet | 3281 | FUEL | 204 | 3 | 46% | 69° | 0 | 12130 | 100 | 0 |
| Şantiye | Pikap | 4897 | FUEL | 370 | 4 | 2% | 48° | 0 | 18680 | 0 | 0 |
| Şantiye | Traktör | 3222 | FUEL | 346 | 3 | 39% | 50° | 0 | 12230 | 0 | 0 |

## 3. Harita × araç, geliştirme hepsi 3. seviye (iyi sürücü)

| Harita | Araç | Mesafe (m) | Bitiş | Süre (sn) | Bidon | En az yakıt | Tepe açı | Takla | Altın | Bonus | Geri vites |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Kır yolu | Cip | 4620 | FUEL | 314 | 5 | 5% | 53° | 0 | 2695 | 0 | 0 |
| Kır yolu | Motosiklet | 6904 | FUEL | 424 | 6 | 17% | 81° | 0 | 6055 | 100 | 0 |
| Kır yolu | Pikap | 6924 | FUEL | 515 | 6 | 19% | 47° | 0 | 5955 | 0 | 0 |
| Kır yolu | Traktör | 6384 | FUEL | 705 | 6 | 3% | 53° | 0 | 5360 | 0 | 0 |
| Çöl | Cip | 3463 | FUEL | 243 | 4 | 24% | 50° | 0 | 2575 | 0 | 0 |
| Çöl | Motosiklet | 5499 | CRASH | 351 | 5 | 14% | 146° | 0 | 4560 | 50 | 0 |
| Çöl | Pikap | 4609 | FUEL | 418 | 5 | 26% | 47° | 0 | 3445 | 0 | 0 |
| Çöl | Traktör | 5398 | FUEL | 580 | 5 | 14% | 51° | 0 | 4505 | 0 | 0 |
| Kutup | Cip | 2614 | FUEL | 227 | 4 | 32% | 48° | 0 | 1815 | 0 | 0 |
| Kutup | Motosiklet | 2887 | FUEL | 242 | 4 | 51% | 72° | 0 | 2055 | 0 | 0 |
| Kutup | Pikap | 2624 | FUEL | 295 | 4 | 57% | 45° | 0 | 1815 | 0 | 0 |
| Kutup | Traktör | 3915 | FUEL | 524 | 5 | 24% | 49° | 0 | 2770 | 0 | 0 |
| Ay | Cip | 1266 | CRASH | 123 | 0 | - | 148° | 0 | 1785 | 1125 | 0 |
| Ay | Motosiklet | 2589 | FUEL | 455 | 4 | 0% | 84° | 0 | 3480 | 2325 | 2 |
| Ay | Pikap | 6164 | FUEL | 626 | 3 | 33% | 80° | 0 | 13230 | 6675 | 0 |
| Ay | Traktör | 7970 | FUEL | 955 | 6 | 5% | 67° | 0 | 20825 | 6375 | 0 |
| Orman | Cip | 3580 | FUEL | 254 | 4 | 21% | 51° | 0 | 6625 | 0 | 0 |
| Orman | Motosiklet | 7627 | FUEL | 503 | 6 | 0% | 125° | 0 | 21155 | 350 | 0 |
| Orman | Pikap | 7638 | FUEL | 599 | 6 | 0% | 51° | 0 | 20935 | 0 | 0 |
| Orman | Traktör | 5432 | FUEL | 619 | 5 | 0% | 52° | 0 | 11860 | 0 | 1 |
| Dağ | Cip | 4184 | FUEL | 312 | 5 | 16% | 58° | 0 | 7535 | 100 | 0 |
| Dağ | Motosiklet | 9642 | FUEL | 681 | 8 | 0% | 83° | 0 | 29130 | 350 | 0 |
| Dağ | Pikap | 3339 | FUEL | 372 | 5 | 46% | 54° | 0 | 4825 | 0 | 0 |
| Dağ | Traktör | 2815 | FUEL | 369 | 4 | 57% | 50° | 0 | 3810 | 0 | 0 |
| Plaj | Cip | 3031 | FUEL | 206 | 3 | 44% | 44° | 0 | 6755 | 0 | 0 |
| Plaj | Motosiklet | 5071 | FUEL | 310 | 4 | 22% | 64° | 0 | 13325 | 0 | 0 |
| Plaj | Pikap | 7613 | FUEL | 556 | 5 | 0% | 47° | 0 | 22190 | 0 | 0 |
| Plaj | Traktör | 5045 | FUEL | 523 | 4 | 13% | 52° | 0 | 13425 | 0 | 0 |
| Mağara | Cip | 3366 | FUEL | 245 | 4 | 34% | 59° | 0 | 9660 | 0 | 0 |
| Mağara | Motosiklet | 7093 | FUEL | 473 | 6 | 15% | 82° | 0 | 23480 | 100 | 0 |
| Mağara | Pikap | 4052 | FUEL | 416 | 5 | 25% | 52° | 0 | 11790 | 0 | 0 |
| Mağara | Traktör | 2427 | FUEL | 401 | 4 | 46% | 53° | 0 | 5820 | 0 | 0 |
| Bataklık | Cip | 3990 | FUEL | 283 | 4 | 0% | 51° | 0 | 10690 | 0 | 0 |
| Bataklık | Motosiklet | 4345 | FUEL | 292 | 4 | 28% | 73° | 0 | 12420 | 0 | 0 |
| Bataklık | Pikap | 4723 | FUEL | 492 | 5 | 6% | 47° | 0 | 13175 | 0 | 1 |
| Bataklık | Traktör | 4301 | FUEL | 478 | 4 | 24% | 52° | 0 | 12195 | 0 | 1 |
| Şantiye | Cip | 3239 | FUEL | 224 | 3 | 39% | 50° | 0 | 12030 | 0 | 0 |
| Şantiye | Motosiklet | 5236 | FUEL | 327 | 4 | 21% | 76° | 0 | 20205 | 350 | 0 |
| Şantiye | Pikap | 7582 | FUEL | 568 | 5 | 0% | 48° | 0 | 27105 | 0 | 0 |
| Şantiye | Traktör | 5164 | FUEL | 544 | 4 | 10% | 53° | 0 | 19630 | 0 | 0 |

## 4. Kır yolu, jeep, geliştirme yok: tohumlar

| Sürücü | Tohum | Mesafe (m) | Bitiş | Süre (sn) | Bidon | Bidon konumları (m, yakıt%) | Tepe açı | Altın | Duyuru | En uzun sessizlik (sn) |
|---|---|---|---|---|---|---|---|---|---|---|
| iyi | 0 | 3196 | FUEL | 218 | 4 | 241 (73%), 681 (59%), 1115 (59%), 2035 (11%) | 51° | 1765 | 54 | 12 |
| iyi | 1 | 3196 | FUEL | 217 | 4 | 241 (73%), 681 (58%), 1115 (58%), 2035 (9%) | 51° | 1765 | 54 | 12 |
| iyi | 2 | 3196 | FUEL | 218 | 4 | 241 (73%), 681 (58%), 1115 (59%), 2035 (10%) | 51° | 1765 | 55 | 12 |
| iyi | 3 | 3196 | FUEL | 220 | 4 | 241 (73%), 681 (59%), 1115 (58%), 2034 (9%) | 51° | 1765 | 55 | 12 |
| iyi | 4 | 3196 | FUEL | 217 | 4 | 241 (73%), 681 (59%), 1115 (60%), 2034 (9%) | 45° | 1765 | 54 | 12 |
| iyi | 5 | 3196 | FUEL | 218 | 4 | 241 (73%), 681 (60%), 1115 (58%), 2034 (9%) | 51° | 1765 | 55 | 12 |
| acemi | 1 | 3196 | FUEL | 217 | 4 | 241 (73%), 681 (58%), 1115 (58%), 2034 (10%) | 63° | 1765 | 55 | 12 |
| acemi | 2 | 3197 | FUEL | 217 | 4 | 241 (73%), 681 (58%), 1115 (59%), 2034 (10%) | 62° | 1765 | 55 | 12 |
| acemi | 3 | 3197 | FUEL | 220 | 4 | 241 (73%), 681 (59%), 1115 (58%), 2035 (10%) | 62° | 1765 | 55 | 12 |
| acemi | 4 | 3197 | FUEL | 217 | 4 | 241 (73%), 681 (59%), 1115 (60%), 2034 (10%) | 50° | 1765 | 54 | 12 |
| acemi | 5 | 3197 | FUEL | 217 | 4 | 241 (73%), 681 (60%), 1115 (58%), 2035 (10%) | 62° | 1765 | 55 | 12 |

İyi sürücü, tohum 1-5: 3. yakıt bidonuna ulaşan = **5/5** (PLAN2 F1 kuralı: en az 4/5).

Motosiklet, Kır yolu, geliştirme yok (iyi sürücü, tohum 0-5): 4933 m FUEL (tepe 81°), 4932 m FUEL (tepe 81°), 4931 m FUEL (tepe 81°), 4931 m FUEL (tepe 81°), 4931 m FUEL (tepe 81°), 4931 m FUEL (tepe 81°).

Haritadaki bidonlar (Kır yolu): 248, 688, 1123, 2042, 3304, 4977, 7015, 9341 m; ardışık aralıklar: 248, 440, 434, 919, 1263, 1673, 2038, 2325 m.

## 5. Takılma, takla ve bitiş olayları

TAKILMA = yerde, yakıt var, hız < 0.5 m/s, 3 sn. TERS = yerde göreli açı > 100°. SON-* = oyunu bitiren olay. Eğim = olay noktasındaki zemin eğimi, Açı = burun açısı (dünyaya göre).

| Koşu | Olay | x (m) | Eğim | Açı | Yakıt | t (sn) |
|---|---|---|---|---|---|---|
| Ay/Ay aracı/y3/iyi/s0 | SON-FUEL | 5771 | 7° | 8° | 0% | 691 |
| Ay/Cip/y0/iyi/s0 | TERS | 2283 | -18° | 97° | 0% | 228 |
| Ay/Cip/y0/iyi/s0 | SON-FUEL | 2317 | 23° | 23° | 0% | 248 |
| Ay/Cip/y3/iyi/s0 | TERS | 1257 | -1° | -108° | 0% | 122 |
| Ay/Cip/y3/iyi/s0 | SON-CRASH | 1266 | 19° | -148° | 0% | 123 |
| Ay/Motosiklet/y0/iyi/s0 | SON-FUEL | 1920 | -8° | -8° | 0% | 319 |
| Ay/Motosiklet/y3/iyi/s0 | TAKILMA | 2588 | 34° | 37° | 72% | 367 |
| Ay/Motosiklet/y3/iyi/s0 | TAKILMA | 2572 | 36° | 36° | 42% | 401 |
| Ay/Motosiklet/y3/iyi/s0 | SON-FUEL | 2539 | 2° | 2° | 0% | 455 |
| Ay/Pikap/y0/iyi/s0 | SON-FUEL | 5358 | 26° | 25° | 0% | 573 |
| Ay/Pikap/y3/iyi/s0 | SON-FUEL | 6138 | 24° | 25° | 0% | 626 |
| Ay/Traktör/y0/iyi/s0 | SON-FUEL | 5417 | 11° | 11° | 0% | 668 |
| Ay/Traktör/y3/iyi/s0 | SON-FUEL | 7949 | 17° | 17° | 0% | 955 |
| Bataklık/Cip/y0/iyi/s0 | SON-FUEL | 2590 | 8° | 7° | 0% | 193 |
| Bataklık/Cip/y3/iyi/s0 | SON-FUEL | 3941 | 25° | 25° | 0% | 283 |
| Bataklık/Motosiklet/y0/iyi/s0 | SON-FUEL | 3175 | 4° | 4° | 0% | 272 |
| Bataklık/Motosiklet/y3/iyi/s0 | SON-FUEL | 4259 | 21° | 22° | 0% | 292 |
| Bataklık/Pikap/y0/iyi/s0 | SON-FUEL | 3857 | -5° | -5° | 0% | 324 |
| Bataklık/Pikap/y3/iyi/s0 | TAKILMA | 4677 | 21° | 22° | 36% | 430 |
| Bataklık/Pikap/y3/iyi/s0 | SON-FUEL | 4643 | -15° | -15° | 0% | 492 |
| Bataklık/Traktör/y0/iyi/s0 | SON-FUEL | 3861 | -3° | -2° | 0% | 431 |
| Bataklık/Traktör/y3/iyi/s0 | SON-FUEL | 4298 | -3° | -4° | 0% | 478 |
| Dağ/Cip/y0/iyi/s0 | SON-FUEL | 2722 | -4° | -1° | 0% | 214 |
| Dağ/Cip/y3/iyi/s0 | SON-FUEL | 4151 | -15° | -20° | 0% | 312 |
| Dağ/Motosiklet/y0/iyi/s0 | SON-FUEL | 5610 | -14° | -13° | 0% | 420 |
| Dağ/Motosiklet/y3/iyi/s0 | SON-FUEL | 9547 | 35° | 40° | 0% | 681 |
| Dağ/Pikap/y0/iyi/s0 | SON-FUEL | 2311 | 18° | 15° | 0% | 254 |
| Dağ/Pikap/y3/iyi/s0 | SON-FUEL | 3257 | 21° | 18° | 0% | 372 |
| Dağ/Traktör/y0/iyi/s0 | SON-FUEL | 1855 | -20° | -20° | 0% | 340 |
| Dağ/Traktör/y3/iyi/s0 | SON-FUEL | 2715 | -35° | -36° | 0% | 369 |
| Kutup/Cip/y0/iyi/s0 | SON-FUEL | 2557 | -1° | -1° | 0% | 217 |
| Kutup/Cip/y3/iyi/s0 | SON-FUEL | 2510 | 2° | 3° | 0% | 227 |
| Kutup/Kar motosikleti/y3/iyi/s0 | SON-FUEL | 8796 | 8° | 5° | 0% | 606 |
| Kutup/Motosiklet/y0/iyi/s0 | SON-FUEL | 2267 | 8° | 18° | 0% | 221 |
| Kutup/Motosiklet/y3/iyi/s0 | SON-FUEL | 2831 | -5° | -5° | 0% | 242 |
| Kutup/Pikap/y0/iyi/s0 | SON-FUEL | 2545 | -6° | -5° | 0% | 269 |
| Kutup/Pikap/y3/iyi/s0 | TAKILMA | 2586 | 20° | 21° | 12% | 269 |
| Kutup/Pikap/y3/iyi/s0 | SON-FUEL | 2541 | 38° | 30° | 0% | 295 |
| Kutup/Traktör/y0/iyi/s0 | SON-FUEL | 2834 | -4° | -3° | 0% | 351 |
| Kutup/Traktör/y3/iyi/s0 | TAKILMA | 3878 | 27° | 30° | 4% | 508 |
| Kutup/Traktör/y3/iyi/s0 | SON-FUEL | 3796 | 22° | 25° | 0% | 524 |
| Kır yolu/Cip/y0/acemi/s1 | SON-FUEL | 3125 | -13° | -13° | 0% | 217 |
| Kır yolu/Cip/y0/acemi/s2 | SON-FUEL | 3125 | -13° | -13° | 0% | 217 |
| Kır yolu/Cip/y0/acemi/s3 | SON-FUEL | 3125 | -13° | -13° | 0% | 220 |
| Kır yolu/Cip/y0/acemi/s4 | SON-FUEL | 3125 | -13° | -13° | 0% | 217 |
| Kır yolu/Cip/y0/acemi/s5 | SON-FUEL | 3125 | -13° | -13° | 0% | 217 |
| Kır yolu/Cip/y0/iyi/s0 | SON-FUEL | 3125 | -13° | -13° | 0% | 218 |
| Kır yolu/Cip/y0/iyi/s1 | SON-FUEL | 3125 | -13° | -13° | 0% | 217 |
| Kır yolu/Cip/y0/iyi/s2 | SON-FUEL | 3125 | -13° | -13° | 0% | 218 |
| Kır yolu/Cip/y0/iyi/s3 | SON-FUEL | 3125 | -13° | -13° | 0% | 220 |
| Kır yolu/Cip/y0/iyi/s4 | SON-FUEL | 3125 | -13° | -13° | 0% | 217 |
| Kır yolu/Cip/y0/iyi/s5 | SON-FUEL | 3125 | -13° | -13° | 0% | 218 |
| Kır yolu/Cip/y3/iyi/s0 | SON-FUEL | 4540 | 17° | 17° | 0% | 314 |
| Kır yolu/Motosiklet/y0/iyi/s0 | SON-FUEL | 4878 | -13° | -13° | 0% | 307 |
| Kır yolu/Motosiklet/y0/iyi/s1 | SON-FUEL | 4880 | -13° | -13° | 0% | 310 |
| Kır yolu/Motosiklet/y0/iyi/s2 | SON-FUEL | 4880 | -13° | -13° | 0% | 312 |
| Kır yolu/Motosiklet/y0/iyi/s3 | SON-FUEL | 4880 | -17° | -14° | 0% | 312 |
| Kır yolu/Motosiklet/y0/iyi/s4 | SON-FUEL | 4880 | -13° | -13° | 0% | 309 |
| Kır yolu/Motosiklet/y0/iyi/s5 | SON-FUEL | 4880 | -13° | -13° | 0% | 310 |
| Kır yolu/Motosiklet/y3/iyi/s0 | SON-FUEL | 6837 | -2° | -2° | 0% | 424 |
| Kır yolu/Pikap/y0/iyi/s0 | SON-FUEL | 4876 | -18° | -18° | 0% | 374 |
| Kır yolu/Pikap/y3/iyi/s0 | SON-FUEL | 6824 | 7° | 7° | 0% | 515 |
| Kır yolu/Traktör/y0/iyi/s0 | SON-FUEL | 3795 | -6° | -6° | 0% | 486 |
| Kır yolu/Traktör/y3/iyi/s0 | TAKILMA | 6362 | 28° | 30° | 7% | 683 |
| Kır yolu/Traktör/y3/iyi/s0 | SON-FUEL | 6301 | -5° | -5° | 0% | 705 |
| Mağara/Cip/y0/iyi/s0 | SON-FUEL | 2345 | 22° | 25° | 0% | 214 |
| Mağara/Cip/y3/iyi/s0 | SON-FUEL | 3343 | 17° | 21° | 0% | 245 |
| Mağara/Motosiklet/y0/iyi/s0 | SON-FUEL | 4790 | -13° | -13° | 0% | 336 |
| Mağara/Motosiklet/y3/iyi/s0 | SON-FUEL | 7081 | -1° | -1° | 0% | 473 |
| Mağara/Pikap/y0/iyi/s0 | SON-FUEL | 2321 | -13° | -19° | 0% | 272 |
| Mağara/Pikap/y3/iyi/s0 | SON-FUEL | 3986 | -5° | -6° | 0% | 416 |
| Mağara/Traktör/y0/iyi/s0 | SON-FUEL | 1419 | 5° | 6° | 0% | 265 |
| Mağara/Traktör/y3/iyi/s0 | SON-FUEL | 2334 | -5° | -8° | 0% | 401 |
| Orman/Cip/y0/iyi/s0 | SON-FUEL | 2186 | -23° | -24° | 0% | 162 |
| Orman/Cip/y3/iyi/s0 | SON-FUEL | 3536 | -10° | -11° | 0% | 254 |
| Orman/Motosiklet/y0/iyi/s0 | SON-FUEL | 5006 | -13° | -14° | 0% | 348 |
| Orman/Motosiklet/y3/iyi/s0 | TERS | 5768 | -9° | -118° | 100% | 368 |
| Orman/Motosiklet/y3/iyi/s0 | SON-FUEL | 7514 | 13° | -16° | 0% | 503 |
| Orman/Pikap/y0/iyi/s0 | SON-FUEL | 3587 | 29° | 28° | 0% | 294 |
| Orman/Pikap/y3/iyi/s0 | SON-FUEL | 7520 | -5° | 0° | 0% | 599 |
| Orman/Traktör/y0/iyi/s0 | SON-FUEL | 2411 | -0° | -0° | 0% | 388 |
| Orman/Traktör/y3/iyi/s0 | SON-FUEL | 5395 | -16° | -20° | 0% | 619 |
| Plaj/Cip/y0/iyi/s0 | SON-FUEL | 2724 | -19° | -20° | 0% | 192 |
| Plaj/Cip/y3/iyi/s0 | SON-FUEL | 2959 | -13° | -13° | 0% | 206 |
| Plaj/Motosiklet/y0/iyi/s0 | SON-FUEL | 3082 | 4° | 4° | 0% | 193 |
| Plaj/Motosiklet/y3/iyi/s0 | SON-FUEL | 5014 | -8° | -7° | 0% | 310 |
| Plaj/Pikap/y0/iyi/s0 | SON-FUEL | 4710 | -2° | -2° | 0% | 349 |
| Plaj/Pikap/y3/iyi/s0 | SON-FUEL | 7544 | -16° | -17° | 0% | 556 |
| Plaj/Traktör/y0/iyi/s0 | SON-FUEL | 2972 | -10° | -10° | 0% | 314 |
| Plaj/Traktör/y3/iyi/s0 | SON-FUEL | 5006 | 2° | 4° | 0% | 523 |
| Çöl/Cip/y0/iyi/s0 | SON-FUEL | 3003 | -7° | -7° | 0% | 227 |
| Çöl/Cip/y3/iyi/s0 | SON-FUEL | 3340 | -8° | -9° | 0% | 243 |
| Çöl/Motosiklet/y0/iyi/s0 | SON-FUEL | 3329 | -16° | -23° | 0% | 241 |
| Çöl/Motosiklet/y3/iyi/s0 | SON-CRASH | 5499 | 15° | -146° | 0% | 351 |
| Çöl/Pikap/y0/iyi/s0 | SON-FUEL | 3372 | 13° | 13° | 0% | 279 |
| Çöl/Pikap/y3/iyi/s0 | SON-FUEL | 4504 | 7° | 6° | 0% | 418 |
| Çöl/Traktör/y0/iyi/s0 | SON-FUEL | 3361 | 22° | 25° | 0% | 372 |
| Çöl/Traktör/y3/iyi/s0 | SON-FUEL | 5362 | -14° | -13° | 0% | 580 |
| Şantiye/Cip/y0/iyi/s0 | SON-FUEL | 2898 | -5° | -5° | 0% | 207 |
| Şantiye/Cip/y3/iyi/s0 | SON-FUEL | 3178 | -13° | -13° | 0% | 224 |
| Şantiye/Motosiklet/y0/iyi/s0 | SON-FUEL | 3242 | 10° | 2° | 0% | 204 |
| Şantiye/Motosiklet/y3/iyi/s0 | SON-FUEL | 5209 | 40° | 35° | 0% | 327 |
| Şantiye/Pikap/y0/iyi/s0 | SON-FUEL | 4857 | 2° | 2° | 0% | 370 |
| Şantiye/Pikap/y3/iyi/s0 | SON-FUEL | 7523 | -10° | -10° | 0% | 568 |
| Şantiye/Traktör/y0/iyi/s0 | SON-FUEL | 3185 | -12° | -13° | 0% | 346 |
| Şantiye/Traktör/y3/iyi/s0 | SON-FUEL | 5139 | -3° | -2° | 0% | 544 |

## 5b. Araç özeti (matris, her iki geliştirme düzeyi, iyi sürücü)

| Araç | Ortalama mesafe (m) | Ortalama süre (sn) | CRASH | FUEL | SÜRE sınırı | Koşu |
|---|---|---|---|---|---|---|
| Cip | 3018 | 226 | 1 | 19 | 0 | 20 |
| Motosiklet | 4738 | 346 | 1 | 19 | 0 | 20 |
| Pikap | 4686 | 411 | 0 | 20 | 0 | 20 |
| Traktör | 4024 | 483 | 0 | 20 | 0 | 20 |

### Yakıt bitişleri

"Bidona kalan" = oyun bittiği yerden sonraki ilk yakıt bidonuna uzaklık. Eğim küçük ve bidon yakınsa neden büyük olasılıkla bidonun kaçırılması ya da erken yakıt tükenmesidir; eğim büyükse yokuşta takılmadır.

| Koşu | Mesafe (m) | Eğim | Bidona kalan (m) | Son bidondan beri (m) | Yakında olan özellikler |
|---|---|---|---|---|---|
| Ay/Cip/y0/iyi/s0 | 2340 | 23° | 26 | 1064 | çukur (2237-2246 m), dik yokuş (2330-2340 m) |
| Ay/Motosiklet/y0/iyi/s0 | 1942 | -8° | 423 | 665 | dik yokuş (1790-1810 m), çukur (1915-1922 m), dik yokuş (1940-1950 m) |
| Ay/Motosiklet/y3/iyi/s0 | 2589 | 2° | 1373 | 249 | tümsekler (2412-2441 m), dik iniş (2450-2470 m), çukur (2484-2490 m) |
| Ay/Pikap/y0/iyi/s0 | 5380 | 26° | 569 | 1470 | tümsekler (5232-5249 m), dik iniş (5260-5310 m), çukur (5318-5329 m), dik yokuş (5360-5400 m) |
| Ay/Pikap/y3/iyi/s0 | 6164 | 24° | 2249 | 2254 | dik iniş (6040-6090 m), çukur (6098-6108 m), dik yokuş (6140-6200 m) |
| Ay/Traktör/y0/iyi/s0 | 5425 | 11° | 510 | 1515 | dik iniş (5260-5310 m), çukur (5318-5329 m), dik yokuş (5360-5400 m), rampa (5430-5434 m) |
| Ay/Traktör/y3/iyi/s0 | 7970 | 17° | 437 | 2045 | tümsekler (7839-7858 m), dik iniş (7870-7900 m), çukur (7914-7923 m), tümsekler (7971-7994 m) |
| Bataklık/Cip/y0/iyi/s0 | 2602 | 8° | 194 | 1146 | çukur (2558-2566 m) |
| Bataklık/Cip/y3/iyi/s0 | 3990 | 25° | 675 | 1208 | tümsekler (3897-3922 m), dik yokuş (3940-3950 m) |
| Bataklık/Motosiklet/y0/iyi/s0 | 3250 | 4° | 1441 | 468 | dik iniş (3100-3110 m), rampa (3160-3164 m) |
| Bataklık/Motosiklet/y3/iyi/s0 | 4345 | 21° | 357 | 1563 | dik iniş (4190-4200 m), çukur (4209-4217 m) |
| Bataklık/Pikap/y0/iyi/s0 | 3942 | -5° | 759 | 1160 | dik iniş (3750-3790 m), çukur (3799-3807 m) |
| Bataklık/Pikap/y3/iyi/s0 | 4723 | -15° | 2346 | 109 | dik iniş (4510-4560 m), çukur (4568-4577 m), çukur (4664-4672 m) |
| Bataklık/Traktör/y0/iyi/s0 | 3923 | -3° | 755 | 1142 | dik iniş (3750-3790 m), çukur (3799-3807 m) |
| Bataklık/Traktör/y3/iyi/s0 | 4301 | -3° | 318 | 1519 | dik iniş (4190-4200 m), çukur (4209-4217 m), çukur (4301-4307 m) |
| Dağ/Cip/y0/iyi/s0 | 2788 | -4° | 97 | 1032 | dik yokuş (2590-2610 m), dik iniş (2640-2650 m), çukur (2665-2675 m), dik iniş (2690-2720 m), çukur (2733-2741 m) |
| Dağ/Cip/y3/iyi/s0 | 4184 | -15° | 49 | 1367 | dik iniş (4040-4070 m), çukur (4084-4092 m), dik iniş (4130-4140 m), çukur (4153-4163 m) |
| Dağ/Motosiklet/y0/iyi/s0 | 5654 | -14° | 208 | 1456 | dik iniş (5480-5510 m), çukur (5525-5535 m), dik iniş (5580-5610 m), çukur (5616-5625 m) |
| Dağ/Motosiklet/y3/iyi/s0 | 9642 | 35° | 342 | 1931 | dik iniş (9440-9470 m), çukur (9482-9490 m), dik yokuş (9520-9580 m) |
| Dağ/Pikap/y0/iyi/s0 | 2347 | 18° | 508 | 591 | dik iniş (2190-2210 m), çukur (2218-2226 m), dik iniş (2240-2270 m), dik yokuş (2290-2300 m), çukur (2311-2318 m), dik yokuş (2330-2360 m) |
| Dağ/Pikap/y3/iyi/s0 | 3339 | 21° | 944 | 522 | dik iniş (3130-3150 m), çukur (3162-3170 m), dik iniş (3200-3230 m), rampa (3270-3275 m) |
| Dağ/Traktör/y0/iyi/s0 | 1932 | -20° | 965 | 176 | dik yokuş (1720-1740 m), tümsekler (1757-1783 m), dik iniş (1800-1810 m), dik iniş (1840-1850 m), çukur (1864-1874 m) |
| Dağ/Traktör/y3/iyi/s0 | 2815 | -35° | 105 | 1059 | dik yokuş (2590-2610 m), dik iniş (2640-2650 m), çukur (2665-2675 m), dik iniş (2690-2720 m), çukur (2733-2741 m) |
| Kutup/Cip/y0/iyi/s0 | 2606 | -1° | 704 | 632 | dik iniş (2470-2500 m), rampa (2542-2547 m) |
| Kutup/Cip/y3/iyi/s0 | 2614 | 2° | 751 | 640 | çukur (2418-2429 m), dik iniş (2470-2500 m) |
| Kutup/Motosiklet/y0/iyi/s0 | 2360 | 8° | 994 | 386 | dik iniş (2160-2180 m), çukur (2188-2197 m), dik iniş (2230-2240 m), çukur (2255-2264 m) |
| Kutup/Motosiklet/y3/iyi/s0 | 2887 | -5° | 430 | 913 | dik iniş (2730-2760 m), çukur (2819-2828 m) |
| Kutup/Pikap/y0/iyi/s0 | 2612 | -6° | 716 | 638 | dik iniş (2470-2500 m), rampa (2542-2547 m) |
| Kutup/Pikap/y3/iyi/s0 | 2624 | 38° | 720 | 650 | çukur (2418-2429 m), dik iniş (2470-2500 m), rampa (2542-2547 m) |
| Kutup/Traktör/y0/iyi/s0 | 2881 | -4° | 427 | 907 | dik iniş (2730-2760 m), çukur (2819-2828 m) |
| Kutup/Traktör/y3/iyi/s0 | 3915 | 22° | 1152 | 656 | dik iniş (3720-3760 m), çukur (3765-3773 m), dik yokuş (3800-3810 m), çukur (3821-3828 m) |
| Kır yolu/Cip/y0/iyi/s0 | 3196 | -13° | 174 | 1162 | tümsekler (3004-3025 m), dik iniş (3050-3060 m), çukur (3074-3082 m) |
| Kır yolu/Cip/y3/iyi/s0 | 4620 | 17° | 432 | 1323 | dik iniş (4420-4460 m), çukur (4473-4482 m), rampa (4556-4562 m) |
| Kır yolu/Motosiklet/y0/iyi/s0 | 4933 | -13° | 94 | 1635 | çukur (4762-4771 m), dik iniş (4850-4880 m), çukur (4886-4896 m) |
| Kır yolu/Motosiklet/y3/iyi/s0 | 6904 | -2° | 174 | 1934 | çukur (6725-6734 m), rampa (6800-6804 m), çukur (6859-6867 m) |
| Kır yolu/Pikap/y0/iyi/s0 | 4933 | -18° | 96 | 1637 | çukur (4762-4771 m), dik iniş (4850-4880 m), çukur (4886-4896 m) |
| Kır yolu/Pikap/y3/iyi/s0 | 6924 | 7° | 186 | 1955 | dik iniş (6650-6710 m), çukur (6725-6734 m), rampa (6800-6804 m) |
| Kır yolu/Traktör/y0/iyi/s0 | 3867 | -6° | 1177 | 571 | dik iniş (3680-3710 m), çukur (3725-3733 m), rampa (3819-3824 m) |
| Kır yolu/Traktör/y3/iyi/s0 | 6384 | -5° | 710 | 1415 | rampa (6220-6224 m), çukur (6325-6331 m) |
| Mağara/Cip/y0/iyi/s0 | 2428 | 22° | 1060 | 344 | dik iniş (2230-2250 m), çukur (2261-2270 m), dik iniş (2290-2320 m), çukur (2328-2338 m), dik yokuş (2350-2360 m) |
| Mağara/Cip/y3/iyi/s0 | 3366 | 17° | 62 | 1282 | çukur (3260-3270 m), dik yokuş (3290-3330 m), rampa (3344-3349 m) |
| Mağara/Motosiklet/y0/iyi/s0 | 4881 | -13° | 306 | 1478 | tümsekler (4659-4679 m), dik iniş (4690-4730 m), çukur (4738-4748 m), çukur (4794-4801 m) |
| Mağara/Motosiklet/y3/iyi/s0 | 7093 | -1° | 52 | 1998 | tümsekler (6982-7004 m), dik yokuş (7020-7060 m), rampa (7074-7079 m) |
| Mağara/Pikap/y0/iyi/s0 | 2426 | -13° | 1083 | 341 | dik yokuş (2180-2210 m), dik iniş (2230-2250 m), çukur (2261-2270 m), dik iniş (2290-2320 m), çukur (2328-2338 m) |
| Mağara/Pikap/y3/iyi/s0 | 4052 | -5° | 1110 | 649 | tümsekler (3862-3881 m), dik iniş (3890-3910 m), rampa (3947-3951 m), çukur (3997-4004 m) |
| Mağara/Traktör/y0/iyi/s0 | 1475 | 5° | 667 | 296 | çukur (1328-1335 m), dik iniş (1360-1390 m) |
| Mağara/Traktör/y3/iyi/s0 | 2427 | -5° | 1071 | 343 | dik iniş (2230-2250 m), çukur (2261-2270 m), dik iniş (2290-2320 m), çukur (2328-2338 m), dik yokuş (2350-2360 m) |
| Orman/Cip/y0/iyi/s0 | 2234 | -23° | 72 | 1047 | dik yokuş (2070-2080 m), rampa (2086-2091 m), dik iniş (2120-2130 m), çukur (2145-2153 m), dik iniş (2180-2190 m) |
| Orman/Cip/y3/iyi/s0 | 3580 | -10° | 228 | 1324 | dik iniş (3440-3450 m), çukur (3465-3474 m), dik iniş (3510-3530 m), çukur (3543-3550 m) |
| Orman/Motosiklet/y0/iyi/s0 | 5062 | -13° | 762 | 1299 | tümsekler (4931-4952 m), dik iniş (4970-5000 m), çukur (5015-5024 m) |
| Orman/Motosiklet/y3/iyi/s0 | 7627 | 13° | 630 | 1860 | dik iniş (7340-7410 m), çukur (7423-7432 m), çukur (7524-7531 m) |
| Orman/Pikap/y0/iyi/s0 | 3665 | 29° | 177 | 1409 | çukur (3465-3474 m), dik iniş (3510-3530 m), çukur (3543-3550 m), dik yokuş (3570-3610 m) |
| Orman/Pikap/y3/iyi/s0 | 7638 | -5° | 624 | 1872 | dik iniş (7340-7410 m), çukur (7423-7432 m), çukur (7524-7531 m) |
| Orman/Traktör/y0/iyi/s0 | 2449 | -0° | 1353 | 193 | dik iniş (2320-2350 m), çukur (2358-2368 m), dik yokuş (2430-2460 m) |
| Orman/Traktör/y3/iyi/s0 | 5432 | -16° | 374 | 1670 | dik iniş (5240-5280 m), çukur (5292-5300 m), dik iniş (5360-5380 m), çukur (5395-5405 m) |
| Plaj/Cip/y0/iyi/s0 | 2785 | -19° | 425 | 1176 | çukur (2740-2748 m) |
| Plaj/Cip/y3/iyi/s0 | 3031 | -13° | 190 | 1422 | rampa (2845-2850 m), çukur (2906-2915 m) |
| Plaj/Motosiklet/y0/iyi/s0 | 3114 | 4° | 67 | 1505 | çukur (3060-3068 m) |
| Plaj/Motosiklet/y3/iyi/s0 | 5071 | -8° | 281 | 1923 | dik iniş (4970-4990 m), çukur (5003-5013 m) |
| Plaj/Pikap/y0/iyi/s0 | 4768 | -2° | 585 | 1621 | dik iniş (4600-4610 m), tümsekler (4652-4675 m) |
| Plaj/Pikap/y3/iyi/s0 | 7613 | -16° | 501 | 2320 | tümsekler (7469-7492 m), tümsekler (7562-7586 m) |
| Plaj/Traktör/y0/iyi/s0 | 3012 | -10° | 177 | 1403 | çukur (2906-2915 m) |
| Plaj/Traktör/y3/iyi/s0 | 5045 | 2° | 289 | 1898 | dik iniş (4970-4990 m), çukur (5003-5013 m) |
| Çöl/Cip/y0/iyi/s0 | 3083 | -7° | 655 | 897 | dik iniş (2920-2960 m), çukur (2967-2975 m) |
| Çöl/Cip/y3/iyi/s0 | 3463 | -8° | 317 | 1276 | dik iniş (3230-3240 m), çukur (3246-3254 m), dik iniş (3300-3320 m), çukur (3334-3344 m) |
| Çöl/Motosiklet/y0/iyi/s0 | 3391 | -16° | 329 | 1204 | dik iniş (3230-3240 m), çukur (3246-3254 m), dik iniş (3300-3320 m), çukur (3334-3344 m) |
| Çöl/Pikap/y0/iyi/s0 | 3486 | 13° | 285 | 1300 | dik iniş (3300-3320 m), çukur (3334-3344 m) |
| Çöl/Pikap/y3/iyi/s0 | 4609 | 7° | 1131 | 954 | tümsekler (4369-4399 m), dik iniş (4410-4430 m), çukur (4464-4470 m) |
| Çöl/Traktör/y0/iyi/s0 | 3448 | 22° | 296 | 1262 | çukur (3246-3254 m), dik iniş (3300-3320 m), çukur (3334-3344 m) |
| Çöl/Traktör/y3/iyi/s0 | 5398 | -14° | 273 | 1742 | dik iniş (5300-5350 m), çukur (5361-5371 m) |
| Şantiye/Cip/y0/iyi/s0 | 2942 | -5° | 406 | 1231 | rampa (2882-2886 m) |
| Şantiye/Cip/y3/iyi/s0 | 3239 | -13° | 126 | 1528 | çukur (3113-3123 m), çukur (3185-3195 m) |
| Şantiye/Motosiklet/y0/iyi/s0 | 3281 | 10° | 63 | 1570 | çukur (3185-3195 m), rampa (3242-3246 m) |
| Şantiye/Motosiklet/y3/iyi/s0 | 5236 | 40° | 313 | 1933 | çukur (5139-5147 m), rampa (5212-5217 m) |
| Şantiye/Pikap/y0/iyi/s0 | 4897 | 2° | 666 | 1595 | tümsekler (4789-4808 m) |
| Şantiye/Pikap/y3/iyi/s0 | 7582 | -10° | 821 | 2062 | dik yokuş (7400-7470 m), rampa (7484-7489 m) |
| Şantiye/Traktör/y0/iyi/s0 | 3222 | -12° | 119 | 1512 | çukur (3113-3123 m), çukur (3185-3195 m) |
| Şantiye/Traktör/y3/iyi/s0 | 5164 | -3° | 384 | 1862 | çukur (5028-5038 m), çukur (5139-5147 m) |

## 6. Yakıt: menzil ve bidon aralığı

Menzil = depo / ölçülen yakıt harcaması (m başına). Bu değer tam gazla ve yokuşlarla bu sürücünün gerçek ortalamasıdır; bidon aralığı menzilin çok altında olmalı.

| Harita (jeep, y0) | Haritadaki bidon aralığı (m) | Menzil (m) | Aralık/menzil | En uzun gerçek aralık (m) | En düşük yakıt (alırken) |
|---|---|---|---|---|---|
| Kır yolu | 450 | 1074 | 42% | 919 | 11% |
| Çöl | 500 | 970 | 52% | 1006 | 0% |
| Kutup | 450 | 828 | 54% | 867 | 1% |
| Ay | 500 | 1001 | 50% | 756 | 12% |
| Orman | 500 | 1030 | 49% | 473 | 54% |
| Dağ | 400 | 1030 | 39% | 734 | 27% |
| Plaj | 600 | 1087 | 55% | 783 | 25% |
| Mağara | 450 | 798 | 56% | 906 | 11% |
| Bataklık | 550 | 1056 | 52% | 651 | 34% |
| Şantiye | 600 | 1112 | 54% | 846 | 20% |

## 7. En çok yakıt harcatan yokuşlar (jeep, geliştirme yok)

| Harita | x aralığı (m) | Uzunluk (m) | Yükseklik (m) | En dik eğim | Depo payı |
|---|---|---|---|---|---|
| Kır yolu | 2865-3010 | 145 | 36.0 | 25° | 17% |
| Kır yolu | 2590-2720 | 130 | 33.4 | 25° | 14% |
| Kır yolu | 2325-2450 | 125 | 27.7 | 27° | 14% |
| Çöl | 2705-2900 | 195 | 36.3 | 32° | 27% |
| Çöl | 2200-2325 | 125 | 30.7 | 35° | 19% |
| Çöl | 2445-2570 | 125 | 37.5 | 28° | 17% |
| Kutup | 2270-2390 | 120 | 37.3 | 30° | 22% |
| Kutup | 1565-1710 | 145 | 22.8 | 29° | 19% |
| Kutup | 2030-2145 | 115 | 34.6 | 27° | 16% |
| Ay | 1730-1825 | 95 | 20.9 | 27° | 18% |
| Ay | 1930-2025 | 95 | 23.0 | 34° | 14% |
| Ay | 1405-1490 | 85 | 17.7 | 29° | 10% |
| Orman | 2010-2120 | 110 | 26.8 | 41° | 13% |
| Orman | 1510-1595 | 85 | 22.5 | 32° | 10% |
| Orman | 1685-1765 | 80 | 26.1 | 36° | 10% |
| Dağ | 2275-2395 | 120 | 37.6 | 43° | 17% |
| Dağ | 1880-2000 | 120 | 30.8 | 47° | 16% |
| Dağ | 2525-2625 | 100 | 45.9 | 42° | 16% |
| Plaj | 2300-2415 | 115 | 15.5 | 27° | 12% |
| Plaj | 2515-2630 | 115 | 19.1 | 19° | 12% |
| Plaj | 2130-2235 | 105 | 14.5 | 20° | 11% |
| Mağara | 1840-1995 | 155 | 36.2 | 37° | 18% |
| Mağara | 1400-1540 | 140 | 29.3 | 38° | 16% |
| Mağara | 2105-2220 | 115 | 45.9 | 36° | 15% |
| Bataklık | 1800-1940 | 140 | 22.8 | 27° | 15% |
| Bataklık | 2315-2435 | 120 | 27.5 | 28° | 14% |
| Bataklık | 2045-2150 | 105 | 26.8 | 25° | 14% |
| Şantiye | 2320-2490 | 170 | 22.9 | 24° | 18% |
| Şantiye | 1840-1990 | 150 | 18.7 | 23° | 15% |
| Şantiye | 2610-2735 | 125 | 28.4 | 32° | 15% |

## 8. Duyurular ve karıştırıcı

Mixer.voiceCount en yüksek: **15** (sınır 60). Koşu başına duyuru: ortalama 87.1; en uzun sessizlik: 27 sn (Kutup/Traktör/y3/iyi/s0).

| Koşu | En uzun sessizlik (sn) | Duyuru sayısı | Süre (sn) |
|---|---|---|---|
| Kutup/Traktör/y3/iyi/s0 | 27 | 92 | 524 |
| Ay/Motosiklet/y0/iyi/s0 | 27 | 54 | 319 |
| Dağ/Traktör/y3/iyi/s0 | 26 | 74 | 369 |
| Kır yolu/Traktör/y3/iyi/s0 | 26 | 122 | 705 |
| Bataklık/Pikap/y3/iyi/s0 | 26 | 96 | 492 |
| Ay/Motosiklet/y3/iyi/s0 | 26 | 84 | 455 |

## 9. Ekonomi

Sürücü yakıt bitene ya da 20 dk dolana kadar oynadığı için koşu başı altın gerçek bir oyuncunun kısa koşularından çok daha yüksek çıkar; bu yüzden altın/dakika da verilir. "Koşu" = tam bir koşu (uzun), "dk" = dakika cinsinden oyun süresi.

| Harita | Araç | Geliştirme | Koşu başı altın | Süre (sn) | Altın/dk | Sıradaki araç | Sıradaki harita |
|---|---|---|---|---|---|---|---|
| Kır yolu | Cip | y0 | 1765 | 218 | 486 | Motosiklet (8000): 5 koşu, 17 dk | Çöl (5000): 3 koşu, 11 dk |
| Kır yolu | Cip | y3 | 2695 | 314 | 515 | Motosiklet (8000): 3 koşu, 16 dk | Çöl (5000): 2 koşu, 10 dk |
| Kır yolu | Motosiklet | y0 | 3110 | 310 | 602 | Pikap (15000): 5 koşu, 25 dk | Çöl (5000): 2 koşu, 9 dk |
| Kır yolu | Motosiklet | y3 | 6055 | 424 | 856 | Pikap (15000): 3 koşu, 18 dk | Çöl (5000): 1 koşu, 6 dk |
| Kır yolu | Pikap | y0 | 3110 | 374 | 499 | Traktör (25000): 9 koşu, 51 dk | Çöl (5000): 2 koşu, 11 dk |
| Kır yolu | Pikap | y3 | 5955 | 515 | 693 | Traktör (25000): 5 koşu, 37 dk | Çöl (5000): 1 koşu, 8 dk |
| Kır yolu | Traktör | y0 | 2125 | 486 | 262 | Kum arabası (35000): 17 koşu, 134 dk | Çöl (5000): 3 koşu, 20 dk |
| Kır yolu | Traktör | y3 | 5360 | 705 | 456 | Kum arabası (35000): 7 koşu, 77 dk | Çöl (5000): 1 koşu, 11 dk |
| Çöl | Cip | y0 | 2135 | 227 | 563 | Motosiklet (8000): 4 koşu, 15 dk | Kutup (12000): 6 koşu, 22 dk |
| Çöl | Cip | y3 | 2575 | 243 | 635 | Motosiklet (8000): 4 koşu, 13 dk | Kutup (12000): 5 koşu, 19 dk |
| Çöl | Motosiklet | y0 | 2510 | 241 | 625 | Pikap (15000): 6 koşu, 24 dk | Kutup (12000): 5 koşu, 20 dk |
| Çöl | Motosiklet | y3 | 4560 | 351 | 779 | Pikap (15000): 4 koşu, 20 dk | Kutup (12000): 3 koşu, 16 dk |
| Çöl | Pikap | y0 | 2575 | 279 | 555 | Traktör (25000): 10 koşu, 46 dk | Kutup (12000): 5 koşu, 22 dk |
| Çöl | Pikap | y3 | 3445 | 418 | 494 | Traktör (25000): 8 koşu, 51 dk | Kutup (12000): 4 koşu, 25 dk |
| Çöl | Traktör | y0 | 2575 | 372 | 416 | Kum arabası (35000): 14 koşu, 85 dk | Kutup (12000): 5 koşu, 29 dk |
| Çöl | Traktör | y3 | 4505 | 580 | 466 | Kum arabası (35000): 8 koşu, 76 dk | Kutup (12000): 3 koşu, 26 dk |
| Kutup | Cip | y0 | 1815 | 217 | 501 | Motosiklet (8000): 5 koşu, 16 dk | Ay (20000): 12 koşu, 40 dk |
| Kutup | Cip | y3 | 1815 | 227 | 479 | Motosiklet (8000): 5 koşu, 17 dk | Ay (20000): 12 koşu, 42 dk |
| Kutup | Motosiklet | y0 | 1750 | 221 | 474 | Pikap (15000): 9 koşu, 32 dk | Ay (20000): 12 koşu, 43 dk |
| Kutup | Motosiklet | y3 | 2055 | 242 | 509 | Pikap (15000): 8 koşu, 30 dk | Ay (20000): 10 koşu, 40 dk |
| Kutup | Pikap | y0 | 1815 | 269 | 405 | Traktör (25000): 14 koşu, 62 dk | Ay (20000): 12 koşu, 50 dk |
| Kutup | Pikap | y3 | 1815 | 295 | 369 | Traktör (25000): 14 koşu, 68 dk | Ay (20000): 12 koşu, 55 dk |
| Kutup | Traktör | y0 | 2055 | 351 | 352 | Kum arabası (35000): 18 koşu, 100 dk | Ay (20000): 10 koşu, 57 dk |
| Kutup | Traktör | y3 | 2770 | 524 | 317 | Kum arabası (35000): 13 koşu, 111 dk | Ay (20000): 8 koşu, 64 dk |
| Ay | Cip | y0 | 3380 | 248 | 816 | Motosiklet (8000): 3 koşu, 10 dk | Orman (30000): 9 koşu, 37 dk |
| Ay | Cip | y3 | 1785 | 123 | 873 | Motosiklet (8000): 5 koşu, 10 dk | Orman (30000): 17 koşu, 35 dk |
| Ay | Motosiklet | y0 | 2225 | 319 | 419 | Pikap (15000): 7 koşu, 36 dk | Orman (30000): 14 koşu, 72 dk |
| Ay | Motosiklet | y3 | 3480 | 455 | 459 | Pikap (15000): 5 koşu, 33 dk | Orman (30000): 9 koşu, 66 dk |
| Ay | Pikap | y0 | 10155 | 573 | 1064 | Traktör (25000): 3 koşu, 24 dk | Orman (30000): 3 koşu, 29 dk |
| Ay | Pikap | y3 | 13230 | 626 | 1268 | Traktör (25000): 2 koşu, 20 dk | Orman (30000): 3 koşu, 24 dk |
| Ay | Traktör | y0 | 9470 | 668 | 850 | Kum arabası (35000): 4 koşu, 42 dk | Orman (30000): 4 koşu, 36 dk |
| Ay | Traktör | y3 | 20825 | 955 | 1309 | Kum arabası (35000): 2 koşu, 27 dk | Orman (30000): 2 koşu, 23 dk |
| Orman | Cip | y0 | 2640 | 162 | 976 | Motosiklet (8000): 4 koşu, 9 dk | Dağ (40000): 16 koşu, 41 dk |
| Orman | Cip | y3 | 6625 | 254 | 1565 | Motosiklet (8000): 2 koşu, 6 dk | Dağ (40000): 7 koşu, 26 dk |
| Orman | Motosiklet | y0 | 10615 | 348 | 1830 | Pikap (15000): 2 koşu, 9 dk | Dağ (40000): 4 koşu, 22 dk |
| Orman | Motosiklet | y3 | 21155 | 503 | 2523 | Pikap (15000): 1 koşu, 6 dk | Dağ (40000): 2 koşu, 16 dk |
| Orman | Pikap | y0 | 6625 | 294 | 1354 | Traktör (25000): 4 koşu, 19 dk | Dağ (40000): 7 koşu, 30 dk |
| Orman | Pikap | y3 | 20935 | 599 | 2099 | Traktör (25000): 2 koşu, 12 dk | Dağ (40000): 2 koşu, 20 dk |
| Orman | Traktör | y0 | 2825 | 388 | 437 | Kum arabası (35000): 13 koşu, 81 dk | Dağ (40000): 15 koşu, 92 dk |
| Orman | Traktör | y3 | 11860 | 619 | 1150 | Kum arabası (35000): 3 koşu, 31 dk | Dağ (40000): 4 koşu, 35 dk |
| Dağ | Cip | y0 | 3810 | 214 | 1071 | Motosiklet (8000): 3 koşu, 8 dk | Plaj (50000): 14 koşu, 47 dk |
| Dağ | Cip | y3 | 7535 | 312 | 1449 | Motosiklet (8000): 2 koşu, 6 dk | Plaj (50000): 7 koşu, 35 dk |
| Dağ | Motosiklet | y0 | 13690 | 420 | 1955 | Pikap (15000): 2 koşu, 8 dk | Plaj (50000): 4 koşu, 26 dk |
| Dağ | Motosiklet | y3 | 29130 | 681 | 2565 | Pikap (15000): 1 koşu, 6 dk | Plaj (50000): 2 koşu, 20 dk |
| Dağ | Pikap | y0 | 3370 | 254 | 795 | Traktör (25000): 8 koşu, 32 dk | Plaj (50000): 15 koşu, 63 dk |
| Dağ | Pikap | y3 | 4825 | 372 | 777 | Traktör (25000): 6 koşu, 33 dk | Plaj (50000): 11 koşu, 65 dk |
| Dağ | Traktör | y0 | 3095 | 340 | 546 | Kum arabası (35000): 12 koşu, 65 dk | Plaj (50000): 17 koşu, 92 dk |
| Dağ | Traktör | y3 | 3810 | 369 | 619 | Kum arabası (35000): 10 koşu, 57 dk | Plaj (50000): 14 koşu, 81 dk |
| Plaj | Cip | y0 | 5275 | 192 | 1647 | Motosiklet (8000): 2 koşu, 5 dk | Mağara (60000): 12 koşu, 37 dk |
| Plaj | Cip | y3 | 6755 | 206 | 1969 | Motosiklet (8000): 2 koşu, 5 dk | Mağara (60000): 9 koşu, 31 dk |
| Plaj | Motosiklet | y0 | 7530 | 193 | 2335 | Pikap (15000): 2 koşu, 7 dk | Mağara (60000): 8 koşu, 26 dk |
| Plaj | Motosiklet | y3 | 13325 | 310 | 2578 | Pikap (15000): 2 koşu, 6 dk | Mağara (60000): 5 koşu, 24 dk |
| Plaj | Pikap | y0 | 12000 | 349 | 2064 | Traktör (25000): 3 koşu, 13 dk | Mağara (60000): 5 koşu, 30 dk |
| Plaj | Pikap | y3 | 22190 | 556 | 2394 | Traktör (25000): 2 koşu, 11 dk | Mağara (60000): 3 koşu, 26 dk |
| Plaj | Traktör | y0 | 6755 | 314 | 1292 | Kum arabası (35000): 6 koşu, 28 dk | Mağara (60000): 9 koşu, 47 dk |
| Plaj | Traktör | y3 | 13425 | 523 | 1539 | Kum arabası (35000): 3 koşu, 23 dk | Mağara (60000): 5 koşu, 39 dk |
| Mağara | Cip | y0 | 5820 | 214 | 1630 | Motosiklet (8000): 2 koşu, 5 dk | Bataklık (70000): 13 koşu, 43 dk |
| Mağara | Cip | y3 | 9660 | 245 | 2368 | Motosiklet (8000): 1 koşu, 4 dk | Bataklık (70000): 8 koşu, 30 dk |
| Mağara | Motosiklet | y0 | 14105 | 336 | 2520 | Pikap (15000): 2 koşu, 6 dk | Bataklık (70000): 5 koşu, 28 dk |
| Mağara | Motosiklet | y3 | 23480 | 473 | 2979 | Pikap (15000): 1 koşu, 6 dk | Bataklık (70000): 3 koşu, 24 dk |
| Mağara | Pikap | y0 | 5820 | 272 | 1284 | Traktör (25000): 5 koşu, 20 dk | Bataklık (70000): 13 koşu, 55 dk |
| Mağara | Pikap | y3 | 11790 | 416 | 1700 | Traktör (25000): 3 koşu, 15 dk | Bataklık (70000): 6 koşu, 42 dk |
| Mağara | Traktör | y0 | 3200 | 265 | 725 | Kum arabası (35000): 11 koşu, 49 dk | Bataklık (70000): 22 koşu, 97 dk |
| Mağara | Traktör | y3 | 5820 | 401 | 870 | Kum arabası (35000): 7 koşu, 41 dk | Bataklık (70000): 13 koşu, 81 dk |
| Bataklık | Cip | y0 | 6560 | 193 | 2039 | Motosiklet (8000): 2 koşu, 4 dk | Şantiye (90000): 14 koşu, 45 dk |
| Bataklık | Cip | y3 | 10690 | 283 | 2264 | Motosiklet (8000): 1 koşu, 4 dk | Şantiye (90000): 9 koşu, 40 dk |
| Bataklık | Motosiklet | y0 | 8815 | 272 | 1945 | Pikap (15000): 2 koşu, 8 dk | Şantiye (90000): 11 koşu, 47 dk |
| Bataklık | Motosiklet | y3 | 12420 | 292 | 2555 | Pikap (15000): 2 koşu, 6 dk | Şantiye (90000): 8 koşu, 36 dk |
| Bataklık | Pikap | y0 | 10390 | 324 | 1926 | Traktör (25000): 3 koşu, 13 dk | Şantiye (90000): 9 koşu, 47 dk |
| Bataklık | Pikap | y3 | 13175 | 492 | 1608 | Traktör (25000): 2 koşu, 16 dk | Şantiye (90000): 7 koşu, 56 dk |
| Bataklık | Traktör | y0 | 10340 | 431 | 1440 | Kum arabası (35000): 4 koşu, 25 dk | Şantiye (90000): 9 koşu, 63 dk |
| Bataklık | Traktör | y3 | 12195 | 478 | 1532 | Kum arabası (35000): 3 koşu, 23 dk | Şantiye (90000): 8 koşu, 59 dk |
| Şantiye | Cip | y0 | 10180 | 207 | 2947 | Motosiklet (8000): 1 koşu, 3 dk | - |
| Şantiye | Cip | y3 | 12030 | 224 | 3223 | Motosiklet (8000): 1 koşu, 3 dk | - |
| Şantiye | Motosiklet | y0 | 12130 | 204 | 3572 | Pikap (15000): 2 koşu, 5 dk | - |
| Şantiye | Motosiklet | y3 | 20205 | 327 | 3709 | Pikap (15000): 1 koşu, 5 dk | - |
| Şantiye | Pikap | y0 | 18680 | 370 | 3027 | Traktör (25000): 2 koşu, 9 dk | - |
| Şantiye | Pikap | y3 | 27105 | 568 | 2862 | Traktör (25000): 1 koşu, 9 dk | - |
| Şantiye | Traktör | y0 | 12230 | 346 | 2124 | Kum arabası (35000): 3 koşu, 17 dk | - |
| Şantiye | Traktör | y3 | 19630 | 544 | 2164 | Kum arabası (35000): 2 koşu, 17 dk | - |

Kır yolu + jeep ortalaması (6 koşu): 1765 altın/koşu. Jeep için 4 geliştirmenin 3. seviyeye maliyeti: 8256 altın = 5 koşu; motosiklet (8000): 5 koşu; Çöl haritası (5000): 3 koşu.

**İlerleme özeti (kapılarla aynı hesap):** Kır yolu + jeep (geliştirme yok) iyi koşu ortalaması 1765 altın, medyan koşu süresi 3.6 dk. Motosiklet (8000) = 4.5 koşu, Çöl (5000) = 2.8 koşu. Matristeki tüm iyi sürücü koşularının ortalama hızı 1370 altın/dk; tüm araçlar ve haritalar (1350000 altın) ≈ 16.4 saat. Hedef: motosiklet 4-6 koşu, Çöl 2-3 koşu, tüm mağaza 15-25 saat.

Tüm araçlar ve haritalar toplam 1350000 altın. Kır yolu + jeep hızıyla (486 altın/dk) hepsini almak ≈ 46 saat oyun sürer. Motosiklet gibi devrilgen araçlarla altın/dk 3-5 kat düşer (bkz. tablo).

## 10. Sorunlar (ciddiyet sırasıyla)

1. **[Yüksek]** İlk 1500 m içinde biten 2 koşu: Ay/Cip/y3/iyi/s0 1266 m CRASH (eğim 19°); Mağara/Traktör/y0/iyi/s0 1475 m FUEL (eğim 5°).
2. **[Yüksek]** Yerde göreli açı 100°'yi geçen (ters dönme) olay 3. En sık: Ay x≈2200 m (eğim -18°, açı 97°, 1 kez); Ay x≈1200 m (eğim -1°, açı -108°, 1 kez); Orman x≈5700 m (eğim -9°, açı -118°, 1 kez).
3. **[Orta]** Takılma 6 kez görüldü. En sık noktalar: Ay x≈2550 m (eğim 34°, 2 kez); Kır yolu x≈6350 m (eğim 28°, 1 kez); Kutup x≈2550 m (eğim 20°, 1 kez); Kutup x≈3850 m (eğim 27°, 1 kez); Bataklık x≈4650 m (eğim 21°, 1 kez).
4. **[Orta]** Bidona %5'ten az yakıtla ulaşılan 23 koşu var (marj çok dar): Kır yolu/Cip/y3/iyi/s0; Kır yolu/Traktör/y0/iyi/s0; Kır yolu/Traktör/y3/iyi/s0; Çöl/Cip/y0/iyi/s0; Kutup/Cip/y0/iyi/s0.
5. **[Düşük]** (Tasarım gereği: bidon aralığı mesafeyle uzadığı için iyi koşular çoğunlukla yakıtla biter.) Yakıt bitişiyle sonlanan koşu: 95/97. Haritalara göre: Kır yolu 23, Çöl 7, Kutup 9, Ay 8, Orman 8, Dağ 8, Plaj 8, Mağara 8, Bataklık 8, Şantiye 8. Bunların 18 tanesi bir sonraki bidona 150 m'den az kala bitiyor (menzil marjı çok dar), 5 tanesinde bitişten önce yamaçta takılma olayı var (yani yakıt, yamaçta çıkamayıp beklerken yanmış). "Yakıt bitti" duyurusu oyuncuya nedeni söylemez.

## 11. Ayar önerileri

- Ay x≈2550 m: 2 takılma/ters olayı (Motosiklet), zemin eğimi 34°. Bu yamacın en dik kısmını 26° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Kır yolu x≈6350 m: 1 takılma/ters olayı (Traktör), zemin eğimi 28°. Bu yamacın en dik kısmını 20° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Kutup x≈2550 m: 1 takılma/ters olayı (Pikap), zemin eğimi 20°. Bu yamacın en dik kısmını 12° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Kutup x≈3850 m: 1 takılma/ters olayı (Traktör), zemin eğimi 27°. Bu yamacın en dik kısmını 19° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Ay x≈2250 m: 1 takılma/ters olayı (Cip), zemin eğimi -18°. Bu yamacın en dik kısmını 10° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Ay x≈1250 m: 1 takılma/ters olayı (Cip); olay noktasında zemin eğimi düşük (-1°) ama önceki 60 m'de en dik yamaç 18°: araç yamaçta burnunu çok kaldırıp düzlükte arkaya devriliyor (gaz-burun yukarı). Yakındaki özellikler: dik yokuş (1140-1160 m), rampa (1193-1198 m). Denge yardımının eşiğini (40°) ve gaz kısma eğrisini bu parçada sınayın; yamaç sonu eğimini yumuşatın.
- Kutup: 8 koşu yakıtla bitiyor, 2 tanesinde önce yamaçta takılma var (takılma eğimleri 20-27°, mesafeler 2624, 3915 m). Asıl sorun yakıt değil tırmanış: yüzeyin tutunması (kar, μ=0.45) bu eğim için yetersiz; 20°'nin üstündeki yamaçları azaltın, ya da bu yüzeyin μ değerini/araç torkunu artırın. Takılınca yakıt boşa yanıyor: "yokuşu çıkamıyorsun" ipucu ve geri vites daha erken devreye girmeli.
- Ay: 6 koşu takılmadan yakıtla bitiyor (mesafeler 1942, 2340, 5380, 5425, 6164, 7970 m); 2 km'den kısa olanlar için ilk bidon aralıkları (`fuelEvery`=500 m, `Terrain.fuelGapAt`) bu araçların menziline göre geniş.
- Dağ: 8 koşu takılmadan yakıtla bitiyor (mesafeler 1932, 2347, 2788, 2815, 3339, 4184, 5654, 9642 m); 2 km'den kısa olanlar için ilk bidon aralıkları (`fuelEvery`=400 m, `Terrain.fuelGapAt`) bu araçların menziline göre geniş.
- Mağara: 8 koşu takılmadan yakıtla bitiyor (mesafeler 1475, 2426, 2427, 2428, 3366, 4052, 4881, 7093 m); 2 km'den kısa olanlar için ilk bidon aralıkları (`fuelEvery`=450 m, `Terrain.fuelGapAt`) bu araçların menziline göre geniş.

## 12. Karşılaştırma: düzeltmeden önceki çekirdek (commit d417faf)

Bu bölüm sabit metindir (aynı test, eski çekirdekle bir kez çalıştırıldı). Güncel sonuçlar yukarıdadır.

- 90 koşunun 25'inde hata dedektörü tetiklendi; test FAIL ile çıktı.
- `sırtüstü` (araç yerde göreli açı > 100° iken 3 sn'den uzun süre oyun bitmedi): 43 olay, 25 koşuda. Örnek: Kır yolu/Motosiklet/y0, x=1944 m, göreli açı 109°. Bu, kullanıcının bildirdiği "burun 124°'ye kalktı, yakıt bitti" durumunun birebir aynısıdır: Kır yolu motosiklet 1889 m'de, tepe açı 125°, oyun CRASH ile değil "yakıt bitti" ile bitti.
- `tiltDeg>180`: 1 koşu (Ay/Motosiklet/y3, -181°; ham tepe değeri 494°). `tiltDeg()` sarılmıyordu.
- `TAKILI`: Ay/Traktör/y3, 20 dk sonunda araç duruyor, yakıt var, oyun bitmiyor (kaçırılan bir bitiş koşulu).
- NaN ve voiceCount dedektörleri hiç tetiklenmedi.
- Kır yolu + jeep (iyi sürücü) eski çekirdekte de 5/5 tohumda ~16.2 km gidip 20 dk sınırına ulaştı: ilk haritadaki yokuş, dikkatli sürücü için sorun değil. Sorun, gazı kesmeyen/yalnızca bipe tepki veren oyuncuda (acemi sürücü: 1546 m'de ters dönüp CRASH).

## 13. Karşılaştırma: F1-5 dengelemesinden önce (commit 6c82840)

Bu bölüm sabit metindir (aynı test, F1-5 öncesi çekirdekle). Güncel değerler 0. bölümdedir.

| Kapı | Önce | Sonra (bkz. 0. bölüm) |
|---|---|---|
| Acemi jeep y0 Kır yolu: ortalama mesafe | 4026 m (hepsi CRASH, 3.2-5.3 km) | yakıtla biter, kaza yok |
| Acemi: 3. bidon / ilk 1000 m'de CRASH | 5/5 / yok | 5/5 / yok |
| İyi sürücü jeep y0: 15 dk'dan önce doğal bitiş | 1/6 (4 koşu 20 dk sınırında, biri 17 dk) | 6/6 |
| İyi sürücü jeep y0: medyan mesafe | 16049 m | ~3.2 km |
| Harita asıl aracı y3 ≥ 1000 m | 10/10 (Kır yolu, Orman, Mağara, Dağ 20 dk sınırında) | 10/10, hepsi doğal bitiş |
| Motosiklet y0 Kır yolu medyan | 3568 m, 6 koşunun 6'sı CRASH; matriste 20 koşunun 16'sı CRASH | ~4.9 km, yakıtla |
| Ekonomi: altın/koşu (Kır yolu jeep y0) | 124703 (motosiklet < 1 koşu) | ~1750 (motosiklet 4.5 koşu, Çöl 2.8 koşu) |
| Ekonomi: tüm mağaza | ≈ 3.2 saat (6936 altın/dk) | ≈ 16 saat |
| En uzun sessizlik | 135 sn | < 30 sn |
