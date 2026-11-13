<#
.SYNOPSIS
  Launch ONE run-windows-full.ps1 config as a detached, hidden process so the
  reverse-shell command server returns immediately and the sweep keeps running.

.DESCRIPTION
  Invoke over the reverse shell with a single, quote-free line:

      powershell -NoProfile -ExecutionPolicy Bypass -File launch.ps1 baseline
      powershell -NoProfile -ExecutionPolicy Bypass -File launch.ps1 g1 -XX:+UseG1GC

  First non-flag token is the label (default "baseline"); every remaining token
  is passed verbatim to run-windows-full.ps1 as a JVM flag (it reads $args, so
  -XX:+... tokens survive intact).  The child's stdout/stderr go to
  windows\sweep-<label>.out / .err; jtreg results land in
  windows-results\<label>\ as usual (poll console.log there).

  Deliberately has NO param() block: that way -XX:+UseG1GC etc. are NOT eaten by
  PowerShell's parameter binder and land in $args verbatim.
#>

$ErrorActionPreference = "Stop"

$label = "baseline"
$flags = @()
if ($args -and $args.Count -gt 0) {
    $rest = @($args)
    if ($rest[0] -notlike '-*') { $label = $rest[0]; $rest = @($rest[1..($rest.Count - 1)]) }
    $flags = @($rest)
}

$run = Join-Path $PSScriptRoot "run-windows-full.ps1"
$out = Join-Path $PSScriptRoot ("sweep-" + $label + ".out")
$err = Join-Path $PSScriptRoot ("sweep-" + $label + ".err")

$argList = @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $run) + $flags

$p = Start-Process -FilePath "powershell.exe" -ArgumentList $argList `
    -WindowStyle Hidden -PassThru `
    -RedirectStandardOutput $out -RedirectStandardError $err

Write-Host ("launched pid=" + $p.Id + " label=" + $label + " flags=[" + ($flags -join ' ') + "]")
Write-Host ("stdout -> " + $out)
Write-Host ("results -> " + (Join-Path $PSScriptRoot "windows-results\" + $label))
