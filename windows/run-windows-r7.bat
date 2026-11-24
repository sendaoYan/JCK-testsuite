@echo off
rem r7: additional full rounds of the SAME 13-config orthogonal divergence
rem sweep as r6 (encoding/locale/interpreter/JIT/object-layout/GC/network),
rem chained sequentially inside ONE driver process. Each round gets its own
rem JCK_PREFIX so results land in windows-results\r<N>-<label>\ without
rem overwriting earlier rounds.
rem
rem   run-windows-r7.bat [rounds] [startIndex]      (defaults: 3 rounds, start 7)
rem
rem Rounds MUST be chained here rather than by launching several sweeps: the
rem drivers contend on windows-results\.run.lock and a concurrent one exits 2
rem (see windows/launch-sweep-r7.ps1).
setlocal
set "DRIVER=%~dp0run-windows-r6.bat"
set "ROUNDS=%~1"
if "%ROUNDS%"=="" set "ROUNDS=3"
set "START=%~2"
if "%START%"=="" set "START=7"
set /a LAST=START+ROUNDS-1
for /l %%i in (%START%,1,%LAST%) do (
  set "JCK_PREFIX=r%%i"
  echo.
  echo ##### ROUND r%%i START %date% %time% #####
  call "%DRIVER%"
  echo ##### ROUND r%%i DONE  %date% %time% #####
)
echo r7 multi-round sweep done.
