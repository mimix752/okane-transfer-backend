# 🧪 GUIDE COMPLET DE TESTS - Okane Transfer

## 📋 Prérequis

1. **PostgreSQL 16** démarré
2. **Tomcat 10.1+** démarré
3. **Application déployée** sur Tomcat
4. **Postman** ou **curl** pour tester

---

## 🚀 Étape 1 : Initialiser la base de données

### 1.1 Créer la base de données
```bash
createdb okane_transfer
```

### 1.2 Exécuter le schéma
```bash
psql -U postgres -d okane_transfer -f src/main/resources/db/schema.sql
```

### 1.3 Insérer les données de test
```bash
psql -U postgres -d okane_transfer -f src/main/resources/db/init_data.sql
```

### 1.4 Vérifier les données
```bash
psql -U postgres -d okane_transfer -c "SELECT * FROM users;"
```

---

## 🔐 Étape 2 : Tester l'authentification

### 2.1 Register (Créer un nouvel utilisateur)

**Requête :**
```bash
curl -X POST http://localhost:8080/okane-transfer/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "nouveau_user",
    "email": "nouveau@okane.com",
    "password": "password123",
    "phone": "+33612345678"
  }'
```

**Réponse attendue (201 Created) :**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "nouveau_user",
    "email": "nouveau@okane.com",
    "role": "CLIENT"
  }
}
```

### 2.2 Login (Se connecter)

**Requête :**
```bash
curl -X POST http://localhost:8080/okane-transfer/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "agent_paris",
    "password": "password123"
  }'
```

**Réponse attendue (200 OK) :**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "agent_paris",
    "email": "agent@okane.com",
    "role": "AGENT"
  }
}
```

**⚠️ IMPORTANT : Copier le token pour les requêtes suivantes**

---

## 📤 Étape 3 : Tester la création d'un envoi (Tâche 3.2)

### 3.1 Créer un transfert

**Requête :**
```bash
curl -X POST http://localhost:8080/okane-transfer/api/envoi \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "senderName": "Dupont",
    "senderFirstName": "Jean",
    "senderCIN": "AB123456",
    "senderPhone": "+33612345678",
    "senderCountry": "FR",
    "recipientName": "Diallo",
    "recipientFirstName": "Mamadou",
    "recipientPhone": "+221771234567",
    "recipientCountry": "SN",
    "amount": 500.00,
    "currency": "EUR",
    "corridorId": 1
  }'
```

**Réponse attendue (201 Created) :**
```json
{
  "success": true,
  "message": "Transfert créé avec succès",
  "data": {
    "id": 1,
    "transferCode": "A7K2M9X1",
    "senderName": "agent_paris",
    "recipientName": "Diallo Mamadou",
    "amount": 500.00,
    "currency": "EUR",
    "fees": 15.00,
    "netAmount": 485.00,
    "status": "PENDING",
    "createdAt": "2024-01-15T10:30:00",
    "receipt": "═══════════════════════════════════════\n         REÇU DE TRANSFERT D'ARGENT\n═══════════════════════════════════════\n..."
  }
}
```

---

## 📥 Étape 4 : Tester la récupération des transferts

### 4.1 Récupérer tous les transferts

**Requête :**
```bash
curl -X GET http://localhost:8080/okane-transfer/api/transfers \
  -H "Authorization: Bearer <TOKEN>"
```

**Réponse attendue (200 OK) :**
```json
{
  "success": true,
  "message": "Transfers retrieved",
  "data": [
    {
      "id": 1,
      "transferCode": "A7K2M9X1",
      "senderUsername": "agent_paris",
      "recipientName": "Diallo Mamadou",
      "amount": 500.00,
      "currency": "EUR",
      "status": "PENDING",
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

### 4.2 Récupérer un transfert par ID

**Requête :**
```bash
curl -X GET http://localhost:8080/okane-transfer/api/transfers/1 \
  -H "Authorization: Bearer <TOKEN>"
```

### 4.3 Récupérer un transfert par code

**Requête :**
```bash
curl -X GET http://localhost:8080/okane-transfer/api/transfers/code/A7K2M9X1 \
  -H "Authorization: Bearer <TOKEN>"
```

### 4.4 Récupérer les transferts d'un expéditeur

**Requête :**
```bash
curl -X GET http://localhost:8080/okane-transfer/api/transfers/sender/2 \
  -H "Authorization: Bearer <TOKEN>"
```

### 4.5 Récupérer les transferts par statut

**Requête :**
```bash
curl -X GET http://localhost:8080/okane-transfer/api/transfers/status/PENDING \
  -H "Authorization: Bearer <TOKEN>"
