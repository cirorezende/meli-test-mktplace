# Run script for Orders Processing System (PowerShell version)
# This script starts the complete containerized environment

param(
    [string]$Command = "start"
)

# Functions
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Main function
switch ($Command.ToLower()) {
    "start" {
        Write-Info "Starting Orders Processing System"
        
        # Create logs directory
        if (!(Test-Path "logs")) {
            New-Item -ItemType Directory -Path "logs" | Out-Null
        }
        
        # Start services
        Write-Info "Starting all services..."
        docker-compose up -d
        
        Write-Success "All services are starting!"
        Write-Info "Service endpoints:"
        Write-Host "  Orders API:          http://localhost:8080"
        Write-Host "  API Documentation:   http://localhost:8080/swagger-ui.html"
        Write-Host "  Health Check:        http://localhost:8080/actuator/health"
        Write-Host "  pgAdmin:             http://localhost:5050 (admin@orders.com / admin123)"
        Write-Host "  Redis Insight:       http://localhost:5540"
        Write-Host "  Kafka UI:            http://localhost:8081"
        Write-Host "  WireMock:            http://localhost:3000"
    }
    
    "stop" {
        Write-Info "Stopping all services..."
        docker-compose down --remove-orphans
        Write-Success "All services stopped"
    }
    
    "logs" {
        Write-Info "Showing logs for orders-app (press Ctrl+C to exit)"
        docker-compose logs -f orders-app
    }
    
    "status" {
        Write-Info "Service status:"
        docker-compose ps
    }
    
    "cleanup" {
        Write-Info "Cleaning up containers, volumes, and networks..."
        docker-compose down --volumes --remove-orphans
        docker system prune -f
        Write-Success "Cleanup completed"
    }
    
    default {
        Write-Host "Usage: .\scripts\run.ps1 {start|stop|logs|status|cleanup}"
        Write-Host ""
        Write-Host "Commands:"
        Write-Host "  start    - Start all services"
        Write-Host "  stop     - Stop all services"
        Write-Host "  logs     - Show logs for orders-app"
        Write-Host "  status   - Show status of all services"
        Write-Host "  cleanup  - Stop services and remove volumes"
        exit 1
    }
}