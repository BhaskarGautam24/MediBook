<#
.SYNOPSIS
    MediBook Microservices — Local Dev Startup Script
.DESCRIPTION
    Builds and starts all 11 microservices in correct dependency order.
    MySQL must be running on localhost:3306.
    Usage: .\start-all.ps1
    Optional: .\start-all.ps1 -SkipBuild to skip Maven builds
#>

param(
    [switch]$SkipBuild
)

$ErrorActionPreference = "Continue"

$services = @(
    @{ Name = "config-server";       Port = 8888; Dir = "config-server";       Wait = 15 },
    @{ Name = "eureka-server";       Port = 8761; Dir = "eureka-server";       Wait = 15 },
    @{ Name = "api-gateway";         Port = 8080; Dir = "api-gateway";         Wait = 10 },
    @{ Name = "auth-service";        Port = 8081; Dir = "auth-service";        Wait = 10 },
    @{ Name = "provider-service";    Port = 8082; Dir = "provider-service";    Wait = 8  },
    @{ Name = "schedule-service";    Port = 8083; Dir = "schedule-service";    Wait = 8  },
    @{ Name = "notification-service";Port = 8087; Dir = "notification-service";Wait = 8  },
    @{ Name = "payment-service";     Port = 8085; Dir = "payment-service";     Wait = 8  },
    @{ Name = "appointment-service"; Port = 8084; Dir = "appointment-service"; Wait = 8  },
    @{ Name = "review-service";      Port = 8086; Dir = "review-service";      Wait = 8  },
    @{ Name = "record-service";      Port = 8088; Dir = "record-service";      Wait = 8  }
)

$rootDir = $PSScriptRoot
if (-not $rootDir) { $rootDir = (Get-Location).Path }

Write-Host " "
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MediBook Microservices Startup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " "

# Step 1: Build all services
if (-not $SkipBuild) {
    Write-Host "[BUILD] Building all services..." -ForegroundColor Yellow
    foreach ($svc in $services) {
        $svcDir = Join-Path $rootDir $svc.Dir
        if (Test-Path (Join-Path $svcDir "pom.xml")) {
            Write-Host "  Building $($svc.Name)..." -NoNewline
            $result = & mvn -f "$svcDir\pom.xml" package -DskipTests -q 2>&1
            if ($LASTEXITCODE -eq 0) {
                Write-Host " OK" -ForegroundColor Green
            } else {
                Write-Host " FAILED" -ForegroundColor Red
                Write-Host "  Error: $result" -ForegroundColor Red
            }
        }
    }
    Write-Host " "
}

# Step 2: Start services in order
$processes = @()
foreach ($svc in $services) {
    $svcDir = Join-Path $rootDir $svc.Dir
    $jarPattern = Join-Path $svcDir "target\*.jar"
    $jar = Get-ChildItem $jarPattern -ErrorAction SilentlyContinue | Where-Object { $_.Name -notlike "*-sources*" } | Select-Object -First 1

    if (-not $jar) {
        Write-Host "[SKIP] $($svc.Name) - no JAR found. Run without -SkipBuild" -ForegroundColor Red
        continue
    }

    # Kill any existing process on port
    $existing = Get-NetTCPConnection -LocalPort $svc.Port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($pid in $existing) {
        if ($pid -gt 0) {
            Write-Host "  Killing existing process on port $($svc.Port) (PID $pid)" -ForegroundColor DarkYellow
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
            Start-Sleep -Seconds 1
        }
    }

    Write-Host "[START] $($svc.Name) on :$($svc.Port)..." -ForegroundColor Green
    $logFile = Join-Path $rootDir "logs\$($svc.Name).log"
    New-Item -ItemType Directory -Path (Join-Path $rootDir "logs") -Force | Out-Null

    $proc = Start-Process -FilePath "java" -ArgumentList "--enable-preview", "-jar", $jar.FullName, "-Dserver.port=$($svc.Port)" -RedirectStandardOutput $logFile -RedirectStandardError "$logFile.err" -PassThru -WindowStyle Hidden

    $processes += @{ Name = $svc.Name; Process = $proc; Port = $svc.Port }

    Write-Host "  PID: $($proc.Id) | Log: logs\$($svc.Name).log" -ForegroundColor DarkGray
    Write-Host "  Waiting $($svc.Wait)s for startup..." -ForegroundColor DarkGray
    Start-Sleep -Seconds $svc.Wait
}

Write-Host " "
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All services started!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " "
Write-Host "  Config Server:  http://localhost:8888" -ForegroundColor White
Write-Host "  Eureka:         http://localhost:8761" -ForegroundColor White
Write-Host "  API Gateway:    http://localhost:8080" -ForegroundColor White
Write-Host "  Frontend:       http://localhost:5173" -ForegroundColor White
Write-Host " "
Write-Host "  Stop all: Get-Process java | Stop-Process" -ForegroundColor DarkGray
Write-Host " "

# Save PIDs for stop script
$processes | ForEach-Object {
    "$($_.Name)=$($_.Process.Id)"
} | Out-File (Join-Path $rootDir "logs\.pids") -Force
