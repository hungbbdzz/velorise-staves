@echo off
setlocal
cd /d "%~dp0"

echo Launching Velorise's Staves NeoForge 1.21.1 Client...
cd /d "%~dp0..\neoforge-1.21.1"

if defined JAVA21_HOME (
    set "JAVA_HOME=%JAVA21_HOME%"
    set "PATH=%JAVA21_HOME%\bin;%PATH%"
)

call gradlew.bat runClient
exit /b %ERRORLEVEL%
