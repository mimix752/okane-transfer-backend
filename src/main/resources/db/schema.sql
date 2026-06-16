-- ─────────────────────────────────────────
-- USERS (base table with inheritance JOINED)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
                                     id          BIGSERIAL PRIMARY KEY,
                                     username    VARCHAR(100) NOT NULL UNIQUE,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    role        VARCHAR(20)  NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP             DEFAULT NOW()
    );

-- ─────────────────────────────────────────
-- AGENCY (anciennement "agencies")
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS agency (
                                      id                  BIGSERIAL PRIMARY KEY,
                                      name                VARCHAR(150)   NOT NULL,
    address             VARCHAR(255),
    country             VARCHAR(100),
    daily_limit         NUMERIC(15,2),
    current_balance     NUMERIC(15,2)  NOT NULL DEFAULT 0,
    active              BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP               DEFAULT NOW()
    );

-- ─────────────────────────────────────────
-- AGENT (joined inheritance depuis users)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS agent (
                                     id          BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE RESTRICT,
    agency_id   BIGINT  NOT NULL REFERENCES agency(id) ON DELETE RESTRICT,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP          DEFAULT NOW()
    );

-- ─────────────────────────────────────────
-- JOURNAL AUDIT (immuable, pas de UPDATE)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS journal_audit (
                                             id           BIGSERIAL PRIMARY KEY,
                                             performed_by VARCHAR(100) NOT NULL,
    action       VARCHAR(100) NOT NULL,
    entity_type  VARCHAR(100),
    entity_id    BIGINT,
    performed_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    details      TEXT
    );

-- trigger : interdit tout UPDATE sur journal_audit
CREATE OR REPLACE FUNCTION prevent_audit_update()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'UPDATE not allowed on journal_audit';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER no_update_journal_audit
    BEFORE UPDATE ON journal_audit
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_update();

-- ─────────────────────────────────────────
-- CURRENCY RATES
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS currency_rates (
                                              id            BIGSERIAL PRIMARY KEY,
                                              from_currency VARCHAR(10)   NOT NULL,
    to_currency   VARCHAR(10)   NOT NULL,
    rate          NUMERIC(18,6) NOT NULL,
    updated_at    TIMESTAMP              DEFAULT NOW()
    );

-- ─────────────────────────────────────────
-- TRANSFERS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS transfers (
                                         id               BIGSERIAL PRIMARY KEY,
                                         transfer_code    VARCHAR(20)   NOT NULL UNIQUE,
    sender_id        BIGINT        NOT NULL REFERENCES users(id)  ON DELETE RESTRICT,
    recipient_name   VARCHAR(150)  NOT NULL,
    recipient_phone  VARCHAR(20)   NOT NULL,
    amount           NUMERIC(18,2) NOT NULL,
    currency         VARCHAR(10)   NOT NULL,
    converted_amount NUMERIC(18,2),
    target_currency  VARCHAR(10),
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    agency_id        BIGINT        REFERENCES agency(id) ON DELETE RESTRICT,
    created_at       TIMESTAMP              DEFAULT NOW(),
    updated_at       TIMESTAMP              DEFAULT NOW()
    );