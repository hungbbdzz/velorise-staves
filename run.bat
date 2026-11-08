@echo off
setlocal
cd /d "%~dp0"

set "CHOICE=%~1"
if not "%CHOICE%"=="" goto :HANDLE_CHOICE

:MENU
cls
echo =====================================================================
echo                VELORISE'S STAVES - CLIENT LAUNCHER
echo =====================================================================
echo   1) NeoForge 1.21.1  (Java 21)
echo.
echo   0) Exit
echo ---------------------------------------------------------------------
set /p "CHOICE=Select a loader/version [1]: "

:HANDLE_CHOICE
if "%CHOICE%"=="1" (
    call "%~dp0scripts\run-neoforge.bat"
    exit /b %ERRORLEVEL%
)

if "%CHOICE%"=="0" exit /b 0

echo Invalid selection: "%CHOICE%"
timeout /t 2 >nul
goto :MENU
