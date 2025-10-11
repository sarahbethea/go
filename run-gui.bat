@echo off
echo Compiling Go Board GUI...
javac -d bin src\*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)
echo Starting Go Board GUI...
java -cp bin GoBoardSwing
pause