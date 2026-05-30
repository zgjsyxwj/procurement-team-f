# Tech Stack & Build

## Backend

- Java 21, Spring Boot 3.5, Spring Security
- Spring Data JPA + PostgreSQL 16 + Druid connection pool
- JWT auth (JJWT 0.12.6) + SAML 2.0 SSO
- Apollo config center
- MapStruct + Lombok for DTO mapping and boilerplate reduction
- OpenFeign for remote calls
- SpringDoc OpenAPI for API docs
- Logback for logging
- Apache Commons (lang3, collections4)
- Testing: JUnit 5 + Mockito

## Frontend

- Vue 3 + TypeScript + Vite 6
- Ant Design Vue 4 (UI components)
- Tailwind CSS 4
- Pinia (state management)
- Vue Router 4
- Axios (HTTP client)
- dayjs (date handling)

## Common Commands

### Backend

```bash
cd backend

# Compile
mvn clean compile

# Run tests
mvn test

# Run application (dev mode)
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Denv=DEV"

# Package
mvn clean package -DskipTests
```

### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Dev server (localhost:5173, proxies /api to localhost:9000)
npm run dev

# Type-check and build for production
npm run build
```

## Key Configuration

- Backend port: 9000
- Frontend dev port: 5173 (proxies `/api` → backend)
- Path alias: `@` → `frontend/src/`
- Database: PostgreSQL 16, schema `trial_procurement`
- Test file naming: `**/*Tests.java` (Maven Surefire convention)
- Apollo env flag: `-Denv=DEV`
