package controller;

import game.UnoGame;
import ui.PlayerInput;
import ui.UiView;

final class PlayerPromptController {

    private final UnoGame game;
    private final UiView view;

    PlayerPromptController(UnoGame game, UiView view) {
        this.game = game;
        this.view = view;
    }

    int askHuman() {
        while (true) {
            view.showChooseCardPrompt();
            PlayerInput.CardChoice choice = view.readCardChoice();
            if (choice.type() == PlayerInput.CardChoiceType.DRAW) {
                return -1;
            }

            if (choice.type() == PlayerInput.CardChoiceType.INDEX) {
                if (game.isCurrentHandIndex(choice.index())) {
                    return choice.index();
                }
            } else {
                UnoGame.CardCodeChoice cardChoice = game.chooseCurrentCardByCode(choice.cardCode());
                if (cardChoice.hasLegalMatch()) {
                    return cardChoice.index();
                }
                for (int i = 0; i < cardChoice.illegalMatchCount(); i++) {
                    view.showCardNotLegal();
                }
            }
            view.showCardNotFound();
        }
    }

    String askColor() {
        while (true) {
            view.showCallColorPrompt();
            String input = view.readColorInput();
            if (game.isPlayableColor(input)) {
                return input;
            }
            view.showBadColor();
        }
    }
}
