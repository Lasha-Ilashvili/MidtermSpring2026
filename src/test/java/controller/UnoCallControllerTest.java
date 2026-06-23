package controller;

import game.UnoGame;
import org.junit.jupiter.api.Test;
import ui.PlayerInput;
import ui.cli.CliView;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UnoCallControllerTest {

    @Test
    void cliAcceptsUnoSuffixOnCardAndDrawnCardChoices() {
        CliView view = new CliView();

        PlayerInput.CardChoice indexChoice = view.withInput("3 uno\n", view::readCardChoice);
        PlayerInput.CardChoice codeChoice = view.withInput("R5 uno\n", view::readCardChoice);
        PlayerInput.DrawnCardDecision drawnChoice = view.withInput("yes uno\n", view::readPlayDrawnCardChoice);

        assertAll(
                () -> assertEquals(PlayerInput.CardChoiceType.INDEX, indexChoice.type()),
                () -> assertEquals(3, indexChoice.index()),
                () -> assertTrue(indexChoice.unoCalled()),
                () -> assertEquals(PlayerInput.CardChoiceType.CARD_CODE, codeChoice.type()),
                () -> assertEquals("R5", codeChoice.cardCode()),
                () -> assertTrue(codeChoice.unoCalled()),
                () -> assertTrue(drawnChoice.play()),
                () -> assertTrue(drawnChoice.unoCalled())
        );
    }

    @Test
    void humanWhoMissesUnoReceivesVisiblePenalty() {
        UnoGame game = humanTurnWithTwoCards();
        CliView view = new CliView();
        TurnController turnController = turnController(game, view);
        OutputCapture output = new OutputCapture();

        output.capture(() -> view.withInput("0\n", () -> {
            turnController.playCurrentTurn();
            return null;
        }));

        assertTrue(output.contains("You forgot UNO and draws two."));
        assertFalse(output.contains("You says UNO!"));
    }

    @Test
    void humanWhoCallsUnoAvoidsMissedUnoPenalty() {
        UnoGame game = humanTurnWithTwoCards();
        CliView view = new CliView();
        TurnController turnController = turnController(game, view);
        OutputCapture output = new OutputCapture();

        output.capture(() -> view.withInput("0 uno\n", () -> {
            turnController.playCurrentTurn();
            return null;
        }));

        assertTrue(output.contains("You says UNO!"));
        assertFalse(output.contains("forgot UNO"));
    }

    private UnoGame humanTurnWithTwoCards() {
        UnoGame game = new UnoGame();
        game.setupCurrentHumanTurn("R5", SelfTestSupport.cards("R9", "B2"));
        return game;
    }

    private TurnController turnController(UnoGame game, CliView view) {
        PlayerPromptController promptController = new PlayerPromptController(game, view);
        return new TurnController(game, view, promptController);
    }
}
