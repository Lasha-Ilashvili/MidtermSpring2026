package controller;

import java.util.ArrayList;
import game.UnoGame;
import ui.cli.CliView;

final class PlayerPromptCharacterizationTests {

    private final UnoGame game = new UnoGame();
    private final CliView view = new CliView();
    private final UnoGameController controller = new UnoGameController(game, view);
    private final OutputCapture output = new OutputCapture();

    int run() {
        int passed = 0;
        passed += humanInputQuirkTests();
        passed += colorPromptTests();
        return passed;
    }

    private int humanInputQuirkTests() {
        int passed = 0;
        game.setUpCard("R5");
        game.clearCalledColor();
        passed += SelfTestSupport.check(
                askHumanForSelfTest(SelfTestSupport.cards("R9"), "draw\n") == -1,
                "human can draw while holding legal card");

        game.setUpCard("R5");
        game.clearCalledColor();
        passed += SelfTestSupport.check(
                askHumanForSelfTest(SelfTestSupport.cards("B3", "R9"), "0\n") == 0,
                "human index input bypasses legality check");
        passed += SelfTestSupport.check(
                !game.isLegalForCurrentState("B3"),
                "indexed illegal card remains illegal later");

        game.setUpCard("R5");
        game.clearCalledColor();
        passed += SelfTestSupport.check(
                askHumanForSelfTest(SelfTestSupport.cards("B3", "R9"), "B3\nR9\n") == 1,
                "human card code rejects illegal card");
        passed += SelfTestSupport.check(
                output.contains("That card is not legal."),
                "illegal code prints not legal message");
        passed += SelfTestSupport.check(
                output.contains("Card not found."),
                "illegal code also prints card not found quirk");
        return passed;
    }

    private int colorPromptTests() {
        int passed = 0;
        passed += SelfTestSupport.check(
                askColorForSelfTest().equals("B"),
                "human color prompt accepts valid color after bad input");
        passed += SelfTestSupport.check(output.contains("Bad color."), "bad color message");
        return passed;
    }

    private int askHumanForSelfTest(ArrayList<String> hand, String input) {
        return output.capture(() -> {
            game.setupPlayers(1, true);
            game.setCurrentPlayer(0);
            game.addCardsToCurrentHand(hand);
            return view.withInput(input, controller::askHuman);
        });
    }

    private String askColorForSelfTest() {
        return output.capture(() -> view.withInput("x\nb\n", controller::askColor));
    }
}
