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
        int passed = 0;
        passed += cardParsingTests();
        passed += legalPlayTests();
        passed += botChoiceTests();
        passed += drawPileTests();
        passed += drawnCardDecisionTests();
        passed += penaltyPathTests();
        passed += turnMovementTests();
        passed += scoringTests();
        return passed;
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

    private int cardParsingTests() {
        int passed = 0;
        passed += check(model.color("R5").equals("R"), "color R5");
        passed += check(model.color("YS").equals("Y"), "color YS");
        passed += check(model.color("G+2").equals("G"), "color G+2");
        passed += check(model.color("BR").equals("B"), "color BR");
        passed += check(model.color("W").isEmpty(), "wild has no printed color");
        passed += check(model.rank("W").equals("WILD"), "rank wild");
        passed += check(model.rank("W4").equals("WILD_DRAW_FOUR"), "rank wild draw four");
        passed += check(model.rank("RS").equals("SKIP"), "rank skip");
        passed += check(model.rank("BR").equals("REVERSE"), "rank reverse");
        passed += check(model.rank("G+2").equals("DRAW_TWO"), "rank +2");
        passed += check(model.rank("B7").equals("NUMBER"), "rank number");
        passed += check(model.number("R0") == 0, "number zero");
        passed += check(model.number("B9") == 9, "number nine");
        passed += check(model.number("W") == -1, "wild has no number");
        passed += check(model.number("R+2") == -1, "draw two has no number");
        return passed;
    }

    private int legalPlayTests() {
        int passed = 0;
        passed += check(model.isLegal("R2", "R9", ""), "same color");
        passed += check(model.isLegal("G9", "R9", ""), "same number");
        passed += check(model.isLegal("BS", "RS", ""), "same skip action");
        passed += check(model.isLegal("BR", "YR", ""), "same reverse action");
        passed += check(model.isLegal("R+2", "B+2", ""), "same draw two action");
        passed += check(model.isLegal("W", "B3", ""), "plain wild always legal");
        passed += check(model.isLegal("W4", "B3", ""), "wild draw four always legal");
        passed += check(model.isLegal("B3", "W", "B"), "called color after wild");
        passed += check(model.isLegal("B3", "R9", "B"), "called color can beat up card color");
        passed += check(!model.isLegal("B3", "R9", ""), "illegal mismatch");
        passed += check(!model.isLegal("B3", "R+2", ""), "number does not match action");
        return passed;
    }

    private int botChoiceTests() {
        int passed = 0;
        model.setUpCard("R9");
        model.clearCalledColor();
        passed += check(model.chooseBotCard(cards("B3", "R4", "W")) == 1, "bot number before wild");
        passed += check(model.chooseBotCard(cards("R4", "R+2", "W")) == 1, "bot draw two priority");

        model.setUpCard("G9");
        passed += check(model.chooseBotCard(cards("G3", "GS", "W")) == 1, "bot skip priority");

        model.setUpCard("YR");
        model.clearCalledColor();
        passed += check(model.chooseBotCard(cards("BR", "W")) == 1, "bot reverse quirk before wild");

        model.setUpCard("W");
        model.setCalledColor("G");
        passed += check(model.chooseBotCard(cards("R1", "G3")) == 1, "bot uses called color");

        model.setUpCard("R9");
        model.clearCalledColor();
        passed += check(model.chooseBotCard(cards("B1", "G2")) == -1, "bot draws when no legal card");
        passed += check(model.chooseBotColor(cards("B1", "B2", "R3")).equals("B"), "bot color majority");
        passed += check(model.chooseBotColor(cards("R1", "Y2", "G3", "B4")).equals("R"), "bot color tie defaults red");
        return passed;
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

    private int drawPileTests() {
        int passed = 0;
        model.seedRandom(7);
        model.clearDeck();
        model.clearDiscard();
        model.addToDeck("R1");
        model.addToDeck("B2");
        passed += check(model.draw().equals("R1"), "draw removes top deck card");
        passed += check(model.deckSize() == 1 && model.firstDeckCard().equals("B2"), "draw leaves remaining deck");

        model.clearDeck();
        model.clearDiscard();
        model.discard("G5");
        passed += check(model.draw().equals("G5"), "empty deck refills from discard");
        passed += check(model.isDiscardEmpty(), "discard cleared after refill");

        model.clearDeck();
        model.clearDiscard();
        passed += check(model.draw().equals("W"), "empty draw and discard fallback");
        return passed;
    }

    private int drawnCardDecisionTests() {
        int passed = 0;
        model.setupPlayers(2, false);
        model.setCurrentPlayer(0);
        model.setUpCard("R5");
        model.clearCalledColor();
        ArrayList<String> botHand = model.hand(model.currentPlayer());
        int chosen = -1;
        String drawn = "R9";
        botHand.add(drawn);
        if (model.shouldCurrentPlayerAutoPlayDrawnCard(drawn)) {
            chosen = botHand.size() - 1;
        }
        passed += check(chosen == 0, "bot auto plays legal drawn card");

        model.setupPlayers(2, false);
        model.setCurrentPlayer(0);
        model.setUpCard("R5");
        model.clearCalledColor();
        botHand = model.hand(model.currentPlayer());
        chosen = -1;
        drawn = "B3";
        botHand.add(drawn);
        if (model.shouldCurrentPlayerAutoPlayDrawnCard(drawn)) {
            chosen = botHand.size() - 1;
        }
        passed += check(chosen == -1, "bot keeps illegal drawn card");

        model.setupPlayers(1, true);
        model.setCurrentPlayer(0);
        model.setUpCard("R5");
        model.clearCalledColor();
        ArrayList<String> humanHand = model.hand(model.currentPlayer());
        chosen = -1;
        drawn = "R9";
        humanHand.add(drawn);
        if (model.shouldCurrentPlayerAutoPlayDrawnCard(drawn)) {
            chosen = humanHand.size() - 1;
        }
        passed += check(chosen == -1, "human does not auto play drawn card");
        return passed;
    }

    private int penaltyPathTests() {
        int passed = 0;
        model.setupPlayers(1, true);
        model.setCurrentPlayer(0);
        model.setDirection(1);
        model.setUpCard("R5");
        model.clearCalledColor();
        ArrayList<String> hand = model.hand(model.currentPlayer());
        hand.add("B3");
        model.clearDeck();
        model.clearDiscard();
        model.addToDeck("Y7");

        int chosen = 0;
        if (!model.isLegalForCurrentState(hand.get(chosen))) {
            model.drawPenaltyAndAdvanceCurrentPlayer();
        }
        passed += check(hand.size() == 2 && hand.get(1).equals("Y7"), "illegal indexed card draws penalty card");
        passed += check(model.currentPlayer() == 1, "illegal indexed card loses turn");

        model.setupPlayers(1, true);
        model.setCurrentPlayer(0);
        model.setDirection(1);
        model.clearDeck();
        model.clearDiscard();
        model.addToDeck("G4");
        hand = model.hand(model.currentPlayer());
        chosen = 3;
        if (chosen >= hand.size()) {
            model.drawPenaltyAndAdvanceCurrentPlayer();
        }
        passed += check(hand.size() == 1 && hand.getFirst().equals("G4"), "out of range selected index draws penalty card");
        passed += check(model.currentPlayer() == 1, "out of range selected index loses turn");
        return passed;
    }

    private int turnMovementTests() {
        int passed = 0;
        model.setupPlayers(3, false);
        model.setCurrentPlayer(0);
        model.setDirection(1);
        model.next(model.playerCount());
        passed += check(model.currentPlayer() == 1, "next moves clockwise");

        model.setCurrentPlayer(2);
        model.setDirection(1);
        model.next(model.playerCount());
        passed += check(model.currentPlayer() == 0, "next wraps clockwise");

        model.setCurrentPlayer(0);
        model.setDirection(-1);
        model.next(model.playerCount());
        passed += check(model.currentPlayer() == 2, "next wraps counterclockwise");

        model.setCurrentPlayer(0);
        model.setDirection(1);
        model.next(model.playerCount());
        model.next(model.playerCount());
        passed += check(model.currentPlayer() == 2, "skip advances over one player");

        model.setCurrentPlayer(0);
        model.setDirection(1);
        model.reverseDirection();
        model.next(model.playerCount());
        passed += check(model.currentPlayer() == 2 && model.direction() == -1, "reverse changes direction with three players");

        model.setupPlayers(1, true);
        model.setCurrentPlayer(0);
        model.setDirection(1);
        model.reverseDirection();
        model.next(model.playerCount());
        model.next(model.playerCount());
        passed += check(model.currentPlayer() == 0 && model.direction() == -1, "reverse skips other player with two players");

        model.setupPlayers(3, false);
        model.setCurrentPlayer(0);
        model.setDirection(1);
        model.clearDeck();
        model.clearDiscard();
        model.addToDeck("R1");
        model.addToDeck("B2");
        model.applyCardEffect("R+2");
        passed += check(model.hand(1).size() == 2 && model.currentPlayer() == 2, "draw two draws and skips");

        model.setupPlayers(3, false);
        model.setCurrentPlayer(0);
        model.setDirection(1);
        model.clearDeck();
        model.clearDiscard();
        model.addToDeck("R1");
        model.addToDeck("Y2");
        model.addToDeck("G3");
        model.addToDeck("B4");
        model.applyCardEffect("W4");
        passed += check(model.hand(1).size() == 4 && model.currentPlayer() == 2, "wild draw four draws and skips");
        return passed;
    }

    private int scoringTests() {
        int passed = 0;
        passed += check(model.points("R0") == 0, "zero points");
        passed += check(model.points("B9") == 9, "number points");
        passed += check(model.points("GS") == 20, "skip points");
        passed += check(model.points("GR") == 20, "reverse points");
        passed += check(model.points("G+2") == 20, "draw two points");
        passed += check(model.points("W") == 50, "wild points");
        passed += check(model.points("W4") == 50, "wild draw four points");

        int total = 0;
        for (String card : cards("R5", "B9", "GS", "W")) {
            total += model.points(card);
        }
        passed += check(total == 84, "losing hand score example");
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
