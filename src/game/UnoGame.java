package game;

import java.util.ArrayList;
import java.util.List;
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

    public List<String> playerNamesSnapshot() {
        return players.namesSnapshot();
    }

    public int[] scoresSnapshot() {
        return players.scoresSnapshot();
    }

    void clearScores() {
        players.clearScores();
    }

    int score(int player) {
        return players.score(player);
    }

    void addScore(int player, int points) {
        players.addScore(player, points);
    }

    public int scoreCurrentPlayerFromOpponents() {
        return players.scoreFromOpponents(currentPlayer());
    }

    public String upCard() {
        return playArea.upCard();
    }

    void setUpCard(String upCard) {
        playArea.setUpCard(upCard);
    }

    public String calledColor() {
        return playArea.calledColor();
    }

    public void setCalledColor(String calledColor) {
        playArea.setCalledColor(calledColor);
    }

    void clearCalledColor() {
        playArea.clearCalledColor();
    }

    int playerCount() {
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

    ArrayList<String> hand(int player) {
        return players.hand(player);
    }

    ArrayList<String> currentHand() {
        return hand(currentPlayer());
    }

    public List<String> currentHandSnapshot() {
        return new ArrayList<>(currentHand());
    }

    public void setupCurrentHumanTurn(String upCard, List<String> hand) {
        setupPlayers(1, true);
        setCurrentPlayer(0);
        currentHand().clear();
        currentHand().addAll(hand);
        setUpCard(upCard);
        clearCalledColor();
    }

    int currentHandSize() {
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

    void clearHands() {
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

    void buildDeck() {
        drawPile.buildDeck();
    }

    void dealInitialHands() {
        for (int i = 0; i < playerCount(); i++) {
            for (int j = 0; j < 7; j++) {
                hand(i).add(draw());
            }
        }
    }

    void chooseStartingUpCard() {
        setUpCard(draw());
        while (upCard().startsWith("W")) {
            discard(upCard());
            setUpCard(draw());
        }
    }

    int randomPlayerIndex(int playerCount) {
        return random.nextInt(playerCount);
    }

    void chooseRandomCurrentPlayer(int playerCount) {
        turnOrder.setCurrentPlayer(randomPlayerIndex(playerCount));
    }

    public void advanceToNextPlayer() {
        next(playerCount());
    }

    int currentPlayer() {
        return turnOrder.currentPlayer();
    }

    int direction() {
        return turnOrder.direction();
    }

    void setCurrentPlayer(int currentPlayer) {
        turnOrder.setCurrentPlayer(currentPlayer);
    }

    void setDirection(int direction) {
        turnOrder.setDirection(direction);
    }

    void resetTurnOrder() {
        turnOrder.reset();
    }

    void reverseDirection() {
        turnOrder.reverseDirection();
    }

    void next(int playerCount) {
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

    void clearDeck() {
        drawPile.clearDeck();
    }

    void addToDeck(String card) {
        drawPile.addToDeck(card);
    }

    void shuffleDeck() {
        drawPile.shuffleDeck(random);
    }

    void clearDiscard() {
        drawPile.clearDiscard();
    }

    void discard(String card) {
        drawPile.discard(card);
    }

    int deckSize() {
        return drawPile.deckSize();
    }

    String firstDeckCard() {
        return drawPile.firstDeckCard();
    }

    boolean isDiscardEmpty() {
        return drawPile.isDiscardEmpty();
    }

    String draw() {
        return drawPile.draw(random);
    }

    boolean isLegal(String card, String up, String call) {
        return CardRules.isLegal(card, up, call);
    }

    public boolean isLegalForCurrentState(String card) {
        return isLegal(card, upCard(), calledColor());
    }

    public boolean isWildCard(String card) {
        return CardRules.isWildCard(card);
    }

    int chooseBotCard(List<String> hand) {
        return BotStrategy.chooseBotCard(hand, upCard(), calledColor());
    }

    String chooseBotColor(List<String> hand) {
        return BotStrategy.chooseBotColor(hand);
    }

    String color(String card) {
        return CardRules.color(card);
    }

    String rank(String card) {
        return CardRules.rank(card);
    }

    int number(String card) {
        return CardRules.number(card);
    }

    int points(String card) {
        return CardRules.points(card);
    }
}
