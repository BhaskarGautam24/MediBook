<#
.SYNOPSIS
    Stop all MediBook microservices
#>

$rootDir = $PSScriptRoot
if (-not $rootDir) { $rootDir = (Get-Location).Path }

$pidFile = Join-Path $rootDir "logs\.pids"

if (Test-Path $pidFile) {
    Write-Host "Stopping MediBook services..." -ForegroundColor Yellow
    Get-Content $pidFile | ForEach-Object {
        $parts = $_ -split "="
        $name = $parts[0]
        $pid = [int]$parts[1]
        try {
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
            Write-Host "  Stopped $name (PID $pid)" -ForegroundColor Green
        } catch {
            Write-Host "  $name (PID $pid) already stopped" -ForegroundColor DarkGray
        }
    }
    Remove-Item $pidFile -Force
} else {
    Write-Host "No PID file found. Killing all Java processes..." -ForegroundColor Yellow
    Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
}

Write-Host "All services stopped." -ForegroundColor Green
