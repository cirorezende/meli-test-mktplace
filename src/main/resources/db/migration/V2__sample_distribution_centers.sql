-- Sample distribution centers for testing
-- Using real coordinates from major Brazilian cities for realistic testing scenarios

-- Insert sample distribution centers across Brazil
INSERT INTO distribution_centers (code, name, address, coordinates, active) VALUES
('SP-001', 'Centro de Distribuição São Paulo', 
 '{"street": "Rua das Indústrias, 1000", "city": "São Paulo", "state": "SP", "country": "Brasil", "postalCode": "05037-000"}',
 ST_SetSRID(ST_MakePoint(-46.6333, -23.5505), 4326), true),

('RJ-001', 'Centro de Distribuição Rio de Janeiro',
 '{"street": "Av. Brasil, 15000", "city": "Rio de Janeiro", "state": "RJ", "country": "Brasil", "postalCode": "21040-020"}',
 ST_SetSRID(ST_MakePoint(-43.1729, -22.9068), 4326), true),

('MG-001', 'Centro de Distribuição Belo Horizonte',
 '{"street": "Rodovia BR-040, km 688", "city": "Belo Horizonte", "state": "MG", "country": "Brasil", "postalCode": "30112-000"}',
 ST_SetSRID(ST_MakePoint(-43.9378, -19.9167), 4326), true),

('RS-001', 'Centro de Distribuição Porto Alegre',
 '{"street": "Av. Sertório, 6600", "city": "Porto Alegre", "state": "RS", "country": "Brasil", "postalCode": "91020-001"}',
 ST_SetSRID(ST_MakePoint(-51.2090, -30.0346), 4326), true),

('SC-001', 'Centro de Distribuição Florianópolis',
 '{"street": "Rod. SC-401, km 5", "city": "Florianópolis", "state": "SC", "country": "Brasil", "postalCode": "88032-005"}',
 ST_SetSRID(ST_MakePoint(-48.5482, -27.5954), 4326), true),

('PR-001', 'Centro de Distribuição Curitiba',
 '{"street": "BR-277, km 10", "city": "Curitiba", "state": "PR", "country": "Brasil", "postalCode": "82640-090"}',
 ST_SetSRID(ST_MakePoint(-49.2731, -25.4244), 4326), true),

('BA-001', 'Centro de Distribuição Salvador',
 '{"street": "Via Expressa, 200", "city": "Salvador", "state": "BA", "country": "Brasil", "postalCode": "42700-000"}',
 ST_SetSRID(ST_MakePoint(-38.5014, -12.9714), 4326), true),

('PE-001', 'Centro de Distribuição Recife',
 '{"street": "BR-232, km 14", "city": "Recife", "state": "PE", "country": "Brasil", "postalCode": "52171-900"}',
 ST_SetSRID(ST_MakePoint(-34.8755, -8.0476), 4326), true),

('CE-001', 'Centro de Distribuição Fortaleza',
 '{"street": "BR-116, km 15", "city": "Fortaleza", "state": "CE", "country": "Brasil", "postalCode": "60741-000"}',
 ST_SetSRID(ST_MakePoint(-38.5267, -3.7319), 4326), true),

('GO-001', 'Centro de Distribuição Goiânia',
 '{"street": "GO-040, km 8", "city": "Goiânia", "state": "GO", "country": "Brasil", "postalCode": "74690-090"}',
 ST_SetSRID(ST_MakePoint(-49.2532, -16.6869), 4326), true);

-- Create a view for active distribution centers (commonly used query)
CREATE VIEW active_distribution_centers AS
SELECT code, name, address, coordinates, created_at, updated_at
FROM distribution_centers
WHERE active = true;

-- Comment on the view
COMMENT ON VIEW active_distribution_centers IS 'View showing only active distribution centers for operational queries';