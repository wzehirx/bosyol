# Yokuş Ustası: oyun testi raporu

Bu rapor `bash android/test/run.sh PlayTest` tarafından otomatik üretilir (elle düzenlemeyin, yeniden çalıştırınca yazılır). Sürücü `BlindDriver`: yalnızca sesle algılanabilen bilgilere bakar (eğim açısı, 10-15 m ilerideki radar eğimi, duyurular, havada olma, yakıt uyarıları, hız), 0.25 sn tepki gecikmesi ve küçük gürültüyle oynar. Strateji: tam gaz, burun zemin eğiminden 35° fazla kalkarsa gazı kes, havada burnu iniş eğimine yatır, takılırsa kısa geri git. "Acemi" sürücü yalnızca takla bipi (>70°) çalınca gazı keser.

- Koşu sayısı: 188 (matris 128, tohum 15, araç 38); oyun zamanı sınırı 20 dk; gerçek süre 125 sn.
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
| Tuz gölü: asıl araç (Cip ya da Go-kart) y3 ≥ 1000 m | GEÇTİ | Cip 3137 m FUEL, Go-kart 4901 m FUEL |
| Kutup: asıl araç (Cip ya da Kar motosikleti) y3 ≥ 1000 m | GEÇTİ | Cip 2614 m FUEL, Kar motosikleti 8965 m FUEL |
| Ay: asıl araç (Cip ya da Ay aracı) y3 ≥ 1000 m | GEÇTİ | Cip 2569 m FUEL, Ay aracı 7459 m FUEL |
| Donmuş göl: asıl araç (Cip ya da Kar motosikleti) y3 ≥ 1000 m | GEÇTİ | Cip 2715 m FUEL, Kar motosikleti 3482 m FUEL |
| Orman: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3653 m FUEL |
| Gece çatıları: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 4040 m FUEL |
| Dağ: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 4185 m FUEL |
| Plaj: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3272 m FUEL |
| Mağara: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 4480 m FUEL |
| Mars: asıl araç (Cip ya da Ay aracı) y3 ≥ 1000 m | GEÇTİ | Cip 2174 m FUEL, Ay aracı 7805 m FUEL |
| Bataklık: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3901 m FUEL |
| Volkan: asıl araç (Cip ya da İtfaiye aracı) y3 ≥ 1000 m | GEÇTİ | Cip 4287 m FUEL, İtfaiye aracı 1471 m FUEL |
| Şantiye: asıl araç (Cip) y3 ≥ 1000 m | GEÇTİ | Cip 3056 m FUEL |
| Deniz tabanı: asıl araç (Cip ya da Kum arabası) y3 ≥ 1000 m | GEÇTİ | Cip 2116 m FUEL, Kum arabası 2286 m FUEL |
| Motosiklet y0, Kır yolu: medyan ≥ 1500 m | GEÇTİ | 4931 m (4931 FUEL, 4931 FUEL, 4931 FUEL, 4931 FUEL, 4930 FUEL, 4932 FUEL, 4931 FUEL) |
| Her araç Kır yolunda y0 iyi sürücüyle ≥ 800 m (20 araç) | GEÇTİ | en az 2183 m (garbage) |
| Her araç, acemi sürücü + denge yardımı: ilk 500 m'de kaza yok | GEÇTİ | yok (20 koşu) |
| Ekonomi: ilk araç Scooter (450) 1-2 koşuda | GEÇTİ | 1.0 koşu (432 altın/koşu, medyan koşu 3.6 dk) |
| Ekonomi: ilk harita Çöl (1000) 2-3 koşuda | GEÇTİ | 2.3 koşu |
| Ekonomi: bütün mağaza (iyi oyunla) 25-40 saat | GEÇTİ | 31.8 saat benzetim (311750 altın; matris ortalaması 160 altın/dk) |

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
| Çöl | Cip | 2249 | FUEL | 164 | 3 | 49% | 39° | 0 | 307 | 0 | 0 |
| Çöl | Motosiklet | 3759 | FUEL | 269 | 4 | 21% | 73° | 0 | 512 | 0 | 0 |
| Çöl | Pikap | 3486 | FUEL | 298 | 4 | 25% | 47° | 0 | 470 | 0 | 0 |
| Çöl | Traktör | 3465 | FUEL | 398 | 4 | 12% | 49° | 0 | 470 | 0 | 0 |
| Tuz gölü | Cip | 2871 | FUEL | 206 | 3 | 23% | 31° | 0 | 402 | 0 | 0 |
| Tuz gölü | Motosiklet | 4871 | FUEL | 332 | 4 | 0% | 63° | 0 | 759 | 0 | 0 |
| Tuz gölü | Pikap | 4912 | FUEL | 350 | 4 | 3% | 39° | 0 | 762 | 0 | 0 |
| Tuz gölü | Traktör | 3092 | FUEL | 320 | 3 | 42% | 36° | 0 | 441 | 0 | 0 |
| Kutup | Cip | 2071 | FUEL | 160 | 3 | 51% | 39° | 0 | 292 | 0 | 0 |
| Kutup | Motosiklet | 2360 | FUEL | 228 | 4 | 24% | 64° | 0 | 337 | 0 | 0 |
| Kutup | Pikap | 2612 | FUEL | 277 | 4 | 36% | 46° | 0 | 385 | 0 | 0 |
| Kutup | Traktör | 2881 | FUEL | 363 | 4 | 29% | 48° | 0 | 424 | 0 | 0 |
| Ay | Cip | 1803 | FUEL | 191 | 1 | 15% | 54° | 0 | 2005 | 1800 | 0 |
| Ay | Motosiklet | 1936 | FUEL | 326 | 3 | 45% | 94° | 0 | 1361 | 1100 | 0 |
| Ay | Pikap | 1798 | FUEL | 191 | 0 | - | 45° | 0 | 1942 | 1750 | 0 |
| Ay | Traktör | 5357 | FUEL | 665 | 5 | 7% | 55° | 0 | 5150 | 4400 | 0 |
| Donmuş göl | Cip | 2472 | FUEL | 183 | 3 | 43% | 29° | 0 | 349 | 0 | 0 |
| Donmuş göl | Motosiklet | 2711 | FUEL | 190 | 3 | 58% | 63° | 0 | 380 | 0 | 0 |
| Donmuş göl | Pikap | 3161 | FUEL | 248 | 3 | 64% | 40° | 0 | 455 | 0 | 0 |
| Donmuş göl | Traktör | 4059 | FUEL | 421 | 4 | 9% | 45° | 0 | 592 | 0 | 0 |
| Orman | Cip | 2268 | FUEL | 167 | 3 | 48% | 44° | 0 | 322 | 0 | 0 |
| Orman | Motosiklet | 3863 | FUEL | 258 | 4 | 29% | 74° | 0 | 717 | 150 | 0 |
| Orman | Pikap | 3862 | FUEL | 307 | 4 | 27% | 47° | 0 | 569 | 0 | 0 |
| Orman | Traktör | 2449 | FUEL | 396 | 4 | 11% | 46° | 0 | 347 | 0 | 1 |
| Gece çatıları | Cip | 2486 | FUEL | 177 | 3 | 44% | 42° | 0 | 380 | 0 | 0 |
| Gece çatıları | Motosiklet | 4109 | FUEL | 267 | 4 | 18% | 63° | 0 | 696 | 50 | 0 |
| Gece çatıları | Pikap | 4364 | FUEL | 329 | 4 | 22% | 48° | 0 | 692 | 0 | 0 |
| Gece çatıları | Traktör | 4026 | FUEL | 432 | 4 | 4% | 50° | 0 | 641 | 0 | 0 |
| Dağ | Cip | 2790 | FUEL | 213 | 4 | 24% | 56° | 0 | 477 | 50 | 0 |
| Dağ | Motosiklet | 4248 | FUEL | 321 | 5 | 14% | 72° | 0 | 888 | 250 | 0 |
| Dağ | Pikap | 2347 | FUEL | 259 | 4 | 49% | 50° | 0 | 366 | 0 | 0 |
| Dağ | Traktör | 1932 | FUEL | 346 | 4 | 32% | 50° | 0 | 298 | 0 | 0 |
| Plaj | Cip | 2822 | FUEL | 195 | 3 | 17% | 40° | 0 | 421 | 0 | 0 |
| Plaj | Motosiklet | 3289 | FUEL | 279 | 4 | 0% | 58° | 0 | 514 | 0 | 1 |
| Plaj | Pikap | 4816 | FUEL | 353 | 4 | 5% | 48° | 0 | 791 | 0 | 0 |
| Plaj | Traktör | 3104 | FUEL | 325 | 3 | 38% | 45° | 0 | 479 | 0 | 0 |
| Mağara | Cip | 2428 | FUEL | 207 | 4 | 14% | 60° | 0 | 376 | 0 | 0 |
| Mağara | Motosiklet | 4529 | FUEL | 316 | 5 | 12% | 76° | 0 | 784 | 50 | 0 |
| Mağara | Pikap | 2426 | FUEL | 264 | 4 | 45% | 51° | 0 | 376 | 0 | 0 |
| Mağara | Traktör | 1475 | FUEL | 257 | 3 | 68% | 41° | 0 | 230 | 0 | 0 |
| Mars | Cip | 1753 | FUEL | 175 | 3 | 58% | 49° | 0 | 650 | 375 | 0 |
| Mars | Motosiklet | 1381 | FUEL | 205 | 3 | 55% | 87° | 0 | 375 | 150 | 1 |
| Mars | Pikap | 5332 | FUEL | 476 | 5 | 8% | 53° | 0 | 2368 | 1450 | 0 |
| Mars | Traktör | 5307 | FUEL | 605 | 5 | 0% | 51° | 0 | 1449 | 525 | 0 |
| Bataklık | Cip | 2390 | FUEL | 175 | 3 | 42% | 35° | 0 | 413 | 0 | 0 |
| Bataklık | Motosiklet | 3251 | FUEL | 259 | 4 | 7% | 73° | 0 | 568 | 0 | 0 |
| Bataklık | Pikap | 3942 | FUEL | 308 | 4 | 21% | 48° | 0 | 687 | 0 | 0 |
| Bataklık | Traktör | 3900 | FUEL | 413 | 4 | 8% | 50° | 0 | 677 | 0 | 0 |
| Volkan | Cip | 2257 | FUEL | 209 | 4 | 18% | 56° | 0 | 434 | 50 | 0 |
| Volkan | Motosiklet | 2505 | FUEL | 231 | 4 | 43% | 74° | 0 | 526 | 100 | 1 |
| Volkan | Pikap | 2255 | FUEL | 266 | 4 | 44% | 47° | 0 | 384 | 0 | 0 |
| Volkan | Traktör | 1279 | FUEL | 261 | 3 | 66% | 39° | 0 | 221 | 0 | 1 |
| Şantiye | Cip | 2701 | FUEL | 189 | 3 | 24% | 45° | 0 | 466 | 0 | 0 |
| Şantiye | Motosiklet | 4771 | FUEL | 302 | 4 | 0% | 76° | 0 | 1082 | 250 | 0 |
| Şantiye | Pikap | 4792 | FUEL | 358 | 4 | 4% | 48° | 0 | 864 | 0 | 0 |
| Şantiye | Traktör | 3010 | FUEL | 320 | 3 | 42% | 47° | 0 | 528 | 0 | 0 |
| Deniz tabanı | Cip | 1877 | FUEL | 229 | 3 | 12% | 33° | 0 | 325 | 0 | 0 |
| Deniz tabanı | Motosiklet | 1257 | FUEL | 212 | 2 | 1% | 20° | 0 | 212 | 0 | 0 |
| Deniz tabanı | Pikap | 3516 | FUEL | 375 | 4 | 11% | 41° | 0 | 773 | 150 | 0 |
| Deniz tabanı | Traktör | 3813 | FUEL | 439 | 4 | 19% | 50° | 0 | 841 | 150 | 0 |

