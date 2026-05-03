@echo off
echo Running Bus Route Finder...
java busfinder.home
if %errorlevel% neq 0 (
    echo.
    echo Application exited with an error.
    pause
)
