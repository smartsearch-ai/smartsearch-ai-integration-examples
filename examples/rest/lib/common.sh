# Shared curl plumbing, not an SDK. Source only from run.sh.
# Credentials live in protected temporary files, never command arguments or printed requests.
set -euo pipefail
_REST_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
_REST_TMP=$(mktemp -d "${TMPDIR:-/tmp}/smartsearch-rest.XXXXXX")
chmod 700 "$_REST_TMP"
trap 'rm -rf "$_REST_TMP"' EXIT
umask 077
_BODY="$_REST_TMP/response.json"
_AUTH_TOKEN=''
_AUTH_EXPIRES=0
_DELEGATED=false
# Bounds are decimal seconds, never shell arithmetic expressions.
for _timeout_name in SMARTSEARCH_REST_TIMEOUT_SECONDS SMARTSEARCH_REST_JOB_TIMEOUT_SECONDS; do
  _timeout_value=${!_timeout_name:-60}
  case "$_timeout_value" in ''|*[!0-9]*) echo "ERROR Invalid $_timeout_name (decimal seconds required)" >&2; exit 1 ;; esac
  if [ "${#_timeout_value}" -gt 4 ] || [ "$_timeout_value" -gt 3600 ] ||
      { [ "$_timeout_name" = SMARTSEARCH_REST_TIMEOUT_SECONDS ] && [ "$_timeout_value" -eq 0 ]; }; then
    echo "ERROR Invalid $_timeout_name (request 1..3600, job 0..3600 seconds)" >&2; exit 1
  fi
done