## 3. Harita × araç, geliştirme hepsi 3. seviye (iyi sürücü)

| Harita | Araç | Mesafe (m) | Bitiş | Süre (sn) | Bidon | En az yakıt | Tepe açı | Takla | Altın | Bonus | Geri vites |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Kır yolu | Cip | 4659 | FUEL | 316 | 5 | 3% | 53° | 0 | 650 | 0 | 0 |
| Kır yolu | Motosiklet | 6974 | FUEL | 429 | 6 | 15% | 81° | 0 | 1137 | 100 | 0 |
| Kır yolu | Pikap | 6977 | FUEL | 521 | 6 | 17% | 47° | 0 | 1045 | 0 | 0 |
| Kır yolu | Traktör | 6384 | FUEL | 714 | 6 | 1% | 53° | 0 | 950 | 0 | 0 |
| Çöl | Cip | 3766 | FUEL | 266 | 4 | 11% | 50° | 0 | 512 | 0 | 0 |
| Çöl | Motosiklet | 5546 | FUEL | 378 | 5 | 11% | 78° | 0 | 809 | 50 | 0 |
| Çöl | Pikap | 4609 | FUEL | 445 | 5 | 20% | 47° | 0 | 620 | 0 | 0 |
| Çöl | Traktör | 5568 | FUEL | 609 | 5 | 8% | 51° | 0 | 766 | 0 | 0 |
| Tuz gölü | Cip | 3137 | FUEL | 220 | 3 | 41% | 38° | 0 | 446 | 0 | 0 |
| Tuz gölü | Motosiklet | 5250 | FUEL | 309 | 4 | 19% | 66° | 0 | 817 | 0 | 0 |
| Tuz gölü | Pikap | 7813 | FUEL | 561 | 5 | 0% | 45° | 0 | 1249 | 0 | 0 |
| Tuz gölü | Traktör | 5189 | FUEL | 531 | 4 | 10% | 42° | 0 | 811 | 0 | 0 |
| Kutup | Cip | 2614 | FUEL | 234 | 4 | 28% | 48° | 0 | 385 | 0 | 0 |
| Kutup | Motosiklet | 2887 | FUEL | 248 | 4 | 48% | 72° | 0 | 424 | 0 | 0 |
| Kutup | Pikap | 2624 | FUEL | 303 | 4 | 54% | 45° | 0 | 385 | 0 | 0 |
| Kutup | Traktör | 3915 | FUEL | 539 | 5 | 23% | 49° | 0 | 585 | 0 | 0 |
| Ay | Cip | 2569 | FUEL | 269 | 2 | 39% | 56° | 0 | 3142 | 2850 | 0 |
| Ay | Motosiklet | 3244 | FUEL | 483 | 4 | 0% | 120° | 0 | 3278 | 2825 | 0 |
| Ay | Pikap | 2754 | FUEL | 282 | 0 | - | 60° | 0 | 3412 | 3125 | 0 |
| Ay | Traktör | 7957 | FUEL | 969 | 6 | 8% | 67° | 0 | 7466 | 6325 | 0 |
| Donmuş göl | Cip | 2715 | FUEL | 195 | 3 | 57% | 40° | 0 | 381 | 0 | 0 |
| Donmuş göl | Motosiklet | 3185 | FUEL | 216 | 3 | 69% | 65° | 0 | 453 | 0 | 0 |
| Donmuş göl | Pikap | 3490 | FUEL | 259 | 3 | 73% | 42° | 0 | 503 | 0 | 0 |
| Donmuş göl | Traktör | 4426 | FUEL | 456 | 4 | 30% | 44° | 0 | 649 | 0 | 0 |
| Orman | Cip | 3653 | FUEL | 261 | 4 | 19% | 51° | 0 | 533 | 0 | 0 |
| Orman | Motosiklet | 5829 | FUEL | 384 | 5 | 22% | 81° | 0 | 1164 | 250 | 0 |
| Orman | Pikap | 5859 | FUEL | 594 | 6 | 0% | 51° | 0 | 922 | 0 | 0 |
| Orman | Traktör | 3855 | FUEL | 445 | 4 | 35% | 52° | 0 | 569 | 0 | 1 |
| Gece çatıları | Cip | 4040 | FUEL | 282 | 4 | 6% | 53° | 0 | 628 | 0 | 0 |
| Gece çatıları | Motosiklet | 6438 | CRASH | 404 | 5 | 3% | 162° | 0 | 1333 | 300 | 0 |
| Gece çatıları | Pikap | 6711 | FUEL | 507 | 5 | 13% | 48° | 0 | 1105 | 0 | 0 |
| Gece çatıları | Traktör | 4461 | FUEL | 471 | 4 | 27% | 52° | 0 | 701 | 0 | 0 |
| Dağ | Cip | 4185 | FUEL | 312 | 5 | 16% | 58° | 0 | 740 | 100 | 0 |
| Dağ | Motosiklet | 9724 | FUEL | 692 | 8 | 0% | 73° | 0 | 1964 | 350 | 0 |
| Dağ | Pikap | 3339 | FUEL | 376 | 5 | 45% | 54° | 0 | 525 | 0 | 0 |
| Dağ | Traktör | 2815 | FUEL | 374 | 4 | 56% | 50° | 0 | 439 | 0 | 0 |
| Plaj | Cip | 3272 | FUEL | 228 | 3 | 37% | 44° | 0 | 509 | 0 | 0 |
| Plaj | Motosiklet | 5143 | FUEL | 314 | 4 | 21% | 64° | 0 | 843 | 0 | 0 |
| Plaj | Pikap | 5439 | FUEL | 394 | 4 | 28% | 47° | 0 | 897 | 0 | 0 |
| Plaj | Traktör | 5059 | FUEL | 525 | 4 | 12% | 52° | 0 | 829 | 0 | 0 |
| Mağara | Cip | 4480 | FUEL | 322 | 5 | 3% | 59° | 0 | 774 | 50 | 0 |
| Mağara | Motosiklet | 8777 | FUEL | 579 | 7 | 1% | 82° | 0 | 1691 | 150 | 0 |
| Mağara | Pikap | 4052 | FUEL | 396 | 5 | 32% | 52° | 0 | 645 | 0 | 0 |
| Mağara | Traktör | 2427 | FUEL | 388 | 4 | 49% | 53° | 0 | 376 | 0 | 0 |
| Mars | Cip | 2174 | FUEL | 200 | 3 | 67% | 52° | 0 | 1048 | 700 | 0 |
| Mars | Motosiklet | 4556 | FUEL | 492 | 5 | 0% | 78° | 0 | 2180 | 1400 | 0 |
| Mars | Pikap | 7809 | FUEL | 672 | 6 | 12% | 62° | 0 | 3613 | 2275 | 0 |
| Mars | Traktör | 7797 | FUEL | 875 | 6 | 0% | 52° | 0 | 2688 | 1300 | 0 |
| Bataklık | Cip | 3901 | FUEL | 276 | 4 | 6% | 51° | 0 | 677 | 0 | 0 |
| Bataklık | Motosiklet | 4248 | FUEL | 284 | 4 | 36% | 73° | 0 | 746 | 0 | 0 |
| Bataklık | Pikap | 4723 | FUEL | 479 | 5 | 5% | 47° | 0 | 841 | 0 | 1 |
| Bataklık | Traktör | 4235 | FUEL | 470 | 4 | 30% | 52° | 0 | 740 | 0 | 1 |
| Volkan | Cip | 4287 | FUEL | 321 | 5 | 7% | 56° | 0 | 815 | 50 | 0 |
| Volkan | Motosiklet | 6282 | FUEL | 443 | 6 | 24% | 80° | 0 | 1480 | 350 | 0 |
| Volkan | Pikap | 3601 | FUEL | 391 | 5 | 38% | 48° | 0 | 643 | 0 | 0 |
| Volkan | Traktör | 2255 | FUEL | 389 | 4 | 51% | 48° | 0 | 384 | 0 | 0 |
| Şantiye | Cip | 3056 | FUEL | 214 | 3 | 42% | 45° | 0 | 528 | 0 | 0 |
| Şantiye | Motosiklet | 5186 | FUEL | 321 | 4 | 24% | 76° | 0 | 1263 | 350 | 0 |
| Şantiye | Pikap | 5310 | FUEL | 391 | 4 | 28% | 47° | 0 | 962 | 0 | 0 |
| Şantiye | Traktör | 5164 | FUEL | 545 | 4 | 12% | 53° | 0 | 948 | 0 | 0 |
| Deniz tabanı | Cip | 2116 | FUEL | 234 | 3 | 41% | 37° | 0 | 419 | 50 | 0 |
| Deniz tabanı | Motosiklet | 1994 | FUEL | 297 | 3 | 32% | 39° | 0 | 344 | 0 | 0 |
| Deniz tabanı | Pikap | 3876 | FUEL | 365 | 4 | 38% | 43° | 0 | 841 | 150 | 0 |
| Deniz tabanı | Traktör | 5645 | FUEL | 629 | 5 | 10% | 54° | 0 | 1377 | 300 | 0 |

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

Motosiklet, Kır yolu, geliştirme yok (iyi sürücü, tohum 0-5): 4931 m FUEL (tepe 81°), 4931 m FUEL (tepe 81°), 2387 m CRASH (tepe 122°), 4931 m FUEL (tepe 81°), 4931 m FUEL (tepe 81°), 4930 m FUEL (tepe 81°), 4932 m FUEL (tepe 81°), 4931 m FUEL (tepe 81°).

Haritadaki bidonlar (Kır yolu): 249, 708, 1149, 2077, 3360, 5064, 7057, 9395 m; ardışık aralıklar: 249, 458, 441, 928, 1284, 1704, 1993, 2338 m.

## 4b. Tüm araçlar, Kır yolu, geliştirme yok

Kural: iyi sürücüyle ≥ 800 m; acemi sürücü + denge yardımı ilk 500 m'de kaza yapmaz. Acemi sürücü yalnızca takla bipini duyar, o yüzden sonraki kazalar beklenir.

