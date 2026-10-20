# Yokuş Ustası Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** An audio-first Android game for blind players, close to Hill Climb Racing: 12 vehicles and 10 maps, built in Kotlin under `yokus/`.

**Architecture:**
- `core/` holds the pure Kotlin logic, with no Android imports, so it can be tested on the JVM: Defs, Terrain, Physics, Game and Sonify, plus the `Dsp.kt` mixer copied from bosyol.
- The Android layer (MainActivity, GameView, Store, SoundBank, AudioOut, Speaker) only connects the core to the screen, touch input, sound output and storage.
- The APK is built with `build.py` (official SDK, kotlinc, d8), the same as bosyol.

**Tech Stack:**
- Kotlin 1.9.24 (kotlinc at `/home/user/bosyol/android/tools/kotlinc`)
- Android SDK 34 at `/opt/android-sdk`
- ffmpeg/ffprobe
- Robolectric 4.13 (offline setup at `/tmp/claude-0/-home-user/afa3c9ce-e696-5524-8a18-2a59d29f0c0e/scratchpad/robo`)

**Spec:** `yokus/docs/TASARIM.md` (read it before starting any task)

## Global Constraints
- Package `com.yokus.app`; core package `com.yokus.app.core`. App name "Yokuş Ustası". minSdk 26, targetSdk 34, sensorLandscape.
- Kotlin compile flags are mandatory: `-jvm-target 1.8 -Xlambdas=class -Xsam-conversions=class -Xstring-concat=inline`. Then run d8 with `--min-api 26`. After d8, `dexdump` must show 0 `invoke-custom` (otherwise the app crashes on launch).
- No external libraries: Android SDK plus kotlin-stdlib only.
- All user-facing text is Turkish. Every control has a label (contentDescription or text). Switches are labeled `Switch` widgets. Headings use `isAccessibilityHeading`. Focus moves to the title on every screen change.
- Use only CC0 sounds or sounds already in the bosyol repo. Do not use Hill Climb Racing's name or assets.
- Do not add the keystore `yokus/android/yokus.jks` to git (gitignore it). Generate it with keytool. Password `yokus2026`, alias `yokus`.
- Commit identity: `wzehirx <wzehirx@users.noreply.github.com>`. End every commit message with:
  `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`
  `Claude-Session: https://claude.ai/code/session_01D7Q1ZrADbH6m5pGvh6c5TH`
- World coordinates: metres, x to the right (forward), y up. Angle is in radians, counter-clockwise positive (nose up is positive). Time is in seconds.

## Review Focus
1. Leaving the app or the phone locking mid-game: pause the game and the sound, and resume from the same state when the user returns (G7, onPause test).
2. Car stuck nose-down or upside down, or crawling with no fuel: the game must end with "yakıt bitti" or "kaza" and must not hang (G4 tests `stuckUpsideDownEnds`, `noFuelStopEnds`).
3. Very high speed or a fall from a big ramp: no NaN, no tunnelling through the ground (G3 test `highDropNoTunnel`, plus the 10-minute random-input test).
4. Missing sound file (for example a slot left empty by the research): the game must not crash; use a fallback or stay silent (G5 test `missingSoundsSilentOk`).
5. Screen reader on: announcements must not overlap and must not interrupt each other; the same warning must not repeat more than once every 3 s (G5 test `announceThrottle`).

---

### Task G1: Scaffold
**Files:**
- Create from bosyol, changing the package name:
  - `yokus/android/build.py`
  - `yokus/android/AndroidManifest.xml`
  - `yokus/android/res/mipmap-*/ic_launcher.png` (a new icon, generated with python PIL or ffmpeg)
  - `yokus/android/src/com/yokus/app/core/Dsp.kt`
  - `AudioOut.kt`, `CrashLog.kt`
  - Placeholder `MainActivity.kt`
- Create the symlink `yokus/android/tools` pointing to `/home/user/surus/android/tools`.
- Create `yokus/.gitignore` with: `android/build/`, `android/tools`, `*.jks`, `*.idsig`, `*.apk`, `!indir/*.apk`.

