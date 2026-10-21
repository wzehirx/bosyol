#!/bin/bash
# NewApi denetimi. Kullanım: bash yokus/android/test/lint.sh   (önce build.py ile derlenmiş build/classes gerekir)
# lint, aapt2'nin eklediği minSdk'yi görmez; bu yüzden uses-sdk eklenmiş geçici bir proje klasörü kurulur.
# LINT_SRC ile başka bir kaynak klasörü verilebilir (denetimin gerçekten bulduğunu sınamak için).
set -e
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
A="$(dirname "$HERE")"
SDK="${ANDROID_HOME:-/opt/android-sdk}"
W="$A/build/lint"; rm -rf "$W"; mkdir -p "$W/p"
cp -r "${LINT_SRC:-$A/src}" "$W/p/src"
cp -r "$A/res" "$W/p/res"
sed 's#<application#<uses-sdk android:minSdkVersion="26" android:targetSdkVersion="34"/>\n    <application#' "$A/AndroidManifest.xml" > "$W/p/AndroidManifest.xml"
cd "$W/p"
"$SDK/cmdline-tools/latest/bin/lint" --check NewApi --sdk-home "$SDK" --classpath "$A/build/classes" --text "$W/out.txt" . 2>&1 | grep -v "^Picked up" || true
cat "$W/out.txt"
