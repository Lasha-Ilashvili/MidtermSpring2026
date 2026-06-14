package controller;

import game.UnoGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.UiView;

final class TurnController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TurnController.class);

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

        LOGGER.info("event=player_turn player={}", name);
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
        LOGGER.info("event=card_drawn player={} reason=turn card={}", playerName, drawn);
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
        LOGGER.info("event=card_played player={} card={}", playerName, card);
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
            LOGGER.info("event=invalid_input player={} reason=index_out_of_range index={}", playerName, chosen);
            LOGGER.info("event=card_drawn player={} reason=invalid_input count=1", playerName);
            view.showInvalidIndexPenalty(playerName);
            game.drawPenaltyAndAdvanceCurrentPlayer();
            return true;
        }

        String card = game.currentHandCard(chosen);
        if (!game.isLegalForCurrentState(card)) {
            LOGGER.info("event=invalid_input player={} reason=illegal_indexed_card card={}", playerName, card);
            LOGGER.info("event=card_drawn player={} reason=invalid_input count=1", playerName);
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
        LOGGER.info("event=round_end winner={} points={}", playerName, points);
        view.showWinnerScore(playerName, points);
        return true;
    }

    void showTurnEffect(UnoGame.TurnEffect effect) {
        switch (effect.type()) {
            case DRAW_TWO -> {
                LOGGER.info("event=card_drawn player={} reason=draw_two count=2", effect.playerName());
                view.showDrawTwoPenalty(effect.playerName());
            }
            case DRAW_FOUR -> {
                LOGGER.info("event=card_drawn player={} reason=wild_draw_four count=4", effect.playerName());
                view.showDrawFourPenalty(effect.playerName());
            }
            case NONE -> {
            }
        }
    }
}
