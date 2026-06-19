#!/usr/bin/env sh
set -eu

if [ "$#" -eq 0 ] || [ "$1" = "demo" ] || [ "$1" = "--demo" ]; then
    java -jar /app/uno-cli.jar --bots 3 --games 5 --quiet --seed 123
    java -jar /app/uno-cli.jar --recent-games 10
    java -jar /app/uno-cli.jar --player-wins Bot2
    java -jar /app/uno-cli.jar --highest-scores 10
else
    exec java -jar /app/uno-cli.jar "$@"
fi
