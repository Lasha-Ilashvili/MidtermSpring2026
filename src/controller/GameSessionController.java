package controller;

import model.Model;
import ui.UiView;

final class GameSessionController {

    private final Model model;
    private final UiView view;
    private final TurnController turnController;

    GameSessionController(Model model, UiView view, TurnController turnController) {
        this.model = model;
        this.view = view;
        this.turnController = turnController;
    }

    void setupGame(GameSettings settings) {
        model.seedRandom(settings.seed());
        model.setupPlayers(settings.bots(), settings.human());
    }

    boolean isPlayerCountValid() {
        return model.isPlayerCountValid();
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

    void showFinalScores() {
        view.showFinalScores(model.playerNamesSnapshot(), model.scoresSnapshot());
    }
}
