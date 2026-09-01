# Kerflow — web client

Vuetify web app for [Kerflow](../README.md). Vue 3 (Composition API) · TypeScript · Vite · Vuetify 3 · Pinia · Vue
Router · RxJS · vue-i18n.

The root [README](../README.md) covers features, configuration and the API surface. This file is the short version for
working on the frontend alone.

## Run

```bash
cp .env.example .env      # points at http://localhost:8090 by default
npm ci
npm run dev               # http://localhost:5190
```

You need the backend running — see [`../backend`](../backend/README.md).

## Scripts

| Command              | What it does                     |
|----------------------|----------------------------------|
| `npm run dev`        | Vite dev server on port 5190     |
| `npm run build`      | Production build into `dist/`    |
| `npm run type-check` | `vue-tsc` over the whole project |
| `npm run test`       | Vitest, single run               |
| `npm run lint`       | ESLint with `--fix`              |
| `npm run format`     | Prettier over `src/`             |

## Layout

```
src/
├── api/            HTTP layer — *.api.ts return AxiosObservable, DTOs in api/dtos/
├── composables/    business logic and local state (use*.ts)
├── components/     reusable components
├── views/          routed pages
├── layouts/        Empty / Full / Dynamic layouts
├── stores/         Pinia (auth, prospects)
├── router/         routes and navigation guards
├── locales/        i18n — fr.json and en.json, both always
└── utils/          pure helpers
```

Conventions are in [`../CONTRIBUTING.md`](../CONTRIBUTING.md).

## License

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](LICENSE)
Copyright (C) 2026 Fabien Battais
