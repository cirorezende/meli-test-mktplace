# Database Setup Instructions

This document explains how to set up the PostgreSQL database with PostGIS extension for the ML Marketplace Orders system.

## Prerequisites

- PostgreSQL 15+ installed
- PostGIS extension available
- Java 21+ with Maven

## Database Setup

### 1. Create Database

```sql
-- Connect to PostgreSQL as superuser
CREATE DATABASE mktplace_orders_dev;
CREATE DATABASE mktplace_orders;

-- Create user if needed (optional for development)
CREATE USER mktplace_user WITH PASSWORD 'mktplace_password';
GRANT ALL PRIVILEGES ON DATABASE mktplace_orders_dev TO mktplace_user;
GRANT ALL PRIVILEGES ON DATABASE mktplace_orders TO mktplace_user;
```

### 2. Run Migrations

The application uses Flyway for database migrations. You can run them in several ways:

#### Option A: Using Maven Plugin (Recommended for development)

```bash
# Run migrations using Maven
mvn flyway:migrate

# Clean database (development only)
mvn flyway:clean

# Get migration info
mvn flyway:info
```

#### Option B: Using Spring Boot Application

Migrations will run automatically when starting the application:

```bash
mvn spring-boot:run
```

#### Option C: Manual Migration (Not recommended)

You can run the SQL files manually in order:

1. `V1__initial_schema.sql`
2. `V2__sample_distribution_centers.sql`

### 3. Verify Installation

After running migrations, verify the database structure:

```sql
-- Check tables
\dt

-- Check PostGIS extension
SELECT PostGIS_Version();

-- Check sample distribution centers
SELECT code, name, ST_AsText(coordinates) as location FROM distribution_centers LIMIT 5;

-- Test proximity query
SELECT code, name, 
       ST_Distance_Sphere(
           coordinates, 
           ST_SetSRID(ST_MakePoint(-46.6333, -23.5505), 4326)
       ) / 1000 as distance_km
FROM distribution_centers 
ORDER BY distance_km 
LIMIT 3;
```

## Environment Configuration

### Development

- Database: `mktplace_orders_dev`
- Host: `localhost:5432`
- User: `postgres` / `postgres`
- Flyway: Enabled with clean allowed

### Staging

- Database: `mktplace_orders_staging`
- Host: `staging-db:5432`
- User: Environment variable `DB_USERNAME`
- Password: Environment variable `DB_PASSWORD`
- Flyway: Enabled, clean disabled

### Production

- Database: `mktplace_orders_prod`
- Host: Environment variable `DB_HOST:DB_PORT`
- User: Environment variable `DB_USERNAME`
- Password: Environment variable `DB_PASSWORD`
- Flyway: Enabled, clean disabled, validation enabled

## Database Schema

### Tables Created

1. **orders**: Main orders table with delivery coordinates
2. **order_items**: Items within each order with assigned distribution centers
3. **distribution_centers**: Available distribution centers with geographic coordinates

### Key Features

- PostGIS extension for geospatial calculations
- GIST indexes for spatial queries
- B-tree indexes for common lookups
- Automatic timestamp triggers
- Sample distribution centers across Brazil
- Views for common queries

### Geospatial Features

The database uses PostGIS for geographic calculations:

- **Coordinate System**: WGS84 (SRID: 4326)
- **Distance Calculations**: Sphere distance in meters
- **Proximity Queries**: Optimized with GIST indexes
- **Sample Data**: 15 distribution centers across Brazilian major cities

## Troubleshooting

### PostGIS Extension Issues

```sql
-- Check if PostGIS is installed
SELECT * FROM pg_available_extensions WHERE name = 'postgis';

-- Install PostGIS (requires superuser)
CREATE EXTENSION IF NOT EXISTS postgis;
```

### Flyway Issues

```bash
# Check migration status
mvn flyway:info

# Repair migration checksums
mvn flyway:repair

# Baseline existing database
mvn flyway:baseline
```

### Connection Issues

- Verify PostgreSQL is running
- Check database name and credentials
- Ensure PostGIS extension is available
- Verify network connectivity (for staging/prod)

## Performance Tuning

For production environments, consider these PostgreSQL settings:

```sql
-- Increase shared buffers for PostGIS
SET shared_buffers = '256MB';

-- Optimize for geospatial queries
SET random_page_cost = 1.1;
SET effective_cache_size = '1GB';
```

## Backup and Restore

```bash
# Backup
pg_dump -h localhost -U postgres -d mktplace_orders_dev > backup.sql

# Restore
psql -h localhost -U postgres -d mktplace_orders_dev < backup.sql
```
