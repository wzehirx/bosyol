# Yokuş Ustası: oyun testi raporu

Bu rapor `bash android/test/run.sh PlayTest` tarafından otomatik üretilir (elle düzenlemeyin, yeniden çalıştırınca yazılır). Sürücü `BlindDriver`: yalnızca sesle algılanabilen bilgilere bakar (eğim açısı, 10-15 m ilerideki radar eğimi, duyurular, havada olma, yakıt uyarıları, hız), 0.25 sn tepki gecikmesi ve küçük gürültüyle oynar. Strateji: tam gaz, burun zemin eğiminden 35° fazla kalkarsa gazı kes, havada burnu iniş eğimine yatır, takılırsa kısa geri git. "Acemi" sürücü yalnızca takla bipi (>70°) çalınca gazı keser.

- Koşu sayısı: 97 (matris 80, tohum 15); oyun zamanı sınırı 20 dk; gerçek süre 75 sn.
- Hata dedektörü tetiklenen koşu: 0

## 0. Kabul kapıları (PLAN2 F1-5)

| Kapı | Sonuç | Değer |
|---|---|---|
| Acemi, jeep y0, Kır yolu: ortalama mesafe ≥ 1500 m | GEÇTİ | 3197 m (3197, 3197, 3197, 3197, 3197) |
| Acemi: en az 4/5 koşu 3. bidona ulaşır | GEÇTİ | 5/5 |
| Acemi: ilk 1000 m'de CRASH yok | GEÇTİ | yok |
| İyi sürücü, jeep y0, Kır yolu: her koşu 15 dk'dan önce doğal biter | GEÇTİ | 6/6 (FUEL 3.6 dk, FUEL 3.6 dk, FUEL 3.6 dk, FUEL 3.7 dk, FUEL 3.6 dk, FUEL 3.6 dk) |
| İyi sürücü: medyan mesafe 2500-8000 m | GEÇTİ | 3197 m (3196, 3196, 3197, 3196, 3197, 3197) |
| Kır yolu: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 4659 m FUEL |
| Çöl: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3766 m FUEL |
| Kutup: asıl araç (Cip ya da Kar motosikleti) y3 ≥ 1000 m | GEÇTİ | Cip 2614 m FUEL, Kar motosikleti 8965 m FUEL |
| Ay: asıl araç (Cip ya da Ay aracı) y3 ≥ 1000 m | GEÇTİ | Cip 2569 m FUEL, Ay aracı 7459 m FUEL |
| Orman: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3653 m FUEL |
| Dağ: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 4185 m FUEL |
| Plaj: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3272 m FUEL |
| Mağara: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 4480 m FUEL |
| Bataklık: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3901 m FUEL |
| Şantiye: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3056 m FUEL |
| Motosiklet y0, Kır yolu: medyan ≥ 1500 m | GEÇTİ | 4931 m (4931 FUEL, 4931 FUEL, 4931 FUEL, 4930 FUEL, 4932 FUEL, 4931 FUEL) |
| Ekonomi (bilgi, kapı değil; her altın 1 değerinde, S-C fiyatları yeniden ölçekleyecek): motosiklet (8000) eski hedef 4-6 koşu | GEÇTİ | 18.5 koşu (432 altın/koşu, medyan koşu 3.6 dk) |
| Ekonomi (bilgi, kapı değil): Çöl (5000) eski hedef 2-3 koşu | GEÇTİ | 11.6 koşu |
| Ekonomi (bilgi, kapı değil): tüm mağaza eski hedef 15-25 saat | GEÇTİ | 132.9 saat (1350000 altın, 169 altın/dk) |

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
| Kır yolu | Cip | 3196 | FUEL | 218 | 4 | 11% | 51° | 0 | 432 | 0 | 0 |
| Kır yolu | Motosiklet | 4931 | FUEL | 306 | 5 | 15% | 81° | 0 | 692 | 0 | 0 |
| Kır yolu | Pikap | 4933 | FUEL | 374 | 5 | 17% | 47° | 0 | 698 | 0 | 0 |
| Kır yolu | Traktör | 3867 | FUEL | 491 | 5 | 2% | 51° | 0 | 526 | 0 | 0 |
| Çöl | Cip | 2249 | FUEL | 164 | 3 | 49% | 39° | 0 | 319 | 0 | 0 |
| Çöl | Motosiklet | 3759 | FUEL | 269 | 4 | 21% | 73° | 0 | 536 | 0 | 0 |
| Çöl | Pikap | 3486 | FUEL | 298 | 4 | 25% | 47° | 0 | 501 | 0 | 0 |
| Çöl | Traktör | 3465 | FUEL | 398 | 4 | 12% | 49° | 0 | 501 | 0 | 0 |
| Kutup | Cip | 2071 | FUEL | 160 | 3 | 51% | 39° | 0 | 286 | 0 | 0 |
| Kutup | Motosiklet | 2360 | FUEL | 228 | 4 | 24% | 64° | 0 | 320 | 0 | 0 |
| Kutup | Pikap | 2612 | FUEL | 277 | 4 | 36% | 46° | 0 | 356 | 0 | 0 |
| Kutup | Traktör | 2881 | FUEL | 363 | 4 | 29% | 48° | 0 | 390 | 0 | 0 |
| Ay | Cip | 1803 | FUEL | 191 | 1 | 15% | 54° | 0 | 2007 | 1800 | 0 |
| Ay | Motosiklet | 1936 | FUEL | 326 | 3 | 45% | 94° | 0 | 1349 | 1100 | 0 |
| Ay | Pikap | 1798 | FUEL | 191 | 0 | - | 45° | 0 | 1945 | 1750 | 0 |
| Ay | Traktör | 5357 | FUEL | 665 | 5 | 7% | 55° | 0 | 5130 | 4400 | 0 |
| Orman | Cip | 2268 | FUEL | 167 | 3 | 48% | 44° | 0 | 336 | 0 | 0 |
| Orman | Motosiklet | 3863 | FUEL | 258 | 4 | 29% | 74° | 0 | 728 | 150 | 0 |
| Orman | Pikap | 3862 | FUEL | 307 | 4 | 27% | 47° | 0 | 579 | 0 | 0 |
| Orman | Traktör | 2449 | FUEL | 396 | 4 | 11% | 46° | 0 | 365 | 0 | 1 |
| Dağ | Cip | 2790 | FUEL | 213 | 4 | 24% | 56° | 0 | 478 | 50 | 0 |
| Dağ | Motosiklet | 4248 | FUEL | 321 | 5 | 14% | 72° | 0 | 888 | 250 | 0 |
| Dağ | Pikap | 2347 | FUEL | 259 | 4 | 49% | 50° | 0 | 356 | 0 | 0 |
| Dağ | Traktör | 1932 | FUEL | 346 | 4 | 32% | 50° | 0 | 295 | 0 | 0 |
| Plaj | Cip | 2822 | FUEL | 195 | 3 | 17% | 40° | 0 | 418 | 0 | 0 |
| Plaj | Motosiklet | 3289 | FUEL | 279 | 4 | 0% | 58° | 0 | 496 | 0 | 1 |
| Plaj | Pikap | 4816 | FUEL | 353 | 4 | 5% | 48° | 0 | 770 | 0 | 0 |
| Plaj | Traktör | 3104 | FUEL | 325 | 3 | 38% | 45° | 0 | 472 | 0 | 0 |
| Mağara | Cip | 2428 | FUEL | 207 | 4 | 14% | 60° | 0 | 392 | 0 | 0 |
| Mağara | Motosiklet | 4529 | FUEL | 316 | 5 | 12% | 76° | 0 | 792 | 50 | 0 |
| Mağara | Pikap | 2426 | FUEL | 264 | 4 | 45% | 51° | 0 | 392 | 0 | 0 |
| Mağara | Traktör | 1475 | FUEL | 257 | 3 | 68% | 41° | 0 | 229 | 0 | 0 |
| Bataklık | Cip | 2390 | FUEL | 175 | 3 | 42% | 35° | 0 | 425 | 0 | 0 |
| Bataklık | Motosiklet | 3251 | FUEL | 259 | 4 | 7% | 73° | 0 | 591 | 0 | 0 |
| Bataklık | Pikap | 3942 | FUEL | 308 | 4 | 21% | 48° | 0 | 720 | 0 | 0 |
| Bataklık | Traktör | 3900 | FUEL | 413 | 4 | 8% | 50° | 0 | 710 | 0 | 0 |
| Şantiye | Cip | 2701 | FUEL | 189 | 3 | 24% | 45° | 0 | 466 | 0 | 0 |
| Şantiye | Motosiklet | 4771 | FUEL | 302 | 4 | 0% | 76° | 0 | 1082 | 250 | 0 |
| Şantiye | Pikap | 4792 | FUEL | 358 | 4 | 4% | 48° | 0 | 864 | 0 | 0 |
| Şantiye | Traktör | 3010 | FUEL | 320 | 3 | 42% | 47° | 0 | 528 | 0 | 0 |

## 3. Harita × araç, geliştirme hepsi 3. seviye (iyi sürücü)

