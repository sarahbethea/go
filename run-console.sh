#!/bin/bash
echo "Compiling Go Board..."
javac -d bin src/*.java
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi
echo "Starting Go Board Console Game..."
java -cp bin App
