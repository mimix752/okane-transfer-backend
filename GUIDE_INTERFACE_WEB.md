# Guide d'Utilisation - Interface Web Agent

## 🚀 Démarrage

### 1. Compiler et déployer
```bash
mvn clean package
# Copier le WAR dans Tomcat
cp target/okane-transfer.war $TOMCAT_HOME/webapps/okane_transfer_war.war
# Redémarrer Tomcat
```

### 2. Accéder à l'application
```
http://localhost:8080/okane_transfer_war/
```

Vous serez automatiquement redirigé vers la page de login.

---

## 👤 Authentification

### Identifiants de test

**Agent:**
- Username: `Maroua`
- Password: `Maroua123`

**Admin:**
- Username: `admin`
- Password: `admin123`

---

## 📋 Interface Agent

### Onglet 1: Créer un Transfert

**Étapes:**
1. Remplir les informations de l'expéditeur
   - Nom, Prénom
   - CIN/Passeport
   - Téléphone
   - Pays

2. Remplir les informations du bénéficiaire
   - Nom, Prénom
   - Téléphone
   - Pays

3. Entrer le montant et la devise
   - Montant (EUR, USD, GBP)
   - Sélectionner le corridor

4. Cliquer sur "Créer le Transfert"

**Résultat:**
- ✓ Transfert créé avec code unique
- ✓ Reçu affiché à l'écran
- ✓ Montant converti automatiquement
- ✓ Audit trail enregistré

### Onglet 2: Effectuer un Retrait

**Étapes:**
1. Entrer le code de transfert (obtenu lors de la création)
2. Entrer le téléphone du bénéficiaire
3. Cliquer sur "Effectuer le Retrait"

**Résultat:**
- ✓ Retrait effectué
- ✓ Statut du transfert = COMPLETED
- ✓ Reçu affiché
- ✓ Audit trail enregistré

### Onglet 3: Historique

Affiche tous les transferts créés par l'agent avec:
- Code de transfert
- Noms expéditeur/bénéficiaire
- Montants
- Statut (PENDING, COMPLETED, CANCELLED)
- Date de création

---

## 👨💼 Interface Admin

### Onglet 1: Audit Trail

**Fonctionnalités:**
1. Entrer l'ID de l'agent (ex: 1)
2. Cliquer sur "Charger l'Audit"
3. Voir toutes les actions de l'agent:
   - CREATE_TRANSFER
   - WITHDRAW_TRANSFER
   - LOGIN
   - UPDATE

**Informations affichées:**
- Date/Heure exacte
- Type d'action
- Description
- Adresse IP
- Anciennes/Nouvelles valeurs (JSON)

### Onglet 2: Agents

Liste de tous les agents avec:
- ID
- Nom d'utilisateur
- Rôle
- Statut
- Date de création

### Onglet 3: Transferts

Liste de tous les transferts du système avec:
- Code de transfert
- Expéditeur/Bénéficiaire
- Montants
- Devise
- Statut
- Date

---

## 🧪 Scénario de Test Complet

### Étape 1: Login Agent
1. Aller à `http://localhost:8080/okane_transfer_war/`
2. Entrer: `agent1` / `agent123`
3. Cliquer "Se connecter"

### Étape 2: Créer un Transfert
1. Onglet "Créer un Transfert"
2. Remplir le formulaire:
   - Expéditeur: Jean Dupont, AB123456, +33612345678, FR
   - Bénéficiaire: Ahmed Hassan, +212612345678, MA
   - Montant: 1000 EUR
   - Corridor: France → Maroc

3. Cliquer "Créer le Transfert"
4. **Vérifier:**
   - ✓ Message de succès
   - ✓ Code de transfert généré (ex: TRF20240115143045ABC)
   - ✓ Reçu affiché
   - ✓ Montant converti (1000 EUR → 11000 MAD)

### Étape 3: Effectuer un Retrait
1. Onglet "Effectuer un Retrait"
2. Entrer:
   - Code de transfert: (copier du reçu)
   - Téléphone: +212612345678

