package game;

import java.util.ArrayList;
import java.util.Random;

public class UnoGame {

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

    private final DrawPile drawPile = new DrawPile();
    private final Players players = new Players();
    private final TurnOrder turnOrder = new TurnOrder();
    private final PlayArea playArea = new PlayArea();
    private Random random = new Random();

    public void seedRandom(long seed) {
        random = new Random(seed);
    }

    public boolean isPlayableColor(String color) {
        return CardRules.isPlayableColor(color);
    }

    public void setupPlayers(int bots, boolean human) {
        players.setup(bots, human);
    }

    public ArrayList<String> playerNamesSnapshot() {
        return players.namesSnapshot();
    }

    public int[] scoresSnapshot() {
        return players.scoresSnapshot();
    }

    public void clearScores() {
        players.clearScores();
    }

    public int score(int player) {
        return players.score(player);
    }

    public void addScore(int player, int points) {
        players.addScore(player, points);
    }

    public int scoreCurrentPlayerFromOpponents() {
        return players.scoreFromOpponents(currentPlayer());
    }

    public String upCard() {
        return playArea.upCard();
    }

    public void setUpCard(String upCard) {
        playArea.setUpCard(upCard);
    }

    public String calledColor() {
        return playArea.calledColor();
    }

    public void setCalledColor(String calledColor) {
        playArea.setCalledColor(calledColor);
    }

    public void clearCalledColor() {
        playArea.clearCalledColor();
    }

    public int playerCount() {
        return players.count();
    }

    public boolean isPlayerCountValid() {
        return playerCount() >= 2 && playerCount() <= 4;
    }

    public String currentPlayerName() {
        return players.name(currentPlayer());
    }

    public boolean isHumanCurrentPlayer() {
        return players.isHuman(currentPlayer());
    }

    public boolean shouldCurrentPlayerAutoPlayDrawnCard(String drawn) {
        return isLegalForCurrentState(drawn) && !isHumanCurrentPlayer();
    }

    public boolean shouldAskCurrentPlayerToPlayDrawnCard(String drawn) {
        return isLegalForCurrentState(drawn) && isHumanCurrentPlayer();
    }

    public ArrayList<String> hand(int player) {
        return players.hand(player);
    }

    public ArrayList<String> currentHand() {
        return hand(currentPlayer());
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
        players.clearHands();
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
        drawPile.buildDeck();
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
        turnOrder.setCurrentPlayer(randomPlayerIndex(playerCount));
    }

    public void advanceToNextPlayer() {
        next(playerCount());
    }

    public int currentPlayer() {
        return turnOrder.currentPlayer();
    }

    public int direction() {
        return turnOrder.direction();
    }

    public void setCurrentPlayer(int currentPlayer) {
        turnOrder.setCurrentPlayer(currentPlayer);
    }

    public void setDirection(int direction) {
        turnOrder.setDirection(direction);
    }

    public void resetTurnOrder() {
        turnOrder.reset();
    }

    public void reverseDirection() {
        turnOrder.reverseDirection();
    }

    public void next(int playerCount) {
        turnOrder.next(playerCount);
    }

    public TurnEffect applyCardEffect(String card) {
        return switch (CardRules.rankValue(card)) {
            case SKIP -> skipNextPlayer();
            case REVERSE -> reverseTurnOrder();
            case DRAW_TWO -> drawCardsAndSkip(2, EffectType.DRAW_TWO);
            case WILD_DRAW_FOUR -> drawCardsAndSkip(4, EffectType.DRAW_FOUR);
            default -> advanceNormally();
        };
    }

    private TurnEffect skipNextPlayer() {
        advanceToNextPlayer();
        advanceToNextPlayer();
        return noVisibleEffect();
    }

    private TurnEffect reverseTurnOrder() {
        reverseDirection();
        if (playerCount() == 2) {
            advanceToNextPlayer();
            advanceToNextPlayer();
        } else {
            advanceToNextPlayer();
        }
        return noVisibleEffect();
    }

    private TurnEffect drawCardsAndSkip(int cardCount, EffectType effectType) {
        advanceToNextPlayer();
        for (int i = 0; i < cardCount; i++) {
            currentHand().add(draw());
        }
        String penaltyPlayerName = currentPlayerName();
        advanceToNextPlayer();
        return new TurnEffect(effectType, penaltyPlayerName);
    }

    private TurnEffect advanceNormally() {
        advanceToNextPlayer();
        return noVisibleEffect();
    }

    private TurnEffect noVisibleEffect() {
        return new TurnEffect(EffectType.NONE, "");
    }

    public void clearDeck() {
        drawPile.clearDeck();
    }

    public void addToDeck(String card) {
        drawPile.addToDeck(card);
    }

    public void shuffleDeck() {
        drawPile.shuffleDeck(random);
    }

    public void clearDiscard() {
        drawPile.clearDiscard();
    }

    public void discard(String card) {
        drawPile.discard(card);
    }

    public int deckSize() {
        return drawPile.deckSize();
    }

    public String firstDeckCard() {
        return drawPile.firstDeckCard();
    }

    public boolean isDiscardEmpty() {
        return drawPile.isDiscardEmpty();
    }

    public String draw() {
        return drawPile.draw(random);
    }

    public boolean isLegal(String card, String up, String call) {
        return CardRules.isLegal(card, up, call);
    }

    public boolean isLegalForCurrentState(String card) {
        return isLegal(card, upCard(), calledColor());
    }

    public boolean isWildCard(String card) {
        return CardRules.isWildCard(card);
    }

    public int chooseBotCard(ArrayList<String> hand) {
        return BotStrategy.chooseBotCard(hand, upCard(), calledColor());
    }

    public String chooseBotColor(ArrayList<String> hand) {
        return BotStrategy.chooseBotColor(hand);
    }

    public String color(String card) {
        return CardRules.color(card);
    }

    public String rank(String card) {
        return CardRules.rank(card);
    }

    public int number(String card) {
        return CardRules.number(card);
    }

    public int points(String card) {
        return CardRules.points(card);
    }
}
