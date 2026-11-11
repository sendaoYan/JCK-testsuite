@echo off
rem Double-click wrapper: collects failed .jtr files into windows-jtr-*.txt/zip.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0collect-windows-failures.ps1"
echo.
echo Exit code: %ERRORLEVEL%
pause
