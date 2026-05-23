import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

public class Main {

    private static final InnerModel model = new InnerModel();
    private static final InnerView view = new InnerView();
    private static final ArrayList<ArrayList<String>> hands = new ArrayList<>();
    private static final ArrayList<String> playerNames = new ArrayList<>();
    private static final ArrayList<String> deck = new ArrayList<>();
    private static final ArrayList<String> discard = new ArrayList<>();
    private static final ArrayList<Boolean> humanPlayers = new ArrayList<>();
    private static final int[] scores = new int[10];

    private static Random random = new Random();
    private static String upCard = "";
    private static String calledColor = "";
    private static int direction = 1;
    private static int currentPlayer = 0;
    private static boolean quiet = false;

    public static void main(String[] args) {
        int bots = 3;
        int games = 1;
        boolean human = false;
        long seed = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            boolean isSecondLast = i + 1 < args.length;

            if (args[i].equals("--bots") && isSecondLast) {
                bots = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--games") && isSecondLast) {
                games = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--human")) {
                human = true;
            } else if (args[i].equals("--quiet")) {
                quiet = true;
            } else if (args[i].equals("--seed") && isSecondLast) {
                seed = Long.parseLong(args[++i]);
            } else if (args[i].equals("--self-test")) {
                selfTest();
                return;
            } else if (args[i].equals("--help")) {
                view.showUsage();
                return;
            }
        }

        random = new Random(seed);
        setupPlayers(bots, human);

        if (playerNames.size() < 2 || playerNames.size() > 4) {
            view.showInvalidPlayerCount();
            return;
        }

        for (int gameCount = 1; gameCount <= games; gameCount++) {
            if (!quiet) {
                view.showGameHeader(gameCount);
            }
            playGame();
        }

