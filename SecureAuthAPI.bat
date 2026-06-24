@echo off

echo ---------------------------------------------------
echo ## Building SecureAuthAPI with Maven...
echo ---------------------------------------------------

call mvn clean install
IF %ERRORLEVEL% NEQ 0 (
    echo Build failed! Exiting...
    pause
    exit /b %ERRORLEVEL%
)

echo ---------------------------------------------------
echo ## Launching Spring Boot Server...
echo ---------------------------------------------------

start "SecureAuthAPI Server" cmd /k "mvn spring-boot:run"

echo ---------------------------------------------------
echo ## Waiting 8 seconds for server to start up...
echo ---------------------------------------------------

timeout /t 8 /nobreak >nul

echo ---------------------------------------------------
echo ## Launching browser to http://localhost:8080 ...
echo ---------------------------------------------------

start http://localhost:8080

echo ---------------------------------------------------
echo ## SecureAuthAPI is running.
echo Close the "SecureAuthAPI Server" console window to stop.
echo ---------------------------------------------------