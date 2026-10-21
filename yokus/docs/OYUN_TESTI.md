# Yokuş Ustası: oyun testi raporu

Bu rapor `bash yokus/android/test/run.sh PlayTest` tarafından otomatik üretilir (elle düzenlemeyin, yeniden çalıştırınca yazılır). Sürücü `BlindDriver`: yalnızca sesle algılanabilen bilgilere bakar (eğim açısı, 10-15 m ilerideki radar eğimi, duyurular, havada olma, yakıt uyarıları, hız), 0.25 sn tepki gecikmesi ve küçük gürültüyle oynar. Strateji: tam gaz, burun zemin eğiminden 35° fazla kalkarsa gazı kes, havada burnu iniş eğimine yatır, takılırsa kısa geri git. "Acemi" sürücü yalnızca takla bipi (>70°) çalınca gazı keser.

- Koşu sayısı: 90 (matris 80, tohum 10); oyun zamanı sınırı 20 dk; gerçek süre 89 sn.
- Hata dedektörü tetiklenen koşu: 0

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
| Kır yolu | Cip | 16132 | SÜRE | 1200 | 35 | 25% | 81° | 0 | 141345 | 950 | 0 |
| Kır yolu | Motosiklet | 3095 | CRASH | 187 | 7 | 71% | 120° | 0 | 21495 | 0 | 0 |
| Kır yolu | Pikap | 11303 | FUEL | 1023 | 25 | 65% | 70° | 0 | 105145 | 350 | 1 |
| Kır yolu | Traktör | 5678 | FUEL | 734 | 12 | 60% | 49° | 0 | 49195 | 0 | 0 |
| Çöl | Cip | 4451 | FUEL | 426 | 8 | 20% | 77° | 0 | 34490 | 150 | 0 |
| Çöl | Motosiklet | 1513 | CRASH | 94 | 3 | 62% | 128° | 0 | 4940 | 0 | 0 |
| Çöl | Pikap | 4864 | FUEL | 487 | 9 | 62% | 53° | 0 | 38690 | 150 | 0 |
| Çöl | Traktör | 1978 | FUEL | 372 | 4 | 61% | 40° | 0 | 9840 | 0 | 1 |
| Kutup | Cip | 2257 | FUEL | 290 | 5 | 61% | 46° | 0 | 9645 | 0 | 0 |
| Kutup | Motosiklet | 1034 | CRASH | 63 | 2 | 70% | 119° | 0 | 2445 | 0 | 0 |
| Kutup | Pikap | 2252 | FUEL | 330 | 5 | 67% | 43° | 0 | 9645 | 0 | 0 |
| Kutup | Traktör | 1016 | FUEL | 243 | 2 | 65% | 33° | 0 | 2445 | 0 | 2 |
| Ay | Cip | 1218 | CRASH | 130 | 0 | - | 157° | 0 | 3350 | 1050 | 0 |
| Ay | Motosiklet | 683 | CRASH | 95 | 1 | 66% | 117° | 0 | 1170 | 375 | 0 |
| Ay | Pikap | 3813 | FUEL | 536 | 5 | 49% | 72° | 0 | 24195 | 4850 | 0 |
| Ay | Traktör | 5854 | FUEL | 962 | 12 | 57% | 78° | 0 | 45245 | 5475 | 3 |
| Orman | Cip | 15457 | SÜRE | 1200 | 31 | 55% | 82° | 0 | 143160 | 400 | 0 |
| Orman | Motosiklet | 547 | CRASH | 33 | 1 | 82% | 111° | 0 | 360 | 0 | 0 |
| Orman | Pikap | 10853 | FUEL | 1015 | 22 | 62% | 60° | 0 | 96410 | 50 | 0 |
| Orman | Traktör | 3063 | FUEL | 444 | 6 | 57% | 47° | 0 | 20860 | 0 | 0 |
| Dağ | Cip | 3738 | FUEL | 321 | 8 | 65% | 73° | 0 | 28790 | 150 | 0 |
| Dağ | Motosiklet | 119 | CRASH | 9 | 0 | - | 100° | 0 | 80 | 0 | 0 |
| Dağ | Pikap | 2583 | FUEL | 358 | 7 | 72% | 49° | 0 | 17240 | 0 | 0 |
| Dağ | Traktör | 1571 | FUEL | 336 | 4 | 69% | 46° | 0 | 5140 | 0 | 1 |
| Plaj | Cip | 13059 | FUEL | 1072 | 21 | 0% | 78° | 0 | 116675 | 350 | 1 |
| Plaj | Motosiklet | 1063 | CRASH | 59 | 2 | 63% | 109° | 0 | 2025 | 0 | 0 |
| Plaj | Pikap | 13054 | FUEL | 1162 | 22 | 53% | 63° | 0 | 117525 | 0 | 1 |
| Plaj | Traktör | 8311 | FUEL | 1027 | 14 | 52% | 53° | 0 | 74125 | 0 | 2 |
| Mağara | Cip | 2914 | FUEL | 321 | 5 | 30% | 75° | 0 | 13365 | 100 | 0 |
| Mağara | Motosiklet | 2907 | FUEL | 330 | 5 | 8% | 160° | 0 | 13115 | 250 | 0 |
| Mağara | Pikap | 2909 | FUEL | 341 | 5 | 41% | 59° | 0 | 13665 | 0 | 0 |
| Mağara | Traktör | 1475 | FUEL | 311 | 3 | 65% | 41° | 0 | 4265 | 0 | 0 |
| Bataklık | Cip | 4900 | FUEL | 444 | 9 | 49% | 74° | 0 | 39630 | 200 | 0 |
| Bataklık | Motosiklet | 958 | CRASH | 57 | 2 | 67% | 104° | 0 | 2105 | 0 | 0 |
| Bataklık | Pikap | 4901 | FUEL | 490 | 9 | 59% | 57° | 0 | 40130 | 0 | 1 |
| Bataklık | Traktör | 2867 | FUEL | 428 | 5 | 55% | 42° | 0 | 19630 | 0 | 0 |
| Şantiye | Cip | 9344 | FUEL | 736 | 14 | 50% | 72° | 0 | 84510 | 550 | 0 |
| Şantiye | Motosiklet | 1563 | CRASH | 88 | 3 | 62% | 127° | 0 | 4860 | 0 | 0 |
| Şantiye | Pikap | 8776 | FUEL | 771 | 14 | 58% | 58° | 0 | 79310 | 50 | 0 |
| Şantiye | Traktör | 4727 | FUEL | 702 | 8 | 51% | 49° | 0 | 40160 | 0 | 0 |

## 3. Harita × araç, geliştirme hepsi 3. seviye (iyi sürücü)

