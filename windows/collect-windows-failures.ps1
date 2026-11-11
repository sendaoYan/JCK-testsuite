<#
.SYNOPSIS
  Collect jtreg failure details (.jtr) from a completed Windows full run into
  a single text file + a zip, so they can be scp'd back to hanqinhu for analysis.

.DESCRIPTION
  The full runner (run-windows-full.ps1) writes each test's result to a .jtr file
  under $env:TEMP\jck-<tag>-<run>\w\<API|JLS|JVMS>\**.  This script finds those
  .jtr files, keeps only the ones whose execStatus is Failed or Error, and
  concatenates the relevant sections into one file, then zips the raw .jtr files.

  Output (written next to this script, i.e. under <repo root>\windows\):
    windows-jtr-summary.txt   - one line per failed test: path + execStatus
    windows-jtr-detail.txt    - full .jtr content for every failed test
    windows-jtr.zip           - raw .jtr files, tree preserved

  After it finishes, copy windows-jtr-summary.txt / windows-jtr-detail.txt /
  windows-jtr.zip back to hanqinhu (e.g. MobaXterm scp) under /data/yansendao/tmp/.

  NOTE: this script only scans the top-level jck-* work dirs directly under
  $env:TEMP (which is exactly where run-windows-full.ps1 creates them).  It does
  NOT recurse the whole Temp folder — doing so took hours on a Windows box whose
  %TEMP% has accumulated years of unrelated files.
#>

$ErrorActionPreference = "Continue"
$sw = [System.Diagnostics.Stopwatch]::StartNew()

$OUT    = $PSScriptRoot                                  # <repo root>\windows
$detail = Join-Path $OUT "windows-jtr-detail.txt"
$summ   = Join-Path $OUT "windows-jtr-summary.txt"
$zip    = Join-Path $OUT "windows-jtr.zip"
$stage  = Join-Path $OUT "windows-jtr"

Remove-Item $detail, $summ, $zip -ErrorAction SilentlyContinue
if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
New-Item -ItemType Directory -Force -Path $stage | Out-Null

# Work dirs are created by run-windows-full.ps1 directly under $env:TEMP as
# jck-<jdk8|jdk27>-<run>\w.  Only enumerate top-level jck-* dirs (no -Recurse).
Write-Host ("TEMP = " + $env:TEMP)
$topDirs = @(Get-ChildItem -Path $env:TEMP -Directory -ErrorAction SilentlyContinue |
             Where-Object { $_.Name -like "jck-*" })
Write-Host ("[info] top-level jck-* dirs under TEMP: " + $topDirs.Count)

$workDirs = @()
foreach ($d in $topDirs) {
    $w = Join-Path $d.FullName "w"
    if (Test-Path $w -PathType Container) { $workDirs += Get-Item $w }
}

if ($workDirs.Count -eq 0) {
    Write-Host "[error] no jck-*\w work dirs found under $env:TEMP."
    Write-Host "        Run run-windows-full.ps1 first, or point me at the temp dir."
    exit 1
}
Write-Host ("[info] work dirs to scan: " + $workDirs.Count)

# ---- phase 1: identify failed/error .jtr by header only (fast) ----
# jtreg stores execStatus= near the top of each .jtr, so reading 80 lines is enough;
# we only read the full file for the (few) failed tests in phase 2.
$trees = 'API', 'JLS', 'JVMS'
$failed = @()
foreach ($wd in $workDirs) {
    Write-Host ("[scan] " + $wd.FullName)
    $jtrs = foreach ($t in $trees) {
        $p = Join-Path $wd.FullName $t
        if (Test-Path $p) {
            Get-ChildItem -Path $p -Filter "*.jtr" -Recurse -File -ErrorAction SilentlyContinue
        }
    }
    foreach ($j in $jtrs) {
        $head = Get-Content -Path $j.FullName -TotalCount 80 -ErrorAction SilentlyContinue
        if ($null -eq $head) { continue }
        $headText = $head -join "`n"
        if ($headText -match 'execStatus=(Failed|Error)') {
            $failed += [pscustomobject]@{
                Jtr    = $j
                Status = $Matches[1]
                Rel    = $j.FullName.Substring($wd.FullName.Length).TrimStart('\', '/')
            }
        }
    }
}

if ($failed.Count -eq 0) {
    Write-Host "[info] no failed/error .jtr found in $($workDirs.Count) work dir(s)."
    exit 0
}
Write-Host ("[info] failed/error tests found: " + $failed.Count)

# ---- phase 2: read full content only for failed tests, write outputs ----
$failed = $failed | Sort-Object Rel

# summary: one line per failed test
$failed | ForEach-Object { "{0}`t{1}`t{2}" -f $_.Status, $_.Rel, ($_.Jtr.DirectoryName) } |
    Set-Content -Path $summ -Encoding UTF8

# detail: full .jtr content, separated
$sb = New-Object System.Text.StringBuilder
foreach ($f in $failed) {
    $content = Get-Content -Path $f.Jtr.FullName -Raw -ErrorAction SilentlyContinue
    [void]$sb.AppendLine("========== " + $f.Rel + "  [execStatus=" + $f.Status + "] ==========")
    [void]$sb.AppendLine($content)
    [void]$sb.AppendLine("")
}
[System.IO.File]::WriteAllText($detail, $sb.ToString(), [System.Text.Encoding]::UTF8)

# stage raw .jtr for zipping (preserve relative path)
foreach ($f in $failed) {
    $dest    = Join-Path $stage $f.Rel
    $destDir = Split-Path $dest -Parent
    New-Item -ItemType Directory -Force -Path $destDir | Out-Null
    Copy-Item -Path $f.Jtr.FullName -Destination $dest -Force
}
Compress-Archive -Path (Join-Path $stage "*") -DestinationPath $zip -Force

$sw.Stop()
Write-Host "done: failed/error tests = $($failed.Count), elapsed = $([math]::Round($sw.Elapsed.TotalSeconds))s"
Write-Host "  summary : $summ"
Write-Host "  detail  : $detail"
Write-Host "  zip     : $zip"
Write-Host ""
Write-Host "copy these back to hanqinhu (MobaXterm scp) under /data/yansendao/tmp/"
