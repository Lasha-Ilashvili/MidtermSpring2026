package model;

enum CardRank {
    WILD,
    WILD_DRAW_FOUR,
    SKIP,
    REVERSE,
    DRAW_TWO,
    NUMBER;

    boolean isAction() {
        return this != NUMBER;
    }

    static CardRank fromCardCode(String cardCode) {
        if (cardCode.equals("W")) {
            return WILD;
        }
        if (cardCode.equals("W4")) {
            return WILD_DRAW_FOUR;
        }
        if (cardCode.endsWith("S")) {
            return SKIP;
        }
        if (cardCode.endsWith("R")) {
            return REVERSE;
        }
        if (cardCode.endsWith("+2")) {
            return DRAW_TWO;
        }
        return NUMBER;
    }
}