| Harita | Araç | Mesafe (m) | Bitiş | Süre (sn) | Bidon | En az yakıt | Tepe açı | Takla | Altın | Bonus | Geri vites |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Kır yolu | Cip | 16616 | SÜRE | 1200 | 34 | 46% | 78° | 0 | 145195 | 1100 | 0 |
| Kır yolu | Motosiklet | 1373 | CRASH | 79 | 3 | 78% | 95° | 0 | 4445 | 0 | 0 |
| Kır yolu | Pikap | 15676 | SÜRE | 1200 | 34 | 54% | 68° | 0 | 138295 | 600 | 0 |
| Kır yolu | Traktör | 11277 | SÜRE | 1200 | 25 | 70% | 49° | 0 | 104395 | 0 | 0 |
| Çöl | Cip | 12654 | FUEL | 1106 | 23 | 30% | 81° | 0 | 108790 | 550 | 1 |
| Çöl | Motosiklet | 2560 | CRASH | 210 | 5 | 73% | 144° | 0 | 15490 | 50 | 1 |
| Çöl | Pikap | 7288 | FUEL | 713 | 12 | 42% | 59° | 0 | 60890 | 250 | 1 |
| Çöl | Traktör | 3792 | FUEL | 616 | 8 | 67% | 44° | 0 | 27040 | 0 | 1 |
| Kutup | Cip | 3243 | FUEL | 389 | 7 | 67% | 58° | 0 | 16795 | 50 | 0 |
| Kutup | Motosiklet | 1035 | CRASH | 61 | 2 | 77% | 118° | 0 | 2445 | 0 | 0 |
| Kutup | Pikap | 2259 | FUEL | 343 | 5 | 75% | 43° | 0 | 9645 | 0 | 2 |
| Kutup | Traktör | 1450 | FUEL | 351 | 3 | 70% | 38° | 0 | 4045 | 0 | 2 |
| Ay | Cip | 3819 | FUEL | 621 | 5 | 16% | 85° | 0 | 23670 | 4500 | 2 |
| Ay | Motosiklet | 506 | CRASH | 81 | 1 | 72% | 130° | 0 | 695 | 350 | 0 |
| Ay | Pikap | 1603 | FUEL | 325 | 2 | 65% | 70° | 0 | 5295 | 1875 | 2 |
| Ay | Traktör | 8522 | SÜRE | 1200 | 14 | 42% | 86° | 0 | 72395 | 10925 | 0 |
| Orman | Cip | 15917 | SÜRE | 1200 | 32 | 67% | 69° | 0 | 147810 | 750 | 0 |
| Orman | Motosiklet | 1256 | CRASH | 76 | 3 | 78% | 124° | 0 | 3560 | 0 | 0 |
| Orman | Pikap | 15113 | SÜRE | 1200 | 31 | 72% | 59° | 0 | 140760 | 100 | 0 |
| Orman | Traktör | 7361 | FUEL | 1001 | 15 | 67% | 52° | 0 | 63460 | 0 | 1 |
| Dağ | Cip | 14788 | SÜRE | 1200 | 34 | 49% | 79° | 0 | 137540 | 900 | 0 |
| Dağ | Motosiklet | 119 | CRASH | 9 | 0 | - | 101° | 0 | 80 | 0 | 0 |
| Dağ | Pikap | 3256 | FUEL | 423 | 8 | 78% | 53° | 0 | 25740 | 100 | 1 |
| Dağ | Traktör | 3589 | FUEL | 667 | 9 | 34% | 53° | 0 | 28240 | 0 | 1 |
| Plaj | Cip | 16688 | SÜRE | 1200 | 27 | 26% | 77° | 0 | 149075 | 550 | 0 |
| Plaj | Motosiklet | 1443 | CRASH | 80 | 3 | 72% | 125° | 0 | 3825 | 0 | 0 |
| Plaj | Pikap | 15696 | SÜRE | 1200 | 26 | 66% | 59° | 0 | 143575 | 50 | 0 |
| Plaj | Traktör | 10027 | SÜRE | 1200 | 17 | 61% | 54° | 0 | 89225 | 0 | 2 |
| Mağara | Cip | 1654 | FUEL | 227 | 3 | 74% | 59° | 0 | 6165 | 0 | 0 |
| Mağara | Motosiklet | 1688 | CRASH | 120 | 3 | 78% | 131° | 0 | 6215 | 50 | 0 |
| Mağara | Pikap | 9301 | FUEL | 927 | 20 | 57% | 77° | 0 | 75540 | 100 | 0 |
| Mağara | Traktör | 2902 | FUEL | 478 | 6 | 68% | 53° | 0 | 14565 | 0 | 1 |
| Bataklık | Cip | 4899 | FUEL | 459 | 9 | 66% | 77° | 0 | 39680 | 250 | 0 |
| Bataklık | Motosiklet | 1040 | CRASH | 62 | 2 | 75% | 131° | 0 | 2430 | 0 | 0 |
| Bataklık | Pikap | 6926 | FUEL | 757 | 13 | 68% | 57° | 0 | 60430 | 0 | 1 |
| Bataklık | Traktör | 4279 | FUEL | 651 | 8 | 64% | 46° | 0 | 33730 | 0 | 2 |
| Şantiye | Cip | 9783 | FUEL | 739 | 13 | 29% | 77° | 0 | 86760 | 600 | 0 |
| Şantiye | Motosiklet | 1580 | CRASH | 90 | 3 | 71% | 92° | 0 | 4960 | 0 | 0 |
| Şantiye | Pikap | 15333 | SÜRE | 1200 | 24 | 38% | 80° | 0 | 144210 | 350 | 0 |
| Şantiye | Traktör | 11113 | SÜRE | 1200 | 19 | 62% | 56° | 0 | 101260 | 0 | 0 |

## 4. Kır yolu, jeep, geliştirme yok: tohumlar

