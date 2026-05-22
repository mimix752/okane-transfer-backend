-- Admin par défaut (password: admin123 encodé BCrypt)
INSERT INTO users (username, email, password, role, enabled)
VALUES ('admin', 'admin@okanetransfer.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', true)
ON CONFLICT DO NOTHING;

-- Agence de test
INSERT INTO agencies (name, address, city, country, phone, active)
VALUES ('Agence Centrale', '12 Rue du Commerce', 'Conakry', 'Guinée', '+224620000000', true)
ON CONFLICT DO NOTHING;

-- Taux de change initiaux
INSERT INTO currency_rates (from_currency, to_currency, rate, updated_at)
VALUES
    ('EUR', 'XOF', 655.957, NOW()),
    ('USD', 'XOF', 610.000, NOW()),
    ('XOF', 'EUR', 0.00152, NOW()),
    ('EUR', 'GNF', 9800.000, NOW()),
    ('USD', 'GNF', 9100.000, NOW())
ON CONFLICT DO NOTHING;