| Harita | Araç | Mesafe (m) | Bitiş | Süre (sn) | Bidon | En az yakıt | Tepe açı | Takla | Altın | Bonus | Geri vites |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Kır yolu | Cip | 4659 | FUEL | 316 | 5 | 3% | 53° | 0 | 650 | 0 | 0 |
| Kır yolu | Motosiklet | 6974 | FUEL | 429 | 6 | 15% | 81° | 0 | 1137 | 100 | 0 |
| Kır yolu | Pikap | 6977 | FUEL | 521 | 6 | 17% | 47° | 0 | 1045 | 0 | 0 |
| Kır yolu | Traktör | 6384 | FUEL | 714 | 6 | 1% | 53° | 0 | 950 | 0 | 0 |
| Çöl | Cip | 3766 | FUEL | 266 | 4 | 11% | 50° | 0 | 538 | 0 | 0 |
| Çöl | Motosiklet | 5546 | FUEL | 378 | 5 | 11% | 78° | 0 | 887 | 50 | 0 |
| Çöl | Pikap | 4609 | FUEL | 445 | 5 | 20% | 47° | 0 | 692 | 0 | 0 |
| Çöl | Traktör | 5568 | FUEL | 609 | 5 | 8% | 51° | 0 | 847 | 0 | 0 |
| Kutup | Cip | 2614 | FUEL | 234 | 4 | 28% | 48° | 0 | 357 | 0 | 0 |
| Kutup | Motosiklet | 2887 | FUEL | 248 | 4 | 48% | 72° | 0 | 391 | 0 | 0 |
| Kutup | Pikap | 2624 | FUEL | 303 | 4 | 54% | 45° | 0 | 358 | 0 | 0 |
| Kutup | Traktör | 3915 | FUEL | 539 | 5 | 23% | 49° | 0 | 545 | 0 | 0 |
| Ay | Cip | 2569 | FUEL | 269 | 2 | 39% | 56° | 0 | 3140 | 2850 | 0 |
| Ay | Motosiklet | 3244 | FUEL | 483 | 4 | 0% | 120° | 0 | 3248 | 2825 | 0 |
| Ay | Pikap | 2754 | FUEL | 282 | 0 | - | 60° | 0 | 3419 | 3125 | 0 |
| Ay | Traktör | 7957 | FUEL | 969 | 6 | 8% | 67° | 0 | 7497 | 6325 | 0 |
| Orman | Cip | 3653 | FUEL | 261 | 4 | 19% | 51° | 0 | 547 | 0 | 0 |
| Orman | Motosiklet | 5829 | FUEL | 384 | 5 | 22% | 81° | 0 | 1178 | 250 | 0 |
| Orman | Pikap | 5859 | FUEL | 594 | 6 | 0% | 51° | 0 | 942 | 0 | 0 |
| Orman | Traktör | 3855 | FUEL | 445 | 4 | 35% | 52° | 0 | 579 | 0 | 1 |
| Dağ | Cip | 4185 | FUEL | 312 | 5 | 16% | 58° | 0 | 725 | 100 | 0 |
| Dağ | Motosiklet | 9724 | FUEL | 692 | 8 | 0% | 73° | 0 | 1939 | 350 | 0 |
| Dağ | Pikap | 3339 | FUEL | 376 | 5 | 45% | 54° | 0 | 506 | 0 | 0 |
| Dağ | Traktör | 2815 | FUEL | 374 | 4 | 56% | 50° | 0 | 438 | 0 | 0 |
| Plaj | Cip | 3272 | FUEL | 228 | 3 | 37% | 44° | 0 | 496 | 0 | 0 |
| Plaj | Motosiklet | 5143 | FUEL | 314 | 4 | 21% | 64° | 0 | 823 | 0 | 0 |
| Plaj | Pikap | 5439 | FUEL | 394 | 4 | 28% | 47° | 0 | 881 | 0 | 0 |
| Plaj | Traktör | 5059 | FUEL | 525 | 4 | 12% | 52° | 0 | 820 | 0 | 0 |
| Mağara | Cip | 4480 | FUEL | 322 | 5 | 3% | 59° | 0 | 786 | 50 | 0 |
| Mağara | Motosiklet | 8777 | FUEL | 579 | 7 | 1% | 82° | 0 | 1685 | 150 | 0 |
| Mağara | Pikap | 4052 | FUEL | 396 | 5 | 32% | 52° | 0 | 654 | 0 | 0 |
| Mağara | Traktör | 2427 | FUEL | 388 | 4 | 49% | 53° | 0 | 392 | 0 | 0 |
| Bataklık | Cip | 3901 | FUEL | 276 | 4 | 6% | 51° | 0 | 705 | 0 | 0 |
| Bataklık | Motosiklet | 4248 | FUEL | 284 | 4 | 36% | 73° | 0 | 768 | 0 | 0 |
| Bataklık | Pikap | 4723 | FUEL | 479 | 5 | 5% | 47° | 0 | 852 | 0 | 1 |
| Bataklık | Traktör | 4235 | FUEL | 470 | 4 | 30% | 52° | 0 | 775 | 0 | 1 |
| Şantiye | Cip | 3056 | FUEL | 214 | 3 | 42% | 45° | 0 | 528 | 0 | 0 |
| Şantiye | Motosiklet | 5186 | FUEL | 321 | 4 | 24% | 76° | 0 | 1263 | 350 | 0 |
| Şantiye | Pikap | 5310 | FUEL | 391 | 4 | 28% | 47° | 0 | 962 | 0 | 0 |
| Şantiye | Traktör | 5164 | FUEL | 545 | 4 | 12% | 53° | 0 | 948 | 0 | 0 |

## 4. Kır yolu, jeep, geliştirme yok: tohumlar

| Sürücü | Tohum | Mesafe (m) | Bitiş | Süre (sn) | Bidon | Bidon konumları (m, yakıt%) | Tepe açı | Altın | Duyuru | En uzun sessizlik (sn) |
|---|---|---|---|---|---|---|---|---|---|---|
| iyi | 0 | 3196 | FUEL | 218 | 4 | 242 (73%), 700 (58%), 1142 (58%), 2069 (11%) | 51° | 432 | 54 | 12 |
| iyi | 1 | 3196 | FUEL | 217 | 4 | 242 (73%), 700 (57%), 1141 (57%), 2069 (9%) | 51° | 432 | 55 | 12 |
| iyi | 2 | 3197 | FUEL | 218 | 4 | 242 (73%), 700 (56%), 1142 (58%), 2069 (10%) | 51° | 432 | 55 | 12 |
| iyi | 3 | 3196 | FUEL | 220 | 4 | 242 (73%), 700 (57%), 1142 (57%), 2069 (9%) | 51° | 432 | 56 | 12 |
| iyi | 4 | 3197 | FUEL | 217 | 4 | 242 (73%), 700 (57%), 1142 (59%), 2069 (9%) | 45° | 432 | 55 | 12 |
| iyi | 5 | 3197 | FUEL | 218 | 4 | 242 (73%), 700 (58%), 1142 (57%), 2069 (10%) | 51° | 432 | 56 | 12 |
| acemi | 1 | 3197 | FUEL | 217 | 4 | 242 (73%), 700 (57%), 1142 (57%), 2069 (10%) | 63° | 432 | 55 | 12 |
| acemi | 2 | 3197 | FUEL | 217 | 4 | 242 (73%), 700 (56%), 1141 (58%), 2069 (10%) | 62° | 432 | 55 | 12 |
| acemi | 3 | 3197 | FUEL | 220 | 4 | 242 (73%), 700 (57%), 1141 (57%), 2069 (9%) | 62° | 432 | 55 | 12 |
| acemi | 4 | 3197 | FUEL | 217 | 4 | 242 (73%), 700 (57%), 1142 (59%), 2069 (10%) | 50° | 432 | 54 | 12 |
| acemi | 5 | 3197 | FUEL | 217 | 4 | 242 (73%), 701 (58%), 1141 (57%), 2069 (10%) | 62° | 432 | 55 | 12 |

İyi sürücü, tohum 1-5: 3. yakıt bidonuna ulaşan = **5/5** (PLAN2 F1 kuralı: en az 4/5).

Motosiklet, Kır yolu, geliştirme yok (iyi sürücü, tohum 0-5): 4931 m FUEL (tepe 81°), 4931 m FUEL (tepe 81°), 4931 m FUEL (tepe 81°), 4930 m FUEL (tepe 81°), 4932 m FUEL (tepe 81°), 4931 m FUEL (tepe 81°).

Haritadaki bidonlar (Kır yolu): 249, 708, 1149, 2077, 3360, 5064, 7057, 9395 m; ardışık aralıklar: 249, 458, 441, 928, 1284, 1704, 1993, 2338 m.

## 5. Takılma, takla ve bitiş olayları

TAKILMA = yerde, yakıt var, hız < 0.5 m/s, 3 sn. TERS = yerde göreli açı > 100°. SON-* = oyunu bitiren olay. Eğim = olay noktasındaki zemin eğimi, Açı = burun açısı (dünyaya göre).

