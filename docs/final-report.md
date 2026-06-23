# Final Project Report

## Implemented UNO Rules

The final project implements the full rule menu from the local final-project
reference at a course-project level: classic 108-card deck composition, legal
play validation, Skip, Reverse, Draw Two, Wild, Wild Draw Four, draw/pass flow,
UNO calls with missed-call penalty, round scoring, and multi-round target-score
play.

The supported rule details and simplifications are listed in
`docs/rules-supported.md`.

## CLI Play

The zero-configuration runtime path is Docker Compose:

```bash
docker compose up --build --abort-on-container-exit --exit-code-from app app
```

That command builds the app, starts PostgreSQL, runs the original deterministic
five-round demo, runs a target-score demo, and prints persisted history reports.

Useful direct gameplay commands:

```bash
scripts/run.sh --bots 3 --games 5 --quiet --seed 123
scripts/run.sh --bots 3 --target-score 500 --quiet --seed 123
scripts/run.sh --human --bots 2 --games 1
```

On a human turn, enter a card index, card code, or `draw`. To call UNO, append
`uno` to the play, for example `3 uno`, `R5 uno`, or `yes uno` when asked about
a playable drawn card.

## Architecture

The rule logic is testable without console input. The main separation is:

* `game`: card parsing, legal play rules, turn effects, deck, hands, scoring,
  UNO penalty, and target-score detection.
* `controller`: startup orchestration, turn flow, round/session loop, history
  save/report use cases, and logging.
* `ui` and `ui.cli`: startup argument parsing, player input parsing, prompts,
  and formatted CLI output.
* `history`: immutable completed-game records and persistence-facing service
  interfaces.
* `history.persistence`: Spring Data JPA entities and repositories.

Spring Data JPA repository interfaces own database access. Flyway owns schema
creation and migration. Game and controller code do not bind SQL manually.

## Tests Added

The final project adds rubric-shaped JUnit tests for:

* exact deck composition and legal play validation
* action-card behavior
* draw/pass and scoring behavior
* UNO input parsing and missed-UNO penalty
* target-score session behavior
* target-score persistence metadata through the real controller flow

The previous 75 characterization checks and A5 persistence/report tests remain
part of `mvn clean verify`.

## Persistence And Docker

Assignment 5 persistence still works with PostgreSQL through Docker Compose.
The final project adds Flyway migration V2 with:

* `uno_games.target_score`
* `uno_games.completion_reason`

The Compose demo verifies both normal round-cap sessions and target-score
sessions, then queries reports from the same database.

## Limitations

Wild Draw Four challenges, draw stacking, jump-in, 7-0 house rules, and multiple
human players are intentionally not implemented. The current CLI is a playable
text interface rather than a graphical game.
