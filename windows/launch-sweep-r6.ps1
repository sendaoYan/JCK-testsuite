<#
.SYNOPSIS
  Launch the r6 divergence sweep (run-windows-r6.bat) as a detached, hidden
  process, tagged with JCK_PREFIX=r6 so results land in windows-results\r6-*.

.DESCRIPTION
  Invoke over the reverse shell with one quote-free line:

      powershell -NoProfile -File windows\launch-sweep-r6.ps1

  Same detached-launch rules as launch-sweep.ps1: no -RedirectStandardOutput/Error
  (PS 5.1 would keep the calling shell blocked on the pipe for the whole sweep);
  the .bat's own console output is discarded — per-config console.log is
  authoritative. The sweep runs sequentially (run-windows-full.ps1 holds
  windows-results\.run.lock), so only launch ONE r6 at a time.
#>

$ErrorActionPreference = "Stop"

$env:JCK_PREFIX = "r6"

$bat = Join-Path $PSScriptRoot "run-windows-r6.bat"
$p = Start-Process -FilePath "cmd.exe" -ArgumentList ('/c "' + $bat + '"') `
        -WindowStyle Hidden -PassThru

Write-Host ("launched r6 sweep pid=" + $p.Id + " prefix=r6")
Write-Host ("results -> " + (Join-Path $PSScriptRoot ("windows-results\r6-*")))