| Araç | Sürücü | Mesafe (m) | Bitiş | Süre (sn) | Altın | Tepe açı | Takla |
|---|---|---|---|---|---|---|---|
| Cip | iyi | 3196 | FUEL | 217 | 432 | 51° | 0 |
| Cip | acemi | 3197 | FUEL | 217 | 432 | 63° | 0 |
| Scooter | iyi | 3222 | FUEL | 251 | 436 | 64° | 0 |
| Scooter | acemi | 3223 | FUEL | 251 | 436 | 86° | 0 |
| Motosiklet | iyi | 4931 | FUEL | 309 | 692 | 81° | 0 |
| Motosiklet | acemi | 2387 | CRASH | 135 | 347 | 122° | 0 |
| Go-kart | iyi | 3305 | FUEL | 207 | 451 | 74° | 0 |
| Go-kart | acemi | 2375 | CRASH | 135 | 340 | 127° | 0 |
| Pikap | iyi | 4934 | FUEL | 370 | 699 | 47° | 0 |
| Pikap | acemi | 4934 | FUEL | 371 | 699 | 47° | 0 |
| Dörtteker | iyi | 3232 | FUEL | 247 | 436 | 64° | 0 |
| Dörtteker | acemi | 2369 | CRASH | 171 | 338 | 118° | 0 |
| Traktör | iyi | 3535 | FUEL | 490 | 483 | 49° | 0 |
| Traktör | acemi | 4595 | FUEL | 495 | 643 | 50° | 0 |
| Kum arabası | iyi | 4659 | FUEL | 316 | 653 | 70° | 0 |
| Kum arabası | acemi | 2381 | CRASH | 150 | 344 | 117° | 0 |
| Chopper | iyi | 4932 | FUEL | 336 | 695 | 66° | 0 |
| Chopper | acemi | 4932 | FUEL | 339 | 695 | 83° | 0 |
| Ralli arabası | iyi | 3867 | FUEL | 275 | 526 | 86° | 0 |
| Ralli arabası | acemi | 2389 | CRASH | 124 | 347 | 139° | 0 |
| Hot rod | iyi | 4952 | FUEL | 299 | 749 | 62° | 0 |
| Hot rod | acemi | 3865 | FUEL | 286 | 525 | 84° | 0 |
| Canavar kamyon | iyi | 3886 | FUEL | 366 | 534 | 47° | 0 |
| Canavar kamyon | acemi | 3887 | FUEL | 364 | 534 | 47° | 0 |
| Çöp kamyonu | iyi | 2183 | FUEL | 322 | 312 | 34° | 0 |
| Çöp kamyonu | acemi | 2183 | FUEL | 322 | 312 | 34° | 0 |
| Otobüs | iyi | 3224 | FUEL | 298 | 436 | 37° | 0 |
| Otobüs | acemi | 3224 | FUEL | 298 | 436 | 37° | 0 |
| Kar motosikleti | iyi | 4932 | FUEL | 339 | 698 | 72° | 0 |
| Kar motosikleti | acemi | 2376 | CRASH | 151 | 341 | 120° | 0 |
| İtfaiye aracı | iyi | 3553 | FUEL | 382 | 484 | 40° | 0 |
| İtfaiye aracı | acemi | 3553 | FUEL | 382 | 484 | 40° | 0 |
| Ay aracı | iyi | 4953 | FUEL | 399 | 704 | 59° | 0 |
| Ay aracı | acemi | 3875 | FUEL | 395 | 531 | 72° | 0 |
| Formül araba | iyi | 3197 | FUEL | 162 | 526 | 62° | 0 |
| Formül araba | acemi | 3207 | FUEL | 162 | 478 | 82° | 0 |
| Tank | iyi | 3879 | FUEL | 499 | 532 | 43° | 0 |
| Tank | acemi | 4531 | FUEL | 503 | 633 | 43° | 0 |
| Süper araba | iyi | 3589 | CRASH | 155 | 774 | 125° | 0 |
| Süper araba | acemi | 4942 | FUEL | 238 | 1088 | 62° | 0 |

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
| Deniz tabanı/Cip/y0/iyi/s0 | SON-FUEL | 1857 | 7° | 7° | 0% | 229 |
| Deniz tabanı/Cip/y3/iyi/s0 | SON-FUEL | 2108 | 2° | 2° | 0% | 234 |
| Deniz tabanı/Kum arabası/y3/iyi/s0 | SON-FUEL | 2271 | 4° | 3° | 0% | 268 |
| Deniz tabanı/Motosiklet/y0/iyi/s0 | SON-FUEL | 1232 | 5° | 5° | 0% | 212 |
| Deniz tabanı/Motosiklet/y3/iyi/s0 | SON-FUEL | 1989 | -4° | -4° | 0% | 297 |
| Deniz tabanı/Pikap/y0/iyi/s0 | SON-FUEL | 3491 | -3° | -3° | 0% | 375 |
| Deniz tabanı/Pikap/y3/iyi/s0 | SON-FUEL | 3876 | -1° | 0° | 0% | 365 |
| Deniz tabanı/Traktör/y0/iyi/s0 | SON-FUEL | 3795 | 3° | 4° | 0% | 439 |
| Deniz tabanı/Traktör/y3/iyi/s0 | SON-FUEL | 5637 | 1° | 1° | 0% | 629 |
| Donmuş göl/Cip/y0/iyi/s0 | SON-FUEL | 2444 | 3° | 4° | 0% | 183 |
| Donmuş göl/Cip/y3/iyi/s0 | SON-FUEL | 2663 | 7° | 7° | 0% | 195 |
| Donmuş göl/Kar motosikleti/y3/iyi/s0 | SON-FUEL | 3420 | -0° | -1° | 0% | 229 |
| Donmuş göl/Motosiklet/y0/iyi/s0 | SON-FUEL | 2663 | 7° | 7° | 0% | 190 |
| Donmuş göl/Motosiklet/y3/iyi/s0 | SON-FUEL | 3167 | -1° | -1° | 0% | 216 |
| Donmuş göl/Pikap/y0/iyi/s0 | SON-FUEL | 3145 | 2° | 2° | 0% | 248 |
| Donmuş göl/Pikap/y3/iyi/s0 | SON-FUEL | 3422 | -1° | -1° | 0% | 259 |
| Donmuş göl/Traktör/y0/iyi/s0 | SON-FUEL | 4029 | -8° | -7° | 0% | 421 |
| Donmuş göl/Traktör/y3/iyi/s0 | SON-FUEL | 4365 | 1° | 3° | 0% | 456 |
| Gece çatıları/Cip/y0/iyi/s0 | SON-FUEL | 2411 | 12° | 12° | 0% | 177 |
| Gece çatıları/Cip/y3/iyi/s0 | SON-FUEL | 3981 | -14° | -15° | 0% | 282 |
| Gece çatıları/Motosiklet/y0/iyi/s0 | SON-FUEL | 4062 | 8° | 7° | 0% | 267 |
| Gece çatıları/Motosiklet/y3/iyi/s0 | SON-CRASH | 6438 | -2° | -162° | 0% | 404 |
| Gece çatıları/Pikap/y0/iyi/s0 | SON-FUEL | 4305 | -16° | -17° | 0% | 329 |
| Gece çatıları/Pikap/y3/iyi/s0 | SON-FUEL | 6702 | -5° | -5° | 0% | 507 |
| Gece çatıları/Traktör/y0/iyi/s0 | SON-FUEL | 3993 | -0° | 1° | 0% | 432 |
| Gece çatıları/Traktör/y3/iyi/s0 | SON-FUEL | 4390 | 15° | 15° | 0% | 471 |
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
| Kır yolu/Ay aracı/y0/acemi/s1 | TAKILMA | 3852 | 28° | 31° | 15% | 365 |
| Kır yolu/Ay aracı/y0/acemi/s1 | SON-FUEL | 3769 | 6° | 5° | 0% | 395 |
| Kır yolu/Ay aracı/y0/iyi/s1 | SON-FUEL | 4871 | -27° | -27° | 0% | 399 |
| Kır yolu/Canavar kamyon/y0/acemi/s1 | SON-FUEL | 3780 | -0° | 0° | 0% | 364 |
| Kır yolu/Canavar kamyon/y0/iyi/s1 | SON-FUEL | 3778 | -0° | -0° | 0% | 366 |
| Kır yolu/Chopper/y0/acemi/s1 | SON-FUEL | 4879 | -13° | -12° | 0% | 339 |
| Kır yolu/Chopper/y0/iyi/s1 | SON-FUEL | 4880 | -13° | -13° | 0% | 336 |
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
| Kır yolu/Dörtteker/y0/acemi/s1 | TERS | 2363 | 6° | 115° | 76% | 170 |
| Kır yolu/Dörtteker/y0/acemi/s1 | SON-CRASH | 2369 | 7° | 118° | 76% | 171 |
| Kır yolu/Dörtteker/y0/iyi/s1 | SON-FUEL | 3232 | 1° | 1° | 0% | 247 |
| Kır yolu/Formül araba/y0/acemi/s1 | SON-FUEL | 3131 | -17° | -17° | 0% | 162 |
| Kır yolu/Formül araba/y0/iyi/s1 | SON-FUEL | 3136 | -16° | -16° | 0% | 162 |
| Kır yolu/Go-kart/y0/acemi/s1 | TERS | 2364 | 6° | 108° | 79% | 132 |
| Kır yolu/Go-kart/y0/acemi/s1 | SON-CRASH | 2375 | 5° | 126° | 78% | 135 |
| Kır yolu/Go-kart/y0/iyi/s1 | SON-FUEL | 3253 | 6° | 6° | 0% | 207 |
| Kır yolu/Hot rod/y0/acemi/s1 | SON-FUEL | 3778 | -0° | 1° | 0% | 286 |
| Kır yolu/Hot rod/y0/iyi/s1 | SON-FUEL | 4868 | -29° | -28° | 0% | 299 |
| Kır yolu/Kar motosikleti/y0/acemi/s1 | TERS | 2368 | 7° | 108° | 81% | 149 |
| Kır yolu/Kar motosikleti/y0/acemi/s1 | SON-CRASH | 2376 | 4° | 115° | 81% | 151 |
| Kır yolu/Kar motosikleti/y0/iyi/s1 | SON-FUEL | 4878 | -13° | -14° | 0% | 339 |
| Kır yolu/Kum arabası/y0/acemi/s1 | TERS | 2375 | 5° | 105° | 80% | 149 |
| Kır yolu/Kum arabası/y0/acemi/s1 | SON-CRASH | 2381 | 3° | 116° | 79% | 150 |
| Kır yolu/Kum arabası/y0/iyi/s1 | SON-FUEL | 4540 | 17° | 2° | 0% | 316 |
| Kır yolu/Motosiklet/y0/acemi/s1 | TERS | 2376 | 4° | 115° | 82% | 133 |
| Kır yolu/Motosiklet/y0/acemi/s1 | SON-CRASH | 2387 | 4° | 115° | 81% | 135 |
| Kır yolu/Motosiklet/y0/iyi/s0 | SON-FUEL | 4880 | -13° | -13° | 0% | 306 |
| Kır yolu/Motosiklet/y0/iyi/s1 | SON-FUEL | 4880 | -13° | -13° | 0% | 309 |
| Kır yolu/Motosiklet/y0/iyi/s1 | SON-FUEL | 4880 | -13° | -13° | 0% | 309 |
| Kır yolu/Motosiklet/y0/iyi/s2 | SON-FUEL | 4880 | -17° | -13° | 0% | 311 |
| Kır yolu/Motosiklet/y0/iyi/s3 | SON-FUEL | 4881 | -17° | -15° | 0% | 311 |
| Kır yolu/Motosiklet/y0/iyi/s4 | SON-FUEL | 4879 | -13° | -12° | 0% | 308 |
| Kır yolu/Motosiklet/y0/iyi/s5 | SON-FUEL | 4880 | -17° | -14° | 0% | 309 |
| Kır yolu/Motosiklet/y3/iyi/s0 | SON-FUEL | 6848 | 0° | -22° | 0% | 429 |
| Kır yolu/Otobüs/y0/acemi/s1 | SON-FUEL | 3140 | -14° | -14° | 0% | 298 |
| Kır yolu/Otobüs/y0/iyi/s1 | SON-FUEL | 3140 | -13° | -14° | 0% | 298 |
| Kır yolu/Pikap/y0/acemi/s1 | SON-FUEL | 4875 | -18° | -20° | 0% | 371 |
| Kır yolu/Pikap/y0/iyi/s0 | SON-FUEL | 4876 | -18° | -19° | 0% | 374 |
| Kır yolu/Pikap/y0/iyi/s1 | SON-FUEL | 4875 | -18° | -20° | 0% | 370 |
| Kır yolu/Pikap/y3/iyi/s0 | SON-FUEL | 6856 | -14° | 7° | 0% | 521 |
| Kır yolu/Ralli arabası/y0/acemi/s1 | TERS | 2380 | 3° | 115° | 83% | 123 |
| Kır yolu/Ralli arabası/y0/acemi/s1 | SON-CRASH | 2389 | 7° | 122° | 83% | 124 |
| Kır yolu/Ralli arabası/y0/iyi/s1 | SON-FUEL | 3784 | 2° | 2° | 0% | 275 |
| Kır yolu/Scooter/y0/acemi/s1 | SON-FUEL | 3133 | -17° | -18° | 0% | 251 |
| Kır yolu/Scooter/y0/iyi/s1 | SON-FUEL | 3131 | -17° | -17° | 0% | 251 |
| Kır yolu/Süper araba/y0/acemi/s1 | SON-FUEL | 4882 | -23° | -21° | 0% | 238 |
| Kır yolu/Süper araba/y0/iyi/s1 | TERS | 3577 | 7° | -93° | 85% | 154 |
| Kır yolu/Süper araba/y0/iyi/s1 | SON-CRASH | 3589 | -8° | -125° | 84% | 155 |
| Kır yolu/Tank/y0/acemi/s1 | TAKILMA | 3880 | 26° | 27° | 60% | 398 |
| Kır yolu/Tank/y0/acemi/s1 | SON-FUEL | 4466 | -13° | -14° | 0% | 503 |
| Kır yolu/Tank/y0/iyi/s1 | TAKILMA | 3879 | 30° | 28° | 60% | 397 |
| Kır yolu/Tank/y0/iyi/s1 | SON-FUEL | 3795 | -5° | -5° | 0% | 499 |
| Kır yolu/Traktör/y0/acemi/s1 | SON-FUEL | 4556 | 12° | 11° | 0% | 495 |
| Kır yolu/Traktör/y0/iyi/s0 | SON-FUEL | 3789 | 1° | 1° | 0% | 491 |
| Kır yolu/Traktör/y0/iyi/s1 | TAKILMA | 3497 | 21° | 24° | 13% | 462 |
| Kır yolu/Traktör/y0/iyi/s1 | SON-FUEL | 3448 | -10° | -9° | 0% | 490 |
| Kır yolu/Traktör/y3/iyi/s0 | TAKILMA | 6362 | 28° | 30° | 12% | 683 |
| Kır yolu/Traktör/y3/iyi/s0 | SON-FUEL | 6297 | -3° | -3° | 0% | 714 |
| Kır yolu/Çöp kamyonu/y0/acemi/s1 | TAKILMA | 2182 | 16° | 17° | 85% | 206 |
| Kır yolu/Çöp kamyonu/y0/acemi/s1 | SON-FUEL | 2111 | 3° | 2° | 0% | 322 |
| Kır yolu/Çöp kamyonu/y0/iyi/s1 | TAKILMA | 2182 | 16° | 17° | 85% | 206 |
| Kır yolu/Çöp kamyonu/y0/iyi/s1 | SON-FUEL | 2112 | -2° | -1° | 0% | 322 |
| Kır yolu/İtfaiye aracı/y0/acemi/s1 | TAKILMA | 3546 | 7° | 13° | 59% | 301 |
| Kır yolu/İtfaiye aracı/y0/acemi/s1 | SON-FUEL | 3427 | -2° | -2° | 0% | 382 |
| Kır yolu/İtfaiye aracı/y0/iyi/s1 | TAKILMA | 3546 | 7° | 13° | 59% | 301 |
| Kır yolu/İtfaiye aracı/y0/iyi/s1 | SON-FUEL | 3431 | -7° | -6° | 0% | 382 |
| Mars/Ay aracı/y3/iyi/s0 | SON-FUEL | 7772 | 5° | -1° | 0% | 749 |
| Mars/Cip/y0/iyi/s0 | SON-FUEL | 1719 | -4° | -4° | 0% | 175 |
| Mars/Cip/y3/iyi/s0 | TAKILMA | 2173 | 28° | 28° | 5% | 186 |
| Mars/Cip/y3/iyi/s0 | SON-FUEL | 2128 | -12° | -12° | 0% | 200 |
| Mars/Motosiklet/y0/iyi/s0 | TERS | 978 | 22° | -80° | 80% | 86 |
| Mars/Motosiklet/y0/iyi/s0 | TAKILMA | 1378 | 27° | 28° | 62% | 145 |
| Mars/Motosiklet/y0/iyi/s0 | SON-FUEL | 1350 | 0° | 0° | 0% | 205 |
| Mars/Motosiklet/y3/iyi/s0 | SON-FUEL | 4494 | -8° | -9° | 0% | 492 |
| Mars/Pikap/y0/iyi/s0 | SON-FUEL | 5277 | -10° | -11° | 0% | 476 |
| Mars/Pikap/y3/iyi/s0 | SON-FUEL | 7773 | -1° | -3° | 0% | 672 |
| Mars/Traktör/y0/iyi/s0 | SON-FUEL | 5280 | -9° | -8° | 0% | 605 |
| Mars/Traktör/y3/iyi/s0 | SON-FUEL | 7777 | -6° | -5° | 0% | 875 |
| Mağara/Cip/y0/iyi/s0 | SON-FUEL | 2315 | -31° | -31° | 0% | 207 |
| Mağara/Cip/y3/iyi/s0 | SON-FUEL | 4387 | 27° | 28° | 0% | 322 |
| Mağara/Motosiklet/y0/iyi/s0 | SON-FUEL | 4394 | 34° | -58° | 0% | 316 |
| Mağara/Motosiklet/y3/iyi/s0 | SON-FUEL | 8731 | -23° | -26° | 0% | 579 |
| Mağara/Pikap/y0/iyi/s0 | SON-FUEL | 2348 | 28° | 28° | 0% | 264 |
| Mağara/Pikap/y3/iyi/s0 | SON-FUEL | 3962 | -4° | -4° | 0% | 396 |
| Mağara/Traktör/y0/iyi/s0 | SON-FUEL | 1388 | -23° | -22° | 0% | 257 |

