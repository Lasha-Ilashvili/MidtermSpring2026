package controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;
import model.UnoGame;
import ui.cli.CliView;

final class CharacterizationTests {

    private final UnoGame model = new UnoGame();
    private final CliView view = new CliView();
    private final UnoGameController controller = new UnoGameController(model, view);
    private String selfTestCapturedOutput = "";

    static void run() {
        new CharacterizationTests().runAll();
    }

    private void runAll() {
        int passed = 0;
        passed += modelTests();
        passed += viewTests();
        passed += controllerTests();
        passed += integrationTests();

        System.out.println("Passed " + passed + " characterization checks.");
    }

    private int modelTests() {
        return new ModelCharacterizationTests().run();
    }

    private int viewTests() {
        return consoleBehaviorTests();
    }

    private int controllerTests() {
        return humanInputQuirkTests();
    }

    private int integrationTests() {
        return seededBotGameTests();
    }

    private int humanInputQuirkTests() {
        int passed = 0;
        model.setUpCard("R5");
        model.clearCalledColor();
        passed += check(askHumanForSelfTest(cards("R9"), "draw\n") == -1, "human can draw while holding legal card");

        model.setUpCard("R5");
        model.clearCalledColor();
        passed += check(askHumanForSelfTest(cards("B3", "R9"), "0\n") == 0, "human index input bypasses legality check");
        passed += check(!model.isLegalForCurrentState("B3"), "indexed illegal card remains illegal later");

        model.setUpCard("R5");
        model.clearCalledColor();
        passed += check(askHumanForSelfTest(cards("B3", "R9"), "B3\nR9\n") == 1, "human card code rejects illegal card");
        passed += check(selfTestCapturedOutput.contains("That card is not legal."), "illegal code prints not legal message");
        passed += check(selfTestCapturedOutput.contains("Card not found."), "illegal code also prints card not found quirk");
        return passed;
    }

    private int consoleBehaviorTests() {
        int passed = 0;
        passed += check(view.join(cards("R5", "W", "B+2")).equals("0:R5 1:W 2:B+2"), "hand display includes indexes");
        passed += check(askColorForSelfTest().equals("B"), "human color prompt accepts valid color after bad input");
        passed += check(selfTestCapturedOutput.contains("Bad color."), "bad color message");
        return passed;
    }

    private int seededBotGameTests() {
        int passed = 0;
        view.setQuiet(true);
        model.seedRandom(123);
        model.setupPlayers(3, false);
        model.clearScores();
        for (int game = 0; game < 5; game++) {
            controller.playGame();
        }
        passed += check(model.score(0) == 138, "seeded bot games score Bot1");
        passed += check(model.score(1) == 246, "seeded bot games score Bot2");
        passed += check(model.score(2) == 98, "seeded bot games score Bot3");
        view.setQuiet(false);
        return passed;
    }

    private static int check(boolean condition, String name) {
        if (!condition) {
            fail(name);
        }
        return 1;
    }

    private int askHumanForSelfTest(ArrayList<String> hand, String input) {
        return withCapturedOutput(() -> {
            model.setupPlayers(1, true);
            model.setCurrentPlayer(0);
            model.currentHand().addAll(hand);
            return view.withInput(input, controller::askHuman);
        });
    }

    private String askColorForSelfTest() {
        return withCapturedOutput(() -> view.withInput("x\nb\n", controller::askColor));
    }

    private <T> T withCapturedOutput(Supplier<T> action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        try {
            return action.get();
        } finally {
            System.setOut(originalOut);
            selfTestCapturedOutput = output.toString(StandardCharsets.UTF_8);
        }
    }

    private static ArrayList<String> cards(String... values) {
        ArrayList<String> result = new ArrayList<>();
        Collections.addAll(result, values);
        return result;
    }

    private static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
