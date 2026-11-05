#!/usr/bin/env bash
# Unified native build for the whole testsuite (OpenJDK "make test-image" style).
#
# Discovers every .c/.cpp under JDK-8/API and JDK-27/API, compiles each shared
# library with the JDK toolchain headers of its own side, and places all
# artifacts under one fixed directory:
#
#     build/native/jdk8/lib<name>.so
#     build/native/jdk27/lib<name>.so
#
# Tests then load via jtreg:
#     jtreg -nativepath:build/native/jdk27 ...   (property test.nativepath)
# and inside a test:
#     Path dir = Path.of(System.getProperty("test.nativepath"));
#     System.load(dir.resolve(System.mapLibraryName("jcksyslife")).toString());
#
# Sources with identical content on both sides are compiled twice (per-side
# headers) so artifacts are always consistent with the JDK under test.
#
# Usage: tools/build-native.sh [-f]   (add -f to force rebuild of up-to-date libs)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/build/native"
JDK8_HOME="${JDK8_HOME:-/data/yansendao/JDK/binary/temurin/jdk8u372-b07}"
JDK27_HOME="${JDK27_HOME:-/data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1}"
CC="${CC:-gcc}"
FORCE=0
[ "${1:-}" = "-f" ] && FORCE=1

for j in "$JDK8_HOME" "$JDK27_HOME"; do
    [ -f "$j/include/jni.h" ] || { echo "ERROR: no include/jni.h under $j (set JDK8_HOME/JDK27_HOME)" >&2; exit 1; }
done
command -v "$CC" >/dev/null || { echo "ERROR: $CC not found" >&2; exit 1; }

built=0; skipped=0; failed=0

build_side() { # $1 side dir name (jdk8|jdk27)  $2 jdk home  $3 api root
    local side="$1" jhome="$2" api="$3" src obj out
    while IFS= read -r src; do
        obj="$OUT/$side/lib$(basename "$src" | sed -E 's/\.(c|cpp|cc|C)$//').so"
        mkdir -p "$OUT/$side"
        if [ "$FORCE" -eq 0 ] && [ -f "$obj" ] && [ "$obj" -nt "$src" ]; then
            skipped=$((skipped + 1))
            continue
        fi
        out=$("$CC" -O2 -shared -fPIC \
              -I"$jhome/include" -I"$jhome/include/linux" \
              -o "$obj" "$src" 2>&1) || {
            echo "FAIL: [$side] $src" >&2; echo "$out" | sed 's/^/      /' >&2
            failed=$((failed + 1)); continue
        }
        built=$((built + 1))
        echo "ok: [$side] $(basename "$src") -> ${obj#"$ROOT"/}"
    done < <(find "$api" -type f \( -name '*.c' -o -name '*.cpp' -o -name '*.cc' -o -name '*.C' \) | sort)
}

build_side jdk8  "$JDK8_HOME"  "$ROOT/JDK-8/API"
build_side jdk27 "$JDK27_HOME" "$ROOT/JDK-27/API"

echo
echo "native build summary: built=$built up-to-date=$skipped failed=$failed"
echo "artifact dir: $OUT (pass to jtreg via -nativepath:$OUT/jdk8 / $OUT/jdk27)"
[ "$failed" -eq 0 ] || exit 2
