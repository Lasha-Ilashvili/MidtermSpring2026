package controller;

import game.UnoGame;
import history.CompletedGame;
import history.GameHistoryWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.Startup;
import ui.UiType;
import ui.UiView;
import ui.UiViewFactory;

import java.time.Clock;

public class UnoGameController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnoGameController.class);

    private final UiView view;
    private final GameHistoryWriter gameHistoryWriter;
    private final StartupActionHandler startupActionHandler;
    private final PlayerPromptController playerPromptController;
    private final TurnController turnController;
    private final GameSessionController gameSessionController;

    UnoGameController(UnoGame game, UiView view) {
        this(game, view, GameHistoryWriter.noOp(), Clock.systemUTC());
    }

    UnoGameController(UnoGame game, UiView view, GameHistoryWriter gameHistoryWriter, Clock clock) {
        this.view = view;
        this.gameHistoryWriter = gameHistoryWriter;
        this.startupActionHandler = new StartupActionHandler(view);
        this.playerPromptController = new PlayerPromptController(game, view);
        this.turnController = new TurnController(game, view, playerPromptController);
        this.gameSessionController = new GameSessionController(game, view, turnController, clock);
    }

    public static void startNewGame(UiType uiType) {
        startNewGame(uiType, GameHistoryWriter.noOp());
    }

    public static void startNewGame(UiType uiType, GameHistoryWriter gameHistoryWriter) {
        new UnoGameController(
                new UnoGame(),
                UiViewFactory.create(uiType),
                gameHistoryWriter,
                Clock.systemUTC()
        ).runNewGame(uiType);
    }

    private void runNewGame(UiType uiType) {
        Startup.Input startupInput = view.readStartupInput(uiType);
        view.setQuiet(startupInput.quiet());

        if (handleStartupAction(startupInput.action())) {
            return;
        }

        GameSettings settings = startupSettings(startupInput);
        setupGame(settings);

        if (!gameSessionController.isPlayerCountValid()) {
            view.showInvalidPlayerCount();
            return;
        }

        CompletedGame completedGame = playGames(settings.games());
        gameSessionController.showFinalScores();
        saveGameHistory(completedGame);
    }

    private boolean handleStartupAction(Startup.Action action) {
        return startupActionHandler.handle(action);
    }

    private GameSettings startupSettings(Startup.Input startupInput) {
        return GameSettings.from(startupInput);
    }

    private void setupGame(GameSettings settings) {
        gameSessionController.setupGame(settings);
    }

    private CompletedGame playGames(int games) {
        return gameSessionController.playGames(games);
    }

    private void saveGameHistory(CompletedGame completedGame) {
        try {
            gameHistoryWriter.save(completedGame);
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "event=history_save_failed reason={}",
                    exception.getClass().getSimpleName()
            );
        }
    }

    int askHuman() {
        return playerPromptController.askHuman();
    }

    String askColor() {
        return playerPromptController.askColor();
    }
}
