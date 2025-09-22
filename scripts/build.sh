#!/bin/bash

# Build script for Orders Processing System
# This script builds the Docker image and prepares the environment

set -e

# Configuration
IMAGE_NAME="orders-processing-system"
IMAGE_TAG="${1:-latest}"
FULL_IMAGE_NAME="$IMAGE_NAME:$IMAGE_TAG"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Main build process
main() {
    log_info "Starting build process for Orders Processing System"
    log_info "Image: $FULL_IMAGE_NAME"
    
    # Check if Docker is running
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    
    # Clean up old containers and images if needed
    log_info "Cleaning up old containers..."
    docker-compose down --remove-orphans 2>/dev/null || true
    
    # Remove old image if exists
    if docker image inspect $FULL_IMAGE_NAME > /dev/null 2>&1; then
        log_info "Removing old image: $FULL_IMAGE_NAME"
        docker image rm $FULL_IMAGE_NAME || true
    fi
    
    # Build the application
    log_info "Building application with Maven..."
    if command -v mvn > /dev/null 2>&1; then
        mvn clean package -DskipTests=false -q
        log_success "Maven build completed successfully"
    else
        log_warning "Maven not found in PATH. Docker will handle the build."
    fi
    
    # Build Docker image
    log_info "Building Docker image: $FULL_IMAGE_NAME"
    docker build \
        --tag $FULL_IMAGE_NAME \
        --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
        --build-arg VCS_REF=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown") \
        .
    
    log_success "Docker image built successfully: $FULL_IMAGE_NAME"
    
    # Show image information
    log_info "Image information:"
    docker images $FULL_IMAGE_NAME --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"
    
    # Create logs directory
    mkdir -p logs
    
    log_success "Build process completed successfully!"
    log_info "You can now run the application with: ./scripts/run.sh"
}

# Run main function
main "$@"