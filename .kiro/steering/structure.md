# Project Structure

Monorepo with `backend/` and `frontend/` directories. Both follow Domain-Driven Design (DDD) layered architecture.

## Backend — `backend/src/main/java/com/cdp/ecosaas/procurement/`

```
{module}/
├── domain/              # Core business logic (no framework dependencies)
│   ├── model/           # Entities, aggregates, value objects
│   ├── service/         # Domain services
│   ├── repository/      # Repository interfaces (ports)
│   ├── port/            # Outbound port interfaces (email, token, SAML)
│   └── event/           # Domain events
├── application/         # Use cases / orchestration
│   ├── command/         # Command objects (write operations)
│   ├── query/           # Query objects (read operations)
│   ├── handler/         # Command and query handlers
│   └── service/         # Application services
├── infrastructure/      # Framework implementations
│   ├── persistence/     # JPA entities, Spring Data repositories
│   ├── security/        # Security config, JWT, SAML providers
│   └── external/        # External service adapters (email, etc.)
├── interfaces/          # Inbound adapters
│   └── rest/            # REST controllers + DTO classes
└── shared/              # Module-internal shared code (constants, exceptions)
```

Cross-module shared code lives in `shared/` at the procurement package root:
- `shared/exception/` — GlobalExceptionHandler, BusinessException base class
- `shared/model/` — PageQuery, PageResult
- `shared/util/` — SecurityUtils (get current user)

## Frontend — `frontend/src/`

```
src/
├── modules/{module}/    # Feature modules (DDD layered)
│   ├── domain/          # Entities, value objects, business rules
│   ├── application/     # Use cases
│   ├── infrastructure/  # API service calls
│   ├── presentation/    # UI layer
│   │   ├── views/       # Page components
│   │   ├── components/  # Reusable components
│   │   ├── stores/      # Pinia stores
│   │   └── routes/      # Module route definitions
│   └── types/           # DTOs, VOs, Command types
├── shared/              # Cross-module shared code
│   ├── http/            # Axios instance, CSRF adapter
│   ├── types/           # Common types (pagination)
│   └── utils/           # Utility functions
├── router/              # Global route registry
├── config/              # App config (menu definitions by role)
├── layouts/             # Layout components (MainLayout)
└── views/               # Global views (placeholder pages)
```

## Conventions

- New backend modules: create package under `com.cdp.ecosaas.procurement.{module}` with DDD layers
- New frontend modules: create directory under `src/modules/{module}` with DDD layers
- Business exceptions extend `shared.exception.BusinessException`
- Pagination uses `shared.model.PageQuery` / `PageResult`
- Current user access via `shared.util.SecurityUtils`
- REST controllers go in `interfaces/rest/`, DTOs live alongside controllers
- Database migrations in `backend/src/main/resources/db/migration/`