3. Cliquer "Effectuer le Retrait"
4. **Vérifier:**
   - ✓ Message de succès
   - ✓ Reçu de retrait affiché
   - ✓ Statut = COMPLETED

### Étape 4: Vérifier l'Historique
1. Onglet "Historique"
2. **Vérifier:**
   - ✓ Transfert visible dans la liste
   - ✓ Statut = COMPLETED
   - ✓ Montants corrects

### Étape 5: Vérifier l'Audit (Admin)
1. Logout (agent)
2. Login avec: `admin` / `admin123`
3. Onglet "Audit Trail"
4. Entrer Agent ID: 1
5. Cliquer "Charger l'Audit"
6. **Vérifier:**
   - ✓ Action CREATE_TRANSFER visible
   - ✓ Action WITHDRAW_TRANSFER visible
   - ✓ IP capturée
   - ✓ Timestamps corrects
   - ✓ Anciennes/Nouvelles valeurs en JSON

---

## 📊 Vérifications en Base de Données

### Vérifier le transfert
```sql
SELECT id, transfer_code, amount, currency, converted_amount, target_currency, status 
FROM transfers 
WHERE transfer_code = 'TRF20240115143045ABC';
```

### Vérifier l'audit trail
```sql
SELECT action_type, description, ip_address, created_at 
FROM agent_audit_trail 
WHERE agent_id = 1 
ORDER BY created_at DESC LIMIT 5;
```

### Vérifier le solde de l'agence
```sql
SELECT id, name, current_balance, daily_limit 
FROM agency 
WHERE id = 1;
```

---

## ✅ Checklist de Validation

### Fonctionnalités Agent
- [ ] Login réussit
- [ ] Dashboard affiche les stats (solde, limite, agence)
- [ ] Formulaire de transfert valide les champs
- [ ] Transfert créé avec code unique
- [ ] Montant converti correctement
- [ ] Reçu imprimé à l'écran
- [ ] Retrait effectué
- [ ] Statut du transfert = COMPLETED
- [ ] Historique affiche les transferts
- [ ] Logout fonctionne

### Fonctionnalités Admin
- [ ] Login admin réussit
- [ ] Audit trail affiche les actions
- [ ] IP capturée correctement
- [ ] Timestamps corrects
- [ ] Anciennes/Nouvelles valeurs en JSON
- [ ] Liste des agents affichée
- [ ] Liste des transferts affichée
- [ ] Logout fonctionne

### Audit Trail
- [ ] CREATE_TRANSFER enregistré
- [ ] WITHDRAW_TRANSFER enregistré
- [ ] LOGIN enregistré
- [ ] IP et User-Agent capturés
- [ ] Dates/heures correctes
- [ ] Valeurs JSON valides

---

## 🐛 Dépannage

### Erreur 404 sur login.html
- Vérifier que le WAR est déployé sous le nom `okane_transfer_war`
- Vérifier l'URL: `http://localhost:8080/okane_transfer_war/`

### Token invalide
- Vérifier que le token n'a pas expiré (24h par défaut)
- Refaire un login

### Pas de données affichées
- Vérifier que l'application est en cours d'exécution
- Vérifier les logs de Tomcat
- Vérifier la connexion à la base de données

### Audit trail vide
- Vérifier que l'agent ID existe
- Vérifier que des actions ont été effectuées
- Vérifier la table `agent_audit_trail` en base

---

## 📁 Fichiers créés

- `login.html` - Page de connexion
- `agent-dashboard.html` - Dashboard agent
- `admin-dashboard.html` - Dashboard admin
- `index.jsp` - Redirection vers login

---

## 🔐 Sécurité

- Tokens JWT stockés en localStorage
- Authentification requise pour tous les endpoints
- Rôles vérifiés (AGENT, ADMIN)
- IP capturée pour l'audit trail
- Mots de passe hashés en base de données

---

## 📞 Support

Pour toute question ou problème, consultez:
- Les logs de l'application
- La table `agent_audit_trail` en base
- Le fichier `GUIDE_TEST_NOUVELLES_FONCTIONNALITES.md`
