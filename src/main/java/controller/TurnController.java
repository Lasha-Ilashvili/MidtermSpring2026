package controller;

import game.UnoGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.UiView;

import java.util.Optional;

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

    Optional<RoundOutcome> playCurrentTurn() {
        String name = game.currentPlayerName();

        LOGGER.info("event=player_turn player={}", name);
        view.showTurn(name, game.currentHandSnapshot(), game.upCard(), game.calledColor());

        TurnSelection selection = chooseCardForCurrentPlayer();
        selection = chooseDrawnCardIfNeeded(selection, name);

        return finishTurn(selection, name);
    }

    TurnSelection chooseCardForCurrentPlayer() {
        if (game.isHumanCurrentPlayer()) {
            return playerPromptController.askHumanSelection();
        }
        return new TurnSelection(game.chooseCurrentBotCard(), true);
    }

    TurnSelection chooseDrawnCardIfNeeded(TurnSelection selection, String playerName) {
        if (selection.index() != -1) {
            return selection;
        }

        String drawn = game.drawForCurrentPlayer();
        LOGGER.info("event=card_drawn player={} reason=turn card={}", playerName, drawn);
        view.showCardDrawn(playerName, drawn);

        if (game.shouldCurrentPlayerAutoPlayDrawnCard(drawn)) {
            return new TurnSelection(game.lastCurrentHandIndex(), true);
        }
        if (game.shouldAskCurrentPlayerToPlayDrawnCard(drawn)) {
            view.showPlayDrawnCardPrompt(drawn);
            var decision = view.readPlayDrawnCardChoice();
            if (decision.play()) {
                return new TurnSelection(game.lastCurrentHandIndex(), decision.unoCalled());
            }
        }
        return selection;
    }

    Optional<RoundOutcome> finishTurn(TurnSelection selection, String playerName) {
        int chosen = selection.index();
        if (chosen < 0) {
            game.advanceToNextPlayer();
            return Optional.empty();
        }

        if (penalizeInvalidSelection(chosen, playerName)) {
            return Optional.empty();
        }

        String card = game.currentHandCard(chosen);
        game.playCardFromCurrentHand(chosen);
        LOGGER.info("event=card_played player={} card={}", playerName, card);
        view.showCardPlayed(playerName, card);

        callColorIfNeeded(card, playerName);
        applyUnoIfNeeded(playerName, selection.unoCalled());

        Optional<RoundOutcome> outcome = scoreRoundIfFinished(playerName);
        if (outcome.isPresent()) {
            return outcome;
        }

        showTurnEffect(game.applyCardEffect(card));
        return Optional.empty();
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

    void applyUnoIfNeeded(String playerName, boolean unoCalled) {
        if (game.shouldCurrentPlayerReceiveMissedUnoPenalty(unoCalled)) {
            LOGGER.info("event=card_drawn player={} reason=missed_uno count=2", playerName);
            view.showMissedUnoPenalty(playerName);
            game.drawMissedUnoPenaltyForCurrentPlayer();
            return;
        }
        if (game.shouldShowUnoForCurrentPlayer(unoCalled)) {
            view.showUno(playerName);
        }
    }

    Optional<RoundOutcome> scoreRoundIfFinished(String playerName) {
        if (!game.currentPlayerHasNoCards()) {
            return Optional.empty();
        }

        int points = game.scoreCurrentPlayerFromOpponents();
        LOGGER.info("event=round_end winner={} points={}", playerName, points);
        view.showWinnerScore(playerName, points);
        return Optional.of(RoundOutcome.completed(playerName, points));
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
