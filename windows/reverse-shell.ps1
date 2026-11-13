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
  Long-running jobs should be started detached, e.g.
      start /b cmd /c "powershell -File ... > out.log 2>&1"
  then poll the log with `type out.log`.

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
$tunnel = Start-Job -ArgumentList $SshHost, $Port -ScriptBlock {
    param($h, $p)
    ssh -N -o ExitOnForwardFailure=yes -o ServerAliveInterval=30 -R "${p}:127.0.0.1:${p}" $h
}
Write-Host "Reverse tunnel job started: ssh -R ${Port}:127.0.0.1:${Port} $SshHost"

# --- 2) single-threaded command server on 127.0.0.1:port ---
$listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $Port)
$listener.Start()
Write-Host "Command server listening on 127.0.0.1:$Port (one line = one cmd command)"

while ($true) {
    $client = $listener.AcceptTcpClient()
    $stream = $client.GetStream()
    $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
    $cmd = $reader.ReadLine()
    if ($cmd -and $cmd.Trim().Length -gt 0) {
        $out = ""
        try {
            $out = (& cmd.exe /c $cmd 2>&1 | Out-String)
        } catch {
            $out = "[PS-ERROR] $_"
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
