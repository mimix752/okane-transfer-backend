# Okane Transfer

Application de transfert d'argent — Spring MVC 6 (sans Spring Boot), Hibernate JPA 6, Spring Security 6 + JWT.

## Stack technique

- Java 21
- Spring MVC 6.1.14 / Spring Security 6.3.4
- Hibernate 6.6.3 / Jakarta JPA
- PostgreSQL 16 + HikariCP 5.1
- JWT (jjwt 0.12.6)
- SpringDoc OpenAPI 2.6 (Swagger UI)
- Déploiement WAR sur Tomcat 10+

## Prérequis

- JDK 21
- Maven 3.8+
- PostgreSQL 16
- Tomcat 10.1+

## Démarrage rapide

### 1. Base de données

```sql
CREATE DATABASE okane_transfer;
```

Exécuter les scripts dans l'ordre :
```bash
psql -U postgres -d okane_transfer -f src/main/resources/db/schema.sql
psql -U postgres -d okane_transfer -f src/main/resources/db/data.sql
```

### 2. Configuration

```bash
cp src/main/resources/application.example.properties src/main/resources/application.properties
# Éditer application.properties avec vos valeurs
```

```properties
db.url=jdbc:postgresql://localhost:5432/okane_transfer
db.username=postgres
db.password=votre_mot_de_passe
jwt.secret=votre_secret_min_32_caracteres
```

### 3. Build & déploiement

```bash
mvn clean package
cp target/okane-transfer.war $TOMCAT_HOME/webapps/
# Démarrer Tomcat
```

## Compte admin par défaut

| username | password |
|----------|----------|
| `admin`  | `admin123` |

## API Endpoints

| Méthode | URL | Auth | Description |
|---------|-----|------|-------------|
| POST | `/api/auth/register` | Non | Inscription |
| POST | `/api/auth/login` | Non | Connexion → JWT |
| POST | `/api/transfers` | JWT | Créer un transfert |
| GET | `/api/transfers` | JWT | Lister les transferts |
| GET | `/api/transfers/{code}` | JWT | Détail par code |
| PATCH | `/api/transfers/{id}/status` | JWT | Changer le statut |
| GET | `/api/agencies` | JWT | Lister les agences |
| POST | `/api/agencies` | JWT | Créer une agence |
| GET | `/api/reports?from=&to=` | JWT | Rapport |
| GET | `/api/admin/audit` | JWT + ADMIN | Journal d'audit |

## Authentification

Après login, utiliser le token JWT dans le header :
```
Authorization: Bearer <token>
```

## Swagger UI

```
http://localhost:8080/okane-transfer/swagger-ui/index.html
```
