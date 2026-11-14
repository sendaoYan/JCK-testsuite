@echo off
rem Full GC/JIT sweep: run the baseline and each JVM-flag config by invoking
rem run-windows-full.ps1 once per config. The ps1 probes each JDK with
rem `java -version <flags>` and skips any JDK that rejects the flags (e.g.
rem CMS/ParNew on JDK-27), then drops results in windows-results\<label>\.
rem
rem Label (derived inside the ps1 from the flags) per config:
rem   baseline (no flags)  -> windows-results\baseline\
rem   -XX:+UseConcMarkSweepGC -XX:+UseParNewGC  -> cms_parnew (JDK-8 only)
rem   -XX:+UseG1GC          -> g1
rem   -XX:+UseParNewGC      -> parnew (JDK-8 only)
rem   -XX:+UseSerialGC      -> serial
rem   -Xcomp                -> xcomp
rem   -Xcomp -XX:-TieredCompilation -> xcomp_notiered
rem   -Xcomp -XX:TieredStopAtLevel=1 -> xcomp_tiered1
rem   -Xcomp -XX:TieredStopAtLevel=4 -> xcomp_tiered4
rem   -XX:-BackgroundCompilation -> nobgcomp
rem Flags are handed to the ps1 via the JCK_FLAGS environment variable, NOT the
rem command line: powershell.exe splits -XX:+UseG1GC at the ':' into "-XX" and
rem "+UseG1GC" before the script ever runs, so every colon-bearing flag config
rem was probed as `java -XX +UseG1GC -version` and skipped with
rem "Unrecognized option: -XX". An env-var value is never tokenized.
setlocal
set "PS=powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-windows-full.ps1""

echo === [ 1/10] baseline (no flags) ===
set "JCK_FLAGS="
%PS%

echo === [ 2/10] cms_parnew ===
set "JCK_FLAGS=-XX:+UseConcMarkSweepGC -XX:+UseParNewGC"
%PS%

echo === [ 3/10] g1 ===
set "JCK_FLAGS=-XX:+UseG1GC"
%PS%

echo === [ 4/10] parnew ===
set "JCK_FLAGS=-XX:+UseParNewGC"
%PS%

echo === [ 5/10] serial ===
set "JCK_FLAGS=-XX:+UseSerialGC"
%PS%

echo === [ 6/10] xcomp ===
set "JCK_FLAGS=-Xcomp"
%PS%

echo === [ 7/10] xcomp_notiered ===
set "JCK_FLAGS=-Xcomp -XX:-TieredCompilation"
%PS%

echo === [ 8/10] xcomp_tiered1 ===
set "JCK_FLAGS=-Xcomp -XX:TieredStopAtLevel=1"
%PS%

echo === [ 9/10] xcomp_tiered4 ===
set "JCK_FLAGS=-Xcomp -XX:TieredStopAtLevel=4"
%PS%

echo === [10/10] nobgcomp ===
set "JCK_FLAGS=-XX:-BackgroundCompilation"
%PS%

echo.
echo Sweep done. Results in %~dp0windows-results\ (summary.txt + ^<label^>\*)
pause
