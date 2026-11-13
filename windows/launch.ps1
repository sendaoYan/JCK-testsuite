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
  -XX:+... tokens survive intact).  The child runs hidden and detached; its
  authoritative log is windows-results\<label>\console.log (poll that).

  Deliberately has NO param() block: that way -XX:+UseG1GC etc. are NOT eaten by
  PowerShell's parameter binder and land in $args verbatim.
#>

$ErrorActionPreference = "Stop"

$label = "baseline"
$flags = @()
if ($args -and $args.Count -gt 0) {
    $rest = @($args)
    if ($rest[0] -notlike '-*') {
        $label = $rest[0]
        # Select-Object -Skip 1 yields an empty collection for a 1-element list
        # (a raw $rest[1..0] range would return @($null,$rest[0]) — PowerShell
        # ranges are inclusive/descending, not "empty when out of range").
        $rest = @($rest | Select-Object -Skip 1)
    }
    $flags = @($rest)
}
# Same sanitisation run-windows-full.ps1 applies, so the path printed below is
# the directory that will actually be written.
$label = ($label -replace '[^A-Za-z0-9.+-]', '_')
if (-not $label) { $label = "baseline" }

$run = Join-Path $PSScriptRoot "run-windows-full.ps1"

# The label crosses the process boundary in an environment variable: Start-Process
# has no way to pass a non-JVM argument (run-windows-full.ps1 reads every trailing
# token as a JVM flag), and it inherits the parent's environment. Without this the
# child derives its own label from the flags and writes windows-results\<derived>\,
# so the path printed below would not exist.
$env:JCK_LABEL = $label

# Start-Process -ArgumentList takes ONE string, not an array: PS 5.1 joins an
# array with spaces and does not quote the elements, so a flag containing a space
# (e.g. -XX:OnError="..." ) would be split into several arguments. Quote any token
# that contains whitespace or a quote.
$tokens = @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', ('"' + $run + '"'))
foreach ($f in $flags) {
    if ($f -match '[\s"]') {
        $tokens += ('"' + ($f -replace '"', '\"') + '"')
    } else {
        $tokens += $f
    }
}
$argStr = $tokens -join ' '

# NO -RedirectStandardOutput/-RedirectStandardError: in PS 5.1 those leave a
# stdout pipe that keeps the calling PowerShell alive until the long-running
# child exits, which blocks the single-threaded reverse shell for the whole
# sweep.  run-windows-full.ps1 already writes its authoritative log to
# windows-results\<label>\console.log, so capturing the child's stdout is not
# needed — Start-Process without redirection returns immediately.
$p = Start-Process -FilePath "powershell.exe" -ArgumentList $argStr -WindowStyle Hidden -PassThru

Write-Host ("launched pid=" + $p.Id + " label=" + $label + " flags=[" + ($flags -join ' ') + "]")
$resLog = Join-Path $PSScriptRoot ("windows-results\" + $label + "\console.log")
Write-Host ("results -> " + $resLog)