| Koşu | Olay | x (m) | Eğim | Açı | Yakıt | t (sn) |
|---|---|---|---|---|---|---|
| Ay/Ay aracı/y3/iyi/s0 | SON-FUEL | 7459 | 2° | 2° | 0% | 926 |
| Ay/Cip/y0/iyi/s0 | SON-FUEL | 1784 | 26° | 26° | 0% | 191 |
| Ay/Cip/y3/iyi/s0 | SON-FUEL | 2547 | 11° | 11° | 0% | 269 |
| Ay/Motosiklet/y0/iyi/s0 | SON-FUEL | 1918 | -8° | -8° | 0% | 326 |
| Ay/Motosiklet/y3/iyi/s0 | TERS | 2241 | -3° | -103° | 0% | 312 |
| Ay/Motosiklet/y3/iyi/s0 | SON-FUEL | 3221 | 14° | 15° | 0% | 483 |
| Ay/Pikap/y0/iyi/s0 | SON-FUEL | 1777 | 16° | 16° | 0% | 191 |
| Ay/Pikap/y3/iyi/s0 | SON-FUEL | 2735 | 3° | 4° | 0% | 282 |
| Ay/Traktör/y0/iyi/s0 | SON-FUEL | 5338 | 18° | 19° | 0% | 665 |
| Ay/Traktör/y3/iyi/s0 | SON-FUEL | 7944 | 13° | 14° | 0% | 969 |
| Bataklık/Cip/y0/iyi/s0 | SON-FUEL | 2366 | 6° | 6° | 0% | 175 |
| Bataklık/Cip/y3/iyi/s0 | SON-FUEL | 3850 | -3° | -3° | 0% | 276 |
| Bataklık/Motosiklet/y0/iyi/s0 | SON-FUEL | 3167 | -0° | 0° | 0% | 259 |
| Bataklık/Motosiklet/y3/iyi/s0 | SON-FUEL | 4200 | -10° | -11° | 0% | 284 |
| Bataklık/Pikap/y0/iyi/s0 | SON-FUEL | 3828 | 10° | 9° | 0% | 308 |
| Bataklık/Pikap/y3/iyi/s0 | TAKILMA | 4677 | 21° | 22° | 27% | 430 |
| Bataklık/Pikap/y3/iyi/s0 | SON-FUEL | 4642 | -14° | -14° | 0% | 479 |
| Bataklık/Traktör/y0/iyi/s0 | SON-FUEL | 3854 | -6° | -5° | 0% | 413 |
| Bataklık/Traktör/y3/iyi/s0 | SON-FUEL | 4211 | 5° | 7° | 0% | 470 |
| Dağ/Cip/y0/iyi/s0 | SON-FUEL | 2717 | -27° | -28° | 0% | 213 |
| Dağ/Cip/y3/iyi/s0 | SON-FUEL | 4151 | -21° | -23° | 0% | 312 |
| Dağ/Motosiklet/y0/iyi/s0 | SON-FUEL | 4187 | 35° | 34° | 0% | 321 |
| Dağ/Motosiklet/y3/iyi/s0 | SON-FUEL | 9716 | -0° | -0° | 0% | 692 |
| Dağ/Pikap/y0/iyi/s0 | SON-FUEL | 2265 | -16° | -17° | 0% | 259 |
| Dağ/Pikap/y3/iyi/s0 | SON-FUEL | 3254 | 24° | 24° | 0% | 376 |
| Dağ/Traktör/y0/iyi/s0 | SON-FUEL | 1858 | -13° | -13° | 0% | 346 |
| Dağ/Traktör/y3/iyi/s0 | SON-FUEL | 2728 | 2° | -10° | 0% | 374 |
| Kutup/Cip/y0/iyi/s0 | SON-FUEL | 1997 | -20° | -19° | 0% | 160 |
| Kutup/Cip/y3/iyi/s0 | SON-FUEL | 2552 | -3° | -3° | 0% | 234 |
| Kutup/Kar motosikleti/y3/iyi/s0 | SON-FUEL | 8886 | 8° | 6° | 0% | 617 |
| Kutup/Motosiklet/y0/iyi/s0 | SON-FUEL | 2276 | 28° | 26° | 0% | 228 |
| Kutup/Motosiklet/y3/iyi/s0 | SON-FUEL | 2829 | -6° | -7° | 0% | 248 |
| Kutup/Pikap/y0/iyi/s0 | SON-FUEL | 2531 | 10° | 9° | 0% | 277 |
| Kutup/Pikap/y3/iyi/s0 | TAKILMA | 2586 | 20° | 21° | 17% | 269 |
| Kutup/Pikap/y3/iyi/s0 | SON-FUEL | 2542 | 5° | 14° | 0% | 303 |
| Kutup/Traktör/y0/iyi/s0 | SON-FUEL | 2812 | -8° | -8° | 0% | 363 |
| Kutup/Traktör/y3/iyi/s0 | TAKILMA | 3878 | 27° | 30° | 11% | 508 |
| Kutup/Traktör/y3/iyi/s0 | SON-FUEL | 3790 | 22° | 24° | 0% | 539 |
| Kır yolu/Cip/y0/acemi/s1 | SON-FUEL | 3125 | -13° | -13° | 0% | 217 |
| Kır yolu/Cip/y0/acemi/s2 | SON-FUEL | 3125 | -13° | -13° | 0% | 217 |
| Kır yolu/Cip/y0/acemi/s3 | SON-FUEL | 3125 | -12° | -13° | 0% | 220 |
| Kır yolu/Cip/y0/acemi/s4 | SON-FUEL | 3125 | -12° | -13° | 0% | 217 |
| Kır yolu/Cip/y0/acemi/s5 | SON-FUEL | 3125 | -12° | -13° | 0% | 217 |
| Kır yolu/Cip/y0/iyi/s0 | SON-FUEL | 3125 | -13° | -13° | 0% | 218 |
| Kır yolu/Cip/y0/iyi/s1 | SON-FUEL | 3125 | -13° | -13° | 0% | 217 |
| Kır yolu/Cip/y0/iyi/s2 | SON-FUEL | 3125 | -13° | -13° | 0% | 218 |
| Kır yolu/Cip/y0/iyi/s3 | SON-FUEL | 3125 | -13° | -13° | 0% | 220 |
| Kır yolu/Cip/y0/iyi/s4 | SON-FUEL | 3125 | -13° | -13° | 0% | 217 |
| Kır yolu/Cip/y0/iyi/s5 | SON-FUEL | 3125 | -13° | -13° | 0% | 218 |
| Kır yolu/Cip/y3/iyi/s0 | SON-FUEL | 4540 | 17° | -8° | 0% | 316 |
| Kır yolu/Motosiklet/y0/iyi/s0 | SON-FUEL | 4880 | -13° | -13° | 0% | 306 |
| Kır yolu/Motosiklet/y0/iyi/s1 | SON-FUEL | 4880 | -13° | -13° | 0% | 309 |
| Kır yolu/Motosiklet/y0/iyi/s2 | SON-FUEL | 4880 | -17° | -13° | 0% | 311 |
| Kır yolu/Motosiklet/y0/iyi/s3 | SON-FUEL | 4881 | -17° | -15° | 0% | 311 |
| Kır yolu/Motosiklet/y0/iyi/s4 | SON-FUEL | 4879 | -13° | -12° | 0% | 308 |
| Kır yolu/Motosiklet/y0/iyi/s5 | SON-FUEL | 4880 | -17° | -14° | 0% | 309 |
| Kır yolu/Motosiklet/y3/iyi/s0 | SON-FUEL | 6848 | 0° | -22° | 0% | 429 |
| Kır yolu/Pikap/y0/iyi/s0 | SON-FUEL | 4876 | -18° | -19° | 0% | 374 |
| Kır yolu/Pikap/y3/iyi/s0 | SON-FUEL | 6856 | -14° | 7° | 0% | 521 |
| Kır yolu/Traktör/y0/iyi/s0 | SON-FUEL | 3789 | 1° | 1° | 0% | 491 |
| Kır yolu/Traktör/y3/iyi/s0 | TAKILMA | 6362 | 28° | 30° | 12% | 683 |
| Kır yolu/Traktör/y3/iyi/s0 | SON-FUEL | 6297 | -3° | -3° | 0% | 714 |
| Mağara/Cip/y0/iyi/s0 | SON-FUEL | 2315 | -31° | -31° | 0% | 207 |
| Mağara/Cip/y3/iyi/s0 | SON-FUEL | 4387 | 27° | 28° | 0% | 322 |
| Mağara/Motosiklet/y0/iyi/s0 | SON-FUEL | 4394 | 34° | -58° | 0% | 316 |
| Mağara/Motosiklet/y3/iyi/s0 | SON-FUEL | 8731 | -23° | -26° | 0% | 579 |
| Mağara/Pikap/y0/iyi/s0 | SON-FUEL | 2348 | 28° | 28° | 0% | 264 |
| Mağara/Pikap/y3/iyi/s0 | SON-FUEL | 3962 | -4° | -4° | 0% | 396 |
| Mağara/Traktör/y0/iyi/s0 | SON-FUEL | 1388 | -23° | -22° | 0% | 257 |
| Mağara/Traktör/y3/iyi/s0 | SON-FUEL | 2343 | 18° | 24° | 0% | 388 |
| Orman/Cip/y0/iyi/s0 | SON-FUEL | 2216 | 23° | 23° | 0% | 167 |
| Orman/Cip/y3/iyi/s0 | SON-FUEL | 3598 | 27° | 27° | 0% | 261 |
| Orman/Motosiklet/y0/iyi/s0 | SON-FUEL | 3814 | -8° | -7° | 0% | 258 |
| Orman/Motosiklet/y3/iyi/s0 | SON-FUEL | 5796 | 0° | -2° | 0% | 384 |
| Orman/Pikap/y0/iyi/s0 | SON-FUEL | 3811 | -8° | -9° | 0% | 307 |
| Orman/Pikap/y3/iyi/s0 | SON-FUEL | 5785 | -7° | -8° | 0% | 594 |
| Orman/Traktör/y0/iyi/s0 | SON-FUEL | 2388 | -13° | -12° | 0% | 396 |
| Orman/Traktör/y3/iyi/s0 | SON-FUEL | 3830 | -9° | -9° | 0% | 445 |
| Plaj/Cip/y0/iyi/s0 | SON-FUEL | 2774 | 16° | 16° | 0% | 195 |
| Plaj/Cip/y3/iyi/s0 | SON-FUEL | 3238 | -8° | -8° | 0% | 228 |
| Plaj/Motosiklet/y0/iyi/s0 | TAKILMA | 3271 | 21° | 23° | 15% | 258 |
| Plaj/Motosiklet/y0/iyi/s0 | SON-FUEL | 3240 | -7° | -7° | 0% | 279 |
| Plaj/Motosiklet/y3/iyi/s0 | SON-FUEL | 5079 | 1° | 3° | 0% | 314 |
| Plaj/Pikap/y0/iyi/s0 | SON-FUEL | 4716 | -3° | -3° | 0% | 353 |
| Plaj/Pikap/y3/iyi/s0 | SON-FUEL | 5379 | -17° | -16° | 0% | 394 |
| Plaj/Traktör/y0/iyi/s0 | SON-FUEL | 3063 | 10° | 8° | 0% | 325 |
| Plaj/Traktör/y3/iyi/s0 | SON-FUEL | 5009 | -5° | -7° | 0% | 525 |
| Çöl/Cip/y0/iyi/s0 | SON-FUEL | 2156 | 3° | 3° | 0% | 164 |
| Çöl/Cip/y3/iyi/s0 | SON-FUEL | 3702 | -20° | -22° | 0% | 266 |
| Çöl/Motosiklet/y0/iyi/s0 | SON-FUEL | 3715 | -10° | -10° | 0% | 269 |
| Çöl/Motosiklet/y3/iyi/s0 | SON-FUEL | 5438 | 6° | 5° | 0% | 378 |
| Çöl/Pikap/y0/iyi/s0 | SON-FUEL | 3367 | 20° | 18° | 0% | 298 |
| Çöl/Pikap/y3/iyi/s0 | SON-FUEL | 4476 | 14° | 16° | 0% | 445 |
| Çöl/Traktör/y0/iyi/s0 | SON-FUEL | 3363 | 22° | 24° | 0% | 398 |
| Çöl/Traktör/y3/iyi/s0 | SON-FUEL | 5474 | -3° | -2° | 0% | 609 |
| Şantiye/Cip/y0/iyi/s0 | SON-FUEL | 2620 | 15° | 15° | 0% | 189 |
| Şantiye/Cip/y3/iyi/s0 | SON-FUEL | 3042 | -2° | -2° | 0% | 214 |
| Şantiye/Motosiklet/y0/iyi/s0 | SON-FUEL | 4739 | -8° | -9° | 0% | 302 |
| Şantiye/Motosiklet/y3/iyi/s0 | SON-FUEL | 5127 | -13° | -14° | 0% | 321 |
| Şantiye/Pikap/y0/iyi/s0 | SON-FUEL | 4710 | 3° | 3° | 0% | 358 |
| Şantiye/Pikap/y3/iyi/s0 | SON-FUEL | 5192 | 27° | 31° | 0% | 391 |
| Şantiye/Traktör/y0/iyi/s0 | SON-FUEL | 2987 | 9° | 9° | 0% | 320 |
| Şantiye/Traktör/y3/iyi/s0 | SON-FUEL | 5139 | -3° | -3° | 0% | 545 |

