@echo off
setlocal
rem Reverse shell over an SSH reverse tunnel. Double-click to run.
rem   [1] git pull the latest scripts, [2] start reverse-shell.ps1.
rem Keep this window open; hanqinhu then connects via `nc 127.0.0.1 4444`.

rem Work from the repo root (this file lives in windows\).
cd /d "%~dp0.."

echo [1/2] git pull (get latest)
git pull --ff-only

echo [2/2] starting reverse shell ...
powershell -NoLogo -ExecutionPolicy Bypass -File "%~dp0reverse-shell.ps1" %*

echo.
echo reverse-shell.ps1 exited with code %ERRORLEVEL%.
echo Close this window, or press any key.
pause >nul
