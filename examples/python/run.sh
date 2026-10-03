#!/usr/bin/env bash
# Runs one example by its file name: ./run.sh first_search [args...]
# Finds the script in any topic folder (gettingstarted, search, workplace, users).
# Loads ../../.env when present, so settings stay out of your shell history.
# Uses python3 from PATH (your virtual environment), or $PYTHON when set.
set -euo pipefail
cd "$(dirname "$0")"
if [ $# -lt 1 ]; then
  echo "usage: ./run.sh <example> [args...]   e.g. ./run.sh first_search \"star wars\"" >&2
  exit 2
fi
if [ -f ../../.env ]; then set -a; . ../../.env; set +a; fi
name=${1%.py}; shift
matches=$(find examples -name "$name.py" | sort)
count=$(printf '%s' "$matches" | grep -c . || true)
if [ "$count" -ne 1 ]; then
  echo "No single example named $name (found $count). See the learning path in README.md." >&2
  exit 2
fi
module=$(printf '%s' "$matches" | sed -e 's#\.py$##' -e 's#/#.#g')
exec "${PYTHON:-python3}" -m "$module" "$@"