## 5b. Araç özeti (matris, her iki geliştirme düzeyi, iyi sürücü)

| Araç | Ortalama mesafe (m) | Ortalama süre (sn) | CRASH | FUEL | SÜRE sınırı | Koşu |
|---|---|---|---|---|---|---|
| Cip | 3044 | 229 | 0 | 20 | 0 | 20 |
| Motosiklet | 4725 | 349 | 0 | 20 | 0 | 20 |
| Pikap | 4035 | 359 | 0 | 20 | 0 | 20 |
| Traktör | 3941 | 478 | 0 | 20 | 0 | 20 |

### Yakıt bitişleri

"Bidona kalan" = oyun bittiği yerden sonraki ilk yakıt bidonuna uzaklık. Eğim küçük ve bidon yakınsa neden büyük olasılıkla bidonun kaçırılması ya da erken yakıt tükenmesidir; eğim büyükse yokuşta takılmadır.

| Koşu | Mesafe (m) | Eğim | Bidona kalan (m) | Son bidondan beri (m) | Yakında olan özellikler |
|---|---|---|---|---|---|
| Ay/Cip/y0/iyi/s0 | 1803 | 26° | 468 | 1081 | dik iniş (1690-1700 m), çukur (1713-1720 m), dik yokuş (1790-1810 m) |
| Ay/Cip/y3/iyi/s0 | 2569 | 11° | 1203 | 1352 | tümsekler (2412-2441 m), dik iniş (2450-2470 m), çukur (2484-2490 m), dik yokuş (2570-2580 m) |
| Ay/Motosiklet/y0/iyi/s0 | 1936 | -8° | 334 | 718 | dik yokuş (1790-1810 m), çukur (1915-1922 m), dik yokuş (1940-1950 m) |
| Ay/Motosiklet/y3/iyi/s0 | 3244 | 14° | 530 | 993 | dik iniş (3100-3120 m), çukur (3133-3142 m), dik yokuş (3240-3270 m) |
| Ay/Pikap/y0/iyi/s0 | 1798 | 16° | 475 | 1798 | dik iniş (1690-1700 m), çukur (1713-1720 m), dik yokuş (1790-1810 m) |
| Ay/Pikap/y3/iyi/s0 | 2754 | 3° | 1015 | 2754 | dik iniş (2630-2650 m), çukur (2656-2665 m) |
| Ay/Traktör/y0/iyi/s0 | 5357 | 18° | 350 | 1609 | tümsekler (5232-5249 m), dik iniş (5260-5310 m), çukur (5318-5329 m), dik yokuş (5360-5400 m) |
| Ay/Traktör/y3/iyi/s0 | 7957 | 13° | 153 | 2271 | tümsekler (7839-7858 m), dik iniş (7870-7900 m), çukur (7914-7923 m) |
| Bataklık/Cip/y0/iyi/s0 | 2390 | 6° | 234 | 1033 | çukur (2265-2274 m), dik yokuş (2340-2350 m), çukur (2388-2397 m) |
| Bataklık/Cip/y3/iyi/s0 | 3901 | -3° | 550 | 1303 | dik iniş (3750-3790 m), çukur (3799-3807 m) |
| Bataklık/Motosiklet/y0/iyi/s0 | 3251 | -0° | 1233 | 653 | dik iniş (3100-3110 m), rampa (3160-3164 m) |
| Bataklık/Motosiklet/y3/iyi/s0 | 4248 | -10° | 200 | 1650 | dik iniş (4080-4110 m), çukur (4118-4128 m), dik iniş (4190-4200 m), çukur (4209-4217 m) |
| Bataklık/Pikap/y0/iyi/s0 | 3942 | 10° | 572 | 1344 | dik iniş (3750-3790 m), çukur (3799-3807 m) |
| Bataklık/Pikap/y3/iyi/s0 | 4723 | -14° | 2064 | 325 | dik iniş (4510-4560 m), çukur (4568-4577 m), çukur (4664-4672 m) |
| Bataklık/Traktör/y0/iyi/s0 | 3900 | -6° | 547 | 1302 | dik iniş (3750-3790 m), çukur (3799-3807 m) |
| Bataklık/Traktör/y3/iyi/s0 | 4235 | 5° | 189 | 1637 | dik iniş (4080-4110 m), çukur (4118-4128 m), dik iniş (4190-4200 m), çukur (4209-4217 m) |
| Dağ/Cip/y0/iyi/s0 | 2790 | -27° | 139 | 984 | dik yokuş (2590-2610 m), dik iniş (2640-2650 m), çukur (2665-2675 m), dik iniş (2690-2720 m), çukur (2733-2741 m) |
| Dağ/Cip/y3/iyi/s0 | 4185 | -21° | 120 | 1331 | dik iniş (4040-4070 m), çukur (4084-4092 m), dik iniş (4130-4140 m), çukur (4153-4163 m) |
| Dağ/Motosiklet/y0/iyi/s0 | 4248 | 35° | 84 | 1394 | çukur (4084-4092 m), dik iniş (4130-4140 m), çukur (4153-4163 m), dik yokuş (4180-4220 m) |
| Dağ/Motosiklet/y3/iyi/s0 | 9724 | -0° | 441 | 1817 | dik yokuş (9640-9690 m), rampa (9705-9710 m) |
| Dağ/Pikap/y0/iyi/s0 | 2347 | -16° | 590 | 541 | tümsekler (2146-2175 m), dik iniş (2190-2210 m), çukur (2218-2226 m), dik iniş (2240-2270 m), dik yokuş (2290-2300 m) |
| Dağ/Pikap/y3/iyi/s0 | 3339 | 24° | 1017 | 485 | dik iniş (3130-3150 m), çukur (3162-3170 m), dik iniş (3200-3230 m), rampa (3270-3275 m) |
| Dağ/Traktör/y0/iyi/s0 | 1932 | -13° | 998 | 126 | tümsekler (1757-1783 m), dik iniş (1800-1810 m), dik iniş (1840-1850 m), çukur (1864-1874 m) |
| Dağ/Traktör/y3/iyi/s0 | 2815 | 2° | 128 | 1009 | dik iniş (2640-2650 m), çukur (2665-2675 m), dik iniş (2690-2720 m), çukur (2733-2741 m) |
| Kutup/Cip/y0/iyi/s0 | 2071 | -20° | 89 | 908 | dik iniş (1940-1950 m), çukur (1959-1966 m) |
| Kutup/Cip/y3/iyi/s0 | 2614 | -3° | 863 | 531 | dik iniş (2470-2500 m), rampa (2542-2547 m) |
| Kutup/Motosiklet/y0/iyi/s0 | 2360 | 28° | 1138 | 276 | dik iniş (2160-2180 m), çukur (2188-2197 m), dik iniş (2230-2240 m), çukur (2255-2264 m) |
| Kutup/Motosiklet/y3/iyi/s0 | 2887 | -6° | 585 | 803 | dik iniş (2730-2760 m), çukur (2819-2828 m) |
| Kutup/Pikap/y0/iyi/s0 | 2612 | 10° | 884 | 529 | çukur (2418-2429 m), dik iniş (2470-2500 m), rampa (2542-2547 m) |
| Kutup/Pikap/y3/iyi/s0 | 2624 | 5° | 872 | 541 | çukur (2418-2429 m), dik iniş (2470-2500 m), rampa (2542-2547 m) |
| Kutup/Traktör/y0/iyi/s0 | 2881 | -8° | 603 | 798 | dik iniş (2730-2760 m), çukur (2819-2828 m) |
| Kutup/Traktör/y3/iyi/s0 | 3915 | 22° | 1274 | 502 | dik iniş (3720-3760 m), çukur (3765-3773 m), dik yokuş (3800-3810 m) |
| Kır yolu/Cip/y0/iyi/s0 | 3196 | -13° | 230 | 1127 | tümsekler (3004-3025 m), dik iniş (3050-3060 m), çukur (3074-3082 m) |
| Kır yolu/Cip/y3/iyi/s0 | 4659 | 17° | 519 | 1305 | dik iniş (4420-4460 m), çukur (4473-4482 m), rampa (4556-4562 m) |
| Kır yolu/Motosiklet/y0/iyi/s0 | 4931 | -13° | 179 | 1577 | çukur (4762-4771 m), dik iniş (4850-4880 m), çukur (4886-4896 m) |
| Kır yolu/Motosiklet/y3/iyi/s0 | 6974 | 0° | 205 | 1917 | çukur (6725-6734 m), rampa (6800-6804 m), çukur (6859-6867 m) |
| Kır yolu/Pikap/y0/iyi/s0 | 4933 | -18° | 183 | 1580 | çukur (4762-4771 m), dik iniş (4850-4880 m), çukur (4886-4896 m) |
| Kır yolu/Pikap/y3/iyi/s0 | 6977 | -14° | 196 | 1921 | rampa (6800-6804 m), çukur (6859-6867 m) |
| Kır yolu/Traktör/y0/iyi/s0 | 3867 | 1° | 1270 | 514 | dik iniş (3680-3710 m), çukur (3725-3733 m) |
| Kır yolu/Traktör/y3/iyi/s0 | 6384 | -3° | 755 | 1328 | rampa (6220-6224 m) |
| Mağara/Cip/y0/iyi/s0 | 2428 | -31° | 913 | 493 | dik yokuş (2180-2210 m), dik iniş (2230-2250 m), çukur (2261-2270 m), dik iniş (2290-2320 m), çukur (2328-2338 m) |
| Mağara/Cip/y3/iyi/s0 | 4480 | 27° | 462 | 1256 | dik iniş (4310-4340 m), çukur (4352-4361 m), dik yokuş (4380-4420 m) |
| Mağara/Motosiklet/y0/iyi/s0 | 4529 | 34° | 455 | 1303 | dik iniş (4310-4340 m), çukur (4352-4361 m), dik yokuş (4380-4420 m) |
| Mağara/Motosiklet/y3/iyi/s0 | 8777 | -23° | 398 | 1966 | çukur (8680-8689 m), çukur (8734-8743 m) |
| Mağara/Pikap/y0/iyi/s0 | 2426 | 28° | 879 | 491 | dik iniş (2230-2250 m), çukur (2261-2270 m), dik iniş (2290-2320 m), çukur (2328-2338 m), dik yokuş (2350-2360 m) |
| Mağara/Pikap/y3/iyi/s0 | 4052 | -4° | 887 | 827 | tümsekler (3862-3881 m), dik iniş (3890-3910 m), rampa (3947-3951 m) |
| Mağara/Traktör/y0/iyi/s0 | 1475 | -23° | 550 | 387 | dik yokuş (1280-1300 m), çukur (1328-1335 m), dik iniş (1360-1390 m) |
| Mağara/Traktör/y3/iyi/s0 | 2427 | 18° | 884 | 492 | dik iniş (2230-2250 m), çukur (2261-2270 m), dik iniş (2290-2320 m), çukur (2328-2338 m), dik yokuş (2350-2360 m) |
| Orman/Cip/y0/iyi/s0 | 2268 | 23° | 114 | 1030 | dik iniş (2120-2130 m), çukur (2145-2153 m), dik iniş (2180-2190 m), dik yokuş (2220-2240 m) |
| Orman/Cip/y3/iyi/s0 | 3653 | 27° | 295 | 1325 | dik iniş (3510-3530 m), çukur (3543-3550 m), dik yokuş (3570-3610 m) |
| Orman/Motosiklet/y0/iyi/s0 | 3863 | -8° | 78 | 1534 | dik iniş (3710-3720 m), tümsekler (3727-3744 m), rampa (3795-3800 m) |
| Orman/Motosiklet/y3/iyi/s0 | 5829 | 0° | 65 | 1938 | dik iniş (5630-5690 m), rampa (5732-5737 m), çukur (5794-5803 m) |
| Orman/Pikap/y0/iyi/s0 | 3862 | -8° | 81 | 1534 | dik iniş (3710-3720 m), tümsekler (3727-3744 m), rampa (3795-3800 m) |
| Orman/Pikap/y3/iyi/s0 | 5859 | -7° | 75 | 1 | dik iniş (5630-5690 m), rampa (5732-5737 m), çukur (5794-5803 m) |
| Orman/Traktör/y0/iyi/s0 | 2449 | -13° | 1504 | 121 | dik iniş (2320-2350 m), çukur (2358-2368 m) |
| Orman/Traktör/y3/iyi/s0 | 3855 | -9° | 62 | 1527 | dik iniş (3710-3720 m), tümsekler (3727-3744 m), rampa (3795-3800 m) |
| Plaj/Cip/y0/iyi/s0 | 2822 | 16° | 502 | 1098 | çukur (2740-2748 m) |
| Plaj/Cip/y3/iyi/s0 | 3272 | -8° | 38 | 1548 | dik iniş (3150-3160 m), çukur (3174-3182 m), çukur (3258-3264 m) |
| Plaj/Motosiklet/y0/iyi/s0 | 3289 | -7° | 36 | 15 | dik iniş (3150-3160 m), çukur (3174-3182 m), çukur (3258-3264 m) |
| Plaj/Motosiklet/y3/iyi/s0 | 5143 | 1° | 426 | 1868 | dik iniş (4970-4990 m), çukur (5003-5013 m), tümsekler (5078-5094 m) |
| Plaj/Pikap/y0/iyi/s0 | 4816 | -3° | 789 | 1542 | dik iniş (4600-4610 m), tümsekler (4652-4675 m) |
| Plaj/Pikap/y3/iyi/s0 | 5439 | -17° | 126 | 2165 | tümsekler (5282-5294 m), çukur (5385-5394 m) |
| Plaj/Traktör/y0/iyi/s0 | 3104 | 10° | 213 | 1380 | çukur (3060-3068 m) |
| Plaj/Traktör/y3/iyi/s0 | 5059 | -5° | 497 | 1785 | dik iniş (4970-4990 m), çukur (5003-5013 m) |
| Çöl/Cip/y0/iyi/s0 | 2249 | 3° | 290 | 947 | tümsekler (2026-2046 m), dik iniş (2110-2130 m) |
| Çöl/Cip/y3/iyi/s0 | 3766 | -20° | 317 | 1323 | dik iniş (3600-3620 m), tümsekler (3628-3649 m), çukur (3705-3713 m) |
| Çöl/Motosiklet/y0/iyi/s0 | 3759 | -10° | 305 | 1316 | dik iniş (3600-3620 m), tümsekler (3628-3649 m), çukur (3705-3713 m) |
| Çöl/Motosiklet/y3/iyi/s0 | 5546 | 6° | 627 | 1528 | dik iniş (5300-5350 m), çukur (5361-5371 m) |
| Çöl/Pikap/y0/iyi/s0 | 3486 | 20° | 653 | 1043 | çukur (3246-3254 m), dik iniş (3300-3320 m), çukur (3334-3344 m) |
| Çöl/Pikap/y3/iyi/s0 | 4609 | 14° | 1589 | 592 | tümsekler (4369-4399 m), dik iniş (4410-4430 m), çukur (4464-4470 m) |
| Çöl/Traktör/y0/iyi/s0 | 3465 | 22° | 657 | 1022 | çukur (3246-3254 m), dik iniş (3300-3320 m), çukur (3334-3344 m) |
| Çöl/Traktör/y3/iyi/s0 | 5568 | -3° | 591 | 1551 | çukur (5361-5371 m), rampa (5468-5472 m) |
| Şantiye/Cip/y0/iyi/s0 | 2701 | 15° | 585 | 1059 | çukur (2596-2604 m) |
| Şantiye/Cip/y3/iyi/s0 | 3056 | -2° | 164 | 1414 | tümsekler (2937-2955 m), rampa (3038-3042 m) |
| Şantiye/Motosiklet/y0/iyi/s0 | 4771 | -8° | 622 | 1567 | dik iniş (4600-4640 m), rampa (4729-4733 m) |
| Şantiye/Motosiklet/y3/iyi/s0 | 5186 | -13° | 234 | 1982 | çukur (5028-5038 m), çukur (5139-5147 m) |
| Şantiye/Pikap/y0/iyi/s0 | 4792 | 3° | 651 | 1589 | dik iniş (4600-4640 m), rampa (4729-4733 m) |
| Şantiye/Pikap/y3/iyi/s0 | 5310 | 27° | 169 | 2107 | çukur (5139-5147 m), rampa (5212-5217 m) |
| Şantiye/Traktör/y0/iyi/s0 | 3010 | 9° | 218 | 1367 | rampa (2882-2886 m), tümsekler (2937-2955 m) |
| Şantiye/Traktör/y3/iyi/s0 | 5164 | -3° | 222 | 1961 | çukur (5028-5038 m), çukur (5139-5147 m) |

