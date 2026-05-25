package controller;

import model.UnoGame;
import ui.PlayerInput;
import ui.UiView;

final class PlayerPromptController {

    private final UnoGame model;
    private final UiView view;

    PlayerPromptController(UnoGame model, UiView view) {
        this.model = model;
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
                if (model.isCurrentHandIndex(choice.index())) {
                    return choice.index();
                }
            } else {
                UnoGame.CardCodeChoice cardChoice = model.chooseCurrentCardByCode(choice.cardCode());
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
            if (model.isPlayableColor(input)) {
                return input;
            }
            view.showBadColor();
        }
    }
}
