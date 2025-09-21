-- Initial schema for ML Marketplace Orders system
-- Following ADR-001 (PostgreSQL with PostGIS), ADR-004 (Logistic decisions storage), ADR-008 (ULID identifiers)

-- Enable PostGIS extension for geospatial calculations
CREATE EXTENSION IF NOT EXISTS postgis;

-- Create orders table
-- Using VARCHAR for ULID identifiers and JSONB for flexible address storage
CREATE TABLE orders (
    id VARCHAR(26) PRIMARY KEY,
    customer_id VARCHAR(26) NOT NULL,
    delivery_address JSONB NOT NULL,
    delivery_coordinates GEOMETRY(POINT, 4326),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT orders_status_check CHECK (status IN ('RECEIVED', 'PROCESSING', 'PROCESSED', 'FAILED'))
);

-- Create order_items table
-- Stores individual items within each order
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(26) NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    item_id VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL,
    assigned_distribution_center VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT order_items_quantity_positive CHECK (quantity > 0)
);

-- Create distribution_centers table
-- Stores available distribution centers with geospatial coordinates
CREATE TABLE distribution_centers (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address JSONB NOT NULL,
    coordinates GEOMETRY(POINT, 4326) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance optimization

-- B-tree indexes for common queries
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_item_id ON order_items(item_id);
CREATE INDEX idx_distribution_centers_active ON distribution_centers(active);

-- Geospatial indexes for proximity calculations (ADR-009)
CREATE INDEX idx_orders_delivery_coordinates ON orders USING GIST(delivery_coordinates);
CREATE INDEX idx_distribution_centers_coordinates ON distribution_centers USING GIST(coordinates);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers to automatically update updated_at
CREATE TRIGGER update_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_distribution_centers_updated_at
    BEFORE UPDATE ON distribution_centers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comments for documentation
COMMENT ON TABLE orders IS 'Main orders table storing customer orders with delivery information';
COMMENT ON TABLE order_items IS 'Individual items within each order with quantity and assigned distribution center';
COMMENT ON TABLE distribution_centers IS 'Available distribution centers with geospatial coordinates for proximity calculations';

COMMENT ON COLUMN orders.id IS 'ULID identifier for the order (ADR-008)';
COMMENT ON COLUMN orders.delivery_coordinates IS 'PostGIS POINT geometry for geospatial calculations (ADR-001)';
COMMENT ON COLUMN distribution_centers.coordinates IS 'PostGIS POINT geometry for distribution center location (ADR-001)';