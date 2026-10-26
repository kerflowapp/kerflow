FROM node:24.22.1-slim AS build-stage

WORKDIR /app
COPY package*.json ./

RUN npm ci
COPY ./ .

# Build-time configuration. Vite inlines every VITE_* value into the bundle, so
# they have to be present here and not on the running container. Empty defaults
# keep a plain `docker build` working: the app then falls back to window.location
# and every integration stays off.
ARG VITE_BACKEND_URL=""
ARG VITE_BACKEND_PORT=""
ARG VITE_POSTHOG_ENABLED="false"
ARG VITE_POSTHOG_KEY=""
ARG VITE_POSTHOG_HOST=""
ARG VITE_TERMS_URL=""

ENV VITE_BACKEND_URL=$VITE_BACKEND_URL \
    VITE_BACKEND_PORT=$VITE_BACKEND_PORT \
    VITE_POSTHOG_ENABLED=$VITE_POSTHOG_ENABLED \
    VITE_POSTHOG_KEY=$VITE_POSTHOG_KEY \
    VITE_POSTHOG_HOST=$VITE_POSTHOG_HOST \
    VITE_TERMS_URL=$VITE_TERMS_URL

RUN npm run build

FROM openresty/openresty:1.25.3.2-3-alpine-fat

COPY --from=build-stage /app/dist/ /app/
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 8080

CMD ["/usr/local/openresty/bin/openresty", "-g", "daemon off;"]
