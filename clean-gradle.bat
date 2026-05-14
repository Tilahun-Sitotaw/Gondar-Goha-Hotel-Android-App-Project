@echo off
REM Clean Gradle Cache Script - Run this if you get gradle errors

echo Stopping Gradle Daemons...
taskkill /F /IM java.exe /T 2>nul

echo Clearing Gradle Caches...
rmdir /s /q "%USERPROFILE%\.gradle\caches" 2>nul
rmdir /s /q "%USERPROFILE%\.gradle\daemon" 2>nul
rmdir /s /q "%USERPROFILE%\.gradle\wrapper" 2>nul

echo Clearing Project Caches...
rmdir /s /q ".gradle" 2>nul
rmdir /s /q "app\build" 2>nul
rmdir /s /q "build" 2>nul

echo.
echo Gradle cache cleaned successfully!
echo.
echo Next steps:
echo 1. Close Android Studio
echo 2. Wait 10 seconds
echo 3. Reopen Android Studio
echo 4. Click Build > Rebuild Project
echo.
pause
