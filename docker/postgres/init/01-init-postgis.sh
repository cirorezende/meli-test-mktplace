#!/bin/bash
set -e

# PostgreSQL initialization script for Orders Processing System
echo "Initializing PostgreSQL database for Orders Processing System..."

# Create PostGIS extension
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Enable PostGIS extension
    CREATE EXTENSION IF NOT EXISTS postgis;
    CREATE EXTENSION IF NOT EXISTS postgis_topology;
    
    -- Create uuid extension for ID generation
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    
    -- Verify PostGIS installation
    SELECT PostGIS_version();
    
    -- Grant permissions
    GRANT ALL PRIVILEGES ON DATABASE $POSTGRES_DB TO $POSTGRES_USER;
    
    -- Create basic indexes that Flyway migrations might need
    -- (Application-specific tables will be created by Flyway)
EOSQL

echo "PostgreSQL initialization completed successfully!"