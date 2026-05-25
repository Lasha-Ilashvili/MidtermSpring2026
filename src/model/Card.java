package model;

import java.util.Objects;

record Card(String code) {

    Card {
        Objects.requireNonNull(code);
    }

    static Card fromCode(String code) {
        return new Card(code);
    }

    CardColor color() {
        return CardColor.fromCardCode(code);
    }

    String colorCode() {
        return color().code();
    }

    CardRank rank() {
        return CardRank.fromCardCode(code);
    }

    boolean isWild() {
        return code.equals("W") || code.equals("W4");
    }

    boolean canMatchRank(Card other) {
        return rank() == other.rank() && rank().isAction();
    }

    boolean canMatchNumber(Card other) {
        return rank() == CardRank.NUMBER
                && other.rank() == CardRank.NUMBER
                && number() == other.number();
    }

    int number() {
        if (rank() == CardRank.NUMBER) {
            return Integer.parseInt(code.substring(1));
        }
        return -1;
    }

    int points() {
        return switch (rank()) {
            case NUMBER -> number();
            case SKIP, REVERSE, DRAW_TWO -> 20;
            case WILD, WILD_DRAW_FOUR -> 50;
        };
    }
}
