<#
.SYNOPSIS
  JCK-testsuite Windows full runner: run jdk-8 / jdk-27 each RUNS times (API+JLS+JVMS).

.DESCRIPTION
  Assumes this script sits at <repo root>\windows\ inside a git clone of
  git@github.com:sendaoYan/JCK-testsuite.git (the repo root holds JDK-8\ and
  JDK-27\).  jtreg is expected under D:\yansendao\JDK\jtreg (jtreg.jar found
  recursively).  The two JDKs are expected under D:\yansendao\JDK.

  Usage (PowerShell):
    powershell -ExecutionPolicy Bypass -File run-windows-full.ps1
    (or just double-click run-windows-full.bat)
#>

$ErrorActionPreference = "Continue"

# ---- configurable ----
$JDKROOT = "D:\yansendao\JDK"
$JDK8    = Join-Path $JDKROOT "jdk-8.0.512.9-hotspot"   # JDK under test (8)
$JDK27   = Join-Path $JDKROOT "jdk-27.0.1.0.2+1"        # JDK under test (27) + jtreg runner
$RUNS    = 5                                            # passes per JDK

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

$STAMP = Get-Date -Format "yyyyMMdd-HHmmss"
$LOG   = Join-Path $PSScriptRoot "windows-$STAMP.summary.log"

# Write one line to both the console and the summary log.
function Log([string]$text) {
    Write-Host $text
    Add-Content -Path $LOG -Value $text
}

# Run a command, teeing each output line to console + log.
function Run-Log([string]$label, [string]$exe, [string[]]$argv) {
    Log ("--- " + $label + " ---")
    & $exe @argv 2>&1 | ForEach-Object { Log ("  " + $_) }
}

function Invoke-Tree([string]$tree, [string]$jdk, [string]$tag) {
    $dirs = @((Join-Path $SUITE "$tree\API"),
              (Join-Path $SUITE "$tree\JLS"),
              (Join-Path $SUITE "$tree\JVMS"))
    $dirs = @($dirs | Where-Object { Test-Path $_ })
    for ($r = 1; $r -le $RUNS; $r++) {
        $w   = Join-Path $env:TEMP "jck-$tag-$r\w"
        $rep = Join-Path $env:TEMP "jck-$tag-$r\r"
        New-Item -ItemType Directory -Force -Path $w, $rep | Out-Null
        $msg = "=== [$tag pass $r/$RUNS] jtreg -jdk:$jdk ==="
        Write-Host $msg
        Add-Content -Path $LOG -Value $msg
        $jtArgs = @("-jar", $JTREG_JAR, "-jdk:$jdk", "-w", $w, "-r", $rep,
                    "-conc:8", "-timeoutFactor:5", "-verbose:summary",
                    "-javacoption:-encoding UTF-8") + $dirs
        & $JTREG_JAVA @jtArgs 2>&1 | Tee-Object -FilePath $LOG -Append
    }
}

Log "=== JCK-testsuite Windows full run (RUNS=$RUNS each) ==="
Log "suite : $SUITE"
Log "JDK-8 : $JDK8"
Log "JDK-27: $JDK27"
Log "log   : $LOG"
Log ""

Log "=== versions (recorded for failure analysis) ==="
Run-Log "jtreg version (jar: $JTREG_JAR)" $JTREG_JAVA @("-jar", $JTREG_JAR, "-version")
Run-Log "java -version (JDK-8 under test)" (Join-Path $JDK8 "bin\java.exe") @("-version")
Run-Log "java -version (JDK-27 under test)" (Join-Path $JDK27 "bin\java.exe") @("-version")

# Windows OS + CPU identity (国产 x86 vs Intel/AMD attribution).
Log "--- Windows OS ---"
try {
    $os = Get-CimInstance Win32_OperatingSystem -ErrorAction Stop
    Log ("  " + $os.Caption + "  version " + $os.Version + "  build " + $os.BuildNumber)
} catch {
    Log ("  (Win32_OperatingSystem unavailable: " + $_.Exception.Message + ")")
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

Log "=== cleanup: move leftovers aside (fast) + delete in background ==="
# Remove-Item -Recurse over jck-* work dirs (hundreds of thousands of .jtr
# files) takes minutes on Windows. A rename into a trash dir is O(1), and a
# background job then deletes the trash while jtreg starts immediately.
$trash = Join-Path $env:TEMP ("_jck-trash-" + $STAMP)
# Collect candidates BEFORE creating $trash, so the new trash dir is not among them.
$jckDirs = @()
$jckDirs += @(Get-ChildItem -Path $env:TEMP -Directory -ErrorAction SilentlyContinue |
              Where-Object { $_.Name -like "jck-*" -or $_.Name -like "_jck-trash-*" })
# Defensive: also scan one level deeper (session-dir layout, matching
# collect-windows-failures.ps1's two-level scan).
foreach ($top in (Get-ChildItem -Path $env:TEMP -Directory -ErrorAction SilentlyContinue)) {
    $jckDirs += @(Get-ChildItem -Path $top.FullName -Directory -ErrorAction SilentlyContinue |
                  Where-Object { $_.Name -like "jck-*" })
}
New-Item -ItemType Directory -Force -Path $trash | Out-Null
$moved = 0
foreach ($d in ($jckDirs | Sort-Object FullName -Unique)) {
    $moved++
    Move-Item -LiteralPath $d.FullName -Destination (Join-Path $trash ("jck-" + $moved)) -Force -ErrorAction SilentlyContinue
}
Log ("[cleanup] moved " + $moved + " leftover dir(s) into " + $trash)
if ($moved -gt 0) {
    Start-Job -ScriptBlock { param($p) Remove-Item -LiteralPath $p -Recurse -Force -ErrorAction SilentlyContinue } -ArgumentList $trash | Out-Null
    Log "[cleanup] background delete started (jtreg starts now)"
} else {
    Remove-Item -LiteralPath $trash -Force -ErrorAction SilentlyContinue
}
# Small files next to this script (old summary logs + collect text/zip) delete
# fast synchronously.
foreach ($f in (Get-ChildItem -Path $PSScriptRoot -Filter "windows-*.summary.log" -ErrorAction SilentlyContinue)) {
    if ($f.FullName -eq $LOG) { continue }
    Remove-Item $f.FullName -Force -ErrorAction SilentlyContinue
    Log ("[cleanup] removed old summary log: " + $f.Name)
}
foreach ($a in @("windows-jtr-summary.txt", "windows-jtr-detail.txt", "windows-jtr.zip")) {
    Remove-Item (Join-Path $PSScriptRoot $a) -ErrorAction SilentlyContinue
}
if (Test-Path (Join-Path $PSScriptRoot "windows-jtr")) {
    Remove-Item (Join-Path $PSScriptRoot "windows-jtr") -Recurse -Force -ErrorAction SilentlyContinue
    Log "[cleanup] removed old collect output dir windows-jtr\"
}
Log ""

# ---- run sequentially (shared machine: safer) ----
Invoke-Tree "JDK-8"  $JDK8  "jdk8"
Invoke-Tree "JDK-27" $JDK27 "jdk27"

Log "Done. Summary log: $LOG"
