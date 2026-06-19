# UNO CLI

This repository contains a behavior-preserving refactor of the midterm UNO-like
command-line game. It targets Java 21, uses Maven and Spring Data JPA, manages
its schema with Flyway, and persists game history to PostgreSQL.

Player-facing output is written to stdout. SLF4J and Logback diagnostics are
written to stderr.

## Requirements

Local Maven commands require:

* JDK 21 or newer
* Maven 3.9 or newer

Containerized PostgreSQL commands require Docker Desktop or another running
Docker engine. Docker supplies its own Maven and Java 21 environments.

## Build And Test

Compile the project:

```bash
mvn clean compile
```

Run all tests, including the 75 characterization checks and isolated H2
persistence tests:

```bash
mvn clean verify
```

Create the self-contained executable Spring Boot JAR:

```bash
mvn clean package
```

The artifact is:

```text
target/uno-cli.jar
```

The application artifact includes the PostgreSQL runtime driver. H2 is scoped
to tests only, so normal gameplay does not silently fall back to a local H2
database.

## Run With PostgreSQL

The zero-configuration runtime path is Docker Compose. Compose starts
PostgreSQL 18, waits for it to become healthy, builds the app image when
needed, runs a deterministic five-round game, then runs all three persisted
history reports required by Assignment 5:

```bash
docker compose up --build --abort-on-container-exit --exit-code-from app app
```

The same full demo is available through the script wrapper:

```bash
scripts/run.sh
```

Run a custom game:

```bash
scripts/run.sh --bots 3 --games 5 --quiet --seed 123
```

Run an interactive game:

```bash
scripts/run.sh --human --bots 2 --games 1
```

Direct JAR execution is still supported when a PostgreSQL database is already
reachable and the standard environment variables are supplied:

```bash
UNO_DATABASE_URL=jdbc:postgresql://localhost:5432/uno \
UNO_DATABASE_USERNAME=uno \
java -jar target/uno-cli.jar --bots 3 --games 5 --quiet --seed 123
```

Flyway applies `src/main/resources/db/migration/V1__create_game_history.sql`,
then Hibernate validates the entity mappings against that schema.

## History Reports

List the 10 most recent games:

```bash
scripts/run.sh --recent-games
```

Set a result limit from 1 to 100:

```bash
scripts/run.sh --recent-games 5
scripts/run.sh --highest-scores 10
```

Show a case-insensitive player win count:

```bash
scripts/run.sh --player-wins "Bot2"
```

Report options are mutually exclusive and cannot be combined with gameplay
options.

## PostgreSQL With Docker Compose

Build the app image and start PostgreSQL 18:

```bash
docker compose build app
docker compose up -d database
```

No `.env` file is required for the local Compose setup. PostgreSQL uses trust
authentication on the private Compose network, and the database port is not
published to the host. `.env.example` documents optional overrides for a custom
database name, user, or external password-protected database.

Run and persist a deterministic session:

```bash
docker compose run --rm app --bots 3 --games 5 --quiet --seed 123
```

Run the complete assignment demo from Compose:

```bash
docker compose up --build --abort-on-container-exit --exit-code-from app app
```

Read the stored reports from later containers:

```bash
docker compose run --rm app --recent-games 10
docker compose run --rm app --player-wins Bot2
docker compose run --rm app --highest-scores 10
```

Inspect the schema directly with the default example database and user names:

```bash
docker compose exec database psql -U uno -d uno -c "\dt"
docker compose exec database psql -U uno -d uno -c "SELECT * FROM uno_games ORDER BY completed_at DESC;"
```

Stop the services:

```bash
docker compose down
```

The named `uno-cli-a5_uno-postgres-data` volume retains data across container
recreation. To deliberately delete the database as well:

```bash
docker compose down --volumes
```

## Command-Line Options

| Option | Meaning |
|---|---|
| `--bots N` | Set the number of bot players. |
| `--games N` | Set the number of rounds in the persisted session. |
| `--human` | Add a human player before the configured bots. |
| `--quiet` | Hide turn-by-turn player output. |
| `--seed N` | Use a deterministic random seed. |
| `--recent-games [N]` | List recent persisted sessions. |
| `--player-wins NAME` | Show a player's persisted win count. |
| `--highest-scores [N]` | List the highest final player scores. |
| `--help` | Print command usage. |

UNO requires a total of two to four players. With `--human`, the bot count must
leave room for the human player.

Card input examples:

```text
R5   red 5
YS   yellow skip
BR   blue reverse
G+2  green draw two
W    wild
W4   wild draw four
draw draw a card
```

## Persistence Design

Spring Data JPA repository interfaces provide all application database access.
Derived repository methods handle simple lookups and win counts. An annotated
JPQL projection query handles the high-score report. Game and controller code
contains no JDBC, `EntityManager`, row mapping, or raw SQL.

Flyway owns schema creation. Hibernate uses `ddl-auto=validate` and never
creates production tables. PostgreSQL 18 is used for runtime persistence. H2 is
used only for isolated `@DataJpaTest` tests.

Runtime database settings can be supplied through environment variables. See
[docs/database.md](docs/database.md) for the schema, repository design, and
test details.

## Logging

SLF4J with Logback records game starts, turns, played and drawn cards, invalid
input, round endings, session endings, and successful or failed history saves.
Logs are written to stderr so normal CLI and report output remains on stdout.

Capture the streams separately:

```bash
docker compose build app
docker compose up -d --wait database
docker compose run --rm app --bots 3 --games 1 --quiet > scores.txt 2> game.log
```

## Optional Script Shortcuts

The scripts remain the shortest verified workflow:

```bash
scripts/compile.sh
scripts/test.sh
scripts/run.sh
```

Maven is the primary build system for compilation and tests. GitHub Actions
runs `mvn clean verify` and then builds the Docker image for pull requests.

## Project Documentation

* `docs/database.md`: persistence architecture, schema, setup, and reports
* `docs/rules.html`: implemented game rules
* `docs/refactoring-report.md`: behavior-preserving refactoring history
* `docs/extension-readiness.md`: supported extension points
* `docs/midterm-exam.md`: original midterm brief
* `docs/rubric.md`: original midterm rubric
