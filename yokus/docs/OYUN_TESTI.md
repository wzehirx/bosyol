# Yokuş Ustası: oyun testi raporu

Bu rapor `bash yokus/android/test/run.sh PlayTest` tarafından otomatik üretilir (elle düzenlemeyin, yeniden çalıştırınca yazılır). Sürücü `BlindDriver`: yalnızca sesle algılanabilen bilgilere bakar (eğim açısı, 10-15 m ilerideki radar eğimi, duyurular, havada olma, yakıt uyarıları, hız), 0.25 sn tepki gecikmesi ve küçük gürültüyle oynar. Strateji: tam gaz, burun zemin eğiminden 35° fazla kalkarsa gazı kes, havada burnu iniş eğimine yatır, takılırsa kısa geri git. "Acemi" sürücü yalnızca takla bipi (>70°) çalınca gazı keser.

- Koşu sayısı: 90 (matris 80, tohum 10); oyun zamanı sınırı 20 dk; gerçek süre 152 sn.
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
| Kır yolu | Cip | 7934 | FUEL | 644 | 17 | 61% | 79° | 0 | 71745 | 350 | 0 |
| Kır yolu | Motosiklet | 2828 | CRASH | 171 | 6 | 70% | 128° | 0 | 17095 | 0 | 0 |
| Kır yolu | Pikap | 15061 | SÜRE | 1200 | 32 | 34% | 70° | 0 | 134295 | 700 | 0 |
| Kır yolu | Traktör | 5680 | FUEL | 730 | 12 | 60% | 45° | 0 | 49295 | 0 | 0 |
| Çöl | Cip | 4451 | FUEL | 426 | 8 | 20% | 73° | 0 | 34590 | 150 | 0 |
| Çöl | Motosiklet | 2098 | CRASH | 127 | 4 | 67% | 113° | 0 | 11240 | 0 | 0 |
| Çöl | Pikap | 4864 | FUEL | 488 | 9 | 62% | 48° | 0 | 38690 | 150 | 0 |
| Çöl | Traktör | 1978 | FUEL | 373 | 4 | 61% | 37° | 0 | 9840 | 0 | 1 |
| Kutup | Cip | 2256 | FUEL | 290 | 5 | 60% | 42° | 0 | 9645 | 0 | 0 |
| Kutup | Motosiklet | 2250 | FUEL | 273 | 5 | 63% | 74° | 0 | 9645 | 0 | 1 |
| Kutup | Pikap | 2252 | FUEL | 330 | 5 | 67% | 43° | 0 | 9645 | 0 | 0 |
| Kutup | Traktör | 1016 | FUEL | 243 | 2 | 65% | 33° | 0 | 2445 | 0 | 2 |
| Ay | Cip | 1211 | CRASH | 125 | 0 | - | 175° | 0 | 3370 | 1100 | 0 |
| Ay | Motosiklet | 1599 | FUEL | 309 | 3 | 21% | 119° | 0 | 5245 | 1100 | 0 |
| Ay | Pikap | 3815 | FUEL | 623 | 6 | 21% | 73° | 0 | 24345 | 4725 | 1 |
| Ay | Traktör | 4908 | FUEL | 824 | 10 | 59% | 67° | 0 | 36720 | 4950 | 3 |
| Orman | Cip | 9029 | FUEL | 783 | 18 | 57% | 63° | 0 | 76210 | 250 | 0 |
| Orman | Motosiklet | 443 | CRASH | 26 | 1 | 82% | 117° | 0 | 275 | 0 | 0 |
| Orman | Pikap | 10114 | FUEL | 998 | 21 | 63% | 52° | 0 | 89110 | 50 | 0 |
| Orman | Traktör | 3064 | FUEL | 454 | 6 | 57% | 47° | 0 | 20860 | 0 | 1 |
| Dağ | Cip | 8817 | FUEL | 843 | 20 | 30% | 72° | 0 | 76740 | 200 | 0 |
| Dağ | Motosiklet | 1137 | CRASH | 70 | 3 | 74% | 133° | 0 | 3090 | 0 | 0 |
| Dağ | Pikap | 2583 | FUEL | 359 | 7 | 73% | 49° | 0 | 17290 | 50 | 0 |
| Dağ | Traktör | 1571 | FUEL | 336 | 4 | 69% | 46° | 0 | 5140 | 0 | 1 |
| Plaj | Cip | 13056 | FUEL | 1054 | 21 | 0% | 65° | 0 | 116675 | 350 | 0 |
| Plaj | Motosiklet | 3573 | CRASH | 212 | 6 | 59% | 114° | 0 | 28775 | 50 | 0 |
| Plaj | Pikap | 13055 | FUEL | 1138 | 22 | 54% | 59° | 0 | 117625 | 0 | 0 |
| Plaj | Traktör | 8312 | FUEL | 1028 | 14 | 52% | 47° | 0 | 74125 | 0 | 2 |
| Mağara | Cip | 1654 | FUEL | 198 | 3 | 65% | 47° | 0 | 6140 | 0 | 0 |
| Mağara | Motosiklet | 988 | CRASH | 56 | 2 | 71% | 128° | 0 | 2390 | 0 | 0 |
| Mağara | Pikap | 2909 | FUEL | 340 | 5 | 41% | 55° | 0 | 13665 | 0 | 0 |
| Mağara | Traktör | 1475 | FUEL | 312 | 3 | 65% | 41° | 0 | 4265 | 0 | 0 |
| Bataklık | Cip | 4897 | FUEL | 455 | 9 | 51% | 65° | 0 | 39730 | 200 | 0 |
| Bataklık | Motosiklet | 4284 | FUEL | 407 | 8 | 46% | 97° | 0 | 32680 | 50 | 0 |
| Bataklık | Pikap | 4901 | FUEL | 493 | 9 | 59% | 47° | 0 | 40030 | 0 | 0 |
| Bataklık | Traktör | 2867 | FUEL | 428 | 5 | 55% | 42° | 0 | 19630 | 0 | 0 |
| Şantiye | Cip | 9344 | FUEL | 739 | 14 | 51% | 57° | 0 | 83760 | 500 | 0 |
| Şantiye | Motosiklet | 8745 | CRASH | 633 | 14 | 13% | 122° | 0 | 78260 | 400 | 0 |
| Şantiye | Pikap | 8776 | FUEL | 771 | 14 | 57% | 48° | 0 | 79110 | 50 | 0 |
| Şantiye | Traktör | 8779 | FUEL | 1147 | 15 | 46% | 47° | 0 | 79660 | 0 | 0 |

## 3. Harita × araç, geliştirme hepsi 3. seviye (iyi sürücü)

| Harita | Araç | Mesafe (m) | Bitiş | Süre (sn) | Bidon | En az yakıt | Tepe açı | Takla | Altın | Bonus | Geri vites |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Kır yolu | Cip | 16539 | SÜRE | 1200 | 34 | 46% | 73° | 0 | 144945 | 1150 | 0 |
| Kır yolu | Motosiklet | 1562 | CRASH | 89 | 3 | 78% | 125° | 0 | 4995 | 0 | 0 |
| Kır yolu | Pikap | 15701 | SÜRE | 1200 | 34 | 54% | 67° | 0 | 138945 | 750 | 0 |
| Kır yolu | Traktör | 11299 | SÜRE | 1200 | 25 | 70% | 47° | 0 | 105695 | 0 | 0 |
| Çöl | Cip | 12653 | FUEL | 1107 | 23 | 30% | 69° | 0 | 108940 | 700 | 1 |
| Çöl | Motosiklet | 3568 | CRASH | 244 | 7 | 59% | 133° | 0 | 25640 | 200 | 0 |
| Çöl | Pikap | 7282 | FUEL | 713 | 13 | 42% | 49° | 0 | 60890 | 250 | 1 |
| Çöl | Traktör | 3790 | FUEL | 616 | 8 | 67% | 44° | 0 | 27040 | 0 | 1 |
| Kutup | Cip | 3251 | FUEL | 389 | 7 | 68% | 46° | 0 | 16795 | 50 | 0 |
| Kutup | Motosiklet | 3239 | FUEL | 365 | 7 | 67% | 102° | 0 | 17345 | 0 | 1 |
| Kutup | Pikap | 2259 | FUEL | 354 | 5 | 75% | 43° | 0 | 9645 | 0 | 2 |
| Kutup | Traktör | 1450 | FUEL | 351 | 3 | 70% | 38° | 0 | 4045 | 0 | 2 |
| Ay | Cip | 3817 | FUEL | 620 | 6 | 21% | 75° | 0 | 22895 | 4850 | 2 |
| Ay | Motosiklet | 838 | CRASH | 153 | 2 | 52% | 106° | 0 | 2245 | 1000 | 0 |
| Ay | Pikap | 3818 | FUEL | 615 | 6 | 41% | 84° | 0 | 22995 | 4650 | 2 |
| Ay | Traktör | 8492 | SÜRE | 1200 | 15 | 32% | 80° | 0 | 68845 | 10075 | 0 |
| Orman | Cip | 15899 | SÜRE | 1200 | 31 | 46% | 59° | 0 | 147360 | 800 | 0 |
| Orman | Motosiklet | 442 | CRASH | 25 | 1 | 86% | 117° | 0 | 275 | 0 | 0 |
| Orman | Pikap | 15150 | SÜRE | 1200 | 31 | 73% | 50° | 0 | 140260 | 100 | 0 |
| Orman | Traktör | 10867 | SÜRE | 1200 | 22 | 67% | 48° | 0 | 100560 | 0 | 0 |
| Dağ | Cip | 14684 | SÜRE | 1200 | 34 | 51% | 94° | 0 | 134940 | 800 | 0 |
| Dağ | Motosiklet | 619 | CRASH | 37 | 1 | 87% | 105° | 0 | 1190 | 0 | 0 |
| Dağ | Pikap | 5945 | FUEL | 656 | 15 | 77% | 53° | 0 | 51840 | 100 | 0 |
| Dağ | Traktör | 2910 | FUEL | 491 | 7 | 75% | 52° | 0 | 22040 | 0 | 1 |
| Plaj | Cip | 14942 | FUEL | 1180 | 24 | 27% | 70° | 0 | 135075 | 650 | 1 |
| Plaj | Motosiklet | 4480 | CRASH | 269 | 8 | 68% | 120° | 0 | 37125 | 200 | 0 |
| Plaj | Pikap | 15695 | SÜRE | 1200 | 26 | 67% | 52° | 0 | 142775 | 50 | 0 |
| Plaj | Traktör | 8603 | FUEL | 1100 | 15 | 64% | 47° | 0 | 77625 | 0 | 1 |
| Mağara | Cip | 15192 | SÜRE | 1200 | 33 | 50% | 69° | 0 | 129040 | 1300 | 0 |
| Mağara | Motosiklet | 1105 | CRASH | 65 | 2 | 79% | 116° | 0 | 2765 | 0 | 0 |
| Mağara | Pikap | 14363 | SÜRE | 1200 | 31 | 57% | 62° | 0 | 122240 | 200 | 0 |
| Mağara | Traktör | 2902 | FUEL | 483 | 6 | 69% | 52° | 0 | 14565 | 0 | 1 |
| Bataklık | Cip | 9932 | FUEL | 898 | 18 | 59% | 61° | 0 | 87530 | 400 | 1 |
| Bataklık | Motosiklet | 2637 | CRASH | 160 | 5 | 70% | 100° | 0 | 16630 | 0 | 0 |
| Bataklık | Pikap | 5918 | FUEL | 659 | 11 | 68% | 46° | 0 | 50530 | 0 | 0 |
| Bataklık | Traktör | 4279 | FUEL | 650 | 8 | 64% | 46° | 0 | 33730 | 0 | 0 |
| Şantiye | Cip | 9799 | FUEL | 742 | 13 | 29% | 60° | 0 | 86360 | 600 | 0 |
| Şantiye | Motosiklet | 2369 | CRASH | 139 | 4 | 71% | 90° | 0 | 16660 | 0 | 0 |
| Şantiye | Pikap | 15340 | SÜRE | 1200 | 24 | 39% | 51° | 0 | 144010 | 350 | 0 |
| Şantiye | Traktör | 11118 | SÜRE | 1200 | 19 | 63% | 49° | 0 | 101260 | 0 | 0 |