(215 olaydan ilk 160 gösterildi.)

## 5b. Araç özeti (matris, her iki geliştirme düzeyi, iyi sürücü)

| Araç | Ortalama mesafe (m) | Ortalama süre (sn) | CRASH | FUEL | SÜRE sınırı | Koşu |
|---|---|---|---|---|---|---|
| Cip | 2908 | 225 | 0 | 32 | 0 | 32 |
| Motosiklet | 4345 | 330 | 1 | 31 | 0 | 32 |
| Pikap | 4298 | 374 | 0 | 32 | 0 | 32 |
| Traktör | 4068 | 481 | 0 | 32 | 0 | 32 |

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
| Deniz tabanı/Cip/y0/iyi/s0 | 1877 | 7° | 462 | 618 | çukur (1856-1863 m) |
| Deniz tabanı/Cip/y3/iyi/s0 | 2116 | 2° | 211 | 857 | rampa (1996-2001 m), tümsekler (2070-2095 m) |
| Deniz tabanı/Motosiklet/y0/iyi/s0 | 1257 | 5° | 29 | 508 | tümsekler (1115-1129 m) |
| Deniz tabanı/Motosiklet/y3/iyi/s0 | 1994 | -4° | 330 | 735 | tümsekler (1925-1947 m), rampa (1996-2001 m) |
| Deniz tabanı/Pikap/y0/iyi/s0 | 3516 | -3° | 403 | 1200 | çukur (3403-3409 m), tümsekler (3476-3503 m) |
| Deniz tabanı/Pikap/y3/iyi/s0 | 3876 | -1° | 18 | 1560 | çukur (3804-3810 m), tümsekler (3858-3881 m) |
| Deniz tabanı/Traktör/y0/iyi/s0 | 3813 | 3° | 99 | 1497 | çukur (3679-3690 m), çukur (3804-3810 m) |
| Deniz tabanı/Traktör/y3/iyi/s0 | 5645 | 1° | 254 | 1754 | çukur (5520-5528 m), rampa (5629-5633 m) |
| Donmuş göl/Cip/y0/iyi/s0 | 2472 | 3° | 183 | 1101 | tümsekler (2372-2402 m) |
| Donmuş göl/Cip/y3/iyi/s0 | 2715 | 7° | 1769 | 1345 | rampa (2618-2623 m) |
| Donmuş göl/Motosiklet/y0/iyi/s0 | 2711 | 7° | 1769 | 1340 | rampa (2618-2623 m) |
| Donmuş göl/Motosiklet/y3/iyi/s0 | 3185 | -1° | 1264 | 1815 | rampa (3130-3135 m) |
| Donmuş göl/Pikap/y0/iyi/s0 | 3161 | 2° | 1286 | 1791 | rampa (3130-3135 m) |
| Donmuş göl/Pikap/y3/iyi/s0 | 3490 | -1° | 1010 | 2120 | tümsekler (3367-3382 m) |
| Donmuş göl/Traktör/y0/iyi/s0 | 4059 | -8° | 403 | 1434 | çukur (3941-3950 m), rampa (4009-4014 m) |
| Donmuş göl/Traktör/y3/iyi/s0 | 4426 | 1° | 67 | 1802 | tümsekler (4265-4281 m), çukur (4331-4339 m) |
| Gece çatıları/Cip/y0/iyi/s0 | 2486 | 12° | 269 | 1108 | rampa (2336-2342 m), dik iniş (2370-2380 m), çukur (2394-2402 m) |
| Gece çatıları/Cip/y3/iyi/s0 | 4040 | -14° | 525 | 1363 | çukur (3900-3909 m), çukur (3989-4001 m) |
| Gece çatıları/Motosiklet/y0/iyi/s0 | 4109 | 8° | 444 | 1431 | çukur (3989-4001 m), rampa (4070-4075 m) |
| Gece çatıları/Pikap/y0/iyi/s0 | 4364 | -16° | 201 | 1687 | çukur (4224-4235 m), çukur (4316-4326 m) |
| Gece çatıları/Pikap/y3/iyi/s0 | 6711 | -5° | 173 | 2208 | dik iniş (6550-6600 m), rampa (6657-6661 m), rampa (6712-6717 m) |
| Gece çatıları/Traktör/y0/iyi/s0 | 4026 | -0° | 513 | 1349 | çukur (3900-3909 m), çukur (3989-4001 m) |
| Gece çatıları/Traktör/y3/iyi/s0 | 4461 | 15° | 115 | 1783 | çukur (4316-4326 m), çukur (4405-4413 m) |
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
| Mars/Cip/y0/iyi/s0 | 1753 | -4° | 509 | 553 | çukur (1630-1637 m), rampa (1710-1715 m) |
| Mars/Cip/y3/iyi/s0 | 2174 | -12° | 100 | 974 | dik iniş (2070-2090 m), rampa (2123-2128 m) |
| Mars/Motosiklet/y0/iyi/s0 | 1381 | 0° | 878 | 181 | rampa (1347-1352 m) |
| Mars/Motosiklet/y3/iyi/s0 | 4556 | -8° | 1210 | 800 | dik iniş (4370-4410 m), çukur (4417-4427 m), çukur (4516-4524 m) |
| Mars/Pikap/y0/iyi/s0 | 5332 | -10° | 426 | 1577 | dik iniş (5140-5180 m), çukur (5186-5195 m), çukur (5267-5277 m) |
| Mars/Pikap/y3/iyi/s0 | 7809 | -1° | 334 | 2108 | rampa (7683-7687 m), çukur (7770-7777 m) |
| Mars/Traktör/y0/iyi/s0 | 5307 | -9° | 423 | 1552 | dik iniş (5140-5180 m), çukur (5186-5195 m), çukur (5267-5277 m) |
| Mars/Traktör/y3/iyi/s0 | 7797 | -6° | 329 | 2097 | rampa (7683-7687 m), çukur (7770-7777 m) |
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
| Tuz gölü/Cip/y0/iyi/s0 | 2871 | -0° | 390 | 1218 | rampa (2815-2820 m) |
| Tuz gölü/Cip/y3/iyi/s0 | 3137 | 0° | 107 | 1485 | yok |
| Tuz gölü/Motosiklet/y0/iyi/s0 | 4871 | -0° | 595 | 1633 | çukur (4785-4792 m) |
| Tuz gölü/Motosiklet/y3/iyi/s0 | 5250 | 3° | 224 | 2013 | yok |
| Tuz gölü/Pikap/y0/iyi/s0 | 4912 | -1° | 596 | 1675 | çukur (4785-4792 m) |
| Tuz gölü/Pikap/y3/iyi/s0 | 7813 | -3° | 489 | 2373 | çukur (7732-7741 m) |
| Tuz gölü/Traktör/y0/iyi/s0 | 3092 | -3° | 188 | 1440 | çukur (2942-2950 m) |
| Tuz gölü/Traktör/y3/iyi/s0 | 5189 | 2° | 284 | 1952 | yok |
| Volkan/Cip/y0/iyi/s0 | 2257 | 3° | 818 | 365 | dik iniş (2120-2150 m), rampa (2195-2199 m), dik yokuş (2230-2280 m) |
| Volkan/Cip/y3/iyi/s0 | 4287 | 33° | 343 | 1247 | dik iniş (4080-4120 m), çukur (4129-4138 m), dik yokuş (4150-4210 m) |
| Volkan/Motosiklet/y0/iyi/s0 | 2505 | -31° | 656 | 612 | dik yokuş (2230-2280 m), çukur (2293-2302 m), dik iniş (2360-2390 m) |
| Volkan/Motosiklet/y3/iyi/s0 | 6282 | 29° | 110 | 1763 | tümsekler (6078-6101 m), dik iniş (6110-6140 m), çukur (6147-6156 m), tümsekler (6202-6229 m) |
| Volkan/Pikap/y0/iyi/s0 | 2255 | 20° | 815 | 363 | dik iniş (2120-2150 m), rampa (2195-2199 m), dik yokuş (2230-2280 m) |
| Volkan/Pikap/y3/iyi/s0 | 3601 | 1° | 1022 | 561 | tümsekler (3385-3405 m), dik iniş (3420-3440 m), çukur (3453-3464 m), rampa (3509-3514 m) |
| Volkan/Traktör/y0/iyi/s0 | 1279 | -14° | 658 | 184 | dik iniş (1170-1180 m), çukur (1195-1202 m), dik yokuş (1260-1290 m) |
| Volkan/Traktör/y3/iyi/s0 | 2255 | 14° | 816 | 363 | dik iniş (2120-2150 m), rampa (2195-2199 m), dik yokuş (2230-2280 m) |
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
| Tuz gölü | 600 | 1088 | 55% | 812 | 23% |
| Kutup | 450 | 936 | 48% | 471 | 51% |
| Ay | 500 | 978 | 51% | 723 | 15% |
| Donmuş göl | 550 | 1011 | 54% | 587 | 43% |
| Orman | 500 | 1023 | 49% | 531 | 48% |
| Gece çatıları | 550 | 1067 | 52% | 594 | 44% |
| Dağ | 400 | 1008 | 40% | 753 | 24% |
| Plaj | 600 | 1052 | 57% | 864 | 17% |
| Mağara | 450 | 829 | 54% | 846 | 14% |
| Mars | 500 | 823 | 61% | 481 | 58% |
| Bataklık | 550 | 1011 | 54% | 573 | 42% |
| Volkan | 420 | 777 | 54% | 797 | 18% |
| Şantiye | 600 | 1050 | 57% | 809 | 24% |
| Deniz tabanı | 500 | 583 | 86% | 510 | 12% |

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
| Donmuş göl | 2185-2290 | 105 | 11.7 | 15° | 12% |
| Donmuş göl | 1980-2085 | 105 | 10.4 | 14° | 12% |
| Orman | 2010-2120 | 110 | 26.8 | 41° | 13% |
| Orman | 1510-1595 | 85 | 22.5 | 32° | 10% |
| Orman | 1685-1765 | 80 | 26.1 | 36° | 10% |
| Gece çatıları | 2235-2340 | 105 | 15.8 | 24° | 11% |
| Gece çatıları | 2030-2115 | 85 | 16.3 | 27° | 10% |
| Gece çatıları | 1655-1745 | 90 | 14.8 | 27° | 9% |
| Dağ | 2275-2395 | 120 | 37.6 | 43° | 17% |
| Dağ | 1880-2000 | 120 | 30.8 | 47° | 16% |
| Dağ | 2525-2625 | 100 | 45.9 | 42° | 16% |
| Plaj | 2300-2415 | 115 | 15.5 | 27° | 12% |
| Plaj | 2515-2630 | 115 | 19.1 | 19° | 12% |
| Plaj | 2130-2235 | 105 | 14.5 | 20° | 11% |
| Mağara | 1840-1995 | 155 | 36.2 | 37° | 18% |
| Mağara | 1400-1540 | 140 | 29.3 | 38° | 16% |
| Mağara | 2105-2220 | 115 | 45.9 | 36° | 15% |
| Mars | 215-330 | 115 | 11.8 | 17° | 11% |
| Mars | 1330-1410 | 80 | 20.4 | 31° | 11% |
| Mars | 775-890 | 115 | 16.7 | 26° | 10% |
| Bataklık | 1800-1940 | 140 | 22.8 | 27° | 15% |
| Bataklık | 2045-2150 | 105 | 26.8 | 25° | 14% |
| Bataklık | 1380-1505 | 125 | 18.2 | 23° | 13% |
| Volkan | 1990-2105 | 115 | 38.8 | 37° | 16% |
| Volkan | 1780-1885 | 105 | 36.8 | 36° | 14% |
| Volkan | 1590-1695 | 105 | 28.4 | 44° | 13% |
| Şantiye | 2320-2490 | 170 | 22.9 | 24° | 18% |
| Şantiye | 1840-1990 | 150 | 18.7 | 23° | 15% |
| Şantiye | 1205-1335 | 130 | 15.2 | 27° | 13% |
| Deniz tabanı | 1535-1625 | 90 | 12.1 | 25° | 18% |
| Deniz tabanı | 1365-1450 | 85 | 13.2 | 17° | 17% |
| Deniz tabanı | 840-930 | 90 | 11.8 | 19° | 16% |

