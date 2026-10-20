#!/usr/bin/env python3
"""Yokuş Ustası APK derleyicisi: resmî Android SDK araçlarıyla (aapt2, d8, zipalign, apksigner) ve kotlinc ile derler.
Kullanım: python3 build.py [sürüm_kodu] [sürüm_adı]"""
import os, shutil, subprocess, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
T = os.path.join(HERE, 'tools')
SDK = os.environ.get('ANDROID_HOME', '/opt/android-sdk')
BT = os.path.join(SDK, 'build-tools', '34.0.0')
AJAR = os.path.join(SDK, 'platforms', 'android-34', 'android.jar')
B = os.path.join(HERE, 'build')
VC = sys.argv[1] if len(sys.argv) > 1 else '1'
VN = sys.argv[2] if len(sys.argv) > 2 else '1.0'
KS, KS_PASS, KS_ALIAS = os.path.join(HERE, 'yokus.jks'), 'yokus2026', 'yokus'

def run(*cmd, **kw):
    print('$', ' '.join(cmd)[:160]); subprocess.run(cmd, check=True, **kw)

if not os.path.exists(KS):
    run('keytool', '-genkeypair', '-keystore', KS, '-alias', KS_ALIAS, '-storepass', KS_PASS, '-keypass', KS_PASS,
        '-keyalg', 'RSA', '-keysize', '2048', '-validity', '10000', '-dname', 'CN=Yokus Ustasi')
shutil.rmtree(B, ignore_errors=True)
os.makedirs(B)

# 1) Sesler uygulamanın içine (sıkıştırmasız, doğrudan okunabilsin diye)
SND = os.path.join(ROOT, 'sounds')
if os.path.isdir(SND): shutil.copytree(SND, os.path.join(B, 'assets', 'sounds'))
else: os.makedirs(os.path.join(B, 'assets', 'sounds'))

# 2) Kaynaklar ve bildirim dosyası
flat = os.path.join(B, 'res.zip')
run(os.path.join(BT, 'aapt2'), 'compile', '--dir', os.path.join(HERE, 'res'), '-o', flat)
base = os.path.join(B, 'base.apk')
run(os.path.join(BT, 'aapt2'), 'link', '-o', base, '-I', AJAR,
    '--manifest', os.path.join(HERE, 'AndroidManifest.xml'), '-A', os.path.join(B, 'assets'),
    '--min-sdk-version', '26', '--target-sdk-version', '34', '--version-code', VC, '--version-name', VN,
    '-0', 'mp3', '-0', 'wav', flat)

# 3) Kotlin -> sınıflar -> d8 ile dex (d8 lambdaları Android'e uygun hale getirir)
cls = os.path.join(B, 'classes'); os.makedirs(cls)
srcs = [os.path.join(d, f) for d, _, fs in os.walk(os.path.join(HERE, 'src')) for f in fs if f.endswith('.kt')]
STD = os.path.join(T, 'kotlinc', 'lib', 'kotlin-stdlib.jar')
run(os.path.join(T, 'kotlinc', 'bin', 'kotlinc'), '-jvm-target', '1.8', '-no-reflect', '-nowarn',
    '-Xlambdas=class', '-Xsam-conversions=class', '-Xstring-concat=inline',
    '-cp', AJAR, '-d', cls, *srcs)
classes = [os.path.join(d, f) for d, _, fs in os.walk(cls) for f in fs if f.endswith('.class')]
dex = os.path.join(B, 'dex'); os.makedirs(dex)
run(os.path.join(BT, 'd8'), '--release', '--min-api', '26', '--lib', AJAR, '--output', dex, STD, *classes)
# invoke-custom denetimi: Android'de çökme yaratır, varsa derleme başarısız olur
dd = subprocess.run([os.path.join(BT, 'dexdump'), '-d', os.path.join(dex, 'classes.dex')], capture_output=True, text=True, check=True).stdout
n_ic = dd.count('invoke-custom')
print('invoke-custom sayısı:', n_ic)
if n_ic: sys.exit(f'HATA: {n_ic} adet invoke-custom var')
with zipfile.ZipFile(base, 'a', zipfile.ZIP_DEFLATED) as z:
    for f in sorted(os.listdir(dex)):
        if f.endswith('.dex'): z.write(os.path.join(dex, f), f)

# 4) Hizala ve imzala
aligned = os.path.join(B, 'aligned.apk')
run(os.path.join(BT, 'zipalign'), '-f', '-p', '4', base, aligned)
final = os.path.join(ROOT, f'YokusUstasi-{VN}.apk')
run(os.path.join(BT, 'apksigner'), 'sign', '--ks', KS, '--ks-key-alias', KS_ALIAS, '--ks-pass', 'pass:' + KS_PASS,
    '--key-pass', 'pass:' + KS_PASS, '--out', final, aligned)
run(os.path.join(BT, 'apksigner'), 'verify', '--print-certs', final)
print('APK:', final, os.path.getsize(final) // 1024, 'KB')