## 4. Kır yolu, jeep, geliştirme yok: tohumlar

| Sürücü | Tohum | Mesafe (m) | Bitiş | Süre (sn) | Bidon | Bidon konumları (m, yakıt%) | Tepe açı | Altın | Duyuru | En uzun sessizlik (sn) |
|---|---|---|---|---|---|---|---|---|---|---|
| iyi | 0 | 7934 | FUEL | 644 | 17 | 241 (80%), 676 (69%), 1089 (71%), 1531 (69%), 1976 (66%), 2420 (68%), 2893 (65%), 3362 (65%), 3849 (63%), 4301 (65%), 4786 (61%), 5235 (69%), 5685 (64%), 6141 (65%), 6563 (64%), 7015 (63%), 7501 (63%) | 79° | 71745 | 164 | 34 |
| iyi | 1 | 16082 | SÜRE | 1200 | 33 | 241 (80%), 676 (69%), 1089 (70%), 1976 (35%), 2420 (68%), 2893 (65%), 3363 (66%), 3849 (64%), 4302 (65%), 4786 (60%), 5235 (69%), 5685 (65%), 6141 (66%), 6563 (64%), 7015 (62%), 7501 (63%), 7984 (60%), 8448 (64%), 8923 (61%), 9390 (62%), 9858 (61%), 10344 (64%), 10760 (66%), 11235 (62%), 11647 (69%), 12102 (63%), 13021 (25%), 13435 (68%), 13857 (65%), 14328 (63%), 14817 (59%), 15238 (66%), 15648 (67%) | 82° | 141645 | 409 | 14 |
| iyi | 2 | 16051 | SÜRE | 1200 | 33 | 241 (80%), 676 (69%), 1089 (70%), 1976 (35%), 2420 (67%), 2893 (66%), 3362 (66%), 3849 (63%), 4301 (65%), 4786 (61%), 5235 (69%), 5685 (65%), 6141 (65%), 6563 (62%), 7015 (62%), 7501 (63%), 7984 (60%), 8448 (62%), 8923 (61%), 9391 (64%), 9858 (61%), 10344 (63%), 10760 (65%), 11235 (63%), 11647 (69%), 12102 (63%), 13021 (26%), 13435 (68%), 13857 (65%), 14328 (64%), 14817 (60%), 15238 (66%), 15649 (67%) | 73° | 140845 | 410 | 14 |
| iyi | 3 | 16060 | SÜRE | 1200 | 33 | 241 (80%), 676 (69%), 1089 (70%), 1976 (34%), 2420 (68%), 2893 (66%), 3363 (66%), 3849 (63%), 4301 (65%), 4786 (60%), 5235 (69%), 5685 (65%), 6141 (65%), 6563 (63%), 7015 (61%), 7501 (63%), 7984 (59%), 8448 (64%), 8923 (61%), 9390 (64%), 9858 (61%), 10344 (63%), 10760 (65%), 11235 (63%), 11647 (68%), 12102 (63%), 13021 (25%), 13435 (68%), 13857 (65%), 14328 (63%), 14817 (59%), 15238 (67%), 15648 (66%) | 79° | 140245 | 405 | 14 |
| iyi | 4 | 12428 | FUEL | 1029 | 27 | 241 (80%), 676 (69%), 1089 (71%), 1531 (68%), 1976 (66%), 2420 (67%), 2893 (65%), 3363 (66%), 3849 (63%), 4301 (66%), 4786 (61%), 5235 (69%), 5685 (64%), 6141 (65%), 6563 (64%), 7015 (61%), 7501 (63%), 7984 (59%), 8448 (64%), 8923 (62%), 9390 (62%), 9858 (61%), 10344 (63%), 10760 (66%), 11235 (61%), 11647 (68%), 12102 (63%) | 80° | 113395 | 298 | 60 |
| iyi | 5 | 16046 | SÜRE | 1200 | 33 | 241 (80%), 676 (70%), 1089 (70%), 1976 (34%), 2419 (68%), 2893 (65%), 3363 (66%), 3849 (63%), 4301 (67%), 4786 (60%), 5235 (70%), 5685 (65%), 6141 (65%), 6563 (64%), 7015 (61%), 7501 (63%), 7984 (59%), 8448 (64%), 8923 (61%), 9391 (61%), 9858 (61%), 10344 (63%), 10760 (66%), 11235 (62%), 11647 (68%), 12102 (63%), 13021 (25%), 13435 (68%), 13857 (63%), 14328 (63%), 14817 (59%), 15238 (66%), 15649 (67%) | 73° | 140345 | 405 | 14 |
| acemi | 1 | 3200 | CRASH | 208 | 7 | 241 (80%), 676 (69%), 1089 (70%), 1532 (69%), 1976 (67%), 2420 (68%), 2893 (67%) | 121° | 21795 | 43 | 14 |
| acemi | 2 | 5264 | CRASH | 361 | 12 | 241 (80%), 676 (69%), 1089 (71%), 1532 (68%), 1976 (67%), 2420 (68%), 2893 (68%), 3363 (67%), 3849 (65%), 4301 (67%), 4786 (62%), 5235 (67%) | 138° | 43895 | 94 | 14 |
| acemi | 3 | 5258 | CRASH | 365 | 12 | 241 (80%), 676 (69%), 1089 (70%), 1532 (68%), 1976 (67%), 2420 (69%), 2893 (69%), 3363 (68%), 3849 (65%), 4301 (66%), 4786 (62%), 5235 (61%) | 139° | 43995 | 94 | 15 |
| acemi | 4 | 3206 | CRASH | 209 | 7 | 241 (80%), 676 (69%), 1089 (71%), 1531 (69%), 1976 (67%), 2420 (68%), 2893 (68%) | 132° | 21795 | 43 | 14 |
| acemi | 5 | 3202 | CRASH | 208 | 7 | 241 (80%), 676 (70%), 1089 (70%), 1532 (68%), 1976 (66%), 2420 (69%), 2893 (68%) | 125° | 21795 | 43 | 14 |

İyi sürücü, tohum 1-5: 3. yakıt bidonuna ulaşan = **5/5** (PLAN2 F1 kuralı: en az 4/5).

Haritadaki bidonlar (Kır yolu): 249, 683, 1096, 1537, 1983, 2427, 2901, 3370 m; ardışık aralıklar: 249, 434, 413, 440, 447, 444, 474, 469 m.

## 5. Takılma, takla ve bitiş olayları

TAKILMA = yerde, yakıt var, hız < 0.5 m/s, 3 sn. TERS = yerde göreli açı > 100°. SON-* = oyunu bitiren olay. Eğim = olay noktasındaki zemin eğimi, Açı = burun açısı (dünyaya göre).

