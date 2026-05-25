package controller;

import game.UnoGame;
import ui.Startup;
import ui.UiType;
import ui.UiView;
import ui.UiViewFactory;

public class UnoGameController {

    private final UiView view;
    private final StartupActionHandler startupActionHandler;
    private final PlayerPromptController playerPromptController;
    private final TurnController turnController;
    private final GameSessionController gameSessionController;

    UnoGameController(UnoGame game, UiView view) {
        this.view = view;
        this.startupActionHandler = new StartupActionHandler(view);
        this.playerPromptController = new PlayerPromptController(game, view);
        this.turnController = new TurnController(game, view, playerPromptController);
        this.gameSessionController = new GameSessionController(game, view, turnController);
    }

    public static void startNewGame(UiType uiType) {
        new UnoGameController(new UnoGame(), UiViewFactory.create(uiType)).runNewGame(uiType);
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

        playGames(settings.games());
        gameSessionController.showFinalScores();
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

    private void playGames(int games) {
        gameSessionController.playGames(games);
    }

    int askHuman() {
        return playerPromptController.askHuman();
    }

    String askColor() {
        return playerPromptController.askColor();
    }
}