Changes to make in `build.py`:
- Read sounds from `yokus/sounds/`. If that folder is missing, build with empty assets.
- Output to `yokus/YokusUstasi-<VN>.apk`.
- Keystore `yokus.jks`; if it is missing, create it with keytool (`-dname "CN=Yokus Ustasi"`, RSA 2048, validity 10000).
- After d8, run `dexdump -d` on classes.dex, count `invoke-custom`, and fail the build if the count is non-zero.

Steps:
- [ ] Write the placeholder MainActivity: one heading saying "Yokuş Ustası".
- [ ] Run `python3 yokus/android/build.py 1 0.1`. It must produce an APK and `apksigner verify` must pass.
- [ ] Commit.

### Task G2: Defs + Terrain
**Files:**
- Create `yokus/android/src/com/yokus/app/core/Defs.kt` and `Terrain.kt`
- Test: `yokus/android/test/TerrainTest.kt`
- Create `yokus/android/test/run.sh`: compiles core + test with kotlinc and runs it with `java -cp`; the test entry point is `main` (a simple assert harness, no JUnit).

**Interfaces (Produces):**
```kotlin
enum class Surface(val mu: Double, val rollKey: String, val label: String) {
  DIRT(0.9,"roll_dirt","toprak"), SAND(0.6,"roll_sand","kum"), SNOW(0.45,"roll_snow","kar"), ICE(0.25,"roll_snow","buz"),
  MUD(0.5,"roll_mud","çamur"), MOON(0.7,"roll_dirt","ay tozu"), ROCK(0.95,"roll_rock","kaya"), ASPHALT(1.0,"roll_asphalt","asfalt") }
enum class Drive { REAR, ALL }
data class VehicleDef(val id: String, val name: String, val price: Int, val desc: String,
  val mass: Double, val inertia: Double, val wheelR: Double, val wheelMass: Double,
  val rearX: Double, val frontX: Double, val axleY: Double,          // wheel mount points in body coordinates (m)
  val suspK: Double, val suspC: Double, val suspTravel: Double,      // N/m, N·s/m, m
  val torque: Double, val idleRpm: Double, val maxRpm: Double, val ratio: Double, // engine rpm = wheel rpm × ratio
  val drive: Drive, val airTorque: Double, val fuelL: Double, val fuelUse: Double, // L, L/s at full throttle
  val headX: Double, val headY: Double, val headR: Double,           // head circle in body coordinates
  val engineKey: String, val engineRefRpm: Double, val enginePitch: Double, val electric: Boolean = false,
  val snowGrip: Double = 1.0)                                         // μ multiplier on SNOW/ICE
data class MapDef(val id: String, val name: String, val price: Int, val desc: String, val surface: Surface, val gravity: Double,
  val ambKey: String, val seed: Long, val amp: Double, val growth: Double, val rampEvery: Double, val fuelEvery: Double)
object Vehicles { val all: List<VehicleDef>; fun byId(id: String): VehicleDef }   // 12, in TASARIM order; ids: jeep moto pickup tractor buggy rally monster bus snowmobile rover tank super
object Maps { val all: List<MapDef>; fun byId(id: String): MapDef }               // 10; ids: country desert arctic moon forest mountain beach cave swamp site
data class Upgrades(val engine: Int = 0, val susp: Int = 0, val tire: Int = 0, val fuel: Int = 0)  // each 0..10
enum class UpgradeKind(val label: String) { ENGINE("Motor"), SUSP("Süspansiyon"), TIRE("Lastik"), FUEL("Yakıt deposu") }
fun upgradeCost(v: VehicleDef, kind: UpgradeKind, level: Int): Int  // base = max(400, v.price / 25); cost = round(base × 1.6^level); kind does not change the price
fun VehicleDef.upgraded(u: Upgrades): VehicleDef  // torque×(1+0.08e), fuelL×(1+0.10f), suspK×(1+0.04s), suspC×(1+0.07s); tire is applied in Physics as gripMul
fun gripMul(u: Upgrades): Double = 1 + 0.06 * u.tire

enum class FeatureKind(val say: String) { STEEP_UP("Önde dik yokuş"), STEEP_DOWN("Önde dik iniş"), RAMP("Önde rampa"), PIT("Önde çukur"), BUMPS("Önde tümsekler") }
data class Feature(val kind: FeatureKind, val x0: Double, val x1: Double)
class Coin(val x: Double, val y: Double, val value: Int) { var taken = false }
class FuelCan(val x: Double, val y: Double) { var taken = false }
class Terrain(val map: MapDef, val length: Double = 20000.0) {
  fun height(x: Double): Double      // linear interpolation between samples; x<0 or x>length is clamped
  fun slope(x: Double): Double       // dy/dx
  val surface: Surface
  val features: List<Feature>; val coins: List<Coin>; val fuels: List<FuelCan>
  fun nextFeature(x: Double, range: Double): Feature?   // first feature with x0 in (x, x+range]
}
```

