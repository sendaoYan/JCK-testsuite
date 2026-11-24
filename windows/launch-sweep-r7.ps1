<#
.SYNOPSIS
  Launch the r7 multi-round divergence sweep (run-windows-r7.bat: N sequential
  rounds of the 13-config r6 sweep) as a detached, hidden process.

.DESCRIPTION
  Invoke over the reverse shell with one quote-free line:

      powershell -NoProfile -File windows\launch-sweep-r7.ps1          # 3 rounds, r7..r9
      powershell -NoProfile -File windows\launch-sweep-r7.ps1 4        # 4 rounds, r7..r10
      powershell -NoProfile -File windows\launch-sweep-r7.ps1 2 10     # 2 rounds, r10..r11

  Detached-launch rules are the same as launch-sweep-r6.ps1: no
  -RedirectStandardOutput/Error (PS 5.1 would keep the caller blocked on the
  pipe); Start-Process -WindowStyle Hidden gives the driver its own console so
  closing the reverse-shell window does not kill it.

  Rounds inside the driver run sequentially and respect .run.lock; do NOT
  launch a second sweep while this one is running.
#>

$ErrorActionPreference = "Stop"

$rounds = 3
if ($args -and $args.Count -gt 0) { $rounds = [int]$args[0] }
$start = 7
if ($args -and $args.Count -gt 1) { $start = [int]$args[1] }

$bat = Join-Path $PSScriptRoot "run-windows-r7.bat"
$p = Start-Process -FilePath "cmd.exe" `
        -ArgumentList ('/c "' + $bat + '" ' + $rounds + ' ' + $start) `
        -WindowStyle Hidden -PassThru

$last = $start + $rounds - 1
Write-Host ("launched r7 multi-round sweep pid=" + $p.Id + " rounds=r" + $start + "..r" + $last)
Write-Host ("results -> " + (Join-Path $PSScriptRoot "windows-results") + "\r<N>-*")
