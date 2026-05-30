# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
# CLAUDE.md

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

## What this is

EcoSaaS 采购平台 — an enterprise procurement system (supplier mgmt → RFQ → contract → PO → payment).
Monorepo: `backend/` (Java 21 / Spring Boot 3.5) + `frontend/` (Vue 3 / Vite 6). Both use DDD layering.

**Status:** Only the `auth` module is implemented. Modules 02–07 (`supplier`, `pr`, `rfq`, `contract`, `order`,
`payment`, `report`, `setting`) are empty DDD skeletons — each leaf package holds a `package-info.java` placeholder.
**Treat the `auth` module as the reference template** when filling in any other module.

## Commands

### Backend (`cd backend`)
```bash
mvn clean compile                                              # compile
mvn test                                                       # all tests
mvn test -Dtest=InternalUserTests                             # single test class
mvn test -Dtest=InternalUserTests#should_lock_after_5_failures # single test method
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Denv=DEV" # run (dev)
mvn clean package -DskipTests                                  # → target/ecosaas-procurement-0.0.1.snapshot.jar
```
- `-Denv=DEV` selects the Apollo config environment; required to run.
- Backend serves on **9000**; actuator on **9001** (`/management/**`). Swagger UI at `/swagger-ui/index.html`.
- Test files match **both** `*Test.java` and `*Tests.java` (Surefire). Both naming styles exist in the repo.
- In IDE: `backend/.vscode/launch.json` has Run/Debug configs (F5).

### Frontend (`cd frontend`)
```bash
npm install
npm run dev      # dev server on 5173, proxies /api → localhost:9000
npm run build    # vue-tsc type-check + vite build
```
- `pnpm-lock.yaml` is present but README/docs use `npm`. Path alias `@` → `frontend/src`.
- No frontend test runner is configured.

## Architecture

### Backend request flow
`interfaces/rest` (thin controller) → `application/handler` (`*CommandHandler` / `*QueryHandler`) →
`domain` (model + `domain/service`) → `domain/repository` (interface) ←implemented by→ `infrastructure`.

Controllers stay thin: build a `Command`/`Query` record, call a handler, map the result to a response DTO.
See [AuthController.java](backend/src/main/java/com/cdp/ecosaas/procurement/auth/interfaces/rest/AuthController.java).

### Persistence pattern (the key non-obvious piece)
Each aggregate has **three** types and a delegating repository — domain code never touches JPA entities:
- `domain/model/Xxx` — pure domain object (no JPA annotations)
- `infrastructure/persistence/entity/XxxEntity` — JPA `@Entity`
- `infrastructure/persistence/mapper/XxxMapper` — **MapStruct** mapper (domain ↔ entity)
- `domain/repository/XxxRepository` (interface) is implemented by `infrastructure/persistence/repository/JpaXxxRepository`,
  which delegates to a Spring Data `XxxJpaDao` and converts via the mapper. `save()` reloads the managed entity on update
  to preserve the optimistic-lock version. See
  [JpaInternalUserRepository.java](backend/src/main/java/com/cdp/ecosaas/procurement/auth/infrastructure/persistence/repository/JpaInternalUserRepository.java).

### Cross-module shared code — `procurement/shared/`
- `exception/BusinessException` — base for all business exceptions: `(errorCode "AUTH.1001", messageCode "AUTHENTICATION_FAILED", detail)`.
- `exception/GlobalExceptionHandler` — `@RestControllerAdvice` over the whole package. Maps `messageCode` → HTTP status
  and returns `{code, message, detail, timestamp}`. **To get the right HTTP status for a new error, add its `messageCode`
  to the `switch` in `resolveHttpStatus`** (otherwise it defaults to 400).
- `model/PageQuery` / `PageResult` — pagination contract.
- `util/SecurityUtils` — static `getCurrentUserId()` / `getCurrentUserRole()` / `isAdmin()` from the SecurityContext.

### Auth & security
- **Stateless JWT in an HttpOnly cookie + CSRF double-submit.** Spring's built-in CSRF and sessions are disabled;
  `JwtAuthenticationFilter` validates the JWT cookie and the `X-CSRF-TOKEN` header against the `XSRF-TOKEN` cookie.
  The frontend never reads the JWT.
- **Path-based authorization** in [SecurityConfig.java](backend/src/main/java/com/cdp/ecosaas/procurement/auth/infrastructure/security/SecurityConfig.java):
  `/api/admin/**` requires `ADMIN`; other `/api/**` require auth; login/SSO/forgot/reset and Swagger/actuator are public.
- API path conventions: `/api/internal/auth/*` (internal login/SSO), `/api/supplier/auth/*` (supplier login),
  `/api/auth/*` (authenticated shared: logout, change/forgot/reset password), `/api/admin/*` (ADMIN-only).
- All `auth.*` config (JWT, cookie, lockout, SAML, CORS, password policy, mail) is externalized — see the `auth:` block in
  `config/application.yml` and per-env overrides in `application-{dev,test,prod}.yml`, bound via `infrastructure/config/Auth*Properties`.

### Frontend wiring
- DDD per module under `src/modules/{module}/{domain,application,infrastructure,presentation,types}`.
- `shared/http/api-client.ts` — the shared Axios instance (`withCredentials: true`) with a request interceptor that adds
  `X-CSRF-TOKEN` on POST/PUT/PATCH/DELETE. Use this for module API calls.
- Auth state: `modules/auth/presentation/stores/auth.store.ts` (Pinia). The router guard in
  `modules/auth/.../routes/auth.routes.ts` calls `checkAuth()` to restore the session from the cookie, then enforces
  `meta.roles`. A global Axios 401 interceptor redirects to login on session timeout.
- `src/router/index.ts` holds the full route table; unimplemented pages render `views/placeholder/PlaceholderView.vue`.
- `src/config/menu.ts` builds the sidebar per role — **a menu item's `key` IS the route `path`** (click → `router.push(key)`),
  so menu and router must stay in sync.

## Conventions & gotchas

- **Roles:** exactly four — `ADMIN` (采购经理), `BUYER` (采购员), `BUSINESS_USER` (业务人员), `SUPPLIER` (供应商).
  Legacy `SYSTEM_ADMIN` / `PROCUREMENT_MANAGER` were folded into `ADMIN` (migration `V3`). Don't reintroduce them.
- **Language:** English for code identifiers; Chinese (中文) for UI labels, comments, Javadoc, and docs. Keep this split.
- **Database:** dev connects to DB `caigou`, schema `caigou7` (`currentSchema=caigou7` in `application-dev.yml`).
  Entities use `@Table(name=...)` with **no** schema, so the schema is connection-level.
  ⚠️ The README and `.kiro/steering/tech.md` still say schema `trial_procurement` — that is **stale**; trust `application-dev.yml`.
- **Migrations** live in `backend/src/main/resources/db/migration/` (`V1`–`V3`, applied manually — there's no Flyway).
  But dev runs `spring.jpa.hibernate.ddl-auto: update`, so the schema can also drift from JPA entities.
- **Known gap:** the frontend `checkAuth()` calls `GET /api/auth/me`, but no such endpoint exists in the backend yet —
  session restore on refresh currently falls through to the login page.
- `.kiro/steering/AGENTS.md` is a copy of generic behavioral guidelines, not project context — `product.md`, `tech.md`,
  and `structure.md` in that folder are the project-specific steering docs.
- Detailed specs (treat as source of truth for behavior) live in `init-docs/` — `backend_spec.md`, `frontend_spec.md`,
  `requirements.md`, `quality-standards.md`.
</content>
</invoke>
