package controller;

import model.Model;
import ui.Startup;
import ui.UiType;
import ui.UiView;
import ui.UiViewFactory;

public class Controller {

    private final Model model;
    private final UiView view;
    private final StartupActionHandler startupActionHandler;
    private final PlayerPromptController playerPromptController;
    private final TurnController turnController;

    Controller(Model model, UiView view) {
        this.model = model;
        this.view = view;
        this.startupActionHandler = new StartupActionHandler(view);
        this.playerPromptController = new PlayerPromptController(model, view);
        this.turnController = new TurnController(model, view, playerPromptController);
    }

    public static void startNewGame(UiType uiType) {
        new Controller(new Model(), UiViewFactory.create(uiType)).runNewGame(uiType);
    }

    void runNewGame(UiType uiType) {
        Startup.Input startupInput = view.readStartupInput(uiType);
        view.setQuiet(startupInput.quiet());

        if (handleStartupAction(startupInput.action())) {
            return;
        }

        GameSettings settings = startupSettings(startupInput);
        setupGame(settings);

        if (!model.isPlayerCountValid()) {
            view.showInvalidPlayerCount();
            return;
        }

        playGames(settings.games());
        view.showFinalScores(model.playerNamesSnapshot(), model.scoresSnapshot());
    }

    boolean handleStartupAction(Startup.Action action) {
        return startupActionHandler.handle(action);
    }

    GameSettings startupSettings(Startup.Input startupInput) {
        return GameSettings.from(startupInput);
    }

    void setupGame(GameSettings settings) {
        model.seedRandom(settings.seed());
        model.setupPlayers(settings.bots(), settings.human());
    }

    void playGames(int games) {
        for (int gameCount = 1; gameCount <= games; gameCount++) {
            view.showGameHeader(gameCount);
            playGame();
        }
    }

    void playGame() {
        model.startRound();

        int guard = 0;
        while (guard < 3000) {
            guard++;
            if (turnController.playCurrentTurn()) {
                return;
            }
        }

        view.showSafetyLimitReached();
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

    void showTurnEffect(Model.TurnEffect effect) {
        turnController.showTurnEffect(effect);
    }

    int askHuman() {
        return playerPromptController.askHuman();
    }

    String askColor() {
        return playerPromptController.askColor();
    }
}
