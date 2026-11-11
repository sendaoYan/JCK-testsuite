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
setlocal
set "PS=powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-windows-full.ps1""

echo === [ 1/10] baseline (no flags) ===
%PS%

echo === [ 2/10] cms_parnew ===
%PS% -XX:+UseConcMarkSweepGC -XX:+UseParNewGC

echo === [ 3/10] g1 ===
%PS% -XX:+UseG1GC

echo === [ 4/10] parnew ===
%PS% -XX:+UseParNewGC

echo === [ 5/10] serial ===
%PS% -XX:+UseSerialGC

echo === [ 6/10] xcomp ===
%PS% -Xcomp

echo === [ 7/10] xcomp_notiered ===
%PS% -Xcomp -XX:-TieredCompilation

echo === [ 8/10] xcomp_tiered1 ===
%PS% -Xcomp -XX:TieredStopAtLevel=1

echo === [ 9/10] xcomp_tiered4 ===
%PS% -Xcomp -XX:TieredStopAtLevel=4

echo === [10/10] nobgcomp ===
%PS% -XX:-BackgroundCompilation

echo.
echo Sweep done. Results in %~dp0windows-results\ (summary.txt + ^<label^>\*)
pause
