<#
.SYNOPSIS
  JCK-testsuite Windows runner for ONE set of JVM flags (a single GC/JIT sweep
  step). Run with no arguments for the default baseline; pass JVM flags to run
  the full suite (JDK-8 + JDK-27) under those flags.

.DESCRIPTION
  Every trailing argument is one JVM flag, propagated to the test VM via
  -javaoption:<flag>. Multiple flags = one combined config, e.g.:
      powershell -ExecutionPolicy Bypass -File run-windows-full.ps1 -Xcomp -XX:-TieredCompilation

  With no arguments it runs the baseline (no JVM flags). Before running, each
  JDK is probed with `java -version <flags>`; if that errors (e.g. the flag was
  removed in that JDK), the JDK is skipped and the error is recorded — so
  CMS/ParNew on JDK-27 (removed in JDK 14, "Unrecognized VM option") is handled
  generically rather than by a hardcoded flag list.

  Failure details go to console.log via -verbose:fail,error,time; the work +
  report dirs are deleted after each tree (no .jtr sprawl).  Results land in
  windows-results\<label>\ (label derived from the flags; empty -> "baseline"):
  per-JDK <jdk8|jdk27>.failed.txt, .counts.txt, .new.txt (delta vs baseline).
  A one-line record is appended to windows-results\summary.txt.

  run-windows-full.bat drives the full 9-config sweep by invoking this script
  once per config (baseline first).

  jtreg concurrency (-conc:N) defaults to 16; set JCK_CONC=<n> to override.
#>

# Raw JVM flags from the command line, read via $args (NOT a param() block).
# PowerShell's parameter binder splits tokens like -XX:+UseG1GC at the ':' into
# "-XX" and "+UseG1GC", which makes `java -XX:+UseG1GC` fail with "Unrecognized
# option: -XX" (the exact reason every GC config skipped with "Unrecognized
# option: -XX"). $args receives the arguments verbatim, so the flag stays whole.
# (Guard: when no args are passed, $args is $null — keep $JvmArgs empty so the
# baseline label is still derived as "baseline".)
$JvmArgs = @()
if ($args) { $JvmArgs = @($args) }

$ErrorActionPreference = "Continue"

# ---- configurable ----
# jtreg -conc:N concurrency per config; override with JCK_CONC=<n> in the env.
$Concurrency = if ($env:JCK_CONC) { [int]$env:JCK_CONC } else { 16 }

$JDKROOT = "D:\yansendao\JDK"
$JDK8    = Join-Path $JDKROOT "jdk-8.0.512.9-hotspot"   # JDK under test (8)
$JDK27   = Join-Path $JDKROOT "jdk-27.0.1.0.2+1"        # JDK under test (27) + jtreg runner

$SUITE      = (Split-Path -Parent $PSScriptRoot)          # git clone root (has JDK-8\, JDK-27\)
$JTREG_HOME = Join-Path $JDKROOT "jtreg"

# ---- locate jtreg.jar (recursive under jtreg) ----
$JTREG_JAR = $null
if (Test-Path $JTREG_HOME) {
    $JTREG_JAR = Get-ChildItem -Path $JTREG_HOME -Filter "jtreg.jar" -Recurse -ErrorAction SilentlyContinue |
                 Select-Object -First 1 -ExpandProperty FullName
}
if (-not $JTREG_JAR) {
    Write-Host "[error] jtreg.jar not found under $JTREG_HOME"
    Write-Host "        copy /data/yansendao/JDK/tools/jtreg/jtreg-8.3 (hanqinhu) to D:\yansendao\JDK\jtreg"
    exit 1
}

$JTREG_JAVA = Join-Path $JDK27 "bin\java.exe"           # jtreg itself needs JDK 11+

# ---- validate ----
foreach ($p in @($SUITE, $JDK8, $JDK27, $JTREG_JAR, $JTREG_JAVA)) {
    if (-not (Test-Path $p)) { Write-Host "[error] missing: $p"; exit 1 }
}