## 6. Yakıt: menzil ve bidon aralığı

Menzil = depo / ölçülen yakıt harcaması (m başına). Bu değer tam gazla ve yokuşlarla bu sürücünün gerçek ortalamasıdır; bidon aralığı menzilin çok altında olmalı.

| Harita (jeep, y0) | Haritadaki bidon aralığı (m) | Menzil (m) | Aralık/menzil | En uzun gerçek aralık (m) | En düşük yakıt (alırken) |
|---|---|---|---|---|---|
| Kır yolu | 450 | 1066 | 42% | 928 | 11% |
| Çöl | 500 | 989 | 51% | 535 | 49% |
| Kutup | 450 | 936 | 48% | 471 | 51% |
| Ay | 500 | 978 | 51% | 723 | 15% |
| Orman | 500 | 1023 | 49% | 531 | 48% |
| Dağ | 400 | 1008 | 40% | 753 | 24% |
| Plaj | 600 | 1052 | 57% | 864 | 17% |
| Mağara | 450 | 829 | 54% | 846 | 14% |
| Bataklık | 550 | 1011 | 54% | 573 | 42% |
| Şantiye | 600 | 1050 | 57% | 809 | 24% |

## 7. En çok yakıt harcatan yokuşlar (jeep, geliştirme yok)

