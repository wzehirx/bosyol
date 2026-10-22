# Yokuş Ustası 1.1: Oyun Testi ve Düzeltme Planı

> **For agentic workers:** Use superpowers:subagent-driven-development. Global Constraints in PLAN.md still apply.

**Goal:** Fix the problems the user reported from the first real play. Build a "plays like a blind human" end-to-end test environment and balance the game with it.

**User feedback (21 Oct 2026):**
- On the first map the user reached a hill. The nose rose to 124 degrees and the climb failed. Fuel ran out and the game ended with "yakıt bitti" instead of a crash.
- Coins always sound the same, as if every pickup gave the same amount. In Hill Climb Racing coin values vary.
- The user wants a nicer "trink"-style coin sound, and no pitch rise.
- The user asked for the game to be tested from start to finish in a test environment.

**Root causes found:**
- `Game.kt:102`: the upside-down rule is `|angle| > 2.2 rad` (126°). A car lying on its back at 124° with its hull corners touching the ground slipped under it. Its head never touched the ground either.
- `Game.kt:161`: `tiltDeg()` is not wrapped, so it can exceed 360 after a flip.
- There is no anti-flip aid for blind players.
- The early hills on Kır yolu may be too steep, or too long, for the jeep's fuel at level 0.

## Tasks

### H1 (Haiku): coin sound research
- Download 8–10 CC0 candidates from Freesound into `sounds-raw/coin_cand/`, using the same search method as before.
- Search terms: "coin pickup", "coin chime", "coins jingle", "cash coin", "gold coin", "kaching", "treasure".
- Look for a short, bright, pleasant "trink" or "ching". Also find a bigger "coins pouring" or jackpot sound for the 500-value coin.
- Write `RAPOR.md` (duration, title, URL) and `CREDITS.csv`.

### P1 (Sonnet): end-to-end playtest environment (test code only, no core changes)
- Create `android/test/lib/BlindDriver.kt`. It decides using only what the sounds and announcements give the player:
  - tilt angle (tilt tone and beep)
  - radar slope 10–15 m ahead
  - feature announcements
  - whether the car is in the air (wind)
  - fuel warnings
  - speed (engine pitch)
- Add a reaction delay of 0.25 s and a little noise.
- Strategy, in the HCR style:
  - Full gas.
  - When the tilt is more than 35° steeper than the ground slope, ease off.
  - In the air, keep the nose level using the tilt tone.
  - When stuck, reverse briefly and try again.
- Create `android/test/PlayTest.kt`, a full-game simulation that uses GameSession and Sonify together (Bank = FBank).
  - For each map, run the jeep, moto, pickup and tractor, plus that map's best vehicle.
  - Run with no upgrades and with level-3 upgrades.
  - Play until the game is over, capped at 20 minutes of game time.
- Report for each run:
  - distance and end reason
  - fuel at every can pickup
  - point, slope and tilt where the car got stuck or flipped
  - peak tilt
  - coins and air/flip bonuses
  - number of announcements, and the longest silent stretch
- Economy: how many runs are needed to buy the next vehicle or map.
- Bug checks:
  - The game always ends; it never hangs.
  - A car lying on its back always ends with CRASH.
  - `tiltDeg` stays within ±180.
  - No NaN.
  - `voiceCount` stays bounded.
- Write the output to `docs/OYUN_TESTI.md` in Turkish, with a table and a list of problems.
- `bash android/test/run.sh PlayTest` must print the summary and OK. The OK gates come after the fixes; see task P3.

### F1 (Sonnet): gameplay fixes (core + UI)
1. **Upside-down crash.**
   - Add `wrapAngle(a)` to [-π, π].
   - Wrap `tiltDeg()`.
   - Crash rule: if the relative angle (car angle minus ground angle under the car) exceeds 100° and the car is on the ground for 1.0 s, it is a CRASH. A head hit is still an instant CRASH.
   - Also check the head geometry: if the hull rests on its roof, the head must touch the ground.
   - `EndReason.CRASH` says "Ters döndün" when the car flipped, and "Kafa üstü düştün" for a head hit. Add a `FLIP` reason or a message field.
2. **Balance assist** (`AudioSettings.balanceAssist`, default on, labeled Switch "Denge yardımı" in Settings).
   - On the ground, when the relative angle passes 40°, scale gas down smoothly so that at 60° gas is 0.3.
   - Announce "Dikkat, devrilmek üzeresin" once, throttled.
3. **Stuck hint.** If the player is giving gas, fuel is not empty, and the car moves less than 1 m in 6 s, speak "Yokuşu çıkamıyorsun, biraz geri gidip hızlan" (throttled to once per 20 s).
4. **Coin values like HCR.**
   - In each group, mix the values randomly by distance: early on mostly 5 with some 25; from 500 m, 25 and 100; from 1500 m, 100 with a rare 500.
   - Each value has its own sound, with no pitch rise:
     - 5: `coin_s`
     - 25: `coin_m`
     - 100: `coin_l`
     - 500: `coin_xl`
   - The streak pitch rule is removed.
   - The coin beacon uses the same glint for every value. A 500 gets a slightly brighter double glint.
5. **Fuel and hill balance on the first maps.** Use the P1 report.
   - With no upgrades, the jeep with BlindDriver must reach at least the 3rd fuel can on Kır yolu in 4 out of 5 seeds.
   - If it cannot, soften `Terrain` (amplitude and steepness of the early hills) or adjust the jeep's `fuelUse`.
   - On the first map no single hill may need more than about 60% of a full tank.

### S1 (Sonnet): sound preparation for coins
- After H1, make the 4 coin sounds `coin_s/m/l/xl` from the candidates with `prep_sounds.py`: short, bright and normalized. `xl` is a bigger chord or jingle.
- If a candidate is not good enough, synthesize instead:
  - Metallic "trink": a fast attack, a few inharmonic partials (for example 2.7k, 4.1k, 5.3k), and a short decay of 0.25–0.4 s.
  - `coin_m`: two hits.
  - `coin_l`: three hits plus jingle noise.
  - `coin_xl`: a jingle shower.
- The overall level must be the same. The value must be recognizable from the sound alone.

### P3: re-run, review, delivery
- Re-run PlayTest after F1 and S1. The gates must pass:
  - Kır yolu third-can rule
  - every flip ends as CRASH
  - no hangs
- Robolectric: play a whole game in the UI with BlindDriver through MainActivity tick until the end screen, then buy an upgrade in Garaj and start again.
- A Sonnet reviewer checks the whole diff. Anything left open goes to Opus.
- Version 1.1: build the APK, copy it to `indir/YokusUstasi.apk`, and push.