| Koşu | Olay | x (m) | Eğim | Açı | Yakıt | t (sn) |
|---|---|---|---|---|---|---|
| Ay/Cip/y0/iyi/s0 | SON-CRASH | 1211 | -8° | -175° | 0% | 125 |
| Ay/Cip/y3/iyi/s0 | TAKILMA | 3810 | 26° | 25° | 71% | 481 |
| Ay/Cip/y3/iyi/s0 | TAKILMA | 3813 | 38° | 39° | 20% | 540 |
| Ay/Cip/y3/iyi/s0 | SON-FUEL | 3768 | -4° | -5° | 0% | 620 |
| Ay/Motosiklet/y0/iyi/s0 | TAKILMA | 1597 | 36° | 37° | 37% | 264 |
| Ay/Motosiklet/y0/iyi/s0 | SON-FUEL | 1580 | -7° | -7° | 0% | 309 |
| Ay/Motosiklet/y3/iyi/s0 | TERS | 798 | -5° | 95° | 81% | 152 |
| Ay/Motosiklet/y3/iyi/s0 | SON-CRASH | 796 | -6° | 106° | 81% | 153 |
| Ay/Pikap/y0/iyi/s0 | TAKILMA | 3808 | 16° | 17° | 69% | 482 |
| Ay/Pikap/y0/iyi/s0 | SON-FUEL | 3768 | -6° | -6° | 0% | 623 |
| Ay/Pikap/y3/iyi/s0 | TAKILMA | 3813 | 34° | 37° | 81% | 437 |
| Ay/Pikap/y3/iyi/s0 | TAKILMA | 3813 | 38° | 39° | 47% | 487 |
| Ay/Pikap/y3/iyi/s0 | TAKILMA | 3810 | 21° | 25° | 7% | 545 |
| Ay/Pikap/y3/iyi/s0 | SON-FUEL | 3768 | -4° | -5° | 0% | 615 |
| Ay/Traktör/y0/iyi/s0 | TAKILMA | 4895 | 33° | 33° | 59% | 695 |
| Ay/Traktör/y0/iyi/s0 | TAKILMA | 4894 | 21° | 24° | 10% | 765 |
| Ay/Traktör/y0/iyi/s0 | SON-FUEL | 4888 | -4° | -4° | 0% | 824 |
| Bataklık/Cip/y0/iyi/s0 | SON-FUEL | 4804 | -7° | -7° | 0% | 455 |
| Bataklık/Cip/y3/iyi/s0 | TAKILMA | 9909 | 19° | 22° | 10% | 834 |
| Bataklık/Cip/y3/iyi/s0 | SON-FUEL | 9859 | -1° | -0° | 0% | 898 |
| Bataklık/Motosiklet/y0/iyi/s0 | TERS | 3989 | 30° | -79° | 59% | 268 |
| Bataklık/Motosiklet/y0/iyi/s0 | SON-FUEL | 4199 | -1° | -0° | 0% | 407 |
| Bataklık/Motosiklet/y3/iyi/s0 | TERS | 2625 | 24° | -77° | 92% | 159 |
| Bataklık/Motosiklet/y3/iyi/s0 | SON-CRASH | 2637 | 22° | -88° | 91% | 160 |
| Bataklık/Pikap/y0/iyi/s0 | TAKILMA | 4838 | 19° | 20° | 28% | 433 |
| Bataklık/Pikap/y0/iyi/s0 | SON-FUEL | 4835 | -2° | -1° | 0% | 493 |
| Bataklık/Pikap/y3/iyi/s0 | TAKILMA | 5897 | 17° | 19° | 37% | 525 |
| Bataklık/Pikap/y3/iyi/s0 | SON-FUEL | 5839 | -0° | -0° | 0% | 659 |
| Bataklık/Traktör/y0/iyi/s0 | TAKILMA | 2841 | 14° | 15° | 9% | 384 |
| Bataklık/Traktör/y0/iyi/s0 | SON-FUEL | 2794 | -3° | -5° | 0% | 428 |
| Bataklık/Traktör/y3/iyi/s0 | TAKILMA | 4255 | 17° | 18° | 50% | 524 |
| Bataklık/Traktör/y3/iyi/s0 | TAKILMA | 4255 | 17° | 18° | 32% | 555 |
| Bataklık/Traktör/y3/iyi/s0 | TAKILMA | 4256 | 17° | 19° | 20% | 577 |
| Bataklık/Traktör/y3/iyi/s0 | SON-FUEL | 4207 | -3° | -3° | 0% | 650 |
| Dağ/Cip/y0/iyi/s0 | TAKILMA | 8802 | 21° | 23° | 3% | 796 |
| Dağ/Cip/y0/iyi/s0 | SON-FUEL | 8761 | 3° | 3° | 0% | 843 |
| Dağ/Motosiklet/y0/iyi/s0 | TERS | 1121 | -20° | -120° | 94% | 69 |
| Dağ/Motosiklet/y0/iyi/s0 | SON-CRASH | 1137 | -6° | -114° | 93% | 70 |
| Dağ/Motosiklet/y3/iyi/s0 | TERS | 609 | -3° | 98° | 84% | 36 |
| Dağ/Motosiklet/y3/iyi/s0 | SON-CRASH | 619 | -12° | 98° | 84% | 37 |
| Dağ/Pikap/y0/iyi/s0 | SON-FUEL | 2520 | -3° | -2° | 0% | 359 |
| Dağ/Pikap/y3/iyi/s0 | TAKILMA | 5925 | 27° | 29° | 42% | 557 |
| Dağ/Pikap/y3/iyi/s0 | TAKILMA | 5925 | 27° | 28° | 4% | 612 |
| Dağ/Pikap/y3/iyi/s0 | SON-FUEL | 5917 | 2° | 1° | 0% | 656 |
| Dağ/Traktör/y0/iyi/s0 | TAKILMA | 1551 | 24° | 25° | 57% | 206 |
| Dağ/Traktör/y0/iyi/s0 | SON-FUEL | 1510 | 2° | -0° | 0% | 336 |
| Dağ/Traktör/y3/iyi/s0 | TAKILMA | 2900 | 27° | 35° | 45% | 368 |
| Dağ/Traktör/y3/iyi/s0 | SON-FUEL | 2855 | 0° | -1° | 0% | 491 |
| Kutup/Cip/y0/iyi/s0 | TAKILMA | 2235 | 15° | 16° | 34% | 203 |
| Kutup/Cip/y0/iyi/s0 | TAKILMA | 2228 | 14° | 15° | 7% | 225 |
| Kutup/Cip/y0/iyi/s0 | SON-FUEL | 2171 | -2° | -1° | 0% | 290 |
| Kutup/Cip/y3/iyi/s0 | TAKILMA | 3225 | 19° | 20° | 13% | 309 |
| Kutup/Cip/y3/iyi/s0 | SON-FUEL | 3171 | 1° | 1° | 0% | 389 |
| Kutup/Motosiklet/y0/iyi/s0 | SON-FUEL | 2179 | -2° | -1° | 0% | 273 |
| Kutup/Motosiklet/y3/iyi/s0 | TERS | 3172 | 4° | -96° | 88% | 214 |
| Kutup/Motosiklet/y3/iyi/s0 | TAKILMA | 3227 | 21° | 24° | 40% | 263 |
| Kutup/Motosiklet/y3/iyi/s0 | SON-FUEL | 3169 | -1° | -1° | 0% | 365 |
| Kutup/Pikap/y0/iyi/s0 | TAKILMA | 2221 | 12° | 13° | 51% | 210 |
| Kutup/Pikap/y0/iyi/s0 | TAKILMA | 2229 | 14° | 18° | 22% | 241 |
| Kutup/Pikap/y0/iyi/s0 | SON-FUEL | 2179 | -2° | -1° | 0% | 330 |
| Kutup/Pikap/y3/iyi/s0 | TAKILMA | 2236 | 15° | 16° | 55% | 216 |
| Kutup/Pikap/y3/iyi/s0 | TAKILMA | 2229 | 18° | 20° | 23% | 261 |
| Kutup/Pikap/y3/iyi/s0 | SON-FUEL | 2179 | 1° | 1° | 0% | 354 |
| Kutup/Traktör/y0/iyi/s0 | TAKILMA | 994 | 16° | 16° | 40% | 157 |
| Kutup/Traktör/y0/iyi/s0 | TAKILMA | 994 | 16° | 16° | 13% | 194 |
| Kutup/Traktör/y0/iyi/s0 | SON-FUEL | 984 | 3° | 1° | 0% | 243 |
| Kutup/Traktör/y3/iyi/s0 | TAKILMA | 1448 | 17° | 24° | 73% | 173 |
| Kutup/Traktör/y3/iyi/s0 | TAKILMA | 1422 | 17° | 17° | 21% | 265 |
| Kutup/Traktör/y3/iyi/s0 | SON-FUEL | 1368 | -2° | -1° | 0% | 351 |
| Kır yolu/Cip/y0/acemi/s1 | TERS | 3190 | -2° | 104° | 82% | 207 |
| Kır yolu/Cip/y0/acemi/s1 | SON-CRASH | 3200 | 6° | 121° | 81% | 208 |
| Kır yolu/Cip/y0/acemi/s2 | TERS | 4873 | -7° | 107° | 96% | 329 |
| Kır yolu/Cip/y0/acemi/s2 | TERS | 5241 | -12° | 89° | 100% | 359 |
| Kır yolu/Cip/y0/acemi/s2 | SON-CRASH | 5264 | 22° | 136° | 99% | 361 |
| Kır yolu/Cip/y0/acemi/s3 | TERS | 4874 | -7° | 105° | 96% | 329 |
| Kır yolu/Cip/y0/acemi/s3 | TERS | 5241 | -12° | 89° | 100% | 363 |
| Kır yolu/Cip/y0/acemi/s3 | SON-CRASH | 5258 | 13° | 115° | 100% | 365 |
| Kır yolu/Cip/y0/acemi/s4 | TERS | 3197 | -0° | 100° | 81% | 208 |
| Kır yolu/Cip/y0/acemi/s4 | SON-CRASH | 3206 | 16° | 132° | 81% | 209 |
| Kır yolu/Cip/y0/acemi/s5 | TERS | 3192 | -2° | 98° | 82% | 207 |
| Kır yolu/Cip/y0/acemi/s5 | SON-CRASH | 3202 | 8° | 125° | 82% | 208 |
| Kır yolu/Cip/y0/iyi/s0 | TAKILMA | 7932 | 20° | 31° | 50% | 573 |
| Kır yolu/Cip/y0/iyi/s0 | SON-FUEL | 7891 | -1° | -1° | 0% | 644 |
| Kır yolu/Cip/y0/iyi/s4 | SON-FUEL | 12401 | -0° | -1° | 0% | 1029 |
| Kır yolu/Motosiklet/y0/iyi/s0 | TERS | 2014 | -17° | -119° | 98% | 117 |
| Kır yolu/Motosiklet/y0/iyi/s0 | TERS | 2804 | -5° | -105° | 75% | 168 |
| Kır yolu/Motosiklet/y0/iyi/s0 | SON-CRASH | 2828 | 27° | -81° | 71% | 171 |
| Kır yolu/Motosiklet/y3/iyi/s0 | TERS | 1560 | 14° | 115° | 77% | 88 |
| Kır yolu/Motosiklet/y3/iyi/s0 | SON-CRASH | 1562 | 16° | 125° | 77% | 89 |
| Kır yolu/Traktör/y0/iyi/s0 | SON-FUEL | 5599 | -1° | -1° | 0% | 730 |
| Mağara/Cip/y0/iyi/s0 | TAKILMA | 1644 | 27° | 31° | 14% | 155 |
| Mağara/Cip/y0/iyi/s0 | SON-FUEL | 1640 | -2° | -1° | 0% | 198 |
| Mağara/Motosiklet/y0/iyi/s0 | TERS | 969 | -17° | -118° | 83% | 55 |
| Mağara/Motosiklet/y0/iyi/s0 | SON-CRASH | 988 | 12° | -94° | 82% | 56 |
| Mağara/Motosiklet/y3/iyi/s0 | TERS | 913 | -6° | -108° | 91% | 51 |
| Mağara/Motosiklet/y3/iyi/s0 | TERS | 1089 | 7° | -93° | 82% | 62 |
| Mağara/Motosiklet/y3/iyi/s0 | SON-CRASH | 1105 | 17° | -93° | 80% | 65 |
| Mağara/Pikap/y0/iyi/s0 | SON-FUEL | 2887 | 1° | -1° | 0% | 340 |
| Mağara/Traktör/y0/iyi/s0 | SON-FUEL | 1397 | -0° | -0° | 0% | 312 |
| Mağara/Traktör/y3/iyi/s0 | TAKILMA | 2892 | 24° | 31° | 7% | 438 |
| Mağara/Traktör/y3/iyi/s0 | SON-FUEL | 2887 | -2° | -1° | 0% | 483 |
| Orman/Cip/y0/iyi/s0 | SON-FUEL | 9006 | -1° | -1° | 0% | 783 |
| Orman/Motosiklet/y0/iyi/s0 | TERS | 419 | -3° | -103° | 89% | 25 |
| Orman/Motosiklet/y0/iyi/s0 | SON-CRASH | 443 | -8° | -117° | 88% | 26 |
| Orman/Motosiklet/y3/iyi/s0 | TERS | 423 | -2° | -103° | 92% | 24 |
| Orman/Motosiklet/y3/iyi/s0 | SON-CRASH | 442 | -7° | -114° | 91% | 25 |
| Orman/Pikap/y0/iyi/s0 | SON-FUEL | 10056 | -2° | -2° | 0% | 998 |
| Orman/Traktör/y0/iyi/s0 | TAKILMA | 3053 | 26° | 28° | 24% | 391 |
| Orman/Traktör/y0/iyi/s0 | SON-FUEL | 3049 | 1° | -1° | 0% | 454 |
| Plaj/Cip/y0/iyi/s0 | TAKILMA | 13031 | 21° | 25° | 27% | 987 |
| Plaj/Cip/y0/iyi/s0 | SON-FUEL | 13015 | -1° | -1° | 0% | 1054 |
| Plaj/Cip/y3/iyi/s0 | TAKILMA | 14931 | 23° | 28° | 43% | 1101 |
| Plaj/Cip/y3/iyi/s0 | SON-FUEL | 14896 | -4° | -4° | 0% | 1180 |
| Plaj/Motosiklet/y0/iyi/s0 | TERS | 3275 | 4° | -102° | 95% | 192 |
| Plaj/Motosiklet/y0/iyi/s0 | TERS | 3565 | 4° | 105° | 77% | 211 |
| Plaj/Motosiklet/y0/iyi/s0 | SON-CRASH | 3573 | 5° | 114° | 77% | 212 |
| Plaj/Motosiklet/y3/iyi/s0 | TERS | 1058 | -1° | -101° | 89% | 58 |
| Plaj/Motosiklet/y3/iyi/s0 | TERS | 3277 | 27° | -73° | 96% | 188 |
| Plaj/Motosiklet/y3/iyi/s0 | TERS | 4480 | 29° | -71° | 95% | 268 |
| Plaj/Motosiklet/y3/iyi/s0 | SON-CRASH | 4480 | 29° | -80° | 94% | 269 |
| Plaj/Pikap/y0/iyi/s0 | TAKILMA | 13031 | 21° | 25° | 25% | 1066 |
| Plaj/Pikap/y0/iyi/s0 | SON-FUEL | 13016 | -1° | -1° | 0% | 1138 |
| Plaj/Traktör/y0/iyi/s0 | TAKILMA | 8293 | 24° | 25° | 50% | 898 |
| Plaj/Traktör/y0/iyi/s0 | TAKILMA | 8293 | 24° | 25° | 23% | 935 |
| Plaj/Traktör/y0/iyi/s0 | SON-FUEL | 8240 | -0° | 0° | 0% | 1028 |
| Plaj/Traktör/y3/iyi/s0 | TAKILMA | 8587 | 22° | 24° | 69% | 932 |
| Plaj/Traktör/y3/iyi/s0 | TAKILMA | 8587 | 22° | 24° | 25% | 1013 |
| Plaj/Traktör/y3/iyi/s0 | TAKILMA | 8587 | 22° | 24° | 7% | 1045 |
| Plaj/Traktör/y3/iyi/s0 | SON-FUEL | 8574 | -1° | -2° | 0% | 1100 |
| Çöl/Cip/y0/iyi/s0 | TAKILMA | 4423 | 18° | 20° | 2% | 380 |
| Çöl/Cip/y0/iyi/s0 | SON-FUEL | 4349 | -4° | -4° | 0% | 426 |
| Çöl/Cip/y3/iyi/s0 | TAKILMA | 12636 | 24° | 28° | 48% | 1004 |
| Çöl/Cip/y3/iyi/s0 | TAKILMA | 12631 | 19° | 19° | 15% | 1043 |
| Çöl/Cip/y3/iyi/s0 | SON-FUEL | 12582 | -1° | -1° | 0% | 1107 |
| Çöl/Motosiklet/y0/iyi/s0 | TERS | 2077 | -3° | -104° | 74% | 126 |
| Çöl/Motosiklet/y0/iyi/s0 | SON-CRASH | 2098 | 2° | -109° | 73% | 127 |
| Çöl/Motosiklet/y3/iyi/s0 | TERS | 2909 | -26° | -127° | 89% | 196 |
| Çöl/Motosiklet/y3/iyi/s0 | TERS | 3559 | 3° | 112° | 82% | 243 |
| Çöl/Motosiklet/y3/iyi/s0 | SON-CRASH | 3568 | 1° | 110° | 82% | 244 |
| Çöl/Pikap/y0/iyi/s0 | TAKILMA | 4846 | 24° | 26° | 4% | 432 |
| Çöl/Pikap/y0/iyi/s0 | SON-FUEL | 4764 | -6° | -6° | 0% | 488 |
| Çöl/Pikap/y3/iyi/s0 | TAKILMA | 7271 | 23° | 26° | 35% | 616 |
| Çöl/Pikap/y3/iyi/s0 | SON-FUEL | 7250 | -1° | -1° | 0% | 713 |
| Çöl/Traktör/y0/iyi/s0 | TAKILMA | 1952 | 19° | 20° | 29% | 273 |
| Çöl/Traktör/y0/iyi/s0 | SON-FUEL | 1894 | 1° | 1° | 0% | 373 |
| Çöl/Traktör/y3/iyi/s0 | TAKILMA | 3772 | 25° | 26° | 50% | 486 |
| Çöl/Traktör/y3/iyi/s0 | TAKILMA | 3768 | 22° | 24° | 9% | 559 |
| Çöl/Traktör/y3/iyi/s0 | SON-FUEL | 3759 | 0° | 0° | 0% | 616 |
| Şantiye/Cip/y0/iyi/s0 | SON-FUEL | 9336 | -1° | -1° | 0% | 739 |
| Şantiye/Cip/y3/iyi/s0 | SON-FUEL | 9703 | -6° | -3° | 0% | 742 |
| Şantiye/Motosiklet/y0/iyi/s0 | TERS | 3801 | 9° | -92° | 62% | 234 |
| Şantiye/Motosiklet/y0/iyi/s0 | TERS | 4415 | -8° | -109° | 65% | 280 |
| Şantiye/Motosiklet/y0/iyi/s0 | TERS | 7767 | 39° | -66° | 81% | 539 |
| Şantiye/Motosiklet/y0/iyi/s0 | TERS | 8325 | 40° | -61° | 85% | 584 |
| Şantiye/Motosiklet/y0/iyi/s0 | TERS | 8742 | 6° | 106° | 95% | 632 |
| Şantiye/Motosiklet/y0/iyi/s0 | SON-CRASH | 8745 | -0° | 106° | 95% | 633 |
| Şantiye/Motosiklet/y3/iyi/s0 | TERS | 2359 | -21° | 80° | 86% | 138 |
| Şantiye/Motosiklet/y3/iyi/s0 | SON-CRASH | 2369 | -19° | 90° | 86% | 139 |
| Şantiye/Pikap/y0/iyi/s0 | SON-FUEL | 8688 | -6° | -5° | 0% | 771 |
| Şantiye/Traktör/y0/iyi/s0 | SON-FUEL | 8719 | -0° | -1° | 0% | 1147 |