# ---- derive label from flags ----
$label = if ($JvmArgs.Count -eq 0) { "baseline" }
         else { (($JvmArgs -join '+') -replace '[^A-Za-z0-9.+-]', '_') }

$trees = @(
    @{ tag = "jdk8";  jdk = $JDK8;  dir = "JDK-8" },
    @{ tag = "jdk27"; jdk = $JDK27; dir = "JDK-27" }
)

$flagTokens = @($JvmArgs | ForEach-Object { "-javaoption:$_" })

$resRoot = Join-Path $PSScriptRoot "windows-results"
$resDir  = Join-Path $resRoot $label
New-Item -ItemType Directory -Force -Path $resDir | Out-Null

$LOG = Join-Path $resDir "console.log"

# console.log is written UTF-8 (no BOM) throughout. Two separate corruption
# sources are neutralised here:
#   1. Windows PowerShell 5.1's Tee-Object / Out-File / Add-Content default to
#      UTF-16LE (or the ANSI code page), interleaving NUL bytes into the log.
#   2. jtreg's own STDOUT on this box is UTF-16LE (its stderr summary is plain
#      ASCII). PowerShell 5.1 decodes that UTF-16LE stdout one byte at a time,
#      leaving a NUL character after every ASCII char (e.g. "on" -> "o`0n`0").
#      Re-encoding that as UTF-8 writes the NULs as 0x00 bytes -> the log reads
#      as "invisible characters".
# A single UTF-8 (no BOM) encoding object is reused for every write, and every
# line is NUL-stripped to undo the UTF-16LE mis-decode.  Must be spelled
# .Replace([string]([char]0), "") — writing [char]0 (uncast) makes PowerShell 5.1
# bind the String.Replace(char, char) overload, which then rejects the "" second
# argument ("字符串的长度只能为一个字符").  The cast forces the (string, string)
# overload.  The strip is a no-op for clean lines (script messages, stderr
# summary, java -version), so it is safe to apply uniformly.
$Utf8NoBom = New-Object System.Text.UTF8Encoding($false)

function Log([string]$text) {
    Write-Host $text
    [System.IO.File]::AppendAllText($LOG, $text.Replace([string]([char]0), "") + "`r`n", $Utf8NoBom)
}

# Write an array of lines to a file (empty array -> empty file).
function Write-Lines([string]$path, [string[]]$lines) {
    if ($lines.Count -gt 0) { [System.IO.File]::WriteAllLines($path, [string[]]$lines) }
    else { [System.IO.File]::WriteAllText($path, "") }
}

