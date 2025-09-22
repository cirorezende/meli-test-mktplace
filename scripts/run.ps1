# Run script for Orders Processing System (PowerShell version)
# This script starts the complete containerized environment

param(
    [string]$Command = "start",
    [string]$Service = "orders-app"
)

# Configuration
$ComposeFile = "docker-compose.yml"
$ServicesToCheck = @("postgres", "redis", "kafka", "orders-app")

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

# Check if Docker and Docker Compose are available
function Test-Dependencies {
    if (!(Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-Error "Docker is not installed or not in PATH"
        exit 1
    }
    
    try {
        docker info | Out-Null
    }
    catch {
        Write-Error "Docker is not running. Please start Docker and try again."
        exit 1
    }
    
    if (!(Get-Command docker-compose -ErrorAction SilentlyContinue)) {
        Write-Error "Docker Compose is not installed or not in PATH"
        exit 1
    }
    
    Write-Success "All dependencies are available"
}

# Wait for service to be healthy
function Wait-ForService {
    param(
        [string]$ServiceName,
        [int]$MaxAttempts = 30
    )
    
    Write-Info "Waiting for $ServiceName to be healthy..."
    
    $attempt = 1
    while ($attempt -le $MaxAttempts) {
        try {
            $psOutput = docker-compose ps $ServiceName 2>$null
            if ($psOutput) {
                $healthStatus = if ($psOutput -match "healthy") { "healthy" }
                              elseif ($psOutput -match "unhealthy") { "unhealthy" }
                              elseif ($psOutput -match "starting") { "starting" }
                              else { "unknown" }
                
                switch ($healthStatus) {
                    "healthy" {
                        Write-Success "$ServiceName is healthy"
                        return $true
                    }
                    "unhealthy" {
                        Write-Error "$ServiceName is unhealthy"
                        return $false
                    }
                    default {
                        Write-Host "." -NoNewline
                        Start-Sleep 2
                        $attempt++
                    }
                }
            }
            else {
                Write-Host "." -NoNewline
                Start-Sleep 2
                $attempt++
            }
        }
        catch {
            Write-Host "." -NoNewline
            Start-Sleep 2
            $attempt++
        }
    }
    
    Write-Host ""
    Write-Warning "$ServiceName health check timeout after $MaxAttempts attempts"
    return $false
}

# Display service information
function Show-ServiceInfo {
    Write-Info "Service endpoints:"
    Write-Host "  📱 Orders API:          http://localhost:8080"
    Write-Host "  📊 API Documentation:   http://localhost:8080/swagger-ui.html"
    Write-Host "  ❤️  Health Check:       http://localhost:8080/actuator/health"
    Write-Host "  📈 Metrics:            http://localhost:8080/actuator/metrics"
    Write-Host ""
    Write-Host "  🐘 pgAdmin:            http://localhost:5050 (admin@orders.com / admin123)"
    Write-Host "  🔴 Redis Insight:      http://localhost:5540"
    Write-Host "  📡 Kafka UI:           http://localhost:8081"
    Write-Host "  🔧 WireMock (CD API):   http://localhost:3000"
    Write-Host ""
    Write-Host "  🗄️  PostgreSQL:         localhost:5432 (orders_user / orders_pass / orders_db)"
    Write-Host "  🔴 Redis:              localhost:6379 (password: redis_pass)"
    Write-Host "  📡 Kafka:              localhost:9092"
}

# Show logs for a specific service
function Show-Logs {
    param([string]$ServiceName = "orders-app")
    Write-Info "Showing logs for $ServiceName (press Ctrl+C to exit)"
    docker-compose logs -f $ServiceName
}

# Stop all services
function Stop-Services {
    Write-Info "Stopping all services..."
    docker-compose down --remove-orphans
    Write-Success "All services stopped"
}

# Clean up everything
function Invoke-Cleanup {
    Write-Info "Cleaning up containers, volumes, and networks..."
    docker-compose down --volumes --remove-orphans
    docker system prune -f
    Write-Success "Cleanup completed"
}

# Main function
function Main {
    switch ($Command.ToLower()) {
        "start" {
            Write-Info "Starting Orders Processing System"
            Test-Dependencies
            
            # Create logs directory
            if (!(Test-Path "logs")) {
                New-Item -ItemType Directory -Path "logs" | Out-Null
            }
            
            # Start services
            Write-Info "Starting infrastructure services..."
            docker-compose up -d postgres redis zookeeper kafka distribution-centers-api
            
            # Wait for infrastructure to be ready
            Wait-ForService "postgres" 30
            Wait-ForService "redis" 20
            Wait-ForService "kafka" 45
            
            # Start the application
            Write-Info "Starting Orders Processing application..."
            docker-compose up -d orders-app
            
            # Wait for application to be healthy
            Wait-ForService "orders-app" 60
            
            # Start optional services
            Write-Info "Starting management interfaces..."
            docker-compose up -d pgadmin redis-insight kafka-ui
            
            Write-Success "All services are running!"
            Show-ServiceInfo
            
            Write-Info "To view logs: .\scripts\run.ps1 logs [service-name]"
            Write-Info "To stop: .\scripts\run.ps1 stop"
        }
        
        "stop" {
            Stop-Services
        }
        
        "restart" {
            Stop-Services
            Start-Sleep 2
            & $PSCommandPath start
        }
        
        "logs" {
            Show-Logs $Service
        }
        
        "status" {
            Write-Info "Service status:"
            docker-compose ps
        }
        
        "cleanup" {
            Invoke-Cleanup
        }
        
        "info" {
            Show-ServiceInfo
        }
        
        default {
            Write-Host "Usage: .\scripts\run.ps1 {start|stop|restart|logs|status|cleanup|info}"
            Write-Host ""
            Write-Host "Commands:"
            Write-Host "  start    - Start all services"
            Write-Host "  stop     - Stop all services"
            Write-Host "  restart  - Restart all services"
            Write-Host "  logs     - Show logs for a service (default: orders-app)"
            Write-Host "  status   - Show status of all services"
            Write-Host "  cleanup  - Stop services and remove volumes"
            Write-Host "  info     - Show service endpoints"
            exit 1
        }
    }
}

# Run main function
Main