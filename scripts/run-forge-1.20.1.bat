@echo off
setlocal
set "REPO_ROOT=%~dp0.."
cd /d "%REPO_ROOT%"

if defined JAVA17_HOME (
    set "JAVA_HOME=%JAVA17_HOME%"
) else if exist "C:\Program Files\Java\jdk-17\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-17"
) else if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
) else if exist "C:\Program Files\Eclipse Adoptium\jdk-17\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17"
) else (
    echo [Velorise's Staves] JDK 17 was not found.
    echo Set JAVA17_HOME to the JDK 17 folder and reopen this terminal.
    pause
    exit /b 1
)

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [Velorise's Staves] java.exe was not found at:
    echo %JAVA_HOME%\bin\java.exe
    pause
    exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"
echo [Velorise's Staves] Launching Forge 1.20.1 Client with Java 17...
cd /d "%REPO_ROOT%\forge-1.20.1"
call gradlew.bat runClient
exit /b %ERRORLEVEL%
