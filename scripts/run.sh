#!/usr/bin/env sh
set -eu

docker compose up -d --wait database
docker compose run --rm --build app "$@"

