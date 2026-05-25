package controller;

import model.Model;
import ui.PlayerInput;
import ui.Startup;
import ui.UiType;
import ui.UiView;
import ui.UiViewFactory;

public class Controller {

    private final Model model;
    private final UiView view;

    Controller(Model model, UiView view) {
        this.model = model;
        this.view = view;
    }

    public static void startNewGame(UiType uiType) {
        new Controller(new Model(), UiViewFactory.create(uiType)).runNewGame(uiType);
    }

    void runNewGame(UiType uiType) {
        Startup.Input startupInput = view.readStartupInput(uiType);
        view.setQuiet(startupInput.quiet());

        if (handleStartupAction(startupInput.action())) {
            return;
        }

        GameSettings settings = startupSettings(startupInput);
        setupGame(settings);

        if (!model.isPlayerCountValid()) {
            view.showInvalidPlayerCount();
            return;
        }

        playGames(settings.games());
        view.showFinalScores(model.playerNamesSnapshot(), model.scoresSnapshot());
    }

    boolean handleStartupAction(Startup.Action action) {
        if (action == Startup.Action.SELF_TEST) {
            CharacterizationTests.run();
            return true;
        } else if (action == Startup.Action.HELP) {
            view.showUsage();
            return true;
        } else if (action == Startup.Action.UNSUPPORTED_UI) {
            view.showUnsupportedUiType();
            return true;
        }
        return false;
    }

    GameSettings startupSettings(Startup.Input startupInput) {
        return GameSettings.from(startupInput);
    }

    void setupGame(GameSettings settings) {
        model.seedRandom(settings.seed());
        model.setupPlayers(settings.bots(), settings.human());
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
            String name = model.currentPlayerName();

            view.showTurn(name, model.currentHandSnapshot(), model.upCard(), model.calledColor());

            int chosen = chooseCardForCurrentPlayer();
            chosen = chooseDrawnCardIfNeeded(chosen, name);

            if (finishTurn(chosen, name)) {
                return;
            }
        }

        view.showSafetyLimitReached();
    }

    int chooseCardForCurrentPlayer() {
        if (model.isHumanCurrentPlayer()) {
            return askHuman();
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
            model.setCalledColor(askColor());
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
                Model.CardCodeChoice cardChoice = model.chooseCurrentCardByCode(choice.cardCode());
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
