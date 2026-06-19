# Game History Persistence

## Technology

The application uses:

* Spring Boot in non-web CLI mode
* Spring Data JPA repository interfaces
* Hibernate as the JPA provider
* Flyway for schema migrations
* PostgreSQL 18 for runtime persistence
* H2 for isolated persistence tests

Application code does not bind SQL parameters or map result rows manually.
Flyway migration files are the only raw SQL in the project.

## Dependency Scopes

The normal application artifact includes PostgreSQL and Flyway's PostgreSQL
database module as runtime dependencies:

```bash
mvn clean package
```

H2 is scoped to tests only. It is used by `@DataJpaTest` and does not act as a
fallback for normal gameplay. Runtime database configuration uses:

```text
UNO_DATABASE_URL
UNO_DATABASE_USERNAME
UNO_DATABASE_PASSWORD
```

`scripts/run.sh` and Docker Compose provide these settings for the bundled
PostgreSQL service. Direct JAR execution should set them explicitly when using
an external database.

## Schema Management

Flyway automatically applies:

```text
src/main/resources/db/migration/V1__create_game_history.sql
```

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

This means Flyway creates and evolves the schema while Hibernate checks that
the entity mappings still match it.

## Schema

### `players`

Stores reusable player identities:

* display name
* normalized case-insensitive name
* unique normalized-name constraint

### `uno_games`

Stores one CLI gameplay invocation:

* start timestamp
* completion timestamp
* requested round count
* completed round count

### `game_players`

Associates players with a game:

* player order
* final cumulative score
* final-winner flag

Multiple rows may be marked as winners when final scores are tied.

### `uno_rounds`

Stores each `--games` iteration:

* round number
* start and completion timestamps
* `COMPLETED` or `SAFETY_LIMIT` status
* round winner
* points awarded

### `round_scores`

Stores each player's score accounting for every round:

* score before the round
* score delta
* score after the round

Database checks require `score_after = score_before + score_delta`.

## Repository Design

The persistence layer exposes Spring Data interfaces:

* `PlayerRepository`
* `GameRepository`
* `GamePlayerRepository`
* `RoundRepository`
* `RoundScoreRepository`

Simple operations use derived methods, including normalized-name lookup,
recent-game ordering, and player win counts. Recent games use `@EntityGraph`
to fetch their player summaries efficiently. Highest scores use an annotated
JPQL query projected directly into `HighestScoreProjection`.

`GameHistoryService` is the transactional boundary. Controllers exchange
immutable history records through `GameHistoryWriter` and `GameHistoryReader`;
they do not depend on JPA entities or Spring repositories.

## Persistence Tests

Run all tests:

```bash
mvn clean verify
```

`GameHistoryRepositoryTest` uses `@DataJpaTest` with an isolated in-memory H2
database. It verifies:

* Flyway migration V1 is applied
* Hibernate validates the schema
* a complete game graph persists through repository cascades
* test transactions roll back between cases
* player lookup, recent-game ordering, win count, and high-score queries work

`GameHistoryFlowIntegrationTest` runs the real deterministic controller flow
through `GameHistoryService`. It proves that the five-round seed `123` session
stores 3 players, 5 rounds, 15 round-score rows, final scores `138`, `246`, and
`98`, and `Bot2` as final winner.

Tests do not require Docker, PostgreSQL, or developer-specific machine state.

## Docker PostgreSQL

Build and start the database:

```bash
docker compose build app
docker compose up -d database
```

The official PostgreSQL 18 image stores data under `/var/lib/postgresql`.
Compose mounts the named `uno-cli-a5_uno-postgres-data` volume there.

No `.env` file is required for the default local setup. The database uses trust
authentication on the private Compose network and does not publish a host port.
For a password-protected external database, set `UNO_DATABASE_URL`,
`UNO_DATABASE_USERNAME`, and `UNO_DATABASE_PASSWORD` before running the app.

Run the complete Assignment 4 and 5 Docker demonstration:

```bash
docker compose up --build --abort-on-container-exit --exit-code-from app app
```

This builds the image if needed, starts PostgreSQL, runs the deterministic
five-round bot game, and then runs all three report queries against the same
database.

Run individual games and reports:

```bash
scripts/run.sh
scripts/run.sh --bots 3 --games 5 --quiet --seed 123
scripts/run.sh --recent-games 10
scripts/run.sh --player-wins Bot2
scripts/run.sh --highest-scores 10
```

The equivalent raw Compose commands are:

```bash
docker compose run --rm app --bots 3 --games 5 --quiet --seed 123
docker compose run --rm app --recent-games 10
docker compose run --rm app --player-wins Bot2
docker compose run --rm app --highest-scores 10
```

Stop containers while retaining history:

```bash
docker compose down
```

Delete containers and stored history:

```bash
docker compose down --volumes
```

## Report Semantics

* `--recent-games [N]` returns newest sessions first.
* `--player-wins NAME` performs a case-insensitive player lookup and returns
  zero for an unknown player.
* `--highest-scores [N]` ranks final player scores descending, then completion
  time descending.
* Optional limits default to 10 and accept values from 1 through 100.
* Report modes are mutually exclusive and cannot be mixed with gameplay flags.
