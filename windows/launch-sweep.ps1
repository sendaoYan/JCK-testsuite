<#
.SYNOPSIS
  Launch ONE full 10-config sweep (run-windows-full.bat) as a detached, hidden
  process, tagged with a round prefix so repeated sweeps do not overwrite each
  other's results.

.DESCRIPTION
  Invoke over the reverse shell with a single, quote-free line:

      powershell -NoProfile -ExecutionPolicy Bypass -File launch-sweep.ps1 r2

  The tag (default "sweep") is exported as JCK_PREFIX; run-windows-full.ps1
  prefixes every label with it, so a round writes windows-results\r2-baseline\,
  windows-results\r2-g1\, ...  Each config still writes its authoritative log to
  windows-results\<prefixed-label>\console.log and appends one line to
  windows-results\summary.txt.

  The sweep runs sequentially (one config at a time; run-windows-full.ps1 holds
  windows-results\.run.lock), so only launch ONE sweep per round.

  Same detached-launch rules as launch.ps1: no -RedirectStandardOutput/-Error
  (PS 5.1 would keep the calling shell blocked on the pipe for the whole sweep),
  and the .bat's own console output is discarded — the per-config console.log is
  authoritative.
#>

$ErrorActionPreference = "Stop"

$prefix = "sweep"
if ($args -and $args.Count -gt 0) { $prefix = [string]$args[0] }
$prefix = ($prefix -replace '[^A-Za-z0-9.+-]', '_')
if (-not $prefix) { $prefix = "sweep" }

$env:JCK_PREFIX = $prefix

$bat = Join-Path $PSScriptRoot "run-windows-full.bat"
# Full 10-config sweep, driver output discarded: every step logs to its own dir.
$p = Start-Process -FilePath "cmd.exe" -ArgumentList ('/c "' + $bat + '"') `
        -WindowStyle Hidden -PassThru

Write-Host ("launched sweep pid=" + $p.Id + " prefix=" + $prefix)
Write-Host ("results -> " + (Join-Path $PSScriptRoot ("windows-results\" + $prefix + "-*")))