## 5b. Araç özeti (matris, her iki geliştirme düzeyi, iyi sürücü)

| Araç | Ortalama mesafe (m) | Ortalama süre (sn) | CRASH | FUEL | SÜRE sınırı | Koşu |
|---|---|---|---|---|---|---|
| Cip | 8968 | 765 | 1 | 15 | 4 | 20 |
| Motosiklet | 2440 | 191 | 16 | 4 | 0 | 20 |
| Pikap | 8490 | 787 | 0 | 14 | 6 | 20 |
| Traktör | 5268 | 718 | 0 | 16 | 4 | 20 |

### Yakıt bitişleri

"Bidona kalan" = oyun bittiği yerden sonraki ilk yakıt bidonuna uzaklık. Eğim küçük ve bidon yakınsa neden büyük olasılıkla bidonun kaçırılması ya da erken yakıt tükenmesidir; eğim büyükse yokuşta takılmadır.

| Koşu | Mesafe (m) | Eğim | Bidona kalan (m) | Son bidondan beri (m) | Yakında olan özellikler |
|---|---|---|---|---|---|
| Ay/Cip/y3/iyi/s0 | 3817 | -4° | 450 | 86 | dik iniş (3710-3740 m), rampa (3780-3785 m) |
| Ay/Motosiklet/y0/iyi/s0 | 1599 | -7° | 173 | 343 | dik yokuş (1460-1470 m), çukur (1500-1507 m), rampa (1564-1569 m), dik yokuş (1600-1610 m) |
| Ay/Pikap/y0/iyi/s0 | 3815 | -6° | 450 | 88 | dik iniş (3710-3740 m), rampa (3780-3785 m) |
| Ay/Pikap/y3/iyi/s0 | 3818 | -4° | 450 | 92 | dik iniş (3710-3740 m), rampa (3780-3785 m) |
| Ay/Traktör/y0/iyi/s0 | 4908 | -4° | 285 | 228 | tümsekler (4773-4787 m), rampa (4863-4868 m), dik yokuş (4900-4910 m) |
| Bataklık/Cip/y0/iyi/s0 | 4897 | -7° | 402 | 275 | çukur (4696-4704 m), dik iniş (4750-4770 m), rampa (4810-4814 m) |
| Bataklık/Cip/y3/iyi/s0 | 9932 | -1° | 326 | 306 | dik yokuş (9750-9760 m), çukur (9801-9812 m), dik iniş (9830-9860 m), rampa (9870-9875 m) |
| Bataklık/Motosiklet/y0/iyi/s0 | 4284 | -1° | 425 | 202 | dik iniş (4130-4140 m), çukur (4146-4155 m), dik iniş (4180-4200 m), çukur (4209-4216 m) |
| Bataklık/Pikap/y0/iyi/s0 | 4901 | -2° | 371 | 280 | dik iniş (4750-4770 m), rampa (4810-4814 m), dik yokuş (4840-4850 m) |
| Bataklık/Pikap/y3/iyi/s0 | 5918 | -0° | 433 | 176 | çukur (5722-5730 m), dik iniş (5760-5780 m), çukur (5788-5799 m), dik yokuş (5850-5860 m) |
| Bataklık/Traktör/y0/iyi/s0 | 2867 | -3° | 186 | 411 | rampa (2688-2692 m), dik iniş (2770-2780 m), çukur (2797-2803 m) |
| Bataklık/Traktör/y3/iyi/s0 | 4279 | -3° | 417 | 197 | dik iniş (4130-4140 m), çukur (4146-4155 m), dik iniş (4180-4200 m), çukur (4209-4216 m) |
| Dağ/Cip/y0/iyi/s0 | 8817 | 3° | 85 | 335 | çukur (8647-8656 m), dik yokuş (8670-8680 m), dik iniş (8700-8720 m), dik iniş (8740-8750 m), çukur (8759-8769 m), dik yokuş (8780-8790 m) |
| Dağ/Pikap/y0/iyi/s0 | 2583 | -3° | 40 | 26 | rampa (2431-2436 m), dik iniş (2470-2510 m), çukur (2520-2528 m) |
| Dağ/Pikap/y3/iyi/s0 | 5945 | 2° | 165 | 239 | dik yokuş (5800-5810 m), çukur (5830-5839 m), dik iniş (5860-5870 m), çukur (5882-5890 m), dik yokuş (5930-5950 m) |
| Dağ/Traktör/y0/iyi/s0 | 1571 | 2° | 284 | 157 | rampa (1425-1429 m), dik iniş (1460-1470 m), dik iniş (1490-1510 m) |
| Dağ/Traktör/y3/iyi/s0 | 2910 | 0° | 78 | 352 | çukur (2747-2756 m), dik yokuş (2770-2780 m), dik iniş (2800-2810 m), dik iniş (2830-2850 m), çukur (2856-2864 m) |
| Kutup/Cip/y0/iyi/s0 | 2256 | -2° | 326 | 221 | rampa (2064-2069 m), dik iniş (2100-2110 m), dik iniş (2150-2160 m), çukur (2170-2179 m), dik yokuş (2190-2200 m) |
| Kutup/Cip/y3/iyi/s0 | 3251 | 1° | 223 | 274 | rampa (3064-3068 m), dik iniş (3100-3110 m), dik iniş (3140-3160 m) |
| Kutup/Motosiklet/y0/iyi/s0 | 2250 | -2° | 317 | 215 | rampa (2064-2069 m), dik iniş (2100-2110 m), dik iniş (2150-2160 m), çukur (2170-2179 m), dik yokuş (2190-2200 m) |
| Kutup/Motosiklet/y3/iyi/s0 | 3239 | -1° | 224 | 262 | rampa (3064-3068 m), dik iniş (3100-3110 m), dik iniş (3140-3160 m) |
| Kutup/Pikap/y0/iyi/s0 | 2252 | -2° | 318 | 218 | rampa (2064-2069 m), dik iniş (2100-2110 m), dik iniş (2150-2160 m), çukur (2170-2179 m), dik yokuş (2190-2200 m) |
| Kutup/Pikap/y3/iyi/s0 | 2259 | 1° | 317 | 225 | rampa (2064-2069 m), dik iniş (2100-2110 m), dik iniş (2150-2160 m), çukur (2170-2179 m), dik yokuş (2190-2200 m) |
| Kutup/Traktör/y0/iyi/s0 | 1016 | 3° | 173 | 331 | tümsekler (904-919 m), çukur (983-989 m), dik yokuş (1000-1010 m) |
| Kutup/Traktör/y3/iyi/s0 | 1450 | -2° | 209 | 296 | dik yokuş (1250-1260 m), tümsekler (1284-1309 m), dik iniş (1340-1360 m), çukur (1368-1377 m) |
| Kır yolu/Cip/y0/iyi/s0 | 7934 | -1° | 96 | 433 | dik iniş (7820-7840 m), çukur (7848-7855 m), dik yokuş (7910-7930 m) |
| Kır yolu/Traktör/y0/iyi/s0 | 5680 | -1° | 89 | 445 | dik yokuş (5470-5490 m), rampa (5522-5527 m), dik iniş (5560-5590 m), çukur (5599-5607 m) |
| Mağara/Cip/y0/iyi/s0 | 1654 | -2° | 420 | 466 | dik iniş (1550-1580 m), rampa (1616-1620 m), dik yokuş (1650-1660 m) |
| Mağara/Pikap/y0/iyi/s0 | 2909 | 1° | 97 | 380 | dik iniş (2800-2830 m), rampa (2862-2866 m), dik yokuş (2900-2920 m) |
| Mağara/Traktör/y0/iyi/s0 | 1475 | -0° | 231 | 286 | dik yokuş (1280-1300 m), çukur (1328-1335 m), dik iniş (1360-1390 m) |
| Mağara/Traktör/y3/iyi/s0 | 2902 | -2° | 97 | 373 | dik iniş (2800-2830 m), rampa (2862-2866 m), dik yokuş (2900-2920 m) |
| Orman/Cip/y0/iyi/s0 | 9029 | -1° | 125 | 389 | tümsekler (8912-8938 m), rampa (8983-8988 m), dik yokuş (9020-9040 m) |
| Orman/Pikap/y0/iyi/s0 | 10114 | -2° | 51 | 10 | dik yokuş (9930-9950 m), çukur (9977-9984 m), dik iniş (10010-10020 m), rampa (10031-10035 m) |
| Orman/Traktör/y0/iyi/s0 | 3064 | 1° | 93 | 413 | dik yokuş (2930-2940 m), rampa (2949-2954 m), dik iniş (2980-3000 m), dik iniş (3020-3030 m), çukur (3039-3050 m), dik yokuş (3060-3080 m) |
| Plaj/Cip/y0/iyi/s0 | 13056 | -1° | 275 | 349 | çukur (12904-12911 m), dik iniş (12950-12960 m), rampa (12974-12979 m), çukur (13030-13039 m) |
| Plaj/Cip/y3/iyi/s0 | 14942 | -4° | 256 | 412 | çukur (14784-14793 m), dik iniş (14850-14860 m), rampa (14901-14905 m) |
| Plaj/Pikap/y0/iyi/s0 | 13055 | -1° | 275 | 347 | çukur (12904-12911 m), dik iniş (12950-12960 m), rampa (12974-12979 m), çukur (13030-13039 m) |
| Plaj/Traktör/y0/iyi/s0 | 8312 | -0° | 252 | 431 | dik yokuş (8140-8150 m), çukur (8180-8189 m), dik iniş (8220-8230 m) |
| Plaj/Traktör/y3/iyi/s0 | 8603 | -1° | 541 | 113 | dik iniş (8510-8520 m), çukur (8532-8540 m), dik yokuş (8590-8610 m) |
| Çöl/Cip/y0/iyi/s0 | 4451 | -4° | 427 | 214 | çukur (4228-4235 m), dik iniş (4290-4310 m), rampa (4354-4359 m) |
| Çöl/Cip/y3/iyi/s0 | 12653 | -1° | 315 | 227 | rampa (12469-12474 m), dik iniş (12540-12560 m), çukur (12575-12583 m) |
| Çöl/Pikap/y0/iyi/s0 | 4864 | -6° | 12 | 628 | tümsekler (4650-4672 m), dik iniş (4710-4730 m), rampa (4770-4775 m) |
| Çöl/Pikap/y3/iyi/s0 | 7282 | -1° | 65 | 508 | dik iniş (7180-7200 m), rampa (7209-7213 m), dik iniş (7240-7250 m), çukur (7259-7269 m) |
| Çöl/Traktör/y0/iyi/s0 | 1978 | 1° | 320 | 278 | rampa (1786-1791 m), dik iniş (1820-1830 m), çukur (1836-1845 m) |
| Çöl/Traktör/y3/iyi/s0 | 3790 | 0° | 481 | 88 | tümsekler (3628-3649 m), dik iniş (3680-3700 m), çukur (3711-3720 m), dik yokuş (3780-3800 m) |
| Şantiye/Cip/y0/iyi/s0 | 9344 | -1° | 608 | 1241 | tümsekler (9232-9247 m), dik iniş (9260-9270 m), rampa (9311-9316 m), dik yokuş (9340-9350 m) |
| Şantiye/Cip/y3/iyi/s0 | 9799 | -6° | 240 | 1695 | çukur (9591-9600 m), dik iniş (9650-9670 m), rampa (9709-9713 m) |
| Şantiye/Pikap/y0/iyi/s0 | 8776 | -6° | 16 | 673 | çukur (8590-8598 m), dik iniş (8630-8650 m), rampa (8694-8698 m) |
| Şantiye/Traktör/y0/iyi/s0 | 8779 | -0° | 599 | 77 | dik iniş (8630-8650 m), rampa (8694-8698 m), dik yokuş (8730-8740 m) |

