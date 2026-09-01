# Security Policy

## Reporting a vulnerability

Please **do not open a public issue** for security problems.

Report privately through GitHub's
[private vulnerability reporting](https://github.com/kerflowapp/kerflow/security/advisories/new), or by email to
`hello@kerflowapp.com`.

Include what you need to reproduce: affected endpoint, class or component, steps, and impact. Expect an acknowledgement
within a few days. Please give us a reasonable window to ship a fix before disclosing publicly.

## Scope

This repository holds both the Kerflow backend and web client. The hosted service at
`kerflowapp.com` / `api.kerflowapp.com` is covered too — say so in your report if that is where you found the issue.

## Self-hosting notes

- **Never commit secrets.** Everything is read from the environment; `.env.example` in each project lists the variables,
  and `.env` files are git-ignored.
- **`/actuator/**` is not authenticated** (`configuration/security/SecurityConfig.java`). The prod profile limits
  exposure to `health,info,prometheus`. The **dev profile exposes everything**, including `/actuator/env`,
  `/actuator/configprops` and
  `/actuator/heapdump`, which together leak your whole resolved configuration and environment. Do not run the dev
  profile on a publicly reachable host.
- **`SUPER_ADMIN_EMAILS`** grants admin access and the right to impersonate other users at sign-in. It is empty by
  default — keep it that way unless you need it.
- **The schema is applied by Hibernate `ddl-auto: update`**, in prod too. There are no migrations and no rollback path.
  Back up your database before upgrading.
- Personal API tokens and OAuth client secrets are stored as SHA-256 digests only (`oauth/Secrets.java`), never in
  clear.
- Beta and sandbox unlock sequences in the web client are UI conveniences, not a security boundary — they are in the
  bundle. Anything that must stay restricted is enforced by the backend.