## 8. Duyurular ve karıştırıcı

Mixer.voiceCount en yüksek: **16** (sınır 60). Koşu başına duyuru: ortalama 77.5; en uzun sessizlik: 35 sn (Deniz tabanı/Motosiklet/y3/iyi/s0).

| Koşu | En uzun sessizlik (sn) | Duyuru sayısı | Süre (sn) |
|---|---|---|---|
| Deniz tabanı/Motosiklet/y3/iyi/s0 | 35 | 27 | 297 |
| Kır yolu/Çöp kamyonu/y0/iyi/s1 | 33 | 39 | 322 |
| Tuz gölü/Traktör/y3/iyi/s0 | 33 | 62 | 531 |
| Deniz tabanı/Motosiklet/y0/iyi/s0 | 30 | 20 | 212 |
| Volkan/Traktör/y0/iyi/s0 | 29 | 33 | 261 |
| Tuz gölü/Cip/y3/iyi/s0 | 29 | 37 | 220 |

## 9. Ekonomi

Her altın tam 1 değerindedir (kademe yok); yoğunluk erken haritada ~12-16 altın/100 m'dir ve harita fiyatıyla artar. Fiyatlar (araç, harita, geliştirme) bu gelire göre ölçeklenmiştir ve aşağıdaki ekonomi kapıları gerçek kapıdır.

Sürücü yakıt bitene ya da 20 dk dolana kadar oynadığı için koşu başı altın gerçek bir oyuncunun kısa koşularından çok daha yüksek çıkar; bu yüzden altın/dakika da verilir. "Koşu" = tam bir koşu (uzun), "dk" = dakika cinsinden oyun süresi.