_required() {
  local name=$1 value=${!1:-}
  if [ -z "$value" ]; then echo "ERROR Missing $name (see .env.example)" >&2; return 1; fi
  printf '%s' "$value"
}
_url() {
  local value=$1
  case "$value" in
    https://*) ;;
    http://127.0.0.1:*|http://localhost:*|http://\[::1\]:*) ;;
    *) echo 'ERROR Use HTTPS; HTTP allowed only for loopback fixtures' >&2; return 1 ;;
  esac
  case "$value" in *'@'*|*'?'*|*'#'*|*'\'*|*' '*|*$'\n'*|*$'\r'*) echo 'ERROR Invalid connection URL' >&2; return 1 ;; esac
  printf '%s' "${value%/}"
}
_segment() {
  case "$1" in ''|.|..|*$'\n'*|*$'\r'*) echo 'ERROR Invalid resource ID' >&2; return 1 ;; esac
  jq -nr --arg id "$1" '$id|@uri'
}
_form_field() {
  # Values are encoded with jq, not interpolated into a URL or curl argv.
  printf '%s=%s' "$1" "$(printf '%s' "$2" | jq -Rrs '@uri')"
}
_token_request() {
  local kind=$1 credential=${2:-} auth realm status now client_id client_secret
  auth=$(_url "$(_required SMARTSEARCH_AUTH_BASE_URL)") || return 1
  realm=$(_segment "$(_required SMARTSEARCH_REALM)") || return 1
  client_id=$(_required SMARTSEARCH_CLIENT_ID) || return 1
  client_secret=$(_required SMARTSEARCH_CLIENT_SECRET) || return 1
  : > "$_REST_TMP/form"
  case "$kind" in
    service) printf 'grant_type=client_credentials' >> "$_REST_TMP/form" ;;
    assertion) _form_field grant_type 'urn:ietf:params:oauth:grant-type:jwt-bearer' >> "$_REST_TMP/form"; printf '&' >> "$_REST_TMP/form"; _form_field assertion "$credential" >> "$_REST_TMP/form" ;;
    token)
      _form_field grant_type 'urn:ietf:params:oauth:grant-type:token-exchange' >> "$_REST_TMP/form"
      printf '&' >> "$_REST_TMP/form"; _form_field subject_token "$credential" >> "$_REST_TMP/form"
      printf '&' >> "$_REST_TMP/form"; _form_field subject_token_type 'urn:ietf:params:oauth:token-type:access_token' >> "$_REST_TMP/form"
      printf '&' >> "$_REST_TMP/form"; _form_field requested_token_type 'urn:ietf:params:oauth:token-type:access_token' >> "$_REST_TMP/form"
      printf '&audience=workspace-api&audience=cloud-gateway&audience=search-admin' >> "$_REST_TMP/form" ;;
    *) echo 'ERROR Invalid grant' >&2; return 1 ;;
  esac
  printf '&' >> "$_REST_TMP/form"; _form_field client_id "$client_id" >> "$_REST_TMP/form"
  printf '&' >> "$_REST_TMP/form"; _form_field client_secret "$client_secret" >> "$_REST_TMP/form"
  now=$(date +%s)
  if ! status=$(curl -q --silent --show-error --connect-timeout 5 --max-time 30 --max-filesize 2097152 \
      --request POST --header 'Content-Type: application/x-www-form-urlencoded' \
      --data-binary "@$_REST_TMP/form" --output "$_REST_TMP/token.json" --write-out '%{http_code}' \
      "$auth/realms/$realm/protocol/openid-connect/token" 2>/dev/null); then
    echo 'ERROR TOKEN_ACQUISITION_FAILED' >&2; return 1
  fi
  if [ "$status" != 200 ] || ! jq -e '.access_token|type=="string"' "$_REST_TMP/token.json" >/dev/null 2>&1 ||
      ! jq -e '(.access_token|test("^[!-~]+$")) and (.expires_in|type=="number" and .>0 and floor==.) and (.token_type|ascii_downcase=="bearer")' "$_REST_TMP/token.json" >/dev/null 2>&1; then
    echo 'ERROR TOKEN_ACQUISITION_FAILED' >&2; return 1
  fi
  _AUTH_TOKEN=$(jq -r '.access_token' "$_REST_TMP/token.json")
  _AUTH_EXPIRES=$((now + $(jq -r '.expires_in' "$_REST_TMP/token.json")))
  rm -f "$_REST_TMP/form" "$_REST_TMP/token.json"
}
_service_token() {
  if [ "$(date +%s)" -ge "$((_AUTH_EXPIRES - 30))" ]; then _token_request service; fi
}
rest_as_user() {
  local credential
  case "$1" in
    token)
      credential=$(_required SMARTSEARCH_USER_ACCESS_TOKEN) || return 1
      _token_request token "$credential" || return 1
      ;;
    assertion)
      credential=$(_required SMARTSEARCH_USER_ASSERTION) || return 1
      _token_request assertion "$credential" || return 1
      ;;
    *) return 1 ;;
  esac
  _DELEGATED=true
  echo 'Using a short-lived delegated user identity; it is never renewed as a service identity.'
}
_config() {
  printf 'header = %s\n' "$(printf '%s' "Authorization: Bearer $_AUTH_TOKEN" | jq -Rs '.')" > "$_REST_TMP/curl.conf"
}
_request() {
  local origin=$1 path=$2 method=$3 body=${4:-} key=${5:-} status attempt=0
  if [ -z "$origin" ]; then echo "ERROR Missing request origin" >&2; return 1; fi
  if [ "$_DELEGATED" = true ]; then
    if [ "$(date +%s)" -ge "$_AUTH_EXPIRES" ]; then echo 'ERROR USER_TOKEN_EXPIRED' >&2; return 1; fi
    case "$path" in /workspace/v1/workspaces/*/search|/workspace/v1/workspaces/*/query|/workspace/v1/workspaces/*/chat) ;; *) echo 'ERROR Delegated calls are Workplace-only' >&2; return 1 ;; esac
  else _service_token || return 1; fi
  echo "$method $path"
  if [ -n "$body" ]; then printf '%s' "$body" > "$_REST_TMP/request.json"; jq . "$_REST_TMP/request.json"; fi
  while :; do
    _config
    if [ -n "$key" ]; then printf 'header = %s\n' "$(jq -nr --arg value "Idempotency-Key: $key" '$value|@json')" >> "$_REST_TMP/curl.conf"; fi
    local args=(--request "$method")
    if [ -n "$body" ]; then args+=(--data-binary "@$_REST_TMP/request.json"); fi
    if ! status=$(curl -q --silent --show-error --config "$_REST_TMP/curl.conf" --connect-timeout 5 --max-time "${SMARTSEARCH_REST_TIMEOUT_SECONDS:-60}" --max-filesize 2097152 \
      --header 'Accept: application/json' --header 'Content-Type: application/json' \
      "${args[@]}" --output "$_BODY" --write-out '%{http_code}' "$origin$path" 2>/dev/null); then
      echo 'ERROR TRANSPORT_FAILED (check URL or timeout)' >&2; return 1
    fi
    if [ "$status" = 401 ] && [ "$_DELEGATED" = false ] && [ "$attempt" = 0 ]; then
      _AUTH_EXPIRES=0; _service_token || return 1; attempt=1; continue
    fi
    break
  done
  case "$status" in 2??) ;; *)
    local code
    code=$(jq -r '(.code // .error.code // "REQUEST_REFUSED")|select(type=="string" and test("^[A-Z][A-Z0-9_]{0,95}$"))' "$_BODY" 2>/dev/null || true)
    echo "ERROR HTTP $status ${code:-REQUEST_REFUSED}" >&2; return 1 ;;
  esac
  if ! jq -e 'type=="object"' "$_BODY" >/dev/null 2>&1; then echo 'ERROR INVALID_RESPONSE' >&2; return 1; fi
}
rest_project_search() {
  local project path
  project=$(_segment "$(_required SMARTSEARCH_PROJECT_ID)")
  path="/core/projects/$project/search"
  _request "$(_url "$(_required SMARTSEARCH_API_BASE_URL)")" "$path" POST "$1"
  _core_success
  rest_print_hits
}
_core_success() {
  if ! jq -e '.code==1' "$_BODY" >/dev/null; then echo 'ERROR SEARCH_APPLICATION_FAILED (HTTP success is not Core success)' >&2; return 1; fi
}
rest_print_hits() {
  jq '{received_hits:(.result.hits.hits|length),actual_mode:.effective_neural_mode,warning} , (.result.hits.hits[]? | {_source,highlight})' "$_BODY"
}
rest_usecase_search() {
  local id; id=$(_segment "$(_required SMARTSEARCH_USECASE_ID)")
  _request "$(_url "$(_required SMARTSEARCH_API_BASE_URL)")" "/core/usecases/$id/search" POST "$1"
  _core_success; rest_print_hits
}
rest_multi_search() {
  local id; id=$(_segment "$(_required SMARTSEARCH_PROJECT_ID)")
  _request "$(_url "$(_required SMARTSEARCH_API_BASE_URL)")" "/core/projects/$id/mSearch" POST "$1"
  _core_success
  # A server may ignore per-query sizes: each response is sliced only for display.
  jq '.result.responses|to_entries[]|{query_index:.key,received:(.value.hits.hits|length),shown:.value.hits.hits[:5]}' "$_BODY"
}
rest_workspace() {
  local operation=$1 id
  case "$operation" in search|query|chat) ;; *) return 1 ;; esac
  id=$(_segment "$(_required SMARTSEARCH_WORKSPACE_ID)")
  _request "$(_url "$(_required SMARTSEARCH_API_BASE_URL)")" "/workspace/v1/workspaces/$id/$operation" POST "$2"
  jq . "$_BODY"
}
rest_stream() {
  local operation=$1 id api
  case "$operation" in query|chat) ;; *) return 1 ;; esac
  if [ "$_DELEGATED" = true ]; then echo 'ERROR This stream example uses the service identity; delegated streaming is not exposed here' >&2; return 1; fi
  _service_token || return 1; _config
  id=$(_segment "$(_required SMARTSEARCH_WORKSPACE_ID)"); api=$(_url "$(_required SMARTSEARCH_API_BASE_URL)")
  printf '%s' "$2" > "$_REST_TMP/request.json"
  echo "POST /workspace/v1/workspaces/$id/$operation (Accept: text/event-stream)"
  jq . "$_REST_TMP/request.json"
  # --fail refuses HTTP errors; no -L means a redirect never receives the credential.
  # The parser requires valid completion and cancels reading on failure; provisional text is not success.
  if ! curl -q --silent --show-error --fail --no-buffer --config "$_REST_TMP/curl.conf" --connect-timeout 5 --max-time "${SMARTSEARCH_REST_TIMEOUT_SECONDS:-60}" \
      --header 'Accept: text/event-stream' --header 'Content-Type: application/json' \
      --data-binary "@$_REST_TMP/request.json" "$api/workspace/v1/workspaces/$id/$operation" 2>/dev/null | python3 "$_REST_DIR/lib/sse.py"; then
    echo 'ERROR STREAM_FAILED (discard provisional text)' >&2; return 1
  fi
}
rest_capabilities() {
  _request "$(_url "$(_required SMARTSEARCH_ADMIN_BASE_URL)")" '/search-admin/api/provisioning/v1/capabilities' GET
  jq . "$_BODY"
}
rest_register() {
  local kind=$1 key=$2 body=$3 admin job start now state
  admin=$(_url "$(_required SMARTSEARCH_ADMIN_BASE_URL)")
  _request "$admin" '/search-admin/api/provisioning/v1/jobs' POST "$body" "$key"
  job=$(jq -er '.job_id|select(type=="string" and length>0)' "$_BODY"); job=$(_segment "$job")
  start=$(date +%s)
  while :; do
    _request "$admin" "/search-admin/api/provisioning/v1/jobs/$job" GET
    state=$(jq -r '.state' "$_BODY")
    case "$state" in SUCCEEDED|PARTIAL|FAILED|RECONCILIATION_REQUIRED|CANCELLED) break ;; esac
    now=$(date +%s)
    if [ "$((now - start))" -ge "${SMARTSEARCH_REST_JOB_TIMEOUT_SECONDS:-60}" ]; then echo 'ERROR PROVISIONING_JOB_TIMEOUT (continue polling the submitted job)' >&2; return 1; fi
    sleep 1
  done
  echo "Job finished: $state (inspect each item; terminal does not mean every user succeeded)"
  _request "$admin" "/search-admin/api/provisioning/v1/jobs/$job/items?limit=100" GET
  jq '.values[]?|{item_key,state,principal_id,error_code}' "$_BODY"
  if [ "$kind" = UPSERT_USERS ]; then
    local integration tenant
    integration=$(_segment "$(_required SMARTSEARCH_INTEGRATION_ID)")
    tenant=$(jq -nr --arg v "$(_required SMARTSEARCH_TENANT_ID)" '$v|@uri')
    _request "$admin" "/search-admin/api/provisioning/v1/integrations/$integration/principals?scope_kind=TENANT&scope_id=$tenant&limit=100" GET
    jq '.values[]?|{external_user_id,principal_id}' "$_BODY"
  fi
}
