<#
.SYNOPSIS
  Reverse shell over an SSH reverse tunnel, so hanqinhu can reach this Windows
  box (which normally only initiates outbound SSH, e.g. VSCode Remote-SSH).

.DESCRIPTION
  Run once on Windows and keep this window open:

      powershell -ExecutionPolicy Bypass -File reverse-shell.ps1

  It (1) starts a PowerShell listener on 127.0.0.1:<port> and (2) opens an SSH
  reverse tunnel  <sshHost>:<port> -> 127.0.0.1:<port>, i.e. a port on hanqinhu
  that forwards back to this listener.

  From hanqinhu, to get a PowerShell prompt on this Windows box:

      nc 127.0.0.1 <port>

  Type "exit" to end one shell; connect again for another.  Ctrl-C here stops
  the listener (and, on a clean close, the tunnel job).

.PARAMETER SshHost
  The host alias VSCode Remote-SSH uses for hanqinhu (default "hanqinhu").
  Override with -SshHost <your-host> if your SSH config names it differently.

.PARAMETER Port
  The tunneled port on both sides (default 4444).
#>
param(
    [string]$SshHost = "hanqinhu",
    [int]$Port = 4444
)

$ErrorActionPreference = "Stop"

# --- 1) reverse tunnel (background): sshHost:port -> 127.0.0.1:port ---
# -o ExitOnForwardFailure=yes makes ssh fail fast if the port cannot be bound;
# -o ServerAliveInterval=30 keeps the link alive through idle periods.
$tunnel = Start-Job -ArgumentList $SshHost, $Port -ScriptBlock {
    param($h, $p)
    ssh -N -o ExitOnForwardFailure=yes -o ServerAliveInterval=30 -R "${p}:127.0.0.1:${p}" $h
}
Write-Host "Reverse tunnel job started: ssh -R ${Port}:127.0.0.1:${Port} $SshHost"

Start-Sleep -Seconds 2
$tj = Get-Job -Id $tunnel.Id
if ($tj.State -eq "Failed") {
    Write-Host "[tunnel] ssh failed:"
    Receive-Job -Id $tunnel.Id
}

# --- 2) PowerShell listener on 127.0.0.1:port ---
$listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $Port)
$listener.Start()
Write-Host "Listening on 127.0.0.1:$Port"
Write-Host "On hanqinhu run:  nc 127.0.0.1 $Port"
Write-Host "(Ctrl-C to stop)"

while ($true) {
    $client = $listener.AcceptTcpClient()
    Write-Host "-> connection from $($client.Client.RemoteEndPoint)"
    $stream = $client.GetStream()

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = "$env:WINDIR\System32\WindowsPowerShell\v1.0\powershell.exe"
    $psi.Arguments = "-NoProfile -ExecutionPolicy Bypass -NoLogo"
    $psi.RedirectStandardInput  = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError  = $true
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow  = $true
    $proc = [System.Diagnostics.Process]::Start($psi)

    # Pump process stdout/stderr -> socket, and socket -> process stdin, on
    # three background threads so input and output flow concurrently.
    $outTask = [System.Threading.Tasks.Task]::Run([System.Action]{
        $proc.StandardOutput.BaseStream.CopyTo($stream)
    })
    $errTask = [System.Threading.Tasks.Task]::Run([System.Action]{
        $proc.StandardError.BaseStream.CopyTo($stream)
    })
    $inTask = [System.Threading.Tasks.Task]::Run([System.Action]{
        try { $stream.CopyTo($proc.StandardInput.BaseStream) } catch { }
        try { $proc.StandardInput.Close() } catch { }
    })

    $proc.WaitForExit()
    try { $outTask.Wait() } catch { }
    try { $errTask.Wait() } catch { }
    try { $inTask.Wait() } catch { }
    $stream.Close()
    $client.Close()
    Write-Host "<- shell exited"
}
