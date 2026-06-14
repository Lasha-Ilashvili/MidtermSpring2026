#!/usr/bin/env sh
set -eu

mvn package -DskipTests
java -jar target/uno-cli.jar "$@"

