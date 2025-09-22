#!/bin/bash

# Run script for Orders Processing System
# This script starts the complete containerized environment

set -e

# Configuration
COMPOSE_FILE="docker-compose.yml"
SERVICES_TO_CHECK=("postgres" "redis" "kafka" "orders-app")

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

# Check if Docker and Docker Compose are available
check_dependencies() {
    if ! command -v docker > /dev/null 2>&1; then
        log_error "Docker is not installed or not in PATH"
        exit 1
    fi
    
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    
    if ! command -v docker-compose > /dev/null 2>&1; then
        log_error "Docker Compose is not installed or not in PATH"
        exit 1
    fi
    
    log_success "All dependencies are available"
}

# Wait for service to be healthy
wait_for_service() {
    local service_name=$1
    local max_attempts=${2:-30}
    local attempt=1
    
    log_info "Waiting for $service_name to be healthy..."
    
    while [ $attempt -le $max_attempts ]; do
        if docker-compose ps -q $service_name > /dev/null 2>&1; then
            local health_status=$(docker-compose ps $service_name | grep $service_name | grep -o "healthy\|unhealthy\|starting" || echo "unknown")
            
            case $health_status in
                "healthy")
                    log_success "$service_name is healthy"
                    return 0
                    ;;
                "unhealthy")
                    log_error "$service_name is unhealthy"
                    return 1
                    ;;
                "starting"|"unknown")
                    echo -n "."
                    sleep 2
                    ((attempt++))
                    ;;
            esac
        else
            echo -n "."
            sleep 2
            ((attempt++))
        fi
    done
    
    echo ""
    log_warning "$service_name health check timeout after $max_attempts attempts"
    return 1
}

# Display service information
show_service_info() {
    log_info "Service endpoints:"
    echo "  📱 Orders API:          http://localhost:8080"
    echo "  📊 API Documentation:   http://localhost:8080/swagger-ui.html"
    echo "  ❤️  Health Check:       http://localhost:8080/actuator/health"
    echo "  📈 Metrics:            http://localhost:8080/actuator/metrics"
    echo ""
    echo "  🐘 pgAdmin:            http://localhost:5050 (admin@orders.com / admin123)"
    echo "  🔴 Redis Insight:      http://localhost:5540"
    echo "  📡 Kafka UI:           http://localhost:8081"
    echo "  🔧 WireMock (CD API):   http://localhost:3000"
    echo ""
    echo "  🗄️  PostgreSQL:         localhost:5432 (orders_user / orders_pass / orders_db)"
    echo "  🔴 Redis:              localhost:6379 (password: redis_pass)"
    echo "  📡 Kafka:              localhost:9092"
}

# Show logs for a specific service
show_logs() {
    local service=${1:-orders-app}
    log_info "Showing logs for $service (press Ctrl+C to exit)"
    docker-compose logs -f $service
}

# Stop all services
stop_services() {
    log_info "Stopping all services..."
    docker-compose down --remove-orphans
    log_success "All services stopped"
}

# Clean up everything (containers, volumes, networks)
cleanup() {
    log_info "Cleaning up containers, volumes, and networks..."
    docker-compose down --volumes --remove-orphans
    docker system prune -f
    log_success "Cleanup completed"
}

# Main function
main() {
    local command=${1:-start}
    
    case $command in
        "start")
            log_info "Starting Orders Processing System"
            check_dependencies
            
            # Create logs directory
            mkdir -p logs
            
            # Start services
            log_info "Starting infrastructure services..."
            docker-compose up -d postgres redis zookeeper kafka distribution-centers-api
            
            # Wait for infrastructure to be ready
            wait_for_service "postgres" 30
            wait_for_service "redis" 20
            wait_for_service "kafka" 45
            
            # Start the application
            log_info "Starting Orders Processing application..."
            docker-compose up -d orders-app
            
            # Wait for application to be healthy
            wait_for_service "orders-app" 60
            
            # Start optional services
            log_info "Starting management interfaces..."
            docker-compose up -d pgadmin redis-insight kafka-ui
            
            log_success "All services are running!"
            show_service_info
            
            log_info "To view logs: $0 logs [service-name]"
            log_info "To stop: $0 stop"
            ;;
            
        "stop")
            stop_services
            ;;
            
        "restart")
            stop_services
            sleep 2
            main start
            ;;
            
        "logs")
            show_logs $2
            ;;
            
        "status")
            log_info "Service status:"
            docker-compose ps
            ;;
            
        "cleanup")
            cleanup
            ;;
            
        "info")
            show_service_info
            ;;
            
        *)
            echo "Usage: $0 {start|stop|restart|logs|status|cleanup|info}"
            echo ""
            echo "Commands:"
            echo "  start    - Start all services"
            echo "  stop     - Stop all services"
            echo "  restart  - Restart all services"
            echo "  logs     - Show logs for a service (default: orders-app)"
            echo "  status   - Show status of all services"
            echo "  cleanup  - Stop services and remove volumes"
            echo "  info     - Show service endpoints"
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"