Value table for vehicles: fill in with Sonnet's judgement, but these characters must hold:
- jeep: mass 300, rearX -1.1, frontX 1.1, wheelR 0.4, torque 500, rear drive.
- moto: mass 150, wheelbase 1.4, airTorque high.
- tank: torque high, maxRpm low, snowGrip 1.5.
- super: torque very high, wheelR small, suspension stiff.
- snowmobile: snowGrip 2.2.
- rover: electric = true, engineKey "synth".
- All vehicles: headY > axleY + wheelR, and headR between 0.2 and 0.3.

Value table for maps: the values in the TASARIM table. `ambKey` uses the `amb_*` names from TASARIM. Missing ones fall back to the bosyol `bed_*` names; G6 copies those.

Terrain generation, deterministic from `seed`:
- The first 30 m are flat at height 0.
- Then the sum of sines `amp × (1 + growth × x/1000) × Σ sin(...)` plus value noise (1 m samples, cubic smoothing).
- Every `rampEvery` m, add a ramp (+3 m rise over 12 m, then a sharp drop).
- Add random pits and bump sequences.
- The slope of a single sample must never exceed 1.6 (about 58°); clip it if it does.
- Coin groups every 60–120 m: 5–10 coins 1.2 m above the ground, 2 m apart. Value grows with distance: 5 up to 500 m, 25 up to 1500 m, then 100, with an occasional 500.
- Fuel cans: first at 250 m, then every `fuelEvery` m, at ground height +0.8 m.
- Features: mark every 10 m window whose average slope is > 0.45 as STEEP_UP and < −0.45 as STEEP_DOWN; mark ramps as RAMP, pits as PIT and bump sequences as BUMPS; merge neighbouring features of the same kind.

Steps:
- [ ] Write the failing tests in TerrainTest:
  - `deterministic`: two Terrains with the same map have the same heights at 1000 random x.
  - `flatStart`: height(0..30) == 0.
  - `slopeBounded`: |slope| ≤ 1.7 at every 0.25 m.
  - `fuelSpacing`: the first fuel can is at 250±5 m, and consecutive cans are fuelEvery±60 m apart.
  - `coinsAboveGround`: every coin's y − height(x) is between 1.0 and 1.5.
  - `twelveVehiclesTenMaps`: counts and ids are unique; prices are ascending and match the TASARIM table.
  - `upgradeCostGrows`: cost(level+1) > cost(level), and cost(jeep, 0) == 400.
  - `featuresFound`: country map has at least 10 features in the first 5 km.
- [ ] Run `bash yokus/android/test/run.sh TerrainTest`. It must fail.
- [ ] Implement.
- [ ] Run it again. It must print `OK`.
- [ ] Commit.

### Task G3: Physics
**Files:**
- Create `yokus/android/src/com/yokus/app/core/Physics.kt`
- Test: `yokus/android/test/PhysicsTest.kt`

