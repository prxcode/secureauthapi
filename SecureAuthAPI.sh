#!/bin/bash

echo "---------------------------------------------------"
echo "## Building SecureAuthAPI with Maven..."
echo "---------------------------------------------------"

mvn clean install
BUILD_EXIT_CODE=$?

if [ $BUILD_EXIT_CODE -ne 0 ]; then
echo "Build failed! Exiting..."
exit $BUILD_EXIT_CODE
fi

echo "---------------------------------------------------"
echo "## Launching Spring Boot Server..."
echo "---------------------------------------------------"

mvn spring-boot:run &
SERVER_PID=$!

echo "---------------------------------------------------"
echo "## Waiting 8 seconds for server to start up..."
echo "---------------------------------------------------"

sleep 8

echo "---------------------------------------------------"
echo "## Launching browser to http://localhost:8080 ..."
echo "---------------------------------------------------"

# macOS

if command -v open >/dev/null 2>&1; then
open http://localhost:8080

# Linux

elif command -v xdg-open >/dev/null 2>&1; then
xdg-open http://localhost:8080

else
echo "Could not automatically open browser."
echo "Please visit: http://localhost:8080"
fi

echo "---------------------------------------------------"
echo "## SecureAuthAPI is running."
echo "Server PID: $SERVER_PID"
echo "Press Ctrl+C to stop."
echo "---------------------------------------------------"

wait $SERVER_PID
