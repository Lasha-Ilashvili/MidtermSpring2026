# Extension Readiness

## Best Supported Extension: Replace Or Improve The CLI View

The current design best supports replacing or improving the CLI view.

This extension was chosen because the original code mixed console input, console output, startup parsing, and game rules in one class. The refactored design now gives UI behavior a separate place:

* `UiType` describes the requested UI engine.
* `UiViewFactory` creates the requested UI implementation.
* `UiView` combines the input and output contracts needed by the controller.
* `StartupInputReader` reads startup data.
* `PlayerInputReader` reads player actions.
* `GameView` renders game state and messages.
* `ui.cli` contains the current CLI implementation.

The game and controller layers no longer depend directly on `Scanner` or `System.out`.

## Where The Extension Would Be Implemented

A second UI could be added by:

1. Adding a new `UiType` record, for example `UiType.Gui` or `UiType.Scripted`.
2. Adding a new package beside `ui.cli`, for example `ui.gui` or `ui.scripted`.
3. Implementing `UiView` for the new UI.
4. Updating `UiViewFactory.create(...)` to return the new implementation.

The controller would still call:

```java
UnoGameController.startNewGame(uiType);
```

The game rules would stay in `game`, and the turn/session flow would stay in `controller`.

## Why This Is Easier Now

The original `Main` class read CLI arguments, read player input, printed output, validated moves, executed card effects, scored rounds, and advanced turns in one procedural flow.

Now those responsibilities are separated:

* startup parsing is isolated from game setup
* player input is converted into `PlayerInput.CardChoice`
* console rendering is behind `GameView`
* turn orchestration is in `TurnController`
* game rules and state are behind `UnoGame`

Because of this, a new UI does not need to duplicate card legality, scoring, draw behavior, or turn effects.

## Secondary Extension: Smarter Bot Strategy

The design also supports a smarter bot strategy.

The main change would be in:

* `game.BotStrategy`

The existing controller already asks the game for the current bot choice through `UnoGame.chooseCurrentBotCard()`. A future strategy could consider hand size, opponent hand size, called color, or action-card timing without changing CLI rendering.

This is easier than before because bot priority no longer duplicates the full legal-play condition in three separate loops. Bot choice now uses centralized rule checks.

## What Still Makes Change Difficult

Some parts are intentionally not fully generalized yet:

* Card values still cross several boundaries as compact strings like `R5`, `G+2`, and `W4`.
* `UnoGame` is still a broad facade that exposes the game operations needed by the controller.
* UI messages are still specific to the current text-oriented game flow.
* The project preserves custom characterization helpers behind a small JUnit bridge rather than rewriting every check as an individual test.

These are acceptable remaining limits for this midterm refactor because the main goal was safe behavior-preserving separation, not a complete rewrite. The next clean extension pass would likely introduce typed card values in more public APIs and narrower game use-case methods.

## Verification Support

The extension points are protected by characterization tests:

* CLI formatting is covered by `CliCharacterizationTests`.
* prompt behavior is covered by `PlayerPromptCharacterizationTests`.
* game rules and state behavior are covered by `GameCharacterizationTests`.
* complete startup-to-final-score behavior is covered by `IntegrationCharacterizationTests`.

Any future UI or bot work should keep these tests green, then add new characterization checks around the new behavior.
