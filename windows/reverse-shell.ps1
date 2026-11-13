<#
.SYNOPSIS
  Command server over an SSH reverse tunnel, so hanqinhu can run commands on
  this Windows box (which normally only initiates outbound SSH, e.g. VSCode
  Remote-SSH).

.DESCRIPTION
  Run once on Windows and keep this window open:

      powershell -ExecutionPolicy Bypass -File reverse-shell.ps1

  It (1) opens an SSH reverse tunnel <sshHost>:<port> -> 127.0.0.1:<port> and
  (2) listens on 127.0.0.1:<port>.  Each accepted connection reads ONE line,
  runs it with `cmd.exe /c <line>`, writes the captured stdout+stderr back, and
  closes.  No state is kept between connections, so use absolute paths or chain
  with `&&`/`&`.

  From hanqinhu, run one command:

      printf 'dir D:\\path\r\n' | nc -q 2 127.0.0.1 <port>

  (the trailing \r\n ends the command line; `-q 2` closes nc's side after 2s).
  Long-running jobs should be started detached in a NEW window, e.g.
      start "" /min cmd /c "powershell -File ... > out.log 2>&1"
  then poll the log with `type out.log`.  (`start /b` shares this server's
  console and, despite the temp-file capture below, is best avoided.)

.PARAMETER SshHost
  The host alias VSCode Remote-SSH uses for hanqinhu (default "hanqinhu").

.PARAMETER Port
  The tunneled port on both sides (default 4444).
#>
param(
    [string]$SshHost = "hanqinhu",
    [int]$Port = 4444
)

$ErrorActionPreference = "Continue"

# --- 1) reverse tunnel (background): sshHost:port -> 127.0.0.1:port ---
# Kept in a script block so the tunnel can be restarted when ssh drops (a
# dropped tunnel leaves the listener up but unreachable, which looks exactly
# like "the Windows box went away").
$tunnelBlock = {
    param($h, $p)
    ssh -N -o ExitOnForwardFailure=yes -o ServerAliveInterval=30 -R "${p}:127.0.0.1:${p}" $h
}
$tunnel = Start-Job -ArgumentList $SshHost, $Port -ScriptBlock $tunnelBlock
Write-Host "Reverse tunnel job started: ssh -R ${Port}:127.0.0.1:${Port} $SshHost"

# --- 2) single-threaded command server on 127.0.0.1:port ---
$listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $Port)
$listener.Start()
Write-Host "Command server listening on 127.0.0.1:$Port (one line = one cmd command)"

# cmd.exe writes its output in the OEM code page (GBK on Chinese Windows,
# 437/850 elsewhere). Decode the captured temp file with it so `dir`/`type`/
# `tasklist` text comes back intact; the reply itself is re-encoded UTF-8.
$Oem = [System.Text.Encoding]::GetEncoding([int](Get-Culture).TextInfo.OEMCodePage)

# The `*>` redirection above is performed by PowerShell, not by cmd.exe, and
# PS 5.1's default file encoding is Unicode (UTF-16LE) — so the temp file may be
# UTF-16LE, UTF-8 with BOM, or raw OEM bytes depending on how the text got there.
# Decode by BOM first and fall back to OEM (stripping any stray NULs, which are
# what a UTF-16LE file decoded as OEM leaves behind).
function Read-Captured([string]$path) {
    $b = [System.IO.File]::ReadAllBytes($path)
    if ($b.Length -ge 2 -and $b[0] -eq 0xFF -and $b[1] -eq 0xFE) {
        return [System.Text.Encoding]::Unicode.GetString($b, 2, $b.Length - 2)
    }
    if ($b.Length -ge 3 -and $b[0] -eq 0xEF -and $b[1] -eq 0xBB -and $b[2] -eq 0xBF) {
        return [System.Text.Encoding]::UTF8.GetString($b, 3, $b.Length - 3)
    }
    return $Oem.GetString($b).Replace([string]([char]0), "")
}

while ($true) {
    # A dead tunnel means the listener is reachable only from this box; restart
    # it before taking the next command so the session does not silently rot.
    if ($tunnel.State -ne 'Running') {
        Write-Host ("[warn] reverse tunnel job is {0}; restarting" -f $tunnel.State)
        $tunnel = Start-Job -ArgumentList $SshHost, $Port -ScriptBlock $tunnelBlock
    }
    $client = $listener.AcceptTcpClient()
    $stream = $client.GetStream()
    # The server is single-threaded: a client that connects and never sends a
    # newline would block every later command, so time the read out.
    $stream.ReadTimeout = 60000
    $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
    try {
        $cmd = $reader.ReadLine()
    } catch {
        $cmd = $null
        Write-Host ("[warn] no command line received: " + $_.Exception.Message)
    }
    if ($cmd -and $cmd.Trim().Length -gt 0) {
        $out = ""
        $outFile = [System.IO.Path]::GetTempFileName()
        try {
            # Capture to a temp FILE, not a PowerShell pipe: a `start`-launched
            # background process that inherits stdout would otherwise hold a
            # pipe open and block this whole single-threaded server until that
            # background process exits. A file handle never blocks the read.
            cmd.exe /c $cmd *> $outFile
            $out = Read-Captured $outFile
        } catch {
            $out = "[PS-ERROR] $_"
        } finally {
            Remove-Item $outFile -Force -ErrorAction SilentlyContinue
        }
        try {
            $bytes = [System.Text.Encoding]::UTF8.GetBytes($out)
            $stream.Write($bytes, 0, $bytes.Length)
            $stream.Flush()
        } catch { }
    }
    $reader.Close()
    $stream.Close()
    $client.Close()
}