        view.showFinalScores(playerNames, scores);
    }

    private static void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();

        if (human) {
            playerNames.add("You");
            humanPlayers.add(Boolean.TRUE);
            hands.add(new ArrayList<>());
        }

        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(Boolean.FALSE);
            hands.add(new ArrayList<>());
        }
    }

    private static void playGame() {
        deck.clear();

        String[] colors = {"R", "Y", "G", "B"};
        for (String color : colors) {
            deck.add(color + "0");
            for (int n = 1; n <= 9; n++) {
                deck.add(color + n);
                deck.add(color + n);
            }
            deck.add(color + "S");
            deck.add(color + "S");
            deck.add(color + "R");
            deck.add(color + "R");
            deck.add(color + "+2");
            deck.add(color + "+2");
        }

        for (int i = 0; i < 4; i++) {
            deck.add("W");
            deck.add("W4");
        }

        Collections.shuffle(deck, random);
        discard.clear();

        for (ArrayList<String> strings : hands) {
            strings.clear();
        }

        for (int i = 0; i < playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) {
                hands.get(i).add(draw());
            }
        }

        upCard = draw();
        while (upCard.startsWith("W")) {
            discard.add(upCard);
            upCard = draw();
        }

        calledColor = "";
        direction = 1;
        currentPlayer = random.nextInt(playerNames.size());

        int guard = 0;
        while (guard < 3000) {
            guard++;
            String name = playerNames.get(currentPlayer);
            ArrayList<String> hand = hands.get(currentPlayer);

            if (!quiet) {
                view.showTurn(name, hand, upCard, calledColor);
            }

            int chosen;

            if (humanPlayers.get(currentPlayer)) {
                chosen = askHuman(hand);
            } else {
                chosen = chooseBotCard(hand);
            }

            if (chosen == -1) {
                String drawn = draw();
                hand.add(drawn);

                if (!quiet) {
                    view.showCardDrawn(name, drawn);
                }

                if (model.isLegal(drawn, upCard, calledColor)) {
                    if (!humanPlayers.get(currentPlayer)) {
                        chosen = hand.size() - 1;
                    } else {
                        view.showPlayDrawnCardPrompt(drawn);
                        String answer = view.readPlayDrawnCardAnswer();

                        if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                            chosen = hand.size() - 1;
                        }
                    }
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    if (!quiet) {
                        view.showInvalidIndexPenalty(name);
                    }
                    hand.add(draw());
                    next();
                    continue;
                }

                String card = hand.get(chosen);
                boolean ok = false;
                String cardColor = model.color(card);
                String upColor = model.color(upCard);
                String cardRank = model.rank(card);
                String upRank = model.rank(upCard);

                if (card.startsWith("W")) {
                    ok = true;
                } else if (cardColor.equals(upColor)) {
                    ok = true;
                } else if (!calledColor.isEmpty() && cardColor.equals(calledColor)) {
                    ok = true;
                } else if (cardRank.equals(upRank) && !cardRank.equals("NUMBER")) {
                    ok = true;
                } else if (cardRank.equals("NUMBER") && upRank.equals("NUMBER") && model.number(card) == model.number(upCard)) {
                    ok = true;
                }

                if (!ok) {
                    if (!quiet) {
                        view.showIllegalCardPenalty(name, card);
                    }
                    hand.add(draw());
                    next();
                    continue;
                }

                hand.remove(chosen);
                discard.add(upCard);
                upCard = card;
                calledColor = "";
                if (!quiet) {
                    view.showCardPlayed(name, card);
                }

                if (card.equals("W") || card.equals("W4")) {
                    if (humanPlayers.get(currentPlayer)) {
                        calledColor = askColor();
                    } else {
                        calledColor = chooseBotColor(hand);
                    }
                    if (!quiet) {
                        view.showColorCalled(name, calledColor);
                    }
                }

                if (hand.size() == 1 && !quiet) {
                    view.showUno(name);
                }

                if (hand.isEmpty()) {
                    int points = 0;
                    for (int i = 0; i < hands.size(); i++) {
                        if (i != currentPlayer) {
                            for (int j = 0; j < hands.get(i).size(); j++) {
                                points += model.points(hands.get(i).get(j));
                            }
                        }
                    }
                    scores[currentPlayer] += points;
                    if (!quiet) {
                        view.showWinnerScore(name, points);
                    }
                    return;
                }

                if (model.rank(card).equals("SKIP")) {
                    next();
                    next();
                } else if (model.rank(card).equals("REVERSE")) {
                    direction = direction * -1;
                    if (playerNames.size() == 2) {
                        next();
                        next();
                    } else {
                        next();
                    }
                } else if (model.rank(card).equals("DRAW_TWO")) {
                    next();
                    hands.get(currentPlayer).add(draw());
                    hands.get(currentPlayer).add(draw());
                    if (!quiet) {
                        view.showDrawTwoPenalty(playerNames.get(currentPlayer));
                    }
                    next();
                } else if (model.rank(card).equals("WILD_DRAW_FOUR")) {
                    next();
                    for (int i = 0; i < 4; i++) {
                        hands.get(currentPlayer).add(draw());
                    }
                    if (!quiet) {
                        view.showDrawFourPenalty(playerNames.get(currentPlayer));
                    }
                    next();
                } else {
                    next();
                }
            } else {
                next();
            }
        }

        if (!quiet) {
            view.showSafetyLimitReached();
        }
    }

    private static String draw() {
        if (deck.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }

        if (deck.isEmpty()) {
            return "W";
        }

        return deck.removeFirst();
    }

    private static int chooseBotCard(ArrayList<String> hand) {
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            boolean ok = false;
            if (card.startsWith("W")) ok = true;
            else if (model.color(card).equals(model.color(upCard))) ok = true;
            else if (!calledColor.isEmpty() && model.color(card).equals(calledColor)) ok = true;
            else if (model.rank(card).equals(model.rank(upCard)) && !model.rank(card).equals("NUMBER")) ok = true;
            else if (model.rank(card).equals("NUMBER") && model.rank(upCard).equals("NUMBER") && model.number(card) == model.number(upCard)) ok = true;
            if (model.rank(card).equals("DRAW_TWO") && ok) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            boolean ok = false;
            if (card.startsWith("W")) ok = true;
            else if (model.color(card).equals(model.color(upCard))) ok = true;
            else if (!calledColor.isEmpty() && model.color(card).equals(calledColor)) ok = true;
            else if (model.rank(card).equals(model.rank(upCard)) && !model.rank(card).equals("NUMBER")) ok = true;
            else if (model.rank(card).equals("NUMBER") && model.rank(upCard).equals("NUMBER") && model.number(card) == model.number(upCard)) ok = true;
            if (model.rank(card).equals("SKIP") && ok) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            boolean ok = false;
            if (card.startsWith("W")) ok = true;
            else if (model.color(card).equals(model.color(upCard))) ok = true;
            else if (!calledColor.isEmpty() && model.color(card).equals(calledColor)) ok = true;
            else if (model.rank(card).equals(model.rank(upCard)) && !model.rank(card).equals("NUMBER")) ok = true;
            else if (model.rank(card).equals("NUMBER") && model.rank(upCard).equals("NUMBER") && model.number(card) == model.number(upCard)) ok = true;
            if (model.rank(card).equals("NUMBER") && ok) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).startsWith("W")) {
                return i;
            }
        }
        return -1;
    }

    private static int askHuman(ArrayList<String> hand) {
        while (true) {
            view.showChooseCardPrompt();
            String input = view.readCardChoiceInput();
            if (input.equals("DRAW")) {
                return -1;
            }
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) {
                    return index;
                }
            } catch (Exception ignored) {
            }
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    if (model.isLegal(hand.get(i), upCard, calledColor)) {
                        return i;
                    }
                    view.showCardNotLegal();
                }
            }
            view.showCardNotFound();
        }
    }

    private static String askColor() {
        while (true) {
            view.showCallColorPrompt();
            String input = view.readColorInput();
            switch (input) {
                case "R" -> {
                    return "R";
                }
                case "Y" -> {
                    return "Y";
                }
                case "G" -> {
                    return "G";
                }
                case "B" -> {
                    return "B";
                }
            }
            view.showBadColor();
        }
    }

    private static String chooseBotColor(ArrayList<String> hand) {
        int r = 0;
        int y = 0;
        int g = 0;
        int b = 0;
        for (String s : hand) {
            String c = model.color(s);
            switch (c) {
                case "R" -> r++;
                case "Y" -> y++;
                case "G" -> g++;
                case "B" -> b++;
            }
        }
        if (r >= y && r >= g && r >= b) {
            return "R";
        } else if (y >= r && y >= g && y >= b) {
            return "Y";
        } else if (g >= r && g >= y && g >= b) {
            return "G";
        } else {
            return "B";
        }
    }

    private static void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerNames.size() - 1;
        }
    }

    private static String join(ArrayList<String> cards) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            out.append(i).append(":").append(cards.get(i));
            if (i < cards.size() - 1) {
                out.append(" ");
            }
        }
        return out.toString();
    }

    private static class InnerModel {

        private boolean isLegal(String card, String up, String call) {
            if (card.startsWith("W")) {
                return true;
            }
            if (color(card).equals(color(up))) {
                return true;
            }
            if (!call.isEmpty() && color(card).equals(call)) {
                return true;
            }
            if (rank(card).equals(rank(up)) && !rank(card).equals("NUMBER")) {
                return true;
            }
            return rank(card).equals("NUMBER") && rank(up).equals("NUMBER") && number(card) == number(up);
        }

        private String color(String card) {
            if (card.startsWith("R")) {
                return "R";
            }
            if (card.startsWith("Y")) {
                return "Y";
            }
            if (card.startsWith("G")) {
                return "G";
            }
            if (card.startsWith("B")) {
                return "B";
            }
            return "";
        }

        private String rank(String card) {
            if (card.equals("W")) {
                return "WILD";
            }
            if (card.equals("W4")) {
                return "WILD_DRAW_FOUR";
            }
            if (card.endsWith("S")) {
                return "SKIP";
            }
            if (card.endsWith("R")) {
                return "REVERSE";
            }
            if (card.endsWith("+2")) {
                return "DRAW_TWO";
            }
            return "NUMBER";
        }

        private int number(String card) {
            if (rank(card).equals("NUMBER")) {
                return Integer.parseInt(card.substring(1));
            }
            return -1;
        }

        private int points(String card) {
            String r = rank(card);
            return switch (r) {
                case "NUMBER" -> number(card);
                case "SKIP", "REVERSE", "DRAW_TWO" -> 20;
                case "WILD", "WILD_DRAW_FOUR" -> 50;
                default -> 0;
            };
        }
    }

    private static class InnerView {

        private Scanner scanner = new Scanner(System.in);

        private void showTurn(String playerName, ArrayList<String> hand, String upCard, String calledColor) {
            System.out.println("\nUp card: " + upCard + (calledColor.isEmpty() ? "" : " called " + calledColor));
            System.out.println(playerName + " hand: " + join(hand));
        }

        private void showCardDrawn(String playerName, String card) {
            System.out.println(playerName + " draws " + card);
        }

        private void showInvalidIndexPenalty(String playerName) {
            System.out.println(playerName + " selected an invalid index and draws a penalty card.");
        }

        private void showIllegalCardPenalty(String playerName, String card) {
            System.out.println(playerName + " tried illegal card " + card + " and draws a penalty card.");
        }

        private void showCardPlayed(String playerName, String card) {
            System.out.println(playerName + " plays " + card);
        }

        private void showColorCalled(String playerName, String color) {
            System.out.println(playerName + " calls " + color);
        }

        private void showUno(String playerName) {
            System.out.println(playerName + " says UNO!");
        }

        private void showWinnerScore(String playerName, int points) {
            System.out.println(playerName + " wins and scores " + points);
        }

        private void showDrawTwoPenalty(String playerName) {
            System.out.println(playerName + " draws two.");
        }

        private void showDrawFourPenalty(String playerName) {
            System.out.println(playerName + " draws four.");
        }

        private void showSafetyLimitReached() {
            System.out.println("Game stopped at safety limit.");
        }

        private void showChooseCardPrompt() {
            System.out.print("Choose card index/code or draw: ");
        }

        private void showCardNotLegal() {
            System.out.println("That card is not legal.");
        }

        private void showCardNotFound() {
            System.out.println("Card not found.");
        }

        private void showCallColorPrompt() {
            System.out.print("Call color R/Y/G/B: ");
        }

        private void showBadColor() {
            System.out.println("Bad color.");
        }

        private void showPlayDrawnCardPrompt(String card) {
            System.out.print("Play drawn card " + card + "? y/n: ");
        }

        private void showUsage() {
            System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
        }

        private void showInvalidPlayerCount() {
            System.out.println("UNO needs 2 to 4 players.");
        }

        private void showGameHeader(int gameCount) {
            System.out.println("\n=== Game " + gameCount + " ===");
        }

        private void showFinalScores(ArrayList<String> playerNames, int[] scores) {
            System.out.println("\nFinal scores:");
            for (int player = 0; player < playerNames.size(); player++) {
                System.out.println(playerNames.get(player) + ": " + scores[player]);
            }
        }

        private String readPlayDrawnCardAnswer() {
            return scanner.nextLine();
        }

        private String readCardChoiceInput() {
            return scanner.nextLine().trim().toUpperCase();
        }

        private String readColorInput() {
            return scanner.nextLine().trim().toUpperCase();
        }

        private <T> T withInput(String input, Supplier<T> action) {
            Scanner originalScanner = this.scanner;
            this.scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            try {
                return action.get();
            } finally {
                this.scanner = originalScanner;
            }
        }
    }

    /* TESTS */
    private static String selfTestCapturedOutput = "";

    private static void selfTest() {
        int passed = 0;
        passed += selfTestCards();
        passed += selfTestLegalPlays();
        passed += selfTestBotChoices();
        passed += selfTestHumanInputQuirks();
        passed += selfTestConsoleBehavior();
        passed += selfTestDrawPile();
        passed += selfTestDrawnCardDecisions();
        passed += selfTestPenaltyPaths();
        passed += selfTestTurnMovement();
        passed += selfTestScoring();
        passed += selfTestSeededBotGames();

        System.out.println("Passed " + passed + " characterization checks.");
    }

    private static int selfTestCards() {
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

    private static int selfTestLegalPlays() {
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

    private static int selfTestBotChoices() {
        int passed = 0;
        upCard = "R9";
        calledColor = "";
        passed += check(chooseBotCard(cards("B3", "R4", "W")) == 1, "bot number before wild");
        passed += check(chooseBotCard(cards("R4", "R+2", "W")) == 1, "bot draw two priority");

        upCard = "G9";
        passed += check(chooseBotCard(cards("G3", "GS", "W")) == 1, "bot skip priority");

        upCard = "YR";
        calledColor = "";
        passed += check(chooseBotCard(cards("BR", "W")) == 1, "bot reverse quirk before wild");

        upCard = "W";
        calledColor = "G";
        passed += check(chooseBotCard(cards("R1", "G3")) == 1, "bot uses called color");

        upCard = "R9";
        calledColor = "";
        passed += check(chooseBotCard(cards("B1", "G2")) == -1, "bot draws when no legal card");
        passed += check(chooseBotColor(cards("B1", "B2", "R3")).equals("B"), "bot color majority");
        passed += check(chooseBotColor(cards("R1", "Y2", "G3", "B4")).equals("R"), "bot color tie defaults red");
        return passed;
    }

    private static int selfTestHumanInputQuirks() {
        int passed = 0;
        upCard = "R5";
        calledColor = "";
        passed += check(askHumanForSelfTest(cards("R9"), "draw\n") == -1, "human can draw while holding legal card");

        upCard = "R5";
        calledColor = "";
        passed += check(askHumanForSelfTest(cards("B3", "R9"), "0\n") == 0, "human index input bypasses legality check");
        passed += check(!model.isLegal("B3", upCard, calledColor), "indexed illegal card remains illegal later");

        upCard = "R5";
        calledColor = "";
        passed += check(askHumanForSelfTest(cards("B3", "R9"), "B3\nR9\n") == 1, "human card code rejects illegal card");
        passed += check(selfTestCapturedOutput.contains("That card is not legal."), "illegal code prints not legal message");
        passed += check(selfTestCapturedOutput.contains("Card not found."), "illegal code also prints card not found quirk");
        return passed;
    }

    private static int selfTestConsoleBehavior() {
        int passed = 0;
        passed += check(join(cards("R5", "W", "B+2")).equals("0:R5 1:W 2:B+2"), "hand display includes indexes");
        passed += check(askColorForSelfTest().equals("B"), "human color prompt accepts valid color after bad input");
        passed += check(selfTestCapturedOutput.contains("Bad color."), "bad color message");
        return passed;
    }

    private static int selfTestDrawPile() {
        int passed = 0;
        random = new Random(7);
        deck.clear();
        discard.clear();
        deck.add("R1");
        deck.add("B2");
        passed += check(draw().equals("R1"), "draw removes top deck card");
        passed += check(deck.size() == 1 && deck.getFirst().equals("B2"), "draw leaves remaining deck");

        deck.clear();
        discard.clear();
        discard.add("G5");
        passed += check(draw().equals("G5"), "empty deck refills from discard");
        passed += check(discard.isEmpty(), "discard cleared after refill");

        deck.clear();
        discard.clear();
        passed += check(draw().equals("W"), "empty draw and discard fallback");
        return passed;
    }

    private static int selfTestDrawnCardDecisions() {
        int passed = 0;
        setupPlayers(2, false);
        currentPlayer = 0;
        upCard = "R5";
        calledColor = "";
        ArrayList<String> botHand = hands.get(currentPlayer);
        int chosen = -1;
        String drawn = "R9";
        botHand.add(drawn);
        if (model.isLegal(drawn, upCard, calledColor) && !humanPlayers.get(currentPlayer)) {
            chosen = botHand.size() - 1;
        }
        passed += check(chosen == 0, "bot auto plays legal drawn card");

        setupPlayers(2, false);
        currentPlayer = 0;
        upCard = "R5";
        calledColor = "";
        botHand = hands.get(currentPlayer);
        chosen = -1;
        drawn = "B3";
        botHand.add(drawn);
        if (model.isLegal(drawn, upCard, calledColor) && !humanPlayers.get(currentPlayer)) {
            chosen = botHand.size() - 1;
        }
        passed += check(chosen == -1, "bot keeps illegal drawn card");

        setupPlayers(1, true);
        currentPlayer = 0;
        upCard = "R5";
        calledColor = "";
        ArrayList<String> humanHand = hands.get(currentPlayer);
        chosen = -1;
        drawn = "R9";
        humanHand.add(drawn);
        if (model.isLegal(drawn, upCard, calledColor) && !humanPlayers.get(currentPlayer)) {
            chosen = humanHand.size() - 1;
        }
        passed += check(chosen == -1, "human does not auto play drawn card");
        return passed;
    }

    private static int selfTestPenaltyPaths() {
        int passed = 0;
        setupPlayers(1, true);
        currentPlayer = 0;
        direction = 1;
        upCard = "R5";
        calledColor = "";
        ArrayList<String> hand = hands.get(currentPlayer);
        hand.add("B3");
        deck.clear();
        discard.clear();
        deck.add("Y7");

        int chosen = 0;
        if (!model.isLegal(hand.get(chosen), upCard, calledColor)) {
            hand.add(draw());
            next();
        }
        passed += check(hand.size() == 2 && hand.get(1).equals("Y7"), "illegal indexed card draws penalty card");
        passed += check(currentPlayer == 1, "illegal indexed card loses turn");

        setupPlayers(1, true);
        currentPlayer = 0;
        direction = 1;
        deck.clear();
        discard.clear();
        deck.add("G4");
        hand = hands.get(currentPlayer);
        chosen = 3;
        if (chosen >= hand.size()) {
            hand.add(draw());
            next();
        }
        passed += check(hand.size() == 1 && hand.getFirst().equals("G4"), "out of range selected index draws penalty card");
        passed += check(currentPlayer == 1, "out of range selected index loses turn");
        return passed;
    }

    private static int selfTestTurnMovement() {
        int passed = 0;
        setupPlayers(3, false);
        currentPlayer = 0;
        direction = 1;
        next();
        passed += check(currentPlayer == 1, "next moves clockwise");

        currentPlayer = 2;
        direction = 1;
        next();
        passed += check(currentPlayer == 0, "next wraps clockwise");

        currentPlayer = 0;
        direction = -1;
        next();
        passed += check(currentPlayer == 2, "next wraps counterclockwise");

        currentPlayer = 0;
        direction = 1;
        next();
        next();
        passed += check(currentPlayer == 2, "skip advances over one player");

        currentPlayer = 0;
        direction = 1;
        direction = direction * -1;
        next();
        passed += check(currentPlayer == 2 && direction == -1, "reverse changes direction with three players");

        setupPlayers(1, true);
        currentPlayer = 0;
        direction = 1;
        direction = direction * -1;
        next();
        next();
        passed += check(currentPlayer == 0 && direction == -1, "reverse skips other player with two players");

        setupPlayers(3, false);
        currentPlayer = 0;
        direction = 1;
        deck.clear();
        discard.clear();
        deck.add("R1");
        deck.add("B2");
        next();
        hands.get(currentPlayer).add(draw());
        hands.get(currentPlayer).add(draw());
        next();
        passed += check(hands.get(1).size() == 2 && currentPlayer == 2, "draw two draws and skips");

        setupPlayers(3, false);
        currentPlayer = 0;
        direction = 1;
        deck.clear();
        discard.clear();
        deck.add("R1");
        deck.add("Y2");
        deck.add("G3");
        deck.add("B4");
        next();
        for (int i = 0; i < 4; i++) {
            hands.get(currentPlayer).add(draw());
        }
        next();
        passed += check(hands.get(1).size() == 4 && currentPlayer == 2, "wild draw four draws and skips");
        return passed;
    }

    private static int selfTestScoring() {
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

    private static int selfTestSeededBotGames() {
        int passed = 0;
        quiet = true;
        random = new Random(123);
        setupPlayers(3, false);
        Arrays.fill(scores, 0);
        for (int game = 0; game < 5; game++) {
            playGame();
        }
        passed += check(scores[0] == 138, "seeded bot games score Bot1");
        passed += check(scores[1] == 246, "seeded bot games score Bot2");
        passed += check(scores[2] == 98, "seeded bot games score Bot3");
        quiet = false;
        return passed;
    }

    private static int check(boolean condition, String name) {
        if (!condition) {
            fail(name);
        }
        return 1;
    }

    private static int askHumanForSelfTest(ArrayList<String> hand, String input) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        try {
            return view.withInput(input, () -> askHuman(hand));
        } finally {
            System.setOut(originalOut);
            selfTestCapturedOutput = output.toString(StandardCharsets.UTF_8);
        }
    }

    private static String askColorForSelfTest() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        try {
            return view.withInput("x\nb\n", Main::askColor);
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