| Sürücü | Tohum | Mesafe (m) | Bitiş | Süre (sn) | Bidon | Bidon konumları (m, yakıt%) | Tepe açı | Altın | Duyuru | En uzun sessizlik (sn) |
|---|---|---|---|---|---|---|---|---|---|---|
| iyi | 0 | 16132 | SÜRE | 1200 | 35 | 241 (80%), 676 (69%), 1089 (71%), 1531 (68%), 1976 (66%), 2419 (68%), 2893 (65%), 3362 (65%), 3849 (62%), 4302 (65%), 4786 (61%), 5235 (69%), 5685 (64%), 6141 (64%), 6563 (64%), 7015 (63%), 7501 (63%), 7984 (60%), 8448 (64%), 8923 (61%), 9390 (63%), 9858 (61%), 10344 (64%), 10760 (65%), 11235 (63%), 11647 (68%), 12102 (63%), 13021 (25%), 13435 (68%), 13857 (63%), 14328 (63%), 14817 (59%), 15238 (65%), 15649 (67%), 16088 (64%) | 81° | 141345 | 390 | 14 |
| iyi | 1 | 16196 | SÜRE | 1200 | 35 | 241 (80%), 676 (69%), 1089 (70%), 1531 (68%), 1976 (66%), 2420 (68%), 2893 (64%), 3363 (65%), 3849 (63%), 4301 (65%), 4786 (60%), 5235 (68%), 5685 (64%), 6141 (66%), 6563 (64%), 7015 (63%), 7501 (62%), 7984 (60%), 8448 (64%), 8923 (61%), 9390 (62%), 9858 (61%), 10344 (63%), 10760 (64%), 11235 (62%), 11647 (68%), 12102 (63%), 13021 (24%), 13435 (67%), 13857 (65%), 14328 (62%), 14816 (60%), 15238 (66%), 15649 (67%), 16089 (64%) | 81° | 142995 | 392 | 14 |
| iyi | 2 | 16087 | SÜRE | 1200 | 34 | 241 (80%), 676 (69%), 1089 (70%), 1531 (68%), 1976 (67%), 2420 (67%), 2893 (66%), 3362 (65%), 3849 (62%), 4302 (65%), 4786 (60%), 5235 (68%), 5685 (65%), 6141 (65%), 6563 (64%), 7015 (62%), 7501 (63%), 7984 (61%), 8448 (64%), 8923 (61%), 9391 (62%), 9858 (60%), 10344 (63%), 10760 (64%), 11235 (62%), 11647 (66%), 12102 (62%), 13021 (25%), 13435 (67%), 13857 (66%), 14328 (63%), 14817 (60%), 15238 (66%), 15649 (68%) | 83° | 141895 | 388 | 14 |
| iyi | 3 | 16185 | SÜRE | 1200 | 35 | 241 (80%), 676 (69%), 1089 (70%), 1531 (68%), 1976 (66%), 2420 (68%), 2893 (65%), 3362 (66%), 3849 (63%), 4301 (65%), 4786 (60%), 5235 (69%), 5685 (65%), 6141 (65%), 6563 (65%), 7015 (62%), 7501 (64%), 7984 (60%), 8448 (63%), 8923 (60%), 9390 (62%), 9858 (61%), 10344 (63%), 10760 (65%), 11235 (62%), 11647 (68%), 12102 (62%), 13021 (24%), 13435 (67%), 13857 (65%), 14328 (62%), 14817 (59%), 15238 (66%), 15649 (67%), 16089 (64%) | 79° | 142795 | 390 | 14 |
| iyi | 4 | 16183 | SÜRE | 1200 | 35 | 241 (80%), 676 (69%), 1089 (71%), 1531 (68%), 1976 (66%), 2420 (67%), 2893 (65%), 3363 (65%), 3849 (62%), 4302 (66%), 4786 (60%), 5235 (68%), 5685 (64%), 6141 (65%), 6563 (64%), 7015 (62%), 7501 (63%), 7984 (60%), 8448 (64%), 8923 (61%), 9390 (63%), 9858 (61%), 10344 (63%), 10760 (64%), 11235 (62%), 11647 (68%), 12102 (62%), 13021 (26%), 13435 (67%), 13857 (65%), 14328 (63%), 14817 (59%), 15238 (66%), 15649 (67%), 16088 (64%) | 79° | 141645 | 391 | 14 |
| iyi | 5 | 16190 | SÜRE | 1200 | 34 | 241 (80%), 676 (70%), 1089 (70%), 1976 (34%), 2419 (68%), 2893 (65%), 3362 (66%), 3849 (63%), 4301 (66%), 4786 (60%), 5235 (70%), 5685 (65%), 6141 (65%), 6563 (65%), 7015 (63%), 7501 (62%), 7984 (59%), 8448 (63%), 8922 (60%), 9390 (63%), 9858 (61%), 10344 (63%), 10760 (64%), 11235 (63%), 11647 (67%), 12102 (62%), 13021 (25%), 13435 (67%), 13857 (65%), 14328 (63%), 14816 (60%), 15238 (66%), 15648 (67%), 16088 (63%) | 85° | 143145 | 388 | 14 |
| acemi | 1 | 1546 | CRASH | 98 | 4 | 241 (80%), 676 (69%), 1089 (70%), 1530 (68%) | 163° | 4995 | 14 | 14 |
| acemi | 2 | 1546 | CRASH | 99 | 4 | 241 (80%), 676 (69%), 1089 (71%), 1530 (68%) | 158° | 4995 | 14 | 14 |
| acemi | 3 | 1546 | CRASH | 100 | 4 | 241 (80%), 676 (69%), 1089 (70%), 1530 (68%) | 157° | 4995 | 14 | 14 |
| acemi | 4 | 1543 | CRASH | 98 | 4 | 241 (80%), 676 (69%), 1089 (71%), 1530 (68%) | 141° | 4995 | 14 | 14 |
| acemi | 5 | 1546 | CRASH | 98 | 4 | 241 (80%), 676 (70%), 1089 (70%), 1530 (68%) | 161° | 4995 | 14 | 14 |

İyi sürücü, tohum 1-5: 3. yakıt bidonuna ulaşan = **5/5** (PLAN2 F1 kuralı: en az 4/5).

Haritadaki bidonlar (Kır yolu): 249, 683, 1096, 1537, 1983, 2427, 2901, 3370 m; ardışık aralıklar: 249, 434, 413, 440, 447, 444, 474, 469 m.

## 5. Takılma, takla ve bitiş olayları

TAKILMA = yerde, yakıt var, hız < 0.5 m/s, 3 sn. TERS = yerde göreli açı > 100°. SON-* = oyunu bitiren olay. Eğim = olay noktasındaki zemin eğimi, Açı = burun açısı (dünyaya göre).

