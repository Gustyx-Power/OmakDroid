@echo off
setlocal

echo =======================================================
echo [OmakDroid] Systems Engineering - Debug Build Pipeline
echo =======================================================

echo.
echo [PHASE 1/4] Gradle Wrapper Setup
echo -------------------------------------------------------
if not exist "gradlew.bat" (
    echo [*] Gradle wrapper not found. Generating wrapper...
    where gradle >nul 2>nul
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] Gradle is not installed or not in PATH.
        echo Please install Gradle from https://gradle.org/install/
        goto :error
    )
    call gradle wrapper --gradle-version=8.5 --stacktrace
    if %ERRORLEVEL% neq 0 goto :error
    echo [*] Gradle wrapper generated successfully.
) else (
    echo [*] Gradle wrapper found.
)

echo.
echo [PHASE 2/4] Gradle Sync - Dependency Resolution
echo -------------------------------------------------------
echo [*] Syncing project dependencies and configurations...
call gradlew.bat --refresh-dependencies --stacktrace
if %ERRORLEVEL% neq 0 goto :error

echo.
echo [PHASE 3/4] Gradle Indexing - Build Model Generation
echo -------------------------------------------------------
echo [*] Generating build models and task graphs...
call gradlew.bat tasks --all --stacktrace
if %ERRORLEVEL% neq 0 goto :error

echo [*] Resolving all configurations for indexing...
call gradlew.bat dependencies --configuration debugCompileClasspath --stacktrace
if %ERRORLEVEL% neq 0 goto :error

echo.
echo [PHASE 4/4] Debug Build Execution
echo -------------------------------------------------------
echo [*] Triggering Gradle build sequence: assembleDebug...
call gradlew.bat assembleDebug --stacktrace

if %ERRORLEVEL% neq 0 goto :error

echo.
echo =======================================================
echo [SUCCESS] OmakDroid Debug APK generated successfully!
echo =======================================================
echo [PATH] app\build\outputs\apk\debug\app-debug.apk
echo.
echo [*] Optional: To install via ADB, run:
echo     adb install -r -d app\build\outputs\apk\debug\app-debug.apk
echo.
pause
endlocal
exit /b 0

:error
echo.
echo =======================================================
echo [FATAL ERROR] Build pipeline failed with code %ERRORLEVEL%
echo =======================================================
echo Please review the stack trace above to diagnose the fault.
echo.
pause
endlocal
exit /b %ERRORLEVEL%