# Run one tree (JDK-8 or JDK-27) under the current flags; writes failed.txt + counts.txt.
function Invoke-Tree([string]$tag, [string]$jdk, [string]$dir) {
    $dirs = @((Join-Path $SUITE "$dir\API"),
              (Join-Path $SUITE "$dir\JLS"),
              (Join-Path $SUITE "$dir\JVMS"))
    $dirs = @($dirs | Where-Object { Test-Path $_ })

    $w   = Join-Path $env:TEMP "jck-$tag-$label\w"
    $rep = Join-Path $env:TEMP "jck-$tag-$label\r"
    New-Item -ItemType Directory -Force -Path $w, $rep | Out-Null

    $flagStr = if ($JvmArgs.Count -gt 0) { ($JvmArgs -join ' ') } else { "(no JVM flags)" }
    $msg = "=== [$tag/$label] jtreg -jdk:$jdk $flagStr ==="
    Write-Host $msg
    [System.IO.File]::AppendAllText($LOG, $msg + "`r`n", $Utf8NoBom)

    $jtArgs = @("-jar", $JTREG_JAR, "-jdk:$jdk", "-w", $w, "-r", $rep,
                "-conc:$Concurrency", "-timeoutFactor:5", "-verbose:fail,error,time",
                "-javacoption:-encoding", "-javacoption:UTF-8") + $flagTokens + $dirs
    # jtreg's verbose stdout is UTF-16LE on this box; PowerShell decodes it
    # single-byte, so each ASCII char is followed by a NUL. Strip the NUL fill
    # bytes before writing/parsing (see the encoding note above).
    $out = @(& $JTREG_JAVA @jtArgs 2>&1 | ForEach-Object { ([string]$_).Replace([string]([char]0), "") })
    if ($out.Count -gt 0) {
        [System.IO.File]::AppendAllLines($LOG, [string[]]$out, $Utf8NoBom)
    }

    # Failed/error list straight from the report summary (no .jtr needed).
    $summary = Join-Path $rep "text\summary.txt"
    $failed = @()
    if (Test-Path $summary) {
        $failed = @(Get-Content $summary |
                    Where-Object { $_ -match ' (Failed|Error)\. ' } |
                    ForEach-Object { ($_ -split '\s+')[0] } |
                    Sort-Object)
    }
    Write-Lines (Join-Path $resDir "$tag.failed.txt") $failed

    $countsLine = ($out | Select-String 'Test results:' | Select-Object -Last 1).Line
    if ($countsLine) { Write-Lines (Join-Path $resDir "$tag.counts.txt") @($countsLine) }

    # Failure details already went to console.log; drop work + report dirs so
    # per-test .jtr/.class small files don't accumulate.
    Remove-Item -LiteralPath $w   -Recurse -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $rep -Recurse -Force -ErrorAction SilentlyContinue
}

Log "=== JCK-testsuite Windows sweep: label=$label flags=[$($JvmArgs -join ' ')] ==="
Log "suite : $SUITE"
Log "JDK-8 : $JDK8"
Log "JDK-27: $JDK27"
Log ""

# ---- versions (recorded for failure analysis) ----
Log "=== versions ==="
Log ("--- jtreg ---")
& $JTREG_JAVA -jar $JTREG_JAR -version 2>&1 | ForEach-Object { Log ("  " + $_) }
Log "--- java -version (JDK-8 under test) ---"
& (Join-Path $JDK8 "bin\java.exe") -version 2>&1 | ForEach-Object { Log ("  " + $_) }
Log "--- java -version (JDK-27 under test) ---"
& (Join-Path $JDK27 "bin\java.exe") -version 2>&1 | ForEach-Object { Log ("  " + $_) }

# Windows OS + CPU identity (国产 x86 vs Intel/AMD attribution).
Log "--- Windows OS ---"
try {
    $os = Get-CimInstance Win32_OperatingSystem -ErrorAction Stop
    Log ("  " + $os.Caption + "  version " + $os.Version + "  build " + $os.BuildNumber)
} catch {
    Log ("  OSVersion: " + [System.Environment]::OSVersion.VersionString)
}
try {
    $cpu = Get-CimInstance Win32_Processor -ErrorAction Stop | Select-Object -First 1
    Log ("  CPU: " + $cpu.Name)
} catch {
    Log "  CPU: (unavailable)"
}
Log ("  PROCESSOR_ARCHITECTURE = " + $env:PROCESSOR_ARCHITECTURE)
Log ("  COMPUTERNAME           = " + $env:COMPUTERNAME)
Log ""

# ---- cleanup leftover TEMP dirs from previous (possibly crashed) runs ----
$jckDirs = @(Get-ChildItem -Path $env:TEMP -Directory -ErrorAction SilentlyContinue |
             Where-Object { $_.Name -like "jck-*" -or $_.Name -like "_jck-trash-*" })
foreach ($d in $jckDirs) {
    Remove-Item -LiteralPath $d.FullName -Recurse -Force -ErrorAction SilentlyContinue
}
Log ("[cleanup] removed " + $jckDirs.Count + " leftover TEMP dir(s)")
Log ""