| Koşu | Olay | x (m) | Eğim | Açı | Yakıt | t (sn) |
|---|---|---|---|---|---|---|
| Ay/Cip/y0/iyi/s0 | SON-CRASH | 1218 | -7° | -157° | 0% | 130 |
| Ay/Cip/y3/iyi/s0 | TAKILMA | 3819 | 44° | 46° | 89% | 461 |
| Ay/Cip/y3/iyi/s0 | TAKILMA | 3813 | 38° | 39° | 71% | 481 |
| Ay/Cip/y3/iyi/s0 | TAKILMA | 3813 | 38° | 39° | 21% | 540 |
| Ay/Cip/y3/iyi/s0 | SON-FUEL | 3768 | -6° | -6° | 0% | 621 |
| Ay/Motosiklet/y0/iyi/s0 | TAKILMA | 675 | 3° | 94° | 57% | 91 |
| Ay/Motosiklet/y0/iyi/s0 | TERS | 681 | 9° | 109° | 56% | 94 |
| Ay/Motosiklet/y0/iyi/s0 | SON-CRASH | 683 | 10° | 117° | 56% | 95 |
| Ay/Motosiklet/y3/iyi/s0 | TAKILMA | 505 | 19° | 100° | 77% | 75 |
| Ay/Motosiklet/y3/iyi/s0 | TERS | 504 | 19° | 119° | 77% | 80 |
| Ay/Motosiklet/y3/iyi/s0 | SON-CRASH | 503 | 17° | 130° | 76% | 81 |
| Ay/Pikap/y0/iyi/s0 | TAKILMA | 3808 | 12° | 14° | 17% | 455 |
| Ay/Pikap/y0/iyi/s0 | SON-FUEL | 3768 | -6° | -6° | 0% | 536 |
| Ay/Pikap/y3/iyi/s0 | TAKILMA | 1596 | 33° | 35° | 67% | 187 |
| Ay/Pikap/y3/iyi/s0 | TAKILMA | 1598 | 37° | 37° | 3% | 279 |
| Ay/Pikap/y3/iyi/s0 | SON-FUEL | 1584 | -5° | -5° | 0% | 325 |
| Ay/Traktör/y0/iyi/s0 | TAKILMA | 4551 | -2° | 59° | 76% | 609 |
| Ay/Traktör/y0/iyi/s0 | TAKILMA | 5843 | 32° | 33° | 62% | 868 |
| Ay/Traktör/y0/iyi/s0 | TAKILMA | 5844 | 37° | 38° | 17% | 931 |
| Ay/Traktör/y0/iyi/s0 | SON-FUEL | 5833 | -6° | -7° | 0% | 962 |
| Bataklık/Cip/y0/iyi/s0 | TAKILMA | 4838 | 14° | 19° | 4% | 413 |
| Bataklık/Cip/y0/iyi/s0 | SON-FUEL | 4835 | -2° | -0° | 0% | 444 |
| Bataklık/Cip/y3/iyi/s0 | SON-FUEL | 4804 | -6° | -7° | 0% | 459 |
| Bataklık/Motosiklet/y0/iyi/s0 | TERS | 936 | -6° | 94° | 91% | 55 |
| Bataklık/Motosiklet/y0/iyi/s0 | SON-CRASH | 958 | -17° | 86° | 90% | 57 |
| Bataklık/Motosiklet/y3/iyi/s0 | TERS | 1039 | 20° | 120° | 89% | 61 |
| Bataklık/Motosiklet/y3/iyi/s0 | SON-CRASH | 1040 | 21° | 131° | 89% | 62 |
| Bataklık/Pikap/y0/iyi/s0 | TAKILMA | 4838 | 19° | 20° | 25% | 439 |
| Bataklık/Pikap/y0/iyi/s0 | SON-FUEL | 4835 | -2° | -1° | 0% | 490 |
| Bataklık/Pikap/y3/iyi/s0 | TAKILMA | 6912 | 17° | 18° | 45% | 610 |
| Bataklık/Pikap/y3/iyi/s0 | TAKILMA | 6895 | 25° | 25° | 19% | 648 |
| Bataklık/Pikap/y3/iyi/s0 | SON-FUEL | 6854 | 1° | -0° | 0% | 757 |
| Bataklık/Traktör/y0/iyi/s0 | TAKILMA | 2840 | 14° | 15° | 17% | 372 |
| Bataklık/Traktör/y0/iyi/s0 | SON-FUEL | 2794 | -3° | -5° | 0% | 428 |
| Bataklık/Traktör/y3/iyi/s0 | TAKILMA | 4256 | 19° | 20° | 50% | 523 |
| Bataklık/Traktör/y3/iyi/s0 | TAKILMA | 4255 | 17° | 18° | 26% | 567 |
| Bataklık/Traktör/y3/iyi/s0 | SON-FUEL | 4207 | -3° | -3° | 0% | 651 |
| Dağ/Cip/y0/iyi/s0 | SON-FUEL | 3725 | -5° | -2° | 0% | 321 |
| Dağ/Motosiklet/y0/iyi/s0 | TERS | 103 | -12° | 89° | 91% | 8 |
| Dağ/Motosiklet/y0/iyi/s0 | SON-CRASH | 119 | -7° | 100° | 91% | 9 |
| Dağ/Motosiklet/y3/iyi/s0 | TERS | 101 | -11° | 89° | 94% | 8 |
| Dağ/Motosiklet/y3/iyi/s0 | SON-CRASH | 119 | -7° | 101° | 93% | 9 |
| Dağ/Pikap/y0/iyi/s0 | SON-FUEL | 2520 | 5° | 1° | 0% | 358 |
| Dağ/Pikap/y3/iyi/s0 | TAKILMA | 3233 | 24° | 25° | 39% | 319 |
| Dağ/Pikap/y3/iyi/s0 | SON-FUEL | 3191 | 3° | 2° | 0% | 423 |
| Dağ/Traktör/y0/iyi/s0 | TAKILMA | 1551 | 24° | 25° | 57% | 206 |
| Dağ/Traktör/y0/iyi/s0 | SON-FUEL | 1510 | 2° | -0° | 0% | 336 |
| Dağ/Traktör/y3/iyi/s0 | SON-FUEL | 3532 | -1° | -1° | 0% | 667 |
| Kutup/Cip/y0/iyi/s0 | TAKILMA | 2235 | 15° | 16° | 32% | 204 |
| Kutup/Cip/y0/iyi/s0 | TAKILMA | 2228 | 14° | 15° | 4% | 227 |
| Kutup/Cip/y0/iyi/s0 | SON-FUEL | 2171 | -2° | -1° | 0% | 290 |
| Kutup/Cip/y3/iyi/s0 | TAKILMA | 3224 | 18° | 18° | 19% | 302 |
| Kutup/Cip/y3/iyi/s0 | SON-FUEL | 3171 | 1° | 1° | 0% | 389 |
| Kutup/Motosiklet/y0/iyi/s0 | TERS | 1027 | 10° | 110° | 79% | 62 |
| Kutup/Motosiklet/y0/iyi/s0 | SON-CRASH | 1034 | 7° | 116° | 79% | 63 |
| Kutup/Motosiklet/y3/iyi/s0 | TERS | 1029 | 9° | 110° | 85% | 60 |
| Kutup/Motosiklet/y3/iyi/s0 | SON-CRASH | 1035 | 5° | 115° | 85% | 61 |
| Kutup/Pikap/y0/iyi/s0 | TAKILMA | 2229 | 14° | 18° | 23% | 240 |
| Kutup/Pikap/y0/iyi/s0 | SON-FUEL | 2179 | -2° | -1° | 0% | 330 |
| Kutup/Pikap/y3/iyi/s0 | TAKILMA | 2237 | 15° | 16° | 55% | 215 |
| Kutup/Pikap/y3/iyi/s0 | TAKILMA | 2229 | 18° | 20° | 9% | 280 |
| Kutup/Pikap/y3/iyi/s0 | SON-FUEL | 2173 | 7° | 5° | 0% | 343 |
| Kutup/Traktör/y0/iyi/s0 | TAKILMA | 994 | 16° | 16° | 40% | 157 |
| Kutup/Traktör/y0/iyi/s0 | TAKILMA | 994 | 16° | 16° | 13% | 194 |
| Kutup/Traktör/y0/iyi/s0 | SON-FUEL | 984 | 3° | 1° | 0% | 243 |
| Kutup/Traktör/y3/iyi/s0 | TAKILMA | 1448 | 17° | 24° | 73% | 173 |
| Kutup/Traktör/y3/iyi/s0 | TAKILMA | 1422 | 17° | 17° | 21% | 265 |
| Kutup/Traktör/y3/iyi/s0 | SON-FUEL | 1368 | -2° | -1° | 0% | 351 |
| Kır yolu/Cip/y0/acemi/s1 | TERS | 1538 | 5° | 106° | 100% | 97 |
| Kır yolu/Cip/y0/acemi/s1 | SON-CRASH | 1546 | 8° | 157° | 99% | 98 |
| Kır yolu/Cip/y0/acemi/s2 | SON-CRASH | 1546 | 8° | 135° | 99% | 99 |
| Kır yolu/Cip/y0/acemi/s3 | SON-CRASH | 1546 | 8° | 127° | 99% | 100 |
| Kır yolu/Cip/y0/acemi/s4 | TERS | 1541 | 7° | 122° | 100% | 98 |
| Kır yolu/Cip/y0/acemi/s4 | SON-CRASH | 1543 | 8° | 120° | 100% | 98 |
| Kır yolu/Cip/y0/acemi/s5 | TERS | 1538 | 6° | 106° | 100% | 97 |
| Kır yolu/Cip/y0/acemi/s5 | SON-CRASH | 1546 | 8° | 149° | 99% | 98 |
| Kır yolu/Motosiklet/y0/iyi/s0 | TERS | 3087 | 12° | 113° | 89% | 186 |
| Kır yolu/Motosiklet/y0/iyi/s0 | SON-CRASH | 3095 | 1° | 110° | 88% | 187 |
| Kır yolu/Motosiklet/y3/iyi/s0 | TERS | 1373 | 15° | -85° | 83% | 78 |
| Kır yolu/Motosiklet/y3/iyi/s0 | SON-CRASH | 1371 | 15° | -94° | 82% | 79 |
| Kır yolu/Pikap/y0/iyi/s0 | TAKILMA | 11278 | 26° | 25° | 36% | 945 |
| Kır yolu/Pikap/y0/iyi/s0 | SON-FUEL | 11272 | -8° | -6° | 0% | 1023 |
| Kır yolu/Traktör/y0/iyi/s0 | SON-FUEL | 5598 | -1° | -2° | 0% | 734 |
| Mağara/Cip/y0/iyi/s0 | TAKILMA | 2914 | 24° | 24° | 60% | 220 |
| Mağara/Cip/y0/iyi/s0 | SON-FUEL | 2887 | -2° | -1° | 0% | 321 |
| Mağara/Cip/y3/iyi/s0 | TAKILMA | 1645 | 27° | 33° | 7% | 183 |
| Mağara/Cip/y3/iyi/s0 | SON-FUEL | 1640 | -2° | -1° | 0% | 227 |
| Mağara/Motosiklet/y0/iyi/s0 | TERS | 1404 | 8° | -93° | 85% | 84 |
| Mağara/Motosiklet/y0/iyi/s0 | TERS | 2895 | 43° | 143° | 76% | 231 |
| Mağara/Motosiklet/y0/iyi/s0 | SON-FUEL | 2887 | -2° | -1° | 0% | 330 |
| Mağara/Motosiklet/y3/iyi/s0 | TERS | 1388 | -23° | -123° | 90% | 83 |
| Mağara/Motosiklet/y3/iyi/s0 | TERS | 1646 | 35° | -68° | 77% | 101 |
| Mağara/Motosiklet/y3/iyi/s0 | TERS | 1683 | 11° | 111° | 63% | 119 |
| Mağara/Motosiklet/y3/iyi/s0 | SON-CRASH | 1688 | 22° | 131° | 63% | 120 |
| Mağara/Pikap/y0/iyi/s0 | SON-FUEL | 2887 | 1° | -0° | 0% | 341 |
| Mağara/Pikap/y3/iyi/s0 | SON-FUEL | 9254 | -3° | -3° | 0% | 927 |
| Mağara/Traktör/y0/iyi/s0 | SON-FUEL | 1397 | -0° | -0° | 0% | 311 |
| Mağara/Traktör/y3/iyi/s0 | TAKILMA | 2892 | 31° | 34° | 48% | 364 |
| Mağara/Traktör/y3/iyi/s0 | SON-FUEL | 2886 | -2° | -3° | 0% | 478 |
| Orman/Motosiklet/y0/iyi/s0 | TERS | 535 | 4° | 104° | 83% | 32 |
| Orman/Motosiklet/y0/iyi/s0 | SON-CRASH | 547 | -5° | 104° | 83% | 33 |
| Orman/Motosiklet/y3/iyi/s0 | TERS | 1252 | 18° | 118° | 97% | 75 |
| Orman/Motosiklet/y3/iyi/s0 | SON-CRASH | 1256 | 9° | 118° | 97% | 76 |
| Orman/Pikap/y0/iyi/s0 | TAKILMA | 10841 | 20° | 22° | 6% | 969 |
| Orman/Pikap/y0/iyi/s0 | SON-FUEL | 10823 | 3° | 2° | 0% | 1015 |
| Orman/Traktör/y0/iyi/s0 | SON-FUEL | 3041 | 0° | -3° | 0% | 444 |
| Orman/Traktör/y3/iyi/s0 | TAKILMA | 7181 | 31° | 34° | 66% | 837 |
| Orman/Traktör/y3/iyi/s0 | SON-FUEL | 7299 | 1° | -1° | 0% | 1001 |
| Plaj/Cip/y0/iyi/s0 | TAKILMA | 13039 | 20° | 24° | 5% | 1010 |
| Plaj/Cip/y0/iyi/s0 | SON-FUEL | 13017 | 1° | 1° | 0% | 1072 |
| Plaj/Motosiklet/y0/iyi/s0 | TERS | 1048 | -10° | 91° | 86% | 58 |
| Plaj/Motosiklet/y0/iyi/s0 | SON-CRASH | 1063 | -3° | 106° | 86% | 59 |
| Plaj/Motosiklet/y3/iyi/s0 | TERS | 1377 | -16° | 84° | 74% | 75 |
| Plaj/Motosiklet/y3/iyi/s0 | TERS | 1430 | -1° | -102° | 99% | 79 |
| Plaj/Motosiklet/y3/iyi/s0 | SON-CRASH | 1443 | 6° | -103° | 98% | 80 |
| Plaj/Pikap/y0/iyi/s0 | TAKILMA | 13030 | 21° | 22° | 45% | 1049 |
| Plaj/Pikap/y0/iyi/s0 | SON-FUEL | 13016 | -1° | -1° | 0% | 1162 |
| Plaj/Traktör/y0/iyi/s0 | TAKILMA | 8293 | 18° | 24° | 51% | 898 |
| Plaj/Traktör/y0/iyi/s0 | TAKILMA | 8293 | 24° | 25° | 18% | 943 |
| Plaj/Traktör/y0/iyi/s0 | SON-FUEL | 8240 | -0° | -0° | 0% | 1027 |
| Plaj/Traktör/y3/iyi/s0 | TAKILMA | 10011 | 26° | 27° | 67% | 1070 |
| Plaj/Traktör/y3/iyi/s0 | TAKILMA | 10011 | 26° | 28° | 41% | 1116 |
| Plaj/Traktör/y3/iyi/s0 | TAKILMA | 10011 | 26° | 28° | 4% | 1180 |
| Çöl/Cip/y0/iyi/s0 | TAKILMA | 4423 | 20° | 22° | 4% | 379 |
| Çöl/Cip/y0/iyi/s0 | SON-FUEL | 4349 | -4° | -4° | 0% | 426 |
| Çöl/Cip/y3/iyi/s0 | TAKILMA | 12636 | 28° | 30° | 18% | 1028 |
| Çöl/Cip/y3/iyi/s0 | SON-FUEL | 12583 | -1° | -0° | 0% | 1106 |
| Çöl/Motosiklet/y0/iyi/s0 | TERS | 1502 | 2° | 102° | 84% | 93 |
| Çöl/Motosiklet/y0/iyi/s0 | SON-CRASH | 1513 | 1° | 110° | 84% | 94 |
| Çöl/Motosiklet/y3/iyi/s0 | TAKILMA | 2330 | 31° | 35° | 59% | 182 |
| Çöl/Motosiklet/y3/iyi/s0 | TERS | 2551 | 14° | 115° | 38% | 209 |
| Çöl/Motosiklet/y3/iyi/s0 | SON-CRASH | 2560 | 24° | 134° | 38% | 210 |
| Çöl/Pikap/y0/iyi/s0 | SON-FUEL | 4764 | -6° | -6° | 0% | 487 |
| Çöl/Pikap/y3/iyi/s0 | TAKILMA | 7272 | 23° | 27° | 32% | 620 |
| Çöl/Pikap/y3/iyi/s0 | SON-FUEL | 7250 | -1° | -1° | 0% | 713 |
| Çöl/Traktör/y0/iyi/s0 | TAKILMA | 1952 | 19° | 20° | 29% | 273 |
| Çöl/Traktör/y0/iyi/s0 | SON-FUEL | 1894 | 1° | 1° | 0% | 372 |
| Çöl/Traktör/y3/iyi/s0 | TAKILMA | 3792 | 24° | 24° | 92% | 408 |
| Çöl/Traktör/y3/iyi/s0 | TAKILMA | 3771 | 25° | 26° | 56% | 475 |
| Çöl/Traktör/y3/iyi/s0 | TAKILMA | 3768 | 22° | 24° | 10% | 558 |
| Çöl/Traktör/y3/iyi/s0 | SON-FUEL | 3759 | 0° | 0° | 0% | 616 |
| Şantiye/Cip/y0/iyi/s0 | SON-FUEL | 9336 | -1° | -1° | 0% | 736 |
| Şantiye/Cip/y3/iyi/s0 | SON-FUEL | 9703 | -6° | -2° | 0% | 739 |
| Şantiye/Motosiklet/y0/iyi/s0 | TERS | 1532 | -3° | -103° | 95% | 85 |
| Şantiye/Motosiklet/y0/iyi/s0 | SON-CRASH | 1563 | -18° | -127° | 92% | 88 |
| Şantiye/Motosiklet/y3/iyi/s0 | TERS | 1568 | -19° | 81° | 95% | 89 |
| Şantiye/Motosiklet/y3/iyi/s0 | SON-CRASH | 1580 | -19° | 90° | 95% | 90 |
| Şantiye/Pikap/y0/iyi/s0 | SON-FUEL | 8688 | -6° | -5° | 0% | 771 |
| Şantiye/Traktör/y0/iyi/s0 | TAKILMA | 4725 | 21° | 31° | 71% | 510 |
| Şantiye/Traktör/y0/iyi/s0 | SON-FUEL | 4643 | -0° | -1° | 0% | 702 |

