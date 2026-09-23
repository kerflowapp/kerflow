FROM node:24-alpine AS build-stage

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

RUN apk add --no-cache curl

ENV DOCKERIZE_VERSION=v0.7.0
RUN curl -fSslL https://github.com/jwilder/dockerize/releases/download/${DOCKERIZE_VERSION}/dockerize-linux-amd64-${DOCKERIZE_VERSION}.tar.gz | \
    tar xzv -C /usr/local/bin/

RUN mkdir -p /app

COPY --from=build-stage /app/dist/ /app/
RUN ls -l /app

VOLUME ["/data"]

CMD ["/usr/local/openresty/bin/openresty", "-g", "daemon off;"]
