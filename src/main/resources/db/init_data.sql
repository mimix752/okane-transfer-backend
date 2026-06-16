-- ═══════════════════════════════════════════════════════════════
-- Script d'initialisation des données de test
-- ═══════════════════════════════════════════════════════════════

-- Insérer les utilisateurs de test
INSERT INTO users (username, email, password, phone, role, enabled, dtype, created_at, updated_at) VALUES
('admin', 'admin@okane.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DW5nfRu76j6mX6L5SfA8wy7IUW2JFm', '+33123456789', 'ADMIN', true, 'User', NOW(), NOW()),
('agent_paris', 'agent@okane.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DW5nfRu76j6mX6L5SfA8wy7IUW2JFm', '+33612345678', 'AGENT', true, 'Agent', NOW(), NOW()),
('client_test', 'client@okane.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DW5nfRu76j6mX6L5SfA8wy7IUW2JFm', '+33712345678', 'CLIENT', true, 'User', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insérer l'agence
INSERT INTO agency (name, city, country, phone, email, active, created_at, updated_at) VALUES
('Agence Paris', 'Paris', 'FR', '+33123456789', 'paris@okane.com', true, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insérer l'agent
INSERT INTO agent (id, agency_id, active, created_at, updated_at) VALUES
(2, 1, true, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insérer les devises
INSERT INTO currency (code, name, symbol, active, created_at, updated_at) VALUES
('EUR', 'Euro', '€', true, NOW(), NOW()),
('USD', 'Dollar US', '$', true, NOW(), NOW()),
('XOF', 'Franc CFA', 'CFA', true, NOW(), NOW()),
('GBP', 'Livre Sterling', '£', true, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insérer les corridors
INSERT INTO corridor (source_country, destination_country, source_currency_id, destination_currency_id, active, created_at, updated_at) VALUES
('FR', 'SN', 1, 3, true, NOW(), NOW()),
('FR', 'ML', 1, 3, true, NOW(), NOW()),
('US', 'SN', 2, 3, true, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insérer les grilles tarifaires
INSERT INTO fee_grid (corridor_id, min_amount, max_amount, fixed_fee, percentage_fee, agency_share, central_share, active, created_at, updated_at) VALUES
(1, 0.00, 1000.00, 5.00, 2.5, 60.00, 40.00, true, NOW(), NOW()),
(1, 1000.01, 5000.00, 10.00, 2.0, 60.00, 40.00, true, NOW(), NOW()),
(1, 5000.01, 999999.99, 20.00, 1.5, 60.00, 40.00, true, NOW(), NOW()),
(2, 0.00, 1000.00, 5.00, 2.5, 60.00, 40.00, true, NOW(), NOW()),
(3, 0.00, 1000.00, 10.00, 3.0, 60.00, 40.00, true, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Vérifier les données insérées
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Agency', COUNT(*) FROM agency
UNION ALL
SELECT 'Agent', COUNT(*) FROM agent
UNION ALL
SELECT 'Currency', COUNT(*) FROM currency
UNION ALL
SELECT 'Corridor', COUNT(*) FROM corridor
UNION ALL
SELECT 'FeeGrid', COUNT(*) FROM fee_grid;

-- Afficher les utilisateurs
SELECT id, username, email, role FROM users;

-- Afficher les corridors avec les devises
SELECT c.id, c.source_country, c.destination_country, sc.code as source_currency, dc.code as destination_currency
FROM corridor c
JOIN currency sc ON c.source_currency_id = sc.id
JOIN currency dc ON c.destination_currency_id = dc.id;

-- Afficher les grilles tarifaires
SELECT fg.id, c.source_country, c.destination_country, fg.min_amount, fg.max_amount, fg.fixed_fee, fg.percentage_fee
FROM fee_grid fg
JOIN corridor c ON fg.corridor_id = c.id;