| Harita | x aralığı (m) | Uzunluk (m) | Yükseklik (m) | En dik eğim | Depo payı |
|---|---|---|---|---|---|
| Kır yolu | 2865-3010 | 145 | 36.0 | 25° | 17% |
| Kır yolu | 2590-2720 | 130 | 33.4 | 25° | 14% |
| Kır yolu | 2325-2450 | 125 | 27.7 | 27° | 14% |
| Çöl | 1930-2040 | 110 | 29.6 | 29° | 16% |
| Çöl | 1060-1195 | 135 | 20.2 | 27° | 14% |
| Çöl | 1485-1615 | 130 | 22.8 | 27° | 14% |
| Kutup | 1565-1710 | 145 | 22.8 | 29° | 19% |
| Kutup | 1810-1920 | 110 | 28.5 | 30° | 15% |
| Kutup | 570-705 | 135 | 15.2 | 23° | 14% |
| Ay | 1405-1490 | 85 | 17.7 | 29° | 10% |
| Ay | 630-720 | 90 | 15.8 | 18° | 9% |
| Ay | 1265-1350 | 85 | 18.9 | 22° | 8% |
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
| Bataklık | 2045-2150 | 105 | 26.8 | 25° | 14% |
| Bataklık | 1380-1505 | 125 | 18.2 | 23° | 13% |
| Şantiye | 2320-2490 | 170 | 22.9 | 24° | 18% |
| Şantiye | 1840-1990 | 150 | 18.7 | 23° | 15% |
| Şantiye | 1205-1335 | 130 | 15.2 | 27° | 13% |

## 8. Duyurular ve karıştırıcı

Mixer.voiceCount en yüksek: **17** (sınır 60). Koşu başına duyuru: ortalama 84.2; en uzun sessizlik: 27 sn (Dağ/Traktör/y3/iyi/s0).

| Koşu | En uzun sessizlik (sn) | Duyuru sayısı | Süre (sn) |
|---|---|---|---|
| Dağ/Traktör/y3/iyi/s0 | 27 | 74 | 374 |
| Çöl/Traktör/y3/iyi/s0 | 27 | 119 | 609 |
| Ay/Motosiklet/y0/iyi/s0 | 27 | 54 | 326 |
| Kutup/Traktör/y3/iyi/s0 | 26 | 93 | 539 |
| Bataklık/Pikap/y3/iyi/s0 | 26 | 96 | 479 |
| Orman/Pikap/y3/iyi/s0 | 26 | 142 | 594 |

## 9. Ekonomi

**Not (altın sistemi değişti):** Her altın tam 1 değerindedir (5/25/100/500 kademeleri kaldırıldı), yoğunluk erken haritada ~12-16 altın/100 m'dir. Koşu başı gelir bu yüzden çok düştü; fiyatlar S-C görevinde yeniden ölçeklenecek. Aşağıdaki ekonomi kapıları şimdilik bilgi amaçlıdır (her zaman geçer), eski hedefler yalnızca karşılaştırma içindir.

Sürücü yakıt bitene ya da 20 dk dolana kadar oynadığı için koşu başı altın gerçek bir oyuncunun kısa koşularından çok daha yüksek çıkar; bu yüzden altın/dakika da verilir. "Koşu" = tam bir koşu (uzun), "dk" = dakika cinsinden oyun süresi.

