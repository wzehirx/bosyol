#!/usr/bin/env python3
"""Yokus Ustasi G6: sounds-raw/*.mp3 -> sounds/ (44.1 kHz mono mp3 128k).

Usage: python3 yokus/tools/prep_sounds.py
Needs ffmpeg, ffprobe, numpy.
"""
import csv, json, math, os, re, shutil, subprocess, sys
import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
YOKUS = os.path.dirname(HERE)
ROOT = os.path.dirname(YOKUS)
RAW = os.path.join(YOKUS, "sounds-raw")
OUT = os.path.join(YOKUS, "sounds")
BOS = os.path.join(ROOT, "sounds")
SR = 44100
XFADE = 0.15          # loop crossfade, s
HOP = 0.05            # RMS hop, s
UNSTEADY_DB = 6.0     # engine RMS spread (p5..p95) above this -> fallback
UNSTEADY_OCT = 0.35   # engine spectral-centroid spread (octaves) above this -> fallback

LOOPS = ["eng_jeep", "eng_moto", "eng_tractor", "eng_buggy", "eng_monster", "eng_bus",
         "eng_snowmobile", "eng_rally", "roll_dirt", "roll_sand", "roll_snow", "roll_mud",
         "roll_rock", "roll_asphalt", "wind"] + \
        ["amb_" + n for n in ["country", "desert", "arctic", "moon", "forest", "beach",
                              "cave", "swamp", "mountain", "city"]]
FX_CAP = {"coin": 0.6, "fuel_pick": 1.2, "land_thud": 0.6, "creak": 1.0, "crash": 2.5,
          "flip": 1.0, "buy": 1.0, "fuel_low": 1.2}
FX_FADE = {"crash": 0.3}
FALLBACK = {"eng_buggy": "e2_med_on.wav", "eng_monster": "e1_med_on.wav"}
DEFAULT_FALLBACK = "e0_med_on.wav"
SYNTH_LOOPS = {"roll_sand"}

report = []   # (slot, how, note)
table = {}    # slot -> (dur, lufs, peak)


