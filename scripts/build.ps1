# Build script for Orders Processing System (PowerShell version)
# This script builds the Docker image and prepares the environment

param(
    [string]$Tag = "latest"
)

# Configuration
$ImageName = "orders-processing-system"
$FullImageName = "${ImageName}:${Tag}"

# Functions
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Main build process
function Main {
    Write-Info "Starting build process for Orders Processing System"
    Write-Info "Image: $FullImageName"
    
    # Check if Docker is running
    try {
        docker info | Out-Null
    }
    catch {
        Write-Error "Docker is not running. Please start Docker and try again."
        exit 1
    }
    
    # Clean up old containers and images if needed
    Write-Info "Cleaning up old containers..."
    try {
        docker-compose down --remove-orphans 2>$null
    }
    catch {
        # Ignore errors during cleanup
    }
    
    # Remove old image if exists
    try {
        docker image inspect $FullImageName | Out-Null
        Write-Info "Removing old image: $FullImageName"
        docker image rm $FullImageName
    }
    catch {
        # Image doesn't exist, continue
    }
    
    # Build the application
    Write-Info "Building application with Maven..."
    if (Get-Command mvn -ErrorAction SilentlyContinue) {
        mvn clean package -DskipTests=false -q
        Write-Success "Maven build completed successfully"
    }
    else {
        Write-Warning "Maven not found in PATH. Docker will handle the build."
    }
    
    # Build Docker image
    Write-Info "Building Docker image: $FullImageName"
    
    $buildDate = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    $vcsRef = try { git rev-parse --short HEAD } catch { "unknown" }
    
    docker build `
        --tag $FullImageName `
        --build-arg BUILD_DATE=$buildDate `
        --build-arg VCS_REF=$vcsRef `
        .
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Docker image built successfully: $FullImageName"
    }
    else {
        Write-Error "Docker build failed"
        exit 1
    }
    
    # Show image information
    Write-Info "Image information:"
    docker images $FullImageName --format "table {{.Repository}}`t{{.Tag}}`t{{.Size}}`t{{.CreatedAt}}"
    
    # Create logs directory
    if (!(Test-Path "logs")) {
        New-Item -ItemType Directory -Path "logs" | Out-Null
    }
    
    Write-Success "Build process completed successfully!"
    Write-Info "You can now run the application with: .\scripts\run.ps1"
}

# Run main function
Main