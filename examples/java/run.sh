#!/usr/bin/env bash
# Runs one example by its class name: ./run.sh FirstSearch [args...]
# Finds the class in any topic package (gettingstarted, search, workplace, users).
# Loads ../../.env when present, so settings stay out of your shell history.
set -euo pipefail
cd "$(dirname "$0")"
if [ $# -lt 1 ]; then
  echo "usage: ./run.sh <ExampleClass> [args...]   e.g. ./run.sh FirstSearch \"star wars\"" >&2
  exit 2
fi
if [ -f ../../.env ]; then set -a; . ../../.env; set +a; fi
name=$1; shift
matches=$(find src/main/java/examples -name "${name##*.}.java" | sort)
count=$(printf '%s' "$matches" | grep -c . || true)
if [ "$count" -ne 1 ]; then
  echo "No single example named $name (found $count). See the learning path in README.md." >&2
  exit 2
fi
class=$(printf '%s' "$matches" | sed -e 's#^src/main/java/##' -e 's#\.java$##' -e 's#/#.#g')
if [ $# -gt 0 ]; then
  quoted=""; for arg in "$@"; do quoted="$quoted '${arg//\'/}'"; done   # keep multi-word arguments together
  exec mvn -q compile exec:java -Dexec.mainClass="$class" -Dexec.args="$quoted"
else
  exec mvn -q compile exec:java -Dexec.mainClass="$class"
fi
