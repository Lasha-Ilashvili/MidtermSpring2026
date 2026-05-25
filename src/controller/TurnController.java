package controller;

import model.Model;
import ui.UiView;

final class TurnController {

    private final Model model;
    private final UiView view;
    private final PlayerPromptController playerPromptController;

    TurnController(Model model, UiView view, PlayerPromptController playerPromptController) {
        this.model = model;
        this.view = view;
        this.playerPromptController = playerPromptController;
    }

    boolean playCurrentTurn() {
        String name = model.currentPlayerName();

        view.showTurn(name, model.currentHandSnapshot(), model.upCard(), model.calledColor());

        int chosen = chooseCardForCurrentPlayer();
        chosen = chooseDrawnCardIfNeeded(chosen, name);

        return finishTurn(chosen, name);
    }

    int chooseCardForCurrentPlayer() {
        if (model.isHumanCurrentPlayer()) {
            return playerPromptController.askHuman();
        }
        return model.chooseCurrentBotCard();
    }

    int chooseDrawnCardIfNeeded(int chosen, String playerName) {
        if (chosen != -1) {
            return chosen;
        }

        String drawn = model.drawForCurrentPlayer();
        view.showCardDrawn(playerName, drawn);

        if (model.shouldCurrentPlayerAutoPlayDrawnCard(drawn)) {
            return model.lastCurrentHandIndex();
        }
        if (model.shouldAskCurrentPlayerToPlayDrawnCard(drawn)) {
            view.showPlayDrawnCardPrompt(drawn);
            if (view.readPlayDrawnCardDecision()) {
                return model.lastCurrentHandIndex();
            }
        }
        return chosen;
    }

    boolean finishTurn(int chosen, String playerName) {
        if (chosen < 0) {
            model.advanceToNextPlayer();
            return false;
        }

        if (penalizeInvalidSelection(chosen, playerName)) {
            return false;
        }

        String card = model.currentHandCard(chosen);
        model.playCardFromCurrentHand(chosen);
        view.showCardPlayed(playerName, card);

        callColorIfNeeded(card, playerName);
        showUnoIfNeeded(playerName);

        if (scoreRoundIfFinished(playerName)) {
            return true;
        }

        showTurnEffect(model.applyCardEffect(card));
        return false;
    }

    boolean penalizeInvalidSelection(int chosen, String playerName) {
        if (model.isOutsideCurrentHand(chosen)) {
            view.showInvalidIndexPenalty(playerName);
            model.drawPenaltyAndAdvanceCurrentPlayer();
            return true;
        }

        String card = model.currentHandCard(chosen);
        if (!model.isLegalForCurrentState(card)) {
            view.showIllegalCardPenalty(playerName, card);
            model.drawPenaltyAndAdvanceCurrentPlayer();
            return true;
        }
        return false;
    }

    void callColorIfNeeded(String card, String playerName) {
        if (!model.isWildCard(card)) {
            return;
        }

        if (model.isHumanCurrentPlayer()) {
            model.setCalledColor(playerPromptController.askColor());
        } else {
            model.setCalledColor(model.chooseCurrentBotColor());
        }
        view.showColorCalled(playerName, model.calledColor());
    }

    void showUnoIfNeeded(String playerName) {
        if (model.currentPlayerHasOneCard()) {
            view.showUno(playerName);
        }
    }

    boolean scoreRoundIfFinished(String playerName) {
        if (!model.currentPlayerHasNoCards()) {
            return false;
        }

        int points = model.scoreCurrentPlayerFromOpponents();
        view.showWinnerScore(playerName, points);
        return true;
    }

    void showTurnEffect(Model.TurnEffect effect) {
        if (effect.type() == Model.EffectType.DRAW_TWO) {
            view.showDrawTwoPenalty(effect.playerName());
        } else if (effect.type() == Model.EffectType.DRAW_FOUR) {
            view.showDrawFourPenalty(effect.playerName());
        }
    }
}