def run(cmd, inp=None):
    p = subprocess.run(cmd, input=inp, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if p.returncode != 0:
        raise RuntimeError(" ".join(cmd) + "\n" + p.stderr.decode(errors="replace"))
    return p


def decode(path, af=None):
    cmd = ["ffmpeg", "-v", "error", "-i", path]
    if af:
        cmd += ["-af", af]
    cmd += ["-ac", "1", "-ar", str(SR), "-f", "f32le", "-"]
    return np.frombuffer(run(cmd).stdout, dtype="<f4").astype(np.float64)


def pcm_filter(x, af):
    cmd = ["ffmpeg", "-v", "error", "-f", "f32le", "-ar", str(SR), "-ac", "1", "-i", "-",
           "-af", af, "-f", "f32le", "-"]
    return np.frombuffer(run(cmd, x.astype("<f4").tobytes()).stdout, dtype="<f4").astype(np.float64)


def loudnorm_encode(x, target, dst, tp=-1.5, comp=False):
    """loudnorm measurement pass, then a linear gain to the target LUFS + peak limiter; mp3 128k."""
    src = ["ffmpeg", "-v", "info", "-f", "f32le", "-ar", str(SR), "-ac", "1", "-i", "-"]
    data = x.astype("<f4").tobytes()
    base = f"loudnorm=I={target}:TP={tp}:LRA=11"
    pre = "acompressor=threshold=0.05:ratio=4:attack=3:release=60:makeup=1," if comp else ""
    p = run(src + ["-af", pre + base + ":print_format=json", "-f", "null", "-"], data)
    m = re.search(r"\{[^{}]*\"input_i\"[^{}]*\}", p.stderr.decode(), re.S)
    af = pre + "anull"
    if m:
        j = json.loads(m.group(0))
        try:
            if float(j["input_i"]) > -69:      # linear loudness normalization (keeps loop seams intact)
                af = pre + f"volume={target - float(j['input_i']):.3f}dB"
        except (KeyError, ValueError):
            pass
    af += f",aresample={SR},alimiter=limit=0.89:level=false"
    run(["ffmpeg", "-v", "error", "-y", "-f", "f32le", "-ar", str(SR), "-ac", "1", "-i", "-",
         "-af", af, "-ar", str(SR), "-ac", "1", "-c:a", "libmp3lame", "-b:a", "128k", dst], data)


def frame_stats(x):
    """per-HOP RMS in dB and spectral centroid in octaves-of-Hz (log2)."""
    h = int(HOP * SR)
    n = len(x) // h
    f = x[:n * h].reshape(n, h)
    rms = np.sqrt((f ** 2).mean(1) + 1e-12)
    db = 20 * np.log10(rms)
    win = np.hanning(h)
    spec = np.abs(np.fft.rfft(f * win, axis=1))
    freqs = np.fft.rfftfreq(h, 1 / SR)
    cen = (spec * freqs).sum(1) / (spec.sum(1) + 1e-9)
    return db, np.log2(np.maximum(cen, 50.0))


def seam_of(y):
    n = int(0.05 * SR)
    a = math.sqrt((y[:n] ** 2).mean() + 1e-12)
    b = math.sqrt((y[-n:] ** 2).mean() + 1e-12)
    return abs(20 * math.log10(a / b))


def make_seamless(x, start, length):
    c = int(XFADE * SR)
    seg = x[start:start + length + c]
    if len(seg) < length + c:                 # not enough tail audio: wrap from the head
        seg = np.concatenate([seg, x[:length + c - len(seg)]])
    out = seg[:length].copy()
    t = np.linspace(0, math.pi / 2, c)
    out[:c] = seg[:c] * np.sin(t) + seg[length:length + c] * np.cos(t)
    return out


def best_window(x, is_engine):
    """Pick the steadiest 4-10 s window (lowest dB-RMS variance, plus constant pitch for
    engines) whose crossfaded loop seam is also clean.
    Returns (start_sample, length_samples, spread_db, centroid_spread_oct, seam_db)."""
    h = int(HOP * SR)
    db, lc = frame_stats(x)
    nfr = len(db)
    xf = int(XFADE / HOP) + 2
    start_fr = int(0.5 / HOP)                 # skip first 0.5 s
    med = np.median(db)
    cs = np.concatenate([[0], np.cumsum(db)])
    cs2 = np.concatenate([[0], np.cumsum(db ** 2)])
    cc = np.concatenate([[0], np.cumsum(lc)])
    cc2 = np.concatenate([[0], np.cumsum(lc ** 2)])
    cands = []
    for L in (10.0, 8.0, 6.0, 4.0):
        lf = int(L / HOP)
        last = nfr - lf - xf
        if start_fr > last:
            continue
        sc_list = []
        for s in range(start_fr, last + 1):
            m = (cs[s + lf] - cs[s]) / lf
            sd = math.sqrt(max(0.0, (cs2[s + lf] - cs2[s]) / lf - m * m))
            mc = (cc[s + lf] - cc[s]) / lf
            sc = math.sqrt(max(0.0, (cc2[s + lf] - cc2[s]) / lf - mc * mc))
            score = sd + (6.0 * sc if is_engine else 0.0) - 0.04 * L
            if m < med - 12:
                score += 50
            sc_list.append((score, s, lf, sd, sc))
        sc_list.sort()
        picked = []
        for c in sc_list:                      # best few, at least 0.5 s apart
            if all(abs(c[1] - p[1]) > 10 for p in picked):
                picked.append(c)
            if len(picked) >= 40:
                break
        cands += picked
    if not cands:                              # file too short: use everything
        lf = max(1, nfr - xf)
        cands = [(0, 0, lf, 0, 0)]
    best = None
    for score, s, lf, sd, sc in cands:
        y = make_seamless(x, s * h, lf * h)
        seam = seam_of(y)
        total = score + 4.0 * max(0.0, seam - 1.0)
        if best is None or total < best[0]:
            best = (total, s, lf, seam)
    _, s, lf, seam = best
    w = db[s:s + lf]
    spread = float(np.percentile(w, 95) - np.percentile(w, 5))
    return s * h, lf * h, spread, float(np.std(lc[s:s + lf])), seam


def ffprobe(path):
    p = run(["ffprobe", "-v", "error", "-show_entries", "stream=codec_name,sample_rate,channels:format=duration",
             "-of", "json", path])
    j = json.loads(p.stdout)
    return j["streams"][0], float(j["format"]["duration"])


def measure(path):
    p = run(["ffmpeg", "-nostats", "-i", path, "-af", "ebur128=peak=true", "-f", "null", "-"])
    t = p.stderr.decode()
    tail = t[t.rfind("Summary:"):]
    lufs = re.search(r"I:\s+(-?[\d.inf]+) LUFS", tail)
    pk = re.search(r"Peak:\s+(-?[\d.inf]+) dBFS", tail)
    return (float(lufs.group(1)) if lufs else float("nan"),
            float(pk.group(1)) if pk else float("nan"))


def seam_jump(path):
    x = decode(path)
    n = int(0.05 * SR)
    a = math.sqrt((x[:n] ** 2).mean() + 1e-12)
    b = math.sqrt((x[-n:] ** 2).mean() + 1e-12)
    return abs(20 * math.log10(a / b))


# ---------------------------------------------------------------- synth ----
def synth_sand(seconds=9.0):
    rng = np.random.default_rng(7)
    n = int(seconds * SR)
    noise = rng.standard_normal(n)
    bed = pcm_filter(noise, "highpass=f=500,lowpass=f=4500,volume=0.5")
    # slow, gentle grain-density modulation so it is not a flat hiss
    t = np.arange(n) / SR
    bed *= 0.85 + 0.15 * np.sin(2 * np.pi * 1.7 * t) * np.sin(2 * np.pi * 0.6 * t + 1)
    # fine crackle: poisson grains (~220/s), each a tiny decaying burst
    k = rng.poisson(220 * seconds)
    pos = rng.integers(0, n - 400, k)
    amp = rng.exponential(1.0, k)
    clicks = np.zeros(n)
    for p, a in zip(pos, amp):
        ln = int(rng.integers(30, 160))
        g = rng.standard_normal(ln) * np.exp(-np.arange(ln) / (ln / 4))
        clicks[p:p + ln] += a * g
    clicks = pcm_filter(clicks, "highpass=f=2500,lowpass=f=9000")
    clicks /= np.max(np.abs(clicks)) + 1e-9
    bed /= np.max(np.abs(bed)) + 1e-9
    return 0.6 * bed + 0.35 * clicks


def synth_nope():
    n = int(0.15 * SR)
    t = np.arange(n) / SR
    env = np.minimum(1, t / 0.02) * np.minimum(1, (0.15 - t) / 0.05)
    x = (np.sin(2 * np.pi * 220 * t) + 0.25 * np.sin(2 * np.pi * 440 * t)) * env
    return 0.25 * x / np.max(np.abs(x))


def encode_raw(x, dst):
    """Encode PCM straight to mp3 (no loudnorm) - used for nope."""
    run(["ffmpeg", "-v", "error", "-y", "-f", "f32le", "-ar", str(SR), "-ac", "1", "-i", "-",
         "-c:a", "libmp3lame", "-b:a", "128k", dst], x.astype("<f4").tobytes())


# ----------------------------------------------------------------- main ----
def process_loop(slot):
    dst = os.path.join(OUT, slot + ".mp3")
    is_engine = slot.startswith("eng_")
    if slot in SYNTH_LOOPS:
        x = synth_sand()
        report.append((slot, "synth", "granular sand roll: filtered noise + fine crackle (raw was footsteps)"))
        start, length = 0, len(x) - int(XFADE * SR) - 1
        x_in = x
    else:
        x_in = decode(os.path.join(RAW, slot + ".mp3"))
        start, length, spread, cen, seam = best_window(x_in, is_engine)
        note = f"window {start/SR:.1f}s+{length/SR:.1f}s, RMS spread {spread:.1f} dB, centroid sd {cen:.2f} oct"
        if is_engine and (spread > UNSTEADY_DB or cen > UNSTEADY_OCT):
            fb = FALLBACK.get(slot, DEFAULT_FALLBACK)
            x_in = decode(os.path.join(BOS, fb))
            start, length, spread2, cen2, seam2 = best_window(x_in, True)
            report.append((slot, "fallback:" + fb,
                           f"raw not steady ({note}); used bosyol {fb}, window {start/SR:.1f}s+{length/SR:.1f}s, "
                           f"RMS spread {spread2:.1f} dB"))
        else:
            report.append((slot, "raw", note))
    y = make_seamless(x_in, start, length)
    loudnorm_encode(y, -20, dst)


def trim_lead_silence(x, thresh_db=-40.0):
    """Cut everything before the first 5 ms frame above max(peak-45 dB, -40 dBFS)."""
    h = int(0.005 * SR)
    n = len(x) // h
    rms = np.sqrt((x[:n * h].reshape(n, h) ** 2).mean(1) + 1e-12)
    th = max(rms.max() * 10 ** (-45 / 20), 10 ** (thresh_db / 20))
    idx = np.nonzero(rms > th)[0]
    s = max(0, (int(idx[0]) if len(idx) else 0) * h - int(0.003 * SR))
    return x[s:]


def process_fx(slot):
    x = decode(os.path.join(RAW, slot + ".mp3"))
    x = trim_lead_silence(x)
    cap = FX_CAP[slot]
    cut = len(x) > cap * SR
    x = x[:int(cap * SR)]
    f = min(int(FX_FADE.get(slot, 0.05 if cut else 0.01) * SR), len(x))
    x[len(x) - f:] *= np.linspace(1, 0, f)
    dst = os.path.join(OUT, slot + ".mp3")
    loudnorm_encode(x, -16, dst)
    note = f"trimmed to {len(x)/SR:.2f}s (cap {cap}s)"
    if measure(dst)[0] < -19:                  # peaky material: light compression, then normalize
        loudnorm_encode(x, -16, dst, comp=True)
        note += ", light compression (peaky source)"
    report.append((slot, "raw", note))


def main():
    os.makedirs(OUT, exist_ok=True)
    for f in os.listdir(OUT):
        os.remove(os.path.join(OUT, f))
    for slot in LOOPS:
        process_loop(slot)
    for slot in FX_CAP:
        process_fx(slot)
    # bosyol-derived copies
    for f in sorted(os.listdir(BOS)):
        if re.match(r"e[012]_.*\.wav$", f):
            shutil.copy2(os.path.join(BOS, f), os.path.join(OUT, f))
    shutil.copy2(os.path.join(BOS, "crash.mp3"), os.path.join(OUT, "crash_car.mp3"))
    encode_raw(synth_nope(), os.path.join(OUT, "nope.mp3"))
    report.append(("nope", "synth", "220 Hz soft tone, 0.15 s"))

    # credits
    rows = list(csv.reader(open(os.path.join(RAW, "CREDITS.csv"), encoding="utf-8")))
    hdr = rows[0]
    rows = [r for r in rows[1:] if r]
    for r in rows:
        if r[0] == "roll_sand":
            r[2] += " (UNUSED: replaced by synthesized granular sand roll)"
    rows.append(["e0_*/e1_*/e2_*.wav", "", "bosyol engine layers", "Bruno Simon (folio-2019)",
                 "https://github.com/brunosimon/folio-2019", "MIT"])
    rows.append(["crash_car", "", "bosyol crash.mp3", "bosyol deposu", "bosyol deposu", "bosyol deposu"])
    rows.append(["nope", "", "synthesized 220 Hz tone", "yokus prep_sounds.py", "", "CC0 (generated)"])
    rows.append(["roll_sand", "", "synthesized granular sand roll", "yokus prep_sounds.py", "", "CC0 (generated)"])
    with open(os.path.join(OUT, "CREDITS.csv"), "w", newline="", encoding="utf-8") as fh:
        w = csv.writer(fh)
        w.writerow(hdr)
        w.writerows(rows)

    # verification
    print(f"{'slot':<16}{'dur s':>8}{'LUFS':>8}{'peak dB':>9}{'seam dB':>9}  info")
    bad = []
    total = 0
    for f in sorted(os.listdir(OUT)):
        p = os.path.join(OUT, f)
        total += os.path.getsize(p)
        if f == "CREDITS.csv":
            continue
        st, dur = ffprobe(p)
        slot = os.path.splitext(f)[0]
        if f.endswith(".mp3") and (int(st["sample_rate"]) != SR or st["channels"] != 1 or st["codec_name"] != "mp3"):
            if slot != "crash_car":
                bad.append(f"{f}: {st}")
        lufs, pk = measure(p)
        seam = ""
        if slot in LOOPS:
            j = seam_jump(p)
            seam = f"{j:.2f}"
            if j >= 3.0:
                bad.append(f"{f}: seam jump {j:.2f} dB")
        print(f"{slot:<16}{dur:>8.2f}{lufs:>8.1f}{pk:>9.1f}{seam:>9}  {st['codec_name']} {st['sample_rate']}Hz ch{st['channels']}")
    print(f"\nTOTAL {total/1e6:.2f} MB")
    for r in report:
        if r[1] != "raw":
            print("NOTE", r)
    if total >= 25e6:
        bad.append("total >= 25 MB")
    if bad:
        print("PROBLEMS:", *bad, sep="\n  ")
        sys.exit(1)
    # slot completeness
    for s in LOOPS + list(FX_CAP) + ["nope", "crash_car"]:
        assert os.path.exists(os.path.join(OUT, s + ".mp3")), s


if __name__ == "__main__":
    main()