**Interfaces:**
- Consumes: VehicleDef, Terrain, Surface, `gripMul`.
- Produces:
```kotlin
class Wheel { var x = 0.0; var y = 0.0; var vx = 0.0; var vy = 0.0; var spin = 0.0 /*rad/s, + = forward*/
  var grounded = false; var compression = 0.0 /*0..1*/; var slip = 0.0 /*m/s*/ }
class Car(val def: VehicleDef, val terrain: Terrain, val gravity: Double, val grip: Double = 1.0, startX: Double = 5.0) {
  var x: Double; var y: Double; var angle: Double; var vx: Double; var vy: Double; var omega: Double
  val rear: Wheel; val front: Wheel
  var rpm: Double; var gas = 0.0; var brake = 0.0      // inputs 0..1
  var landImpact = 0.0   // largest landing speed (m/s) since the last step() call; reset at the start of each step
  val airborne: Boolean  // both wheels off the ground
  val speed: Double      // body velocity along the ground, m/s
  fun headWorld(): Pair<Double, Double>
  fun headHit(): Boolean // head circle inside the ground: (headY − height(headX)) < headR
  fun step(dt: Double)   // splits dt into 1/240 s substeps; NaN guard: on NaN or Inf restore the previous state and zero the velocities
}
```

Model (TASARIM "Fizik modeli"):
- Body plus two wheel bodies.
- Suspension: a spring-damper along the body's local −y axis from the mount point, with rest compression at mid-travel. Perpendicular to the axis, a stiff spring (k×20) acts as a constraint. When the travel limit is reached, apply a hard stop (velocity projection).
- Ground contact: when a wheel circle sinks into the ground, push it out along the ground normal and zero the inward component of its velocity (no bounce). Normal force N is estimated from the momentum removed.
- Friction: `Ft = clamp(k_slip × (spin·r − v_t), −μ·grip·N, μ·grip·N)`, where μ = surface.mu × (snowGrip on SNOW/ICE). Ft acts on the wheel's linear motion and, in reverse, on its spin.
- Motor:
  - `rpm = max(idle, |spin_drive| × ratio × 60/2π)`.
  - Torque curve: `torque × (0.6 + 0.4 × sin(π × rpm/maxRpm))`, cut to 0 above maxRpm.
  - Drive: REAR means the rear wheel only; ALL splits it 50/50.
- Brake:
  - Moving forward on the ground: brake torque opposes spin.
  - Speed < 0.5 m/s: reverse at 40% torque.
- Air control: when airborne, angular acceleration `(gas − brake) × airTorque / inertia`.
- Reaction torque: in every case, apply the opposite of the drive torque to the body. This makes the nose lift on the ground too.
- Limits: |spin| ≤ maxRpm/ratio, |v| ≤ 60 m/s, |omega| ≤ 12.

Steps:
- [ ] Write the failing tests:
  - `restsOnFlat`: jeep, gas 0, 5 s on the flat → |vx| < 0.05, |angle| < 0.02, wheels grounded, y stable (variation over the last 1 s < 1 cm).
  - `climbsCountry`: jeep, gas 1, 60 s on country → x > 300, and headHit stays false while angle stays within ±1.4 the whole time. The test is a pure physics check; it does not use GameSession.
  - `airGasNoseUp`: jeep released at 10 m height, gas 1 → in 0.5 s, angle increases by more than 0.3.
  - `airBrakeNoseDown`: the same with brake 1 → angle decreases by more than 0.3.
  - `upsideDownHeadHit`: body placed with angle π, 1 m above the ground, gas 0 → headHit within 2 s.
  - `moonFloaty`: moon map, a ramp jump → airtime is more than 2× the country value. Measure it by releasing the jeep from a 5 m height with initial vx 10: time to ground at gravity 1.62 vs 9.81.
  - `highDropNoTunnel`: dropped from 40 m → after 2 s, both wheels' y ≥ height − 0.05.
  - `randomInputs10min`: random gas and brake for 600 s → no NaN in any state, and x keeps increasing (> 200).
  - `allVehiclesClimb`: every vehicle, gas 1, 30 s on country → x > 100. Moto and super may flip; it is enough that x > 60 before a flip.
- [ ] Run `bash yokus/android/test/run.sh PhysicsTest`. It must fail.
- [ ] Implement; tune the values in Defs until the tests pass.
- [ ] Run it again. It must print `OK`.
- [ ] Commit.

### Task G4: Game session
**Files:**
- Create `yokus/android/src/com/yokus/app/core/Game.kt`
- Test: `yokus/android/test/GameTest.kt`

