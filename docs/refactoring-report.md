# Refactoring Report

## Summary

The original project was a working UNO-like CLI game where almost all behavior lived in one large `Main` class. The refactoring preserved the existing game behavior while moving the code toward a clearer MVC-like structure:

* `Main` now only starts the game.
* `controller` owns startup flow, turn orchestration, player prompts, and game session flow.
* `game` owns game state, card rules, deck/discard behavior, turn order, scoring, and bot decisions.
* `ui` and `ui.cli` own startup input parsing, player input reading, and console output.

This was done as incremental refactoring, not a rewrite. The code still uses the same simplified UNO rules and preserves the documented quirks from `docs/rules.html`.

## Behavior Characterized Before And During Refactoring

Characterization tests were expanded from the original small self-test into 75 checks. They describe the current implementation rather than ideal UNO.

The tests cover:

* card parsing for colors, ranks, numbers, wilds, and action cards
* legal play by color
* legal play by number
* legal play by action type
* wild and wild draw four legality
* the implementation quirk where any card code starting with `W` is legal as a wild prefix
* called color after a wild
* bot card priority and color selection
* drawing from the deck
* refilling the deck from the discard pile
* fallback draw behavior when both deck and discard are empty
* bot auto-playing a legal drawn card
* human players not auto-playing a legal drawn card
* human players being allowed to type `draw` even when holding a legal card
* illegal indexed card penalty behavior
* out-of-range index penalty behavior
* turn movement, skip, reverse, draw two, and wild draw four
* scoring values and losing-hand scoring
* CLI hand display formatting
* deterministic full-game score output for a fixed seed

The checks are integrated into JUnit Jupiter and run through Maven:

```bash
mvn test
```

The current expected result is:

```text
Passed 75 characterization checks.
```

## Worst Design Problems Found

The original design had several coupled responsibilities:

* startup argument parsing was mixed into the game entry point
* console input and output were mixed into turn execution
* card legality was duplicated in the turn loop and bot logic
* card effects were embedded directly in the main game loop
* deck, discard, players, scores, and turn direction were global mutable state
* bot decisions depended directly on duplicated rule checks
* tests could only reach behavior through the monolithic `Main` class or global helpers
* the `Main` class was too large to move safely in one step

The most important risk was behavior drift. Some behavior is intentionally unusual, so replacing it with cleaner UNO behavior would have been a wrong refactoring.

## Refactoring Strategy

The strategy evolved in stages.

### 1. Characterization First

The first step was to add tests around existing behavior before moving risky logic. These tests intentionally froze quirks instead of correcting them.

Examples:

* humans can draw even when they have a legal card
* illegal card-code input prints both "That card is not legal." and then "Card not found."
* illegal index input is not reprompted; it causes a penalty card and turn loss
* bots automatically play a legal drawn card

### 2. Inner-Class Blueprint

Before moving files into packages, the monolithic code was separated inside `Main` into rough inner view, model/game, and controller responsibilities. This made the separation visible while keeping the code physically close to the original implementation.

This stage was useful because it allowed small movements with tests after each step. It also exposed mistakes in responsibility placement, such as prompt validation belonging in controller flow while raw CLI reading belongs in the view layer.

### 3. Extracted MVC Skeleton

After the inner structure was stable, the layers were copied out into packages:

* `ui`
* `ui.cli`
* `controller`
* `game`

At this point `Main` became the entry point only:

```java
UnoGameController.startNewGame(new UiType.Cli(args));
```

This created the destination structure for later layer-specific refactoring.

### 4. UI Layer Refactoring

The UI layer was separated into contracts and CLI implementation classes:

* `UiType` describes which UI engine is requested.
* `UiViewFactory` creates the matching view.
* `StartupInputReader` parses startup input into generic startup data.
* `PlayerInputReader` reads player actions into structured input.
* `GameView` prints game state and messages.
* `CliView` composes the CLI input and output pieces.

The important design decision was that the controller should not receive raw `args` directly. The CLI view reads CLI-specific input and returns generic startup input that the controller can interpret.

### 5. Game Layer Refactoring

Game behavior was extracted from the old main loop into purpose-specific game classes:

* `Card`, `CardColor`, and `CardRank` hold card parsing and card meaning.
* `CardRules` centralizes legal-play and point rules.
* `BotStrategy` owns bot card and color choices.
* `DrawPile` owns deck, discard, shuffling, refilling, and fallback draw behavior.
* `Players` owns player names, human flags, hands, and scores.
* `TurnOrder` owns current player and direction.
* `PlayArea` owns the up card and called color.
* `UnoGame` acts as the public game facade used by the controller.