| Harita | Araç | Geliştirme | Koşu başı altın | Süre (sn) | Altın/dk | Sıradaki araç | Sıradaki harita |
|---|---|---|---|---|---|---|---|
| Kır yolu | Cip | y0 | 432 | 218 | 119 | Scooter (450): 2 koşu, 4 dk | Çöl (1000): 3 koşu, 9 dk |
| Kır yolu | Cip | y3 | 650 | 316 | 123 | Scooter (450): 1 koşu, 4 dk | Çöl (1000): 2 koşu, 9 dk |
| Kır yolu | Motosiklet | y0 | 692 | 309 | 134 | Go-kart (1700): 3 koşu, 13 dk | Çöl (1000): 2 koşu, 8 dk |
| Kır yolu | Motosiklet | y3 | 1137 | 429 | 159 | Go-kart (1700): 2 koşu, 11 dk | Çöl (1000): 1 koşu, 7 dk |
| Kır yolu | Pikap | y0 | 699 | 372 | 113 | Dörtteker (2900): 5 koşu, 26 dk | Çöl (1000): 2 koşu, 9 dk |
| Kır yolu | Pikap | y3 | 1045 | 521 | 120 | Dörtteker (2900): 3 koşu, 25 dk | Çöl (1000): 1 koşu, 9 dk |
| Kır yolu | Traktör | y0 | 505 | 490 | 62 | Kum arabası (5000): 10 koşu, 82 dk | Çöl (1000): 2 koşu, 17 dk |
| Kır yolu | Traktör | y3 | 950 | 714 | 80 | Kum arabası (5000): 6 koşu, 63 dk | Çöl (1000): 2 koşu, 13 dk |
| Çöl | Cip | y0 | 307 | 164 | 112 | Scooter (450): 2 koşu, 5 dk | Tuz gölü (1300): 5 koşu, 12 dk |
| Çöl | Cip | y3 | 512 | 266 | 116 | Scooter (450): 1 koşu, 4 dk | Tuz gölü (1300): 3 koşu, 12 dk |
| Çöl | Motosiklet | y0 | 512 | 269 | 114 | Go-kart (1700): 4 koşu, 15 dk | Tuz gölü (1300): 3 koşu, 12 dk |
| Çöl | Motosiklet | y3 | 809 | 378 | 128 | Go-kart (1700): 3 koşu, 14 dk | Tuz gölü (1300): 2 koşu, 11 dk |
| Çöl | Pikap | y0 | 470 | 298 | 95 | Dörtteker (2900): 7 koşu, 31 dk | Tuz gölü (1300): 3 koşu, 14 dk |
| Çöl | Pikap | y3 | 620 | 445 | 84 | Dörtteker (2900): 5 koşu, 35 dk | Tuz gölü (1300): 3 koşu, 16 dk |
| Çöl | Traktör | y0 | 470 | 398 | 71 | Kum arabası (5000): 11 koşu, 71 dk | Tuz gölü (1300): 3 koşu, 19 dk |
| Çöl | Traktör | y3 | 766 | 609 | 75 | Kum arabası (5000): 7 koşu, 67 dk | Tuz gölü (1300): 2 koşu, 18 dk |
| Tuz gölü | Cip | y0 | 402 | 206 | 117 | Scooter (450): 2 koşu, 4 dk | Kutup (1800): 5 koşu, 16 dk |
| Tuz gölü | Cip | y3 | 446 | 220 | 122 | Scooter (450): 2 koşu, 4 dk | Kutup (1800): 5 koşu, 15 dk |
| Tuz gölü | Motosiklet | y0 | 759 | 332 | 137 | Go-kart (1700): 3 koşu, 13 dk | Kutup (1800): 3 koşu, 14 dk |
| Tuz gölü | Motosiklet | y3 | 817 | 309 | 159 | Go-kart (1700): 3 koşu, 11 dk | Kutup (1800): 3 koşu, 12 dk |
| Tuz gölü | Pikap | y0 | 762 | 350 | 131 | Dörtteker (2900): 4 koşu, 23 dk | Kutup (1800): 3 koşu, 14 dk |
| Tuz gölü | Pikap | y3 | 1249 | 561 | 134 | Dörtteker (2900): 3 koşu, 22 dk | Kutup (1800): 2 koşu, 14 dk |
| Tuz gölü | Traktör | y0 | 441 | 320 | 83 | Kum arabası (5000): 12 koşu, 61 dk | Kutup (1800): 5 koşu, 22 dk |
| Tuz gölü | Traktör | y3 | 811 | 531 | 92 | Kum arabası (5000): 7 koşu, 55 dk | Kutup (1800): 3 koşu, 20 dk |
| Kutup | Cip | y0 | 292 | 160 | 109 | Scooter (450): 2 koşu, 5 dk | Ay (2800): 10 koşu, 26 dk |
| Kutup | Cip | y3 | 385 | 234 | 99 | Scooter (450): 2 koşu, 5 dk | Ay (2800): 8 koşu, 29 dk |
| Kutup | Motosiklet | y0 | 337 | 228 | 89 | Go-kart (1700): 6 koşu, 20 dk | Ay (2800): 9 koşu, 32 dk |
| Kutup | Motosiklet | y3 | 424 | 248 | 103 | Go-kart (1700): 5 koşu, 17 dk | Ay (2800): 7 koşu, 28 dk |
| Kutup | Pikap | y0 | 385 | 277 | 83 | Dörtteker (2900): 8 koşu, 35 dk | Ay (2800): 8 koşu, 34 dk |
| Kutup | Pikap | y3 | 385 | 303 | 76 | Dörtteker (2900): 8 koşu, 39 dk | Ay (2800): 8 koşu, 37 dk |
| Kutup | Traktör | y0 | 424 | 363 | 70 | Kum arabası (5000): 12 koşu, 72 dk | Ay (2800): 7 koşu, 40 dk |
| Kutup | Traktör | y3 | 585 | 539 | 65 | Kum arabası (5000): 9 koşu, 77 dk | Ay (2800): 5 koşu, 43 dk |
| Ay | Cip | y0 | 2005 | 191 | 631 | Scooter (450): 1 koşu, 1 dk | Donmuş göl (3500): 2 koşu, 6 dk |
| Ay | Cip | y3 | 3142 | 269 | 701 | Scooter (450): 1 koşu, 1 dk | Donmuş göl (3500): 2 koşu, 5 dk |
| Ay | Motosiklet | y0 | 1361 | 326 | 250 | Go-kart (1700): 2 koşu, 7 dk | Donmuş göl (3500): 3 koşu, 14 dk |
| Ay | Motosiklet | y3 | 3278 | 483 | 407 | Go-kart (1700): 1 koşu, 5 dk | Donmuş göl (3500): 2 koşu, 9 dk |
| Ay | Pikap | y0 | 1942 | 191 | 612 | Dörtteker (2900): 2 koşu, 5 dk | Donmuş göl (3500): 2 koşu, 6 dk |
| Ay | Pikap | y3 | 3412 | 282 | 725 | Dörtteker (2900): 1 koşu, 4 dk | Donmuş göl (3500): 2 koşu, 5 dk |
| Ay | Traktör | y0 | 5150 | 665 | 465 | Kum arabası (5000): 1 koşu, 11 dk | Donmuş göl (3500): 1 koşu, 8 dk |
| Ay | Traktör | y3 | 7466 | 969 | 462 | Kum arabası (5000): 1 koşu, 11 dk | Donmuş göl (3500): 1 koşu, 8 dk |
| Donmuş göl | Cip | y0 | 349 | 183 | 114 | Scooter (450): 2 koşu, 4 dk | Orman (4200): 13 koşu, 37 dk |
| Donmuş göl | Cip | y3 | 381 | 195 | 117 | Scooter (450): 2 koşu, 4 dk | Orman (4200): 12 koşu, 36 dk |
| Donmuş göl | Motosiklet | y0 | 380 | 190 | 120 | Go-kart (1700): 5 koşu, 15 dk | Orman (4200): 12 koşu, 35 dk |
| Donmuş göl | Motosiklet | y3 | 453 | 216 | 126 | Go-kart (1700): 4 koşu, 14 dk | Orman (4200): 10 koşu, 34 dk |
| Donmuş göl | Pikap | y0 | 455 | 248 | 110 | Dörtteker (2900): 7 koşu, 27 dk | Orman (4200): 10 koşu, 39 dk |
| Donmuş göl | Pikap | y3 | 503 | 259 | 116 | Dörtteker (2900): 6 koşu, 25 dk | Orman (4200): 9 koşu, 37 dk |
| Donmuş göl | Traktör | y0 | 592 | 421 | 84 | Kum arabası (5000): 9 koşu, 60 dk | Orman (4200): 8 koşu, 50 dk |
| Donmuş göl | Traktör | y3 | 649 | 456 | 85 | Kum arabası (5000): 8 koşu, 59 dk | Orman (4200): 7 koşu, 50 dk |
| Orman | Cip | y0 | 322 | 167 | 116 | Scooter (450): 2 koşu, 4 dk | Gece çatıları (5000): 16 koşu, 44 dk |
| Orman | Cip | y3 | 533 | 261 | 122 | Scooter (450): 1 koşu, 4 dk | Gece çatıları (5000): 10 koşu, 41 dk |
| Orman | Motosiklet | y0 | 717 | 258 | 166 | Go-kart (1700): 3 koşu, 11 dk | Gece çatıları (5000): 7 koşu, 31 dk |
| Orman | Motosiklet | y3 | 1164 | 384 | 182 | Go-kart (1700): 2 koşu, 10 dk | Gece çatıları (5000): 5 koşu, 28 dk |
| Orman | Pikap | y0 | 569 | 307 | 111 | Dörtteker (2900): 6 koşu, 27 dk | Gece çatıları (5000): 9 koşu, 46 dk |
| Orman | Pikap | y3 | 922 | 594 | 93 | Dörtteker (2900): 4 koşu, 32 dk | Gece çatıları (5000): 6 koşu, 54 dk |
| Orman | Traktör | y0 | 347 | 396 | 53 | Kum arabası (5000): 15 koşu, 96 dk | Gece çatıları (5000): 15 koşu, 96 dk |
| Orman | Traktör | y3 | 569 | 445 | 77 | Kum arabası (5000): 9 koşu, 66 dk | Gece çatıları (5000): 9 koşu, 66 dk |
| Gece çatıları | Cip | y0 | 380 | 177 | 129 | Scooter (450): 2 koşu, 4 dk | Dağ (5800): 16 koşu, 45 dk |
| Gece çatıları | Cip | y3 | 628 | 282 | 133 | Scooter (450): 1 koşu, 4 dk | Dağ (5800): 10 koşu, 44 dk |
| Gece çatıları | Motosiklet | y0 | 696 | 267 | 156 | Go-kart (1700): 3 koşu, 11 dk | Dağ (5800): 9 koşu, 38 dk |
| Gece çatıları | Motosiklet | y3 | 1333 | 404 | 198 | Go-kart (1700): 2 koşu, 9 dk | Dağ (5800): 5 koşu, 30 dk |
| Gece çatıları | Pikap | y0 | 692 | 329 | 126 | Dörtteker (2900): 5 koşu, 23 dk | Dağ (5800): 9 koşu, 46 dk |
| Gece çatıları | Pikap | y3 | 1105 | 507 | 131 | Dörtteker (2900): 3 koşu, 23 dk | Dağ (5800): 6 koşu, 45 dk |
| Gece çatıları | Traktör | y0 | 641 | 432 | 89 | Kum arabası (5000): 8 koşu, 57 dk | Dağ (5800): 10 koşu, 66 dk |
| Gece çatıları | Traktör | y3 | 701 | 471 | 89 | Kum arabası (5000): 8 koşu, 57 dk | Dağ (5800): 9 koşu, 65 dk |
| Dağ | Cip | y0 | 477 | 213 | 134 | Scooter (450): 1 koşu, 4 dk | Plaj (7200): 16 koşu, 54 dk |
| Dağ | Cip | y3 | 740 | 312 | 142 | Scooter (450): 1 koşu, 4 dk | Plaj (7200): 10 koşu, 51 dk |
| Dağ | Motosiklet | y0 | 888 | 321 | 166 | Go-kart (1700): 2 koşu, 11 dk | Plaj (7200): 9 koşu, 44 dk |
| Dağ | Motosiklet | y3 | 1964 | 692 | 170 | Go-kart (1700): 1 koşu, 10 dk | Plaj (7200): 4 koşu, 43 dk |
| Dağ | Pikap | y0 | 366 | 259 | 85 | Dörtteker (2900): 8 koşu, 35 dk | Plaj (7200): 20 koşu, 85 dk |
| Dağ | Pikap | y3 | 525 | 376 | 84 | Dörtteker (2900): 6 koşu, 35 dk | Plaj (7200): 14 koşu, 86 dk |
| Dağ | Traktör | y0 | 298 | 346 | 52 | Kum arabası (5000): 17 koşu, 97 dk | Plaj (7200): 25 koşu, 140 dk |
| Dağ | Traktör | y3 | 439 | 374 | 70 | Kum arabası (5000): 12 koşu, 72 dk | Plaj (7200): 17 koşu, 103 dk |
| Plaj | Cip | y0 | 421 | 195 | 129 | Scooter (450): 2 koşu, 4 dk | Mağara (8600): 21 koşu, 67 dk |
| Plaj | Cip | y3 | 509 | 228 | 134 | Scooter (450): 1 koşu, 4 dk | Mağara (8600): 17 koşu, 65 dk |
| Plaj | Motosiklet | y0 | 514 | 279 | 110 | Go-kart (1700): 4 koşu, 16 dk | Mağara (8600): 17 koşu, 78 dk |
| Plaj | Motosiklet | y3 | 843 | 314 | 161 | Go-kart (1700): 3 koşu, 11 dk | Mağara (8600): 11 koşu, 54 dk |
| Plaj | Pikap | y0 | 791 | 353 | 134 | Dörtteker (2900): 4 koşu, 22 dk | Mağara (8600): 11 koşu, 65 dk |
| Plaj | Pikap | y3 | 897 | 394 | 137 | Dörtteker (2900): 4 koşu, 22 dk | Mağara (8600): 10 koşu, 63 dk |
| Plaj | Traktör | y0 | 479 | 325 | 88 | Kum arabası (5000): 11 koşu, 57 dk | Mağara (8600): 18 koşu, 98 dk |
| Plaj | Traktör | y3 | 829 | 525 | 95 | Kum arabası (5000): 7 koşu, 53 dk | Mağara (8600): 11 koşu, 91 dk |
| Mağara | Cip | y0 | 376 | 207 | 109 | Scooter (450): 2 koşu, 5 dk | Mars (9300): 25 koşu, 86 dk |
| Mağara | Cip | y3 | 774 | 322 | 144 | Scooter (450): 1 koşu, 4 dk | Mars (9300): 13 koşu, 65 dk |
| Mağara | Motosiklet | y0 | 784 | 316 | 149 | Go-kart (1700): 3 koşu, 12 dk | Mars (9300): 12 koşu, 63 dk |
| Mağara | Motosiklet | y3 | 1691 | 579 | 175 | Go-kart (1700): 2 koşu, 10 dk | Mars (9300): 6 koşu, 54 dk |
| Mağara | Pikap | y0 | 376 | 264 | 86 | Dörtteker (2900): 8 koşu, 34 dk | Mars (9300): 25 koşu, 109 dk |
| Mağara | Pikap | y3 | 645 | 396 | 98 | Dörtteker (2900): 5 koşu, 30 dk | Mars (9300): 15 koşu, 96 dk |
| Mağara | Traktör | y0 | 230 | 257 | 54 | Kum arabası (5000): 22 koşu, 94 dk | Mars (9300): 41 koşu, 174 dk |
| Mağara | Traktör | y3 | 376 | 388 | 58 | Kum arabası (5000): 14 koşu, 86 dk | Mars (9300): 25 koşu, 160 dk |
| Mars | Cip | y0 | 650 | 175 | 223 | Scooter (450): 1 koşu, 3 dk | Bataklık (10000): 16 koşu, 45 dk |
| Mars | Cip | y3 | 1048 | 200 | 314 | Scooter (450): 1 koşu, 2 dk | Bataklık (10000): 10 koşu, 32 dk |
| Mars | Motosiklet | y0 | 375 | 205 | 110 | Go-kart (1700): 5 koşu, 16 dk | Bataklık (10000): 27 koşu, 91 dk |
| Mars | Motosiklet | y3 | 2180 | 492 | 266 | Go-kart (1700): 1 koşu, 7 dk | Bataklık (10000): 5 koşu, 38 dk |
| Mars | Pikap | y0 | 2368 | 476 | 299 | Dörtteker (2900): 2 koşu, 10 dk | Bataklık (10000): 5 koşu, 34 dk |
| Mars | Pikap | y3 | 3613 | 672 | 323 | Dörtteker (2900): 1 koşu, 9 dk | Bataklık (10000): 3 koşu, 31 dk |
| Mars | Traktör | y0 | 1449 | 605 | 144 | Kum arabası (5000): 4 koşu, 35 dk | Bataklık (10000): 7 koşu, 70 dk |
| Mars | Traktör | y3 | 2688 | 875 | 184 | Kum arabası (5000): 2 koşu, 28 dk | Bataklık (10000): 4 koşu, 55 dk |
| Bataklık | Cip | y0 | 413 | 175 | 142 | Scooter (450): 2 koşu, 4 dk | Volkan (11500): 28 koşu, 82 dk |
| Bataklık | Cip | y3 | 677 | 276 | 147 | Scooter (450): 1 koşu, 4 dk | Volkan (11500): 17 koşu, 79 dk |
| Bataklık | Motosiklet | y0 | 568 | 259 | 132 | Go-kart (1700): 3 koşu, 13 dk | Volkan (11500): 21 koşu, 88 dk |
| Bataklık | Motosiklet | y3 | 746 | 284 | 158 | Go-kart (1700): 3 koşu, 11 dk | Volkan (11500): 16 koşu, 73 dk |
| Bataklık | Pikap | y0 | 687 | 308 | 134 | Dörtteker (2900): 5 koşu, 22 dk | Volkan (11500): 17 koşu, 86 dk |
| Bataklık | Pikap | y3 | 841 | 479 | 105 | Dörtteker (2900): 4 koşu, 28 dk | Volkan (11500): 14 koşu, 110 dk |
| Bataklık | Traktör | y0 | 677 | 413 | 98 | Kum arabası (5000): 8 koşu, 51 dk | Volkan (11500): 17 koşu, 117 dk |
| Bataklık | Traktör | y3 | 740 | 470 | 95 | Kum arabası (5000): 7 koşu, 53 dk | Volkan (11500): 16 koşu, 122 dk |
| Volkan | Cip | y0 | 434 | 209 | 125 | Scooter (450): 2 koşu, 4 dk | Şantiye (13000): 30 koşu, 105 dk |
| Volkan | Cip | y3 | 815 | 321 | 152 | Scooter (450): 1 koşu, 3 dk | Şantiye (13000): 16 koşu, 86 dk |
| Volkan | Motosiklet | y0 | 526 | 231 | 137 | Go-kart (1700): 4 koşu, 13 dk | Şantiye (13000): 25 koşu, 96 dk |
| Volkan | Motosiklet | y3 | 1480 | 443 | 201 | Go-kart (1700): 2 koşu, 9 dk | Şantiye (13000): 9 koşu, 65 dk |
| Volkan | Pikap | y0 | 384 | 266 | 86 | Dörtteker (2900): 8 koşu, 34 dk | Şantiye (13000): 34 koşu, 151 dk |
| Volkan | Pikap | y3 | 643 | 391 | 99 | Dörtteker (2900): 5 koşu, 30 dk | Şantiye (13000): 21 koşu, 132 dk |
| Volkan | Traktör | y0 | 221 | 261 | 51 | Kum arabası (5000): 23 koşu, 99 dk | Şantiye (13000): 59 koşu, 257 dk |
| Volkan | Traktör | y3 | 384 | 389 | 59 | Kum arabası (5000): 14 koşu, 85 dk | Şantiye (13000): 34 koşu, 220 dk |
| Şantiye | Cip | y0 | 466 | 189 | 148 | Scooter (450): 1 koşu, 4 dk | Deniz tabanı (14500): 32 koşu, 98 dk |
| Şantiye | Cip | y3 | 528 | 214 | 148 | Scooter (450): 1 koşu, 4 dk | Deniz tabanı (14500): 28 koşu, 98 dk |
| Şantiye | Motosiklet | y0 | 1082 | 302 | 215 | Go-kart (1700): 2 koşu, 8 dk | Deniz tabanı (14500): 14 koşu, 68 dk |
| Şantiye | Motosiklet | y3 | 1263 | 321 | 236 | Go-kart (1700): 2 koşu, 8 dk | Deniz tabanı (14500): 12 koşu, 62 dk |
| Şantiye | Pikap | y0 | 864 | 358 | 145 | Dörtteker (2900): 4 koşu, 21 dk | Deniz tabanı (14500): 17 koşu, 101 dk |
| Şantiye | Pikap | y3 | 962 | 391 | 148 | Dörtteker (2900): 4 koşu, 20 dk | Deniz tabanı (14500): 16 koşu, 99 dk |
| Şantiye | Traktör | y0 | 528 | 320 | 99 | Kum arabası (5000): 10 koşu, 51 dk | Deniz tabanı (14500): 28 koşu, 147 dk |
| Şantiye | Traktör | y3 | 948 | 545 | 104 | Kum arabası (5000): 6 koşu, 48 dk | Deniz tabanı (14500): 16 koşu, 140 dk |
| Deniz tabanı | Cip | y0 | 325 | 229 | 85 | Scooter (450): 2 koşu, 6 dk | - |
| Deniz tabanı | Cip | y3 | 419 | 234 | 108 | Scooter (450): 2 koşu, 5 dk | - |
| Deniz tabanı | Motosiklet | y0 | 212 | 212 | 60 | Go-kart (1700): 9 koşu, 29 dk | - |
| Deniz tabanı | Motosiklet | y3 | 344 | 297 | 69 | Go-kart (1700): 5 koşu, 25 dk | - |
| Deniz tabanı | Pikap | y0 | 773 | 375 | 124 | Dörtteker (2900): 4 koşu, 24 dk | - |
| Deniz tabanı | Pikap | y3 | 841 | 365 | 138 | Dörtteker (2900): 4 koşu, 21 dk | - |
| Deniz tabanı | Traktör | y0 | 841 | 439 | 115 | Kum arabası (5000): 6 koşu, 44 dk | - |
| Deniz tabanı | Traktör | y3 | 1377 | 629 | 131 | Kum arabası (5000): 4 koşu, 39 dk | - |

