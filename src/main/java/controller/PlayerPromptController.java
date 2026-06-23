package controller;

import game.UnoGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.PlayerInput;
import ui.UiView;

final class PlayerPromptController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerPromptController.class);

    private final UnoGame game;
    private final UiView view;

    PlayerPromptController(UnoGame game, UiView view) {
        this.game = game;
        this.view = view;
    }

    TurnSelection askHumanSelection() {
        while (true) {
            view.showChooseCardPrompt();
            PlayerInput.CardChoice choice = view.readCardChoice();
            if (choice.type() == PlayerInput.CardChoiceType.DRAW) {
                return TurnSelection.draw();
            }

            if (choice.type() == PlayerInput.CardChoiceType.INDEX) {
                if (game.isCurrentHandIndex(choice.index())) {
                    return new TurnSelection(choice.index(), choice.unoCalled());
                }
                LOGGER.info("event=invalid_input reason=card_not_found input_type=index index={}", choice.index());
            } else {
                UnoGame.CardCodeChoice cardChoice = game.chooseCurrentCardByCode(choice.cardCode());
                if (cardChoice.hasLegalMatch()) {
                    return new TurnSelection(cardChoice.index(), choice.unoCalled());
                }
                if (cardChoice.illegalMatchCount() > 0) {
                    LOGGER.info("event=invalid_input reason=illegal_card_code");
                } else {
                    LOGGER.info("event=invalid_input reason=card_not_found input_type=card_code");
                }
                for (int i = 0; i < cardChoice.illegalMatchCount(); i++) {
                    view.showCardNotLegal();
                }
            }
            view.showCardNotFound();
        }
    }

    int askHuman() {
        return askHumanSelection().index();
    }

    String askColor() {
        while (true) {
            view.showCallColorPrompt();
            String input = view.readColorInput();
            if (game.isPlayableColor(input)) {
                return input;
            }
            LOGGER.info("event=invalid_input reason=invalid_color");
            view.showBadColor();
        }
    }
}