Duplicated legal-play checks were reduced so bot choice and turn validation use the same rule logic.

### 6. Controller Layer Refactoring

Controller responsibilities were split by orchestration concern:

* `UnoGameController` starts and wires a new game.
* `StartupActionHandler` handles help and unsupported UI actions.
* `GameSettings` converts startup input into typed runtime settings.
* `GameSessionController` owns multi-game session flow and the safety limit loop.
* `TurnController` owns one turn of gameplay.
* `PlayerPromptController` owns human prompt loops and prompt-level validation.

The controller now coordinates game and UI behavior instead of containing the game rules itself.

### 7. Cleanup And Surface Narrowing

After extraction, several cleanup passes made the package boundaries stricter:

* renamed generic model naming to `game` and `UnoGame`
* renamed CLI facade classes to describe what they do
* moved game tests beside game internals so package-private helpers do not need to become public
* hid mutable current-hand access from controller tests
* returned snapshot `List` interfaces instead of exposing concrete mutable `ArrayList` values
* changed an integration test to run through `UnoGameController.startNewGame(...)`
* removed stale controller delegates after extraction

## Git Strategy

The midterm work was organized as small refactoring branches and merged checkpoints:

* a characterization-test branch
* a CI branch
* an initial MVC blueprint branch
* a test-refactoring branch
* an MVC skeleton extraction branch
* separate UI, game, and controller layer branches
* a cleanup branch for naming and surface improvements
* a game-surface branch for stricter public API and test placement

Midterm refactoring commits were kept small and named with `chore:` because the work was behavior-preserving refactoring rather than feature work. Each meaningful refactoring step was followed by compile/test verification before the next step. The Assignment 4 continuation uses plain descriptive commit messages after Maven, logging, Docker, CI, and documentation verification gates.

This branch strategy made it possible to stop after any checkpoint with a compiling, runnable project.

## CI Pipeline

A GitHub Actions workflow was added in `.github/workflows/ci.yml`.

The workflow:

* runs on `pull_request`
* uses Ubuntu GitHub-hosted runners
* installs Java 21 with Temurin
* caches Maven dependencies
* runs `mvn --batch-mode --no-transfer-progress clean verify`
* builds the Docker image only after Maven verification succeeds

The CI intentionally does not run on every push. It runs when pull requests are opened or updated, which matches the branch-protection workflow and avoids unnecessary checks on local checkpoint pushes.

The project now follows Maven's standard `src/main/java` and `src/test/java` layout. The compatibility scripts delegate to Maven instead of maintaining a second compilation path.

## Behavior Intentionally Preserved

No gameplay behavior change was intentional.

Preserved quirks include:

* all hands are visible in the terminal
* a human can type `draw` even with a legal card in hand
* an illegal card code is rejected and reprompted
* an illegal index causes a penalty card and turn loss
* an out-of-range index causes a penalty card and turn loss
* bots automatically play legal drawn cards
* wild and wild draw four can always be played
* called color can override the up-card color for legality
* reverse with two players effectively skips the other player
* the game stops after the 3000-turn safety limit
* if deck and discard are empty, drawing returns `W`

## Risks That Remain

The design is much easier to change than the original, but a few risks remain:

* `UnoGame` is still a broad facade because the controller needs a single game API during this refactoring stage.
* Card codes still cross some public boundaries as strings because the CLI is text-based and the original behavior is based on compact text codes.
* The original characterization helpers are preserved behind four JUnit bridge tests so Maven can discover them without rewriting their assertions.
* Some game tests use package-private helpers so behavior can be characterized without widening production APIs.
* Invalid numeric startup arguments still follow the original direct parse behavior rather than introducing a new validation flow.

These risks are known and documented rather than hidden. The current design is ready for a next pass where card codes could be made more strongly typed across controller/game boundaries.

## Final Verification

The Maven and Docker continuation is verified with:

```bash
mvn clean verify
java -jar target/uno-cli.jar --bots 3 --games 5 --quiet --seed 123
docker build -t uno-cli .
docker run --rm uno-cli --bots 3 --games 5 --quiet --seed 123
```

The deterministic smoke run produced:

```text
Final scores:
Bot1: 138
Bot2: 246
Bot3: 98
```