## 6. Yakıt: menzil ve bidon aralığı

Menzil = depo / ölçülen yakıt harcaması (m başına). Bu değer tam gazla ve yokuşlarla bu sürücünün gerçek ortalamasıdır; bidon aralığı menzilin çok altında olmalı.

| Harita (jeep, y0) | Haritadaki bidon aralığı (m) | Menzil (m) | Aralık/menzil | En uzun gerçek aralık (m) | En düşük yakıt (alırken) |
|---|---|---|---|---|---|
| Kır yolu | 450 | 1186 | 38% | 487 | 61% |
| Çöl | 500 | 1046 | 48% | 994 | 20% |
| Kutup | 450 | 853 | 53% | 469 | 60% |
| Ay | 500 | 1216 | 41% | - | - |
| Orman | 500 | 1163 | 43% | 532 | 57% |
| Dağ | 400 | 1071 | 37% | 816 | 30% |
| Plaj | 600 | 1190 | 50% | 1250 | 0% |
| Mağara | 450 | 886 | 51% | 484 | 65% |
| Bataklık | 550 | 1050 | 52% | 584 | 51% |
| Şantiye | 600 | 1315 | 46% | 620 | 51% |

## 7. En çok yakıt harcatan yokuşlar (jeep, geliştirme yok)

