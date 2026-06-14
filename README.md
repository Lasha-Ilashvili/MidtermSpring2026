# UNO CLI

This repository contains a behavior-preserving refactor of the midterm UNO-like
command-line game. The application targets Java 21, uses Maven for builds and
tests, writes player-facing output to stdout, and writes diagnostic logs to
stderr.

## Requirements

Local Maven commands require:

* JDK 21 or newer
* Maven 3.9 or newer

Docker commands require Docker Desktop or another running Docker engine. The
Docker build supplies its own Maven and Java 21 environments, so the host does
not need Maven or Java to run the containerized application.

## Build And Test

Compile the project:

```bash
mvn clean compile
```

Run the JUnit suite and all 75 characterization checks:

```bash
mvn test
```

Create the self-contained executable JAR:

```bash
mvn clean package
```

The Maven Assembly Plugin writes the application and all runtime dependencies
to:

```text
target/uno-cli.jar
```

## Run Locally

Run through Maven from a clean terminal:

```bash
mvn compile exec:java -Dexec.args="--bots 3 --games 5 --quiet"
```

After compilation, the shorter equivalent is:

```bash
mvn exec:java -Dexec.args="--bots 3 --games 5 --quiet"
```

Run the packaged application:

```bash
java -jar target/uno-cli.jar --bots 3 --games 5 --quiet
```

Run an interactive game:

```bash
java -jar target/uno-cli.jar --human --bots 2 --games 1
```

## Docker

Build the image:

```bash
docker build -t uno-cli .
```

Run the finite default bot game:

```bash
docker run --rm uno-cli
```

Override the default arguments:

```bash
docker run --rm uno-cli --bots 3 --games 5 --quiet --seed 123
```

Run an interactive game:

```bash
docker run --rm -it uno-cli --human --bots 2 --games 1
```

The image builds the Maven project in a Java 21 builder stage, then runs only
`/app/uno-cli.jar` on a Java 21 JRE as an unprivileged user.

## Command-Line Options

| Option | Meaning |
|---|---|
| `--bots N` | Set the number of bot players. |
| `--games N` | Set the number of games in the session. |
| `--human` | Add a human player before the configured bots. |
| `--quiet` | Hide turn-by-turn player output. |
| `--seed N` | Use a deterministic random seed. |
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

## Logging

SLF4J with Logback records game starts, turns, played and drawn cards, invalid
input, round endings, and session endings. Logs are written to stderr so normal
CLI output remains readable on stdout.

For example, capture the two streams separately:

```bash
java -jar target/uno-cli.jar --bots 3 --games 1 --quiet > scores.txt 2> game.log
```

## Optional Script Shortcuts

The legacy scripts remain available as Maven-backed shortcuts:

```bash
scripts/compile.sh
scripts/test.sh
scripts/run.sh --bots 3 --games 5 --quiet
```

Maven is the primary build system. GitHub Actions runs `mvn clean verify` and
then builds the Docker image for pull requests.

## Project Documentation

* `docs/rules.html`: implemented game rules
* `docs/refactoring-report.md`: behavior-preserving refactoring history
* `docs/extension-readiness.md`: supported extension points
* `docs/midterm-exam.md`: original midterm brief
* `docs/rubric.md`: original midterm rubric
