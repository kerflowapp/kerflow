# Kerflow — guide for coding agents

Monorepo: `backend/` (Spring Boot 3.4, Java 21, Gradle Kotlin DSL) and `frontend/`
(Vue 3, Vuetify 3, Vite, TypeScript). Product context, configuration and self-hosting caveats are
in [README.md](README.md); contributor conventions in
[CONTRIBUTING.md](CONTRIBUTING.md). This file only covers what is not obvious from the code.

## Domain

Kerflow is a B2B local-prospecting engine. A user searches for local businesses, the backend enriches and scores them,
and they move through a kanban pipeline. The backend is the product; the web app is one client among others, and any
MCP-capable agent (Claude, ChatGPT) drives the same pipeline through `/mcp`.

## Backend

Layering: `api/` (thin controllers + DTOs) → `services/` (all business logic) →
`repositories/` (Spring Data JPA) over `domain/` entities. MapStruct for mapping, Lombok for boilerplate.

- **Logger is `LOGGER`**, not `log` — the classes declare it explicitly.
- **Every integration degrades gracefully.** AWS injects a placeholder credential rather than failing the context
  (`AWSConfiguration`), Resend logs instead of sending (`DummyResendService`), Discord and Sentry default to disabled.
  Keep it that way: a fresh clone with no environment variables must still boot.
- **No migrations.** The schema comes from Hibernate `ddl-auto: update`, in prod too. A change to an entity is a schema
  change with no rollback path — think before renaming a column.
- **Secrets are hashed, never stored.** API tokens and OAuth client secrets live as SHA-256 digests
  (`oauth/Secrets.java`).
- **Authorization** goes through `@IsSuperAdmin` / `ControllerGuardService`, backed by
  `SUPER_ADMIN_EMAILS` (empty by default). Never gate anything on the client alone.
- New behaviour comes with a test. `./gradlew test`.

## Frontend

Strict layering — **never skip a level**:

```
api/*.api.ts  →  composables/use*.ts  →  components/, views/
```

- **`api/`** — HTTP only. Every function returns an `AxiosObservable<T>`, is named with a
  `$` suffix, and gets its instance from `useHttp()`. Types live in `api/dtos/*.dto.ts`.
- **`composables/`** — business logic and local state, one responsibility each.
  Use `useTrigger()` for the loading flag, the RxJS subscription, the unsubscribe on unmount and the 401 handling — do
  not hand-roll it.
- **`components/`, `views/`** — UI and user events only. No API call, no heavy logic. Split a component before it
  reaches ~300 lines.
- **No barrel files.** Import the module that defines the symbol — `~/composables/useTrigger`,
  `~/api/dtos/prospect.dto`, `~/utils/tokenStorage` — never a directory. Do not add a re-exporting `index.ts`
  (`src/router/index.ts` is the router itself, not a barrel).

Rules that are easy to break:

- `<script setup lang="ts">` always. Typed `Props` and `Emits` interfaces.
- **Vuetify only** for styling: its components, its props, its theme. No Tailwind, no other CSS framework, custom CSS
  only where a prop cannot do the job. `elevation="0"` on
  `v-card`.
- **No hardcoded strings.** Everything goes through `vue-i18n`, added to **both**
  `locales/fr.json` and `locales/en.json`, keys in kebab-case under a namespace — except the leaves keyed by a backend
  enum value (`signals.types.NO_WEBSITE`,
  `business.facts.COMPANY_AGE`…), which keep the enum's exact spelling.
- Form validation uses Vuetify `:rules` directly. `vee-validate` is gone; do not bring it back.
- No `any`.

## Things that must not come back

- **Secrets in the repository.** Everything reads from the environment; `.env` files are git-ignored and documented in
  `.env.example`. This includes analytics keys.
- **Legal and marketing content.** The terms of service belong to whoever operates the instance and live outside this
  repository — the web app links to `VITE_TERMS_URL` and hides the link when it is empty.
- **Real customer data**, in fixtures, screenshots, tests or landing pages.
- **Real-estate vocabulary** (mandates, agencies, agents, DPE/GES) — leftovers from an earlier product.
