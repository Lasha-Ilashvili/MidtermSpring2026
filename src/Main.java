import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
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
                System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return;
            }
        }

        random = new Random(seed);
        setupPlayers(bots, human);

        if (playerNames.size() < 2 || playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        for (int gameCount = 1; gameCount <= games; gameCount++) {
            if (!quiet) {
                System.out.println("\n=== Game " + gameCount + " ===");
            }
            playGame();
        }

        System.out.println("\nFinal scores:");
        for (int player = 0; player < playerNames.size(); player++) {
            System.out.println(playerNames.get(player) + ": " + scores[player]);
        }
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
                System.out.println("\nUp card: " + upCard + (calledColor.isEmpty() ? "" : " called " + calledColor));
                System.out.println(name + " hand: " + join(hand));
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
                    System.out.println(name + " draws " + drawn);
                }

                if (isLegal(drawn, upCard, calledColor)) {
                    if (!humanPlayers.get(currentPlayer)) {
                        chosen = hand.size() - 1;
                    } else {
                        System.out.print("Play drawn card " + drawn + "? y/n: ");
                        String answer = scanner.nextLine();

                        if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                            chosen = hand.size() - 1;
                        }
                    }
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    if (!quiet) {
                        System.out.println(name + " selected an invalid index and draws a penalty card.");
                    }
                    hand.add(draw());
                    next();
                    continue;
                }

                String card = hand.get(chosen);
                boolean ok = false;
                String cardColor = color(card);
                String upColor = color(upCard);
                String cardRank = rank(card);
                String upRank = rank(upCard);

                if (card.startsWith("W")) {
                    ok = true;
                } else if (cardColor.equals(upColor)) {
                    ok = true;
                } else if (!calledColor.isEmpty() && cardColor.equals(calledColor)) {
                    ok = true;
                } else if (cardRank.equals(upRank) && !cardRank.equals("NUMBER")) {
                    ok = true;
                } else if (cardRank.equals("NUMBER") && upRank.equals("NUMBER") && number(card) == number(upCard)) {
                    ok = true;
                }

                if (!ok) {
                    if (!quiet) {
                        System.out.println(name + " tried illegal card " + card + " and draws a penalty card.");
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
                    System.out.println(name + " plays " + card);
                }

                if (card.equals("W") || card.equals("W4")) {
                    if (humanPlayers.get(currentPlayer)) {
                        calledColor = askColor();
                    } else {
                        calledColor = chooseBotColor(hand);
                    }
                    if (!quiet) {
                        System.out.println(name + " calls " + calledColor);
                    }
                }

                if (hand.size() == 1 && !quiet) {
                    System.out.println(name + " says UNO!");
                }

                if (hand.isEmpty()) {
                    int points = 0;
                    for (int i = 0; i < hands.size(); i++) {
                        if (i != currentPlayer) {
                            for (int j = 0; j < hands.get(i).size(); j++) {
                                points += points(hands.get(i).get(j));
                            }
                        }
                    }
                    scores[currentPlayer] += points;
                    if (!quiet) {
                        System.out.println(name + " wins and scores " + points);
                    }
                    return;
                }

                if (rank(card).equals("SKIP")) {
                    next();
                    next();
                } else if (rank(card).equals("REVERSE")) {
                    direction = direction * -1;
                    if (playerNames.size() == 2) {
                        next();
                        next();
                    } else {
                        next();
                    }
                } else if (rank(card).equals("DRAW_TWO")) {
                    next();
                    hands.get(currentPlayer).add(draw());
                    hands.get(currentPlayer).add(draw());
                    if (!quiet) {
                        System.out.println(playerNames.get(currentPlayer) + " draws two.");
                    }
                    next();
                } else if (rank(card).equals("WILD_DRAW_FOUR")) {
                    next();
                    for (int i = 0; i < 4; i++) {
                        hands.get(currentPlayer).add(draw());
                    }
                    if (!quiet) {
                        System.out.println(playerNames.get(currentPlayer) + " draws four.");
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
            System.out.println("Game stopped at safety limit.");
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
            else if (color(card).equals(color(upCard))) ok = true;
            else if (!calledColor.isEmpty() && color(card).equals(calledColor)) ok = true;
            else if (rank(card).equals(rank(upCard)) && !rank(card).equals("NUMBER")) ok = true;
            else if (rank(card).equals("NUMBER") && rank(upCard).equals("NUMBER") && number(card) == number(upCard)) ok = true;
            if (rank(card).equals("DRAW_TWO") && ok) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            boolean ok = false;
            if (card.startsWith("W")) ok = true;
            else if (color(card).equals(color(upCard))) ok = true;
            else if (!calledColor.isEmpty() && color(card).equals(calledColor)) ok = true;
            else if (rank(card).equals(rank(upCard)) && !rank(card).equals("NUMBER")) ok = true;
            else if (rank(card).equals("NUMBER") && rank(upCard).equals("NUMBER") && number(card) == number(upCard)) ok = true;
            if (rank(card).equals("SKIP") && ok) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            boolean ok = false;
            if (card.startsWith("W")) ok = true;
            else if (color(card).equals(color(upCard))) ok = true;
            else if (!calledColor.isEmpty() && color(card).equals(calledColor)) ok = true;
            else if (rank(card).equals(rank(upCard)) && !rank(card).equals("NUMBER")) ok = true;
            else if (rank(card).equals("NUMBER") && rank(upCard).equals("NUMBER") && number(card) == number(upCard)) ok = true;
            if (rank(card).equals("NUMBER") && ok) {
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
            System.out.print("Choose card index/code or draw: ");
            String input = scanner.nextLine().trim().toUpperCase();
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
                    if (isLegal(hand.get(i), upCard, calledColor)) {
                        return i;
                    }
                    System.out.println("That card is not legal.");
                }
            }
            System.out.println("Card not found.");
        }
    }

    private static String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String input = scanner.nextLine().trim().toUpperCase();
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
            System.out.println("Bad color.");
        }
    }

    private static String chooseBotColor(ArrayList<String> hand) {
        int r = 0;
        int y = 0;
        int g = 0;
        int b = 0;
        for (String s : hand) {
            String c = color(s);
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

    private static boolean isLegal(String card, String up, String call) {
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

    private static String color(String card) {
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

    private static String rank(String card) {
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

    private static int number(String card) {
        if (rank(card).equals("NUMBER")) {
            return Integer.parseInt(card.substring(1));
        }
        return -1;
    }

    private static int points(String card) {
        String r = rank(card);
        return switch (r) {
            case "NUMBER" -> number(card);
            case "SKIP", "REVERSE", "DRAW_TWO" -> 20;
            case "WILD", "WILD_DRAW_FOUR" -> 50;
            default -> 0;
        };
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

    private static void selfTest() {
        int passed = 0;
        if (color("R5").equals("R")) passed++; else fail("color R5");
        if (rank("G+2").equals("DRAW_TWO")) passed++; else fail("rank +2");
        if (points("W4") == 50) passed++; else fail("wild points");
        if (isLegal("R2", "R9", "")) passed++; else fail("same color");
        if (isLegal("G9", "R9", "")) passed++; else fail("same number");
        if (isLegal("B3", "W", "B")) passed++; else fail("called color");
        if (!isLegal("B3", "R9", "")) passed++; else fail("illegal mismatch");

        ArrayList<String> h = new ArrayList<>();
        h.add("B3");
        h.add("R4");
        h.add("W");
        upCard = "R9";
        calledColor = "";
        if (chooseBotCard(h) == 1) passed++; else fail("bot normal before wild");

        ArrayList<String> h2 = new ArrayList<>();
        h2.add("B1");
        h2.add("B2");
        h2.add("R3");
        if (chooseBotColor(h2).equals("B")) passed++; else fail("bot color");

        System.out.println("Passed " + passed + " characterization checks.");
    }

    private static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