## 6. Yakıt: menzil ve bidon aralığı

Menzil = depo / ölçülen yakıt harcaması (m başına). Bu değer tam gazla ve yokuşlarla bu sürücünün gerçek ortalamasıdır; bidon aralığı menzilin çok altında olmalı.

| Harita (jeep, y0) | Haritadaki bidon aralığı (m) | Menzil (m) | Aralık/menzil | En uzun gerçek aralık (m) | En düşük yakıt (alırken) |
|---|---|---|---|---|---|
| Kır yolu | 450 | 1271 | 35% | 919 | 25% |
| Çöl | 500 | 1042 | 48% | 994 | 20% |
| Kutup | 450 | 854 | 53% | 469 | 61% |
| Ay | 500 | 1223 | 41% | - | - |
| Orman | 500 | 1241 | 40% | 537 | 55% |
| Dağ | 400 | 1140 | 35% | 423 | 65% |
| Plaj | 600 | 1176 | 51% | 1250 | 0% |
| Mağara | 450 | 976 | 46% | 868 | 30% |
| Bataklık | 550 | 1044 | 53% | 584 | 49% |
| Şantiye | 600 | 1304 | 46% | 620 | 50% |

## 7. En çok yakıt harcatan yokuşlar (jeep, geliştirme yok)

