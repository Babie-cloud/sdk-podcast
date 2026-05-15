#!/usr/bin/env bash
# Vérifie que l'API répond. Prérequis : PostgreSQL + `mvn spring-boot:run` (port 8080).
# Usage : ./scripts/verify-api.sh   ou   API_BASE=http://localhost:8080 ./scripts/verify-api.sh

set -euo pipefail

API_BASE="${API_BASE:-http://127.0.0.1:8080}"
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT

say() { printf '\n=== %s ===\n' "$1"; }

die() { printf 'ERREUR: %s\n' "$1" >&2; exit 1; }

need_http() {
  local url="$1"
  local expected="${2:-}"
  local code
  code=$(curl -sS -o "$TMP" -w "%{http_code}" "$url" --connect-timeout 3 --max-time 15) || code="000"
  if [[ "$code" == "000" ]]; then
    die "Aucune réponse depuis ${API_BASE} (code curl 000). Démarre PostgreSQL puis lance : mvn spring-boot:run"
  fi
  if [[ -n "$expected" ]] && [[ "$code" != "$expected" ]]; then
    cat "$TMP" >&2 || true
    die "HTTP $code pour $url (attendu $expected)"
  fi
  printf 'OK HTTP %s  %s\n' "$code" "$url"
}

say "0. Connectivité — GET /api/podcasts"
need_http "${API_BASE}/api/podcasts" "200"

say "1. POST /auth/register (utilisateur jetable)"
EMAIL="verify_$(date +%s)_${RANDOM}@test.local"
REGISTER_JSON=$(printf '{"name":"Verify","prenom":"Script","email":"%s","password":"password123"}' "$EMAIL")
code=$(curl -sS -o "$TMP" -w "%{http_code}" -X POST "${API_BASE}/auth/register" \
  -H "Content-Type: application/json" \
  -d "$REGISTER_JSON" \
  --connect-timeout 3 --max-time 15) || code="000"
[[ "$code" == "000" ]] && die "Register : pas de réponse (backend arrêté ?)"
[[ "$code" != "200" ]] && { cat "$TMP" >&2; die "POST /auth/register → HTTP $code"; }
printf 'OK HTTP %s  POST /auth/register (%s)\n' "$code" "$EMAIL"

TOKEN=$(TMPFILE="$TMP" python3 -c "import json,os; print(json.load(open(os.environ['TMPFILE'])).get('token',''))" 2>/dev/null || true)
[[ -z "$TOKEN" ]] && die "Pas de champ token dans la réponse register"

say "2. GET /users/me (avec JWT)"
code=$(curl -sS -o "$TMP" -w "%{http_code}" "${API_BASE}/users/me" \
  -H "Authorization: Bearer ${TOKEN}" \
  --connect-timeout 3 --max-time 15)
[[ "$code" != "200" ]] && { cat "$TMP" >&2; die "GET /users/me → HTTP $code"; }
printf 'OK HTTP %s  GET /users/me\n' "$code"

say "3. GET /api/podcasts/mine (avec JWT)"
code=$(curl -sS -o "$TMP" -w "%{http_code}" "${API_BASE}/api/podcasts/mine" \
  -H "Authorization: Bearer ${TOKEN}" \
  --connect-timeout 3 --max-time 15)
[[ "$code" != "200" ]] && { cat "$TMP" >&2; die "GET /api/podcasts/mine → HTTP $code"; }
printf 'OK HTTP %s  GET /api/podcasts/mine\n' "$code"

say "4. GET /api/writings (public)"
need_http "${API_BASE}/api/writings" "200"

say "5. GET /api/storytellings (public)"
need_http "${API_BASE}/api/storytellings" "200"

say "6. POST /auth/login"
LOGIN_JSON=$(printf '{"email":"%s","password":"password123"}' "$EMAIL")
code=$(curl -sS -o "$TMP" -w "%{http_code}" -X POST "${API_BASE}/auth/login" \
  -H "Content-Type: application/json" \
  -d "$LOGIN_JSON" \
  --connect-timeout 3 --max-time 15)
[[ "$code" != "200" ]] && { cat "$TMP" >&2; die "POST /auth/login → HTTP $code"; }
printf 'OK HTTP %s  POST /auth/login\n' "$code"

say "7. GET /users/me sans token (doit être refusé)"
code=$(curl -sS -o "$TMP" -w "%{http_code}" "${API_BASE}/users/me" \
  --connect-timeout 3 --max-time 15)
if [[ "$code" == "401" ]] || [[ "$code" == "403" ]]; then
  printf 'OK HTTP %s  GET /users/me sans JWT (refus attendu)\n' "$code"
else
  cat "$TMP" >&2 || true
  die "Attendu 401/403 sans token, obtenu $code"
fi

printf '\nToutes les vérifications automatiques ont réussi.\n'
printf 'Endpoints non couverts ici (multipart, ids dynamiques) : POST /api/podcasts, POST .../episodes, DELETE, writings/storytellings POST/DELETE.\n'
