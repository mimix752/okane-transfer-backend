-- Migration: fix contraintes transfers suite au merge Espace_Admin_Devise

-- 1. Supprimer le check constraint sur target_currency (varchar -> bigint FK)
ALTER TABLE transfers DROP CONSTRAINT IF EXISTS transfers_target_currency_check;

-- 2. Migrer target_currency de varchar vers bigint (FK vers currency.id)
UPDATE transfers SET target_currency = NULL WHERE target_currency IS NOT NULL;
ALTER TABLE transfers ALTER COLUMN target_currency TYPE bigint USING NULL;

-- 3. Mettre a jour le check constraint sur status pour inclure les nouveaux statuts
ALTER TABLE transfers DROP CONSTRAINT IF EXISTS transfers_status_check;
ALTER TABLE transfers ADD CONSTRAINT transfers_status_check
CHECK (status IN ('PENDING', 'VALIDATED', 'PAID', 'CANCELLED', 'EXPIRED', 'MOBILE_SENT', 'RECONCILED'));

-- 4. Supprimer l'ancienne colonne transfercode si elle existe encore
ALTER TABLE transfers DROP COLUMN IF EXISTS transfercode;
