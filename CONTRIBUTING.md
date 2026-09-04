# Contributing to Kerflow

Thanks for taking the time. This is a small project; the process is deliberately light.

## Before you start

For anything larger than a bug fix, open an issue first so we can agree on the approach.
[docs/roadmap.md](docs/roadmap.md) shows what is already planned.

## Local setup

**Prerequisites:** JDK 21, Node 24+, Docker.

```bash
# Backend — PostgreSQL on localhost:5490, API on localhost:8090
cd backend
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=dev'

# Frontend — dev server on localhost:5190
cd frontend
cp .env.example .env
npm ci
npm run dev
```

No credential is needed to boot. Integrations you have no key for stay disabled and the
app degrades gracefully — see the configuration table in the [README](README.md).

## Before you open a pull request

```bash
cd backend  && ./gradlew test
cd frontend && npm run test && npm run type-check && npm run lint
```

CI runs exactly this. A green local run is a green CI run.

## Conventions

**Commits** follow [Conventional Commits](https://www.conventionalcommits.org/):
`feat:`, `fix:`, `refactor:`, `docs:`, `chore:`, `test:`. Keep the subject under 72
characters and write it in English.

**Backend** — controllers stay thin, business logic lives in `services/`, persistence in
`repositories/`. MapStruct for mapping, Lombok for boilerplate. New behaviour comes with a
test.

**Frontend** — Vue 3 Composition API with `<script setup lang="ts">` only. The layering is
`api/ → composables/ → components,views/`:

- `api/*.api.ts` — HTTP only. Functions return an `AxiosObservable` and end with `$`.
  Types live in `api/dtos/*.dto.ts`.
- `composables/use*.ts` — business logic and local state. Use `useTrigger()` for the
  loading + subscription + error plumbing.
- `components/`, `views/` — UI and user events. No direct API calls, no heavy logic.

Styling is **Vuetify only** — its components, props and theme. No Tailwind, no other CSS
framework, as little custom CSS as possible. Every user-facing string goes through
`vue-i18n`, in both `locales/fr.json` and `locales/en.json`. No `any`.

## Reporting bugs

Include what you ran, what you expected, what happened, and the relevant log or console
output. For security problems, read [SECURITY.md](SECURITY.md) instead — do not open a
public issue.

## License

By contributing you agree that your contributions are licensed under the
[AGPL-3.0](LICENSE), like the rest of the project.
