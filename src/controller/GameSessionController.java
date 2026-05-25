package controller;

import model.UnoGame;
import ui.UiView;

final class GameSessionController {

    private final UnoGame game;
    private final UiView view;
    private final TurnController turnController;

    GameSessionController(UnoGame game, UiView view, TurnController turnController) {
        this.game = game;
        this.view = view;
        this.turnController = turnController;
    }

    void setupGame(GameSettings settings) {
        game.seedRandom(settings.seed());
        game.setupPlayers(settings.bots(), settings.human());
    }

    boolean isPlayerCountValid() {
        return game.isPlayerCountValid();
    }

    void playGames(int games) {
        for (int gameCount = 1; gameCount <= games; gameCount++) {
            view.showGameHeader(gameCount);
            playGame();
        }
    }

    void playGame() {
        game.startRound();

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
        view.showFinalScores(game.playerNamesSnapshot(), game.scoresSnapshot());
    }
}
