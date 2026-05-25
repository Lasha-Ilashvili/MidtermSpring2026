package controller;

import game.UnoGame;
import ui.UiView;

final class TurnController {

    private final UnoGame game;
    private final UiView view;
    private final PlayerPromptController playerPromptController;

    TurnController(UnoGame game, UiView view, PlayerPromptController playerPromptController) {
        this.game = game;
        this.view = view;
        this.playerPromptController = playerPromptController;
    }

    boolean playCurrentTurn() {
        String name = game.currentPlayerName();

        view.showTurn(name, game.currentHandSnapshot(), game.upCard(), game.calledColor());

        int chosen = chooseCardForCurrentPlayer();
        chosen = chooseDrawnCardIfNeeded(chosen, name);

        return finishTurn(chosen, name);
    }

    int chooseCardForCurrentPlayer() {
        if (game.isHumanCurrentPlayer()) {
            return playerPromptController.askHuman();
        }
        return game.chooseCurrentBotCard();
    }

    int chooseDrawnCardIfNeeded(int chosen, String playerName) {
        if (chosen != -1) {
            return chosen;
        }

        String drawn = game.drawForCurrentPlayer();
        view.showCardDrawn(playerName, drawn);

        if (game.shouldCurrentPlayerAutoPlayDrawnCard(drawn)) {
            return game.lastCurrentHandIndex();
        }
        if (game.shouldAskCurrentPlayerToPlayDrawnCard(drawn)) {
            view.showPlayDrawnCardPrompt(drawn);
            if (view.readPlayDrawnCardDecision()) {
                return game.lastCurrentHandIndex();
            }
        }
        return chosen;
    }

    boolean finishTurn(int chosen, String playerName) {
        if (chosen < 0) {
            game.advanceToNextPlayer();
            return false;
        }

        if (penalizeInvalidSelection(chosen, playerName)) {
            return false;
        }

        String card = game.currentHandCard(chosen);
        game.playCardFromCurrentHand(chosen);
        view.showCardPlayed(playerName, card);

        callColorIfNeeded(card, playerName);
        showUnoIfNeeded(playerName);

        if (scoreRoundIfFinished(playerName)) {
            return true;
        }

        showTurnEffect(game.applyCardEffect(card));
        return false;
    }

    boolean penalizeInvalidSelection(int chosen, String playerName) {
        if (game.isOutsideCurrentHand(chosen)) {
            view.showInvalidIndexPenalty(playerName);
            game.drawPenaltyAndAdvanceCurrentPlayer();
            return true;
        }

        String card = game.currentHandCard(chosen);
        if (!game.isLegalForCurrentState(card)) {
            view.showIllegalCardPenalty(playerName, card);
            game.drawPenaltyAndAdvanceCurrentPlayer();
            return true;
        }
        return false;
    }

    void callColorIfNeeded(String card, String playerName) {
        if (!game.isWildCard(card)) {
            return;
        }

        if (game.isHumanCurrentPlayer()) {
            game.setCalledColor(playerPromptController.askColor());
        } else {
            game.setCalledColor(game.chooseCurrentBotColor());
        }
        view.showColorCalled(playerName, game.calledColor());
    }

    void showUnoIfNeeded(String playerName) {
        if (game.currentPlayerHasOneCard()) {
            view.showUno(playerName);
        }
    }

    boolean scoreRoundIfFinished(String playerName) {
        if (!game.currentPlayerHasNoCards()) {
            return false;
        }

        int points = game.scoreCurrentPlayerFromOpponents();
        view.showWinnerScore(playerName, points);
        return true;
    }

    void showTurnEffect(UnoGame.TurnEffect effect) {
        switch (effect.type()) {
            case DRAW_TWO -> view.showDrawTwoPenalty(effect.playerName());
            case DRAW_FOUR -> view.showDrawFourPenalty(effect.playerName());
            case NONE -> {
            }
        }
    }
}