Kır yolu + jeep ortalaması (6 koşu): 432 altın/koşu. Jeep için 4 geliştirmenin 3. seviyeye maliyeti: 1240 altın = 3 koşu; Scooter (450): 2 koşu; Çöl haritası (1000): 3 koşu.

**İlerleme özeti (kapılarla aynı hesap):** Kır yolu + jeep (geliştirme yok) iyi koşu ortalaması 432 altın, medyan koşu süresi 3.6 dk. Scooter (450) = 1.0 koşu, Çöl (1000) = 2.3 koşu. Matristeki tüm iyi sürücü koşularının ortalama hızı 160 altın/dk. Tüm araçlar ve haritalar (311750 altın): ilerleme benzetimiyle ≈ **31.8 saat**. Hedef: Scooter 1-2 koşu, Çöl 2-3 koşu, tüm mağaza 25-40 saat. Benzetim: ürünler fiyat sırasıyla alınır, oyuncu sahip olduğu haritaların hepsini sırayla oynar; harita başına jeep y0 altın/dk: Kır yolu 119, Çöl 112, Tuz gölü 117, Kutup 109, Ay 631, Donmuş göl 114, Orman 116, Gece çatıları 129, Dağ 134, Plaj 129, Mağara 109, Mars 223, Bataklık 142, Volkan 125, Şantiye 148, Deniz tabanı 85. Geliştirmeler (isteğe bağlı) dahil değildir. Ay ve Mars'taki havada kalma bonusu altını 2-5 kat artırır; oyuncu bu iki haritayı hiç oynamazsa en kötü durum ≈ 43.4 saat.

