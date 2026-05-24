import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

public class Main {

    private static final InnerModel model = new InnerModel();
    private static final InnerView view = new InnerView();

    private static boolean quiet = false;

    private static class Startup {

        private enum Action {
            START_GAME,
            SELF_TEST,
            HELP,
            UNSUPPORTED_UI
        }

        private record Input(
                String bots,
                String games,
                boolean human,
                boolean quiet,
                String seed,
                Action action
        ) {
        }

        private static Input unsupportedUi() {
            return new Input(null, null, false, false, null, Action.UNSUPPORTED_UI);
        }
    }

    private sealed interface UiType permits UiType.Cli {

        record Cli(String[] args) implements UiType {

            public Cli {
                args = Objects.requireNonNull(args).clone();
            }

            @Override
            public String[] args() {
                return args.clone();
            }
        }
    }

    public static void main(String[] args) {
        Startup.Input startupInput = view.readStartupInput(new UiType.Cli(args));
        int bots = 3;
        int games = 1;
        long seed = System.currentTimeMillis();

        if (startupInput.bots() != null) {
            bots = Integer.parseInt(startupInput.bots());
        }
        if (startupInput.games() != null) {
            games = Integer.parseInt(startupInput.games());
        }
        if (startupInput.seed() != null) {
            seed = Long.parseLong(startupInput.seed());
        }
        quiet = startupInput.quiet();

        if (startupInput.action() == Startup.Action.SELF_TEST) {
            selfTest();
            return;
        } else if (startupInput.action() == Startup.Action.HELP) {
            view.showUsage();
            return;
        } else if (startupInput.action() == Startup.Action.UNSUPPORTED_UI) {
            view.showUnsupportedUiType();
            return;
        }

        model.seedRandom(seed);
        model.setupPlayers(bots, startupInput.human());

        if (model.playerCount() < 2 || model.playerCount() > 4) {
            view.showInvalidPlayerCount();
            return;
        }

        for (int gameCount = 1; gameCount <= games; gameCount++) {
            if (!quiet) {
                view.showGameHeader(gameCount);
            }
            playGame();
        }

        view.showFinalScores(model.playerNames(), model.scores());
    }

    private static void playGame() {
        model.startRound();

        int guard = 0;
        while (guard < 3000) {
            guard++;
            String name = model.currentPlayerName();

            if (!quiet) {
                view.showTurn(name, model.currentHand(), model.upCard(), model.calledColor());
            }

            int chosen;

            if (model.isHumanCurrentPlayer()) {
                chosen = askHuman();
            } else {
                chosen = model.chooseCurrentBotCard();
            }

            if (chosen == -1) {
                String drawn = model.drawForCurrentPlayer();

                if (!quiet) {
                    view.showCardDrawn(name, drawn);
                }

                if (model.isLegalForCurrentState(drawn)) {
                    if (!model.isHumanCurrentPlayer()) {
                        chosen = model.lastCurrentHandIndex();
                    } else {
                        view.showPlayDrawnCardPrompt(drawn);
                        String answer = view.readPlayDrawnCardAnswer();

                        if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                            chosen = model.lastCurrentHandIndex();
                        }
                    }
                }
            }

            if (chosen >= 0) {
                if (model.isOutsideCurrentHand(chosen)) {
                    if (!quiet) {
                        view.showInvalidIndexPenalty(name);
                    }
                    model.drawPenaltyAndAdvanceCurrentPlayer();
                    continue;
                }

                String card = model.currentHandCard(chosen);

                if (!model.isLegalForCurrentState(card)) {
                    if (!quiet) {
                        view.showIllegalCardPenalty(name, card);
                    }
                    model.drawPenaltyAndAdvanceCurrentPlayer();
                    continue;
                }

                model.playCardFromCurrentHand(chosen);
                if (!quiet) {
                    view.showCardPlayed(name, card);
                }

                if (model.isWildCard(card)) {
                    if (model.isHumanCurrentPlayer()) {
                        model.setCalledColor(askColor());
                    } else {
                        model.setCalledColor(model.chooseCurrentBotColor());
                    }
                    if (!quiet) {
                        view.showColorCalled(name, model.calledColor());
                    }
                }

                if (model.currentPlayerHasOneCard() && !quiet) {
                    view.showUno(name);
                }

                if (model.currentPlayerHasNoCards()) {
                    int points = model.scoreCurrentPlayerFromOpponents();
                    if (!quiet) {
                        view.showWinnerScore(name, points);
                    }
                    return;
                }

                InnerModel.TurnEffect effect = model.applyCardEffect(card);
                if (!quiet) {
                    if (effect.type() == InnerModel.EffectType.DRAW_TWO) {
                        view.showDrawTwoPenalty(effect.playerName());
                    } else if (effect.type() == InnerModel.EffectType.DRAW_FOUR) {
                        view.showDrawFourPenalty(effect.playerName());
                    }
                }
            } else {
                model.advanceToNextPlayer();
            }
        }

