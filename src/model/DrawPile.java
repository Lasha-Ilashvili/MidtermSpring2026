package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

final class DrawPile {

    private final ArrayList<String> deck = new ArrayList<>();
    private final ArrayList<String> discard = new ArrayList<>();

    void buildDeck() {
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

    void clearDeck() {
        deck.clear();
    }

    void addToDeck(String card) {
        deck.add(card);
    }

    void shuffleDeck(Random random) {
        Collections.shuffle(deck, random);
    }

    void clearDiscard() {
        discard.clear();
    }

    void discard(String card) {
        discard.add(card);
    }

    int deckSize() {
        return deck.size();
    }

    String firstDeckCard() {
        return deck.getFirst();
    }

    boolean isDiscardEmpty() {
        return discard.isEmpty();
    }

    String draw(Random random) {
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
}