| Harita | Araç | Geliştirme | Koşu başı altın | Süre (sn) | Altın/dk | Sıradaki araç | Sıradaki harita |
|---|---|---|---|---|---|---|---|
| Kır yolu | Cip | y0 | 432 | 218 | 119 | Motosiklet (8000): 19 koşu, 68 dk | Çöl (5000): 12 koşu, 43 dk |
| Kır yolu | Cip | y3 | 650 | 316 | 123 | Motosiklet (8000): 13 koşu, 65 dk | Çöl (5000): 8 koşu, 41 dk |
| Kır yolu | Motosiklet | y0 | 692 | 309 | 134 | Pikap (15000): 22 koşu, 112 dk | Çöl (5000): 8 koşu, 38 dk |
| Kır yolu | Motosiklet | y3 | 1137 | 429 | 159 | Pikap (15000): 14 koşu, 95 dk | Çöl (5000): 5 koşu, 32 dk |
| Kır yolu | Pikap | y0 | 698 | 374 | 112 | Traktör (25000): 36 koşu, 224 dk | Çöl (5000): 8 koşu, 45 dk |
| Kır yolu | Pikap | y3 | 1045 | 521 | 120 | Traktör (25000): 24 koşu, 208 dk | Çöl (5000): 5 koşu, 42 dk |
| Kır yolu | Traktör | y0 | 526 | 491 | 64 | Kum arabası (35000): 67 koşu, 545 dk | Çöl (5000): 10 koşu, 78 dk |
| Kır yolu | Traktör | y3 | 950 | 714 | 80 | Kum arabası (35000): 37 koşu, 439 dk | Çöl (5000): 6 koşu, 63 dk |
| Çöl | Cip | y0 | 319 | 164 | 117 | Motosiklet (8000): 26 koşu, 69 dk | Kutup (12000): 38 koşu, 103 dk |
| Çöl | Cip | y3 | 538 | 266 | 121 | Motosiklet (8000): 15 koşu, 66 dk | Kutup (12000): 23 koşu, 99 dk |
| Çöl | Motosiklet | y0 | 536 | 269 | 120 | Pikap (15000): 28 koşu, 126 dk | Kutup (12000): 23 koşu, 101 dk |
| Çöl | Motosiklet | y3 | 887 | 378 | 141 | Pikap (15000): 17 koşu, 107 dk | Kutup (12000): 14 koşu, 86 dk |
| Çöl | Pikap | y0 | 501 | 298 | 101 | Traktör (25000): 50 koşu, 248 dk | Kutup (12000): 24 koşu, 120 dk |
| Çöl | Pikap | y3 | 692 | 445 | 93 | Traktör (25000): 37 koşu, 268 dk | Kutup (12000): 18 koşu, 129 dk |
| Çöl | Traktör | y0 | 501 | 398 | 76 | Kum arabası (35000): 70 koşu, 464 dk | Kutup (12000): 24 koşu, 159 dk |
| Çöl | Traktör | y3 | 847 | 609 | 83 | Kum arabası (35000): 42 koşu, 420 dk | Kutup (12000): 15 koşu, 144 dk |
| Kutup | Cip | y0 | 286 | 160 | 107 | Motosiklet (8000): 28 koşu, 75 dk | Ay (20000): 70 koşu, 187 dk |
| Kutup | Cip | y3 | 357 | 234 | 91 | Motosiklet (8000): 23 koşu, 88 dk | Ay (20000): 57 koşu, 219 dk |
| Kutup | Motosiklet | y0 | 320 | 228 | 84 | Pikap (15000): 47 koşu, 178 dk | Ay (20000): 63 koşu, 238 dk |
| Kutup | Motosiklet | y3 | 391 | 248 | 95 | Pikap (15000): 39 koşu, 159 dk | Ay (20000): 52 koşu, 212 dk |
| Kutup | Pikap | y0 | 356 | 277 | 77 | Traktör (25000): 71 koşu, 325 dk | Ay (20000): 57 koşu, 260 dk |
| Kutup | Pikap | y3 | 358 | 303 | 71 | Traktör (25000): 70 koşu, 353 dk | Ay (20000): 56 koşu, 283 dk |
| Kutup | Traktör | y0 | 390 | 363 | 64 | Kum arabası (35000): 90 koşu, 543 dk | Ay (20000): 52 koşu, 311 dk |
| Kutup | Traktör | y3 | 545 | 539 | 61 | Kum arabası (35000): 65 koşu, 577 dk | Ay (20000): 37 koşu, 330 dk |
| Ay | Cip | y0 | 2007 | 191 | 632 | Motosiklet (8000): 4 koşu, 13 dk | Orman (30000): 15 koşu, 48 dk |
| Ay | Cip | y3 | 3140 | 269 | 701 | Motosiklet (8000): 3 koşu, 12 dk | Orman (30000): 10 koşu, 43 dk |
| Ay | Motosiklet | y0 | 1349 | 326 | 248 | Pikap (15000): 12 koşu, 61 dk | Orman (30000): 23 koşu, 121 dk |
| Ay | Motosiklet | y3 | 3248 | 483 | 404 | Pikap (15000): 5 koşu, 38 dk | Orman (30000): 10 koşu, 75 dk |
| Ay | Pikap | y0 | 1945 | 191 | 613 | Traktör (25000): 13 koşu, 41 dk | Orman (30000): 16 koşu, 49 dk |
| Ay | Pikap | y3 | 3419 | 282 | 727 | Traktör (25000): 8 koşu, 35 dk | Orman (30000): 9 koşu, 42 dk |
| Ay | Traktör | y0 | 5130 | 665 | 463 | Kum arabası (35000): 7 koşu, 76 dk | Orman (30000): 6 koşu, 65 dk |
| Ay | Traktör | y3 | 7497 | 969 | 464 | Kum arabası (35000): 5 koşu, 76 dk | Orman (30000): 5 koşu, 65 dk |
| Orman | Cip | y0 | 336 | 167 | 121 | Motosiklet (8000): 24 koşu, 67 dk | Dağ (40000): 120 koşu, 332 dk |
| Orman | Cip | y3 | 547 | 261 | 126 | Motosiklet (8000): 15 koşu, 64 dk | Dağ (40000): 74 koşu, 319 dk |
| Orman | Motosiklet | y0 | 728 | 258 | 169 | Pikap (15000): 21 koşu, 89 dk | Dağ (40000): 55 koşu, 237 dk |
| Orman | Motosiklet | y3 | 1178 | 384 | 184 | Pikap (15000): 13 koşu, 82 dk | Dağ (40000): 34 koşu, 218 dk |
| Orman | Pikap | y0 | 579 | 307 | 113 | Traktör (25000): 44 koşu, 222 dk | Dağ (40000): 70 koşu, 354 dk |
| Orman | Pikap | y3 | 942 | 594 | 95 | Traktör (25000): 27 koşu, 263 dk | Dağ (40000): 43 koşu, 421 dk |
| Orman | Traktör | y0 | 365 | 396 | 55 | Kum arabası (35000): 96 koşu, 634 dk | Dağ (40000): 110 koşu, 724 dk |
| Orman | Traktör | y3 | 579 | 445 | 78 | Kum arabası (35000): 61 koşu, 449 dk | Dağ (40000): 70 koşu, 513 dk |
| Dağ | Cip | y0 | 478 | 213 | 135 | Motosiklet (8000): 17 koşu, 60 dk | Plaj (50000): 105 koşu, 372 dk |
| Dağ | Cip | y3 | 725 | 312 | 139 | Motosiklet (8000): 12 koşu, 58 dk | Plaj (50000): 69 koşu, 359 dk |
| Dağ | Motosiklet | y0 | 888 | 321 | 166 | Pikap (15000): 17 koşu, 91 dk | Plaj (50000): 57 koşu, 302 dk |
| Dağ | Motosiklet | y3 | 1939 | 692 | 168 | Pikap (15000): 8 koşu, 90 dk | Plaj (50000): 26 koşu, 298 dk |
| Dağ | Pikap | y0 | 356 | 259 | 82 | Traktör (25000): 71 koşu, 304 dk | Plaj (50000): 141 koşu, 607 dk |
| Dağ | Pikap | y3 | 506 | 376 | 81 | Traktör (25000): 50 koşu, 310 dk | Plaj (50000): 99 koşu, 620 dk |
| Dağ | Traktör | y0 | 295 | 346 | 51 | Kum arabası (35000): 119 koşu, 685 dk | Plaj (50000): 170 koşu, 979 dk |
| Dağ | Traktör | y3 | 438 | 374 | 70 | Kum arabası (35000): 80 koşu, 499 dk | Plaj (50000): 115 koşu, 713 dk |
| Plaj | Cip | y0 | 418 | 195 | 129 | Motosiklet (8000): 20 koşu, 63 dk | Mağara (60000): 144 koşu, 467 dk |
| Plaj | Cip | y3 | 496 | 228 | 131 | Motosiklet (8000): 17 koşu, 62 dk | Mağara (60000): 121 koşu, 459 dk |
| Plaj | Motosiklet | y0 | 496 | 279 | 107 | Pikap (15000): 31 koşu, 141 dk | Mağara (60000): 121 koşu, 564 dk |
| Plaj | Motosiklet | y3 | 823 | 314 | 157 | Pikap (15000): 19 koşu, 96 dk | Mağara (60000): 73 koşu, 382 dk |
| Plaj | Pikap | y0 | 770 | 353 | 131 | Traktör (25000): 33 koşu, 192 dk | Mağara (60000): 78 koşu, 459 dk |
| Plaj | Pikap | y3 | 881 | 394 | 134 | Traktör (25000): 29 koşu, 187 dk | Mağara (60000): 69 koşu, 448 dk |
| Plaj | Traktör | y0 | 472 | 325 | 87 | Kum arabası (35000): 75 koşu, 402 dk | Mağara (60000): 128 koşu, 689 dk |
| Plaj | Traktör | y3 | 820 | 525 | 94 | Kum arabası (35000): 43 koşu, 374 dk | Mağara (60000): 74 koşu, 641 dk |
| Mağara | Cip | y0 | 392 | 207 | 113 | Motosiklet (8000): 21 koşu, 71 dk | Bataklık (70000): 179 koşu, 617 dk |
| Mağara | Cip | y3 | 786 | 322 | 147 | Motosiklet (8000): 11 koşu, 55 dk | Bataklık (70000): 90 koşu, 478 dk |
| Mağara | Motosiklet | y0 | 792 | 316 | 150 | Pikap (15000): 19 koşu, 100 dk | Bataklık (70000): 89 koşu, 466 dk |
| Mağara | Motosiklet | y3 | 1685 | 579 | 175 | Pikap (15000): 9 koşu, 86 dk | Bataklık (70000): 42 koşu, 401 dk |
| Mağara | Pikap | y0 | 392 | 264 | 89 | Traktör (25000): 64 koşu, 281 dk | Bataklık (70000): 179 koşu, 786 dk |
| Mağara | Pikap | y3 | 654 | 396 | 99 | Traktör (25000): 39 koşu, 253 dk | Bataklık (70000): 108 koşu, 707 dk |
| Mağara | Traktör | y0 | 229 | 257 | 53 | Kum arabası (35000): 153 koşu, 656 dk | Bataklık (70000): 306 koşu, 1311 dk |
| Mağara | Traktör | y3 | 392 | 388 | 61 | Kum arabası (35000): 90 koşu, 578 dk | Bataklık (70000): 179 koşu, 1155 dk |
| Bataklık | Cip | y0 | 425 | 175 | 146 | Motosiklet (8000): 19 koşu, 55 dk | Şantiye (90000): 212 koşu, 619 dk |
| Bataklık | Cip | y3 | 705 | 276 | 153 | Motosiklet (8000): 12 koşu, 53 dk | Şantiye (90000): 128 koşu, 589 dk |
| Bataklık | Motosiklet | y0 | 591 | 259 | 137 | Pikap (15000): 26 koşu, 110 dk | Şantiye (90000): 153 koşu, 657 dk |
| Bataklık | Motosiklet | y3 | 768 | 284 | 162 | Pikap (15000): 20 koşu, 93 dk | Şantiye (90000): 118 koşu, 555 dk |
| Bataklık | Pikap | y0 | 720 | 308 | 140 | Traktör (25000): 35 koşu, 179 dk | Şantiye (90000): 125 koşu, 642 dk |
| Bataklık | Pikap | y3 | 852 | 479 | 107 | Traktör (25000): 30 koşu, 235 dk | Şantiye (90000): 106 koşu, 844 dk |
| Bataklık | Traktör | y0 | 710 | 413 | 103 | Kum arabası (35000): 50 koşu, 339 dk | Şantiye (90000): 127 koşu, 872 dk |
| Bataklık | Traktör | y3 | 775 | 470 | 99 | Kum arabası (35000): 46 koşu, 354 dk | Şantiye (90000): 117 koşu, 909 dk |
| Şantiye | Cip | y0 | 466 | 189 | 148 | Motosiklet (8000): 18 koşu, 54 dk | - |
| Şantiye | Cip | y3 | 528 | 214 | 148 | Motosiklet (8000): 16 koşu, 55 dk | - |
| Şantiye | Motosiklet | y0 | 1082 | 302 | 215 | Pikap (15000): 14 koşu, 70 dk | - |
| Şantiye | Motosiklet | y3 | 1263 | 321 | 236 | Pikap (15000): 12 koşu, 64 dk | - |
| Şantiye | Pikap | y0 | 864 | 358 | 145 | Traktör (25000): 29 koşu, 173 dk | - |
| Şantiye | Pikap | y3 | 962 | 391 | 148 | Traktör (25000): 26 koşu, 170 dk | - |
| Şantiye | Traktör | y0 | 528 | 320 | 99 | Kum arabası (35000): 67 koşu, 354 dk | - |
| Şantiye | Traktör | y3 | 948 | 545 | 104 | Kum arabası (35000): 37 koşu, 336 dk | - |