| Harita | x aralığı (m) | Uzunluk (m) | Yükseklik (m) | En dik eğim | Depo payı |
|---|---|---|---|---|---|
| Kır yolu | 13525-13630 | 105 | 25.0 | 38° | 12% |
| Kır yolu | 6200-6285 | 85 | 27.1 | 41° | 12% |
| Kır yolu | 10305-10415 | 110 | 23.9 | 42° | 11% |
| Çöl | 2505-2640 | 135 | 24.8 | 32° | 11% |
| Çöl | 3555-3665 | 110 | 25.0 | 36° | 11% |
| Çöl | 4180-4275 | 95 | 31.0 | 35° | 11% |
| Kutup | 1565-1700 | 135 | 21.9 | 30° | 13% |
| Kutup | 1990-2095 | 105 | 26.2 | 29° | 11% |
| Kutup | 570-705 | 135 | 15.2 | 22° | 10% |
| Ay | 950-1035 | 85 | 18.3 | 22° | 7% |
| Ay | 635-720 | 85 | 15.9 | 18° | 7% |
| Ay | 805-860 | 55 | 14.3 | 25° | 7% |
| Orman | 9015-9085 | 70 | 17.5 | 45° | 14% |
| Orman | 13430-13500 | 70 | 21.5 | 45° | 11% |
| Orman | 7625-7710 | 85 | 22.4 | 45° | 11% |
| Dağ | 2860-2970 | 110 | 29.6 | 50° | 12% |
| Dağ | 2525-2635 | 110 | 29.9 | 46° | 12% |
| Dağ | 1850-1960 | 110 | 26.5 | 46° | 12% |
| Plaj | 11435-11515 | 80 | 21.5 | 32° | 12% |
| Plaj | 9205-9290 | 85 | 21.0 | 34° | 10% |
| Plaj | 11280-11350 | 70 | 21.7 | 42° | 10% |
| Mağara | 1400-1540 | 140 | 29.1 | 39° | 12% |
| Mağara | 1815-1955 | 140 | 31.9 | 38° | 12% |
| Mağara | 1645-1725 | 80 | 32.4 | 45° | 12% |
| Bataklık | 4440-4520 | 80 | 26.2 | 38° | 13% |
| Bataklık | 4210-4340 | 130 | 25.1 | 32° | 13% |
| Bataklık | 4610-4740 | 130 | 25.6 | 34° | 12% |
| Şantiye | 9105-9235 | 130 | 30.0 | 45° | 13% |
| Şantiye | 8095-8225 | 130 | 28.4 | 36° | 13% |
| Şantiye | 5665-5795 | 130 | 23.9 | 38° | 12% |

## 8. Duyurular ve karıştırıcı

Mixer.voiceCount en yüksek: **17** (sınır 60). Koşu başına duyuru: ortalama 134.0; en uzun sessizlik: 110 sn (Dağ/Traktör/y3/iyi/s0).

| Koşu | En uzun sessizlik (sn) | Duyuru sayısı | Süre (sn) |
|---|---|---|---|
| Dağ/Traktör/y3/iyi/s0 | 110 | 92 | 667 |
| Mağara/Pikap/y3/iyi/s0 | 100 | 238 | 927 |
| Bataklık/Pikap/y3/iyi/s0 | 97 | 144 | 757 |
| Çöl/Traktör/y3/iyi/s0 | 89 | 71 | 616 |
| Dağ/Traktör/y0/iyi/s0 | 88 | 33 | 336 |
| Şantiye/Traktör/y0/iyi/s0 | 84 | 62 | 702 |

