# Guide de Test - Fonctionnalités Nouvelles

## 1. GESTION DES DEVISES MULTIPLES

### Test 1.1 : Vérifier les taux de change actifs
```bash
curl -X GET "http://localhost:8080/okane-transfer/api/currencies/rates" \
  -H "Authorization: Bearer <token>"
```
**Résultat attendu** : Liste des taux de change avec fromCurrency, toCurrency, rate

### Test 1.2 : Convertir un montant
```bash
curl -X GET "http://localhost:8080/okane-transfer/api/currencies/convert?amount=1000&from=EUR&to=XOF" \
  -H "Authorization: Bearer <token>"
```
**Résultat attendu** : Montant converti (ex: 655957.50 XOF)

### Test 1.3 : Obtenir un taux de change spécifique
```bash
curl -X GET "http://localhost:8080/okane-transfer/api/currencies/rate?from=EUR&to=USD" \
  -H "Authorization: Bearer <token>"
```
**Résultat attendu** : Taux de change (ex: 1.08)

### Test 1.4 : Mettre à jour un taux (ADMIN)
```bash
curl -X POST "http://localhost:8080/okane-transfer/api/currencies/rates?from=EUR&to=USD&rate=1.09" \
  -H "Authorization: Bearer <admin_token>"
```
**Résultat attendu** : Nouveau taux créé avec active=true, ancien désactivé

### Test 1.5 : Créer un transfert avec conversion
```bash
curl -X POST "http://localhost:8080/okane-transfer/api/envoi/create" \
  -H "Authorization: Bearer <agent_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "senderName": "Jean",
    "senderFirstName": "Dupont",
    "senderCIN": "AB123456",
    "senderPhone": "+33612345678",
    "senderCountry": "FR",
    "recipientName": "Ahmed",
    "recipientFirstName": "Hassan",
    "recipientPhone": "+212612345678",
    "recipientCountry": "MA",
    "amount": 1000,
    "currency": "EUR",
    "corridorId": 1
  }'
```
**Résultat attendu** : 
- Transfer créé avec convertedAmount en devise de destination
- Reçu imprimé (voir logs)
- Montant débité de l'agence

---

## 2. AUDIT TRAIL DES AGENTS

### Test 2.1 : Consulter l'audit trail d'un agent
```bash
curl -X GET "http://localhost:8080/okane-transfer/api/audit/agents/1" \
  -H "Authorization: Bearer <admin_token>"
```
**Résultat attendu** : Liste des actions de l'agent avec timestamps, IP, descriptions

### Test 2.2 : Audit trail avec filtre de dates
```bash
curl -X GET "http://localhost:8080/okane-transfer/api/audit/agents/1?from=2024-01-01T00:00:00&to=2024-01-31T23:59:59" \
  -H "Authorization: Bearer <admin_token>"
```
**Résultat attendu** : Actions filtrées par période

### Test 2.3 : Audit trail d'une entité spécifique
```bash
curl -X GET "http://localhost:8080/okane-transfer/api/audit/agents/entity/Transfer/123" \
  -H "Authorization: Bearer <admin_token>"
```
**Résultat attendu** : Toutes les actions liées au transfert #123

### Test 2.4 : Compteur d'activité agent
```bash
curl -X GET "http://localhost:8080/okane-transfer/api/audit/agents/activity/1?since=2024-01-01T00:00:00" \
  -H "Authorization: Bearer <admin_token>"
```
**Résultat attendu** : Nombre d'actions depuis la date spécifiée

### Test 2.5 : Audit trail récent (dernières 24h)
```bash
curl -X GET "http://localhost:8080/okane-transfer/api/audit/agents/recent?hours=24" \
  -H "Authorization: Bearer <admin_token>"
```
**Résultat attendu** : Toutes les actions des 24 dernières heures

