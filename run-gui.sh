#!/bin/bash
echo "Compiling Go Board GUI..."
javac -d bin src/*.java
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi
echo "Starting Go Board GUI..."
java -cp bin GoBoardSwing
