-- ═══════════════════════════════════════════════════════════════
-- Schéma BD pour Tâche 3.2 - Enregistrement d'un envoi
-- ═══════════════════════════════════════════════════════════════

-- ─── Table USERS (Entité de base) ───
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    dtype VARCHAR(31) NOT NULL
);

-- ─── Table AGENCY (Agence) ───
CREATE TABLE IF NOT EXISTS agency (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100),
    country VARCHAR(3),
    phone VARCHAR(20),
    email VARCHAR(100),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ─── Table AGENT (Hérité de USERS) ───
CREATE TABLE IF NOT EXISTS agent (
    id BIGINT PRIMARY KEY,
    agency_id BIGINT NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (agency_id) REFERENCES agency(id) ON DELETE RESTRICT
);

-- ─── Table CURRENCY (Devise) ───
CREATE TABLE IF NOT EXISTS currency (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(5),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ─── Table CORRIDOR (Route pays source → destination) ───
CREATE TABLE IF NOT EXISTS corridor (
    id BIGSERIAL PRIMARY KEY,
    source_country VARCHAR(3) NOT NULL,
    destination_country VARCHAR(3) NOT NULL,
    source_currency_id BIGINT NOT NULL,
    destination_currency_id BIGINT NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (source_currency_id) REFERENCES currency(id),
    FOREIGN KEY (destination_currency_id) REFERENCES currency(id),
    UNIQUE (source_country, destination_country),
    CONSTRAINT uk_corridor_source_dest UNIQUE (source_country, destination_country)
);

-- ─── Table FEE_GRID (Grille tarifaire) ───
CREATE TABLE IF NOT EXISTS fee_grid (
    id BIGSERIAL PRIMARY KEY,
    corridor_id BIGINT NOT NULL,
    min_amount NUMERIC(15, 2) NOT NULL,
    max_amount NUMERIC(15, 2) NOT NULL,
    fixed_fee NUMERIC(10, 2) NOT NULL DEFAULT 0,
    percentage_fee NUMERIC(5, 2) NOT NULL DEFAULT 0,
    agency_share NUMERIC(5, 2) NOT NULL,
    central_share NUMERIC(5, 2) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (corridor_id) REFERENCES corridor(id) ON DELETE CASCADE,
    CONSTRAINT fk_feegrid_corridor FOREIGN KEY (corridor_id) REFERENCES corridor(id)
);

-- ─── Table TRANSFERS (Transferts d'argent) ───
CREATE TABLE IF NOT EXISTS transfers (
    id BIGSERIAL PRIMARY KEY,
    transfer_code VARCHAR(20) NOT NULL UNIQUE,
    sender_id BIGINT NOT NULL,
    recipient_name VARCHAR(100),
    recipient_phone VARCHAR(20),
    amount NUMERIC(18, 2) NOT NULL,
    currency VARCHAR(3),
    converted_amount NUMERIC(18, 2),
    target_currency VARCHAR(3),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    agency_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (agency_id) REFERENCES agency(id) ON DELETE SET NULL
);

-- ─── Index pour optimiser les requêtes ───
CREATE INDEX idx_transfers_code ON transfers(transfer_code);
CREATE INDEX idx_transfers_sender ON transfers(sender_id);
CREATE INDEX idx_transfers_status ON transfers(status);
CREATE INDEX idx_transfers_created ON transfers(created_at);
CREATE INDEX idx_fee_grid_corridor ON fee_grid(corridor_id);
CREATE INDEX idx_fee_grid_active ON fee_grid(active);
CREATE INDEX idx_corridor_countries ON corridor(source_country, destination_country);

-- ═══════════════════════════════════════════════════════════════
-- Données de test
-- ═══════════════════════════════════════════════════════════════

-- Insérer les devises
INSERT INTO currency (code, name, symbol, active) VALUES
('EUR', 'Euro', '€', true),
('USD', 'Dollar US', '$', true),
('XOF', 'Franc CFA', 'CFA', true),
('GBP', 'Livre Sterling', '£', true)
ON CONFLICT DO NOTHING;

-- Insérer une agence
INSERT INTO agency (name, city, country, phone, email, active) VALUES
('Agence Paris', 'Paris', 'FR', '+33123456789', 'paris@okane.com', true)
ON CONFLICT DO NOTHING;

-- Insérer un agent
INSERT INTO users (username, email, password, phone, role, enabled, dtype) VALUES
('agent_paris', 'agent@okane.com', '$2a$10$...', '+33612345678', 'AGENT', true, 'Agent')
ON CONFLICT DO NOTHING;

INSERT INTO agent (id, agency_id, active) VALUES
(1, 1, true)
ON CONFLICT DO NOTHING;

-- Insérer un corridor (France → Sénégal)
INSERT INTO corridor (source_country, destination_country, source_currency_id, destination_currency_id, active) VALUES
('FR', 'SN', 1, 3, true)
ON CONFLICT DO NOTHING;

-- Insérer une grille tarifaire
INSERT INTO fee_grid (corridor_id, min_amount, max_amount, fixed_fee, percentage_fee, agency_share, central_share, active) VALUES
(1, 0.00, 1000.00, 5.00, 2.5, 60.00, 40.00, true),
(1, 1000.01, 5000.00, 10.00, 2.0, 60.00, 40.00, true),
(1, 5000.01, 999999.99, 20.00, 1.5, 60.00, 40.00, true)
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════
-- Vérification des données
-- ═══════════════════════════════════════════════════════════════

-- Vérifier les devises
SELECT * FROM currency;

-- Vérifier les agences
SELECT * FROM agency;

-- Vérifier les agents
SELECT u.username, u.email, a.agency_id FROM users u
JOIN agent a ON u.id = a.id;

-- Vérifier les corridors
SELECT c.id, c.source_country, c.destination_country, 
       sc.code as source_currency, dc.code as destination_currency
FROM corridor c
JOIN currency sc ON c.source_currency_id = sc.id
JOIN currency dc ON c.destination_currency_id = dc.id;

-- Vérifier les grilles tarifaires
SELECT fg.id, c.source_country, c.destination_country,
       fg.min_amount, fg.max_amount, fg.fixed_fee, fg.percentage_fee
FROM fee_grid fg
JOIN corridor c ON fg.corridor_id = c.id;

-- ═══════════════════════════════════════════════════════════════
-- Requêtes utiles pour la soutenance
-- ═══════════════════════════════════════════════════════════════

-- Voir tous les transferts
SELECT * FROM transfers ORDER BY created_at DESC;

-- Voir les transferts d'un agent
SELECT * FROM transfers WHERE sender_id = 1 ORDER BY created_at DESC;

-- Voir les transferts par statut
SELECT status, COUNT(*) as count FROM transfers GROUP BY status;

-- Voir les transferts par corridor
SELECT c.source_country, c.destination_country, COUNT(*) as count
FROM transfers t
JOIN agency a ON t.agency_id = a.id
JOIN corridor c ON a.id = c.id
GROUP BY c.source_country, c.destination_country;

-- Voir le montant total transféré
SELECT SUM(amount) as total_amount, COUNT(*) as count FROM transfers;

-- Voir les frais générés
SELECT SUM(amount * 0.025) as total_fees FROM transfers;
