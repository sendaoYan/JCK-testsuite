<#
.SYNOPSIS
  Launch the r6 divergence sweep (run-windows-r6.bat) as a detached, hidden
  process, tagged with JCK_PREFIX=r6 (or a caller-supplied prefix) so results
  land in windows-results\<prefix>-*.

.DESCRIPTION
  Invoke over the reverse shell with one quote-free line:

      powershell -NoProfile -File windows\launch-sweep-r6.ps1          # prefix=r6
      powershell -NoProfile -File windows\launch-sweep-r6.ps1 r6b      # prefix=r6b

  Same detached-launch rules as launch-sweep.ps1: no -RedirectStandardOutput/Error
  (PS 5.1 would keep the calling shell blocked on the pipe for the whole sweep);
  the .bat's own console output is discarded — per-config console.log is
  authoritative. The sweep runs sequentially (run-windows-full.ps1 holds
  windows-results\.run.lock), so only launch ONE r6 at a time.
#>

$ErrorActionPreference = "Stop"

$prefix = "r6"
if ($args -and $args.Count -gt 0) { $prefix = [string]$args[0] }
$prefix = ($prefix -replace '[^A-Za-z0-9.+-]', '_')
if (-not $prefix) { $prefix = "r6" }

$env:JCK_PREFIX = $prefix

$bat = Join-Path $PSScriptRoot "run-windows-r6.bat"
$p = Start-Process -FilePath "cmd.exe" -ArgumentList ('/c "' + $bat + '"') `
        -WindowStyle Hidden -PassThru

Write-Host ("launched r6 sweep pid=" + $p.Id + " prefix=" + $prefix)
Write-Host ("results -> " + (Join-Path $PSScriptRoot ("windows-results\" + $prefix + "-*")))