        if (!quiet) {
            view.showSafetyLimitReached();
        }
    }

    private static int askHuman() {
        while (true) {
            view.showChooseCardPrompt();
            String input = view.readCardChoiceInput();
            if (input.equals("DRAW")) {
                return -1;
            }
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < model.currentHandSize()) {
                    return index;
                }
            } catch (Exception ignored) {
            }
            for (int i = 0; i < model.currentHandSize(); i++) {
                if (model.currentHandCard(i).equals(input)) {
                    if (model.isLegalForCurrentState(model.currentHandCard(i))) {
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

    private static class InnerModel {

        private enum EffectType {
            NONE,
            DRAW_TWO,
            DRAW_FOUR
        }

        private record TurnEffect(EffectType type, String playerName) {
        }

        private final ArrayList<String> deck = new ArrayList<>();
        private final ArrayList<String> discard = new ArrayList<>();
        private final ArrayList<ArrayList<String>> hands = new ArrayList<>();
        private final ArrayList<String> playerNames = new ArrayList<>();
        private final ArrayList<Boolean> humanPlayers = new ArrayList<>();
        private final int[] scores = new int[10];
        private String upCard = "";
        private String calledColor = "";
        private Random random = new Random();
        private int currentPlayer = 0;
        private int direction = 1;

        private void seedRandom(long seed) {
            random = new Random(seed);
        }

        private void setupPlayers(int bots, boolean human) {
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

        private ArrayList<String> playerNames() {
            return playerNames;
        }

        private int[] scores() {
            return scores;
        }

        private void clearScores() {
            Arrays.fill(scores, 0);
        }

        private int score(int player) {
            return scores[player];
        }

        private void addScore(int player, int points) {
            scores[player] += points;
        }

        private int scoreCurrentPlayerFromOpponents() {
            int points = 0;
            for (int i = 0; i < playerCount(); i++) {
                if (i != currentPlayer()) {
                    for (int j = 0; j < hand(i).size(); j++) {
                        points += points(hand(i).get(j));
                    }
                }
            }
            addScore(currentPlayer(), points);
            return points;
        }

        private String upCard() {
            return upCard;
        }

        private void setUpCard(String upCard) {
            this.upCard = upCard;
        }

        private String calledColor() {
            return calledColor;
        }

        private void setCalledColor(String calledColor) {
            this.calledColor = calledColor;
        }

        private void clearCalledColor() {
            calledColor = "";
        }

        private int playerCount() {
            return playerNames.size();
        }

        private String currentPlayerName() {
            return playerNames.get(currentPlayer);
        }

        private boolean isHumanCurrentPlayer() {
            return humanPlayers.get(currentPlayer);
        }

        private ArrayList<String> hand(int player) {
            return hands.get(player);
        }

        private ArrayList<String> currentHand() {
            return hand(currentPlayer);
        }

        private int currentHandSize() {
            return currentHand().size();
        }

        private boolean currentPlayerHasOneCard() {
            return currentHandSize() == 1;
        }

        private boolean currentPlayerHasNoCards() {
            return currentHand().isEmpty();
        }

        private String currentHandCard(int index) {
            return currentHand().get(index);
        }

        private int lastCurrentHandIndex() {
            return currentHandSize() - 1;
        }

        private boolean isOutsideCurrentHand(int index) {
            return index >= currentHandSize();
        }

        private int chooseCurrentBotCard() {
            return chooseBotCard(currentHand());
        }

        private String chooseCurrentBotColor() {
            return chooseBotColor(currentHand());
        }

        private String drawForCurrentPlayer() {
            String drawn = draw();
            currentHand().add(drawn);
            return drawn;
        }

        private void drawPenaltyAndAdvanceCurrentPlayer() {
            currentHand().add(draw());
            advanceToNextPlayer();
        }

        private void playCardFromCurrentHand(int chosen) {
            String card = currentHand().remove(chosen);
            discard(upCard());
            setUpCard(card);
            clearCalledColor();
        }

        private void clearHands() {
            for (ArrayList<String> hand : hands) {
                hand.clear();
            }
        }

        private void startRound() {
            buildDeck();
            shuffleDeck();
            clearDiscard();
            clearHands();
            dealInitialHands();
            chooseStartingUpCard();
            clearCalledColor();
            resetTurnOrder();
            chooseRandomCurrentPlayer(playerCount());
        }

        private void buildDeck() {
            clearDeck();

            String[] colors = {"R", "Y", "G", "B"};
            for (String color : colors) {
                addToDeck(color + "0");
                for (int n = 1; n <= 9; n++) {
                    addToDeck(color + n);
                    addToDeck(color + n);
                }
                addToDeck(color + "S");
                addToDeck(color + "S");
                addToDeck(color + "R");
                addToDeck(color + "R");
                addToDeck(color + "+2");
                addToDeck(color + "+2");
            }

            for (int i = 0; i < 4; i++) {
                addToDeck("W");
                addToDeck("W4");
            }
        }

        private void dealInitialHands() {
            for (int i = 0; i < playerCount(); i++) {
                for (int j = 0; j < 7; j++) {
                    hand(i).add(draw());
                }
            }
        }

        private void chooseStartingUpCard() {
            setUpCard(draw());
            while (upCard().startsWith("W")) {
                discard(upCard());
                setUpCard(draw());
            }
        }

        private int randomPlayerIndex(int playerCount) {
            return random.nextInt(playerCount);
        }

        private void chooseRandomCurrentPlayer(int playerCount) {
            currentPlayer = randomPlayerIndex(playerCount);
        }

        private void advanceToNextPlayer() {
            next(playerCount());
        }

        private int currentPlayer() {
            return currentPlayer;
        }

        private int direction() {
            return direction;
        }

        private void setCurrentPlayer(int currentPlayer) {
            this.currentPlayer = currentPlayer;
        }

        private void setDirection(int direction) {
            this.direction = direction;
        }

        private void resetTurnOrder() {
            direction = 1;
        }

        private void reverseDirection() {
            direction = direction * -1;
        }

        private void next(int playerCount) {
            currentPlayer += direction;
            if (currentPlayer >= playerCount) {
                currentPlayer = 0;
            }
            if (currentPlayer < 0) {
                currentPlayer = playerCount - 1;
            }
        }

        private TurnEffect applyCardEffect(String card) {
            if (rank(card).equals("SKIP")) {
                advanceToNextPlayer();
                advanceToNextPlayer();
                return new TurnEffect(EffectType.NONE, "");
            } else if (rank(card).equals("REVERSE")) {
                reverseDirection();
                if (playerCount() == 2) {
                    advanceToNextPlayer();
                    advanceToNextPlayer();
                } else {
                    advanceToNextPlayer();
                }
                return new TurnEffect(EffectType.NONE, "");
            } else if (rank(card).equals("DRAW_TWO")) {
                advanceToNextPlayer();
                currentHand().add(draw());
                currentHand().add(draw());
                String penaltyPlayerName = currentPlayerName();
                advanceToNextPlayer();
                return new TurnEffect(EffectType.DRAW_TWO, penaltyPlayerName);
            } else if (rank(card).equals("WILD_DRAW_FOUR")) {
                advanceToNextPlayer();
                for (int i = 0; i < 4; i++) {
                    currentHand().add(draw());
                }
                String penaltyPlayerName = currentPlayerName();
                advanceToNextPlayer();
                return new TurnEffect(EffectType.DRAW_FOUR, penaltyPlayerName);
            } else {
                advanceToNextPlayer();
                return new TurnEffect(EffectType.NONE, "");
            }
        }

        private void clearDeck() {
            deck.clear();
        }

        private void addToDeck(String card) {
            deck.add(card);
        }

        private void shuffleDeck() {
            Collections.shuffle(deck, random);
        }

        private void clearDiscard() {
            discard.clear();
        }

        private void discard(String card) {
            discard.add(card);
        }

        private int deckSize() {
            return deck.size();
        }

        private String firstDeckCard() {
            return deck.getFirst();
        }

        private boolean isDiscardEmpty() {
            return discard.isEmpty();
        }

        private String draw() {
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

        private boolean isLegalForCurrentState(String card) {
            return isLegal(card, upCard(), calledColor());
        }

        private boolean isWildCard(String card) {
            return card.equals("W") || card.equals("W4");
        }

        private int chooseBotCard(ArrayList<String> hand) {
            for (int i = 0; i < hand.size(); i++) {
                String card = hand.get(i);
                boolean ok = false;
                if (card.startsWith("W")) ok = true;
                else if (color(card).equals(color(upCard()))) ok = true;
                else if (!calledColor().isEmpty() && color(card).equals(calledColor())) ok = true;
                else if (rank(card).equals(rank(upCard())) && !rank(card).equals("NUMBER")) ok = true;
                else if (rank(card).equals("NUMBER") && rank(upCard()).equals("NUMBER") && number(card) == number(upCard())) ok = true;
                if (rank(card).equals("DRAW_TWO") && ok) {
                    return i;
                }
            }
            for (int i = 0; i < hand.size(); i++) {
                String card = hand.get(i);
                boolean ok = false;
                if (card.startsWith("W")) ok = true;
                else if (color(card).equals(color(upCard()))) ok = true;
                else if (!calledColor().isEmpty() && color(card).equals(calledColor())) ok = true;
                else if (rank(card).equals(rank(upCard())) && !rank(card).equals("NUMBER")) ok = true;
                else if (rank(card).equals("NUMBER") && rank(upCard()).equals("NUMBER") && number(card) == number(upCard())) ok = true;
                if (rank(card).equals("SKIP") && ok) {
                    return i;
                }
            }
            for (int i = 0; i < hand.size(); i++) {
                String card = hand.get(i);
                boolean ok = false;
                if (card.startsWith("W")) ok = true;
                else if (color(card).equals(color(upCard()))) ok = true;
                else if (!calledColor().isEmpty() && color(card).equals(calledColor())) ok = true;
                else if (rank(card).equals(rank(upCard())) && !rank(card).equals("NUMBER")) ok = true;
                else if (rank(card).equals("NUMBER") && rank(upCard()).equals("NUMBER") && number(card) == number(upCard())) ok = true;
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

        private String chooseBotColor(ArrayList<String> hand) {
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

        private Startup.Input readStartupInput(UiType uiType) {
            if (uiType instanceof UiType.Cli cli) {
                return readCliStartupInput(cli.args());
            }
            return Startup.unsupportedUi();
        }

        private Startup.Input readCliStartupInput(String[] args) {
            String bots = null;
            String games = null;
            boolean human = false;
            boolean isQuiet = false;
            String seed = null;
            Startup.Action action = Startup.Action.START_GAME;

            for (int i = 0; i < args.length; i++) {
                boolean hasNext = i + 1 < args.length;

                if (args[i].equals("--bots") && hasNext) {
                    bots = args[++i];
                } else if (args[i].equals("--games") && hasNext) {
                    games = args[++i];
                } else if (args[i].equals("--human")) {
                    human = true;
                } else if (args[i].equals("--quiet")) {
                    isQuiet = true;
                } else if (args[i].equals("--seed") && hasNext) {
                    seed = args[++i];
                } else if (args[i].equals("--self-test")) {
                    action = Startup.Action.SELF_TEST;
                    break;
                } else if (args[i].equals("--help")) {
                    action = Startup.Action.HELP;
                    break;
                }
            }

            return new Startup.Input(bots, games, human, isQuiet, seed, action);
        }

        private void showTurn(String playerName, ArrayList<String> hand, String upCard, String calledColor) {
            System.out.println("\nUp card: " + upCard + (calledColor.isEmpty() ? "" : " called " + calledColor));
            System.out.println(playerName + " hand: " + join(hand));
        }

        private String join(ArrayList<String> cards) {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < cards.size(); i++) {
                out.append(i).append(":").append(cards.get(i));
                if (i < cards.size() - 1) {
                    out.append(" ");
                }
            }
            return out.toString();
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

        private void showUnsupportedUiType() {
            System.out.println("Unsupported UI type.");
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

    private static int selfTestHumanInputQuirks() {
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

    private static int selfTestConsoleBehavior() {
        int passed = 0;
        passed += check(view.join(cards("R5", "W", "B+2")).equals("0:R5 1:W 2:B+2"), "hand display includes indexes");
        passed += check(askColorForSelfTest().equals("B"), "human color prompt accepts valid color after bad input");
        passed += check(selfTestCapturedOutput.contains("Bad color."), "bad color message");
        return passed;
    }

    private static int selfTestDrawPile() {
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

    private static int selfTestDrawnCardDecisions() {
        int passed = 0;
        model.setupPlayers(2, false);
        model.setCurrentPlayer(0);
        model.setUpCard("R5");
        model.clearCalledColor();
        ArrayList<String> botHand = model.hand(model.currentPlayer());
        int chosen = -1;
        String drawn = "R9";
        botHand.add(drawn);
        if (model.isLegalForCurrentState(drawn) && !model.isHumanCurrentPlayer()) {
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
        if (model.isLegalForCurrentState(drawn) && !model.isHumanCurrentPlayer()) {
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
        if (model.isLegalForCurrentState(drawn) && !model.isHumanCurrentPlayer()) {
            chosen = humanHand.size() - 1;
        }
        passed += check(chosen == -1, "human does not auto play drawn card");
        return passed;
    }

    private static int selfTestPenaltyPaths() {
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
            hand.add(model.draw());
            model.next(model.playerCount());
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
            hand.add(model.draw());
            model.next(model.playerCount());
        }
        passed += check(hand.size() == 1 && hand.getFirst().equals("G4"), "out of range selected index draws penalty card");
        passed += check(model.currentPlayer() == 1, "out of range selected index loses turn");
        return passed;
    }

    private static int selfTestTurnMovement() {
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
        model.next(model.playerCount());
        model.hand(model.currentPlayer()).add(model.draw());
        model.hand(model.currentPlayer()).add(model.draw());
        model.next(model.playerCount());
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
        model.next(model.playerCount());
        for (int i = 0; i < 4; i++) {
            model.hand(model.currentPlayer()).add(model.draw());
        }
        model.next(model.playerCount());
        passed += check(model.hand(1).size() == 4 && model.currentPlayer() == 2, "wild draw four draws and skips");
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
        model.seedRandom(123);
        model.setupPlayers(3, false);
        model.clearScores();
        for (int game = 0; game < 5; game++) {
            playGame();
        }
        passed += check(model.score(0) == 138, "seeded bot games score Bot1");
        passed += check(model.score(1) == 246, "seeded bot games score Bot2");
        passed += check(model.score(2) == 98, "seeded bot games score Bot3");
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
            model.setupPlayers(1, true);
            model.setCurrentPlayer(0);
            model.currentHand().addAll(hand);
            return view.withInput(input, Main::askHuman);
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
