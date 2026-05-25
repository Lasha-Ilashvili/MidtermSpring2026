package controller;

import java.util.List;
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
        passed += SelfTestSupport.check(
                askHumanForSelfTest(SelfTestSupport.cards("R9"), "R5", "draw\n") == -1,
                "human can draw while holding legal card");

        passed += SelfTestSupport.check(
                askHumanForSelfTest(SelfTestSupport.cards("B3", "R9"), "R5", "0\n") == 0,
                "human index input bypasses legality check");
        passed += SelfTestSupport.check(
                !game.isLegalForCurrentState("B3"),
                "indexed illegal card remains illegal later");

        passed += SelfTestSupport.check(
                askHumanForSelfTest(SelfTestSupport.cards("B3", "R9"), "R5", "B3\nR9\n") == 1,
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

    private int askHumanForSelfTest(List<String> hand, String upCard, String input) {
        return output.capture(() -> {
            game.setupCurrentHumanTurn(upCard, hand);
            return view.withInput(input, controller::askHuman);
        });
    }

    private String askColorForSelfTest() {
        return output.capture(() -> view.withInput("x\nb\n", controller::askColor));
    }
}