| Harita | x aralığı (m) | Uzunluk (m) | Yükseklik (m) | En dik eğim | Depo payı |
|---|---|---|---|---|---|
| Kır yolu | 6200-6285 | 85 | 27.1 | 41° | 12% |
| Kır yolu | 4475-4575 | 100 | 25.4 | 34° | 10% |
| Kır yolu | 7135-7220 | 85 | 27.0 | 35° | 10% |
| Çöl | 2505-2640 | 135 | 24.8 | 32° | 11% |
| Çöl | 3555-3665 | 110 | 25.0 | 36° | 11% |
| Çöl | 4180-4275 | 95 | 31.0 | 35° | 11% |
| Kutup | 1565-1700 | 135 | 21.9 | 30° | 13% |
| Kutup | 1990-2095 | 105 | 26.2 | 29° | 11% |
| Kutup | 570-705 | 135 | 15.2 | 22° | 10% |
| Ay | 1100-1175 | 75 | 15.2 | 28° | 8% |
| Ay | 950-1035 | 85 | 18.3 | 22° | 6% |
| Ay | 635-720 | 85 | 15.9 | 18° | 6% |
| Orman | 7625-7710 | 85 | 22.4 | 45° | 11% |
| Orman | 7180-7255 | 75 | 20.2 | 45° | 11% |
| Orman | 7920-8020 | 100 | 23.2 | 39° | 10% |
| Dağ | 6745-6825 | 80 | 32.8 | 41° | 16% |
| Dağ | 3730-3795 | 65 | 30.3 | 47° | 14% |
| Dağ | 5750-5815 | 65 | 28.4 | 41° | 13% |
| Plaj | 8075-8165 | 90 | 19.1 | 29° | 11% |
| Plaj | 11280-11350 | 70 | 21.7 | 42° | 10% |
| Plaj | 9205-9290 | 85 | 21.0 | 34° | 10% |
| Mağara | 1400-1540 | 140 | 29.1 | 39° | 12% |
| Mağara | 1225-1310 | 85 | 30.2 | 37° | 9% |
| Mağara | 1030-1120 | 90 | 25.6 | 31° | 8% |
| Bataklık | 4210-4340 | 130 | 25.1 | 32° | 13% |
| Bataklık | 4610-4740 | 130 | 25.6 | 34° | 12% |
| Bataklık | 3415-3505 | 90 | 25.0 | 29° | 12% |
| Şantiye | 8095-8225 | 130 | 28.4 | 36° | 13% |
| Şantiye | 8325-8405 | 80 | 29.0 | 42° | 12% |
| Şantiye | 9105-9235 | 130 | 30.0 | 45° | 12% |

## 8. Duyurular ve karıştırıcı

Mixer.voiceCount en yüksek: **17** (sınır 60). Koşu başına duyuru: ortalama 152.8; en uzun sessizlik: 135 sn (Çöl/Traktör/y3/iyi/s0).

| Koşu | En uzun sessizlik (sn) | Duyuru sayısı | Süre (sn) |
|---|---|---|---|
| Çöl/Traktör/y3/iyi/s0 | 135 | 70 | 616 |
| Bataklık/Traktör/y3/iyi/s0 | 105 | 81 | 650 |
| Şantiye/Traktör/y0/iyi/s0 | 95 | 160 | 1147 |
| Bataklık/Pikap/y3/iyi/s0 | 94 | 120 | 659 |
| Orman/Pikap/y0/iyi/s0 | 82 | 261 | 998 |
| Dağ/Pikap/y0/iyi/s0 | 81 | 60 | 359 |

## 9. Ekonomi

Sürücü yakıt bitene ya da 20 dk dolana kadar oynadığı için koşu başı altın gerçek bir oyuncunun kısa koşularından çok daha yüksek çıkar; bu yüzden altın/dakika da verilir. "Koşu" = tam bir koşu (uzun), "dk" = dakika cinsinden oyun süresi.

| Harita | Araç | Geliştirme | Koşu başı altın | Süre (sn) | Altın/dk | Sıradaki araç | Sıradaki harita |
|---|---|---|---|---|---|---|---|
| Kır yolu | Cip | y0 | 124703 | 1079 | 6936 | Motosiklet (8000): 1 koşu, 2 dk | Çöl (5000): 1 koşu, 1 dk |
| Kır yolu | Cip | y3 | 144945 | 1200 | 7247 | Motosiklet (8000): 1 koşu, 2 dk | Çöl (5000): 1 koşu, 1 dk |
| Kır yolu | Motosiklet | y0 | 17095 | 171 | 5981 | Pikap (15000): 1 koşu, 3 dk | Çöl (5000): 1 koşu, 1 dk |
| Kır yolu | Motosiklet | y3 | 4995 | 89 | 3385 | Pikap (15000): 4 koşu, 5 dk | Çöl (5000): 2 koşu, 2 dk |
| Kır yolu | Pikap | y0 | 134295 | 1200 | 6715 | Traktör (25000): 1 koşu, 4 dk | Çöl (5000): 1 koşu, 1 dk |
| Kır yolu | Pikap | y3 | 138945 | 1200 | 6947 | Traktör (25000): 1 koşu, 4 dk | Çöl (5000): 1 koşu, 1 dk |
| Kır yolu | Traktör | y0 | 49295 | 730 | 4054 | Kum arabası (35000): 1 koşu, 9 dk | Çöl (5000): 1 koşu, 2 dk |
| Kır yolu | Traktör | y3 | 105695 | 1200 | 5285 | Kum arabası (35000): 1 koşu, 7 dk | Çöl (5000): 1 koşu, 1 dk |
| Çöl | Cip | y0 | 34590 | 426 | 4877 | Motosiklet (8000): 1 koşu, 2 dk | Kutup (12000): 1 koşu, 3 dk |
| Çöl | Cip | y3 | 108940 | 1107 | 5904 | Motosiklet (8000): 1 koşu, 2 dk | Kutup (12000): 1 koşu, 3 dk |
| Çöl | Motosiklet | y0 | 11240 | 127 | 5323 | Pikap (15000): 2 koşu, 3 dk | Kutup (12000): 2 koşu, 3 dk |
| Çöl | Motosiklet | y3 | 25640 | 244 | 6310 | Pikap (15000): 1 koşu, 3 dk | Kutup (12000): 1 koşu, 2 dk |
| Çöl | Pikap | y0 | 38690 | 488 | 4761 | Traktör (25000): 1 koşu, 6 dk | Kutup (12000): 1 koşu, 3 dk |
| Çöl | Pikap | y3 | 60890 | 713 | 5121 | Traktör (25000): 1 koşu, 5 dk | Kutup (12000): 1 koşu, 3 dk |
| Çöl | Traktör | y0 | 9840 | 373 | 1584 | Kum arabası (35000): 4 koşu, 23 dk | Kutup (12000): 2 koşu, 8 dk |
| Çöl | Traktör | y3 | 27040 | 616 | 2635 | Kum arabası (35000): 2 koşu, 14 dk | Kutup (12000): 1 koşu, 5 dk |
| Kutup | Cip | y0 | 9645 | 290 | 1993 | Motosiklet (8000): 1 koşu, 5 dk | Ay (20000): 3 koşu, 11 dk |
| Kutup | Cip | y3 | 16795 | 389 | 2589 | Motosiklet (8000): 1 koşu, 4 dk | Ay (20000): 2 koşu, 8 dk |
| Kutup | Motosiklet | y0 | 9645 | 273 | 2123 | Pikap (15000): 2 koşu, 8 dk | Ay (20000): 3 koşu, 10 dk |
| Kutup | Motosiklet | y3 | 17345 | 365 | 2850 | Pikap (15000): 1 koşu, 6 dk | Ay (20000): 2 koşu, 8 dk |
| Kutup | Pikap | y0 | 9645 | 330 | 1755 | Traktör (25000): 3 koşu, 15 dk | Ay (20000): 3 koşu, 12 dk |
| Kutup | Pikap | y3 | 9645 | 354 | 1633 | Traktör (25000): 3 koşu, 16 dk | Ay (20000): 3 koşu, 13 dk |
| Kutup | Traktör | y0 | 2445 | 243 | 604 | Kum arabası (35000): 15 koşu, 58 dk | Ay (20000): 9 koşu, 34 dk |
| Kutup | Traktör | y3 | 4045 | 351 | 691 | Kum arabası (35000): 9 koşu, 51 dk | Ay (20000): 5 koşu, 29 dk |
| Ay | Cip | y0 | 3370 | 125 | 1620 | Motosiklet (8000): 3 koşu, 5 dk | Orman (30000): 9 koşu, 19 dk |
| Ay | Cip | y3 | 22895 | 620 | 2214 | Motosiklet (8000): 1 koşu, 4 dk | Orman (30000): 2 koşu, 14 dk |
| Ay | Motosiklet | y0 | 5245 | 309 | 1019 | Pikap (15000): 3 koşu, 15 dk | Orman (30000): 6 koşu, 30 dk |
| Ay | Motosiklet | y3 | 2245 | 153 | 883 | Pikap (15000): 7 koşu, 17 dk | Orman (30000): 14 koşu, 34 dk |
| Ay | Pikap | y0 | 24345 | 623 | 2346 | Traktör (25000): 2 koşu, 11 dk | Orman (30000): 2 koşu, 13 dk |
| Ay | Pikap | y3 | 22995 | 615 | 2244 | Traktör (25000): 2 koşu, 12 dk | Orman (30000): 2 koşu, 14 dk |
| Ay | Traktör | y0 | 36720 | 824 | 2674 | Kum arabası (35000): 1 koşu, 14 dk | Orman (30000): 1 koşu, 12 dk |
| Ay | Traktör | y3 | 68845 | 1200 | 3442 | Kum arabası (35000): 1 koşu, 11 dk | Orman (30000): 1 koşu, 9 dk |
| Orman | Cip | y0 | 76210 | 783 | 5843 | Motosiklet (8000): 1 koşu, 2 dk | Dağ (40000): 1 koşu, 7 dk |
| Orman | Cip | y3 | 147360 | 1200 | 7368 | Motosiklet (8000): 1 koşu, 2 dk | Dağ (40000): 1 koşu, 6 dk |
| Orman | Motosiklet | y0 | 275 | 26 | 632 | Pikap (15000): 55 koşu, 24 dk | Dağ (40000): 146 koşu, 64 dk |
| Orman | Motosiklet | y3 | 275 | 25 | 650 | Pikap (15000): 55 koşu, 24 dk | Dağ (40000): 146 koşu, 62 dk |
| Orman | Pikap | y0 | 89110 | 998 | 5359 | Traktör (25000): 1 koşu, 5 dk | Dağ (40000): 1 koşu, 8 dk |
| Orman | Pikap | y3 | 140260 | 1200 | 7013 | Traktör (25000): 1 koşu, 4 dk | Dağ (40000): 1 koşu, 6 dk |
| Orman | Traktör | y0 | 20860 | 454 | 2758 | Kum arabası (35000): 2 koşu, 13 dk | Dağ (40000): 2 koşu, 15 dk |
| Orman | Traktör | y3 | 100560 | 1200 | 5028 | Kum arabası (35000): 1 koşu, 7 dk | Dağ (40000): 1 koşu, 8 dk |
| Dağ | Cip | y0 | 76740 | 843 | 5464 | Motosiklet (8000): 1 koşu, 2 dk | Plaj (50000): 1 koşu, 10 dk |
| Dağ | Cip | y3 | 134940 | 1200 | 6747 | Motosiklet (8000): 1 koşu, 2 dk | Plaj (50000): 1 koşu, 8 dk |
| Dağ | Motosiklet | y0 | 3090 | 70 | 2645 | Pikap (15000): 5 koşu, 6 dk | Plaj (50000): 17 koşu, 19 dk |
| Dağ | Motosiklet | y3 | 1190 | 37 | 1912 | Pikap (15000): 13 koşu, 8 dk | Plaj (50000): 43 koşu, 27 dk |
| Dağ | Pikap | y0 | 17290 | 359 | 2889 | Traktör (25000): 2 koşu, 9 dk | Plaj (50000): 3 koşu, 18 dk |
| Dağ | Pikap | y3 | 51840 | 656 | 4743 | Traktör (25000): 1 koşu, 6 dk | Plaj (50000): 1 koşu, 11 dk |
| Dağ | Traktör | y0 | 5140 | 336 | 918 | Kum arabası (35000): 7 koşu, 39 dk | Plaj (50000): 10 koşu, 55 dk |
| Dağ | Traktör | y3 | 22040 | 491 | 2693 | Kum arabası (35000): 2 koşu, 13 dk | Plaj (50000): 3 koşu, 19 dk |
| Plaj | Cip | y0 | 116675 | 1054 | 6643 | Motosiklet (8000): 1 koşu, 2 dk | Mağara (60000): 1 koşu, 10 dk |
| Plaj | Cip | y3 | 135075 | 1180 | 6866 | Motosiklet (8000): 1 koşu, 2 dk | Mağara (60000): 1 koşu, 9 dk |
| Plaj | Motosiklet | y0 | 28775 | 212 | 8141 | Pikap (15000): 1 koşu, 2 dk | Mağara (60000): 3 koşu, 8 dk |
| Plaj | Motosiklet | y3 | 37125 | 269 | 8282 | Pikap (15000): 1 koşu, 2 dk | Mağara (60000): 2 koşu, 8 dk |
| Plaj | Pikap | y0 | 117625 | 1138 | 6204 | Traktör (25000): 1 koşu, 5 dk | Mağara (60000): 1 koşu, 10 dk |
| Plaj | Pikap | y3 | 142775 | 1200 | 7139 | Traktör (25000): 1 koşu, 4 dk | Mağara (60000): 1 koşu, 9 dk |
| Plaj | Traktör | y0 | 74125 | 1028 | 4327 | Kum arabası (35000): 1 koşu, 9 dk | Mağara (60000): 1 koşu, 14 dk |
| Plaj | Traktör | y3 | 77625 | 1100 | 4235 | Kum arabası (35000): 1 koşu, 9 dk | Mağara (60000): 1 koşu, 15 dk |
| Mağara | Cip | y0 | 6140 | 198 | 1865 | Motosiklet (8000): 2 koşu, 5 dk | Bataklık (70000): 12 koşu, 38 dk |
| Mağara | Cip | y3 | 129040 | 1200 | 6452 | Motosiklet (8000): 1 koşu, 2 dk | Bataklık (70000): 1 koşu, 11 dk |
| Mağara | Motosiklet | y0 | 2390 | 56 | 2566 | Pikap (15000): 7 koşu, 6 dk | Bataklık (70000): 30 koşu, 28 dk |
| Mağara | Motosiklet | y3 | 2765 | 65 | 2569 | Pikap (15000): 6 koşu, 6 dk | Bataklık (70000): 26 koşu, 28 dk |
| Mağara | Pikap | y0 | 13665 | 340 | 2411 | Traktör (25000): 2 koşu, 11 dk | Bataklık (70000): 6 koşu, 30 dk |
| Mağara | Pikap | y3 | 122240 | 1200 | 6112 | Traktör (25000): 1 koşu, 5 dk | Bataklık (70000): 1 koşu, 12 dk |
| Mağara | Traktör | y0 | 4265 | 312 | 821 | Kum arabası (35000): 9 koşu, 43 dk | Bataklık (70000): 17 koşu, 86 dk |
| Mağara | Traktör | y3 | 14565 | 483 | 1809 | Kum arabası (35000): 3 koşu, 20 dk | Bataklık (70000): 5 koşu, 39 dk |
| Bataklık | Cip | y0 | 39730 | 455 | 5236 | Motosiklet (8000): 1 koşu, 2 dk | Şantiye (90000): 3 koşu, 18 dk |
| Bataklık | Cip | y3 | 87530 | 898 | 5850 | Motosiklet (8000): 1 koşu, 2 dk | Şantiye (90000): 2 koşu, 16 dk |
| Bataklık | Motosiklet | y0 | 32680 | 407 | 4822 | Pikap (15000): 1 koşu, 4 dk | Şantiye (90000): 3 koşu, 19 dk |
| Bataklık | Motosiklet | y3 | 16630 | 160 | 6249 | Pikap (15000): 1 koşu, 3 dk | Şantiye (90000): 6 koşu, 15 dk |
| Bataklık | Pikap | y0 | 40030 | 493 | 4869 | Traktör (25000): 1 koşu, 6 dk | Şantiye (90000): 3 koşu, 19 dk |
| Bataklık | Pikap | y3 | 50530 | 659 | 4598 | Traktör (25000): 1 koşu, 6 dk | Şantiye (90000): 2 koşu, 20 dk |
| Bataklık | Traktör | y0 | 19630 | 428 | 2749 | Kum arabası (35000): 2 koşu, 13 dk | Şantiye (90000): 5 koşu, 33 dk |
| Bataklık | Traktör | y3 | 33730 | 650 | 3115 | Kum arabası (35000): 2 koşu, 12 dk | Şantiye (90000): 3 koşu, 29 dk |
| Şantiye | Cip | y0 | 83760 | 739 | 6803 | Motosiklet (8000): 1 koşu, 2 dk | - |
| Şantiye | Cip | y3 | 86360 | 742 | 6988 | Motosiklet (8000): 1 koşu, 2 dk | - |
| Şantiye | Motosiklet | y0 | 78260 | 633 | 7415 | Pikap (15000): 1 koşu, 3 dk | - |
| Şantiye | Motosiklet | y3 | 16660 | 139 | 7175 | Pikap (15000): 1 koşu, 3 dk | - |
| Şantiye | Pikap | y0 | 79110 | 771 | 6158 | Traktör (25000): 1 koşu, 5 dk | - |
| Şantiye | Pikap | y3 | 144010 | 1200 | 7200 | Traktör (25000): 1 koşu, 4 dk | - |
| Şantiye | Traktör | y0 | 79660 | 1147 | 4169 | Kum arabası (35000): 1 koşu, 9 dk | - |
| Şantiye | Traktör | y3 | 101260 | 1200 | 5063 | Kum arabası (35000): 1 koşu, 7 dk | - |

