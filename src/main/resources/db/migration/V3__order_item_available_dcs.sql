-- Add JSONB column to store ordered available DCs per item
ALTER TABLE order_items
ADD COLUMN IF NOT EXISTS available_distribution_centers JSONB;

COMMENT ON COLUMN order_items.available_distribution_centers IS 'Ordered list of nearby DCs for this item (array of {code, distanceKm})';