## 9. Ekonomi

| Harita | Araç | Geliştirme | Koşu başı altın | Süre (sn) | Sıradaki araç: koşu sayısı | Sıradaki harita: koşu sayısı |
|---|---|---|---|---|---|---|
| Kır yolu | Cip | y0 | 142303 | 1200 | Motosiklet (8000): 1 | Çöl (5000): 1 |
| Kır yolu | Cip | y3 | 145195 | 1200 | Motosiklet (8000): 1 | Çöl (5000): 1 |
| Kır yolu | Motosiklet | y0 | 21495 | 187 | Pikap (15000): 1 | Çöl (5000): 1 |
| Kır yolu | Motosiklet | y3 | 4445 | 79 | Pikap (15000): 4 | Çöl (5000): 2 |
| Kır yolu | Pikap | y0 | 105145 | 1023 | Traktör (25000): 1 | Çöl (5000): 1 |
| Kır yolu | Pikap | y3 | 138295 | 1200 | Traktör (25000): 1 | Çöl (5000): 1 |
| Kır yolu | Traktör | y0 | 49195 | 734 | Kum arabası (35000): 1 | Çöl (5000): 1 |
| Kır yolu | Traktör | y3 | 104395 | 1200 | Kum arabası (35000): 1 | Çöl (5000): 1 |
| Çöl | Cip | y0 | 34490 | 426 | Motosiklet (8000): 1 | Kutup (12000): 1 |
| Çöl | Cip | y3 | 108790 | 1106 | Motosiklet (8000): 1 | Kutup (12000): 1 |
| Çöl | Motosiklet | y0 | 4940 | 94 | Pikap (15000): 4 | Kutup (12000): 3 |
| Çöl | Motosiklet | y3 | 15490 | 210 | Pikap (15000): 1 | Kutup (12000): 1 |
| Çöl | Pikap | y0 | 38690 | 487 | Traktör (25000): 1 | Kutup (12000): 1 |
| Çöl | Pikap | y3 | 60890 | 713 | Traktör (25000): 1 | Kutup (12000): 1 |
| Çöl | Traktör | y0 | 9840 | 372 | Kum arabası (35000): 4 | Kutup (12000): 2 |
| Çöl | Traktör | y3 | 27040 | 616 | Kum arabası (35000): 2 | Kutup (12000): 1 |
| Kutup | Cip | y0 | 9645 | 290 | Motosiklet (8000): 1 | Ay (20000): 3 |
| Kutup | Cip | y3 | 16795 | 389 | Motosiklet (8000): 1 | Ay (20000): 2 |
| Kutup | Motosiklet | y0 | 2445 | 63 | Pikap (15000): 7 | Ay (20000): 9 |
| Kutup | Motosiklet | y3 | 2445 | 61 | Pikap (15000): 7 | Ay (20000): 9 |
| Kutup | Pikap | y0 | 9645 | 330 | Traktör (25000): 3 | Ay (20000): 3 |
| Kutup | Pikap | y3 | 9645 | 343 | Traktör (25000): 3 | Ay (20000): 3 |
| Kutup | Traktör | y0 | 2445 | 243 | Kum arabası (35000): 15 | Ay (20000): 9 |
| Kutup | Traktör | y3 | 4045 | 351 | Kum arabası (35000): 9 | Ay (20000): 5 |
| Ay | Cip | y0 | 3350 | 130 | Motosiklet (8000): 3 | Orman (30000): 9 |
| Ay | Cip | y3 | 23670 | 621 | Motosiklet (8000): 1 | Orman (30000): 2 |
| Ay | Motosiklet | y0 | 1170 | 95 | Pikap (15000): 13 | Orman (30000): 26 |
| Ay | Motosiklet | y3 | 695 | 81 | Pikap (15000): 22 | Orman (30000): 44 |
| Ay | Pikap | y0 | 24195 | 536 | Traktör (25000): 2 | Orman (30000): 2 |
| Ay | Pikap | y3 | 5295 | 325 | Traktör (25000): 5 | Orman (30000): 6 |
| Ay | Traktör | y0 | 45245 | 962 | Kum arabası (35000): 1 | Orman (30000): 1 |
| Ay | Traktör | y3 | 72395 | 1200 | Kum arabası (35000): 1 | Orman (30000): 1 |
| Orman | Cip | y0 | 143160 | 1200 | Motosiklet (8000): 1 | Dağ (40000): 1 |
| Orman | Cip | y3 | 147810 | 1200 | Motosiklet (8000): 1 | Dağ (40000): 1 |
| Orman | Motosiklet | y0 | 360 | 33 | Pikap (15000): 42 | Dağ (40000): 112 |
| Orman | Motosiklet | y3 | 3560 | 76 | Pikap (15000): 5 | Dağ (40000): 12 |
| Orman | Pikap | y0 | 96410 | 1015 | Traktör (25000): 1 | Dağ (40000): 1 |
| Orman | Pikap | y3 | 140760 | 1200 | Traktör (25000): 1 | Dağ (40000): 1 |
| Orman | Traktör | y0 | 20860 | 444 | Kum arabası (35000): 2 | Dağ (40000): 2 |
| Orman | Traktör | y3 | 63460 | 1001 | Kum arabası (35000): 1 | Dağ (40000): 1 |
| Dağ | Cip | y0 | 28790 | 321 | Motosiklet (8000): 1 | Plaj (50000): 2 |
| Dağ | Cip | y3 | 137540 | 1200 | Motosiklet (8000): 1 | Plaj (50000): 1 |
| Dağ | Motosiklet | y0 | 80 | 9 | Pikap (15000): 188 | Plaj (50000): 625 |
| Dağ | Motosiklet | y3 | 80 | 9 | Pikap (15000): 188 | Plaj (50000): 625 |
| Dağ | Pikap | y0 | 17240 | 358 | Traktör (25000): 2 | Plaj (50000): 3 |
| Dağ | Pikap | y3 | 25740 | 423 | Traktör (25000): 1 | Plaj (50000): 2 |
| Dağ | Traktör | y0 | 5140 | 336 | Kum arabası (35000): 7 | Plaj (50000): 10 |
| Dağ | Traktör | y3 | 28240 | 667 | Kum arabası (35000): 2 | Plaj (50000): 2 |
| Plaj | Cip | y0 | 116675 | 1072 | Motosiklet (8000): 1 | Mağara (60000): 1 |
| Plaj | Cip | y3 | 149075 | 1200 | Motosiklet (8000): 1 | Mağara (60000): 1 |
| Plaj | Motosiklet | y0 | 2025 | 59 | Pikap (15000): 8 | Mağara (60000): 30 |
| Plaj | Motosiklet | y3 | 3825 | 80 | Pikap (15000): 4 | Mağara (60000): 16 |
| Plaj | Pikap | y0 | 117525 | 1162 | Traktör (25000): 1 | Mağara (60000): 1 |
| Plaj | Pikap | y3 | 143575 | 1200 | Traktör (25000): 1 | Mağara (60000): 1 |
| Plaj | Traktör | y0 | 74125 | 1027 | Kum arabası (35000): 1 | Mağara (60000): 1 |
| Plaj | Traktör | y3 | 89225 | 1200 | Kum arabası (35000): 1 | Mağara (60000): 1 |
| Mağara | Cip | y0 | 13365 | 321 | Motosiklet (8000): 1 | Bataklık (70000): 6 |
| Mağara | Cip | y3 | 6165 | 227 | Motosiklet (8000): 2 | Bataklık (70000): 12 |
| Mağara | Motosiklet | y0 | 13115 | 330 | Pikap (15000): 2 | Bataklık (70000): 6 |
| Mağara | Motosiklet | y3 | 6215 | 120 | Pikap (15000): 3 | Bataklık (70000): 12 |
| Mağara | Pikap | y0 | 13665 | 341 | Traktör (25000): 2 | Bataklık (70000): 6 |
| Mağara | Pikap | y3 | 75540 | 927 | Traktör (25000): 1 | Bataklık (70000): 1 |
| Mağara | Traktör | y0 | 4265 | 311 | Kum arabası (35000): 9 | Bataklık (70000): 17 |
| Mağara | Traktör | y3 | 14565 | 478 | Kum arabası (35000): 3 | Bataklık (70000): 5 |
| Bataklık | Cip | y0 | 39630 | 444 | Motosiklet (8000): 1 | Şantiye (90000): 3 |
| Bataklık | Cip | y3 | 39680 | 459 | Motosiklet (8000): 1 | Şantiye (90000): 3 |
| Bataklık | Motosiklet | y0 | 2105 | 57 | Pikap (15000): 8 | Şantiye (90000): 43 |
| Bataklık | Motosiklet | y3 | 2430 | 62 | Pikap (15000): 7 | Şantiye (90000): 38 |
| Bataklık | Pikap | y0 | 40130 | 490 | Traktör (25000): 1 | Şantiye (90000): 3 |
| Bataklık | Pikap | y3 | 60430 | 757 | Traktör (25000): 1 | Şantiye (90000): 2 |
| Bataklık | Traktör | y0 | 19630 | 428 | Kum arabası (35000): 2 | Şantiye (90000): 5 |
| Bataklık | Traktör | y3 | 33730 | 651 | Kum arabası (35000): 2 | Şantiye (90000): 3 |
| Şantiye | Cip | y0 | 84510 | 736 | Motosiklet (8000): 1 | - |
| Şantiye | Cip | y3 | 86760 | 739 | Motosiklet (8000): 1 | - |
| Şantiye | Motosiklet | y0 | 4860 | 88 | Pikap (15000): 4 | - |
| Şantiye | Motosiklet | y3 | 4960 | 90 | Pikap (15000): 4 | - |
| Şantiye | Pikap | y0 | 79310 | 771 | Traktör (25000): 1 | - |
| Şantiye | Pikap | y3 | 144210 | 1200 | Traktör (25000): 1 | - |
| Şantiye | Traktör | y0 | 40160 | 702 | Kum arabası (35000): 1 | - |
| Şantiye | Traktör | y3 | 101260 | 1200 | Kum arabası (35000): 1 | - |

