# 📖 README - Tâche 3.2 Enregistrement d'un envoi

## 🎯 Objectif

Permettre à un **agent d'une agence** d'enregistrer un transfert d'argent en saisissant les informations de l'expéditeur, du bénéficiaire, du montant et de la devise. Le système calcule automatiquement les frais, génère un code de retrait unique et envoie un reçu.

---

## 📁 Fichiers créés

### **Code source**
```
src/main/java/com/okanetransfer/
├── dto/
│   ├── request/
│   │   └── EnvoiRequestDTO.java
│   └── response/
│       └── EnvoiResponseDTO.java
├── service/
│   ├── envoi/
│   │   ├── EnvoiService.java
│   │   └── TransferCodeService.java
│   └── NotificationService.java
└── controller/
    └── envoi/
        └── EnvoiController.java
```

### **Documentation**
```
├── TASK_3_2_DOCUMENTATION.md    (Documentation technique complète)
├── TASK_3_2_TESTS.md            (Guide de test)
├── TASK_3_2_SCHEMA.sql          (Schéma BD)
├── TASK_3_2_RESUME.md           (Résumé exécutif)
├── TASK_3_2_DIAGRAMS.md         (Diagrammes d'architecture)
├── TASK_3_2_PRESENTATION.md     (Guide de présentation)
└── README.md                    (Ce fichier)
```

---

## 🚀 Démarrage rapide

### **1. Prérequis**

- JDK 21
- Maven 3.8+
- PostgreSQL 16
- Tomcat 10.1+

### **2. Configuration de la base de données**

```bash
# Créer la base de données
createdb okane_transfer

# Exécuter les scripts SQL
psql -U postgres -d okane_transfer -f src/main/resources/db/schema.sql
psql -U postgres -d okane_transfer -f src/main/resources/db/task_3_2_schema.sql
```

### **3. Configuration de l'application**

```bash
# Copier le fichier de configuration
cp src/main/resources/application.example.properties src/main/resources/application.properties

# Éditer application.properties avec vos valeurs
# db.url=jdbc:postgresql://localhost:5432/okane_transfer
# db.username=postgres
# db.password=votre_mot_de_passe
# jwt.secret=votre_secret_min_32_caracteres
```

### **4. Build et déploiement**

```bash
# Build
mvn clean package

# Déployer sur Tomcat
cp target/okane-transfer.war $TOMCAT_HOME/webapps/

# Démarrer Tomcat
$TOMCAT_HOME/bin/startup.sh
```

### **5. Vérifier que l'application est démarrée**

```bash
curl http://localhost:8080/okane-transfer/swagger-ui/index.html
```

---

## 🧪 Tests

### **Test 1 : Obtenir un token JWT**

```bash
curl -X POST http://localhost:8080/okane-transfer/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "agent_paris",
    "password": "password123"
  }'
```

**Réponse** :
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "agent_paris",
    "role": "AGENT"
  }
}
```

### **Test 2 : Créer un transfert**

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

**Réponse** :
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

### **Test 3 : Vérifier les données en BD**

```sql
-- Vérifier le transfert créé
SELECT * FROM transfers ORDER BY created_at DESC LIMIT 1;

-- Vérifier les frais appliqués
SELECT * FROM fee_grid WHERE corridor_id = 1;

-- Vérifier le corridor
SELECT * FROM corridor WHERE id = 1;
```

---

## 📊 Architecture

### **Flux d'un envoi**

```
Agent saisit formulaire
    ↓
POST /api/envoi + JWT token
    ↓
EnvoiController valide et authentifie
    ↓
EnvoiService orchestre :
├─ Récupère agent, corridor
├─ Calcule frais (FeeGridService)
├─ Génère code retrait (TransferCodeService)
├─ Crée Transfer en BD
└─ Envoie notifications (NotificationService)
    ↓
Retour EnvoiResponseDTO (201 Created)
    ↓
Affichage à l'agent
```

### **Entités utilisées**

- **Transfer** : Transfert d'argent
- **Corridor** : Route pays source → destination
- **FeeGrid** : Grille tarifaire
- **User/Agent** : Utilisateur/Agent
- **Agency** : Agence
- **Currency** : Devise

---

## 🔐 Sécurité

### **Authentification JWT**
- Token dans le header : `Authorization: Bearer <token>`
- Extraction de l'ID agent depuis le token

### **Autorisation par rôle**
- Endpoint protégé : `@PreAuthorize("hasRole('AGENT')")`
- Seuls les agents peuvent créer des transferts

### **Validations des données**
- Annotations Jakarta (@NotBlank, @Pattern, @Positive, etc.)
- Vérification que le corridor existe et est actif
- Vérification que la grille tarifaire existe

---

## 📚 Documentation

- **TASK_3_2_DOCUMENTATION.md** : Documentation technique complète
- **TASK_3_2_TESTS.md** : Guide de test avec exemples
- **TASK_3_2_SCHEMA.sql** : Schéma BD et données de test
- **TASK_3_2_RESUME.md** : Résumé exécutif
- **TASK_3_2_DIAGRAMS.md** : Diagrammes d'architecture
- **TASK_3_2_PRESENTATION.md** : Guide de présentation pour la soutenance

---

## 🎓 Concepts clés

### **DTO (Data Transfer Object)**
- Sépare les données d'entrée/sortie de l'entité BD
- Évite d'exposer toute la structure BD
- Validation centralisée avec annotations

### **Service (Logique métier)**
- Orchestre les opérations
- Appelle plusieurs repositories et services
- Gestion des transactions (@Transactional)

### **Controller (API REST)**
- Reçoit requêtes HTTP
- Retourne réponses structurées
- Codes HTTP appropriés (201, 400, 401, 403, 404)

### **Sécurité (JWT)**
- Authentification : Vérifier que l'utilisateur est connecté
- Autorisation : Vérifier que l'utilisateur a le rôle AGENT
- Validations des données

### **Transactions**
- @Transactional : Garantir cohérence BD
- Rollback : Si une étape échoue, tout est annulé

---

## 🚀 Améliorations futures

1. **Intégration SMS** : Twilio, AWS SNS
2. **Intégration Email** : JavaMail, SendGrid
3. **Paiement** : Passerelle paiement (Stripe, PayPal)
4. **Conversion devises** : Taux de change en temps réel
5. **Audit** : Journalisation complète des actions
6. **Notifications** : WebSocket pour notifications en temps réel
7. **Limites de transfert** : Vérifier les limites par agent/agence
8. **Frais dynamiques** : Basés sur le montant et le corridor

---

## 📞 Support

Pour toute question ou problème, consultez :
- La documentation technique : `TASK_3_2_DOCUMENTATION.md`
- Le guide de test : `TASK_3_2_TESTS.md`
- Le guide de présentation : `TASK_3_2_PRESENTATION.md`

---

## ✅ Checklist de validation

- [x] DTOs créés et validés
- [x] Services implémentés
- [x] Controller créé
- [x] Authentification JWT vérifiée
- [x] Autorisation AGENT vérifiée
- [x] Calcul des frais fonctionnel
- [x] Génération code retrait fonctionnelle
- [x] Notifications implémentées
- [x] Données sauvegardées en BD
- [x] Tests effectués
- [x] Documentation complète
- [x] Logs pour traçabilité

---

**Auteur** : Okane Transfer Team  
**Date** : 2024  
**Version** : 1.0  
**Statut** : ✅ Complété et prêt pour la soutenance
