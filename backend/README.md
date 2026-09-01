# Kerflow — backend

REST API and MCP server for [Kerflow](../README.md). Spring Boot 3.4 · Java 21 · PostgreSQL 17 · Spring AI MCP · Spring
Security (OAuth2 resource server) · Hibernate · MapStruct · Lombok · Gradle (Kotlin DSL).

The root [README](../README.md) covers features, configuration, the API surface and the self-hosting caveats. This file
is the short version for working on the backend alone.

## Run

```bash
docker compose up -d          # PostgreSQL 17 on localhost:5490
./gradlew bootRun --args='--spring.profiles.active=dev'
curl http://localhost:8090/health
```

No credential is required. The dev profile boots with every integration disabled;
`.env.example` lists the variables to export in your shell once you want to turn one on — Spring Boot reads them from
the environment, not from a file.

The schema is created by Hibernate (`ddl-auto: update`), so there is no migration step.
`DATABASE.md` lists the one SQL function to create by hand if you use array-overlap queries.

## Test

```bash
./gradlew test
```

## Layout

```
src/main/java/com/kerflowapp/kerflow/
├── api/             REST controllers + DTOs
├── services/        business logic (enrichment, scoring, billing, prospects, …)
├── mcp/             MCP server config, tools, API-token auth
├── oauth/           OAuth 2.1 authorization server (PKCE, dynamic registration)
├── repositories/    Spring Data JPA
├── domain/          JPA entities
├── mappers/         MapStruct
└── configuration/   Spring config, security, authorization aspects
```

## License

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](LICENSE)
Copyright (C) 2026 Fabien Battais
