<#
.SYNOPSIS
  Collect jtreg failure details (.jtr) from a completed Windows full run into
  a single text file + a zip, so they can be scp'd back to hanqinhu for analysis.

.DESCRIPTION
  The full runner (run-windows-full.ps1) writes each test's result to a .jtr file
  under $env:TEMP\<session>\jck-*\w\**.  This script finds those .jtr files,
  keeps only the ones whose execStatus is Failed or Error, and concatenates the
  relevant sections into one file, then zips the raw .jtr files.

  Output (written next to this script, i.e. under <repo root>\windows\):
    windows-jtr-summary.txt   - one line per failed test: path + execStatus
    windows-jtr-detail.txt    - full .jtr content for every failed test
    windows-jtr.zip           - raw .jtr files, tree preserved

  After it finishes, copy windows-jtr-summary.txt / windows-jtr-detail.txt /
  windows-jtr.zip back to hanqinhu (e.g. MobaXterm scp) under /data/yansendao/tmp/.
#>

$ErrorActionPreference = "Continue"

$OUT   = $PSScriptRoot                                  # <repo root>\windows
$detail = Join-Path $OUT "windows-jtr-detail.txt"
$summ   = Join-Path $OUT "windows-jtr-summary.txt"
$zip    = Join-Path $OUT "windows-jtr.zip"
$stage  = Join-Path $OUT "windows-jtr"

Remove-Item $detail, $summ, $zip -ErrorAction SilentlyContinue
if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
New-Item -ItemType Directory -Force -Path $stage | Out-Null

# Find all work dirs from previous runs (they live under $env:TEMP\<session>\jck-*\w)
$workDirs = Get-ChildItem -Path $env:TEMP -Directory -Recurse -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -eq "w" -and $_.Parent.Name -like "jck-*" }

if (-not $workDirs) {
    Write-Host "[error] no jtreg work dirs found under $env:TEMP (jck-*\w)."
    Write-Host "        Run run-windows-full.ps1 first, or point me at the temp dir."
    exit 1
}

$failed = @()
foreach ($wd in $workDirs) {
    $jtrs = Get-ChildItem -Path $wd.FullName -Filter "*.jtr" -Recurse -ErrorAction SilentlyContinue
    foreach ($j in $jtrs) {
        $content = Get-Content -Path $j.FullName -Raw -ErrorAction SilentlyContinue
        if ($content -match "execStatus=(Failed|Error)") {
            $failed += [pscustomobject]@{
                Jtr     = $j
                Status  = $matches[1]
                Content = $content
                Rel     = $j.FullName.Substring($wd.FullName.Length).TrimStart('\','/')
            }
        }
    }
}

if ($failed.Count -eq 0) {
    Write-Host "[info] no failed/error .jtr found in $($workDirs.Count) work dir(s)."
    exit 0
}

$failed = $failed | Sort-Object Rel

# summary: one line per failed test
$failed | ForEach-Object { "{0}`t{1}`t{2}" -f $_.Status, $_.Rel, ($_.Jtr.DirectoryName) } |
    Set-Content -Path $summ -Encoding UTF8

# detail: full .jtr content, separated
$sb = New-Object System.Text.StringBuilder
foreach ($f in $failed) {
    [void]$sb.AppendLine("========== " + $f.Rel + "  [execStatus=" + $f.Status + "] ==========")
    [void]$sb.AppendLine($f.Content)
    [void]$sb.AppendLine("")
}
[System.IO.File]::WriteAllText($detail, $sb.ToString(), [System.Text.Encoding]::UTF8)

# stage raw .jtr for zipping (preserve relative path)
foreach ($f in $failed) {
    $dest = Join-Path $stage $f.Rel
    $destDir = Split-Path $dest -Parent
    New-Item -ItemType Directory -Force -Path $destDir | Out-Null
    Copy-Item -Path $f.Jtr.FullName -Destination $dest -Force
}
Compress-Archive -Path (Join-Path $stage "*") -DestinationPath $zip -Force

Write-Host "done: failed/error tests = $($failed.Count)"
Write-Host "  summary : $summ"
Write-Host "  detail  : $detail"
Write-Host "  zip     : $zip"
Write-Host ""
Write-Host "copy these back to hanqinhu (MobaXterm scp) under /data/yansendao/tmp/"