Kır yolu + jeep ortalaması (6 koşu): 142303 altın/koşu. Jeep için 4 geliştirmenin 3. seviyeye maliyeti: 8256 altın = 1 koşu; motosiklet (8000): 1 koşu; Çöl haritası (5000): 1 koşu.

## 10. Sorunlar (ciddiyet sırasıyla)

1. **[Yüksek]** İlk 400 m içinde biten 2 koşu: Dağ/Motosiklet/y0/iyi/s0 119 m CRASH; Dağ/Motosiklet/y3/iyi/s0 119 m CRASH.
2. **[Yüksek]** Yakıt bitişiyle sonlanan koşu: 46/90. Haritalara göre: Kır yolu 2, Çöl 6, Kutup 6, Ay 4, Orman 3, Dağ 5, Plaj 3, Mağara 7, Bataklık 6, Şantiye 4. Yakıt bitişi oyuncuya "yakıt bitti" olarak söylenir; asıl neden çoğu zaman takılma (yokuş çıkılamaması).
3. **[Yüksek]** Takılma 52 kez görüldü. En sık noktalar: Kutup x≈2200 m (eğim 15°, 5 kez); Ay x≈3800 m (eğim 44°, 4 kez); Çöl x≈3750 m (eğim 24°, 3 kez); Plaj x≈10000 m (eğim 26°, 3 kez); Kutup x≈950 m (eğim 16°, 2 kez); Kutup x≈1400 m (eğim 17°, 2 kez).
4. **[Yüksek]** Yerde göreli açı 100°'yi geçen (ters dönme) olay 27. En sık: Kır yolu x≈1500 m (eğim 5°, açı 106°, 3 kez); Kutup x≈1000 m (eğim 10°, açı 110°, 2 kez); Dağ x≈100 m (eğim -12°, açı 89°, 2 kez); Mağara x≈1600 m (eğim 35°, açı -68°, 2 kez); Şantiye x≈1500 m (eğim -3°, açı -103°, 2 kez).
5. **[Orta]** Bidona %5'ten az yakıtla ulaşılan 1 koşu var (marj çok dar): Plaj/Cip/y0/iyi/s0.
6. **[Orta]** 20 dk sınırına dayanan 19 koşu (oyun bitmedi, araç hâlâ ilerliyor ya da yavaş): Kır yolu/Cip/y0/iyi/s0 16132 m; Kır yolu/Cip/y3/iyi/s0 16616 m; Kır yolu/Pikap/y3/iyi/s0 15676 m; Kır yolu/Traktör/y3/iyi/s0 11277 m; Ay/Traktör/y3/iyi/s0 8522 m; Orman/Cip/y0/iyi/s0 15457 m.
7. **[Orta]** Acemi sürücü (yalnızca takla bipiyle) Kır yolunda ortalama 1545 m gidiyor, iyi sürücü 16168 m. Denge yardımı ve eğim duyuruları olmadan ilk oyun çok zor.
8. **[Düşük]** 30 sn'den uzun duyurusuz süre olan 48 koşu (en uzunu 110 sn). Oyuncu ilerlediğini bilemiyor olabilir: mesafe duyurusu varsayılan kapalı.

## 11. Ayar önerileri

- Kutup x≈2200 m: 5 takılma/ters olayı, zemin eğimi 15°. Bu parçanın dikliğini 10° civarına indirin (`maxSlopeAt` ya da bölgeye özel genlik).
- Ay x≈3800 m: 4 takılma/ters olayı, zemin eğimi 44°. Bu parçanın dikliğini 25° civarına indirin (`maxSlopeAt` ya da bölgeye özel genlik).
- Çöl x≈3750 m: 3 takılma/ters olayı, zemin eğimi 24°. Bu parçanın dikliğini 19° civarına indirin (`maxSlopeAt` ya da bölgeye özel genlik).
- Plaj x≈10000 m: 3 takılma/ters olayı, zemin eğimi 26°. Bu parçanın dikliğini 21° civarına indirin (`maxSlopeAt` ya da bölgeye özel genlik).
- Kır yolu x≈1500 m: 3 takılma/ters olayı, zemin eğimi 5°. Bu parçanın dikliğini 0° civarına indirin (`maxSlopeAt` ya da bölgeye özel genlik).
