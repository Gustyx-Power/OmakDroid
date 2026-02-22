@echo off
setlocal

echo =======================================================
echo [OmakDroid] Systems Engineering - Debug Build Pipeline
echo =======================================================

echo.
echo [*] Triggering Gradle build sequence: assembleDebug...
call gradlew.bat assembleDebug --stacktrace

if %ERRORLEVEL% equ 0 (
    echo.
    echo [SUCCESS] OmakDroid Debug APK generated successfully!
    echo [PATH] app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo [*] Optional: To install via ADB, run:
    echo     adb install -r -d app\build\outputs\apk\debug\app-debug.apk
) else (
    echo.
    echo [FATAL ERROR] Build pipeline failed with code %ERRORLEVEL%.
    echo Please review the stack trace above to diagnose the Rust/Kotlin integration fault.
)

echo.
pause
endlocal
