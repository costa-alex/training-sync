#!/bin/bash
set -euo pipefail

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DATA_DIR="${HOME}/Library/Application Support/TrainingSync"

mkdir -p "$DATA_DIR"

export SPRING_DATASOURCE_URL="jdbc:sqlite:${DATA_DIR}/training-sync.sqlite"
export LOGGING_FILE_NAME="${DATA_DIR}/training-sync.log"
export SERVER_ADDRESS="127.0.0.1"
export SERVER_PORT="8080"

echo "TrainingSync data and logs: $DATA_DIR"
echo "Open http://localhost:8080 after the server starts."
echo "Press Ctrl+C to stop TrainingSync."

exec "$APP_DIR/runtime/bin/java" -jar "$APP_DIR/training-sync.jar"
