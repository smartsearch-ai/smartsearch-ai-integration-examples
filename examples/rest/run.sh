#!/usr/bin/env bash
# Run one public REST learning step. Bash 3.2 compatible; help/list are completely offline.
set -euo pipefail
cd "$(dirname "$0")"
name=${1:---help}
if [ "$#" -gt 0 ]; then shift; fi
if [ "$name" = --help ] || [ "$name" = --list ]; then
  echo 'Usage: ./run.sh <example_name> [args...]'
  while IFS='|' read -r step example topic description; do
    printf '%s. %s: %s\n' "$step" "$example" "$description"
  done < examples.tsv
  exit 0
fi
entry=$(awk -F '|' -v name="$name" '$2==name {print $0}' examples.tsv)
if [ -z "$entry" ]; then echo 'Unknown example; use ./run.sh --list' >&2; exit 2; fi
IFS='|' read -r step example topic description <<< "$entry"
script="$topic/$example.sh"
if [ "${1:-}" = --help ]; then
  sed -n '/^#/p' "$script"
  exit 0
fi
for command in curl jq python3; do
  if ! command -v "$command" >/dev/null; then echo "ERROR Install $command (see REST README)" >&2; exit 1; fi
done
# Tests explicitly set SMARTSEARCH_REST_ENV_FILE to /dev/null, never reading a real .env.
env_file=${SMARTSEARCH_REST_ENV_FILE:-../../.env}
if [ -f "$env_file" ]; then set -a; . "$env_file"; set +a; fi
. lib/common.sh
. "$script"