**Interfaces:**
- Consumes: Car, Terrain, Defs.
- Produces:
```kotlin
enum class EndReason(val say: String) { CRASH("Kaza yaptın"), FUEL("Yakıt bitti"), QUIT("Bırakıldı") }
interface GameEvents {
  fun onCoin(value: Int, streak: Int) {}; fun onFuel() {}; fun onFuelLow(pct: Int) {}   // pct is 25 or 10
  fun onLand(impact: Double) {}; fun onFlip(count: Int, bonus: Int) {}; fun onAirBonus(sec: Double, bonus: Int) {}
  fun onFeature(f: Feature) {}; fun onDistance(m: Int) {}; fun onEnd(reason: EndReason) {} }
class GameSession(val vehicle: VehicleDef, val upgrades: Upgrades, val map: MapDef, var events: GameEvents? = null) {
  val terrain: Terrain; val car: Car
  var fuel: Double      // litres
  val fuelMax: Double
  var coins = 0; var distance = 0.0 /*m, max x − start*/; var airTime = 0.0
  var flips = 0; var over = false; var endReason: EndReason? = null; var time = 0.0
  fun update(dt: Double, gas: Double, brake: Double)
  fun fuelPct(): Int
  fun tiltDeg(): Int    // + means nose up
  fun statusText(): String  // "Mesafe 123 metre. Yakıt yüzde 45. Altın 230. Burun 12 derece yukarı." (yukarı/aşağı/düz)
  fun nearestAhead(): Pair<Coin?, FuelCan?>  // next untaken coin and can within 60 m
}
```

Rules:
- Fuel use: `fuelUse × (0.2 + 0.8 × gas) × dt`.
- With fuel at 0, gas is cut. If |speed| < 0.3 for 3 s, end with FUEL.
- Pickup: a wheel or the body centre within 1.3 m of a coin or can (|dx| < 1.3 and |dy| < 1.8). Coins add their value; a can sets fuel to full. A coin within 2.5 s of the previous one increases streak.
- headHit → CRASH. Also: if upside down (|angle| > 2.2) with speed < 0.5 for 2.5 s, end with CRASH.
- Flip: accumulate the angle change while airborne. Each full 2π turn counts 1 flip, bonus 250 × count, awarded on landing.
- Air bonus: on landing, if airtime ≥ 2 s, bonus `25 × floor(airTime)`.
- Announcements:
  - onFeature: once per feature when it is ≤ 35 m ahead.
  - onDistance: every 100 m.
  - onFuelLow: once each at 25 and 10. After a refill, these can fire again.

Steps:
- [ ] Write the failing tests:
  - `coinsCollected`: jeep on country, gas 1 for 40 s → coins > 0 and onCoin was called.
  - `fuelRuns`: fuelL = 0.5 (with `def.copy`), gas 1 → onFuelLow(25), then onFuelLow(10), then onEnd(FUEL).
  - `noFuelStopEnds`: with fuel 0 on the flat, onEnd(FUEL) arrives within 4 s.
  - `stuckUpsideDownEnds`: car set to angle π just above the ground with the head not touching, using a flat test MapDef → CRASH within 4 s.
  - `refuel`: car placed right before a fuel can → fuel == fuelMax.
  - `flipCounted`: in the air, `car.omega = 8` set manually, 10 m height, moon gravity → onFlip with count ≥ 1 on landing.
  - `statusTextFormat`: contains "Mesafe" and "Yakıt yüzde".
  - `featureAnnouncedOnce`: each feature is announced at most once in 60 s of gas.
- [ ] Run `bash yokus/android/test/run.sh GameTest`. It must fail.
- [ ] Implement.
- [ ] Run it again. It must print `OK`.
- [ ] Commit.

### Task G5: Sound design (core/Sonify.kt)
**Files:**
- Create `yokus/android/src/com/yokus/app/core/Sonify.kt`
- Test: `yokus/android/test/SonifyTest.kt`, plus `yokus/android/test/FBank.kt` (decodes `yokus/sounds/<key>.(mp3|wav)` with ffmpeg; copy the pattern from bosyol `android/test/Harness.kt`)

