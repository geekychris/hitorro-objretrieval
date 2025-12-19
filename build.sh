#!/bin/bash

set -e

echo "============================================"
echo "Building Hitorro Object Retrieval"
echo "============================================"

# Check for Java 19+
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 19 ]; then
    echo "ERROR: Java 19 or higher is required (found Java $JAVA_VERSION)"
    exit 1
fi

echo "✓ Java version: $JAVA_VERSION"

# Check for Maven
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed"
    exit 1
fi

echo "✓ Maven detected"

# Clean build
echo ""
echo "Cleaning previous build..."
mvn clean

# Build with tests
echo ""
echo "Building project..."
if mvn install; then
    echo ""
    echo "============================================"
    echo "✓ Build successful!"
    echo "============================================"
    echo "Artifact: target/hitorro-objretrieval-3.0.0.jar"
    echo ""
    echo "To use in your project:"
    echo "  <dependency>"
    echo "    <groupId>com.hitorro</groupId>"
    echo "    <artifactId>hitorro-objretrieval</artifactId>"
    echo "    <version>3.0.0</version>"
    echo "  </dependency>"
else
    echo ""
    echo "============================================"
    echo "✗ Build failed!"
    echo "============================================"
    exit 1
fi
