#!/usr/bin/env bash
# Compile, then run the example with the original argv unchanged.
# Quoting belongs to your shell, not to Maven's string-based exec.args parser.
set -euo pipefail
cd "$(dirname "$0")"
if [ $# -lt 1 ]; then
  echo 'usage: ./run.sh <ExampleClass> [args...]   e.g. ./run.sh FirstSearch "star wars"' >&2
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
# Build the dependency classpath as a file; no query text is interpolated into a command.
mvn -q compile dependency:build-classpath -Dmdep.outputFile=target/example-classpath.txt
classpath=$(cat target/example-classpath.txt)
exec java -cp "target/classes:$classpath" "$class" "$@"
