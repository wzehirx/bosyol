#!/usr/bin/env python3
"""Yokus Ustasi S1: synthesized coin sounds (coin_s/s2/s3, coin_m, coin_l, coin_xl).

Usage: python3 tools/make_coins.py [out_dir]   (prep_sounds.py calls render_all())
Needs ffmpeg and numpy. Deterministic (fixed seeds). Output: 44.1 kHz mono mp3 128k, about -16 LUFS,
sample peak <= -1 dBFS. No pitch rise between pickups: all pickups of a value share the same pitch
family; variety comes only from the small partial-set variants (coin_s, coin_s2, coin_s3).
"""
import json, os, re, subprocess, sys
import numpy as np

SR = 44100
TARGET_LUFS = -16.0
PEAK_DB = -1.0
HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(os.path.dirname(HERE), "sounds")

# (frequencies Hz, relative amplitudes) of the struck coin; higher partials decay faster
SETS = {
    "a": ([2600, 3900, 5200, 6800, 8100], [1.0, 0.8, 0.6, 0.4, 0.25]),
    "b": ([2750, 4050, 5350, 6950, 8300], [1.0, 0.7, 0.65, 0.35, 0.3]),
    "c": ([2480, 3750, 5050, 6600, 7900], [0.9, 1.0, 0.5, 0.45, 0.2]),
}


