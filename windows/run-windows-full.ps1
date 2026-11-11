<#
.SYNOPSIS
  JCK-testsuite Windows full runner: run jdk-8 / jdk-27 each RUNS times (API+JLS+JVMS).

.DESCRIPTION
  Assumes this script sits at the root of a git clone of
  git@github.com:sendaoYan/JCK-testsuite.git (next to JDK-8\ and JDK-27\).
  jtreg is expected under D:\yansendao\JDK\jtreg (jtreg.jar found recursively).
  The two JDKs are expected under D:\yansendao\JDK.

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

$SUITE      = $PSScriptRoot                             # git clone root (has JDK-8\, JDK-27\)
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
$LOG   = Join-Path $SUITE "windows-$STAMP.summary.log"

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
                    "-conc:8", "-timeoutFactor:5", "-verbose:summary") + $dirs
        & $JTREG_JAVA @jtArgs 2>&1 | Tee-Object -FilePath $LOG -Append
    }
}

Write-Host "=== JCK-testsuite Windows full run (RUNS=$RUNS each) ==="
Write-Host "suite : $SUITE"
Write-Host "JDK-8 : $JDK8"
Write-Host "JDK-27: $JDK27"
Write-Host "log   : $LOG"
Add-Content -Path $LOG -Value "=== JCK-testsuite Windows full run ==="
Add-Content -Path $LOG -Value "suite : $SUITE"
Add-Content -Path $LOG -Value "JDK-8 : $JDK8"
Add-Content -Path $LOG -Value "JDK-27: $JDK27"

# ---- run sequentially (shared machine: safer) ----
Invoke-Tree "JDK-8"  $JDK8  "jdk8"
Invoke-Tree "JDK-27" $JDK27 "jdk27"

Write-Host "Done. Summary log: $LOG"