### Vérifier dans la base de données
```sql
SELECT * FROM agent_audit_trail 
WHERE agent_id = 1 
ORDER BY created_at DESC 
LIMIT 10;
```
**Colonnes à vérifier** :
- actionType (CREATE_TRANSFER, WITHDRAW_TRANSFER, LOGIN, etc.)
- ipAddress (adresse IP de l'agent)
- userAgent (navigateur/client)
- oldValues, newValues (JSON des modifications)
- createdAt (timestamp exact)

---

## 3. IMPRESSION DE REÇUS

### Test 3.1 : Vérifier l'impression du reçu de transfert
**Action** : Créer un transfert (voir Test 1.5)

**Vérifier dans les logs** :
```
=== IMPRESSION REÇU ===
===============================================
           OKANE TRANSFER - REÇU
===============================================

Date: 15/01/2024 14:30:45
Code de transfert: TRF20240115143045ABC
...
```

### Test 3.2 : Vérifier l'impression du reçu de retrait
```bash
curl -X POST "http://localhost:8080/okane-transfer/api/retrait/retirer" \
  -H "Authorization: Bearer <agent_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "transferCode": "TRF20240115143045ABC",
    "recipientPhone": "+212612345678"
  }'
```

**Vérifier dans les logs** :
```
=== IMPRESSION REÇU ===
===============================================
        OKANE TRANSFER - RETRAIT
===============================================

Date de retrait: 15/01/2024 14:35:20
Code de transfert: TRF20240115143045ABC
...
```

### Test 3.3 : Vérifier le contenu du reçu
Le reçu doit contenir :
- ✓ Date et heure
- ✓ Code de transfert unique
- ✓ Informations expéditeur/bénéficiaire
- ✓ Montants (original + converti si applicable)
- ✓ Frais
- ✓ Statut du transfert
- ✓ Instructions pour le bénéficiaire

---

## 4. TESTS INTÉGRÉS (Scénario complet)

### Scénario : Transfert EUR → MAD avec audit et reçu

**Étape 1** : Vérifier les taux
```bash
curl -X GET "http://localhost:8080/okane-transfer/api/currencies/rate?from=EUR&to=MAD" \
  -H "Authorization: Bearer <token>"
```

**Étape 2** : Créer le transfert
```bash
curl -X POST "http://localhost:8080/okane-transfer/api/envoi/create" \
  -H "Authorization: Bearer <agent_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "senderName": "Pierre",
    "senderFirstName": "Martin",
    "senderCIN": "FR123456",
    "senderPhone": "+33612345678",
    "senderCountry": "FR",
    "recipientName": "Mohammed",
    "recipientFirstName": "Ali",
    "recipientPhone": "+212612345678",
    "recipientCountry": "MA",
    "amount": 500,
    "currency": "EUR",
    "corridorId": 1
  }'
```

**Étape 3** : Vérifier l'audit trail
```bash
curl -X GET "http://localhost:8080/okane-transfer/api/audit/agents/1" \
  -H "Authorization: Bearer <admin_token>"
```
**Vérifier** : Action CREATE_TRANSFER avec IP, timestamp, valeurs

**Étape 4** : Vérifier la base de données
```sql
-- Vérifier le transfert
SELECT id, transfer_code, amount, currency, converted_amount, target_currency 
FROM transfers 
WHERE id = (SELECT MAX(id) FROM transfers);

-- Vérifier l'audit
SELECT action_type, description, ip_address, created_at 
FROM agent_audit_trail 
WHERE entity_type = 'Transfer' 
ORDER BY created_at DESC LIMIT 1;

-- Vérifier le solde de l'agence
SELECT current_balance FROM agency WHERE id = 1;
```

**Étape 5** : Effectuer le retrait
```bash
curl -X POST "http://localhost:8080/okane-transfer/api/retrait/retirer" \
  -H "Authorization: Bearer <agent_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "transferCode": "<transfer_code_from_step_2>",
    "recipientPhone": "+212612345678"
  }'
```

**Étape 6** : Vérifier l'audit du retrait
```bash
curl -X GET "http://localhost:8080/okane-transfer/api/audit/agents/1" \
  -H "Authorization: Bearer <admin_token>"
```
**Vérifier** : Action WITHDRAW_TRANSFER

---

## 5. VÉRIFICATIONS EN BASE DE DONNÉES

### Tables à vérifier

**1. currency_rate**
```sql
SELECT * FROM currency_rate WHERE active = true;
```
Doit afficher les taux actifs avec from_currency, to_currency, rate

**2. agent_audit_trail**
```sql
SELECT * FROM agent_audit_trail ORDER BY created_at DESC LIMIT 20;
```
Doit afficher toutes les actions avec détails complets

**3. transfers**
```sql
SELECT id, transfer_code, amount, currency, converted_amount, target_currency, sender_cin 
FROM transfers 
ORDER BY created_at DESC LIMIT 5;
```
Doit afficher les montants convertis

**4. agency**
```sql
SELECT id, name, current_balance, daily_limit 
FROM agency;
```
Doit afficher les soldes mis à jour

---

## 6. LOGS À VÉRIFIER

### Logs d'impression de reçu
```
=== IMPRESSION REÇU ===
===============================================
           OKANE TRANSFER - REÇU
===============================================
```

### Logs de conversion
```
Conversion: 500 EUR -> 5500 MAD (taux: 11.00)
```

### Logs d'audit
```
Agent: agent1 | Action: CREATE_TRANSFER | IP: 192.168.1.100 | Timestamp: 2024-01-15 14:30:45
```

---

## 7. CHECKLIST DE VALIDATION

- [ ] Taux de change affichés correctement
- [ ] Conversion automatique lors du transfert
- [ ] Montant converti stocké en base
- [ ] Audit trail créé pour chaque action
- [ ] IP et User-Agent capturés
- [ ] Reçu imprimé après transfert
- [ ] Reçu imprimé après retrait
- [ ] Solde de l'agence débité/crédité
- [ ] Dates et heures correctes
- [ ] Statuts de transfert corrects

---

## 8. DÉPANNAGE

### Pas de reçu imprimé
- Vérifier les logs de l'application
- Vérifier que ReceiptPrintingService est injecté
- Vérifier que la méthode printReceipt() est appelée

### Audit trail vide
- Vérifier que AgentAuditService est injecté
- Vérifier que SecurityUtils.getCurrentUserId() retourne une valeur
- Vérifier la table agent_audit_trail en base

### Conversion échouée
- Vérifier que les taux de change existent en base
- Vérifier que les codes de devise sont corrects (EUR, USD, MAD, XOF)
- Vérifier les logs d'erreur CurrencyConversionService
