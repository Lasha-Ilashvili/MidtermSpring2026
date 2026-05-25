package controller;

import model.UnoGame;
import ui.Startup;
import ui.UiType;
import ui.UiView;
import ui.UiViewFactory;

public class Controller {

    private final UiView view;
    private final StartupActionHandler startupActionHandler;
    private final PlayerPromptController playerPromptController;
    private final TurnController turnController;
    private final GameSessionController gameSessionController;

    Controller(UnoGame model, UiView view) {
        this.view = view;
        this.startupActionHandler = new StartupActionHandler(view);
        this.playerPromptController = new PlayerPromptController(model, view);
        this.turnController = new TurnController(model, view, playerPromptController);
        this.gameSessionController = new GameSessionController(model, view, turnController);
    }

    public static void startNewGame(UiType uiType) {
        new Controller(new UnoGame(), UiViewFactory.create(uiType)).runNewGame(uiType);
    }

    void runNewGame(UiType uiType) {
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

    boolean handleStartupAction(Startup.Action action) {
        return startupActionHandler.handle(action);
    }

    GameSettings startupSettings(Startup.Input startupInput) {
        return GameSettings.from(startupInput);
    }

    void setupGame(GameSettings settings) {
        gameSessionController.setupGame(settings);
    }

    void playGames(int games) {
        gameSessionController.playGames(games);
    }

    void playGame() {
        gameSessionController.playGame();
    }

    int chooseCardForCurrentPlayer() {
        return turnController.chooseCardForCurrentPlayer();
    }

    int chooseDrawnCardIfNeeded(int chosen, String playerName) {
        return turnController.chooseDrawnCardIfNeeded(chosen, playerName);
    }

    boolean finishTurn(int chosen, String playerName) {
        return turnController.finishTurn(chosen, playerName);
    }

    boolean penalizeInvalidSelection(int chosen, String playerName) {
        return turnController.penalizeInvalidSelection(chosen, playerName);
    }

    void callColorIfNeeded(String card, String playerName) {
        turnController.callColorIfNeeded(card, playerName);
    }

    void showUnoIfNeeded(String playerName) {
        turnController.showUnoIfNeeded(playerName);
    }

    boolean scoreRoundIfFinished(String playerName) {
        return turnController.scoreRoundIfFinished(playerName);
    }

    void showTurnEffect(UnoGame.TurnEffect effect) {
        turnController.showTurnEffect(effect);
    }

    int askHuman() {
        return playerPromptController.askHuman();
    }

    String askColor() {
        return playerPromptController.askColor();
    }
}
