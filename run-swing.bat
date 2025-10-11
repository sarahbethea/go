@echo off
echo Compiling Go Board Swing GUI...

REM Compile the Java files (no extra dependencies needed!)
javac -d bin src\*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo Starting Go Board Swing GUI...

REM Run the Swing GUI (no extra modules needed!)
java -cp bin GoBoardSwing

pause
