@echo off
rem Double-click wrapper: runs the PowerShell runner with execution policy bypassed.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-windows-full.ps1"
echo.
echo Exit code: %ERRORLEVEL%
pause
