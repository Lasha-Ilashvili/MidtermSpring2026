package controller;

import game.UnoGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.UiView;

final class GameSessionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameSessionController.class);

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
            LOGGER.info("event=game_start game={}", gameCount);
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

        LOGGER.info("event=round_end result=safety_limit");
        view.showSafetyLimitReached();
    }

    void showFinalScores() {
        var playerNames = game.playerNamesSnapshot();
        LOGGER.info("event=session_end players={}", playerNames.size());
        view.showFinalScores(playerNames, game.scoresSnapshot());
    }
}
