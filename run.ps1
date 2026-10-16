# ==============================================================================
# FitCore Pro - PowerShell Launcher Script
# ==============================================================================

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Starting FitCore Pro - Fitness Management System" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan

$javaExe = "java"
if ($env:JAVA_HOME) {
    $javaExe = "$env:JAVA_HOME\bin\java.exe"
} elseif (Test-Path "C:\Users\abhib\.tools\jdk-21.0.12.1+1\bin\java.exe") {
    $javaExe = "C:\Users\abhib\.tools\jdk-21.0.12.1+1\bin\java.exe"
}

$jarPath = "target\fitcore-pro-1.0.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "[!] target\fitcore-pro-1.0.0.jar not found. Compiling package..." -ForegroundColor Yellow
    mvn clean package -DskipTests
}

Write-Host "[*] Launching FitCore Pro Swing Desktop Interface..." -ForegroundColor Cyan
Start-Process -FilePath $javaExe -ArgumentList "-jar", $jarPath
Write-Host "[+] Application process started successfully." -ForegroundColor Green
