# Deployment

## Environments

| Environment | Frontend | Backend | Database |
|-------------|----------|---------|----------|
| Local dev | `npm run dev` (port 3000) | `./mvnw spring-boot:run` (port 8080) | H2 in-memory |
| Production | Azure Static Web App or App Service | Azure App Service (Java 21) | Azure MySQL Flexible Server |

---

## CI/CD

GitHub Actions builds and deploys on every merge to `main`.

Workflows: `.github/workflows/`

---

## Production Setup

### 1. Provision Azure MySQL

- Create an **Azure Database for MySQL — Flexible Server**
- Note the server hostname, database name, username, and password

### 2. Create `application-prod.properties`

Add this file at `backend/src/main/resources/application-prod.properties`:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=validate

spring.h2.console.enabled=false

spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

security.jwt.secret-key=${JWT_SECRET}
app.base-url=${APP_BASE_URL}
```

### 3. Add MySQL driver to `pom.xml`

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 4. Provision Azure App Service

- Runtime: Java 21, Linux
- Download the Publish Profile

### 5. Set GitHub Secrets

| Secret | Value |
|--------|-------|
| `AZURE_WEBAPP_NAME` | App Service name |
| `AZURE_WEBAPP_PUBLISH_PROFILE` | Publish Profile XML content |

### 6. Set App Service Environment Variables

| Key | Value |
|-----|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `jdbc:mysql://<server>:3306/<dbname>` |
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET` | Random 256-bit Base64 string (`openssl rand -base64 32`) |
| `MAIL_USERNAME` | Gmail address for invite emails |
| `MAIL_PASSWORD` | Gmail app password |
| `APP_BASE_URL` | Live URL (e.g. `https://project-pulse.azurewebsites.net`) |

### 7. Update CORS

Add the live Azure URL to the allowed origins in `SecurityConfig.java`.

### 8. Deploy

Merge to `main` → GitHub Actions deploys automatically.

---

## Verify

```
GET https://<your-app>.azurewebsites.net/actuator/health
```

Should return `{"status":"UP"}`.
