#!/usr/bin/env bash
# Runs one example: ./run.sh Ex01ServiceConnect [args...]
# Loads ../../.env when present, so settings stay out of your shell history.
set -euo pipefail
cd "$(dirname "$0")"
if [ $# -lt 1 ]; then
  echo "usage: ./run.sh <ExampleClass> [args...]   e.g. ./run.sh P01BasicSearch \"star wars\"" >&2
  exit 2
fi
if [ -f ../../.env ]; then set -a; . ../../.env; set +a; fi
example=$1; shift
if [ $# -gt 0 ]; then
  quoted=""; for arg in "$@"; do quoted="$quoted '${arg//\'/}'"; done   # keep multi-word arguments together
  exec mvn -q compile exec:java -Dexec.mainClass="examples.$example" -Dexec.args="$quoted"
else
  exec mvn -q compile exec:java -Dexec.mainClass="examples.$example"
fi