# ---- probe each JDK: skip it if `java -version <flags>` errors (flag removed) ----
$run = @()
foreach ($t in $trees) {
    $java = Join-Path $t.jdk "bin\java.exe"
    $probeArgs = @($JvmArgs) + @("-version")
    $probeOut = & $java @probeArgs 2>&1
    if ($LASTEXITCODE -ne 0) {
        Log ("[skip] " + $t.tag + ": java -version " + ($JvmArgs -join ' ') + " -> exit code " + $LASTEXITCODE)
        $probeOut | ForEach-Object { Log ("  " + $_) }
        # Record the exact flags that were rejected, so the .skipped.txt shows
        # which option triggered the skip (not just the JVM's generic error).
        $skipLines = @("flags: " + ($JvmArgs -join ' ')) + @($probeOut)
        Write-Lines (Join-Path $resDir ($t.tag + ".skipped.txt")) $skipLines
    } else {
        $run += $t
    }
}

# ---- run the (non-skipped) trees ----
foreach ($t in $run) {
    Invoke-Tree $t.tag $t.jdk $t.dir
}

# ---- delta vs baseline ----
foreach ($t in $trees) {
    if (Test-Path (Join-Path $resDir "$($t.tag).skipped.txt")) { continue }
    $baseFailed = Join-Path $resRoot "baseline\$($t.tag).failed.txt"
    $failedPath = Join-Path $resDir "$($t.tag).failed.txt"
    $newPath    = Join-Path $resDir "$($t.tag).new.txt"
    if ($label -ne "baseline" -and (Test-Path $baseFailed)) {
        $base = @(Get-Content $baseFailed -ErrorAction SilentlyContinue)
        $f    = @(Get-Content $failedPath -ErrorAction SilentlyContinue)
        $n    = @($f | Where-Object { $base -cnotcontains $_ })
        Write-Lines $newPath $n
    } else {
        Write-Lines $newPath @()
    }
}

# ---- append one-line record to summary.txt ----
# Column value for summary.txt: "skip" if that JDK was skipped by the probe,
# otherwise the line count of <tag>.<kind>.txt.
function Col([string]$tag, [string]$kind) {
    if (Test-Path (Join-Path $resDir "$tag.skipped.txt")) { return "skip" }
    $p = Join-Path $resDir "$tag.$kind.txt"
    if (Test-Path $p) { return ("" + @(Get-Content $p -ErrorAction SilentlyContinue).Count) }
    return "0"
}
$jdk8F = Col "jdk8" "failed"
$jdk8N = Col "jdk8" "new"
$jdk27F = Col "jdk27" "failed"
$jdk27N = Col "jdk27" "new"

$sumFile = Join-Path $resRoot "summary.txt"
if (-not (Test-Path $sumFile)) {
    [System.IO.File]::WriteAllText($sumFile, ("{0,-40}`t{1,7}`t{2,7}`t{3,7}`t{4,7}" -f "label", "jdk8fail", "jdk8new", "jdk27fail", "jdk27new") + "`r`n", $Utf8NoBom)
} else {
    # Older runs wrote the header with Set-Content -Encoding UTF8, which left a
    # leading UTF-8 BOM (U+FEFF, shows as "﻿" before "label"). Strip it once so
    # every later append stays clean no-BOM UTF-8.
    $existing = [System.IO.File]::ReadAllText($sumFile)
    if ($existing.StartsWith([char]0xFEFF)) {
        [System.IO.File]::WriteAllText($sumFile, $existing.Substring(1), $Utf8NoBom)
    }
}
[System.IO.File]::AppendAllText($sumFile, ("{0,-40}`t{1,7}`t{2,7}`t{3,7}`t{4,7}" -f $label, $jdk8F, $jdk8N, $jdk27F, $jdk27N) + "`r`n", $Utf8NoBom)

Log ""
Log ("=== DONE label=$label  jdk8.failed=$jdk8F jdk8.new=$jdk8N jdk27.failed=$jdk27F jdk27.new=$jdk27N ===")
