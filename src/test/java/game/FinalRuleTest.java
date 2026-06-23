package game;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FinalRuleTest {

    @Test
    void deckContainsClassicUnoComposition() {
        UnoGame game = new UnoGame();
        game.buildDeck();

        Map<String, Integer> counts = drawAllCards(game);

        assertEquals(108, counts.values().stream().mapToInt(Integer::intValue).sum());
        for (String color : new String[]{"R", "Y", "G", "B"}) {
            assertEquals(1, counts.get(color + "0"));
            for (int number = 1; number <= 9; number++) {
                assertEquals(2, counts.get(color + number));
            }
            assertEquals(2, counts.get(color + "S"));
            assertEquals(2, counts.get(color + "R"));
            assertEquals(2, counts.get(color + "+2"));
        }
        assertEquals(4, counts.get("W"));
        assertEquals(4, counts.get("W4"));
    }

    @Test
    void legalPlayValidationMatchesFinalRulesReference() {
        UnoGame game = new UnoGame();

        assertAll(
                () -> assertTrue(game.isLegal("R2", "R9", ""), "matches by color"),
                () -> assertTrue(game.isLegal("G9", "R9", ""), "matches by number"),
                () -> assertTrue(game.isLegal("BS", "RS", ""), "matches skip action"),
                () -> assertTrue(game.isLegal("BR", "YR", ""), "matches reverse action"),
                () -> assertTrue(game.isLegal("R+2", "B+2", ""), "matches draw two action"),
                () -> assertTrue(game.isLegal("W", "B3", ""), "plain wild is legal"),
                () -> assertTrue(game.isLegal("W4", "B3", ""), "wild draw four is legal"),
                () -> assertTrue(game.isLegal("B3", "R9", "B"), "called color becomes active color"),
                () -> assertFalse(game.isLegal("B3", "R9", ""), "mismatched number/color is illegal"),
                () -> assertFalse(game.isLegal("WX", "B3", ""), "only W and W4 are wild cards")
        );
    }

    @Test
    void actionCardsApplyFinalRulesReferenceEffects() {
        UnoGame game = new UnoGame();

        game.setupPlayers(3, false);
        game.setCurrentPlayer(0);
        game.setDirection(1);
        game.applyCardEffect("RS");
        assertEquals(2, game.currentPlayer(), "skip advances past next player");

        game.setupPlayers(3, false);
        game.setCurrentPlayer(0);
        game.setDirection(1);
        game.applyCardEffect("RR");
        assertEquals(2, game.currentPlayer(), "reverse changes direction for three players");
        assertEquals(-1, game.direction());

        game.setupPlayers(1, true);
        game.setCurrentPlayer(0);
        game.setDirection(1);
        game.applyCardEffect("RR");
        assertEquals(0, game.currentPlayer(), "reverse acts like skip in two-player game");
        assertEquals(-1, game.direction());

        game.setupPlayers(3, false);
        game.setCurrentPlayer(0);
        game.setDirection(1);
        game.clearDeck();
        game.clearDiscard();
        game.addToDeck("R1");
        game.addToDeck("B2");
        UnoGame.TurnEffect drawTwo = game.applyCardEffect("R+2");
        assertEquals(2, game.hand(1).size(), "draw two gives next player two cards");
        assertEquals(2, game.currentPlayer(), "draw two skips penalized player");
        assertEquals(UnoGame.EffectType.DRAW_TWO, drawTwo.type());

        game.setupPlayers(3, false);
        game.setCurrentPlayer(0);
        game.setDirection(1);
        game.clearDeck();
        game.clearDiscard();
        game.addToDeck("R1");
        game.addToDeck("Y2");
        game.addToDeck("G3");
        game.addToDeck("B4");
        UnoGame.TurnEffect drawFour = game.applyCardEffect("W4");
        assertEquals(4, game.hand(1).size(), "wild draw four gives next player four cards");
        assertEquals(2, game.currentPlayer(), "wild draw four skips penalized player");
        assertEquals(UnoGame.EffectType.DRAW_FOUR, drawFour.type());
    }

    @Test
    void drawPassAndScoringRulesRemainInTheModel() {
        UnoGame game = new UnoGame();
        game.setupPlayers(2, false);
        game.setCurrentPlayer(0);
        game.setUpCard("R5");
        game.clearCalledColor();

        assertTrue(game.shouldCurrentPlayerAutoPlayDrawnCard("R9"), "bot may play legal drawn card");
        assertFalse(game.shouldCurrentPlayerAutoPlayDrawnCard("B3"), "bot passes illegal drawn card");

        int score = game.points("R5")
                + game.points("B9")
                + game.points("GS")
                + game.points("GR")
                + game.points("G+2")
                + game.points("W")
                + game.points("W4");
        assertEquals(174, score);
    }

    private Map<String, Integer> drawAllCards(UnoGame game) {
        Map<String, Integer> counts = new HashMap<>();
        int cards = game.deckSize();
        for (int i = 0; i < cards; i++) {
            counts.merge(game.draw(), 1, Integer::sum);
        }
        return counts;
    }
}
