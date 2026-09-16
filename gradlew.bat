@echo off
setlocal

where gradle >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
  echo Gradle is not installed or not on PATH. Install Gradle 9.1.1 and try again.
  exit /b 1
)

gradle %*
