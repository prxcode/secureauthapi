@echo off


call some-executable-batchfiles/build.bat
IF %ERRORLEVEL% NEQ 0 (
    echo Build failed! Exiting...
    pause
    exit /b %ERRORLEVEL%
)

call some-executable-batchfiles/show.bat

echo =========================
echo Running the Spring Boot server...
echo =========================
call some-executable-batchfiles/run.bat

REM Optional: wait a few seconds to make sure the server is up
timeout /t 10 /nobreak >nul


pause