Tüm araçlar ve haritalar toplam 311750 altın. Yalnızca Kır yolu + jeep hızıyla (119 altın/dk) hepsini almak ≈ 44 saat oyun sürerdi; zengin haritalar (Ay, Mars, Şantiye vb.) hızı artırdığı için benzetim daha kısa çıkar.

## 10. Sorunlar (ciddiyet sırasıyla)

1. **[Yüksek]** İlk 1500 m içinde biten 4 koşu: Deniz tabanı/Motosiklet/y0/iyi/s0 1257 m FUEL (eğim 5°); Volkan/Traktör/y0/iyi/s0 1279 m FUEL (eğim -14°); Mars/Motosiklet/y0/iyi/s0 1381 m FUEL (eğim 0°); Mağara/Traktör/y0/iyi/s0 1475 m FUEL (eğim -23°).
2. **[Yüksek]** Yerde göreli açı 100°'yi geçen (ters dönme) olay 9. En sık: Kır yolu x≈2300 m (eğim 4°, açı 115°, 6 kez); Ay x≈2200 m (eğim -3°, açı -103°, 1 kez); Mars x≈900 m (eğim 22°, açı -80°, 1 kez); Kır yolu x≈3500 m (eğim 7°, açı -93°, 1 kez).
3. **[Orta]** Takılma 18 kez görüldü. En sık noktalar: Kır yolu x≈3850 m (eğim 28°, 3 kez); Kır yolu x≈2150 m (eğim 16°, 2 kez); Kır yolu x≈3500 m (eğim 7°, 2 kez); Kır yolu x≈6350 m (eğim 28°, 1 kez); Kutup x≈2550 m (eğim 20°, 1 kez); Kutup x≈3850 m (eğim 27°, 1 kez).
4. **[Orta]** Bidona %5'ten az yakıtla ulaşılan 29 koşu var (marj çok dar): Kır yolu/Cip/y3/iyi/s0; Kır yolu/Traktör/y0/iyi/s0; Kır yolu/Traktör/y3/iyi/s0; Tuz gölü/Motosiklet/y0/iyi/s0; Tuz gölü/Pikap/y0/iyi/s0.
5. **[Düşük]** (Tasarım gereği: bidon aralığı mesafeyle uzadığı için iyi koşular çoğunlukla yakıtla biter.) Yakıt bitişiyle sonlanan koşu: 180/188. Haritalara göre: Kır yolu 54, Çöl 8, Tuz gölü 9, Kutup 9, Ay 9, Donmuş göl 9, Orman 8, Gece çatıları 7, Dağ 8, Plaj 8, Mağara 8, Mars 9, Bataklık 8, Volkan 9, Şantiye 8, Deniz tabanı 9. Bunların 25 tanesi bir sonraki bidona 150 m'den az kala bitiyor (menzil marjı çok dar), 18 tanesinde bitişten önce yamaçta takılma olayı var (yani yakıt, yamaçta çıkamayıp beklerken yanmış). "Yakıt bitti" duyurusu oyuncuya nedeni söylemez.
6. **[Düşük]** 30 sn'den uzun duyurusuz süre olan 3 koşu (en uzunu 35 sn). Mesafe duyurusu (250 m) varsayılan açık; bu süreler o duyurular dahil ölçülür.

## 11. Ayar önerileri

- Deniz tabanı: jeep deposu ortalama 583 m menzil veriyor, bidon aralığı 500 m (menzilin %86'i). `fuelEvery` değerini en çok 350 m'ye indirin ya da jeep `fuelUse` değerini %40 azaltın.
- Kır yolu x≈2350 m: 6 takılma/ters olayı (Motosiklet/Go-kart/Dörtteker/Kum arabası/Ralli arabası/Kar motosikleti); olay noktasında zemin eğimi düşük (4°) ama önceki 60 m'de en dik yamaç 37°: araç yamaçta burnunu çok kaldırıp düzlükte arkaya devriliyor (gaz-burun yukarı). Yakındaki özellikler: tümsekler (2266-2285 m), rampa (2347-2351 m). Denge yardımının eşiğini (40°) ve gaz kısma eğrisini bu parçada sınayın; yamaç sonu eğimini yumuşatın.
- Kır yolu x≈3850 m: 3 takılma/ters olayı (Ay aracı/Tank), zemin eğimi 28°. Bu yamacın en dik kısmını 20° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Kır yolu x≈2150 m: 2 takılma/ters olayı (Çöp kamyonu), zemin eğimi 16°. Bu yamacın en dik kısmını 10° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Kır yolu x≈3500 m: 2 takılma/ters olayı (İtfaiye aracı); olay noktasında zemin eğimi düşük (7°) ama önceki 60 m'de en dik yamaç 20°: araç yamaçta burnunu çok kaldırıp düzlükte arkaya devriliyor (gaz-burun yukarı). Yakındaki özellikler: çukur (3441-3450 m), dik yokuş (3510-3540 m). Denge yardımının eşiğini (40°) ve gaz kısma eğrisini bu parçada sınayın; yamaç sonu eğimini yumuşatın.
- Kır yolu x≈6350 m: 1 takılma/ters olayı (Traktör), zemin eğimi 28°. Bu yamacın en dik kısmını 20° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Kutup x≈2550 m: 1 takılma/ters olayı (Pikap), zemin eğimi 20°. Bu yamacın en dik kısmını 12° civarına indirin (`Terrain` genliği ya da `maxSlopeAt`); alternatif olarak yamacın başına yakıt bidonu koyun.
- Kutup: 8 koşu yakıtla bitiyor, 2 tanesinde önce yamaçta takılma var (takılma eğimleri 20-27°, mesafeler 2624, 3915 m). Asıl sorun yakıt değil tırmanış: yüzeyin tutunması (kar, μ=0.45) bu eğim için yetersiz; 20°'nin üstündeki yamaçları azaltın, ya da bu yüzeyin μ değerini/araç torkunu artırın. Takılınca yakıt boşa yanıyor: "yokuşu çıkamıyorsun" ipucu ve geri vites daha erken devreye girmeli.
- Ay: 8 koşu takılmadan yakıtla bitiyor (mesafeler 1798, 1803, 1936, 2569, 2754, 3244, 5357, 7957 m); 2 km'den kısa olanlar için ilk bidon aralıkları (`fuelEvery`=500 m, `Terrain.fuelGapAt`) bu araçların menziline göre geniş.
- Dağ: 8 koşu takılmadan yakıtla bitiyor (mesafeler 1932, 2347, 2790, 2815, 3339, 4185, 4248, 9724 m); 2 km'den kısa olanlar için ilk bidon aralıkları (`fuelEvery`=400 m, `Terrain.fuelGapAt`) bu araçların menziline göre geniş.
- Mağara: 8 koşu takılmadan yakıtla bitiyor (mesafeler 1475, 2426, 2427, 2428, 4052, 4480, 4529, 8777 m); 2 km'den kısa olanlar için ilk bidon aralıkları (`fuelEvery`=450 m, `Terrain.fuelGapAt`) bu araçların menziline göre geniş.
- Mars: 8 koşu yakıtla bitiyor, 2 tanesinde önce yamaçta takılma var (takılma eğimleri 27-28°, mesafeler 1381, 2174 m). Asıl sorun yakıt değil tırmanış: yüzeyin tutunması (kırmızı toz, μ=0.65) bu eğim için yetersiz; 27°'nin üstündeki yamaçları azaltın, ya da bu yüzeyin μ değerini/araç torkunu artırın. Takılınca yakıt boşa yanıyor: "yokuşu çıkamıyorsun" ipucu ve geri vites daha erken devreye girmeli.
- Mars: 6 koşu takılmadan yakıtla bitiyor (mesafeler 1753, 4556, 5307, 5332, 7797, 7809 m); 2 km'den kısa olanlar için ilk bidon aralıkları (`fuelEvery`=500 m, `Terrain.fuelGapAt`) bu araçların menziline göre geniş.
- Volkan: 8 koşu yakıtla bitiyor, 2 tanesinde önce yamaçta takılma var (takılma eğimleri 25-38°, mesafeler 1279, 2505 m). Asıl sorun yakıt değil tırmanış: yüzeyin tutunması (kaya, μ=0.95) bu eğim için yetersiz; 25°'nin üstündeki yamaçları azaltın, ya da bu yüzeyin μ değerini/araç torkunu artırın. Takılınca yakıt boşa yanıyor: "yokuşu çıkamıyorsun" ipucu ve geri vites daha erken devreye girmeli.
- Deniz tabanı: 8 koşu takılmadan yakıtla bitiyor (mesafeler 1257, 1877, 1994, 2116, 3516, 3813, 3876, 5645 m); 2 km'den kısa olanlar için ilk bidon aralıkları (`fuelEvery`=500 m, `Terrain.fuelGapAt`) bu araçların menziline göre geniş.

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
