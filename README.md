# Kerflow

**An agentic local-prospecting engine.** Search for local businesses, let the backend enrich them from public sources
and score them transparently, then work them through a kanban pipeline — from a web app, or from an AI assistant over
MCP.

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](LICENSE)

Kerflow is headless first: the Spring Boot backend is the product, and the Vuetify web app is one client among others.
Claude, ChatGPT and any MCP-capable agent drive the same pipeline through the `/mcp` endpoint.

## Repository layout

```
backend/    Spring Boot 4.1 · Java 21 · PostgreSQL · MCP server
frontend/   Vue 4 · Vuetify 4 · Vite · RxJS
docs/       product roadmap and end-user how-tos
```

Both projects are self-contained: open the repository root in your IDE, or open
`backend/` alone (the Gradle wrapper lives there) or `frontend/` alone.

## Features

- **Prospect pipeline** — kanban columns, drag-and-drop ordering, per-prospect messages and notes, CSV import.
- **Enrichment** — chained enrichers that resolve a business profile, estimate company size, and collect signals from
  Google Places, the prospect's own website, and the French
  [Recherche d'Entreprises](https://recherche-entreprises.api.gouv.fr) open API.
- **Scoring** — a rule-based score with a per-reason breakdown. Explainable, no black box.
- **MCP server** — 10 tools (`search_local_businesses`, `create_prospect`,
  `enrich_prospect`, `save_prospect_analysis`, `save_prospect_email`, …) over streamable HTTP at `/mcp`, secured by
  OAuth 2.1 + PKCE with dynamic client registration, or by a personal API token.
- **Billing** — Stripe Checkout, customer portal, and subscription sync. Optional.

## Getting started

**Prerequisites:** JDK 21, Node 24+, Docker.

```bash
git clone https://github.com/kerflowapp/kerflow.git
cd kerflow

# 1. Backend — PostgreSQL 17 on localhost:5490, API on localhost:8090
cd backend
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=dev'

# 2. Frontend — dev server on localhost:5190
cd ../frontend
cp .env.example .env
npm ci
npm run dev
```

**No credential is required for a first run.** The dev profile boots with every integration disabled;
see [Configuration](#configuration) for what that costs you.

### Tests

```bash
cd backend  && ./gradlew test
cd frontend && npm run test && npm run type-check && npm run lint
```

## Configuration

Backend configuration lives in `backend/src/main/resources/application*.yml` and reads from the environment — Spring
Boot reads the variables from your shell, not from a `.env` file. Frontend configuration is a handful of `VITE_*`
variables in `frontend/.env`. **No secret is ever committed**; `backend/.env.example` and `frontend/.env.example` list
every variable.

Vite reads the `VITE_*` values **at build time** and inlines them into the JavaScript bundle, so they cannot be set on
the running container. `frontend/.env` covers local development; a production image takes them as Docker build
arguments:

```bash
docker build ./frontend -t kerflow-frontend \
  --build-arg VITE_BACKEND_URL=https://api.example.com
```

The `Deploy frontend` workflow does the same from the repository's Actions variables and secrets. This means the
published `ghcr.io/kerflowapp/kerflow/frontend` image is built against *our* backend: **self-hosting means rebuilding
the image with your own build arguments**, not just pointing a container at a different URL. Building with no argument
at all still works — the app then derives the backend from `window.location` and every integration stays off.

Because the values end up readable in the shipped JavaScript, a `VITE_*` variable is never a place for an actual secret,
whatever it is stored in upstream.

Every integration is optional and degrades gracefully:

| Integration   | Variables                                                                      | Without it                                               |
|---------------|--------------------------------------------------------------------------------|----------------------------------------------------------|
| AWS Cognito   | `AWS_REGION`, `AWS_ACCESS_KEY_ID`, `AWS_ACCESS_KEY_SECRET`, `SPRING_COGNITO_*` | App boots; sign-up / sign-in fail                        |
| Google Places | `GOOGLE_PLACES_API_KEY`                                                        | Business search returns nothing                          |
| Stripe        | `STRIPE_API_KEY`, `STRIPE_PRICE_ID`, `STRIPE_PRICE_ID_ANNUAL`                  | Billing endpoints unusable                               |
| Resend        | `RESEND_API_KEY`                                                               | Emails are logged instead of sent (`DummyResendService`) |
| Discord       | `DISCORD_BOT_TOKEN`, `DISCORD_GUILD_ID`                                        | No internal notifications                                |
| Sentry        | `SENTRY_DSN` (runtime), `SENTRY_AUTH_TOKEN` + `SENTRY_ORG` (build)             | No error reporting                                       |
| PostHog       | `VITE_POSTHOG_ENABLED`, `VITE_POSTHOG_KEY`                                     | No product analytics                                     |

`SUPER_ADMIN_EMAILS` is a comma-separated list of logins allowed to reach admin endpoints and to impersonate. It is
**empty by default**: nobody is super-admin until you set it.

`VITE_TERMS_URL` points at the terms of service of your own instance. The legal terms are not shipped with the code;
when the variable is empty, the links to them are not rendered.

## Self-hosting caveats

Two things to know before putting this on a public host.

**The schema is managed by Hibernate, not by migrations.** `ddl-auto: update` is active in both the dev and the prod
profile — there is no Flyway or Liquibase. Hibernate will add columns but never drop or narrow them, and it gives you no
rollback path. Back up before upgrading. `backend/DATABASE.md` also lists one SQL function (`array_overlap`) to create
by hand if you use array-overlap queries.

**`/actuator/**` is not authenticated.** In the prod profile the exposure is limited to
`health,info,prometheus`, which is fine. **The dev profile exposes everything**
(`application-dev.yml`), including `/actuator/env`, `/actuator/configprops` and
`/actuator/heapdump` — anyone who can reach the port can read your entire resolved configuration, environment variables
included. Never run the dev profile on a host that is reachable from the internet, or narrow
`management.endpoints.web.exposure.include` first.

## API

Base path `/v1`. Bearer token (Cognito JWT, OAuth access token, or personal API token).

| Path                             | Purpose                                                    |
|----------------------------------|------------------------------------------------------------|
| `/v1/authentication`             | Sign-up, sign-in, refresh, password reset, OTP             |
| `/v1/prospects`                  | Prospects, pipeline columns, messages, enrichment, scoring |
| `/v1/search`                     | Local business search                                      |
| `/v1/users/{user-id}`            | Profile, notifications                                     |
| `/v1/users/me/api-tokens`        | Personal API tokens for MCP                                |
| `/v1/billing`                    | Stripe checkout, portal, subscription sync                 |
| `/v1/oauth2/*`, `/.well-known/*` | OAuth 2.1 authorization server for MCP clients             |
| `/mcp`                           | MCP streamable-HTTP endpoint                               |
| `/health`, `/actuator/*`         | Health, info, Prometheus metrics                           |

To connect Claude or ChatGPT, follow
[docs/modops/connecter-claude-chatgpt.md](docs/modops/connecter-claude-chatgpt.md).

## Roadmap

[docs/roadmap.md](docs/roadmap.md) tracks what is shipped and what is next.
[docs/backlog.md](docs/backlog.md) is the unordered idea list.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Commits follow
[Conventional Commits](https://www.conventionalcommits.org/); CI runs the backend and frontend test suites on every pull
request.

Security issues: read [SECURITY.md](SECURITY.md) — please do not open a public issue.

## License

Copyright (C) 2026 Fabien Battais.

Licensed under the [GNU Affero General Public License v3.0](LICENSE).

If you run a modified version of this software as a network service, the AGPL requires you to offer your users the
corresponding source code.
