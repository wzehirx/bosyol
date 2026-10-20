#!/bin/bash
# Kullanım: bash yokus/android/test/run.sh <TestAdı>
# core/*.kt + test/<TestAdı>.kt + test/lib/*.kt dosyalarını derler ve çalıştırır.
set -e
NAME="${1:?test adı gerekli, örn. TerrainTest}"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
A="$(dirname "$HERE")"
KC=/home/user/bosyol/android/tools/kotlinc
OUT="$A/build/test"
mkdir -p "$OUT"
rm -rf "$OUT/$NAME"; mkdir -p "$OUT/$NAME"
SRCS=$(find "$A/src/com/yokus/app/core" -name '*.kt')
LIBS=$(find "$HERE/lib" -name '*.kt' 2>/dev/null || true)
"$KC/bin/kotlinc" -nowarn -jvm-target 1.8 -no-reflect -d "$OUT/$NAME" $SRCS $LIBS "$HERE/$NAME.kt"
java -cp "$OUT/$NAME:$KC/lib/kotlin-stdlib.jar" "${NAME}Kt"
