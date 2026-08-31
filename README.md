# EtuBibliotheque

Application de gestion d'étudiants avec authentification JWT. Le dépôt est un monorepo contenant deux applications distinctes :

- `Backend/` — API REST Spring Boot (Java 21, Maven, MySQL)
- `Frontend/` — Application Angular 19

## Prérequis

- Java 21 (JDK)
- Docker et Docker Compose (pour la base MySQL du back)
- Node.js 20 ou supérieur
- npm

Le back utilise le Maven Wrapper (`./mvnw`) : aucune installation de Maven n'est nécessaire.

## Structure du dépôt

```
.
├── Backend/     API Spring Boot
└── Frontend/    Application Angular
```

## Back-end

### Configuration

Le fichier `.env` à la racine de `Backend/` contient les variables nécessaires (identifiants MySQL, secret JWT). Une valeur par défaut est fournie pour le développement local ; changez `JWT_SECRET` et `DB_ROOT_PASSWORD` avant tout déploiement réel.

### Démarrer l'application

```
cd Backend
./mvnw spring-boot:run
```

Cette commande démarre automatiquement le conteneur MySQL défini dans `compose.yaml` (via `spring-boot-docker-compose`), puis l'application sur le port 8080.

Vérification :

```
curl localhost:8080/actuator/health
```

### Lancer les tests

```
cd Backend
./mvnw clean test
```

Exécute l'ensemble des tests unitaires et d'intégration. Les tests de contrôleur démarrent un conteneur MySQL éphémère via Testcontainers.

### Vérifier la couverture de code

```
cd Backend
./mvnw clean verify
```

Génère le rapport JaCoCo (`target/site/jacoco/index.html`) et fait échouer le build si la couverture descend sous les seuils configurés (80 % des lignes, 70 % des branches, sur le périmètre métier hors code généré par Lombok).

## Front-end

### Installer les dépendances

```
cd Frontend
npm install
```

### Démarrer l'application

```
cd Frontend
npm start
```

L'application est servie sur `http://localhost:4200`. Le fichier `proxy.conf.json` redirige les appels `/api` vers le back-end sur `http://localhost:8080` : le back doit être démarré au préalable.

### Lancer les tests unitaires

```
cd Frontend
npm test
```

Exécute les tests Jest (services et composants) et génère un rapport de couverture (`coverage/index.html`). Un seuil minimum de 80 % est configuré sur les statements, branches, functions et lignes.

Mode watch (relance automatique à chaque modification) :

```
npm run test:watch
```

### Lancer les tests end-to-end

Le back-end doit être démarré (les tests e2e appellent la vraie API, sans mock).

```
cd Frontend
npm run cypress:open
```

ou en mode headless :

```
cd Frontend
npm run cypress:run
```

Scripts liés à la tentative de couverture de code e2e (voir limitation ci-dessous) :

```
npm run build:cypress:coverage    build instrumenté de l'application
npm run serve:cypress:coverage    sert le build instrumenté sur le port 4200
npm run e2e:coverage:full         enchaîne build, serveur et exécution Cypress
npm run e2e:coverage:report       affiche le rapport de couverture nyc
```

## Tests

Les deux applications disposent d'une suite de tests complète, couvrant la logique métier, les endpoints HTTP et les principaux parcours utilisateur.

### Back-end

- Tests unitaires de la couche service (`StudentService`, `UserService`, `JwtService`), avec mocks des dépendances
- Tests unitaires du gestionnaire d'exceptions (`RestExceptionHandler`)
- Tests d'intégration des contrôleurs REST (`StudentController`, `UserController`), exécutés contre une vraie base MySQL via Testcontainers
- Couverture de code mesurée avec JaCoCo, seuil minimum appliqué au build (`mvn verify`)

Les plans de test détaillés se trouvent dans `Backend/rapport/` (ou à la racine selon l'organisation retenue).

### Front-end

- Tests unitaires Jest des services (`AuthService`, `StudentService`, `UserService`) via `HttpClientTestingModule`
- Tests unitaires Jest des composants (`LoginComponent`, `RegisterComponent`, `StudentsComponent`) avec dépendances mockées
- Tests end-to-end Cypress couvrant l'inscription, la connexion et la gestion des étudiants, exécutés contre l'API réelle
- Couverture de code mesurée avec Jest, seuil minimum appliqué à chaque exécution (`npm test`)

Les plans de test détaillés se trouvent dans `Frontend/rapport/` (ou à la racine selon l'organisation retenue).