Kır yolu + jeep ortalaması (6 koşu): 124703 altın/koşu. Jeep için 4 geliştirmenin 3. seviyeye maliyeti: 8256 altın = 1 koşu; motosiklet (8000): 1 koşu; Çöl haritası (5000): 1 koşu.

Tüm araçlar ve haritalar toplam 1350000 altın. Kır yolu + jeep hızıyla (6936 altın/dk) hepsini almak ≈ 3 saat oyun sürer. Motosiklet gibi devrilgen araçlarla altın/dk 3-5 kat düşer (bkz. tablo).

## 10. Sorunlar (ciddiyet sırasıyla)

1. **[Yüksek]** İlk 1500 m içinde biten 11 koşu: Orman/Motosiklet/y3/iyi/s0 442 m CRASH (eğim -7°); Orman/Motosiklet/y0/iyi/s0 443 m CRASH (eğim -8°); Dağ/Motosiklet/y3/iyi/s0 619 m CRASH (eğim -12°); Ay/Motosiklet/y3/iyi/s0 838 m CRASH (eğim -6°); Mağara/Motosiklet/y0/iyi/s0 988 m CRASH (eğim 12°); Kutup/Traktör/y0/iyi/s0 1016 m FUEL (eğim 3°); Mağara/Motosiklet/y3/iyi/s0 1105 m CRASH (eğim 17°); Dağ/Motosiklet/y0/iyi/s0 1137 m CRASH (eğim -6°); Ay/Cip/y0/iyi/s0 1211 m CRASH (eğim -8°); Kutup/Traktör/y3/iyi/s0 1450 m FUEL (eğim -2°); Mağara/Traktör/y0/iyi/s0 1475 m FUEL (eğim -0°).
2. **[Yüksek]** Cip: 20 koşunun 15 tanesi yakıt bitişiyle sonlanıyor, ortalama mesafe 8968 m; çoğunda araç yokuşta ya da kumda çok yavaşlıyor, yakıt bidona yetmiyor.
3. **[Yüksek]** Motosiklet: 20 koşunun 16 tanesi CRASH ile bitiyor, ortalama mesafe 2440 m. Araç aşırı devrilgen (denge/atalet/havada tork ayarı gerekir).
4. **[Yüksek]** Pikap: 20 koşunun 14 tanesi yakıt bitişiyle sonlanıyor, ortalama mesafe 8490 m; çoğunda araç yokuşta ya da kumda çok yavaşlıyor, yakıt bidona yetmiyor.
5. **[Yüksek]** Traktör: 20 koşunun 16 tanesi yakıt bitişiyle sonlanıyor, ortalama mesafe 5268 m; çoğunda araç yokuşta ya da kumda çok yavaşlıyor, yakıt bidona yetmiyor.
6. **[Yüksek]** Yakıt bitişiyle sonlanan koşu: 50/90. Haritalara göre: Kır yolu 3, Çöl 6, Kutup 8, Ay 5, Orman 3, Dağ 5, Plaj 5, Mağara 4, Bataklık 7, Şantiye 4. Bunların 13 tanesi bir sonraki bidona 150 m'den az kala bitiyor (menzil marjı çok dar), 36 tanesinde bitişten önce yamaçta takılma olayı var (yani yakıt, yamaçta çıkamayıp beklerken yanmış). "Yakıt bitti" duyurusu oyuncuya nedeni söylemez.
7. **[Yüksek]** Takılma 53 kez görüldü. En sık noktalar: Kutup x≈2200 m (eğim 15°, 6 kez); Ay x≈3800 m (eğim 26°, 6 kez); Plaj x≈8550 m (eğim 22°, 3 kez); Bataklık x≈4250 m (eğim 17°, 3 kez); Çöl x≈12600 m (eğim 24°, 2 kez); Çöl x≈3750 m (eğim 25°, 2 kez).
8. **[Yüksek]** Yerde göreli açı 100°'yi geçen (ters dönme) olay 35. En sık: Kır yolu x≈3100 m (eğim -2°, açı 104°, 3 kez); Orman x≈400 m (eğim -3°, açı -103°, 2 kez); Plaj x≈3200 m (eğim 4°, açı -102°, 2 kez); Mağara x≈900 m (eğim -17°, açı -118°, 2 kez); Kır yolu x≈4800 m (eğim -7°, açı 107°, 2 kez).
9. **[Orta]** Bidona %5'ten az yakıtla ulaşılan 1 koşu var (marj çok dar): Plaj/Cip/y0/iyi/s0.
10. **[Orta]** Acemi sürücü (yalnızca takla bipiyle) Kır yolunda ortalama 4026 m gidiyor, iyi sürücü 15333 m. Denge yardımı ve eğim duyuruları olmadan ilk oyun çok zor.
11. **[Orta]** Ekonomi çok hızlı: Kır yolunda jeepin altını 6685 altın/dk. İlk araç (motosiklet 8000) 2 dk, Çöl haritası 1 dk, tüm mağaza (1350000 altın) ≈ 3.4 saat. Altın değerleri düşürülür ya da fiyatlar artırılırsa ilerleme uzar.
12. **[Düşük]** 20 dk sınırına dayanan 18 koşu (oyun doğal bitmedi; sürücü sorunsuz ilerliyor, harita uzunluğu 20 km): Kır yolu/Cip/y3/iyi/s0 16539 m; Kır yolu/Pikap/y0/iyi/s0 15061 m; Kır yolu/Pikap/y3/iyi/s0 15701 m; Kır yolu/Traktör/y3/iyi/s0 11299 m; Ay/Traktör/y3/iyi/s0 8492 m; Orman/Cip/y3/iyi/s0 15899 m.
13. **[Düşük]** 30 sn'den uzun duyurusuz süre olan 50 koşu (en uzunu 135 sn). Oyuncu ilerlediğini bilemiyor olabilir: mesafe duyurusu varsayılan kapalı.

