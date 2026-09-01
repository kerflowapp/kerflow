#!/usr/bin/env bash
# Génère les images Open Graph du site vitrine (1200x630) depuis scripts/og/og-card.html.
# Prérequis : Google Chrome installé (mode headless).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
CARD="$ROOT/scripts/og/og-card.html"
CHROME="${CHROME:-/Applications/Google Chrome.app/Contents/MacOS/Google Chrome}"

if [ ! -x "$CHROME" ]; then
    echo "Chrome introuvable : $CHROME (surcharger avec CHROME=/chemin/vers/chrome)" >&2
    exit 1
fi

shoot() {
    local lang="$1" out="$2"
    "$CHROME" \
        --headless=new \
        --disable-gpu \
        --hide-scrollbars \
        --force-device-scale-factor=1 \
        --window-size=1200,630 \
        --virtual-time-budget=8000 \
        --screenshot="$out" \
        "file://$CARD?lang=$lang" >/dev/null 2>&1
    echo "→ $out"
    sips -g pixelWidth -g pixelHeight "$out" | tail -2
}

shoot en "$ROOT/landing/og-image.png"
shoot fr "$ROOT/landing/fr/og-image.png"