**Interfaces:**
- Consumes: Mixer, SampleVoice, OscVoice, Biquad, Pcm, `seamless` (Dsp.kt); GameSession, GameEvents.
- Produces:
```kotlin
interface Bank { fun get(key: String): Pcm? }   // key = file name without extension; null if missing
class AudioSettings { var tiltTone = true; var radar = true; var distanceCalls = false
  var volMaster = 80; var volEngine = 80; var volWorld = 70; var volCues = 80 }
class Sonify(val mix: Mixer, val bank: Bank, val set: AudioSettings, val speak: (String) -> Unit) : GameEvents {
  fun start(s: GameSession)     // sets s.events = this; starts the engine, surface, ambience and wind loops
  fun update(dt: Double)        // call every frame
  fun stop()                    // mix.stopAll(0.4f)
  fun applyVolumes()
  fun ui(key: String)           // menu sounds: "buy", "coin", "nope" (short)
}
```

Sound rules (TASARIM "Ses tasarımı"):
- Ahead to the right. `pan = clamp(dx / 25, -1, 1) × 0.85`. Distance gain `1/(1+(d/8)²)`. Height: `rate *= 2^(dy/12)`.
- Engine:
  - `bank.get(def.engineKey)`, falling back to "e0_med_on", then to an OscVoice SAW.
  - Run it through `seamless(p, 0.08)` and loop it.
  - `rate = clamp(rpm / engineRefRpm × enginePitch, 0.35, 2.4)`, gain `0.5 + 0.5 × gas`, with a 0.03 s fade.
  - electric: two OscVoices (SINE + SAW through a lowpass), freq `120 + rpm × 0.12`.
- Surface roll: `bank.get(surface.rollKey)` as a loop. Gain `min(1, |wheel speed|/12)` when grounded, else 0. Rate `0.8 + 0.4 × min(1, |v|/20)`.
- Wind: "wind" loop. Gain `min(1, |v|/25) × 0.3`, rising to 0.6 when airborne. Ambience: map.ambKey as a loop at 0.35.
- Tilt tone: OscVoice SINE, `freq = 330 × 2^(angle/(π/2))`, gain 0.05. When |angle| > 70°, add a BEEP OscVoice that pulses: every `max(0.08, 0.4 − (|angle|−70°)/90°)` s, 0.04 s long, 1200 Hz.
- Radar: every 0.4 s, a 0.035 s OscVoice TRIANGLE at `600 × 2^(clamp(slope ahead, −1.5, 1.5))`, where slope ahead = (h(x+15) − h(x+10))/5. Pan 0.5 (right).
- Coin beacon: a short 1800 Hz sine "glint" from the nearest coin ahead every 0.7 s, with gain and pan from the direction rule. Fuel beacon: a 2-note 500→750 Hz pulse every 1.0 s.
- Events:
  - onCoin: "coin" sample with `rate = 1 + 0.06 × min(streak, 12)`.
  - onFuel: "fuel_pick" plus speak("Yakıt doldu").
  - onFuelLow: "fuel_low" plus speak("Yakıt yüzde $pct").
  - onLand: "land_thud" at gain `min(1, impact/8)`; if impact > 3, also "creak".
  - onFlip: "flip" plus speak("Takla! $bonus altın").
  - onAirBonus: speak.
  - onFeature: speak(f.kind.say).
  - onDistance: speak("$m metre") if set.distanceCalls.
  - onEnd: "crash" if CRASH, plus speak(reason.say).
- Announcement throttle: `speak` is called through `Sonify.say(text)`. The same text is suppressed if it repeated within 3 s. At most 1 announcement per 0.8 s; while waiting, keep only the most recent one.
- Missing sound: any `bank.get` returning null silently skips that layer and never throws.
- Buses: engine → BUS_ENGINE; surface, wind and ambience → BUS_WORLD; tilt, radar and beacons → BUS_CABIN (volCues); events → BUS_FX.

