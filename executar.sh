#!/usr/bin/env sh
set -eu
cd "$(CDPATH='' cd -- "$(dirname -- "$0")" && pwd)"
if [ ! -f target/controle-financeiro-1.0.0.jar ]; then
  echo 'Compile primeiro: ./mvnw clean package'
  exit 1
fi
if [ -n "${JAVA_HOME:-}" ]; then
  exec "$JAVA_HOME/bin/java" -jar target/controle-financeiro-1.0.0.jar
else
  exec java -jar target/controle-financeiro-1.0.0.jar
fi
