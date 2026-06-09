#
# Maven wrapper script for PowerShell
#

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Error "Maven is not installed and not found in PATH"
    Write-Error "Please install Maven: https://maven.apache.org/download.cgi"
    exit 1
}

& mvn @args
exit $LASTEXITCODE
