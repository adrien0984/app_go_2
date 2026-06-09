#!/bin/sh
#
# Maven wrapper script for Unix
#

if ! command -v mvn >/dev/null 2>&1; then
    echo "Error: Maven is not installed and not found in PATH"
    echo "Please install Maven: https://maven.apache.org/download.cgi"
    exit 1
fi

exec mvn "$@"
