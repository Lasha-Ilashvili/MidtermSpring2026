package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Model {

    public enum EffectType {
        NONE,
        DRAW_TWO,
        DRAW_FOUR
    }

    public record TurnEffect(EffectType type, String playerName) {
    }

    public record CardCodeChoice(int index, int illegalMatchCount) {

        public boolean hasLegalMatch() {
            return index != -1;
        }
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

    public void seedRandom(long seed) {
        random = new Random(seed);
    }

    public boolean isPlayableColor(String color) {
        return color.equals("R") || color.equals("Y") || color.equals("G") || color.equals("B");
    }

    public void setupPlayers(int bots, boolean human) {
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

    public ArrayList<String> playerNamesSnapshot() {
        return new ArrayList<>(playerNames);
    }

    public int[] scoresSnapshot() {
        return scores.clone();
    }

    public void clearScores() {
        Arrays.fill(scores, 0);
    }

    public int score(int player) {
        return scores[player];
    }

    public void addScore(int player, int points) {
        scores[player] += points;
    }

    public int scoreCurrentPlayerFromOpponents() {
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

    public String upCard() {
        return upCard;
    }

    public void setUpCard(String upCard) {
        this.upCard = upCard;
    }

    public String calledColor() {
        return calledColor;
    }

    public void setCalledColor(String calledColor) {
        this.calledColor = calledColor;
    }

    public void clearCalledColor() {
        calledColor = "";
    }

    public int playerCount() {
        return playerNames.size();
    }

    public boolean isPlayerCountValid() {
        return playerCount() >= 2 && playerCount() <= 4;
    }

    public String currentPlayerName() {
        return playerNames.get(currentPlayer);
    }

    public boolean isHumanCurrentPlayer() {
        return humanPlayers.get(currentPlayer);
    }

    public boolean shouldCurrentPlayerAutoPlayDrawnCard(String drawn) {
        return isLegalForCurrentState(drawn) && !isHumanCurrentPlayer();
    }

    public boolean shouldAskCurrentPlayerToPlayDrawnCard(String drawn) {
        return isLegalForCurrentState(drawn) && isHumanCurrentPlayer();
    }

    public ArrayList<String> hand(int player) {
        return hands.get(player);
    }

    public ArrayList<String> currentHand() {
        return hand(currentPlayer);
    }

    public ArrayList<String> currentHandSnapshot() {
        return new ArrayList<>(currentHand());
    }

    public int currentHandSize() {
        return currentHand().size();
    }

    public boolean currentPlayerHasOneCard() {
        return currentHandSize() == 1;
    }

    public boolean currentPlayerHasNoCards() {
        return currentHand().isEmpty();
    }

    public String currentHandCard(int index) {
        return currentHand().get(index);
    }

    public int lastCurrentHandIndex() {
        return currentHandSize() - 1;
    }

    public boolean isCurrentHandIndex(int index) {
        return index >= 0 && index < currentHandSize();
    }

    public boolean isOutsideCurrentHand(int index) {
        return index >= currentHandSize();
    }

    public CardCodeChoice chooseCurrentCardByCode(String cardCode) {
        int illegalMatches = 0;
        for (int i = 0; i < currentHandSize(); i++) {
            if (currentHandCard(i).equals(cardCode)) {
                if (isLegalForCurrentState(currentHandCard(i))) {
                    return new CardCodeChoice(i, illegalMatches);
                }
                illegalMatches++;
            }
        }
        return new CardCodeChoice(-1, illegalMatches);
    }

    public int chooseCurrentBotCard() {
        return chooseBotCard(currentHand());
    }

    public String chooseCurrentBotColor() {
        return chooseBotColor(currentHand());
    }

    public String drawForCurrentPlayer() {
        String drawn = draw();
        currentHand().add(drawn);
        return drawn;
    }

    public void drawPenaltyAndAdvanceCurrentPlayer() {
        currentHand().add(draw());
        advanceToNextPlayer();
    }

    public void playCardFromCurrentHand(int chosen) {
        String card = currentHand().remove(chosen);
        discard(upCard());
        setUpCard(card);
        clearCalledColor();
    }

    public void clearHands() {
        for (ArrayList<String> hand : hands) {
            hand.clear();
        }
    }

    public void startRound() {
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

    public void buildDeck() {
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

    public void dealInitialHands() {
        for (int i = 0; i < playerCount(); i++) {
            for (int j = 0; j < 7; j++) {
                hand(i).add(draw());
            }
        }
    }

    public void chooseStartingUpCard() {
        setUpCard(draw());
        while (upCard().startsWith("W")) {
            discard(upCard());
            setUpCard(draw());
        }
    }

    public int randomPlayerIndex(int playerCount) {
        return random.nextInt(playerCount);
    }

    public void chooseRandomCurrentPlayer(int playerCount) {
        currentPlayer = randomPlayerIndex(playerCount);
    }

    public void advanceToNextPlayer() {
        next(playerCount());
    }

    public int currentPlayer() {
        return currentPlayer;
    }

    public int direction() {
        return direction;
    }

    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void resetTurnOrder() {
        direction = 1;
    }

    public void reverseDirection() {
        direction = direction * -1;
    }

    public void next(int playerCount) {
        currentPlayer += direction;
        if (currentPlayer >= playerCount) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerCount - 1;
        }
    }

    public TurnEffect applyCardEffect(String card) {
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

    public void clearDeck() {
        deck.clear();
    }

    public void addToDeck(String card) {
        deck.add(card);
    }

    public void shuffleDeck() {
        Collections.shuffle(deck, random);
    }

    public void clearDiscard() {
        discard.clear();
    }

    public void discard(String card) {
        discard.add(card);
    }

    public int deckSize() {
        return deck.size();
    }

    public String firstDeckCard() {
        return deck.getFirst();
    }

    public boolean isDiscardEmpty() {
        return discard.isEmpty();
    }

    public String draw() {
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

    public boolean isLegal(String card, String up, String call) {
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

    public boolean isLegalForCurrentState(String card) {
        return isLegal(card, upCard(), calledColor());
    }

    public boolean isWildCard(String card) {
        return card.equals("W") || card.equals("W4");
    }

    public int chooseBotCard(ArrayList<String> hand) {
        int chosen = chooseFirstLegalCardByRank(hand, "DRAW_TWO");
        if (chosen != -1) {
            return chosen;
        }
        chosen = chooseFirstLegalCardByRank(hand, "SKIP");
        if (chosen != -1) {
            return chosen;
        }
        chosen = chooseFirstLegalCardByRank(hand, "NUMBER");
        if (chosen != -1) {
            return chosen;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).startsWith("W")) {
                return i;
            }
        }
        return -1;
    }

    public int chooseFirstLegalCardByRank(ArrayList<String> hand, String targetRank) {
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (rank(card).equals(targetRank) && isLegalForCurrentState(card)) {
                return i;
            }
        }
        return -1;
    }

    public String chooseBotColor(ArrayList<String> hand) {
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

    public String color(String card) {
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

    public String rank(String card) {
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

    public int number(String card) {
        if (rank(card).equals("NUMBER")) {
            return Integer.parseInt(card.substring(1));
        }
        return -1;
    }

    public int points(String card) {
        String r = rank(card);
        return switch (r) {
            case "NUMBER" -> number(card);
            case "SKIP", "REVERSE", "DRAW_TWO" -> 20;
            case "WILD", "WILD_DRAW_FOUR" -> 50;
            default -> 0;
        };
    }
}
