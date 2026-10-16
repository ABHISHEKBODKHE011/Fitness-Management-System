@echo off
setlocal
title FitCore Pro - Fitness Management System
echo ===================================================
echo   Launching FitCore Pro Desktop Application...
echo ===================================================

REM Check for JAVA_HOME or tools path
if defined JAVA_HOME (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else if exist "C:\Users\abhib\.tools\jdk-21.0.12.1+1\bin\java.exe" (
    set "JAVA_EXE=C:\Users\abhib\.tools\jdk-21.0.12.1+1\bin\java.exe"
) else (
    set "JAVA_EXE=java"
)

if not exist "target\fitcore-pro-1.0.0.jar" (
    echo [ERROR] Application JAR not found. Please build the project first.
    pause
    exit /b 1
)

start "" "%JAVA_EXE%" -jar "target\fitcore-pro-1.0.0.jar"
endlocal
