#!/usr/bin/env bash
# Unified native build for the whole testsuite (OpenJDK "make test-image" style).
#
# Discovers every .c/.cpp under JDK-8/API and JDK-27/API, compiles each shared
# library with the JDK toolchain headers of its own side, and places all
# artifacts under one fixed directory, keyed by BOTH JDK side and CPU arch:
#
#     build/native/<side>-<arch>/lib<name>.so     e.g. build/native/jdk8-riscv64/
#
# Tests then load via jtreg:
#     jtreg -nativepath:build/native/jdk8-riscv64 ...   (property test.nativepath)
# and inside a test:
#     Path dir = Path.of(System.getProperty("test.nativepath"));
#     System.load(dir.resolve(System.mapLibraryName("jcksyslife")).toString());
#
# Arch selection & cross compiling:
#     tools/build-native.sh [-a <arch>] [-s <side>] [-f]
#       -a amd64|aarch64|riscv64|...   default: host arch (uname -m, canonicalized)
#       -s jdk8|jdk27                  build only one side (default: both)
#       -f                             force rebuild of up-to-date libs
#     Cross builds pick the compiler automatically:
#       CC/CXX env  >  <arch>-unknown-linux-gnu-gcc >  <arch>-linux-gnu-gcc
#     e.g. for riscv64 this finds riscv64-unknown-linux-gnu-gcc (user toolchain)
#     or the distro riscv64-linux-gnu-gcc. .c files use $CC, .cpp use $CXX.
#
# Env overrides: JDK8_HOME, JDK27_HOME (JDK include/ dirs; jni.h is
# arch-independent), CC, CXX.
#
# Sources with identical content on both sides are compiled once per
# (side, arch) so artifacts always match the JDK under test.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/build/native"
JDK8_HOME="${JDK8_HOME:-/data/yansendao/JDK/binary/temurin/jdk8u372-b07}"
JDK27_HOME="${JDK27_HOME:-/data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1}"

host_arch() {
    case "$(uname -m)" in
        x86_64)         echo amd64 ;;
        i?86)           echo x86 ;;
        aarch64|arm64)  echo aarch64 ;;
        *)              echo "$(uname -m)" ;;
    esac
}

ARCH="$(host_arch)"
SIDES="jdk8 jdk27"
FORCE=0
while getopts "a:s:f" opt; do
    case "$opt" in
        a) ARCH="$OPTARG" ;;
        s) SIDES="$OPTARG" ;;
        f) FORCE=1 ;;
        *) echo "usage: $0 [-a arch] [-s side] [-f]" >&2; exit 2 ;;
    esac
done
shift $((OPTIND - 1))

HOST_ARCH="$(host_arch)"

pick_compiler() { # $1 lang (cc|cxx)  -> prints compiler command
    local lang="$1" host_cc host_cand1 host_cand2 cross1 cross2
    host_cc="gcc"; [ "$lang" = cxx ] && host_cc="g++"
    if [ -n "${CC:-}" ] || [ -n "${CXX:-}" ]; then
        # explicit override wins for its language
        if [ "$lang" = cc ] && [ -n "${CC:-}" ]; then echo "$CC"; return; fi
        if [ "$lang" = cxx ] && [ -n "${CXX:-}" ]; then echo "$CXX"; return; fi
    fi
    if [ "$ARCH" = "$HOST_ARCH" ]; then
        echo "$host_cc"; return
    fi
    cross1="${ARCH/x86_/amd64}"     # canonicalize (amd64-* triplets are rare; both listed)
    for cand in "${ARCH}-unknown-linux-gnu-${host_cc}" "${ARCH}-linux-gnu-${host_cc}" \
                "${cross1}-unknown-linux-gnu-${host_cc}" "${cross1}-linux-gnu-${host_cc}"; do
        command -v "$cand" >/dev/null 2>&1 && { echo "$cand"; return; }
    done
    echo "ERROR: no cross $host_cc found for arch $ARCH (set CC/CXX env)" >&2
    return 1
}

CC_BIN="$(pick_compiler cc)"
CXX_BIN="$(pick_compiler cxx)"

for j in "$JDK8_HOME" "$JDK27_HOME"; do
    [ -f "$j/include/jni.h" ] || { echo "ERROR: no include/jni.h under $j (set JDK8_HOME/JDK27_HOME)" >&2; exit 1; }
done

built=0; skipped=0; failed=0

build_side() { # $1 side (jdk8|jdk27)  $2 jdk home  $3 api root
    local side="$1" jhome="$2" api="$3" src obj out tagdir
    tagdir="$OUT/$side-$ARCH"
    while IFS= read -r src; do
        local comp="$CC_BIN"
        case "$src" in *.cpp|*.cc|*.C) comp="$CXX_BIN" ;; esac
        obj="$tagdir/lib$(basename "$src" | sed -E 's/\.(c|cpp|cc|C)$//').so"
        mkdir -p "$tagdir"
        if [ "$FORCE" -eq 0 ] && [ -f "$obj" ] && [ "$obj" -nt "$src" ]; then
            skipped=$((skipped + 1))
            continue
        fi
        out=$("$comp" -O2 -shared -fPIC \
              -I"$jhome/include" -I"$jhome/include/linux" \
              -o "$obj" "$src" 2>&1) || {
            echo "FAIL: [$side-$ARCH] $src" >&2; echo "$out" | sed 's/^/      /' >&2
            failed=$((failed + 1)); continue
        }
        built=$((built + 1))
        echo "ok: [$side-$ARCH] $(basename "$src") -> ${obj#"$ROOT"/}"
    done < <(find "$api" -type f \( -name '*.c' -o -name '*.cpp' -o -name '*.cc' -o -name '*.C' \) | sort)
}

for s in $SIDES; do
    case "$s" in
        jdk8)  build_side jdk8  "$JDK8_HOME"  "$ROOT/JDK-8/API" ;;
        jdk27) build_side jdk27 "$JDK27_HOME" "$ROOT/JDK-27/API" ;;
        *) echo "ERROR: unknown side '$s' (jdk8|jdk27)" >&2; exit 2 ;;
    esac
done

echo
echo "native build summary: arch=$ARCH compiler=$CC_BIN built=$built up-to-date=$skipped failed=$failed"
echo "artifact dirs: $OUT/<side>-$ARCH  (jtreg -nativepath:$OUT/<side>-$ARCH)"
[ "$failed" -eq 0 ] || exit 2