Steps:
- [ ] Write the failing tests in SonifyTest:
  - `missingSoundsSilentOk`: a Bank returning only null → 20 s of GameSession + render → no exception.
  - `rendersFinite`: with FBank, 30 s of gas → every sample is finite, peak ≤ 1.0, and engine bus RMS > 0.01.
  - `engineFollowsRpm`: engine voice rate at higher rpm > rate at idle.
  - `announceThrottle`: say("A") 5 times within 1 s → speak is called once; say("B") then say("C") within 0.2 s → C wins.
  - `tiltBeepAbove70`: angle 80° → a beep voice is created; angle 30° → none.
  - Write a 20 s WAV to the scratchpad for manual listening.
- [ ] Run `bash yokus/android/test/run.sh SonifyTest`. It must fail.
- [ ] Implement.
- [ ] Run it again. It must print `OK`.
- [ ] Commit.

### Task G6: Sound preparation (run before G5's FBank test; it can run in parallel with G3/G4)
**Files:**
- Create `yokus/tools/prep_sounds.py`
- Output: `yokus/sounds/*` and `yokus/sounds/CREDITS.csv`

Steps:
- [ ] Write the script. For each slot:
  - Read `yokus/sounds-raw/<slot>.mp3`.
  - Loops (eng_*, roll_*, amb_*, wind): trim to a section of at most 12 s (skipping the first 0.5 s), convert to mono, `loudnorm=I=-20`, and make the loop seamless with a 0.1 s crossfade (ffmpeg `afade` plus overlap).
  - FX: trim leading silence, `loudnorm=I=-16`, at most 3 s.
  - Output 44.1 kHz mp3 at 128k.
- [ ] Copy these from bosyol `sounds/`: `e0_*`, `e1_*`, `e2_*` (wav), `crash.mp3`, and `bed_*.mp3`. For missing amb_* slots, copy the matching bed_* file under the amb_ name: country→bed_forest, desert→bed_highway, arctic→bed_mountain, beach→bed_coast, city→bed_city, swamp→em_frog, forest→bed_forest, mountain→bed_mountain, cave→bed_night, moon→synthesize.
- [ ] Synthesize any missing slot with ffmpeg:
  - coin: 1568 Hz + 2093 Hz sine, 0.15 s, decaying.
  - fuel_low: 880/660 Hz beep.
  - flip: noise sweep.
  - buy: 3-note arpeggio.
  - nope: 220 Hz, 0.15 s.
  - land_thud: lowpassed noise at 80 Hz, 0.2 s.
  - wind: pink noise with a lowpass.
  - roll_*: brown noise through a bandpass.
- [ ] Run `python3 yokus/tools/prep_sounds.py`. Every slot must exist in `yokus/sounds/` and the total size must be < 25 MB.
- [ ] Merge CREDITS.csv with the bosyol credits.
- [ ] Commit `yokus/sounds` and `yokus/sounds-raw/CREDITS.csv`. Do not commit the raw mp3s: add `sounds-raw/*.mp3` to `.gitignore`.

### Task G7: Android interface
**Files:**
- Create in `yokus/android/src/com/yokus/app/`:
  - `MainActivity.kt` (screens)
  - `GameView.kt` (Canvas: ground line, car rectangle and wheels, coins, cans; the camera follows the car)
  - `Store.kt`
  - `SoundBank.kt` (implements core Bank; adapt the bosyol decode code; reads assets `sounds/<key>.mp3|.wav`)
  - `Speaker.kt`: `announceForAccessibility` when TalkBack or touch exploration is on, otherwise TTS in tr-TR
  - `AudioOut.kt` (from G1)

**Interfaces:**
```kotlin
class Store(ctx: Context) {
  var gold: Int; fun owns(vehicleId: String): Boolean; fun ownsMap(id: String): Boolean
  fun buyVehicle(v: VehicleDef): Boolean; fun buyMap(m: MapDef): Boolean   // false if not enough gold
  var vehicleId: String; var mapId: String
  fun upgrades(vehicleId: String): Upgrades; fun buyUpgrade(v: VehicleDef, k: UpgradeKind): Boolean  // false at max level 10
  fun best(mapId: String): Int; fun setBest(mapId: String, m: Int)
  val audio: AudioSettings   // load/save
  var volumeKeys: Boolean; var latch: Boolean
}
```