Kır yolu + jeep ortalaması (6 koşu): 432 altın/koşu. Jeep için 4 geliştirmenin 3. seviyeye maliyeti: 8256 altın = 20 koşu; motosiklet (8000): 19 koşu; Çöl haritası (5000): 12 koşu.

**İlerleme özeti (kapılarla aynı hesap):** Kır yolu + jeep (geliştirme yok) iyi koşu ortalaması 432 altın, medyan koşu süresi 3.6 dk. Motosiklet (8000) = 18.5 koşu, Çöl (5000) = 11.6 koşu. Matristeki tüm iyi sürücü koşularının ortalama hızı 169 altın/dk; tüm araçlar ve haritalar (1350000 altın) ≈ 132.9 saat. Hedef: motosiklet 4-6 koşu, Çöl 2-3 koşu, tüm mağaza 15-25 saat.

Tüm araçlar ve haritalar toplam 1350000 altın. Kır yolu + jeep hızıyla (119 altın/dk) hepsini almak ≈ 189 saat oyun sürer. Motosiklet gibi devrilgen araçlarla altın/dk 3-5 kat düşer (bkz. tablo).

## 10. Sorunlar (ciddiyet sırasıyla)

1. **[Yüksek]** İlk 1500 m içinde biten 1 koşu: Mağara/Traktör/y0/iyi/s0 1475 m FUEL (eğim -23°).
2. **[Yüksek]** Yerde göreli açı 100°'yi geçen (ters dönme) olay 1. En sık: Ay x≈2200 m (eğim -3°, açı -103°, 1 kez).
3. **[Orta]** Takılma 5 kez görüldü. En sık noktalar: Kır yolu x≈6350 m (eğim 28°, 1 kez); Kutup x≈2550 m (eğim 20°, 1 kez); Kutup x≈3850 m (eğim 27°, 1 kez); Plaj x≈3250 m (eğim 21°, 1 kez); Bataklık x≈4650 m (eğim 21°, 1 kez).
4. **[Orta]** Bidona %5'ten az yakıtla ulaşılan 14 koşu var (marj çok dar): Kır yolu/Cip/y3/iyi/s0; Kır yolu/Traktör/y0/iyi/s0; Kır yolu/Traktör/y3/iyi/s0; Ay/Motosiklet/y3/iyi/s0; Orman/Pikap/y3/iyi/s0.
5. **[Düşük]** (Tasarım gereği: bidon aralığı mesafeyle uzadığı için iyi koşular çoğunlukla yakıtla biter.) Yakıt bitişiyle sonlanan koşu: 97/97. Haritalara göre: Kır yolu 23, Çöl 8, Kutup 9, Ay 9, Orman 8, Dağ 8, Plaj 8, Mağara 8, Bataklık 8, Şantiye 8. Bunların 14 tanesi bir sonraki bidona 150 m'den az kala bitiyor (menzil marjı çok dar), 5 tanesinde bitişten önce yamaçta takılma olayı var (yani yakıt, yamaçta çıkamayıp beklerken yanmış). "Yakıt bitti" duyurusu oyuncuya nedeni söylemez.

## 11. Ayar önerileri

- Kır yolu x≈6350 m: 1 takılma/ters olayı (Traktör), zemin eğimi 28°. Bu yamacın en dik kısmını 20° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Kutup x≈2550 m: 1 takılma/ters olayı (Pikap), zemin eğimi 20°. Bu yamacın en dik kısmını 12° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Kutup x≈3850 m: 1 takılma/ters olayı (Traktör), zemin eğimi 27°. Bu yamacın en dik kısmını 19° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Ay x≈2200 m: 1 takılma/ters olayı (Motosiklet); olay noktasında zemin eğimi düşük (-3°) ama önceki 60 m'de en dik yamaç 28°: araç yamaçta burnunu çok kaldırıp düzlükte arkaya devriliyor (gaz-burun yukarı). Yakındaki özellikler: dik yokuş (2160-2180 m). Denge yardımının eşiğini (40°) ve gaz kısma eğrisini bu parçada sınayın; yamaç sonu eğimini yumuşatın.
- Plaj x≈3250 m: 1 takılma/ters olayı (Motosiklet), zemin eğimi 21°. Bu yamacın en dik kısmını 13° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Bataklık x≈4650 m: 1 takılma/ters olayı (Pikap), zemin eğimi 21°. Bu yamacın en dik kısmını 13° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Kutup: 8 koşu yakıtla bitiyor, 2 tanesinde önce yamaçta takılma var (takılma eğimleri 20-27°, mesafeler 2624, 3915 m). Asıl sorun yakıt değil tırmanış: yüzeyin tutunması (kar, μ=0.45) bu eğim için yetersiz; 20°'nin üstündeki yamaçları azaltın, ya da bu yüzeyin μ değerini/araç torkunu artırın. Takılınca yakıt boşa yanıyor: "yokuşu çıkamıyorsun" ipucu ve geri vites daha erken devreye girmeli.
- Ay: 8 koşu takılmadan yakıtla bitiyor (mesafeler 1798, 1803, 1936, 2569, 2754, 3244, 5357, 7957 m); 2 km'den kısa olanlar için ilk bidon aralıkları (`fuelEvery`=500 m, `Terrain.fuelGapAt`) bu araçların menziline göre geniş.
- Dağ: 8 koşu takılmadan yakıtla bitiyor (mesafeler 1932, 2347, 2790, 2815, 3339, 4185, 4248, 9724 m); 2 km'den kısa olanlar için ilk bidon aralıkları (`fuelEvery`=400 m, `Terrain.fuelGapAt`) bu araçların menziline göre geniş.
- Mağara: 8 koşu takılmadan yakıtla bitiyor (mesafeler 1475, 2426, 2427, 2428, 4052, 4480, 4529, 8777 m); 2 km'den kısa olanlar için ilk bidon aralıkları (`fuelEvery`=450 m, `Terrain.fuelGapAt`) bu araçların menziline göre geniş.

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
