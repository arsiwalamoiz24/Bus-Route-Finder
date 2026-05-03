@echo off
echo Compiling project...
javac busfinder/*.java busfinder/data/*.java busfinder/gui/*.java busfinder/helpful/*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
echo Compilation successful!
pause