Screens, following the TASARIM "Arayüz" section and the bosyol MainActivity pattern:
- **Main menu:** a gold line (TextView) and the buttons Başla, Araçlar, Haritalar, Garaj, Ayarlar, Nasıl oynanır, Çıkış.
- **Araçlar / Haritalar:** one button per entry, labelled "Cip, sahipsin, seçili" or "Motosiklet, 8000 altın, açıklama". Tapping an owned one selects it; tapping an unowned one with enough gold buys and selects it, otherwise says "Yeterli altın yok, X altın gerek".
- **Garaj:** 4 buttons, "Motor seviye 3 / 10, sonraki 1024 altın".
- **Ayarlar:** Switches (Eğim tonu, Yol radarı, Mesafe duyurusu, Ses tuşları gaz/fren, Pedal kilidi), SeekBars (Ana ses, Motor, Ortam, İşaret sesleri, each with a stateDescription "yüzde X"), and Geri.
- **Oyun:**
  - Full-screen GameView. A large Fren area on the left third and Gaz on the right third, both with contentDescription. In the middle, Durum and Duraklat.
  - Pedals use `setOnTouchListener` press/hold, with latch if enabled. They have `importantForAccessibility=yes`; in touch-exploration mode a double-tap-and-hold works via an `ACTION_DOWN` simulation, and with latch on a double tap toggles the pedal.
  - Volume keys: up = gas, down = brake. The back key opens Duraklat.
- **Duraklat:** Devam, Yeniden başla, Ana menü.
- **Oyun sonu:** "Kaza yaptın. 534 metre, 230 altın kazandın. En iyi: 600 metre." with Tekrar, Garaj, Ana menü. The coins are added to the store.
- Game loop: a `Choreographer` or a 60 Hz `postDelayed` Runnable calls `session.update(dt)` and `sonify.update(dt)`, with dt clamped to ≤ 0.05.
- onPause: pause the game and stop AudioOut. onResume: return to the Duraklat screen.
- CrashLog: on launch, if a crash log exists, show a "Son çökme kaydı" dialog with a Kopyala button.
- Status update: every second, update the contentDescription of a TextView in the middle with the status text. Do not announce it automatically; it is read when Durum is pressed.

Steps:
- [ ] Implement.
- [ ] Run `python3 yokus/android/build.py 1 1.0`. The build must pass and dexdump must show 0 invoke-custom.
- [ ] Commit.

### Task G8: Robolectric UI walkthrough + lint
**Files:**
- Test: `yokus/android/test/robo/UiWalkTest.kt`
- Runner: `yokus/android/test/robo/run.sh` (uses the classpath under `/tmp/claude-0/-home-user/afa3c9ce-e696-5524-8a18-2a59d29f0c0e/scratchpad/robo`; see bosyol's previous robo runner `robo/src`; if the folder is missing, rebuild it from Maven Central)

Steps:
- [ ] Write the walkthrough test:
  - App launches → a "Yokuş Ustası" heading.
  - Araçlar: tapping Motosiklet with 0 gold → still not owned. With store.gold = 10000, tapping again → owned and selected.
  - Haritalar → Çöl purchase.
  - Garaj → Motor upgrade.
  - Ayarlar → toggle the Eğim tonu Switch; reopen and it is persisted.
  - Başla → GameView exists. Press gas for 2 s (simulated time) → session.distance > 0. Durum shows a status text. Back → Duraklat → Ana menü.
  - Every clickable view has text or a contentDescription; collect them with a View tree walk and assert.
- [ ] Run the test. It must print `OK`.
- [ ] Run lint (`/opt/android-sdk/cmdline-tools/latest/bin/lint` if available; otherwise skip and write a note) with `--check NewApi`. There must be 0 errors.
- [ ] Commit.

### Task G9: Delivery
- [ ] Write `yokus/README.md` (Turkish): how to play, controls, the list of vehicles and maps, sound credits.
- [ ] Run `python3 yokus/android/build.py 1 1.0`, then copy the result to `yokus/indir/YokusUstasi.apk`.
- [ ] Add a "Yokuş Ustası" section and download link to the main `README.md`.
- [ ] Commit and push: `git push -u origin main`.
- [ ] Mark ILERLEME.md as done.
