package controller;

import java.util.ArrayList;
import model.UnoGame;

final class ModelCharacterizationTests {

    private final UnoGame game = new UnoGame();

    int run() {
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

    private int cardParsingTests() {
        int passed = 0;
        passed += SelfTestSupport.check(game.color("R5").equals("R"), "color R5");
        passed += SelfTestSupport.check(game.color("YS").equals("Y"), "color YS");
        passed += SelfTestSupport.check(game.color("G+2").equals("G"), "color G+2");
        passed += SelfTestSupport.check(game.color("BR").equals("B"), "color BR");
        passed += SelfTestSupport.check(game.color("W").isEmpty(), "wild has no printed color");
        passed += SelfTestSupport.check(game.rank("W").equals("WILD"), "rank wild");
        passed += SelfTestSupport.check(game.rank("W4").equals("WILD_DRAW_FOUR"), "rank wild draw four");
        passed += SelfTestSupport.check(game.rank("RS").equals("SKIP"), "rank skip");
        passed += SelfTestSupport.check(game.rank("BR").equals("REVERSE"), "rank reverse");
        passed += SelfTestSupport.check(game.rank("G+2").equals("DRAW_TWO"), "rank +2");
        passed += SelfTestSupport.check(game.rank("B7").equals("NUMBER"), "rank number");
        passed += SelfTestSupport.check(game.number("R0") == 0, "number zero");
        passed += SelfTestSupport.check(game.number("B9") == 9, "number nine");
        passed += SelfTestSupport.check(game.number("W") == -1, "wild has no number");
        passed += SelfTestSupport.check(game.number("R+2") == -1, "draw two has no number");
        return passed;
    }

    private int legalPlayTests() {
        int passed = 0;
        passed += SelfTestSupport.check(game.isLegal("R2", "R9", ""), "same color");
        passed += SelfTestSupport.check(game.isLegal("G9", "R9", ""), "same number");
        passed += SelfTestSupport.check(game.isLegal("BS", "RS", ""), "same skip action");
        passed += SelfTestSupport.check(game.isLegal("BR", "YR", ""), "same reverse action");
        passed += SelfTestSupport.check(game.isLegal("R+2", "B+2", ""), "same draw two action");
        passed += SelfTestSupport.check(game.isLegal("W", "B3", ""), "plain wild always legal");
        passed += SelfTestSupport.check(game.isLegal("W4", "B3", ""), "wild draw four always legal");
        passed += SelfTestSupport.check(game.isLegal("WX", "B3", ""), "wild prefix quirk is legal");
        passed += SelfTestSupport.check(game.isLegal("B3", "W", "B"), "called color after wild");
        passed += SelfTestSupport.check(game.isLegal("B3", "R9", "B"), "called color can beat up card color");
        passed += SelfTestSupport.check(!game.isLegal("B3", "R9", ""), "illegal mismatch");
        passed += SelfTestSupport.check(!game.isLegal("B3", "R+2", ""), "number does not match action");
        return passed;
    }

    private int botChoiceTests() {
        int passed = 0;
        game.setUpCard("R9");
        game.clearCalledColor();
        passed += SelfTestSupport.check(game.chooseBotCard(SelfTestSupport.cards("B3", "R4", "W")) == 1, "bot number before wild");
        passed += SelfTestSupport.check(game.chooseBotCard(SelfTestSupport.cards("R4", "R+2", "W")) == 1, "bot draw two priority");

        game.setUpCard("G9");
        passed += SelfTestSupport.check(game.chooseBotCard(SelfTestSupport.cards("G3", "GS", "W")) == 1, "bot skip priority");

        game.setUpCard("YR");
        game.clearCalledColor();
        passed += SelfTestSupport.check(game.chooseBotCard(SelfTestSupport.cards("BR", "W")) == 1, "bot reverse quirk before wild");

        game.setUpCard("W");
        game.setCalledColor("G");
        passed += SelfTestSupport.check(game.chooseBotCard(SelfTestSupport.cards("R1", "G3")) == 1, "bot uses called color");

        game.setUpCard("R9");
        game.clearCalledColor();
        passed += SelfTestSupport.check(game.chooseBotCard(SelfTestSupport.cards("B1", "G2")) == -1, "bot draws when no legal card");
        passed += SelfTestSupport.check(game.chooseBotColor(SelfTestSupport.cards("B1", "B2", "R3")).equals("B"), "bot color majority");
        passed += SelfTestSupport.check(game.chooseBotColor(SelfTestSupport.cards("R1", "Y2", "G3", "B4")).equals("R"), "bot color tie defaults red");
        return passed;
    }

    private int drawPileTests() {
        int passed = 0;
        game.seedRandom(7);
        game.clearDeck();
        game.clearDiscard();
        game.addToDeck("R1");
        game.addToDeck("B2");
        passed += SelfTestSupport.check(game.draw().equals("R1"), "draw removes top deck card");
        passed += SelfTestSupport.check(game.deckSize() == 1 && game.firstDeckCard().equals("B2"), "draw leaves remaining deck");

        game.clearDeck();
        game.clearDiscard();
        game.discard("G5");
        passed += SelfTestSupport.check(game.draw().equals("G5"), "empty deck refills from discard");
        passed += SelfTestSupport.check(game.isDiscardEmpty(), "discard cleared after refill");

        game.clearDeck();
        game.clearDiscard();
        passed += SelfTestSupport.check(game.draw().equals("W"), "empty draw and discard fallback");
        return passed;
    }

    private int drawnCardDecisionTests() {
        int passed = 0;
        game.setupPlayers(2, false);
        game.setCurrentPlayer(0);
        game.setUpCard("R5");
        game.clearCalledColor();
        ArrayList<String> botHand = game.hand(game.currentPlayer());
        int chosen = -1;
        String drawn = "R9";
        botHand.add(drawn);
        if (game.shouldCurrentPlayerAutoPlayDrawnCard(drawn)) {
            chosen = botHand.size() - 1;
        }
        passed += SelfTestSupport.check(chosen == 0, "bot auto plays legal drawn card");

        game.setupPlayers(2, false);
        game.setCurrentPlayer(0);
        game.setUpCard("R5");
        game.clearCalledColor();
        botHand = game.hand(game.currentPlayer());
        chosen = -1;
        drawn = "B3";
        botHand.add(drawn);
        if (game.shouldCurrentPlayerAutoPlayDrawnCard(drawn)) {
            chosen = botHand.size() - 1;
        }
        passed += SelfTestSupport.check(chosen == -1, "bot keeps illegal drawn card");

        game.setupPlayers(1, true);
        game.setCurrentPlayer(0);
        game.setUpCard("R5");
        game.clearCalledColor();
        ArrayList<String> humanHand = game.hand(game.currentPlayer());
        chosen = -1;
        drawn = "R9";
        humanHand.add(drawn);
        if (game.shouldCurrentPlayerAutoPlayDrawnCard(drawn)) {
            chosen = humanHand.size() - 1;
        }
        passed += SelfTestSupport.check(chosen == -1, "human does not auto play drawn card");
        return passed;
    }

    private int penaltyPathTests() {
        int passed = 0;
        game.setupPlayers(1, true);
        game.setCurrentPlayer(0);
        game.setDirection(1);
        game.setUpCard("R5");
        game.clearCalledColor();
        ArrayList<String> hand = game.hand(game.currentPlayer());
        hand.add("B3");
        game.clearDeck();
        game.clearDiscard();
        game.addToDeck("Y7");

        int chosen = 0;
        if (!game.isLegalForCurrentState(hand.get(chosen))) {
            game.drawPenaltyAndAdvanceCurrentPlayer();
        }
        passed += SelfTestSupport.check(hand.size() == 2 && hand.get(1).equals("Y7"), "illegal indexed card draws penalty card");
        passed += SelfTestSupport.check(game.currentPlayer() == 1, "illegal indexed card loses turn");

        game.setupPlayers(1, true);
        game.setCurrentPlayer(0);
        game.setDirection(1);
        game.clearDeck();
        game.clearDiscard();
        game.addToDeck("G4");
        hand = game.hand(game.currentPlayer());
        chosen = 3;
        if (chosen >= hand.size()) {
            game.drawPenaltyAndAdvanceCurrentPlayer();
        }
        passed += SelfTestSupport.check(hand.size() == 1 && hand.getFirst().equals("G4"), "out of range selected index draws penalty card");
        passed += SelfTestSupport.check(game.currentPlayer() == 1, "out of range selected index loses turn");
        return passed;
    }

    private int turnMovementTests() {
        int passed = 0;
        game.setupPlayers(3, false);
        game.setCurrentPlayer(0);
        game.setDirection(1);
        game.next(game.playerCount());
        passed += SelfTestSupport.check(game.currentPlayer() == 1, "next moves clockwise");

        game.setCurrentPlayer(2);
        game.setDirection(1);
        game.next(game.playerCount());
        passed += SelfTestSupport.check(game.currentPlayer() == 0, "next wraps clockwise");

        game.setCurrentPlayer(0);
        game.setDirection(-1);
        game.next(game.playerCount());
        passed += SelfTestSupport.check(game.currentPlayer() == 2, "next wraps counterclockwise");

        game.setCurrentPlayer(0);
        game.setDirection(1);
        game.next(game.playerCount());
        game.next(game.playerCount());
        passed += SelfTestSupport.check(game.currentPlayer() == 2, "skip advances over one player");

        game.setCurrentPlayer(0);
        game.setDirection(1);
        game.reverseDirection();
        game.next(game.playerCount());
        passed += SelfTestSupport.check(game.currentPlayer() == 2 && game.direction() == -1, "reverse changes direction with three players");

        game.setupPlayers(1, true);
        game.setCurrentPlayer(0);
        game.setDirection(1);
        game.reverseDirection();
        game.next(game.playerCount());
        game.next(game.playerCount());
        passed += SelfTestSupport.check(game.currentPlayer() == 0 && game.direction() == -1, "reverse skips other player with two players");

        game.setupPlayers(3, false);
        game.setCurrentPlayer(0);
        game.setDirection(1);
        game.clearDeck();
        game.clearDiscard();
        game.addToDeck("R1");
        game.addToDeck("B2");
        game.applyCardEffect("R+2");
        passed += SelfTestSupport.check(game.hand(1).size() == 2 && game.currentPlayer() == 2, "draw two draws and skips");

        game.setupPlayers(3, false);
        game.setCurrentPlayer(0);
        game.setDirection(1);
        game.clearDeck();
        game.clearDiscard();
        game.addToDeck("R1");
        game.addToDeck("Y2");
        game.addToDeck("G3");
        game.addToDeck("B4");
        game.applyCardEffect("W4");
        passed += SelfTestSupport.check(game.hand(1).size() == 4 && game.currentPlayer() == 2, "wild draw four draws and skips");
        return passed;
    }

    private int scoringTests() {
        int passed = 0;
        passed += SelfTestSupport.check(game.points("R0") == 0, "zero points");
        passed += SelfTestSupport.check(game.points("B9") == 9, "number points");
        passed += SelfTestSupport.check(game.points("GS") == 20, "skip points");
        passed += SelfTestSupport.check(game.points("GR") == 20, "reverse points");
        passed += SelfTestSupport.check(game.points("G+2") == 20, "draw two points");
        passed += SelfTestSupport.check(game.points("W") == 50, "wild points");
        passed += SelfTestSupport.check(game.points("W4") == 50, "wild draw four points");

        int total = 0;
        for (String card : SelfTestSupport.cards("R5", "B9", "GS", "W")) {
            total += game.points(card);
        }
        passed += SelfTestSupport.check(total == 84, "losing hand score example");
        return passed;
    }
}
