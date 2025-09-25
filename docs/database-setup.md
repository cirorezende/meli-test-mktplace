# Database info

## Tables Created

1. **orders**: Main orders table with delivery coordinates
2. **order_items**: Items within each order with assigned distribution centers
3. **distribution_centers**: Available distribution centers with geographic coordinates

## Key Features

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