def _run(cmd, inp=None):
    p = subprocess.run(cmd, input=inp, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if p.returncode != 0:
        raise RuntimeError(" ".join(cmd) + "\n" + p.stderr.decode(errors="replace"))
    return p


def hit(buf, t0, freqs, amps, rng, gain=1.0, tau0=0.07, bright=1.0, fscale=1.0, dur=0.3):
    """Add one struck-coin hit into buf at time t0 (s)."""
    i0 = int(t0 * SR)
    n = min(int(dur * SR), len(buf) - i0)
    if n <= 0:
        return
    t = np.arange(n) / SR
    att = np.minimum(1.0, t / rng.uniform(0.001, 0.002))
    y = np.zeros(n)
    for k, (f, a) in enumerate(zip(freqs, amps)):
        tau = tau0 / (1.0 + 0.55 * k)                    # higher partial -> faster decay
        w = a * (bright ** k)
        f = f * fscale * rng.uniform(0.995, 1.005)
        ph = rng.uniform(0, 2 * np.pi)
        y += w * np.exp(-t / tau) * np.sin(2 * np.pi * f * t + ph)
        beat = rng.uniform(3.0, 9.0)                      # detuned twin -> shimmer
        y += 0.45 * w * np.exp(-t / tau) * np.sin(2 * np.pi * (f + beat) * t + ph + 1.0)
    # tiny noise click at onset (1.5 ms)
    nc = int(0.0015 * SR)
    click = rng.standard_normal(nc) * np.linspace(1, 0, nc) ** 2
    y[:nc] += 0.5 * click
    y *= att
    buf[i0:i0 + n] += gain * y


def ring(buf, t0, dur, gain, rng):
    """Bright major-triad ring (C7 E7 G7 C8, harmonic) with slow beating."""
    n = min(int(dur * SR), len(buf) - int(t0 * SR))
    t = np.arange(n) / SR
    env = np.minimum(1.0, t / 0.012) * np.exp(-t / 0.42)
    y = np.zeros(n)
    for f, a in [(2093.0, 1.0), (2637.0, 0.8), (3136.0, 0.8), (4186.0, 0.5), (5274.0, 0.25)]:
        ph = rng.uniform(0, 2 * np.pi)
        y += a * np.sin(2 * np.pi * f * t + ph)
        y += 0.5 * a * np.sin(2 * np.pi * (f + rng.uniform(2.0, 5.0)) * t + ph)
    i0 = int(t0 * SR)
    buf[i0:i0 + n] += gain * env * y


def fade_tail(x, ms=15):
    n = int(ms / 1000 * SR)
    x[-n:] *= np.linspace(1, 0, n)
    return x


def synth(name):
    seed = {"coin_s": 11, "coin_s2": 12, "coin_s3": 13, "coin_m": 21, "coin_l": 31, "coin_xl": 41}[name]
    rng = np.random.default_rng(seed)
    if name.startswith("coin_s"):
        key = {"coin_s": "a", "coin_s2": "b", "coin_s3": "c"}[name]
        f, a = SETS[key]
        x = np.zeros(int(0.32 * SR))
        hit(x, 0.0, f, a, rng, tau0=0.075, dur=0.32)
    elif name == "coin_m":
        f, a = SETS["a"]
        x = np.zeros(int(0.42 * SR))
        hit(x, 0.0, f, a, rng, gain=0.9, tau0=0.06, dur=0.22)
        hit(x, 0.060, f, a, rng, gain=1.0, tau0=0.07, bright=1.18, fscale=1.0, dur=0.36)
    elif name == "coin_l":
        f, a = SETS["a"]
        x = np.zeros(int(0.62 * SR))
        for k, t0 in enumerate([0.0, 0.055, 0.115]):
            hit(x, t0, f, a, rng, gain=0.85 + 0.1 * k, tau0=0.06, bright=1.0 + 0.12 * k, dur=0.25)
        fb, ab = SETS["b"]
        for _ in range(9):                                # soft jingle tail of tiny coins
            t0 = rng.uniform(0.17, 0.48)
            hit(x, t0, fb if rng.random() < 0.5 else SETS["c"][0], ab, rng,
                gain=0.30 * np.exp(-(t0 - 0.17) / 0.2), tau0=0.03, fscale=rng.uniform(0.9, 1.3), dur=0.14)
    elif name == "coin_xl":
        x = np.zeros(int(1.22 * SR))
        times = np.sort(rng.uniform(0.0, 1.0, 20))
        times[0] = 0.0
        sets = list(SETS.values())
        for t0 in times:
            f, a = sets[rng.integers(3)]
            hit(x, t0, f, a, rng, gain=rng.uniform(0.35, 0.6) * (1.0 - 0.35 * t0), tau0=0.035,
                fscale=rng.uniform(0.85, 1.3), dur=0.16)
        f, a = SETS["a"]
        hit(x, 0.0, f, a, rng, gain=1.0, tau0=0.08, dur=0.3)
        ring(x, 0.02, 1.15, 0.22, rng)
    else:
        raise KeyError(name)
    return fade_tail(x)


def _measure_pcm(x):
    """Integrated LUFS (silence-padded to 1.2 s so the 400 ms gate has blocks) of float samples."""
    pad = np.concatenate([x, np.zeros(max(0, int(1.2 * SR) - len(x)))])
    p = _run(["ffmpeg", "-nostats", "-f", "f32le", "-ar", str(SR), "-ac", "1", "-i", "-",
              "-af", "ebur128", "-f", "null", "-"], pad.astype("<f4").tobytes())
    t = p.stderr.decode()
    m = re.search(r"I:\s+(-?[\d.]+) LUFS", t[t.rfind("Summary:"):])
    return float(m.group(1))


def _decode(path):
    p = _run(["ffmpeg", "-v", "error", "-i", path, "-ac", "1", "-ar", str(SR), "-f", "f32le", "-"])
    return np.frombuffer(p.stdout, dtype="<f4").astype(np.float64)


def _encode(x, dst):
    _run(["ffmpeg", "-v", "error", "-y", "-f", "f32le", "-ar", str(SR), "-ac", "1", "-i", "-",
          "-ar", str(SR), "-ac", "1", "-c:a", "libmp3lame", "-b:a", "128k", dst], x.astype("<f4").tobytes())


def render(name, dst):
    x = synth(name)
    # gentle highpass ~800 Hz (2nd order, via ffmpeg), then remove DC
    p = _run(["ffmpeg", "-v", "error", "-f", "f32le", "-ar", str(SR), "-ac", "1", "-i", "-",
              "-af", "highpass=f=800", "-f", "f32le", "-"], x.astype("<f4").tobytes())
    x = np.frombuffer(p.stdout, dtype="<f4").astype(np.float64)
    x = fade_tail(x)
    lim = 10 ** (PEAK_DB / 20) * 0.96                     # margin for mp3 overshoot
    g = 10 ** ((TARGET_LUFS - _measure_pcm(x)) / 20)
    for _ in range(10):                                    # iterate on the decoded mp3
        y = lim * np.tanh(x * g / lim)                     # soft peak limiter: short hits reach the target loudness
        _encode(y, dst)
        d = _decode(dst)
        lufs = _measure_pcm(d)
        if abs(lufs - TARGET_LUFS) <= 0.25 and np.abs(d).max() <= 10 ** (PEAK_DB / 20):
            break
        g *= 10 ** ((TARGET_LUFS - lufs) / 20 * 0.8)
        if np.abs(d).max() > 10 ** (PEAK_DB / 20):
            lim *= 0.98
    return dst


def stats(path):
    d = _decode(path)
    lufs = _measure_pcm(d)
    pk = 20 * np.log10(np.abs(d).max() + 1e-12)
    sp = np.abs(np.fft.rfft(d * np.hanning(len(d))))
    fr = np.fft.rfftfreq(len(d), 1 / SR)
    cen = (sp * fr).sum() / (sp.sum() + 1e-12)
    return len(d) / SR, lufs, pk, cen


NAMES = ["coin_s", "coin_s2", "coin_s3", "coin_m", "coin_l", "coin_xl"]


def render_all(out_dir=OUT):
    os.makedirs(out_dir, exist_ok=True)
    for n in NAMES:
        render(n, os.path.join(out_dir, n + ".mp3"))
    return [n + ".mp3" for n in NAMES]


def print_table(out_dir=OUT, extra=("coin",)):
    print(f"{'file':<14}{'dur s':>7}{'LUFS':>8}{'peak dB':>9}{'centroid Hz':>13}")
    for n in NAMES + list(extra):
        p = os.path.join(out_dir, n + ".mp3")
        if os.path.exists(p):
            d, l, pk, c = stats(p)
            print(f"{n + '.mp3':<14}{d:>7.2f}{l:>8.1f}{pk:>9.1f}{c:>13.0f}")


if __name__ == "__main__":
    o = sys.argv[1] if len(sys.argv) > 1 else OUT
    render_all(o)
    print_table(o)
