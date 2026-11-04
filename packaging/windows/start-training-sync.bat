@echo off
setlocal

set "APP_DIR=%~dp0"

if not defined LOCALAPPDATA (
    echo LOCALAPPDATA is not defined. Cannot choose a data directory.
    pause
    exit /b 1
)

set "DATA_DIR=%LOCALAPPDATA%\TrainingSync"
if not exist "%DATA_DIR%" mkdir "%DATA_DIR%"
if errorlevel 1 (
    echo Could not create the data directory: %DATA_DIR%
    pause
    exit /b 1
)

set "DATA_DIR_SQL=%DATA_DIR:\=/%"
set "SPRING_DATASOURCE_URL=jdbc:sqlite:%DATA_DIR_SQL%/training-sync.sqlite"
set "LOGGING_FILE_NAME=%DATA_DIR%\training-sync.log"
set "SERVER_ADDRESS=127.0.0.1"
set "SERVER_PORT=8080"

echo TrainingSync data and logs: %DATA_DIR%
echo Open http://localhost:8080 after the server starts.
echo Press Ctrl+C to stop TrainingSync.

"%APP_DIR%runtime\bin\java.exe" -jar "%APP_DIR%training-sync.jar"
exit /b %ERRORLEVEL%