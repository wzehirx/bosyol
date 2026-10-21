#!/bin/bash
# Robolectric duman testi. Kullanım: bash yokus/android/test/robo/run.sh
# Gerekli: ROBO_LIB (36 jar: robolectric 4.13 + android-all-instrumented 14), varsayılanı aşağıdaki geçici klasör.
set -e
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
A="$(cd "$HERE/../.." && pwd)"
ATC="${ROBO_ATC:-/tmp/claude-0/-home-user/afa3c9ce-e696-5524-8a18-2a59d29f0c0e/scratchpad/atc}"  # androidx.test için derlenmiş küçük taklitler (monitor aar indirilemedi)
LIB="${ROBO_LIB:-/tmp/claude-0/-home-user/afa3c9ce-e696-5524-8a18-2a59d29f0c0e/scratchpad/robo/lib}"
KC="$A/tools/kotlinc"
OUT="$A/build/robo"
rm -rf "$OUT"; mkdir -p "$OUT"
CP="$(ls "$LIB"/*.jar | tr '\n' ':')$KC/lib/kotlin-stdlib.jar"
# derleme: gerçek android.jar (android-all-instrumented soyut $$robo üyeleri ekler, derlemeye girmemeli)
CCP="$A/tools/android.jar:$(ls "$LIB"/*.jar | grep -v android-all | tr '\n' ':')$KC/lib/kotlin-stdlib.jar"
SRCS=$(find "$A/src" -name '*.kt')
"$KC/bin/kotlinc" -nowarn -jvm-target 17 -no-reflect -cp "$CCP" -d "$OUT" $SRCS "$HERE/UiSmoke.kt" 2>&1 | grep -v "^Picked up" || true
cd "$A"
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Drobolectric.offline=true -Drobolectric.dependency.dir="$LIB" -Dyokus.manifest="$A/AndroidManifest.xml" \
  -cp "$OUT:$ATC:$CP" org.junit.runner.JUnitCore UiSmoke 2>&1 | grep -v "^Picked up"
