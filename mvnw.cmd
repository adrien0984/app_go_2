@REM
@REM Maven wrapper script for Windows
@REM

@echo off
setlocal enabledelayedexpansion

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_HOME=%DIRNAME%

where mvn >nul 2>&1
if !ERRORLEVEL! EQU 0 (
    call mvn %*
    exit /b !ERRORLEVEL!
)

echo Error: Maven is not installed and not found in PATH
echo Please install Maven: https://maven.apache.org/download.cgi
exit /b 1
