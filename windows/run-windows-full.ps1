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
$RequestedConc = if ($env:JCK_CONC) { [int]$env:JCK_CONC } else { 16 }
$Concurrency = $RequestedConc

# ---- heap sizing from physical memory (standing rule) ----
# Each jtreg slot can have ONE test VM and ONE javac alive at the same time, so
# a slot commits TestHeap + JavacHeap.  Give the test VM ~1/4 of the per-slot
# physical share and javac ~1/8.  -Xms = -Xmx and AlwaysPreTouch on the test VM
# keep GC/JIT timing stable (the sweep exists to compare configs).
#
# On this box the JVMs' Windows default max heap is physical/4 = 16 GB each;
# 16 slots of that exhausts the commit limit (RAM + pagefile) and kills the
# run with native OOM (malloc failed / "unable to create new native thread").
$MemMB = [int]((Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory / 1MB)
$TestHeapMB  = [int][math]::Max(512, [math]::Min(4096, $MemMB / $RequestedConc / 4))
$JavacHeapMB = [int][math]::Max(512, [math]::Min(2048, $MemMB / $RequestedConc / 8))

# The machine is shared: other sessions can hold most of the commit charge, so
# also trim concurrency to what the CURRENT commit headroom can carry (use 60%
# of it, keeping the rest for the OS and other users).
$FreeCommitMB = 0
try { $FreeCommitMB = [int]((Get-CimInstance Win32_OperatingSystem).FreeVirtualMemory / 1KB) } catch { }
$SlotMB = $TestHeapMB + $JavacHeapMB
$ConcNote = ""
if ($FreeCommitMB -gt 0) {
    $Fit = [int][math]::Floor(($FreeCommitMB * 0.6) / $SlotMB)
    if ($Fit -lt $Concurrency) {
        $Concurrency = [math]::Max(1, $Fit)
        $ConcNote = " (trimmed from ${RequestedConc}: only ${FreeCommitMB} MB commit free)"
    }
}

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

# Start each run with a fresh log.  Log() appends, so without this a rerun's
# output would pile onto the previous run's — and a previous run that died
# mid-tree would leave a truncated log that later analysis could mistake for
# this run's.  The old copy is kept as console.prev.log for reference.
if (Test-Path $LOG) { Move-Item -LiteralPath $LOG -Destination (Join-Path $resDir "console.prev.log") -Force }

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

# .gitattributes forces eol=lf for *.jasm/*.jcod because asmtools' jasm is
# CRLF-sensitive: a CRLF file dies with `"<}>" token is expected` at the end of
# a line.  A clone made BEFORE those attributes were added keeps CRLF, and
# `git pull` never re-checkouts existing files, so normalise in place here.
# Byte-level CR-before-LF removal is idempotent and cannot corrupt content.
function Repair-JasmEol([string]$root) {
    $files = @(& git -C $root ls-files -- '*.jasm' '*.jcod' 2>$null)
    $fixed = 0
    foreach ($f in $files) {
        $p = Join-Path $root ($f -replace '/', '\')
        if (-not (Test-Path $p)) { continue }
        $b = [System.IO.File]::ReadAllBytes($p)
        $hasCrlf = $false
        for ($i = 0; $i -lt $b.Length - 1; $i++) {
            if ($b[$i] -eq 13 -and $b[$i + 1] -eq 10) { $hasCrlf = $true; break }
        }
        if (-not $hasCrlf) { continue }
        $o = New-Object 'System.Collections.Generic.List[byte]'
        for ($i = 0; $i -lt $b.Length; $i++) {
            if (($b[$i] -eq 13) -and (($i + 1) -lt $b.Length) -and ($b[$i + 1] -eq 10)) { continue }
            $o.Add($b[$i])
        }
        [System.IO.File]::WriteAllBytes($p, $o.ToArray())
        $fixed++
    }
    return $fixed
}

# Parse jtreg's -verbose:fail,error output into failing tests.  Reading the
# failure list off the console (rather than only from report/text/summary.txt)
# is what makes a HALF-DEAD run visible: when jtreg dies (native OOM, killed
# process) it never writes summary.txt, and a summary-only reader then reports
# "0 failures" for a run that actually crashed mid-tree.
#
# Returns objects: Test (path), Result (text after "Failed."/"Error."),
# Oom ($true when the failure block carries a memory-exhaustion signature).
function Parse-Failures([string[]]$lines) {
    $cur = $null
    $block = New-Object System.Collections.ArrayList
    $fails = New-Object System.Collections.ArrayList
    foreach ($l in $lines) {
        if ($l -match '^TEST:\s+(\S.*?)\s*$') {
            $cur = $matches[1]
            $block.Clear()
        } elseif ($l -match '^TEST RESULT:\s+(Failed|Error)\.\s*(.*)$') {
            $text = $matches[2] + "`n" + ($block -join "`n")
            $isOom = $text -match 'insufficient memory|Native memory allocation|OutOfMemoryError|unable to create new native thread|hs_err_pid|exit code: -107374'
            [void]$fails.Add([pscustomobject]@{ Test = $cur; Result = $matches[2]; Oom = [bool]$isOom })
        } else {
            [void]$block.Add($l)
        }
    }
    return @($fails)
}

# Heap/error-file tokens shared by the main run and the OOM retry pass.
function Get-HeapTokens([int]$testMB, [int]$javacMB, [string]$tag) {
    $errFile = Join-Path $env:TEMP ("hs_err_{0}_%p.log" -f $tag)
    return @(
        "-javaoption:-Xms${testMB}m",
        "-javaoption:-Xmx${testMB}m",
        "-javaoption:-XX:+AlwaysPreTouch",
        "-javaoption:-XX:ErrorFile=$errFile",
        "-javacoption:-J-Xms${javacMB}m",
        "-javacoption:-J-Xmx${javacMB}m",
        "-javacoption:-J-XX:ErrorFile=$errFile"
    )
}

# Re-run each memory-failure test on its own, serially, with twice the heap.
# Under -conc:N a slot competes with 15 others for commit charge; run alone, the
# same test usually passes. A test that still OOMs 3 times alone is a real
# finding (and stays in failed.txt, since only passes are dropped there).
function Invoke-OomRetry([string]$tag, [string]$jdk, [string]$dir, [string[]]$tests) {
    $attempts = 3
    $retryHeap = [math]::Min(4096, $TestHeapMB * 2)
    $lines = @()
    $lines += ("=== OOM retry: {0} test(s), serial (-conc:1), heap {1}m x{2} ===" -f $tests.Count, $retryHeap, $attempts)

    foreach ($t in $tests) {
        $path = Join-Path $SUITE ($dir + "\" + ($t -replace '/', '\'))
        if (-not (Test-Path $path)) {
            $lines += ("MISSING  {0}" -f $t)
            continue
        }
        $ok = $false
        $lastResult = ""
        for ($a = 1; $a -le $attempts -and -not $ok; $a++) {
            $rw = Join-Path $env:TEMP ("jck-retry-{0}-{1}\w" -f $tag, $label)
            $rr = Join-Path $env:TEMP ("jck-retry-{0}-{1}\r" -f $tag, $label)
            New-Item -ItemType Directory -Force -Path $rw, $rr | Out-Null
            $rArgs = @("-jar", $JTREG_JAR, "-jdk:$jdk", "-w", $rw, "-r", $rr,
                       "-conc:1", "-timeoutFactor:5", "-verbose:fail,error",
                       "-javacoption:-encoding", "-javacoption:UTF-8") +
                     (Get-HeapTokens $retryHeap $JavacHeapMB $tag) + $flagTokens + @($path)
            $rout = @(& $JTREG_JAVA @rArgs 2>&1 | ForEach-Object { ([string]$_).Replace([string]([char]0), "") })
            $rf = @(Parse-Failures $rout)
            $lastResult = if ($rf.Count -gt 0) { $rf[0].Result } else { "pass" }
            # A pass needs an actual summary line with zero failures/errors: a
            # crashed run also has no failure lines and must not count as a pass.
            $rl = ($rout | Select-String 'Test results:' | Select-Object -Last 1).Line
            $ok = [bool]($rl -and ($rl -notmatch 'failed: [1-9]') -and ($rl -notmatch 'error: [1-9]'))
            Remove-Item -LiteralPath $rw -Recurse -Force -ErrorAction SilentlyContinue
            Remove-Item -LiteralPath $rr -Recurse -Force -ErrorAction SilentlyContinue
        }
        if ($ok) {
            $lines += ("PASS     {0}  (attempt {1}; removed from failed.txt)" -f $t, ($a - 1))
        } else {
            $lines += ("STILL-FAIL {0}  ({1})" -f $t, $lastResult)
        }
    }

    # A test that passed alone was only a contention casualty: drop it from the
    # failure list so .new.txt (delta vs baseline) does not carry phantom rows.
    $passedAlone = @($lines | Where-Object { $_ -like "PASS *" } |
                     ForEach-Object { ($_ -split '\s+')[1] })
    if ($passedAlone.Count -gt 0) {
        $failPath = Join-Path $resDir "$tag.failed.txt"
        $rest = @(Get-Content $failPath -ErrorAction SilentlyContinue |
                  Where-Object { $passedAlone -notcontains $_ })
        Write-Lines $failPath $rest
    }

    $retryPath = Join-Path $resDir "$tag.oom-retry.txt"
    Write-Lines $retryPath $lines
    $lines | ForEach-Object { Log ("  [retry] " + $_) }
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
                "-javacoption:-encoding", "-javacoption:UTF-8") +
              (Get-HeapTokens $TestHeapMB $JavacHeapMB $tag) + $flagTokens + $dirs
    # jtreg's verbose stdout is UTF-16LE on this box; PowerShell decodes it
    # single-byte, so each ASCII char is followed by a NUL. Strip the NUL fill
    # bytes before writing/parsing (see the encoding note above).
    $out = @(& $JTREG_JAVA @jtArgs 2>&1 | ForEach-Object { ([string]$_).Replace([string]([char]0), "") })
    if ($out.Count -gt 0) {
        [System.IO.File]::AppendAllLines($LOG, [string[]]$out, $Utf8NoBom)
    }

    # Failed/error list from the verbose stream (see Parse-Failures), with the
    # report summary as a fallback for the (unusual) case where the verbose
    # lines were not emitted.
    $parsed = @(Parse-Failures $out)
    $failed = @($parsed | Where-Object { $_.Test } | ForEach-Object { $_.Test } | Sort-Object -Unique)
    $summary = Join-Path $rep "text\summary.txt"
    if (Test-Path $summary) {
        $fromSummary = @(Get-Content $summary |
                         Where-Object { $_ -match ' (Failed|Error)\. ' } |
                         ForEach-Object { ($_ -split '\s+')[0] });
        $failed = @(@($failed) + @($fromSummary) | Sort-Object -Unique)
    }
    Write-Lines (Join-Path $resDir "$tag.failed.txt") $failed

    # OOM-prone failures get a second, serial chance after the tree finishes.
    $oomTests = @($parsed | Where-Object { $_.Oom -and $_.Test } |
                  ForEach-Object { $_.Test } | Sort-Object -Unique)
    if ($oomTests.Count -gt 0) {
        Log ("[{0}] {1} failure(s) carry a memory-exhaustion signature -> serial retry" -f $tag, $oomTests.Count)
        Invoke-OomRetry $tag $jdk $dir $oomTests
    }

    $countsLine = ($out | Select-String 'Test results:' | Select-Object -Last 1).Line
    if ($countsLine) {
        Write-Lines (Join-Path $resDir "$tag.counts.txt") @($countsLine)
    } else {
        # No summary line => jtreg never reached the end of the tree. Say so
        # loudly; an empty failed.txt must never be read as "all passed".
        $msg = ("INCOMPLETE: jtreg produced no 'Test results:' line (run died); " +
                "failures recorded: {0}" -f @($failed).Count)
        Write-Lines (Join-Path $resDir "$tag.counts.txt") @($msg)
        Log ("[!!] " + $tag + ": " + $msg)
    }

    # HotSpot crash dumps land in the jtreg process CWD (the repo root) when a
    # javac/test VM dies; they are not part of the suite.
    Get-ChildItem -Path $SUITE -Filter "replay_pid*.log" -ErrorAction SilentlyContinue |
        Remove-Item -Force -ErrorAction SilentlyContinue
    Get-ChildItem -Path $SUITE -Filter "hs_err_pid*.log" -ErrorAction SilentlyContinue |
        Remove-Item -Force -ErrorAction SilentlyContinue

    # Failure details already went to console.log; drop work + report dirs so
    # per-test .jtr/.class small files don't accumulate.
    Remove-Item -LiteralPath $w   -Recurse -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $rep -Recurse -Force -ErrorAction SilentlyContinue
}

Log "=== JCK-testsuite Windows sweep: label=$label flags=[$($JvmArgs -join ' ')] ==="
Log "suite : $SUITE"
Log "JDK-8 : $JDK8"
Log "JDK-27: $JDK27"
Log ("memory: ${MemMB} MB RAM; commit free ${FreeCommitMB} MB")
Log ("heap  : test VM -Xms/-Xmx ${TestHeapMB}m +AlwaysPreTouch; javac ${JavacHeapMB}m; conc=$Concurrency$ConcNote")

# asmtools needs LF in .jasm/.jcod (see Repair-JasmEol); fix a stale clone.
$eolFixed = Repair-JasmEol $SUITE
if ($eolFixed -gt 0) {
    Log ("[eol] normalised CRLF -> LF in $eolFixed jasm/jcod file(s) (asmtools is CRLF-sensitive)")
}
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
