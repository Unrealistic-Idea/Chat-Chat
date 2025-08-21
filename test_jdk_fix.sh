#!/bin/bash

# Test script for JDK image transformation fix
# Run this script to verify the fix works

echo "Testing JDK Image Transformation Fix"
echo "======================================"

# Clean previous build artifacts
echo "1. Cleaning previous build artifacts..."
./gradlew clean

# Check Gradle version
echo "2. Checking Gradle version..."
./gradlew --version

# Test basic task listing (this should work if AGP resolves correctly)
echo "3. Testing basic Gradle tasks..."
./gradlew tasks --all

# Test Java compilation specifically
echo "4. Testing Java compilation (the problematic task)..."
./gradlew app:compileDebugJava

# Test full build
echo "5. Testing full build..."
./gradlew build

echo "Test completed. If no errors occurred, the JDK image transformation fix is working."