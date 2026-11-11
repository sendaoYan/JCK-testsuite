<#
.SYNOPSIS
  JCK-testsuite Windows runner for ONE set of JVM flags (a single GC/JIT sweep
  step). Run with no arguments for the default baseline; pass JVM flags to run
  the full suite (JDK-8 + JDK-27) under those flags.

.DESCRIPTION
  Every trailing argument is one JVM flag, propagated to the test VM via
  -javaoption:<flag>. Multiple flags = one combined config, e.g.:
      powershell -ExecutionPolicy Bypass -File run-windows-full.ps1 -Xcomp -XX:-TieredCompilation

  With no arguments it runs the baseline (no JVM flags). JDK-27 is skipped when
  the flags are JDK-8-only (-XX:+UseConcMarkSweepGC / -XX:+UseParNewGC, removed
  in JDK 14 and a hard "Unrecognized VM option" error on JDK-27).

  Failure details go to console.log via -verbose:fail,error,time; the work +
  report dirs are deleted after each tree (no .jtr sprawl).  Results land in
  windows-results\<label>\ (label derived from the flags; empty -> "baseline"):
  per-JDK <jdk8|jdk27>.failed.txt, .counts.txt, .new.txt (delta vs baseline).
  A one-line record is appended to windows-results\summary.txt.

  run-windows-full.bat drives the full 9-config sweep by invoking this script
  once per config (baseline first).
#>

param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$JvmArgs = @()
)
if ($null -eq $JvmArgs) { $JvmArgs = @() }

$ErrorActionPreference = "Continue"

# ---- configurable ----
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

# ---- derive label + JDK set from flags ----
$label = if ($JvmArgs.Count -eq 0) { "baseline" }
         else { (($JvmArgs -join '+') -replace '[^A-Za-z0-9.+-]', '_') }

$isJdk8Only = ($JvmArgs -contains "-XX:+UseConcMarkSweepGC") -or ($JvmArgs -contains "-XX:+UseParNewGC")

$trees = @(@{ tag = "jdk8";  jdk = $JDK8;  dir = "JDK-8" })
if (-not $isJdk8Only) {
    $trees += @{ tag = "jdk27"; jdk = $JDK27; dir = "JDK-27" }
}

$flagTokens = @($JvmArgs | ForEach-Object { "-javaoption:$_" })

$resRoot = Join-Path $PSScriptRoot "windows-results"
$resDir  = Join-Path $resRoot $label
New-Item -ItemType Directory -Force -Path $resDir | Out-Null

$LOG = Join-Path $resDir "console.log"

function Log([string]$text) {
    Write-Host $text
    Add-Content -Path $LOG -Value $text
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
    Add-Content -Path $LOG -Value $msg

    $jtArgs = @("-jar", $JTREG_JAR, "-jdk:$jdk", "-w", $w, "-r", $rep,
                "-conc:8", "-timeoutFactor:5", "-verbose:fail,error,time",
                "-javacoption:-encoding", "-javacoption:UTF-8") + $flagTokens + $dirs
    $out = & $JTREG_JAVA @jtArgs 2>&1 | Tee-Object -FilePath $LOG -Append

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

# ---- run the trees ----
foreach ($t in $trees) {
    Invoke-Tree $t.tag $t.jdk $t.dir
}

# ---- delta vs baseline ----
foreach ($t in $trees) {
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
function Count-File([string]$path) {
    if (Test-Path $path) { return @(Get-Content $path -ErrorAction SilentlyContinue).Count }
    return 0
}
$jdk8F = Count-File (Join-Path $resDir "jdk8.failed.txt")
$jdk8N = Count-File (Join-Path $resDir "jdk8.new.txt")
$jdk27F = Count-File (Join-Path $resDir "jdk27.failed.txt")
$jdk27N = Count-File (Join-Path $resDir "jdk27.new.txt")

$sumFile = Join-Path $resRoot "summary.txt"
if (-not (Test-Path $sumFile)) {
    Set-Content -Path $sumFile -Value ("{0,-40}`t{1,7}`t{2,7}`t{3,7}`t{4,7}" -f "label", "jdk8fail", "jdk8new", "jdk27fail", "jdk27new") -Encoding UTF8
}
Add-Content -Path $sumFile -Value ("{0,-40}`t{1,7}`t{2,7}`t{3,7}`t{4,7}" -f $label, $jdk8F, $jdk8N, $jdk27F, $jdk27N) -Encoding UTF8

Log ""
Log ("=== DONE label=$label  jdk8.failed=$jdk8F jdk8.new=$jdk8N jdk27.failed=$jdk27F jdk27.new=$jdk27N ===")