## 11. Ayar önerileri

- Kutup x≈2200 m: 6 takılma/ters olayı (Cip/Pikap), zemin eğimi 15°. Bu yamacın en dik kısmını 10° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Ay x≈3800 m: 6 takılma/ters olayı (Cip/Pikap), zemin eğimi 26°. Bu yamacın en dik kısmını 18° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Plaj x≈8550 m: 3 takılma/ters olayı (Traktör), zemin eğimi 22°. Bu yamacın en dik kısmını 14° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Bataklık x≈4250 m: 3 takılma/ters olayı (Traktör), zemin eğimi 17°. Bu yamacın en dik kısmını 10° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Kır yolu x≈3150 m: 3 takılma/ters olayı (Cip); olay noktasında zemin eğimi düşük (-2°) ama önceki 60 m'de en dik yamaç 31°: araç yamaçta burnunu çok kaldırıp düzlükte arkaya devriliyor (gaz-burun yukarı). Yakındaki özellikler: çukur (3072-3079 m), dik iniş (3120-3130 m), rampa (3140-3145 m). Denge yardımının eşiğini (40°) ve gaz kısma eğrisini bu parçada sınayın; yamaç sonu eğimini yumuşatın.
- Çöl x≈12600 m: 2 takılma/ters olayı (Cip), zemin eğimi 24°. Bu yamacın en dik kısmını 16° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Çöl: 6 koşu yakıtla bitiyor, 6 tanesinde önce yamaçta takılma var (takılma eğimleri 18-25°, mesafeler 12653, 1978, 3790, 4451, 4864, 7282 m). Asıl sorun yakıt değil tırmanış: yüzeyin tutunması (kum, μ=0.6) bu eğim için yetersiz; 18°'nin üstündeki yamaçları azaltın, ya da bu yüzeyin μ değerini/araç torkunu artırın. Takılınca yakıt boşa yanıyor: "yokuşu çıkamıyorsun" ipucu ve geri vites daha erken devreye girmeli.
- Kutup: 8 koşu yakıtla bitiyor, 7 tanesinde önce yamaçta takılma var (takılma eğimleri 12-21°, mesafeler 1016, 1450, 2252, 2256, 2259, 3239, 3251 m). Asıl sorun yakıt değil tırmanış: yüzeyin tutunması (kar, μ=0.45) bu eğim için yetersiz; 12°'nin üstündeki yamaçları azaltın, ya da bu yüzeyin μ değerini/araç torkunu artırın. Takılınca yakıt boşa yanıyor: "yokuşu çıkamıyorsun" ipucu ve geri vites daha erken devreye girmeli.
- Ay: 5 koşu yakıtla bitiyor, 5 tanesinde önce yamaçta takılma var (takılma eğimleri 16-38°, mesafeler 1599, 3815, 3817, 3818, 4908 m). Asıl sorun yakıt değil tırmanış: yüzeyin tutunması (ay tozu, μ=0.7) bu eğim için yetersiz; 16°'nin üstündeki yamaçları azaltın, ya da bu yüzeyin μ değerini/araç torkunu artırın. Takılınca yakıt boşa yanıyor: "yokuşu çıkamıyorsun" ipucu ve geri vites daha erken devreye girmeli.
- Orman: 2 koşu takılmadan yakıtla bitiyor (mesafeler 10114, 9029 m); bidon aralığı `fuelEvery`=500 m menzile göre geniş. `fuelEvery` değerini 350 m'ye indirmeyi deneyin.
- Dağ: 5 koşu yakıtla bitiyor, 4 tanesinde önce yamaçta takılma var (takılma eğimleri 21-27°, mesafeler 1571, 2910, 5945, 8817 m). Asıl sorun yakıt değil tırmanış: yüzeyin tutunması (kaya, μ=0.95) bu eğim için yetersiz; 21°'nin üstündeki yamaçları azaltın, ya da bu yüzeyin μ değerini/araç torkunu artırın. Takılınca yakıt boşa yanıyor: "yokuşu çıkamıyorsun" ipucu ve geri vites daha erken devreye girmeli.
- Plaj: 5 koşu yakıtla bitiyor, 5 tanesinde önce yamaçta takılma var (takılma eğimleri 21-24°, mesafeler 13055, 13056, 14942, 8312, 8603 m). Asıl sorun yakıt değil tırmanış: yüzeyin tutunması (kum, μ=0.6) bu eğim için yetersiz; 21°'nin üstündeki yamaçları azaltın, ya da bu yüzeyin μ değerini/araç torkunu artırın. Takılınca yakıt boşa yanıyor: "yokuşu çıkamıyorsun" ipucu ve geri vites daha erken devreye girmeli.
- Mağara: 4 koşu yakıtla bitiyor, 2 tanesinde önce yamaçta takılma var (takılma eğimleri 24-27°, mesafeler 1654, 2902 m). Asıl sorun yakıt değil tırmanış: yüzeyin tutunması (kaya, μ=0.95) bu eğim için yetersiz; 24°'nin üstündeki yamaçları azaltın, ya da bu yüzeyin μ değerini/araç torkunu artırın. Takılınca yakıt boşa yanıyor: "yokuşu çıkamıyorsun" ipucu ve geri vites daha erken devreye girmeli.
- Mağara: 2 koşu takılmadan yakıtla bitiyor (mesafeler 1475, 2909 m); bidon aralığı `fuelEvery`=450 m menzile göre geniş. `fuelEvery` değerini 315 m'ye indirmeyi deneyin.
- Bataklık: 7 koşu yakıtla bitiyor, 5 tanesinde önce yamaçta takılma var (takılma eğimleri 14-19°, mesafeler 2867, 4279, 4901, 5918, 9932 m). Asıl sorun yakıt değil tırmanış: yüzeyin tutunması (çamur, μ=0.5) bu eğim için yetersiz; 14°'nin üstündeki yamaçları azaltın, ya da bu yüzeyin μ değerini/araç torkunu artırın. Takılınca yakıt boşa yanıyor: "yokuşu çıkamıyorsun" ipucu ve geri vites daha erken devreye girmeli.
- Bataklık: 2 koşu takılmadan yakıtla bitiyor (mesafeler 4284, 4897 m); bidon aralığı `fuelEvery`=550 m menzile göre geniş. `fuelEvery` değerini 385 m'ye indirmeyi deneyin.
- Şantiye: 4 koşu takılmadan yakıtla bitiyor (mesafeler 8776, 8779, 9344, 9799 m); bidon aralığı `fuelEvery`=600 m menzile göre geniş. `fuelEvery` değerini 420 m'ye indirmeyi deneyin.

## 12. Karşılaştırma: düzeltmeden önceki çekirdek (commit d417faf)

Bu bölüm sabit metindir (aynı test, eski çekirdekle bir kez çalıştırıldı). Güncel sonuçlar yukarıdadır.

- 90 koşunun 25'inde hata dedektörü tetiklendi; test FAIL ile çıktı.
- `sırtüstü` (araç yerde göreli açı > 100° iken 3 sn'den uzun süre oyun bitmedi): 43 olay, 25 koşuda. Örnek: Kır yolu/Motosiklet/y0, x=1944 m, göreli açı 109°. Bu, kullanıcının bildirdiği "burun 124°'ye kalktı, yakıt bitti" durumunun birebir aynısıdır: Kır yolu motosiklet 1889 m'de, tepe açı 125°, oyun CRASH ile değil "yakıt bitti" ile bitti.
- `tiltDeg>180`: 1 koşu (Ay/Motosiklet/y3, -181°; ham tepe değeri 494°). `tiltDeg()` sarılmıyordu.
- `TAKILI`: Ay/Traktör/y3, 20 dk sonunda araç duruyor, yakıt var, oyun bitmiyor (kaçırılan bir bitiş koşulu).
- NaN ve voiceCount dedektörleri hiç tetiklenmedi.
- Kır yolu + jeep (iyi sürücü) eski çekirdekte de 5/5 tohumda ~16.2 km gidip 20 dk sınırına ulaştı: ilk haritadaki yokuş, dikkatli sürücü için sorun değil. Sorun, gazı kesmeyen/yalnızca bipe tepki veren oyuncuda (acemi sürücü: 1546 m'de ters dönüp CRASH).