```

---

## ✏️ Étape 5 : Tester la mise à jour du statut

### 5.1 Changer le statut d'un transfert

**Requête :**
```bash
curl -X PATCH http://localhost:8080/okane-transfer/api/transfers/1/status?status=COMPLETED \
  -H "Authorization: Bearer <TOKEN>"
```

**Réponse attendue (200 OK) :**
```json
{
  "success": true,
  "message": "Transfer status updated",
  "data": {
    "id": 1,
    "transferCode": "A7K2M9X1",
    "status": "COMPLETED",
    "updatedAt": "2024-01-15T10:35:00"
  }
}
```

---

## ❌ Étape 6 : Tester les cas d'erreur

### 6.1 Erreur 400 - Données invalides

**Requête :**
```bash
curl -X POST http://localhost:8080/okane-transfer/api/envoi \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "senderName": "Dupont",
    "amount": -100.00,
    "currency": "EUR",
    "corridorId": 1
  }'
```

**Réponse attendue (400 Bad Request) :**
```json
{
  "success": false,
  "message": "Validation error",
  "errors": {
    "amount": "Amount must be positive",
    "senderFirstName": "Sender first name is required"
  }
}
```

### 6.2 Erreur 401 - Pas d'authentification

**Requête :**
```bash
curl -X GET http://localhost:8080/okane-transfer/api/transfers
```

**Réponse attendue (401 Unauthorized) :**
```json
{
  "success": false,
  "message": "Unauthorized"
}
```

### 6.3 Erreur 404 - Ressource non trouvée

**Requête :**
```bash
curl -X GET http://localhost:8080/okane-transfer/api/transfers/999 \
  -H "Authorization: Bearer <TOKEN>"
```

**Réponse attendue (404 Not Found) :**
```json
{
  "success": false,
  "message": "Transfer not found"
}
```

---

## 📊 Étape 7 : Vérifier les données en BD

### 7.1 Vérifier les utilisateurs
```sql
SELECT id, username, email, role FROM users;
```

### 7.2 Vérifier les transferts
```sql
SELECT * FROM transfers ORDER BY created_at DESC;
```

### 7.3 Vérifier les frais appliqués
```sql
SELECT * FROM fee_grid WHERE corridor_id = 1;
```

### 7.4 Vérifier les corridors
```sql
SELECT c.id, c.source_country, c.destination_country, 
       sc.code as source_currency, dc.code as destination_currency
FROM corridor c
JOIN currency sc ON c.source_currency_id = sc.id
JOIN currency dc ON c.destination_currency_id = dc.id;
```

---

## 🎯 Checklist de validation

- [ ] Register fonctionne (201 Created)
- [ ] Login fonctionne (200 OK + token)
- [ ] Créer un transfert fonctionne (201 Created)
- [ ] Récupérer tous les transferts fonctionne (200 OK)
- [ ] Récupérer un transfert par ID fonctionne (200 OK)
- [ ] Récupérer un transfert par code fonctionne (200 OK)
- [ ] Changer le statut fonctionne (200 OK)
- [ ] Erreur 400 retournée correctement
- [ ] Erreur 401 retournée correctement
- [ ] Erreur 404 retournée correctement
- [ ] Données sauvegardées en BD
- [ ] Frais calculés correctement
- [ ] Code retrait généré et unique

---

## 📝 Notes importantes

1. **Token JWT** : Valide 24 heures (86400000 ms)
2. **Mot de passe par défaut** : `password123` (hashé en BCrypt)
3. **Frais** : Calculés selon la grille tarifaire du corridor
4. **Code retrait** : Généré aléatoirement (8 caractères alphanumérique)
5. **Statut par défaut** : PENDING

---

## 🔗 Endpoints disponibles

| Méthode | URL | Auth | Description |
|---------|-----|------|-------------|
| POST | `/api/auth/register` | Non | Créer un utilisateur |
| POST | `/api/auth/login` | Non | Se connecter |
| POST | `/api/envoi` | JWT | Créer un transfert |
| GET | `/api/transfers` | JWT | Lister les transferts |
| GET | `/api/transfers/{id}` | JWT | Détail par ID |
| GET | `/api/transfers/code/{code}` | JWT | Détail par code |
| GET | `/api/transfers/sender/{senderId}` | JWT | Transferts d'un expéditeur |
| GET | `/api/transfers/status/{status}` | JWT | Transferts par statut |
| PATCH | `/api/transfers/{id}/status` | JWT | Changer le statut |

---

**Auteur** : Okane Transfer Team  
**Date** : 2024  
**Version** : 1.0
