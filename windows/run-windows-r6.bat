@echo off
rem r6 divergence sweep: baseline + 12 options ORTHOGONAL to r5's GC/JIT set.
rem Dimensions: file encoding (Windows is GBK code page 936 by default),
rem locale/timezone, interpreter mode, JIT compiler thread count, object layout
rem (compressed oops / compact strings), jdk27-only GCs (Parallel/Shenandoah/ZGC),
rem and the IPv4/IPv6 network stack. Each config sets JCK_LABEL (human label) +
rem JCK_FLAGS (space-separated JVM flags via env var, NEVER the command line:
rem powershell.exe splits -XX:+Foo at ':' into "-XX" "+Foo" before the script runs).
rem The ps1 probes each JDK with `java -version <flags>` and skips any JDK that
rem rejects them (e.g. jdk8 rejects -XX:-CompactStrings / Shenandoah / ZGC).
setlocal
set "PS=powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-windows-full.ps1""

echo === [ 0/13] baseline ===
set "JCK_LABEL="
set "JCK_FLAGS="
%PS%

echo === [ 1/13] utf8 ===
set "JCK_LABEL=utf8"
set "JCK_FLAGS=-Dfile.encoding=UTF-8"
%PS%

echo === [ 2/13] latin1 ===
set "JCK_LABEL=latin1"
set "JCK_FLAGS=-Dfile.encoding=ISO-8859-1"
%PS%

echo === [ 3/13] enlocale ===
set "JCK_LABEL=enlocale"
set "JCK_FLAGS=-Duser.language=en -Duser.country=US -Duser.timezone=UTC"
%PS%

echo === [ 4/13] xint ===
set "JCK_LABEL=xint"
set "JCK_FLAGS=-Xint"
%PS%

echo === [ 5/13] tiered2 ===
set "JCK_LABEL=tiered2"
set "JCK_FLAGS=-XX:TieredStopAtLevel=2"
%PS%

echo === [ 6/13] ci1 ===
set "JCK_LABEL=ci1"
set "JCK_FLAGS=-XX:CICompilerCount=2"
%PS%

echo === [ 7/13] nooops ===
set "JCK_LABEL=nooops"
set "JCK_FLAGS=-XX:-UseCompressedOops"
%PS%

echo === [ 8/13] nocompactstr ===
set "JCK_LABEL=nocompactstr"
set "JCK_FLAGS=-XX:-CompactStrings"
%PS%

echo === [ 9/13] parallel ===
set "JCK_LABEL=parallel"
set "JCK_FLAGS=-XX:+UseParallelGC"
%PS%

echo === [10/13] shenandoah ===
set "JCK_LABEL=shenandoah"
set "JCK_FLAGS=-XX:+UseShenandoahGC"
%PS%

echo === [11/13] zgc ===
set "JCK_LABEL=zgc"
set "JCK_FLAGS=-XX:+UseZGC"
%PS%

echo === [12/13] ipv4 ===
set "JCK_LABEL=ipv4"
set "JCK_FLAGS=-Djava.net.preferIPv4Stack=true"
%PS%

echo.
echo r6 sweep done. Results in %~dp0windows-results\ (summary.txt + r6-*\)
